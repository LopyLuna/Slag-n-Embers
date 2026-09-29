package dev.lopyluna.slag.content.blocks.crucible;

import dev.lopyluna.slag.config.SlagServerConfigs;
import dev.lopyluna.slag.content.blocks.multiblock.FluidMultiBlockEntity;
import dev.lopyluna.slag.content.blocks.multiblock.MultiQueue;
import dev.lopyluna.slag.content.blocks.multiblock.MultiQueue.Area;
import dev.lopyluna.slag.content.blocks.multiblock.MultiQueue.Sweep;
import dev.lopyluna.slag.content.temperature.Temperatures;
import dev.lopyluna.slag.content.temperature.Temperatures.Heat;
import dev.lopyluna.slag.content.temperature.Temperatures.Tiers;
import dev.lopyluna.slag.register.AllBlocks;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.fluids.FluidStack;
import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressWarnings({"DataFlowIssue", "ConstantValue", "deprecation", "NullableProblems"})
public class CrucibleBE extends FluidMultiBlockEntity {
    public static final float CAP_HEIGHT = 1 / 16f;
    public static final float HULL_WIDTH = 0.1f / 16f + 1 / 128f;
    private static final double FLUID_INSET = 2 / 16d;
    public static final float PUDDLE_HEIGHT = 3.6f / 16f;

    public boolean hasCover = false;
    public Heat heat = Heat.NONE;
    public boolean updateHeat = true;
    private int heatVersion = -1;
    private final List<LivingEntity> freezing = new ArrayList<>();

    public final List<AABB> fluidBoxes = new ArrayList<>();
    public final List<FluidStack> fluidBoxStacks = new ArrayList<>();
    public boolean updateFluidBoxes = true;
    public boolean placed;
    private int[] lights = new int[0];
    private boolean near = true;
    private boolean lightsPending;
    private boolean relight = true;
    private boolean sealed;
    private long closed = -20;

    public CrucibleBE(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public void tick() {
        super.tick();
        if (!isController || level == null || level.isClientSide) return;
        if (!freezing.isEmpty()) freezeEntities();
        if (!updateHeat) return;
        updateHeat = false;
        heatVersion = Temperatures.version;
        var heat = Temperatures.average(level, worldPosition.below(), widthX, widthZ);
        if (heat == this.heat) return;
        this.heat = heat;
        freezing.clear();
        setChanged();
        sendData();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level == null) return;
        if (level.isClientSide) return;
        if (!isController) return;
        near = playerNear();
        var sealed = sealed();
        if (sealed != null) this.sealed = sealed;
        if (near && lightsPending) catchUpLights();
        updateLuminosity();
        if (heatVersion != Temperatures.version) updateHeat = true;
        heatEntities(level);

        if (tankInventory.tryAlloy(level, Math.max(1, height + Math.round(((10f - (widthZ + widthX)) / 10f) * 5f)), heat)) tankInventory.onContentsChanged();

        var cover = hasCover();
        if (cover == null) return;
        if (hasCover != cover) {
            hasCover = cover;
            setChanged();
        }

        if (!hasCover) tankInventory.noCoverTick();
    }

    private void freezeEntities() {
        for (var entity : freezing) if (entity.canFreeze() && isStandingOn(entity)) entity.setTicksFrozen(Math.min(entity.getTicksRequiredToFreeze() + 2, entity.getTicksFrozen() + 3));
    }

    private boolean isStandingOn(LivingEntity entity) {
        if (entity.isRemoved() || !entity.onGround()) return false;
        var on = entity.getOnPos();
        var x = on.getX() - worldPosition.getX();
        var y = on.getY() - worldPosition.getY();
        var z = on.getZ() - worldPosition.getZ();
        return x >= 0 && x < widthX && y >= 0 && y < height && z >= 0 && z < widthZ;
    }

