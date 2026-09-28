package dev.lopyluna.slag.content.blocks.casting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import javax.annotation.Nonnull;

public record CastingInput(Fluid fluid) implements RecipeInput {
    @Override
    public boolean isEmpty() {
        return fluid == null || fluid == Fluids.EMPTY;
    }

    @Override
    public @Nonnull ItemStack getItem(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 1;
    }
}
