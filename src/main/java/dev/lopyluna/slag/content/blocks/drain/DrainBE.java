package dev.lopyluna.slag.content.blocks.drain;

import dev.lopyluna.slag.compat.sable.SableCompat;
import dev.lopyluna.slag.config.SlagServerConfigs;
import dev.lopyluna.slag.content.blocks.crucible.CrucibleBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.checkerframework.checker.nullness.qual.NonNull;

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
    private IFluidHandler getFluidHandlerDir(Direction dir) {
        assert level != null;
        var pos = worldPosition.relative(dir);
        if (!level.isLoaded(pos)) return null;
        var be = level.getBlockEntity(pos);
        if (be == null) return null;
        return level.getCapability(Capabilities.FluidHandler.BLOCK, pos, dir.getOpposite());
    }

    public IFluidHandler getInputHandler() {
        return getFluidHandlerDir(getBlockState().getValue(DrainBlock.FACING).getOpposite());
    }
    public @Nullable IFluidHandler getOutputHandler() {
        if (level == null || target == null || !level.isLoaded(target)) return null;
        return level.getCapability(Capabilities.FluidHandler.BLOCK, target, Direction.UP);
    }

    private void scan(IFluidHandler input) {
        if (level == null) return;
        scanCooldown = SCAN_RATE;
        target = null;
        var hit = trace(level, worldPosition, SlagServerConfigs.DRAIN_RANGE.get());
        if (hit == null) return;
        var output = level.getCapability(Capabilities.FluidHandler.BLOCK, hit.pos(), Direction.UP);
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
        }, l -> null);
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
        if (drainState != DrainState.POWERED) drainState = drainState == DrainState.OFF ? DrainState.POURING : DrainState.OFF;
    }

    public void update() {
        setChanged();
        if (level == null) return;
        var state = getBlockState();
        level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
    }

    int random = 0;
    public void tick() {
        if (level == null || level.isClientSide) return;
        if (drainState == DrainState.OFF) {
            if (level.random.nextInt(6) == 0) random = Mth.clamp(level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).getCommandResult() * 2, 0, 128);
            if (random != 0 && level.random.nextInt(129 - random) == 0) checkPowered();
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
        if (inputInv.getTanks() <= 0 || outputInv.getTanks() <= 0) return null;

        var inputFluid = inputInv.getFluidInTank(0);
        if (inputFluid.isEmpty()) return DrainState.OFF;
        var outputFluid = outputInv.getFluidInTank(0);
        var outputAmount = outputFluid.getAmount();
        var outputCapacity = outputInv.getTankCapacity(0);
        if (outputAmount >= outputCapacity) return cur == DrainState.POWERED ? cur : DrainState.OFF;
        var targetDrain = Mth.clamp(outputFluid.isEmpty() ? 1 : SlagServerConfigs.DRAIN_MB_SPEED.get(), 0, outputCapacity - outputAmount);
        if (targetDrain == 0) return cur == DrainState.POWERED ? cur : DrainState.OFF;


        var drained = inputInv.drain(targetDrain, IFluidHandler.FluidAction.SIMULATE);
        if (drained.getAmount() != outputInv.fill(drained, IFluidHandler.FluidAction.SIMULATE)) return cur == DrainState.POWERED ? cur : DrainState.OFF;

        drainingFluid = inputFluid.copy();
        inputInv.drain(targetDrain, IFluidHandler.FluidAction.EXECUTE);
        outputInv.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        return cur;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        drainState = DrainState.fromString(tag.getString("state"));
        this.drainingFluid = FluidStack.parseOptional(registries, tag.getCompound("Fluid"));
        target = tag.contains("Target") ? BlockPos.of(tag.getLong("Target")) : null;
        landing = tag.getDouble("Landing");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (drainState != null) tag.putString("state", "" + drainState);
        if (!this.drainingFluid.isEmpty()) tag.put("Fluid", this.drainingFluid.save(registries));
        if (target != null) tag.putLong("Target", target.asLong());
        if (target != null) tag.putDouble("Landing", landing);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NonNull CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
