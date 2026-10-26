package dev.lopyluna.slag.content.jei;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.blocks.basin.BasinCastingRecipe;
import dev.lopyluna.slag.content.blocks.crucible.AlloyingRecipe;
import dev.lopyluna.slag.content.blocks.forge.DoubleSmeltingRecipe;
import dev.lopyluna.slag.content.blocks.melter.MeltingRecipe;
import dev.lopyluna.slag.content.blocks.table.TableCastingRecipe;
import dev.lopyluna.slag.content.jei.category.HeatingCategory;
import dev.lopyluna.slag.content.types.ModularType;
import dev.lopyluna.slag.register.AllRecipes;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.recipe.types.IRecipeType;

import java.util.function.Supplier;

public class EmbersRecipesJEI {
    public static final Supplier<IRecipeHolderType<DoubleSmeltingRecipe>> DOUBLE_SMELTING = IRecipeHolderType.createDeferred(AllRecipes.DOUBLE_SMELTING::get);
    public static final Supplier<IRecipeHolderType<MeltingRecipe>> MELTING = IRecipeHolderType.createDeferred(AllRecipes.MELTING::get);
    public static final Supplier<IRecipeHolderType<TableCastingRecipe>> TABLE_CASTING = IRecipeHolderType.createDeferred(AllRecipes.TABLE_CASTING::get);
    public static final Supplier<IRecipeHolderType<BasinCastingRecipe>> BASIN_CASTING = IRecipeHolderType.createDeferred(AllRecipes.BASIN_CASTING::get);
    public static final Supplier<IRecipeHolderType<AlloyingRecipe>> ALLOYING = IRecipeHolderType.createDeferred(AllRecipes.ALLOYING::get);

    public static final IRecipeType<HeatingCategory.Heater> HEATING = IRecipeType.create(SlagEmbers.MOD_ID, "heating", HeatingCategory.Heater.class);
    public static final IRecipeType<ModularType> MODULAR = IRecipeType.create(SlagEmbers.MOD_ID, "modular", ModularType.class);

    public static void register() {}
}
