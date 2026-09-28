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
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.function.Supplier;

public class EmbersRecipesJEI {
    public static final Supplier<RecipeType<RecipeHolder<DoubleSmeltingRecipe>>> DOUBLE_SMELTING = RecipeType.createFromDeferredVanilla(AllRecipes.DOUBLE_SMELTING::get);
    public static final Supplier<RecipeType<RecipeHolder<MeltingRecipe>>> MELTING = RecipeType.createFromDeferredVanilla(AllRecipes.MELTING::get);
    public static final Supplier<RecipeType<RecipeHolder<TableCastingRecipe>>> TABLE_CASTING = RecipeType.createFromDeferredVanilla(AllRecipes.TABLE_CASTING::get);
    public static final Supplier<RecipeType<RecipeHolder<BasinCastingRecipe>>> BASIN_CASTING = RecipeType.createFromDeferredVanilla(AllRecipes.BASIN_CASTING::get);
    public static final Supplier<RecipeType<RecipeHolder<AlloyingRecipe>>> ALLOYING = RecipeType.createFromDeferredVanilla(AllRecipes.ALLOYING::get);

    public static final RecipeType<HeatingCategory.Heater> HEATING = RecipeType.create(SlagEmbers.MOD_ID, "heating", HeatingCategory.Heater.class);
    public static final RecipeType<ModularType> MODULAR = RecipeType.create(SlagEmbers.MOD_ID, "modular", ModularType.class);

    public static void register() {}
}
