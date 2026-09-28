package dev.lopyluna.slag.content.smithing.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.client.ClientUtils;
import dev.lopyluna.slag.content.items.modular.ModularItem;
import dev.lopyluna.slag.content.smithing.ModularSmithingMenu;
import dev.lopyluna.slag.register.AllLangs;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings({"NullableProblems", "SuspiciousNameCombination"})
public class ModularSmithingScreen extends AbstractContainerScreen<ModularSmithingMenu> {
    private static final ResourceLocation TEXTURE = SlagEmbers.loc("textures/gui/modular_smithing.png");
    private static final ResourceLocation PANEL = ResourceLocation.withDefaultNamespace("container/inventory/effect_background_small");
    private static final Vector3f STAND_TRANSLATION = new Vector3f();
    private static final Quaternionf STAND_ANGLE = new Quaternionf().rotationXYZ(0.43633232F, 0.0F, (float) Math.PI);
    private static final int LEFT_W = 142, LEFT_H = 166, RIGHT_W = 100, RIGHT_H = 166, GAP = 4, MARGIN = 6, LINE = 10, ROWS = 5;
    private static final float ORBIT_X = 18f, ORBIT_Y = 6f, ORBIT_SCALE = 0.15f, ORBIT_SPEED = 0.03f, HOVER_SCALE = 0.2f, GLOW = 1.5f;

    private final List<Line> lines = new ArrayList<>();
    private final List<Part> parts = new ArrayList<>();
    private ItemStack panelStack = ItemStack.EMPTY;
    private ItemStack preview = ItemStack.EMPTY;
    private @Nullable ArmorStand stand;
    private float orbit;
    private long orbitTime;
    private int scroll;
    private int listScroll;
    private int picked = -1;

    private record Line(FormattedCharSequence text, int indent) {}
    private record Part(int index, ItemStack stack, float x, float y, float scale) {}

    public ModularSmithingScreen(ModularSmithingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        titleLabelX = 44;
        titleLabelY = 15;
    }

    @Override
    protected void init() {
        super.init();
        if (minecraft == null) return;
        if (minecraft.level != null) {
            stand = new ArmorStand(minecraft.level, 0, 0, 0);
            stand.setNoBasePlate(true);
            stand.setShowArms(true);
            stand.yBodyRot = 210f;
            stand.setXRot(25f);
            stand.yHeadRot = stand.getYRot();
            stand.yHeadRotO = stand.getYRot();
        }
        orbitTime = Util.getMillis();
        updateStand(result());
        updateLines();
    }

    private ItemStack result() {
        return menu.getSlot(ModularSmithingMenu.RESULT).getItem();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        var result = result();
        if (!ItemStack.matches(preview, result)) {
            preview = result.copy();
            updateStand(result);
        }
        updateLines();
    }

    private void updateStand(ItemStack stack) {
        if (stand == null) return;
        for (var slot : EquipmentSlot.values()) stand.setItemSlot(slot, ItemStack.EMPTY);
        if (stack.isEmpty()) return;
        stand.setItemSlot(stand.getEquipmentSlotForItem(stack), stack.copy());
    }

    private void updateLines() {
        var result = result();
        var stack = result.isEmpty() ? menu.blueprint() : result;
        if (ItemStack.matches(panelStack, stack)) return;
        panelStack = stack.copy();
        lines.clear();
        if (!(stack.getItem() instanceof ModularItem item)) return;
        var parts = item.getParts(stack);
        if (parts == null || parts.isEmpty()) return;

        var type = item.hasModularType(stack) ? item.getModularType(stack) : item.getModularTypeFromParts(parts);
        if (type != null) add(type.getName().copy().withStyle(ChatFormatting.YELLOW));

        var stats = new ArrayList<Line>();
        var traits = item.getTraits(stack);
        var dura = item.getDura(stack);
        var tier = item.getTier(stack);
        var ench = Math.round(item.getEnch(stack));
        if (traits.armor) {
            var defense = Math.round(item.getDefense(stack));
            var kbRes = item.getKbRes(stack);
            var tough = Math.round(item.getTough(stack));
            stat(stats, "modular_defense", defense, AllLangs.format(defense));
            stat(stats, "modular_durability", dura, AllLangs.format(dura));
            stat(stats, "modular_knockback_resistance", kbRes, AllLangs.format(kbRes));
            stat(stats, "modular_toughness", tough, AllLangs.format(tough));
        } else {
            var sharp = item.getSharp(stack);
            var attackSpeed = item.getAttackSpeed(stack);
            var speed = item.getSpeed(stack);
            stat(stats, "modular_damage", sharp, AllLangs.format(1 + sharp));
            stat(stats, "modular_durability", dura, AllLangs.format(dura));
            stat(stats, "modular_attack_speed", attackSpeed, AllLangs.format(4 - attackSpeed));
            stat(stats, "modular_mine_speed", speed, AllLangs.format(speed));
        }
        stat(stats, "modular_tier", tier, AllLangs.format(tier));
        stat(stats, "modular_enchantability", ench, "" + ench);
        if (!stats.isEmpty()) {
            add(AllLangs.tr("modular_stats").append(":").withStyle(ChatFormatting.GRAY));
            lines.addAll(stats);
        }

        if (!traits.visible.isEmpty()) {
            add(AllLangs.tr("modular_traits").append(":").withStyle(ChatFormatting.GRAY));
            for (var trait : traits.visible) add(Component.literal(" ").append(AllLangs.trait(trait)));
        }

        add(AllLangs.tr("modular_parts").append(":").withStyle(ChatFormatting.GRAY));
        for (var part : parts.itemsCopy()) add(Component.literal(" ").append(part.getHoverName()).append(part.getCount() == 1 ? "" : " " + part.getCount() + "x").withColor(FastColor.ARGB32.color(115, 115, 115)));
    }

