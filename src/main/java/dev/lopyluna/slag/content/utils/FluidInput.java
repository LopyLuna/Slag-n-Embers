package dev.lopyluna.slag.content.utils;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.lopyluna.slag.register.AllFluids;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import javax.annotation.Nullable;
import java.util.List;

public class FluidInput {
    public static final Codec<SizedFluidIngredient> CODEC = Codec.either(SizedFluidIngredient.FLAT_CODEC, FluidStack.CODEC)
            .xmap(either -> either.map(input -> input, SizedFluidIngredient::of), Either::left);
    public static final Codec<List<SizedFluidIngredient>> LIST_CODEC = CODEC.listOf();

    public static SizedFluidIngredient of(Fluid fluid, int amount) {
        var tag = AllFluids.commonTag(fluid);
        return tag == null ? SizedFluidIngredient.of(fluid, amount) : SizedFluidIngredient.of(tag, amount);
    }

    public static SizedFluidIngredient of(FluidStack stack) {
        return of(stack.getFluid(), stack.getAmount());
    }

    public static boolean test(SizedFluidIngredient input, Fluid fluid) {
        return input.ingredient().test(new FluidStack(fluid, 1));
    }

    public static boolean narrower(SizedFluidIngredient input, @Nullable SizedFluidIngredient other) {
        return other == null || input.getFluids().length < other.getFluids().length;
    }

    public static FluidStack first(SizedFluidIngredient input) {
        var fluids = input.getFluids();
        return fluids.length == 0 ? FluidStack.EMPTY : fluids[0];
    }
}
