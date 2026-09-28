package dev.lopyluna.slag.content.blocks.casting;

import dev.lopyluna.slag.content.blocks.smart.SmartFluidTank;
import net.neoforged.neoforge.fluids.FluidStack;
import javax.annotation.Nonnull;

public class CastingTank extends SmartFluidTank {
    public final CastingBE be;

    public CastingTank(CastingBE be, int capacity) {
        super(capacity, be::onFluidChanged);
        this.be = be;
    }

    @Override
    public int fill(@Nonnull FluidStack resource, @Nonnull FluidAction action) {
        var handler = be.getHandler(resource);
        if (handler == null || fluid.getAmount() >= handler.capacity()) return 0;
        capacity = handler.capacity();
        return super.fill(resource, action);
    }
}
