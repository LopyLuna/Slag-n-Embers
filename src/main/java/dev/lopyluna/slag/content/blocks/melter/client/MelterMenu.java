package dev.lopyluna.slag.content.blocks.melter.client;

import dev.lopyluna.slag.content.AllUtils;
import dev.lopyluna.slag.content.blocks.melter.MelterBE;
import dev.lopyluna.slag.register.AllMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Unit;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import javax.annotation.Nonnull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class MelterMenu extends AbstractContainerMenu {
    private final Container inventory; //input only
    protected final Player player;
    protected final Level level;
    protected final BlockPos pos;
    public final @Nullable MelterBE be;
    public final ContainerData data;
    public BlockState belowState;
    private ItemStack belowStack = ItemStack.EMPTY;

    public MelterMenu(int windowId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(windowId, playerInventory, new SimpleContainer(1), buf.readBlockPos(), new SimpleContainerData(3));
    }

    public MelterMenu(int syncId, Inventory playerInventory, Container inventory, BlockPos pos, ContainerData data) {
        super(AllMenuTypes.MELTER.get(), syncId);
        checkContainerSize(inventory, 1);
        checkContainerDataCount(data, 3);
        this.inventory = inventory;
        this.player = playerInventory.player;
        this.level = player.level();
        this.pos = pos;
        this.data = data;
        be = level.getBlockEntity(pos) instanceof MelterBE melter ? melter : null;

        this.addSlot(new Slot(inventory, 0, 56, 17));

        // Player inventory
        for (int m = 0; m < 3; ++m) for (int l = 0; l < 9; ++l) this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
        // Hotbar
        for (int m = 0; m < 9; ++m) this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));

        addDataSlots(data);
    }

    @Override
    public @Nonnull ItemStack quickMoveStack(Player player, int index) {
        var copy = ItemStack.EMPTY;
        var slot = this.slots.get(index);
        if (slot.hasItem()) {
            var stack = slot.getItem();
            copy = stack.copy();
            var size = this.slots.size();

            //if (!this.moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
            if (index != 0) {
                if (!this.moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
                else if (index >= 1 && index < 28) {
                    if (!this.moveItemStackTo(stack, 28, size, false)) return ItemStack.EMPTY;
                } else if (index >= 28 && index < size && !this.moveItemStackTo(stack, 1, 28, false)) return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(stack, 1, size, false)) return ItemStack.EMPTY;

            if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();

            if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, stack);
        }
        return copy;
    }

    public ItemStack getBelowStack() {
        var state = level.getBlockState(pos.below());
        if (state == belowState) return belowStack;
        belowState = state;
        belowStack = state.isAir() ? ItemStack.EMPTY : AllUtils.getStackFromBlock(state.getBlock(), false);
        if (belowStack.isEmpty()) return belowStack;
        belowStack.set(DataComponents.HIDE_TOOLTIP, Unit.INSTANCE);
        belowStack.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        return belowStack;
    }

    public List<FluidStack> getFluids() {
        if (be == null) return List.of();
        return be.getTankInventory().getFluids();
    }

    public int getCapacity() {
        if (be == null) return 0;
        return be.getTankInventory().getCapacity();
    }

    @Override
    public boolean stillValid(Player player) {
        return inventory.stillValid(player);
    }
}
