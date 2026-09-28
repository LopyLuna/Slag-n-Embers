package dev.lopyluna.slag.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class SlagServerConfigs {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue CRUCIBLE_MAX_WIDTH = BUILDER.comment("Max Width for the Crucible")
            .defineInRange("CrucibleMaxWidth", 8, 1, 48);
    public static final ModConfigSpec.IntValue CRUCIBLE_MAX_HEIGHT = BUILDER.comment("Max Height for the Crucible")
            .defineInRange("CrucibleMaxHeight", 12, 1, 48);
    public static final ModConfigSpec.IntValue CRUCIBLE_UPDATES_PER_TICK = BUILDER.comment("How many Crucible block updates (shape, light, sync, placing) each dimension applies per tick, large Crucibles spread the rest over the next ticks. Raise it on strong machines for faster updates, lower it on weak machines for less stutter")
            .defineInRange("CrucibleUpdatesPerTick", 2048, 64, 1048576);
    public static final ModConfigSpec.IntValue CRUCIBLE_LIGHT_RANGE = BUILDER.comment("How close in blocks a player must be for a Crucible to update its light, farther Crucibles catch up once a player comes near")
            .defineInRange("CrucibleLightRange", 64, 8, 1024);
    public static final ModConfigSpec.IntValue DRAIN_MB_SPEED = BUILDER.comment("Drain Speed in Millibuckets")
            .defineInRange("DrainMbSpeed", 25, 1, 10000);
    public static final ModConfigSpec.IntValue DRAIN_RANGE = BUILDER.comment("How many blocks below the Drain it can pour into a Fluid Container")
            .defineInRange("DrainRange", 16, 1, 64);

    public static final ModConfigSpec.BooleanValue INSERT_FLUID_ITEM_INTO_CRUCIBLE = BUILDER.comment("Whether to insert Fluid Items into the Crucible")
            .define("InsertFluidIntoCrucible", true);
    public static final ModConfigSpec.BooleanValue EXTRACT_FLUID_FROM_DRAIN_TO_ITEM = BUILDER.comment("Whether to extract Fluid from Drain to Item")
            .define("ExtractFluidFromDrainToItem", true);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> TAG_RESULT_PRIORITIES = BUILDER.comment("Mod priorities for tag recipe results as modid=priority, the highest priority item in the tag is used, unlisted mods are 0 and negative values are a last resort")
            .defineListAllowEmpty("TagResultPriorities", List.of("create=9500", "unify=9000", "ad_astra=5000", "oritech=-90", "immersiveengineering=-100"), () -> "modid=0", entry -> entry instanceof String string && string.matches("\\s*[a-z0-9_.-]+\\s*=\\s*-?\\d+\\s*"));

    public static final ModConfigSpec SPEC = BUILDER.build();
}
