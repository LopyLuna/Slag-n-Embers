package dev.lopyluna.slag.content.blocks.melter;

import com.mojang.serialization.MapCodec;
import dev.lopyluna.slag.content.blocks.smart.SmartBlock;
import dev.lopyluna.slag.content.utils.ShapeUtils;
import dev.lopyluna.slag.register.AllBETypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import javax.annotation.Nonnull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class MelterBlock extends SmartBlock<MelterBE> {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final MapCodec<MelterBlock> CODEC = simpleCodec(MelterBlock::new);
    public static final VoxelShape SHAPE = ShapeUtils.shape(0, 0, 0, 16, 4, 16)
            .add(0, 0, 0, 16, 16, 3).add(0, 0, 13, 16, 16, 16)
            .add(0, 0, 0, 3, 16, 16).add(13, 0, 0, 16, 16, 16).build();

    public MelterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return defaultBlockState().setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite());
    }

    @Override
    protected @Nonnull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof MelterBE be) player.openMenu(be);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof ItemEntity itemEntity) {
            var stack = itemEntity.getItem();
            if (stack.isEmpty()) return;
            var cap = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.DOWN);
            if (cap == null) return;
            var remainder = ItemHandlerHelper.insertItemStacked(cap, stack, false);
           // System.out.println("Tried insert: in= " + stack + " out= " + remainder);

            if (remainder.getCount() == stack.getCount()) return;

            if (remainder.isEmpty()) entity.discard();
            else itemEntity.setItem(remainder);
        }
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof MelterBE be ? be.getLuminosity() : 0;
    }

    @Override
    protected @Nonnull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (neighborPos.equals(pos.below())) withBlockEntityDo(level, pos, MelterBE::updateHeat);
    }

    @Override
    public Class<MelterBE> getBlockEntityClass() {
        return MelterBE.class;
    }

    @Override
    public BlockEntityType<? extends MelterBE> getBlockEntityType() {
        return AllBETypes.MELTER.get();
    }

    public @Nonnull BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
    }
    public @Nonnull BlockState mirror(BlockState pState, Mirror pMirror) {
        return super.rotate(pState, pMirror.getRotation(pState.getValue(FACING)));
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof MelterBE be && be.itemInventory != null ? AbstractContainerMenu.getRedstoneSignalFromContainer(be.itemInventory) : 0;
    }

    @Override
    protected @Nonnull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}
