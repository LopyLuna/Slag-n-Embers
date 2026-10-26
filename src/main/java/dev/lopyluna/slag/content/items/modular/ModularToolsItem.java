package dev.lopyluna.slag.content.items.modular;

import dev.lopyluna.slag.content.traits.Traits;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbility;
import javax.annotation.Nonnull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ModularToolsItem extends ModularItem {
    public ModularToolsItem(Properties properties) {
        super(properties);
    }

    @Override
    public @Nonnull ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        var traits = Traits.of(stack);
        return traits.isEmpty() ? super.getDefaultAttributeModifiers(stack) : traits.attributes;
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var traits = Traits.of(stack);
        if (!traits.tool) return;
        for (var hook : traits.hurtEnemy) hook.effect().onHurtEnemy(hook.trait(), stack, target, attacker);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        var traits = Traits.of(stack);
        return traits.tool || traits.armor;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        var traits = Traits.of(stack);
        if (!level.isClientSide() && traits.tool && state.getDestroySpeed(level, pos) != 0f) stack.hurtAndBreak(traits.blockCost, miningEntity, EquipmentSlot.MAINHAND);
        return traits.tool;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        var traits = Traits.of(stack);
        if (!traits.tool && !traits.armor) return super.getMaxDamage(stack);
        return Math.round(traits.maxDamage);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        var traits = Traits.of(stack);
        if (!traits.tool || !isCorrectToolForDrops(stack, state)) return super.getDestroySpeed(stack, state);
        return traits.miningSpeed * traits.miningMultiplier(state);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        var traits = Traits.of(stack);
        if (!traits.tool) return super.isCorrectToolForDrops(stack, state);
        if (!traits.isCorrectForDrops(state)) return false;
        var i = 0f;
        if (state.is(BlockTags.INCORRECT_FOR_WOODEN_TOOL)) i = 1f;
        if (state.is(BlockTags.INCORRECT_FOR_GOLD_TOOL)) i = 2f;
        if (state.is(BlockTags.INCORRECT_FOR_STONE_TOOL)) i = 3f;
        if (state.is(BlockTags.INCORRECT_FOR_IRON_TOOL)) i = 4f;
        if (state.is(BlockTags.INCORRECT_FOR_DIAMOND_TOOL)) i = 5f;
        if (state.is(BlockTags.INCORRECT_FOR_NETHERITE_TOOL)) i = 6f;
        return traits.miningTier > i + .5f;
    }

    @Override
    public @Nonnull InteractionResult useOn(UseOnContext context) {
        for (var hook : Traits.of(context.getItemInHand()).useOn) {
            var result = hook.effect().useOn(hook.trait(), context);
            if (result.consumesAction()) return result;
        }
        return super.useOn(context);
    }

    @Override
    public boolean canPerformAction(ItemInstance stack, ItemAbility itemAbility) {
        return stack instanceof ItemStack item && Traits.of(item).abilities.contains(itemAbility) || super.canPerformAction(stack, itemAbility);
    }
}
