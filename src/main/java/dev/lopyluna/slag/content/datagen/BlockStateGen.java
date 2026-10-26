package dev.lopyluna.slag.content.datagen;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

@SuppressWarnings("unused")
public class BlockStateGen {
    public static VariantMutator genDirectional(Direction dir) {
        return switch (dir) {
            case DOWN -> BlockModelGenerators.X_ROT_90;
            case UP -> BlockModelGenerators.X_ROT_270;
            case NORTH -> BlockModelGenerators.NOP;
            case SOUTH -> BlockModelGenerators.Y_ROT_180;
            case WEST -> BlockModelGenerators.Y_ROT_270;
            case EAST -> BlockModelGenerators.Y_ROT_90;
        };
    }

    public static VariantMutator genHorizontalDirectional(Direction dir) {
        return switch (dir) {
            case SOUTH -> BlockModelGenerators.Y_ROT_180;
            case WEST -> BlockModelGenerators.Y_ROT_270;
            case EAST -> BlockModelGenerators.Y_ROT_90;
            default -> BlockModelGenerators.NOP;
        };
    }

    public static <T extends Block> NonNullSupplier<NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockModelGenerator>> genHorizontalDirectional() {
        return () -> BlockStateGen::genHorizontalDirectional;
    }

    public static void genHorizontalDirectional(DataGenContext<Block, ? extends Block> c, RegistrateBlockModelGenerator p) {
        var block = c.get();
        var state = block.defaultBlockState();
        var model = BlockModelGenerators.plainVariant(p.modLoc("block/" + c.getName()));
        var prop = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) ? BlockStateProperties.HORIZONTAL_FACING :
                state.hasProperty(BlockStateProperties.FACING) ? BlockStateProperties.FACING :
                state.hasProperty(BlockStateProperties.FACING_HOPPER) ? BlockStateProperties.FACING_HOPPER : null;
        if (prop == null) p.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, model));
        else p.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, model).with(PropertyDispatch.modify(prop).generate(BlockStateGen::genHorizontalDirectional)));
    }

    public static Identifier getExistingModel(DataGenContext<Block, ? extends Block> ctx, RegistrateBlockModelGenerator prov, String... suffix) {
        StringBuilder string = new StringBuilder("/block");
        for (var suf : suffix) if (!suf.isEmpty()) string.append("_").append(suf);
        final String location = "block/" + ctx.getName() + string;
        return prov.modLoc(location.contains("empty") ? "block/" + ctx.getName() + "/empty" : location);
        //to generate all the models with correct naming
        //return prov.models().withExistingParent(prov.modLoc(location.contains("empty") ? "block/" + ctx.getName() + "/empty" : location).toString(), prov.modLoc("block/" + ctx.getName() + "/empty"));
    }
}
