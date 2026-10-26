package dev.lopyluna.slag.client;

import com.mojang.blaze3d.platform.NativeImage;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.items.dynamic_part.DynamicPartRenderer;
import dev.lopyluna.slag.content.types.Incompatible;
import dev.lopyluna.slag.mixin.SpriteLoaderAccessor;
import dev.lopyluna.slag.register.AllDynamicTypes;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.data.AtlasIds;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.ARGB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntUnaryOperator;

@EventBusSubscriber(modid = SlagEmbers.MOD_ID, value = Dist.CLIENT)
public class MaterialTextures {
    public static final Identifier ATLAS = SlagEmbers.loc("textures/atlas/materials.png");
    public static final RenderType ITEM = RenderTypes.itemTranslucent(ATLAS);
    private static final FileToIdConverter TEXTURES = new FileToIdConverter("textures", ".png");
    private static final Identifier PALETTE = SlagEmbers.loc("color_palettes/base_palette");
    private static final Identifier ARMOR_PALETTE = SlagEmbers.loc("color_palettes/armor/base_palette");

    private static TextureAtlas atlas;
    private static int version = -1;

    @SubscribeEvent
    public static void registerReloadListener(AddClientReloadListenersEvent event) {
        event.addListener(SlagEmbers.loc("material_textures"), (ResourceManagerReloadListener) MaterialTextures::build);
    }

    public static void tick() {
        if (atlas != null && version != AllDynamicTypes.version) build(Minecraft.getInstance().getResourceManager());
    }

    public static TextureAtlasSprite getSprite(Identifier id) {
        if (atlas == null) return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.ITEMS).getSprite(MissingTextureAtlasSprite.getLocation());
        return atlas.getSprite(id);
    }

    public static @Nullable TextureAtlasSprite findSprite(Identifier id) {
        var sprite = getSprite(id);
        return sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation()) ? null : sprite;
    }

    public static void build(ResourceManager manager) {
        version = AllDynamicTypes.version;
        var palette = palette(manager, PALETTE);
        var armorPalette = palette(manager, ARMOR_PALETTE);
        var templates = new HashMap<Identifier, NativeImage>();
        var sprites = new HashMap<Identifier, SpriteContents>();
        var parts = AllDynamicTypes.getAllPartsList();
        var modulars = AllDynamicTypes.getAllModularsList();

        for (var material : AllDynamicTypes.getAllMaterials()) {
            var colors = palette(manager, material.id.withPrefix("color_palettes/"));
            if (colors == null && !material.palette.isEmpty()) colors = argb(PaletteHelper.colorsToGradient(8, material.palette));
            var armorColors = palette(manager, material.id.withPrefix("color_palettes/armor/"));
            if (armorColors == null && colors != null && colors.length > 1) armorColors = armor(colors);
            var item = mapping(palette, colors);
            var armor = mapping(armorPalette, armorColors);
            var style = material.texture;

            for (var part : parts) {
                if (!Incompatible.compatible(material, part)) continue;
                var ns = part.id.getNamespace();
                var path = part.id.getPath();
                add(manager, templates, sprites, SlagEmbers.loc(ns, "item/dynamic_parts/" + style + "/" + path), material.id, item);
                for (var modular : modulars) if (modular.segments.contains(part.segmentPart) && Incompatible.compatible(material, modular) && Incompatible.compatible(part, modular)) add(manager, templates, sprites, SlagEmbers.loc(ns, "item/modular/" + modular.id.getPath() + "/" + style + "/" + path), material.id, item);
                var prefix = path.contains("helmet") || path.contains("chestplate") || path.contains("leggings") || path.contains("boots") ? "armors/" : "armors/" + path + "/";
                for (var layer = 1; layer <= 2; layer++) add(manager, templates, sprites, SlagEmbers.loc(ns, prefix + style + "_layer_" + layer), material.id, armor);
            }
        }
        for (var image : templates.values()) if (image != null) image.close();

        var contents = new ArrayList<>(sprites.values());
        contents.add(MissingTextureAtlasSprite.create());
        if (atlas == null) Minecraft.getInstance().getTextureManager().register(ATLAS, atlas = new TextureAtlas(ATLAS));
        var preparations = ((SpriteLoaderAccessor) SpriteLoader.create(atlas)).slag$stitch(contents, 0, Util.backgroundExecutor());
        preparations.readyForUpload().join();
        atlas.upload(preparations);
        DynamicPartRenderer.clear();
    }

    private static void add(ResourceManager manager, Map<Identifier, NativeImage> templates, Map<Identifier, SpriteContents> sprites, Identifier template, Identifier material, @Nullable IntUnaryOperator mapping) {
        var id = template.withSuffix("_" + material.getPath());
        if (sprites.containsKey(id)) return;
        var image = read(manager, id);
        if (image == null) {
            if (mapping == null) return;
            if (!templates.containsKey(template)) templates.put(template, read(manager, template));
            var base = templates.get(template);
            if (base == null) return;
            image = base.mappedCopy(mapping);
        }
        sprites.put(id, new SpriteContents(id, new FrameSize(image.getWidth(), image.getHeight()), image));
    }

    private static @Nullable NativeImage read(ResourceManager manager, Identifier id) {
        var resource = manager.getResource(TEXTURES.idToFile(id)).orElse(null);
        if (resource == null) return null;
        try (var stream = resource.open()) {
            return NativeImage.read(stream);
        } catch (IOException e) {
            SlagEmbers.LOGGER.error("Failed to read texture {}", id, e);
            return null;
        }
    }

    private static int[] palette(ResourceManager manager, Identifier id) {
        try (var image = read(manager, id)) { return image == null ? null : image.getPixels(); }
    }

    private static int[] armor(int[] colors) {
        var rgb = new ArrayList<Integer>();
        for (var i = 0; i < colors.length - 1; i++) rgb.add(colors[i] & 0xFFFFFF);
        return argb(PaletteHelper.colorsToGradient(9, rgb));
    }

    private static int[] argb(List<Integer> rgb) {
        var colors = new int[rgb.size()];
        for (var i = 0; i < colors.length; i++) colors[i] = 0xFF000000 | rgb.get(i);
        return colors;
    }

    private static @Nullable IntUnaryOperator mapping(int[] keys, int[] values) {
        if (keys == null || values == null || keys.length != values.length) return null;
        var map = new Int2IntOpenHashMap(keys.length);
        for (var i = 0; i < keys.length; i++) if (ARGB.alpha(keys[i]) != 0) map.put(ARGB.transparent(keys[i]), values[i]);
        return color -> {
            var alpha = ARGB.alpha(color);
            if (alpha == 0) return color;
            var rgb = ARGB.transparent(color);
            var mapped = map.getOrDefault(rgb, ARGB.opaque(rgb));
            return ARGB.color(alpha * ARGB.alpha(mapped) / 255, mapped);
        };
    }
}
