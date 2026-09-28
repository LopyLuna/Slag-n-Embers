package dev.lopyluna.slag.register;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.traits.TraitType;
import dev.lopyluna.slag.content.traits.effects.*;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.Tags;

@SuppressWarnings("unused")
public class AllTraits {
    private static int order;

    public static final TraitType ATTACK_DAMAGE = register(new TraitType.Builder("attack_damage").hidden()
            .effect(AttributeEffect.held(Attributes.ATTACK_DAMAGE, Item.BASE_ATTACK_DAMAGE_ID)));
    public static final TraitType ATTACK_SPEED = register(new TraitType.Builder("attack_speed").hidden()
            .effect(AttributeEffect.held(Attributes.ATTACK_SPEED, Item.BASE_ATTACK_SPEED_ID).scaled(-1f)));
    public static final TraitType ARMOR = register(new TraitType.Builder("armor").hidden()
            .effect(AttributeEffect.equipped(Attributes.ARMOR, SlagEmbers.locMC("armor")).rounded()));
    public static final TraitType ARMOR_TOUGHNESS = register(new TraitType.Builder("armor_toughness").hidden()
            .effect(AttributeEffect.equipped(Attributes.ARMOR_TOUGHNESS, SlagEmbers.locMC("armor")).rounded()));
    public static final TraitType KNOCKBACK_RESISTANCE = register(new TraitType.Builder("knockback_resistance").hidden()
            .effect(AttributeEffect.equipped(Attributes.KNOCKBACK_RESISTANCE, SlagEmbers.locMC("armor"))));
    public static final TraitType DURABILITY = register(new TraitType.Builder("durability").hidden().partScale(.25f)
            .effect(new StatEffect(StatEffect.Stat.MAX_DAMAGE)));
    public static final TraitType MINING_SPEED = register(new TraitType.Builder("mining_speed").hidden()
            .effect(new StatEffect(StatEffect.Stat.MINING_SPEED)));
    public static final TraitType MINING_TIER = register(new TraitType.Builder("mining_tier").hidden()
            .effect(new StatEffect(StatEffect.Stat.MINING_TIER)));
    public static final TraitType ENCHANTABILITY = register(new TraitType.Builder("enchantability").hidden()
            .effect(new StatEffect(StatEffect.Stat.ENCHANTABILITY)));
    public static final TraitType STACK_SIZE = register(new TraitType.Builder("stack_size").hidden()
            .effect(new StatEffect(StatEffect.Stat.MAX_STACK_SIZE)));

