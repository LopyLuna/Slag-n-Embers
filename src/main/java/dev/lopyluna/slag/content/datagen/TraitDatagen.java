package dev.lopyluna.slag.content.datagen;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.traits.TraitType;
import dev.lopyluna.slag.register.AllDynamicTypes;
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

public class TraitDatagen extends DatapackBuiltinEntriesProvider {

    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(AllRegistries.TRAIT_TYPE_REGISTRY_KEY, b -> { for (var trait : AllDynamicTypes.getAllTraits()) registerTrait(b, trait); });

    private static void registerTrait(BootstrapContext<TraitType> bootstrap, TraitType trait) {
        var key = ResourceKey.create(AllRegistries.TRAIT_TYPE_REGISTRY_KEY, trait.id);
        bootstrap.register(key, trait);
    }

    public TraitDatagen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(SlagEmbers.MOD_ID));
    }

    @Override
    public @Nonnull String getName() {
        return "Slag Trait Datagen";
    }
}
