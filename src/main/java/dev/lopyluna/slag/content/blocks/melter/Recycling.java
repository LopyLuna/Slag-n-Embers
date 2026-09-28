package dev.lopyluna.slag.content.blocks.melter;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.api.RecyclingSources;
import dev.lopyluna.slag.config.SlagCommonConfigs;
import dev.lopyluna.slag.content.temperature.Temperatures.Tiers;
import dev.lopyluna.slag.mixin.SmithingTransformRecipeAccessor;
import dev.lopyluna.slag.register.AllTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings("deprecation")
public class Recycling {
    public static final String PATH = "slag/recycling.json";

    private static final int PASSES = 8;
    private static final Melt FREE = new Melt(Map.of(), Tiers.FROZEN, 0);
    private static final List<RecipeType<?>> TYPES = List.of(RecipeType.CRAFTING, RecipeType.SMELTING, RecipeType.BLASTING, RecipeType.STONECUTTING, RecipeType.SMITHING);

    private static volatile Filter pending = new Filter(Set.of(), Set.of(), Set.of());

    public static void prepare(ResourceManager manager) {
        pending = filter(manager);
    }

    public static List<RecipeHolder<?>> generate(Collection<RecipeHolder<?>> recipes, HolderLookup.Provider registries) {
        if (SlagCommonConfigs.SPEC.isLoaded() && !SlagCommonConfigs.RECYCLING.get()) return List.of();
        var filter = pending;
        var melts = new IdentityHashMap<Item, Melt>();
        var manual = Collections.<Item>newSetFromMap(new IdentityHashMap<>());
        var sources = new ArrayList<RecipeHolder<?>>();
        for (var holder : recipes) {
            var recipe = holder.value();
            if (recipe instanceof MeltingRecipe melting) index(melts, manual, melting);
            else if (usable(holder, filter)) sources.add(holder);
        }
        sources.sort(Comparator.comparing(holder -> holder.id().toString()));

        var free = new IdentityHashMap<Item, Boolean>();
        var candidates = new ArrayList<Candidate>();
        for (var holder : sources) collect(holder, registries, manual, filter, candidates);

        var worthless = Collections.newSetFromMap(new IdentityHashMap<Item, Boolean>());
        for (var candidate : candidates) if (worthless(candidate, free)) worthless.add(candidate.result);
        candidates.removeIf(candidate -> worthless.contains(candidate.result));

        var generated = new IdentityHashMap<Item, Melt>();
        for (var pass = 0; pass < PASSES; pass++) {
            var changed = false;
            for (var candidate : candidates) {
                var melt = evaluate(candidate, melts, free);
                if (melt == null) continue;
                var known = generated.get(candidate.result);
                if (known != null && known.total <= melt.total) continue;
                generated.put(candidate.result, melt);
                melts.put(candidate.result, melt);
                changed = true;
            }
            if (!changed) break;
        }

        var byResult = new IdentityHashMap<Item, List<Candidate>>();
        for (var candidate : candidates) byResult.computeIfAbsent(candidate.result, result -> new ArrayList<>()).add(candidate);
        for (var changed = true; changed; ) {
            changed = false;
            for (var entry : byResult.entrySet()) {
                var item = entry.getKey();
                var known = generated.get(item);
                if (known == null) continue;
                var best = merge(entry.getValue(), melts, free);
                if (best != null) {
                    if (best.total == known.total) continue;
                    generated.put(item, best);
                    melts.put(item, best);
                    changed = true;
                    continue;
                }
                generated.remove(item);
                melts.remove(item);
                changed = true;
            }
        }

        var list = new ArrayList<RecipeHolder<?>>();
        for (var entry : generated.entrySet()) if (!entry.getValue().fluids.isEmpty() && entry.getValue().total <= MelterBE.CAPACITY) list.add(holder(entry.getKey(), entry.getValue()));
        list.sort(Comparator.comparing(holder -> holder.id().toString()));
        if (!list.isEmpty()) SlagEmbers.LOGGER.info("Generated {} recycling recipes from crafting, cooking, stonecutting and smithing recipes", list.size());
        return list;
    }

    private static boolean usable(RecipeHolder<?> holder, Filter filter) {
        var recipe = holder.value();
        var vanilla = TYPES.contains(recipe.getType());
        if (vanilla ? recipe.isSpecial() : RecyclingSources.get(recipe.getType()) == null) return false;
        if (recipe.getType() == RecipeType.SMITHING && !(recipe instanceof SmithingTransformRecipe)) return false;
        return !filter.recipes.contains(holder.id()) && !filter.namespaces.contains(holder.id().getNamespace());
    }

