package dev.lopyluna.slag.client;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.createmod.catnip.impl.client.render.TransformingVertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3x2f;

import javax.annotation.Nullable;

@SuppressWarnings("NullableProblems")
public class BlockPreviewRenderer extends PictureInPictureRenderer<BlockPreviewRenderer.State> {
    private static final float ICON_SCALE = 0.625f;
    private final TransformingVertexConsumer transforming = new TransformingVertexConsumer();

    public record State(MovingBlockRenderState block, @Nullable BlockEntityRenderState entityState, Matrix3x2f pose, int x0, int y0, int x1, int y1, float scale, @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds) implements PictureInPictureRenderState {}

    public BlockPreviewRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    public static void submit(GuiGraphicsExtractor g, BlockState state, @Nullable BlockEntity entity, int x, int y, int size) {
        var pose = new Matrix3x2f(g.pose());
        var scissor = g.peekScissorStack();
        var rect = new ScreenRectangle(x, y, size, size).transformMaxBounds(pose);
        var bounds = scissor == null ? rect : scissor.intersection(rect);
        g.submitPictureInPictureRenderState(new State(block(state), entity == null ? null : extract(entity), pose, x, y, x + size, y + size, size * ICON_SCALE, scissor, bounds));
    }

    private static MovingBlockRenderState block(BlockState state) {
        var block = new MovingBlockRenderState();
        block.blockState = state;
        var mc = Minecraft.getInstance();
        if (mc.level == null) return block;
        block.biome = mc.level.getBiome(mc.player == null ? BlockPos.ZERO : mc.player.blockPosition());
        block.cardinalLighting = mc.level.cardinalLighting();
        return block;
    }

    private static <E extends BlockEntity, S extends BlockEntityRenderState> @Nullable S extract(E entity) {
        BlockEntityRenderer<E, S> renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(entity);
        if (renderer == null || !entity.hasLevel()) return null;
        var state = renderer.createRenderState();
        renderer.extractRenderState(entity, state, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true), Vec3.ZERO, null);
        state.blockPos = BlockPos.ZERO;
        state.lightCoords = LightCoordsUtil.FULL_BRIGHT;
        return state;
    }

    @Override
    public Class<State> getRenderStateClass() {
        return State.class;
    }

    @Override
    protected void renderToTexture(State state, PoseStack poseStack) {
        var mc = Minecraft.getInstance();
        mc.gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_3D);
        poseStack.scale(1, -1, -1);
        poseStack.mulPose(Axis.XP.rotationDegrees(30));
        poseStack.mulPose(Axis.YP.rotationDegrees(225));
        poseStack.translate(-0.5f, -0.5f, -0.5f);

        var block = state.block();
        var features = mc.gameRenderer.getFeatureRenderDispatcher();
        var storage = features.getSubmitNodeStorage();
        if (block.blockState.getRenderShape() == RenderShape.MODEL) storage.submitMovingBlock(poseStack, block);
        if (state.entityState() != null) mc.getBlockEntityRenderDispatcher().submit(state.entityState(), poseStack, storage, new CameraRenderState());
        features.renderAllFeatures();

        var fluid = block.blockState.getFluidState();
        if (fluid.isEmpty()) return;
        new FluidRenderer(mc.getModelManager().getFluidStateModelSet()).tesselate(block, BlockPos.ZERO, layer -> {
            transforming.prepare(bufferSource.getBuffer(switch (layer) {
                case SOLID -> RenderTypes.solidMovingBlock();
                case CUTOUT -> RenderTypes.cutoutMovingBlock();
                case TRANSLUCENT -> RenderTypes.translucentMovingBlock();
            }), poseStack);
            return transforming;
        }, block.blockState, fluid);
        bufferSource.endBatch();
        transforming.clear();
    }

    @Override
    protected float getTranslateY(int height, int guiScale) {
        return height / 2f;
    }

    @Override
    protected String getTextureLabel() {
        return "slag_block_preview";
    }
}
