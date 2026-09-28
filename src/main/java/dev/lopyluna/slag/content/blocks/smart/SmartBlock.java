package dev.lopyluna.slag.content.blocks.smart;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nonnull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public abstract class SmartBlock<T extends BlockEntity> extends BaseEntityBlock {
    protected SmartBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @Nonnull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public abstract Class<T> getBlockEntityClass();

    public abstract BlockEntityType<? extends T> getBlockEntityType();

    public void withBlockEntityDo(BlockGetter getter, BlockPos pos, Consumer<T> action) {
        getBlockEntityOptional(getter, pos).ifPresent(action);
    }

    public InteractionResult onBlockEntityUse(BlockGetter getter, BlockPos pos, Function<T, InteractionResult> action) {
        return getBlockEntityOptional(getter, pos).map(action).orElse(InteractionResult.PASS);
    }

    public ItemInteractionResult onBlockEntityUseItemOn(BlockGetter getter, BlockPos pos, Function<T, ItemInteractionResult> action) {
        return getBlockEntityOptional(getter, pos).map(action).orElse(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.hasBlockEntity()) return;
        if (state.is(newState.getBlock()) && newState.hasBlockEntity()) return;
        if (level.getBlockEntity(pos) instanceof SmartBlockEntity be) be.destroy();
        level.removeBlockEntity(pos);
    }

    public Optional<T> getBlockEntityOptional(BlockGetter getter, BlockPos pos) {
        return Optional.ofNullable(getBlockEntity(getter, pos));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return getBlockEntityType().create(pos, state);
    }


    @Override
    public <S extends BlockEntity> BlockEntityTicker<S> getTicker(Level level, BlockState state, BlockEntityType<S> type) {
        if (SmartBlockEntity.class.isAssignableFrom(getBlockEntityClass())) return new SmartBlockEntityTicker<>();
        return null;
    }

    @javax.annotation.Nullable
    @SuppressWarnings("unchecked")
    public T getBlockEntity(BlockGetter worldIn, BlockPos pos) {
        BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        Class<T> expectedClass = getBlockEntityClass();

        if (blockEntity == null) return null;
        if (!expectedClass.isInstance(blockEntity)) return null;

        return (T) blockEntity;
    }
}
