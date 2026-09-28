package dev.lopyluna.slag.register;

import dev.lopyluna.slag.content.types.Incompatible;
import dev.lopyluna.slag.content.types.MaterialType;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;

@SuppressWarnings("unused")
public class AllMaterials {

    public static final MaterialType WOOD = register(new MaterialType.Builder("wooden", () -> Ingredient.of(ItemTags.PLANKS)).setSortOrder(10)
            .trait(AllTraits.ATTACK_DAMAGE, 3f)
            .trait(AllTraits.DURABILITY, 64)
            .trait(AllTraits.MINING_TIER, 1)
            .trait(AllTraits.MINING_SPEED, 2)
            .trait(AllTraits.ENCHANTABILITY, 15)
            .trait(AllTraits.ARMOR, 1f)
            .incompatible(AllMaterials::noArmor)
            .setTexture("soft")
            .register());
            
    //public static final MaterialType GLOWSTONE = register(new MaterialType.Builder("glowstone", () -> Ingredient.of(Items.GLOWSTONE)).setSortOrder(220)
    //        .trait(AllTraits.ATTACK_DAMAGE, 3.5f)
    //        .trait(AllTraits.DURABILITY, 96)
    //        .trait(AllTraits.MINING_TIER, 1)
    //        .trait(AllTraits.MINING_SPEED, 1)
    //        .trait(AllTraits.ENCHANTABILITY, 12)
    //        .trait(AllTraits.ARMOR, 2f)
    //        .trait(AllTraits.KNOCKBACK_RESISTANCE, 0.1F)
    //        .setTexture("shiny")
    //        .register());
            
    public static final MaterialType STONE = register(new MaterialType.Builder("stone", () -> Ingredient.of(ItemTags.STONE_TOOL_MATERIALS)).setSortOrder(20)
            .trait(AllTraits.ATTACK_DAMAGE, 4f)
            .trait(AllTraits.DURABILITY, 128)
            .trait(AllTraits.MINING_TIER, 3)
            .trait(AllTraits.MINING_SPEED, 4)
            .trait(AllTraits.ENCHANTABILITY, 5)
            .trait(AllTraits.ARMOR, 2f)
            .trait(AllTraits.ARMOR_TOUGHNESS, 1f)
            .trait(AllTraits.KNOCKBACK_RESISTANCE, 0.1F)
            .incompatible(AllMaterials::noArmor)
            .setTexture("soft")
            .register());
            
    //public static final MaterialType REDSTONE = register(new MaterialType.Builder("redstone", () -> Ingredient.of(Tags.Items.DUSTS_REDSTONE)).setSortOrder(160)
    //        .trait(AllTraits.ATTACK_DAMAGE, 4f)
    //        .trait(AllTraits.DURABILITY, 160)
    //        .trait(AllTraits.MINING_TIER, 2)
    //        .trait(AllTraits.MINING_SPEED, 3)
    //        .trait(AllTraits.ENCHANTABILITY, 16)
    //        .trait(AllTraits.ARMOR, 3f)
    //        .setTexture("shiny")
    //        .moltenFluid(AllFluids.MOLTEN_REDSTONE::getSource)
    //        .register());
            
    public static final MaterialType LAPIS = register(new MaterialType.Builder("lapis", () -> Ingredient.of(Tags.Items.GEMS_LAPIS)).setSortOrder(170)
            .trait(AllTraits.ATTACK_DAMAGE, 4.5f)
            .trait(AllTraits.DURABILITY, 384)
            .trait(AllTraits.MINING_TIER, 3)
            .trait(AllTraits.MINING_SPEED, 3)
            .trait(AllTraits.ENCHANTABILITY, 32)
            .trait(AllTraits.ARMOR, 4f)
            .trait(AllTraits.ARMOR_TOUGHNESS, 1f)
            .moltenFluid(AllFluids.MOLTEN_LAPIS::getSource)
            .register());
            
