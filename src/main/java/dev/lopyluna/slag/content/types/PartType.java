package dev.lopyluna.slag.content.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.traits.TraitEntry;
import dev.lopyluna.slag.content.traits.TraitType;
import dev.lopyluna.slag.register.AllTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class PartType {
    public final ResourceLocation id;
    public int sortOrder;

    public final List<TraitEntry> traits;
    public final Incompatible incompatible;
    public final List<ICondition> conditions;

    public final TagKey<Item> segmentPart;
    public final List<TagKey<Item>> itemTags;

    public boolean dontRegister;

    public static final Codec<PartType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(m -> m.id),
            Codec.INT.optionalFieldOf("sort_order", 0).forGetter(m -> m.sortOrder),
            TraitEntry.CODEC.listOf().optionalFieldOf("traits", List.of()).forGetter(m -> m.traits),
            Incompatible.CODEC.optionalFieldOf("incompatible", Incompatible.EMPTY).forGetter(m -> m.incompatible),
            ICondition.LIST_CODEC.optionalFieldOf("conditions", List.of()).forGetter(m -> m.conditions),
            TagKey.codec(Registries.ITEM).fieldOf("segment_part").forGetter(m -> m.segmentPart),
            TagKey.codec(Registries.ITEM).listOf().optionalFieldOf("item_tags", new ArrayList<>()).forGetter(m -> m.itemTags)
    ).apply(instance, PartType::new));

    @Override
    public int hashCode() {
        var segmentPartHash = 0;
        if (segmentPart != null) segmentPartHash = segmentPart.hashCode();
        return 31 * id.hashCode() + 31 * itemTagsHash() + 31 * segmentPartHash + Objects.hash(traits, incompatible, conditions, sortOrder);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PartType other)) return false;
        if (!other.traits.equals(traits)) return false;
        if (!other.incompatible.equals(incompatible)) return false;
        if (!other.conditions.equals(conditions)) return false;
        if (!other.segmentPart.equals(segmentPart)) return false;
        if (other.sortOrder != sortOrder) return false;
        if (!equalItemTags(other.itemTags)) return false;
        return other.id.equals(id);
    }

    public Component getName() {
        return Component.translatableWithFallback("part." + id.getNamespace() + "." + id.getPath(), RegistrateLangProvider.toEnglishName(id.getPath()));
    }

    private PartType(ResourceLocation id, int sortOrder, List<TraitEntry> traits, Incompatible incompatible, List<ICondition> conditions, TagKey<Item> segmentPart, List<TagKey<Item>> itemTags) {
        dontRegister = id == null || id.getNamespace().isEmpty() || id.getPath().isEmpty() || id.getPath().equals("null") || id.getPath().equals("empty");
        this.id = id;
        this.sortOrder = sortOrder;
        this.traits = traits;
        this.incompatible = incompatible;
        this.conditions = conditions;
        this.segmentPart = segmentPart;
        this.itemTags = itemTags;
    }

    public static class Builder {
        private final List<TraitEntry> traits = new ArrayList<>();
        private final List<ICondition> conditions = new ArrayList<>();
        private final Incompatible.Builder incompatible = new Incompatible.Builder();
        private final ResourceLocation id;
        private int sortOrder = 0;
        private TagKey<Item> segmentPart;
        private List<TagKey<Item>> itemTags = new ArrayList<>();

        public Builder(ResourceLocation id) {
            this.id = id;
        }
        public Builder(String id) {
            this.id = SlagEmbers.loc(id);
        }

        public Builder setSortOrder(int value) { sortOrder = value; return this; }

        public Builder trait(TraitEntry entry) { traits.add(entry); return this; }
        public Builder trait(TraitType trait) { return trait(TraitEntry.of(trait)); }
        public Builder trait(TraitType trait, float value) { return trait(TraitEntry.of(trait, value)); }
        public Builder multiply(TraitType trait, float value) { return trait(TraitEntry.multiply(trait, value)); }

        public Builder condition(ICondition... conditions) { this.conditions.addAll(List.of(conditions)); return this; }
        public Builder modLoaded(String modId) { return condition(new ModLoadedCondition(modId)); }
        public Builder requiresTag(TagKey<Item> tag) { return condition(AllTags.present(tag)); }
        public Builder incompatible(Consumer<Incompatible.Builder> builder) { builder.accept(incompatible); return this; }

        public Builder setSegmentPart(TagKey<Item> value) { segmentPart = value; return this; }

        public Builder itemTags(List<TagKey<Item>> itemTags) { this.itemTags = itemTags; return this; }
        @SafeVarargs public final Builder itemTags(TagKey<Item>... itemTags) { this.itemTags = List.of(itemTags); return this; }
        public Builder addItemTag(TagKey<Item> itemTag) { this.itemTags.add(itemTag); return this; }
        public Builder addItemTags(List<TagKey<Item>> itemTags) { this.itemTags.addAll(itemTags); return this; }
        @SafeVarargs public final Builder addItemTags(TagKey<Item>... itemTags) { this.itemTags.addAll(List.of(itemTags)); return this; }


        public PartType register() {
            return new PartType(id, sortOrder, List.copyOf(traits), incompatible.build(), List.copyOf(conditions), segmentPart, itemTags);
        }
    }


    public int itemTagsHash() {
        int hash = 0;
        if (itemTags != null && !itemTags.isEmpty()) {
            hash = itemTags.hashCode();
            for (var tag : itemTags) hash += tag.hashCode();
        }
        return hash;
    }

    public boolean equalItemTags(List<TagKey<Item>> other) {
        if (this.itemTags == null || other == null || (this.itemTags.isEmpty() != other.isEmpty())) return false;
        for (var tag : other) if (!itemTags.contains(tag)) return false;
        return itemTags.size() == other.size();
    }
}
