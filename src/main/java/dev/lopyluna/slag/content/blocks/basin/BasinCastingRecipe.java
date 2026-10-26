package dev.lopyluna.slag.content.blocks.basin;

import dev.lopyluna.slag.content.blocks.casting.CastItem;
import dev.lopyluna.slag.content.blocks.casting.CastingInput;
import dev.lopyluna.slag.content.utils.FluidInput;
import dev.lopyluna.slag.content.utils.ItemResult;
import dev.lopyluna.slag.register.AllRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import javax.annotation.Nonnull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@SuppressWarnings({"unused", "OptionalUsedAsFieldOrParameterType"})
@ParametersAreNonnullByDefault
public class BasinCastingRecipe implements Recipe<CastingInput> {
    protected final RecipeType<?> type;
    protected final String group;

    private final SizedFluidIngredient input;
    private final @Nullable CastItem castItem;
    private final int duration;
    private final float speed;
    private final ItemResult output;

    public BasinCastingRecipe(String group, SizedFluidIngredient inputs, Optional<CastItem> castItem, int duration, float speed, ItemResult output) {
        this(AllRecipes.BASIN_CASTING.get(), group, inputs, castItem, duration, speed, output);
    }
    public BasinCastingRecipe(RecipeType<?> type, String group, SizedFluidIngredient inputs, Optional<CastItem> castItem, int duration, float speed, ItemResult output) {
        this.type = type;
        this.group = group;
        this.input = inputs;
        this.castItem = castItem.orElse(null);
        this.duration = duration;
        this.speed = speed;
        this.output = output;
    }

    public SizedFluidIngredient getInput() {
        return input;
    }

    public @Nullable CastItem getCastItem() {
        return castItem;
    }

    public Optional<CastItem> getItem() {
        return Optional.ofNullable(castItem);
    }

    public int getDuration() {
        return duration;
    }

    public float getSpeed() {
        return speed;
    }

    public ItemStack getOutput() {
        return output.stack();
    }

    public ItemResult getResult() {
        return output;
    }

    @Override
    public @Nonnull String group() {
        return group;
    }

    @Override
    public boolean matches(CastingInput fluidInput, Level level) {
        return FluidInput.test(input, fluidInput.fluid());
    }

    public boolean hasEnoughFluid(FluidStack stack) {
        return input.test(stack);
    }

    @Override
    public @Nonnull ItemStack assemble(CastingInput fluidInput) {
        return output.stack().copy();
    }


    public @Nonnull ItemStack getResultItem(HolderLookup.Provider provider) {
        return output.stack().copy();
    }

    @Override
    public @Nonnull RecipeSerializer<? extends Recipe<CastingInput>> getSerializer() {
        return AllRecipes.BASIN_CASTING_SER.get();
    }

    @Override
    public @Nonnull RecipeType<? extends Recipe<CastingInput>> getType() {
        return (RecipeType<? extends Recipe<CastingInput>>) type;
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

    public static class Type implements RecipeType<BasinCastingRecipe> {
        private Type() {
        }
        public static final BasinCastingRecipe.Type INSTANCE = new BasinCastingRecipe.Type();
        @Override
        public String toString() {
            return "basin_casting";
        }
    }

    public interface Factory {
        BasinCastingRecipe create(String var1, SizedFluidIngredient var2, Optional<CastItem> var3, int var4, float var5, ItemResult var6);
    }

}
