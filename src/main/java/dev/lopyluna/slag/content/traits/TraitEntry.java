package dev.lopyluna.slag.content.traits;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

@SuppressWarnings({"unused"})
public record TraitEntry(ResourceLocation trait, Optional<Float> value, Optional<TraitOperation> operation, Optional<Boolean> hidden) {
    private static final Codec<TraitEntry> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("trait").forGetter(TraitEntry::trait),
            Codec.FLOAT.optionalFieldOf("value").forGetter(TraitEntry::value),
            TraitOperation.CODEC.optionalFieldOf("operation").forGetter(TraitEntry::operation),
            Codec.BOOL.optionalFieldOf("hidden").forGetter(TraitEntry::hidden)
    ).apply(instance, TraitEntry::new));

    public static final Codec<TraitEntry> CODEC = Codec.either(ResourceLocation.CODEC, FULL_CODEC).xmap(
            either -> either.map(TraitEntry::of, entry -> entry),
            entry -> entry.isSimple() ? Either.left(entry.trait) : Either.right(entry));

    public static TraitEntry of(ResourceLocation trait) {
        return new TraitEntry(trait, Optional.empty(), Optional.empty(), Optional.empty());
    }
    public static TraitEntry of(TraitType trait) {
        return of(trait.id);
    }
    public static TraitEntry of(TraitType trait, float value) {
        return new TraitEntry(trait.id, Optional.of(value), Optional.empty(), Optional.empty());
    }
    public static TraitEntry multiply(TraitType trait, float value) {
        return new TraitEntry(trait.id, Optional.of(value), Optional.of(TraitOperation.MULTIPLY), Optional.empty());
    }

    public TraitEntry withHidden(boolean hidden) {
        return new TraitEntry(trait, value, operation, Optional.of(hidden));
    }

    public boolean isSimple() {
        return value.isEmpty() && operation.isEmpty() && hidden.isEmpty();
    }

    public float value(TraitType type) {
        return value.orElse(type.value);
    }

    public TraitOperation operation(TraitType type) {
        return operation.orElse(type.operation);
    }
}
