package dev.lopyluna.slag.content.blocks.crucible_interface.client;

import com.mojang.datafixers.util.Pair;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.client.FluidRenderHelper;
import dev.lopyluna.slag.content.blocks.melter.MelterBE;
import dev.lopyluna.slag.network.packets.SelectFluidIndexC2S;
import dev.lopyluna.slag.register.AllLangs;
import dev.lopyluna.slag.register.AllTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("NullableProblems")
public class InterfaceScreen extends AbstractContainerScreen<InterfaceMenu> {
    private static final Identifier TEXTURE = SlagEmbers.loc("textures/gui/interface.png");
    private static final Identifier TEXTURE_OVERLAY = SlagEmbers.loc("textures/gui/interface_overlay.png");

    private static final int BOX_X = 39, BOX_Y = 19, BOX_W = 97, BOX_H = 48;
    private final Level level;
    public @Nullable Object ingredient;
    public @Nullable Rect2i ingredientArea;

    public InterfaceScreen(InterfaceMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        level = Minecraft.getInstance().level;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float pt) {
        super.extractBackground(g, mx, my, pt);
        ingredient = null;
        ingredientArea = null;
        int iX = (width - imageWidth) / 2;
        int iY = (height - imageHeight) / 2;
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, iX, iY, 0, 0, imageWidth, imageHeight, 256, 256);
        var capacity = menu.getCapacity();
        if (capacity == 0) return;
        renderFluidLayers(g, mx, my);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.extractLabels(guiGraphics, mouseX, mouseY);
        float capacity = (float) menu.getCapacity() / Math.max(1, menu.getFluids().size());
        if (capacity == 0) return;
        float amount = menu.getAmount();

        var bucketCapacity = capacity > 1000f;
        if (bucketCapacity) capacity /= 100f;
        capacity = Math.round(capacity);
        capacity /= bucketCapacity ? 10f : 1f;

        var bucketAmount = amount > 1000f;
        if (bucketAmount) amount /= 100f;
        amount = Math.round(amount);
        amount /= bucketAmount ? 10f : 1f;


