package dev.lopyluna.slag.content.blocks.crucible_interface;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lopyluna.slag.client.ClientUtils;
import dev.lopyluna.slag.content.blocks.crucible.CrucibleTank;
import dev.lopyluna.slag.content.blocks.multiblock.renderer.SafeBlockEntityRenderer;
import dev.lopyluna.slag.content.utils.ShapeUtils;
import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

@SuppressWarnings("unused")
public class InterfaceRenderer extends SafeBlockEntityRenderer<InterfaceBE> {
    public static final VoxelShaper FLUID = ShapeUtils.shape(-0.01, 7, 7.99, 16.009999999999998, 10, 9).forDirectional(Direction.NORTH);

    public InterfaceRenderer(BlockEntityRendererProvider.Context context) {}
    @Override
    protected void renderSafe(InterfaceBE be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        var level = be.getLevel();
        if (level == null) return;
        var pos = be.getBlockPos();
        var fluidStack = getFluid(level, pos);
        if (fluidStack == null || fluidStack.isEmpty()) return;
        var state = be.getBlockState();
        Direction facing = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) ? state.getValue(BlockStateProperties.HORIZONTAL_FACING) : state.hasProperty(BlockStateProperties.FACING) ? state.getValue(BlockStateProperties.FACING) : Direction.NORTH;

        var shape = FLUID.get(facing);

        ms.pushPose();
        ClientUtils.renderFluidShape(fluidStack, shape, buffer, ms, light, true, true);
        ms.popPose();
    }

    public IFluidHandler getFluidHandler(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof InterfaceBE be) return be.getTarget();
        return null;
    }
    public FluidStack getFluid(Level level, BlockPos pos) {
        var handler = getFluidHandler(level, pos);
        if (handler == null) return FluidStack.EMPTY;
        if (handler instanceof CrucibleTank tank) return tank.getFluid();
        else for (int i = 0; handler.getTanks() > i; i++) {
            var fluid = handler.getFluidInTank(i);
            if (fluid.isEmpty()) continue;
            return fluid;
        }
        return FluidStack.EMPTY;
    }
}