    private void stat(List<Line> stats, String key, double value, String display) {
        var pair = AllLangs.stat(key, value, display);
        if (pair == null) return;
        var split = font.split(Component.literal(" ").append(AllLangs.tr(pair.getFirst())).append(": " + pair.getSecond()).withStyle(ChatFormatting.BLUE), LEFT_W - MARGIN * 2 - 4);
        for (var i = 0; i < split.size(); i++) stats.add(new Line(split.get(i), i == 0 ? 0 : 6));
    }

    private void add(Component line) {
        var split = font.split(line, LEFT_W - MARGIN * 2 - 4);
        for (var i = 0; i < split.size(); i++) lines.add(new Line(split.get(i), i == 0 ? 0 : 6));
    }

    private int leftX() {
        return leftPos - GAP - LEFT_W;
    }

    private int rightX() {
        return leftPos + imageWidth + GAP;
    }

    private int listY() {
        return topPos + MARGIN + LINE + 2;
    }

    public List<Rect2i> extraAreas() {
        var areas = new ArrayList<Rect2i>();
        areas.add(new Rect2i(rightX(), topPos, RIGHT_W, RIGHT_H));
        if (!lines.isEmpty()) areas.add(new Rect2i(leftX(), topPos, LEFT_W, LEFT_H));
        return areas;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partial, int mouseX, int mouseY) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        ClientUtils.panel(g, PANEL, rightX(), topPos, RIGHT_W, RIGHT_H);
        renderList(g, mouseX, mouseY);
        if (stand != null) InventoryScreen.renderEntityInInventory(g, rightX() + RIGHT_W / 2f, topPos + RIGHT_H - 18f, 25f, STAND_TRANSLATION, STAND_ANGLE, null, stand);

        if (!lines.isEmpty()) {
            ClientUtils.panel(g, PANEL, leftX(), topPos, LEFT_W, LEFT_H);
            renderPanel(g);
        }

