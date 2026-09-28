package dev.lopyluna.slag.content.blocks.basin;

import dev.lopyluna.slag.content.blocks.casting.CastingBE;
import dev.lopyluna.slag.content.blocks.casting.CastingInventory;
import dev.lopyluna.slag.content.utils.FluidInput;
import dev.lopyluna.slag.register.AllRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.Nullable;
import java.util.*;

public class BasinBE extends CastingBE {
    public static final float COOLING_RATE = 0.5f;

    private static final Map<Fluid, BasinCastingRecipe> handlers = new IdentityHashMap<>();
    private static final List<BasinCastingRecipe> itemHandlers = new ArrayList<>();
    private static Collection<RecipeHolder<?>> handlerSource;

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
        refreshHandlers();
        var placed = itemInventory.getItem(RESULT);
        if (!placed.isEmpty()) {
            for (var recipe : itemHandlers) {
                var castItem = recipe.getCastItem();
                if (castItem == null || !FluidInput.test(recipe.getInput(), fluid) || !castItem.matches(placed)) continue;
                return handler(recipe.getInput().amount(), recipe.getDuration(), recipe.getSpeed(), recipe.getResult(), castItem);
            }
            return null;
        }
        var recipe = handlers.get(fluid);
        if (recipe == null || recipe.getOutput().isEmpty()) return null;
        return handler(recipe.getInput().amount(), recipe.getDuration(), recipe.getSpeed(), recipe.getResult(), null);
    }

    @Override
    public boolean isCastItem(ItemStack stack) {
        if (level == null || stack.isEmpty()) return false;
        refreshHandlers();
        for (var recipe : itemHandlers) {
            var castItem = recipe.getCastItem();
            if (castItem != null && castItem.matches(stack)) return true;
        }
        return false;
    }

    private void refreshHandlers() {
        if (level == null) return;
        var manager = level.getRecipeManager();
        if (manager.getRecipes() == handlerSource) return;
        handlerSource = manager.getRecipes();
        handlers.clear();
        itemHandlers.clear();
        for (var holder : manager.getAllRecipesFor(AllRecipes.BASIN_CASTING.get())) {
            var recipe = holder.value();
            if (recipe.getCastItem() != null) itemHandlers.add(recipe);
            else for (var fluid : recipe.getInput().getFluids()) {
                var known = handlers.get(fluid.getFluid());
                if (FluidInput.narrower(recipe.getInput(), known == null ? null : known.getInput())) handlers.put(fluid.getFluid(), recipe);
            }
        }
    }
}
