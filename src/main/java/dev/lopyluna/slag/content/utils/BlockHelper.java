package dev.lopyluna.slag.content.utils;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.function.ToIntFunction;

@SuppressWarnings("unused")
public class BlockHelper {
    public static Boolean always(BlockState state, BlockGetter blockGetter, BlockPos pos, EntityType<?> entity) {
        return true;
    }
    private static boolean always(BlockState state, BlockGetter blockGetter, BlockPos pos) {
        return true;
    }

    public static Boolean never(BlockState state, BlockGetter blockGetter, BlockPos pos, EntityType<?> entity) {
        return false;
    }
    private static boolean never(BlockState state, BlockGetter blockGetter, BlockPos pos) {
        return false;
    }

    public static <T extends Block> NonNullSupplier<NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockModelGenerator>> empty() {
        return () -> (ctx, p) -> {};
    }

    public static ToIntFunction<BlockState> litBlockEmission(int i) {
        return (blockState) -> (Boolean) blockState.getValue(BlockStateProperties.LIT) ? i : 0;
    }
}
