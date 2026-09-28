package dev.lopyluna.slag.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.createmod.catnip.annotations.ClientOnly;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.platform.services.ModFluidHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.function.Function;

@SuppressWarnings({"unused", "MultipleVariablesInDeclaration", "UnusedAssignment"})
public class FluidRenderHelper {
    private FluidRenderHelper() {}

    @ClientOnly
    public static TextureAtlasSprite getFlowingTexture(FluidStack fluid) {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(IClientFluidTypeExtensions.of(fluid.getFluid()).getFlowingTexture(fluid));
    }

    @ClientOnly
    public static TextureAtlasSprite getFlowingTextureOrMissing(FluidStack fluid) {
        TextureAtlasSprite texture = getFlowingTexture(fluid);
        if (texture != null) return texture;
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(MissingTextureAtlasSprite.getLocation());
    }

    public static void renderFlowingFluidBox(FluidStack fluid, float xMin, float yMin, float zMin, float xMax, float yMax, float zMax, MultiBufferSource buffer, PoseStack ms, int light, boolean renderBottom, boolean invertGasses) {
        renderFlowingFluidBox(fluid, xMin, yMin, zMin, xMax, yMax, zMax, getFluidBuilder(buffer), ms, light, renderBottom, invertGasses);
    }

    @SuppressWarnings("unchecked")
    public static void renderFlowingFluidBox(FluidStack fluid, float xMin, float yMin, float zMin, float xMax, float yMax, float zMax, VertexConsumer builder, PoseStack ms, int light, boolean renderBottom, boolean invertGasses) {
        ModFluidHelper<FluidStack> helper = (ModFluidHelper<FluidStack>) CatnipServices.FLUID_HELPER;

        TextureAtlasSprite fluidTexture = getFlowingTextureOrMissing(fluid);
        int color = helper.getColor(fluid, null, null);

        int blockLightIn = (light >> 4) & 0xF;
        int luminosity = Math.max(blockLightIn, helper.getLuminosity(fluid));
        light = (light & 0xF00000) | luminosity << 4;

        Vec3 center = new Vec3(xMin + (xMax - xMin) / 2, yMin + (yMax - yMin) / 2, zMin + (zMax - zMin) / 2);
        ms.pushPose();
        if (invertGasses && helper.isLighterThanAir(fluid)) {
            ms.translate(center.x, center.y, center.z);
            ms.mulPose(Axis.XP.rotationDegrees(180));
            ms.translate(-center.x, -center.y, -center.z);
        }

        for (Direction side : Iterate.directions) {
            if (side == Direction.DOWN && !renderBottom) continue;

            boolean positive = side.getAxisDirection() == Direction.AxisDirection.POSITIVE;
            if (side.getAxis().isHorizontal()) {
                if (side.getAxis() == Direction.Axis.X) net.createmod.catnip.render.FluidRenderHelper.renderTiledFace(side, zMin, yMin, zMax, yMax, positive ? xMax : xMin, builder, ms, light, color, fluidTexture, 0.5f);
                else net.createmod.catnip.render.FluidRenderHelper.renderTiledFace(side, xMin, yMin, xMax, yMax, positive ? zMax : zMin, builder, ms, light, color, fluidTexture, 0.5f);
            } else net.createmod.catnip.render.FluidRenderHelper.renderTiledFace(side, xMin, zMin, xMax, zMax, positive ? yMax : yMin, builder, ms, light, color, fluidTexture, 0.5f);
        }
        ms.popPose();
    }

    public static void drawFluidBox(GuiGraphics g, int x, int y, int sizeX, int sizeY, FluidStack fs) {
        if (fs == null || fs.isEmpty()) return;
        TextureAtlasSprite spr = getStillTextureOrMissing(fs);
        int argb = getColor(fs, null, null);
        drawFluidBox(g, x, y, sizeX, sizeY, spr, argb);

    }

