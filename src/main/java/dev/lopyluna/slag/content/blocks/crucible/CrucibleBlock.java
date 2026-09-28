package dev.lopyluna.slag.content.blocks.crucible;

import com.mojang.serialization.MapCodec;
import dev.lopyluna.slag.compat.sable.SableCompat;
import dev.lopyluna.slag.config.SlagServerConfigs;
import dev.lopyluna.slag.content.blocks.multiblock.ConnectivityHandler;
import dev.lopyluna.slag.content.blocks.smart.SmartBlock;
import dev.lopyluna.slag.content.utils.ShapeUtils;
import dev.lopyluna.slag.register.AllBETypes;
import dev.lopyluna.slag.register.AllLangs;
import dev.lopyluna.slag.register.AllSoundTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.EffectCures;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import javax.annotation.Nonnull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static dev.lopyluna.slag.content.blocks.crucible.CrucibleBE.*;

@ParametersAreNonnullByDefault
public class CrucibleBlock extends SmartBlock<CrucibleBE> {
    public static final MapCodec<CrucibleBlock> CODEC = simpleCodec(CrucibleBlock::new);

    public CrucibleBlock(Properties properties) {
        super(properties.forceSolidOn().lightLevel(state -> state.getValue(LIGHT)));
        registerDefaultState(super.defaultBlockState().setValue(WINDOW, false).setValue(TOP, true).setValue(BOTTOM, true).setValue(SHAPE, Shape.PLAIN).setValue(LIGHT, 0));
    }

    @Override
    protected boolean canBeReplaced(BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> pTooltips, TooltipFlag pFlag) {
        super.appendHoverText(pStack, pContext, pTooltips, pFlag);
        pTooltips.add(AllLangs.tr("dynamic_multiblock").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public Class<CrucibleBE> getBlockEntityClass() {
        return CrucibleBE.class;
    }

    @Override
    public @Nonnull BlockEntityType<CrucibleBE> getBlockEntityType() {
        return AllBETypes.CRUCIBLE.get();
    }
    @Override
    protected @Nonnull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (!(entity instanceof LivingEntity living) || !(level.getBlockEntity(pos) instanceof CrucibleBE part) || !(part.getControllerBE() instanceof CrucibleBE be)) return;
        if (be.updateFluidBoxes) be.updateFluidBoxes();

        var bounds = entity.getBoundingBox();
        var points = SableCompat.localPoints(level, pos, bounds);
        var hot = false;
        var milk = false;
        var extinguish = false;
        for (var i = 0; i < be.fluidBoxes.size(); i++) {
            var box = be.fluidBoxes.get(i);
            if (points == null ? !box.intersects(bounds) : points.stream().noneMatch(box::contains)) continue;
            var fluid = be.fluidBoxStacks.get(i);
            var type = fluid.getFluidType();
            hot |= type.getTemperature() >= 1000;
            milk |= fluid.is(NeoForgeMod.MILK);
            extinguish |= type.canExtinguish(entity);
        }

        applyFluid(level, living, hot, milk, extinguish);
    }

    public static void applyFluid(Level level, LivingEntity living, boolean hot, boolean milk, boolean extinguish) {
        if (milk && !living.getActiveEffects().isEmpty()) living.removeEffectsCuredBy(EffectCures.MILK);
        if (hot) living.lavaHurt();
        else if (extinguish && living.isOnFire()) {
            living.extinguishFire();
            living.playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, 0.7F, 1.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.4F);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (level.getBlockEntity(pos) instanceof CrucibleBE cbe && cbe.getControllerBE() instanceof CrucibleBE be) {
            var fluids = be.tankInventory.getFluids();
            if (fluids == null || fluids.isEmpty()) return;
            var totalSize = be.getTotalSize();
            var fillState = be.getFillState();
            for (var fluid : fluids) if (random.nextInt(fluids.size() * totalSize) <= totalSize * 0.25f * fillState) {
                if ((fluid.is(Fluids.WATER) || fluid.is(NeoForgeMod.MILK)) && random.nextInt(96) == 0) level.playLocalSound((double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F, SoundEvents.WATER_AMBIENT, SoundSource.BLOCKS, random.nextFloat() * 0.25F + 0.75F, random.nextFloat() + 0.5F, false);
                fluid.getFluid().defaultFluidState().animateTick(level, pos, random);
            }
        }
    }

    @Override
    public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        return !state.getValue(BOTTOM) && !entity.isDescending();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WINDOW, TOP, BOTTOM, SHAPE, LIGHT);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        if (oldState.getBlock() == state.getBlock() || moved) return;
        withBlockEntityDo(level, pos, be -> {
            if (be.placed) {
                be.placed = false;
                ConnectivityHandler.formMulti(be);
            } else if (be.isController && be.getTotalSize() == 1 && !be.held) be.updateConnectivity = true;
        });
    }

