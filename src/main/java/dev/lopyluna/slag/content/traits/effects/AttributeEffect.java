package dev.lopyluna.slag.content.traits.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lopyluna.slag.content.traits.Trait;
import dev.lopyluna.slag.content.traits.TraitEffect;
import dev.lopyluna.slag.content.traits.Traits;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import javax.annotation.Nonnull;

import java.util.Optional;

public record AttributeEffect(Holder<Attribute> attribute, Optional<EquipmentSlotGroup> slot, ResourceLocation id, AttributeModifier.Operation operation, float scale, boolean round) implements TraitEffect {
    public static final MapCodec<AttributeEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Attribute.CODEC.fieldOf("attribute").forGetter(AttributeEffect::attribute),
            EquipmentSlotGroup.CODEC.optionalFieldOf("slot").forGetter(AttributeEffect::slot),
            ResourceLocation.CODEC.fieldOf("id").forGetter(AttributeEffect::id),
            AttributeModifier.Operation.CODEC.optionalFieldOf("operation", AttributeModifier.Operation.ADD_VALUE).forGetter(AttributeEffect::operation),
            Codec.FLOAT.optionalFieldOf("scale", 1f).forGetter(AttributeEffect::scale),
            Codec.BOOL.optionalFieldOf("round", false).forGetter(AttributeEffect::round)
    ).apply(instance, AttributeEffect::new));

    public static AttributeEffect held(Holder<Attribute> attribute, ResourceLocation id) {
        return new AttributeEffect(attribute, Optional.of(EquipmentSlotGroup.MAINHAND), id, AttributeModifier.Operation.ADD_VALUE, 1f, false);
    }

    public static AttributeEffect equipped(Holder<Attribute> attribute, ResourceLocation id) {
        return new AttributeEffect(attribute, Optional.empty(), id, AttributeModifier.Operation.ADD_VALUE, 1f, false);
    }

    public AttributeEffect scaled(float scale) {
        return new AttributeEffect(attribute, slot, id, operation, scale, round);
    }

    public AttributeEffect rounded() {
        return new AttributeEffect(attribute, slot, id, operation, scale, true);
    }

    @Override
    public @Nonnull MapCodec<AttributeEffect> codec() {
        return CODEC;
    }

    @Override
    public void collect(Trait trait, Traits.Builder builder) {
        var amount = trait.value() * scale;
        builder.attribute(attribute, id, round ? Math.round(amount) : amount, operation, slot.orElse(null));
    }
}
