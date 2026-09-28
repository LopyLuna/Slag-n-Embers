package dev.lopyluna.slag.content.traits.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.lopyluna.slag.content.traits.Trait;
import dev.lopyluna.slag.content.traits.TraitEffect;
import dev.lopyluna.slag.content.traits.Traits;
import dev.lopyluna.slag.mixin.AxeItemAccessor;
import dev.lopyluna.slag.register.AllTags;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import javax.annotation.Nonnull;

import javax.annotation.Nullable;
import java.util.List;

public record BlockActionEffect(Action action) implements TraitEffect {
    public static final MapCodec<BlockActionEffect> CODEC = Action.CODEC.fieldOf("action").xmap(BlockActionEffect::new, BlockActionEffect::action);

    public static final BlockActionEffect STRIP = new BlockActionEffect(Action.STRIP);
    public static final BlockActionEffect FLATTEN = new BlockActionEffect(Action.FLATTEN);
    public static final BlockActionEffect TILL = new BlockActionEffect(Action.TILL);
    public static final BlockActionEffect HARVEST = new BlockActionEffect(Action.HARVEST);

    private static final List<IntegerProperty> AGES = List.of(BlockStateProperties.AGE_1, BlockStateProperties.AGE_2, BlockStateProperties.AGE_3, BlockStateProperties.AGE_4, BlockStateProperties.AGE_5, BlockStateProperties.AGE_7, BlockStateProperties.AGE_15, BlockStateProperties.AGE_25);

    @Override
    public @Nonnull MapCodec<BlockActionEffect> codec() {
        return CODEC;
    }

    @Override
    public void collect(Trait trait, Traits.Builder builder) {
        builder.useOn(trait, this);
    }

    @Override
    public InteractionResult useOn(Trait trait, UseOnContext context) {
        return switch (action) {
            case STRIP -> strip(context);
            case FLATTEN -> flatten(context);
            case TILL -> till(context);
            case HARVEST -> harvest(context);
        };
    }

    private static InteractionResult strip(UseOnContext context) {
        if (AxeItemAccessor.playerHasShieldUseIntent(context)) return InteractionResult.PASS;
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var player = context.getPlayer();
        var result = stripped(level, pos, player, level.getBlockState(pos), context);
        if (result == null) return InteractionResult.PASS;
        var stack = context.getItemInHand();
        if (player instanceof ServerPlayer serverPlayer) CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
        level.setBlock(pos, result, 11);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, result));
        if (player != null) stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static @Nullable BlockState stripped(Level level, BlockPos pos, @Nullable Player player, BlockState state, UseOnContext context) {
        var strip = state.getToolModifiedState(context, ItemAbilities.AXE_STRIP, false);
        if (strip != null) {
            level.playSound(player, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1f, 1f);
            return strip;
        }
        var scrape = state.getToolModifiedState(context, ItemAbilities.AXE_SCRAPE, false);
        if (scrape != null) {
            level.playSound(player, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1f, 1f);
            level.levelEvent(player, 3005, pos, 0);
            return scrape;
        }
        var waxOff = state.getToolModifiedState(context, ItemAbilities.AXE_WAX_OFF, false);
        if (waxOff != null) {
            level.playSound(player, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1f, 1f);
            level.levelEvent(player, 3004, pos, 0);
        }
        return waxOff;
    }

    private static InteractionResult flatten(UseOnContext context) {
        if (context.getClickedFace() == Direction.DOWN) return InteractionResult.PASS;
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var state = level.getBlockState(pos);
        var player = context.getPlayer();
        var flat = state.getToolModifiedState(context, ItemAbilities.SHOVEL_FLATTEN, false);
        BlockState result;
        if (flat != null && level.getBlockState(pos.above()).isAir()) {
            level.playSound(player, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1f, 1f);
            result = flat;
        } else if ((result = state.getToolModifiedState(context, ItemAbilities.SHOVEL_DOUSE, false)) != null && !level.isClientSide()) level.levelEvent(null, 1009, pos, 0);
        if (result == null) return InteractionResult.PASS;
        if (!level.isClientSide) {
            level.setBlock(pos, result, 11);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, result));
            if (player != null) context.getItemInHand().hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static InteractionResult till(UseOnContext context) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var tilled = level.getBlockState(pos).getToolModifiedState(context, ItemAbilities.HOE_TILL, false);
        if (tilled == null) return InteractionResult.PASS;
        var player = context.getPlayer();
        level.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1f, 1f);
        if (!level.isClientSide) {
            HoeItem.changeIntoState(tilled).accept(context);
            if (player != null) context.getItemInHand().hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static InteractionResult harvest(UseOnContext context) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var player = context.getPlayer();
        var state = level.getBlockState(pos);
        if (!state.is(AllTags.HARVESTABLE)) return InteractionResult.PASS;
        var result = InteractionResult.PASS;
        var aged = false;
        for (var age : AGES) if (state.hasProperty(age)) {
            if (state.getValue(age) == age.getPossibleValues().size() - 1) {
                level.destroyBlock(pos, true, player);
                level.setBlockAndUpdate(pos, state.setValue(age, 0));
                result = InteractionResult.SUCCESS;
            }
            aged = true;
            break;
        }
        if (!aged) {
            level.destroyBlock(pos, true, player);
            result = InteractionResult.SUCCESS;
        }
        if (result == InteractionResult.SUCCESS && player != null) context.getItemInHand().hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
        return result;
    }

    public enum Action implements StringRepresentable {
        STRIP("strip"),
        FLATTEN("flatten"),
        TILL("till"),
        HARVEST("harvest");

        public static final Codec<Action> CODEC = StringRepresentable.fromEnum(Action::values);

        private final String name;

        Action(String name) {
            this.name = name;
        }

        @Override
        public @Nonnull String getSerializedName() {
            return name;
        }
    }
}
