package dev.lopyluna.slag.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public abstract class CustomRenderedItemModelRenderer extends BlockEntityWithoutLevelRenderer {
    public final Minecraft mc = Minecraft.getInstance();
    @SuppressWarnings("all") public CustomRenderedItemModelRenderer() {
        super(null, null);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        var itemRenderer = mc.getItemRenderer();
        if (!(itemRenderer.getModel(stack, null, null, 0) instanceof CustomRenderedItemModel mainModel)) return;
        var renderer = PartialItemModelRenderer.of(stack, transformType, ms, buffer, overlay);
        ms.pushPose();
        ms.translate(0.5F, 0.5F, 0.5F);
        render(stack, itemRenderer, mainModel, renderer, transformType, ms, buffer, light, overlay);
        ms.popPose();
    }

    public static BakedModel getModel(ItemStack stack, ClientLevel level, Player player, ResourceLocation location, ModelManager manager) {
        var model = getModel(location, manager);
        model = model.getOverrides().resolve(model, stack, level, player, 0);
        return model;
    }

    @SuppressWarnings("ConstantValue")
    public static BakedModel getModel(ResourceLocation location, ModelManager manager) {
        var model = manager.getModel(ModelResourceLocation.standalone(location));
        return model == null ? manager.getMissingModel() : model;
    }

    protected abstract void render(ItemStack stack, ItemRenderer itemRenderer, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay);
}