    private void heatEntities(Level level) {
        freezing.clear();
        if (heat.tier == null || heat.tier == Tiers.COOL || heat.tier == Tiers.WARM) return;
        var x = worldPosition.getX();
        var y = worldPosition.getY();
        var z = worldPosition.getZ();
        var sources = level.damageSources();
        for (var entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(x, y, z, x + widthX, y + height + 0.25, z + widthZ))) {
            if (!isStandingOn(entity)) continue;
            switch (heat.tier) {
                case FROZEN -> {
                    if (entity.canFreeze()) entity.hurt(sources.freeze(), 1);
                }
                case FREEZING -> freezing.add(entity);
                case SMOLDERING -> {
                    if (!entity.isSteppingCarefully()) entity.hurt(sources.hotFloor(), 1);
                }
                case HEATED -> entity.hurt(sources.inFire(), 1);
                case BLAZING -> {
                    if (!entity.fireImmune()) entity.igniteForSeconds(8);
                    entity.hurt(sources.inFire(), 1);
                }
                case INFERNAL -> entity.lavaHurt();
                default -> {}
            }
        }
    }

    public Boolean hasCover() {
        for (int widthX = 0; widthX < getWidthX(); widthX++) for (int widthZ = 0; widthZ < getWidthZ(); widthZ++) {
            var pos = getBlockPos().offset(widthX, height, widthZ);
            if (!level.isLoaded(pos)) return null;
            if (!covered(pos)) return false;
        }
        return true;
    }

    private Boolean sealed() {
        if (window) return false;
        for (var x = 0; x < widthX; x++) for (var z = 0; z < widthZ; z++) {
            var pos = worldPosition.offset(x, height, z);
            if (!level.isLoaded(pos)) return null;
            if (!level.getBlockState(pos).isSolidRender(level, pos)) return false;
        }
        return true;
    }

    private boolean covered(BlockPos pos) {
        if (!level.isLoaded(pos)) return false;
        var state = level.getBlockState(pos);
        if (state.isAir()) return false;
        if (!state.isFaceSturdy(level, pos, Direction.DOWN)) return false;
        return !state.canBeReplaced();
    }

    @Override
    public void updateLuminosity() {
        if (level == null || level.isClientSide || !isController || isRemoved()) return;
        var fresh = lights.length != height;
        if (fresh) lights = new int[height];
        if (fresh && relight) {
            relight = false;
            lightsPending = true;
        }
        var layers = new ArrayList<Sweep>();
        for (var y = 0; y < height; y++) {
            var light = layerLight(y);
            if (!fresh && lights[y] == light) continue;
            lights[y] = light;
            if (fresh) continue;
            if (!near) {
                lightsPending = true;
                continue;
            }
            layers.add(Sweep.full(new Area(worldPosition.above(y), widthX, widthZ, y == 0 ? Math.min(2, height) : 1)));
        }
        if (!layers.isEmpty()) MultiQueue.settle(this, layers, List.of());
    }

    private void catchUpLights() {
        lightsPending = false;
        MultiQueue.settle(this, List.of(), List.of(Sweep.full(area())));
    }

    private boolean glowing() {
        for (var fluid : tankInventory.getFluids()) if (!fluid.isEmpty() && fluid.getFluidType().getLightLevel(fluid) > 0) return true;
        return false;
    }

    private boolean playerNear() {
        int range = SlagServerConfigs.CRUCIBLE_LIGHT_RANGE.get();
        int x = worldPosition.getX(), y = worldPosition.getY(), z = worldPosition.getZ();
        var box = new AABB(x, y, z, x + widthX, y + height, z + widthZ);
        for (var player : level.players()) if (box.distanceToSqr(player.position()) <= (double) range * range) return true;
        return false;
    }

    @Override
    protected int settledLight(int y, BlockPos pos) {
        return 0;
    }

    @Override
    protected BlockState settledState(BlockState state, int x, int y, int z) {
        return state.is(AllBlocks.CRUCIBLE) ? shaped(state, x, y, z, widthX, widthZ, height, window).setValue(LIGHT, emission(x, y, z)) : super.settledState(state, x, y, z);
    }

    private int emission(int x, int y, int z) {
        if (lights.length != height) updateLuminosity();
        if (y < 0 || y >= lights.length) return 0;
        if (widthX < 3 || widthZ < 3 || height < 2) return lights[y];
        if (y == 0 || x == 0 || z == 0 || x == widthX - 1 || z == widthZ - 1) return 0;
        return y == 1 ? Math.max(lights[0], lights[1]) : lights[y];
    }

    private int layerLight(int layer) {
        var total = tankInventory.total;
        var capacity = tankInventory.getCapacity();
        if (sealed || total <= 0 || capacity <= 0 || height <= 0) return 0;

        var filled = (double) total / capacity * height;
        var fill = Mth.clamp(filled - layer, 0, 1);
        if (fill <= 0) return 0;

        var light = 0;
        var bottom = 0d;
        for (var fluid : tankInventory.getFluids()) {
            if (fluid.isEmpty()) continue;
            var top = bottom + filled * fluid.getAmount() / total;
            if (top > layer && bottom < layer + fill) light = Math.max(light, fluid.getFluidType().getLightLevel(fluid));
            bottom = top;
        }
        return Mth.ceil(light * fill);
    }

    @Override
    protected void onFluidStackChanged(List<FluidStack> newFluids) {
        updateFluidBoxes = true;
        super.onFluidStackChanged(newFluids);
    }

    public void updateFluidBoxes() {
        updateFluidBoxes = false;
        fluidBoxes.clear();
        fluidBoxStacks.clear();
        var total = tankInventory.total;
        var capacity = tankInventory.getCapacity();
        if (total <= 0 || capacity <= 0) return;

        var totalHeight = height - 2 * CAP_HEIGHT - PUDDLE_HEIGHT;
        var filled = Mth.clamp(((float) total / capacity + 0.0225f / 16f) * totalHeight, 0f, totalHeight);
        var x = (double) worldPosition.getX();
        var z = (double) worldPosition.getZ();
        var bottom = (double) worldPosition.getY() + CAP_HEIGHT + PUDDLE_HEIGHT;
        for (var fluid : tankInventory.getFluids()) {
            if (fluid.isEmpty()) continue;
            var top = bottom + filled * fluid.getAmount() / total;
            fluidBoxes.add(new AABB(x + FLUID_INSET, bottom, z + FLUID_INSET, x + widthX - FLUID_INSET, top, z + widthZ - FLUID_INSET));
            fluidBoxStacks.add(fluid);
            bottom = top;
        }
    }

    @Override
    public void formed(LongList fresh, List<Area> absorbed) {
        lights = new int[0];
        relight = false;
        updateFluidBoxes = true;
        updateHeat = true;
        super.formed(fresh, absorbed);
        if (level != null && !level.isClientSide && (glowing() || fresh.isEmpty() || !absorbed.isEmpty())) MultiQueue.settle(this, List.of(), List.of(Sweep.full(area())));
    }

    @Override
    public void removeController() {
        lights = new int[0];
        freezing.clear();
        updateFluidBoxes = true;
        updateHeat = true;
        super.removeController();
    }

    @Override
    public void detachController() {
        super.detachController();
        lights = new int[0];
        freezing.clear();
        updateFluidBoxes = true;
        updateHeat = true;
    }

    @Override
    public @Nonnull ModelData getModelData() {
        if (hidden(getBlockState())) return ModelData.EMPTY;
        return getControllerBE() instanceof CrucibleBE ctrl ? ctrl.heat.modelData : ModelData.EMPTY;
    }

    @Override
    public void setBlockState(BlockState state) {
        var old = getBlockState();
        super.setBlockState(state);
        if (level == null) return;
        if (level.isClientSide && hidden(old) != hidden(state)) requestModelDataUpdate();
        if (old == state || old.getLightEmission() <= 0 || state.getLightEmission() <= 0) return;
        var light = level.getChunkSource().getLightEngine();
        for (var direction : Direction.values()) light.checkBlock(worldPosition.relative(direction));
    }

    private static boolean hidden(BlockState state) {
        return state.hasProperty(SHAPE) && state.getValue(SHAPE) == Shape.INNER && !state.getValue(BOTTOM);
    }

    private void refreshHeatVisuals() {
        if (level == null) return;
        var sweep = Sweep.shell(area());
        while (sweep.next()) {
            var pos = sweep.pos();
            if (!(level.getBlockEntity(pos) instanceof CrucibleBE be)) continue;
            be.requestModelDataUpdate();
            level.sendBlockUpdated(pos, be.getBlockState(), be.getBlockState(), 0);
        }
    }

    @Override
    public int getMaxLength(Direction.Axis longAxis, int width) {
        return longAxis == Direction.Axis.Y ? SlagServerConfigs.CRUCIBLE_MAX_HEIGHT.get() : getMaxWidth();
    }

    @Override
    public int getMaxWidth() {
        return SlagServerConfigs.CRUCIBLE_MAX_WIDTH.get();
    }

    @SuppressWarnings("unused")
    public boolean isSameController(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof CrucibleBE be && be.getController().equals(getController());
    }

    @Override
    public void setWindows(boolean window) {
        var was = this.window;
        super.setWindows(window);
        if (level == null) return;
        if (level.isClientSide) {
            if (was && !window) closed = level.getGameTime();
            predictWindows();
            return;
        }
        sealed = Boolean.TRUE.equals(sealed());
        lights = new int[0];
        updateLuminosity();
        MultiQueue.settle(this, List.of(Sweep.shell(area())), List.of(Sweep.full(area())));
    }

    public boolean windowVisible() {
        return window || level != null && level.getGameTime() - closed < 10;
    }

    private void predictWindows() {
        var area = area();
        if (area.shell() > MultiQueue.updates()) return;
        var sweep = Sweep.shell(area);
        while (sweep.next()) {
            var pos = sweep.pos();
            var state = level.getBlockState(pos);
            if (state.is(AllBlocks.CRUCIBLE) && state.getValue(WINDOW) != window) level.setBlock(pos, state.setValue(WINDOW, window), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        var controllerBefore = getController();
        var visualBefore = visual();
        var was = window;
        super.read(compound, registries, clientPacket);
        if (clientPacket && was && !window && level != null) closed = level.getGameTime();
        hasCover = compound.getBoolean("hasCover");
        updateFluidBoxes = true;
        heat = Heat.read(compound);
        if (!clientPacket) {
            updateHeat = true;
            return;
        }
        if (isController && visualBefore != heat.visual) refreshHeatVisuals();
        else if (!controllerBefore.equals(getController()) && visual() != visualBefore) {
            requestModelDataUpdate();
            if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 0);
        }
    }

    private int visual() {
        return getControllerBE() instanceof CrucibleBE ctrl ? ctrl.heat.visual : -1;
    }

    @Override
    public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.putBoolean("hasCover", hasCover);
        heat.write(compound);
    }

    public static BlockState shaped(BlockState state, int x, int y, int z, int widthX, int widthZ, int height, boolean window) {
        var inner = y > 0 && y < height - 1 && x > 0 && x < widthX - 1 && z > 0 && z < widthZ - 1;
        return state.setValue(BOTTOM, y == 0).setValue(TOP, y == height - 1).setValue(WINDOW, window && !inner).setValue(SHAPE, shapeAt(x, z, widthX, widthZ));
    }

    public static boolean ticks(BlockState state) {
        if (!state.getValue(BOTTOM)) return false;
        var shape = state.getValue(SHAPE);
        return shape == Shape.NW || shape == Shape.PLAIN;
    }

    public static Shape shapeAt(int xOffset, int zOffset, int widthX, int widthZ) {
        if (widthX == 1 || widthZ == 1) return Shape.PLAIN;

        boolean isNorth = zOffset == 0;
        boolean isSouth = zOffset == widthZ - 1;
        boolean isWest = xOffset == 0;
        boolean isEast = xOffset == widthX - 1;

        int edgeCount = (isNorth ? 1 : 0) + (isSouth ? 1 : 0) + (isWest ? 1 : 0) + (isEast ? 1 : 0);

        if (edgeCount == 2) {
            if (isNorth && isWest) return Shape.NW;
            if (isNorth && isEast) return Shape.NE;
            if (isSouth && isWest) return Shape.SW;
            if (isSouth && isEast) return Shape.SE;
        }

        if (edgeCount == 1) {
            if (isNorth) return Shape.NORTH;
            if (isSouth) return Shape.SOUTH;
            if (isWest) return Shape.WEST;
            if (isEast) return Shape.EAST;
        }

        return Shape.INNER;
    }

    public static final BooleanProperty TOP = BooleanProperty.create("top");
    public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");
    public static final BooleanProperty WINDOW = BooleanProperty.create("window");
    public static final EnumProperty<Shape> SHAPE = EnumProperty.create("shape", Shape.class);
    public static final IntegerProperty LIGHT = IntegerProperty.create("light", 0, 15);

    @SuppressWarnings("unused")
    public enum Shape implements StringRepresentable {
        PLAIN,
        INNER,
        NW, SW, NE, SE,
        NORTH, SOUTH, WEST, EAST;

        @Override
        public @Nonnull String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public static Shape fromDir(Direction direction) {
            return switch (direction) {
                case DOWN, UP -> PLAIN;
                case NORTH -> NORTH;
                case SOUTH -> SOUTH;
                case WEST -> WEST;
                case EAST -> EAST;
            };
        }
        public static Shape fromDirDir(Direction dir1, Direction dir2) {
            return switch (dir1) {
                case NORTH -> switch (dir2) {
                    case NORTH -> NORTH;
                    case WEST -> NW;
                    case EAST -> NE;
                    default -> PLAIN;
                };
                case SOUTH -> switch (dir2) {
                    case SOUTH -> SOUTH;
                    case WEST -> SW;
                    case EAST -> SE;
                    default -> PLAIN;
                };
                case WEST -> switch (dir2) {
                    case NORTH -> NW;
                    case SOUTH -> SW;
                    case WEST -> WEST;
                    default -> PLAIN;
                };
                case EAST -> switch (dir2) {
                    case NORTH -> NE;
                    case SOUTH -> SE;
                    case EAST -> EAST;
                    default -> PLAIN;
                };
                default -> PLAIN;
            };
        }

        public boolean isWall() {
            return this.equals(NORTH) || this.equals(SOUTH) || this.equals(WEST) || this.equals(EAST);
        }
        public boolean isCorner() {
            return this.equals(NW) || this.equals(SW) || this.equals(NE) || this.equals(SE);
        }

        public Direction toDirection() {
            return switch (this) {
                case NE, NORTH -> Direction.NORTH;
                case NW, WEST -> Direction.WEST;
                case SW, SOUTH -> Direction.SOUTH;
                case SE, EAST -> Direction.EAST;
                default -> Direction.DOWN;
            };
        }
    }
}
