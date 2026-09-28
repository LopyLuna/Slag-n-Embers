package dev.lopyluna.slag.content.items.modular;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.traits.Traits;
import net.minecraft.core.Holder;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import javax.annotation.Nonnull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class ModularEquipablesItem extends ModularToolsItem {
    public ModularEquipablesItem(Properties properties) {
        super(properties);
        DispenserBlock.registerBehavior(this, DISPENSE_ITEM_BEHAVIOR);
    }

    public Holder<SoundEvent> getEquipSound(ItemStack stack) {
        var parts = getDynamicParts(stack);
        if (parts.isEmpty()) return SoundEvents.ARMOR_EQUIP_DIAMOND;
        var pair = parts.getFirst();
        var part = pair.getSecond();
        var material = part.getMaterialType(pair.getFirst());
        if (material.isEmpty()) return SoundEvents.ARMOR_EQUIP_DIAMOND;
        var mat = material.get();
        return switch (mat.texture) {
            case "soft" -> SoundEvents.ARMOR_EQUIP_WOLF;
            case "leather" -> SoundEvents.ARMOR_EQUIP_LEATHER;
            case "base" -> SoundEvents.ARMOR_EQUIP_IRON;
            case "shiny" -> SoundEvents.ARMOR_EQUIP_DIAMOND;
            case "metal" -> SoundEvents.ARMOR_EQUIP_NETHERITE;
            default -> SoundEvents.ARMOR_EQUIP_GENERIC;
        };
    }

    public Holder<ArmorMaterial> getPotentialArmorMaterials() {
        return ArmorMaterials.LEATHER;
    }

    @Override
    public @Nullable EquipmentSlot getEquipmentSlot(@Nonnull ItemStack stack) {
        var slot = Traits.of(stack).equipmentSlot;
        return slot != null ? slot : super.getEquipmentSlot(stack);
    }

    public @Nonnull InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (!isArmor(stack)) return super.use(level, player, hand);
        return swapWithEquipmentSlot(stack, level, player, hand);
    }

    public List<ResourceLocation> getArmorTextures(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        var list = new ArrayList<ResourceLocation>();
        var num = innerModel ? 2 : 1;

        var parts = getDynamicParts(stack);
        if (parts.isEmpty()) return list;
        for (var pair : parts) {
            var partStack = pair.getFirst();
            var partDynamic = pair.getSecond();
            var material = partDynamic.getMaterialType(partStack);
            if (material.isEmpty()) return list;
            var part = partDynamic.getPartType(partStack);
            if (part.isEmpty()) return list;
            var p = part.get();
            var m = material.get();
            var pPath = p.id.getPath();
            var bool = pPath.contains("helmet") || pPath.contains("chestplate") || pPath.contains("leggings") || pPath.contains("boots");
            var prefix = bool ? "" : pPath + "/";
            var path = "armors/" + prefix + m.texture + "_layer_" + num + "_" + m.id.getPath();
            if (bool) list.addFirst(SlagEmbers.loc(p.id.getNamespace(), path));
            else list.add(SlagEmbers.loc(p.id.getNamespace(), path));
        }
        if (list.isEmpty()) list.add(SlagEmbers.loc("textures/armors/metal_layer_" + num + ".png"));
        return list;
    }

    public static final DispenseItemBehavior DISPENSE_ITEM_BEHAVIOR = new DefaultDispenseItemBehavior() {
        @Override
        protected @Nonnull ItemStack execute(@Nonnull BlockSource source, @Nonnull ItemStack stack) {
            if (Traits.of(stack).equipmentSlot == null) return super.execute(source, stack);
            return ArmorItem.dispenseArmor(source, stack) ? stack : super.execute(source, stack);
        }
    };

    public InteractionResultHolder<ItemStack> swapWithEquipmentSlot(ItemStack stack, Level level, Player player, InteractionHand hand) {
        var equipmentslot = player.getEquipmentSlotForItem(stack);
        if (!player.canUseSlot(equipmentslot)) return InteractionResultHolder.pass(stack);
        var slotStack = player.getItemBySlot(equipmentslot);
        if ((!EnchantmentHelper.has(slotStack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE) || player.isCreative()) && !ItemStack.matches(stack, slotStack)) {
            if (!level.isClientSide()) player.awardStat(Stats.ITEM_USED.get(this));

            var stackA = slotStack.isEmpty() ? stack : slotStack.copyAndClear();
            var stackB = player.isCreative() ? stack.copy() : stack.copyAndClear();
            player.setItemSlot(equipmentslot, stackB);
            return InteractionResultHolder.sidedSuccess(stackA, level.isClientSide());
        }
        return InteractionResultHolder.fail(stack);
    }
}
