package dev.lopyluna.slag.content.traits.effects;

import com.mojang.serialization.MapCodec;
import dev.lopyluna.slag.content.traits.Trait;
import dev.lopyluna.slag.content.traits.TraitEffect;
import dev.lopyluna.slag.content.traits.Traits;
import net.neoforged.neoforge.common.ItemAbility;
import javax.annotation.Nonnull;

import java.util.List;

public record AbilitiesEffect(List<ItemAbility> abilities) implements TraitEffect {
    public static final MapCodec<AbilitiesEffect> CODEC = ItemAbility.CODEC.listOf().fieldOf("abilities").xmap(AbilitiesEffect::new, AbilitiesEffect::abilities);

    public static AbilitiesEffect of(ItemAbility... abilities) {
        return new AbilitiesEffect(List.of(abilities));
    }

    @Override
    public @Nonnull MapCodec<AbilitiesEffect> codec() {
        return CODEC;
    }

    @Override
    public void collect(Trait trait, Traits.Builder builder) {
        builder.abilities.addAll(abilities);
    }
}
