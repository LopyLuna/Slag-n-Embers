package dev.lopyluna.slag.client;

import com.mojang.blaze3d.platform.NativeImage;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.items.dynamic_part.DynamicPartRenderer;
import dev.lopyluna.slag.content.types.Incompatible;
import dev.lopyluna.slag.register.AllDynamicTypes;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.FastColor;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntUnaryOperator;

@EventBusSubscriber(modid = SlagEmbers.MOD_ID, value = Dist.CLIENT)
public class MaterialTextures {
    public static final ResourceLocation ATLAS = SlagEmbers.loc("textures/atlas/materials.png");
    public static final RenderType ITEM = RenderType.itemEntityTranslucentCull(ATLAS);
    private static final FileToIdConverter TEXTURES = new FileToIdConverter("textures", ".png");
    private static final ResourceLocation PALETTE = SlagEmbers.loc("color_palettes/base_palette");
    private static final ResourceLocation ARMOR_PALETTE = SlagEmbers.loc("color_palettes/armor/base_palette");

    private static TextureAtlas atlas;
    private static int version = -1;

    @SubscribeEvent
    public static void registerReloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) MaterialTextures::build);
    }

    public static void tick() {
        if (atlas != null && version != AllDynamicTypes.version) build(Minecraft.getInstance().getResourceManager());
    }

    public static TextureAtlasSprite getSprite(ResourceLocation id) {
        if (atlas == null) return Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(MissingTextureAtlasSprite.getLocation());
        return atlas.getSprite(id);
    }

    public static @Nullable TextureAtlasSprite findSprite(ResourceLocation id) {
        var sprite = getSprite(id);
        return sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation()) ? null : sprite;
    }

    public static void build(ResourceManager manager) {
        version = AllDynamicTypes.version;
        var palette = palette(manager, PALETTE);
        var armorPalette = palette(manager, ARMOR_PALETTE);
        var templates = new HashMap<ResourceLocation, NativeImage>();
        var sprites = new HashMap<ResourceLocation, SpriteContents>();
        var parts = AllDynamicTypes.getAllPartsList();
        var modulars = AllDynamicTypes.getAllModularsList();

        for (var material : AllDynamicTypes.getAllMaterials()) {
            var colors = palette(manager, material.id.withPrefix("color_palettes/"));
            if (colors == null && !material.palette.isEmpty()) colors = abgr(PaletteHelper.colorsToGradient(8, material.palette));
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
        var preparations = SpriteLoader.create(atlas).stitch(contents, 0, Util.backgroundExecutor()).waitForUpload().join();
        atlas.upload(preparations);
        atlas.updateFilter(preparations);
        DynamicPartRenderer.clear();
    }

    private static void add(ResourceManager manager, Map<ResourceLocation, NativeImage> templates, Map<ResourceLocation, SpriteContents> sprites, ResourceLocation template, ResourceLocation material, @Nullable IntUnaryOperator mapping) {
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
        sprites.put(id, new SpriteContents(id, new FrameSize(image.getWidth(), image.getHeight()), image, ResourceMetadata.EMPTY));
    }

    private static @Nullable NativeImage read(ResourceManager manager, ResourceLocation id) {
        var resource = manager.getResource(TEXTURES.idToFile(id)).orElse(null);
        if (resource == null) return null;
        try (var stream = resource.open()) {
            return NativeImage.read(stream);
        } catch (IOException e) {
            SlagEmbers.LOGGER.error("Failed to read texture {}", id, e);
            return null;
        }
    }

    private static int[] palette(ResourceManager manager, ResourceLocation id) {
        try (var image = read(manager, id)) { return image == null ? null : image.getPixelsRGBA(); }
    }

    private static int[] armor(int[] colors) {
        var rgb = new ArrayList<Integer>();
        for (var i = 0; i < colors.length - 1; i++) rgb.add(FastColor.ABGR32.fromArgb32(colors[i]) & 0xFFFFFF);
        return abgr(PaletteHelper.colorsToGradient(9, rgb));
    }

    private static int[] abgr(List<Integer> rgb) {
        var colors = new int[rgb.size()];
        for (var i = 0; i < colors.length; i++) colors[i] = FastColor.ABGR32.fromArgb32(0xFF000000 | rgb.get(i));
        return colors;
    }

    private static @Nullable IntUnaryOperator mapping(int[] keys, int[] values) {
        if (keys == null || values == null || keys.length != values.length) return null;
        var map = new Int2IntOpenHashMap(keys.length);
        for (var i = 0; i < keys.length; i++) if (FastColor.ABGR32.alpha(keys[i]) != 0) map.put(FastColor.ABGR32.transparent(keys[i]), values[i]);
        return color -> {
            var alpha = FastColor.ABGR32.alpha(color);
            if (alpha == 0) return color;
            var rgb = FastColor.ABGR32.transparent(color);
            var mapped = map.getOrDefault(rgb, FastColor.ABGR32.opaque(rgb));
            return FastColor.ABGR32.color(alpha * FastColor.ABGR32.alpha(mapped) / 255, mapped);
        };
    }
}
