package dev.lopyluna.slag.register;

import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.FluidEntry;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.blocks.basin.BasinBlock;
import dev.lopyluna.slag.content.blocks.casting.CastItem;
import dev.lopyluna.slag.content.blocks.crucible.CrucibleBlock;
import dev.lopyluna.slag.content.blocks.crucible.CrucibleItem;
import dev.lopyluna.slag.content.blocks.crucible_interface.InterfaceBlock;
import dev.lopyluna.slag.content.blocks.drain.DrainBlock;
import dev.lopyluna.slag.content.blocks.forge.ForgeBlock;
import dev.lopyluna.slag.content.blocks.melter.MelterBE;
import dev.lopyluna.slag.content.blocks.melter.MelterBlock;
import dev.lopyluna.slag.content.blocks.table.TableBlock;
import dev.lopyluna.slag.content.datagen.*;
import dev.lopyluna.slag.content.temperature.Temperatures.Tiers;
import dev.lopyluna.slag.content.utils.BlockHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.function.Function;

import static com.tterrag.registrate.providers.RegistrateRecipeProvider.has;
import static dev.lopyluna.slag.SlagEmbers.REG;
import static dev.lopyluna.slag.content.blocks.crucible.CrucibleBE.*;
import static dev.lopyluna.slag.content.datagen.AlloyingRecipeBuilder.fluid;
import static dev.lopyluna.slag.content.datagen.MeltingRecipeBuilder.*;
import static dev.lopyluna.slag.content.utils.BlockHelper.getExistingModel;

