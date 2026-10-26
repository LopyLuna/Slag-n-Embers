package dev.lopyluna.slag.api;

import net.minecraft.core.HolderLookup;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
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
        var result = result(recipe, registries);
        return result.isEmpty() ? List.of() : List.of(new Source(inputs(recipe), result));
    }

    public static List<Ingredient> inputs(Recipe<?> recipe) {
        if (!(recipe instanceof SmithingTransformRecipe smithing)) return recipe.placementInfo().ingredients();
        var list = new ArrayList<Ingredient>();
        list.add(smithing.baseIngredient());
        smithing.additionIngredient().ifPresent(list::add);
        return list;
    }

    public static ItemStack result(Recipe<?> recipe, HolderLookup.Provider registries) {
        var context = new ContextMap.Builder().withParameter(SlotDisplayContext.REGISTRIES, registries).create(SlotDisplayContext.CONTEXT);
        for (var display : recipe.display()) {
            var stack = display.result().resolveForFirstStack(context);
            if (!stack.isEmpty()) return stack;
        }
        return ItemStack.EMPTY;
    }

    @FunctionalInterface
    public interface Adapter {
        List<Source> convert(Recipe<?> recipe, HolderLookup.Provider registries);
    }

    public record Source(List<Ingredient> inputs, ItemStack result) {}

    private RecyclingSources() {}
}
