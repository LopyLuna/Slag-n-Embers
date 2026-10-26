package dev.lopyluna.slag.content.blocks.forge;

import com.google.common.base.Suppliers;
import dev.lopyluna.slag.register.AllRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.level.Level;
import javax.annotation.Nonnull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public class DoubleSmeltingRecipe implements Recipe<DoubleSmeltingRecipe.DoubleRecipeInput> {
    protected final RecipeType<?> type;

    protected final String group;

    private final Ingredient inputA;
    private final Ingredient inputB;

    private final ItemStackTemplate output;
    private final Supplier<ItemStack> outputStack;

    private final int cookingTime;
    private final float experience;

    public DoubleSmeltingRecipe(String group, Ingredient inputA, Ingredient inputB, ItemStackTemplate output, float experience, int cookingTime) {
        this(AllRecipes.DOUBLE_SMELTING.get(), group, inputA, inputB, output, cookingTime, experience);
    }

    public DoubleSmeltingRecipe(RecipeType<?> type, String group, Ingredient inputA, Ingredient inputB, ItemStackTemplate output, int cookingTime, float experience) {
        this.inputA = inputA;
        this.inputB = inputB;
        this.output = output;
        this.outputStack = Suppliers.memoize(output::create);
        this.type = type;
        this.group = group;
        this.cookingTime = cookingTime;
        this.experience = experience;
    }

    @Override
    public @Nonnull String group() {
        return group;
    }

    public Ingredient getInputA() {
        return inputA;
    }
    public Ingredient getInputB() {
        return inputB;
    }

    public ItemStack getOutput() {
        return outputStack.get();
    }
    public ItemStackTemplate getOutputTemplate() {
        return output;
    }

    public int getCookingTime() {
        return cookingTime;
    }
    public float getExperience() {
        return experience;
    }


    @Override
    public boolean matches(DoubleRecipeInput doubleRecipeInput, Level level) {
        return (inputA.test(doubleRecipeInput.getItem(0)) && inputB.test(doubleRecipeInput.getItem(1))) ||
                (inputA.test(doubleRecipeInput.getItem(1)) && inputB.test(doubleRecipeInput.getItem(0)));
    }

    @Override
    public @Nonnull ItemStack assemble(DoubleRecipeInput doubleRecipeInput) {
        return getOutput().copy();
    }


    public @Nonnull ItemStack getResultItem(HolderLookup.Provider provider) {
        return getOutput().copy();
    }

    @Override
    public @Nonnull RecipeSerializer<? extends Recipe<DoubleRecipeInput>> getSerializer() {
        return AllRecipes.DOUBLE_SMELTING_SER.get();
    }

    @Override
    public @Nonnull RecipeType<? extends Recipe<DoubleRecipeInput>> getType() {
        return (RecipeType<? extends Recipe<DoubleRecipeInput>>) type;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public @Nonnull PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public @Nonnull RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public static class Type implements RecipeType<DoubleSmeltingRecipe> {
        private Type() {
        }
        public static final Type INSTANCE = new Type();
        @Override
        public String toString() {
            return "double_smelting";
        }
    }

    public interface Factory {
        DoubleSmeltingRecipe create(String var1, Ingredient var2, Ingredient var3, ItemStackTemplate var4, float var5, int var6);
    }

    public record DoubleRecipeInput(ItemStack itemA, ItemStack itemB) implements RecipeInput {
        @Override
        public @Nonnull ItemStack getItem(int i) {
            ItemStack stack;
            switch (i) {
                case 0 -> stack = this.itemA;
                case 1 -> stack = this.itemB;
                default -> throw new IllegalArgumentException("Recipe does not contain slot " + i);
            }

            return stack;
        }

        @Override
        public int size() {
            return 2;
        }

        @Override
        public boolean isEmpty() {
            return itemA.isEmpty() && itemB.isEmpty();
        }
    }
}
