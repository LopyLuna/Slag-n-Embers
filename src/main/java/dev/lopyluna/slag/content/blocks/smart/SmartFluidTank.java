package dev.lopyluna.slag.content.blocks.smart;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import javax.annotation.Nonnull;

import java.util.function.Consumer;

public class SmartFluidTank extends FluidTank {

    private final Consumer<FluidStack> updateCallback;

    public SmartFluidTank(int capacity, Consumer<FluidStack> updateCallback) {
        super(capacity);
        this.updateCallback = updateCallback;
    }

    @Override
    protected void onContentsChanged() {
        super.onContentsChanged();
        updateCallback.accept(getFluid());
    }

    @Override
    public void setFluid(@Nonnull FluidStack stack) {
        super.setFluid(stack);
        updateCallback.accept(stack);
    }

}
