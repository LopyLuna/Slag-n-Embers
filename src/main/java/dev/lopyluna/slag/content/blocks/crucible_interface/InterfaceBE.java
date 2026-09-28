package dev.lopyluna.slag.content.blocks.crucible_interface;

import dev.lopyluna.slag.content.blocks.crucible_interface.client.InterfaceMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import javax.annotation.Nonnull;

import javax.annotation.Nullable;
import static dev.lopyluna.slag.content.blocks.crucible_interface.InterfaceBlock.FACING;

@SuppressWarnings("NullableProblems")
public class InterfaceBE extends BlockEntity implements MenuProvider {
    public int update = 0;
    public IFluidHandler targetCap = null;

    public InterfaceBE(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public @Nonnull Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, @Nonnull Inventory inventory, @Nonnull Player player) {
        update();
        return new InterfaceMenu(i, inventory, worldPosition);
    }

    @Override
    public void writeClientSideData(@Nonnull AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(worldPosition);
    }

    public void tick() {
        if (update > 1) update--;
        else if (update > 0) {
            update();
            update--;
        } else update = 240;
    }

    public void update() {
        if (level == null) return;
        var facing = getBlockState().getValue(FACING);
        var relPos = worldPosition.relative(facing.getOpposite());
        if (!level.isLoaded(relPos)) {
            targetCap = null;
            return;
        }
        targetCap = level.getCapability(Capabilities.FluidHandler.BLOCK, relPos, facing);
    }

    public IFluidHandler getTarget() {
        update();
        return targetCap;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void setChanged() {
        super.setChanged();
    }

    @Override
    public @Nonnull CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    protected void loadAdditional(@Nonnull CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        update = 8;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        update = 8;
    }
}
