package dev.lopyluna.slag.content.blocks.table;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.lopyluna.slag.client.AlphaBufferSource;
import dev.lopyluna.slag.content.blocks.casting.CastingBE;
import dev.lopyluna.slag.content.blocks.casting.CastingRenderer;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;

@SuppressWarnings("unused")
public class TableRenderer extends CastingRenderer<TableBE> {
    public TableRenderer(BlockEntityRendererProvider.Context context) {
        super(5f / 16f);
    }

    @Override
    protected void renderSafe(TableBE be, float pt, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        renderFluid(be, pt, ms, buffer, light);
        var state = be.getBlockState();
        var facing = state.hasProperty(TableBlock.FACING) ? state.getValue(TableBlock.FACING) : Direction.NORTH;
        var angle = AngleHelper.horizontalAngle(facing.getAxis() == Direction.Axis.Z ? facing.getOpposite() : facing);
        renderResultItem(be, angle, ms, buffer, light, overlay);
        renderMoldItem(be, angle, ms, buffer, light, overlay);
    }

    public void renderMoldItem(TableBE be, float angle, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        if (be.moldCutout.isEmpty()) return;
        var ir = mc.getItemRenderer();

        ms.pushPose();
        ms.translate(0.5f, 0.625f, 0.5f);
        ms.mulPose(Axis.XP.rotationDegrees(90));
        ms.mulPose(Axis.ZP.rotationDegrees(angle));
        ms.scale(12.1f/16f, 12f/16f, 12.1f/16f);

        ms.pushPose();
        ms.translate(0f, 0f, 0.1f);
        ir.renderStatic(be.moldBase, ItemDisplayContext.FIXED, light, overlay, ms, buffer, mc.level, 0);
        ms.popPose();

        ms.pushPose();
        ms.scale(1f, 1f, 2.6f);
        ms.translate(0f, 0f, -0.002f);
        ir.renderStatic(be.moldCutout, ItemDisplayContext.FIXED, light, overlay, ms, buffer, mc.level, 0);
        ms.popPose();

        ms.popPose();
    }

    public void renderResultItem(TableBE be, float angle, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        var stack = be.itemInventory.getItem(CastingBE.RESULT);
        var alpha = 1f;
        if (stack.isEmpty()) {
            stack = be.preview;
            alpha = coolingAlpha(be);
        }
        if (stack.isEmpty()) return;

        ms.pushPose();
        ms.translate(0.5f, 0.672f, 0.5f);
        ms.mulPose(Axis.XP.rotationDegrees(90));
        ms.mulPose(Axis.ZP.rotationDegrees(angle));
        ms.scale(12.1f/16f, 12f/16f, 12.1f/16f);
        mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, ms, new AlphaBufferSource(buffer, alpha), mc.level, 0);
        ms.popPose();
    }
}
