package dev.lopyluna.slag.content.traits.effects;

import com.mojang.serialization.MapCodec;
import dev.lopyluna.slag.content.traits.Trait;
import dev.lopyluna.slag.content.traits.TraitEffect;
import dev.lopyluna.slag.content.traits.Traits;
import net.minecraft.core.component.DataComponentPatch;
import javax.annotation.Nonnull;

public record ComponentsEffect(DataComponentPatch components) implements TraitEffect {
    public static final MapCodec<ComponentsEffect> CODEC = DataComponentPatch.CODEC.fieldOf("components").xmap(ComponentsEffect::new, ComponentsEffect::components);

    @Override
    public @Nonnull MapCodec<ComponentsEffect> codec() {
        return CODEC;
    }

    @Override
    public void collect(Trait trait, Traits.Builder builder) {
        builder.components.add(components);
    }
}
