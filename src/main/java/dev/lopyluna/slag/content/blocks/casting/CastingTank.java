package dev.lopyluna.slag.content.blocks.casting;

import dev.lopyluna.slag.content.blocks.smart.SmartFluidTank;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

@SuppressWarnings("NullableProblems")
public class CastingTank extends SmartFluidTank {
    public final CastingBE be;

    public CastingTank(CastingBE be, int capacity) {
        super(capacity, be::onFluidChanged);
        this.be = be;
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        var handler = be.getHandler(resource.toStack(1));
        if (handler == null) return 0;
        capacity = handler.capacity();
        return super.insert(index, resource, amount, transaction);
    }
}
