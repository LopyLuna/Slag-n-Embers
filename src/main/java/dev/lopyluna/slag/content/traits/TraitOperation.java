package dev.lopyluna.slag.content.traits;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import javax.annotation.Nonnull;

public enum TraitOperation implements StringRepresentable {
    ADD("add"),
    MULTIPLY("multiply");

    public static final Codec<TraitOperation> CODEC = StringRepresentable.fromEnum(TraitOperation::values);

    private final String name;

    TraitOperation(String name) {
        this.name = name;
    }

    @Override
    public @Nonnull String getSerializedName() {
        return name;
    }
}
