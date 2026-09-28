package dev.lopyluna.slag.content.traits.effects;

import com.mojang.serialization.MapCodec;
import dev.lopyluna.slag.content.traits.Trait;
import dev.lopyluna.slag.content.traits.TraitEffect;
import dev.lopyluna.slag.content.traits.Traits;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import javax.annotation.Nonnull;

public record DamageImmunityEffect(TagKey<DamageType> damageTypes) implements TraitEffect {
    public static final MapCodec<DamageImmunityEffect> CODEC = TagKey.codec(Registries.DAMAGE_TYPE).fieldOf("damage_types").xmap(DamageImmunityEffect::new, DamageImmunityEffect::damageTypes);

    @Override
    public @Nonnull MapCodec<DamageImmunityEffect> codec() {
        return CODEC;
    }

    @Override
    public void collect(Trait trait, Traits.Builder builder) {
        builder.immunities.add(damageTypes);
    }
}
