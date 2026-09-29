package dev.lopyluna.slag.content.blocks.melter;

import dev.lopyluna.slag.content.blocks.crucible.CrucibleTank;
import dev.lopyluna.slag.content.blocks.melter.client.MelterMenu;
import dev.lopyluna.slag.content.blocks.multiblock.LerpedFloat;
import dev.lopyluna.slag.content.blocks.smart.BlockEntityBehaviour;
import dev.lopyluna.slag.content.blocks.smart.SmartBlockEntity;
import dev.lopyluna.slag.content.temperature.Temperatures;
import dev.lopyluna.slag.content.temperature.Temperatures.Heat;
import dev.lopyluna.slag.register.AllBETypes;
import dev.lopyluna.slag.register.AllRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "deprecation", "NullableProblems"})
public class MelterBE extends SmartBlockEntity implements MenuProvider {
    public static final int BLOCK_SIZE = 648; //BLOCKS | 1:1
    public static final int SMALL_BLOCK_SIZE = 288; //SMALL/CRYSTAL BLOCKS | 1:2.25
    public static final int CRYSTAL_SIZE = 162; //PEARL/BALL/CRYSTALS | 1:4
    public static final int INGOT_SIZE = 72; //INGOT/GEM | 1:9
    public static final int SHARD_SIZE = 18; //GEM NUGGET | 1:36
    public static final int NUGGET_SIZE = 8; //INGOT NUGGET | 1:81
    public static final int CAPACITY = 4000;

    private final RecipeManager.CachedCheck<SingleRecipeInput, MeltingRecipe> quickCheck;

    protected IItemHandler itemCapability;
    protected IFluidHandler fluidCapability;
    protected boolean forceFluidLevelUpdate;
    protected MelterInventory itemInventory;
    protected CrucibleTank tankInventory;
    protected boolean updateCapability;
    protected int luminosity;

    protected BlockCapabilityCache<IFluidHandler, Direction> behind;
    protected BlockCapabilityCache<IFluidHandler, Direction> below;
    protected boolean updateBehind;
    protected boolean behindMelter;
    public Heat heat = Heat.NONE;
    private int heatVersion = -1;

    private MeltingRecipe lastRecipe;
    private MeltingRecipe meltingRecipe;
    private int meltingTotal;
    private float speed;

    private static final int SYNC_RATE = 8;
    protected int syncCooldown;
    protected boolean queuedSync;

    // For rendering purposes only
    private LerpedFloat fluidLevel;

