package dev.lopyluna.slag.client;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.temperature.Temperatures.Heat;
import dev.lopyluna.slag.content.temperature.Temperatures.Tiers;
import dev.lopyluna.slag.content.temperature.Temperatures.Type;
import dev.lopyluna.slag.register.AllBlocks;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TriState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HeatModel extends DelegateBlockStateModel {
    private final TextureAtlasSprite[] from;
    private final TextureAtlasSprite[][] to;
    private final boolean occlusion;
    private final Map<BlockStateModelPart, Part>[] parts;
    private final Map<BlockStateModelPart, Part> plain = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public HeatModel(BlockStateModel model, TextureAtlasSprite[] from, TextureAtlasSprite[][] to, boolean occlusion) {
        super(model);
        this.from = from;
        this.to = to;
        this.occlusion = occlusion;
        parts = new Map[to.length];
        for (var i = 0; i < parts.length; i++) parts[i] = new ConcurrentHashMap<>();
    }

    public static void wrap(ModelEvent.ModifyBakingResult event) {
        wrap(event, AllBlocks.CRUCIBLE.get(), "crucible_states", "crucible_side", "crucible_side_window", "crucible_inside");
        wrap(event, AllBlocks.MELTER.get(), "melter_states", "melter", "melter_side", "melter_back");
    }

    private static void wrap(ModelEvent.ModifyBakingResult event, Block block, String folder, String... textures) {
        var sprites = event.getTextureGetter();
        var from = new TextureAtlasSprite[textures.length];
        for (var i = 0; i < textures.length; i++) from[i] = sprites.apply(SlagEmbers.loc("block/" + textures[i]));

        var to = new TextureAtlasSprite[Heat.VISUALS][textures.length];
        for (var type : Type.values()) for (var tier : Tiers.values()) {
            var heat = Heat.of(tier, type);
            if (type == Type.NONE || heat.visual < 0) continue;
            for (var i = 0; i < textures.length; i++) to[heat.visual][i] = sprites.apply(SlagEmbers.loc("block/" + folder + "/" + type.id + "_" + tier.id + "_" + textures[i]));
        }

        var models = event.getBakingResult().blockStateModels();
        var occlusion = block == AllBlocks.CRUCIBLE.get();
        var wrapped = new IdentityHashMap<BlockStateModel, BlockStateModel>();
        for (var state : block.getStateDefinition().getPossibleStates()) {
            var model = models.get(state);
            if (model != null) models.put(state, wrapped.computeIfAbsent(model, base -> new HeatModel(base, from, to, occlusion)));
        }
    }

    @Override
    public void collectParts(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull RandomSource random, @Nonnull List<BlockStateModelPart> output) {
        var visual = visual(level, pos);
        if (visual == null && !occlusion) {
            super.collectParts(level, pos, state, random, output);
            return;
        }
        var collected = new ArrayList<BlockStateModelPart>();
        super.collectParts(level, pos, state, random, collected);
        var cache = visual == null ? plain : parts[visual];
        for (var part : collected) output.add(cache.computeIfAbsent(part, base -> new Part(base, visual == null ? null : to[visual])));
    }

    private static @Nullable Integer visual(BlockAndTintGetter level, BlockPos pos) {
        var visual = level.getModelData(pos).get(Heat.VISUAL);
        if (visual != null) return visual;
        var be = level.getBlockEntity(pos);
        return be == null ? null : be.getModelData().get(Heat.VISUAL);
    }

    private BakedQuad retexture(BakedQuad quad, TextureAtlasSprite[] sprites) {
        var info = quad.materialInfo();
        var sprite = info.sprite();
        for (var i = 0; i < from.length; i++) {
            if (sprite != from[i]) continue;
            var target = sprites[i];
            if (target == null) return quad;
            return new BakedQuad(quad.position0(), quad.position1(), quad.position2(), quad.position3(),
                    uv(quad.packedUV0(), sprite, target), uv(quad.packedUV1(), sprite, target), uv(quad.packedUV2(), sprite, target), uv(quad.packedUV3(), sprite, target),
                    quad.direction(), new BakedQuad.MaterialInfo(target, info.layer(), info.itemRenderType(), info.tintIndex(), info.shade(), info.lightEmission(), info.ambientOcclusion()),
                    quad.bakedNormals(), quad.bakedColors());
        }
        return quad;
    }

    private static long uv(long packed, TextureAtlasSprite from, TextureAtlasSprite to) {
        var u = (UVPair.unpackU(packed) - from.getU0()) / (from.getU1() - from.getU0());
        var v = (UVPair.unpackV(packed) - from.getV0()) / (from.getV1() - from.getV0());
        return UVPair.pack(to.getU(u), to.getV(v));
    }

    private class Part implements BlockStateModelPart {
        private final BlockStateModelPart part;
        private final @Nullable TextureAtlasSprite[] sprites;
        private final List<?>[] quads = new List<?>[7];

        private Part(BlockStateModelPart part, @Nullable TextureAtlasSprite[] sprites) {
            this.part = part;
            this.sprites = sprites;
        }

        @SuppressWarnings("unchecked")
        @Override
        public @Nonnull List<BakedQuad> getQuads(@Nullable Direction direction) {
            if (sprites == null) return part.getQuads(direction);
            var index = direction == null ? 6 : direction.ordinal();
            var cached = quads[index];
            if (cached != null) return (List<BakedQuad>) cached;
            var list = new ArrayList<BakedQuad>();
            for (var quad : part.getQuads(direction)) list.add(retexture(quad, sprites));
            quads[index] = list;
            return list;
        }

        @SuppressWarnings("deprecation")
        @Override
        public boolean useAmbientOcclusion() {
            return occlusion || part.useAmbientOcclusion();
        }

        @Override
        public @Nonnull TriState ambientOcclusion() {
            return occlusion ? TriState.TRUE : part.ambientOcclusion();
        }

        @Override
        public @Nonnull Material.Baked particleMaterial() {
            return part.particleMaterial();
        }

        @Override
        public int materialFlags() {
            return part.materialFlags();
        }
    }
}
