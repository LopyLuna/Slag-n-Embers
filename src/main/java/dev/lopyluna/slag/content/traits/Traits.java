package dev.lopyluna.slag.content.traits;

import dev.lopyluna.slag.content.items.dynamic_part.IDynamicPart;
import dev.lopyluna.slag.content.items.dynamic_part.IModularItem;
import dev.lopyluna.slag.content.traits.effects.MiningEffect;
import dev.lopyluna.slag.content.types.MaterialType;
import dev.lopyluna.slag.content.types.ModularType;
import dev.lopyluna.slag.content.types.PartType;
import dev.lopyluna.slag.register.AllDataComponents;
import dev.lopyluna.slag.register.AllDynamicTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbility;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class Traits {
    public static final Traits EMPTY = new Builder(List.of()).build();

    private static final Comparator<Trait> ORDER = Comparator.<Trait>comparingInt(trait -> trait.type().sortOrder).thenComparing(Trait::id);
    private static final Map<PartKey, Traits> PARTS = new ConcurrentHashMap<>();
    private static volatile int partsVersion = -1;

    public final List<Trait> all;
    public final List<Trait> visible;
    public final @Nullable EquipmentSlot equipmentSlot;
    public final boolean tool, armor, disablesShield, piglinNeutral, silent, gliding;
    public final float maxDamage, enchantability, miningSpeed, miningTier, maxStackSize;
    public final int blockCost;
    public final List<MiningEffect> mining;
    public final Set<ItemAbility> abilities;
    public final Set<TagKey<Item>> itemTags;
    public final List<TagKey<DamageType>> immunities;
    public final List<DataComponentPatch> components;
    public final ItemAttributeModifiers attributes;
    public final List<Hook> useOn, blockBreak, blockDrops, hurtEnemy, inventoryTick;
    private final Map<ResourceLocation, Trait> byId;

    private Traits(Builder builder) {
        all = List.copyOf(builder.traits);
        var visible = new ArrayList<Trait>();
        var byId = new HashMap<ResourceLocation, Trait>();
        for (var trait : all) {
            if (!trait.hidden()) visible.add(trait);
            byId.put(trait.id(), trait);
        }
        this.visible = List.copyOf(visible);
        this.byId = byId;

        equipmentSlot = builder.equipmentSlot;
        tool = builder.tool;
        armor = equipmentSlot != null && equipmentSlot.isArmor();
        disablesShield = builder.disablesShield;
        piglinNeutral = builder.piglinNeutral;
        silent = builder.silent;
        gliding = builder.gliding;
        maxDamage = builder.maxDamage;
        enchantability = builder.enchantability;
        miningSpeed = builder.miningSpeed;
        miningTier = builder.miningTier;
        maxStackSize = builder.maxStackSize;
        blockCost = builder.blockCost == Integer.MAX_VALUE ? 2 : builder.blockCost;
        mining = List.copyOf(builder.mining);
        abilities = Set.copyOf(builder.abilities);
        itemTags = Set.copyOf(builder.itemTags);
        immunities = List.copyOf(builder.immunities);
        components = List.copyOf(builder.components);
        useOn = List.copyOf(builder.useOn);
        blockBreak = List.copyOf(builder.blockBreak);
        blockDrops = List.copyOf(builder.blockDrops);
        hurtEnemy = List.copyOf(builder.hurtEnemy);
        inventoryTick = List.copyOf(builder.inventoryTick);

        var merged = new LinkedHashMap<ModifierKey, Double>();
        for (var modifier : builder.modifiers) {
            var slot = modifier.slot;
            var id = modifier.id;
            if (slot == null) {
                if (equipmentSlot == null) continue;
                slot = EquipmentSlotGroup.bySlot(equipmentSlot);
                id = id.withSuffix("." + slotName(equipmentSlot));
            }
            merged.merge(new ModifierKey(modifier.attribute, id, modifier.operation, slot), modifier.amount, Double::sum);
        }
        var attributes = ItemAttributeModifiers.builder();
        for (var entry : merged.entrySet()) {
            var key = entry.getKey();
            if (entry.getValue() != 0) attributes.add(key.attribute, new AttributeModifier(key.id, entry.getValue(), key.operation), key.slot);
        }
        this.attributes = attributes.build();
    }

    public boolean has(TraitType type) {
        return byId.containsKey(type.id);
    }

    public @Nullable Trait get(TraitType type) {
        return byId.get(type.id);
    }

    public float value(TraitType type) {
        var trait = byId.get(type.id);
        return trait == null ? 0 : trait.value();
    }

    public boolean isEmpty() {
        return all.isEmpty();
    }

    public boolean isCorrectForDrops(BlockState state) {
        for (var effect : mining) if (state.is(effect.blocks())) return true;
        return false;
    }

    public float miningMultiplier(BlockState state) {
        var best = 0f;
        for (var effect : mining) if (state.is(effect.blocks())) best = Math.max(best, effect.speed());
        return best;
    }

    public boolean immuneTo(DamageSource source) {
        for (var tag : immunities) if (source.is(tag)) return true;
        return false;
    }

    public void applyComponents(ItemStack stack) {
        for (var patch : components) stack.applyComponents(patch);
    }

    public static Traits of(ItemStack stack) {
        if (stack.getItem() instanceof IModularItem item) return stack.has(AllDataComponents.MODULAR_TYPE) ? item.getTraits(stack) : EMPTY;
        if (stack.getItem() instanceof IDynamicPart part) return part.getTraits(stack);
        return EMPTY;
    }

    public static ItemStack glider(LivingEntity entity, ItemStack chest, boolean visual) {
        if (visual ? chest.is(Items.ELYTRA) || of(chest).gliding || chest.canElytraFly(entity) : chest.canElytraFly(entity)) return chest;
        for (var slot : EquipmentSlot.values()) if (slot.isArmor() && slot != EquipmentSlot.CHEST) {
            var stack = entity.getItemBySlot(slot);
            if (of(stack).gliding && (visual || stack.canElytraFly(entity))) return stack;
        }
        return chest;
    }

    public static Traits part(@Nullable MaterialType material, @Nullable PartType part) {
        var version = AllDynamicTypes.version;
        if (partsVersion != version) {
            PARTS.clear();
            partsVersion = version;
        }
        var key = new PartKey(material == null ? null : material.id, part == null ? null : part.id);
        return PARTS.computeIfAbsent(key, k -> resolve(Collections.singletonList(material), Collections.singletonList(part), null));
    }

    public static Traits resolve(List<ItemStack> items, @Nullable ModularType modular) {
        var materials = new ArrayList<MaterialType>();
        var parts = new ArrayList<PartType>();
        for (var stack : items) if (stack.getItem() instanceof IDynamicPart part) {
            materials.add(part.getMaterialType(stack).orElse(null));
            parts.add(part.getPartType(stack).orElse(null));
        }
        return resolve(materials, parts, modular);
    }

    public static Traits resolve(List<MaterialType> materials, List<PartType> parts, @Nullable ModularType modular) {
        var size = materials.size();
        var sums = new LinkedHashMap<ResourceLocation, Sum>();
        for (var i = 0; i < size; i++) {
            var material = materials.get(i);
            var part = parts.get(i);
            if (material != null) for (var entry : material.traits) {
                var sum = sum(sums, entry, size);
                if (sum != null) sum.material(i, entry);
            }
            if (part != null) for (var entry : part.traits) {
                var sum = sum(sums, entry, size);
                if (sum != null) sum.part(i, entry);
            }
        }
        if (modular != null) for (var entry : modular.traits) {
            var sum = sum(sums, entry, size);
            if (sum != null) sum.modular(entry);
        }
        if (sums.isEmpty()) return EMPTY;
        var traits = new ArrayList<Trait>();
        for (var sum : sums.values()) traits.add(sum.trait(size));
        traits.sort(ORDER);
        return new Builder(traits).build();
    }

    private static @Nullable Sum sum(Map<ResourceLocation, Sum> sums, TraitEntry entry, int size) {
        var sum = sums.get(entry.trait());
        if (sum != null) return sum;
        var type = AllDynamicTypes.getTrait(entry.trait()).orElse(null);
        if (type == null) return null;
        sum = new Sum(type, size);
        sums.put(type.id, sum);
        return sum;
    }

    private static String slotName(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> "helmet";
            case CHEST -> "chestplate";
            case LEGS -> "leggings";
            case FEET -> "boots";
            default -> slot.getName();
        };
    }

    public record Hook(Trait trait, TraitEffect effect) {}

    private record PartKey(@Nullable ResourceLocation material, @Nullable ResourceLocation part) {}

    private record Modifier(Holder<Attribute> attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation, @Nullable EquipmentSlotGroup slot) {}

    private record ModifierKey(Holder<Attribute> attribute, ResourceLocation id, AttributeModifier.Operation operation, EquipmentSlotGroup slot) {}

    private static class Sum {
        private final TraitType type;
        private final float[] materialAdd, partAdd, multiply;
        private final boolean[] multiplied;
        private float modularAdd, modularMultiply = 1;
        private @Nullable Boolean materialHidden, partHidden, modularHidden;

        private Sum(TraitType type, int size) {
            this.type = type;
            materialAdd = new float[size];
            partAdd = new float[size];
            multiply = new float[size];
            multiplied = new boolean[size];
        }

        private void material(int i, TraitEntry entry) {
            apply(i, entry, materialAdd);
            if (entry.hidden().isPresent()) materialHidden = entry.hidden().get();
        }

        private void part(int i, TraitEntry entry) {
            apply(i, entry, partAdd);
            if (entry.hidden().isPresent()) partHidden = entry.hidden().get();
        }

        private void modular(TraitEntry entry) {
            var value = entry.value(type);
            if (entry.operation(type) == TraitOperation.MULTIPLY) modularMultiply *= value;
            else modularAdd += value;
            if (entry.hidden().isPresent()) modularHidden = entry.hidden().get();
        }

        private void apply(int i, TraitEntry entry, float[] add) {
            var value = entry.value(type);
            if (entry.operation(type) == TraitOperation.ADD) {
                add[i] += value;
                return;
            }
            multiply[i] = multiplied[i] ? multiply[i] * value : value;
            multiplied[i] = true;
        }

        private Trait trait(int size) {
            var count = 0;
            for (var flag : multiplied) if (flag) count++;
            var mul = count == 0 ? 1f : average(multiply, multiplied, count);
            var value = (average(materialAdd, null, size) + average(partAdd, null, size) + modularAdd) * mul * modularMultiply;
            var hidden = modularHidden != null ? modularHidden : partHidden != null ? partHidden : materialHidden != null ? materialHidden : type.hidden;
            return new Trait(type, value, hidden);
        }

        private float average(float[] values, boolean[] mask, int count) {
            if (count == 0) return 0f;
            var scale = type.partScale == 0 ? 1f : count * type.partScale + 1f - type.partScale;
            var sum = 0d;
            for (var i = 0; i < values.length; i++) if (mask == null || mask[i]) sum += (double) values[i] * scale;
            return (int) ((float) (sum / count) * 100f) / 100f;
        }
    }

    public static class Builder {
        private final List<Trait> traits;
        private final List<Modifier> modifiers = new ArrayList<>();
        public @Nullable EquipmentSlot equipmentSlot;
        public boolean tool, disablesShield, piglinNeutral, silent, gliding;
        public float maxDamage, enchantability, miningSpeed, miningTier, maxStackSize;
        public int blockCost = Integer.MAX_VALUE;
        public final List<MiningEffect> mining = new ArrayList<>();
        public final Set<ItemAbility> abilities = new HashSet<>();
        public final Set<TagKey<Item>> itemTags = new HashSet<>();
        public final List<TagKey<DamageType>> immunities = new ArrayList<>();
        public final List<DataComponentPatch> components = new ArrayList<>();
        public final List<Hook> useOn = new ArrayList<>(), blockBreak = new ArrayList<>(), blockDrops = new ArrayList<>(), hurtEnemy = new ArrayList<>(), inventoryTick = new ArrayList<>();

        private Builder(List<Trait> traits) {
            this.traits = traits;
            for (var trait : traits) for (var effect : trait.type().effects) effect.collect(trait, this);
        }

        public void attribute(Holder<Attribute> attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation, @Nullable EquipmentSlotGroup slot) {
            modifiers.add(new Modifier(attribute, id, amount, operation, slot));
        }

        public void useOn(Trait trait, TraitEffect effect) { useOn.add(new Hook(trait, effect)); }
        public void blockBreak(Trait trait, TraitEffect effect) { blockBreak.add(new Hook(trait, effect)); }
        public void blockDrops(Trait trait, TraitEffect effect) { blockDrops.add(new Hook(trait, effect)); }
        public void hurtEnemy(Trait trait, TraitEffect effect) { hurtEnemy.add(new Hook(trait, effect)); }
        public void inventoryTick(Trait trait, TraitEffect effect) { inventoryTick.add(new Hook(trait, effect)); }

        private Traits build() {
            return new Traits(this);
        }
    }
}
