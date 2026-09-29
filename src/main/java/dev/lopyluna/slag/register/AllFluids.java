package dev.lopyluna.slag.register;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.util.entry.FluidEntry;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.utils.Registration;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static dev.lopyluna.slag.SlagEmbers.REG;

@SuppressWarnings("unused")
public class AllFluids {
    public static final Map<FluidEntry<LavaLikeFluid.Flowing>, TagKey<Item>> COMPAT = new LinkedHashMap<>();
    public static List<FluidEntry<LavaLikeFluid.Flowing>> HIDDEN = List.of();

    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_OBSIDIAN =
            newMoltenFluid(REG, "Obsidian", () -> 0x3B145F).register();

    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_AMETHYST =
            newMoltenFluid(REG, "Amethyst", () -> 0xAC87CF).register();
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_COPPER =
            newMoltenFluid(REG, "Copper", () -> 0xBC674C).register();
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_DEBRIS =
            newMoltenFluid(REG, "Debris", () -> 0x5C3A30).register();
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_DIAMOND =
            newMoltenFluid(REG, "Diamond", () -> 0x54CAC1).register();
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_ECHO =
            newMoltenFluid(REG, "Echo", () -> 0x0E5A68).register();
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_EMERALD =
            newMoltenFluid(REG, "Emerald", () -> 0x40C066).register();
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_GLOWSTONE =
            newMoltenFluid(REG, "Glowstone", () -> 0xD9A45C).properties(b -> b.lightLevel(15)).register();
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_GOLD =
            newMoltenFluid(REG, "Gold", () -> 0xEDCC68).register();
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_IRON =
            newMoltenFluid(REG, "Iron", () -> 0xACB2B4).register();
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_LAPIS =
            newMoltenFluid(REG, "Lapis", () -> 0x2A54A5).register();
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_NETHERITE =
            newMoltenFluid(REG, "Netherite", () -> 0x50474E).register();
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_PRISMARINE =
            newMoltenFluid(REG, "Prismarine", () -> 0x7BB5A4).register();
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_QUARTZ =
            newMoltenFluid(REG, "Quartz", () -> 0xD9D1C3).register();
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_REDSTONE =
            newMoltenFluid(REG, "Redstone", () -> 0x910F04).register();
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_ROSE_GOLD =
            newMoltenFluid(REG, "Rose Gold", () -> 0xEBAFB6).register();

    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_ALUMINIUM =
            compatFluid(REG, "Aluminium", () -> 0xBDB6B8);
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_BRASS =
            compatFluid(REG, "Brass", () -> 0xDDB878);
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_BRONZE =
            compatFluid(REG, "Bronze", () -> 0xC88556);
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_CAST_IRON =
            compatFluid(REG, "Cast Iron", () -> 0x414143);
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_ELECTRUM =
            compatFluid(REG, "Electrum", () -> 0xE1C382);
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_INVAR =
            compatFluid(REG, "Invar", () -> 0xAEB0C8);
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_LEAD =
            compatFluid(REG, "Lead", () -> 0x595468);
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_NICKEL =
            compatFluid(REG, "Nickel", () -> 0xD6B879);
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_OSMIUM =
            compatFluid(REG, "Osmium", () -> 0xA5B7C7);
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_PLATINUM =
            compatFluid(REG, "Platinum", () -> 0x9EC3D2);
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_ROSE_QUARTZ =
            compatFluid(REG, "Rose Quartz", () -> 0xF07A92, AllTags.ROSE_QUARTZ);
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_SILVER =
            compatFluid(REG, "Silver", () -> 0x97A2A9);
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_STEEL =
            compatFluid(REG, "Steel", () -> 0x6F6A6E);
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_TIN =
            compatFluid(REG, "Tin", () -> 0xB0CACD);
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_TUNGSTEN =
            compatFluid(REG, "Tungsten", () -> 0xB2B589);
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_ZINC =
            compatFluid(REG, "Zinc", () -> 0xA7BAA4);

    public static FluidBuilder<LavaLikeFluid.Flowing, Registration> newMoltenFluid(Registration reg, String type, Supplier<Integer> hexColor) {
        var name = "Molten " + type;
        String id = name.toLowerCase(Locale.ROOT).replace(" ", "_");
        return standardFluidLavaLike(reg, id, TintableFluidType.create(hexColor.get(), () -> 0.025f))
                .lang(name)
                .renderType(() -> RenderType::solid)
                .properties(b -> b
                        .lightLevel(12)
                        .viscosity(6000)
                        .density(1600)
                        .temperature(1000)
                        .canSwim(false)
                        .canDrown(false)
                        .pathType(PathType.LAVA)
                        .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
                        .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
                ).fluidProperties(p -> p.levelDecreasePerBlock(2)
                        .tickRate(10)
                        .slopeFindDistance(3)
                        .explosionResistance(100f))
                .tag(AllTags.fluidC(id), FluidTags.LAVA)
                .source(LavaLikeFluid.Source::new)
                .bucket()
                .tag(AllTags.itemC("buckets/" + id))
                .build()
                .block()
                .tag(AllTags.HEATED_FLUIDS)
                .build();
    }