        var text = amount + (bucketAmount ? "B" : "mB") + "/" + capacity + (bucketCapacity ? "B" : "mB");
        text = text.replace(".0", "");
        guiGraphics.text(this.font, Component.literal(text), this.titleLabelX + imageWidth - font.width(text) - 12, this.titleLabelY, 0xFF404040, false);
    }

    public void renderFluidLayers(GuiGraphicsExtractor g, int mx, int my) {
        if (level == null) return;
        var fluids = menu.getFluids();
        if (fluids.isEmpty()) return;

        int boxX = leftPos + BOX_X;
        int boxY = topPos  + BOX_Y;
        int boxW = BOX_W;
        int boxH = BOX_H;

        int maxFluids = 8;
        int displayCount = Math.min(fluids.size(), maxFluids);

        int totalMb = 0;
        for (int i = 0; i < displayCount; i++) totalMb += fluids.get(i).getAmount();
        if (totalMb <= 0) return;

        int[] heights = new int[displayCount];
        int baseHeight = boxH / displayCount;
        int remainder = boxH % displayCount;

        for (int i = 0; i < displayCount; i++) heights[i] = baseHeight + (i < remainder ? 1 : 0);

        int yTop = boxY + boxH;

        for (int i = 0; i < displayCount; i++) {
            var fs = fluids.get(i);
            int h = heights[i];
            if (h <= 0) continue;

            int y = yTop - h;

            FluidRenderHelper.drawFluidBox(g, boxX, y, boxW, h, fs);

            if (i == 0) g.fill(boxX, y, boxX + boxW, y + h, 0x1affffFF);

            yTop -= h;
        }

        var p = g.pose();
        p.pushMatrix();
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_OVERLAY, (width - imageWidth) / 2, (height - imageHeight) / 2, 0, 0, imageWidth, imageHeight, 256, 256);
        p.popMatrix();

        if (mx >= boxX && mx < boxX + boxW && my >= boxY && my < boxY + boxH) {
            int yTop2 = boxY + boxH;
            for (int i = 0; i < displayCount; i++) {
                var fs = fluids.get(i);
                int h = heights[i];
                if (h <= 0) continue;

                int y = yTop2 - h;
                if (my >= y && my < yTop2) {
                    ingredient = fs;
                    ingredientArea = new Rect2i(boxX, y, boxW, h);
                    g.fill(boxX, y, boxX + boxW, y + h, 0x1affffFF);

                    var tooltipFlag = Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
                    var tooltip = new ArrayList<Component>();
                    tooltip.add(fs.getHoverName());
                    createLang(fs, tooltip).run();
                    if (tooltipFlag.advanced()) tooltip.add(Component.literal(BuiltInRegistries.FLUID.getKey(fs.getFluid()).toString()).withStyle(ChatFormatting.DARK_GRAY));

                    g.setTooltipForNextFrame(font, tooltip, Optional.empty(), mx, my);
                    break;
                }
                yTop2 -= h;
            }
        }
    }

    public static Runnable createLang(FluidStack fs, List<Component> tooltip) {
        var shift = !Minecraft.getInstance().hasShiftDown();
        var amount = fs.getAmount();
        if (fs.is(AllTags.MOLTEN_METALS)) return () -> {
            if (shift) createTooltipAmounts(tooltip, amount, pNS("blocks", MelterBE.BLOCK_SIZE), pNS("ingots", MelterBE.INGOT_SIZE), pNS("nuggets", MelterBE.NUGGET_SIZE));
            else extra(amount, tooltip);
        };
        if (fs.is(AllTags.MOLTEN_GEMS)) return () -> {
            if (shift) createTooltipAmounts(tooltip, amount, pNS("blocks", MelterBE.BLOCK_SIZE), pNS("gems", MelterBE.INGOT_SIZE), pNS("shards", MelterBE.SHARD_SIZE));
            else extra(amount, tooltip);
        };
        if (fs.is(AllTags.MOLTEN_CRYSTALS)) return () -> {
            if (shift) createTooltipAmounts(tooltip, amount, pNS("blocks", MelterBE.SMALL_BLOCK_SIZE), pNS("gems", MelterBE.INGOT_SIZE), pNS("shards", MelterBE.SHARD_SIZE));
            else extra(amount, tooltip);
        };
        if (fs.is(AllTags.MOLTEN_DUSTS)) return () -> {
            if (shift) createTooltipAmounts(tooltip, amount, pNS("blocks", MelterBE.BLOCK_SIZE), pNS("dusts", MelterBE.INGOT_SIZE), pNS("grits", MelterBE.NUGGET_SIZE));
            else extra(amount, tooltip);
        };
        if (fs.is(AllTags.MOLTEN_DUSTS_SMALL)) return () -> {
            if (shift) createTooltipAmounts(tooltip, amount, pNS("blocks", MelterBE.SMALL_BLOCK_SIZE), pNS("dusts", MelterBE.INGOT_SIZE), pNS("grits", MelterBE.NUGGET_SIZE));
            else extra(amount, tooltip);
        };
        if (fs.is(AllTags.MOLTEN_BALLS)) return () -> {
            if (shift) createTooltipAmounts(tooltip, amount, pNS("blocks", MelterBE.BLOCK_SIZE), pNS("balls", MelterBE.INGOT_SIZE));
            else extra(amount, tooltip);
        };
        if (fs.is(AllTags.MOLTEN_BALLS_SMALL)) return () -> {
            if (shift) createTooltipAmounts(tooltip, amount, pNS("blocks", MelterBE.SMALL_BLOCK_SIZE), pNS("balls", MelterBE.INGOT_SIZE));
            else extra(amount, tooltip);
        };
        return () -> extra(amount, tooltip);
    }

    @SafeVarargs
    public static void createTooltipAmounts(List<Component> tooltip, int amount, Pair<String, Integer>... pairs) {
        final int n = pairs.length;
        String[] keys = new String[n];
        int[] sizes   = new int[n + 1];

        for (int i = 0; i < n; i++) {
            var p = Objects.requireNonNull(pairs[i], "pair");
            keys[i]  = Objects.requireNonNull(p.getFirst(), "name");
            sizes[i] = Objects.requireNonNull(p.getSecond(), "size");
            if (sizes[i] <= 0) throw new IllegalArgumentException("size must be > 0");
            if (i > 0 && sizes[i] >= sizes[i - 1]) throw new IllegalArgumentException("sizes must be strictly descending");
        }
        sizes[n] = 1; // mB
        int[] m = breakdown(amount, sizes); // m[0..n-1]=units, m[n]=mB
        for (int i = 0; i < n; i++) AllLangs.trAmounts(tooltip, keys[i], m[i], true);
        AllLangs.trAmounts(tooltip, "mb", m[n], false);
    }

    public static Pair<String, Integer> pNS(String name, int size) {
        return Pair.of(name, size);
    }

    private static int[] breakdown(int amount, int... sizes) {
        int[] out = new int[sizes.length];
        int rem = amount;
        for (int i = 0; i < sizes.length; i++) {
            int v = sizes[i];
            if (v <= 0) throw new IllegalArgumentException("size must be > 0");
            out[i] = rem / v;
            rem    = rem % v;
        }
        return out;
    }

    public static void extra(int amount, List<Component> tooltip) {
        var b = (int) ((float) amount / 1000f);
        var mb = amount - (b*1000);
        AllLangs.trAmounts(tooltip, "buckets", b, true);
        AllLangs.trAmounts(tooltip, "mb", mb, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        var mx = event.x();
        var my = event.y();
        var button = event.button();
        if (level == null || !menu.isCrucible() || menu.player == null || menu.player.isSpectator()) return super.mouseClicked(event, doubleClick);
        if (button == 0) {
            int boxX = leftPos + BOX_X;
            int boxY = topPos  + BOX_Y;
            int boxH = BOX_H;

            var fluids = menu.getFluids();

            if (fluids.size() <= 1) return super.mouseClicked(event, doubleClick);

            if (mx >= boxX && mx < boxX + BOX_W && my >= boxY && my < boxY + boxH) {
                int maxFluids = 8;
                int displayCount = Math.min(fluids.size(), maxFluids);

                int totalMb = 0;
                for (int i = 0; i < displayCount; i++) totalMb += fluids.get(i).getAmount();
                if (totalMb > 0) {
                    int[] heights = new int[displayCount];
                    int baseHeight = boxH / displayCount;
                    int remainder = boxH % displayCount;

                    for (int i = 0; i < displayCount; i++) heights[i] = baseHeight + (i < remainder ? 1 : 0);

                    int yTop = boxY + boxH;
                    for (int i = 0; i < displayCount; i++) {
                        var fs = fluids.get(i);
                        int h = heights[i];
                        if (h <= 0) continue;

                        int y = yTop - h;
                        if (my >= y && my < yTop) {
                            var type = fs.getFluidType();
                            Optional.ofNullable(type.getSound(SoundActions.BUCKET_FILL)).ifPresent(s -> menu.player.playSound(s, 0.5f, 1f));
                            Optional.ofNullable(type.getSound(SoundActions.BUCKET_EMPTY)).ifPresent(s -> menu.player.playSound(s, 0.5f, 1f));
                            ClientPacketDistributor.sendToServer(new SelectFluidIndexC2S(i));
                            return true;
                        }
                        yTop -= h;
                    }
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(gui, mouseX, mouseY, partialTick);
    }
}