    public static final MaterialType COPPER = register(new MaterialType.Builder("copper", () -> Ingredient.of(Tags.Items.INGOTS_COPPER)).setSortOrder(30)
            .trait(AllTraits.ATTACK_DAMAGE, 4.5f)
            .trait(AllTraits.DURABILITY, 192)
            .trait(AllTraits.MINING_TIER, 3)
            .trait(AllTraits.MINING_SPEED, 5)
            .trait(AllTraits.ENCHANTABILITY, 8)
            .trait(AllTraits.ARMOR, 4f)
            .moltenFluid(AllFluids.MOLTEN_COPPER::getSource)
            .register());
            
    public static final MaterialType AMETHYST = register(new MaterialType.Builder("amethyst", () -> Ingredient.of(Tags.Items.GEMS_AMETHYST)).setSortOrder(180)
            .trait(AllTraits.ATTACK_DAMAGE, 5.5f)
            .trait(AllTraits.DURABILITY, 224)
            .trait(AllTraits.MINING_TIER, 3)
            .trait(AllTraits.MINING_SPEED, 7)
            .setTexture("metal")
            .trait(AllTraits.ENCHANTABILITY, 18)
            .trait(AllTraits.ARMOR, 5f)
            .moltenFluid(AllFluids.MOLTEN_AMETHYST::getSource)
            .register());
            
    public static final MaterialType GOLD = register(new MaterialType.Builder("golden", () -> Ingredient.of(Tags.Items.INGOTS_GOLD)).setSortOrder(50)
            .trait(AllTraits.PIGLIN_LOVED)
            .trait(AllTraits.ATTACK_DAMAGE, 3f)
            .trait(AllTraits.DURABILITY, 32)
            .trait(AllTraits.MINING_TIER, 2)
            .trait(AllTraits.MINING_SPEED, 12)
            .trait(AllTraits.ENCHANTABILITY, 22)
            .trait(AllTraits.ARMOR, 5f)
            .setTexture("shiny")
            .moltenFluid(AllFluids.MOLTEN_GOLD::getSource)
            .register());
            
    public static final MaterialType IRON = register(new MaterialType.Builder("iron", () -> Ingredient.of(Tags.Items.INGOTS_IRON)).setSortOrder(40)
            .trait(AllTraits.ATTACK_DAMAGE, 5f)
            .trait(AllTraits.DURABILITY, 256)
            .trait(AllTraits.MINING_TIER, 4)
            .trait(AllTraits.MINING_SPEED, 6)
            .trait(AllTraits.ENCHANTABILITY, 14)
            .trait(AllTraits.ARMOR, 6f)
            .moltenFluid(AllFluids.MOLTEN_IRON::getSource)
            .register());
            
    public static final MaterialType ROSE_GOLD = register(new MaterialType.Builder("rose_gold", () -> Ingredient.of(AllTags.itemC("ingots/rose_gold"))).setSortOrder(110)
            .trait(AllTraits.ATTACK_DAMAGE, 6f)
            .trait(AllTraits.DURABILITY, 480)
            .trait(AllTraits.MINING_TIER, 4)
            .trait(AllTraits.MINING_SPEED, 10)
            .trait(AllTraits.ENCHANTABILITY, 15)
            .trait(AllTraits.ARMOR, 6f)
            .setTexture("shiny")
            .moltenFluid(AllFluids.MOLTEN_ROSE_GOLD::getSource)
            .register());
            
    public static final MaterialType QUARTZ = register(new MaterialType.Builder("quartz", () -> Ingredient.of(Tags.Items.GEMS_QUARTZ)).setSortOrder(150)
            .trait(AllTraits.ATTACK_DAMAGE, 6.5f)
            .trait(AllTraits.DURABILITY, 288)
            .trait(AllTraits.MINING_TIER, 4)
            .trait(AllTraits.MINING_SPEED, 7)
            .trait(AllTraits.ENCHANTABILITY, 16)
            .trait(AllTraits.ARMOR, 7f)
            .setTexture("metal")
            .moltenFluid(AllFluids.MOLTEN_QUARTZ::getSource)
            .register());
            
