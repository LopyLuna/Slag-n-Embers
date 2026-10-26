package dev.lopyluna.slag.content.blocks.basin;

import com.mojang.serialization.MapCodec;
import dev.lopyluna.slag.content.AllUtils;
import dev.lopyluna.slag.content.blocks.casting.CastingBE;
import dev.lopyluna.slag.content.blocks.smart.SmartBlock;
import dev.lopyluna.slag.content.utils.ShapeUtils;
import dev.lopyluna.slag.register.AllBETypes;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import javax.annotation.Nonnull;

import javax.annotation.ParametersAreNonnullByDefault;

import static dev.lopyluna.slag.content.blocks.crucible.CrucibleBlock.getEmptySound;
import static dev.lopyluna.slag.content.blocks.crucible.CrucibleBlock.getFillSound;

@ParametersAreNonnullByDefault
public class BasinBlock extends SmartBlock<BasinBE> {
    public static final MapCodec<BasinBlock> CODEC = simpleCodec(BasinBlock::new);
    public static final VoxelShape SHAPE = ShapeUtils.shape(0, 0, 0, 16, 3, 16)
            .add(0, 0, 0, 16, 16, 2).add(0, 0, 14, 16, 16, 16)
            .add(0, 0, 0, 2, 16, 16).add(14, 0, 0, 16, 16, 16).build();
    public BasinBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @Nonnull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof BasinBE be) || be.coolingProgress > 0) return InteractionResult.PASS;
        var shift = player.isShiftKeyDown();
        if (shift && !player.getMainHandItem().isEmpty()) return InteractionResult.PASS;
        var tank = be.tankInventory;
        if (shift && !tank.isEmpty()) {
            if (!level.isClientSide()) tank.drain(tank.getFluidAmount());
            return InteractionResult.SUCCESS;
        }
        var stack = be.itemInventory.getItem(CastingBE.RESULT);
        if (stack.isEmpty()) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            AllUtils.give(player, stack.copy());
            be.itemInventory.setItem(CastingBE.RESULT, ItemStack.EMPTY);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected @Nonnull InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (held.isEmpty() || !(level.getBlockEntity(pos) instanceof BasinBE be)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        var itemHandler = AllUtils.heldFluids(player, hand);
        if (itemHandler == null) return placeCastItem(held, level, be);
        var drained = AllUtils.drain(itemHandler, Integer.MAX_VALUE, true);
        if (drained.isEmpty()) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level.isClientSide()) return InteractionResult.CONSUME;
        if (ResourceHandlerUtil.move(itemHandler, be.tankInventory, FluidResource.of(drained)::equals, drained.getAmount(), null) <= 0) return InteractionResult.CONSUME;

        be.sendDataImmediately();
        level.playSound(null, pos, getFillSound(drained), SoundSource.BLOCKS, .5f, 1);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), getEmptySound(drained), SoundSource.PLAYERS, .5f, 1);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult placeCastItem(ItemStack held, Level level, BasinBE be) {
        var inventory = be.itemInventory;
        if (!inventory.getItem(CastingBE.RESULT).isEmpty() || !be.tankInventory.isEmpty() || !inventory.isItemValid(CastingBE.RESULT, held)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!level.isClientSide()) {
            inventory.insertItem(CastingBE.RESULT, held.copyWithCount(1), false);
            held.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public Class<BasinBE> getBlockEntityClass() {
        return BasinBE.class;
    }

    @Override
    public BlockEntityType<? extends BasinBE> getBlockEntityType() {
        return AllBETypes.BASIN.get();
    }

    @Override
    protected @Nonnull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof BasinBE be ? be.getLuminosity() : 0;
    }

    @Override
    protected @Nonnull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
