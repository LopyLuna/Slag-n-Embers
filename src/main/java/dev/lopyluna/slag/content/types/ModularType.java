package dev.lopyluna.slag.content.types;

import com.google.common.base.Suppliers;
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
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

@SuppressWarnings({"unused", "OptionalUsedAsFieldOrParameterType"})
public class ModularType {
    public final Identifier id;

    public final String modelType;
    public final int sortOrder;
    public final List<TagKey<Item>> segments;
    public final List<ItemStackTemplate> finalSegments;
    private final Optional<ItemStackTemplate> result;
    private final Supplier<List<ItemStack>> finalSegmentStacks;
    public final List<TraitEntry> traits;
    public final Incompatible incompatible;
    public final List<ICondition> conditions;

    public final List<TagKey<Item>> itemTags;

    public boolean dontRegister;

    public static final Codec<ModularType> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("id").forGetter(m -> m.id),
                    Codec.STRING.optionalFieldOf("model_type", "").forGetter(m -> m.modelType),
                    Codec.INT.fieldOf("sort_order").forGetter(m -> m.sortOrder),
                    TagKey.codec(Registries.ITEM).listOf().optionalFieldOf("segments", new ArrayList<>()).forGetter(m -> m.segments),
                    ItemStackTemplate.CODEC.listOf().optionalFieldOf("final_segment_stacks", List.of()).forGetter(m -> m.finalSegments),
                    ItemStackTemplate.CODEC.optionalFieldOf("result_stack").forGetter(m -> m.result),
                    TraitEntry.CODEC.listOf().optionalFieldOf("traits", List.of()).forGetter(m -> m.traits),
                    Incompatible.CODEC.optionalFieldOf("incompatible", Incompatible.EMPTY).forGetter(m -> m.incompatible),
                    ICondition.LIST_CODEC.optionalFieldOf(ConditionalOps.DEFAULT_CONDITIONS_KEY, List.of()).forGetter(m -> m.conditions),
                    TagKey.codec(Registries.ITEM).listOf().optionalFieldOf("item_tags", new ArrayList<>()).forGetter(m -> m.itemTags)
            ).apply(instance, ModularType::new)
    );

    @Override
    public int hashCode() {
        var traitHash = traits.hashCode() + incompatible.hashCode() + conditions.hashCode();
        return 31 * id.hashCode() + 31 * traitHash + 31 * segmentHash() + 31 * finalSegmentStacksHash() + 31 * itemTagsHash() + 31 * result.hashCode() + Objects.hash(modelType, sortOrder);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ModularType) obj;
        return this.id.equals(that.id) && this.modelType.equals(that.modelType) && this.sortOrder == that.sortOrder && equalSegment(that.segments) && equalFinalSegmentStacks(that.finalSegments) && equalItemTags(that.itemTags) && this.result.equals(that.result) && this.traits.equals(that.traits) && this.incompatible.equals(that.incompatible) && this.conditions.equals(that.conditions);
    }

    public Component getName() {
        return name(id);
    }

    public static Component name(Identifier id) {
        return Component.translatableWithFallback("modular." + id.getNamespace() + "." + id.getPath(), RegistrateLangProvider.toEnglishName(id.getPath()));
    }

    public ItemStack getResultStack() {
        return result.map(ItemStackTemplate::create).orElse(ItemStack.EMPTY);
    }

    public List<ItemStack> finalSegmentStacks() {
        return finalSegmentStacks.get();
    }

    private ModularType(Identifier id, String modelType, int sortOrder, List<TagKey<Item>> segments, List<ItemStackTemplate> finalSegments, Optional<ItemStackTemplate> result, List<TraitEntry> traits, Incompatible incompatible, List<ICondition> conditions, List<TagKey<Item>> itemTags) {
        dontRegister = id == null || id.getNamespace().isEmpty() || id.getPath().isEmpty() || id.getPath().equals("null") || id.getPath().equals("empty");
        this.id = id;
        this.modelType = modelType;
        this.sortOrder = sortOrder;
        this.segments = segments;
        this.finalSegments = finalSegments;
        this.result = result;
        finalSegmentStacks = Suppliers.memoize(() -> finalSegments.stream().map(ItemStackTemplate::create).toList());
        this.traits = traits;
        this.incompatible = incompatible;
        this.conditions = conditions;
        this.itemTags = itemTags;
    }

    public boolean hasTrait(TraitType trait) {
        for (var entry : traits) if (entry.trait().equals(trait.id)) return true;
        return false;
    }

    @SuppressWarnings("unused")
    public static class Builder {
        private final Identifier id;
        private String modelType = "";
        private int sortOrder;
        private List<TagKey<Item>> segments = new ArrayList<>();
        private List<ItemStackTemplate> finalSegments = new ArrayList<>();
        private ItemStackTemplate result;
        private final List<TraitEntry> traits = new ArrayList<>();
        private final List<ICondition> conditions = new ArrayList<>();
        private final Incompatible.Builder incompatible = new Incompatible.Builder();
        private List<TagKey<Item>> itemTags = new ArrayList<>();

        public Builder(Identifier id) { this.id = id; }
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

        public Builder segmentStacks(List<ItemStackTemplate> finalSegments) { this.finalSegments = new ArrayList<>(finalSegments); return this; }
        public Builder segmentStacksFromItems(List<Item> finalSegments) { this.finalSegments = new ArrayList<>(finalSegments.stream().map(ItemStackTemplate::new).toList()); return this; }
        public Builder segmentStacksFromItems(int count, List<Item> finalSegments) { this.finalSegments = new ArrayList<>(finalSegments.stream().map(item -> new ItemStackTemplate(item, count)).toList()); return this; }

        public final Builder segmentStacks(ItemStackTemplate... finalSegments) { this.finalSegments = new ArrayList<>(List.of(finalSegments)); return this; }
        public final Builder segmentStacksFromItems(Item... finalSegments) { this.finalSegments = new ArrayList<>(Stream.of(finalSegments).map(ItemStackTemplate::new).toList()); return this; }
        public final Builder segmentStacksFromItems(int count, Item... finalSegments) { this.finalSegments = new ArrayList<>(Stream.of(finalSegments).map(item -> new ItemStackTemplate(item, count)).toList()); return this; }

        public Builder addSegmentStack(ItemStackTemplate finalSegment) { this.finalSegments.add(finalSegment); return this; }
        public Builder addSegmentStack(Item finalSegment) { this.finalSegments.add(new ItemStackTemplate(finalSegment)); return this; }
        public Builder addSegmentStack(Item finalSegment, int count) { this.finalSegments.add(new ItemStackTemplate(finalSegment, count)); return this; }

        public Builder addSegmentStacks(List<ItemStackTemplate> finalSegments) { this.finalSegments.addAll(finalSegments); return this; }
        public Builder addSegmentStacksFromItems(List<Item> finalSegments) { this.finalSegments.addAll(finalSegments.stream().map(ItemStackTemplate::new).toList()); return this; }
        public Builder addSegmentStacksFromItems(int count, List<Item> finalSegments) { this.finalSegments.addAll(finalSegments.stream().map(item -> new ItemStackTemplate(item, count)).toList()); return this; }

        public final Builder addSegmentStacks(ItemStackTemplate... finalSegments) { this.finalSegments.addAll(List.of(finalSegments)); return this; }
        public final Builder addSegmentStacksFromItems(Item... finalSegments) { this.finalSegments.addAll(Stream.of(finalSegments).map(ItemStackTemplate::new).toList()); return this; }
        public final Builder addSegmentStacksFromItems(int count, Item... finalSegments) { this.finalSegments.addAll(Stream.of(finalSegments).map(item -> new ItemStackTemplate(item, count)).toList()); return this; }

        public Builder resultStack(ItemStackTemplate result) { this.result = result; return this; }
        public Builder resultStack(Item result) { this.result = new ItemStackTemplate(result); return this; }
        public Builder resultStack(Item result, int count) { this.result = new ItemStackTemplate(result, count); return this; }

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

        public ModularType register() {
            return new ModularType(id, modelType, sortOrder, segments, List.copyOf(finalSegments), Optional.ofNullable(result), List.copyOf(traits), incompatible.build(), List.copyOf(conditions), itemTags);
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
        if (finalSegments != null && !finalSegments.isEmpty()) {
            hash = finalSegments.hashCode();
            for (var stack : finalSegments) hash += stack.hashCode();
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

    public boolean equalFinalSegmentStacks(List<ItemStackTemplate> other) {
        if (this.finalSegments == null || other == null || (this.finalSegments.isEmpty() != other.isEmpty())) return false;
        return finalSegments.size() == other.size() && new HashSet<>(finalSegments).containsAll(other);
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
        for (var finalStack : finalSegmentStacks()) if (ItemStack.isSameItemSameComponents(newStack, finalStack) && newStack.getCount() == finalStack.getCount()) return true;
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
