package dev.lopyluna.slag.content.blocks.forge.client;

import dev.lopyluna.slag.SlagEmbers;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

@SuppressWarnings("NullableProblems")
public class ForgeScreen extends AbstractContainerScreen<ForgeMenu> {
    private static final Identifier TEXTURE = SlagEmbers.loc("textures/gui/brick_forge.png");

    public ForgeScreen(ForgeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);

        if (this.menu.isLit()) {
            int prog = Mth.ceil(this.menu.getLitProgress() * 13.0F) + 1;
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + 56, topPos + 36 + 14 - prog, 176, 14 - prog, 14, prog, 256, 256);
        }

        int cook = Mth.ceil(this.menu.getBurnProgress() * 24.0F);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + 79, topPos + 34, 176, 14, cook, 16, 256, 256);
    }



    @Override
    public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(gui, mouseX, mouseY, partialTick);
    }


    @Override
    protected void init() {
        super.init();

        titleLabelX = (imageWidth - font.width(title)) / 2;
    }
}
