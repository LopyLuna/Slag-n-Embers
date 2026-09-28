package dev.lopyluna.slag.compat.create;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import dev.lopyluna.slag.api.RecyclingSources;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.List;

public class CreateRecycling {
    private static final List<AllRecipeTypes> PROCESSING = List.of(
            AllRecipeTypes.CONVERSION, AllRecipeTypes.CRUSHING, AllRecipeTypes.CUTTING, AllRecipeTypes.MILLING,
            AllRecipeTypes.MIXING, AllRecipeTypes.COMPACTING, AllRecipeTypes.PRESSING, AllRecipeTypes.SANDPAPER_POLISHING,
            AllRecipeTypes.DEPLOYING, AllRecipeTypes.ITEM_APPLICATION, AllRecipeTypes.SPLASHING, AllRecipeTypes.HAUNTING);

    public static void register() {
        for (var type : PROCESSING) RecyclingSources.register(type.getType(), CreateRecycling::processing);
        RecyclingSources.register(AllRecipeTypes.MECHANICAL_CRAFTING.getType(), RecyclingSources::vanillaLike);
    }

    private static List<RecyclingSources.Source> processing(Recipe<?> recipe, HolderLookup.Provider registries) {
        if (!(recipe instanceof ProcessingRecipe<?, ?> processing)) return List.of();
        if (!processing.getFluidIngredients().isEmpty() || !processing.getFluidResults().isEmpty()) return List.of();
        var result = ItemStack.EMPTY;
        for (var output : processing.getRollableResults()) {
            if (output.getChance() < 1) continue;
            if (!result.isEmpty()) return List.of();
            result = output.getStack();
        }
        if (result.isEmpty()) return List.of();
        return List.of(new RecyclingSources.Source(inputs(processing), result));
    }

    private static List<Ingredient> inputs(ProcessingRecipe<?, ?> recipe) {
        if (!(recipe instanceof ItemApplicationRecipe application) || !application.shouldKeepHeldItem()) return recipe.getIngredients();
        return List.of(application.getProcessedItem());
    }
}
