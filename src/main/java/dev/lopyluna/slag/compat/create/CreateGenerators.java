package dev.lopyluna.slag.compat.create;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.api.RecipeGenerators;
import dev.lopyluna.slag.config.SlagCommonConfigs;
import dev.lopyluna.slag.content.blocks.basin.BasinCastingRecipe;
import dev.lopyluna.slag.content.blocks.crucible.AlloyingRecipe;
import dev.lopyluna.slag.content.blocks.melter.MelterBE;
import dev.lopyluna.slag.content.blocks.melter.MeltingRecipe;
import dev.lopyluna.slag.content.blocks.table.TableCastingRecipe;
import dev.lopyluna.slag.content.temperature.Temperatures.Tiers;
import dev.lopyluna.slag.content.utils.ItemResult;
import dev.lopyluna.slag.register.AllTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.*;

public class CreateGenerators {
    public static void register() {
        RecipeGenerators.register(CreateGenerators::generate);
    }

    private static List<RecipeHolder<?>> generate(Collection<RecipeHolder<?>> recipes, HolderLookup.Provider registries) {
        if (SlagCommonConfigs.SPEC.isLoaded() && !SlagCommonConfigs.GENERATED_CREATE.get()) return List.of();
        var mixing = AllRecipeTypes.MIXING.getType();
        var compacting = AllRecipeTypes.COMPACTING.getType();
        var alloyed = Collections.newSetFromMap(new IdentityHashMap<Fluid, Boolean>());
        var melted = Collections.newSetFromMap(new IdentityHashMap<Item, Boolean>());
        var cast = Collections.newSetFromMap(new IdentityHashMap<Item, Boolean>());
        var sources = new ArrayList<RecipeHolder<?>>();
        for (var holder : recipes) {
            var recipe = holder.value();
            if (recipe instanceof AlloyingRecipe alloying) alloyed.add(alloying.getOutput().getFluid());
            else if (recipe instanceof MeltingRecipe melting) {
                for (var stack : melting.getInputs()) melted.add(stack.getItem());
                for (var stack : melting.getInput().getItems()) melted.add(stack.getItem());
            } else if (recipe instanceof TableCastingRecipe table) cast.add(table.getOutput().getItem());
            else if (recipe instanceof BasinCastingRecipe basin) cast.add(basin.getOutput().getItem());
            else if (recipe.getType() == mixing || recipe.getType() == compacting) sources.add(holder);
        }

        var list = new ArrayList<RecipeHolder<?>>();
        var counts = new int[3];
        for (var holder : sources) {
            if (!(holder.value() instanceof ProcessingRecipe<?, ?> processing)) continue;
            var id = holder.id();
            if (processing.getType() == mixing) {
                var alloy = alloying(processing, id, alloyed);
                if (alloy != null) {
                    list.add(alloy);
                    counts[0]++;
                    continue;
                }
                var melt = melting(processing, id, melted);
                if (melt != null) {
                    list.add(melt);
                    counts[1]++;
                }
                continue;
            }
            var casting = casting(processing, id, cast);
            if (casting == null) continue;
            list.add(casting);
            counts[2]++;
        }
        if (!list.isEmpty()) SlagEmbers.LOGGER.info("Generated {} alloying, {} melting and {} casting recipes from Create's mixing and compacting recipes", counts[0], counts[1], counts[2]);
        return list;
    }

    private static @Nullable RecipeHolder<?> alloying(ProcessingRecipe<?, ?> recipe, ResourceLocation id, Set<Fluid> alloyed) {
        if (!recipe.getIngredients().isEmpty() || !recipe.getRollableResults().isEmpty()) return null;
        var inputs = recipe.getFluidIngredients();
        var outputs = recipe.getFluidResults();
        if (inputs.isEmpty() || outputs.size() != 1 || outputs.getFirst().isEmpty()) return null;
        var output = outputs.getFirst();
        if (!alloyed.add(output.getFluid())) return null;
        var tier = tier(recipe.getRequiredHeat(), Tiers.WARM);
        var alloy = new AlloyingRecipe("", List.copyOf(inputs), output.copy(), tier, Optional.empty(), false, 0, 1f);
        return new RecipeHolder<>(SlagEmbers.loc("alloying/generated/" + id.getNamespace() + "/" + id.getPath()), alloy);
    }

    private static @Nullable RecipeHolder<?> melting(ProcessingRecipe<?, ?> recipe, ResourceLocation id, Set<Item> melted) {
        if (!recipe.getFluidIngredients().isEmpty() || !recipe.getRollableResults().isEmpty()) return null;
        var ingredients = recipe.getIngredients();
        var results = recipe.getFluidResults();
        if (ingredients.isEmpty() || results.isEmpty()) return null;
        var input = ingredients.getFirst();
        var items = items(input);
        if (items.isEmpty()) return null;
        for (var ingredient : ingredients) if (!items(ingredient).equals(items)) return null;
        for (var item : items) if (melted.contains(item)) return null;

        var count = ingredients.size();
        var outputs = new ArrayList<FluidStack>();
        var total = 0;
        for (var result : results) {
            var amount = result.getAmount() / count;
            if (result.isEmpty() || amount <= 0) return null;
            outputs.add(result.copyWithAmount(amount));
            total += amount;
        }
        if (total > MelterBE.CAPACITY) return null;
        melted.addAll(items);
        var tier = tier(recipe.getRequiredHeat(), Tiers.SMOLDERING);
        var melt = new MeltingRecipe("", List.of(), input, outputs, tier, Optional.empty(), 0, 1f);
        return new RecipeHolder<>(SlagEmbers.loc("melting/generated/" + id.getNamespace() + "/" + id.getPath()), melt);
    }

    private static @Nullable RecipeHolder<?> casting(ProcessingRecipe<?, ?> recipe, ResourceLocation id, Set<Item> cast) {
        if (!recipe.getIngredients().isEmpty() || !recipe.getFluidResults().isEmpty()) return null;
        var inputs = recipe.getFluidIngredients();
        var outputs = recipe.getRollableResults();
        if (inputs.size() != 1 || outputs.size() != 1) return null;
        var output = outputs.getFirst();
        var result = output.getStack();
        if (output.getChance() < 1 || result.isEmpty() || !cast.add(result.getItem())) return null;
        var path = id.getNamespace() + "/" + id.getPath();
        if (result.getItem() instanceof BlockItem) {
            var basin = new BasinCastingRecipe("", inputs.getFirst(), Optional.empty(), 0, 1f, ItemResult.of(result.copy()));
            return new RecipeHolder<>(SlagEmbers.loc("casting/basin/generated/" + path), basin);
        }
        var table = new TableCastingRecipe("", castType(result), inputs.getFirst(), Optional.empty(), 0, 1f, ItemResult.of(result.copy()));
        return new RecipeHolder<>(SlagEmbers.loc("casting/table/generated/" + path), table);
    }

    private static Optional<TagKey<Item>> castType(ItemStack stack) {
        for (var tag : AllTags.CASTS) if (stack.is(tag)) return Optional.of(tag);
        return Optional.empty();
    }

    private static Set<Item> items(Ingredient ingredient) {
        var items = Collections.newSetFromMap(new IdentityHashMap<Item, Boolean>());
        for (var stack : ingredient.getItems()) if (!stack.isEmpty()) items.add(stack.getItem());
        return items;
    }

    private static Tiers tier(HeatCondition heat, Tiers none) {
        return switch (heat) {
            case HEATED -> Tiers.HEATED;
            case SUPERHEATED -> Tiers.BLAZING;
            default -> none;
        };
    }
}
