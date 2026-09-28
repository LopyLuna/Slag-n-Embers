package dev.lopyluna.slag.content.traits;

import net.minecraft.resources.ResourceLocation;

public record Trait(TraitType type, float value, boolean hidden) {
    public ResourceLocation id() {
        return type.id;
    }
}
