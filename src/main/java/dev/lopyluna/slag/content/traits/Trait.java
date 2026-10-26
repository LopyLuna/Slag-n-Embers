package dev.lopyluna.slag.content.traits;

import net.minecraft.resources.Identifier;

public record Trait(TraitType type, float value, boolean hidden) {
    public Identifier id() {
        return type.id;
    }
}
