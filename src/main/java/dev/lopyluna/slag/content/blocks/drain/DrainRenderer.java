package dev.lopyluna.slag.content.blocks.drain;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lopyluna.slag.client.FluidRenderHelper;
import dev.lopyluna.slag.compat.sable.SableCompat;
import dev.lopyluna.slag.config.SlagServerConfigs;
import dev.lopyluna.slag.content.blocks.multiblock.renderer.SafeBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class DrainRenderer extends SafeBlockEntityRenderer<DrainBE> {
    private static final float TOP = 10f / 16f;
    public DrainRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    protected void renderSafe(DrainBE be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        var fluid = be.drainingFluid;
        if (fluid == null || fluid.isEmpty() || be.drainState == null || be.drainState == DrainState.OFF || be.target == null) return;
        var level = be.getLevel();
        if (level == null) return;
        var pos = be.getBlockPos();
        var hit = be.renderHit();
        if (hit == null) return;
        var length = SableCompat.toWorld(level, Vec3.atBottomCenterOf(pos).add(0, TOP, 0)).y - hit.world().y;
        var down = SableCompat.localDown(level, pos);
        var len = (float) (length * down.length());

        ms.pushPose();
        ms.translate(0.5, TOP, 0.5);
        ms.mulPose(new Quaternionf().rotationTo(0, -1, 0, (float) down.x, (float) down.y, (float) down.z));
        FluidRenderHelper.renderFlowingFluidBox(fluid, -2f / 16f, -len, -2f / 16f, 2f / 16f, 0, 2f / 16f, buffer, ms, light, true, true);
        ms.popPose();
    }

    @Override
    public @Nonnull AABB getRenderBoundingBox(@Nonnull DrainBE be) {
        var box = new AABB(be.getBlockPos());
        return be.target == null ? box : box.inflate(SlagServerConfigs.DRAIN_RANGE.get());
    }
}
