package dev.lopyluna.slag.content.temperature;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.lopyluna.slag.SlagEmbers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import javax.annotation.Nonnull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"unused", "NonAtomicOperationOnVolatileField"})
public class Temperatures {
    public static final String PATH = "slag/temperatures.json";
    public static final Reloader RELOADER = new Reloader();
    public static final Codec<Tiers> TIER_CODEC = enumCodec(Tiers.class);
    public static final Codec<Type> TYPE_CODEC = enumCodec(Type.class);

    private static final Tiers[] TIERS = Tiers.values();
    private static final Type[] TYPES = Type.values();

    public static volatile Map<Block, List<Entry>> blocks = Map.of();
    public static volatile List<Pair<TagKey<Block>, List<Entry>>> tags = List.of();
    public static volatile String json = "{}";
    public static volatile int version;
    private static volatile Map<BlockState, Heat> states = new ConcurrentHashMap<>();
    public static volatile Runnable onChange = () -> {};

    public static Heat get(BlockState state) {
        var cache = states;
        var heat = cache.get(state);
        if (heat != null) return heat;
        heat = find(state);
        cache.put(state, heat);
        return heat;
    }

    private static Heat find(BlockState state) {
        var entries = blocks.get(state.getBlock());
        if (entries != null) return match(state, entries);
        for (var tag : tags) {
            if (!state.is(tag.getFirst())) continue;
            var heat = match(state, tag.getSecond());
            if (heat != Heat.NONE) return heat;
        }
        return Heat.NONE;
    }

    private static Heat match(BlockState state, List<Entry> entries) {
        for (var entry : entries) if (entry.matches(state)) return entry.heat;
        return Heat.NONE;
    }

    public static Heat average(Level level, BlockPos origin, int widthX, int widthZ) {
        var sum = 0;
        var heaters = 0;
        var scores = new int[TYPES.length];
        for (var x = 0; x < widthX; x++) for (var z = 0; z < widthZ; z++) {
            var pos = origin.offset(x, 0, z);
            if (!level.isLoaded(pos)) continue;
            var heat = get(level.getBlockState(pos));
            if (heat.tier == null) continue;
            heaters++;
            sum += heat.tier.ordinal() - Tiers.WARM.ordinal();
            if (heat.type != Type.NONE) scores[heat.type.ordinal()] += 1 << heat.tier.ordinal();
        }
        if (heaters == 0) return Heat.NONE;
        var best = Type.NONE;
        for (var type : TYPES) if (scores[type.ordinal()] > 0 && scores[type.ordinal()] >= scores[best.ordinal()]) best = type;
        var count = widthX * widthZ;
        var tier = Tiers.WARM.ordinal() + Math.floorDiv(sum * 2 + count, count * 2);
        return Heat.of(TIERS[Mth.clamp(tier, 0, TIERS.length - 1)], best);
    }

    public static void clearCache() {
        states = new ConcurrentHashMap<>();
        version++;
        onChange.run();
    }

    public static void load(JsonObject root, boolean log) {
        var blocks = new IdentityHashMap<Block, List<Entry>>();
        var tags = new ArrayList<Pair<TagKey<Block>, List<Entry>>>();
        for (var element : root.entrySet()) {
            var key = element.getKey();
            var value = element.getValue();
            if (value.isJsonNull()) continue;
            var tag = key.startsWith("#");
            var id = ResourceLocation.tryParse(tag ? key.substring(1) : key);
            if (id == null) {
                error(log, "{} isn't a valid id! is it a typo or something?", key);
                continue;
            }
            if (tag) {
                var entries = entries(key, value, null, log);
                if (!entries.isEmpty()) tags.add(Pair.of(TagKey.create(Registries.BLOCK, id), entries));
                continue;
            }
            if (!ModList.get().isLoaded(id.getNamespace())) continue;
            var block = BuiltInRegistries.BLOCK.getOptional(id);
            if (block.isEmpty()) {
                error(log, "{} doesn't exist! is it a typo or something?", key);
                continue;
            }
            var entries = entries(key, value, block.get(), log);
            if (!entries.isEmpty()) blocks.put(block.get(), entries);
        }
        Temperatures.blocks = blocks;
        Temperatures.tags = tags;
        json = root.toString();
        clearCache();
    }

