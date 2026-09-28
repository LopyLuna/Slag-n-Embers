package dev.lopyluna.slag.content.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lopyluna.slag.content.items.dynamic_part.IDynamicPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public record Incompatible(List<ResourceLocation> materials, List<ResourceLocation> parts, List<ResourceLocation> modulars) {
    public static final Incompatible EMPTY = new Incompatible(List.of(), List.of(), List.of());

    public static final Codec<Incompatible> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.listOf().optionalFieldOf("materials", List.of()).forGetter(Incompatible::materials),
            ResourceLocation.CODEC.listOf().optionalFieldOf("parts", List.of()).forGetter(Incompatible::parts),
            ResourceLocation.CODEC.listOf().optionalFieldOf("modulars", List.of()).forGetter(Incompatible::modulars)
    ).apply(instance, Incompatible::new));

    public static boolean compatible(MaterialType material, PartType part) {
        return !material.incompatible.parts.contains(part.id) && !part.incompatible.materials.contains(material.id);
    }

    public static boolean compatible(MaterialType material, ModularType modular) {
        return !material.incompatible.modulars.contains(modular.id) && !modular.incompatible.materials.contains(material.id);
    }

    public static boolean compatible(PartType part, ModularType modular) {
        return !part.incompatible.modulars.contains(modular.id) && !modular.incompatible.parts.contains(part.id);
    }

    public static boolean compatible(MaterialType a, MaterialType b) {
        return !a.incompatible.materials.contains(b.id) && !b.incompatible.materials.contains(a.id);
    }

    public static boolean compatible(PartType a, PartType b) {
        return !a.incompatible.parts.contains(b.id) && !b.incompatible.parts.contains(a.id);
    }

    public static boolean compatible(List<ItemStack> items, @Nullable ModularType modular) {
        var materials = new ArrayList<MaterialType>();
        var parts = new ArrayList<PartType>();
        for (var stack : items) {
            if (!(stack.getItem() instanceof IDynamicPart dynamic)) continue;
            var material = dynamic.getMaterialType(stack).orElse(null);
            var part = dynamic.getPartType(stack).orElse(null);
            if (material != null) {
                if (part != null && !compatible(material, part)) return false;
                if (modular != null && !compatible(material, modular)) return false;
                if (!materials.contains(material)) {
                    for (var other : materials) if (!compatible(material, other)) return false;
                    materials.add(material);
                }
            }
            if (part != null) {
                if (modular != null && !compatible(part, modular)) return false;
                if (!parts.contains(part)) {
                    for (var other : parts) if (!compatible(part, other)) return false;
                    parts.add(part);
                }
            }
        }
        return true;
    }

    public static class Builder {
        private final List<ResourceLocation> materials = new ArrayList<>();
        private final List<ResourceLocation> parts = new ArrayList<>();
        private final List<ResourceLocation> modulars = new ArrayList<>();

        public Builder materials(ResourceLocation... ids) { materials.addAll(List.of(ids)); return this; }
        public Builder parts(ResourceLocation... ids) { parts.addAll(List.of(ids)); return this; }
        public Builder modulars(ResourceLocation... ids) { modulars.addAll(List.of(ids)); return this; }
        public Builder materials(MaterialType... types) { for (var type : types) materials.add(type.id); return this; }
        public Builder parts(PartType... types) { for (var type : types) parts.add(type.id); return this; }
        public Builder modulars(ModularType... types) { for (var type : types) modulars.add(type.id); return this; }

        public Incompatible build() {
            if (materials.isEmpty() && parts.isEmpty() && modulars.isEmpty()) return EMPTY;
            return new Incompatible(List.copyOf(materials), List.copyOf(parts), List.copyOf(modulars));
        }
    }
}
