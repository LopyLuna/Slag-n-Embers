package dev.lopyluna.slag.content.items.dynamic_part;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.client.render.SimpleCustomRenderer;
import dev.lopyluna.slag.content.types.MaterialType;
import dev.lopyluna.slag.content.types.PartType;
import dev.lopyluna.slag.register.AllDataComponents;
import dev.lopyluna.slag.register.AllDynamicTypes;
import dev.lopyluna.slag.register.AllLangs;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import javax.annotation.Nonnull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

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
    public @Nonnull String getDescriptionId(ItemStack stack) {
        var material = getMaterialType(stack).orElse(null);
        if (material == null) return super.getDescriptionId(stack);
        var part = getPartType(stack).orElse(null);
        if (part == null) return super.getDescriptionId(stack);
        var materialLoc = material.id;
        var mod = materialLoc.getNamespace();
        if (mod.isEmpty()) return super.getDescriptionId(stack);
        var matID = materialLoc.getPath();
        if (matID.isEmpty()) return super.getDescriptionId(stack);
        var partID = part.id.getPath();
        if (partID.isEmpty()) return super.getDescriptionId(stack);
        return Util.makeDescriptionId("item", SlagEmbers.loc(mod, matID + "_" + partID));
    }

    @Override
    public @Nonnull Component getName(ItemStack stack) {
        var material = getMaterialType(stack).orElse(null);
        var part = getPartType(stack).orElse(null);
        if (material == null || part == null) return super.getName(stack);
        return Component.translatable("item.slag.part_name", material.getName(), part.getName());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        AllLangs.traits(tooltip, getTraits(stack));
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

    @SuppressWarnings("removal")
    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new DynamicPartRenderer()));
    }
}
