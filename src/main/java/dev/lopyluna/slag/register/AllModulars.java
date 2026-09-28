package dev.lopyluna.slag.register;

import dev.lopyluna.slag.content.types.ModularType;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

@SuppressWarnings("unused")
public class AllModulars {

    public static final ModularType AXE = register(new ModularType.Builder("axe")
            .modelType("handheld")
            .traits(AllTraits.TOOL, AllTraits.AXE_MINING, AllTraits.STRIPPING, AllTraits.SHIELD_BREAKING).sortOrder(2)
            .rodCount(2)
            .segments(AllTags.PARTS_AXE_HEADS)
            .itemTags(ItemTags.AXES, Tags.Items.MELEE_WEAPON_TOOLS)
            .betterCombatPreset("bettercombat:axe")
            .register());

    public static final ModularType PICKAXE = register(new ModularType.Builder("pickaxe")
            .modelType("handheld").sortOrder(1)
            .traits(AllTraits.TOOL, AllTraits.PICKAXE_MINING)
            .rodCount(2)
            .segments(AllTags.PARTS_PICKAXE_HEADS)
            .itemTags(ItemTags.PICKAXES)
            .register());

    public static final ModularType SHOVEL = register(new ModularType.Builder("shovel")
            .modelType("handheld").sortOrder(0)
            .traits(AllTraits.TOOL, AllTraits.SHOVEL_MINING, AllTraits.FLATTENING)
            .rodCount(2)
            .segments(AllTags.PARTS_SHOVEL_HEADS)
            .itemTags(ItemTags.SHOVELS)
            .register());

    public static final ModularType HOE = register(new ModularType.Builder("hoe")
            .modelType("handheld").sortOrder(3)
            .traits(AllTraits.TOOL, AllTraits.HOE_MINING, AllTraits.TILLING)
            .rodCount(2)
            .segments(AllTags.PARTS_HOE_HEADS)
            .itemTags(ItemTags.HOES)
            .register());

    public static final ModularType SWORD = register(new ModularType.Builder("sword")
            .modelType("handheld").sortOrder(4)
            .traits(AllTraits.TOOL, AllTraits.SWORD_MINING, AllTraits.SWEEPING)
            .rodCount(1)
            .segments(AllTags.PARTS_SWORD_BLADES, AllTags.PARTS_GUARDS)
            .itemTags(ItemTags.SWORDS, Tags.Items.MELEE_WEAPON_TOOLS)
            .betterCombatPreset("bettercombat:sword")
            .register());

    public static final ModularType MATTOCK = register(new ModularType.Builder("mattock")
            .modelType("handheld").sortOrder(5)
            .traits(AllTraits.TOOL, AllTraits.AXE_MINING, AllTraits.STRIPPING, AllTraits.SHIELD_BREAKING, AllTraits.HOE_MINING, AllTraits.TILLING)
            .rodCount(2)
            .segments(AllTags.PARTS_AXE_HEADS, AllTags.PARTS_HOE_HEADS)
            .itemTags(ItemTags.AXES, ItemTags.HOES, Tags.Items.MELEE_WEAPON_TOOLS)
            .register());

    public static final ModularType PRYBAR = register(new ModularType.Builder("prybar")
            .modelType("handheld").sortOrder(7)
            .traits(AllTraits.TOOL, AllTraits.PICKAXE_MINING, AllTraits.SHOVEL_MINING, AllTraits.FLATTENING)
            .rodCount(2)
            .segments(AllTags.PARTS_PICKAXE_HEADS, AllTags.PARTS_SHOVEL_HEADS)
            .itemTags(ItemTags.PICKAXES, ItemTags.SHOVELS)
            .register());

    public static final ModularType GRAIP = register(new ModularType.Builder("graip")
            .modelType("handheld").sortOrder(8)
            .traits(AllTraits.TOOL, AllTraits.SHOVEL_MINING, AllTraits.FLATTENING, AllTraits.HOE_MINING, AllTraits.TILLING)
            .rodCount(2)
            .segments(AllTags.PARTS_SHOVEL_HEADS, AllTags.PARTS_HOE_HEADS)
            .itemTags(ItemTags.SHOVELS, ItemTags.HOES)
            .register());

    public static final ModularType MALLET = register(new ModularType.Builder("mallet")
            .modelType("handheld").sortOrder(6)
            .traits(AllTraits.TOOL, AllTraits.PICKAXE_MINING, AllTraits.AXE_MINING, AllTraits.STRIPPING, AllTraits.SHIELD_BREAKING)
            .rodCount(2)
            .segments(AllTags.PARTS_PICKAXE_HEADS, AllTags.PARTS_AXE_HEADS)
            .itemTags(ItemTags.PICKAXES, ItemTags.AXES, Tags.Items.MELEE_WEAPON_TOOLS)
            .betterCombatPreset("bettercombat:mace")
            .register());

