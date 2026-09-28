package dev.lopyluna.slag.events;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.AllUtils;
import dev.lopyluna.slag.content.utils.ItemResult;
import dev.lopyluna.slag.register.AllDynamicTypes;
import dev.lopyluna.slag.register.AllFluids;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

@EventBusSubscriber(modid = SlagEmbers.MOD_ID)
public class MaterialRegistryEvents {
    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        AllUtils.invalidateTags();
        if (!event.shouldUpdateStaticData()) return;
        ItemResult.invalidate();
        AllFluids.updateHidden();
        AllDynamicTypes.load(event.getRegistryAccess());
        SlagEmbers.LOGGER.info("Loaded {} traits, {} materials, {} parts and {} modulars", AllDynamicTypes.getAllTraits().size(), AllDynamicTypes.getAllMaterials().size(), AllDynamicTypes.getAllParts().size(), AllDynamicTypes.getAllModulars().size());
    }
}
