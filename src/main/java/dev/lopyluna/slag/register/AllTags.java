package dev.lopyluna.slag.register;

import com.tterrag.registrate.providers.RegistrateTagsProvider;
import dev.lopyluna.slag.SlagEmbers;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.conditions.TagEmptyCondition;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static dev.lopyluna.slag.SlagEmbers.REG;

@SuppressWarnings({"deprecation", "unused"})
public class AllTags {

    public static void addGenerators() {
        REG.generateTags();
    }

    public static TagKey<Fluid> HOT_FLUIDS = fluid("hot_fluids");

    public static TagKey<Fluid> MOLTEN_METALS = fluid("molten_metals");
    public static TagKey<Fluid> MOLTEN_GEMS = fluid("molten_gems");
    public static TagKey<Fluid> MOLTEN_CRYSTALS = fluid("molten_crystals");
    public static TagKey<Fluid> MOLTEN_DUSTS = fluid("molten_dusts");
    public static TagKey<Fluid> MOLTEN_DUSTS_SMALL = fluid("molten_dusts_small");
    public static TagKey<Fluid> MOLTEN_BALLS = fluid("molten_balls");
    public static TagKey<Fluid> MOLTEN_BALLS_SMALL = fluid("molten_balls_small");

    public static void genFluidTags(RegistrateTagsProvider<Fluid> provIn) {
        TagsProvider<Fluid> prov = new TagsProvider<>(provIn, Fluid::builtInRegistryHolder);

        for (var fluid : BuiltInRegistries.FLUID) {
            var tag = AllFluids.commonTag(fluid);
            if (tag != null && fluid.isSource(fluid.defaultFluidState())) prov.tag(tag).add(fluid);
        }

        prov.tag(MOLTEN_METALS)
                .add(AllFluids.MOLTEN_COPPER.getSource())
                .add(AllFluids.MOLTEN_GOLD.getSource())
                .add(AllFluids.MOLTEN_IRON.getSource())
                .add(AllFluids.MOLTEN_NETHERITE.getSource())
                .add(AllFluids.MOLTEN_DEBRIS.getSource())
                .add(AllFluids.MOLTEN_ROSE_GOLD.getSource())
                .add(AllFluids.MOLTEN_ALUMINIUM.getSource())
                .add(AllFluids.MOLTEN_BRASS.getSource())
                .add(AllFluids.MOLTEN_BRONZE.getSource())
                .add(AllFluids.MOLTEN_CAST_IRON.getSource())
                .add(AllFluids.MOLTEN_ELECTRUM.getSource())
                .add(AllFluids.MOLTEN_INVAR.getSource())
                .add(AllFluids.MOLTEN_LEAD.getSource())
                .add(AllFluids.MOLTEN_NICKEL.getSource())
                .add(AllFluids.MOLTEN_OSMIUM.getSource())
                .add(AllFluids.MOLTEN_PLATINUM.getSource())
                .add(AllFluids.MOLTEN_SILVER.getSource())
                .add(AllFluids.MOLTEN_STEEL.getSource())
                .add(AllFluids.MOLTEN_TIN.getSource())
                .add(AllFluids.MOLTEN_TUNGSTEN.getSource())
                .add(AllFluids.MOLTEN_ZINC.getSource())
        ;
        prov.tag(MOLTEN_GEMS)
                .add(AllFluids.MOLTEN_DIAMOND.getSource())
                .add(AllFluids.MOLTEN_EMERALD.getSource())
                .add(AllFluids.MOLTEN_LAPIS.getSource())
        ;
        prov.tag(MOLTEN_CRYSTALS)
                .add(AllFluids.MOLTEN_AMETHYST.getSource())
                .add(AllFluids.MOLTEN_PRISMARINE.getSource())
                .add(AllFluids.MOLTEN_QUARTZ.getSource())
                .add(AllFluids.MOLTEN_ECHO.getSource())
                .add(AllFluids.MOLTEN_ROSE_QUARTZ.getSource())
        ;
        prov.tag(MOLTEN_DUSTS)
                .add(AllFluids.MOLTEN_REDSTONE.getSource())
                .add(AllFluids.MOLTEN_OBSIDIAN.getSource())
        ;
        prov.tag(MOLTEN_DUSTS_SMALL)
                .add(AllFluids.MOLTEN_GLOWSTONE.getSource())
        ;
    }

