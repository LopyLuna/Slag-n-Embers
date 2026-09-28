package dev.lopyluna.slag.content.utils;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lopyluna.slag.config.SlagServerConfigs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@SuppressWarnings({"unused", "NonAtomicOperationOnVolatileField"})
public final class ItemResult {
    private static final Map<TagKey<Item>, Item> PREFERRED = new ConcurrentHashMap<>();
    private static volatile @Nullable Map<String, Integer> priorities;
    private static volatile int version;

    private static final Codec<ItemResult> TAG_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(result -> result.tag),
            ExtraCodecs.intRange(1, 99).optionalFieldOf("count", 1).forGetter(result -> result.count)
    ).apply(instance, ItemResult::of));

    public static final Codec<ItemResult> CODEC = Codec.either(ItemStack.CODEC, TAG_CODEC)
            .xmap(either -> either.map(ItemResult::of, Function.identity()), result -> result.tag == null ? Either.left(result.stack) : Either.right(result));

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemResult> STREAM_CODEC = StreamCodec.of((buffer, result) -> {
        buffer.writeBoolean(result.tag != null);
        if (result.tag == null) ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, result.stack);
        else {
            buffer.writeResourceLocation(result.tag.location());
            buffer.writeVarInt(result.count);
        }
    }, buffer -> buffer.readBoolean() ? of(TagKey.create(Registries.ITEM, buffer.readResourceLocation()), buffer.readVarInt()) : of(ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer)));

    private final ItemStack stack;
    private final @Nullable TagKey<Item> tag;
    private final int count;
    private volatile ItemStack resolved = ItemStack.EMPTY;
    private volatile int resolvedVersion = -1;

    private ItemResult(ItemStack stack, @Nullable TagKey<Item> tag, int count) {
        this.stack = stack;
        this.tag = tag;
        this.count = count;
    }

    public static ItemResult of(ItemStack stack) {
        return new ItemResult(stack, null, stack.getCount());
    }

    public static ItemResult of(TagKey<Item> tag, int count) {
        return new ItemResult(ItemStack.EMPTY, tag, count);
    }

    public @Nullable TagKey<Item> tag() {
        return tag;
    }

    public ItemStack stack() {
        if (tag == null) return stack;
        var current = version;
        if (resolvedVersion == current) return resolved;
        var item = preferred(tag);
        resolved = item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item, count);
        resolvedVersion = current;
        return resolved;
    }

    public static void invalidate() {
        PREFERRED.clear();
        priorities = null;
        version++;
    }

    public static Item preferred(TagKey<Item> tag) {
        return PREFERRED.computeIfAbsent(tag, ItemResult::find);
    }

    private static Item find(TagKey<Item> tag) {
        var best = Items.AIR;
        var bestPriority = Integer.MIN_VALUE;
        for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
            var priority = priority(holder.value());
            if (priority <= bestPriority) continue;
            best = holder.value();
            bestPriority = priority;
        }
        return best;
    }

    public static int priority(Item item) {
        var map = priorities;
        if (map == null) priorities = map = parsePriorities();
        return map.getOrDefault(BuiltInRegistries.ITEM.getKey(item).getNamespace(), 0);
    }

    private static Map<String, Integer> parsePriorities() {
        if (!SlagServerConfigs.SPEC.isLoaded()) return Map.of();
        var map = new HashMap<String, Integer>();
        for (var entry : SlagServerConfigs.TAG_RESULT_PRIORITIES.get()) {
            var split = entry.indexOf('=');
            if (split > 0) map.put(entry.substring(0, split).trim(), Integer.parseInt(entry.substring(split + 1).trim()));
        }
        return map;
    }
}
