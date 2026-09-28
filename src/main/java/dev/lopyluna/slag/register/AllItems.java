package dev.lopyluna.slag.register;

import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import com.tterrag.registrate.util.entry.ItemEntry;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.AllUtils;
import dev.lopyluna.slag.content.blocks.casting.CastItem;
import dev.lopyluna.slag.content.blocks.melter.MelterBE;
import dev.lopyluna.slag.content.conditions.CastOnlyCondition;
import dev.lopyluna.slag.content.datagen.DualCookingRecipeBuilder;
import dev.lopyluna.slag.content.datagen.MeltingRecipeBuilder;
import dev.lopyluna.slag.content.datagen.TableCastingRecipeBuilder;
import dev.lopyluna.slag.content.items.dynamic_mold.DynamicMoldItem;
import dev.lopyluna.slag.content.items.dynamic_part.DynamicPartItem;
import dev.lopyluna.slag.content.items.modular.ModularEquipablesItem;
import dev.lopyluna.slag.content.types.Incompatible;
import dev.lopyluna.slag.content.types.MaterialType;
import dev.lopyluna.slag.content.types.ModularType;
import dev.lopyluna.slag.content.types.PartType;
import net.createmod.catnip.data.Iterate;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.ArrayList;
import java.util.List;

import static com.tterrag.registrate.providers.RegistrateRecipeProvider.has;
import static dev.lopyluna.slag.SlagEmbers.REG;
import static dev.lopyluna.slag.content.AllUtils.compressible9x;

@SuppressWarnings("unused")
public class AllItems {
    public static final int TERRACOTTA_MOLD_USES = 12;

    public static final List<PartType> PART_TYPES = AllDynamicTypes.getAllPartsList();
    public static final List<ModularType> MODULAR_TYPES = AllDynamicTypes.getAllModularsList();
    public static final List<MaterialType> MATERIAL_TYPES = AllDynamicTypes.getAllMaterialsList();

    public static final ItemEntry<DynamicPartItem> DYNAMIC_PART = REG.item("dynamic_part", DynamicPartItem::new)
            .model((c, p) -> p.withExistingParent(c.getName(), "item/generated"))
            .register();