        updateParts(mouseX, mouseY);
        renderParts(g, partAt(mouseX, mouseY));
    }

    private void updateParts(int mouseX, int mouseY) {
        parts.clear();
        var millis = Util.getMillis();
        var delta = millis - orbitTime;
        orbitTime = millis;
        var area = isHovering(ModularSmithingMenu.AREA_X, ModularSmithingMenu.AREA_Y, ModularSmithingMenu.AREA_W, ModularSmithingMenu.AREA_H, mouseX, mouseY);
        if (!area) orbit = (orbit + delta * ORBIT_SPEED) % 360f;

        var data = menu.parts();
        if (data == null || data.isEmpty()) {
            picked = -1;
            return;
        }
        var items = data.items();
        if (picked >= items.size()) picked = -1;
        var centerX = leftPos + ModularSmithingMenu.AREA_X + ModularSmithingMenu.AREA_W / 2f;
        var centerY = topPos + ModularSmithingMenu.AREA_Y + ModularSmithingMenu.AREA_H / 2f;
        var single = items.size() == 1;
        for (var i = 0; i < items.size(); i++) {
            var stack = items.get(i);
            if (stack.isEmpty()) continue;
            var angle = Math.toRadians(orbit + 360f / items.size() * i);
            var sin = single ? 0 : (float) Math.sin(angle);
            var x = single ? centerX : centerX + (float) Math.cos(angle) * ORBIT_X;
            var y = centerY + sin * ORBIT_Y;
            parts.add(new Part(i, stack, x, y, 1 + sin * ORBIT_SCALE));
        }
        parts.sort(Comparator.comparingDouble((Part part) -> part.index() == picked ? Double.MAX_VALUE : part.y()));
    }

    private void renderParts(GuiGraphics g, @Nullable Part hovered) {
        var pose = g.pose();
        for (var part : parts) {
            var selected = part.index() == picked;
            var scale = part.scale() * (part == hovered || selected ? 1 + HOVER_SCALE : 1);
            pose.pushPose();
            pose.translate(part.x(), part.y(), selected ? 32 : 0);
            pose.scale(scale, scale, 1);
            pose.translate(-8, -8, 0);
            if (selected) RenderSystem.setShaderColor(GLOW, GLOW, GLOW, 1f);
            g.renderItem(part.stack(), 0, 0);
            if (selected) RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            pose.popPose();
        }
    }

    private @Nullable Part partAt(double mouseX, double mouseY) {
        for (var i = parts.size() - 1; i >= 0; i--) {
            var part = parts.get(i);
            var half = 8 * part.scale();
            if (mouseX >= part.x() - half && mouseX < part.x() + half && mouseY >= part.y() - half && mouseY < part.y() + half) return part;
        }
        return null;
    }

    private void renderList(GuiGraphics g, int mouseX, int mouseY) {
        var modulars = menu.constructible();
        var x = rightX();
        if (modulars.isEmpty()) {
            var split = font.split(AllLangs.tr("modular_tool_waiting"), RIGHT_W - MARGIN * 2);
            for (var i = 0; i < split.size(); i++) g.drawString(font, split.get(i), x + MARGIN, topPos + MARGIN + i * LINE, 0xA0A0A0, false);
            return;
        }
        g.drawString(font, AllLangs.tr("modular_possible").append(":"), x + MARGIN, topPos + MARGIN, 0xFFFFFF, false);
        listScroll = Mth.clamp(listScroll, 0, Math.max(0, modulars.size() - ROWS));
        var bar = modulars.size() > ROWS;
        var width = RIGHT_W - MARGIN * 2 - (bar ? 4 : 0);
        var selected = menu.selected();
        for (var row = 0; row < Math.min(ROWS, modulars.size()); row++) {
            var index = row + listScroll;
            var y = listY() + row * LINE;
            var hovered = mouseX >= x + MARGIN - 1 && mouseX < x + MARGIN + width + 1 && mouseY >= y - 1 && mouseY < y + LINE - 1;
            if (hovered && index != selected) g.fill(x + MARGIN - 1, y - 1, x + MARGIN + width + 1, y + LINE - 1, 0x30FFFFFF);
            var name = modulars.get(index).getName();
            g.drawString(font, font.split(name, width).getFirst(), x + MARGIN, y, index == selected ? 0xFFFF55 : 0xC0C0C0, false);
        }
        if (!bar) return;
        var top = listY();
        var height = ROWS * LINE;
        var barHeight = Math.max(8, height * ROWS / modulars.size());
        var barY = top + (height - barHeight) * listScroll / (modulars.size() - ROWS);
        var barX = x + RIGHT_W - MARGIN - 2;
        g.fill(barX, top, barX + 2, top + height, 0x60000000);
        g.fill(barX, barY, barX + 2, barY + barHeight, 0xFFA0A0A0);
    }

    private int listAt(double mouseX, double mouseY) {
        var modulars = menu.constructible();
        if (modulars.isEmpty() || mouseX < rightX() + MARGIN - 1 || mouseX >= rightX() + RIGHT_W - MARGIN + 1) return -1;
        var row = Mth.floor((mouseY - listY() + 1) / LINE);
        if (row < 0 || row >= Math.min(ROWS, modulars.size())) return -1;
        return row + listScroll;
    }

    private void renderPanel(GuiGraphics g) {
        var x = leftX();
        var top = topPos + MARGIN;
        var height = LEFT_H - MARGIN * 2;
        var content = lines.size() * LINE;
        scroll = Mth.clamp(scroll, 0, Math.max(0, content - height));
        g.enableScissor(x + MARGIN, top, x + LEFT_W - MARGIN, top + height);
        for (var i = 0; i < lines.size(); i++) g.drawString(font, lines.get(i).text(), x + MARGIN + lines.get(i).indent(), top + i * LINE - scroll, 0xFFFFFF, false);
        g.disableScissor();
        if (content <= height) return;
        var barX = x + LEFT_W - MARGIN;
        var barHeight = Math.max(8, height * height / content);
        var barY = top + (height - barHeight) * scroll / (content - height);
        g.fill(barX, top, barX + 2, top + height, 0x60000000);
        g.fill(barX, barY, barX + 2, barY + barHeight, 0xFFA0A0A0);
    }

    private void press(int id) {
        press(id, true);
    }

    private void press(int id, boolean click) {
        if (minecraft == null || minecraft.player == null || minecraft.gameMode == null) return;
        if (!menu.clickMenuButton(minecraft.player, id)) return;
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        if (click) minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            var part = partAt(mouseX, mouseY);
            if (part != null) {
                picked = part.index() == picked ? -1 : part.index();
                if (minecraft != null) minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
            var index = listAt(mouseX, mouseY);
            if (index >= 0) {
                press(ModularSmithingMenu.SELECT + index);
                return true;
            }
            var extract = menu.getSlot(ModularSmithingMenu.SLOT_B);
            if (!extract.hasItem() && isHovering(extract.x, extract.y, 16, 16, mouseX, mouseY)) {
                if (picked >= 0) press(ModularSmithingMenu.EXTRACT + picked, false);
                picked = -1;
                return true;
            }
            if (parts.isEmpty() && isHovering(ModularSmithingMenu.AREA_X, ModularSmithingMenu.AREA_Y, ModularSmithingMenu.AREA_W, ModularSmithingMenu.AREA_H, mouseX, mouseY)) {
                press(ModularSmithingMenu.TEMPLATE);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (deltaY != 0) {
            if (picked >= 0 && isHovering(ModularSmithingMenu.AREA_X, ModularSmithingMenu.AREA_Y, ModularSmithingMenu.AREA_W, ModularSmithingMenu.AREA_H, mouseX, mouseY)) {
                var data = menu.parts();
                var size = data == null ? 0 : data.items().size();
                press((deltaY > 0 ? ModularSmithingMenu.EARLIER : ModularSmithingMenu.LATER) + picked);
                if (size > 0) picked = Math.floorMod(picked + (deltaY > 0 ? -1 : 1), size);
                return true;
            }
            if (!lines.isEmpty() && mouseX >= leftX() && mouseX < leftX() + LEFT_W && mouseY >= topPos && mouseY < topPos + LEFT_H) {
                scroll -= (int) (deltaY * LINE);
                return true;
            }
            if (mouseX >= rightX() && mouseX < rightX() + RIGHT_W && mouseY >= topPos && mouseY < topPos + RIGHT_H) {
                listScroll -= (int) deltaY;
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    @Override
    protected void renderSlot(GuiGraphics g, Slot slot) {
        if (slot == menu.getSlot(ModularSmithingMenu.BLUEPRINT)) return;
        var ghost = slot == menu.getSlot(ModularSmithingMenu.SLOT_B) ? pickedStack() : ItemStack.EMPTY;
        if (!ghost.isEmpty() && !slot.hasItem()) {
            renderSlotContents(g, ghost, slot, null);
            return;
        }
        super.renderSlot(g, slot);
    }

    private ItemStack pickedStack() {
        var data = menu.parts();
        if (picked < 0 || data == null || picked >= data.items().size()) return ItemStack.EMPTY;
        return data.items().get(picked);
    }

    @Override
    protected void renderSlotHighlight(GuiGraphics g, Slot slot, int mouseX, int mouseY, float partial) {
        if (slot != menu.getSlot(ModularSmithingMenu.BLUEPRINT)) {
            super.renderSlotHighlight(g, slot, mouseX, mouseY, partial);
            return;
        }
        if (partAt(mouseX, mouseY) != null) return;
        g.fill(ModularSmithingMenu.AREA_X, ModularSmithingMenu.AREA_Y, ModularSmithingMenu.AREA_X + ModularSmithingMenu.AREA_W, ModularSmithingMenu.AREA_Y + ModularSmithingMenu.AREA_H, -2130706433);
    }

    @Override
    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        var slot = menu.getSlot(ModularSmithingMenu.BLUEPRINT);
        if (width == 16 && height == 16 && x == slot.x && y == slot.y) return super.isHovering(ModularSmithingMenu.AREA_X, ModularSmithingMenu.AREA_Y, ModularSmithingMenu.AREA_W, ModularSmithingMenu.AREA_H, mouseX, mouseY);
        return super.isHovering(x, y, width, height, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics g, int mouseX, int mouseY) {
        var part = partAt(mouseX, mouseY);
        if (part != null) {
            g.renderTooltip(font, getTooltipFromContainerItem(part.stack()), part.stack().getTooltipImage(), mouseX, mouseY);
            return;
        }
        if (hoveredSlot != null && hoveredSlot == menu.getSlot(ModularSmithingMenu.SLOT_B) && !hoveredSlot.hasItem() && menu.getCarried().isEmpty()) {
            var ghost = pickedStack();
            if (ghost.isEmpty()) g.renderTooltip(font, font.split(AllLangs.tr("modular_extract"), 140), mouseX, mouseY);
            else g.renderTooltip(font, getTooltipFromContainerItem(ghost), ghost.getTooltipImage(), mouseX, mouseY);
            return;
        }
        super.renderTooltip(g, mouseX, mouseY);
    }

    @Override
    public void render(@Nonnull GuiGraphics g, int mouseX, int mouseY, float partial) {
        super.render(g, mouseX, mouseY, partial);
        renderTooltip(g, mouseX, mouseY);
    }
}