    public static final TraitType TOOL = register(new TraitType.Builder("tool").hidden()
            .effect(FlagEffect.TOOL));
    public static final TraitType PICKAXE_MINING = register(new TraitType.Builder("pickaxe_mining").hidden()
            .effects(MiningEffect.of(BlockTags.MINEABLE_WITH_PICKAXE), AbilitiesEffect.of(ItemAbilities.PICKAXE_DIG)));
    public static final TraitType AXE_MINING = register(new TraitType.Builder("axe_mining").hidden()
            .effects(MiningEffect.of(BlockTags.MINEABLE_WITH_AXE), AbilitiesEffect.of(ItemAbilities.AXE_DIG)));
    public static final TraitType SHOVEL_MINING = register(new TraitType.Builder("shovel_mining").hidden()
            .effects(MiningEffect.of(BlockTags.MINEABLE_WITH_SHOVEL), AbilitiesEffect.of(ItemAbilities.SHOVEL_DIG)));
    public static final TraitType HOE_MINING = register(new TraitType.Builder("hoe_mining").hidden()
            .effects(MiningEffect.of(BlockTags.MINEABLE_WITH_HOE), AbilitiesEffect.of(ItemAbilities.HOE_DIG)));
    public static final TraitType SWORD_MINING = register(new TraitType.Builder("sword_mining").hidden()
            .effects(MiningEffect.of(BlockTags.SWORD_EFFICIENT).cost(2), MiningEffect.of(Blocks.COBWEB).speed(2f).cost(2), AbilitiesEffect.of(ItemAbilities.SWORD_DIG)));
    public static final TraitType KNIFE_MINING = register(new TraitType.Builder("knife_mining").hidden()
            .effects(MiningEffect.of(AllTags.KNIFE_MINEABLE), AbilitiesEffect.of(ItemAbilities.SHEARS_CARVE, ItemAbilities.SWORD_DIG, ItemAbility.get("knife_dig"), ItemAbility.get("knife_harvest"))));
    public static final TraitType SWEEPING = register(new TraitType.Builder("sweeping").hidden()
            .effect(AbilitiesEffect.of(ItemAbilities.SWORD_SWEEP)));
    public static final TraitType STRIPPING = register(new TraitType.Builder("stripping").hidden()
            .effects(BlockActionEffect.STRIP, AbilitiesEffect.of(ItemAbilities.AXE_STRIP, ItemAbilities.AXE_SCRAPE, ItemAbilities.AXE_WAX_OFF)));
    public static final TraitType FLATTENING = register(new TraitType.Builder("flattening").hidden()
            .effects(BlockActionEffect.FLATTEN, AbilitiesEffect.of(ItemAbilities.SHOVEL_FLATTEN, ItemAbilities.SHOVEL_DOUSE)));
    public static final TraitType TILLING = register(new TraitType.Builder("tilling").hidden()
            .effects(BlockActionEffect.TILL, AbilitiesEffect.of(ItemAbilities.HOE_TILL)));
    public static final TraitType HARVESTING = register(new TraitType.Builder("harvesting").hidden()
            .effect(BlockActionEffect.HARVEST));
    public static final TraitType SHIELD_BREAKING = register(new TraitType.Builder("shield_breaking").hidden()
            .effect(FlagEffect.DISABLE_SHIELD));

    public static final TraitType HEAD_SLOT = register(new TraitType.Builder("head_slot").hidden()
            .effect(new EquipmentSlotEffect(EquipmentSlot.HEAD)));
    public static final TraitType CHEST_SLOT = register(new TraitType.Builder("chest_slot").hidden()
            .effect(new EquipmentSlotEffect(EquipmentSlot.CHEST)));
    public static final TraitType LEGS_SLOT = register(new TraitType.Builder("legs_slot").hidden()
            .effect(new EquipmentSlotEffect(EquipmentSlot.LEGS)));
    public static final TraitType FEET_SLOT = register(new TraitType.Builder("feet_slot").hidden()
            .effect(new EquipmentSlotEffect(EquipmentSlot.FEET)));

    public static final TraitType FIRE_RESISTANT = register(new TraitType.Builder("fire_resistant").color(0x613E37)
            .effects(new ComponentsEffect(DataComponentPatch.builder().set(DataComponents.FIRE_RESISTANT, Unit.INSTANCE).build()), new DamageImmunityEffect(DamageTypeTags.IS_FIRE)));
    public static final TraitType PIGLIN_LOVED = register(new TraitType.Builder("piglin_loved").color(0xF2C93D)
            .effects(ItemTagsEffect.of(ItemTags.PIGLIN_LOVED), FlagEffect.PIGLIN_NEUTRAL));
    public static final TraitType SILENT = register(new TraitType.Builder("silent").color(0x009295)
            .effects(ItemTagsEffect.of(ItemTags.DAMPENS_VIBRATIONS), FlagEffect.SILENT));
    public static final TraitType VEIN_MINING = register(new TraitType.Builder("vein_mining").value(8)
            .effect(ChainBreakEffect.of(Tags.Blocks.ORES)));
    public static final TraitType TIMBER = register(new TraitType.Builder("timber").value(32)
            .effect(ChainBreakEffect.of(BlockTags.LOGS)));
    public static final TraitType AUTO_SMELTING = register(new TraitType.Builder("auto_smelting").color(0xFFC22B)
            .effect(AutoSmeltEffect.INSTANCE));
    public static final TraitType GLIDING = register(new TraitType.Builder("gliding").color(0x613E37)
            .effect(FlagEffect.GLIDING));

    private static TraitType register(TraitType.Builder builder) {
        order += 10;
        return AllDynamicTypes.registerTrait(builder.sortOrder(order).register());
    }

    public static void register() {}
}
