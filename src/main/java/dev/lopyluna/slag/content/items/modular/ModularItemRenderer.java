package dev.lopyluna.slag.content.items.modular;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.client.render.CustomRenderedItemModel;
import dev.lopyluna.slag.client.render.CustomRenderedItemModelRenderer;
import dev.lopyluna.slag.client.render.PartialItemModelRenderer;
import dev.lopyluna.slag.content.AllUtils;
import dev.lopyluna.slag.content.items.dynamic_part.DynamicPartRenderer;
import dev.lopyluna.slag.content.items.dynamic_part.IDynamicPart;
import dev.lopyluna.slag.content.items.dynamic_part.IModularItem;
import dev.lopyluna.slag.content.traits.Traits;
import dev.lopyluna.slag.register.AllDataComponents;
import dev.lopyluna.slag.register.AllDynamicTypes;
import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static dev.lopyluna.slag.SlagEmbers.MOD_ID;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
@ParametersAreNonnullByDefault
public class ModularItemRenderer extends CustomRenderedItemModelRenderer {
    private static final FileToIdConverter MODELS = FileToIdConverter.json("models");
    private static final String FIRE_PROOF = "_fire_proof_";
    private static final List<String> ARMOR_TYPES = List.of("boots", "chestplate", "helmet", "leggings");
    private static final Map<Handle, Optional<CustomRenderedItemModel>> HANDLES = new ConcurrentHashMap<>();

    @Override protected void render(ItemStack stack, PartialItemModelRenderer renderer, ItemDisplayContext ctx) {
        if (!(stack.getItem() instanceof IModularItem tool)) return;

        var baked = stack.has(AllDataComponents.MODULAR_TYPE);
        var bakedLoc = stack.get(AllDataComponents.MODULAR_TYPE);
        var modularType = AllDynamicTypes.getModular(bakedLoc).orElse(null);
        var bakedPath = bakedLoc != null ? bakedLoc.getPath() : "";
        var suffix = modularType != null && !modularType.modelType.isEmpty() ? "_" + modularType.modelType : "";

        var baseModel = getModel(SlagEmbers.loc(baked ? "item/modular_item_baked" + suffix : "item/modular_item_blueprint"));

        var nudge = ctx == ItemDisplayContext.GROUND || ctx == ItemDisplayContext.FIXED ? 0.01f : 0.001f;
        int i = 1;

        var dynamicParts = tool.getParts(stack);

        var totalItems = !baked ? dynamicParts == null || dynamicParts.isEmpty() ? 0 : dynamicParts.size() : -1;
        var currentIndex = 0;
        var random = !baked ? new Random(totalItems * 100L + (0 >= totalItems ? 0 : dynamicParts.hashCode())) : null;
        var randomRot = !baked ? random.nextFloat() * 360 : -1;
        var randomSpeedMod = !baked ? (3.25f + (random.nextFloat() * 1.25f)) * (random.nextBoolean() ? 1 : -1) : -1;

        var traits = Traits.of(stack);
        var fireImmune = traits.immunities.contains(DamageTypeTags.IS_FIRE);
        var ms = renderer.pose();

        renderer.transform(baseModel);
        if (!baked) {
            renderer.render(baseModel);
            renderer.animated();
        }
        if (dynamicParts != null && !dynamicParts.isEmpty()) for (var part : dynamicParts.itemCopyRandom(random)) {
            if (part.isEmpty()) continue;
            ms.pushPose();
            ms.scale(1 + (i * nudge), 1 + (i * nudge), 1 + (i * (nudge * 2f)));

            if (baked) renderBakedPart(part, (fireImmune ? FIRE_PROOF : "_") + bakedPath, renderer);
            else renderNonBakedPart(part, totalItems, currentIndex, randomRot, randomSpeedMod, renderer);
            ms.popPose();
            i++;
            currentIndex++;
        }

        if (ctx == ItemDisplayContext.HEAD || !suffix.equals("_equipable")) return;
        var trim = trimModel(stack, traits);
        if (trim == null) return;
        ms.pushPose();
        renderer.render(trim);
        ms.scale(1 + (i * nudge), 1 + (i * nudge), 1 + (i * (nudge * 2f)));
        renderer.render(trim);
        ms.popPose();
    }

