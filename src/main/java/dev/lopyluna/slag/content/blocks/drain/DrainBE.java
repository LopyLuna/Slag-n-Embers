package dev.lopyluna.slag.content.blocks.drain;

import dev.lopyluna.slag.compat.sable.SableCompat;
import dev.lopyluna.slag.config.SlagServerConfigs;
import dev.lopyluna.slag.content.AllUtils;
import dev.lopyluna.slag.content.blocks.crucible.CrucibleBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@SuppressWarnings("DataFlowIssue")
@ParametersAreNonnullByDefault
public class DrainBE extends BlockEntity {
    private static final int SCAN_RATE = 10;


    public DrainState drainState = DrainState.OFF;
    public FluidStack drainingFluid = FluidStack.EMPTY;
    public @Nullable BlockPos target;
    private double landing;
    private boolean moving;
    private int scanCooldown;
    private long renderTick = -1;
    private @Nullable Hit renderHit;
    public DrainBE(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }
    private @Nullable ResourceHandler<FluidResource> getFluidHandlerDir(Direction dir) {
        assert level != null;
        var pos = worldPosition.relative(dir);
        if (!level.isLoaded(pos)) return null;
        var be = level.getBlockEntity(pos);
        if (be == null) return null;
        return AllUtils.fluids(level, pos, dir.getOpposite());
    }

    public @Nullable ResourceHandler<FluidResource> getInputHandler() {
        return getFluidHandlerDir(getBlockState().getValue(DrainBlock.FACING).getOpposite());
    }
    public @Nullable ResourceHandler<FluidResource> getOutputHandler() {
        if (level == null || target == null || !level.isLoaded(target)) return null;
        return AllUtils.fluids(level, target, Direction.UP);
    }

    private void scan(ResourceHandler<FluidResource> input) {
        if (level == null) return;
        scanCooldown = SCAN_RATE;
        target = null;
        var hit = trace(level, worldPosition, SlagServerConfigs.DRAIN_RANGE.get());
        if (hit == null) return;
        var output = AllUtils.fluids(level, hit.pos(), Direction.UP);
        if (output == null || output == input) return;
        var behind = worldPosition.relative(getBlockState().getValue(DrainBlock.FACING).getOpposite());
        if (hit.pos().equals(behind)) return;
        target = hit.pos();
        landing = hit.world().y;
        moving = SableCompat.inSubLevel(level, worldPosition) || SableCompat.inSubLevel(level, target);
    }

    public @Nullable Hit renderHit() {
        if (level == null) return null;
        var tick = level.getGameTime();
        if (tick == renderTick) return renderHit;
        renderTick = tick;
        renderHit = trace(level, worldPosition, SlagServerConfigs.DRAIN_RANGE.get());
        return renderHit;
    }

    public record Hit(BlockPos pos, Vec3 world) {}

    public static @Nullable Hit trace(Level level, BlockPos pos, int range) {
        var from = SableCompat.toWorld(level, Vec3.atBottomCenterOf(pos));
        var to = from.add(0, -range, 0);
        var best = (Hit) null;
        var main = clip(level, from, to);
        if (main != null) best = new Hit(main.getBlockPos().immutable(), main.getLocation());
        for (var ray : SableCompat.localRays(level, from, to)) {
            var local = clip(level, ray.from(), ray.to());
            if (local == null) continue;
            var world = SableCompat.toWorld(level, local.getLocation());
            if (best == null || world.y > best.world().y) best = new Hit(local.getBlockPos().immutable(), world);
        }
        return best;
    }

    private static @Nullable BlockHitResult clip(Level level, Vec3 from, Vec3 to) {
        return BlockGetter.traverseBlocks(from, to, level, (l, p) -> {
            if (!l.isLoaded(p)) return null;
            var state = l.getBlockState(p);
            if (state.getBlock() instanceof DrainBlock) return null;
            var shape = state.getCollisionShape(l, p);
            return shape.isEmpty() ? null : shape.clip(from, to, p.immutable());
        }, _ -> null);
    }

