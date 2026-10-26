package dev.lopyluna.slag.content.blocks;

import dev.lopyluna.slag.content.blocks.smart.SyncedBlockEntity;
import dev.lopyluna.slag.content.utils.NBTHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public abstract class DirtyInventory<BE extends SyncedBlockEntity> extends SnapshotJournal<NonNullList<ItemStack>> implements Container, ResourceHandler<ItemResource> {
    public NonNullList<ItemStack> stacks;
    public BE be;
    public boolean extractionAllowed;
    public boolean insertionAllowed;
    public boolean stackNonStackables;
    public int stackSize;

    public DirtyInventory(int slots, BE be, int stackSize, boolean stackNonStackables) {
        this.stacks = NonNullList.withSize(slots, ItemStack.EMPTY);
        this.be = be;
        this.stackNonStackables = stackNonStackables;
        insertionAllowed = true;
        extractionAllowed = true;
        this.stackSize = stackSize;
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        Collections.fill(stacks, ItemStack.EMPTY);
        for (var entry : tag.getListOrEmpty("Items")) {
            if (!(entry instanceof CompoundTag item)) continue;
            var slot = item.getByteOr("Slot", (byte) 0) & 255;
            if (slot < stacks.size()) stacks.set(slot, NBTHelper.readItem(item, registries));
        }
    }

    public void save(CompoundTag tag, HolderLookup.Provider registries) {
        var list = new ListTag();
        for (var i = 0; i < stacks.size(); i++) {
            var stack = stacks.get(i);
            if (stack.isEmpty() || !(NBTHelper.writeItem(stack, registries) instanceof CompoundTag item)) continue;
            item.putByte("Slot", (byte) i);
            list.add(item);
        }
        tag.put("Items", list);
    }

    @Override
    public int getContainerSize() {
        return stacks.size();
    }

    @Override
    public boolean isEmpty() {
        for (var stack : stacks) if (!stack.isEmpty()) return false;
        return true;
    }

    public ItemStack getFirstItem() {
        for (var s : stacks) if (!s.isEmpty()) return s;
        return ItemStack.EMPTY;
    }

    @Override
    public @Nonnull ItemStack getItem(int slot) {
        return stacks.get(slot);
    }

    @Override
    public @Nonnull ItemStack removeItem(int slot, int amount) {
        var stack = ContainerHelper.removeItem(stacks, slot, amount);
        if (!stack.isEmpty()) {
            setChanged();
            onContentsChanged(slot);
        }
        return stack;
    }

    @Override
    public @Nonnull ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(stacks, slot);
    }

    @Override
    public void setItem(int slot, @Nonnull ItemStack stack) {
        stacks.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        setChanged();
        onContentsChanged(slot);
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return Container.stillValidBlockEntity(be, player);
    }

    @Override
    public void clearContent() {
        stacks.clear();
    }

    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        validateSlotIndex(slot);
        setItem(slot, stack);
    }

    public int getSlots() {
        return getContainerSize();
    }

    public @Nonnull ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot);
        return getItem(slot);
    }

    @Override
    public int size() {
        return stacks.size();
    }

    @Override
    public @Nonnull ItemResource getResource(int index) {
        return ItemResource.of(getStackInSlot(index));
    }

    @Override
    public long getAmountAsLong(int index) {
        return getStackInSlot(index).getCount();
    }

    @Override
    public long getCapacityAsLong(int index, @Nonnull ItemResource resource) {
        return resource.isEmpty() ? getSlotLimit(index) : getStackLimit(index, resource.toStack());
    }

    @Override
    public boolean isValid(int index, @Nonnull ItemResource resource) {
        return !resource.isEmpty() && isItemValid(index, resource.toStack());
    }

    @Override
    public int insert(int index, @Nonnull ItemResource resource, int amount, @Nonnull TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        var inserted = amount - insertItem(index, resource.toStack(amount), true).getCount();
        if (inserted <= 0) return 0;
        updateSnapshots(transaction);
        var existing = stacks.get(index);
        stacks.set(index, existing.isEmpty() ? resource.toStack(inserted) : existing.copyWithCount(existing.getCount() + inserted));
        return inserted;
    }

    @Override
    public int extract(int index, @Nonnull ItemResource resource, int amount, @Nonnull TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        var existing = getStackInSlot(index);
        if (!resource.matches(existing)) return 0;
        var extracted = extractItem(index, amount, true).getCount();
        if (extracted <= 0) return 0;
        updateSnapshots(transaction);
        stacks.set(index, existing.getCount() <= extracted ? ItemStack.EMPTY : existing.copyWithCount(existing.getCount() - extracted));
        return extracted;
    }

    @Override
    protected @Nonnull NonNullList<ItemStack> createSnapshot() {
        var copy = NonNullList.withSize(stacks.size(), ItemStack.EMPTY);
        for (var i = 0; i < stacks.size(); i++) copy.set(i, stacks.get(i).copy());
        return copy;
    }

    @Override
    protected void revertToSnapshot(@Nonnull NonNullList<ItemStack> snapshot) {
        for (var i = 0; i < snapshot.size(); i++) stacks.set(i, snapshot.get(i));
    }

    @Override
    protected void onRootCommit(@Nonnull NonNullList<ItemStack> original) {
        setChanged();
        for (var i = 0; i < original.size(); i++) if (!ItemStack.matches(original.get(i), stacks.get(i))) onContentsChanged(i);
    }

    public @Nonnull ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (!insertionAllowed) return stack;
        if (stack.isEmpty()) return ItemStack.EMPTY;
        else if (!isItemValid(slot, stack)) return stack;
        else {
            validateSlotIndex(slot);
            var existing = getItem(slot);
            int limit = getStackLimit(slot, stack);
            if (!existing.isEmpty()) {
                if (!ItemStack.isSameItemSameComponents(stack, existing)) return stack;
                limit -= existing.getCount();
            }

            if (limit <= 0) return stack;
            else {
                var reachedLimit = stack.getCount() > limit;
                if (!simulate) {
                    if (existing.isEmpty()) setItem(slot, reachedLimit ? stack.copyWithCount(limit) : stack);
                    else existing.grow(reachedLimit ? limit : stack.getCount());
                    onContentsChanged(slot);
                }
                return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
            }
        }
    }

    public @Nonnull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!extractionAllowed) return ItemStack.EMPTY;
        if (stackNonStackables) {
            var extractItem = extractingItem(slot, amount, true);
            if (!extractItem.isEmpty() && extractItem.getOrDefault(DataComponents.MAX_STACK_SIZE, 64) < extractItem.getCount()) amount = extractItem.getOrDefault(DataComponents.MAX_STACK_SIZE, 64);
        }
        return extractingItem(slot, amount, simulate);
    }

    protected ItemStack extractingItem(int slot, int amount, boolean simulate) {
        if (amount == 0) return ItemStack.EMPTY;
        else {
            validateSlotIndex(slot);
            var existing = this.stacks.get(slot);
            if (existing.isEmpty()) return ItemStack.EMPTY;
            else {
                int toExtract = Math.min(amount, existing.getMaxStackSize());
                if (existing.getCount() <= toExtract) {
                    if (!simulate) {
                        setItem(slot, ItemStack.EMPTY);
                        onContentsChanged(slot);
                        return existing;
                    } else return existing.copy();
                } else {
                    if (!simulate) {
                        setItem(slot, existing.copyWithCount(existing.getCount() - toExtract));
                        onContentsChanged(slot);
                    }
                    return existing.copyWithCount(toExtract);
                }
            }
        }
    }

    private Consumer<Integer> updateCallback;

    @SuppressWarnings("unused")
    public void whenContentsChange(Consumer<Integer> updateCallback) {
        this.updateCallback = updateCallback;
    }

    protected void onContentsChanged(int slot) {
        if (updateCallback != null) updateCallback.accept(slot);
        be.notifyUpdate();
    }


    public int getSlotLimit(int slot) {
        return Math.min(stackNonStackables ? 64 : 99, stackSize);
    }

    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return true;
    }

    public int getStackLimit(int slot, @Nonnull ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getOrDefault(DataComponents.MAX_STACK_SIZE, 64));
    }

    protected void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= this.stacks.size()) throw new RuntimeException("Slot " + slot + " not in valid range - [0," + this.stacks.size() + ")");
    }

    public NonNullList<ItemStack> getItems() {
        return stacks;
    }
}
