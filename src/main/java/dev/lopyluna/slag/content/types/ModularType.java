package dev.lopyluna.slag.content.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.AllUtils;
import dev.lopyluna.slag.content.items.dynamic_part.IDynamicPart;
import dev.lopyluna.slag.content.items.modular.actions.*;
import dev.lopyluna.slag.register.AllDataComponents;
import net.bettercombat.api.component.BetterCombatDataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class ModularType {
    public final ResourceLocation id;

    public final String modelType;
    public final int sortOrder;
    public final List<TagKey<Item>> segments;
    public final List<ItemStack> finalSegmentStacks;
    private final ItemStack resultStack;
    public final List<String> actions; //like "pickaxe" gives effects of an actual pickaxe or "shield" gives effects of an actual shield for example
    public final Optional<ResourceLocation> betterCombatPreset; // compat w/ better combat

    public final List<TagKey<Item>> itemTags;

    public boolean dontRegister;

    public static final Codec<ModularType> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(m -> m.id),
                    Codec.STRING.optionalFieldOf("model_type", "").forGetter(m -> m.modelType),
                    Codec.INT.fieldOf("sort_order").forGetter(m -> m.sortOrder),
                    TagKey.codec(Registries.ITEM).listOf().optionalFieldOf("segments", new ArrayList<>()).forGetter(m -> m.segments),
                    ItemStack.CODEC.listOf().optionalFieldOf("final_segment_stacks", new ArrayList<>()).forGetter(m -> m.finalSegmentStacks),
                    ItemStack.CODEC.optionalFieldOf("result_stack", ItemStack.EMPTY).forGetter(m -> m.resultStack),
                    Codec.STRING.listOf().optionalFieldOf("actions", new ArrayList<>()).forGetter(m -> m.actions),
                    TagKey.codec(Registries.ITEM).listOf().optionalFieldOf("item_tags", new ArrayList<>()).forGetter(m -> m.itemTags),
                    ResourceLocation.CODEC.optionalFieldOf("betterCombatPreset").forGetter(m -> m.betterCombatPreset)
            ).apply(instance, ModularType::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ModularType> STREAM_CODEC = StreamCodec.of(ModularType::toNetwork, ModularType::fromNetwork);

    @Override
    public int hashCode() {
        var actionHash = 0;
        if (actions != null && !actions.isEmpty()) actionHash = actions.hashCode();
        var resultStackHash = 0;
        if (resultStack != null) resultStackHash = resultStack.copy().hashCode();
        return 31 * id.hashCode() + 31 * actionHash + 31 * segmentHash() + 31 * finalSegmentStacksHash() + 31 * itemTagsHash() + 31 * resultStackHash + Objects.hash(modelType, sortOrder);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ModularType) obj;
        return this.id.equals(that.id) && this.modelType.equals(that.modelType) && this.sortOrder == that.sortOrder && equalSegment(that.segments) && equalFinalSegmentStacks(that.finalSegmentStacks) && equalItemTags(that.itemTags) && ItemStack.isSameItemSameComponents(this.resultStack, that.resultStack) && this.actions.equals(that.actions);
    }

    public ItemStack getResultStack() {
        return resultStack.copy();
    }

    public static ModularType fromNetwork(RegistryFriendlyByteBuf buf) {
        var id = ResourceLocation.STREAM_CODEC.decode(buf);
        var modelType = buf.readUtf();
        int sorting = buf.readInt();

        boolean hasSegments = buf.readBoolean();
        var segments = new ArrayList<TagKey<Item>>();
        if (hasSegments) {
            int segmentsSize = buf.readVarInt();
            if (segmentsSize > 0) for (int i = 0; i < segmentsSize; i++) segments.add(AllUtils.tagKeyStreamCodec(Registries.ITEM).decode(buf));
        }

        boolean hasActions = buf.readBoolean();
        var actions = new ArrayList<String>();
        if (hasActions) {
            int actionsSize = buf.readVarInt();
            if (actionsSize > 0) for (int i = 0; i < actionsSize; i++) actions.add(buf.readUtf());
        }

        boolean hasFinalSegmentStacks = buf.readBoolean();
        var finalSegmentStacks = new ArrayList<ItemStack>();
        if (hasFinalSegmentStacks) {
            int finalSegmentStacksSize = buf.readVarInt();
            if (finalSegmentStacksSize > 0) for (int i = 0; i < finalSegmentStacksSize; i++) finalSegmentStacks.add(ItemStack.STREAM_CODEC.decode(buf));
        }

        boolean hasResultStack = buf.readBoolean();
        ItemStack resultStack = hasResultStack ? ItemStack.STREAM_CODEC.decode(buf) : ItemStack.EMPTY;

        boolean hasItemTags = buf.readBoolean();
        var itemTags = new ArrayList<TagKey<Item>>();
        if (hasItemTags) {
            int itemTagsSize = buf.readVarInt();
            if (itemTagsSize > 0) for (int i = 0; i < itemTagsSize; i++) itemTags.add(AllUtils.tagKeyStreamCodec(Registries.ITEM).decode(buf));
        }

        boolean hasBetterCombatPreset = buf.readBoolean();
        Optional<ResourceLocation> betterCombatPreset = Optional.empty();
        if (hasBetterCombatPreset) {
            betterCombatPreset = Optional.of(ResourceLocation.STREAM_CODEC.decode(buf));
        }

        return new ModularType(id, modelType, sorting, segments, finalSegmentStacks, resultStack, actions, itemTags, betterCombatPreset);
    }

    public static void toNetwork(RegistryFriendlyByteBuf buf, ModularType type) {
        ResourceLocation.STREAM_CODEC.encode(buf, type.id);
        buf.writeUtf(type.modelType);
        buf.writeInt(type.sortOrder);

        buf.writeBoolean(type.segments != null && !type.segments.isEmpty());
        if (type.segments != null && !type.segments.isEmpty()) {
            buf.writeVarInt(type.segments.size());
            for (var segment : type.segments) AllUtils.tagKeyStreamCodec(Registries.ITEM).encode(buf, segment);
        }

        buf.writeBoolean(type.actions != null && !type.actions.isEmpty());
        if (type.actions != null && !type.actions.isEmpty()) {
            buf.writeVarInt(type.actions.size());
            for (var action : type.actions) buf.writeUtf(action);
        }

        buf.writeBoolean(type.finalSegmentStacks != null && !type.finalSegmentStacks.isEmpty());
        if (type.finalSegmentStacks != null && !type.finalSegmentStacks.isEmpty()) {
            buf.writeVarInt(type.finalSegmentStacks.size());
            for (var stack : type.finalSegmentStacks) ItemStack.STREAM_CODEC.encode(buf, stack);
        }

        buf.writeBoolean(type.resultStack != null && !type.resultStack.isEmpty());
        if (type.resultStack != null && !type.resultStack.isEmpty()) ItemStack.STREAM_CODEC.encode(buf, type.resultStack);

        buf.writeBoolean(type.itemTags != null && !type.itemTags.isEmpty());
        if (type.itemTags != null && !type.itemTags.isEmpty()) {
            buf.writeVarInt(type.itemTags.size());
            for (var tag : type.itemTags) AllUtils.tagKeyStreamCodec(Registries.ITEM).encode(buf, tag);
        }

        buf.writeBoolean(type.betterCombatPreset != null && type.betterCombatPreset.isPresent());
        if (type.betterCombatPreset != null && type.betterCombatPreset.isPresent()) {
            ResourceLocation.STREAM_CODEC.encode(buf, type.betterCombatPreset.get());
        }
    }

    private ModularType(ResourceLocation id, String modelType, int sortOrder, List<TagKey<Item>> segments, List<ItemStack> finalSegmentStacks, ItemStack resultStack, List<String> actions, List<TagKey<Item>> itemTags, Optional<ResourceLocation> betterCombatPreset) {
        dontRegister = id == null || id.getNamespace().isEmpty() || id.getPath().isEmpty() || id.getPath().equals("null") || id.getPath().equals("empty");
        this.id = id;
        this.modelType = modelType;
        this.sortOrder = sortOrder;
        this.segments = segments;
        this.finalSegmentStacks = finalSegmentStacks;
        this.resultStack = resultStack;
        this.actions = actions;
        this.itemTags = itemTags;
        this.betterCombatPreset = betterCombatPreset;
    }

    @SuppressWarnings("unused")
    public static class Builder {
        private final ResourceLocation id;
        private String modelType = "";
        private int sortOrder;
        private List<TagKey<Item>> segments = new ArrayList<>();
        private List<ItemStack> finalSegmentStacks = new ArrayList<>();
        private ItemStack resultStack;
        private List<String> actions = new ArrayList<>();
        private List<TagKey<Item>> itemTags = new ArrayList<>();
        private Optional<ResourceLocation> betterCombatPreset = Optional.empty();

        public Builder(ResourceLocation id) { this.id = id; }
        public Builder(String id) { this.id = SlagEmbers.loc(id); }

        public Builder modelType(String modelType) { this.modelType = modelType; return this; }

        public Builder sortOrder(int sorting) { this.sortOrder = sorting; return this; }

        public Builder segments(List<TagKey<Item>> segments) { this.segments = segments; return this; }
        @SafeVarargs public final Builder segments(TagKey<Item>... segments) { this.segments = List.of(segments); return this; }
        public Builder addSegment(TagKey<Item> segment) { this.segments.add(segment); return this; }
        public Builder addSegments(List<TagKey<Item>> segments) { this.segments.addAll(segments); return this; }
        @SafeVarargs public final Builder addSegments(TagKey<Item>... segments) { this.segments.addAll(List.of(segments)); return this; }


        public Builder rodCount(int rodCount) {
            return addSegmentStack(Items.STICK, rodCount);
        }

        public Builder segmentStacks(List<ItemStack> finalSegmentStacks) { this.finalSegmentStacks = finalSegmentStacks; return this; }
        public Builder segmentStacksFromItems(List<Item> finalSegmentStacks) { this.finalSegmentStacks = finalSegmentStacks.stream().map(ItemStack::new).toList(); return this; }
        public Builder segmentStacksFromItems(int count, List<Item> finalSegmentStacks) { this.finalSegmentStacks = finalSegmentStacks.stream().map(item -> new ItemStack(item, count)).toList(); return this; }

        public final Builder segmentStacks(ItemStack... finalSegmentStacks) { this.finalSegmentStacks = List.of(finalSegmentStacks); return this; }
        public final Builder segmentStacksFromItems(Item... finalSegmentStacks) { this.finalSegmentStacks = Stream.of(finalSegmentStacks).map(ItemStack::new).toList(); return this; }
        public final Builder segmentStacksFromItems(int count, Item... finalSegmentStacks) { this.finalSegmentStacks = Stream.of(finalSegmentStacks).map(item -> new ItemStack(item, count)).toList(); return this; }

        public Builder addSegmentStack(ItemStack finalSegmentStack) { this.finalSegmentStacks.add(finalSegmentStack); return this; }
        public Builder addSegmentStack(Item finalSegmentStack) { this.finalSegmentStacks.add(new ItemStack(finalSegmentStack)); return this; }
        public Builder addSegmentStack(Item finalSegmentStack, int count) { this.finalSegmentStacks.add(new ItemStack(finalSegmentStack, count)); return this; }

        public Builder addSegmentStacks(List<ItemStack> finalSegmentStacks) { this.finalSegmentStacks.addAll(finalSegmentStacks); return this; }
        public Builder addSegmentStacksFromItems(List<Item> finalSegmentStacks) { this.finalSegmentStacks.addAll(finalSegmentStacks.stream().map(ItemStack::new).toList()); return this; }
        public Builder addSegmentStacksFromItems(int count, List<Item> finalSegmentStacks) { this.finalSegmentStacks.addAll(finalSegmentStacks.stream().map(item -> new ItemStack(item, count)).toList()); return this; }

        public final Builder addSegmentStacks(ItemStack... finalSegmentStacks) { this.finalSegmentStacks.addAll(List.of(finalSegmentStacks)); return this; }
        public final Builder addSegmentStacksFromItems(Item... finalSegmentStacks) { this.finalSegmentStacks.addAll(Stream.of(finalSegmentStacks).map(ItemStack::new).toList()); return this; }
        public final Builder addSegmentStacksFromItems(int count, Item... finalSegmentStacks) { this.finalSegmentStacks.addAll(Stream.of(finalSegmentStacks).map(item -> new ItemStack(item, count)).toList()); return this; }

        public Builder resultStack(ItemStack resultStack) { this.resultStack = resultStack; return this; }
        public Builder resultStack(Item resultStack) { this.resultStack = new ItemStack(resultStack); return this; }
        public Builder resultStack(Item resultStack, int count) { this.resultStack = new ItemStack(resultStack, count); return this; }

        public Builder actions(List<String> actions) { this.actions = actions; return this; }
        public Builder actions(String... actions) { this.actions = List.of(actions); return this; }
        public Builder addAction(String action) { this.actions.add(action); return this; }
        public Builder addActions(List<String> actions) { this.actions.addAll(actions); return this; }
        public Builder addActions(String... actions) { this.actions.addAll(List.of(actions)); return this; }

        public Builder itemTags(List<TagKey<Item>> itemTags) { this.itemTags = itemTags; return this; }
        @SafeVarargs public final Builder itemTags(TagKey<Item>... itemTags) { this.itemTags = List.of(itemTags); return this; }
        public Builder addItemTag(TagKey<Item> itemTag) { this.itemTags.add(itemTag); return this; }
        public Builder addItemTags(List<TagKey<Item>> itemTags) { this.itemTags.addAll(itemTags); return this; }
        @SafeVarargs public final Builder addItemTags(TagKey<Item>... itemTags) { this.itemTags.addAll(List.of(itemTags)); return this; }

        public Builder betterCombatPreset(ResourceLocation id) { betterCombatPreset = Optional.of(id); return this; }
        public Builder betterCombatPreset(String id) { betterCombatPreset(ResourceLocation.read(id).getOrThrow()); return this; }

        public ModularType register() { return new ModularType(id, modelType, sortOrder, segments, finalSegmentStacks, resultStack == null ? ItemStack.EMPTY : resultStack, actions, itemTags, betterCombatPreset); }
    }

    @SuppressWarnings("unused")
    public static Object doAction(String action, String type, Object... args) {
        List<Object> argList = Arrays.stream(args).toList();

        return switch (action) {
            //Tools
            case "pickaxe" -> PickaxeActions.INSTANCE.doAction(type, argList);
            case "axe" -> AxeActions.INSTANCE.doAction(type, argList);
            case "shovel" -> ShovelActions.INSTANCE.doAction(type, argList);
            case "hoe" -> HoeActions.INSTANCE.doAction(type, argList);

            case "disableShield" -> DisableShieldAction.INSTANCE.doAction(type, argList);
            case "strip" -> StripBlockAction.INSTANCE.doAction(type, argList);
            case "plow" -> PlowBlockAction.INSTANCE.doAction(type, argList);
            case "till" -> TillBlockAction.INSTANCE.doAction(type, argList);

            //Tools Extra
            case "harvest" -> HarvestActions.INSTANCE.doAction(type, argList);
            case "scythe" -> "scythe"; //Args: onUse
            case "hammer" -> "hammer"; //Args: onBreak
            case "cutting" -> "cutting"; //Args: onUse
            case "vein" -> "vein"; //Args: onUse

            //Melee
            case "sword" -> SwordActions.INSTANCE.doAction(type, argList);
            case "mace" -> "mace"; //Args: onKnockback, onPostHurt

            //Defense
            case "shield" -> "shield"; //Args: onUse

            //Ranged
            case "bow" -> "bow"; //Args: onUse
            case "crossbow" -> "crossbow"; //Args: onUse
            case "throwing" -> "throwing"; //Args: onUse
            case "arrow" -> "arrow"; //Args: ammo

            //Ranged/Melee
            case "trident" -> "trident"; //Args: onUse
            case "spear" -> "spear"; //Args: onUse

            //Misc
            case "fishing_rod" -> "fishing_rod"; //Args: onUse
            case "shears" -> "shears"; //Args: onUse
            case "flint_and_steel" -> "flint_and_steel"; //Args: onUse
            case "bucket" -> "bucket"; //Args: onUse

            //Misc Extra
            case "wrench" -> "wrench"; //Args: onUse

            //Body
            case "glide" -> "glide"; //Args: onUse, onTick

            case "helmet" -> ArmorActions.INSTANCE.doAction(type, EquipmentSlot.HEAD, argList); //Args: onUse, equipable
            case "chestplate" -> ArmorActions.INSTANCE.doAction(type, EquipmentSlot.CHEST, argList); //Args: onUse, equipable
            case "leggings" -> ArmorActions.INSTANCE.doAction(type, EquipmentSlot.LEGS, argList); //Args: onUse, equipable
            case "boots" -> ArmorActions.INSTANCE.doAction(type, EquipmentSlot.FEET, argList); //Args: onUse, equipable

            //Curios
            case "head" -> "head"; //Args: onUse, onTick, equipable
            case "body" -> "body"; //Args: onUse, onTick, equipable
            case "gloves" -> "gloves"; //Args: onUse, onTick, equipable
            case "belt" -> "belt"; //Args: onUse, onTick, equipable
            case "foot" -> "foot"; //Args: onUse, onTick, equipable

            case "ring" -> "ring"; //Args: onUse, onTick, equipable
            case "bracelet" -> "bracelet"; //Args: onUse, onTick, equipable
            case "necklace" -> "necklace"; //Args: onUse, onTick, equipable

            case "charm" -> "charm"; //Args: onUse, onTick, equipable
            default -> null;
        };
    }

    public int segmentHash() {
        int hash = 0;
        if (segments != null && !segments.isEmpty()) {
            hash = segments.hashCode();
            for (var segment : segments) hash += segment.hashCode();
        }
        return hash;
    }

    public int finalSegmentStacksHash() {
        int hash = 0;
        if (finalSegmentStacks != null && !finalSegmentStacks.isEmpty()) {
            hash = finalSegmentStacks.hashCode();
            for (var stack : finalSegmentStacks) hash += stack.hashCode();
        }
        return hash;
    }

    public int itemTagsHash() {
        int hash = 0;
        if (itemTags != null && !itemTags.isEmpty()) {
            hash = itemTags.hashCode();
            for (var tag : itemTags) hash += tag.hashCode();
        }
        return hash;
    }

    public boolean equalSegment(List<TagKey<Item>> other) {
        if (this.segments == null || other == null || (this.segments.isEmpty() != other.isEmpty())) return false;
        for (var segment : other) if (!segments.contains(segment)) return false;
        return segments.size() == other.size();
    }

    public boolean equalFinalSegmentStacks(List<ItemStack> other) {
        if (this.finalSegmentStacks == null || other == null || (this.finalSegmentStacks.isEmpty() != other.isEmpty())) return false;
        for (var stack : other) if (!containsStack(stack, true)) return false;
        return finalSegmentStacks.size() == other.size();
    }

    public boolean equalItemTags(List<TagKey<Item>> other) {
        if (this.itemTags == null || other == null || (this.itemTags.isEmpty() != other.isEmpty())) return false;
        for (var tag : other) if (!itemTags.contains(tag)) return false;
        return itemTags.size() == other.size();
    }

    public boolean containsStack(ItemStack stack, boolean built) {
        if (stack == null || stack.isEmpty()) return false;
        var newStack = stack.copy();
        if (!built) newStack.remove(AllDataComponents.BUILT);
        for (var finalStack : finalSegmentStacks) if (ItemStack.isSameItemSameComponents(newStack, finalStack) && newStack.getCount() == finalStack.getCount()) return true;
        return false;
    }

    public boolean containsTag(ItemStack stack) {
        if (!(stack.getItem() instanceof IDynamicPart part)) return false;
        var partSegment = part.getPartSegment(stack);
        for (var segment : segments) if (partSegment.location().equals(segment.location())) return true;
        return false;
    }

    public boolean contains(ItemStack stack, boolean built) {
        if (containsTag(stack)) return true;
        return !(stack.getItem() instanceof IDynamicPart) && containsStack(stack, built);
    }

    public boolean containsItemTag(TagKey<Item> tag) {
        for (var itemTag : itemTags) if (itemTag.equals(tag) || itemTag.location().equals(tag.location())) return true;
        return false;
    }
}
