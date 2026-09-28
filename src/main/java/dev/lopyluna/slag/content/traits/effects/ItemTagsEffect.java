package dev.lopyluna.slag.content.traits.effects;

import com.mojang.serialization.MapCodec;
import dev.lopyluna.slag.content.traits.Trait;
import dev.lopyluna.slag.content.traits.TraitEffect;
import dev.lopyluna.slag.content.traits.Traits;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import javax.annotation.Nonnull;

import java.util.List;

public record ItemTagsEffect(List<TagKey<Item>> tags) implements TraitEffect {
    public static final MapCodec<ItemTagsEffect> CODEC = TagKey.codec(Registries.ITEM).listOf().fieldOf("tags").xmap(ItemTagsEffect::new, ItemTagsEffect::tags);

    @SafeVarargs
    public static ItemTagsEffect of(TagKey<Item>... tags) {
        return new ItemTagsEffect(List.of(tags));
    }

    @Override
    public @Nonnull MapCodec<ItemTagsEffect> codec() {
        return CODEC;
    }

    @Override
    public void collect(Trait trait, Traits.Builder builder) {
        builder.itemTags.addAll(tags);
    }
}
