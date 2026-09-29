package dev.lopyluna.slag.content.blocks.forge;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.config.SlagCommonConfigs;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.*;

public class DoubleSmeltingGenerator {
    public static List<RecipeHolder<?>> generate(Collection<RecipeHolder<?>> recipes, HolderLookup.Provider registries) {
        if (SlagCommonConfigs.SPEC.isLoaded() && !SlagCommonConfigs.GENERATED_FORGE.get()) return List.of();
        var alternatives = Collections.newSetFromMap(new IdentityHashMap<Item, Boolean>());
        var existing = new ArrayList<DoubleSmeltingRecipe>();
        var sources = new ArrayList<RecipeHolder<?>>();
        for (var holder : recipes) {
            var recipe = holder.value();
            var type = recipe.getType();
            if (recipe instanceof DoubleSmeltingRecipe forge) existing.add(forge);
            else if (type == RecipeType.BLASTING || type == RecipeType.SMOKING) alternatives.add(recipe.getResultItem(registries).getItem());
            else if (type == RecipeType.SMELTING && recipe instanceof AbstractCookingRecipe && !recipe.isSpecial()) sources.add(holder);
        }

        var results = Collections.newSetFromMap(new IdentityHashMap<Item, Boolean>());
        for (var forge : existing) results.add(forge.getOutput().getItem());

        var list = new ArrayList<RecipeHolder<?>>();
        for (var holder : sources) {
            var cooking = (AbstractCookingRecipe) holder.value();
            var result = cooking.getResultItem(registries);
            if (result.isEmpty() || !result.isStackable() || result.getCount() * 2 > result.getMaxStackSize()) continue;
            if (alternatives.contains(result.getItem()) || results.contains(result.getItem())) continue;
            var ingredients = cooking.getIngredients();
            if (ingredients.isEmpty() || ingredients.getFirst().hasNoItems()) continue;
            var input = ingredients.getFirst();
            if (covered(existing, input.getItems())) continue;
            var recipe = new DoubleSmeltingRecipe("", input, input, result.copyWithCount(result.getCount() * 2), cooking.getExperience() * 2, cooking.getCookingTime());
            list.add(new RecipeHolder<>(SlagEmbers.loc("double_smelting/generated/" + holder.id().getNamespace() + "/" + holder.id().getPath()), recipe));
        }
        if (!list.isEmpty()) SlagEmbers.LOGGER.info("Generated {} brick forge recipes from smelting recipes", list.size());
        return list;
    }

    private static boolean covered(List<DoubleSmeltingRecipe> existing, ItemStack[] stacks) {
        for (var forge : existing) for (var stack : stacks) if (forge.getInputA().test(stack) && forge.getInputB().test(stack)) return true;
        return false;
    }
}
