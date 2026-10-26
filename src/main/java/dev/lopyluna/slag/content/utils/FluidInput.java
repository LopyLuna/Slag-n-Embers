package dev.lopyluna.slag.content.utils;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.lopyluna.slag.content.AllUtils;
import dev.lopyluna.slag.register.AllFluids;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import javax.annotation.Nullable;
import java.util.List;

public class FluidInput {
    public static final Codec<SizedFluidIngredient> CODEC = Codec.either(SizedFluidIngredient.CODEC, FluidStackTemplate.CODEC)
            .xmap(either -> either.map(input -> input, stack -> SizedFluidIngredient.of(stack.fluid().value(), stack.amount())), Either::left);
    public static final Codec<List<SizedFluidIngredient>> LIST_CODEC = CODEC.listOf();

    public static SizedFluidIngredient of(Fluid fluid, int amount) {
        var tag = AllFluids.commonTag(fluid);
        return tag == null ? SizedFluidIngredient.of(fluid, amount) : new SizedFluidIngredient(FluidIngredient.of(AllUtils.getter(BuiltInRegistries.FLUID).getOrThrow(tag)), amount);
    }

    public static SizedFluidIngredient of(FluidStack stack) {
        return of(stack.getFluid(), stack.getAmount());
    }

    public static boolean test(SizedFluidIngredient input, Fluid fluid) {
        return input.ingredient().test(new FluidStack(fluid, 1));
    }

    public static boolean narrower(SizedFluidIngredient input, @Nullable SizedFluidIngredient other) {
        return other == null || input.ingredient().fluids().size() < other.ingredient().fluids().size();
    }

    public static FluidStack first(SizedFluidIngredient input) {
        var fluids = input.ingredient().fluids();
        return fluids.isEmpty() ? FluidStack.EMPTY : new FluidStack(fluids.getFirst(), input.amount());
    }

    public static List<FluidStack> stacks(SizedFluidIngredient input) {
        return input.ingredient().fluids().stream().map(fluid -> new FluidStack(fluid, input.amount())).toList();
    }
}
