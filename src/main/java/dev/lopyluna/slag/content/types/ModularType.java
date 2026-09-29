package dev.lopyluna.slag.content.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.items.dynamic_part.IDynamicPart;
import dev.lopyluna.slag.content.traits.TraitEntry;
import dev.lopyluna.slag.content.traits.TraitType;
import dev.lopyluna.slag.register.AllDataComponents;
import dev.lopyluna.slag.register.AllTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class ModularType {
    public final ResourceLocation id;

    public final String modelType;
    public final int sortOrder;
    public final List<TagKey<Item>> segments;
    public final List<ItemStack> finalSegmentStacks;
    private final ItemStack resultStack;
    public final List<TraitEntry> traits;
    public final Incompatible incompatible;
    public final List<ICondition> conditions;

    public final List<TagKey<Item>> itemTags;
    public final Optional<ResourceLocation> betterCombatPreset; // better combat animation preset

    public boolean dontRegister;

    public static final Codec<ModularType> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(m -> m.id),
                    Codec.STRING.optionalFieldOf("model_type", "").forGetter(m -> m.modelType),
                    Codec.INT.fieldOf("sort_order").forGetter(m -> m.sortOrder),
                    TagKey.codec(Registries.ITEM).listOf().optionalFieldOf("segments", new ArrayList<>()).forGetter(m -> m.segments),
                    ItemStack.CODEC.listOf().optionalFieldOf("final_segment_stacks", new ArrayList<>()).forGetter(m -> m.finalSegmentStacks),
                    ItemStack.CODEC.optionalFieldOf("result_stack", ItemStack.EMPTY).forGetter(m -> m.resultStack),
                    TraitEntry.CODEC.listOf().optionalFieldOf("traits", List.of()).forGetter(m -> m.traits),
                    Incompatible.CODEC.optionalFieldOf("incompatible", Incompatible.EMPTY).forGetter(m -> m.incompatible),
                    ICondition.LIST_CODEC.optionalFieldOf("conditions", List.of()).forGetter(m -> m.conditions),
                    TagKey.codec(Registries.ITEM).listOf().optionalFieldOf("item_tags", new ArrayList<>()).forGetter(m -> m.itemTags),
                    ResourceLocation.CODEC.optionalFieldOf("betterCombatPreset").forGetter(m -> m.betterCombatPreset)
            ).apply(instance, ModularType::new)
    );

    @Override
    public int hashCode() {
        var traitHash = traits.hashCode() + incompatible.hashCode() + conditions.hashCode();
        var resultStackHash = 0;
        if (resultStack != null) resultStackHash = resultStack.copy().hashCode();
        return 31 * id.hashCode() + 31 * traitHash + 31 * segmentHash() + 31 * finalSegmentStacksHash() + 31 * itemTagsHash() + 31 * resultStackHash + Objects.hash(modelType, sortOrder);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ModularType) obj;
        return this.id.equals(that.id) && this.modelType.equals(that.modelType) && this.sortOrder == that.sortOrder && equalSegment(that.segments) && equalFinalSegmentStacks(that.finalSegmentStacks) && equalItemTags(that.itemTags) && ItemStack.isSameItemSameComponents(this.resultStack, that.resultStack) && this.traits.equals(that.traits) && this.incompatible.equals(that.incompatible) && this.conditions.equals(that.conditions);
    }

    public Component getName() {
        return name(id);
    }

    public static Component name(ResourceLocation id) {
        return Component.translatableWithFallback("modular." + id.getNamespace() + "." + id.getPath(), RegistrateLangProvider.toEnglishName(id.getPath()));
    }

    public ItemStack getResultStack() {
        return resultStack.copy();
    }

    private ModularType(ResourceLocation id, String modelType, int sortOrder, List<TagKey<Item>> segments, List<ItemStack> finalSegmentStacks, ItemStack resultStack, List<TraitEntry> traits, Incompatible incompatible, List<ICondition> conditions, List<TagKey<Item>> itemTags, Optional<ResourceLocation> betterCombatPreset) {
        dontRegister = id == null || id.getNamespace().isEmpty() || id.getPath().isEmpty() || id.getPath().equals("null") || id.getPath().equals("empty");
        this.id = id;
        this.modelType = modelType;
        this.sortOrder = sortOrder;
        this.segments = segments;
        this.finalSegmentStacks = finalSegmentStacks;
        this.resultStack = resultStack;
        this.traits = traits;
        this.incompatible = incompatible;
        this.conditions = conditions;
        this.itemTags = itemTags;
        this.betterCombatPreset = betterCombatPreset;
    }

    public boolean hasTrait(TraitType trait) {
        for (var entry : traits) if (entry.trait().equals(trait.id)) return true;
        return false;
    }

    @SuppressWarnings("unused")
    public static class Builder {
        private final ResourceLocation id;
        private String modelType = "";
        private int sortOrder;
        private List<TagKey<Item>> segments = new ArrayList<>();
        private List<ItemStack> finalSegmentStacks = new ArrayList<>();
        private ItemStack resultStack;
        private final List<TraitEntry> traits = new ArrayList<>();
        private final List<ICondition> conditions = new ArrayList<>();
        private final Incompatible.Builder incompatible = new Incompatible.Builder();
        private List<TagKey<Item>> itemTags = new ArrayList<>();
        private Optional<ResourceLocation> betterCombatPreset = Optional.empty();

        public Builder(ResourceLocation id) { this.id = id; }
        public Builder(String id) { this.id = SlagEmbers.loc(id); }

        public Builder modelType(String modelType) { this.modelType = modelType; return this; }

        public Builder sortOrder(int sorting) { this.sortOrder = sorting; return this; }

        public Builder segments(List<TagKey<Item>> segments) { this.segments = segments; return this; }
        @SafeVarargs public final Builder segments(TagKey<Item>... segments) { this.segments = List.of(segments); return this; }
        public Builder addSegment(TagKey<Item> segment) { this.segments.add(segment); return this; }
        public Builder addSegments(List<TagKey<Item>> segments) { this.segments.addAll(segments); return this; }
        @SafeVarargs public final Builder addSegments(TagKey<Item>... segments) { this.segments.addAll(List.of(segments)); return this; }


        public Builder rodCount(int rodCount) {
            return addSegmentStack(Items.STICK, rodCount);
        }

        public Builder segmentStacks(List<ItemStack> finalSegmentStacks) { this.finalSegmentStacks = finalSegmentStacks; return this; }
        public Builder segmentStacksFromItems(List<Item> finalSegmentStacks) { this.finalSegmentStacks = finalSegmentStacks.stream().map(ItemStack::new).toList(); return this; }
        public Builder segmentStacksFromItems(int count, List<Item> finalSegmentStacks) { this.finalSegmentStacks = finalSegmentStacks.stream().map(item -> new ItemStack(item, count)).toList(); return this; }

        public final Builder segmentStacks(ItemStack... finalSegmentStacks) { this.finalSegmentStacks = List.of(finalSegmentStacks); return this; }
        public final Builder segmentStacksFromItems(Item... finalSegmentStacks) { this.finalSegmentStacks = Stream.of(finalSegmentStacks).map(ItemStack::new).toList(); return this; }
        public final Builder segmentStacksFromItems(int count, Item... finalSegmentStacks) { this.finalSegmentStacks = Stream.of(finalSegmentStacks).map(item -> new ItemStack(item, count)).toList(); return this; }

        public Builder addSegmentStack(ItemStack finalSegmentStack) { this.finalSegmentStacks.add(finalSegmentStack); return this; }
        public Builder addSegmentStack(Item finalSegmentStack) { this.finalSegmentStacks.add(new ItemStack(finalSegmentStack)); return this; }
        public Builder addSegmentStack(Item finalSegmentStack, int count) { this.finalSegmentStacks.add(new ItemStack(finalSegmentStack, count)); return this; }

        public Builder addSegmentStacks(List<ItemStack> finalSegmentStacks) { this.finalSegmentStacks.addAll(finalSegmentStacks); return this; }
        public Builder addSegmentStacksFromItems(List<Item> finalSegmentStacks) { this.finalSegmentStacks.addAll(finalSegmentStacks.stream().map(ItemStack::new).toList()); return this; }
        public Builder addSegmentStacksFromItems(int count, List<Item> finalSegmentStacks) { this.finalSegmentStacks.addAll(finalSegmentStacks.stream().map(item -> new ItemStack(item, count)).toList()); return this; }

        public final Builder addSegmentStacks(ItemStack... finalSegmentStacks) { this.finalSegmentStacks.addAll(List.of(finalSegmentStacks)); return this; }
        public final Builder addSegmentStacksFromItems(Item... finalSegmentStacks) { this.finalSegmentStacks.addAll(Stream.of(finalSegmentStacks).map(ItemStack::new).toList()); return this; }
        public final Builder addSegmentStacksFromItems(int count, Item... finalSegmentStacks) { this.finalSegmentStacks.addAll(Stream.of(finalSegmentStacks).map(item -> new ItemStack(item, count)).toList()); return this; }

        public Builder resultStack(ItemStack resultStack) { this.resultStack = resultStack; return this; }
        public Builder resultStack(Item resultStack) { this.resultStack = new ItemStack(resultStack); return this; }
        public Builder resultStack(Item resultStack, int count) { this.resultStack = new ItemStack(resultStack, count); return this; }

        public Builder trait(TraitEntry entry) { traits.add(entry); return this; }
        public Builder trait(TraitType trait) { return trait(TraitEntry.of(trait)); }
        public Builder trait(TraitType trait, float value) { return trait(TraitEntry.of(trait, value)); }
        public Builder multiply(TraitType trait, float value) { return trait(TraitEntry.multiply(trait, value)); }
        public Builder traits(TraitType... traits) { for (var trait : traits) trait(trait); return this; }

        public Builder condition(ICondition... conditions) { this.conditions.addAll(List.of(conditions)); return this; }
        public Builder modLoaded(String modId) { return condition(new ModLoadedCondition(modId)); }
        public Builder requiresTag(TagKey<Item> tag) { return condition(AllTags.present(tag)); }
        public Builder incompatible(Consumer<Incompatible.Builder> builder) { builder.accept(incompatible); return this; }

        public Builder itemTags(List<TagKey<Item>> itemTags) { this.itemTags = itemTags; return this; }
        @SafeVarargs public final Builder itemTags(TagKey<Item>... itemTags) { this.itemTags = List.of(itemTags); return this; }
        public Builder addItemTag(TagKey<Item> itemTag) { this.itemTags.add(itemTag); return this; }
        public Builder addItemTags(List<TagKey<Item>> itemTags) { this.itemTags.addAll(itemTags); return this; }
        @SafeVarargs public final Builder addItemTags(TagKey<Item>... itemTags) { this.itemTags.addAll(List.of(itemTags)); return this; }
        public Builder betterCombatPreset(ResourceLocation id) { betterCombatPreset = Optional.of(id); return this; }
        public Builder betterCombatPreset(String id) { betterCombatPreset(ResourceLocation.read(id).getOrThrow()); return this; }

        public ModularType register() {
            return new ModularType(id, modelType, sortOrder, segments, finalSegmentStacks, resultStack == null ? ItemStack.EMPTY : resultStack, List.copyOf(traits), incompatible.build(), List.copyOf(conditions), itemTags, betterCombatPreset);
        }
    }

    public int segmentHash() {
        int hash = 0;
        if (segments != null && !segments.isEmpty()) {
            hash = segments.hashCode();
            for (var segment : segments) hash += segment.hashCode();
        }
        return hash;
    }

    public int finalSegmentStacksHash() {
        int hash = 0;
        if (finalSegmentStacks != null && !finalSegmentStacks.isEmpty()) {
            hash = finalSegmentStacks.hashCode();
            for (var stack : finalSegmentStacks) hash += stack.hashCode();
        }
        return hash;
    }

    public int itemTagsHash() {
        int hash = 0;
        if (itemTags != null && !itemTags.isEmpty()) {
            hash = itemTags.hashCode();
            for (var tag : itemTags) hash += tag.hashCode();
        }
        return hash;
    }

    public boolean equalSegment(List<TagKey<Item>> other) {
        if (this.segments == null || other == null || (this.segments.isEmpty() != other.isEmpty())) return false;
        for (var segment : other) if (!segments.contains(segment)) return false;
        return segments.size() == other.size();
    }

    public boolean equalFinalSegmentStacks(List<ItemStack> other) {
        if (this.finalSegmentStacks == null || other == null || (this.finalSegmentStacks.isEmpty() != other.isEmpty())) return false;
        for (var stack : other) if (!containsStack(stack, true)) return false;
        return finalSegmentStacks.size() == other.size();
    }

    public boolean equalItemTags(List<TagKey<Item>> other) {
        if (this.itemTags == null || other == null || (this.itemTags.isEmpty() != other.isEmpty())) return false;
        for (var tag : other) if (!itemTags.contains(tag)) return false;
        return itemTags.size() == other.size();
    }

    public boolean containsStack(ItemStack stack, boolean built) {
        if (stack == null || stack.isEmpty()) return false;
        var newStack = stack.copy();
        if (!built) newStack.remove(AllDataComponents.BUILT);
        for (var finalStack : finalSegmentStacks) if (ItemStack.isSameItemSameComponents(newStack, finalStack) && newStack.getCount() == finalStack.getCount()) return true;
        return false;
    }

    public boolean containsTag(ItemStack stack) {
        if (!(stack.getItem() instanceof IDynamicPart part)) return false;
        var partSegment = part.getPartSegment(stack);
        for (var segment : segments) if (partSegment.location().equals(segment.location())) return true;
        return false;
    }

    public boolean contains(ItemStack stack, boolean built) {
        if (containsTag(stack)) return true;
        return !(stack.getItem() instanceof IDynamicPart) && containsStack(stack, built);
    }

    public boolean containsItemTag(TagKey<Item> tag) {
        for (var itemTag : itemTags) if (itemTag.equals(tag) || itemTag.location().equals(tag.location())) return true;
        return false;
    }
}
