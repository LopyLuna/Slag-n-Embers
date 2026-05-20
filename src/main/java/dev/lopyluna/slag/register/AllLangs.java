package dev.lopyluna.slag.register;

import com.mojang.datafixers.util.Pair;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.items.dynamic_part.IModularItem;
import dev.lopyluna.slag.content.items.modular.DataDynamicParts;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.List;

import static dev.lopyluna.slag.SlagEmbers.REG;

@SuppressWarnings("unused")
public class AllLangs {

    public static void trAmounts(List<Component> tooltip, String path, int amount, boolean bool) {
        if (amount > 0) {
            if (bool) tooltip.add(trArgs(path, amount, amount > 1 ? "s" : "").withStyle(ChatFormatting.GRAY));
            else tooltip.add(trArgs(path, amount).withStyle(ChatFormatting.GRAY));
        }
    }

    public static MutableComponent trArgs(ResourceLocation id, Object... args) {
        return Component.translatable("tooltip." + id.getNamespace() + "." + id.getPath(), args);
    }
    public static MutableComponent trArgs(String path, Object... args) {
        return Component.translatable("tooltip." + SlagEmbers.MOD_ID + "." + path, args);
    }
    public static MutableComponent tr(String path) {
        return Component.translatable("tooltip." + SlagEmbers.MOD_ID + "." + path);
    }

    @SafeVarargs
    public static void trHoldKeyTooltip(List<Component> tooltip, boolean isDown, String key, String desc, ChatFormatting titleFormat, String title, boolean reverse, ChatFormatting pathFormat, Pair<String, String>... paths) {
        tooltip.add(Component.translatable("tooltip." + SlagEmbers.MOD_ID + "." + desc, tr(key).withStyle(isDown ? ChatFormatting.WHITE : ChatFormatting.GRAY)).withStyle(ChatFormatting.DARK_GRAY));
        if (isDown) {
            if (titleFormat == null || title == null || pathFormat == null || paths == null || paths.length == 0) return;
            tooltip.add(Component.literal(" ").append(tr(title)).append(":").withStyle(titleFormat));
            if (reverse) for (var path : paths) tooltip.add(Component.literal("  "+path.getSecond()+" ").append(tr(path.getFirst())).withStyle(pathFormat));
            else for (var path : paths) tooltip.add(Component.literal("  ").append(tr(path.getFirst())).append(": " + path.getSecond()).withStyle(pathFormat));
        }
    }

    public static Pair<String, String> pair(String key, String value) {
        return Pair.of(key, value);
    }

    public static String format(double number) {
        return ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(number);
    }

    public static void modularParts(List<Component> tooltip, List<ItemStack> toolParts) {
        var shift = Screen.hasShiftDown();
        trHoldKeyTooltip(tooltip, shift, "shift", "parts", null, null, false, null);
        if (shift) {
            tooltip.add(Component.literal(" ").append(tr("modular_parts")).append(":").withStyle(ChatFormatting.GRAY));
            for (var part : toolParts) tooltip.add(Component.literal("  ").append(part.getHoverName()).append(part.getCount() == 1 ? "" : " " + part.getCount() + "x").withColor(FastColor.ARGB32.color(115, 115, 115)));
        }
    }

