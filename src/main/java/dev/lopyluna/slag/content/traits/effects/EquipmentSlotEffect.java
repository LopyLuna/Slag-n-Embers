package dev.lopyluna.slag.content.traits.effects;

import com.mojang.serialization.MapCodec;
import dev.lopyluna.slag.content.traits.Trait;
import dev.lopyluna.slag.content.traits.TraitEffect;
import dev.lopyluna.slag.content.traits.Traits;
import net.minecraft.world.entity.EquipmentSlot;
import javax.annotation.Nonnull;

public record EquipmentSlotEffect(EquipmentSlot slot) implements TraitEffect {
    public static final MapCodec<EquipmentSlotEffect> CODEC = EquipmentSlot.CODEC.fieldOf("slot").xmap(EquipmentSlotEffect::new, EquipmentSlotEffect::slot);

    @Override
    public @Nonnull MapCodec<EquipmentSlotEffect> codec() {
        return CODEC;
    }

    @Override
    public void collect(Trait trait, Traits.Builder builder) {
        if (builder.equipmentSlot == null) builder.equipmentSlot = slot;
    }
}
