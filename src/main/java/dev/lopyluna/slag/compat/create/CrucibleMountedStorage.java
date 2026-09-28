package dev.lopyluna.slag.compat.create;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.api.contraption.storage.SyncedMountedStorage;
import com.simibubi.create.api.contraption.storage.fluid.WrapperMountedFluidStorage;
import com.simibubi.create.content.contraptions.Contraption;
import dev.lopyluna.slag.content.blocks.crucible.CrucibleTank;
import dev.lopyluna.slag.content.blocks.multiblock.FluidMultiBlockEntity;
import dev.lopyluna.slag.content.blocks.multiblock.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.List;

public class CrucibleMountedStorage extends WrapperMountedFluidStorage<CrucibleMountedStorage.Tank> implements SyncedMountedStorage {
    public static final MapCodec<CrucibleMountedStorage> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("capacity").forGetter(s -> s.wrapped.getCapacity()),
            FluidStack.OPTIONAL_CODEC.listOf().fieldOf("fluids").forGetter(s -> s.wrapped.getFluidCopy())
    ).apply(i, CrucibleMountedStorage::new));

    private boolean dirty;

    public CrucibleMountedStorage(int capacity, List<FluidStack> fluids) {
        super(CreateCompat.CRUCIBLE.get(), new Tank(capacity, fluids));
        wrapped.onChange = () -> dirty = true;
    }

    @Override
    public void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        if (be instanceof FluidMultiBlockEntity crucible && crucible.isController) crucible.tankInventory.setFluids(wrapped.getFluidCopy());
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void markClean() {
        dirty = false;
    }

    @Override
    public void afterSync(Contraption contraption, BlockPos localPos) {
        if (!(contraption.getBlockEntityClientSide(localPos) instanceof FluidMultiBlockEntity be)) return;
        be.tankInventory.setFluids(wrapped.getFluidCopy());
        be.setFluidLevel(LerpedFloat.linear().startWithValue(be.getFillState()));
    }

    public static class Tank extends CrucibleTank {
        private Runnable onChange;

        public Tank(int capacity, List<FluidStack> fluids) {
            super(capacity, list -> {});
            setFluids(fluids);
        }

        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            if (onChange != null) onChange.run();
        }
    }
}