    public static final ModularType HAMMER = register(new ModularType.Builder("hammer")
            .modelType("handheld").sortOrder(9)
            .traits(AllTraits.TOOL, AllTraits.PICKAXE_MINING, AllTraits.AXE_MINING, AllTraits.STRIPPING, AllTraits.SHIELD_BREAKING, AllTraits.SHOVEL_MINING, AllTraits.FLATTENING)
            .rodCount(3)
            .segments(AllTags.PARTS_PICKAXE_HEADS, AllTags.PARTS_AXE_HEADS, AllTags.PARTS_SHOVEL_HEADS)
            .itemTags(ItemTags.PICKAXES, ItemTags.AXES, ItemTags.SHOVELS, Tags.Items.MELEE_WEAPON_TOOLS)
            .betterCombatPreset("bettercombat:hammer")
            .register());

    public static final ModularType SCYTHE = register(new ModularType.Builder("scythe")
            .modelType("handheld").sortOrder(10)
            .traits(AllTraits.TOOL, AllTraits.HOE_MINING, AllTraits.TILLING, AllTraits.SWORD_MINING, AllTraits.SWEEPING)
            .rodCount(3)
            .segments(AllTags.PARTS_HOE_HEADS, AllTags.PARTS_SWORD_BLADES, AllTags.PARTS_GUARDS)
            .itemTags(ItemTags.HOES, ItemTags.SWORDS, Tags.Items.MELEE_WEAPON_TOOLS)
            .betterCombatPreset("bettercombat:scythe")
            .register());

    public static final ModularType MAUL = register(new ModularType.Builder("maul")
            .modelType("handheld").sortOrder(11)
            .traits(AllTraits.TOOL, AllTraits.PICKAXE_MINING, AllTraits.AXE_MINING, AllTraits.STRIPPING, AllTraits.SHIELD_BREAKING, AllTraits.SWORD_MINING, AllTraits.SWEEPING)
            .rodCount(3)
            .segments(AllTags.PARTS_PICKAXE_HEADS, AllTags.PARTS_AXE_HEADS, AllTags.PARTS_SWORD_BLADES)
            .itemTags(ItemTags.PICKAXES, ItemTags.AXES, ItemTags.SWORDS, Tags.Items.MELEE_WEAPON_TOOLS)
            .betterCombatPreset("bettercombat:mace")
            .register());

    public static final ModularType PAXEL = register(new ModularType.Builder("paxel")
            .modelType("handheld").sortOrder(12)
            .traits(AllTraits.TOOL, AllTraits.PICKAXE_MINING, AllTraits.AXE_MINING, AllTraits.STRIPPING, AllTraits.SHIELD_BREAKING, AllTraits.SHOVEL_MINING, AllTraits.FLATTENING, AllTraits.HOE_MINING, AllTraits.TILLING, AllTraits.SWORD_MINING, AllTraits.SWEEPING)
            .rodCount(3)
            .segments(AllTags.PARTS_PICKAXE_HEADS, AllTags.PARTS_AXE_HEADS, AllTags.PARTS_SHOVEL_HEADS, AllTags.PARTS_HOE_HEADS, AllTags.PARTS_SWORD_BLADES)
            .itemTags(ItemTags.PICKAXES, ItemTags.AXES, ItemTags.SHOVELS, ItemTags.HOES, ItemTags.SWORDS, Tags.Items.MELEE_WEAPON_TOOLS)
            .betterCombatPreset("bettercombat:axe")
            .register());

    public static final ModularType KNIFE = register(new ModularType.Builder("knife")
            .modelType("handheld").sortOrder(17)
            .traits(AllTraits.TOOL, AllTraits.KNIFE_MINING)
            .trait(AllTraits.ATTACK_DAMAGE, -2.5f)
            .trait(AllTraits.ATTACK_SPEED, -0.4f)
            .rodCount(1)
            .segments(AllTags.PARTS_SWORD_BLADES)
            .itemTags(AllTags.KNIVES, AllTags.FD_KNIVES, Tags.Items.MELEE_WEAPON_TOOLS, ItemTags.SHARP_WEAPON_ENCHANTABLE)
            .modLoaded("farmersdelight")
            .betterCombatPreset("bettercombat:dagger")
            .register());

    public static final ModularType HELMET = register(new ModularType.Builder("helmet")
            .modelType("equipable").sortOrder(13)
            .traits(AllTraits.HEAD_SLOT)
            .segments(AllTags.PARTS_PLATES, AllTags.PARTS_HELMETS)
            .itemTags(ItemTags.HEAD_ARMOR, ItemTags.TRIMMABLE_ARMOR)
            .register());

    public static final ModularType CHESTPLATE = register(new ModularType.Builder("chestplate")
            .modelType("equipable").sortOrder(14)
            .traits(AllTraits.CHEST_SLOT)
            .segments(AllTags.PARTS_PLATES, AllTags.PARTS_CHESTPLATES)
            .itemTags(ItemTags.CHEST_ARMOR, ItemTags.TRIMMABLE_ARMOR)
            .register());

