package dev.lopyluna.slag.content.datagen;

import dev.lopyluna.slag.content.blocks.crucible.AlloyingRecipe;
import dev.lopyluna.slag.content.temperature.Temperatures.Tiers;
import dev.lopyluna.slag.content.temperature.Temperatures.Type;
import dev.lopyluna.slag.content.utils.FluidInput;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import javax.annotation.Nonnull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@SuppressWarnings({"UnusedReturnValue", "unused"})
@ParametersAreNonnullByDefault
public class AlloyingRecipeBuilder implements RecipeBuilder {
    private final Fluid result;
    private final FluidStack stackResult;
    private final List<SizedFluidIngredient> inputs;
    private final Map<String, Criterion<?>> criteria;
    @Nullable
    private String group;
    private final AlloyingRecipe.Factory factory;
    private int duration;
    private float speed = 1f;
    private Tiers temperature = Tiers.HEATED;
    @Nullable
    private Type heatType;
    private boolean strict;

    private AlloyingRecipeBuilder(Fluid result, int count, List<FluidStack> inputs) {
        this(new FluidStack(result, count), inputs);
    }
    private AlloyingRecipeBuilder(FluidStack result, List<FluidStack> inputs) {
        this.criteria = new LinkedHashMap<>();
        this.result = result.getFluid();
        this.stackResult = result;
        this.inputs = inputs.stream().map(FluidInput::of).toList();
        this.factory = AlloyingRecipe::new;
    }

    @Override
    public @Nonnull RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }


    public AlloyingRecipeBuilder duration(int ticks) {
        this.duration = ticks;
        return this;
    }

    public AlloyingRecipeBuilder speed(float speed) {
        this.speed = speed;
        return this;
    }

    @Override
    public @Nonnull RecipeBuilder group(@Nullable String name) {
        this.group = name;
        return this;
    }

    public AlloyingRecipeBuilder temperature(Tiers temperature) {
        this.temperature = temperature;
        return this;
    }

    public AlloyingRecipeBuilder strict() {
        this.strict = true;
        return this;
    }

    public AlloyingRecipeBuilder heatType(@Nullable Type heatType) {
        this.heatType = heatType;
        return this;
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceLocation loc) {
        if (this.criteria.isEmpty()) throw new IllegalStateException("No way of obtaining recipe " + loc);
        var builder = recipeOutput.advancement().addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(loc))
                .rewards(AdvancementRewards.Builder.recipe(loc)).requirements(AdvancementRequirements.Strategy.OR);
        Objects.requireNonNull(builder);
        this.criteria.forEach(builder::addCriterion);

        AlloyingRecipe recipe = this.factory.create(Objects.requireNonNullElse(this.group, ""), this.inputs, this.stackResult, this.temperature, Optional.ofNullable(this.heatType), this.strict, this.duration, this.speed);
        recipeOutput.accept(loc, recipe, builder.build(loc.withPrefix("recipes/misc/")));

    }

    @Override
    public @Nonnull Item getResult() {
        return Items.AIR;
    }
    public Fluid getResultFluid() {
        return result;
    }

    @Override
    public void save(RecipeOutput recipeOutput) {
        this.save(recipeOutput, getDefaultRecipeId(this.getResultFluid()));
    }
    @Override
    public void save(RecipeOutput recipeOutput, String id) {
        var loc = getDefaultRecipeId(this.getResultFluid());
        var parse = ResourceLocation.parse(id);
        if (parse.equals(loc)) throw new IllegalStateException("Recipe " + id + " should remove its 'save' argument as it is equal to default one");
        else this.save(recipeOutput, parse);
    }

    static ResourceLocation getDefaultRecipeId(Fluid fluid) {
        return BuiltInRegistries.FLUID.getKey(fluid);
    }

    public static AlloyingRecipeBuilder create(Fluid result, int mb, List<FluidStack> fluids) {
        return new AlloyingRecipeBuilder(result, mb, fluids);
    }
    public static AlloyingRecipeBuilder create(FluidStack result, List<FluidStack> fluids) {
        return new AlloyingRecipeBuilder(result, fluids);
    }

    public static AlloyingRecipeBuilder create(Fluid result, int mb, FluidStack... fluids) {
        return new AlloyingRecipeBuilder(result, mb, fluids(fluids));
    }
    public static AlloyingRecipeBuilder create(FluidStack result, FluidStack... fluids) {
        return new AlloyingRecipeBuilder(result, fluids(fluids));
    }

    public static FluidStack fluid(Fluid fluid, int mb) {
        return new FluidStack(fluid, mb);
    }
    public static List<FluidStack> fluids(FluidStack... fluids) {
        return new ArrayList<>(List.of(fluids));
    }
}