    public static FluidEntry<LavaLikeFluid.Flowing> compatFluid(Registration reg, String type, Supplier<Integer> hexColor) {
        return compatFluid(reg, type, hexColor, AllTags.itemC("ingots/" + type.toLowerCase(Locale.ROOT).replace(" ", "_")));
    }

    public static FluidEntry<LavaLikeFluid.Flowing> compatFluid(Registration reg, String type, Supplier<Integer> hexColor, TagKey<Item> tag) {
        var entry = newMoltenFluid(reg, type, hexColor).register();
        COMPAT.put(entry, tag);
        return entry;
    }

    public static final String MOLTEN = "molten_";

    public static @Nullable TagKey<Fluid> commonTag(Fluid fluid) {
        var id = BuiltInRegistries.FLUID.getKey(fluid instanceof FlowingFluid flowing ? flowing.getSource() : fluid);
        if (!id.getNamespace().equals(SlagEmbers.MOD_ID) || !id.getPath().startsWith(MOLTEN)) return null;
        return AllTags.fluidC("molten/" + id.getPath().substring(MOLTEN.length()));
    }

    public static List<FluidEntry<LavaLikeFluid.Flowing>> updateHidden() {
        return HIDDEN = COMPAT.entrySet().stream().filter(e -> BuiltInRegistries.ITEM.getTag(e.getValue()).map(tag -> tag.size() == 0).orElse(true)).map(Map.Entry::getKey).toList();
    }

    public static void register() {}


    public static FluidBuilder<LavaLikeFluid.Flowing, Registration> standardFluidLavaLike(Registration reg, String name, FluidBuilder.FluidTypeFactory typeFactory) {
        return reg.fluid(name, SlagEmbers.loc("fluid/" + name + "_still"), SlagEmbers.loc("fluid/" + name + "_flow"), typeFactory, LavaLikeFluid.Flowing::new).tag(AllTags.HOT_FLUIDS);
    }

    public static FluidBuilder<BaseFlowingFluid.Flowing, Registration> standardFluid(Registration reg, String name, FluidBuilder.FluidTypeFactory typeFactory) {
        return reg.fluid(name, SlagEmbers.loc("fluid/" + name + "_still"), SlagEmbers.loc("fluid/" + name + "_flow"), typeFactory);
    }

    @ParametersAreNonnullByDefault
    public abstract static class LavaLikeFluid extends BaseFlowingFluid {
        protected LavaLikeFluid(Properties properties) {
            super(properties);
        }

        public void animateTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
            var above = pos.above();
            if (level.getBlockState(above).isAir() && !level.getBlockState(above).isSolidRender(level, above)) {
                if (random.nextInt(100) == 0) {
                    double d0 = (double)pos.getX() + random.nextDouble(), d1 = (double)pos.getY() + (double)1.0F, d2 = (double)pos.getZ() + random.nextDouble();
                    level.playLocalSound(d0, d1, d2, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
                }
                if (random.nextInt(200) == 0) level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.LAVA_AMBIENT, SoundSource.BLOCKS, 0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
            }
        }

        @SuppressWarnings("deprecation")
        public void randomTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
            if (level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
                int i = random.nextInt(3);
                if (i > 0) {
                    var relPos = pos;
                    for (int j = 0; j < i; ++j) {
                        relPos = relPos.offset(random.nextInt(3) - 1, 1, random.nextInt(3) - 1);
                        if (!level.isLoaded(relPos)) return;

                        var relState = level.getBlockState(relPos);
                        if (relState.isAir()) {
                            if (hasFlammableNeighbours(level, relPos)) {
                                level.setBlockAndUpdate(relPos, EventHooks.fireFluidPlaceBlockEvent(level, relPos, pos, BaseFireBlock.getState(level, relPos)));
                                return;
                            }
                        } else if (relState.blocksMotion()) return;
                    }
                } else for (int k = 0; k < 3; ++k) {
                    var offset = pos.offset(random.nextInt(3) - 1, 0, random.nextInt(3) - 1);
                    if (!level.isLoaded(offset)) return;
                    if (level.isEmptyBlock(offset.above()) && isFlammable(level, offset, Direction.UP)) level.setBlockAndUpdate(offset.above(), EventHooks.fireFluidPlaceBlockEvent(level, offset.above(), pos, BaseFireBlock.getState(level, offset)));
                }
            }
        }

