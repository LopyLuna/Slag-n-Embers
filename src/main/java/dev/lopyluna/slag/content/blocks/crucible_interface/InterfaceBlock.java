package dev.lopyluna.slag.content.blocks.crucible_interface;

import com.mojang.serialization.MapCodec;
import dev.lopyluna.slag.content.blocks.BEBlock;
import dev.lopyluna.slag.content.utils.ShapeUtils;
import dev.lopyluna.slag.register.AllBETypes;
import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import javax.annotation.Nonnull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class InterfaceBlock extends BEBlock {
    public static final MapCodec<InterfaceBlock> CODEC = simpleCodec(InterfaceBlock::new);
    public static final VoxelShaper SHAPE = ShapeUtils.shape(0, 0, 0, 16, 16, 8).forHorizontal(Direction.SOUTH);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public InterfaceBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected @Nonnull BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (level.getBlockEntity(pos) instanceof InterfaceBE be) be.update = 4;
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected @Nonnull InteractionResult useWithoutItem(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, BlockHitResult pHit) {
        if (!(pLevel.getBlockEntity(pPos) instanceof InterfaceBE be)) return InteractionResult.PASS;
        var facing = pState.getValue(FACING);
        var relPos = pPos.relative(facing.getOpposite());
        if (!pLevel.isLoaded(relPos)) return InteractionResult.PASS;
        var cap = pLevel.getCapability(Capabilities.FluidHandler.BLOCK, relPos, facing);
        if (cap == null) return InteractionResult.PASS;
        be.targetCap = cap;
        if (!pLevel.isClientSide) {
            var provider = pState.getMenuProvider(pLevel, pPos);
            if (provider != null) pPlayer.openMenu(provider);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected @Nonnull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE.get(state.getValue(FACING));
    }

    public @Nonnull BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
    }
    public @Nonnull BlockState mirror(BlockState pState, Mirror pMirror) {
        return super.rotate(pState, pMirror.getRotation(pState.getValue(FACING)));
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (l, p, s, t) -> {
            if (t instanceof InterfaceBE be) be.tick();
        };
    }

    @Override
    public @Nonnull RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nonnull BlockEntityType<? extends BlockEntity> getBlockEntityType() {
        return AllBETypes.INTERFACE.get();
    }

    @Override
    protected @Nonnull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}
