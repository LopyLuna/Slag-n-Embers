package dev.lopyluna.slag.content.blocks.table;

import dev.lopyluna.slag.content.blocks.casting.CastingBE;
import dev.lopyluna.slag.content.blocks.casting.CastingInventory;
import dev.lopyluna.slag.content.items.dynamic_mold.DynamicMoldItem;
import dev.lopyluna.slag.register.AllDataComponents;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nonnull;

import java.util.Collections;

public class TableInventory extends CastingInventory {
    public TableInventory(TableBE be) {
        super(be, 2, 1, false);
    }

    @Override
    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        super.load(tag, registries);
        if (stacks.get(CastingBE.RESULT).getItem() instanceof DynamicMoldItem) Collections.swap(stacks, CastingBE.RESULT, TableBE.MOLD);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        if (slot == TableBE.MOLD) return stack.getItem() instanceof DynamicMoldItem && stack.has(AllDataComponents.CAST_TYPE);
        return super.isItemValid(slot, stack);
    }

    @Override
    public @Nonnull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return slot == TableBE.MOLD ? ItemStack.EMPTY : super.extractItem(slot, amount, simulate);
    }
}
