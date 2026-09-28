package dev.lopyluna.slag.content.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public abstract class SimpleBEBlock extends BEBlock {
    protected SimpleBEBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @Nonnull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nonnull
    @Override
    public abstract BlockEntityType<? extends BlockEntity> getBlockEntityType();

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof SimpleBE be && !state.is(newState.getBlock())) be.onRemoved(level, pos, state);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.getBlockEntity(pos) instanceof SimpleBE be) be.onPlaced(placer, stack, level, pos, state);
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Override
    protected @Nonnull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof SimpleBE be) {
            var result = be.useItem(stack, state, level, pos, player, hand, hitResult);
            if (result != null) return result;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected @Nonnull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof SimpleBE be) {
            var result = be.useItem(ItemStack.EMPTY, state, level, pos, player, InteractionHand.MAIN_HAND, hitResult);
            if (result != null) return result.result();
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }



    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> blockEntityType) {
        return (level, blockPos, blockState, t) -> {
            if (t instanceof SimpleBE be) be.tick(level, blockPos, blockState);
        };
    }
}
