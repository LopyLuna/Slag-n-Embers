package dev.lopyluna.slag.content.datagen;

import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.blocks.melter.MelterBE;
import dev.lopyluna.slag.content.blocks.melter.MeltingRecipe;
import dev.lopyluna.slag.content.temperature.Temperatures.Tiers;
import dev.lopyluna.slag.content.temperature.Temperatures.Type;
import dev.lopyluna.slag.register.AllTags;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import javax.annotation.Nonnull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

import static com.tterrag.registrate.providers.RegistrateRecipeProvider.has;

@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public class MeltingRecipeBuilder implements RecipeBuilder {
    private final List<Fluid> result;
    private final List<FluidStack> stackResult;
    private final List<ItemStack> inputStacks;
    private final Ingredient input;
    private final Map<String, Criterion<?>> criteria;
    @Nullable
    private String group;
    private final MeltingRecipe.Factory factory;
    private int duration;
    private float speed = 1f;
    private Tiers temperature = Tiers.HEATED;
    @Nullable
    private Type heatType;

    private MeltingRecipeBuilder(Fluid result, int count, Ingredient input) {
        this(new FluidStack(result, count), input);
    }
    private MeltingRecipeBuilder(Fluid result, int count, List<ItemStack> inputStacks) {
        this(new FluidStack(result, count), inputStacks);
    }
    private MeltingRecipeBuilder(FluidStack result, Ingredient input) {
        this(new ArrayList<>(List.of(result)), input);
    }
    private MeltingRecipeBuilder(FluidStack result, List<ItemStack> inputStacks) {
        this(new ArrayList<>(List.of(result)), inputStacks);
    }
    private MeltingRecipeBuilder(List<Fluid> result, List<Integer> resultCount, Ingredient input) {
        this(result.stream().map(fluid -> new FluidStack(fluid, resultCount.get(result.indexOf(fluid)))).toList(), input);
    }
    private MeltingRecipeBuilder(List<Fluid> result, List<Integer> resultCount, List<ItemStack> inputStacks) {
        this(result.stream().map(fluid -> new FluidStack(fluid, resultCount.get(result.indexOf(fluid)))).toList(), inputStacks);
    }
    private MeltingRecipeBuilder(List<FluidStack> result, Ingredient input) {
        this.criteria = new LinkedHashMap<>();
        this.result = result.stream().map(FluidStack::getFluid).toList();
        this.stackResult = result;
        this.inputStacks = new ArrayList<>();
        this.input = input;
        this.factory = MeltingRecipe::new;
    }
    private MeltingRecipeBuilder(List<FluidStack> result, List<ItemStack> inputStacks) {
        this.criteria = new LinkedHashMap<>();
        this.result = result.stream().map(FluidStack::getFluid).toList();
        this.stackResult = result;
        this.inputStacks = inputStacks;
        this.input = Ingredient.EMPTY;
        this.factory = MeltingRecipe::new;
    }

    @Override
    public @Nonnull RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }


    public MeltingRecipeBuilder duration(int ticks) {
        this.duration = ticks;
        return this;
    }

    public MeltingRecipeBuilder speed(float speed) {
        this.speed = speed;
        return this;
    }

    @Override
    public @Nonnull RecipeBuilder group(@Nullable String name) {
        this.group = name;
        return this;
    }

    public MeltingRecipeBuilder temperature(Tiers temperature) {
        this.temperature = temperature;
        return this;
    }

    public MeltingRecipeBuilder heatType(@Nullable Type heatType) {
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

        MeltingRecipe recipe = this.factory.create(Objects.requireNonNullElse(this.group, ""), this.inputStacks, this.input, this.stackResult, this.temperature, Optional.ofNullable(this.heatType), this.duration, this.speed);
        recipeOutput.accept(loc, recipe, builder.build(loc.withPrefix("recipes/misc/")));
    }

    @Override
    public @Nonnull Item getResult() {
        return Items.AIR;
    }
    public List<Fluid> getResultFluid() {
        return result;
    }

    @Override
    public void save(RecipeOutput recipeOutput) {
        this.save(recipeOutput, getDefaultRecipeId(this.getResultFluid().getFirst()));
    }
    @Override
    public void save(RecipeOutput recipeOutput, String id) {
        var loc = getDefaultRecipeId(this.getResultFluid().getFirst());
        var parse = ResourceLocation.parse(id);
        if (parse.equals(loc)) throw new IllegalStateException("Recipe " + id + " should remove its 'save' argument as it is equal to default one");
        else this.save(recipeOutput, parse);
    }

    static ResourceLocation getDefaultRecipeId(Fluid fluid) {
        return BuiltInRegistries.FLUID.getKey(fluid);
    }

    public static MeltingRecipeBuilder create(Fluid result, int mb, Ingredient ingredient) {
        return new MeltingRecipeBuilder(result, mb, ingredient);
    }
    public static MeltingRecipeBuilder create(FluidStack result, Ingredient ingredient) {
        return new MeltingRecipeBuilder(result, ingredient);
    }

    public static MeltingRecipeBuilder create(Fluid result, int mb, ItemStack item) {
        return new MeltingRecipeBuilder(result, mb, new ArrayList<>(List.of(item)));
    }
    public static MeltingRecipeBuilder create(FluidStack result, ItemStack item) {
        return new MeltingRecipeBuilder(result, new ArrayList<>(List.of(item)));
    }

    public static MeltingRecipeBuilder create(Fluid result, int mb, List<ItemStack> items) {
        return new MeltingRecipeBuilder(result, mb, items);
    }
    public static MeltingRecipeBuilder create(FluidStack result, List<ItemStack> items) {
        return new MeltingRecipeBuilder(result, items);
    }

    public static MeltingRecipeBuilder create(List<Fluid> result, List<Integer> mb, Ingredient ingredient) {
        return new MeltingRecipeBuilder(result, mb, ingredient);
    }
    public static MeltingRecipeBuilder create(List<Fluid> result, List<Integer> mb, List<ItemStack> items) {
        return new MeltingRecipeBuilder(result, mb, items);
    }
    public static MeltingRecipeBuilder create(List<FluidStack> result, Ingredient ingredient) {
        return new MeltingRecipeBuilder(result, ingredient);
    }
    public static MeltingRecipeBuilder create(List<FluidStack> result, TagKey<Item> item) {
        return new MeltingRecipeBuilder(result, Ingredient.of(item));
    }
    public static MeltingRecipeBuilder create(List<FluidStack> result, List<ItemStack> items) {
        return new MeltingRecipeBuilder(result, items);
    }
    public static MeltingRecipeBuilder create(List<FluidStack> result, ItemStack item) {
        return new MeltingRecipeBuilder(result, new ArrayList<>(List.of(item)));
    }
    public static MeltingRecipeBuilder create(List<FluidStack> result, ItemLike item) {
        return new MeltingRecipeBuilder(result, new ArrayList<>(List.of(item.asItem().getDefaultInstance())));
    }
    public static MeltingRecipeBuilder create(List<FluidStack> result, Item item) {
        return new MeltingRecipeBuilder(result, new ArrayList<>(List.of(item.getDefaultInstance())));
    }

    public static MeltingRecipeBuilder create(Fluid result, int mb, ItemStack... item) {
        return new MeltingRecipeBuilder(result, mb, new ArrayList<>(Arrays.asList(item)));
    }
    public static MeltingRecipeBuilder create(FluidStack result, ItemStack... item) {
        return new MeltingRecipeBuilder(result, new ArrayList<>(Arrays.asList(item)));
    }

    public static MeltingRecipeBuilder create(Fluid result, int mb, TagKey<Item> tag) {
        return new MeltingRecipeBuilder(result, mb, Ingredient.of(tag));
    }
    public static MeltingRecipeBuilder create(FluidStack result, TagKey<Item> tag) {
        return new MeltingRecipeBuilder(result, Ingredient.of(tag));
    }

    public static MeltingRecipeBuilder create(Fluid result, int mb, ItemLike item) {
        return new MeltingRecipeBuilder(result, mb, Ingredient.of(item));
    }
    public static MeltingRecipeBuilder create(FluidStack result, ItemLike item) {
        return new MeltingRecipeBuilder(result, Ingredient.of(item));
    }

    public static void create(RegistrateRecipeProvider p, String name, Fluid fluid, int mb, TagKey<Item> tag) {
        MeltingRecipeBuilder.create(fluid, mb, tag)
                .unlockedBy("has_meltable_" + name, has(tag))
                .save(p, SlagEmbers.loc("melting/" + name));
    }
    public static void create(RegistrateRecipeProvider p, String name, FluidStack fluid, TagKey<Item> tag) {
        MeltingRecipeBuilder.create(fluid, tag)
                .unlockedBy("has_meltable_" + name, has(tag))
                .save(p, SlagEmbers.loc("melting/" + name));
    }
    public static void create(RegistrateRecipeProvider p, String name, Fluid fluid, int mb, ItemLike item) {
        MeltingRecipeBuilder.create(fluid, mb, item)
                .unlockedBy("has_meltable_" + name, has(item))
                .save(p, SlagEmbers.loc("melting/" + name));
    }
    public static void create(RegistrateRecipeProvider p, String name, FluidStack fluid, ItemLike item) {
        MeltingRecipeBuilder.create(fluid, item)
                .unlockedBy("has_meltable_" + name, has(item))
                .save(p, SlagEmbers.loc("melting/" + name));
    }
    public static void create(RegistrateRecipeProvider p, Fluid fluid, int mb, Item item) {
        var name = BuiltInRegistries.ITEM.getKey(item).getPath();
        MeltingRecipeBuilder.create(fluid, mb, item)
                .unlockedBy("has_meltable_" + name, has(item))
                .save(p, SlagEmbers.loc("melting/" + name));
    }
    public static void create(RegistrateRecipeProvider p, FluidStack fluid, Item item) {
        var name = BuiltInRegistries.ITEM.getKey(item).getPath();
        MeltingRecipeBuilder.create(fluid, item)
                .unlockedBy("has_meltable_" + name, has(item))
                .save(p, SlagEmbers.loc("melting/" + name));
    }
    public static void create(RegistrateRecipeProvider p, List<FluidStack> fluid, TagKey<Item> tag) {
        var name = tag.location().getPath();
        MeltingRecipeBuilder.create(fluid, tag)
                .unlockedBy("has_meltable_" + name, has(tag))
                .save(p, SlagEmbers.loc("melting/" + name));
    }
    public static void create(RegistrateRecipeProvider p, List<FluidStack> fluid, Item item) {
        var name = BuiltInRegistries.ITEM.getKey(item).getPath();
        MeltingRecipeBuilder.create(fluid, item)
                .unlockedBy("has_meltable_" + name, has(item))
                .save(p, SlagEmbers.loc("melting/" + name));
    }



    public static void oreMeltableGem(RegistrateRecipeProvider p, String name, Fluid fluid, @Nullable TagKey<Item> blocks, @Nullable TagKey<Item> material, @Nullable TagKey<Item> nuggets) {
        if (blocks != null) MeltingRecipeBuilder.create(fluid, MelterBE.BLOCK_SIZE + MelterBE.INGOT_SIZE + MelterBE.INGOT_SIZE, blocks)
                .unlockedBy("has_meltable_" + name, has(blocks))
                .save(p, SlagEmbers.loc("melting/" + name + "_blocks"));
        if (material != null) MeltingRecipeBuilder.create(fluid, MelterBE.INGOT_SIZE + MelterBE.SHARD_SIZE + MelterBE.SHARD_SIZE, material)
                .unlockedBy("has_meltable_" + name, has(material))
                .save(p, SlagEmbers.loc("melting/" + name + "_materials"));
        if (nuggets != null) MeltingRecipeBuilder.create(fluid, MelterBE.SHARD_SIZE, nuggets)
                .unlockedBy("has_meltable_" + name, has(nuggets))
                .save(p, SlagEmbers.loc("melting/" + name + "_nuggets"));
    }

    public static void oreMeltable(RegistrateRecipeProvider p, String name, Fluid fluid, @Nullable TagKey<Item> blocks, @Nullable TagKey<Item> material, @Nullable TagKey<Item> nuggets) {
        if (blocks != null) MeltingRecipeBuilder.create(fluid, MelterBE.BLOCK_SIZE + MelterBE.INGOT_SIZE + MelterBE.INGOT_SIZE + MelterBE.INGOT_SIZE, blocks)
                .unlockedBy("has_meltable_" + name, has(blocks))
                .save(p, SlagEmbers.loc("melting/" + name + "_blocks"));
        if (material != null) MeltingRecipeBuilder.create(fluid, MelterBE.INGOT_SIZE + MelterBE.NUGGET_SIZE + MelterBE.NUGGET_SIZE + MelterBE.NUGGET_SIZE, material)
                .unlockedBy("has_meltable_" + name, has(material))
                .save(p, SlagEmbers.loc("melting/" + name + "_materials"));
        if (nuggets != null) MeltingRecipeBuilder.create(fluid, MelterBE.NUGGET_SIZE, nuggets)
                .unlockedBy("has_meltable_" + name, has(nuggets))
                .save(p, SlagEmbers.loc("melting/" + name + "_nuggets"));
    }

    public static void ingotMeltable(RegistrateRecipeProvider p, String name, Fluid fluid, @Nullable TagKey<Item> blocks, @Nullable TagKey<Item> ingots, @Nullable TagKey<Item> nuggets) {
        if (blocks != null) MeltingRecipeBuilder.create(fluid, MelterBE.BLOCK_SIZE, blocks)
                .unlockedBy("has_meltable_" + name, has(blocks))
                .save(p, SlagEmbers.loc("melting/" + name + "_blocks"));
        if (ingots != null) MeltingRecipeBuilder.create(fluid, MelterBE.INGOT_SIZE, ingots)
                .unlockedBy("has_meltable_" + name, has(ingots))
                .save(p, SlagEmbers.loc("melting/" + name + "_ingots"));
        if (nuggets != null) MeltingRecipeBuilder.create(fluid, MelterBE.NUGGET_SIZE, nuggets)
                .unlockedBy("has_meltable_" + name, has(nuggets))
                .save(p, SlagEmbers.loc("melting/" + name + "_nuggets"));
    }
    public static void crystalMeltable(RegistrateRecipeProvider p, String name, Fluid fluid, @Nullable TagKey<Item> blocks, @Nullable TagKey<Item> crystals) {
        if (blocks != null) MeltingRecipeBuilder.create(fluid, MelterBE.SMALL_BLOCK_SIZE, blocks)
                .unlockedBy("has_meltable_" + name, has(blocks))
                .save(p, SlagEmbers.loc("melting/" + name + "_blocks"));
        if (crystals != null) MeltingRecipeBuilder.create(fluid, MelterBE.INGOT_SIZE, crystals)
                .unlockedBy("has_meltable_" + name, has(crystals))
                .save(p, SlagEmbers.loc("melting/" + name + "_crystals"));
    }
    public static void gemMeltable(RegistrateRecipeProvider p, String name, Fluid fluid, @Nullable TagKey<Item> blocks, @Nullable TagKey<Item> gems, @Nullable TagKey<Item> shards) {
        if (blocks != null) MeltingRecipeBuilder.create(fluid, MelterBE.BLOCK_SIZE, blocks)
                .unlockedBy("has_meltable_" + name, has(blocks))
                .save(p, SlagEmbers.loc("melting/" + name + "_blocks"));
        if (gems != null) MeltingRecipeBuilder.create(fluid, MelterBE.INGOT_SIZE, gems)
                .unlockedBy("has_meltable_" + name, has(gems))
                .save(p, SlagEmbers.loc("melting/" + name + "_gems"));
        if (shards != null) MeltingRecipeBuilder.create(fluid, MelterBE.SHARD_SIZE, shards)
                .unlockedBy("has_meltable_" + name, has(shards))
                .save(p, SlagEmbers.loc("melting/" + name + "_shards"));
    }
    public static void dustMeltable(RegistrateRecipeProvider p, String name, Fluid fluid, @Nullable TagKey<Item> blocks, @Nullable TagKey<Item> dusts) {
        if (blocks != null) MeltingRecipeBuilder.create(fluid, MelterBE.BLOCK_SIZE, blocks)
                .unlockedBy("has_meltable_" + name, has(blocks))
                .save(p, SlagEmbers.loc("melting/" + name + "_blocks"));
        if (dusts != null) MeltingRecipeBuilder.create(fluid, MelterBE.INGOT_SIZE, dusts)
                .unlockedBy("has_meltable_" + name, has(dusts))
                .save(p, SlagEmbers.loc("melting/" + name + "_dusts"));
    }

    public static void compatMeltable(RegistrateRecipeProvider p, String name, Fluid fluid, boolean raw) {
        compatMeltable(p, name + "_blocks", fluid, MelterBE.BLOCK_SIZE, AllTags.itemC("storage_blocks/" + name));
        compatMeltable(p, name + "_ingots", fluid, MelterBE.INGOT_SIZE, AllTags.itemC("ingots/" + name));
        compatMeltable(p, name + "_nuggets", fluid, MelterBE.NUGGET_SIZE, AllTags.itemC("nuggets/" + name));
        metalMeltable(p, name, fluid, raw);
        if (!raw) return;
        compatMeltable(p, "raw_" + name + "_blocks", fluid, MelterBE.BLOCK_SIZE + MelterBE.INGOT_SIZE * 3, AllTags.itemC("storage_blocks/raw_" + name));
        compatMeltable(p, "raw_" + name + "_materials", fluid, MelterBE.INGOT_SIZE + MelterBE.NUGGET_SIZE * 3, AllTags.itemC("raw_materials/" + name));
        compatMeltable(p, name + "_ores", fluid, MelterBE.INGOT_SIZE + MelterBE.NUGGET_SIZE * 3, AllTags.itemC("ores/" + name));
    }

    public static void formMeltable(RegistrateRecipeProvider p, String name, Fluid fluid) {
        compatMeltable(p, name + "_plates", fluid, MelterBE.INGOT_SIZE, AllTags.itemC("plates/" + name));
        compatMeltable(p, name + "_rods", fluid, MelterBE.INGOT_SIZE / 2, AllTags.itemC("rods/" + name));
        compatMeltable(p, name + "_dusts", fluid, MelterBE.INGOT_SIZE, AllTags.itemC("dusts/" + name));
    }

    public static void metalMeltable(RegistrateRecipeProvider p, String name, Fluid fluid, boolean ores) {
        formMeltable(p, name, fluid);
        compatMeltable(p, name + "_wires", fluid, MelterBE.INGOT_SIZE / 4, AllTags.itemC("wires/" + name));
        if (ores) compatMeltable(p, name + "_clumps", fluid, MelterBE.INGOT_SIZE + MelterBE.NUGGET_SIZE * 3, AllTags.itemC("clumps/" + name));
    }

    public static void compatMeltable(RegistrateRecipeProvider p, String name, Fluid fluid, int mb, TagKey<Item> tag) {
        MeltingRecipeBuilder.create(fluid, mb, tag)
                .unlockedBy("has_meltable_" + name, has(tag))
                .save(p.withConditions(AllTags.present(tag)), SlagEmbers.loc("melting/" + name));
    }

    public static FluidStack fluid(Fluid fluid, int mb) {
        return new FluidStack(fluid, mb);
    }
    public static List<FluidStack> fluids(FluidStack... fluids) {
        return new ArrayList<>(List.of(fluids));
    }
}
