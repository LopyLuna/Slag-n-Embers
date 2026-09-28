package dev.lopyluna.slag.content.blocks.crucible;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lopyluna.slag.client.FluidRenderHelper;
import dev.lopyluna.slag.compat.sable.SableCompat;
import dev.lopyluna.slag.content.blocks.multiblock.renderer.SafeBlockEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "NullableProblems"})
public class CrucibleRenderer extends SafeBlockEntityRenderer<CrucibleBE> {

    public CrucibleRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(CrucibleBE be, float pt, PoseStack ms, MultiBufferSource bs, int light, int overlay) {
        if (!be.isController) return;
        if (be.fluidLevel == null) return;

        float capHeight = CrucibleBE.CAP_HEIGHT;
        float tankHullWidth = CrucibleBE.HULL_WIDTH;
        float minPuddleHeight = CrucibleBE.PUDDLE_HEIGHT;
        float totalHeight = be.getHeight() - 2 * capHeight - minPuddleHeight;

        float level = be.fluidLevel.getValue(pt) + 0.0225f / 16f;
        if (level < 1 / (512f * totalHeight)) return;
        float clampedLevel = Mth.clamp(level * totalHeight, 0f, totalHeight);

        float xMax = tankHullWidth + be.getWidthX() - 2f * tankHullWidth;
        float zMax = tankHullWidth + be.getWidthZ() - 2f * tankHullWidth;
        float yBase = totalHeight + capHeight + minPuddleHeight - clampedLevel;
        List<FluidStack> liquids = new ArrayList<>();
        int totalMb = 0;
        for (FluidStack f : be.tankInventory.getFluids()) {
            if (f.isEmpty()) continue;
            totalMb += f.getAmount();
            liquids.add(f);
        }

        if (totalMb == 0) return;

        float unit = clampedLevel / (float) totalMb;
        final float eps = 1f / 512f;

        ms.pushPose();
        ms.translate(0, clampedLevel - totalHeight, 0);

        var window = be.windowVisible();
        var pos = be.getBlockPos();
        var mc = Minecraft.getInstance();
        var eye = be.getLevel() != mc.level ? null : SableCompat.renderLocal(be, mc.gameRenderer.getMainCamera().getPosition(), pt).subtract(pos.getX(), pos.getY() + clampedLevel - totalHeight, pos.getZ());
        var builder = FluidRenderHelper.getFluidBuilder(bs);
        float yCur = yBase;
        for (int i = 0; i < liquids.size(); i++) {
            FluidStack f = liquids.get(i);
            float h = Math.max(0f, f.getAmount() * unit);
            if (i == liquids.size() - 1) h = yBase + clampedLevel - yCur;

            if (h > eps) {
                FluidRenderHelper.renderFluidFaces(f, tankHullWidth, yCur, tankHullWidth, xMax, yCur + h, zMax, builder, ms, light, window && yCur == yBase, true, window, eye);
                yCur += h;
            }
        }
        ms.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(CrucibleBE be) {
        return be.isController;
    }

    @Override
    public boolean shouldRender(CrucibleBE be, Vec3 cameraPos) {
        var distance = getViewDistance();
        return be.getRenderBoundingBox().distanceToSqr(cameraPos) < distance * distance;
    }
}