    private static void index(Map<Item, Melt> melts, Set<Item> manual, MeltingRecipe recipe) {
        var outputs = recipe.getOutputs();
        if (outputs.isEmpty()) return;
        var fluids = new LinkedHashMap<Fluid, Integer>();
        var total = 0;
        for (var stack : outputs) {
            if (stack.isEmpty()) continue;
            fluids.merge(stack.getFluid(), stack.getAmount(), Integer::sum);
            total += stack.getAmount();
        }
        if (fluids.isEmpty()) return;
        var melt = new Melt(fluids, recipe.temperature, total);
        for (var stack : recipe.getInputs()) if (!stack.isEmpty()) mark(melts, manual, stack.getItem(), melt);
        for (var stack : recipe.getInput().getItems()) if (!stack.isEmpty()) mark(melts, manual, stack.getItem(), melt);
    }

    private static void mark(Map<Item, Melt> melts, Set<Item> manual, Item item, Melt melt) {
        manual.add(item);
        var known = melts.get(item);
        if (known == null || melt.total < known.total) melts.put(item, melt);
    }

    private static void collect(RecipeHolder<?> holder, HolderLookup.Provider registries, Set<Item> manual, Filter filter, List<Candidate> out) {
        var recipe = holder.value();
        var adapter = RecyclingSources.get(recipe.getType());
        if (adapter == null) {
            var candidate = candidate(recipe.getResultItem(registries), inputs(recipe), manual, filter);
            if (candidate != null) out.add(candidate);
            return;
        }
        for (var source : adapter.convert(recipe, registries)) {
            var candidate = candidate(source.result(), source.inputs(), manual, filter);
            if (candidate != null) out.add(candidate);
        }
    }

    private static List<Ingredient> inputs(Recipe<?> recipe) {
        if (!(recipe instanceof SmithingTransformRecipe smithing)) return recipe.getIngredients();
        var accessor = (SmithingTransformRecipeAccessor) smithing;
        return List.of(accessor.slag$getBase(), accessor.slag$getAddition());
    }

    private static @Nullable Candidate candidate(ItemStack result, List<Ingredient> ingredients, Set<Item> manual, Filter filter) {
        if (result.isEmpty() || result.getCount() <= 0) return null;
        var item = result.getItem();
        if (manual.contains(item) || excluded(item, filter)) return null;
        var inputs = new ArrayList<Item[]>();
        for (var ingredient : ingredients) {
            if (ingredient.isEmpty()) continue;
            var items = items(ingredient);
            if (items.length == 0) return null;
            for (var option : items) if (option == item) return null;
            inputs.add(items);
        }
        return inputs.isEmpty() ? null : new Candidate(item, result.getCount(), inputs);
    }

    private static boolean excluded(Item item, Filter filter) {
        if (item.builtInRegistryHolder().is(AllTags.RECYCLING_BLACKLIST)) return true;
        return filter.items.contains(BuiltInRegistries.ITEM.getKey(item).getNamespace());
    }

    private static Item[] items(Ingredient ingredient) {
        var stacks = ingredient.getItems();
        var items = new Item[stacks.length];
        var size = 0;
        for (var stack : stacks) if (!stack.isEmpty()) items[size++] = stack.getItem();
        return size == stacks.length ? items : Arrays.copyOf(items, size);
    }

    private static @Nullable Melt evaluate(Candidate candidate, Map<Item, Melt> melts, Map<Item, Boolean> free) {
        var fluids = new LinkedHashMap<Fluid, Integer>();
        var tier = Tiers.FROZEN;
        var used = false;
        for (var options : candidate.inputs) {
            var melt = cheapest(options, melts, free);
            if (melt == null) return null;
            if (melt.fluids.isEmpty()) continue;
            used = true;
            for (var entry : melt.fluids.entrySet()) fluids.merge(entry.getKey(), entry.getValue(), Integer::sum);
            if (melt.tier.ordinal() > tier.ordinal()) tier = melt.tier;
        }
        if (!used) return null;
        var scaled = new LinkedHashMap<Fluid, Integer>();
        var total = 0;
        for (var entry : fluids.entrySet()) {
            var amount = entry.getValue() / candidate.count;
            if (amount <= 0) continue;
            scaled.put(entry.getKey(), amount);
            total += amount;
        }
        if (scaled.isEmpty()) return null;
        return new Melt(scaled, tier, total);
    }

