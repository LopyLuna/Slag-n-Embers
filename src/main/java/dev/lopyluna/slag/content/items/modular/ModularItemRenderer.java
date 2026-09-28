package dev.lopyluna.slag.content.items.modular;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.client.render.CustomRenderedItemModel;
import dev.lopyluna.slag.client.render.CustomRenderedItemModelRenderer;
import dev.lopyluna.slag.client.render.PartialItemModelRenderer;
import dev.lopyluna.slag.content.items.dynamic_part.DynamicPartRenderer;
import dev.lopyluna.slag.content.items.dynamic_part.IDynamicPart;
import dev.lopyluna.slag.content.items.dynamic_part.IModularItem;
import dev.lopyluna.slag.register.AllDataComponents;
import dev.lopyluna.slag.register.AllDynamicTypes;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
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
    private static final Map<Handle, Optional<BakedModel>> HANDLES = new ConcurrentHashMap<>();

    @Override protected void render(ItemStack stack, ItemRenderer itemRenderer, CustomRenderedItemModel model, PartialItemModelRenderer renderer,
                                    ItemDisplayContext ctx, PoseStack ms, MultiBufferSource buf, int light, int overlay) {
        var level = mc.level;
        var player = mc.player;
        if (level == null || player == null) return;
        var item = stack.getItem();

        if (!(item instanceof IModularItem tool)) {
            renderer.render(model.getOriginalModel(), light);
            return;
        }
        var shaper = itemRenderer.getItemModelShaper();
        var manager = shaper.getModelManager();

        var baked = stack.has(AllDataComponents.MODULAR_TYPE);
        var bakedLoc = stack.get(AllDataComponents.MODULAR_TYPE);
        var modularType = AllDynamicTypes.getModular(bakedLoc).orElse(null);
        var bakedPath = bakedLoc != null ? bakedLoc.getPath() : "";
        var suffix = modularType != null && !modularType.modelType.isEmpty() ? "_" + modularType.modelType : "";

        var baseModel = getModel(stack, level, player, SlagEmbers.loc(baked ? "item/modular_item_baked" + suffix : "item/modular_item_blueprint"), manager);

        var nudge = ctx == ItemDisplayContext.GROUND || ctx == ItemDisplayContext.FIXED ? 0.01f : 0.001f;
        int i = 1;

        var dynamicParts = tool.getParts(stack);

        var totalItems = !baked ? dynamicParts == null || dynamicParts.isEmpty() ? 0 : dynamicParts.size() : -1;
        var currentIndex = 0;
        var random = !baked ? new Random(totalItems * 100L + (0 >= totalItems ? 0 : dynamicParts.hashCode())) : null;
        var randomRot = !baked ? random.nextFloat() * 360 : -1;
        var randomSpeedMod = !baked ? (3.25f + (random.nextFloat() * 1.25f)) * (random.nextBoolean() ? 1 : -1) : -1;

        var fireImmune = stack.has(DataComponents.FIRE_RESISTANT);
        var left = player.getMainArm() == HumanoidArm.LEFT && (ctx == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || ctx == ItemDisplayContext.THIRD_PERSON_LEFT_HAND);

        ms.pushPose();
        baseModel.applyTransform(ctx, ms, left);
        if (!baked) renderer.render(baseModel, light);
        if (dynamicParts != null && !dynamicParts.isEmpty()) for (var part : dynamicParts.itemCopyRandom(random)) {
            if (part.isEmpty()) continue;
            ms.pushPose();
            ms.scale(1 + (i * nudge), 1 + (i * nudge), 1 + (i * (nudge * 2f)));

            if (baked) renderBakedPart(level, player, part, light, (fireImmune ? "_fire_proof_" : "_") + bakedPath, itemRenderer, renderer, manager);
            else renderNonBakedPart(level, player, part, light, totalItems, currentIndex, randomRot, randomSpeedMod, ms, itemRenderer, renderer, manager);
            ms.popPose();
            i++;
            currentIndex++;
        }

        if (ctx != ItemDisplayContext.HEAD && stack.has(DataComponents.TRIM) && suffix.equals("_equipable")) {
            ms.pushPose();
            var bakedModel = getModel(stack, level, player, SlagEmbers.loc("item/modular_item_baked_trim"), manager);
            renderer.render(bakedModel, light);
            ms.scale(1 + (i * nudge), 1 + (i * nudge), 1 + (i * (nudge * 2f)));
            renderer.render(bakedModel, light);
            ms.popPose();
        }
        ms.popPose();
    }

    public static void renderBakedPart(ClientLevel level, Player player, ItemStack stack, int light, String suffix,
                                       ItemRenderer itemRenderer, PartialItemModelRenderer renderer, ModelManager manager) {
        if (stack.getItem() instanceof IDynamicPart) DynamicPartRenderer.renderPart(stack, renderer, light);
        else renderer.render(remapItemToCustomModel(stack, "", "", suffix, itemRenderer, manager, level, player), light);
    }

    public static void renderNonBakedPart(Level level, Player player, ItemStack stack, int light, int totalItems, int currentIndex, float randomRot, float randomSpeedMod,
                                          PoseStack ms, ItemRenderer itemRenderer, PartialItemModelRenderer renderer, ModelManager manager) {

        float angleStep = 360.0f / totalItems;
        float angle = angleStep * currentIndex;

        float rotAngle = ((AnimationTickHolder.getRenderTime() * randomSpeedMod) + randomRot % 360);

        float angleRad = (float) Math.toRadians(angle + rotAngle);

        float radius = totalItems != 1 ? 1f / 16.0f : 0.5f/16f;
        float offsetX = (float) Math.cos(angleRad) * radius;
        float offsetY = (float) Math.sin(angleRad) * radius;

        ms.translate(offsetX, offsetY, 0);

        if (stack.getItem() instanceof IDynamicPart) DynamicPartRenderer.renderPart(stack, renderer, light);
        else renderer.render(itemRenderer.getModel(stack, level, player, 0), light);

    }

    public static BakedModel remapItemToCustomModel(ItemStack stack, String prefix, @Nullable String name, String suffix, ItemRenderer itemRenderer, ModelManager manager, ClientLevel level, Player player) {
        if (stack.is(Items.STICK)) {
            var targetName = name == null || name.isEmpty() ? "handle" : name;
            var fireProof = suffix.startsWith(FIRE_PROOF);
            var modular = suffix.substring(fireProof ? FIRE_PROOF.length() : 1);
            var key = new Handle(SlagEmbers.loc("item/" + prefix + targetName + suffix), SlagEmbers.loc("item/built/" + targetName + (fireProof ? "_fire_proof" : "") + "/" + modular));
            var model = HANDLES.computeIfAbsent(key, ModularItemRenderer::handle);
            if (model.isPresent()) return model.get();
        }
        return itemRenderer.getModel(stack, level, player, 0);
    }

    private static Optional<BakedModel> handle(Handle handle) {
        var manager = Minecraft.getInstance().getModelManager();
        var model = manager.getModel(ModelResourceLocation.standalone(handle.model()));
        if (model != manager.getMissingModel()) return Optional.of(model);
        var sprite = manager.getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(handle.texture());
        if (sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation())) return Optional.empty();
        return Optional.of(DynamicPartRenderer.flat(sprite));
    }

    @SubscribeEvent
    public static void onBakingCompleted(ModelEvent.BakingCompleted e) {
        HANDLES.clear();
    }

    private record Handle(ResourceLocation model, ResourceLocation texture) {}

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional e) {
        e.register(ModelResourceLocation.standalone(SlagEmbers.loc("item/modular_item_blueprint")));
        e.register(ModelResourceLocation.standalone(SlagEmbers.loc("item/modular_item_baked")));
        e.register(ModelResourceLocation.standalone(SlagEmbers.loc("item/modular_item_baked_handheld")));
        e.register(ModelResourceLocation.standalone(SlagEmbers.loc("item/modular_item_baked_equipable")));
        e.register(ModelResourceLocation.standalone(SlagEmbers.loc("item/modular_item_baked_trim")));

        var resources = Minecraft.getInstance().getResourceManager();
        for (var file : resources.listResources("models/item", loc -> loc.getNamespace().equals(MOD_ID) && loc.getPath().startsWith("models/item/handle_") && loc.getPath().endsWith(".json")).keySet())
            e.register(ModelResourceLocation.standalone(MODELS.fileToId(file)));
    }
}