    public static void drawFluidBox(GuiGraphics g, int x, int y, int sizeX, int sizeY, TextureAtlasSprite spr, int argb) {
        if (spr == null) return;

        float a = ((argb >>> 24) & 0xFF) / 255f;
        float r = ((argb >>> 16) & 0xFF) / 255f;
        float gg = ((argb >>>  8) & 0xFF) / 255f;
        float b =  (argb & 0xFF) / 255f;
        g.setColor(r, gg, b, a);

        final int u0px = spr.getX();
        final int v0px = spr.getY();
        final int sw   = spr.contents().width();
        final int sh   = spr.contents().height();
        final float u0 = spr.getU0(), u1 = spr.getU1();
        final float v0 = spr.getV0(), v1 = spr.getV1();
        final int atlasW = Math.round(sw / (u1 - u0));
        final int atlasH = Math.round(sh / (v1 - v0));

        for (int oy = 0; oy < sizeY; ) {
            int h = Math.min(sh, sizeY - oy);
            for (int ox = 0; ox < sizeX; ) {
                int w = Math.min(sw, sizeX - ox);
                g.blit(spr.atlasLocation(), x + ox, y + oy, w, h, (float) u0px, (float) v0px, w, h, atlasW, atlasH);
                ox += w;
            }
            oy += h;
        }

        g.setColor(1f, 1f, 1f, 1f);
    }

    public static void renderFluidBox(FluidState fluid, float xMin, float yMin, float zMin, float xMax, float yMax, float zMax,
                               MultiBufferSource buffer, PoseStack ms, int light, boolean renderBottom, boolean invertGasses) {
        renderFluidBox(fluid, xMin, yMin, zMin, xMax, yMax, zMax, getFluidBuilder(buffer), ms, light, renderBottom, invertGasses);
    }

    public static void renderFluidBox(FluidState fluid,  float xMin, float yMin, float zMin, float xMax,
                               float yMax, float zMax, VertexConsumer builder, PoseStack ms, int light, boolean renderBottom, boolean invertGasses) {
        renderFluidBox(toStack(fluid), xMin, yMin, zMin, xMax, yMax, zMax, builder, ms, light, renderBottom, invertGasses);
    }

    public static void renderFluidBox(FluidStack fluid,  float xMin, float yMin, float zMin, float xMax,
                               float yMax, float zMax, MultiBufferSource buffer, PoseStack ms, int light, boolean renderBottom, boolean invertGasses) {
        renderFluidBox(fluid, xMin, yMin, zMin, xMax, yMax, zMax, getFluidBuilder(buffer), ms, light, renderBottom, invertGasses);
    }

    public static void renderFluidBlock(FluidStack fluid, float height, MultiBufferSource buffer, PoseStack ms, int light) {
        var builder = getFluidBuilder(buffer);
        var still = getStillTextureOrMissing(fluid);
        var flowing = getFlowingTextureOrMissing(fluid);
        var color = getColor(fluid, null, null);
        light = (light & 0xF00000) | Math.max((light >> 4) & 0xF, getLuminosity(fluid)) << 4;
        for (var side : Direction.values()) {
            var positive = side.getAxisDirection() == Direction.AxisDirection.POSITIVE;
            if (side.getAxis() == Direction.Axis.Y) renderStillTiledFace(side, 0, 0, 1, 1, positive ? height : 0, builder, ms, light, color, still);
            else net.createmod.catnip.render.FluidRenderHelper.renderTiledFace(side, 0, 0, 1, height, positive ? 1 : 0, builder, ms, light, color, flowing, 0.5f);
        }
    }

    public static VertexConsumer getFluidBuilder(MultiBufferSource buffer) {
        return buffer.getBuffer(RenderType.translucent());
    }

