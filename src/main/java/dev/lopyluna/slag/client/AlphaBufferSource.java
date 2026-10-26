package dev.lopyluna.slag.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

@SuppressWarnings("NullableProblems")
@ParametersAreNonnullByDefault
public record AlphaBufferSource(SubmitNodeCollector parent, float alpha) implements SubmitNodeCollector {
    @Override
    public void submitItem(PoseStack ms, ItemDisplayContext context, int light, int overlay, int outline, int[] tints, List<BakedQuad> quads, ItemStackRenderState.FoilType foil) {
        if (alpha >= 1f) {
            parent.submitItem(ms, context, light, overlay, outline, tints, quads, foil);
            return;
        }
        if (alpha <= 0f) return;
        var groups = new IdentityHashMap<RenderType, List<BakedQuad>>();
        for (var quad : quads) groups.computeIfAbsent(translucent(quad.materialInfo().itemRenderType()), _ -> new ArrayList<>()).add(quad);
        for (var entry : groups.entrySet()) parent.submitCustomGeometry(ms, entry.getKey(), (pose, buffer) -> {
            var instance = new QuadInstance();
            instance.setLightCoords(light);
            instance.setOverlayCoords(overlay);
            for (var quad : entry.getValue()) {
                var tint = quad.materialInfo().tintIndex();
                instance.setColor(ARGB.multiplyAlpha(tint >= 0 && tint < tints.length ? tints[tint] : -1, alpha));
                buffer.putBakedQuad(pose, quad, instance);
            }
        });
    }

    private static RenderType translucent(RenderType type) {
        if (type == Sheets.cutoutItemSheet()) return Sheets.translucentItemSheet();
        if (type == Sheets.cutoutBlockItemSheet() || type == Sheets.cutoutBlockSheet()) return Sheets.translucentBlockItemSheet();
        return type;
    }

    @Override public OrderedSubmitNodeCollector order(int order) { return parent.order(order); }
    @Override public void submitShadow(PoseStack ms, float radius, List<EntityRenderState.ShadowPiece> pieces) { parent.submitShadow(ms, radius, pieces); }
    @Override public void submitNameTag(PoseStack ms, @Nullable Vec3 attachment, int offset, Component name, boolean seeThrough, int light, double distance, CameraRenderState camera) { parent.submitNameTag(ms, attachment, offset, name, seeThrough, light, distance, camera); }
    @Override public void submitText(PoseStack ms, float x, float y, FormattedCharSequence string, boolean shadow, Font.DisplayMode mode, int light, int color, int background, int outline) { parent.submitText(ms, x, y, string, shadow, mode, light, color, background, outline); }
    @Override public void submitFlame(PoseStack ms, EntityRenderState state, Quaternionf rotation) { parent.submitFlame(ms, state, rotation); }
    @Override public void submitLeash(PoseStack ms, EntityRenderState.LeashState state) { parent.submitLeash(ms, state); }
    @Override public <S> void submitModel(Model<? super S> model, S state, PoseStack ms, RenderType type, int light, int overlay, int tint, @Nullable TextureAtlasSprite sprite, int outline, @Nullable ModelFeatureRenderer.CrumblingOverlay crumbling) { parent.submitModel(model, state, ms, type, light, overlay, tint, sprite, outline, crumbling); }
    @Override public void submitModelPart(ModelPart part, PoseStack ms, RenderType type, int light, int overlay, @Nullable TextureAtlasSprite sprite, boolean sheeted, boolean foil, int tint, @Nullable ModelFeatureRenderer.CrumblingOverlay crumbling, int outline) { parent.submitModelPart(part, ms, type, light, overlay, sprite, sheeted, foil, tint, crumbling, outline); }
    @Override public void submitMovingBlock(PoseStack ms, MovingBlockRenderState state) { parent.submitMovingBlock(ms, state); }
    @Override public void submitBlockModel(PoseStack ms, RenderType type, List<BlockStateModelPart> parts, int[] tints, int light, int overlay, int outline) { parent.submitBlockModel(ms, type, parts, tints, light, overlay, outline); }
    @Override public void submitBreakingBlockModel(PoseStack ms, BlockStateModel model, long seed, int progress) { parent.submitBreakingBlockModel(ms, model, seed, progress); }
    @Override public void submitCustomGeometry(PoseStack ms, RenderType type, CustomGeometryRenderer renderer) { parent.submitCustomGeometry(ms, type, renderer); }
    @Override public void submitParticleGroup(ParticleGroupRenderer renderer) { parent.submitParticleGroup(renderer); }
    @Override public void submitMultiLayerBlockModel(PoseStack ms, List<BlockStateModelPart> parts, boolean translucent, int[] tints, int light, int overlay, int outline) { parent.submitMultiLayerBlockModel(ms, parts, translucent, tints, light, overlay, outline); }
}
