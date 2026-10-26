package dev.lopyluna.slag.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class ClientUtils {

    public static void renderFluidShape(FluidStack fluid, VoxelShape shape, SubmitNodeCollector collector, PoseStack ms, int light, boolean renderBottom, boolean invertGasses) {
        var boxes = shape.optimize().toAabbs();
        FluidRenderHelper.submit(collector, ms, (builder, stack) -> {
            for (var box : boxes) FluidRenderHelper.renderFluidBox(fluid, (float) box.minX, (float) box.minY, (float) box.minZ, (float) box.maxX, (float) box.maxY, (float) box.maxZ, builder, stack, light, renderBottom, invertGasses);
        });
    }

    public static void panel(GuiGraphicsExtractor g, Identifier sprite, int x, int y, int w, int h) {
        panel(g, sprite, 32, 32, 5, x, y, w, h);
    }

    public static void panel(GuiGraphicsExtractor g, Identifier sprite, int texW, int texH, int border, int x, int y, int w, int h) {
        int innerW = w - border * 2, innerH = h - border * 2;
        int tileW = texW - border * 2, tileH = texH - border * 2;
        g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, texW, texH, 0, 0, x, y, border, border);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, texW, texH, texW - border, 0, x + w - border, y, border, border);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, texW, texH, 0, texH - border, x, y + h - border, border, border);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, texW, texH, texW - border, texH - border, x + w - border, y + h - border, border, border);
        tile(g, sprite, texW, texH, border, 0, tileW, border, x + border, y, innerW, border);
        tile(g, sprite, texW, texH, border, texH - border, tileW, border, x + border, y + h - border, innerW, border);
        tile(g, sprite, texW, texH, 0, border, border, tileH, x, y + border, border, innerH);
        tile(g, sprite, texW, texH, texW - border, border, border, tileH, x + w - border, y + border, border, innerH);
        tile(g, sprite, texW, texH, border, border, tileW, tileH, x + border, y + border, innerW, innerH);
    }

    private static void tile(GuiGraphicsExtractor g, Identifier sprite, int texW, int texH, int u, int v, int tileW, int tileH, int x, int y, int w, int h) {
        for (var dx = 0; dx < w; dx += tileW) for (var dy = 0; dy < h; dy += tileH) g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, texW, texH, u, v, x + dx, y + dy, Math.min(tileW, w - dx), Math.min(tileH, h - dy));
    }

    public static void renderFakeSlot(GuiGraphicsExtractor guiGraphics, ItemStack itemstack, int x, int y, @Nullable String countString, int imageWidth, Font font) {
        int j1 = x + y * imageWidth;
        guiGraphics.fakeItem(itemstack, x, y, j1);
        guiGraphics.itemDecorations(font, itemstack, x, y, countString);
    }
}
