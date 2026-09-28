package dev.lopyluna.slag.register;

import com.mojang.datafixers.util.Pair;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.items.dynamic_part.IModularItem;
import dev.lopyluna.slag.content.items.modular.DataDynamicParts;
import dev.lopyluna.slag.content.temperature.Temperatures.Heat;
import dev.lopyluna.slag.content.temperature.Temperatures.Tiers;
import dev.lopyluna.slag.content.temperature.Temperatures.Type;
import dev.lopyluna.slag.content.traits.Trait;
import dev.lopyluna.slag.content.traits.Traits;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import javax.annotation.Nullable;
import java.util.List;

import static dev.lopyluna.slag.SlagEmbers.REG;

@SuppressWarnings("unused")
public class AllLangs {

    public static void trAmounts(List<Component> tooltip, String path, int amount, boolean bool) {
        if (amount > 0) {
            if (bool) tooltip.add(trArgs(amount == 1 ? path + ".one" : path, amount).withStyle(ChatFormatting.GRAY));
            else tooltip.add(trArgs(path, amount).withStyle(ChatFormatting.GRAY));
        }
    }

    public static MutableComponent trArgs(ResourceLocation id, Object... args) {
        return Component.translatable("tooltip." + id.getNamespace() + "." + id.getPath(), args);
    }
    public static MutableComponent trArgs(String path, Object... args) {
        return Component.translatable("tooltip." + SlagEmbers.MOD_ID + "." + path, args);
    }
    public static MutableComponent container(String path) {
        return Component.translatable("container." + SlagEmbers.MOD_ID + "." + path);
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
            for (var path : paths) {
                if (path == null) continue;
                if (reverse) tooltip.add(Component.literal("  "+path.getSecond()+" ").append(tr(path.getFirst())).withStyle(pathFormat));
                else tooltip.add(Component.literal("  ").append(tr(path.getFirst())).append(": " + path.getSecond()).withStyle(pathFormat));
            }
        }
    }

    public static MutableComponent heat(Heat heat, float speed) {
        return trArgs("temperature", heat.tier == null ? Component.empty() : tr("temperature." + heat.tier.id), format(speed));
    }

    public static MutableComponent requires(Tiers tier, @Nullable Type type) {
        if (type == null) return trArgs("temperature.required", tr("temperature." + tier.id));
        return trArgs("temperature.required_type", tr("heat_type." + type.id), tr("temperature." + tier.id));
    }

    public static Pair<String, String> pair(String key, String value) {
        return Pair.of(key, value);
    }

    public static @Nullable Pair<String, String> stat(String key, double value, String display) {
        return value == 0 ? null : Pair.of(key, display);
    }

    public static String format(double number) {
        return ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(number);
    }

    public static MutableComponent trait(Trait trait) {
        var name = trait.type().name();
        if (trait.value() != 0) name.append(" " + format(trait.value()));
        return name.withStyle(style -> style.withColor(trait.type().color));
    }

    public static void traits(List<Component> tooltip, Traits traits) {
        for (var trait : traits.visible) tooltip.add(trait(trait));
    }

    public static void modularTraits(List<Component> tooltip, boolean ctrl, IModularItem modular, ItemStack stack) {
        if (!ctrl) return;
        var traits = modular.getTraits(stack).visible;
        if (traits.isEmpty()) return;
        tooltip.add(Component.literal(" ").append(tr("modular_traits")).append(":").withStyle(ChatFormatting.GRAY));
        for (var trait : traits) tooltip.add(Component.literal("  ").append(trait(trait)));
    }

    public static void modularParts(List<Component> tooltip, List<ItemStack> toolParts) {
        var shift = Screen.hasShiftDown();
        trHoldKeyTooltip(tooltip, shift, "shift", "parts", null, null, false, null);
        if (shift) {
            tooltip.add(Component.literal(" ").append(tr("modular_parts")).append(":").withStyle(ChatFormatting.GRAY));
            for (var part : toolParts) tooltip.add(Component.literal("  ").append(part.getHoverName()).append(part.getCount() == 1 ? "" : " " + part.getCount() + "x").withColor(FastColor.ARGB32.color(115, 115, 115)));
        }
    }

    private static String durability(ItemStack stack, IModularItem modular, float dura) {
        return modular instanceof Item item && stack.isDamageableItem() ? (item.getMaxDamage(stack) - item.getDamage(stack)) + " / " + item.getMaxDamage(stack) : format(dura);
    }

    public static void modularToolStats(List<Component> tooltip, DataDynamicParts parts, ItemStack stack, IModularItem modular) {
        var ctrl = Screen.hasControlDown();
        var sharp = modular.getSharp(stack);
        var dura = modular.getDura(stack);
        var attackSpeed = modular.getAttackSpeed(stack);
        var speed = modular.getSpeed(stack);
        var tier = modular.getTier(stack);
        var ench = Math.round(modular.getEnch(stack));
        trHoldKeyTooltip(tooltip, ctrl, "ctrl", "stats",
                ChatFormatting.GRAY, "modular_stats", false, ChatFormatting.BLUE,
                stat("modular_damage", sharp, format(1 + sharp)),
                stat("modular_durability", dura, durability(stack, modular, dura)),
                stat("modular_attack_speed", attackSpeed, format(4 - attackSpeed)),
                stat("modular_mine_speed", speed, format(speed)),
                stat("modular_tier", tier, format(tier)),
                stat("modular_enchantability", ench, "" + ench)
        );
        modularTraits(tooltip, ctrl, modular, stack);
    }
    public static void modularArmorStats(List<Component> tooltip, DataDynamicParts parts, ItemStack stack, IModularItem modular) {
        var ctrl = Screen.hasControlDown();
        var defense = Math.round(modular.getDefense(stack));
        var dura = modular.getDura(stack);
        var kbRes = modular.getKbRes(stack);
        var tough = Math.round(modular.getTough(stack));
        var tier = modular.getTier(stack);
        var ench = Math.round(modular.getEnch(stack));
        trHoldKeyTooltip(tooltip, ctrl, "ctrl", "stats",
                ChatFormatting.GRAY, "modular_stats", false, ChatFormatting.BLUE,
                stat("modular_defense", defense, format(defense)),
                stat("modular_durability", dura, durability(stack, modular, dura)),
                stat("modular_knockback_resistance", kbRes, format(kbRes)),
                stat("modular_toughness", tough, format(tough)),
                stat("modular_tier", tier, format(tier)),
                stat("modular_enchantability", ench, "" + ench)
        );
        modularTraits(tooltip, ctrl, modular, stack);
    }

    private static String partName(String id) {
        return switch (id) {
            case "plate" -> "Armor Plate";
            case "helmet" -> "Head Piece";
            case "chestplate" -> "Chest Piece";
            case "leggings" -> "Leg Piece";
            case "boots" -> "Foot Piece";
            default -> RegistrateLangProvider.toEnglishName(id);
        };
    }

    public static void addTranslations() {
        for (var trait : AllDynamicTypes.getAllTraits()) REG.addLang("trait", trait.id, RegistrateLangProvider.toEnglishName(trait.id.getPath()));
        for (var part : AllDynamicTypes.getAllParts()) REG.addLang("part", part.id, partName(part.id.getPath()));
        for (var material : AllDynamicTypes.getAllMaterials()) REG.addLang("material", material.id, RegistrateLangProvider.toEnglishName(material.id.getPath()));
        for (var modular : AllDynamicTypes.getAllModulars()) REG.addLang("modular", modular.id, RegistrateLangProvider.toEnglishName(modular.id.getPath()));
        REG.addRawLang("item.slag.part_name", "%1$s %2$s");
        REG.addRawLang("item.slag.modular_name", "%1$s %2$s");
        REG.addRawLang("item.slag.material_pair", "%1$s %2$s");
        REG.addRawLang("slag.ponder.smeltery.header", "Building a Smeltery");
        REG.addRawLang("slag.ponder.smeltery.text_1", "Crucibles are heated by the blocks placed beneath them");
        REG.addRawLang("slag.ponder.smeltery.text_2", "Crucibles placed next to or on top of each other connect into one big tank");
        REG.addRawLang("slag.ponder.smeltery.text_3", "Sneak and Right-click with an empty hand to toggle the windows");
        REG.addRawLang("slag.ponder.smeltery.text_4", "Melters turn items into molten fluid and pour it into the tank behind them");
        REG.addRawLang("slag.ponder.smeltery.text_5", "The Fluid Interface shows every fluid inside the Crucible");
        REG.addRawLang("slag.ponder.smeltery.text_6", "Drains move fluid from the container behind them into any fluid container below");
        REG.addRawLang("slag.ponder.crucible.header", "Heating and Filling Crucibles");
        REG.addRawLang("slag.ponder.crucible.text_1", "Crucibles take their heat from the blocks directly below them");
        REG.addRawLang("slag.ponder.crucible.text_2", "Their temperature is the average of every block underneath, so cover the whole floor for the most heat");
        REG.addRawLang("slag.ponder.crucible.text_3", "Molten fluids can be poured in with Buckets, or fed in by Melters");
        REG.addRawLang("slag.ponder.crucible.text_4", "When the Crucible is hot enough, fluids that make an alloy will mix on their own");
        REG.addRawLang("slag.ponder.crucible.text_5", "Sneak and Right-click with an empty hand to toggle the windows");
        REG.addRawLang("slag.ponder.melter.header", "Melting Items");
        REG.addRawLang("slag.ponder.melter.text_1", "Melters turn items into molten fluid");
        REG.addRawLang("slag.ponder.melter.text_2", "They need a heat source directly below them");
        REG.addRawLang("slag.ponder.melter.text_3", "Items can be thrown in, inserted with Hoppers, or placed through its menu");
        REG.addRawLang("slag.ponder.melter.text_4", "Molten fluid is poured into the tank directly behind it");
        REG.addRawLang("slag.ponder.melter.text_5", "Weaker heat sources, such as Magma Blocks, melt items more slowly");
        REG.addRawLang("slag.ponder.casting.header", "Casting Molten Fluids");
        REG.addRawLang("slag.ponder.casting.text_1", "Drains take fluid from the container they are attached to, such as a Crucible, and pour it downwards");
        REG.addRawLang("slag.ponder.casting.text_2", "A Casting Table catches the fluid, but needs a mold to shape it");
        REG.addRawLang("slag.ponder.casting.text_3", "Right-click the Drain to start pouring. It stops once the container below is full");
        REG.addRawLang("slag.ponder.casting.text_4", "Once it has cooled, Right-click with an empty hand to take out the result");
        REG.addRawLang("slag.ponder.casting.text_5", "Casting Basins work the same way, but cast whole blocks without a mold");
        REG.addRawLang("slag.ponder.casting.text_6", "A powered Drain keeps pouring whenever there is room below");
        REG.addRawLang("slag.ponder.fluid_interface.header", "Choosing which Fluid to Pour");
        REG.addRawLang("slag.ponder.fluid_interface.text_1", "A Crucible can hold many fluids at once, stacked in layers");
        REG.addRawLang("slag.ponder.fluid_interface.text_2", "Right-click the Fluid Interface to see every fluid inside");
        REG.addRawLang("slag.ponder.fluid_interface.text_3", "Selecting a fluid moves it to the bottom of the Crucible");
        REG.addRawLang("slag.ponder.fluid_interface.text_4", "Drains always pour from the bottom layer first");

        REG.addLang("tooltip", SlagEmbers.loc("cast_shift_clear"), "Interact with Empty Hand while Crouching:");
        REG.addLang("tooltip", SlagEmbers.loc("cast_shift_clear.desc"), "Clear Fluid Contents");

        REG.addLang("tooltip", SlagEmbers.loc("temperature"), "%s (%sx Speed)");
        REG.addLang("tooltip", SlagEmbers.loc("temperature.required"), "Requires %s");
        REG.addLang("tooltip", SlagEmbers.loc("temperature.required_type"), "Requires %s %s");
        for (var tier : Tiers.values()) REG.addLang("tooltip", SlagEmbers.loc("temperature." + tier.id), RegistrateLangProvider.toEnglishName(tier.id));
        for (var type : Type.values()) REG.addLang("tooltip", SlagEmbers.loc("heat_type." + type.id), RegistrateLangProvider.toEnglishName(type.id));

        REG.addLang("tooltip", SlagEmbers.loc("modular_tool_waiting"), "Add parts to start building");
        REG.addLang("tooltip", SlagEmbers.loc("modular_template"), "Place in a Smithing Table to start building a tool");
        REG.addLang("container", SlagEmbers.loc("modular_smithing"), "Modular Smithing");

        REG.addLang("tooltip", SlagEmbers.loc("modular_stats"), "Modular Stats");
        REG.addLang("tooltip", SlagEmbers.loc("modular_traits"), "Traits");
        REG.addLang("tooltip", SlagEmbers.loc("modular_defense"), "Defense");
        REG.addLang("tooltip", SlagEmbers.loc("modular_knockback_resistance"), "Knockback Resistance");
        REG.addLang("tooltip", SlagEmbers.loc("modular_toughness"), "Toughness");
        REG.addLang("tooltip", SlagEmbers.loc("modular_damage"), "Damage");
        REG.addLang("tooltip", SlagEmbers.loc("modular_durability"), "Durability");
        REG.addLang("tooltip", SlagEmbers.loc("modular_attack_speed"), "Attack Speed");
        REG.addLang("tooltip", SlagEmbers.loc("modular_mine_speed"), "Mining Speed");
        REG.addLang("tooltip", SlagEmbers.loc("modular_tier"), "Tier");
        REG.addLang("tooltip", SlagEmbers.loc("modular_enchantability"), "Enchantability");
        REG.addLang("tooltip", SlagEmbers.loc("modular_parts"), "Modular Parts");
        REG.addLang("tooltip", SlagEmbers.loc("modular_possible"), "Possible Items");
        REG.addLang("tooltip", SlagEmbers.loc("modular_possible_parts"), "Possible Parts");
        REG.addLang("tooltip", SlagEmbers.loc("modular_extract"), "Click a part on the tool to take it out");

        REG.addLang("tooltip", SlagEmbers.loc("imprint"), "Imprint a Shape:");
        REG.addLang("tooltip", SlagEmbers.loc("imprint.desc"), "In your inventory, right-click certain items onto this mold, such as ingots or tool parts");
        REG.addLang("tooltip", SlagEmbers.loc("clear_imprint"), "Clear the Imprint:");
        REG.addLang("tooltip", SlagEmbers.loc("clear_imprint.desc"), "Right-click it with an empty cursor, or Sneak + Use while holding it");

        REG.addLang("tooltip", SlagEmbers.loc("shift"), "Shift");
        REG.addLang("tooltip", SlagEmbers.loc("ctrl"), "Ctrl");

        REG.addLang("tooltip", SlagEmbers.loc("stats"), "Hold [%s] for Stats");
        REG.addLang("tooltip", SlagEmbers.loc("parts"), "Hold [%s] for Parts");
        REG.addLang("tooltip", SlagEmbers.loc("dynamic_multiblock"), "Dynamic Multiblock");

        REG.addLang("tooltip", SlagEmbers.loc("blocks"), "%s Blocks");
        REG.addLang("tooltip", SlagEmbers.loc("blocks.one"), "%s Block");
        REG.addLang("tooltip", SlagEmbers.loc("ingots"), "%s Ingots");
        REG.addLang("tooltip", SlagEmbers.loc("ingots.one"), "%s Ingot");
        REG.addLang("tooltip", SlagEmbers.loc("nuggets"), "%s Nuggets");
        REG.addLang("tooltip", SlagEmbers.loc("nuggets.one"), "%s Nugget");
        REG.addLang("tooltip", SlagEmbers.loc("gems"), "%s Gems");
        REG.addLang("tooltip", SlagEmbers.loc("gems.one"), "%s Gem");
        REG.addLang("tooltip", SlagEmbers.loc("shards"), "%s Shards");
        REG.addLang("tooltip", SlagEmbers.loc("shards.one"), "%s Shard");
        REG.addLang("tooltip", SlagEmbers.loc("dusts"), "%s Dusts");
        REG.addLang("tooltip", SlagEmbers.loc("dusts.one"), "%s Dust");
        REG.addLang("tooltip", SlagEmbers.loc("grits"), "%s Grits");
        REG.addLang("tooltip", SlagEmbers.loc("grits.one"), "%s Grit");
        REG.addLang("tooltip", SlagEmbers.loc("balls"), "%s Balls");
        REG.addLang("tooltip", SlagEmbers.loc("balls.one"), "%s Ball");
        REG.addLang("tooltip", SlagEmbers.loc("buckets"), "%s Buckets");
        REG.addLang("tooltip", SlagEmbers.loc("buckets.one"), "%s Bucket");
        REG.addLang("tooltip", SlagEmbers.loc("mb"), "%s mB");
    }
}