    public static final MaterialType EMERALD = register(new MaterialType.Builder("emerald", () -> Ingredient.of(Tags.Items.GEMS_EMERALD)).setSortOrder(100)
            .trait(AllTraits.ATTACK_DAMAGE, 5.5f)
            .trait(AllTraits.DURABILITY, 512)
            .trait(AllTraits.MINING_TIER, 5)
            .trait(AllTraits.MINING_SPEED, 8)
            .trait(AllTraits.ENCHANTABILITY, 9)
            .trait(AllTraits.ARMOR, 7f)
            .trait(AllTraits.ARMOR_TOUGHNESS, 1f)
            .setTexture("shiny")
            .moltenFluid(AllFluids.MOLTEN_EMERALD::getSource)
            .register());
            
    public static final MaterialType DEEP_ALLOY_MATERIAL = register(new MaterialType.Builder("deep_alloy", () -> Ingredient.of(AllTags.itemC("ingots/deep_alloy"))).setSortOrder(140)
            .trait(AllTraits.ATTACK_DAMAGE, 5f)
            .trait(AllTraits.DURABILITY, 704)
            .trait(AllTraits.MINING_TIER, 4)
            .trait(AllTraits.MINING_SPEED, 5)
            .trait(AllTraits.ENCHANTABILITY, 11)
            .trait(AllTraits.ARMOR, 6f)
            .trait(AllTraits.ARMOR_TOUGHNESS, 1f)
            .trait(AllTraits.KNOCKBACK_RESISTANCE, 0.1F)
            .setTexture("shiny")
            .trait(AllTraits.FIRE_RESISTANT)
            .register());
            
    //public static final MaterialType PRISMARINE = register(new MaterialType.Builder("prismarine", () -> Ingredient.of(Tags.Items.GEMS_PRISMARINE)).setSortOrder(130)
    //        .trait(AllTraits.ATTACK_DAMAGE, 6f)
    //        .trait(AllTraits.DURABILITY, 1280)
    //        .trait(AllTraits.MINING_TIER, 4)
    //        .trait(AllTraits.MINING_SPEED, 9)
    //        .trait(AllTraits.ENCHANTABILITY, 11)
    //        .trait(AllTraits.ARMOR, 6f)
    //        .moltenFluid(AllFluids.MOLTEN_PRISMARINE::getSource)
    //        .register());
            
    //public static final MaterialType BLUE_ICE = register(new MaterialType.Builder("blue_icy", () -> Ingredient.of(Items.BLUE_ICE)).setSortOrder(120)
    //        .trait(AllTraits.ATTACK_DAMAGE, 7f)
    //        .trait(AllTraits.DURABILITY, 768)
    //        .trait(AllTraits.MINING_TIER, 4)
    //        .trait(AllTraits.MINING_SPEED, 6)
    //        .trait(AllTraits.ENCHANTABILITY, 6)
    //        .trait(AllTraits.ARMOR, 5f)
    //        .setTexture("shiny")
    //        .register());
            
    public static final MaterialType DIAMOND = register(new MaterialType.Builder("diamond", () -> Ingredient.of(Tags.Items.GEMS_DIAMOND)).setSortOrder(60)
            .trait(AllTraits.ATTACK_DAMAGE, 6f)
            .trait(AllTraits.DURABILITY, 1024)
            .trait(AllTraits.MINING_TIER, 5)
            .trait(AllTraits.MINING_SPEED, 8)
            .trait(AllTraits.ENCHANTABILITY, 10)
            .trait(AllTraits.ARMOR, 8f)
            .trait(AllTraits.ARMOR_TOUGHNESS, 2f)
            .setTexture("shiny")
            .moltenFluid(AllFluids.MOLTEN_DIAMOND::getSource)
            .register());
            
