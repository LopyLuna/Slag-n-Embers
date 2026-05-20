package dev.lopyluna.slag.content.items.dynamic_part;

import com.tterrag.registrate.providers.RegistrateLangProvider;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.client.render.SimpleCustomRenderer;
import dev.lopyluna.slag.content.types.MaterialType;
import dev.lopyluna.slag.content.types.PartType;
import dev.lopyluna.slag.register.AllDataComponents;
import dev.lopyluna.slag.register.AllDynamicTypes;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class DynamicPartItem extends Item implements IDynamicPart {
    public DynamicPartItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (getMaterialType(stack).isEmpty() || getPartType(stack).isEmpty()) stack.setCount(0);
    }

    @Override
    public Optional<MaterialType> getMaterialType(ItemStack stack) {
        return AllDynamicTypes.getMaterial(stack.get(AllDataComponents.MATERIAL_TYPE));
    }

    public void setMaterialType(ItemStack stack, MaterialType materialType) {
        stack.set(AllDataComponents.MATERIAL_TYPE, materialType.id);
    }

    @Override
    public Optional<PartType> getPartType(ItemStack stack) {
        return AllDynamicTypes.getPart(stack.get(AllDataComponents.PART_TYPE));
    }

    public void setPartType(ItemStack stack, PartType partType) {
        stack.set(AllDataComponents.PART_TYPE, partType.id);
    }

    @Override
    public @NotNull String getDescriptionId(ItemStack stack) {
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
        return Util.makeDescriptionId("item", SlagEmbers.loc(mod, "part_" + matID + "_" + partID));
    }

    @Override
    public @NotNull Component getName(ItemStack stack) {
        var id = getDescriptionId(stack);
        var name = id.split("\\.")[2];
        return Component.translatableWithFallback(id, RegistrateLangProvider.toEnglishName(name));
    }

    @SuppressWarnings("removal")
    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new DynamicPartRenderer()));
    }
}
