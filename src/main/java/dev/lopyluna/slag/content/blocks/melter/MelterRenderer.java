package dev.lopyluna.slag.content.blocks.melter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.lopyluna.slag.client.ClientUtils;
import dev.lopyluna.slag.content.blocks.multiblock.renderer.SafeBlockEntityRenderer;
import dev.lopyluna.slag.content.utils.ShapeUtils;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VoxelShaper;
import net.createmod.catnip.platform.NeoForgeCatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Random;

@SuppressWarnings("unused")
public class MelterRenderer extends SafeBlockEntityRenderer<MelterBE> {
    public static final VoxelShaper SPOUT = ShapeUtils.shape(4, 7, -0.01, 6, 10, 0.99)
            .add(7, 7, -0.01, 9, 10, 0.99).add(10, 7, -0.01, 12, 10, 0.99).forHorizontal(Direction.NORTH);

    public MelterRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(MelterBE be, float pt, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        var state = be.getBlockState();
        var facing = state.hasProperty(MelterBlock.FACING) ? state.getValue(MelterBlock.FACING) : Direction.NORTH;
        renderFluid(be, facing, pt, ms, buffer, light, overlay);

        var level = 0f;
        var fluidLevel = be.getFluidLevel();
        if (fluidLevel == null) level = 0f;
        else level = fluidLevel.getValue(pt);
        var inv = be.getItemInventory();
        if (inv == null || inv.isEmpty()) return;
        var item = inv.getFirstItem();
        if (item.isEmpty()) return;

        var angle = AnimationTickHolder.getRenderTime(be.getLevel()) * 0.1f;

        var ite = item.getItem();
        for (int i = 0; Math.min(item.getMaxStackSize(), ((int) Mth.clamp(11f * ((float) item.getCount() / (float) item.getMaxStackSize()), 0, 11)) + 1) > i; i++) {
            var r = new Random(i + ite.hashCode() + be.getBlockPos().hashCode());

            var offset = (float) i / 11f;
            var vecOff = i == 0 ? Vec3.ZERO : new Vec3((r.nextFloat() * 0.2) - 0.1, 0, (r.nextFloat() * 0.2) - 0.1);
            var angleOffset = i == 0 ? 0 : 360f * offset * 1.5f;

            ms.pushPose();
            ms.translate(.5, .3 + (offset * 0.5f) + (level * 0.26f), .5);
            ms.translate(vecOff.x, 0, vecOff.z);
            ms.mulPose(Axis.YP.rotationDegrees(AngleHelper.horizontalAngle(facing)));
            ms.mulPose(Axis.YP.rotationDegrees(angleOffset));
            ms.mulPose(Axis.XP.rotationDegrees(90));
            ms.mulPose(Axis.ZP.rotationDegrees(AngleHelper.wrapAngle180(angle * (0.75f + (r.nextFloat() * 0.25f)))));
            ms.translate(0, -0.1f, 0);

            renderItem(ms, buffer, light, overlay, item);
            ms.popPose();
        }
    }

    public void renderFluid(MelterBE be, Direction facing, float pt, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        var fluidLevel = be.getFluidLevel();
        if (fluidLevel == null) return;
        var tank = be.tankInventory;
        var fluidStack = tank.first;
        if (fluidStack.isEmpty()) return;
        ms.pushPose();
        ClientUtils.renderFluidShape(fluidStack, SPOUT.get(facing), buffer, ms, light, true, true);
        ms.popPose();

        float capHeight = 1 / 16f;
        float tankHullWidth = 0.1f / 16f + 1 / 128f;
        float minPuddleHeight = 4 / 16f;
        float totalHeight = 1 - 2 * capHeight - minPuddleHeight;

        float level = fluidLevel.getValue(pt) + 0.0225f / 16f;
        if (level < 1 / (512f * totalHeight)) return;
        float clampedLevel = Mth.clamp(level * totalHeight, 0, totalHeight);
        float xMax = tankHullWidth + 1 - 2 * tankHullWidth, yMin = totalHeight + capHeight + minPuddleHeight - clampedLevel, yMax = yMin + clampedLevel, zMax = tankHullWidth + 1 - 2 * tankHullWidth;

        var fluids = tank.getFluids();
        var total = tank.total;
        if (total <= 0) return;

        ms.pushPose();
        ms.translate(0, clampedLevel - totalHeight, 0);
        var bottom = yMin;
        for (var i = 0; i < fluids.size(); i++) {
            var fluid = fluids.get(i);
            if (fluid.isEmpty()) continue;
            var top = i == fluids.size() - 1 ? yMax : bottom + clampedLevel * fluid.getAmount() / total;
            NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(fluid, tankHullWidth, bottom, tankHullWidth, xMax, top, zMax, buffer, ms, light, false, true);
            bottom = top;
        }
        ms.popPose();
    }
    protected void renderItem(PoseStack ms, MultiBufferSource buffer, int light, int overlay, ItemStack stack) {
        var mc = Minecraft.getInstance();
        mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND, light, overlay, ms, buffer, mc.level, 0);
    }
}