    public static final MaterialType OBSIDIAN = register(new MaterialType.Builder("obsidian", () -> Ingredient.of(Tags.Items.OBSIDIANS)).setSortOrder(90)
            .trait(AllTraits.ATTACK_DAMAGE, 6.5f)
            .trait(AllTraits.DURABILITY, 2560)
            .trait(AllTraits.MINING_TIER, 5)
            .trait(AllTraits.MINING_SPEED, 5)
            .trait(AllTraits.ENCHANTABILITY, 21)
            .trait(AllTraits.ARMOR, 7f)
            .trait(AllTraits.ARMOR_TOUGHNESS, 5f)
            .trait(AllTraits.KNOCKBACK_RESISTANCE, 0.1F)
            .setTexture("shiny")
            .moltenFluid(AllFluids.MOLTEN_OBSIDIAN::getSource)
            .register());
            
    public static final MaterialType ECHO = register(new MaterialType.Builder("echo", () -> Ingredient.of(Items.ECHO_SHARD)).setSortOrder(80)
            .moltenFluid(AllFluids.MOLTEN_ECHO::getSource)
            .trait(AllTraits.ATTACK_DAMAGE, 7f)
            .trait(AllTraits.DURABILITY, 1536)
            .trait(AllTraits.MINING_TIER, 6)
            .trait(AllTraits.MINING_SPEED, 10)
            .trait(AllTraits.ENCHANTABILITY, 24)
            .trait(AllTraits.ARMOR, 7f)
            .trait(AllTraits.ARMOR_TOUGHNESS, 3f)
            .trait(AllTraits.KNOCKBACK_RESISTANCE, 0.1F)
            .trait(AllTraits.SILENT)
            .setTexture("metal")
            .register());
            
    public static final MaterialType NETHERITE = register(new MaterialType.Builder("netherite", () -> Ingredient.of(Tags.Items.INGOTS_NETHERITE)).setSortOrder(70)
            .trait(AllTraits.ATTACK_DAMAGE, 7f)
            .trait(AllTraits.DURABILITY, 2048)
            .trait(AllTraits.MINING_TIER, 6)
            .trait(AllTraits.MINING_SPEED, 9)
            .trait(AllTraits.ENCHANTABILITY, 15)
            .trait(AllTraits.ARMOR, 8f)
            .trait(AllTraits.ARMOR_TOUGHNESS, 3f)
            .trait(AllTraits.KNOCKBACK_RESISTANCE, 0.1F)
            .setTexture("metal")
            .trait(AllTraits.FIRE_RESISTANT)
            .moltenFluid(AllFluids.MOLTEN_NETHERITE::getSource)
            .register());
            
    //public static final MaterialType POPPED_CHORUS = register(new MaterialType.Builder("purpur", () -> Ingredient.of(Items.POPPED_CHORUS_FRUIT)).setSortOrder(190)
    //        .trait(AllTraits.ATTACK_DAMAGE, 6.5f)
    //        .trait(AllTraits.DURABILITY, 832)
    //        .trait(AllTraits.MINING_TIER, 4)
    //        .trait(AllTraits.MINING_SPEED, 5)
    //        .trait(AllTraits.ENCHANTABILITY, 16)
    //        .trait(AllTraits.ARMOR, 6f)
    //        .trait(AllTraits.ARMOR_TOUGHNESS, 1f)
    //        .trait(AllTraits.KNOCKBACK_RESISTANCE, 0.1F)
    //        .register());
            
    //public static final MaterialType NAUTILUS = register(new MaterialType.Builder("nautilus", () -> Ingredient.of(Items.NAUTILUS_SHELL)).setSortOrder(200)
    //        .trait(AllTraits.ATTACK_DAMAGE, 6f)
    //        .trait(AllTraits.DURABILITY, 1120)
    //        .trait(AllTraits.MINING_TIER, 3)
    //        .trait(AllTraits.MINING_SPEED, 11)
    //        .trait(AllTraits.ENCHANTABILITY, 19)
    //        .trait(AllTraits.ARMOR, 5f)
    //        .trait(AllTraits.ARMOR_TOUGHNESS, 1f)
    //        .trait(AllTraits.KNOCKBACK_RESISTANCE, 0.1F)
    //        .setTexture("soft")
    //        .register());
            
