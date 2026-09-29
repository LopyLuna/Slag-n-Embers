package dev.lopyluna.slag.content.blocks.multiblock;

import com.google.common.collect.ImmutableMap;
import dev.lopyluna.slag.config.SlagCommonConfigs;
import dev.lopyluna.slag.content.blocks.crucible.CrucibleTank;
import dev.lopyluna.slag.content.blocks.multiblock.MultiQueue.Area;
import dev.lopyluna.slag.content.blocks.multiblock.MultiQueue.Sweep;
import dev.lopyluna.slag.content.blocks.smart.BlockEntityBehaviour;
import dev.lopyluna.slag.content.blocks.smart.SmartBlockEntity;
import dev.lopyluna.slag.register.AllBETypes;
import it.unimi.dsi.fastutil.longs.LongList;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dev.lopyluna.slag.content.blocks.crucible.CrucibleBE.BOTTOM;
import static dev.lopyluna.slag.content.blocks.crucible.CrucibleBE.TOP;

@SuppressWarnings("unchecked")
public class FluidMultiBlockEntity extends SmartBlockEntity implements IMultiBlockEntityContainer.FluidMulti {

    public boolean forceFluidLevelUpdate;
    public CrucibleTank tankInventory;
    public BlockPos controller;
    public boolean isController = true;
    public BlockPos lastKnownPos;
    public boolean updateConnectivity;
    public boolean updateCapability;
    public boolean updateOutputSignal;
    public boolean window;
    public int luminosity;
    public int widthX;
    public int widthZ;
    public int height;

    private static final int SYNC_RATE = 8;
    public int syncCooldown;
    public boolean queuedSync;
    public boolean settling;
    public boolean held;
    public boolean pendingSync;
    private boolean partial;
    private int checkLayer;

    // For rendering purposes only
    public LerpedFloat fluidLevel;