    private static List<Entry> entries(String key, JsonElement value, @Nullable Block block, boolean log) {
        var list = new ArrayList<Entry>();
        if (!value.isJsonArray()) {
            var entry = entry(key, value, block, log);
            if (entry != null) list.add(entry);
            return list;
        }
        for (var element : value.getAsJsonArray()) {
            var entry = entry(key, element, block, log);
            if (entry != null) list.add(entry);
        }
        return list;
    }

    private static @Nullable Entry entry(String key, JsonElement element, @Nullable Block block, boolean log) {
        if (!element.isJsonObject()) return error(log, "{} has an invalid entry: {}", key, element);
        var object = element.getAsJsonObject();
        var tier = parse(Tiers.class, object.get("temperature"));
        if (tier == null) return error(log, "{} has an invalid temperature: {}", key, object.get("temperature"));
        var type = object.has("type") ? parse(Type.class, object.get("type")) : Type.NONE;
        if (type == null) return error(log, "{} has an invalid type: {}", key, object.get("type"));

        var names = new ArrayList<String>();
        var values = new ArrayList<String>();
        for (var property : object.entrySet()) {
            var name = property.getKey();
            if (name.equals("temperature") || name.equals("type")) continue;
            if (!property.getValue().isJsonPrimitive()) return error(log, "{} has an invalid \"{}\" value: {}", key, name, property.getValue());
            var value = property.getValue().getAsString();
            if (block != null) {
                var found = block.getStateDefinition().getProperty(name);
                if (found == null) return error(log, "{} doesn't have a \"{}\" property! is it a typo or something?", key, name);
                if (found.getValue(value.toLowerCase(Locale.ROOT)).isEmpty()) return error(log, "{} doesn't have \"{}\" as a \"{}\" value! is it a typo or something?", key, value, name);
            }
            names.add(name);
            values.add(value);
        }
        return new Entry(Heat.of(tier, type), names.toArray(String[]::new), values.toArray(String[]::new));
    }

    private static @Nullable Entry error(boolean log, String message, Object... args) {
        if (log) SlagEmbers.LOGGER.error(message, args);
        return null;
    }

    private static <E extends Enum<E>> @Nullable E parse(Class<E> clazz, @Nullable JsonElement element) {
        if (element == null || !element.isJsonPrimitive()) return null;
        return parse(clazz, element.getAsString());
    }