    private static @Nullable Melt merge(List<Candidate> candidates, Map<Item, Melt> melts, Map<Item, Boolean> free) {
        var best = (Melt) null;
        for (var candidate : candidates) {
            var melt = evaluate(candidate, melts, free);
            if (melt == null) return null;
            if (best == null) {
                best = melt;
                continue;
            }
            if (!best.fluids.keySet().equals(melt.fluids.keySet())) return null;
            var fluids = new LinkedHashMap<Fluid, Integer>();
            var total = 0;
            for (var entry : best.fluids.entrySet()) {
                var amount = Math.min(entry.getValue(), melt.fluids.get(entry.getKey()));
                fluids.put(entry.getKey(), amount);
                total += amount;
            }
            best = new Melt(fluids, best.tier.ordinal() > melt.tier.ordinal() ? best.tier : melt.tier, total);
        }
        return best;
    }

    private static boolean worthless(Candidate candidate, Map<Item, Boolean> free) {
        for (var options : candidate.inputs) {
            var any = false;
            for (var option : options) if (free.computeIfAbsent(option, Recycling::free)) {
                any = true;
                break;
            }
            if (!any) return false;
        }
        return true;
    }

    private static @Nullable Melt cheapest(Item[] options, Map<Item, Melt> melts, Map<Item, Boolean> free) {
        var best = (Melt) null;
        for (var option : options) {
            var melt = melts.get(option);
            if (melt == null) {
                if (!free.computeIfAbsent(option, Recycling::free)) return null;
                melt = FREE;
            }
            if (best == null || melt.total < best.total) best = melt;
        }
        return best;
    }

    private static boolean free(Item item) {
        if (item.builtInRegistryHolder().is(AllTags.RECYCLING_FREE)) return true;
        return ComposterBlock.COMPOSTABLES.containsKey(item) || item.getDefaultInstance().getBurnTime(RecipeType.SMELTING) > 0;
    }

    private static RecipeHolder<?> holder(Item item, Melt melt) {
        var outputs = new ArrayList<FluidStack>();
        melt.fluids.entrySet().stream()
                .sorted(Comparator.comparing(entry -> BuiltInRegistries.FLUID.getKey(entry.getKey()).toString()))
                .forEach(entry -> outputs.add(new FluidStack(entry.getKey(), entry.getValue())));
        var id = BuiltInRegistries.ITEM.getKey(item);
        var recipe = new MeltingRecipe("", List.of(), Ingredient.of(item), outputs, melt.tier, Optional.empty(), 0, 1f);
        return new RecipeHolder<>(SlagEmbers.loc("recycling/generated/" + id.getNamespace() + "/" + id.getPath()), recipe);
    }

    private static Filter filter(ResourceManager manager) {
        var filter = new Filter(new HashSet<>(), new HashSet<>(), new HashSet<>());
        for (var namespace : manager.getNamespaces()) for (var resource : manager.getResourceStack(ResourceLocation.fromNamespaceAndPath(namespace, PATH))) {
            try (var reader = resource.openAsReader()) {
                var root = JsonParser.parseReader(reader).getAsJsonObject();
                strings(root.get("namespaces"), filter.namespaces::add);
                strings(root.get("item_namespaces"), filter.items::add);
                strings(root.get("recipes"), id -> {
                    var parsed = ResourceLocation.tryParse(id);
                    if (parsed == null) SlagEmbers.LOGGER.error("{} isn't a valid recipe id! is it a typo or something?", id);
                    else filter.recipes.add(parsed);
                });
            } catch (Exception e) {
                SlagEmbers.LOGGER.error("Couldn't read {}:{} from {}", namespace, PATH, resource.sourcePackId(), e);
            }
        }
        return filter;
    }

    private static void strings(@Nullable JsonElement element, Consumer<String> consumer) {
        if (element == null || !element.isJsonArray()) return;
        for (var entry : element.getAsJsonArray()) if (entry.isJsonPrimitive()) consumer.accept(entry.getAsString());
    }

    private record Candidate(Item result, int count, List<Item[]> inputs) {}

    private record Melt(Map<Fluid, Integer> fluids, Tiers tier, int total) {}

    private record Filter(Set<String> namespaces, Set<String> items, Set<ResourceLocation> recipes) {}
}