    public static final MaterialType BONE = register(new MaterialType.Builder("bone", () -> Ingredient.of(Tags.Items.BONES)).setSortOrder(210)
            .trait(AllTraits.ATTACK_DAMAGE, 5.5f)
            .trait(AllTraits.DURABILITY, 144)
            .trait(AllTraits.MINING_TIER, 3)
            .trait(AllTraits.MINING_SPEED, 4)
            .trait(AllTraits.ENCHANTABILITY, 8)
            .trait(AllTraits.ARMOR, 3f)
            .trait(AllTraits.ARMOR_TOUGHNESS, 3f)
            .incompatible(AllMaterials::noArmor)
            .setTexture("soft")
            .register());
            
    public static final MaterialType FLINT = register(new MaterialType.Builder("flint", () -> Ingredient.of(Items.FLINT)).setSortOrder(230)
            .trait(AllTraits.ATTACK_DAMAGE, 5f)
            .trait(AllTraits.DURABILITY, 112)
            .trait(AllTraits.MINING_TIER, 2)
            .trait(AllTraits.MINING_SPEED, 3)
            .trait(AllTraits.ENCHANTABILITY, 5)
            .trait(AllTraits.ARMOR, 2f)
            .trait(AllTraits.ARMOR_TOUGHNESS, 1f)
            .incompatible(AllMaterials::noArmor)
            .setTexture("shiny")
            .register());

    public static final MaterialType ALUMINIUM = register(compat("aluminium", 240)
            .trait(AllTraits.ATTACK_DAMAGE, 4f)
            .trait(AllTraits.DURABILITY, 208)
            .trait(AllTraits.MINING_TIER, 3)
            .trait(AllTraits.MINING_SPEED, 8)
            .trait(AllTraits.ENCHANTABILITY, 16)
            .trait(AllTraits.ARMOR, 4f)
            .setTexture("soft")
            .moltenFluid(AllFluids.MOLTEN_ALUMINIUM::getSource)
            .register());

    public static final MaterialType TIN = register(compat("tin", 250)
            .trait(AllTraits.ATTACK_DAMAGE, 4.5f)
            .trait(AllTraits.DURABILITY, 208)
            .trait(AllTraits.MINING_TIER, 4)
            .trait(AllTraits.MINING_SPEED, 6)
            .trait(AllTraits.ENCHANTABILITY, 14)
            .trait(AllTraits.ARMOR, 4f)
            .setTexture("shiny")
            .moltenFluid(AllFluids.MOLTEN_TIN::getSource)
            .register());

    public static final MaterialType ZINC = register(compat("zinc", 260)
            .trait(AllTraits.ATTACK_DAMAGE, 5.5f)
            .trait(AllTraits.DURABILITY, 272)
            .trait(AllTraits.MINING_TIER, 4)
            .trait(AllTraits.MINING_SPEED, 7)
            .trait(AllTraits.ENCHANTABILITY, 12)
            .trait(AllTraits.ARMOR, 6f)
            .setTexture("shiny")
            .moltenFluid(AllFluids.MOLTEN_ZINC::getSource)
            .register());

    public static final MaterialType LEAD = register(compat("lead", 270)
            .trait(AllTraits.ATTACK_DAMAGE, 5f)
            .trait(AllTraits.DURABILITY, 272)
            .trait(AllTraits.MINING_TIER, 4)
            .trait(AllTraits.MINING_SPEED, 4)
            .trait(AllTraits.ENCHANTABILITY, 10)
            .trait(AllTraits.ARMOR, 6f)
            .trait(AllTraits.ARMOR_TOUGHNESS, 1f)
            .trait(AllTraits.KNOCKBACK_RESISTANCE, 0.1F)
            .moltenFluid(AllFluids.MOLTEN_LEAD::getSource)
            .register());

