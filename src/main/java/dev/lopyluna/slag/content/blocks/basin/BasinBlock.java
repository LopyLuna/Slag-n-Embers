package dev.lopyluna.slag.content.blocks.basin;

import com.mojang.serialization.MapCodec;
import dev.lopyluna.slag.content.blocks.casting.CastingBE;
import dev.lopyluna.slag.content.blocks.smart.SmartBlock;
import dev.lopyluna.slag.content.utils.ShapeUtils;
import dev.lopyluna.slag.register.AllBETypes;
import dev.lopyluna.slag.register.AllLangs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import javax.annotation.Nonnull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.empty());
        tooltip.add(AllLangs.tr("cast_shift_clear").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(" ").append(AllLangs.tr("cast_shift_clear.desc")).withStyle(ChatFormatting.BLUE));
    }

    @Override
    protected @Nonnull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof BasinBE be) || be.coolingProgress > 0) return InteractionResult.PASS;
        var shift = player.isShiftKeyDown();
        if (shift && !player.getMainHandItem().isEmpty()) return InteractionResult.PASS;
        var tank = be.tankInventory;
        if (shift && !tank.isEmpty()) {
            if (!level.isClientSide) tank.drain(tank.getFluidAmount(), IFluidHandler.FluidAction.EXECUTE);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        var stack = be.itemInventory.getItem(CastingBE.RESULT);
        if (stack.isEmpty()) return InteractionResult.PASS;
        if (!level.isClientSide) {
            ItemHandlerHelper.giveItemToPlayer(player, stack.copy());
            be.itemInventory.setItem(CastingBE.RESULT, ItemStack.EMPTY);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected @Nonnull ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (held.isEmpty() || !(level.getBlockEntity(pos) instanceof BasinBE be)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        var itemHandler = held.copyWithCount(1).getCapability(Capabilities.FluidHandler.ITEM);
        if (itemHandler == null) return placeCastItem(held, level, be);
        var available = itemHandler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
        if (available.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (level.isClientSide) return ItemInteractionResult.CONSUME;

        var tank = be.tankInventory;
        var fillable = tank.fill(available, IFluidHandler.FluidAction.SIMULATE);
        if (fillable <= 0) return ItemInteractionResult.CONSUME;
        var drained = itemHandler.drain(fillable, IFluidHandler.FluidAction.EXECUTE);
        if (drained.isEmpty() || tank.fill(drained, IFluidHandler.FluidAction.EXECUTE) <= 0) return ItemInteractionResult.CONSUME;

        if (!player.getAbilities().instabuild) {
            var container = itemHandler.getContainer();
            held.shrink(1);
            if (held.isEmpty()) player.setItemInHand(hand, container);
            else if (!container.isEmpty() && !player.getInventory().add(container)) player.drop(container, false);
        }
        be.sendDataImmediately();
        level.playSound(null, pos, getFillSound(drained), SoundSource.BLOCKS, .5f, 1);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), getEmptySound(drained), SoundSource.PLAYERS, .5f, 1);
        return ItemInteractionResult.SUCCESS;
    }

    private ItemInteractionResult placeCastItem(ItemStack held, Level level, BasinBE be) {
        var inventory = be.itemInventory;
        if (!inventory.getItem(CastingBE.RESULT).isEmpty() || !be.tankInventory.isEmpty() || !inventory.isItemValid(CastingBE.RESULT, held)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!level.isClientSide) {
            inventory.insertItem(CastingBE.RESULT, held.copyWithCount(1), false);
            held.shrink(1);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
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