    public static void renderFluidBox(FluidStack fluid, float xMin, float yMin, float zMin, float xMax, float yMax, float zMax, VertexConsumer builder, PoseStack ms, int light, boolean renderBottom, boolean invertGasses) {

        TextureAtlasSprite fluidTexture = getStillTextureOrMissing(fluid);
        int color = getColor(fluid, null, null);

        int blockLightIn = (light >> 4) & 0xF;
        int luminosity = Math.max(blockLightIn, getLuminosity(fluid));
        light = (light & 0xF00000) | luminosity << 4;

        Vec3 center = new Vec3(xMin + (xMax - xMin) / 2, yMin + (yMax - yMin) / 2, zMin + (zMax - zMin) / 2);
        ms.pushPose();
        if (invertGasses && isLighterThanAir(fluid)) {
            ms.translate(center.x, center.y, center.z);
            ms.mulPose(Axis.XP.rotationDegrees(180));
            ms.translate(-center.x, -center.y, -center.z);
        }

        for (Direction side : Direction.values()) {
            if (side == Direction.DOWN && !renderBottom) continue;

            boolean positive = side.getAxisDirection() == Direction.AxisDirection.POSITIVE;
            if (side.getAxis().isHorizontal()) {
                if (side.getAxis() == Direction.Axis.X) renderStillTiledFace(side, zMin, yMin, zMax, yMax, positive ? xMax : xMin, builder, ms, light, color, fluidTexture);
                else renderStillTiledFace(side, xMin, yMin, xMax, yMax, positive ? zMax : zMin, builder, ms, light, color, fluidTexture);
            } else renderStillTiledFace(side, xMin, zMin, xMax, zMax, positive ? yMax : yMin, builder, ms, light, color, fluidTexture);
        }
        ms.popPose();
    }

    public static void renderFluidFaces(FluidStack fluid, float xMin, float yMin, float zMin, float xMax, float yMax, float zMax, VertexConsumer builder, PoseStack ms, int light, boolean bottom, boolean top, boolean sides, Vec3 eye) {
        var texture = getStillTextureOrMissing(fluid);
        var color = getColor(fluid, null, null);
        light = (light & 0xF00000) | Math.max((light >> 4) & 0xF, getLuminosity(fluid)) << 4;
        var all = eye == null;
        if (top && (all || eye.y > yMax)) renderStillTiledFace(Direction.UP, xMin, zMin, xMax, zMax, yMax, builder, ms, light, color, texture);
        if (bottom && (all || eye.y < yMin)) renderStillTiledFace(Direction.DOWN, xMin, zMin, xMax, zMax, yMin, builder, ms, light, color, texture);
        if (!sides) return;
        if (all || eye.z < zMin) renderStillTiledFace(Direction.NORTH, xMin, yMin, xMax, yMax, zMin, builder, ms, light, color, texture);
        if (all || eye.z > zMax) renderStillTiledFace(Direction.SOUTH, xMin, yMin, xMax, yMax, zMax, builder, ms, light, color, texture);
        if (all || eye.x < xMin) renderStillTiledFace(Direction.WEST, zMin, yMin, zMax, yMax, xMin, builder, ms, light, color, texture);
        if (all || eye.x > xMax) renderStillTiledFace(Direction.EAST, zMin, yMin, zMax, yMax, xMax, builder, ms, light, color, texture);
    }

    public static void renderStillTiledFace(Direction dir, float left, float down, float right, float up,
                                            float depth, VertexConsumer builder, PoseStack ms, int light, int color, TextureAtlasSprite texture) {
        renderTiledFace(dir, left, down, right, up, depth, builder, ms, light, color, texture, 1);
    }

