package dev.lopyluna.slag.content.blocks.crucible;

import dev.lopyluna.slag.content.AllUtils;
import dev.lopyluna.slag.content.blocks.multiblock.ConnectivityHandler;
import dev.lopyluna.slag.content.blocks.multiblock.FluidMultiBlockEntity;
import dev.lopyluna.slag.content.blocks.multiblock.MultiQueue;
import dev.lopyluna.slag.content.blocks.multiblock.MultiQueue.Sweep;
import dev.lopyluna.slag.content.blocks.smart.SmartBlock;
import dev.lopyluna.slag.content.utils.NBTHelper;
import dev.lopyluna.slag.register.AllBETypes;
import dev.lopyluna.slag.register.AllTags;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.fluids.FluidStack;
import javax.annotation.Nonnull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;

@ParametersAreNonnullByDefault
public class CrucibleItem extends BlockItem {
    public CrucibleItem(Block block, Properties properties) {
        super(block, properties);
    }
    @Override
    public @Nonnull InteractionResult place(BlockPlaceContext ctx) {
        var result = super.place(ctx);
        if (!result.consumesAction()) return result;
        var level = ctx.getLevel();
        var pos = ctx.getClickedPos();
        var placed = new LongArrayList();
        placed.add(pos.asLong());
        var rest = tryMultiPlace(ctx, placed);
        var player = ctx.getPlayer();
        if (rest != null && !rest.isEmpty() && player != null) {
            var stack = ctx.getItemInHand();
            var template = stack.copyWithCount(1);
            if (!player.isCreative()) stack.shrink(rest.size());
            if (level.isClientSide) return result;
            Placement.hold(level, placed);
            MultiQueue.of(level).add(new Placement(level, player, this, template, ctx.getClickedFace(), pos, placed, rest));
            return result;
        }
        if (!(level.getBlockEntity(pos) instanceof CrucibleBE be)) return result;
        if (level.isClientSide) predict(level, be, placed);
        else if (level.captureBlockSnapshots) be.placed = true;
        else ConnectivityHandler.formMulti(be);
        return result;
    }

