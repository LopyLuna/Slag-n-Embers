package dev.lopyluna.slag.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.lopyluna.slag.content.items.modular.ModularEquipablesItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public class ArmorRendering {

    public static <T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> void renderArmorPiece(HumanoidArmorLayer<T, M, A> humanoidArmorLayer,
            Supplier<M> getParentModel, Runnable setPartVisibility, Function<ItemStack, Model> getArmorModelHook, Supplier<Boolean> usesInnerModel,
            PropertyDispatch.QuadFunction<Holder<ArmorMaterial>, ArmorTrim, Model, Boolean, Runnable> renderTrim, Function<Model, Runnable> renderGlint,
            PoseStack pose, MultiBufferSource buffer, T living, EquipmentSlot slot, int light, A pModel,
            float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        var stack = living.getItemBySlot(slot);
        if (stack.getItem() instanceof ModularEquipablesItem item) {
            if (item.getEquipmentSlot(stack) == slot) {
                getParentModel.get().copyPropertiesTo(pModel);
                setPartVisibility.run();
                var model = getArmorModelHook.apply(stack);
                boolean flag = usesInnerModel.get();
                var value = item.getPotentialArmorMaterials().value();

                var extensions = IClientItemExtensions.of(stack);
                extensions.setupModelAnimations(living, stack, slot, model, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch);
                int fallbackColor = extensions.getDefaultDyeColor(stack);

                for (int layerIdx = 0; layerIdx < value.layers().size(); layerIdx++) {
                    var layer = value.layers().get(layerIdx);
                    int j = extensions.getArmorLayerTintColor(stack, living, layer, layerIdx, fallbackColor);

                    var textures = item.getArmorTextures(stack, living, slot, layer, flag);
                    if (textures == null || textures.isEmpty()) break;
                    if (j != 0) for (var texture : textures) if (texture != null) model.renderToBuffer(pose, armorBuffer(buffer, texture), light, OverlayTexture.NO_OVERLAY, j);
                }

                var armortrim = stack.get(DataComponents.TRIM);
                if (armortrim != null) renderTrim.apply(item.getPotentialArmorMaterials(), armortrim, model, flag).run();
                if (stack.hasFoil()) renderGlint.apply(model).run();
            }
            ci.cancel();
        }
    }

    private static VertexConsumer armorBuffer(MultiBufferSource buffer, ResourceLocation texture) {
        if (texture.getPath().endsWith(".png")) return buffer.getBuffer(RenderType.armorCutoutNoCull(texture));
        var sprite = MaterialTextures.getSprite(texture);
        return sprite.wrap(buffer.getBuffer(RenderType.armorCutoutNoCull(sprite.atlasLocation())));
    }
}