    public static final MaterialType SILVER = register(compat("silver", 280)
            .trait(AllTraits.ATTACK_DAMAGE, 5.5f)
            .trait(AllTraits.DURABILITY, 288)
            .trait(AllTraits.MINING_TIER, 4)
            .trait(AllTraits.MINING_SPEED, 10)
            .trait(AllTraits.ENCHANTABILITY, 22)
            .trait(AllTraits.ARMOR, 6f)
            .trait(AllTraits.ARMOR_TOUGHNESS, 1f)
            .setTexture("shiny")
            .moltenFluid(AllFluids.MOLTEN_SILVER::getSource)
            .register());

    public static final MaterialType NICKEL = register(compat("nickel", 290)
            .trait(AllTraits.ATTACK_DAMAGE, 5f)
            .trait(AllTraits.DURABILITY, 288)
            .trait(AllTraits.MINING_TIER, 4)
            .trait(AllTraits.MINING_SPEED, 6)
            .trait(AllTraits.ENCHANTABILITY, 13)
            .trait(AllTraits.ARMOR, 5f)
            .setTexture("metal")
            .moltenFluid(AllFluids.MOLTEN_NICKEL::getSource)
            .register());

    public static final MaterialType BRASS = register(compat("brass", 300)
            .trait(AllTraits.ATTACK_DAMAGE, 4.5f)
            .trait(AllTraits.DURABILITY, 256)
            .trait(AllTraits.MINING_TIER, 4)
            .trait(AllTraits.MINING_SPEED, 8)
            .trait(AllTraits.ENCHANTABILITY, 18)
            .trait(AllTraits.ARMOR, 5f)
            .setTexture("shiny")
            .moltenFluid(AllFluids.MOLTEN_BRASS::getSource)
            .register());

    public static final MaterialType ELECTRUM = register(compat("electrum", 310)
            .trait(AllTraits.PIGLIN_LOVED)
            .trait(AllTraits.ATTACK_DAMAGE, 6f)
            .trait(AllTraits.DURABILITY, 352)
            .trait(AllTraits.MINING_TIER, 4)
            .trait(AllTraits.MINING_SPEED, 13)
            .trait(AllTraits.ENCHANTABILITY, 26)
            .trait(AllTraits.ARMOR, 6f)
            .trait(AllTraits.ARMOR_TOUGHNESS, 1f)
            .setTexture("shiny")
            .moltenFluid(AllFluids.MOLTEN_ELECTRUM::getSource)
            .register());

    public static final MaterialType BRONZE = register(compat("bronze", 320)
            .trait(AllTraits.ATTACK_DAMAGE, 5.5f)
            .trait(AllTraits.DURABILITY, 384)
            .trait(AllTraits.MINING_TIER, 4)
            .trait(AllTraits.MINING_SPEED, 6)
            .trait(AllTraits.ENCHANTABILITY, 12)
            .trait(AllTraits.ARMOR, 6f)
            .trait(AllTraits.ARMOR_TOUGHNESS, 1f)
            .setTexture("metal")
            .moltenFluid(AllFluids.MOLTEN_BRONZE::getSource)
            .register());

    public static final MaterialType CAST_IRON = register(compat("cast_iron", 330)
            .trait(AllTraits.ATTACK_DAMAGE, 6f)
            .trait(AllTraits.DURABILITY, 384)
            .trait(AllTraits.MINING_TIER, 4)
            .trait(AllTraits.MINING_SPEED, 5)
            .trait(AllTraits.ENCHANTABILITY, 8)
            .trait(AllTraits.ARMOR, 6f)
            .trait(AllTraits.ARMOR_TOUGHNESS, 1f)
            .trait(AllTraits.KNOCKBACK_RESISTANCE, 0.05F)
            .setTexture("metal")
            .moltenFluid(AllFluids.MOLTEN_CAST_IRON::getSource)
            .register());

