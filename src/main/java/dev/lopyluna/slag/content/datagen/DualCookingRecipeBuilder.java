package dev.lopyluna.slag.content.datagen;

import dev.lopyluna.slag.content.AllUtils;
import dev.lopyluna.slag.content.blocks.forge.DoubleSmeltingRecipe;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import javax.annotation.Nonnull;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("NullableProblems")
public class DualCookingRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final Item result;
    private final ItemStack stackResult;
    private final Ingredient ingA;
    private final Ingredient ingB;
    private final float exp;
    private final int time;
    private final Map<String, Criterion<?>> criteria;
    @Nullable
    private String group;
    private final DoubleSmeltingRecipe.Factory factory;

    private DualCookingRecipeBuilder(RecipeCategory category, ItemLike result, int count, Ingredient ingA, Ingredient ingB, float exp, int time) {
        this(category, new ItemStack(result, count), ingA, ingB, exp, time);
    }
    private DualCookingRecipeBuilder(RecipeCategory category, ItemStack result, Ingredient ingA, Ingredient ingB, float exp, int time) {
        this.criteria = new LinkedHashMap<>();
        this.category = category;
        this.result = result.getItem();
        this.stackResult = result;
        this.ingA = ingA;
        this.ingB = ingB;
        this.exp = exp;
        this.time = time;
        this.factory = DoubleSmeltingRecipe::new;
    }

    @Override
    public @Nonnull RecipeBuilder unlockedBy(@Nonnull String name, @Nonnull Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public @Nonnull RecipeBuilder group(@Nullable String name) {
        this.group = name;
        return this;
    }

    public @Nonnull Item getResult() {
        return this.result;
    }

    @Override
    public @Nonnull ResourceKey<Recipe<?>> defaultId() {
        return ResourceKey.create(Registries.RECIPE, BuiltInRegistries.ITEM.getKey(result));
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceKey<Recipe<?>> key) {
        if (this.criteria.isEmpty()) throw new IllegalStateException("No way of obtaining recipe " + key.identifier());
        var builder = recipeOutput.advancement().addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(key))
                .rewards(AdvancementRewards.Builder.recipe(key)).requirements(AdvancementRequirements.Strategy.OR);
        Objects.requireNonNull(builder);
        this.criteria.forEach(builder::addCriterion);
        DoubleSmeltingRecipe recipe = this.factory.create(Objects.requireNonNullElse(this.group, ""), this.ingA, this.ingB, ItemStackTemplate.fromNonEmptyStack(this.stackResult), this.exp, this.time);
        recipeOutput.accept(key, recipe, builder.build(key.identifier().withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }


    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, int count, TagKey<Item> ingA, TagKey<Item> ingB, float exp, int time) {
        return new DualCookingRecipeBuilder(category, result, count, AllUtils.tag(ingA), AllUtils.tag(ingB), exp, time);
    }
    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, int count, ItemLike ingA, ItemLike ingB, float exp, int time) {
        return new DualCookingRecipeBuilder(category, result, count, Ingredient.of(ingA), Ingredient.of(ingB), exp, time);
    }
    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, int count, ItemLike ingA, TagKey<Item> ingB, float exp, int time) {
        return new DualCookingRecipeBuilder(category, result, count, Ingredient.of(ingA), AllUtils.tag(ingB), exp, time);
    }
    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, int count, TagKey<Item> ingA, ItemLike ingB, float exp, int time) {
        return new DualCookingRecipeBuilder(category, result, count, AllUtils.tag(ingA), Ingredient.of(ingB), exp, time);
    }
    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, int count, ItemLike ingA, Ingredient ingB, float exp, int time) {
        return new DualCookingRecipeBuilder(category, result, count, Ingredient.of(ingA), ingB, exp, time);
    }
    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, int count, TagKey<Item> ingA, Ingredient ingB, float exp, int time) {
        return new DualCookingRecipeBuilder(category, result, count, AllUtils.tag(ingA), ingB, exp, time);
    }
    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, int count, Ingredient ingA, TagKey<Item> ingB, float exp, int time) {
        return new DualCookingRecipeBuilder(category, result, count, ingA, AllUtils.tag(ingB), exp, time);
    }
    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, int count, Ingredient ingA, ItemLike ingB, float exp, int time) {
        return new DualCookingRecipeBuilder(category, result, count, ingA, Ingredient.of(ingB), exp, time);
    }
    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, int count, Ingredient ingA, Ingredient ingB, float exp, int time) {
        return new DualCookingRecipeBuilder(category, result, count, ingA, ingB, exp, time);
    }

    public static DualCookingRecipeBuilder doubleSingle(RecipeCategory category, ItemLike result, Ingredient ing, float exp) {
        return new DualCookingRecipeBuilder(category, result, 2, ing, ing, exp * 2f, 200);
    }


    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, TagKey<Item> ingA, TagKey<Item> ingB, float exp, int time) {
        return new DualCookingRecipeBuilder(category, result, 1, AllUtils.tag(ingA), AllUtils.tag(ingB), exp, time);
    }
    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, ItemLike ingA, ItemLike ingB, float exp, int time) {
        return new DualCookingRecipeBuilder(category, result, 1, Ingredient.of(ingA), Ingredient.of(ingB), exp, time);
    }
    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, ItemLike ingA, TagKey<Item> ingB, float exp, int time) {
        return new DualCookingRecipeBuilder(category, result, 1, Ingredient.of(ingA), AllUtils.tag(ingB), exp, time);
    }
    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, TagKey<Item> ingA, ItemLike ingB, float exp, int time) {
        return new DualCookingRecipeBuilder(category, result, 1, AllUtils.tag(ingA), Ingredient.of(ingB), exp, time);
    }
    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, ItemLike ingA, Ingredient ingB, float exp, int time) {
        return new DualCookingRecipeBuilder(category, result, 1, Ingredient.of(ingA), ingB, exp, time);
    }
    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, TagKey<Item> ingA, Ingredient ingB, float exp, int time) {
        return new DualCookingRecipeBuilder(category, result, 1, AllUtils.tag(ingA), ingB, exp, time);
    }
    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, Ingredient ingA, TagKey<Item> ingB, float exp, int time) {
        return new DualCookingRecipeBuilder(category, result, 1, ingA, AllUtils.tag(ingB), exp, time);
    }
    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, Ingredient ingA, ItemLike ingB, float exp, int time) {
        return new DualCookingRecipeBuilder(category, result, 1, ingA, Ingredient.of(ingB), exp, time);
    }
    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, Ingredient ingA, Ingredient ingB, float exp, int time) {
        return new DualCookingRecipeBuilder(category, result, 1, ingA, ingB, exp, time);
    }


    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, int count, TagKey<Item> ingA, TagKey<Item> ingB, float exp) {
        return new DualCookingRecipeBuilder(category, result, count, AllUtils.tag(ingA), AllUtils.tag(ingB), exp, 200);
    }
    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, int count, ItemLike ingA, ItemLike ingB, float exp) {
        return new DualCookingRecipeBuilder(category, result, count, Ingredient.of(ingA), Ingredient.of(ingB), exp, 200);
    }
    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, int count, ItemLike ingA, TagKey<Item> ingB, float exp) {
        return new DualCookingRecipeBuilder(category, result, count, Ingredient.of(ingA), AllUtils.tag(ingB), exp, 200);
    }
    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, int count, TagKey<Item> ingA, ItemLike ingB, float exp) {
        return new DualCookingRecipeBuilder(category, result, count, AllUtils.tag(ingA), Ingredient.of(ingB), exp, 200);
    }
    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, int count, ItemLike ingA, Ingredient ingB, float exp) {
        return new DualCookingRecipeBuilder(category, result, count, Ingredient.of(ingA), ingB, exp, 200);
    }
    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, int count, TagKey<Item> ingA, Ingredient ingB, float exp) {
        return new DualCookingRecipeBuilder(category, result, count, AllUtils.tag(ingA), ingB, exp, 200);
    }
    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, int count, Ingredient ingA, TagKey<Item> ingB, float exp) {
        return new DualCookingRecipeBuilder(category, result, count, ingA, AllUtils.tag(ingB), exp, 200);
    }
    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, int count, Ingredient ingA, ItemLike ingB, float exp) {
        return new DualCookingRecipeBuilder(category, result, count, ingA, Ingredient.of(ingB), exp, 200);
    }
    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, int count, Ingredient ingA, Ingredient ingB, float exp) {
        return new DualCookingRecipeBuilder(category, result, count, ingA, ingB, exp, 200);
    }


    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, TagKey<Item> ingA, TagKey<Item> ingB, float exp) {
        return new DualCookingRecipeBuilder(category, result, 1, AllUtils.tag(ingA), AllUtils.tag(ingB), exp, 200);
    }
    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, ItemLike ingA, ItemLike ingB, float exp) {
        return new DualCookingRecipeBuilder(category, result, 1, Ingredient.of(ingA), Ingredient.of(ingB), exp, 200);
    }
    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, ItemLike ingA, TagKey<Item> ingB, float exp) {
        return new DualCookingRecipeBuilder(category, result, 1, Ingredient.of(ingA), AllUtils.tag(ingB), exp, 200);
    }
    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, TagKey<Item> ingA, ItemLike ingB, float exp) {
        return new DualCookingRecipeBuilder(category, result, 1, AllUtils.tag(ingA), Ingredient.of(ingB), exp, 200);
    }
    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, ItemLike ingA, Ingredient ingB, float exp) {
        return new DualCookingRecipeBuilder(category, result, 1, Ingredient.of(ingA), ingB, exp, 200);
    }
    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, TagKey<Item> ingA, Ingredient ingB, float exp) {
        return new DualCookingRecipeBuilder(category, result, 1, AllUtils.tag(ingA), ingB, exp, 200);
    }
    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, Ingredient ingA, TagKey<Item> ingB, float exp) {
        return new DualCookingRecipeBuilder(category, result, 1, ingA, AllUtils.tag(ingB), exp, 200);
    }
    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, Ingredient ingA, ItemLike ingB, float exp) {
        return new DualCookingRecipeBuilder(category, result, 1, ingA, Ingredient.of(ingB), exp, 200);
    }
    public static DualCookingRecipeBuilder create(RecipeCategory category, ItemLike result, Ingredient ingA, Ingredient ingB, float exp) {
        return new DualCookingRecipeBuilder(category, result, 1, ingA, ingB, exp, 200);
    }
}