    private static final List<ResourceKey<TrimMaterial>> VANILLA_TRIM_MATERIALS = List.of(TrimMaterials.QUARTZ, TrimMaterials.IRON, TrimMaterials.NETHERITE, TrimMaterials.REDSTONE, TrimMaterials.COPPER, TrimMaterials.GOLD, TrimMaterials.EMERALD, TrimMaterials.DIAMOND, TrimMaterials.LAPIS, TrimMaterials.AMETHYST);
    public static final ItemEntry<ModularEquipablesItem> MODULAR_ITEM = REG.item("modular_item", ModularEquipablesItem::new)
            .model((c, p) -> {
                p.withExistingParent(c.getName(), SlagEmbers.loc("item/no_transform_item"));
                p.withExistingParent(c.getName() + "_blueprint", "item/generated").texture("layer0", SlagEmbers.loc("item/blueprint"));
                p.withExistingParent(c.getName() + "_baked", "item/generated");
                p.withExistingParent(c.getName() + "_baked_handheld", "item/handheld");
                var equipableModel = p.withExistingParent(c.getName() + "_baked_equipable", "item/generated");
                equipableModel.transforms().transform(ItemDisplayContext.HEAD).scale(0f).end();

                var bakedModel = p.withExistingParent(c.getName() + "_baked_trim", "item/generated");
                AllUtils.TRIM_MATERIALS.forEach((trim, value) -> {
                    bakedModel.override().predicate(SlagEmbers.loc("armor_type"), 1).predicate(SlagEmbers.locMC("trim_type"), value)
                            .model(p.withExistingParent(c.getName() + "_baked_boots_trim_" + trim.location().getPath(), "item/generated")
                                    .texture("layer0", SlagEmbers.locMC("trims/items/boots_trim_" + trim.location().getPath()))).end();
                    bakedModel.override().predicate(SlagEmbers.loc("armor_type"), 2).predicate(SlagEmbers.locMC("trim_type"), value)
                            .model(p.withExistingParent(c.getName() + "_baked_chestplate_trim_" + trim.location().getPath(), "item/generated")
                                    .texture("layer0", SlagEmbers.locMC("trims/items/chestplate_trim_" + trim.location().getPath()))).end();
                    bakedModel.override().predicate(SlagEmbers.loc("armor_type"), 3).predicate(SlagEmbers.locMC("trim_type"), value)
                            .model(p.withExistingParent(c.getName() + "_baked_helmet_trim_" + trim.location().getPath(), "item/generated")
                                    .texture("layer0", SlagEmbers.locMC("trims/items/helmet_trim_" + trim.location().getPath()))).end();
                    bakedModel.override().predicate(SlagEmbers.loc("armor_type"), 4).predicate(SlagEmbers.locMC("trim_type"), value)
                            .model(p.withExistingParent(c.getName() + "_baked_leggings_trim_" + trim.location().getPath(), "item/generated")
                                    .texture("layer0", SlagEmbers.locMC("trims/items/leggings_trim_" + trim.location().getPath()))).end();
                });
            }).recipe((c, p) -> {
                ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, c.get())
                        .requires(Items.PAPER)
                        .requires(Items.PAPER)
                        .requires(Items.PAPER)
                        .requires(Items.CLAY_BALL)
                        .unlockedBy("has_paper", has(Items.PAPER))
                        .save(p, SlagEmbers.loc("crafting/" + c.getName()));

                var materials = AllDynamicTypes.getAllMaterials();
                var parts = AllDynamicTypes.getAllParts();
                if (materials.isEmpty() || parts.isEmpty()) return;
                var item = DYNAMIC_PART.get();
                for (var material : materials) for (var part : parts) {
                    var partID = part.id.getPath();
                    var matID = material.id.getPath();
                    if (!(partID.equals("axe_head") || partID.equals("pickaxe_head") || partID.equals("shovel_head") || partID.equals("hoe_head") || partID.equals("sword_blade") || partID.equals("guard") || partID.equals("plate") || partID.equals("helmet") || partID.equals("chestplate") || partID.equals("leggings") || partID.equals("boots"))) continue;
                    if (!Incompatible.compatible(material, part)) continue;
                    var output = withConditions(p, material, part);
                    var castable = castSize(material, part) > 0 && getCast(part) != null;
                    var paperOutput = castable ? withConditions(p, material, part, new CastOnlyCondition(miningTier(material))) : output;
                    var stack = item.getDefaultInstance();
                    item.setMaterialType(stack, material);
                    item.setPartType(stack, part);

                    //noinspection StatementWithEmptyBody
                    if (matID.equals("netherite")) {
                        //SMITHING
                    } else buildPattern(ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, stack), part)
                            .define('M', material.repairMaterials.get())
                            .define('R', Items.PAPER)
                            .unlockedBy("has_paper", has(Items.PAPER))
                            .save(paperOutput, SlagEmbers.loc("crafting/parts/" + partID + "_" + matID));

                    var fluid = material.moltenFluid.get();
                    if (fluid != null) {
                        var cast = getCast(part);
                        var size = castSize(material, part);

                        if (size > 0) {
                            if (cast != null) TableCastingRecipeBuilder.create(stack, fluid, size, cast)
                                    .unlockedBy("has_table", has(AllBlocks.TABLE.get()))
                                    .save(output, SlagEmbers.loc("casting/table/" + partID + "_" + matID));

                            MeltingRecipeBuilder.create(fluid, size, stack)
                                    .unlockedBy("has_melter", has(AllBlocks.MELTER.get()))
                                    .save(output, SlagEmbers.loc("melting/" + partID + "_" + matID));
                        }
                    }
                }
            }).lang("Modular Template")
            .register();

    public static final ItemEntry<DynamicMoldItem> SANDSTONE_MOLD = REG.item("sandstone_mold", DynamicMoldItem::new)
            .recipe((c, p) -> ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, c.get(), 4)
                    .requires(Tags.Items.SANDSTONE_UNCOLORED_BLOCKS).unlockedBy("has_sandstone", has(Tags.Items.SANDSTONE_UNCOLORED_BLOCKS)).save(p, SlagEmbers.loc("crafting/" + c.getName())))
            .model((c, p) -> moldModels(c.getName(), p)).tag(AllTags.MOLDS_SINGLE)
            .register();
    public static final ItemEntry<DynamicMoldItem> TERRACOTTA_MOLD = REG.item("terracotta_mold", p -> new DynamicMoldItem(p, false, TERRACOTTA_MOLD_USES))
            .recipe((c, p) -> ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, c.get(), 2)
                    .requires(ItemTags.TERRACOTTA).unlockedBy("has_terracotta", has(ItemTags.TERRACOTTA)).save(p, SlagEmbers.loc("crafting/" + c.getName())))
            .model((c, p) -> moldModels(c.getName(), p)).tag(AllTags.MOLDS_REUSABLE)
            .register();
    public static final ItemEntry<DynamicMoldItem> CAST_IRON_MOLD = REG.item("cast_iron_mold", p -> new DynamicMoldItem(p, true, 0))
            .recipe((c, p) -> {
                TableCastingRecipeBuilder.create(c.get(), AllFluids.MOLTEN_CAST_IRON.getSource(), MelterBE.INGOT_SIZE)
                        .castItem(CastItem.imprint(Ingredient.of(AllTags.IMPRINTABLE)))
                        .unlockedBy("has_cast_iron", has(AllTags.itemC("ingots/cast_iron"))).save(p, SlagEmbers.loc("casting/table/cast_iron_mold"));

                MeltingRecipeBuilder.create(AllFluids.MOLTEN_CAST_IRON.getSource(), MelterBE.INGOT_SIZE, c.get())
                        .unlockedBy("has_mold", has(c.get())).save(p, SlagEmbers.loc("melting/cast_iron_mold"));
            })
            .model((c, p) -> moldModels(c.getName(), p)).tag(AllTags.MOLDS_REUSABLE)
            .register();

    public static final List<ItemEntry<DynamicMoldItem>> MOLDS = List.of(SANDSTONE_MOLD, TERRACOTTA_MOLD, CAST_IRON_MOLD);

    private static void moldModels(String name, RegistrateItemModelProvider p) {
        var castTypes = new ArrayList<>(List.of("axe_heads", "balls", "dusts", "gems", "guards", "hoe_heads", "ingots", "nuggets", "pickaxe_heads", "rods", "shovel_heads", "sword_blades", "plates", "helmets", "chestplates", "leggings", "boots"));
        for (var cast : castTypes) for (var cutout : Iterate.trueAndFalse) {
            var loc = SlagEmbers.loc("item/" + (cutout ? "cutout/" : "") + name + "/" + cast);
            p.withExistingParent(loc.getPath(), "item/generated").texture("layer0", loc);
        }
        p.withExistingParent("item/cutout/" + name, "item/generated").texture("layer0", SlagEmbers.loc("item/cutout/" + name));
        p.withExistingParent(name, "item/generated").texture("layer0", SlagEmbers.loc("item/" + name));
    }

    public static final ItemEntry<Item> CAST_IRON_INGOT = REG.item("cast_iron_ingot", Item::new)
            .recipe((c, p) -> {
                compressible9x(c, p, Ingredient.of(AllTags.itemC("ingots/cast_iron")), Ingredient.of(AllTags.itemC("storage_blocks/cast_iron")), c.get(), AllBlocks.CAST_IRON_BLOCK);
                DualCookingRecipeBuilder.create(RecipeCategory.MISC, c.get(), AllTags.itemC("ingots/iron"), Items.CHARCOAL, 1.4f)
                        .unlockedBy("has_iron", has(AllTags.itemC("ingots/iron")))
                        .save(p, SlagEmbers.loc("double_smelting/" + c.getName()));
            })
            .tag(AllTags.itemC("ingots/cast_iron"), AllTags.itemC("ingots"), ItemTags.BEACON_PAYMENT_ITEMS)
            .register();
    public static final ItemEntry<Item> CAST_IRON_NUGGET = REG.item("cast_iron_nugget", Item::new)
            .recipe((c, p) -> compressible9x(c, p,
                    Ingredient.of(AllTags.itemC("nuggets/cast_iron")), Ingredient.of(AllTags.itemC("ingots/cast_iron")),
                    c.get(), CAST_IRON_INGOT))
            .tag(AllTags.itemC("nuggets/cast_iron"), AllTags.itemC("nuggets"))
            .register();

    public static final ItemEntry<Item> ROSE_GOLD_INGOT = REG.item("rose_gold_ingot", Item::new)
            .recipe((c, p) -> {
                compressible9x(c, p, Ingredient.of(AllTags.itemC("ingots/rose_gold")), Ingredient.of(AllTags.itemC("storage_blocks/rose_gold")), c.get(), AllBlocks.ROSE_GOLD_BLOCK);
                DualCookingRecipeBuilder.create(RecipeCategory.MISC, c.get(), 2, AllTags.itemC("ingots/copper"), AllTags.itemC("ingots/gold"), 1.4f)
                        .unlockedBy("has_copper", has(AllTags.itemC("ingots/copper")))
                        .save(p, SlagEmbers.loc("double_smelting/" + c.getName()));
            })
            .tag(AllTags.itemC("ingots/rose_gold"), AllTags.itemC("ingots"), ItemTags.BEACON_PAYMENT_ITEMS)
            .register();
    public static final ItemEntry<Item> ROSE_GOLD_NUGGET = REG.item("rose_gold_nugget", Item::new)
            .recipe((c, p) -> compressible9x(c, p,
                    Ingredient.of(AllTags.itemC("nuggets/rose_gold")), Ingredient.of(AllTags.itemC("ingots/rose_gold")),
                    c.get(), ROSE_GOLD_INGOT))
            .tag(AllTags.itemC("nuggets/rose_gold"), AllTags.itemC("nuggets"))
            .register();


    public static final ItemEntry<Item> DEEP_ALLOY = REG.item("deep_alloy", Item::new)
            .recipe((c, p) -> {
                compressible9x(c, p, Ingredient.of(AllTags.itemC("ingots/deep_alloy")), Ingredient.of(AllTags.itemC("storage_blocks/deep_alloy")), c.get(), AllBlocks.DEEP_ALLOY_BLOCK);
                DualCookingRecipeBuilder.create(RecipeCategory.MISC, c.get(), AllTags.itemC("ingots/iron"), Items.POLISHED_DEEPSLATE, 1.4f)
                        .unlockedBy("has_iron", has(AllTags.itemC("ingots/iron")))
                        .save(p, SlagEmbers.loc("double_smelting/" + c.getName()));
            }).tag(AllTags.itemC("ingots/deep_alloy"), AllTags.itemC("ingots"), ItemTags.BEACON_PAYMENT_ITEMS)
            .register();

    private static int castSize(MaterialType material, PartType part) {
        var fluid = material.moltenFluid.get();
        if (fluid == null) return 0;
        var size = fluid == AllFluids.MOLTEN_OBSIDIAN.getSource() ? MelterBE.BLOCK_SIZE : MelterBE.INGOT_SIZE;
        size *= getSize(part);
        if (size > 0 && fluid == AllFluids.MOLTEN_NETHERITE.getSource()) size = MelterBE.INGOT_SIZE;
        return size;
    }

    private static float miningTier(MaterialType material) {
        for (var trait : material.traits) if (trait.trait().equals(AllTraits.MINING_TIER.id)) return trait.value().orElse(0f);
        return 0;
    }

    private static RecipeOutput withConditions(RecipeOutput output, MaterialType material, PartType part, ICondition... extra) {
        var conditions = new ArrayList<>(material.conditions);
        conditions.addAll(part.conditions);
        conditions.addAll(List.of(extra));
        return conditions.isEmpty() ? output : output.withConditions(conditions.toArray(ICondition[]::new));
    }

    public static TagKey<Item> getCast(PartType part) {
        return switch (part.id.getPath()) {
            case "axe_head" -> AllTags.CAST_AXE_HEADS;
            case "pickaxe_head" -> AllTags.CAST_PICKAXE_HEADS;
            case "shovel_head" -> AllTags.CAST_SHOVEL_HEADS;
            case "hoe_head" -> AllTags.CAST_HOE_HEADS;
            case "sword_blade" -> AllTags.CAST_SWORD_BLADES;
            case "guard" -> AllTags.CAST_GUARDS;
            case "plate" -> AllTags.CAST_PLATES;
            case "helmet" -> AllTags.CAST_HELMETS;
            case "chestplate" -> AllTags.CAST_CHESTPLATES;
            case "leggings" -> AllTags.CAST_LEGGINGS;
            case "boots" -> AllTags.CAST_BOOTS;
            default -> null;
        };
    }


    public static int getSize(PartType part) {
        return switch (part.id.getPath()) {
            case "chestplate" -> 6;
            case "leggings" -> 5;
            case "axe_head", "pickaxe_head", "helmet" -> 3;
            case "hoe_head", "plate", "boots" -> 2;
            case "sword_blade", "guard", "shovel_head" -> 1;
            default -> 0;
        };
    }

    public static ShapedRecipeBuilder buildPattern(ShapedRecipeBuilder value, PartType part) {
        return switch (part.id.getPath()) {
            case "axe_head" -> value
                    .pattern("MM")
                    .pattern("MR")
                    .pattern(" R");
            case "pickaxe_head" -> value
                    .pattern("MMM")
                    .pattern(" R ")
                    .pattern(" R ");
            case "shovel_head" -> value
                    .pattern("M")
                    .pattern("R")
                    .pattern("R");
            case "hoe_head" -> value
                    .pattern("MM")
                    .pattern(" R")
                    .pattern(" R");
            case "sword_blade" -> value
                    .pattern("M")
                    .pattern("R");
            case "guard" -> value
                    .pattern("R")
                    .pattern("M");
            case "plate" -> value
                    .pattern("MR")
                    .pattern("RM");
            case "helmet" -> value
                    .pattern("MMM")
                    .pattern("R R");
            case "chestplate" -> value
                    .pattern("R R")
                    .pattern("MMM")
                    .pattern("MMM");
            case "leggings" -> value
                    .pattern("MMM")
                    .pattern("M M")
                    .pattern("R R");
            case "boots" -> value
                    .pattern("R R")
                    .pattern("M M");
            default -> value;
        };
    }

    public static List<String> testMixture(String part) {
        List<String> parts = new ArrayList<>();
        if (List.of("axe_head", "hoe_head").contains(part)) parts.add("mattock");
        if (List.of("pickaxe_head", "shovel_head").contains(part)) parts.add("prybar");
        if (List.of("shovel_head", "hoe_head").contains(part)) parts.add("graip");
        if (List.of("pickaxe_head", "axe_head").contains(part)) parts.add("mallet");

        if (List.of("pickaxe_head", "axe_head", "shovel_head").contains(part)) parts.add("hammer");
        if (List.of("hoe_head", "sword_blade", "guard").contains(part)) parts.add("scythe");
        if (List.of("pickaxe_head", "axe_head", "sword_blade").contains(part)) parts.add("maul");

        if (List.of("pickaxe_head", "axe_head", "shovel_head", "hoe_head", "sword_blade").contains(part)) parts.add("paxel");
        return parts;
    }


    public static void register() {}
}
