package dev.lopyluna.slag.content.blocks.forge.client;

import dev.lopyluna.slag.SlagEmbers;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import javax.annotation.Nonnull;

public class ForgeScreen extends AbstractContainerScreen<ForgeMenu> {
    private static final ResourceLocation TEXTURE = SlagEmbers.loc("textures/gui/brick_forge.png");

    public ForgeScreen(ForgeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        if (this.menu.isLit()) {
            int prog = Mth.ceil(this.menu.getLitProgress() * 13.0F) + 1;
            graphics.blit(TEXTURE, leftPos + 56, topPos + 36 + 14 - prog, 176, 14 - prog, 14, prog);
        }

        int cook = Mth.ceil(this.menu.getBurnProgress() * 24.0F);
        graphics.blit(TEXTURE, leftPos + 79, topPos + 34, 176, 14, cook, 16);
    }



    @Override
    public void render(@Nonnull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        //renderBackground(gui, mouseX, mouseY, partialTick);
        super.render(gui, mouseX, mouseY, partialTick);
        renderTooltip(gui, mouseX, mouseY);
    }


    @Override
    protected void init() {
        super.init();

        titleLabelX = (imageWidth - font.width(title)) / 2;
    }
}