    public static void modularToolStats(List<Component> tooltip, DataDynamicParts parts, ItemStack stack, IModularItem modular) {
        var ctrl = Screen.hasControlDown();
        trHoldKeyTooltip(tooltip, ctrl, "ctrl", "stats",
                ChatFormatting.GRAY, "modular_stats", false, ChatFormatting.BLUE,
                pair("modular_damage", format(1 + modular.getSharp(stack))),
                pair("modular_durability", modular instanceof Item item && stack.isDamageableItem() ? (item.getMaxDamage(stack)-item.getDamage(stack)) + " / " + item.getMaxDamage(stack) : format(modular.getDura(stack))),
                pair("modular_attack_speed", format(4 - modular.getAttackSpeed(stack))),
                pair("modular_mine_speed", format(modular.getSpeed(stack))),
                pair("modular_tier", format(modular.getTier(stack))),
                pair("modular_enchantability", ""+Math.round(modular.getEnch(stack)))
        );
        //if (ctrl) {
        //    var hammer = modular.getHammerTier(parts, stack);
        //    if (hammer > 0) tooltip.add(Component.literal("  ").append(tr("modular_forging_tier")).append(": " + hammer).withStyle(ChatFormatting.BLUE));
        //}
    }
    public static void modularArmorStats(List<Component> tooltip, DataDynamicParts parts, ItemStack stack, IModularItem modular) {
        var ctrl = Screen.hasControlDown();
        trHoldKeyTooltip(tooltip, ctrl, "ctrl", "stats",
                ChatFormatting.GRAY, "modular_stats", false, ChatFormatting.BLUE,
                pair("modular_defense", format(1 + modular.getDefense(stack))),
                pair("modular_durability", modular instanceof Item item && stack.isDamageableItem() ? (item.getMaxDamage(stack)-item.getDamage(stack)) + " / " + item.getMaxDamage(stack) : format(modular.getDura(stack))),
                pair("modular_knockback_resistance", format(modular.getKbRes(stack))),
                pair("modular_toughness", format(modular.getTough(stack))),
                pair("modular_tier", format(modular.getTier(stack))),
                pair("modular_enchantability", ""+Math.round(modular.getEnch(stack)))
        );
        //if (ctrl) {
        //    var hammer = modular.getHammerTier(parts, stack);
        //    if (hammer > 0) tooltip.add(Component.literal("  ").append(tr("modular_forging_tier")).append(": " + hammer).withStyle(ChatFormatting.BLUE));
        //}
    }

