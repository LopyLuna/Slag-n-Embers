package dev.lopyluna.slag.api;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RecyclingSources {
    private static final Map<RecipeType<?>, Adapter> ADAPTERS = new ConcurrentHashMap<>();

    public static void register(RecipeType<?> type, Adapter adapter) {
        ADAPTERS.put(type, adapter);
    }

    public static @Nullable Adapter get(RecipeType<?> type) {
        return ADAPTERS.isEmpty() ? null : ADAPTERS.get(type);
    }

    public static List<Source> vanillaLike(Recipe<?> recipe, HolderLookup.Provider registries) {
        var result = recipe.getResultItem(registries);
        return result.isEmpty() ? List.of() : List.of(new Source(recipe.getIngredients(), result));
    }

    @FunctionalInterface
    public interface Adapter {
        List<Source> convert(Recipe<?> recipe, HolderLookup.Provider registries);
    }

    public record Source(List<Ingredient> inputs, ItemStack result) {}

    private RecyclingSources() {}
}
