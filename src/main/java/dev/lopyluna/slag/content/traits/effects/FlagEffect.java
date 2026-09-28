package dev.lopyluna.slag.content.traits.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.lopyluna.slag.content.traits.Trait;
import dev.lopyluna.slag.content.traits.TraitEffect;
import dev.lopyluna.slag.content.traits.Traits;
import net.minecraft.util.StringRepresentable;
import javax.annotation.Nonnull;

public record FlagEffect(Flag flag) implements TraitEffect {
    public static final MapCodec<FlagEffect> CODEC = Flag.CODEC.fieldOf("flag").xmap(FlagEffect::new, FlagEffect::flag);

    public static final FlagEffect TOOL = new FlagEffect(Flag.TOOL);
    public static final FlagEffect DISABLE_SHIELD = new FlagEffect(Flag.DISABLE_SHIELD);
    public static final FlagEffect PIGLIN_NEUTRAL = new FlagEffect(Flag.PIGLIN_NEUTRAL);
    public static final FlagEffect SILENT = new FlagEffect(Flag.SILENT);
    public static final FlagEffect GLIDING = new FlagEffect(Flag.GLIDING);

    @Override
    public @Nonnull MapCodec<FlagEffect> codec() {
        return CODEC;
    }

    @Override
    public void collect(Trait trait, Traits.Builder builder) {
        switch (flag) {
            case TOOL -> builder.tool = true;
            case DISABLE_SHIELD -> builder.disablesShield = true;
            case PIGLIN_NEUTRAL -> builder.piglinNeutral = true;
            case SILENT -> builder.silent = true;
            case GLIDING -> builder.gliding = true;
        }
    }

    public enum Flag implements StringRepresentable {
        TOOL("tool"),
        DISABLE_SHIELD("disable_shield"),
        PIGLIN_NEUTRAL("piglin_neutral"),
        SILENT("silent"),
        GLIDING("gliding");

        public static final Codec<Flag> CODEC = StringRepresentable.fromEnum(Flag::values);

        private final String name;

        Flag(String name) {
            this.name = name;
        }

        @Override
        public @Nonnull String getSerializedName() {
            return name;
        }
    }
}
