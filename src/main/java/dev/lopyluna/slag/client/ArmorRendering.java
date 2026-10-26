package dev.lopyluna.slag.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.items.modular.ModularEquipablesItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

public class ArmorRendering {
    private static final ResourceKey<EquipmentAsset> ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, SlagEmbers.loc("modular"));

    public static <S extends HumanoidRenderState> boolean renderArmorPiece(HumanoidModel<S> model, PoseStack pose, SubmitNodeCollector collector, ItemStack stack, EquipmentSlot slot, int light, S state) {
        if (!(stack.getItem() instanceof ModularEquipablesItem item)) return false;
        if (item.getEquipmentSlot(stack) != slot) return true;
        var textures = item.getArmorTextures(stack, slot, slot == EquipmentSlot.LEGS);
        var order = 1;
        var foil = stack.hasFoil();
        for (var texture : textures) {
            if (texture == null) continue;
            if (texture.getPath().endsWith(".png")) collector.order(order++).submitModel(model, state, pose, RenderTypes.armorCutoutNoCull(texture), light, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
            else {
                var sprite = MaterialTextures.getSprite(texture);
                collector.order(order++).submitModel(model, state, pose, RenderTypes.armorCutoutNoCull(sprite.atlasLocation()), light, OverlayTexture.NO_OVERLAY, -1, sprite, state.outlineColor, null);
            }
            if (!foil) continue;
            collector.order(order++).submitModel(model, state, pose, RenderTypes.armorEntityGlint(), light, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
            foil = false;
        }

        var trim = stack.get(DataComponents.TRIM);
        var baby = state.isBaby && state.entityType != EntityType.ARMOR_STAND;
        if (trim == null || baby) return true;
        var layer = slot == EquipmentSlot.LEGS ? EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS : EquipmentClientInfo.LayerType.HUMANOID;
        var sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.ARMOR_TRIMS).getSprite(trim.layerAssetId(layer.trimAssetPrefix(), ASSET));
        collector.order(order).submitModel(model, state, pose, Sheets.armorTrimsSheet(trim.pattern().value().decal()), light, OverlayTexture.NO_OVERLAY, -1, sprite, state.outlineColor, null);
        return true;
    }
}
