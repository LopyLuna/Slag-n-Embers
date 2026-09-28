package dev.lopyluna.slag;

import dev.lopyluna.slag.client.render.CustomRenderedItemModel;
import dev.lopyluna.slag.client.render.CustomRenderedItems;
import dev.lopyluna.slag.content.ponder.SlagPonderPlugin;
import dev.lopyluna.slag.content.traits.Traits;
import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

import java.util.Map;
import java.util.function.Function;

import static dev.lopyluna.slag.SlagEmbers.MOD_ID;

@Mod(value = MOD_ID, dist = Dist.CLIENT)
public class SlagEmbersClient {
    public SlagEmbersClient(IEventBus modEventBus) {
        modEventBus.addListener(SlagEmbersClient::onModelBake);
        modEventBus.addListener(SlagEmbersClient::clientInit);
    }

    public static void clientInit(final FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new SlagPonderPlugin());

        event.enqueueWork(() -> {
            ItemProperties.registerGeneric(SlagEmbers.loc("armor_type"), (stack, world, entity, seed) -> {
                var slot = Traits.of(stack).equipmentSlot;
                if (slot == null) return 0;
                return switch (slot) {
                    case HEAD -> 3;
                    case CHEST -> 2;
                    case LEGS -> 4;
                    case FEET -> 1;
                    default -> 0;
                };
            });
        });
    }

    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> modelRegistry = event.getModels();
        CustomRenderedItems.forEach(item -> swapModels(modelRegistry, getItemModelLocation(item), CustomRenderedItemModel::new));
    }

    public static <T extends BakedModel> void swapModels(Map<ModelResourceLocation, BakedModel> modelRegistry, ModelResourceLocation location, Function<BakedModel, T> factory) {
        modelRegistry.put(location, factory.apply(modelRegistry.get(location)));
    }

    public static ModelResourceLocation getItemModelLocation(Item item, String... suffixes) {
        var suffix = String.join("_", suffixes);
        var id = RegisteredObjectsHelper.getKeyOrThrow(item);
        if (suffix.isEmpty()) return new ModelResourceLocation(id, "inventory");
        return new ModelResourceLocation(SlagEmbers.loc(id.getNamespace(), id.getPath() + "_" + suffix), "inventory");
    }
}
