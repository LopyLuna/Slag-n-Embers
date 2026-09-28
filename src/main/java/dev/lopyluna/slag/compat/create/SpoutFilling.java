package dev.lopyluna.slag.compat.create;

import com.simibubi.create.api.behaviour.spouting.BlockSpoutingBehaviour;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;
import dev.lopyluna.slag.config.SlagCommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.function.BooleanSupplier;

public record SpoutFilling(BooleanSupplier enabled) implements BlockSpoutingBehaviour {
    public static final SpoutFilling TABLE = new SpoutFilling(SlagCommonConfigs.SPOUT_TABLE);
    public static final SpoutFilling BASIN = new SpoutFilling(SlagCommonConfigs.SPOUT_BASIN);

    public SpoutFilling(ModConfigSpec.BooleanValue config) {
        this(() -> SlagCommonConfigs.SPEC.isLoaded() && config.get());
    }

    @Override
    public int fillBlock(Level level, BlockPos pos, SpoutBlockEntity spout, FluidStack fluid, boolean simulate) {
        if (!enabled.getAsBoolean()) return 0;
        var handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, Direction.UP);
        if (handler == null) return 0;
        return handler.fill(fluid, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
    }
}
