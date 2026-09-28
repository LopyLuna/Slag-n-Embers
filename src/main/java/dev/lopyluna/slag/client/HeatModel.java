package dev.lopyluna.slag.client;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.temperature.Temperatures.Heat;
import dev.lopyluna.slag.content.temperature.Temperatures.Tiers;
import dev.lopyluna.slag.content.temperature.Temperatures.Type;
import dev.lopyluna.slag.register.AllBlocks;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.util.TriState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

public class HeatModel extends BakedModelWrapper<BakedModel> {
    private final TextureAtlasSprite[] from;
    private final TextureAtlasSprite[][] to;
    private final List<BakedQuad>[][] quads;

    @SuppressWarnings("unchecked")
    public HeatModel(BakedModel model, TextureAtlasSprite[] from, TextureAtlasSprite[][] to) {
        super(model);
        this.from = from;
        this.to = to;
        quads = new List[to.length][7];
    }

    public static void wrap(ModelEvent.ModifyBakingResult event) {
        wrap(event, AllBlocks.CRUCIBLE.get(), "crucible_states", "crucible_side", "crucible_side_window", "crucible_inside");
        wrap(event, AllBlocks.MELTER.get(), "melter_states", "melter", "melter_side", "melter_back");
    }

    private static void wrap(ModelEvent.ModifyBakingResult event, Block block, String folder, String... textures) {
        var sprites = event.getTextureGetter();
        var from = new TextureAtlasSprite[textures.length];
        for (var i = 0; i < textures.length; i++) from[i] = sprites.apply(material(textures[i]));

        var to = new TextureAtlasSprite[Heat.VISUALS][textures.length];
        for (var type : Type.values()) for (var tier : Tiers.values()) {
            var heat = Heat.of(tier, type);
            if (type == Type.NONE || heat.visual < 0) continue;
            for (var i = 0; i < textures.length; i++) to[heat.visual][i] = sprites.apply(material(folder + "/" + type.id + "_" + tier.id + "_" + textures[i]));
        }

        var models = event.getModels();
        var wrapped = new IdentityHashMap<BakedModel, BakedModel>();
        for (var state : block.getStateDefinition().getPossibleStates()) {
            var location = BlockModelShaper.stateToModelLocation(state);
            var model = models.get(location);
            if (model != null) models.put(location, wrapped.computeIfAbsent(model, base -> new HeatModel(base, from, to)));
        }
    }

    private static Material material(String path) {
        return new Material(InventoryMenu.BLOCK_ATLAS, SlagEmbers.loc("block/" + path));
    }

    @Override
    public @Nonnull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand, @Nonnull ModelData data, @Nullable RenderType renderType) {
        var visual = data.get(Heat.VISUAL);
        if (visual == null) return super.getQuads(state, side, rand, data, renderType);
        var index = side == null ? 6 : side.ordinal();
        var cached = quads[visual][index];
        if (cached != null) return cached;

        var sprites = to[visual];
        var list = new ArrayList<BakedQuad>();
        for (var quad : super.getQuads(state, side, rand, data, renderType)) list.add(retexture(quad, sprites));
        return quads[visual][index] = list;
    }

    @Override
    public @Nonnull ModelData getModelData(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull ModelData data) {
        if (data.get(Heat.VISUAL) != null || data == ModelData.EMPTY && !(level instanceof Level world && world.getModelDataManager() == null)) return super.getModelData(level, pos, state, data);
        var be = level.getBlockEntity(pos);
        return be == null ? super.getModelData(level, pos, state, data) : be.getModelData();
    }

    @Override
    public @Nonnull TriState useAmbientOcclusion(@Nonnull BlockState state, @Nonnull ModelData data, @Nonnull RenderType renderType) {
        return state.is(AllBlocks.CRUCIBLE) ? TriState.TRUE : super.useAmbientOcclusion(state, data, renderType);
    }

    private BakedQuad retexture(BakedQuad quad, TextureAtlasSprite[] sprites) {
        var sprite = quad.getSprite();
        for (var i = 0; i < from.length; i++) {
            if (sprite != from[i]) continue;
            var target = sprites[i];
            var vertices = quad.getVertices().clone();
            for (var vertex = 0; vertex < 4; vertex++) {
                var offset = vertex * IQuadTransformer.STRIDE + IQuadTransformer.UV0;
                vertices[offset] = Float.floatToRawIntBits(target.getU(sprite.getUOffset(Float.intBitsToFloat(vertices[offset]))));
                vertices[offset + 1] = Float.floatToRawIntBits(target.getV(sprite.getVOffset(Float.intBitsToFloat(vertices[offset + 1]))));
            }
            return new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), target, quad.isShade(), quad.hasAmbientOcclusion());
        }
        return quad;
    }
}
