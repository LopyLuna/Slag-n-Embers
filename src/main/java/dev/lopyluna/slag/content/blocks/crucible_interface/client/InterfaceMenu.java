package dev.lopyluna.slag.content.blocks.crucible_interface.client;

import dev.lopyluna.slag.content.blocks.crucible.CrucibleBE;
import dev.lopyluna.slag.content.blocks.crucible_interface.InterfaceBE;
import dev.lopyluna.slag.register.AllMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class InterfaceMenu extends AbstractContainerMenu {
    protected final Player player;
    protected final Level level;
    protected final BlockPos pos;

    public InterfaceMenu(int windowId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(windowId, playerInventory, buf.readBlockPos());
    }

    public InterfaceMenu(int syncId, Inventory playerInventory, BlockPos pos) {
        super(AllMenuTypes.INTERFACE.get(), syncId);
        this.player = playerInventory.player;
        this.level = player.level();
        this.pos = pos;


        // Player inventory
        for (int m = 0; m < 3; ++m) for (int l = 0; l < 9; ++l) this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
        // Hotbar
        for (int m = 0; m < 9; ++m) this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
    }

    public @Nullable CrucibleBE getCrucible() {
        var s = level.getBlockState(pos);
        if (!s.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) return null;
        var back = pos.relative(s.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite());
        if (!level.isLoaded(back) || !(level.getBlockEntity(back) instanceof CrucibleBE cr)) return null;
        return cr.getControllerBE() instanceof CrucibleBE be && be.tankInventory != null ? be : null;
    }

    public boolean isCrucible() {
        return getCrucible() != null;
    }

    public IFluidHandler getFluidHandler() {
        if (level.getBlockEntity(pos) instanceof InterfaceBE be) return be.getTarget();
        return null;
    }

    public int getCapacity() {
        var handler = getFluidHandler();
        if (handler == null) return 0;
        var capacity = 0;
        for (int i = 0; handler.getTanks() > i; i++) capacity += handler.getTankCapacity(i);
        return capacity;
    }

    public int getAmount() {
        var handler = getFluidHandler();
        if (handler == null) return 0;
        var amount = 0;
        for (int i = 0; handler.getTanks() > i; i++) amount += handler.getFluidInTank(i).getAmount();
        return amount;
    }

    public List<FluidStack> getFluids() {
        var list = new ArrayList<FluidStack>();
        var handler = getFluidHandler();
        if (handler == null) return list;
        var crucible = getCrucible();
        if (crucible != null) list.addAll(crucible.tankInventory.getFluidCopy());
        else for (int i = 0; handler.getTanks() > i; i++) {
            var fluid = handler.getFluidInTank(i);
            if (fluid.isEmpty()) continue;
            list.add(fluid);
        }
        return list;
    }

    @Override
    public @Nonnull ItemStack quickMoveStack(@Nonnull Player player, int index) {
        var copy = ItemStack.EMPTY;
        var slot = this.slots.get(index);
        if (slot.hasItem()) {
            var stack = slot.getItem();
            copy = stack.copy();
            var size = this.slots.size();

            if (index >= 0 && index < 27) {
                if (!this.moveItemStackTo(stack, 27, size, false)) return ItemStack.EMPTY;
            } else if (index >= 27 && index < size && !this.moveItemStackTo(stack, 0, 27, false)) return ItemStack.EMPTY;

            if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();

            if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, stack);
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        if (!player.canInteractWithBlock(pos, 4.0)) return false;
        return level.getBlockEntity(pos) instanceof InterfaceBE be && !be.isRemoved();
    }
}
