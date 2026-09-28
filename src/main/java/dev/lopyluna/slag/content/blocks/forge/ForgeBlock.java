package dev.lopyluna.slag.content.blocks.forge;

import com.mojang.serialization.MapCodec;
import dev.lopyluna.slag.content.blocks.BEBlock;
import dev.lopyluna.slag.register.AllBETypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nonnull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ForgeBlock extends BEBlock {
    public static final MapCodec<ForgeBlock> CODEC = simpleCodec(ForgeBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public ForgeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    @Override
    protected @Nonnull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return defaultBlockState().setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite());
    }

    @Override
    public void stepOn(Level pLevel, BlockPos pPos, BlockState pState, Entity pEntity) {
        if (pLevel.random.nextDouble() < 0.2D && pState.getValue(LIT) && !pEntity.isSteppingCarefully() && pEntity instanceof LivingEntity living && living.getHealth() > 1.0F) pEntity.hurt(pLevel.damageSources().hotFloor(), 1.0F);
        super.stepOn(pLevel, pPos, pState, pEntity);
    }

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
        if (pState.getValue(LIT)) {
            double d0 = pPos.getX() + 0.5D;
            double d1 = pPos.getY();
            double d2 = pPos.getZ() + 0.5D;

            if (pRandom.nextDouble() < 0.1D) {
                pLevel.playLocalSound(d0, d1 + (10.0D / 16.0D), d2, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 0.75F, 0.95F, false);
                pLevel.playLocalSound(d0, d1 + (10.0D / 16.0D), d2, SoundEvents.SMOKER_SMOKE, SoundSource.BLOCKS, 0.75F, 0.95F, false);
            }

            var direction = pState.getValue(FACING);
            var axis = direction.getAxis();

            double d4 = pRandom.nextDouble() * 0.6D - 0.3D;
            double d5 = axis == Direction.Axis.Z ? (double) direction.getStepZ() * 0.5D : d4;
            double d6 = pRandom.nextDouble() * (2.5D / 16.0D);
            double d7 = axis == Direction.Axis.X ? (double) direction.getStepX() * 0.5D : d4;

            double d8 = axis == Direction.Axis.Z ? (double) direction.getStepZ() * -0.5D : d4;
            double d9 = axis == Direction.Axis.X ? (double) direction.getStepX() * -0.5D : d4;

            double d10 = axis == Direction.Axis.X ? (double) direction.getStepX() * 0.5D : d4;
            double d11 = axis == Direction.Axis.Z ? (double) direction.getStepZ() * 0.5D : d4;


            pLevel.addParticle(ParticleTypes.SMOKE, d0 + d5, d1 + (8.5D / 16.0D) + d6, d2 + d7, 0.0D, 0.0D, 0.0D);
            pLevel.addParticle(ParticleTypes.SMOKE, d0 + d5, d1 + (8.5D / 16.0D) + d6, d2 + d7, 0.0D, 0.0D, 0.0D);
            pLevel.addParticle(ParticleTypes.FLAME, d0 + d5, d1 + (8.5D / 16.0D) + d6, d2 + d7, 0.0D, 0.0D, 0.0D);
            pLevel.addParticle(ParticleTypes.SMOKE, d0 + d8, d1 + (8.5D / 16.0D) + d6, d2 + d9, 0.0D, 0.0D, 0.0D);
            pLevel.addParticle(ParticleTypes.SMOKE, d0 + d8, d1 + (8.5D / 16.0D) + d6, d2 + d9, 0.0D, 0.0D, 0.0D);
            pLevel.addParticle(ParticleTypes.FLAME, d0 + d8, d1 + (8.5D / 16.0D) + d6, d2 + d9, 0.0D, 0.0D, 0.0D);

            pLevel.addParticle(ParticleTypes.SMOKE, d0 + d10, d1 + (10.0D / 16.0D) + d6, d2 + d11, 0.0D, 0.0D, 0.0D);
            pLevel.addParticle(ParticleTypes.SMOKE, d0 + d10, d1 + (10.0D / 16.0D) + d6, d2 + d11, 0.0D, 0.0D, 0.0D);
            pLevel.addParticle(ParticleTypes.FLAME, d0 + d10, d1 + (10.0D / 16.0D) + d6, d2 + d11, 0.0D, 0.0D, 0.0D);

            pLevel.addParticle(ParticleTypes.SMOKE, d0 + (d4 / 0.9), d1 + 1.1D, d2 + (d4 / 0.9), 0.0D, 0.0D, 0.0D);
            pLevel.addParticle(ParticleTypes.SMOKE, d0 + (d4 / 0.9), d1 + 1.1D, d2 + (d4 / 0.9), 0.0D, 0.0D, 0.0D);
            pLevel.addParticle(ParticleTypes.FLAME, d0 + (d4 / 0.9), d1 + 1.1D, d2 + (d4 / 0.9), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected @Nonnull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            var provider = state.getMenuProvider(level, pos);
            if (provider != null) player.openMenu(provider);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof ForgeBE be) {
                Containers.dropContents(level, pos, be);
                if (level instanceof ServerLevel server) be.getRecipesToAwardAndPopExperience(server, Vec3.atCenterOf(pos));
                super.onRemove(state, level, pos, newState, moved);
                level.updateNeighbourForOutputSignal(pos, this);
            } else super.onRemove(state, level, pos, newState, moved);
        }
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
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(blockPos));
    }

    @Override
    public @Nonnull RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nonnull BlockEntityType<? extends BlockEntity> getBlockEntityType() {
        return AllBETypes.FORGE.get();
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : ForgeBE::serverTick;
    }
}
