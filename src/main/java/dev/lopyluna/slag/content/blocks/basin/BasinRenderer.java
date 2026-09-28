package dev.lopyluna.slag.content.blocks.basin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lopyluna.slag.client.AlphaBufferSource;
import dev.lopyluna.slag.content.blocks.casting.CastingBE;
import dev.lopyluna.slag.content.blocks.casting.CastingRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;

@SuppressWarnings("unused")
public class BasinRenderer extends CastingRenderer<BasinBE> {
    public BasinRenderer(BlockEntityRendererProvider.Context context) {
        super(1 / 16f);
    }

    @Override
    protected void renderSafe(BasinBE be, float pt, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        renderFluid(be, pt, ms, buffer, light);
        var stack = be.itemInventory.getItem(CastingBE.RESULT);
        var alpha = 1f;
        if (stack.isEmpty()) {
            stack = be.preview;
            alpha = coolingAlpha(be);
        }
        if (stack.isEmpty()) return;

        ms.pushPose();
        ms.translate(0.5f, 0.575f, 0.5f);
        ms.scale(1.5f, 1.5f, 1.5f);
        mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, ms, new AlphaBufferSource(buffer, alpha), mc.level, 0);
        ms.popPose();
    }
}
