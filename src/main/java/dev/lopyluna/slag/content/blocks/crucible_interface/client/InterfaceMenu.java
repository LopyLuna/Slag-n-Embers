package dev.lopyluna.slag.content.blocks.crucible_interface.client;

import dev.lopyluna.slag.content.blocks.crucible.CrucibleTank;
import dev.lopyluna.slag.content.blocks.crucible_interface.InterfaceBE;
import dev.lopyluna.slag.content.blocks.multiblock.IMultiBlockEntityContainer;
import dev.lopyluna.slag.register.AllMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

    public BlockPos getControllerPos() {
        var s = level.getBlockState(this.pos);
        var facing = s.hasProperty(BlockStateProperties.HORIZONTAL_FACING) ? s.getValue(BlockStateProperties.HORIZONTAL_FACING) :
                s.hasProperty(BlockStateProperties.FACING) ? s.getValue(BlockStateProperties.FACING) : Direction.NORTH;
        var back = pos.relative(facing.getOpposite());
        var be = level.getBlockEntity(back);
        if (be instanceof IMultiBlockEntityContainer.FluidMulti mb && mb.getControllerBE() != null) return be.getBlockPos();
        return back;
    }

    public boolean isCrucible() {
        return getFluidHandler() instanceof CrucibleTank;
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
        if (handler instanceof CrucibleTank tank) list.addAll(tank.getFluidCopy());
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
