package dev.lopyluna.slag.register;

import dev.lopyluna.slag.content.traits.TraitType;
import dev.lopyluna.slag.content.types.MaterialType;
import dev.lopyluna.slag.content.types.ModularType;
import dev.lopyluna.slag.content.types.PartType;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.data.loading.DatagenModLoader;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings({"UnusedReturnValue", "unused", "NullableProblems", "NonAtomicOperationOnVolatileField"})
public class AllDynamicTypes {
    private static final Map<ResourceLocation, MaterialType> MATERIAL_TYPES = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, PartType> PART_TYPES = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, ModularType> MODULAR_TYPES = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, TraitType> TRAIT_TYPES = new ConcurrentHashMap<>();
    public static volatile int version;

    public static MaterialType registerMaterial(MaterialType materialType) {
        if (materialType == null || materialType.id == null) return null;
        if (conditionsMet(materialType.conditions, ICondition.IContext.EMPTY)) MATERIAL_TYPES.put(materialType.id, materialType);
        return materialType;
    }

    public static PartType registerPart(PartType partType) {
        if (partType == null || partType.id == null) return null;
        if (conditionsMet(partType.conditions, ICondition.IContext.EMPTY)) PART_TYPES.put(partType.id, partType);
        return partType;
    }

    public static ModularType registerModular(ModularType modularType) {
        if (modularType == null || modularType.id == null) return null;
        if (conditionsMet(modularType.conditions, ICondition.IContext.EMPTY)) MODULAR_TYPES.put(modularType.id, modularType);
        return modularType;
    }

    public static TraitType registerTrait(TraitType traitType) {
        if (traitType == null || traitType.id == null) return null;
        if (conditionsMet(traitType.conditions, ICondition.IContext.EMPTY)) TRAIT_TYPES.put(traitType.id, traitType);
        return traitType;
    }

    public static boolean conditionsMet(List<ICondition> conditions, ICondition.IContext context) {
        if (conditions.isEmpty() || DatagenModLoader.isRunningDataGen()) return true;
        for (var condition : conditions) if (!condition.test(context)) return false;
        return true;
    }

    public static ICondition.IContext context(RegistryAccess access) {
        return new ICondition.IContext() {
            @Override
            public <T> Map<ResourceLocation, Collection<Holder<T>>> getAllTags(ResourceKey<? extends Registry<T>> key) {
                var tags = new HashMap<ResourceLocation, Collection<Holder<T>>>();
                access.registry(key).ifPresent(registry -> registry.getTags().forEach(pair -> tags.put(pair.getFirst().location(), pair.getSecond().stream().toList())));
                return tags;
            }

            @Override
            public <T> Collection<Holder<T>> getTag(TagKey<T> key) {
                var registry = access.registry(key.registry()).orElse(null);
                if (registry == null) return List.of();
                return registry.getTag(key).<Collection<Holder<T>>>map(set -> set.stream().toList()).orElse(List.of());
            }
        };
    }

    public static Optional<TraitType> getTrait(ResourceLocation id) {
        return id == null ? Optional.empty() : Optional.ofNullable(TRAIT_TYPES.get(id));
    }

    public static Collection<TraitType> getAllTraits() {
        return TRAIT_TYPES.values();
    }

    public static Optional<MaterialType> getMaterial(ResourceLocation id) {
        return id == null ? Optional.empty() : Optional.ofNullable(MATERIAL_TYPES.get(id));
    }

    public static Optional<PartType> getPart(ResourceLocation id) {
        return id == null ? Optional.empty() : Optional.ofNullable(PART_TYPES.get(id));
    }

    public static Optional<ModularType> getModular(ResourceLocation id) {
        return id == null ? Optional.empty() : Optional.ofNullable(MODULAR_TYPES.get(id));
    }

    public static Collection<MaterialType> getAllMaterials() {
        return MATERIAL_TYPES.values();
    }

    public static List<MaterialType> getAllMaterialsList() {
        return new ArrayList<>(getAllMaterials());
    }

    public static Collection<PartType> getAllParts() {
        return PART_TYPES.values();
    }

    public static List<PartType> getAllPartsList() {
        return new ArrayList<>(getAllParts());
    }

    public static List<PartType> getAllPartsFromModular(ModularType type) {
        return getAllPartsList().stream().filter(part -> type.segments.contains(part.segmentPart)).toList();
    }

    public static Collection<ModularType> getAllModulars() {
        return MODULAR_TYPES.values();
    }

    public static List<ModularType> getAllModularsList() {
        return new ArrayList<>(getAllModulars());
    }

    public static void load(RegistryAccess access) {
        var context = context(access);
        var changed = load(TRAIT_TYPES, access, AllRegistries.TRAIT_TYPE_REGISTRY_KEY, t -> t.id, t -> t.dontRegister || !conditionsMet(t.conditions, context));
        changed |= load(MATERIAL_TYPES, access, AllRegistries.MATERIAL_TYPE_REGISTRY_KEY, m -> m.id, m -> m.dontRegister || !conditionsMet(m.conditions, context));
        changed |= load(PART_TYPES, access, AllRegistries.PART_TYPE_REGISTRY_KEY, p -> p.id, p -> p.dontRegister || !conditionsMet(p.conditions, context));
        changed |= load(MODULAR_TYPES, access, AllRegistries.MODULAR_TYPE_REGISTRY_KEY, m -> m.id, m -> m.dontRegister || !conditionsMet(m.conditions, context));
        if (changed) version++;
    }

    private static <T> boolean load(Map<ResourceLocation, T> types, RegistryAccess access, ResourceKey<Registry<T>> key, Function<T, ResourceLocation> id, Predicate<T> skip) {
        var registry = access.registry(key).orElse(null);
        if (registry == null) return false;
        var loaded = new HashMap<ResourceLocation, T>();
        for (var type : registry) if (!skip.test(type)) loaded.put(id.apply(type), type);
        if (types.equals(loaded)) return false;
        types.putAll(loaded);
        types.keySet().retainAll(loaded.keySet());
        return true;
    }
}
