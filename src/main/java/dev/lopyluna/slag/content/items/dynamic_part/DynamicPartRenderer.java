package dev.lopyluna.slag.content.items.dynamic_part;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.client.MaterialTextures;
import dev.lopyluna.slag.client.render.CustomRenderedItemModel;
import dev.lopyluna.slag.client.render.CustomRenderedItemModelRenderer;
import dev.lopyluna.slag.client.render.PartialItemModelRenderer;
import dev.lopyluna.slag.register.AllDataComponents;
import dev.lopyluna.slag.register.AllDynamicTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.geometry.UnbakedGeometryHelper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("deprecation")
@EventBusSubscriber(modid = SlagEmbers.MOD_ID, value = Dist.CLIENT)
@ParametersAreNonnullByDefault
public class DynamicPartRenderer extends CustomRenderedItemModelRenderer {
    private static final FileToIdConverter MODELS = FileToIdConverter.json("models");
    private static final Map<Key, Part> PARTS = new ConcurrentHashMap<>();

    @Override protected void render(ItemStack stack, ItemRenderer itemRenderer, CustomRenderedItemModel model, PartialItemModelRenderer renderer,
                                    ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buf, int light, int overlay) {
        renderPart(stack, renderer, light);
    }

    public static void renderPart(ItemStack stack, PartialItemModelRenderer renderer, int light) {
        var part = PARTS.computeIfAbsent(new Key(stack.get(AllDataComponents.MATERIAL_TYPE), stack.get(AllDataComponents.PART_TYPE), stack.get(AllDataComponents.BUILT)), DynamicPartRenderer::bake);
        renderer.render(part.model, part.type, light);
    }

    public static void clear() {
        PARTS.clear();
    }

    private static Part bake(Key key) {
        var material = AllDynamicTypes.getMaterial(key.material).orElse(null);
        var type = AllDynamicTypes.getPart(key.part).orElse(null);
        Part part = null;
        if (material != null && type != null) {
            var ns = material.id.getNamespace();
            var mat = material.id.getPath();
            var partNs = type.id.getNamespace();
            var path = type.id.getPath();
            var style = material.texture;
            if (key.built != null) {
                var built = key.built.getPath();
                part = model(SlagEmbers.loc(ns, "item/modular/" + built + "/" + mat + "/" + path));
                if (part == null) part = sprite(SlagEmbers.loc(partNs, "item/modular/" + built + "/" + style + "/" + path + "_" + mat));
            }
            if (part == null) part = model(SlagEmbers.loc(ns, "item/dynamic_parts/" + mat + "/" + path));
            if (part == null) part = sprite(SlagEmbers.loc(partNs, "item/dynamic_parts/" + style + "/" + path + "_" + mat));
        }
        return part == null ? new Part(Minecraft.getInstance().getModelManager().getMissingModel(), Sheets.translucentCullBlockSheet()) : part;
    }

    private static @Nullable Part model(ResourceLocation id) {
        var manager = Minecraft.getInstance().getModelManager();
        var model = getModel(id, manager);
        return model == manager.getMissingModel() ? null : new Part(model, Sheets.translucentCullBlockSheet());
    }

    private static @Nullable Part sprite(ResourceLocation id) {
        var sprite = MaterialTextures.findSprite(id);
        return sprite == null ? null : new Part(flat(sprite), MaterialTextures.ITEM);
    }

    public static BakedModel flat(TextureAtlasSprite sprite) {
        var builder = new SimpleBakedModel.Builder(false, false, false, ItemTransforms.NO_TRANSFORMS, ItemOverrides.EMPTY).particle(sprite);
        for (var element : UnbakedGeometryHelper.createUnbakedItemElements(0, sprite)) for (var face : element.faces.entrySet())
            builder.addUnculledFace(UnbakedGeometryHelper.bakeElementFace(element, face.getValue(), sprite, face.getKey(), BlockModelRotation.X0_Y0));
        return builder.build();
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        var manager = Minecraft.getInstance().getResourceManager();
        for (var folder : List.of("models/item/dynamic_parts", "models/item/modular")) for (var file : manager.listResources(folder, loc -> loc.getPath().endsWith(".json")).keySet())
            event.register(ModelResourceLocation.standalone(MODELS.fileToId(file)));
    }

    @SubscribeEvent
    public static void onBakingCompleted(ModelEvent.BakingCompleted event) {
        clear();
    }

    private record Key(@Nullable ResourceLocation material, @Nullable ResourceLocation part, @Nullable ResourceLocation built) {}

    private record Part(BakedModel model, RenderType type) {}
}
