package dev.lopyluna.slag.content.items.dynamic_part;

import dev.lopyluna.slag.content.types.MaterialType;
import dev.lopyluna.slag.content.types.PartType;
import dev.lopyluna.slag.register.AllDataComponents;
import dev.lopyluna.slag.register.AllDynamicTypes;
import dev.lopyluna.slag.register.AllLangs;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import javax.annotation.Nonnull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings("deprecation")
@ParametersAreNonnullByDefault
public class DynamicPartItem extends Item implements IDynamicPart {
    public DynamicPartItem(Properties properties) {
        super(properties);
    }

    @Override
    public Optional<MaterialType> getMaterialType(ItemStack stack) {
        return AllDynamicTypes.getMaterial(stack.get(AllDataComponents.MATERIAL_TYPE));
    }

    public void setMaterialType(ItemStack stack, MaterialType materialType) {
        stack.set(AllDataComponents.MATERIAL_TYPE, materialType.id);
        getTraits(stack).applyComponents(stack);
    }

    @Override
    public Optional<PartType> getPartType(ItemStack stack) {
        return AllDynamicTypes.getPart(stack.get(AllDataComponents.PART_TYPE));
    }

    public void setPartType(ItemStack stack, PartType partType) {
        stack.set(AllDataComponents.PART_TYPE, partType.id);
        getTraits(stack).applyComponents(stack);
    }

    @Override
    public @Nonnull Component getName(ItemStack stack) {
        var material = getMaterialType(stack).orElse(null);
        var part = getPartType(stack).orElse(null);
        if (material == null || part == null) return super.getName(stack);
        return Component.translatable("item.slag.part_name", material.getName(), part.getName());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        var tooltip = new ArrayList<Component>();
        AllLangs.traits(tooltip, getTraits(stack));
        tooltip.forEach(builder);
    }

    @Override
    public boolean canBeHurtBy(ItemStack stack, DamageSource source) {
        return !getTraits(stack).immuneTo(source);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        var size = Math.round(getTraits(stack).maxStackSize);
        return size > 0 ? Mth.clamp(size, 1, 99) : super.getMaxStackSize(stack);
    }


}
