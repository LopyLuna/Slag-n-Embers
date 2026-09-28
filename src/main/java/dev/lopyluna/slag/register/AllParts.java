package dev.lopyluna.slag.register;

import dev.lopyluna.slag.content.types.PartType;

@SuppressWarnings("unused")
public class AllParts {

    public static final PartType PICKAXE_HEAD = register(new PartType.Builder("pickaxe_head").setSortOrder(1)
            .multiply(AllTraits.DURABILITY, 1f)
            .multiply(AllTraits.ATTACK_DAMAGE, 0.6f)
            .trait(AllTraits.ATTACK_SPEED, 2.8f)
            .setSegmentPart(AllTags.PARTS_PICKAXE_HEADS)
            .itemTags(AllTags.CAST_PICKAXE_HEADS, AllTags.IMPRINTABLE)
            .register());
    public static final PartType AXE_HEAD = register(new PartType.Builder("axe_head").setSortOrder(2)
            .multiply(AllTraits.DURABILITY, 1f)
            .multiply(AllTraits.ATTACK_DAMAGE, 1.6f)
            .trait(AllTraits.ATTACK_SPEED, 3f)
            .setSegmentPart(AllTags.PARTS_AXE_HEADS)
            .itemTags(AllTags.CAST_AXE_HEADS, AllTags.IMPRINTABLE)
            .register());
    public static final PartType SHOVEL_HEAD = register(new PartType.Builder("shovel_head").setSortOrder(0)
            .multiply(AllTraits.DURABILITY, 1f)
            .multiply(AllTraits.ATTACK_DAMAGE, 0.75f)
            .trait(AllTraits.ATTACK_SPEED, 3f)
            .setSegmentPart(AllTags.PARTS_SHOVEL_HEADS)
            .itemTags(AllTags.CAST_SHOVEL_HEADS, AllTags.IMPRINTABLE)
            .register());
    public static final PartType HOE_HEAD = register(new PartType.Builder("hoe_head").setSortOrder(3)
            .multiply(AllTraits.DURABILITY, 1f)
            .multiply(AllTraits.ATTACK_DAMAGE, 0f)
            .trait(AllTraits.ATTACK_SPEED, 0f)
            .setSegmentPart(AllTags.PARTS_HOE_HEADS)
            .itemTags(AllTags.CAST_HOE_HEADS, AllTags.IMPRINTABLE)
            .register());
    public static final PartType SWORD_BLADE = register(new PartType.Builder("sword_blade").setSortOrder(4)
            .multiply(AllTraits.ATTACK_DAMAGE, 1f)
            .multiply(AllTraits.DURABILITY, 1f)
            .trait(AllTraits.ATTACK_SPEED, 2.4f)
            .setSegmentPart(AllTags.PARTS_SWORD_BLADES)
            .itemTags(AllTags.CAST_SWORD_BLADES, AllTags.IMPRINTABLE)
            .register());
    public static final PartType GUARD = register(new PartType.Builder("guard").setSortOrder(5)
            .multiply(AllTraits.ATTACK_DAMAGE, 1f)
            .trait(AllTraits.ATTACK_SPEED, 2.4f)
            .multiply(AllTraits.DURABILITY, 0.28f)
            .setSegmentPart(AllTags.PARTS_GUARDS)
            .itemTags(AllTags.CAST_GUARDS, AllTags.IMPRINTABLE)
            .register());
    public static final PartType PLATE = register(new PartType.Builder("plate").setSortOrder(6)
            .multiply(AllTraits.ATTACK_DAMAGE, 0f)
            .trait(AllTraits.DURABILITY, 64)
            .setSegmentPart(AllTags.PARTS_PLATES)
            .itemTags(AllTags.CAST_PLATES, AllTags.IMPRINTABLE)
            .register());
    public static final PartType HELMET = register(new PartType.Builder("helmet").setSortOrder(7)
            .multiply(AllTraits.ATTACK_DAMAGE, 0f)
            .trait(AllTraits.DURABILITY, 256)
            .multiply(AllTraits.DURABILITY, 0.32f)
            .multiply(AllTraits.ARMOR, 0.36f)
            .setSegmentPart(AllTags.PARTS_HELMETS)
            .itemTags(AllTags.CAST_HELMETS, AllTags.IMPRINTABLE)
            .register());
    public static final PartType CHESTPLATE = register(new PartType.Builder("chestplate").setSortOrder(8)
            .multiply(AllTraits.ATTACK_DAMAGE, 0f)
            .trait(AllTraits.DURABILITY, 416)
            .multiply(AllTraits.DURABILITY, 0.35f)
            .multiply(AllTraits.ARMOR, 1f)
            .setSegmentPart(AllTags.PARTS_CHESTPLATES)
            .itemTags(AllTags.CAST_CHESTPLATES, AllTags.IMPRINTABLE)
            .register());
    public static final PartType LEGGINGS = register(new PartType.Builder("leggings").setSortOrder(9)
            .multiply(AllTraits.ATTACK_DAMAGE, 0f)
            .trait(AllTraits.DURABILITY, 376)
            .multiply(AllTraits.DURABILITY, 0.35f)
            .multiply(AllTraits.ARMOR, 0.7f)
            .setSegmentPart(AllTags.PARTS_LEGGINGS)
            .itemTags(AllTags.CAST_LEGGINGS, AllTags.IMPRINTABLE)
            .register());
    public static final PartType BOOTS = register(new PartType.Builder("boots").setSortOrder(10)
            .multiply(AllTraits.ATTACK_DAMAGE, 0f)
            .trait(AllTraits.DURABILITY, 296)
            .multiply(AllTraits.DURABILITY, 0.34f)
            .multiply(AllTraits.ARMOR, 0.325f)
            .setSegmentPart(AllTags.PARTS_BOOTS)
            .itemTags(AllTags.CAST_BOOTS, AllTags.IMPRINTABLE)
            .register());
    
    private static PartType register(PartType part) {
        return AllDynamicTypes.registerPart(part);
    }

    public static void register() {}
}
