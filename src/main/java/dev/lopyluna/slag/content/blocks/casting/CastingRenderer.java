package dev.lopyluna.slag.content.blocks.casting;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lopyluna.slag.client.SolidBufferSource;
import dev.lopyluna.slag.content.blocks.multiblock.renderer.SafeBlockEntityRenderer;
import net.createmod.catnip.platform.NeoForgeCatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

public abstract class CastingRenderer<T extends CastingBE> extends SafeBlockEntityRenderer<T> {
    private static final float HULL_WIDTH = 0.1f / 16f + 1 / 128f;
    private static final float PUDDLE_HEIGHT = 4 / 16f;

    protected final Minecraft mc = Minecraft.getInstance();
    private final float capHeight;
    private final float totalHeight;

    public CastingRenderer(float capHeight) {
        this.capHeight = capHeight;
        totalHeight = 1 - 2 * capHeight - PUDDLE_HEIGHT;
    }

    public float coolingAlpha(T be) {
        return be.coolingTarget == 0 ? 0f : Mth.clamp((float) be.coolingProgress / be.coolingTarget - 0.2f, 0f, 1f);
    }

    public void renderFluid(T be, float pt, PoseStack ms, MultiBufferSource buffer, int light) {
        var fluidLevel = be.fluidLevel;
        if (fluidLevel == null) return;
        var fluidStack = be.tankInventory.getFluid();
        if (fluidStack.isEmpty()) return;

        float level = fluidLevel.getValue(pt) + 0.0225f / 16f;
        if (level < 1 / (512f * totalHeight)) return;
        float clampedLevel = Mth.clamp(level * totalHeight, 0, totalHeight);
        float max = 1 - HULL_WIDTH, yMin = totalHeight + capHeight + PUDDLE_HEIGHT - clampedLevel, yMax = yMin + clampedLevel;

        ms.pushPose();
        ms.translate(0, clampedLevel - totalHeight, 0);
        NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(fluidStack, HULL_WIDTH, yMin, HULL_WIDTH, max, yMax, max, new SolidBufferSource(buffer).getBuffer(RenderType.SOLID), ms, light, false, true);
        ms.popPose();
    }
}