    public int meltingTarget;
    public float meltingProgress;
    public boolean melting;
    public final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> (int) meltingProgress;
                case 1 -> meltingTarget;
                default -> melting ? 1 : 0;
            };
        }

        @Override
        public void set(int index, int value) {}

        @Override
        public int getCount() {
            return 3;
        }
    };

    public MelterBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.quickCheck = RecipeManager.createCheck(AllRecipes.MELTING.get());
        itemInventory = new MelterInventory(1, this);
        tankInventory = createInventory();
        forceFluidLevelUpdate = true;

        updateCapability = false;
        refreshCapability();
    }

    public int getLuminosity() {
        return luminosity;
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, AllBETypes.MELTER.get(), (be, context) -> {
            if (be.fluidCapability == null) be.refreshCapability();
            return be.fluidCapability;
        });
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, AllBETypes.MELTER.get(), (be, context) -> {
            if (be.itemCapability == null) be.refreshCapability();
            return be.itemCapability;
        });
    }

    @Override
    public void tick() {
        super.tick();
        if (syncCooldown > 0) {
            syncCooldown--;
            if (syncCooldown == 0 && queuedSync) sendData();
        }

        if (updateCapability) {
            updateCapability = false;
            refreshCapability();
        }

        if (fluidLevel != null) fluidLevel.tickChaser();

        if (level == null || (level.isClientSide && !isVirtual())) return;

        melting = tickRecipe(level);

        if (!(level instanceof ServerLevel server)) return;

        if (behind == null) {
            var facing = getBlockState().getValue(MelterBlock.FACING);
            var pos = worldPosition.relative(facing.getOpposite());
            behind = BlockCapabilityCache.create(Capabilities.FluidHandler.BLOCK, server, pos, facing, () -> !isRemoved(), () -> updateBehind = true);
            below = BlockCapabilityCache.create(Capabilities.FluidHandler.BLOCK, server, pos.below(), Direction.UP, () -> !isRemoved(), () -> {});
            updateBehind = true;
        }
        if (updateBehind) {
            updateBehind = false;
            var pos = behind.pos();
            behindMelter = server.isLoaded(pos) && server.getBlockEntity(pos) instanceof MelterBE;
        }

        if (!behindMelter && pour(behind.getCapability())) return;
        pour(below.getCapability());
    }

    @Override
    public void setBlockState(BlockState state) {
        super.setBlockState(state);
        behind = null;
        below = null;
    }

    private boolean pour(@Nullable IFluidHandler output) {
        if (output == null) return false;
        for (var fluid : tankInventory.getFluids()) {
            if (fluid.isEmpty()) continue;
            var accepted = output.fill(fluid.copyWithAmount(Math.min(10, fluid.getAmount())), IFluidHandler.FluidAction.SIMULATE);
            if (accepted <= 0) continue;
            output.fill(tankInventory.drain(fluid.copyWithAmount(accepted), IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
            return true;
        }
        return false;
    }

    public static int meltingTicks(int total, int duration, float speed) {
        var ticks = duration > 0 ? duration : Mth.clamp((int) ((float) total * 0.5f), 4, 256);
        return Math.max(1, Math.round(ticks / Math.max(0.01f, speed)));
    }

    public boolean tickRecipe(Level level) {
        var stack = getStack();
        if (stack.isEmpty()) {
            meltingTarget = 0;
            meltingProgress = 0;
            return false;
        }
        if (heat.tier == null) {
            notMelting();
            return false;
        }

        var input = new SingleRecipeInput(stack);
        var recipeholder = quickCheck.getRecipeFor(input, level).orElse(null);
        var tank = getTankInventory();
        if (tank != null && recipeholder != null && recipeholder.value() instanceof MeltingRecipe recipe) {
            if (meltingRecipe != null && recipe != meltingRecipe) meltingProgress = 0;
            meltingRecipe = recipe;
            if (recipe != lastRecipe) {
                lastRecipe = recipe;
                meltingTotal = getTotalAmount(recipe.getResultFluids(level.registryAccess()));
                speed = heat.speed(recipe.temperature, recipe.heatType);
            }
            if (speed <= 0) {
                notMelting();
                return false;
            }
            meltingTarget = meltingTicks(meltingTotal, recipe.duration, recipe.speed);
            if (meltingTarget > meltingProgress) {
                meltingProgress += speed;
                return true;
            }

            var multiplier = stack.isDamaged() ? 1f - ((float) stack.getDamageValue() / (float) stack.getMaxDamage()) : 1f;
            var toFill = new ArrayList<FluidStack>();
            var filling = 0;
            for (var fluid : recipe.getResultFluids(level.registryAccess())) {
                var fill = new FluidStack(fluid.getFluidHolder(), Math.round((float) fluid.getAmount() * multiplier));
                filling += fill.getAmount();
                toFill.add(fill);
            }
            if (filling > tank.getSpace()) return true;
            meltingProgress = 0;
            stack.shrink(1);
            for (var fluid : toFill) tank.fill(fluid, IFluidHandler.FluidAction.EXECUTE);
            return true;
        }
        notMelting();
        return false;
    }

    public static int getTotalAmount(List<FluidStack> fluids) {
        return fluids.stream().mapToInt(FluidStack::getAmount).sum();
    }

    public void notMelting() {
        if (meltingProgress > 0) meltingProgress = Math.max(0, meltingProgress - 1);
    }

    public void updateHeat() {
        if (level == null || (level.isClientSide && !isVirtual())) return;
        heatVersion = Temperatures.version;
        var heat = Temperatures.get(level.getBlockState(worldPosition.below()));
        if (heat == this.heat) return;
        this.heat = heat;
        lastRecipe = null;
        setChanged();
        sendData();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (heatVersion != Temperatures.version) updateHeat();
    }

    protected CrucibleTank createInventory() {
        var tank = new CrucibleTank(CAPACITY, this::onFluidStackChanged);
        tank.sortByAmount = true;
        return tank;
    }

    public void refreshCapability() {
        fluidCapability = handlerForCapability();
        itemCapability = handlerForCapabilityItem();
        invalidateCapabilities();
    }

    private IItemHandler handlerForCapabilityItem() {
        return itemInventory;
    }

    private IFluidHandler handlerForCapability() {
        return tankInventory;
    }

    protected void onFluidStackChanged(List<FluidStack> newFluids) {
        if (level == null) return;

        level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());

        if (!level.isClientSide) {
            updateLuminosity();
            setChanged();
            sendData();
        } else {
            if (fluidLevel == null) fluidLevel = LerpedFloat.linear().startWithValue(getFillState());
            fluidLevel.chase(getFillState(), 0.5f, LerpedFloat.Chaser.EXP);
        }
    }

    protected void setLuminosity(int luminosity) {
        assert level != null;
        if (level.isClientSide) return;
        if (this.luminosity == luminosity) return;
        this.luminosity = luminosity;
        level.getChunkSource().getLightEngine().checkBlock(worldPosition);
        sendData();
    }

    protected void updateLuminosity() {
        var light = 0;
        for (var fluid : tankInventory.getFluids()) if (!fluid.isEmpty()) light = Math.max(light, fluid.getFluidType().getLightLevel(fluid));
        setLuminosity(light);
    }

    public float getFillState() {
        return (float) tankInventory.total / tankInventory.getCapacity();
    }

    @Override
    public void destroy() {
        super.destroy();
        dropContents(level, worldPosition, itemInventory);
    }

    public static void dropContents(Level level, BlockPos pos, IItemHandler inv) {
        for (int slot = 0; slot < inv.getSlots(); slot++) Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), inv.getStackInSlot(slot));
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        var visual = heat.visual;
        heat = Heat.read(tag);
        meltingProgress = tag.getFloat("MeltingProgress");
        melting = tag.getBoolean("Melting");
        itemInventory.load(tag, registries);
        int prevLum = luminosity;
        luminosity = tag.getInt("Luminosity");

        tankInventory.setCapacity(4000);

        var content = tag.getCompound("TankContent");
        tankInventory.readFromNBT(registries, content);
        if (content.contains("Fluid")) tankInventory.fill(FluidStack.parseOptional(registries, content.getCompound("Fluid")), IFluidHandler.FluidAction.EXECUTE);

        if (tag.contains("ForceFluidLevel") || fluidLevel == null) fluidLevel = LerpedFloat.linear().startWithValue(getFillState());

        updateCapability = true;

        if (!clientPacket) return;

        if (visual != heat.visual && level != null) {
            requestModelDataUpdate();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 0);
        }

        float fillState = getFillState();
        if (tag.contains("ForceFluidLevel") || fluidLevel == null) fluidLevel = LerpedFloat.linear().startWithValue(fillState);
        fluidLevel.chase(fillState, 0.5f, LerpedFloat.Chaser.EXP);

        if (luminosity != prevLum && level != null) level.getChunkSource().getLightEngine().checkBlock(worldPosition);

        if (tag.contains("LazySync")) fluidLevel.chase(fluidLevel.getChaseTarget(), 0.125f, LerpedFloat.Chaser.EXP);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        tag.put("TankContent", tankInventory.writeToNBT(registries, new CompoundTag()));
        tag.putInt("Luminosity", luminosity);
        super.write(tag, registries, clientPacket);
        heat.write(tag);
        tag.putFloat("MeltingProgress", meltingProgress);
        tag.putBoolean("Melting", melting);
        itemInventory.save(tag, registries);
        if (!clientPacket) return;
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

    @Override
    public void invalidate() {
        if (itemInventory != null || fluidCapability != null) invalidateCapabilities();
        super.invalidate();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        itemInventory.setChanged();
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
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    public ItemStack getStack() {
        return getItemInventory().getFirstItem();
    }

    public MelterInventory getItemInventory() {
        return itemInventory;
    }

    public CrucibleTank getTankInventory() {
        return tankInventory;
    }

    public LerpedFloat getFluidLevel() {
        return fluidLevel;
    }

    @SuppressWarnings("unused")
    public void setFluidLevel(LerpedFloat fluidLevel) {
        this.fluidLevel = fluidLevel;
    }

    @Override
    public @Nonnull ModelData getModelData() {
        return heat.modelData;
    }

    @Override
    public @Nonnull Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, @Nonnull Inventory inventory, @Nonnull Player player) {
        if (itemInventory == null) itemInventory = new MelterInventory(1, this);
        return new MelterMenu(i, inventory, itemInventory, worldPosition, data);
    }

    @Override
    public void writeClientSideData(@Nonnull AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(worldPosition);
    }
}
