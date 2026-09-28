package dev.lopyluna.slag.content.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lopyluna.slag.config.SlagCommonConfigs;
import net.neoforged.neoforge.common.conditions.ICondition;
import javax.annotation.Nonnull;

public record CastOnlyCondition(float tier) implements ICondition {
    public static final MapCodec<CastOnlyCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.FLOAT.fieldOf("tier").forGetter(CastOnlyCondition::tier)
    ).apply(i, CastOnlyCondition::new));

    @Override
    public boolean test(@Nonnull IContext context) {
        if (!SlagCommonConfigs.SPEC.isLoaded()) return true;
        return !SlagCommonConfigs.CAST_ONLY_PARTS.get() || tier < SlagCommonConfigs.CAST_ONLY_TIER.get();
    }

    @Override
    public @Nonnull MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
