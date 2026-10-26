package dev.lopyluna.slag.content.blocks.multiblock.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lopyluna.slag.content.blocks.smart.CachedRenderBBBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings({"unused", "SameParameterValue"})
public abstract class SafeBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, SafeBlockEntityRenderer.State<T>> {
    @Override
    public @Nonnull State<T> createRenderState() {
        return new State<>();
    }

    @Override
    public void extractRenderState(@Nonnull T be, @Nonnull State<T> state, float partialTicks, @Nonnull Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        state.be = isInvalid(be) ? null : be;
        state.partialTicks = partialTicks;
    }

    @Override
    public final void submit(@Nonnull State<T> state, @Nonnull PoseStack ms, @Nonnull SubmitNodeCollector collector, @Nonnull CameraRenderState camera) {
        if (state.be == null) return;
        renderSafe(state.be, state.partialTicks, ms, collector, state.lightCoords, OverlayTexture.NO_OVERLAY);
    }

    protected abstract void renderSafe(T be, float partialTicks, PoseStack ms, SubmitNodeCollector collector, int light, int overlay);

    public boolean isInvalid(T be) {
        return !be.hasLevel() || be.getBlockState().getBlock() == Blocks.AIR;
    }

    public static void renderItem(ItemStack stack, ItemDisplayContext context, PoseStack ms, SubmitNodeCollector collector, int light, int overlay) {
        var mc = Minecraft.getInstance();
        var state = new ItemStackRenderState();
        mc.getItemModelResolver().updateForTopItem(state, stack, context, mc.level, null, 0);
        state.submit(ms, collector, light, overlay, 0);
    }

    @Override
    public @Nonnull AABB getRenderBoundingBox(@Nonnull T blockEntity) {
        if (blockEntity instanceof CachedRenderBBBlockEntity cbe) return cbe.getRenderBoundingBox();
        return BlockEntityRenderer.super.getRenderBoundingBox(blockEntity);
    }

    public static class State<T> extends BlockEntityRenderState {
        public @Nullable T be;
        public float partialTicks;
    }
}
