package dev.lopyluna.slag.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.platform.NeoForgeCatnipServices;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;

public class ClientUtils {

    public static void renderFluidShape(FluidStack fluid, VoxelShape shape, MultiBufferSource buffer, PoseStack ms, int light, boolean renderBottom, boolean invertGasses) {
        shape.optimize().forAllBoxes((x1, y1, z1, x2, y2, z2) -> NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(fluid, (float) x1, (float) y1, (float) z1, (float) x2, (float) y2, (float) z2, buffer, ms, light, renderBottom, invertGasses));
    }

    public static void panel(GuiGraphics g, ResourceLocation sprite, int x, int y, int w, int h) {
        panel(g, sprite, 32, 32, 5, x, y, w, h);
    }

    public static void panel(GuiGraphics g, ResourceLocation sprite, int texW, int texH, int border, int x, int y, int w, int h) {
        int innerW = w - border * 2, innerH = h - border * 2;
        int tileW = texW - border * 2, tileH = texH - border * 2;
        g.blitSprite(sprite, texW, texH, 0, 0, x, y, border, border);
        g.blitSprite(sprite, texW, texH, texW - border, 0, x + w - border, y, border, border);
        g.blitSprite(sprite, texW, texH, 0, texH - border, x, y + h - border, border, border);
        g.blitSprite(sprite, texW, texH, texW - border, texH - border, x + w - border, y + h - border, border, border);
        tile(g, sprite, texW, texH, border, 0, tileW, border, x + border, y, innerW, border);
        tile(g, sprite, texW, texH, border, texH - border, tileW, border, x + border, y + h - border, innerW, border);
        tile(g, sprite, texW, texH, 0, border, border, tileH, x, y + border, border, innerH);
        tile(g, sprite, texW, texH, texW - border, border, border, tileH, x + w - border, y + border, border, innerH);
        tile(g, sprite, texW, texH, border, border, tileW, tileH, x + border, y + border, innerW, innerH);
    }

    private static void tile(GuiGraphics g, ResourceLocation sprite, int texW, int texH, int u, int v, int tileW, int tileH, int x, int y, int w, int h) {
        for (var dx = 0; dx < w; dx += tileW) for (var dy = 0; dy < h; dy += tileH) g.blitSprite(sprite, texW, texH, u, v, x + dx, y + dy, Math.min(tileW, w - dx), Math.min(tileH, h - dy));
    }

    public static void renderFakeSlot(GuiGraphics guiGraphics, ItemStack itemstack, int x, int y, @Nullable String countString, int imageWidth, Font font) {
        int j1 = x + y * imageWidth;
        guiGraphics.renderFakeItem(itemstack, x, y, j1);
        guiGraphics.renderItemDecorations(font, itemstack, x, y, countString);
    }
}
