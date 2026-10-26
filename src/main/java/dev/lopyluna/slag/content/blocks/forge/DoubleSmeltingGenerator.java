package dev.lopyluna.slag.content.blocks.forge;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.api.RecyclingSources;
import dev.lopyluna.slag.config.SlagCommonConfigs;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
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
            else if (type == RecipeType.BLASTING || type == RecipeType.SMOKING) alternatives.add(RecyclingSources.result(recipe, registries).getItem());
            else if (type == RecipeType.SMELTING && recipe instanceof AbstractCookingRecipe && !recipe.isSpecial()) sources.add(holder);
        }

        var results = Collections.newSetFromMap(new IdentityHashMap<Item, Boolean>());
        for (var forge : existing) results.add(forge.getOutput().getItem());

        var list = new ArrayList<RecipeHolder<?>>();
        for (var holder : sources) {
            var cooking = (AbstractCookingRecipe) holder.value();
            var result = RecyclingSources.result(cooking, registries);
            if (result.isEmpty() || !result.isStackable() || result.getCount() * 2 > result.getMaxStackSize()) continue;
            if (alternatives.contains(result.getItem()) || results.contains(result.getItem())) continue;
            var input = cooking.input();
            if (input.isEmpty() || covered(existing, input)) continue;
            var recipe = new DoubleSmeltingRecipe("", input, input, ItemStackTemplate.fromNonEmptyStack(result.copyWithCount(result.getCount() * 2)), cooking.experience() * 2, cooking.cookingTime());
            var id = holder.id().identifier();
            list.add(new RecipeHolder<>(SlagEmbers.recipe("double_smelting/generated/" + id.getNamespace() + "/" + id.getPath()), recipe));
        }
        if (!list.isEmpty()) SlagEmbers.LOGGER.info("Generated {} brick forge recipes from smelting recipes", list.size());
        return list;
    }

    @SuppressWarnings("deprecation")
    private static boolean covered(List<DoubleSmeltingRecipe> existing, Ingredient input) {
        for (var forge : existing) for (var item : input.items().toList()) if (forge.getInputA().acceptsItem(item) && forge.getInputB().acceptsItem(item)) return true;
        return false;
    }
}