    public static void addTranslations() {
        REG.addRawLang("slag.ponder.smeltery.header", "How to make a Smeltery!");
        REG.addRawLang("slag.ponder.smeltery.text_1", "Toggles Window Visuals");

        REG.addLang("tooltip", SlagEmbers.loc("cast_shift_clear"), "Interact with Empty Hand while Crouching:");
        REG.addLang("tooltip", SlagEmbers.loc("cast_shift_clear.desc"), "Clear Fluid Contents");

        REG.addLang("tooltip", SlagEmbers.loc("modular_tool_invalid"), "Invalid Tool!");
        REG.addLang("tooltip", SlagEmbers.loc("modular_tool_insert_stick"), "Insert %sx Stick%s!");
        REG.addLang("tooltip", SlagEmbers.loc("modular_tool_too_many_stick"), "Too many Sticks");
        REG.addLang("tooltip", SlagEmbers.loc("modular_tool_too_few_stick"), "Too few Sticks!");
        //REG.addLang("tooltip", SlagEmbers.loc("modular_tool_anvil"), "Must look at an Anvil atleast to combine said planned tool!!!");
        //REG.addLang("tooltip", SlagEmbers.loc("modular_tool_smithing_table"), "Must look at a Smithing Table atleast to combine said planned tool!!!");
        //REG.addLang("tooltip", SlagEmbers.loc("modular_tool_crafting_table"), "Must look at a Crafting Table atleast to combine said planned tool!!!");
        REG.addLang("tooltip", SlagEmbers.loc("modular_tool_crafting_hammer_weak"), "Use Rock/Wood type item or Mallet/Hammers on the Blueprint to combine!");
        REG.addLang("tooltip", SlagEmbers.loc("modular_tool_crafting_hammer"), "Use Mallet/Hammers with Forging Tier >= '%s' on the Blueprint to combine!");

        REG.addLang("tooltip", SlagEmbers.loc("modular_tool_waiting"), "Insert Parts to get started!");

        REG.addLang("tooltip", SlagEmbers.loc("modular_stats"), "Modular Stats");
        REG.addLang("tooltip", SlagEmbers.loc("modular_defense"), "Defense");
        REG.addLang("tooltip", SlagEmbers.loc("modular_knockback_resistance"), "Knockback Resistance");
        REG.addLang("tooltip", SlagEmbers.loc("modular_toughness"), "Toughness");
        REG.addLang("tooltip", SlagEmbers.loc("modular_damage"), "Damage");
        REG.addLang("tooltip", SlagEmbers.loc("modular_durability"), "Durability");
        REG.addLang("tooltip", SlagEmbers.loc("modular_attack_speed"), "Attack Speed");
        REG.addLang("tooltip", SlagEmbers.loc("modular_mine_speed"), "Mine Speed");
        REG.addLang("tooltip", SlagEmbers.loc("modular_tier"), "Tier");
        REG.addLang("tooltip", SlagEmbers.loc("modular_enchantability"), "Enchantability");
        REG.addLang("tooltip", SlagEmbers.loc("modular_forging_tier"), "Forging Tier");
        REG.addLang("tooltip", SlagEmbers.loc("modular_parts"), "Modular Parts");

        REG.addLang("tooltip", SlagEmbers.loc("imprint"), "Right-click with certain items to imprint the mold.");
        REG.addLang("tooltip", SlagEmbers.loc("clear_imprint"), "Right-click with nothing to clear imprint.");

        REG.addLang("tooltip", SlagEmbers.loc("alt"), "Alt");
        REG.addLang("tooltip", SlagEmbers.loc("shift"), "Shift");
        REG.addLang("tooltip", SlagEmbers.loc("ctrl"), "Ctrl");
        REG.addLang("tooltip", SlagEmbers.loc("lmb"), "Left Click");
        REG.addLang("tooltip", SlagEmbers.loc("rmb"), "Right Click");

        REG.addLang("tooltip", SlagEmbers.loc("construct"), "%s + %s in hand to construct!");

        REG.addLang("tooltip", SlagEmbers.loc("stats"), "Hold [%s] for Stats");
        REG.addLang("tooltip", SlagEmbers.loc("parts"), "Hold [%s] for Parts");
        REG.addLang("tooltip", SlagEmbers.loc("desc"), "Hold [%s] for Info");
        REG.addLang("tooltip", SlagEmbers.loc("dynamic_multiblock"), "Dynamic Multiblock");

        REG.addLang("tooltip", SlagEmbers.loc("blocks"), "%s Block%s");
        REG.addLang("tooltip", SlagEmbers.loc("ingots"), "%s Ingot%s");
        REG.addLang("tooltip", SlagEmbers.loc("nuggets"), "%s Nugget%s");
        REG.addLang("tooltip", SlagEmbers.loc("gems"), "%s Gem%s");
        REG.addLang("tooltip", SlagEmbers.loc("shards"), "%s Shard%s");
        REG.addLang("tooltip", SlagEmbers.loc("dusts"), "%s Dust%s");
        REG.addLang("tooltip", SlagEmbers.loc("grits"), "%s Grit%s");
        REG.addLang("tooltip", SlagEmbers.loc("balls"), "%s Ball%s");
        REG.addLang("tooltip", SlagEmbers.loc("buckets"), "%s Bucket%s");
        REG.addLang("tooltip", SlagEmbers.loc("mb"), "%s mB");

        for (var material : List.of("purpur", "flint", "bone", "nautilus", "rose_gold", "deep_alloy",
                "wooden", "stone", "quartz", "iron", "golden", "diamond", "netherite", "redstone", "copper",
                "emerald", "lapis", "amethyst", "obsidian", "blue_icy", "echo", "prismarine", "glowstone"
        )) for (var mixture : List.of("pickaxe", "axe", "shovel", "hoe", "sword", "mattock", "prybar", "graip", "mallet", "hammer", "scythe", "maul", "paxel")) {
            var name = material + "_" + mixture;
            REG.addRawLang(Util.makeDescriptionId("item", SlagEmbers.loc(name)), RegistrateLangProvider.toEnglishName(name));
        }
        for (var material : List.of("purpur", "flint", "bone", "nautilus", "rose_gold", "deep_alloy",
                "wooden", "stone", "quartz", "iron", "golden", "diamond", "netherite", "redstone", "copper",
                "emerald", "lapis", "amethyst", "obsidian", "blue_icy", "echo", "prismarine", "glowstone"
        )) for (var armor : List.of("helmet", "chestplate", "leggings", "boots")) {
            var name = material + "_" + armor;
            REG.addRawLang(Util.makeDescriptionId("item", SlagEmbers.loc(name)), RegistrateLangProvider.toEnglishName(name));
        }
    }
}