    private void affect() {
        if (level == null) return;
        var from = SableCompat.toWorld(level, Vec3.atBottomCenterOf(worldPosition));
        var box = new AABB(from.x - 2 / 16d, landing, from.z - 2 / 16d, from.x + 2 / 16d, from.y + 10 / 16d, from.z + 2 / 16d);
        var type = drainingFluid.getFluidType();
        var hot = type.getTemperature() >= 1000;
        var milk = drainingFluid.is(NeoForgeMod.MILK);
        for (var living : level.getEntitiesOfClass(LivingEntity.class, box)) CrucibleBlock.applyFluid(level, living, hot, milk, type.canExtinguish(living));
    }

    public void checkPowered() {
        if (level == null) return;
        drainState = level.hasNeighborSignal(worldPosition) ? DrainState.POWERED : drainState == DrainState.POURING ? DrainState.POURING : DrainState.OFF;
    }

    public void cycleDrain() {
        if (drainState == DrainState.POWERED) return;
        drainState = drainState == DrainState.OFF ? DrainState.POURING : DrainState.OFF;
        if (level != null && !level.isClientSide()) update();
    }

    public void update() {
        setChanged();
        if (level == null) return;
        var state = getBlockState();
        level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
    }

    int random = 0;
    public void tick() {
        if (level == null || level.isClientSide()) return;
        if (drainState == DrainState.OFF) {
            if (level.getRandom().nextInt(6) == 0) random = Mth.clamp((level instanceof ServerLevel server ? server.getGameRules().get(GameRules.RANDOM_TICK_SPEED) : 3) * 2, 0, 128);
            if (random != 0 && level.getRandom().nextInt(129 - random) == 0) checkPowered();
        }

        var prevFluid = drainingFluid.copy();
        var prevState = drainState;
        var prevTarget = target;

        var drain = drain(drainState);
        if (drain != null && drain != drainState) drainState = drain;
        if (!drainingFluid.isEmpty() && target != null) affect();
        if (!FluidStack.isSameFluidSameComponents(prevFluid, drainingFluid) || prevState != drainState || !Objects.equals(prevTarget, target)) update();
    }

    public DrainState drain(DrainState cur) {
        drainingFluid = FluidStack.EMPTY;
        if (cur == DrainState.OFF) return null;
        var inputInv = getInputHandler();
        if (inputInv == null) return null;
        if (moving || --scanCooldown <= 0 || target == null) scan(inputInv);
        var outputInv = getOutputHandler();
        if (outputInv == null) return null;
        if (inputInv.size() <= 0 || outputInv.size() <= 0) return null;

        var inputFluid = FluidUtil.getStack(inputInv, 0);
        if (inputFluid.isEmpty()) return DrainState.OFF;
        var outputFluid = FluidUtil.getStack(outputInv, 0);
        var outputAmount = outputFluid.getAmount();
        var outputCapacity = outputInv.getCapacityAsInt(0, FluidResource.of(inputFluid));
        if (outputAmount >= outputCapacity) return cur == DrainState.POWERED ? cur : DrainState.OFF;
        var targetDrain = Mth.clamp(outputFluid.isEmpty() ? 1 : SlagServerConfigs.DRAIN_MB_SPEED.get(), 0, outputCapacity - outputAmount);
        if (targetDrain == 0) return cur == DrainState.POWERED ? cur : DrainState.OFF;


        if (!AllUtils.moveExact(inputInv, outputInv, inputFluid.copyWithAmount(Math.min(targetDrain, inputFluid.getAmount())), false)) return cur == DrainState.POWERED ? cur : DrainState.OFF;

        drainingFluid = inputFluid.copy();
        return cur;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        drainState = DrainState.fromString(input.getStringOr("state", ""));
        drainingFluid = input.read("Fluid", FluidStack.OPTIONAL_CODEC).orElse(FluidStack.EMPTY);
        target = input.getLong("Target").map(BlockPos::of).orElse(null);
        landing = input.getDoubleOr("Landing", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (drainState != null) output.putString("state", "" + drainState);
        if (!drainingFluid.isEmpty()) output.store("Fluid", FluidStack.OPTIONAL_CODEC, drainingFluid);
        if (target != null) output.putLong("Target", target.asLong());
        if (target != null) output.putDouble("Landing", landing);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @Nonnull CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