    public static final ModularType LEGGINGS = register(new ModularType.Builder("leggings")
            .modelType("equipable").sortOrder(15)
            .traits(AllTraits.LEGS_SLOT)
            .segments(AllTags.PARTS_PLATES, AllTags.PARTS_LEGGINGS)
            .itemTags(ItemTags.LEG_ARMOR, ItemTags.TRIMMABLE_ARMOR)
            .register());

    public static final ModularType BOOTS = register(new ModularType.Builder("boots")
            .modelType("equipable").sortOrder(16)
            .traits(AllTraits.FEET_SLOT)
            .segments(AllTags.PARTS_PLATES, AllTags.PARTS_BOOTS)
            .itemTags(ItemTags.FOOT_ARMOR, ItemTags.TRIMMABLE_ARMOR)
            .register());

    public static final ModularType MACE = register(new ModularType.Builder("mace")
            .sortOrder(100)
            .addSegmentStack(Items.BREEZE_ROD)
            .addSegmentStack(Items.HEAVY_CORE)
            .resultStack(Items.MACE)
            .register());
    public static final ModularType TRIDENT = register(new ModularType.Builder("trident")
            .sortOrder(100)
            .addSegmentStack(Items.QUARTZ)
            .addSegmentStack(Items.PRISMARINE_CRYSTALS)
            .addSegmentStack(Items.PRISMARINE_SHARD, 2)
            .resultStack(Items.TRIDENT)
            .register());
    public static final ModularType BOW = register(new ModularType.Builder("bow")
            .sortOrder(100)
            .addSegmentStack(Items.STRING, 3)
            .addSegmentStack(Items.STICK, 3)
            .resultStack(Items.BOW)
            .register());
    public static final ModularType CROSSBOW = register(new ModularType.Builder("crossbow")
            .sortOrder(100)
            .addSegmentStack(Items.STRING, 2)
            .addSegmentStack(Items.STICK, 3)
            .addSegmentStack(Items.IRON_INGOT, 1)
            .addSegmentStack(Items.TRIPWIRE_HOOK, 1)
            .resultStack(Items.CROSSBOW)
            .register());
    public static final ModularType FISHING_ROD = register(new ModularType.Builder("fishing_rod")
            .sortOrder(100)
            .addSegmentStack(Items.STRING, 2)
            .addSegmentStack(Items.STICK, 3)
            .resultStack(Items.FISHING_ROD)
            .register());
    public static final ModularType SHEARS = register(new ModularType.Builder("shears")
            .sortOrder(100)
            .addSegmentStack(Items.IRON_INGOT, 2)
            .resultStack(Items.SHEARS)
            .register());
    public static final ModularType FLINT_AND_STEEL = register(new ModularType.Builder("flint_and_steel")
            .sortOrder(100)
            .addSegmentStack(Items.FLINT, 1)
            .addSegmentStack(Items.IRON_INGOT, 1)
            .resultStack(Items.FLINT_AND_STEEL)
            .register());
    public static final ModularType BRUSH = register(new ModularType.Builder("brush")
            .sortOrder(100)
            .addSegmentStack(Items.FEATHER, 1)
            .addSegmentStack(Items.COPPER_INGOT, 1)
            .addSegmentStack(Items.STICK, 1)
            .resultStack(Items.BRUSH)
            .register());
    public static final ModularType SPYGLASS = register(new ModularType.Builder("spyglass")
            .sortOrder(100)
            .addSegmentStack(Items.AMETHYST_SHARD, 1)
            .addSegmentStack(Items.COPPER_INGOT, 2)
            .resultStack(Items.SPYGLASS)
            .register());
    public static final ModularType ARROW = register(new ModularType.Builder("arrow")
            .sortOrder(100)
            .addSegmentStack(Items.FLINT, 1)
            .addSegmentStack(Items.STICK, 1)
            .addSegmentStack(Items.FEATHER, 1)
            .resultStack(Items.ARROW, 4)
            .register());
    public static final ModularType SPECTRAL_ARROW = register(new ModularType.Builder("spectral_arrow")
            .sortOrder(100)
            .addSegmentStack(Items.GLOWSTONE_DUST, 4)
            .addSegmentStack(Items.FLINT, 1)
            .addSegmentStack(Items.STICK, 1)
            .addSegmentStack(Items.FEATHER, 1)
            .resultStack(Items.SPECTRAL_ARROW, 8)
            .register());

    public static final ModularType ELYTRA = register(new ModularType.Builder("elytra")
            .sortOrder(100)
            .addSegmentStack(Items.PHANTOM_MEMBRANE, 6)
            .addSegmentStack(Items.PAPER, 4)
            .addSegmentStack(Items.FEATHER, 8)
            .addSegmentStack(Items.WIND_CHARGE, 4)
            .addSegmentStack(Items.DRAGON_BREATH, 2)
            .resultStack(Items.ELYTRA)
            .register());


    private static ModularType register(ModularType part) {
        return AllDynamicTypes.registerModular(part);
    }

    public static void register() {}
}