    @Override
    public <S extends BlockEntity> BlockEntityTicker<S> getTicker(Level level, BlockState state, BlockEntityType<S> type) {
        return ticks(state) ? super.getTicker(level, state, type) : null;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return !ticks(state);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof CrucibleBE be)) return;
        if (!be.isController && be.notIntact()) be.removeController();
        else be.notifyMultiUpdated();
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!(level.getBlockEntity(pos) instanceof CrucibleBE be) || !(be.getControllerBE() instanceof CrucibleBE ctrl)) return;
        if (neighborPos.getY() >= pos.getY() || !state.getValue(BOTTOM)) return;
        ctrl.updateHeat = true;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.hasBlockEntity() && (state.getBlock() != newState.getBlock() || !newState.hasBlockEntity())) {
            if (!(level.getBlockEntity(pos) instanceof CrucibleBE tankBE)) return;
            level.removeBlockEntity(pos);
            ConnectivityHandler.splitMulti(tankBE);
        }
    }

    @Override
    protected @Nonnull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.getMainHandItem().isEmpty() && player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof CrucibleBE be) {
            var ctrl = be.getControllerBE();
            if (ctrl == null) ctrl = be;
            ctrl.setWindows(!ctrl.window);
            return InteractionResult.SUCCESS_NO_ITEM_USED;
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    protected @Nonnull ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!SlagServerConfigs.INSERT_FLUID_ITEM_INTO_CRUCIBLE.get()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!(level.getBlockEntity(pos) instanceof CrucibleBE be)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        var ctrl = be.getControllerBE();
        if (ctrl == null) ctrl = be;
        var tank = ctrl.tankInventory;

        var single = held.copyWithCount(1);
        var itemHandler = single.getCapability(Capabilities.FluidHandler.ITEM);
        if (itemHandler == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        FluidStack available = itemHandler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
        if (available.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        int fillable = tank.fill(available, IFluidHandler.FluidAction.SIMULATE);
        if (fillable != available.getAmount()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (!level.isClientSide) {
            FluidStack drained = itemHandler.drain(fillable, IFluidHandler.FluidAction.EXECUTE);
            if (!drained.isEmpty()) {
                int accepted = tank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                if (accepted > 0 && !player.getAbilities().instabuild) {
                    var container = itemHandler.getContainer();
                    held.shrink(1);
                    if (held.isEmpty()) player.setItemInHand(hand, container);
                    else if (!container.isEmpty() && !player.getInventory().add(container)) player.drop(container, false);
                }

                ctrl.sendDataImmediately();
                ctrl.setChanged();
            }
        }
        var soundFill = getFillSound(available);
        var soundEmpty = getEmptySound(available);
        if (soundFill != null) level.playSound(null, pos, soundFill, SoundSource.BLOCKS, .5f, 1);
        if (soundEmpty != null) player.playSound(soundEmpty, .5f, 1);
        return ItemInteractionResult.SUCCESS;
    }


    public static SoundEvent getFillSound(FluidStack fluid) {
        SoundEvent soundevent = fluid.getFluid().getFluidType().getSound(fluid, SoundActions.BUCKET_FILL);
        if (soundevent == null) soundevent = isTag(fluid, FluidTags.LAVA) ? SoundEvents.BUCKET_FILL_LAVA : SoundEvents.BUCKET_FILL;
        return soundevent;
    }
    public static SoundEvent getEmptySound(FluidStack fluid) {
        SoundEvent soundevent = fluid.getFluid().getFluidType().getSound(fluid, SoundActions.BUCKET_EMPTY);
        if (soundevent == null) soundevent = isTag(fluid, FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        return soundevent;
    }
    @SuppressWarnings("deprecation")
    public static boolean isTag(Fluid fluid, TagKey<Fluid> tag) {
        return fluid.is(tag);
    }
    public static boolean isTag(FluidStack fluid, TagKey<Fluid> tag) {
        return isTag(fluid.getFluid(), tag);
    }

    @Override
    protected @Nonnull VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (context.equals(CollisionContext.empty())) return Shapes.block();
        return state.getValue(WINDOW) ? Shapes.empty() : super.getVisualShape(state, level, pos, context);
    }

    private static final VoxelShape[] SHAPES = createShapes();

    private static VoxelShape[] createShapes() {
        var shapes = new VoxelShape[Shape.values().length * 2];
        var wall = ShapeUtils.shape(0, 0, 0, 16, 16, 3).forHorizontal(Direction.NORTH);

        for (var shape : Shape.values()) for (var bottom = 0; bottom < 2; bottom++) {
            var shaper = ShapeUtils.shape(Shapes.empty());
            if (bottom == 1) shaper.add(0, 0, 0, 16, 4, 16);

            switch (shape) {
                case PLAIN -> shaper.add(wall.get(Direction.NORTH)).add(wall.get(Direction.SOUTH)).add(wall.get(Direction.EAST)).add(wall.get(Direction.WEST));
                case NW -> shaper.add(wall.get(Direction.NORTH)).add(wall.get(Direction.WEST));
                case SW -> shaper.add(wall.get(Direction.SOUTH)).add(wall.get(Direction.WEST));
                case NE -> shaper.add(wall.get(Direction.NORTH)).add(wall.get(Direction.EAST));
                case SE -> shaper.add(wall.get(Direction.SOUTH)).add(wall.get(Direction.EAST));
                case NORTH -> shaper.add(wall.get(Direction.NORTH));
                case SOUTH -> shaper.add(wall.get(Direction.SOUTH));
                case WEST -> shaper.add(wall.get(Direction.WEST));
                case EAST -> shaper.add(wall.get(Direction.EAST));
                default -> {}
            }
            shapes[shape.ordinal() * 2 + bottom] = shaper.build();
        }
        return shapes;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return !state.getValue(WINDOW);
    }

    @Override
    protected @Nonnull VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(WINDOW) ? Shapes.empty() : SHAPES[state.getValue(SHAPE).ordinal() * 2 + (state.getValue(BOTTOM) ? 1 : 0)];
    }

    @Override
    protected @Nonnull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(SHAPE).ordinal() * 2 + (state.getValue(BOTTOM) ? 1 : 0)];
    }

    @Override
    protected @Nonnull VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    public static final SoundType SILENCED_SOUND = new DeferredSoundType(0.1F, 1.5F,
            AllSoundTypes.CRUCIBLE::getBreakSound,
            AllSoundTypes.CRUCIBLE::getStepSound,
            AllSoundTypes.CRUCIBLE::getPlaceSound,
            AllSoundTypes.CRUCIBLE::getHitSound,
            AllSoundTypes.CRUCIBLE::getFallSound
    );

    @Override
    public @Nonnull SoundType getSoundType(BlockState state, LevelReader world, BlockPos pos, @Nullable Entity entity) {
        var soundType = super.getSoundType(state, world, pos, entity);
        if (entity instanceof Player && entity.getPersistentData().contains("SilencePlacingSound")) return SILENCED_SOUND;
        return soundType;
    }
}
