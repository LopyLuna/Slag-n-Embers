package dev.lopyluna.slag.api;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.blocks.forge.DoubleSmeltingGenerator;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class RecipeGenerators {
    private static final List<Generator> GENERATORS = new CopyOnWriteArrayList<>(List.of(DoubleSmeltingGenerator::generate));

    public static void register(Generator generator) {
        GENERATORS.add(generator);
    }

    public static List<RecipeHolder<?>> generate(Collection<RecipeHolder<?>> recipes, HolderLookup.Provider registries) {
        var out = new ArrayList<RecipeHolder<?>>();
        for (var generator : GENERATORS) {
            try {
                out.addAll(generator.generate(recipes, registries));
            } catch (Exception e) {
                SlagEmbers.LOGGER.error("Couldn't generate recipes with {}", generator, e);
            }
        }
        return out;
    }

    @FunctionalInterface
    public interface Generator {
        List<RecipeHolder<?>> generate(Collection<RecipeHolder<?>> recipes, HolderLookup.Provider registries);
    }

    private RecipeGenerators() {}
}
