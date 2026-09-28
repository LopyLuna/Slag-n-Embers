package dev.lopyluna.slag.register;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.traits.TraitType;
import dev.lopyluna.slag.content.types.MaterialType;
import dev.lopyluna.slag.content.types.ModularType;
import dev.lopyluna.slag.content.types.PartType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

@EventBusSubscriber(modid = SlagEmbers.MOD_ID)
public class AllRegistries {

    public static final ResourceKey<Registry<TraitType>> TRAIT_TYPE_REGISTRY_KEY = ResourceKey.createRegistryKey(SlagEmbers.loc("traits"));
    public static final ResourceKey<Registry<MaterialType>> MATERIAL_TYPE_REGISTRY_KEY = ResourceKey.createRegistryKey(SlagEmbers.loc("materials"));
    public static final ResourceKey<Registry<PartType>> PART_TYPE_REGISTRY_KEY = ResourceKey.createRegistryKey(SlagEmbers.loc("parts"));
    public static final ResourceKey<Registry<ModularType>> MODULAR_TYPE_REGISTRY_KEY = ResourceKey.createRegistryKey(SlagEmbers.loc("modulars"));

    @SubscribeEvent
    public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(TRAIT_TYPE_REGISTRY_KEY, TraitType.CODEC, TraitType.CODEC);
        event.dataPackRegistry(MATERIAL_TYPE_REGISTRY_KEY, MaterialType.CODEC, MaterialType.CODEC);
        event.dataPackRegistry(PART_TYPE_REGISTRY_KEY, PartType.CODEC, PartType.CODEC);
        event.dataPackRegistry(MODULAR_TYPE_REGISTRY_KEY, ModularType.CODEC, ModularType.CODEC);
    }
}