        private boolean hasFlammableNeighbours(LevelReader level, BlockPos pos) {
            for (var dir : Direction.values()) if (isFlammable(level, pos.relative(dir), dir.getOpposite())) return true;
            return false;
        }

        @SuppressWarnings("deprecation")
        private boolean isFlammable(LevelReader pLevel, BlockPos pPos, Direction pFace) {
            if (pPos.getY() >= pLevel.getMinBuildHeight() && pPos.getY() < pLevel.getMaxBuildHeight() && !pLevel.hasChunkAt(pPos)) return false;
            var state = pLevel.getBlockState(pPos);
            return state.ignitedByLava() && state.isFlammable(pLevel, pPos, pFace);
        }

        protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
            this.fizz(level, pos);
        }

        private void fizz(LevelAccessor level, BlockPos pos) {
            level.levelEvent(1501, pos, 0);
        }

        @Override
        protected boolean isRandomlyTicking() {
            return true;
        }

        public static class Flowing extends LavaLikeFluid {
            protected Flowing(Properties properties) { super(properties); this.registerDefaultState(this.getStateDefinition().any().setValue(LEVEL, 7)); }
            @Override protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) { super.createFluidStateDefinition(builder); builder.add(LEVEL); }
            @Override public boolean isSource(FluidState fluidState) { return false; }
            @Override public int getAmount(FluidState fluidState) { return fluidState.getValue(LEVEL); }
        }
        public static class Source extends LavaLikeFluid {
            protected Source(Properties properties) { super(properties); }
            @Override public boolean isSource(FluidState fluidState) { return true; }
            @Override public int getAmount(FluidState fluidState) { return 8; }
        }
    }

    @ParametersAreNonnullByDefault
    public static class TintableFluidType extends AllFluids.TintedFluidType {
        private Vector3f fogColor;
        private Supplier<Float> fogDistance;

        public static FluidBuilder.FluidTypeFactory create(int fogColor, Supplier<Float> fogDistance) {
            return (p, s, f) -> {
                var fluidType = new TintableFluidType(p, s, f);
                fluidType.fogColor = new Color(fogColor, false).asVectorF();
                fluidType.fogDistance = fogDistance;
                return fluidType;
            };
        }

        private TintableFluidType(Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture) { super(properties, stillTexture, flowingTexture); }


        @Override protected int getTintColor(FluidStack stack) {
            return NO_TINT;
        }
        @Override public int getTintColor(FluidState state, BlockAndTintGetter world, BlockPos pos) {
            return 0x00ffffff;
        }
        @Override protected Vector3f getCustomFogColor() {
            return fogColor;
        }
        @Override protected float getFogDistanceModifier() {
            return fogDistance.get();
        }

    }


    @SuppressWarnings("removal")
    @ParametersAreNonnullByDefault
    public static abstract class TintedFluidType extends FluidType {

        protected static final int NO_TINT = 0xffffffff;
        private final ResourceLocation stillTexture;
        private final ResourceLocation flowingTexture;

        public TintedFluidType(Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
            super(properties);
            this.stillTexture = stillTexture;
            this.flowingTexture = flowingTexture;
        }

        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new IClientFluidTypeExtensions() {
                @Override public @Nonnull ResourceLocation getStillTexture() { return stillTexture; }
                @Override public @Nonnull ResourceLocation getFlowingTexture() { return flowingTexture; }
                @Override public int getTintColor(FluidStack stack) { return TintedFluidType.this.getTintColor(stack); }
                @Override public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) { return TintedFluidType.this.getTintColor(state, getter, pos); }

                @Override
                public @Nonnull Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
                    var customFogColor = TintedFluidType.this.getCustomFogColor();
                    return customFogColor == null ? fluidFogColor : customFogColor;
                }

                @Override
                public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape) {
                    float modifier = TintedFluidType.this.getFogDistanceModifier();
                    float baseWaterFog = 96.0f;
                    if (modifier != 1f) {
                        RenderSystem.setShaderFogShape(FogShape.CYLINDER);
                        RenderSystem.setShaderFogStart(-8);
                        RenderSystem.setShaderFogEnd(baseWaterFog * modifier);
                    }
                }
            });
        }
        protected abstract int getTintColor(FluidStack stack);
        protected abstract int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos);
        protected Vector3f getCustomFogColor() {
            return null;
        }
        protected float getFogDistanceModifier() {
            return 1f;
        }
    }
}