    public FluidMultiBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        tankInventory = createInventory();
        forceFluidLevelUpdate = true;
        updateConnectivity = false;
        updateCapability = false;
        window = false;
        height = 1;
        widthX = 1;
        widthZ = 1;
        refreshCapability();
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, AllBETypes.CRUCIBLE.get(), (be, context) -> be.handlerForCapability());
    }

    protected CrucibleTank createInventory() {
        return new CrucibleTank(getCapacityMultiplier(), this::onFluidStackChanged);
    }

    public void updateConnectivity() {
        assert level != null;
        updateConnectivity = false;
        if (level.isClientSide) return;
        if (!isController) return;
        ConnectivityHandler.formMulti(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (syncCooldown > 0) {
            syncCooldown--;
            if (syncCooldown == 0 && queuedSync) sendData();
        }

        if (lastKnownPos == null) lastKnownPos = getBlockPos();
        else if (!lastKnownPos.equals(worldPosition)) {
            onPositionChanged();
            return;
        }

        if (updateCapability) {
            updateCapability = false;
            refreshCapability();
        }
        if (updateConnectivity) updateConnectivity();
        if (updateOutputSignal) updateOutputSignal();
        if (fluidLevel != null) fluidLevel.tickChaser();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level == null || level.isClientSide || updateConnectivity) return;
        if (!isController) {
            if (notIntact()) removeController();
            return;
        }
        if (getTotalSize() > 1 && brokenLayer()) ConnectivityHandler.splitMulti(this);
    }

    private boolean brokenLayer() {
        if (level == null) return false;
        if (checkLayer >= height) checkLayer = 0;
        var y = checkLayer++;
        for (var x = 0; x < widthX; x++) for (var z = 0; z < widthZ; z++) {
            var pos = worldPosition.offset(x, y, z);
            if (!level.isLoaded(pos)) continue;
            if (!(level.getBlockEntity(pos) instanceof FluidMultiBlockEntity part) || part.getType() != getType() || !part.getController().equals(worldPosition)) return true;
        }
        return false;
    }

    public boolean notIntact() {
        if (level == null) return true;
        var origin = getController();
        if (!level.isLoaded(origin)) return false;
        if (!(level.getBlockEntity(origin) instanceof FluidMultiBlockEntity ctrl) || ctrl.getType() != getType() || !ctrl.isController) return true;
        var local = worldPosition.subtract(origin);
        if (local.getX() < 0 || local.getY() < 0 || local.getZ() < 0 || local.getX() >= ctrl.widthX || local.getY() >= ctrl.height || local.getZ() >= ctrl.widthZ) return true;
        if (!isController) return false;
        for (var y = 0; y < ctrl.height; y++) for (var x = 0; x < ctrl.widthX; x++) for (var z = 0; z < ctrl.widthZ; z++) {
            var pos = origin.offset(x, y, z);
            if (!level.isLoaded(pos)) continue;
            if (!(level.getBlockEntity(pos) instanceof FluidMultiBlockEntity part) || part.getType() != getType() || !part.getController().equals(origin)) return true;
        }
        return false;
    }

    @Override
    public BlockPos getLastKnownPos() {
        return lastKnownPos;
    }

    @Override
    public boolean isController() {
        return isController;
    }

    @Override
    public void initialize() {
        super.initialize();
        assert level != null;
        sendData();
        level.getChunkSource().getLightEngine().checkBlock(worldPosition);
        if (level.isClientSide) invalidateRenderBoundingBox();
        else if (isController && !held) {
            if (settling) resettle();
            MultiQueue.connect(level, area(), false);
        }
    }

    public Area area() {
        return new Area(worldPosition, widthX, widthZ, height);
    }

    public void resettle() {
        var area = area();
        MultiQueue.settle(this, List.of(Sweep.shell(area)), List.of(Sweep.full(area)));
    }

    public int settle(BlockPos pos) {
        if (level == null) return 1;
        if (!level.isLoaded(pos)) {
            partial = true;
            return 1;
        }
        if (!(level.getBlockEntity(pos) instanceof FluidMultiBlockEntity part) || part.getType() != getType()) return 1;
        var at = part.getController();
        if (at.equals(worldPosition)) return isController && !isRemoved() ? apply(part, pos) : 1;
        if (!level.isLoaded(at) || !(level.getBlockEntity(at) instanceof FluidMultiBlockEntity owner) || owner.getType() != getType() || !owner.isController) return 1;
        return owner.apply(part, pos);
    }

    private int apply(FluidMultiBlockEntity part, BlockPos pos) {
        var cost = 1;
        if (level == null) return cost;
        var state = part.getBlockState();
        var settled = settledState(state, pos.getX() - worldPosition.getX(), pos.getY() - worldPosition.getY(), pos.getZ() - worldPosition.getZ());
        if (settled != state) {
            level.setBlock(pos, settled, 22);
            cost = MultiQueue.CHANGED;
        }
        var light = settledLight(pos.getY() - worldPosition.getY(), pos);
        if (part.luminosity != light) {
            part.luminosity = light;
            level.getChunkSource().getLightEngine().checkBlock(pos);
            part.pendingSync = true;
            cost = MultiQueue.CHANGED;
        }
        if (part.pendingSync) {
            part.pendingSync = false;
            part.sendData();
            level.blockEntityChanged(pos);
            cost = MultiQueue.CHANGED;
        }
        return cost;
    }

    public void settled() {
        settling = partial;
        partial = false;
        if (level != null) level.blockEntityChanged(worldPosition);
    }

    protected BlockState settledState(BlockState state, int x, int y, int z) {
        if (!state.hasProperty(BOTTOM) || !state.hasProperty(TOP)) return state;
        return state.setValue(BOTTOM, y == 0).setValue(TOP, y == height - 1);
    }

    protected int settledLight(int y, BlockPos pos) {
        return luminosity;
    }

    @Override
    public void formed(LongList fresh, List<Area> absorbed) {
        if (level == null || level.isClientSide) return;
        refreshCapability();
        var state = getBlockState();
        var settled = settledState(state, 0, 0, 0);
        if (settled != state) level.setBlock(worldPosition, settled, 22);
        onFluidStackChanged(tankInventory.getFluids());
        var area = area();
        var urgent = new ArrayList<Sweep>();
        urgent.add(Sweep.points(fresh));
        urgent.add(Sweep.shell(area));
        for (var old : absorbed) urgent.add(Sweep.shell(old));
        MultiQueue.settle(this, urgent, List.of());
        MultiQueue.connect(level, area, false);
    }

    protected void onPositionChanged() {
        lastKnownPos = worldPosition;
        if (notIntact()) {
            if (isController) ConnectivityHandler.splitMulti(this);
            else removeController();
        }
        setChanged();
    }

    protected void onFluidStackChanged(List<FluidStack> newFluids) {
        if (level == null) return;

        if (!level.isClientSide) {
            updateOutputSignal = true;
            setChanged();
            sendData();
        }
        if (isVirtual()) {
            if (fluidLevel == null) fluidLevel = LerpedFloat.linear().startWithValue(getFillState());
            fluidLevel.chase(getFillState(), 0.5f, LerpedFloat.Chaser.EXP);
        }
    }

    public void updateOutputSignal() {
        updateOutputSignal = false;
        if (level == null || !isController || !getBlockState().hasAnalogOutputSignal()) return;

        for (var y = 0; y < height; y++) for (var x = 0; x < widthX; x++) for (var z = 0; z < widthZ; z++) {
            if (y > 0 && y < height - 1 && x > 0 && x < widthX - 1 && z > 0 && z < widthZ - 1) continue;
            var pos = worldPosition.offset(x, y, z);
            var part = ConnectivityHandler.partAt(getType(), level, pos);
            if (part != null) level.updateNeighbourForOutputSignal(pos, part.getBlockState().getBlock());
        }
    }

    protected void setLuminosity(int luminosity) {
        if (level == null || level.isClientSide) return;
        if (this.luminosity == luminosity) return;
        this.luminosity = luminosity;
        level.getChunkSource().getLightEngine().checkBlock(worldPosition);
        sendData();
    }

    public void updateLuminosity() {
        if (level == null || level.isClientSide || !isController) return;
        var light = 0;
        for (var fluid : tankInventory.getFluids()) if (!fluid.isEmpty()) light = Math.max(light, fluid.getFluidType().getLightLevel(fluid));
        if (light == luminosity) return;
        setLuminosity(light);
        resettle();
    }

    @SuppressWarnings("unchecked")
    @Override
    public FluidMultiBlockEntity getControllerBE() {
        if (isController || level == null) return this;
        if (level.getBlockEntity(getController()) instanceof FluidMultiBlockEntity be) return be;
        return null;
    }

    public void applyFluidTankSize(int blocks) {
        tankInventory.setCapacity(capacityOf(blocks));
        tankInventory.trim();
        forceFluidLevelUpdate = true;
    }

    public static int capacityOf(long blocks) {
        return (int) Math.min(Integer.MAX_VALUE, blocks * getCapacityMultiplier());
    }

    @Override
    public void removeController() {
        if (level == null || level.isClientSide) return;
        updateConnectivity = true;
        controller = null;
        isController = true;
        widthX = 1;
        widthZ = 1;
        height = 1;
        checkLayer = 0;
        applyFluidTankSize(1);
        onFluidStackChanged(tankInventory.getFluids());
        settle(worldPosition);
        refreshCapability();
        setChanged();
        sendData();
    }

    @Override
    public void detachController() {
        controller = null;
        isController = true;
        updateConnectivity = false;
        widthX = 1;
        widthZ = 1;
        height = 1;
        checkLayer = 0;
        applyFluidTankSize(1);
    }

    @SuppressWarnings("unused")
    public void toggleWindows() {
        FluidMultiBlockEntity be = getControllerBE();
        if (be == null) return;
        be.setWindows(!be.window);
    }

    public void sendDataImmediately() {
        syncCooldown = 0;
        queuedSync = false;
        sendData();
    }

    @Override
    public void sendData() {
        if (!isController) {
            queuedSync = false;
            super.sendData();
            return;
        }
        if (syncCooldown > 0) {
            queuedSync = true;
            return;
        }
        super.sendData();
        queuedSync = false;
        syncCooldown = SYNC_RATE;
    }

    public void setWindows(boolean window) {
        this.window = window;
    }

    @Override
    public void setController(BlockPos controller) {
        if (level == null || (level.isClientSide && !isVirtual())) return;
        if (controller.equals(this.controller)) return;
        this.controller = controller;
        isController = controller.equals(worldPosition);
        pendingSync = true;
        refreshCapability();
        level.blockEntityChanged(worldPosition);
    }

    public void refreshCapability() {
        invalidateCapabilities();
    }

    private static final FluidTank NO_TANK = new FluidTank(0);

    protected final IFluidHandler controllerHandler = new IFluidHandler() {
        private IFluidHandler get() {
            return getControllerBE() instanceof FluidMultiBlockEntity be && be.isController ? be.tankInventory : NO_TANK;
        }
        @Override public int getTanks() { return get().getTanks(); }
        @Override public @Nonnull FluidStack getFluidInTank(int tank) { return get().getFluidInTank(tank); }
        @Override public int getTankCapacity(int tank) { return get().getTankCapacity(tank); }
        @Override public boolean isFluidValid(int tank, @Nonnull FluidStack stack) { return get().isFluidValid(tank, stack); }
        @Override public int fill(@Nonnull FluidStack resource, @Nonnull FluidAction action) { return get().fill(resource, action); }
        @Override public @Nonnull FluidStack drain(@Nonnull FluidStack resource, @Nonnull FluidAction action) { return get().drain(resource, action); }
        @Override public @Nonnull FluidStack drain(int maxDrain, @Nonnull FluidAction action) { return get().drain(maxDrain, action); }
    };

    protected IFluidHandler handlerForCapability() {
        return isController ? tankInventory : controllerHandler;
    }

    @Override
    public BlockPos getController() {
        return isController ? worldPosition : controller;
    }

    @Override
    protected AABB createRenderBoundingBox() {
        if (isController) return super.createRenderBoundingBox().expandTowards(widthX - 1, height - 1, widthZ - 1);
        else return super.createRenderBoundingBox();
    }

    @SuppressWarnings("unused")
    @Nullable
    public FluidMultiBlockEntity getOtherFluidMultiBlockEntity(Direction direction) {
        if (level == null) return null;
        if (level.getBlockEntity(worldPosition.relative(direction)) instanceof FluidMultiBlockEntity be) return be;
        return null;
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);

        BlockPos controllerBefore = controller;
        int prevWX = widthX;
        int prevWZ = widthZ;
        int prevHeight = height;
        int prevLum = luminosity;

        if (compound.contains("Uninitialized")) updateConnectivity = true;
        settling = compound.getBoolean("Settling");
        luminosity = compound.getInt("Luminosity");

        lastKnownPos = null;
        if (compound.contains("LastKnownPos")) lastKnownPos = NBTHelper.readBlockPos(compound, "LastKnownPos");

        controller = null;
        if (compound.contains("ControllerOffset")) controller = worldPosition.offset(NBTHelper.readBlockPos(compound, "ControllerOffset"));
        else if (compound.contains("Controller")) controller = NBTHelper.readBlockPos(compound, "Controller");
        isController = controller == null || controller.equals(worldPosition);

        if (isController) {
            window = compound.getBoolean("Window");
            widthX = Math.max(1, compound.getInt("WidthX"));
            widthZ = Math.max(1, compound.getInt("WidthZ"));
            height = Math.max(1, compound.getInt("Height"));
            tankInventory.setCapacity(capacityOf(getTotalSize()));

            tankInventory.readFromNBT(registries, compound.getCompound("TankContent"));
            if (tankInventory.getSpace() < 0) tankInventory.drain(-tankInventory.getSpace(), IFluidHandler.FluidAction.EXECUTE);
        }

        if (compound.contains("ForceFluidLevel") || fluidLevel == null) fluidLevel = LerpedFloat.linear().startWithValue(getFillState());

        updateCapability = true;

        if (!clientPacket) return;

        var wasController = controllerBefore == null || controllerBefore.equals(worldPosition);
        if (wasController != isController || (isController && (prevWX != widthX || prevWZ != widthZ || prevHeight != height))) {
            if (level != null) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 16);
            if (wasController != isController && level != null && level.getChunkAt(worldPosition) instanceof ChunkBlockEntities chunk) chunk.slag$touch();
            if (isController) tankInventory.setCapacity(capacityOf(getTotalSize()));
            invalidateRenderBoundingBox();
        }
        if (isController) {
            float fillState = getFillState();
            if (compound.contains("ForceFluidLevel") || fluidLevel == null) fluidLevel = LerpedFloat.linear().startWithValue(fillState);
            fluidLevel.chase(fillState, 0.5f, LerpedFloat.Chaser.EXP);
        }
        if (luminosity != prevLum && level != null) level.getChunkSource().getLightEngine().checkBlock(worldPosition);

        if (compound.contains("LazySync")) fluidLevel.chase(fluidLevel.getChaseTarget(), 0.125f, LerpedFloat.Chaser.EXP);
    }

    public float getFillState() {
        return (float) tankInventory.total / tankInventory.getCapacity();
    }

    @Override
    public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        if (updateConnectivity) compound.putBoolean("Uninitialized", true);
        if (lastKnownPos != null) compound.put("LastKnownPos", NbtUtils.writeBlockPos(lastKnownPos));
        if (!isController) compound.put("ControllerOffset", NbtUtils.writeBlockPos(controller.subtract(worldPosition)));
        if (isController) {
            if (settling) compound.putBoolean("Settling", true);
            compound.putBoolean("Window", window);
            compound.put("TankContent", tankInventory.writeToNBT(registries, new CompoundTag()));
            compound.putInt("WidthX", widthX);
            compound.putInt("WidthZ", widthZ);
            compound.putInt("Height", height);
        }
        compound.putInt("Luminosity", luminosity);
        super.write(compound, registries, clientPacket);

        if (!clientPacket) return;
        if (forceFluidLevelUpdate) compound.putBoolean("ForceFluidLevel", true);
        if (queuedSync) compound.putBoolean("LazySync", true);
        forceFluidLevelUpdate = false;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    public int getTotalSize() {
        return widthX * widthZ * height;
    }

    public int getTotalTankSize() {
        return capacityOf(getTotalSize());
    }

    public static int getCapacityMultiplier() {
        return SlagCommonConfigs.CAPACITY_PER_CRUCIBLE.get();
    }

    @SuppressWarnings("unused")
    public void setFluidLevel(LerpedFloat fluidLevel) {
        this.fluidLevel = fluidLevel;
    }

    @Override
    public void preventConnectivityUpdate() {
        updateConnectivity = false;
    }

    @Override
    public void notifyMultiUpdated() {
        if (level == null || level.isClientSide) return;
        if (isController) {
            formed(LongList.of(), List.of());
            return;
        }
        var ctrl = getControllerBE();
        if (ctrl != null && ctrl != this && ctrl.isController) ctrl.settle(worldPosition);
    }

    @Override
    public void setExtraData(@Nullable Object data) {
        if (data instanceof Boolean) window = (boolean) data;
    }

    @Override
    @Nullable
    public Object getExtraData() {
        return window;
    }

    @Override
    public Object modifyExtraData(Object data) {
        if (data instanceof Boolean windows) {
            windows |= window;
            return windows;
        }
        return data;
    }

    @Override
    public int getMaxLength(Direction.Axis longAxis, int width) {
        return longAxis == Direction.Axis.Y ? 6 : getMaxWidth();
    }

    @Override
    public int getMaxWidth() {
        return 4;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public int getWidthX() {
        return widthX;
    }
    @Override
    public int getWidthZ() {
        return widthZ;
    }

    @Override
    public void setWidthX(int width) {
        this.widthX = width;
    }

    @Override
    public void setWidthZ(int width) {
        this.widthZ = width;
    }

    @Override
    public boolean hasTank() {
        return true;
    }

    @Override
    public int getTankSize() {
        return getCapacityMultiplier();
    }

    @Override
    public void setTankSize(int blocks) {
        applyFluidTankSize(blocks);
    }

    @Override
    public IFluidTank getTank() {
        return tankInventory;
    }

    @Override
    public List<FluidStack> getFluids() {
        return tankInventory.getFluidCopy();
    }

    @Override
    public List<FluidStack> takeFluids() {
        if (tankInventory.total <= 0 && tankInventory.getFluids().isEmpty()) return List.of();
        var fluids = new ArrayList<>(tankInventory.getFluidCopy());
        if (!fluids.isEmpty()) tankInventory.clear();
        return fluids;
    }

    @Override
    public void giveFluids(List<FluidStack> fluids) {
        if (!fluids.isEmpty()) tankInventory.insert(fluids);
    }

    public interface ChunkBlockEntities {
        void slag$touch();

        ImmutableMap<BlockPos, BlockEntity> slag$rendered(Map<BlockPos, BlockEntity> all);
    }
}
