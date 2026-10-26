package dev.lopyluna.slag.content.blocks.crucible_interface;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lopyluna.slag.client.ClientUtils;
import dev.lopyluna.slag.content.AllUtils;
import dev.lopyluna.slag.content.blocks.multiblock.renderer.SafeBlockEntityRenderer;
import dev.lopyluna.slag.content.utils.ShapeUtils;
import net.createmod.catnip.api.math.VoxelShaper;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class InterfaceRenderer extends SafeBlockEntityRenderer<InterfaceBE> {
    public static final VoxelShaper FLUID = ShapeUtils.shape(-0.01, 7, 7.99, 16.009999999999998, 10, 9).forDirectional(Direction.NORTH);

    public InterfaceRenderer(BlockEntityRendererProvider.Context context) {}
    @Override
    protected void renderSafe(InterfaceBE be, float partialTicks, PoseStack ms, SubmitNodeCollector buffer, int light, int overlay) {
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

    public @Nullable ResourceHandler<FluidResource> getFluidHandler(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof InterfaceBE be) return be.getTarget();
        return null;
    }
    public FluidStack getFluid(Level level, BlockPos pos) {
        var handler = getFluidHandler(level, pos);
        return AllUtils.firstFluid(handler);
    }
}
