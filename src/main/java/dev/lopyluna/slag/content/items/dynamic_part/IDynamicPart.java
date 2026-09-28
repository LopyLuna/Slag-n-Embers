package dev.lopyluna.slag.content.items.dynamic_part;

import dev.lopyluna.slag.content.traits.Traits;
import dev.lopyluna.slag.content.types.MaterialType;
import dev.lopyluna.slag.content.types.PartType;
import dev.lopyluna.slag.register.AllTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public interface IDynamicPart {
    TagKey<Item> EMPTY_SEGMENT = AllTags.item("empty");

    Optional<MaterialType> getMaterialType(ItemStack stack);
    Optional<PartType> getPartType(ItemStack stack);

    default TagKey<Item> getPartSegment(ItemStack stack) {
        return getPartType(stack).map(type -> type.segmentPart).orElse(EMPTY_SEGMENT);
    }

    default Traits getTraits(ItemStack stack) {
        return Traits.part(getMaterialType(stack).orElse(null), getPartType(stack).orElse(null));
    }
}
