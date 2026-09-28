package dev.lopyluna.slag.content.datagen;

import dev.lopyluna.slag.content.blocks.basin.BasinCastingRecipe;
import dev.lopyluna.slag.content.blocks.casting.CastItem;
import dev.lopyluna.slag.content.utils.FluidInput;
import dev.lopyluna.slag.content.utils.ItemResult;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import javax.annotation.Nonnull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class BasinCastingRecipeBuilder implements RecipeBuilder {
    private final ItemResult result;
    private final SizedFluidIngredient input;
    private final Map<String, Criterion<?>> criteria;
    @Nullable
    private String group;
    private final BasinCastingRecipe.Factory factory;
    private CastItem castItem;
    private int duration;
    private float speed = 1f;

    private BasinCastingRecipeBuilder(ItemResult result, FluidStack input) {
        this.criteria = new LinkedHashMap<>();
        this.result = result;
        this.input = FluidInput.of(input);
        this.factory = BasinCastingRecipe::new;
    }

    @Override
    public @Nonnull RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    public BasinCastingRecipeBuilder castItem(CastItem castItem) {
        this.castItem = castItem;
        return this;
    }

    public BasinCastingRecipeBuilder duration(int ticks) {
        this.duration = ticks;
        return this;
    }

    public BasinCastingRecipeBuilder speed(float speed) {
        this.speed = speed;
        return this;
    }

    @Override
    public @Nonnull RecipeBuilder group(@Nullable String name) {
        this.group = name;
        return this;
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceLocation loc) {
        if (this.criteria.isEmpty()) throw new IllegalStateException("No way of obtaining recipe " + loc);
        var builder = recipeOutput.advancement().addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(loc))
                .rewards(AdvancementRewards.Builder.recipe(loc)).requirements(AdvancementRequirements.Strategy.OR);
        Objects.requireNonNull(builder);
        this.criteria.forEach(builder::addCriterion);

        BasinCastingRecipe recipe = this.factory.create(Objects.requireNonNullElse(this.group, ""), this.input, Optional.ofNullable(this.castItem), this.duration, this.speed, this.result);
        recipeOutput.accept(loc, recipe, builder.build(loc.withPrefix("recipes/misc/")));

    }

    @Override
    public @Nonnull Item getResult() {
        return result.stack().getItem();
    }

    @Override
    public void save(RecipeOutput recipeOutput) {
        this.save(recipeOutput, getDefaultRecipeId(this.getResult()));
    }
    @Override
    public void save(RecipeOutput recipeOutput, String id) {
        var loc = getDefaultRecipeId(this.getResult());
        var parse = ResourceLocation.parse(id);
        if (parse.equals(loc)) throw new IllegalStateException("Recipe " + id + " should remove its 'save' argument as it is equal to default one");
        else this.save(recipeOutput, parse);
    }

    static ResourceLocation getDefaultRecipeId(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }

    public static BasinCastingRecipeBuilder create(Item result, int count, Fluid fluid, int mb) {
        return new BasinCastingRecipeBuilder(ItemResult.of(item(result, count)), fluid(fluid, mb));
    }
    public static BasinCastingRecipeBuilder create(Item result, int count, FluidStack fluid) {
        return new BasinCastingRecipeBuilder(ItemResult.of(item(result, count)), fluid);
    }
    public static BasinCastingRecipeBuilder create(ItemStack result, Fluid fluid, int mb) {
        return new BasinCastingRecipeBuilder(ItemResult.of(result), fluid(fluid, mb));
    }
    public static BasinCastingRecipeBuilder create(ItemStack result, FluidStack fluid) {
        return new BasinCastingRecipeBuilder(ItemResult.of(result), fluid);
    }
    public static BasinCastingRecipeBuilder create(TagKey<Item> result, int count, Fluid fluid, int mb) {
        return new BasinCastingRecipeBuilder(ItemResult.of(result, count), fluid(fluid, mb));
    }

    public static FluidStack fluid(Fluid fluid, int mb) {
        return new FluidStack(fluid, mb);
    }
    public static ItemStack item(Item item, int count) {
        return new ItemStack(item, count);
    }
}
