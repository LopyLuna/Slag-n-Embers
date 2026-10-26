package dev.lopyluna.slag.content;

import com.tterrag.registrate.providers.ProviderType;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.datagen.MaterialDatagen;
import dev.lopyluna.slag.content.datagen.ModularDatagen;
import dev.lopyluna.slag.content.datagen.PartDatagen;
import dev.lopyluna.slag.content.datagen.TraitDatagen;
import dev.lopyluna.slag.register.AllSoundEvents;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.ItemTags;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.function.BiConsumer;

public class EmbersDatagen {
    public static void gatherDataHighPriority(GatherDataEvent.Client event) {
        if (!event.getModContainer().getModId().equals(SlagEmbers.MOD_ID)) return;
        bindComponents(event.getLookupProvider().join());
        addExtraRegistrateData();
    }

    @SuppressWarnings("deprecation")
    private static void bindComponents(HolderLookup.Provider registries) {
        CommonHooks.markComponentClassAsValid(HolderSet.emptyNamed(BuiltInRegistries.ITEM, ItemTags.PLANKS).getClass());
        AllUtils.datagen = registries;
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(registries).forEach(DataComponentInitializers.PendingComponents::apply);
    }

    public static void gatherData(GatherDataEvent.Client event) {
        if (!event.getModContainer().getModId().equals(SlagEmbers.MOD_ID)) return;
        DataGenerator generator = event.getGenerator();
        event.addProvider(AllSoundEvents.provider(generator));

        event.createProvider(TraitDatagen::new);
        event.createProvider(MaterialDatagen::new);
        event.createProvider(PartDatagen::new);
        event.createProvider(ModularDatagen::new);
    }

    private static void addExtraRegistrateData() {
        SlagEmbers.REG.addDataGenerator(ProviderType.LANG, provider -> {
            BiConsumer<String, String> langConsumer = provider::add;
            AllSoundEvents.provideLang(langConsumer);
        });
    }
}