    private static @Nullable CustomRenderedItemModel trimModel(ItemStack stack, Traits traits) {
        var trim = stack.get(DataComponents.TRIM);
        var slot = traits.equipmentSlot;
        if (trim == null || slot == null) return null;
        var type = switch (slot) {
            case HEAD -> "helmet";
            case CHEST -> "chestplate";
            case LEGS -> "leggings";
            case FEET -> "boots";
            default -> null;
        };
        var material = trim.material().unwrapKey().orElse(null);
        if (type == null || material == null) return null;
        return CustomRenderedItemModel.get(SlagEmbers.loc("item/modular_item_baked_" + type + "_trim_" + material.identifier().getPath()));
    }

    public static void renderBakedPart(ItemStack stack, String suffix, PartialItemModelRenderer renderer) {
        if (stack.getItem() instanceof IDynamicPart) {
            DynamicPartRenderer.renderPart(stack, renderer);
            return;
        }
        var model = remapItemToCustomModel(stack, "", "", suffix);
        if (model != null) renderer.render(model);
        else renderer.render(stack);
    }

    public static void renderNonBakedPart(ItemStack stack, int totalItems, int currentIndex, float randomRot, float randomSpeedMod, PartialItemModelRenderer renderer) {

        float angleStep = 360.0f / totalItems;
        float angle = angleStep * currentIndex;

        float rotAngle = ((AnimationTickHolder.getRenderTime() * randomSpeedMod) + randomRot % 360);

        float angleRad = (float) Math.toRadians(angle + rotAngle);

        float radius = totalItems != 1 ? 1f / 16.0f : 0.5f/16f;
        float offsetX = (float) Math.cos(angleRad) * radius;
        float offsetY = (float) Math.sin(angleRad) * radius;

        renderer.pose().translate(offsetX, offsetY, 0);

        if (stack.getItem() instanceof IDynamicPart) DynamicPartRenderer.renderPart(stack, renderer);
        else renderer.render(stack);

    }

    public static @Nullable CustomRenderedItemModel remapItemToCustomModel(ItemStack stack, String prefix, @Nullable String name, String suffix) {
        if (!stack.is(Items.STICK)) return null;
        var targetName = name == null || name.isEmpty() ? "handle" : name;
        var fireProof = suffix.startsWith(FIRE_PROOF);
        var modular = suffix.substring(fireProof ? FIRE_PROOF.length() : 1);
        var key = new Handle(SlagEmbers.loc("item/" + prefix + targetName + suffix), SlagEmbers.loc("item/built/" + targetName + (fireProof ? "_fire_proof" : "") + "/" + modular));
        return HANDLES.computeIfAbsent(key, ModularItemRenderer::handle).orElse(null);
    }

    private static Optional<CustomRenderedItemModel> handle(Handle handle) {
        var model = CustomRenderedItemModel.get(handle.model());
        if (model != null) return Optional.of(model);
        var sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.ITEMS).getSprite(handle.texture());
        if (sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation())) return Optional.empty();
        return Optional.of(DynamicPartRenderer.flat(sprite, Sheets.translucentItemSheet()));
    }

    @SubscribeEvent
    public static void onBakingCompleted(ModelEvent.BakingCompleted e) {
        HANDLES.clear();
    }

    private record Handle(Identifier model, Identifier texture) {}

    @SubscribeEvent
    public static void registerStandalone(ModelEvent.RegisterStandalone e) {
        CustomRenderedItemModel.register(e, SlagEmbers.loc("item/modular_item_blueprint"));
        CustomRenderedItemModel.register(e, SlagEmbers.loc("item/modular_item_baked"));
        CustomRenderedItemModel.register(e, SlagEmbers.loc("item/modular_item_baked_handheld"));
        CustomRenderedItemModel.register(e, SlagEmbers.loc("item/modular_item_baked_equipable"));
        for (var trim : AllUtils.TRIM_MATERIALS.keySet()) for (var type : ARMOR_TYPES)
            CustomRenderedItemModel.register(e, SlagEmbers.loc("item/modular_item_baked_" + type + "_trim_" + trim.identifier().getPath()));

        var resources = Minecraft.getInstance().getResourceManager();
        for (var file : resources.listResources("models/item", loc -> loc.getNamespace().equals(MOD_ID) && loc.getPath().startsWith("models/item/handle_") && loc.getPath().endsWith(".json")).keySet())
            CustomRenderedItemModel.register(e, MODELS.fileToId(file));
    }
}
