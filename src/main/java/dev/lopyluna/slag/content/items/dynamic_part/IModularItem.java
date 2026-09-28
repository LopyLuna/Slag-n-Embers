package dev.lopyluna.slag.content.items.dynamic_part;

import com.mojang.datafixers.util.Pair;
import dev.lopyluna.slag.content.items.modular.DataDynamicParts;
import dev.lopyluna.slag.content.traits.Traits;
import dev.lopyluna.slag.content.types.MaterialType;
import dev.lopyluna.slag.content.types.ModularType;
import dev.lopyluna.slag.register.AllDataComponents;
import dev.lopyluna.slag.register.AllTraits;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public interface IModularItem {
    default @Nullable ModularType getModularTypeFromParts(@Nullable DataDynamicParts parts) {
        return parts == null ? null : parts.getModularType();
    }

    default ModularType getModularTypeFromStack(ItemStack stack) {
        return getModularTypeFromParts(getParts(stack));
    }

    default List<Pair<ItemStack, IDynamicPart>> getDynamicParts(ItemStack pStack) {
        var parts = new ArrayList<Pair<ItemStack, IDynamicPart>>();
        var dataParts = getParts(pStack);
        if (dataParts == null || dataParts.isEmpty()) return parts;
        var copy = dataParts.itemsCopy();
        for (var stack : copy) if (stack.getItem() instanceof IDynamicPart part) parts.add(Pair.of(stack, part));
        return parts;
    }
    default List<MaterialType> getMaterialTypes(DataDynamicParts parts) {
        return parts.getMaterialTypes();
    }
    default List<ItemStack> getNonDynamicParts(ItemStack pStack) {
        var parts = new ArrayList<ItemStack>();
        var dataParts = getParts(pStack);
        if (dataParts == null || dataParts.isEmpty()) return parts;
        for (var stack : dataParts.itemsCopy()) if (!(stack.getItem() instanceof IDynamicPart)) parts.add(stack);
        return parts;
    }
    default DataDynamicParts getParts(ItemStack pStack) {
        return pStack.get(AllDataComponents.DYNAMIC_PARTS);
    }
    default void setParts(ItemStack pStack, List<ItemStack> pStacks) {
        pStack.set(AllDataComponents.DYNAMIC_PARTS, new DataDynamicParts(pStacks));
    }

    default Traits getTraits(ItemStack stack) {
        var parts = getParts(stack);
        return parts == null ? Traits.EMPTY : parts.getTraits();
    }

    default float getSpeed(ItemStack stack) { return getTraits(stack).miningSpeed; }
    default float getAttackSpeed(ItemStack stack) { return getTraits(stack).value(AllTraits.ATTACK_SPEED); }
    default float getDura(ItemStack stack) { return getTraits(stack).maxDamage; }
    default float getTier(ItemStack stack) { return getTraits(stack).miningTier; }
    default float getSharp(ItemStack stack) { return getTraits(stack).value(AllTraits.ATTACK_DAMAGE); }
    default float getKbRes(ItemStack stack) { return getTraits(stack).value(AllTraits.KNOCKBACK_RESISTANCE); }
    default float getDefense(ItemStack stack) { return getTraits(stack).value(AllTraits.ARMOR); }
    default float getTough(ItemStack stack) { return getTraits(stack).value(AllTraits.ARMOR_TOUGHNESS); }
    default float getEnch(ItemStack stack) { return getTraits(stack).enchantability; }
}