    public static void renderTiledFace(Direction dir, float left, float down, float right, float up,
                                       float depth, VertexConsumer builder, PoseStack ms, int light, int color, TextureAtlasSprite texture,
                                       float textureScale) {
        boolean positive = dir.getAxisDirection() == Direction.AxisDirection.POSITIVE;
        boolean horizontal = dir.getAxis().isHorizontal();
        boolean x = dir.getAxis() == Direction.Axis.X;

        float shrink = texture.uvShrinkRatio() * 0.25f * textureScale;
        float centerU = texture.getU0() + (texture.getU1() - texture.getU0()) * 0.5f * textureScale;
        float centerV = texture.getV0() + (texture.getV1() - texture.getV0()) * 0.5f * textureScale;

        float f;
        float x2 = 0, y2 = 0;
        float u1, u2;
        float v1, v2;
        for (float x1 = left; x1 < right; x1 = x2) {
            f = Mth.floor(x1);
            x2 = Math.min(f + 1, right);
            if (dir == Direction.NORTH || dir == Direction.EAST) {
                f = Mth.ceil(x2);
                u1 = texture.getU((f - x2) * textureScale);
                u2 = texture.getU((f - x1) * textureScale);
            } else {
                u1 = texture.getU((x1 - f) * textureScale);
                u2 = texture.getU((x2 - f) * textureScale);
            }
            u1 = Mth.lerp(shrink, u1, centerU);
            u2 = Mth.lerp(shrink, u2, centerU);
            for (float y1 = down; y1 < up; y1 = y2) {
                f = Mth.floor(y1);
                y2 = Math.min(f + 1, up);
                if (dir == Direction.UP) {
                    v1 = texture.getV((y1 - f) * textureScale);
                    v2 = texture.getV((y2 - f) * textureScale);
                } else {
                    f = Mth.ceil(y2);
                    v1 = texture.getV((f - y2) * textureScale);
                    v2 = texture.getV((f - y1) * textureScale);
                }
                v1 = Mth.lerp(shrink, v1, centerV);
                v2 = Mth.lerp(shrink, v2, centerV);

                if (horizontal) {
                    if (x) {
                        putVertex(builder, ms, depth, y2, positive ? x2 : x1, color, u1, v1, dir, light);
                        putVertex(builder, ms, depth, y1, positive ? x2 : x1, color, u1, v2, dir, light);
                        putVertex(builder, ms, depth, y1, positive ? x1 : x2, color, u2, v2, dir, light);
                        putVertex(builder, ms, depth, y2, positive ? x1 : x2, color, u2, v1, dir, light);
                    } else {
                        putVertex(builder, ms, positive ? x1 : x2, y2, depth, color, u1, v1, dir, light);
                        putVertex(builder, ms, positive ? x1 : x2, y1, depth, color, u1, v2, dir, light);
                        putVertex(builder, ms, positive ? x2 : x1, y1, depth, color, u2, v2, dir, light);
                        putVertex(builder, ms, positive ? x2 : x1, y2, depth, color, u2, v1, dir, light);
                    }
                } else {
                    putVertex(builder, ms, x1, depth, positive ? y1 : y2, color, u1, v1, dir, light);
                    putVertex(builder, ms, x1, depth, positive ? y2 : y1, color, u1, v2, dir, light);
                    putVertex(builder, ms, x2, depth, positive ? y2 : y1, color, u2, v2, dir, light);
                    putVertex(builder, ms, x2, depth, positive ? y1 : y2, color, u2, v1, dir, light);
                }
            }
        }
    }

    private static void putVertex(VertexConsumer builder, PoseStack ms, float x, float y, float z, int color, float u, float v, Direction face, int light) {
        Vec3i normal = face.getNormal();
        PoseStack.Pose peek = ms.last();
        int a = color >> 24 & 0xff;
        int r = color >> 16 & 0xff;
        int g = color >> 8 & 0xff;
        int b = color & 0xff;

        builder.addVertex(peek.pose(), x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setLight(light)
                .setNormal(peek, normal.getX(), normal.getY(), normal.getZ())
        ;
    }

    private static TextureAtlasSprite safe(Function<ResourceLocation, TextureAtlasSprite> atlas, net.minecraft.resources.ResourceLocation rl) {
        return rl != null ? atlas.apply(rl) : atlas.apply(MissingTextureAtlasSprite.getLocation());
    }

    public static int getLuminosity(FluidStack fluid) {
        return fluid.getFluid().getFluidType().getLightLevel();
    }

    public static TextureAtlasSprite getStillTexture(FluidStack fluid) {
        ResourceLocation id = IClientFluidTypeExtensions.of(fluid.getFluid()).getStillTexture(fluid);
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(id);
    }

    public static TextureAtlasSprite getStillTextureOrMissing(FluidStack fluid) {
        TextureAtlasSprite texture = getStillTexture(fluid);
        if (texture != null) return texture;
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(MissingTextureAtlasSprite.getLocation());
    }
    public static int getColor(FluidStack stack, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos) {
        Fluid fluid = stack.getFluid();
        IClientFluidTypeExtensions extension = IClientFluidTypeExtensions.of(fluid);
        if (level == null || pos == null)
            return extension.getTintColor(stack);
        return extension.getTintColor(fluid.defaultFluidState(), level, pos);
    }

    public static boolean isLighterThanAir(FluidStack fluid) {
        return fluid.getFluid().getFluidType().isLighterThanAir();
    }

    public static FluidStack toStack(FluidState state) {
        return new FluidStack(state.getType(), 1000);
    }

}