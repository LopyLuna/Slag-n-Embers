package dev.lopyluna.slag.content.traits.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.lopyluna.slag.content.traits.Trait;
import dev.lopyluna.slag.content.traits.TraitEffect;
import dev.lopyluna.slag.content.traits.Traits;
import net.minecraft.util.StringRepresentable;
import javax.annotation.Nonnull;

public record StatEffect(Stat stat) implements TraitEffect {
    public static final MapCodec<StatEffect> CODEC = Stat.CODEC.fieldOf("stat").xmap(StatEffect::new, StatEffect::stat);

    @Override
    public @Nonnull MapCodec<StatEffect> codec() {
        return CODEC;
    }

    @Override
    public void collect(Trait trait, Traits.Builder builder) {
        var value = trait.value();
        switch (stat) {
            case MAX_DAMAGE -> builder.maxDamage += value;
            case ENCHANTABILITY -> builder.enchantability += value;
            case MINING_SPEED -> builder.miningSpeed += value;
            case MINING_TIER -> builder.miningTier += value;
            case MAX_STACK_SIZE -> builder.maxStackSize += value;
        }
    }

    public enum Stat implements StringRepresentable {
        MAX_DAMAGE("max_damage"),
        ENCHANTABILITY("enchantability"),
        MINING_SPEED("mining_speed"),
        MINING_TIER("mining_tier"),
        MAX_STACK_SIZE("max_stack_size");

        public static final Codec<Stat> CODEC = StringRepresentable.fromEnum(Stat::values);

        private final String name;

        Stat(String name) {
            this.name = name;
        }

        @Override
        public @Nonnull String getSerializedName() {
            return name;
        }
    }
}
