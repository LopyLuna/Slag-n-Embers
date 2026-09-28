package dev.lopyluna.slag.content.datagen;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.types.ModularType;
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

public class ModularDatagen extends DatapackBuiltinEntriesProvider {

    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(AllRegistries.MODULAR_TYPE_REGISTRY_KEY, b -> { for (var modular : AllItems.MODULAR_TYPES) registerPart(b, modular); });

    private static void registerPart(BootstrapContext<ModularType> bootstrap, ModularType part) {
        var key = ResourceKey.create(AllRegistries.MODULAR_TYPE_REGISTRY_KEY, part.id);
        bootstrap.register(key, part);
    }

    public ModularDatagen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(SlagEmbers.MOD_ID));
    }

    @Override
    public @Nonnull String getName() {
        return "Slag Modular Datagen";
    }
}