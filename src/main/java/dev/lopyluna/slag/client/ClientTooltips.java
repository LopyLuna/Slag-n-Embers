package dev.lopyluna.slag.client;

import dev.lopyluna.slag.content.items.modular.ModularItem;
import dev.lopyluna.slag.register.AllLangs;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

@SuppressWarnings("unused")
public class ClientTooltips {

    public static void appendHoverTextModularTool(ModularItem item, ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (context.level() instanceof ClientLevel level) {
            var parts = item.getParts(stack);
            if (parts != null && !parts.isEmpty()) {
                var copyParts = parts.itemsCopy();
                if (copyParts.isEmpty()) {
                    tooltip.add(AllLangs.tr("modular_tool_waiting").withStyle(ChatFormatting.GRAY));
                    return;
                }
                if (item.isArmor(stack)) AllLangs.modularArmorStats(tooltip, parts, stack, item);
                else AllLangs.modularToolStats(tooltip, parts, stack, item);
                AllLangs.modularParts(tooltip, copyParts);
            } else tooltip.add(AllLangs.tr("modular_tool_waiting").withStyle(ChatFormatting.GRAY));
        }
    }

}
