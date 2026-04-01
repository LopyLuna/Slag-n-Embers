package dev.lopyluna.slag.content;

import com.tterrag.registrate.providers.ProviderType;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.datagen.LangProviderDatagen;
import dev.lopyluna.slag.content.datagen.MaterialDatagen;
import dev.lopyluna.slag.content.datagen.ModularDatagen;
import dev.lopyluna.slag.content.datagen.PartDatagen;
import dev.lopyluna.slag.register.AllSoundEvents;
import net.minecraft.data.DataGenerator;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.function.BiConsumer;

public class EmbersDatagen {
    public static void gatherDataHighPriority(GatherDataEvent event) {
        if (event.getMods().contains(SlagEmbers.MOD_ID)) addExtraRegistrateData();
    }

    public static void gatherData(GatherDataEvent event) {
        if (!event.getMods().contains(SlagEmbers.MOD_ID)) return;
        DataGenerator generator = event.getGenerator();
        generator.addProvider(event.includeClient(), AllSoundEvents.provider(generator));

        generator.addProvider(event.includeServer(), new MaterialDatagen(generator.getPackOutput(), event.getLookupProvider()));
        generator.addProvider(event.includeServer(), new PartDatagen(generator.getPackOutput(), event.getLookupProvider()));
        generator.addProvider(event.includeServer(), new ModularDatagen(generator.getPackOutput(), event.getLookupProvider()));
    }

    private static void addExtraRegistrateData() {
        SlagEmbers.REG.addDataGenerator(ProviderType.LANG, provider -> {
            BiConsumer<String, String> langConsumer = provider::add;
            AllSoundEvents.provideLang(langConsumer);
            LangProviderDatagen.lang(provider);
        });
    }
}