    public static TagKey<Block> HEATED_FLUIDS = block("heated_fluids");

    public static TagKey<Block> HARVESTABLE = block("harvestable");
    public static TagKey<Block> VEIN_MINEABLE = block("vein_mineable");
    public static TagKey<Block> KNIFE_MINEABLE = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("farmersdelight", "mineable/knife"));
    public static TagKey<Item> KNIVES = itemC("tools/knife");
    public static TagKey<Item> FD_KNIVES = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("farmersdelight", "tools/knives"));

    public static void genBlockTags(RegistrateTagsProvider<Block> provIn) {
        TagsProvider<Block> prov = new TagsProvider<>(provIn, Block::builtInRegistryHolder);
        prov.tag(HARVESTABLE)
                .addTag(BlockTags.CROPS)
        ;
        prov.tag(VEIN_MINEABLE)
                .addTag(Tags.Blocks.ORES)
        ;
    }
    public static TagKey<Item> COPPER_BLOCKS = item("copper_blocks");
    public static TagKey<Item> QUARTZ_BLOCKS = item("quartz_blocks");
    public static TagKey<Item> AMETHYST_BLOCKS = item("amethyst_blocks");
    public static TagKey<Item> COPPER_RAW_MATERIALS = item("copper_raw_materials");
    public static TagKey<Item> IRON_RAW_MATERIALS = item("iron_raw_materials");
    public static TagKey<Item> GOLD_RAW_MATERIALS = item("gold_raw_materials");

    public static TagKey<Item> PARTS_AXE_HEADS = item("parts/axe_heads");
    public static TagKey<Item> PARTS_PICKAXE_HEADS = item("parts/pickaxe_heads");
    public static TagKey<Item> PARTS_SHOVEL_HEADS = item("parts/shovel_heads");
    public static TagKey<Item> PARTS_HOE_HEADS = item("parts/hoe_heads");
    public static TagKey<Item> PARTS_SWORD_BLADES = item("parts/sword_blades");
    public static TagKey<Item> PARTS_GUARDS = item("parts/guards");
    public static TagKey<Item> PARTS_PLATES = item("parts/plates");
    public static TagKey<Item> PARTS_HELMETS = item("parts/helmets");
    public static TagKey<Item> PARTS_CHESTPLATES = item("parts/chestplates");
    public static TagKey<Item> PARTS_LEGGINGS = item("parts/leggings");
    public static TagKey<Item> PARTS_BOOTS = item("parts/boots");

    public static TagKey<Item> CAST_AXE_HEADS = item("cast/axe_heads");
    public static TagKey<Item> CAST_PICKAXE_HEADS = item("cast/pickaxe_heads");
    public static TagKey<Item> CAST_SHOVEL_HEADS = item("cast/shovel_heads");
    public static TagKey<Item> CAST_HOE_HEADS = item("cast/hoe_heads");
    public static TagKey<Item> CAST_SWORD_BLADES = item("cast/sword_blades");
    public static TagKey<Item> CAST_GUARDS = item("cast/guards");
    public static TagKey<Item> CAST_PLATES = item("cast/plates");
    public static TagKey<Item> CAST_HELMETS = item("cast/helmets");
    public static TagKey<Item> CAST_CHESTPLATES = item("cast/chestplates");
    public static TagKey<Item> CAST_LEGGINGS = item("cast/leggings");
    public static TagKey<Item> CAST_BOOTS = item("cast/boots");

    public static TagKey<Item> CAST_INGOTS = item("cast/ingots");
    public static TagKey<Item> CAST_GEMS = item("cast/gems");
    public static TagKey<Item> CAST_BALLS = item("cast/balls");
    public static TagKey<Item> CAST_NUGGETS = item("cast/nuggets");
    public static TagKey<Item> CAST_DUSTS = item("cast/dusts");
    public static TagKey<Item> CAST_RODS = item("cast/rods");
    public static List<TagKey<Item>> CASTS = List.of(CAST_AXE_HEADS, CAST_PICKAXE_HEADS, CAST_SHOVEL_HEADS, CAST_HOE_HEADS, CAST_SWORD_BLADES, CAST_GUARDS, CAST_PLATES, CAST_HELMETS, CAST_CHESTPLATES, CAST_LEGGINGS, CAST_BOOTS, CAST_INGOTS, CAST_GEMS, CAST_BALLS, CAST_NUGGETS, CAST_DUSTS, CAST_RODS);

    public static TagKey<Item> IMPRINTABLE = item("imprintable");
    public static TagKey<Item> MOLDS_REUSABLE = item("molds/reusable");
    public static TagKey<Item> MOLDS_SINGLE = item("molds/single");

    public static TagKey<Item> BARS_COPPER = itemC("bars/copper");
    public static TagKey<Item> BARS_BRASS = itemC("bars/brass");
    public static TagKey<Item> ROSE_QUARTZ = itemC("gems/rose_quartz");
    public static TagKey<Item> POLISHED_ROSE_QUARTZ = item("polished_rose_quartz");
    public static TagKey<Item> ENGINE_ASSEMBLY = item("engine_assembly");
    public static TagKey<Item> ANDESITE_ALLOY = itemC("alloys/andesite");

    public static TagKey<Item> RECYCLING_BLACKLIST = item("recycling_blacklist");
    public static TagKey<Item> RECYCLING_FREE = item("recycling_free");
    public static TagKey<Item> BLACKLISTED_HOTBAR_ITEMS = item("blacklisted_hotbar_items");
    public static TagKey<Item> MEKANISM_CLUMPS = TagKey.create(Registries.ITEM, SlagEmbers.loc("mekanism", "clumps"));

    public static void genItemTags(RegistrateTagsProvider<Item> provIn) {
        TagsProvider<Item> prov = new TagsProvider<>(provIn, Item::builtInRegistryHolder);

        prov.tag(BLACKLISTED_HOTBAR_ITEMS).addOptional(SlagEmbers.loc("create", "wand_of_symmetry"));

        prov.tag(BARS_COPPER).addOptional(SlagEmbers.loc("create", "copper_bars"));
        prov.tag(BARS_BRASS).addOptional(SlagEmbers.loc("create", "brass_bars"));
        prov.tag(ROSE_QUARTZ).addOptional(SlagEmbers.loc("create", "rose_quartz"));
        prov.tag(POLISHED_ROSE_QUARTZ).addOptional(SlagEmbers.loc("create", "polished_rose_quartz"));
        prov.tag(ENGINE_ASSEMBLY).addOptional(SlagEmbers.loc("simulated", "engine_assembly"));
        prov.tag(ANDESITE_ALLOY).addOptional(SlagEmbers.loc("create", "andesite_alloy"));

        for (var path : List.of("ingots/", "nuggets/", "storage_blocks/", "storage_blocks/raw_", "raw_materials/", "ores/", "dusts/", "plates/", "rods/", "wires/", "clumps/")) prov.tag(itemC(path + "aluminium")).addOptionalTag(itemC(path + "aluminum").location());
        for (var metal : List.of("copper", "gold", "iron", "aluminum", "lead", "nickel", "osmium", "platinum", "silver", "tin", "zinc")) {
            prov.tag(itemC("clumps/" + metal)).addOptional(SlagEmbers.loc("create", "crushed_raw_" + metal));
            prov.tag(MEKANISM_CLUMPS).addOptionalTag(itemC("clumps/" + metal).location());
        }

        for (var cast : CASTS) prov.tag(IMPRINTABLE).addOptionalTag(cast.location());

        for (var gem : List.of("diamond", "emerald", "lapis")) prov.tag(itemC("nuggets/" + gem));

        prov.tag(RECYCLING_FREE)
                .add(Items.PAPER)
                .add(Items.BOOK)
                .add(Items.STRING)
                .add(Items.FEATHER)
                .add(Items.RABBIT_HIDE)
                .add(Items.RABBIT_FOOT)
                .add(Items.SADDLE)
                .add(Items.ENDER_PEARL)
                .add(Items.ENDER_EYE)
                .add(Items.MAGMA_CREAM)
                .add(Items.FLINT)
                .add(Items.HONEYCOMB)
                .add(Items.HONEYCOMB_BLOCK)
                .add(Items.TORCH)
                .add(Items.SOUL_TORCH)
                .add(Items.SCULK)
                .add(Items.SCULK_VEIN)
                .add(Items.SCULK_SENSOR)
                .addOptional(SlagEmbers.loc("supplementaries", "soap"))
                .addTag(Tags.Items.DYES)
                .addTag(Tags.Items.LEATHERS)
                .addTag(Tags.Items.RODS_WOODEN)
                .addTag(Tags.Items.STRIPPED_LOGS)
                .addTag(Tags.Items.STRIPPED_WOODS)
                .addTag(Tags.Items.FENCES_WOODEN)
                .addTag(Tags.Items.FENCE_GATES_WOODEN)
                .addTag(Tags.Items.CHESTS_WOODEN)
                .addTag(Tags.Items.BARRELS_WOODEN)
                .addTag(Tags.Items.SLIME_BALLS)
                .addTag(Tags.Items.STORAGE_BLOCKS_SLIME)
                .addTag(Tags.Items.DUSTS)
                .addTag(ItemTags.CANDLES)
                .addTag(ItemTags.LOGS)
                .addTag(ItemTags.PLANKS)
                .addTag(ItemTags.WOODEN_SLABS)
                .addTag(ItemTags.WOODEN_STAIRS)
                .addTag(ItemTags.WOODEN_FENCES)
                .addTag(ItemTags.WOODEN_DOORS)
                .addTag(ItemTags.WOODEN_TRAPDOORS)
                .addTag(ItemTags.WOODEN_BUTTONS)
                .addTag(ItemTags.WOODEN_PRESSURE_PLATES)
                .addTag(ItemTags.SIGNS)
                .addTag(ItemTags.HANGING_SIGNS)
                .addTag(ItemTags.BOATS)
                .addTag(ItemTags.CHEST_BOATS)
                .addTag(ItemTags.BAMBOO_BLOCKS)
                .addTag(ItemTags.SAPLINGS)
                .addTag(ItemTags.LEAVES)
                .addOptionalTag(SlagEmbers.loc("c", "fuels"));

        prov.tag(CAST_INGOTS).add(Items.NETHERITE_SCRAP).addTag(Tags.Items.INGOTS).addTag(Tags.Items.BRICKS).addOptional(SlagEmbers.loc("create", "bar_of_chocolate"));
        prov.tag(CAST_GEMS).add(Items.ECHO_SHARD).addTag(ItemTags.COALS).addTag(Tags.Items.GEMS).addTag(Tags.Items.NETHER_STARS);
        prov.tag(CAST_BALLS).add(Items.WIND_CHARGE).add(Items.FIRE_CHARGE).add(Items.FIREWORK_STAR).add(Items.ENDER_EYE).add(Items.CLAY_BALL).add(Items.SNOWBALL).add(Items.MAGMA_CREAM).add(Items.HEART_OF_THE_SEA).addTag(Tags.Items.SLIME_BALLS).addTag(Tags.Items.ENDER_PEARLS);
        prov.tag(CAST_NUGGETS).addTag(Tags.Items.NUGGETS);
        prov.tag(CAST_DUSTS).add(Items.BLAZE_POWDER).add(Items.SUGAR).add(Items.GUNPOWDER).addTag(Tags.Items.DUSTS);
        prov.tag(CAST_RODS).add(Items.END_ROD).add(Items.LIGHTNING_ROD).add(Items.BAMBOO).addTag(Tags.Items.RODS);

        prov.tag(COPPER_RAW_MATERIALS)
                .addTag(Tags.Items.ORES_COPPER)
                .addTag(Tags.Items.RAW_MATERIALS_COPPER);
        prov.tag(IRON_RAW_MATERIALS)
                .addTag(Tags.Items.ORES_IRON)
                .addTag(Tags.Items.RAW_MATERIALS_IRON);
        prov.tag(GOLD_RAW_MATERIALS)
                .addTag(Tags.Items.ORES_GOLD)
                .addTag(Tags.Items.RAW_MATERIALS_GOLD);
        prov.tag(AMETHYST_BLOCKS)
                .add(Items.AMETHYST_CLUSTER)
                .add(Items.BUDDING_AMETHYST)
                .add(Items.AMETHYST_BLOCK);
        prov.tag(QUARTZ_BLOCKS)
                .add(Items.CHISELED_QUARTZ_BLOCK)
                .add(Items.QUARTZ_PILLAR)
                .add(Items.QUARTZ_BRICKS)
                .add(Items.SMOOTH_QUARTZ)
                .add(Items.QUARTZ_BLOCK);
        prov.tag(COPPER_BLOCKS)
                .add(Items.EXPOSED_COPPER)
                .add(Items.WEATHERED_COPPER)
                .add(Items.OXIDIZED_COPPER)
                .add(Items.WAXED_COPPER_BLOCK)
                .add(Items.WAXED_EXPOSED_COPPER)
                .add(Items.WAXED_WEATHERED_COPPER)
                .add(Items.WAXED_OXIDIZED_COPPER)
                .addTag(Tags.Items.STORAGE_BLOCKS_COPPER);

        prov.tag(itemC("doors/copper"))
                .add(Items.COPPER_DOOR)
                .add(Items.EXPOSED_COPPER_DOOR)
                .add(Items.WEATHERED_COPPER_DOOR)
                .add(Items.OXIDIZED_COPPER_DOOR)
                .add(Items.WAXED_COPPER_DOOR)
                .add(Items.WAXED_EXPOSED_COPPER_DOOR)
                .add(Items.WAXED_WEATHERED_COPPER_DOOR)
                .add(Items.WAXED_OXIDIZED_COPPER_DOOR);

        prov.tag(itemC("trapdoors/copper"))
                .add(Items.COPPER_TRAPDOOR)
                .add(Items.EXPOSED_COPPER_TRAPDOOR)
                .add(Items.WEATHERED_COPPER_TRAPDOOR)
                .add(Items.OXIDIZED_COPPER_TRAPDOOR)
                .add(Items.WAXED_COPPER_TRAPDOOR)
                .add(Items.WAXED_EXPOSED_COPPER_TRAPDOOR)
                .add(Items.WAXED_WEATHERED_COPPER_TRAPDOOR)
                .add(Items.WAXED_OXIDIZED_COPPER_TRAPDOOR);

        prov.tag(itemC("grate_blocks/copper"))
                .add(Items.COPPER_GRATE)
                .add(Items.EXPOSED_COPPER_GRATE)
                .add(Items.WEATHERED_COPPER_GRATE)
                .add(Items.OXIDIZED_COPPER_GRATE)
                .add(Items.WAXED_COPPER_GRATE)
                .add(Items.WAXED_EXPOSED_COPPER_GRATE)
                .add(Items.WAXED_WEATHERED_COPPER_GRATE)
                .add(Items.WAXED_OXIDIZED_COPPER_GRATE);

        prov.tag(itemC("chiseled_blocks/copper"))
                .add(Items.CHISELED_COPPER)
                .add(Items.EXPOSED_CHISELED_COPPER)
                .add(Items.WEATHERED_CHISELED_COPPER)
                .add(Items.OXIDIZED_CHISELED_COPPER)
                .add(Items.WAXED_CHISELED_COPPER)
                .add(Items.WAXED_EXPOSED_CHISELED_COPPER)
                .add(Items.WAXED_WEATHERED_CHISELED_COPPER)
                .add(Items.WAXED_OXIDIZED_CHISELED_COPPER);

        prov.tag(itemC("cut_blocks/copper"))
                .add(Items.CUT_COPPER)
                .add(Items.EXPOSED_CUT_COPPER)
                .add(Items.WEATHERED_CUT_COPPER)
                .add(Items.OXIDIZED_CUT_COPPER)
                .add(Items.WAXED_CUT_COPPER)
                .add(Items.WAXED_EXPOSED_CUT_COPPER)
                .add(Items.WAXED_WEATHERED_CUT_COPPER)
                .add(Items.WAXED_OXIDIZED_CUT_COPPER);

    }

    public static TagKey<Block> block(String name) { return TagKey.create(Registries.BLOCK, SlagEmbers.loc(name)); }
    public static TagKey<Block> blockC(String name) { return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", name)); }
    public static TagKey<Block> blockMC(String name) { return TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace(name)); }
    public static TagKey<Item> item(String name) { return TagKey.create(Registries.ITEM, SlagEmbers.loc(name)); }
    public static ICondition present(TagKey<Item> tag) { return new NotCondition(new TagEmptyCondition(tag)); }
    public static TagKey<Item> itemC(String name) { return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", name)); }
    public static TagKey<Item> itemMC(String name) { return TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace(name)); }
    public static TagKey<Fluid> fluid(String name) { return TagKey.create(Registries.FLUID, SlagEmbers.loc(name)); }
    public static TagKey<Fluid> fluidC(String name) { return TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("c", name)); }
    public static TagKey<Fluid> fluidMC(String name) { return TagKey.create(Registries.FLUID, ResourceLocation.withDefaultNamespace(name)); }

    public static class TagsProvider<T> {

        private final RegistrateTagsProvider<T> provider;
        private final Function<T, ResourceKey<T>> keyExtractor;

        public TagsProvider(RegistrateTagsProvider<T> provider, Function<T, Holder.Reference<T>> refExtractor) {
            this.provider = provider;
            this.keyExtractor = refExtractor.andThen(Holder.Reference::key);
        }

        public TagAppender<T> tag(TagKey<T> tag) {
            TagBuilder tagbuilder = getOrCreateRawBuilder(tag);
            return new TagAppender<>(tagbuilder, keyExtractor);
        }

        public TagBuilder getOrCreateRawBuilder(TagKey<T> tag) {
            return provider.addTag(tag).getInternalBuilder();
        }

    }

    public static class TagAppender<T> extends net.minecraft.data.tags.TagsProvider.TagAppender<T> {

        private final Function<T, ResourceKey<T>> keyExtractor;

        public TagAppender(TagBuilder pBuilder, Function<T, ResourceKey<T>> pKeyExtractor) {
            super(pBuilder);
            this.keyExtractor = pKeyExtractor;
        }

        public TagAppender<T> add(T entry) {
            this.add(this.keyExtractor.apply(entry));
            return this;
        }

        @SafeVarargs
        public final TagAppender<T> add(T... entries) {
            Stream.of(entries)
                    .map(this.keyExtractor)
                    .forEach(this::add);
            return this;
        }
    }
}
