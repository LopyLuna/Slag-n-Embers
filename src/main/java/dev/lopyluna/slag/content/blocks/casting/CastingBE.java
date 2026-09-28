package dev.lopyluna.slag.content.blocks.casting;

import dev.lopyluna.slag.content.AllUtils;
import dev.lopyluna.slag.content.blocks.multiblock.LerpedFloat;
import dev.lopyluna.slag.content.blocks.smart.BlockEntityBehaviour;
import dev.lopyluna.slag.content.blocks.smart.SmartBlockEntity;
import dev.lopyluna.slag.content.utils.ItemResult;
import dev.lopyluna.slag.register.AllDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public abstract class CastingBE extends SmartBlockEntity {
    public static final int RESULT = 0;
    private static final int SYNC_RATE = 8;

    public final CastingInventory itemInventory;
    public final CastingTank tankInventory;
    public LerpedFloat fluidLevel;
    public ItemStack preview = ItemStack.EMPTY;
    public int coolingTarget;
    public int coolingProgress;
    public boolean updateRecipe = true;
    public int luminosity;

    protected int maxLight = 15;
    protected float coolingRate = 0.5f;
    protected float chaseSpeed = 0.5f;
    protected float lazyChaseSpeed = 0.125f;
    protected Handler handler;

    private Fluid recipeFluid = Fluids.EMPTY;
    private Collection<RecipeHolder<?>> recipeSource;
    private boolean forceFluidLevelUpdate = true;
    private long lastCool = -1;
    private int syncCooldown;
    private boolean queuedSync;

    public CastingBE(BlockEntityType<?> type, BlockPos pos, BlockState state, int capacity) {
        super(type, pos, state);
        itemInventory = createInventory();
        tankInventory = new CastingTank(this, capacity);
    }

    protected abstract CastingInventory createInventory();

    protected abstract @Nullable Handler findHandler(Fluid fluid);

    public abstract boolean isCastItem(ItemStack stack);

    protected Handler handler(int capacity, int duration, float speed, ItemResult output, @Nullable CastItem item) {
        return new Handler(capacity, castingTicks(capacity, coolingRate, duration, speed), output, item);
    }

    public static int castingTicks(int capacity, float coolingRate, int duration, float speed) {
        var ticks = duration > 0 ? duration : Mth.clamp((int) (capacity * coolingRate), 4, 256);
        return Math.max(1, Math.round(ticks / Math.max(0.01f, speed)));
    }

    protected void onCast() {}

    protected void store(ItemStack stack) {
        if (itemInventory.getItem(RESULT).isEmpty()) itemInventory.setItem(RESULT, stack);
        else if (level != null) Block.popResourceFromFace(level, worldPosition, Direction.UP, stack);
    }

    public static <T extends CastingBE> void registerCapabilities(RegisterCapabilitiesEvent event, BlockEntityType<T> type) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, type, (be, context) -> be.tankInventory);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type, (be, context) -> be.itemInventory);
    }

    @Override
    public void tick() {
        super.tick();
        if (syncCooldown > 0 && --syncCooldown == 0 && queuedSync) sendData();
        if (level == null) return;
        updateLuminosity();
        if (level.isClientSide) {
            if (fluidLevel != null) fluidLevel.tickChaser();
            if (coolingProgress > 0 && coolingProgress < coolingTarget) coolingProgress++;
            return;
        }

        if (updateRecipe || recipeSource != level.getRecipeManager().getRecipes()) refreshRecipe();
        if (handler == null || tankInventory.getFluidAmount() < handler.capacity()) {
            if (coolingProgress == 0) return;
            coolingProgress = 0;
            sendDataImmediately();
            return;
        }

        if (lastCool == level.getGameTime()) return;
        lastCool = level.getGameTime();
        if (coolingProgress++ == 0) sendDataImmediately();
        if (coolingProgress < coolingTarget) return;
        coolingProgress = 0;
        var capacity = handler.capacity();
        var item = handler.item();
        var placed = itemInventory.getItem(RESULT);
        var result = handler.result().copy();
        if (item != null && item.imprint()) {
            var type = AllUtils.castType(placed);
            if (type != null) result.set(AllDataComponents.CAST_TYPE, type);
        }
        var leftover = item == null ? ItemStack.EMPTY : item.leftover(placed);
        itemInventory.setItem(RESULT, ItemStack.EMPTY);
        onCast();
        store(result);
        if (!leftover.isEmpty()) store(leftover);
        tankInventory.drain(capacity, IFluidHandler.FluidAction.EXECUTE);
        level.playSound(null, worldPosition, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 1F);
    }

    private void refreshRecipe() {
        if (level == null) return;
        updateRecipe = false;
        recipeSource = level.getRecipeManager().getRecipes();
        var fluid = tankInventory.getFluid();
        recipeFluid = fluid.getFluid();
        handler = fluid.isEmpty() ? null : findHandler(recipeFluid);
        if (handler != null) {
            tankInventory.setCapacity(handler.capacity());
            coolingTarget = handler.duration();
        }

        var preview = handler == null ? ItemStack.EMPTY : handler.result();
        if (this.preview == preview) return;
        this.preview = preview;
        sendData();
    }

    public @Nullable Handler getHandler(FluidStack resource) {
        if (level == null || level.isClientSide || resource.isEmpty()) return null;
        if (updateRecipe) refreshRecipe();
        return tankInventory.isEmpty() ? findHandler(resource.getFluid()) : handler;
    }

    protected void onFluidChanged(FluidStack stack) {
        if (level == null || level.isClientSide) return;
        if (stack.getFluid() != recipeFluid) updateRecipe = true;
        setChanged();
        sendData();
        startCooling();
    }

    private void startCooling() {
        if (level == null || coolingProgress > 0) return;
        if (updateRecipe) refreshRecipe();
        if (handler == null || tankInventory.getFluidAmount() < handler.capacity()) return;
        coolingProgress = 1;
        lastCool = level.getGameTime();
        sendDataImmediately();
    }

    public int getLuminosity() {
        return luminosity;
    }

    protected void updateLuminosity() {
        var light = 0;
        var fluid = tankInventory.getFluid();
        var capacity = tankInventory.getCapacity();
        if (!fluid.isEmpty() && capacity > 0) {
            var cooled = coolingTarget <= 0 ? 0f : Mth.clamp((float) coolingProgress / coolingTarget, 0f, 1f);
            var max = Math.min(maxLight, fluid.getFluidType().getLightLevel(fluid));
            light = Math.round(max * Mth.clamp(getFillState(), 0f, 1f) * (1 - cooled));
        }
        if (light == luminosity || level == null) return;
        luminosity = light;
        level.getChunkSource().getLightEngine().checkBlock(worldPosition);
    }

    private float getFillState() {
        return (float) tankInventory.getFluidAmount() / tankInventory.getCapacity();
    }

    @Override
    public void destroy() {
        super.destroy();
        if (level != null) Containers.dropContents(level, worldPosition, itemInventory);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        itemInventory.load(tag, registries);
        tankInventory.readFromNBT(registries, tag.getCompound("TankContent"));
        if (tag.contains("Capacity")) tankInventory.setCapacity(tag.getInt("Capacity"));
        updateRecipe = true;
        if (fluidLevel == null) fluidLevel = LerpedFloat.linear().startWithValue(getFillState());
        if (!clientPacket) return;

        preview = tag.contains("ResultPreview") ? ItemStack.parseOptional(registries, tag.getCompound("ResultPreview")) : ItemStack.EMPTY;
        coolingProgress = tag.getInt("CoolingProgress");
        coolingTarget = tag.getInt("CoolingTarget");

        var fill = getFillState();
        if (tag.contains("ForceFluidLevel")) fluidLevel = LerpedFloat.linear().startWithValue(fill);
        fluidLevel.chase(fill, chaseSpeed, LerpedFloat.Chaser.EXP);
        if (tag.contains("LazySync")) fluidLevel.chase(fluidLevel.getChaseTarget(), lazyChaseSpeed, LerpedFloat.Chaser.EXP);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("TankContent", tankInventory.writeToNBT(registries, new CompoundTag()));
        itemInventory.save(tag, registries);
        if (!clientPacket) return;

        tag.putInt("Capacity", tankInventory.getCapacity());
        if (!preview.isEmpty()) tag.put("ResultPreview", preview.save(registries, new CompoundTag()));
        tag.putInt("CoolingProgress", coolingProgress);
        tag.putInt("CoolingTarget", coolingTarget);
        if (forceFluidLevelUpdate) tag.putBoolean("ForceFluidLevel", true);
        if (queuedSync) tag.putBoolean("LazySync", true);
        forceFluidLevelUpdate = false;
    }

    @Override
    public void initialize() {
        super.initialize();
        sendData();
        if (level != null) level.getChunkSource().getLightEngine().checkBlock(worldPosition);
    }

    public void sendDataImmediately() {
        syncCooldown = 0;
        queuedSync = false;
        sendData();
    }

    @Override
    public void sendData() {
        if (syncCooldown > 0) {
            queuedSync = true;
            return;
        }
        super.sendData();
        queuedSync = false;
        syncCooldown = SYNC_RATE;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

    public record Handler(int capacity, int duration, ItemResult output, @Nullable CastItem item) {
        public ItemStack result() {
            return output.stack();
        }
    }
}