@SuppressWarnings({"removal", "unused"})
public class AllBlocks {
    public static final BlockEntry<Block> ROSE_GOLD_BLOCK = REG.block("rose_gold_block", Block::new)
            .lang("Block of Rose Gold")
            .initialProperties(() -> Blocks.GOLD_BLOCK)
            .properties(p -> p.mapColor(MapColor.COLOR_PINK).requiresCorrectToolForDrops())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_IRON_TOOL, BlockTags.BEACON_BASE_BLOCKS, AllTags.blockC("storage_blocks"), AllTags.blockC("storage_blocks/rose_gold"))
            .item()
            .tag(AllTags.itemC("storage_blocks"), AllTags.itemC("storage_blocks/rose_gold"))
            .build()
            .register();

    public static final BlockEntry<Block> CAST_IRON_BLOCK = REG.block("cast_iron_block", Block::new)
            .lang("Block of Cast Iron")
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK).mapColor(MapColor.COLOR_GRAY).requiresCorrectToolForDrops())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_STONE_TOOL, BlockTags.BEACON_BASE_BLOCKS, AllTags.blockC("storage_blocks"), AllTags.blockC("storage_blocks/cast_iron"))
            .item()
            .tag(AllTags.itemC("storage_blocks"), AllTags.itemC("storage_blocks/cast_iron"))
            .build()
            .register();

    public static final BlockEntry<Block> DEEP_ALLOY_BLOCK = REG.block("deep_alloy_block", Block::new)
            .lang("Block of Deep Alloy")
            .initialProperties(() -> Blocks.GOLD_BLOCK)
            .properties(p -> p.sound(AllSoundTypes.CRUCIBLE).noOcclusion().mapColor(MapColor.COLOR_GRAY).requiresCorrectToolForDrops())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_IRON_TOOL, BlockTags.BEACON_BASE_BLOCKS, AllTags.blockC("storage_blocks"), AllTags.blockC("storage_blocks/deep_alloy"))
            .item()
            .tag(AllTags.itemC("storage_blocks"), AllTags.itemC("storage_blocks/deep_alloy"))
            .build()
            .register();

    public static final BlockEntry<CrucibleBlock> CRUCIBLE = REG.block("crucible", CrucibleBlock::new)
            .blockstate((c, p) -> p.getVariantBuilder(c.get()).forAllStatesExcept(state -> {
                Function<BlockState, ModelFile> modelFunc = s -> {
                    var model = getExistingModel(c, p,
                            switch (s.getValue(SHAPE)) { case PLAIN -> ""; case INNER -> "inner"; case NW -> "nw"; case SW -> "sw"; case NE -> "ne"; case SE -> "se"; case NORTH -> "north"; case SOUTH -> "south"; case WEST -> "west"; case EAST -> "east"; },
                            s.getValue(TOP) ? "top" : "",
                            s.getValue(BOTTOM) ? "bottom" : ""
                    );
                    var loc = model.getLocation();
                    var wModel = p.models().withExistingParent(loc.getPath() + "_window", loc).texture("1", "block/crucible_side_window");
                    return s.getValue(WINDOW) ? wModel : model;
                };
                return ConfiguredModel.builder().modelFile(modelFunc.apply(state)).build();
            }, LIGHT)).properties(p -> p.strength(2.0F).sound(AllSoundTypes.CRUCIBLE).mapColor(MapColor.COLOR_GRAY).requiresCorrectToolForDrops())
            .recipe((c, p) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, c.get(), 4)
                        .pattern("D D")
                        .pattern("D D")
                        .pattern("DBD")
                        .define('D', AllTags.itemC("ingots/deep_alloy")).define('B', AllTags.itemC("storage_blocks/deep_alloy"))
                        .unlockedBy("has_deep_alloy", has(AllTags.itemC("ingots/deep_alloy"))).save(p, SlagEmbers.loc("crafting/" + c.getName()));

                AlloyingRecipeBuilder.create(AllFluids.MOLTEN_OBSIDIAN.getSource(), MelterBE.NUGGET_SIZE, fluid(Fluids.WATER, MelterBE.NUGGET_SIZE), fluid(Fluids.LAVA, MelterBE.NUGGET_SIZE))
                        .unlockedBy("has_lava", has(Items.LAVA_BUCKET)).save(p, SlagEmbers.loc("alloying/molten_obsidian"));
                AlloyingRecipeBuilder.create(AllFluids.MOLTEN_ROSE_GOLD.getSource(), MelterBE.NUGGET_SIZE * 2, fluid(AllFluids.MOLTEN_GOLD.getSource(), MelterBE.NUGGET_SIZE), fluid(AllFluids.MOLTEN_COPPER.getSource(), MelterBE.NUGGET_SIZE))
                        .unlockedBy("has_lava", has(Items.LAVA_BUCKET)).save(p, SlagEmbers.loc("alloying/molten_rose_gold"));
                AlloyingRecipeBuilder.create(AllFluids.MOLTEN_ROSE_QUARTZ.getSource(), MelterBE.NUGGET_SIZE, fluid(AllFluids.MOLTEN_QUARTZ.getSource(), MelterBE.NUGGET_SIZE), fluid(AllFluids.MOLTEN_REDSTONE.getSource(), MelterBE.NUGGET_SIZE * 4))
                        .unlockedBy("has_quartz", has(Tags.Items.GEMS_QUARTZ)).save(p.withConditions(AllTags.present(AllTags.ROSE_QUARTZ)), SlagEmbers.loc("alloying/molten_rose_quartz"));
                alloy(p, AllFluids.MOLTEN_BRONZE, 4, Tiers.HEATED, false, "bronze", fluid(AllFluids.MOLTEN_COPPER.getSource(), MelterBE.NUGGET_SIZE * 3), "tin", fluid(AllFluids.MOLTEN_TIN.getSource(), MelterBE.NUGGET_SIZE));
                alloy(p, AllFluids.MOLTEN_BRASS, 2, Tiers.BLAZING, true, "brass", fluid(AllFluids.MOLTEN_COPPER.getSource(), MelterBE.NUGGET_SIZE), "zinc", fluid(AllFluids.MOLTEN_ZINC.getSource(), MelterBE.NUGGET_SIZE));
                alloy(p, AllFluids.MOLTEN_ELECTRUM, 2, Tiers.HEATED, false, "electrum", fluid(AllFluids.MOLTEN_GOLD.getSource(), MelterBE.NUGGET_SIZE), "silver", fluid(AllFluids.MOLTEN_SILVER.getSource(), MelterBE.NUGGET_SIZE));
                alloy(p, AllFluids.MOLTEN_INVAR, 3, Tiers.BLAZING, false, "invar", fluid(AllFluids.MOLTEN_IRON.getSource(), MelterBE.NUGGET_SIZE * 2), "nickel", fluid(AllFluids.MOLTEN_NICKEL.getSource(), MelterBE.NUGGET_SIZE));
            })
            .addLayer(() -> RenderType::cutoutMipped)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .item(CrucibleItem::new)
            .model((c, p) -> p.withExistingParent("item/" + c.getName(), p.modLoc("block/"+c.getName()+"/block_top_bottom")))
            .build()
            .register();

    public static final BlockEntry<TableBlock> TABLE = REG.block("table", TableBlock::new)
            .initialProperties(() -> Blocks.CAULDRON)
            .lang("Casting Table")
            .blockstate((c, p) -> p.simpleBlock(c.get(), p.models().getExistingFile(p.modLoc("block/table"))))
            .properties(p -> p.sound(AllSoundTypes.CRUCIBLE).noOcclusion().mapColor(MapColor.COLOR_GRAY).requiresCorrectToolForDrops())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .recipe((c, p) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, c.get(), 1)
                        .pattern("AAA")
                        .pattern("A A")
                        .define('A', AllTags.itemC("ingots/deep_alloy"))
                        .unlockedBy("has_deep_alloy", has(AllTags.itemC("ingots/deep_alloy"))).save(p, SlagEmbers.loc("crafting/" + c.getName()));

                create(p, "ingot", "copper", Items.COPPER_INGOT, AllFluids.MOLTEN_COPPER, MelterBE.INGOT_SIZE, AllTags.CAST_INGOTS);
                create(p, "ingot", "gold", Items.GOLD_INGOT, AllFluids.MOLTEN_GOLD, MelterBE.INGOT_SIZE, AllTags.CAST_INGOTS);
                create(p, "ingot", "iron", Items.IRON_INGOT, AllFluids.MOLTEN_IRON, MelterBE.INGOT_SIZE, AllTags.CAST_INGOTS);
                create(p, "ingot", "rose_gold", AllItems.ROSE_GOLD_INGOT.get(), AllFluids.MOLTEN_ROSE_GOLD, MelterBE.INGOT_SIZE, AllTags.CAST_INGOTS);
                create(p, "ingot", "netherite", Items.NETHERITE_INGOT, AllFluids.MOLTEN_NETHERITE, MelterBE.INGOT_SIZE, AllTags.CAST_INGOTS);

                create(p, "gem", "diamond", Items.DIAMOND, AllFluids.MOLTEN_DIAMOND, MelterBE.INGOT_SIZE, AllTags.CAST_GEMS);
                create(p, "gem", "emerald", Items.EMERALD, AllFluids.MOLTEN_EMERALD, MelterBE.INGOT_SIZE, AllTags.CAST_GEMS);
                create(p, "gem", "lapis", Items.LAPIS_LAZULI, AllFluids.MOLTEN_LAPIS, MelterBE.INGOT_SIZE, AllTags.CAST_GEMS);

                create(p, "gem", "quartz", Items.QUARTZ, AllFluids.MOLTEN_QUARTZ, MelterBE.INGOT_SIZE, AllTags.CAST_GEMS);
                create(p, "gem", "amethyst", Items.AMETHYST_SHARD, AllFluids.MOLTEN_AMETHYST, MelterBE.INGOT_SIZE, AllTags.CAST_GEMS);
                create(p, "gem", "prismarine", Items.PRISMARINE_CRYSTALS, AllFluids.MOLTEN_PRISMARINE, MelterBE.INGOT_SIZE, AllTags.CAST_GEMS);
                create(p, "gem", "echo", Items.ECHO_SHARD, AllFluids.MOLTEN_ECHO, MelterBE.INGOT_SIZE, AllTags.CAST_GEMS);
                createTag(p, "gem", "rose_quartz", AllFluids.MOLTEN_ROSE_QUARTZ, MelterBE.INGOT_SIZE, AllTags.CAST_GEMS);
                TableCastingRecipeBuilder.create(AllTags.POLISHED_ROSE_QUARTZ, 1, AllFluids.MOLTEN_ROSE_QUARTZ.getSource(), MelterBE.INGOT_SIZE, AllTags.CAST_INGOTS)
                        .unlockedBy("has_rose_quartz", has(AllTags.ROSE_QUARTZ)).save(p.withConditions(AllTags.present(AllTags.POLISHED_ROSE_QUARTZ)), SlagEmbers.loc("casting/table/polished_rose_quartz"));

                create(p, "dust", "redstone", Items.REDSTONE, AllFluids.MOLTEN_REDSTONE, MelterBE.INGOT_SIZE, AllTags.CAST_DUSTS);
                create(p, "dust", "glowstone", Items.GLOWSTONE_DUST, AllFluids.MOLTEN_GLOWSTONE, MelterBE.INGOT_SIZE, AllTags.CAST_DUSTS);

                create(p, "nugget", "gold", Items.GOLD_NUGGET, AllFluids.MOLTEN_GOLD, MelterBE.NUGGET_SIZE, AllTags.CAST_NUGGETS);
                create(p, "nugget", "iron", Items.IRON_NUGGET, AllFluids.MOLTEN_IRON, MelterBE.NUGGET_SIZE, AllTags.CAST_NUGGETS);
                create(p, "nugget", "rose_gold", AllItems.ROSE_GOLD_NUGGET.get(), AllFluids.MOLTEN_ROSE_GOLD, MelterBE.NUGGET_SIZE, AllTags.CAST_NUGGETS);
                createTag(p, "nugget", "copper", AllFluids.MOLTEN_COPPER, MelterBE.NUGGET_SIZE, AllTags.CAST_NUGGETS);
                createTag(p, "nugget", "netherite", AllFluids.MOLTEN_NETHERITE, MelterBE.NUGGET_SIZE, AllTags.CAST_NUGGETS);

                createTag(p, "dust", "copper", AllFluids.MOLTEN_COPPER, MelterBE.INGOT_SIZE, AllTags.CAST_DUSTS);
                createTag(p, "dust", "gold", AllFluids.MOLTEN_GOLD, MelterBE.INGOT_SIZE, AllTags.CAST_DUSTS);
                createTag(p, "dust", "iron", AllFluids.MOLTEN_IRON, MelterBE.INGOT_SIZE, AllTags.CAST_DUSTS);
                createTag(p, "dust", "netherite", AllFluids.MOLTEN_NETHERITE, MelterBE.INGOT_SIZE, AllTags.CAST_DUSTS);
                createTag(p, "dust", "diamond", AllFluids.MOLTEN_DIAMOND, MelterBE.INGOT_SIZE, AllTags.CAST_DUSTS);
                createTag(p, "dust", "emerald", AllFluids.MOLTEN_EMERALD, MelterBE.INGOT_SIZE, AllTags.CAST_DUSTS);
                createTag(p, "dust", "lapis", AllFluids.MOLTEN_LAPIS, MelterBE.INGOT_SIZE, AllTags.CAST_DUSTS);
                createTag(p, "dust", "quartz", AllFluids.MOLTEN_QUARTZ, MelterBE.INGOT_SIZE, AllTags.CAST_DUSTS);
                createTag(p, "dust", "amethyst", AllFluids.MOLTEN_AMETHYST, MelterBE.INGOT_SIZE, AllTags.CAST_DUSTS);
                createTag(p, "dust", "prismarine", AllFluids.MOLTEN_PRISMARINE, MelterBE.INGOT_SIZE, AllTags.CAST_DUSTS);

                createTag(p, "rod", "copper", AllFluids.MOLTEN_COPPER, MelterBE.INGOT_SIZE / 2, AllTags.CAST_RODS);
                createTag(p, "rod", "gold", AllFluids.MOLTEN_GOLD, MelterBE.INGOT_SIZE / 2, AllTags.CAST_RODS);
                createTag(p, "rod", "iron", AllFluids.MOLTEN_IRON, MelterBE.INGOT_SIZE / 2, AllTags.CAST_RODS);
                createTag(p, "rod", "netherite", AllFluids.MOLTEN_NETHERITE, MelterBE.INGOT_SIZE / 2, AllTags.CAST_RODS);
                createTag(p, "rod", "diamond", AllFluids.MOLTEN_DIAMOND, MelterBE.INGOT_SIZE / 2, AllTags.CAST_RODS);
                createTag(p, "rod", "emerald", AllFluids.MOLTEN_EMERALD, MelterBE.INGOT_SIZE / 2, AllTags.CAST_RODS);
                createTag(p, "rod", "lapis", AllFluids.MOLTEN_LAPIS, MelterBE.INGOT_SIZE / 2, AllTags.CAST_RODS);
                createTag(p, "rod", "quartz", AllFluids.MOLTEN_QUARTZ, MelterBE.INGOT_SIZE / 2, AllTags.CAST_RODS);
                createTag(p, "rod", "amethyst", AllFluids.MOLTEN_AMETHYST, MelterBE.INGOT_SIZE / 2, AllTags.CAST_RODS);
                createTag(p, "rod", "prismarine", AllFluids.MOLTEN_PRISMARINE, MelterBE.INGOT_SIZE / 2, AllTags.CAST_RODS);

                createTag(p, "ingot", "aluminium", AllFluids.MOLTEN_ALUMINIUM, MelterBE.INGOT_SIZE, AllTags.CAST_INGOTS);
                createTag(p, "nugget", "aluminium", AllFluids.MOLTEN_ALUMINIUM, MelterBE.NUGGET_SIZE, AllTags.CAST_NUGGETS);
                createTag(p, "dust", "aluminium", AllFluids.MOLTEN_ALUMINIUM, MelterBE.INGOT_SIZE, AllTags.CAST_DUSTS);
                createTag(p, "rod", "aluminium", AllFluids.MOLTEN_ALUMINIUM, MelterBE.INGOT_SIZE / 2, AllTags.CAST_RODS);
                createTag(p, "ingot", "brass", AllFluids.MOLTEN_BRASS, MelterBE.INGOT_SIZE, AllTags.CAST_INGOTS);
                createTag(p, "nugget", "brass", AllFluids.MOLTEN_BRASS, MelterBE.NUGGET_SIZE, AllTags.CAST_NUGGETS);
                createTag(p, "dust", "brass", AllFluids.MOLTEN_BRASS, MelterBE.INGOT_SIZE, AllTags.CAST_DUSTS);
                createTag(p, "rod", "brass", AllFluids.MOLTEN_BRASS, MelterBE.INGOT_SIZE / 2, AllTags.CAST_RODS);
                createTag(p, "ingot", "bronze", AllFluids.MOLTEN_BRONZE, MelterBE.INGOT_SIZE, AllTags.CAST_INGOTS);
                createTag(p, "nugget", "bronze", AllFluids.MOLTEN_BRONZE, MelterBE.NUGGET_SIZE, AllTags.CAST_NUGGETS);
                createTag(p, "dust", "bronze", AllFluids.MOLTEN_BRONZE, MelterBE.INGOT_SIZE, AllTags.CAST_DUSTS);
                createTag(p, "rod", "bronze", AllFluids.MOLTEN_BRONZE, MelterBE.INGOT_SIZE / 2, AllTags.CAST_RODS);
                createTag(p, "ingot", "cast_iron", AllFluids.MOLTEN_CAST_IRON, MelterBE.INGOT_SIZE, AllTags.CAST_INGOTS);
                createTag(p, "nugget", "cast_iron", AllFluids.MOLTEN_CAST_IRON, MelterBE.NUGGET_SIZE, AllTags.CAST_NUGGETS);
                createTag(p, "dust", "cast_iron", AllFluids.MOLTEN_CAST_IRON, MelterBE.INGOT_SIZE, AllTags.CAST_DUSTS);
                createTag(p, "rod", "cast_iron", AllFluids.MOLTEN_CAST_IRON, MelterBE.INGOT_SIZE / 2, AllTags.CAST_RODS);
                createTag(p, "ingot", "electrum", AllFluids.MOLTEN_ELECTRUM, MelterBE.INGOT_SIZE, AllTags.CAST_INGOTS);
                createTag(p, "nugget", "electrum", AllFluids.MOLTEN_ELECTRUM, MelterBE.NUGGET_SIZE, AllTags.CAST_NUGGETS);
                createTag(p, "dust", "electrum", AllFluids.MOLTEN_ELECTRUM, MelterBE.INGOT_SIZE, AllTags.CAST_DUSTS);
                createTag(p, "rod", "electrum", AllFluids.MOLTEN_ELECTRUM, MelterBE.INGOT_SIZE / 2, AllTags.CAST_RODS);
                createTag(p, "ingot", "invar", AllFluids.MOLTEN_INVAR, MelterBE.INGOT_SIZE, AllTags.CAST_INGOTS);
                createTag(p, "nugget", "invar", AllFluids.MOLTEN_INVAR, MelterBE.NUGGET_SIZE, AllTags.CAST_NUGGETS);
                createTag(p, "dust", "invar", AllFluids.MOLTEN_INVAR, MelterBE.INGOT_SIZE, AllTags.CAST_DUSTS);
                createTag(p, "rod", "invar", AllFluids.MOLTEN_INVAR, MelterBE.INGOT_SIZE / 2, AllTags.CAST_RODS);
                createTag(p, "ingot", "lead", AllFluids.MOLTEN_LEAD, MelterBE.INGOT_SIZE, AllTags.CAST_INGOTS);
                createTag(p, "nugget", "lead", AllFluids.MOLTEN_LEAD, MelterBE.NUGGET_SIZE, AllTags.CAST_NUGGETS);
                createTag(p, "dust", "lead", AllFluids.MOLTEN_LEAD, MelterBE.INGOT_SIZE, AllTags.CAST_DUSTS);
                createTag(p, "rod", "lead", AllFluids.MOLTEN_LEAD, MelterBE.INGOT_SIZE / 2, AllTags.CAST_RODS);
                createTag(p, "ingot", "nickel", AllFluids.MOLTEN_NICKEL, MelterBE.INGOT_SIZE, AllTags.CAST_INGOTS);
                createTag(p, "nugget", "nickel", AllFluids.MOLTEN_NICKEL, MelterBE.NUGGET_SIZE, AllTags.CAST_NUGGETS);
                createTag(p, "dust", "nickel", AllFluids.MOLTEN_NICKEL, MelterBE.INGOT_SIZE, AllTags.CAST_DUSTS);
                createTag(p, "rod", "nickel", AllFluids.MOLTEN_NICKEL, MelterBE.INGOT_SIZE / 2, AllTags.CAST_RODS);
                createTag(p, "ingot", "osmium", AllFluids.MOLTEN_OSMIUM, MelterBE.INGOT_SIZE, AllTags.CAST_INGOTS);
                createTag(p, "nugget", "osmium", AllFluids.MOLTEN_OSMIUM, MelterBE.NUGGET_SIZE, AllTags.CAST_NUGGETS);
                createTag(p, "dust", "osmium", AllFluids.MOLTEN_OSMIUM, MelterBE.INGOT_SIZE, AllTags.CAST_DUSTS);
                createTag(p, "rod", "osmium", AllFluids.MOLTEN_OSMIUM, MelterBE.INGOT_SIZE / 2, AllTags.CAST_RODS);
                createTag(p, "ingot", "platinum", AllFluids.MOLTEN_PLATINUM, MelterBE.INGOT_SIZE, AllTags.CAST_INGOTS);
                createTag(p, "nugget", "platinum", AllFluids.MOLTEN_PLATINUM, MelterBE.NUGGET_SIZE, AllTags.CAST_NUGGETS);
                createTag(p, "dust", "platinum", AllFluids.MOLTEN_PLATINUM, MelterBE.INGOT_SIZE, AllTags.CAST_DUSTS);
                createTag(p, "rod", "platinum", AllFluids.MOLTEN_PLATINUM, MelterBE.INGOT_SIZE / 2, AllTags.CAST_RODS);
                createTag(p, "ingot", "silver", AllFluids.MOLTEN_SILVER, MelterBE.INGOT_SIZE, AllTags.CAST_INGOTS);
                createTag(p, "nugget", "silver", AllFluids.MOLTEN_SILVER, MelterBE.NUGGET_SIZE, AllTags.CAST_NUGGETS);
                createTag(p, "dust", "silver", AllFluids.MOLTEN_SILVER, MelterBE.INGOT_SIZE, AllTags.CAST_DUSTS);
                createTag(p, "rod", "silver", AllFluids.MOLTEN_SILVER, MelterBE.INGOT_SIZE / 2, AllTags.CAST_RODS);
                createTag(p, "ingot", "steel", AllFluids.MOLTEN_STEEL, MelterBE.INGOT_SIZE, AllTags.CAST_INGOTS);
                createTag(p, "nugget", "steel", AllFluids.MOLTEN_STEEL, MelterBE.NUGGET_SIZE, AllTags.CAST_NUGGETS);
                createTag(p, "dust", "steel", AllFluids.MOLTEN_STEEL, MelterBE.INGOT_SIZE, AllTags.CAST_DUSTS);
                createTag(p, "rod", "steel", AllFluids.MOLTEN_STEEL, MelterBE.INGOT_SIZE / 2, AllTags.CAST_RODS);
                createTag(p, "ingot", "tin", AllFluids.MOLTEN_TIN, MelterBE.INGOT_SIZE, AllTags.CAST_INGOTS);
                createTag(p, "nugget", "tin", AllFluids.MOLTEN_TIN, MelterBE.NUGGET_SIZE, AllTags.CAST_NUGGETS);
                createTag(p, "dust", "tin", AllFluids.MOLTEN_TIN, MelterBE.INGOT_SIZE, AllTags.CAST_DUSTS);
                createTag(p, "rod", "tin", AllFluids.MOLTEN_TIN, MelterBE.INGOT_SIZE / 2, AllTags.CAST_RODS);
                createTag(p, "ingot", "tungsten", AllFluids.MOLTEN_TUNGSTEN, MelterBE.INGOT_SIZE, AllTags.CAST_INGOTS);
                createTag(p, "nugget", "tungsten", AllFluids.MOLTEN_TUNGSTEN, MelterBE.NUGGET_SIZE, AllTags.CAST_NUGGETS);
                createTag(p, "dust", "tungsten", AllFluids.MOLTEN_TUNGSTEN, MelterBE.INGOT_SIZE, AllTags.CAST_DUSTS);
                createTag(p, "rod", "tungsten", AllFluids.MOLTEN_TUNGSTEN, MelterBE.INGOT_SIZE / 2, AllTags.CAST_RODS);
                createTag(p, "ingot", "zinc", AllFluids.MOLTEN_ZINC, MelterBE.INGOT_SIZE, AllTags.CAST_INGOTS);
                createTag(p, "nugget", "zinc", AllFluids.MOLTEN_ZINC, MelterBE.NUGGET_SIZE, AllTags.CAST_NUGGETS);
                createTag(p, "dust", "zinc", AllFluids.MOLTEN_ZINC, MelterBE.INGOT_SIZE, AllTags.CAST_DUSTS);
                createTag(p, "rod", "zinc", AllFluids.MOLTEN_ZINC, MelterBE.INGOT_SIZE / 2, AllTags.CAST_RODS);

                TableCastingRecipeBuilder.create(Items.IRON_BARS, AllFluids.MOLTEN_IRON.getSource(), MelterBE.INGOT_SIZE * 3 / 8)
                        .unlockedBy("has_iron", has(AllTags.itemC("ingots/iron"))).save(p, SlagEmbers.loc("casting/table/iron_bars"));
                TableCastingRecipeBuilder.create(AllTags.BARS_COPPER, 1, AllFluids.MOLTEN_COPPER.getSource(), MelterBE.INGOT_SIZE / 4)
                        .unlockedBy("has_copper", has(AllTags.itemC("ingots/copper"))).save(p.withConditions(AllTags.present(AllTags.BARS_COPPER)), SlagEmbers.loc("casting/table/copper_bars"));
                TableCastingRecipeBuilder.create(AllTags.BARS_BRASS, 1, AllFluids.MOLTEN_BRASS.getSource(), MelterBE.INGOT_SIZE / 4)
                        .unlockedBy("has_brass", has(AllTags.itemC("ingots/brass"))).save(p.withConditions(AllTags.present(AllTags.BARS_BRASS)), SlagEmbers.loc("casting/table/brass_bars"));

                stone(p, Items.GRANITE, Ingredient.of(Items.DIORITE), "granite");
                stone(p, Items.DIORITE, Ingredient.of(Tags.Items.COBBLESTONES), "diorite");
                stone(p, Items.ANDESITE, Ingredient.of(Items.STONE), "andesite");

                TableCastingRecipeBuilder.create(Items.WET_SPONGE, Fluids.WATER, 1000).duration(60)
                        .castItem(CastItem.of(Ingredient.of(Items.SPONGE), CastItem.Mode.CONSUME))
                        .unlockedBy("has_sponge", has(Items.SPONGE)).save(p, SlagEmbers.loc("casting/table/wet_sponge"));

                TableCastingRecipeBuilder.create(AllTags.ROSE_QUARTZ, 1, AllFluids.MOLTEN_REDSTONE.getSource(), MelterBE.INGOT_SIZE * 4)
                        .castItem(CastItem.of(Ingredient.of(Tags.Items.GEMS_QUARTZ), CastItem.Mode.CONSUME))
                        .unlockedBy("has_quartz", has(Tags.Items.GEMS_QUARTZ)).save(p.withConditions(AllTags.present(AllTags.ROSE_QUARTZ)), SlagEmbers.loc("casting/table/rose_quartz"));

                andesite(p, AllFluids.MOLTEN_ZINC.getSource(), "zinc");
                andesite(p, AllFluids.MOLTEN_IRON.getSource(), "iron");

                gold(p, Items.GOLDEN_APPLE, Items.APPLE, MelterBE.INGOT_SIZE * 8, "golden_apple");
                gold(p, Items.GOLDEN_CARROT, Items.CARROT, MelterBE.NUGGET_SIZE * 8, "golden_carrot");
                gold(p, Items.GLISTERING_MELON_SLICE, Items.MELON_SLICE, MelterBE.NUGGET_SIZE * 8, "glistering_melon_slice");
            })
            .addLayer(() -> RenderType::cutoutMipped)
            .item()
            .model((c, p) -> p.withExistingParent("item/" + c.getName(), p.modLoc("block/table")))
            .build()
            .register();

    private static void alloy(RegistrateRecipeProvider p, FluidEntry<?> result, int nuggets, Tiers temperature, boolean strict, String name, FluidStack base, String metal, FluidStack added) {
        var builder = AlloyingRecipeBuilder.create(result.getSource(), MelterBE.NUGGET_SIZE * nuggets, base, added).temperature(temperature);
        if (strict) builder.strict();
        builder.unlockedBy("has_lava", has(Items.LAVA_BUCKET)).save(p.withConditions(AllTags.present(AllTags.itemC("ingots/" + name)), AllTags.present(AllTags.itemC("ingots/" + metal))), SlagEmbers.loc("alloying/molten_" + name));
    }

    public static void andesite(RegistrateRecipeProvider p, Fluid fluid, String metal) {
        TableCastingRecipeBuilder.create(AllTags.ANDESITE_ALLOY, 1, fluid, MelterBE.NUGGET_SIZE)
                .castItem(CastItem.of(Ingredient.of(Items.ANDESITE), CastItem.Mode.CONSUME))
                .unlockedBy("has_andesite", has(Items.ANDESITE)).save(p.withConditions(AllTags.present(AllTags.ANDESITE_ALLOY)), SlagEmbers.loc("casting/table/andesite_alloy_" + metal));
    }

    public static void gold(RegistrateRecipeProvider p, Item result, Item input, int mb, String name) {
        TableCastingRecipeBuilder.create(result, AllFluids.MOLTEN_GOLD.getSource(), mb)
                .castItem(CastItem.of(Ingredient.of(input), CastItem.Mode.CONSUME))
                .unlockedBy("has_gold", has(AllTags.itemC("ingots/gold"))).save(p, SlagEmbers.loc("casting/table/" + name));
    }

    public static void soak(RegistrateRecipeProvider p, Item result, Ingredient input, Fluid fluid, int mb, String name) {
        BasinCastingRecipeBuilder.create(result, 1, fluid, mb)
                .castItem(CastItem.of(input, CastItem.Mode.CONSUME))
                .unlockedBy("has_basin", has(BASIN.get())).save(p, SlagEmbers.loc("casting/basin/" + name));
    }

    public static void stone(RegistrateRecipeProvider p, Item result, Ingredient input, String name) {
        TableCastingRecipeBuilder.create(result, AllFluids.MOLTEN_QUARTZ.getSource(), MelterBE.INGOT_SIZE)
                .castItem(CastItem.of(input, CastItem.Mode.CONSUME))
                .unlockedBy("has_quartz", has(Tags.Items.GEMS_QUARTZ)).save(p, SlagEmbers.loc("casting/table/" + name));
    }

    public static void createTag(RegistrateRecipeProvider p, String key, String type, FluidEntry<?> fluid, int mb, TagKey<Item> castType) {
        var result = AllTags.itemC(key + "s/" + type);
        TableCastingRecipeBuilder.create(result, 1, fluid.getSource(), mb, castType)
                .unlockedBy("has_" + key, has(result)).save(p.withConditions(AllTags.present(result)), SlagEmbers.loc("casting/table/" + type + "_" + key));
    }

    public static void createTagBlock(RegistrateRecipeProvider p, String type, FluidEntry<?> fluid) {
        var result = AllTags.itemC("storage_blocks/" + type);
        BasinCastingRecipeBuilder.create(result, 1, fluid.getSource(), MelterBE.BLOCK_SIZE)
                .unlockedBy("has_block", has(result)).save(p.withConditions(AllTags.present(result)), SlagEmbers.loc("casting/basin/" + type + "_block"));
    }

    public static void create(RegistrateRecipeProvider p, String key, String type, Item result, FluidEntry<?> fluid, int mb, TagKey<Item> castType) {
        create(p, key, type, result, fluid.getSource(), mb, castType);
    }
    public static void create(RegistrateRecipeProvider p, String key, String type, ItemStack result, FluidEntry<?> fluid, int mb, TagKey<Item> castType) {
        create(p, key, type, result, fluid.getSource(), mb, castType);
    }
    public static void create(RegistrateRecipeProvider p, String key, String type, Item result, Fluid fluid, int mb, TagKey<Item> castType) {
        create(p, key, type, new ItemStack(result), fluid, mb, castType);
    }
    public static void create(RegistrateRecipeProvider p, String key, String type, ItemStack result, Fluid fluid, int mb, TagKey<Item> castType) {
        TableCastingRecipeBuilder.create(result, fluid, mb, castType)
                .unlockedBy("has_" + key, has(AllTags.itemC(key + "s/" + type))).save(p, SlagEmbers.loc("casting/table/" + type + "_" + key));
    }

    public static final BlockEntry<BasinBlock> BASIN = REG.block("basin", BasinBlock::new)
            .initialProperties(() -> Blocks.CAULDRON)
            .lang("Casting Basin")
            .blockstate((c, p) -> p.simpleBlock(c.get(), p.models().getExistingFile(p.modLoc("block/basin"))))
            .properties(p -> p.sound(AllSoundTypes.CRUCIBLE).noOcclusion().mapColor(MapColor.COLOR_GRAY).requiresCorrectToolForDrops())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .recipe((c, p) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, c.get(), 1)
                        .pattern("A A")
                        .pattern("A A")
                        .pattern("AAA")
                        .define('A', AllTags.itemC("ingots/deep_alloy"))
                        .unlockedBy("has_deep_alloy", has(AllTags.itemC("ingots/deep_alloy"))).save(p, SlagEmbers.loc("crafting/" + c.getName()));
                BasinCastingRecipeBuilder.create(Items.AMETHYST_BLOCK, 1, AllFluids.MOLTEN_AMETHYST.getSource(), MelterBE.SMALL_BLOCK_SIZE).unlockedBy("has_block", has(AllTags.itemC("storage_blocks/amethyst"))).save(p, SlagEmbers.loc("casting/basin/amethyst_block"));
                BasinCastingRecipeBuilder.create(Items.COPPER_BLOCK, 1, AllFluids.MOLTEN_COPPER.getSource(), MelterBE.BLOCK_SIZE).unlockedBy("has_block", has(AllTags.COPPER_BLOCKS)).save(p, SlagEmbers.loc("casting/basin/copper_block"));
                BasinCastingRecipeBuilder.create(Items.DIAMOND_BLOCK, 1, AllFluids.MOLTEN_DIAMOND.getSource(), MelterBE.BLOCK_SIZE).unlockedBy("has_block", has(AllTags.itemC("storage_blocks/diamond"))).save(p, SlagEmbers.loc("casting/basin/diamond_block"));
                BasinCastingRecipeBuilder.create(Items.EMERALD_BLOCK, 1, AllFluids.MOLTEN_EMERALD.getSource(), MelterBE.BLOCK_SIZE).unlockedBy("has_block", has(AllTags.itemC("storage_blocks/emerald"))).save(p, SlagEmbers.loc("casting/basin/emerald_block"));
                BasinCastingRecipeBuilder.create(Items.GLOWSTONE, 1, AllFluids.MOLTEN_GLOWSTONE.getSource(), MelterBE.SMALL_BLOCK_SIZE).unlockedBy("has_block", has(Items.GLOWSTONE)).save(p, SlagEmbers.loc("casting/basin/glowstone"));
                BasinCastingRecipeBuilder.create(Items.GOLD_BLOCK, 1, AllFluids.MOLTEN_GOLD.getSource(), MelterBE.BLOCK_SIZE).unlockedBy("has_block", has(AllTags.itemC("storage_blocks/gold"))).save(p, SlagEmbers.loc("casting/basin/gold_block"));
                BasinCastingRecipeBuilder.create(Items.IRON_BLOCK, 1, AllFluids.MOLTEN_IRON.getSource(), MelterBE.BLOCK_SIZE).unlockedBy("has_block", has(AllTags.itemC("storage_blocks/iron"))).save(p, SlagEmbers.loc("casting/basin/iron_block"));
                BasinCastingRecipeBuilder.create(Items.LAPIS_BLOCK, 1, AllFluids.MOLTEN_LAPIS.getSource(), MelterBE.BLOCK_SIZE).unlockedBy("has_block", has(AllTags.itemC("storage_blocks/lapis"))).save(p, SlagEmbers.loc("casting/basin/lapis_block"));
                BasinCastingRecipeBuilder.create(Items.NETHERITE_BLOCK, 1, AllFluids.MOLTEN_NETHERITE.getSource(), MelterBE.BLOCK_SIZE).unlockedBy("has_block", has(AllTags.itemC("storage_blocks/netherite"))).save(p, SlagEmbers.loc("casting/basin/netherite_block"));
                BasinCastingRecipeBuilder.create(Items.QUARTZ_BLOCK, 1, AllFluids.MOLTEN_QUARTZ.getSource(), MelterBE.SMALL_BLOCK_SIZE).unlockedBy("has_block", has(AllTags.QUARTZ_BLOCKS)).save(p, SlagEmbers.loc("casting/basin/quartz_block"));
                BasinCastingRecipeBuilder.create(Items.REDSTONE_BLOCK, 1, AllFluids.MOLTEN_REDSTONE.getSource(), MelterBE.BLOCK_SIZE).unlockedBy("has_block", has(AllTags.itemC("storage_blocks/redstone"))).save(p, SlagEmbers.loc("casting/basin/redstone_block"));
                BasinCastingRecipeBuilder.create(ROSE_GOLD_BLOCK.asItem(), 1, AllFluids.MOLTEN_ROSE_GOLD.getSource(), MelterBE.BLOCK_SIZE).unlockedBy("has_block", has(AllTags.itemC("storage_blocks/rose_gold"))).save(p, SlagEmbers.loc("casting/basin/rose_gold_block"));

                BasinCastingRecipeBuilder.create(Items.OBSIDIAN, 1, AllFluids.MOLTEN_OBSIDIAN.getSource(), MelterBE.BLOCK_SIZE).unlockedBy("has_block", has(Tags.Items.OBSIDIANS)).save(p, SlagEmbers.loc("casting/basin/obsidian"));

                soak(p, Items.TINTED_GLASS, Ingredient.of(Tags.Items.GLASS_BLOCKS_COLORLESS), AllFluids.MOLTEN_AMETHYST.getSource(), MelterBE.INGOT_SIZE * 2, "tinted_glass");
                soak(p, Items.REDSTONE_LAMP, Ingredient.of(Items.GLOWSTONE), AllFluids.MOLTEN_REDSTONE.getSource(), MelterBE.INGOT_SIZE * 4, "redstone_lamp");
                soak(p, Items.MUD, Ingredient.of(Items.DIRT), Fluids.WATER, 250, "mud");
                soak(p, Items.STONE, Ingredient.of(Items.COBBLESTONE), Fluids.LAVA, 50, "stone");
                soak(p, Items.DEEPSLATE, Ingredient.of(Items.COBBLED_DEEPSLATE), Fluids.LAVA, 50, "deepslate");
                soak(p, Items.GLASS, Ingredient.of(ItemTags.SAND), Fluids.LAVA, 50, "glass");

                createTagBlock(p, "aluminium", AllFluids.MOLTEN_ALUMINIUM);
                createTagBlock(p, "brass", AllFluids.MOLTEN_BRASS);
                createTagBlock(p, "bronze", AllFluids.MOLTEN_BRONZE);
                createTagBlock(p, "cast_iron", AllFluids.MOLTEN_CAST_IRON);
                createTagBlock(p, "electrum", AllFluids.MOLTEN_ELECTRUM);
                createTagBlock(p, "invar", AllFluids.MOLTEN_INVAR);
                createTagBlock(p, "lead", AllFluids.MOLTEN_LEAD);
                createTagBlock(p, "nickel", AllFluids.MOLTEN_NICKEL);
                createTagBlock(p, "osmium", AllFluids.MOLTEN_OSMIUM);
                createTagBlock(p, "platinum", AllFluids.MOLTEN_PLATINUM);
                createTagBlock(p, "silver", AllFluids.MOLTEN_SILVER);
                createTagBlock(p, "steel", AllFluids.MOLTEN_STEEL);
                createTagBlock(p, "tin", AllFluids.MOLTEN_TIN);
                createTagBlock(p, "tungsten", AllFluids.MOLTEN_TUNGSTEN);
                createTagBlock(p, "zinc", AllFluids.MOLTEN_ZINC);
            })
            .addLayer(() -> RenderType::cutoutMipped)
            .item()
            .model((c, p) -> p.withExistingParent("item/" + c.getName(), p.modLoc("block/basin")))
            .build()
            .register();

    public static final BlockEntry<DrainBlock> DRAIN = REG.block("drain", DrainBlock::new)
            .initialProperties(() -> Blocks.CAULDRON)
            .blockstate(BlockHelper::genHorizontalDirectional)
            .properties(p -> p.sound(AllSoundTypes.CRUCIBLE).noOcclusion().mapColor(MapColor.COLOR_GRAY).requiresCorrectToolForDrops())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .recipe((c, p) -> ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, c.get(), 1)
                    .requires(AllTags.itemC("ingots/deep_alloy")).unlockedBy("has_deep_alloy", has(AllTags.itemC("ingots/deep_alloy"))).save(p, SlagEmbers.loc("crafting/" + c.getName())))
            .addLayer(() -> RenderType::cutoutMipped)
            .item()
            .model((c, p) -> p.withExistingParent("item/" + c.getName(), p.modLoc("block/drain")))
            .build()
            .register();

    public static final BlockEntry<MelterBlock> MELTER = REG.block("melter", MelterBlock::new)
            .initialProperties(() -> Blocks.CAULDRON)
            .blockstate(BlockHelper::genHorizontalDirectional)
            .properties(p -> p.sound(AllSoundTypes.CRUCIBLE).noOcclusion().mapColor(MapColor.COLOR_GRAY).requiresCorrectToolForDrops())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .recipe((c, p) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, c.get(), 1)
                        .pattern("AAA")
                        .pattern("RDR")
                        .pattern("AAA")
                        .define('A', AllTags.itemC("ingots/deep_alloy")).define('R', AllTags.itemC("ingots/rose_gold")).define('D', DRAIN.get())
                        .unlockedBy("has_deep_alloy", has(AllTags.itemC("ingots/deep_alloy"))).save(p, SlagEmbers.loc("crafting/" + c.getName()));

                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), MelterBE.INGOT_SIZE, Items.LODESTONE);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), MelterBE.INGOT_SIZE * 24, Items.HEAVY_CORE);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_GOLD.getSource(), MelterBE.INGOT_SIZE * 8 * 4, Items.ENCHANTED_GOLDEN_APPLE);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_GOLD.getSource(), MelterBE.BLOCK_SIZE, Items.BELL);

                gemMeltable(p, "diamond", AllFluids.MOLTEN_DIAMOND.getSource(), Tags.Items.STORAGE_BLOCKS_DIAMOND, Tags.Items.GEMS_DIAMOND, AllTags.itemC("nuggets/diamond"));
                gemMeltable(p, "emerald", AllFluids.MOLTEN_EMERALD.getSource(), Tags.Items.STORAGE_BLOCKS_EMERALD, Tags.Items.GEMS_EMERALD, AllTags.itemC("nuggets/emerald"));
                gemMeltable(p, "lapis", AllFluids.MOLTEN_LAPIS.getSource(), Tags.Items.STORAGE_BLOCKS_LAPIS, Tags.Items.GEMS_LAPIS, AllTags.itemC("nuggets/lapis"));
                dustMeltable(p, "redstone", AllFluids.MOLTEN_REDSTONE.getSource(), Tags.Items.STORAGE_BLOCKS_REDSTONE, Tags.Items.DUSTS_REDSTONE);
                crystalMeltable(p, "quartz", AllFluids.MOLTEN_QUARTZ.getSource(), AllTags.QUARTZ_BLOCKS, Tags.Items.GEMS_QUARTZ);
                crystalMeltable(p, "amethyst", AllFluids.MOLTEN_AMETHYST.getSource(), AllTags.AMETHYST_BLOCKS, Tags.Items.GEMS_AMETHYST);
                crystalMeltable(p, "prismarine", AllFluids.MOLTEN_PRISMARINE.getSource(), null, Tags.Items.GEMS_PRISMARINE);
                compatMeltable(p, "rose_quartz_crystals", AllFluids.MOLTEN_ROSE_QUARTZ.getSource(), MelterBE.INGOT_SIZE, AllTags.ROSE_QUARTZ);
                compatMeltable(p, "polished_rose_quartz", AllFluids.MOLTEN_ROSE_QUARTZ.getSource(), MelterBE.INGOT_SIZE, AllTags.POLISHED_ROSE_QUARTZ);
                MeltingRecipeBuilder.create(p, "echo_shards", AllFluids.MOLTEN_ECHO.getSource(), MelterBE.INGOT_SIZE, Items.ECHO_SHARD);
                dustMeltable(p, "glowstone", AllFluids.MOLTEN_GLOWSTONE.getSource(), null, Tags.Items.DUSTS_GLOWSTONE);
                MeltingRecipeBuilder.create(p, "glowstone_blocks", AllFluids.MOLTEN_GLOWSTONE.getSource(), MelterBE.SMALL_BLOCK_SIZE, Items.GLOWSTONE);
                ingotMeltable(p, "copper", AllFluids.MOLTEN_COPPER.getSource(), AllTags.COPPER_BLOCKS, Tags.Items.INGOTS_COPPER, AllTags.itemC("nuggets/copper"));
                ingotMeltable(p, "gold", AllFluids.MOLTEN_GOLD.getSource(), Tags.Items.STORAGE_BLOCKS_GOLD, Tags.Items.INGOTS_GOLD, Tags.Items.NUGGETS_GOLD);
                ingotMeltable(p, "iron", AllFluids.MOLTEN_IRON.getSource(), Tags.Items.STORAGE_BLOCKS_IRON, Tags.Items.INGOTS_IRON, Tags.Items.NUGGETS_IRON);
                ingotMeltable(p, "netherite", AllFluids.MOLTEN_NETHERITE.getSource(), Tags.Items.STORAGE_BLOCKS_NETHERITE, Tags.Items.INGOTS_NETHERITE, AllTags.itemC("nuggets/netherite"));
                ingotMeltable(p, "rose_gold", AllFluids.MOLTEN_ROSE_GOLD.getSource(), AllTags.itemC("storage_blocks/rose_gold"), AllTags.itemC("ingots/rose_gold"), AllTags.itemC("nuggets/rose_gold"));

                oreMeltable(p, "raw_copper", AllFluids.MOLTEN_COPPER.getSource(), Tags.Items.STORAGE_BLOCKS_RAW_COPPER, AllTags.COPPER_RAW_MATERIALS, null);
                oreMeltable(p, "raw_iron", AllFluids.MOLTEN_IRON.getSource(), Tags.Items.STORAGE_BLOCKS_RAW_IRON, AllTags.IRON_RAW_MATERIALS, null);
                oreMeltable(p, "raw_gold", AllFluids.MOLTEN_GOLD.getSource(), Tags.Items.STORAGE_BLOCKS_RAW_GOLD, AllTags.GOLD_RAW_MATERIALS, null);

                oreMeltableGem(p, "raw_diamond", AllFluids.MOLTEN_DIAMOND.getSource(), null, Tags.Items.ORES_DIAMOND, null);
                oreMeltableGem(p, "raw_emerald", AllFluids.MOLTEN_EMERALD.getSource(), null, Tags.Items.ORES_EMERALD, null);
                oreMeltableGem(p, "raw_quartz", AllFluids.MOLTEN_QUARTZ.getSource(), null, Tags.Items.ORES_QUARTZ, null);

                metalMeltable(p, "copper", AllFluids.MOLTEN_COPPER.getSource(), true);
                metalMeltable(p, "gold", AllFluids.MOLTEN_GOLD.getSource(), true);
                metalMeltable(p, "iron", AllFluids.MOLTEN_IRON.getSource(), true);
                metalMeltable(p, "netherite", AllFluids.MOLTEN_NETHERITE.getSource(), false);
                formMeltable(p, "diamond", AllFluids.MOLTEN_DIAMOND.getSource());
                formMeltable(p, "emerald", AllFluids.MOLTEN_EMERALD.getSource());
                formMeltable(p, "lapis", AllFluids.MOLTEN_LAPIS.getSource());
                formMeltable(p, "quartz", AllFluids.MOLTEN_QUARTZ.getSource());
                formMeltable(p, "amethyst", AllFluids.MOLTEN_AMETHYST.getSource());
                formMeltable(p, "prismarine", AllFluids.MOLTEN_PRISMARINE.getSource());
                formMeltable(p, "rose_quartz", AllFluids.MOLTEN_ROSE_QUARTZ.getSource());

                compatMeltable(p, "aluminium", AllFluids.MOLTEN_ALUMINIUM.getSource(), true);
                compatMeltable(p, "brass", AllFluids.MOLTEN_BRASS.getSource(), false);
                compatMeltable(p, "bronze", AllFluids.MOLTEN_BRONZE.getSource(), false);
                compatMeltable(p, "cast_iron", AllFluids.MOLTEN_CAST_IRON.getSource(), false);
                compatMeltable(p, "electrum", AllFluids.MOLTEN_ELECTRUM.getSource(), false);
                compatMeltable(p, "invar", AllFluids.MOLTEN_INVAR.getSource(), false);
                compatMeltable(p, "lead", AllFluids.MOLTEN_LEAD.getSource(), true);
                compatMeltable(p, "nickel", AllFluids.MOLTEN_NICKEL.getSource(), true);
                compatMeltable(p, "osmium", AllFluids.MOLTEN_OSMIUM.getSource(), true);
                compatMeltable(p, "platinum", AllFluids.MOLTEN_PLATINUM.getSource(), true);
                compatMeltable(p, "silver", AllFluids.MOLTEN_SILVER.getSource(), true);
                compatMeltable(p, "steel", AllFluids.MOLTEN_STEEL.getSource(), false);
                compatMeltable(p, "tin", AllFluids.MOLTEN_TIN.getSource(), true);
                compatMeltable(p, "tungsten", AllFluids.MOLTEN_TUNGSTEN.getSource(), true);
                compatMeltable(p, "zinc", AllFluids.MOLTEN_ZINC.getSource(), true);

                MeltingRecipeBuilder.create(AllFluids.MOLTEN_OBSIDIAN.getSource(), MelterBE.BLOCK_SIZE, Tags.Items.OBSIDIANS)
                        .unlockedBy("has_meltable_obsidian", has(Tags.Items.OBSIDIANS))
                        .save(p, SlagEmbers.loc("melting/obsidian_blocks"));
                compatMeltable(p, "obsidian_plates", AllFluids.MOLTEN_OBSIDIAN.getSource(), MelterBE.INGOT_SIZE, AllTags.itemC("plates/obsidian"));

                MeltingRecipeBuilder.create(AllFluids.MOLTEN_AMETHYST.getSource(), MelterBE.INGOT_SIZE * 3, Items.LARGE_AMETHYST_BUD)
                        .unlockedBy("has_meltable_buds", has(Tags.Items.BUDS))
                        .save(p, SlagEmbers.loc("melting/large_amethyst_bud"));
                MeltingRecipeBuilder.create(AllFluids.MOLTEN_AMETHYST.getSource(), MelterBE.INGOT_SIZE * 2, Items.MEDIUM_AMETHYST_BUD)
                        .unlockedBy("has_meltable_buds", has(Tags.Items.BUDS))
                        .save(p, SlagEmbers.loc("melting/medium_amethyst_bud"));
                MeltingRecipeBuilder.create(AllFluids.MOLTEN_AMETHYST.getSource(), MelterBE.INGOT_SIZE, Items.SMALL_AMETHYST_BUD)
                        .unlockedBy("has_meltable_buds", has(Tags.Items.BUDS))
                        .save(p, SlagEmbers.loc("melting/small_amethyst_bud"));
            })
            .addLayer(() -> RenderType::cutoutMipped)
            .item()
            .model((c, p) -> p.withExistingParent("item/" + c.getName(), p.modLoc("block/melter")))
            .build()
            .register();

    public static final BlockEntry<InterfaceBlock> INTERFACE = REG.block("crucible_interface", InterfaceBlock::new)
            .initialProperties(() -> Blocks.CAULDRON)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY).sound(AllSoundTypes.CRUCIBLE).requiresCorrectToolForDrops())
            .blockstate(BlockHelper::genHorizontalDirectional)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .recipe((c, p) -> ShapedRecipeBuilder.shaped(RecipeCategory.MISC, c.get(), 1)
                    .pattern("AA")  .pattern("GG")
                    .define('A', AllTags.itemC("ingots/deep_alloy")).define('G', AllTags.itemC("ingots/gold"))
                    .unlockedBy("has_deep_alloy", has(AllTags.itemC("ingots/deep_alloy"))).save(p, SlagEmbers.loc("crafting/" + c.getName())))
            .lang("Fluid Interface")
            .simpleItem()
            .register();

    public static final BlockEntry<ForgeBlock> FORGE = REG.block("brick_forge", ForgeBlock::new)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
                    .sound(SoundType.MUD_BRICKS)
                    .strength(2.5f, 3.0f)
                    .requiresCorrectToolForDrops()
                    .lightLevel(BlockHelper.litBlockEmission(13))
            ).blockstate(BlockHelper.empty())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .recipe((c, p) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, c.get(), 1).pattern("BLB").pattern("LFL").pattern("BLB")
                        .define('F', Items.FURNACE).define('B', Items.MUD_BRICKS).define('L', ItemTags.LOGS)
                        .unlockedBy("has_furnace", has(Items.FURNACE)).save(p, SlagEmbers.loc("crafting/" + c.getName()));

                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.BUILDING_BLOCKS, Blocks.GLASS, Ingredient.of(ItemTags.SMELTS_TO_GLASS), 0.1F)
                        .unlockedBy("has_smelts_to_glass", has(ItemTags.SMELTS_TO_GLASS))
                        .save(p, SlagEmbers.loc("double_smelting/smelts_to_glass"));


                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.MISC, Items.BRICK, Ingredient.of(Items.CLAY_BALL), 0.3F)
                        .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                        .save(p, SlagEmbers.loc("double_smelting/brick"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.MISC, Items.CHARCOAL, Ingredient.of(ItemTags.LOGS_THAT_BURN), 0.15F)
                        .unlockedBy("has_log", has(ItemTags.LOGS_THAT_BURN))
                        .save(p, SlagEmbers.loc("double_smelting/charcoal"));

                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.MISC, Items.POPPED_CHORUS_FRUIT, Ingredient.of(Items.CHORUS_FRUIT), 0.1F)
                        .unlockedBy("has_chorus_fruit", has(Items.CHORUS_FRUIT))
                        .save(p, SlagEmbers.loc("double_smelting/popped_chorus_fruit"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.MISC, Items.LIME_DYE, Ingredient.of(Blocks.SEA_PICKLE), 0.1F)
                        .unlockedBy("has_sea_pickle", has(Blocks.SEA_PICKLE))
                        .save(p, SlagEmbers.loc("double_smelting/lime_dye"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.MISC, Items.GREEN_DYE, Ingredient.of(Blocks.CACTUS), 1.0F)
                        .unlockedBy("has_cactus", has(Blocks.CACTUS))
                        .save(p, SlagEmbers.loc("double_smelting/green_dye"));


                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.BUILDING_BLOCKS, Blocks.TERRACOTTA.asItem(), Ingredient.of(Blocks.CLAY), 0.35F)
                        .unlockedBy("has_clay_block", has(Blocks.CLAY))
                        .save(p, SlagEmbers.loc("double_smelting/terracotta"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.MISC, Items.NETHER_BRICK, Ingredient.of(Blocks.NETHERRACK), 0.1F)
                        .unlockedBy("has_netherrack", has(Blocks.NETHERRACK))
                        .save(p, SlagEmbers.loc("double_smelting/nether_brick"));

                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_STONE.asItem(), Ingredient.of(Blocks.STONE), 0.1F)
                        .unlockedBy("has_stone", has(Blocks.STONE))
                        .save(p, SlagEmbers.loc("double_smelting/smooth_stone"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_SANDSTONE.asItem(), Ingredient.of(Blocks.SANDSTONE), 0.1F)
                        .unlockedBy("has_sandstone", has(Blocks.SANDSTONE))
                        .save(p, SlagEmbers.loc("double_smelting/smooth_sandstone"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_RED_SANDSTONE.asItem(), Ingredient.of(Blocks.RED_SANDSTONE), 0.1F)
                        .unlockedBy("has_red_sandstone", has(Blocks.RED_SANDSTONE))
                        .save(p, SlagEmbers.loc("double_smelting/smooth_red_sandstone"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_QUARTZ.asItem(), Ingredient.of(Blocks.QUARTZ_BLOCK), 0.1F)
                        .unlockedBy("has_quartz_block", has(Blocks.QUARTZ_BLOCK))
                        .save(p, SlagEmbers.loc("double_smelting/smooth_quartz"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_BASALT, Ingredient.of(Blocks.BASALT), 0.1F)
                        .unlockedBy("has_basalt", has(Blocks.BASALT))
                        .save(p, SlagEmbers.loc("double_smelting/smooth_basalt"));

                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.BUILDING_BLOCKS, Blocks.STONE.asItem(), Ingredient.of(Blocks.COBBLESTONE), 0.1F)
                        .unlockedBy("has_cobblestone", has(Blocks.COBBLESTONE))
                        .save(p, SlagEmbers.loc("double_smelting/stone"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE, Ingredient.of(Blocks.COBBLED_DEEPSLATE), 0.1F)
                        .unlockedBy("has_cobbled_deepslate", has(Blocks.COBBLED_DEEPSLATE))
                        .save(p, SlagEmbers.loc("double_smelting/deepslate"));

                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.BUILDING_BLOCKS, Blocks.CRACKED_STONE_BRICKS.asItem(), Ingredient.of(Blocks.STONE_BRICKS), 0.1F)
                        .unlockedBy("has_stone_bricks", has(Blocks.STONE_BRICKS))
                        .save(p, SlagEmbers.loc("double_smelting/cracked_stone_bricks"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.BUILDING_BLOCKS, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.asItem(), Ingredient.of(Blocks.POLISHED_BLACKSTONE_BRICKS), 0.1F)
                        .unlockedBy("has_polished_blackstone_bricks", has(Blocks.POLISHED_BLACKSTONE_BRICKS))
                        .save(p, SlagEmbers.loc("double_smelting/cracked_polished_blackstone_bricks"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.BUILDING_BLOCKS, Blocks.CRACKED_NETHER_BRICKS.asItem(), Ingredient.of(Blocks.NETHER_BRICKS), 0.1F)
                        .unlockedBy("has_nether_bricks", has(Blocks.NETHER_BRICKS))
                        .save(p, SlagEmbers.loc("double_smelting/cracked_nether_bricks"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.BUILDING_BLOCKS, Blocks.CRACKED_DEEPSLATE_BRICKS.asItem(), Ingredient.of(Blocks.DEEPSLATE_BRICKS), 0.1F)
                        .unlockedBy("has_deepslate_bricks", has(Blocks.DEEPSLATE_BRICKS))
                        .save(p, SlagEmbers.loc("double_smelting/cracked_deepslate_bricks"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.BUILDING_BLOCKS, Blocks.CRACKED_DEEPSLATE_TILES.asItem(), Ingredient.of(Blocks.DEEPSLATE_TILES), 0.1F)
                        .unlockedBy("has_deepslate_tiles", has(Blocks.DEEPSLATE_TILES))
                        .save(p, SlagEmbers.loc("double_smelting/cracked_deepslate_tiles"));


                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.DECORATIONS, Blocks.BLACK_GLAZED_TERRACOTTA.asItem(), Ingredient.of(Blocks.BLACK_TERRACOTTA), 0.1F)
                        .unlockedBy("has_black_terracotta", has(Blocks.BLACK_TERRACOTTA))
                        .save(p, SlagEmbers.loc("double_smelting/black_glazed_terracotta"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.DECORATIONS, Blocks.BLUE_GLAZED_TERRACOTTA.asItem(), Ingredient.of(Blocks.BLUE_TERRACOTTA), 0.1F)
                        .unlockedBy("has_blue_terracotta", has(Blocks.BLUE_TERRACOTTA))
                        .save(p, SlagEmbers.loc("double_smelting/blue_glazed_terracotta"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.DECORATIONS, Blocks.BROWN_GLAZED_TERRACOTTA.asItem(), Ingredient.of(Blocks.BROWN_TERRACOTTA), 0.1F)
                        .unlockedBy("has_brown_terracotta", has(Blocks.BROWN_TERRACOTTA))
                        .save(p, SlagEmbers.loc("double_smelting/brown_glazed_terracotta"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.DECORATIONS, Blocks.CYAN_GLAZED_TERRACOTTA.asItem(), Ingredient.of(Blocks.CYAN_TERRACOTTA), 0.1F)
                        .unlockedBy("has_cyan_terracotta", has(Blocks.CYAN_TERRACOTTA))
                        .save(p, SlagEmbers.loc("double_smelting/cyan_glazed_terracotta"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.DECORATIONS, Blocks.GRAY_GLAZED_TERRACOTTA.asItem(), Ingredient.of(Blocks.GRAY_TERRACOTTA), 0.1F)
                        .unlockedBy("has_gray_terracotta", has(Blocks.GRAY_TERRACOTTA))
                        .save(p, SlagEmbers.loc("double_smelting/gray_glazed_terracotta"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.DECORATIONS, Blocks.GREEN_GLAZED_TERRACOTTA.asItem(), Ingredient.of(Blocks.GREEN_TERRACOTTA), 0.1F)
                        .unlockedBy("has_green_terracotta", has(Blocks.GREEN_TERRACOTTA))
                        .save(p, SlagEmbers.loc("double_smelting/green_glazed_terracotta"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.DECORATIONS, Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA.asItem(), Ingredient.of(Blocks.LIGHT_BLUE_TERRACOTTA), 0.1F)
                        .unlockedBy("has_light_blue_terracotta", has(Blocks.LIGHT_BLUE_TERRACOTTA))
                        .save(p, SlagEmbers.loc("double_smelting/light_blue_glazed_terracotta"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.DECORATIONS, Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA.asItem(), Ingredient.of(Blocks.LIGHT_GRAY_TERRACOTTA), 0.1F)
                        .unlockedBy("has_light_gray_terracotta", has(Blocks.LIGHT_GRAY_TERRACOTTA))
                        .save(p, SlagEmbers.loc("double_smelting/light_gray_glazed_terracotta"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.DECORATIONS, Blocks.LIME_GLAZED_TERRACOTTA.asItem(), Ingredient.of(Blocks.LIME_TERRACOTTA), 0.1F)
                        .unlockedBy("has_lime_terracotta", has(Blocks.LIME_TERRACOTTA))
                        .save(p, SlagEmbers.loc("double_smelting/lime_glazed_terracotta"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.DECORATIONS, Blocks.MAGENTA_GLAZED_TERRACOTTA.asItem(), Ingredient.of(Blocks.MAGENTA_TERRACOTTA), 0.1F)
                        .unlockedBy("has_magenta_terracotta", has(Blocks.MAGENTA_TERRACOTTA))
                        .save(p, SlagEmbers.loc("double_smelting/magenta_glazed_terracotta"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.DECORATIONS, Blocks.ORANGE_GLAZED_TERRACOTTA.asItem(), Ingredient.of(Blocks.ORANGE_TERRACOTTA), 0.1F)
                        .unlockedBy("has_orange_terracotta", has(Blocks.ORANGE_TERRACOTTA))
                        .save(p, SlagEmbers.loc("double_smelting/orange_glazed_terracotta"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.DECORATIONS, Blocks.PINK_GLAZED_TERRACOTTA.asItem(), Ingredient.of(Blocks.PINK_TERRACOTTA), 0.1F)
                        .unlockedBy("has_pink_terracotta", has(Blocks.PINK_TERRACOTTA))
                        .save(p, SlagEmbers.loc("double_smelting/pink_glazed_terracotta"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.DECORATIONS, Blocks.PURPLE_GLAZED_TERRACOTTA.asItem(), Ingredient.of(Blocks.PURPLE_TERRACOTTA), 0.1F)
                        .unlockedBy("has_purple_terracotta", has(Blocks.PURPLE_TERRACOTTA))
                        .save(p, SlagEmbers.loc("double_smelting/purple_glazed_terracotta"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.DECORATIONS, Blocks.RED_GLAZED_TERRACOTTA.asItem(), Ingredient.of(Blocks.RED_TERRACOTTA), 0.1F)
                        .unlockedBy("has_red_terracotta", has(Blocks.RED_TERRACOTTA))
                        .save(p, SlagEmbers.loc("double_smelting/red_glazed_terracotta"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.DECORATIONS, Blocks.WHITE_GLAZED_TERRACOTTA.asItem(), Ingredient.of(Blocks.WHITE_TERRACOTTA), 0.1F)
                        .unlockedBy("has_white_terracotta", has(Blocks.WHITE_TERRACOTTA))
                        .save(p, SlagEmbers.loc("double_smelting/white_glazed_terracotta"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.DECORATIONS, Blocks.YELLOW_GLAZED_TERRACOTTA.asItem(), Ingredient.of(Blocks.YELLOW_TERRACOTTA), 0.1F)
                        .unlockedBy("has_yellow_terracotta", has(Blocks.YELLOW_TERRACOTTA))
                        .save(p, SlagEmbers.loc("double_smelting/yellow_glazed_terracotta"));

            }).simpleItem()
            .register();


    public static final BlockEntry<Block> DEEP_ALLOY_BRICKS = REG.block("deep_alloy_bricks", Block::new)
            .lang("Deep Alloy Bricks")
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY).sound(AllSoundTypes.CRUCIBLE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_STONE_TOOL)
            .recipe((c, p) -> ShapedRecipeBuilder.shaped(RecipeCategory.MISC, c.get(), 4)
                    .pattern("DD").pattern("DD").define('D', AllTags.itemC("ingots/deep_alloy"))
                    .unlockedBy("has_deep_alloy", has(AllTags.itemC("ingots/deep_alloy"))).save(p, SlagEmbers.loc("crafting/" + c.getName())))
            .simpleItem()
            .register();

    public static final BlockEntry<StairBlock> DEEP_ALLOY_BRICK_STAIRS = REG.block("deep_alloy_brick_stairs", p -> new StairBlock(DEEP_ALLOY_BRICKS.getDefaultState(), p))
            .lang("Deep Alloy Brick Stairs")
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY).sound(AllSoundTypes.CRUCIBLE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_STONE_TOOL)
            .recipe((c, p) -> ShapedRecipeBuilder.shaped(RecipeCategory.MISC, c.get(), 4)
                    .pattern("D  ").pattern("DD ").pattern("DDD").define('D', DEEP_ALLOY_BRICKS.get())
                    .unlockedBy("has_deep_alloy", has(AllTags.itemC("ingots/deep_alloy"))).save(p, SlagEmbers.loc("crafting/" + c.getName())))
            .blockstate((c, p) -> p.stairsBlock(c.get(), SlagEmbers.loc("block/deep_alloy_bricks")))
            .simpleItem()
            .register();

    public static final BlockEntry<SlabBlock> DEEP_ALLOY_BRICK_SLAB = REG.block("deep_alloy_brick_slab", SlabBlock::new)
            .initialProperties(() -> Blocks.DEEPSLATE_BRICK_SLAB)
            .lang("Deep Alloy Brick Slab")
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY).sound(AllSoundTypes.CRUCIBLE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_STONE_TOOL)
            .recipe((c, p) -> ShapedRecipeBuilder.shaped(RecipeCategory.MISC, c.get(), 6)
                    .pattern("DDD").define('D', DEEP_ALLOY_BRICKS.get())
                    .unlockedBy("has_deep_alloy", has(AllTags.itemC("ingots/deep_alloy"))).save(p, SlagEmbers.loc("crafting/" + c.getName())))
            .blockstate((c, p) -> p.slabBlock(c.get(), SlagEmbers.loc("block/deep_alloy_bricks"), SlagEmbers.loc("block/deep_alloy_bricks")))
            .loot((p, c) -> p.add(c, p.createSlabItemTable(c)))
            .simpleItem()
            .register();

    public static void register() {}
}
