package dev.lopyluna.slag.content.blocks.melter;

import dev.lopyluna.slag.content.blocks.crucible.AlloyingRecipe;
import dev.lopyluna.slag.content.temperature.Temperatures;
import dev.lopyluna.slag.content.temperature.Temperatures.Tiers;
import dev.lopyluna.slag.register.AllRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import javax.annotation.Nonnull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings({"unused", "OptionalUsedAsFieldOrParameterType"})
@ParametersAreNonnullByDefault
public class MeltingRecipe implements Recipe<SingleRecipeInput> {
    protected final RecipeType<?> type;
    protected final String group;

    private final List<ItemStack> inputs;
    private final Ingredient input;
    private final List<FluidStack> outputs;
    public final Tiers temperature;
    public final @Nullable Temperatures.Type heatType;
    public final int duration;
    public final float speed;

    public MeltingRecipe(String group, List<ItemStack> inputs, Ingredient input, List<FluidStack> outputs, Tiers temperature, Optional<Temperatures.Type> heatType, int duration, float speed) {
        this(AllRecipes.MELTING.get(), group, inputs, input, outputs, temperature, heatType, duration, speed);
    }
    public MeltingRecipe(RecipeType<?> type, String group, List<ItemStack> inputs, Ingredient input, List<FluidStack> outputs, Tiers temperature, Optional<Temperatures.Type> heatType, int duration, float speed) {
        this.type = type;
        this.group = group;
        this.inputs = inputs;
        this.input = input;
        this.outputs = outputs;
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
        if (!inputs.isEmpty()) for (var stack : inputs) {
            if (stack.isEmpty()) continue;
            if (ItemStack.isSameItemSameComponents(stack, input.item())) return true;
        }
        return this.input.test(input.item());
    }

    public List<FluidStack> assembleWithFluid(AlloyingRecipe.AlloyRecipeInput alloyRecipeInput, HolderLookup.Provider provider) {
        return getResultFluids(provider);
    }
    public List<FluidStack> getResultFluids(HolderLookup.Provider provider) {
        var list = new ArrayList<FluidStack>();
        var i = 0;
        for (var stack : outputs) {
            if (i > 12) break;
            list.add(stack.copy());
            i++;
        }
        return list;
    }

    @Override public @Nonnull String getGroup() {
        return group;
    }
    public List<ItemStack> getInputs() {
        return inputs;
    }
    public Ingredient getInput() {
        return input;
    }
    public List<FluidStack> getOutputs() {
        return outputs;
    }
    @Override public boolean canCraftInDimensions(int i, int i1) {
        return false;
    }
    @Override public @Nonnull ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }
    @Override public @Nonnull ItemStack getResultItem(HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }

    @Override public @Nonnull RecipeSerializer<?> getSerializer() {
        return AllRecipes.MELTING_SER.get();
    }
    @Override public @Nonnull RecipeType<?> getType() {
        return type;
    }

    public static class Type implements RecipeType<MeltingRecipe> {
        private Type() {}
        public static final MeltingRecipe.Type INSTANCE = new MeltingRecipe.Type();
        @Override public String toString() {
            return "melting";
        }
    }

    public interface Factory {
        MeltingRecipe create(String var1, List<ItemStack> var2, Ingredient var3, List<FluidStack> var4, Tiers var5, Optional<Temperatures.Type> var6, int var7, float var8);
    }
}

