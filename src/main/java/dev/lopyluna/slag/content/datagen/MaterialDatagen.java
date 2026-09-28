package dev.lopyluna.slag.content.datagen;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.types.MaterialType;
import dev.lopyluna.slag.register.AllItems;
import dev.lopyluna.slag.register.AllRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import javax.annotation.Nonnull;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class MaterialDatagen extends DatapackBuiltinEntriesProvider {

    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
        .add(AllRegistries.MATERIAL_TYPE_REGISTRY_KEY, b -> { for (var material : AllItems.MATERIAL_TYPES) registerMaterial(b, material); });

    private static void registerMaterial(BootstrapContext<MaterialType> bootstrap, MaterialType material) {
        var key = ResourceKey.create(AllRegistries.MATERIAL_TYPE_REGISTRY_KEY, material.id);
        bootstrap.register(key, material);
    }

    public MaterialDatagen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(SlagEmbers.MOD_ID));
    }

    @Override
    public @Nonnull String getName() {
        return "Slag Material Datagen";
    }
}

