package dev.lopyluna.slag.content.blocks.casting;

import dev.lopyluna.slag.content.blocks.DirtyInventory;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nonnull;

public class CastingInventory extends DirtyInventory<CastingBE> {
    public CastingInventory(CastingBE be, int slots, int stackSize, boolean stackNonStackables) {
        super(slots, be, stackSize, stackNonStackables);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return slot == CastingBE.RESULT && be.isCastItem(stack);
    }

    @Override
    public @Nonnull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return be.coolingProgress > 0 ? ItemStack.EMPTY : super.extractItem(slot, amount, simulate);
    }

    @Override
    public void setChanged() {
        be.setChanged();
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        be.updateRecipe = true;
        be.sendDataImmediately();
    }
}
