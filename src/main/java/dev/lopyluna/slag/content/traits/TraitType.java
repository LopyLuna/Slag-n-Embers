package dev.lopyluna.slag.content.traits;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.register.AllTags;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
public class TraitType {
    public static final TextColor GRAY = TextColor.fromLegacyFormat(ChatFormatting.GRAY);
    public static final Codec<TextColor> COLOR_CODEC = Codec.STRING.comapFlatMap(TraitType::parseColor, TextColor::serialize);

    public final ResourceLocation id;
    public final int sortOrder;
    public final boolean hidden;
    public final float value;
    public final TraitOperation operation;
    public final float partScale;
    public final TextColor color;
    public final List<TraitEffect> effects;
    public final List<ICondition> conditions;

    public boolean dontRegister;

    public static final Codec<TraitType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(t -> t.id),
            Codec.INT.optionalFieldOf("sort_order", 0).forGetter(t -> t.sortOrder),
            Codec.BOOL.optionalFieldOf("hidden", false).forGetter(t -> t.hidden),
            Codec.FLOAT.optionalFieldOf("value", 0f).forGetter(t -> t.value),
            TraitOperation.CODEC.optionalFieldOf("operation", TraitOperation.ADD).forGetter(t -> t.operation),
            Codec.FLOAT.optionalFieldOf("part_scale", 0f).forGetter(t -> t.partScale),
            COLOR_CODEC.optionalFieldOf("color", GRAY).forGetter(t -> t.color),
            TraitEffect.CODEC.listOf().optionalFieldOf("effects", List.of()).forGetter(t -> t.effects),
            ICondition.LIST_CODEC.optionalFieldOf("conditions", List.of()).forGetter(t -> t.conditions)
    ).apply(instance, TraitType::new));

    public TraitType(ResourceLocation id, int sortOrder, boolean hidden, float value, TraitOperation operation, float partScale, TextColor color, List<TraitEffect> effects, List<ICondition> conditions) {
        dontRegister = id == null || id.getNamespace().isEmpty() || id.getPath().isEmpty() || id.getPath().equals("null") || id.getPath().equals("empty");
        this.id = id;
        this.sortOrder = sortOrder;
        this.hidden = hidden;
        this.value = value;
        this.operation = operation;
        this.partScale = partScale;
        this.color = color;
        this.effects = effects;
        this.conditions = conditions;
    }

    public static DataResult<TextColor> parseColor(String input) {
        var hex = input.startsWith("#") ? input.substring(1) : input.regionMatches(true, 0, "0x", 0, 2) ? input.substring(2) : input;
        if (!hex.isEmpty() && hex.length() <= 6 && hex.chars().allMatch(c -> Character.digit(c, 16) >= 0)) return DataResult.success(TextColor.fromRgb(Integer.parseInt(hex, 16)));
        return TextColor.parseColor(input);
    }

    public MutableComponent name() {
        return Component.translatableWithFallback("trait." + id.getNamespace() + "." + id.getPath(), RegistrateLangProvider.toEnglishName(id.getPath()));
    }

    @Override
    public int hashCode() {
        return 31 * id.hashCode() + effects.hashCode() + conditions.hashCode() + Objects.hash(sortOrder, hidden, value, operation, partScale, color);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof TraitType other)) return false;
        return id.equals(other.id) && sortOrder == other.sortOrder && hidden == other.hidden && value == other.value && operation == other.operation && partScale == other.partScale && color.equals(other.color) && effects.equals(other.effects) && conditions.equals(other.conditions);
    }

    public static class Builder {
        private final ResourceLocation id;
        private int sortOrder;
        private boolean hidden;
        private float value;
        private TraitOperation operation = TraitOperation.ADD;
        private float partScale;
        private TextColor color = GRAY;
        private final List<TraitEffect> effects = new ArrayList<>();
        private final List<ICondition> conditions = new ArrayList<>();

        public Builder(ResourceLocation id) { this.id = id; }
        public Builder(String id) { this.id = SlagEmbers.loc(id); }

        public Builder sortOrder(int sortOrder) { this.sortOrder = sortOrder; return this; }
        public Builder hidden() { hidden = true; return this; }
        public Builder value(float value) { this.value = value; return this; }
        public Builder operation(TraitOperation operation) { this.operation = operation; return this; }
        public Builder partScale(float partScale) { this.partScale = partScale; return this; }
        public Builder color(int rgb) { color = TextColor.fromRgb(rgb); return this; }
        public Builder effect(TraitEffect effect) { effects.add(effect); return this; }
        public Builder effects(TraitEffect... effects) { this.effects.addAll(List.of(effects)); return this; }
        public Builder condition(ICondition... conditions) { this.conditions.addAll(List.of(conditions)); return this; }
        public Builder modLoaded(String modId) { return condition(new ModLoadedCondition(modId)); }
        public Builder requiresTag(TagKey<Item> tag) { return condition(AllTags.present(tag)); }

        public TraitType register() {
            return new TraitType(id, sortOrder, hidden, value, operation, partScale, color, List.copyOf(effects), List.copyOf(conditions));
        }
    }
}