    public static <E extends Enum<E>> @Nullable E parse(Class<E> clazz, String name) {
        try {
            return Enum.valueOf(clazz, name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static <E extends Enum<E>> Codec<E> enumCodec(Class<E> clazz) {
        return Codec.STRING.comapFlatMap(name -> {
            var value = parse(clazz, name);
            return value == null ? DataResult.error(() -> name + " isn't a valid " + clazz.getSimpleName()) : DataResult.success(value);
        }, Enum::name);
    }

    public static class Reloader extends SimplePreparableReloadListener<JsonObject> {
        @Override
        protected @Nonnull JsonObject prepare(ResourceManager manager, @Nonnull ProfilerFiller profiler) {
            var merged = new JsonObject();
            var namespaces = new ArrayList<>(manager.getNamespaces());
            namespaces.sort(Comparator.comparing((String namespace) -> !namespace.equals(SlagEmbers.MOD_ID)).thenComparing(namespace -> namespace));
            for (var namespace : namespaces) for (var resource : manager.getResourceStack(ResourceLocation.fromNamespaceAndPath(namespace, PATH))) {
                try (var reader = resource.openAsReader()) {
                    for (var entry : JsonParser.parseReader(reader).getAsJsonObject().entrySet()) {
                        if (entry.getValue().isJsonNull()) merged.remove(entry.getKey());
                        else merged.add(entry.getKey(), entry.getValue());
                    }
                } catch (Exception e) {
                    SlagEmbers.LOGGER.error("Couldn't read {}:{} from {}", namespace, PATH, resource.sourcePackId(), e);
                }
            }
            return merged;
        }

        @Override
        protected void apply(@Nonnull JsonObject root, @Nonnull ResourceManager manager, @Nonnull ProfilerFiller profiler) {
            load(root, true);
        }
    }

    public static class Entry {
        public final Heat heat;
        public final String[] names;
        public final String[] values;

        public Entry(Heat heat, String[] names, String[] values) {
            this.heat = heat;
            this.names = names;
            this.values = values;
        }

        public boolean matches(BlockState state) {
            var definition = state.getBlock().getStateDefinition();
            for (var i = 0; i < names.length; i++) {
                var property = definition.getProperty(names[i]);
                if (property == null || !values[i].equalsIgnoreCase(name(state, property))) return false;
            }
            return true;
        }

        private static <T extends Comparable<T>> String name(BlockState state, Property<T> property) {
            return property.getName(state.getValue(property));
        }
    }

    public static class Heat {
        public static final ModelProperty<Integer> VISUAL = new ModelProperty<>();
        public static final int VISUALS = (TYPES.length - 1) * (TIERS.length - Tiers.SMOLDERING.ordinal());
        public static final Heat NONE = new Heat(null, Type.NONE);
        private static final Heat[][] ALL = new Heat[TIERS.length][TYPES.length];

        static {
            for (var tier : TIERS) for (var type : TYPES) ALL[tier.ordinal()][type.ordinal()] = new Heat(tier, type);
        }

        public final @Nullable Tiers tier;
        public final Type type;
        public final boolean hot;
        public final int visual;
        public final ModelData modelData;

        private Heat(@Nullable Tiers tier, Type type) {
            this.tier = tier;
            this.type = type;
            hot = tier != null && tier.ordinal() >= Tiers.SMOLDERING.ordinal();
            var shown = type == Type.NONE ? Type.ORANGE : type;
            visual = hot ? (shown.ordinal() - 1) * (TIERS.length - Tiers.SMOLDERING.ordinal()) + tier.ordinal() - Tiers.SMOLDERING.ordinal() : -1;
            modelData = visual < 0 ? ModelData.EMPTY : ModelData.builder().with(VISUAL, visual).build();
        }

        public static Heat of(@Nullable Tiers tier, @Nullable Type type) {
            if (tier == null) return NONE;
            return ALL[tier.ordinal()][(type == null ? Type.NONE : type).ordinal()];
        }

        public static Heat read(CompoundTag tag) {
            return tag.contains("HeatTier") ? of(parse(Tiers.class, tag.getString("HeatTier")), parse(Type.class, tag.getString("HeatType"))) : NONE;
        }

        public void write(CompoundTag tag) {
            if (tier == null) return;
            tag.putString("HeatTier", tier.name());
            tag.putString("HeatType", type.name());
        }

        public float speed(Tiers required, @Nullable Type requiredType) {
            return speed(required, requiredType, false);
        }

        public float speed(Tiers required, @Nullable Type requiredType, boolean strict) {
            if (tier == null || (requiredType != null && requiredType != type)) return 0;
            var diff = tier.ordinal() - required.ordinal();
            return diff < (strict ? 0 : -1) ? 0 : diff == -1 ? 0.5f : 1 << diff;
        }
    }

    public enum Type {
        NONE, //ONLY USE FOR WARM & BELOW
        RED,
        ORANGE,
        YELLOW,
        SULFUR,
        GREEN,
        CYAN,
        TEAL,
        BLUE,
        PURPLE,
        PINK;

        public final String id = name().toLowerCase(Locale.ROOT);
    }

    public enum Tiers {
        FROZEN,
        FREEZING,
        COOL,
        WARM,
        SMOLDERING,
        HEATED,
        BLAZING,
        INFERNAL;

        public final String id = name().toLowerCase(Locale.ROOT);
    }
}
