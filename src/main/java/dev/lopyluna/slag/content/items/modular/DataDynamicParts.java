package dev.lopyluna.slag.content.items.modular;

import com.mojang.serialization.Codec;
import dev.lopyluna.slag.content.items.dynamic_part.IDynamicPart;
import dev.lopyluna.slag.content.traits.Traits;
import dev.lopyluna.slag.content.types.Incompatible;
import dev.lopyluna.slag.content.types.MaterialType;
import dev.lopyluna.slag.content.types.ModularType;
import dev.lopyluna.slag.register.AllDataComponents;
import dev.lopyluna.slag.register.AllDynamicTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

import static net.minecraft.world.item.ItemStack.isSameItemSameComponents;

@SuppressWarnings({"unused"})
public class DataDynamicParts implements TooltipComponent {
    public static final DataDynamicParts EMPTY = new DataDynamicParts(List.of());

    public static final Codec<DataDynamicParts> CODEC = ItemStack.CODEC.listOf().xmap(DataDynamicParts::new, parts -> parts.items);
    public static final StreamCodec<RegistryFriendlyByteBuf, DataDynamicParts> STREAM_CODEC =
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).map(DataDynamicParts::new, parts -> parts.items);

    public final List<ItemStack> items;
    private volatile Cache cache;
    private volatile Possible possible;

    public DataDynamicParts(List<ItemStack> items) {
        this.items = items == null ? List.of() : List.copyOf(items);
    }

    public @Nullable ModularType getModularType() {
        return cache().modular;
    }

    public List<MaterialType> getMaterialTypes() {
        return cache().materials;
    }

    public Traits getTraits() {
        return cache().traits;
    }

    private Cache cache() {
        var version = AllDynamicTypes.version;
        var cache = this.cache;
        if (cache != null && cache.version == version) return cache;
        var modular = findModularType();
        return this.cache = new Cache(version, modular, findMaterialTypes(), Traits.resolve(items, modular));
    }

    public ItemStack getItem(Item item) {
        for (var itemstack : this.items) if (itemstack.is(item)) return itemstack;
        return ItemStack.EMPTY;
    }

    public boolean contains(Item item) {
        for (var itemstack : this.items) if (itemstack.is(item)) return true;
        return false;
    }
    public boolean contains(ItemStack stack) {
        for (var itemstack : this.items) if (isSameItemSameComponents(itemstack, stack)) return true;
        return false;
    }


    public boolean containsDynamicPartSegment(IDynamicPart part, ItemStack stack) {
        return containsDynamicPartSegment(part.getPartSegment(stack));
    }
    public boolean containsDynamicPartSegment(TagKey<Item> part) {
        for (var itemstack : this.items) if (itemstack.getItem() instanceof IDynamicPart dynamic && dynamic.getPartSegment(itemstack).equals(part)) return true;
        return false;
    }

    public boolean hasAllDynamicPartSegments(List<TagKey<Item>> required) {
        if (required == null) return false;
        var have = new ArrayList<TagKey<Item>>();
        for (var s : this.items) {
            if (!(s.getItem() instanceof IDynamicPart p)) continue;
            var seg = p.getPartSegment(s);
            if (seg == null) return false;
            have.add(seg);
        }
        return containsAll(have, required);
    }

    public ItemStack getItemUnsafe(int index) {
        return this.items.get(index);
    }
    public Stream<ItemStack> itemCopyStream() {
        return this.items.stream().map(ItemStack::copy);
    }
    public List<ItemStack> items() {
        return this.items;
    }
    public List<ItemStack> itemsCopy() {
        if (isEmpty()) return new ArrayList<>();
        List<ItemStack> items = new ArrayList<>();
        for (var stack : this.items) items.add(stack.copy());
        return items;
    }
    public List<ItemStack> getAllNonDynamicParts() {
        if (isEmpty()) return new ArrayList<>();
        List<ItemStack> items = new ArrayList<>();
        for (var stack : this.items) {
            if (stack.getItem() instanceof IDynamicPart) continue;
            items.add(stack.copy());
        }
        return items;
    }
    public List<ItemStack> getAllDynamicParts() {
        if (isEmpty()) return new ArrayList<>();
        List<ItemStack> items = new ArrayList<>();
        for (var stack : this.items) {
            if (!(stack.getItem() instanceof IDynamicPart)) continue;
            items.add(stack.copy());
        }
        return items;
    }
    public List<TagKey<Item>> getAllDynamicPartSegments() {
        if (isEmpty()) return new ArrayList<>();
        List<TagKey<Item>> items = new ArrayList<>();
        for (var stack : this.items) {
            if (!(stack.getItem() instanceof IDynamicPart p)) continue;
            var seg = p.getPartSegment(stack);
            if (seg == null) continue;
            items.add(seg);
        }
        return items;
    }
    public List<ItemStack> itemCopyRandom(Random random) {
        if (isEmpty()) return new ArrayList<>();
        List<ItemStack> items = itemsCopy();
        if (random == null) {
            var stick = ItemStack.EMPTY;
            int i = 0;
            for (var stack : items) {
                if (stack.is(Items.STICK)) {
                    stick = stack;
                    items.remove(i);
                    break;
                }
                i++;
            }
            if (!stick.isEmpty()) items.addFirst(stick);
            return items;
        }
        Collections.shuffle(items, random);
        var stick = ItemStack.EMPTY;
        int i = 0;
        for (var stack : items) {
            if (stack.is(Items.STICK)) {
                stick = stack;
                items.remove(i);
                break;
            }
            i++;
        }
        if (!stick.isEmpty()) items.addFirst(stick);
        return items;
    }

    public int size() {
        return this.items.size();
    }
    public boolean isEmpty() {
        return this.items == null || this.items.isEmpty();
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof DataDynamicParts parts && itemMatches(this.items, parts.items));
    }

    public static boolean itemMatches(List<ItemStack> list, List<ItemStack> other) {
        var size = list.size();
        if (size != other.size()) return false;
        for (var i = 0; i < size; i++) if (!ItemStack.matches(list.get(i), other.get(i))) return false;
        return true;
    }

    @Override
    public int hashCode() {
        var hash = 0;
        for (var stack : items) hash += ItemStack.hashItemAndComponents(stack) * 31 + stack.getCount();
        return hash;
    }

    @Override
    public String toString() {
        return "ToolParts" + this.items;
    }

    public int getLargestPossibleCount(ItemStack target, List<ItemStack> list) {
        var targetItem = target.getItem();
        var maxCount = target.getCount();
        var best = 0;
        for (var stack : list) {
            if (stack.getItem() != targetItem) continue;
            var count = stack.getCount();
            if (maxCount >= count && count > best) best = count;
        }
        return best;
    }

    public List<ItemStack> getPossibleStacks(List<Object> list) {
        return list.stream().filter(o -> o instanceof ItemStack).map(o -> (ItemStack) o).toList();
    }

    @SuppressWarnings("unchecked")
    public List<TagKey<Item>> getPossibleTags(List<Object> list) {
        return list.stream().filter(o -> o instanceof TagKey).map(o -> (TagKey<Item>) o).toList();
    }

    public List<Object> getPossibleParts() {
        return possible().parts;
    }

    public List<ModularType> getPossibleModulars() {
        return possible().modulars;
    }

    public List<ModularType> getConstructibleModulars() {
        var result = new ArrayList<ModularType>();
        if (isEmpty()) return result;
        for (var modular : AllDynamicTypes.getAllModulars()) if (match(modular) != null) result.add(modular);
        result.sort(Comparator.comparingInt((ModularType modular) -> modular.sortOrder).thenComparing(modular -> modular.id.toString()));
        return result;
    }

    public @Nullable Match match(ModularType modular) {
        var used = new ArrayList<ItemStack>();
        var order = new ArrayList<Integer>();
        var left = itemsCopy();
        var source = new ArrayList<Integer>();
        for (var i = 0; i < left.size(); i++) source.add(i);
        for (var segment : modular.finalSegmentStacks) {
            var target = unbuilt(segment);
            var found = -1;
            for (var i = 0; i < left.size(); i++) {
                var stack = unbuilt(left.get(i));
                if (ItemStack.isSameItemSameComponents(stack, target) && stack.getCount() >= target.getCount()) {
                    found = i;
                    break;
                }
            }
            if (found < 0) return null;
            var stack = left.get(found);
            used.add(stack.copyWithCount(target.getCount()));
            order.add(source.get(found));
            stack.shrink(target.getCount());
            if (stack.isEmpty()) {
                left.remove(found);
                source.remove(found);
            }
        }
        for (var segment : modular.segments) {
            var found = -1;
            for (var i = 0; i < left.size(); i++) {
                var stack = left.get(i);
                if (stack.getItem() instanceof IDynamicPart part ? part.getPartSegment(stack).equals(segment) : stack.is(segment)) {
                    found = i;
                    break;
                }
            }
            if (found < 0) return null;
            used.add(left.remove(found));
            order.add(source.remove(found));
        }
        var indices = new ArrayList<Integer>();
        for (var i = 0; i < used.size(); i++) indices.add(i);
        indices.sort(Comparator.comparingInt(order::get));
        var sorted = new ArrayList<ItemStack>();
        for (var i : indices) sorted.add(used.get(i));
        return Incompatible.compatible(sorted, modular) ? new Match(List.copyOf(sorted), List.copyOf(left)) : null;
    }

    public record Match(List<ItemStack> used, List<ItemStack> leftover) {}

    private Possible possible() {
        var version = AllDynamicTypes.version;
        var possible = this.possible;
        if (possible != null && possible.version == version) return possible;
        return this.possible = new Possible(version, List.copyOf(findPossibleParts()), List.copyOf(findPossibleModulars()));
    }

    private List<Object> findPossibleParts() {
        var result = new ArrayList<>();
        var modulars = AllDynamicTypes.getAllModulars();

        if (this.isEmpty()) {
            for (var modular : modulars) {
                for (var sStack : modular.finalSegmentStacks) {
                    var stack = unbuilt(sStack);
                    if (!contains(result, stack)) result.add(stack);
                }
                for (var tag : modular.segments) if (!contains(result, tag)) result.add(tag);
            }
            return result;
        }

        for (var modular : modulars) {
            var missingParts = getMissingParts(modular);
            if (missingParts != null) for (var part : missingParts) if (!contains(result, part)) result.add(part);
        }

        return result;
    }

    private List<ModularType> findPossibleModulars() {
        var modulars = AllDynamicTypes.getAllModulars();
        var result = new ArrayList<ModularType>();

        if (this.isEmpty()) {
            for (var modular : modulars) if (modularExcluded(result, modular)) result.add(modular);
            return result;
        } else for (var modular : getPotentialModulars()) if (modularExcluded(result, modular)) result.add(modular);
        return result;
    }

    private List<ModularType> getPotentialModulars() {
        var modulars = AllDynamicTypes.getAllModulars();
        var potential = new ArrayList<ModularType>();
        var ignore = new ArrayList<ModularType>();

        for (var modular : modulars) for (var item : this.items) if (!modular.contains(item, false)) ignore.add(modular);
        for (var modular : modulars) if (!ignore.contains(modular) && Incompatible.compatible(items, modular)) potential.add(modular);

        return potential;
    }

    private List<Object> getMissingParts(ModularType modular) {
        var missing = new ArrayList<>();
        var matched = new ArrayList<>();

        for (var item : this.items) if (!modular.contains(item, false)) return null;
        if (!Incompatible.compatible(items, modular)) return null;

        for (var sStack : modular.finalSegmentStacks) {
            var stack = unbuilt(sStack);
            if (containsInItems(stack)) matched.add(stack);
            else missing.add(stack);
        }

        for (var tag : modular.segments) {
            if (containsTagInItems(tag)) matched.add(tag);
            else missing.add(tag);
        }

        if (matched.isEmpty()) return null;
        return missing;
    }

    // Helpers
    // =====================================================================================================================================================================================

    private @Nullable ModularType findModularType() {
        if (isEmpty()) return null;
        var tags = getAllDynamicPartSegments();
        var stacks = getAllNonDynamicParts();
        for (var modular : AllDynamicTypes.getAllModulars()) if (containsExactlyAllStacks(stacks, modular.finalSegmentStacks) && containsExactlyAllTags(tags, modular.segments) && Incompatible.compatible(items, modular)) return modular;
        return null;
    }

    private List<MaterialType> findMaterialTypes() {
        var types = new ArrayList<MaterialType>();
        for (var stack : items) if (stack.getItem() instanceof IDynamicPart part) {
            var type = part.getMaterialType(stack).orElse(null);
            if (type != null && !types.contains(type)) types.add(type);
        }
        return List.copyOf(types);
    }

    private static boolean containsExactlyAllStacks(List<ItemStack> stacks, List<ItemStack> others) {
        if (stacks.size() != others.size()) return false;
        for (var stack : stacks) {
            var copy = unbuilt(stack);
            if (others.stream().noneMatch(other -> ItemStack.matches(copy, unbuilt(other)))) return false;
        }
        return true;
    }

    private static boolean containsExactlyAllTags(List<TagKey<Item>> tags, List<TagKey<Item>> others) {
        if (tags.size() != others.size()) return false;
        for (var tag : tags) if (others.stream().noneMatch(other -> tag.location().equals(other.location()) && tag.registry().location().equals(other.registry().location()))) return false;
        return true;
    }

    private static ItemStack unbuilt(ItemStack stack) {
        var copy = stack.copy();
        copy.remove(AllDataComponents.BUILT);
        return copy;
    }

    private boolean containsInItems(ItemStack stack) {
        for (var item : this.items) if (ItemStack.isSameItemSameComponents(item, stack)) return true;
        return false;
    }

    private boolean containsTagInItems(TagKey<Item> tag) {
        for (var item : this.items) {
            if (item.getItem() instanceof IDynamicPart part && part.getPartSegment(item).equals(tag)) return true;
            if (item.is(tag)) return true;
        }
        return false;
    }

    private static boolean modularExcluded(List<ModularType> modulars, ModularType modular) {
        for (var m : modulars) if (m.equals(modular)) return false;
        return true;
    }

    public static boolean containsAll(List<?> list, List<?> other) {
        if (list == null || other == null) return false;
        if (list.isEmpty() || other.isEmpty()) return false;
        if (list.size() != other.size()) return false;
        for (var o : other) if (!contains(list, o)) return false;
        return true;
    }

    @SuppressWarnings("unchecked")
    public static boolean contains(List<?> list, Object other) {
        for (var o : list) {
            if (o instanceof ItemStack s && other instanceof ItemStack oStack) if (ItemStack.isSameItemSameComponents(s, oStack) && s.getCount() == oStack.getCount()) return true;
            if (o instanceof TagKey<?> t && other instanceof TagKey<?> oTag) if (t.equals(oTag)) return true;
            if (o instanceof ItemStack s && other instanceof TagKey<?> oTag) if (oTag.isFor(Registries.ITEM) && s.is((TagKey<Item>) oTag)) return true;
            if (o.equals(other)) return true;
        }
        return false;
    }

    private record Cache(int version, @Nullable ModularType modular, List<MaterialType> materials, Traits traits) {}

    private record Possible(int version, List<Object> parts, List<ModularType> modulars) {}
}
