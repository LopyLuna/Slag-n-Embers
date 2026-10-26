package dev.lopyluna.slag.content.blocks.basin;

import dev.lopyluna.slag.content.AllUtils;
import dev.lopyluna.slag.content.blocks.casting.CastingBE;
import dev.lopyluna.slag.content.blocks.casting.CastingInventory;
import dev.lopyluna.slag.content.utils.FluidInput;
import dev.lopyluna.slag.register.AllRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.Nullable;
import java.util.*;

public class BasinBE extends CastingBE {
    public static final float COOLING_RATE = 0.5f;

    private static final Handlers SERVER = new Handlers(), CLIENT = new Handlers();

    private static final class Handlers {
        private final Map<Fluid, BasinCastingRecipe> fluids = new IdentityHashMap<>();
        private final List<BasinCastingRecipe> items = new ArrayList<>();
        private @Nullable RecipeMap source;
    }

    public BasinBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 1000);
        coolingRate = COOLING_RATE;
        maxLight = 12;
    }

    @Override
    protected CastingInventory createInventory() {
        return new BasinInventory(this);
    }

    @Override
    protected @Nullable Handler findHandler(Fluid fluid) {
        if (level == null) return null;
        var cache = handlers();
        var placed = itemInventory.getItem(RESULT);
        if (!placed.isEmpty()) {
            for (var recipe : cache.items) {
                var castItem = recipe.getCastItem();
                if (castItem == null || !FluidInput.test(recipe.getInput(), fluid) || !castItem.matches(placed)) continue;
                return handler(recipe.getInput().amount(), recipe.getDuration(), recipe.getSpeed(), recipe.getResult(), castItem);
            }
            return null;
        }
        var recipe = cache.fluids.get(fluid);
        if (recipe == null || recipe.getOutput().isEmpty()) return null;
        return handler(recipe.getInput().amount(), recipe.getDuration(), recipe.getSpeed(), recipe.getResult(), null);
    }

    @Override
    public boolean isCastItem(ItemStack stack) {
        if (level == null || stack.isEmpty()) return false;
        for (var recipe : handlers().items) {
            var castItem = recipe.getCastItem();
            if (castItem != null && castItem.matches(stack)) return true;
        }
        return false;
    }

    private Handlers handlers() {
        var cache = level != null && level.isClientSide() ? CLIENT : SERVER;
        var map = AllUtils.recipeMap(level);
        if (map == cache.source) return cache;
        cache.source = map;
        cache.fluids.clear();
        cache.items.clear();
        for (var holder : map.byType(AllRecipes.BASIN_CASTING.get())) {
            var recipe = holder.value();
            if (recipe.getCastItem() != null) cache.items.add(recipe);
            else for (var fluid : FluidInput.stacks(recipe.getInput())) {
                var known = cache.fluids.get(fluid.getFluid());
                if (FluidInput.narrower(recipe.getInput(), known == null ? null : known.getInput())) cache.fluids.put(fluid.getFluid(), recipe);
            }
        }
        return cache;
    }
}
