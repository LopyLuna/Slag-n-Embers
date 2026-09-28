package dev.lopyluna.slag.content.blocks.drain;

import com.mojang.serialization.MapCodec;
import dev.lopyluna.slag.config.SlagServerConfigs;
import dev.lopyluna.slag.content.blocks.melter.MelterBlock;
import dev.lopyluna.slag.content.blocks.smart.SmartBlock;
import dev.lopyluna.slag.content.utils.ShapeUtils;
import dev.lopyluna.slag.register.AllBETypes;
import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import javax.annotation.Nonnull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class DrainBlock extends SmartBlock<DrainBE> {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final MapCodec<DrainBlock> CODEC = simpleCodec(DrainBlock::new);
    public static final VoxelShaper SHAPE = ShapeUtils.shape(4, 4, 8, 12, 12, 16).forHorizontal(Direction.NORTH);
    public DrainBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected @Nonnull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!SlagServerConfigs.EXTRACT_FLUID_FROM_DRAIN_TO_ITEM.get()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!(level.getBlockEntity(pos) instanceof DrainBE be)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        var single = stack.copyWithCount(1);
        var itemHandler = single.getCapability(Capabilities.FluidHandler.ITEM);
        if (itemHandler != null) {
            var inputInv = be.getInputHandler();
            if (inputInv == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            var inputFluid = inputInv.getFluidInTank(0);
            if (inputFluid.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

            var drained = inputInv.drain(itemHandler.getTankCapacity(0), IFluidHandler.FluidAction.SIMULATE);
            if (drained.getAmount() != itemHandler.fill(drained, IFluidHandler.FluidAction.SIMULATE)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            if (level.isClientSide) return ItemInteractionResult.SUCCESS;

            inputInv.drain(itemHandler.getTankCapacity(0), IFluidHandler.FluidAction.EXECUTE);
            itemHandler.fill(drained, IFluidHandler.FluidAction.EXECUTE);
            stack.shrink(1);
            var newStack = itemHandler.getContainer();

            drained.getFluid().getPickupSound().ifPresent(s -> level.playSound(player, player.getX(), player.getY(), player.getZ(), s, SoundSource.BLOCKS, 1.0F, 1.0F));
            if (stack.isEmpty()) player.setItemInHand(hand, newStack);
            else if (!player.getInventory().add(newStack)) player.drop(newStack, false);

            level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
            return ItemInteractionResult.CONSUME;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected @Nonnull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.isShiftKeyDown() || !(level.getBlockEntity(pos) instanceof DrainBE be)) return InteractionResult.PASS;
        be.cycleDrain();
        return InteractionResult.SUCCESS;
    }

    @Override
    protected @Nonnull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE.get(state.getValue(FACING));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FACING));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        var face = context.getClickedFace();
        var horizontal = face.getAxis().isHorizontal();
        if (horizontal) {
            var level = context.getLevel();
            var pos = context.getClickedPos();
            var state = level.getBlockState(pos.relative(face.getOpposite()));
            if (state.getBlock() instanceof MelterBlock && state.getValue(MelterBlock.FACING).equals(face.getOpposite())) return null;
        }
        return defaultBlockState().setValue(FACING, horizontal ? face : context.getHorizontalDirection());
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (level.isClientSide || !(level.getBlockEntity(pos) instanceof DrainBE be)) return;
        var previous = be.drainState;
        be.checkPowered();
        if (previous != be.drainState) be.update();
    }

    @Override
    public <S extends BlockEntity> BlockEntityTicker<S> getTicker(Level level, BlockState state, BlockEntityType<S> type) {
        return (level1, blockPos, blockState, s) -> {
            if (!(s instanceof DrainBE be)) return;
            be.tick();
        };
    }

    @Override
    public Class<DrainBE> getBlockEntityClass() {
        return DrainBE.class;
    }

    @Override
    public BlockEntityType<? extends DrainBE> getBlockEntityType() {
        return AllBETypes.DRAIN.get();
    }

    @Override
    protected @Nonnull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }
}