    private static void predict(Level level, CrucibleBE placed, LongArrayList positions) {
        var plan = ConnectivityHandler.plan(placed);
        if (plan == null) return;
        var area = plan.area();
        var work = positions.size() + area.shell();
        for (var old : plan.absorbed()) work += old.shell();
        if (work > MultiQueue.updates() * MultiQueue.CHANGED) return;
        var origin = area.origin();
        int widthX = area.widthX(), widthZ = area.widthZ(), height = area.height();
        var window = plan.data() instanceof Boolean windows && windows;
        var sweeps = new ArrayList<Sweep>();
        sweeps.add(Sweep.points(positions));
        sweeps.add(Sweep.shell(area));
        for (var old : plan.absorbed()) sweeps.add(Sweep.shell(old));
        for (var sweep : sweeps) while (sweep.next()) {
            var pos = sweep.pos();
            int x = pos.getX() - origin.getX(), y = pos.getY() - origin.getY(), z = pos.getZ() - origin.getZ();
            if (x < 0 || y < 0 || z < 0 || x >= widthX || y >= height || z >= widthZ) continue;
            var state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof CrucibleBlock)) continue;
            var shaped = CrucibleBE.shaped(state, x, y, z, widthX, widthZ, height, window);
            if (shaped != state) level.setBlock(pos, shaped, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player, ItemStack stack, BlockState state) {
        var server = level.getServer();
        if (server == null) return false;
        var data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data != null) {
            CompoundTag nbt = data.copyTag();
            nbt.remove("Luminosity");
            nbt.remove("Size");
            nbt.remove("Height");
            nbt.remove("Controller");
            nbt.remove("ControllerOffset");
            nbt.remove("WidthX");
            nbt.remove("WidthZ");
            nbt.remove("LastKnownPos");
            if (nbt.contains("TankContent")) {
                var provider = server.registryAccess();
                var fluids = NBTHelper.readFluidList(nbt.getCompound("TankContent").getList("Fluids", Tag.TAG_COMPOUND), provider);

                int totalAmount = fluids.stream().mapToInt(FluidStack::getAmount).sum();
                int baseCapacity = CrucibleBE.getCapacityMultiplier();

                if (totalAmount > baseCapacity) {
                    int toRemove = totalAmount - baseCapacity;
                    for (int i = fluids.size() - 1; i >= 0 && toRemove > 0; i--) {
                        var fluid = fluids.get(i);
                        int take = Math.min(fluid.getAmount(), toRemove);
                        fluid.shrink(take);
                        toRemove -= take;
                        if (fluid.isEmpty()) fluids.remove(i);
                    }
                }

                var tag = new CompoundTag();
                tag.put("Fluids", NBTHelper.writeFluidList(fluids, provider));
                nbt.put("TankContent", tag);
            }
            BlockEntity.addEntityType(nbt, ((SmartBlock<?>) this.getBlock()).getBlockEntityType());
            stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(nbt));
        }
        return super.updateCustomBlockEntityTag(pos, level, player, stack, state);
    }

    private @Nullable LongArrayList tryMultiPlace(BlockPlaceContext ctx, LongArrayList placed) {
        var player = ctx.getPlayer();
        if (player == null || player.isShiftKeyDown()) return null;
        var face = ctx.getClickedFace();
        var stack = ctx.getItemInHand();
        var level = ctx.getLevel();
        var pos = ctx.getClickedPos();
        var placedOnPos = pos.relative(face.getOpposite());

        if (!(level.getBlockState(placedOnPos).getBlock() instanceof CrucibleBlock)) return null;
        if (AllUtils.tagPresentInHotbar(player, AllTags.BLACKLISTED_HOTBAR_ITEMS)) return null;
        var crucibleAt = ConnectivityHandler.partAt(AllBETypes.CRUCIBLE.get(), level, placedOnPos);
        if (crucibleAt == null) return null;
        var ctrlBE = crucibleAt.getControllerBE();
        if (ctrlBE == null) return null;
        int widthX = ctrlBE.getWidthX(), widthZ = ctrlBE.getWidthZ(), height = ctrlBE.getHeight();
        if (widthX < 2 || widthZ < 2) return null;
        var full = switch (face.getAxis()) {
            case X -> widthX >= ctrlBE.getMaxWidthX();
            case Z -> widthZ >= ctrlBE.getMaxWidthZ();
            case Y -> height >= ctrlBE.getMaxLength(Direction.Axis.Y, Math.max(widthX, widthZ));
        };
        if (full) return null;

        var startPos = switch (face) {
            case DOWN -> ctrlBE.getBlockPos().below();
            case UP -> ctrlBE.getBlockPos().above(height);
            case NORTH -> ctrlBE.getBlockPos().north();
            case SOUTH -> ctrlBE.getBlockPos().south(widthZ);
            case WEST -> ctrlBE.getBlockPos().west();
            case EAST -> ctrlBE.getBlockPos().east(widthX);
        };
        var axis = face.getAxis();
        if (startPos.get(axis) != pos.get(axis)) return null;

        var aAxis = switch (axis) {
            case Z, Y -> Direction.Axis.X;
            case X -> Direction.Axis.Y;
        };
        var bAxis = switch (axis) {
            case X, Y -> Direction.Axis.Z;
            case Z -> Direction.Axis.Y;
        };
        var aTarget = switch (axis) {
            case Z, Y -> widthX;
            case X -> height;
        };
        var bTarget = switch (axis) {
            case X, Y -> widthZ;
            case Z -> height;
        };

        var targets = new ArrayList<BlockPlaceContext>();
        for (var aOff = 0; aOff < aTarget; aOff++) for (var bOff = 0; bOff < bTarget; bOff++) {
            var offPos = startPos.relative(aAxis, aOff).relative(bAxis, bOff);
            if (level.getBlockState(offPos).getBlock() instanceof CrucibleBlock) continue;
            var at = BlockPlaceContext.at(ctx, offPos, face);
            if (!at.replacingClickedOnBlock() || getPlacementState(at) == null) return null;
            targets.add(at);
        }
        if (!player.isCreative() && stack.getCount() < targets.size()) return null;
        targets.sort((a, b) -> Sweep.byColumn(a.getClickedPos().asLong(), b.getClickedPos().asLong()));

        var now = Math.min(targets.size(), MultiQueue.updates());
        player.getPersistentData().putBoolean("SilencePlacingSound", true);
        for (var i = 0; i < now; i++) if (super.place(targets.get(i)).consumesAction()) placed.add(targets.get(i).getClickedPos().asLong());
        player.getPersistentData().remove("SilencePlacingSound");
        var rest = new LongArrayList(targets.size() - now);
        for (var i = now; i < targets.size(); i++) rest.add(targets.get(i).getClickedPos().asLong());
        return rest;
    }

    static final class Placement implements MultiQueue.Job {
        private final Level level;
        private final Player player;
        private final CrucibleItem item;
        private final ItemStack template;
        private final Direction face;
        private final BlockPos anchor;
        private final LongArrayList placed;
        private final LongArrayList rest;
        private int at;
        private int failed;
        private boolean started;
        private boolean cancelled;

        Placement(Level level, Player player, CrucibleItem item, ItemStack template, Direction face, BlockPos anchor, LongArrayList placed, LongArrayList rest) {
            this.level = level;
            this.player = player;
            this.item = item;
            this.template = template;
            this.face = face;
            this.anchor = anchor;
            this.placed = placed;
            this.rest = rest;
        }

        static void hold(Level level, LongArrayList positions) {
            for (var i = 0; i < positions.size(); i++) if (level.getBlockEntity(BlockPos.of(positions.getLong(i))) instanceof CrucibleBE be) {
                be.held = true;
                be.updateConnectivity = false;
            }
        }

        @Override
        public int run(int budget, boolean urgent) {
            if (!urgent) return 0;
            if (!started) {
                started = true;
                cancelled = !(level.getBlockEntity(anchor) instanceof CrucibleBE);
                if (cancelled) at = rest.size();
            }
            var used = 0;
            while (used < budget && at < rest.size()) {
                var pos = BlockPos.of(rest.getLong(at++));
                used += 2 * MultiQueue.CHANGED;
                if (place(pos)) placed.add(pos.asLong());
                else failed++;
            }
            return used;
        }

        private boolean place(BlockPos pos) {
            if (player.isRemoved() || !level.isLoaded(pos)) return false;
            var state = item.getBlock().defaultBlockState();
            if (!level.getBlockState(pos).canBeReplaced() || !level.isUnobstructed(state, pos, CollisionContext.of(player))) return false;
            var snapshot = BlockSnapshot.create(level.dimension(), level, pos);
            if (!level.setBlock(pos, state, Block.UPDATE_ALL)) return false;
            if (EventHooks.onBlockPlace(player, snapshot, face)) {
                snapshot.restore(snapshot.getFlags() | Block.UPDATE_CLIENTS);
                return false;
            }
            item.updateCustomBlockEntityTag(pos, level, player, template, state);
            state.getBlock().setPlacedBy(level, pos, state, player, template);
            if (level.getBlockEntity(pos) instanceof CrucibleBE be) {
                be.held = true;
                be.updateConnectivity = false;
            }
            level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, state));
            return true;
        }

        @Override
        public boolean done() {
            return at >= rest.size();
        }

        @Override
        public void finish() {
            var start = (FluidMultiBlockEntity) null;
            for (var i = 0; i < placed.size(); i++) if (level.getBlockEntity(BlockPos.of(placed.getLong(i))) instanceof CrucibleBE be) {
                be.held = false;
                if (start == null && be.isController) start = be;
            }
            if (cancelled) return;
            if (failed > 0 && !player.isCreative()) {
                var refund = template.copyWithCount(failed);
                if (player.isRemoved()) Containers.dropItemStack(level, anchor.getX(), anchor.getY(), anchor.getZ(), refund);
                else player.getInventory().placeItemBackInInventory(refund);
            }
            if (level.getBlockEntity(anchor) instanceof CrucibleBE be) start = be.isController ? be : be.getControllerBE();
            if (start != null) ConnectivityHandler.formMulti(start);
            for (var i = 0; i < placed.size(); i++) if (level.getBlockEntity(BlockPos.of(placed.getLong(i))) instanceof CrucibleBE be && be.isController && be.getTotalSize() == 1) be.updateConnectivity = true;
        }
    }
}
