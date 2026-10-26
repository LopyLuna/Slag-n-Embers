package dev.lopyluna.slag.content.blocks.melter;

import com.google.common.base.Suppliers;
import dev.lopyluna.slag.content.blocks.crucible.AlloyingRecipe;
import dev.lopyluna.slag.content.temperature.Temperatures;
import dev.lopyluna.slag.content.temperature.Temperatures.Tiers;
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
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import javax.annotation.Nonnull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@SuppressWarnings({"unused", "OptionalUsedAsFieldOrParameterType"})
@ParametersAreNonnullByDefault
public class MeltingRecipe implements Recipe<SingleRecipeInput> {
    protected final RecipeType<?> type;
    protected final String group;

    private final List<ItemStackTemplate> inputs;
    private final Optional<Ingredient> input;
    private final List<FluidStackTemplate> outputs;
    private final Supplier<List<ItemStack>> inputStacks;
    private final Supplier<List<FluidStack>> outputStacks;
    public final Tiers temperature;
    public final @Nullable Temperatures.Type heatType;
    public final int duration;
    public final float speed;

    public MeltingRecipe(String group, List<ItemStackTemplate> inputs, Optional<Ingredient> input, List<FluidStackTemplate> outputs, Tiers temperature, Optional<Temperatures.Type> heatType, int duration, float speed) {
        this(AllRecipes.MELTING.get(), group, inputs, input, outputs, temperature, heatType, duration, speed);
    }
    public MeltingRecipe(RecipeType<?> type, String group, List<ItemStackTemplate> inputs, Optional<Ingredient> input, List<FluidStackTemplate> outputs, Tiers temperature, Optional<Temperatures.Type> heatType, int duration, float speed) {
        this.type = type;
        this.group = group;
        this.inputs = inputs;
        this.input = input;
        this.outputs = outputs;
        this.inputStacks = Suppliers.memoize(() -> inputs.stream().map(ItemStackTemplate::create).toList());
        this.outputStacks = Suppliers.memoize(() -> outputs.stream().map(FluidStackTemplate::create).toList());
        this.temperature = temperature;
        this.heatType = heatType.orElse(null);
        this.duration = duration;
        this.speed = speed;
    }

    public int getDuration() {
        return duration;
    }

    public float getSpeed() {
        return speed;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        if (input.item().isEmpty()) return false;
        if (!inputs.isEmpty()) for (var stack : getInputs()) {
            if (stack.isEmpty()) continue;
            if (ItemStack.isSameItemSameComponents(stack, input.item())) return true;
        }
        return this.input.isPresent() && this.input.get().test(input.item());
    }

    public List<FluidStack> assembleWithFluid(AlloyingRecipe.AlloyRecipeInput alloyRecipeInput, HolderLookup.Provider provider) {
        return getResultFluids(provider);
    }
    public List<FluidStack> getResultFluids(HolderLookup.Provider provider) {
        var list = new ArrayList<FluidStack>();
        var i = 0;
        for (var stack : getOutputs()) {
            if (i > 12) break;
            list.add(stack.copy());
            i++;
        }
        return list;
    }

    @Override public @Nonnull String group() {
        return group;
    }
    public List<ItemStack> getInputs() {
        return inputStacks.get();
    }
    public List<ItemStackTemplate> getInputTemplates() {
        return inputs;
    }
    public Optional<Ingredient> getInput() {
        return input;
    }
    public List<FluidStackTemplate> getOutputTemplates() {
        return outputs;
    }
    public List<FluidStack> getOutputs() {
        return outputStacks.get();
    }
    @Override public @Nonnull ItemStack assemble(SingleRecipeInput input) {
        return ItemStack.EMPTY;
    }
    public @Nonnull ItemStack getResultItem(HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }

    @Override public @Nonnull RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
        return AllRecipes.MELTING_SER.get();
    }
    @Override public @Nonnull RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return (RecipeType<? extends Recipe<SingleRecipeInput>>) type;
    }

    @Override public boolean showNotification() {
        return false;
    }
    @Override public @Nonnull PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }
    @Override public @Nonnull RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public static class Type implements RecipeType<MeltingRecipe> {
        private Type() {}
        public static final MeltingRecipe.Type INSTANCE = new MeltingRecipe.Type();
        @Override public String toString() {
            return "melting";
        }
    }

    public interface Factory {
        MeltingRecipe create(String var1, List<ItemStackTemplate> var2, Optional<Ingredient> var3, List<FluidStackTemplate> var4, Tiers var5, Optional<Temperatures.Type> var6, int var7, float var8);
    }
}

