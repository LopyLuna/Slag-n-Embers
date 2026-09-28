package dev.lopyluna.slag.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class SlagCommonConfigs {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue CAPACITY_PER_CRUCIBLE = BUILDER.comment("CapacityPerCrucible")
            .defineInRange("CapacityPerCrucible", 1000, 10, 10000);

    public static final ModConfigSpec.BooleanValue CAST_ONLY_PARTS = BUILDER.comment("Disables the paper template crafting recipes for parts that can be cast in the smeltery once their material reaches CastOnlyTier (requires /reload)")
            .define("CastOnlyParts", false);

    public static final ModConfigSpec.DoubleValue CAST_ONLY_TIER = BUILDER.comment("Minimum material tier affected by CastOnlyParts")
            .defineInRange("CastOnlyTier", 3.5, 0, 100);

    public static final ModConfigSpec.BooleanValue CAST_MOLD_IMPRINTING = BUILDER.comment("Allows imprinting molds that are made by casting, such as the cast iron mold, by right-clicking them with items in the inventory while not in creative mode")
            .define("CastMoldImprinting", false);

    public static final ModConfigSpec.BooleanValue SPOUT_TABLE = BUILDER.comment("Lets Create's spout pour into the casting table")
            .define("SpoutFillsTable", true);

    public static final ModConfigSpec.BooleanValue SPOUT_BASIN = BUILDER.comment("Lets Create's spout pour into the casting basin")
            .define("SpoutFillsBasin", false);

    public static final ModConfigSpec.BooleanValue RECYCLING = BUILDER.comment("Whether to generate recycling recipes from crafting, cooking, stonecutting and smithing recipes, for items that don't have a melting recipe yet")
            .define("RecyclingRecipes", true);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
