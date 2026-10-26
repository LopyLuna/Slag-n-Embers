package dev.lopyluna.slag.content.blocks.melter.client;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.client.FluidRenderHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Optional;

import static dev.lopyluna.slag.client.ClientUtils.renderFakeSlot;
import static dev.lopyluna.slag.content.blocks.crucible_interface.client.InterfaceScreen.createLang;

@SuppressWarnings({"unused", "MathClampMigration", "NullableProblems"})
public class MelterScreen extends AbstractContainerScreen<MelterMenu> {
    static Minecraft mc = Minecraft.getInstance();
    private static final Identifier TEXTURE = SlagEmbers.loc("textures/gui/melter.png");
    private static final Identifier TEXTURE_OVERLAY = SlagEmbers.loc("textures/gui/melter_overlay.png");

    private static final int BOX_X = 112, BOX_Y = 19, BOX_W = 24, BOX_H = 48;
    private static final float MIN_LAYER = 3;
    private final Level level;
    public @Nullable Object ingredient;
    public @Nullable Rect2i ingredientArea;

    public MelterScreen(MelterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        level = Minecraft.getInstance().level;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        ingredient = null;
        ingredientArea = null;
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
        renderFluidLayers(graphics, mouseX, mouseY);

        var data = menu.data;
        if (data.get(2) != 0) graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + 57, topPos + 36, 176, 0, 14, 14, 256, 256);
        var target = data.get(1);
        if (target <= 0) return;
        int cook = Math.min(24, Mth.ceil(data.get(0) * 24.0F / target));
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + 79, topPos + 34, 176, 14, cook, 16, 256, 256);
    }


    @Override
    public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(gui, mouseX, mouseY, partialTick);
        var stack = menu.getBelowStack();
        if (!stack.isEmpty()) {
            var heatable = menu.be != null && menu.be.heat.hot;
            var l = leftPos + 56;
            var t = topPos + 54;
            if (heatable) gui.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + 54, topPos + 52, 176, 31, 20, 20, 256, 256);
            renderFakeSlot(gui, stack, l, t, heatable ? "✔" : "↓", this.imageWidth, this.font);
            if (mouseX >= l && mouseX < l + 18 && mouseY >= t && mouseY < t + 18) {
                ingredient = stack;
                ingredientArea = new Rect2i(l, t, 18, 18);
                var tooltipFlag = mc.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
                var tooltip = new ArrayList<Component>();

                tooltip.add(stack.getHoverName());
                if (tooltipFlag.advanced()) tooltip.add(Component.literal(BuiltInRegistries.BLOCK.getKey(menu.belowState.getBlock()).toString()).withStyle(ChatFormatting.DARK_GRAY));

                gui.setTooltipForNextFrame(font, tooltip, stack.getTooltipImage(), mouseX, mouseY);
            }
        }
    }

    @Override
    protected void init() {
        super.init();

        titleLabelX = (imageWidth - font.width(title)) / 2;
    }

    public void renderFluidLayers(GuiGraphicsExtractor g, int mx, int my) {
        if (level == null) return;
        var fluids = menu.getFluids();
        if (fluids.isEmpty()) return;
        var capacity = menu.getCapacity();
        if (capacity <= 0) return;

        int boxX = leftPos + BOX_X;
        int boxY = topPos  + BOX_Y;
        int boxW = BOX_W;

        var total = 0;
        var size = 0;
        for (var fluid : fluids) {
            if (fluid.isEmpty()) continue;
            total += fluid.getAmount();
            size++;
        }
        if (total <= 0) return;
        var fill = Math.min(49f, Math.max(49f * Mth.clamp((float) total / capacity, 0f, 1f), MIN_LAYER * size));
        var spare = Math.max(0f, fill - MIN_LAYER * size);

        var bottom = boxY + 49;
        var filled = 0f;
        FluidStack hovered = null;
        for (var fluid : fluids) {
            if (fluid.isEmpty()) continue;
            filled += MIN_LAYER + spare * fluid.getAmount() / total;
            var top = Math.max(boxY, boxY + 49 - Math.round(filled));
            if (top >= bottom) continue;
            FluidRenderHelper.drawFluidBox(g, boxX, top, boxW, bottom - top, fluid);
            if (mx >= boxX && mx < boxX + boxW && my >= top && my < bottom) {
                hovered = fluid;
                ingredient = fluid;
                ingredientArea = new Rect2i(boxX, top, boxW, bottom - top);
            }
            bottom = top;
        }

        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_OVERLAY, (width - imageWidth) / 2, (height - imageHeight) / 2, 0, 0, imageWidth, imageHeight, 256, 256);

        if (hovered != null) {
            var tooltipFlag = mc.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
            var tooltip = new ArrayList<Component>();
            tooltip.add(hovered.getHoverName());
            createLang(hovered, tooltip).run();
            if (tooltipFlag.advanced()) tooltip.add(Component.literal(BuiltInRegistries.FLUID.getKey(hovered.getFluid()).toString()).withStyle(ChatFormatting.DARK_GRAY));

            g.setTooltipForNextFrame(font, tooltip, Optional.empty(), mx, my);
        }
    }
}