    public static final MaterialType INVAR = register(compat("invar", 340)
            .trait(AllTraits.ATTACK_DAMAGE, 5.5f)
            .trait(AllTraits.DURABILITY, 512)
            .trait(AllTraits.MINING_TIER, 4)
            .trait(AllTraits.MINING_SPEED, 6)
            .trait(AllTraits.ENCHANTABILITY, 12)
            .trait(AllTraits.ARMOR, 6f)
            .trait(AllTraits.ARMOR_TOUGHNESS, 1f)
            .moltenFluid(AllFluids.MOLTEN_INVAR::getSource)
            .register());

    public static final MaterialType PLATINUM = register(compat("platinum", 350)
            .trait(AllTraits.ATTACK_DAMAGE, 5f)
            .trait(AllTraits.DURABILITY, 640)
            .trait(AllTraits.MINING_TIER, 5)
            .trait(AllTraits.MINING_SPEED, 11)
            .trait(AllTraits.ENCHANTABILITY, 22)
            .trait(AllTraits.ARMOR, 6f)
            .trait(AllTraits.ARMOR_TOUGHNESS, 1f)
            .setTexture("shiny")
            .moltenFluid(AllFluids.MOLTEN_PLATINUM::getSource)
            .register());

    public static final MaterialType STEEL = register(compat("steel", 360)
            .trait(AllTraits.ATTACK_DAMAGE, 6f)
            .trait(AllTraits.DURABILITY, 768)
            .trait(AllTraits.MINING_TIER, 5)
            .trait(AllTraits.MINING_SPEED, 7)
            .trait(AllTraits.ENCHANTABILITY, 12)
            .trait(AllTraits.ARMOR, 7f)
            .trait(AllTraits.ARMOR_TOUGHNESS, 1f)
            .setTexture("metal")
            .moltenFluid(AllFluids.MOLTEN_STEEL::getSource)
            .register());

    public static final MaterialType OSMIUM = register(compat("osmium", 370)
            .trait(AllTraits.ATTACK_DAMAGE, 6f)
            .trait(AllTraits.DURABILITY, 1152)
            .trait(AllTraits.MINING_TIER, 5)
            .trait(AllTraits.MINING_SPEED, 7)
            .trait(AllTraits.ENCHANTABILITY, 12)
            .trait(AllTraits.ARMOR, 7f)
            .trait(AllTraits.ARMOR_TOUGHNESS, 2f)
            .setTexture("shiny")
            .moltenFluid(AllFluids.MOLTEN_OSMIUM::getSource)
            .register());

    public static final MaterialType TUNGSTEN = register(compat("tungsten", 380)
            .trait(AllTraits.ATTACK_DAMAGE, 6.5f)
            .trait(AllTraits.DURABILITY, 1408)
            .trait(AllTraits.MINING_TIER, 6)
            .trait(AllTraits.MINING_SPEED, 7)
            .trait(AllTraits.ENCHANTABILITY, 10)
            .trait(AllTraits.ARMOR, 8f)
            .trait(AllTraits.ARMOR_TOUGHNESS, 2f)
            .trait(AllTraits.KNOCKBACK_RESISTANCE, 0.1F)
            .moltenFluid(AllFluids.MOLTEN_TUNGSTEN::getSource)
            .register());

    private static MaterialType.Builder compat(String name, int sortOrder) {
        var ingots = AllTags.itemC("ingots/" + name);
        return new MaterialType.Builder(name, () -> Ingredient.of(ingots)).setSortOrder(sortOrder).requiresTag(ingots);
    }

    private static void noArmor(Incompatible.Builder incompatible) {
        incompatible.parts(AllParts.PLATE, AllParts.HELMET, AllParts.CHESTPLATE, AllParts.LEGGINGS, AllParts.BOOTS);
    }

    private static MaterialType register(MaterialType material) {
        return AllDynamicTypes.registerMaterial(material);
    }

    public static void register() {}
}

