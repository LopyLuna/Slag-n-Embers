package dev.lopyluna.slag.content.items.dynamic_part;

import com.mojang.math.Quadrant;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.client.MaterialTextures;
import dev.lopyluna.slag.client.render.CustomRenderedItemModel;
import dev.lopyluna.slag.client.render.CustomRenderedItemModelRenderer;
import dev.lopyluna.slag.client.render.PartialItemModelRenderer;
import dev.lopyluna.slag.register.AllDataComponents;
import dev.lopyluna.slag.register.AllDynamicTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.cuboid.ItemModelGenerator;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.core.Direction;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.ExtraFaceData;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = SlagEmbers.MOD_ID, value = Dist.CLIENT)
@ParametersAreNonnullByDefault
public class DynamicPartRenderer extends CustomRenderedItemModelRenderer {
    private static final FileToIdConverter MODELS = FileToIdConverter.json("models");
    private static final Identifier BASE = SlagEmbers.loc("item/dynamic_part");
    private static final Map<Key, CustomRenderedItemModel> PARTS = new ConcurrentHashMap<>();
    private static final ModelBaker.Interner INTERNER = new ModelBaker.Interner() {
        @Override public @Nonnull Vector3fc vector(Vector3fc vector) { return vector; }
        @Override public @Nonnull BakedQuad.MaterialInfo materialInfo(BakedQuad.MaterialInfo material) { return material; }
    };

    @Override protected void render(ItemStack stack, PartialItemModelRenderer renderer, ItemDisplayContext ctx) {
        renderer.transform(getModel(BASE));
        renderPart(stack, renderer);
    }

    public static void renderPart(ItemStack stack, PartialItemModelRenderer renderer) {
        renderer.render(PARTS.computeIfAbsent(new Key(stack.get(AllDataComponents.MATERIAL_TYPE), stack.get(AllDataComponents.PART_TYPE), stack.get(AllDataComponents.BUILT)), DynamicPartRenderer::bake));
    }

    public static void clear() {
        PARTS.clear();
    }

    private static CustomRenderedItemModel bake(Key key) {
        var material = AllDynamicTypes.getMaterial(key.material).orElse(null);
        var type = AllDynamicTypes.getPart(key.part).orElse(null);
        CustomRenderedItemModel part = null;
        if (material != null && type != null) {
            var ns = material.id.getNamespace();
            var mat = material.id.getPath();
            var partNs = type.id.getNamespace();
            var path = type.id.getPath();
            var style = material.texture;
            if (key.built != null) {
                var built = key.built.getPath();
                part = CustomRenderedItemModel.get(SlagEmbers.loc(ns, "item/modular/" + built + "/" + mat + "/" + path));
                if (part == null) part = sprite(SlagEmbers.loc(partNs, "item/modular/" + built + "/" + style + "/" + path + "_" + mat));
            }
            if (part == null) part = CustomRenderedItemModel.get(SlagEmbers.loc(ns, "item/dynamic_parts/" + mat + "/" + path));
            if (part == null) part = sprite(SlagEmbers.loc(partNs, "item/dynamic_parts/" + style + "/" + path + "_" + mat));
        }
        return part == null ? CustomRenderedItemModel.EMPTY : part;
    }

    private static @Nullable CustomRenderedItemModel sprite(Identifier id) {
        var sprite = MaterialTextures.findSprite(id);
        return sprite == null ? null : flat(sprite, MaterialTextures.ITEM);
    }

    public static CustomRenderedItemModel flat(TextureAtlasSprite sprite, RenderType type) {
        var info = new BakedQuad.MaterialInfo(sprite, ChunkSectionLayer.TRANSLUCENT, type, 0, true, 0, true);
        var from = new Vector3f(0f, 0f, 7.5f);
        var to = new Vector3f(16f, 16f, 8.5f);
        var builder = new QuadCollection.Builder();
        builder.addUnculledFace(FaceBakery.bakeQuad(INTERNER, from, to, new CuboidFace.UVs(0f, 0f, 16f, 16f), Quadrant.R0, info, Direction.SOUTH, BlockModelRotation.IDENTITY, null, ExtraFaceData.DEFAULT));
        builder.addUnculledFace(FaceBakery.bakeQuad(INTERNER, from, to, new CuboidFace.UVs(16f, 0f, 0f, 16f), Quadrant.R0, info, Direction.NORTH, BlockModelRotation.IDENTITY, null, ExtraFaceData.DEFAULT));
        ItemModelGenerator.bakeSideFaces(builder, INTERNER, BlockModelRotation.IDENTITY, info, ExtraFaceData.DEFAULT);
        return new CustomRenderedItemModel(builder.build().getAll(), null);
    }

    @SubscribeEvent
    public static void registerStandalone(ModelEvent.RegisterStandalone event) {
        CustomRenderedItemModel.register(event, BASE);
        var manager = Minecraft.getInstance().getResourceManager();
        for (var folder : List.of("models/item/dynamic_parts", "models/item/modular")) for (var file : manager.listResources(folder, loc -> loc.getPath().endsWith(".json")).keySet())
            CustomRenderedItemModel.register(event, MODELS.fileToId(file));
    }

    @SubscribeEvent
    public static void onBakingCompleted(ModelEvent.BakingCompleted event) {
        clear();
    }

    private record Key(@Nullable Identifier material, @Nullable Identifier part, @Nullable Identifier built) {}
}
