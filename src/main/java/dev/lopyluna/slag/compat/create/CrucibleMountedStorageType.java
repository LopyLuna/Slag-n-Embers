package dev.lopyluna.slag.compat.create;

import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType;
import dev.lopyluna.slag.content.blocks.multiblock.FluidMultiBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class CrucibleMountedStorageType extends MountedFluidStorageType<CrucibleMountedStorage> {
    public CrucibleMountedStorageType() {
        super(CrucibleMountedStorage.CODEC);
    }

    @Override
    public @Nullable CrucibleMountedStorage mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        if (!(be instanceof FluidMultiBlockEntity crucible) || !crucible.isController) return null;
        return new CrucibleMountedStorage(crucible.tankInventory.getCapacity(), crucible.tankInventory.getFluidCopy());
    }
}
