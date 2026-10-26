package dev.lopyluna.slag.content.blocks.smart;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;

import java.util.function.Consumer;

@SuppressWarnings("NullableProblems")
public class SmartFluidTank extends FluidStacksResourceHandler {

    private final Consumer<FluidStack> updateCallback;

    public SmartFluidTank(int capacity, Consumer<FluidStack> updateCallback) {
        super(1, capacity);
        this.updateCallback = updateCallback;
    }

    @Override
    protected void onContentsChanged(int index, FluidStack previousContents) {
        updateCallback.accept(getFluid());
    }

    public FluidStack getFluid() {
        return stacks.getFirst();
    }

    public void setFluid(FluidStack stack) {
        set(0, FluidResource.of(stack), stack.getAmount());
    }

    public int getFluidAmount() {
        return getFluid().getAmount();
    }

    public boolean isEmpty() {
        return getFluid().isEmpty();
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void drain(int amount) {
        var fluid = getFluid();
        if (fluid.isEmpty() || amount <= 0) return;
        setFluid(fluid.copyWithAmount(Math.max(0, fluid.getAmount() - amount)));
    }
}
