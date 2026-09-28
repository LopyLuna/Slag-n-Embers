package dev.lopyluna.slag.content.blocks.multiblock;

import dev.lopyluna.slag.content.blocks.multiblock.MultiQueue.Area;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public interface IMultiBlockEntityContainer {

    BlockPos getController();
    <T extends BlockEntity & IMultiBlockEntityContainer> T getControllerBE();
    boolean isController();
    void setController(BlockPos pos);
    void removeController();
    default void detachController() { removeController(); }
    BlockPos getLastKnownPos();

    void preventConnectivityUpdate();
    void notifyMultiUpdated();
    default void formed(LongList fresh, List<Area> absorbed) { notifyMultiUpdated(); }

    default void setExtraData(@Nullable Object data) {}
    @Nullable
    default Object getExtraData() { return null; }
    default Object modifyExtraData(Object data) { return data; }

    int getMaxLength(Direction.Axis longAxis, int width);
    int getMaxWidth();
    default int getMaxWidthX() { return getMaxWidth(); }
    default int getMaxWidthZ() { return getMaxWidth(); }

    int getHeight();
    void setHeight(int height);
    int getWidthX();
    void setWidthX(int width);
    int getWidthZ();
    void setWidthZ(int width);

    interface FluidMulti extends IMultiBlockEntityContainer {
        default boolean hasTank() { return false; }

        default int getTankSize() {	return 0; }

        default int getTotalTankSize() { return 0; }

        default void setTankSize(int blocks) {}

        default IFluidTank getTank() { return null; }

        default List<FluidStack> getFluids() {	return new ArrayList<>(); }

        default List<FluidStack> takeFluids() { return new ArrayList<>(); }

        default void giveFluids(List<FluidStack> fluids) {}
    }
}
