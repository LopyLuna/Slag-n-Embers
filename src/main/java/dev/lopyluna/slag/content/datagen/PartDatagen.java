package dev.lopyluna.slag.content.datagen;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.types.PartType;
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

public class PartDatagen extends DatapackBuiltinEntriesProvider {

    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
        .add(AllRegistries.PART_TYPE_REGISTRY_KEY, b -> { for (var part : AllItems.PART_TYPES) registerPart(b, part); });

    private static void registerPart(BootstrapContext<PartType> bootstrap, PartType part) {
        var key = ResourceKey.create(AllRegistries.PART_TYPE_REGISTRY_KEY, part.id);
        bootstrap.register(key, part);
    }

    public PartDatagen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(SlagEmbers.MOD_ID));
    }

    @Override
    public @Nonnull String getName() {
        return "Slag Part Datagen";
    }
}

