package dev.lopyluna.slag.content.jei;

import dev.lopyluna.slag.content.items.dynamic_part.IDynamicPart;
import dev.lopyluna.slag.content.items.modular.ModularItem;
import dev.lopyluna.slag.content.jei.category.ModularCategory;
import dev.lopyluna.slag.content.types.ModularType;
import dev.lopyluna.slag.register.AllDynamicTypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.advanced.ISimpleRecipeManagerPlugin;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("NullableProblems")
@ParametersAreNonnullByDefault
public class ModularRecipes implements ISimpleRecipeManagerPlugin<ModularType> {
    private int version = -1;
    private Set<ModularType> valid = Set.of();

    @Override
    public boolean isHandledInput(ITypedIngredient<?> input) {
        return !getRecipesForInput(input).isEmpty();
    }

    @Override
    public boolean isHandledOutput(ITypedIngredient<?> output) {
        return !getRecipesForOutput(output).isEmpty();
    }

    @Override
    public List<ModularType> getRecipesForInput(ITypedIngredient<?> input) {
        var stack = stack(input);
        if (stack.isEmpty()) return List.of();
        var result = new ArrayList<ModularType>();
        var valid = valid();
        for (var modular : AllDynamicTypes.getAllModulars()) if (valid.contains(modular) && uses(modular, stack)) result.add(modular);
        return result;
    }

    @Override
    public List<ModularType> getRecipesForOutput(ITypedIngredient<?> output) {
        var stack = stack(output);
        if (!(stack.getItem() instanceof ModularItem item)) return List.of();
        var modular = item.getModularType(stack);
        return modular == null || !valid().contains(modular) ? List.of() : List.of(modular);
    }

    @Override
    public List<ModularType> getAllRecipes() {
        return List.of();
    }

    private Set<ModularType> valid() {
        var current = AllDynamicTypes.version;
        if (version == current) return valid;
        var result = new HashSet<ModularType>();
        for (var modular : AllDynamicTypes.getAllModulars()) if (!ModularCategory.results(modular).isEmpty()) result.add(modular);
        valid = Set.copyOf(result);
        version = current;
        return valid;
    }

    private static boolean uses(ModularType modular, ItemStack stack) {
        if (stack.getItem() instanceof IDynamicPart part) return modular.segments.contains(part.getPartSegment(stack));
        for (var segment : modular.finalSegmentStacks) if (ItemStack.isSameItem(segment, stack)) return true;
        for (var segment : modular.segments) if (stack.is(segment)) return true;
        return false;
    }

    private static ItemStack stack(ITypedIngredient<?> ingredient) {
        return ingredient.getItemStack().orElse(ItemStack.EMPTY);
    }
}
