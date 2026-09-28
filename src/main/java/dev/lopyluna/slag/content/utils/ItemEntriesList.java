package dev.lopyluna.slag.content.utils;

import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.Item;
import javax.annotation.Nonnull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

@SuppressWarnings("unused")
public class ItemEntriesList<T extends Item, L extends ListProvider> implements Iterable<ItemEntry<T>> {
    private final ItemEntry<?>[] values;

    public ItemEntriesList(List<L> list, Function<L, ItemEntry<T>> func) {
        values = new ItemEntry<?>[list.size()];
        for (var type : list) {
            int index = list.indexOf(type);
            if (index >= 0) values[index] = func.apply(type);
        }
    }

    @SuppressWarnings("unchecked")
    public ItemEntry<T> get(List<L> list, L type) {
        int index = list.indexOf(type);
        if (index >= 0) return (ItemEntry<T>) values[index];
        return null;
    }

    @SuppressWarnings("unchecked")
    public ItemEntry<T>[] toArray() {
        return (ItemEntry<T>[]) Arrays.copyOf(values, values.length);
    }

    @Override
    public @Nonnull Iterator<ItemEntry<T>> iterator() {
        return new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < values.length;
            }

            @SuppressWarnings("unchecked")
            @Override
            public ItemEntry<T> next() {
                if (!hasNext()) throw new NoSuchElementException();
                return (ItemEntry<T>) values[index++];
            }
        };
    }
}
