package dev.lopyluna.slag.content.blocks.multiblock.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lopyluna.slag.content.blocks.smart.CachedRenderBBBlockEntity;
import dev.lopyluna.slag.mixin.LevelRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public abstract class SafeBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    @Override
    public final void render(@Nonnull T be, float partialTicks, @Nonnull PoseStack ms, @Nonnull MultiBufferSource bufferSource, int light, int overlay) {
        if (isInvalid(be)) return;
        renderSafe(be, partialTicks, ms, bufferSource, light, overlay);
    }

    protected abstract void renderSafe(T be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay);

    public boolean isInvalid(T be) {
        return !be.hasLevel() || be.getBlockState().getBlock() == Blocks.AIR;
    }

    public boolean shouldCullItem(Vec3 itemPos, Level level) {
        LevelRendererAccessor accessor = (LevelRendererAccessor) Minecraft.getInstance().levelRenderer;
        Frustum frustum = accessor.slag$getCapturedFrustum() != null ? accessor.slag$getCapturedFrustum() : accessor.slag$getCullingFrustum();
        AABB itemBB = new AABB(itemPos.x - 0.25, itemPos.y - 0.25, itemPos.z - 0.25, itemPos.x + 0.25, itemPos.y + 0.25, itemPos.z + 0.25);
        return frustum != null && !frustum.isVisible(itemBB);
    }

    @Override
    public @Nonnull AABB getRenderBoundingBox(@Nonnull T blockEntity) {
        if (blockEntity instanceof CachedRenderBBBlockEntity cbe) return cbe.getRenderBoundingBox();
        return BlockEntityRenderer.super.getRenderBoundingBox(blockEntity);
    }
}
