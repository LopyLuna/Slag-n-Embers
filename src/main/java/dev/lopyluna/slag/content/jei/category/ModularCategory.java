package dev.lopyluna.slag.content.jei.category;

import dev.lopyluna.slag.content.items.modular.DataDynamicParts;
import dev.lopyluna.slag.content.jei.EmbersRecipesJEI;
import dev.lopyluna.slag.content.traits.Traits;
import dev.lopyluna.slag.content.types.Incompatible;
import dev.lopyluna.slag.content.types.MaterialType;
import dev.lopyluna.slag.content.types.ModularType;
import dev.lopyluna.slag.content.types.PartType;
import dev.lopyluna.slag.register.AllDataComponents;
import dev.lopyluna.slag.register.AllDynamicTypes;
import dev.lopyluna.slag.register.AllItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@ParametersAreNonnullByDefault
public class ModularCategory extends AbstractRecipeCategory<ModularType> {
    private static final int COLS = 3, ROWS = 2, SLOT = 18, GAP = 4, SCROLLBAR = 16, ARROW_W = 22, ARROW_H = 16;
    private static final int WIDTH = 118, HEIGHT = 40;

    public ModularCategory(IGuiHelper guiHelper) {
        super(
                EmbersRecipesJEI.MODULAR,
                Component.translatableWithFallback("gui.slag.category.modular", "Modular Smithing"),
                guiHelper.createDrawableItemLike(Blocks.SMITHING_TABLE),
                WIDTH, HEIGHT);
    }

    public static List<MaterialType> materials(ModularType modular) {
        var parts = AllDynamicTypes.getAllPartsFromModular(modular);
        var result = new ArrayList<MaterialType>();
        if (parts.isEmpty()) return result;
        for (var material : AllDynamicTypes.getAllMaterials()) {
            if (!Incompatible.compatible(material, modular)) continue;
            if (!build(modular, parts, material).isEmpty()) result.add(material);
        }
        result.sort(Comparator.comparingInt(material -> material.sortOrder));
        return result;
    }

    public static List<ItemStack> results(ModularType modular) {
        var parts = AllDynamicTypes.getAllPartsFromModular(modular);
        var results = new ArrayList<ItemStack>();
        if (parts.isEmpty()) {
            var stack = build(modular, parts, null);
            if (!stack.isEmpty()) results.add(stack);
            return results;
        }
        for (var material : materials(modular)) results.add(build(modular, parts, material));
        return results;
    }

    private static ItemStack build(ModularType modular, List<PartType> parts, @Nullable MaterialType material) {
        var stacks = new ArrayList<ItemStack>();
        for (var part : parts) {
            if (material == null || !Incompatible.compatible(material, part)) return ItemStack.EMPTY;
            stacks.add(partStack(material, part, modular));
        }
        for (var stack : modular.finalSegmentStacks) {
            var copy = stack.copy();
            copy.set(AllDataComponents.BUILT, modular.id);
            stacks.add(copy);
        }
        if (!Incompatible.compatible(stacks, modular)) return ItemStack.EMPTY;
        var result = modular.getResultStack();
        if (!result.isEmpty()) return result;
        var tool = AllItems.MODULAR_ITEM.asStack();
        tool.set(AllDataComponents.DYNAMIC_PARTS, new DataDynamicParts(stacks));
        tool.set(AllDataComponents.MODULAR_TYPE, modular.id);
        Traits.of(tool).applyComponents(tool);
        return tool;
    }

    private static ItemStack partStack(MaterialType material, PartType part, ModularType modular) {
        var item = AllItems.DYNAMIC_PART.get();
        var stack = item.getDefaultInstance();
        item.setMaterialType(stack, material);
        item.setPartType(stack, part);
        stack.set(AllDataComponents.BUILT, modular.id);
        return stack;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ModularType modular, IFocusGroup focuses) {
        var inputs = new ArrayList<List<ItemStack>>();
        var materials = materials(modular);
        for (var part : AllDynamicTypes.getAllPartsFromModular(modular)) {
            var stacks = new ArrayList<ItemStack>();
            for (var material : materials) {
                var stack = partStack(material, part, modular);
                stack.remove(AllDataComponents.BUILT);
                stacks.add(stack);
            }
            inputs.add(stacks);
        }
        for (var stack : modular.finalSegmentStacks) inputs.add(List.of(stack.copy()));

        var count = inputs.size();
        var scrolls = scrolls(count);
        var cols = columns(count);
        for (var i = 0; i < count; i++) {
            var slot = builder.addInputSlot();
            if (!scrolls) slot.setPosition(originX(count) + i % cols * SLOT + 1, originY(count) + i / cols * SLOT + 1).setStandardSlotBackground();
            slot.addItemStacks(inputs.get(i));
        }

        builder.addOutputSlot(outputX(count), (HEIGHT - SLOT) / 2 + 1).setStandardSlotBackground().addItemStacks(results(modular));
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, ModularType modular, IFocusGroup focuses) {
        var inputs = builder.getRecipeSlots().getSlots(RecipeIngredientRole.INPUT);
        var count = inputs.size();
        if (scrolls(count)) builder.addScrollGridWidget(inputs, columns(count), ROWS).setPosition(originX(count), originY(count));
        builder.addRecipeArrow().setPosition(arrowX(count), (HEIGHT - ARROW_H) / 2);
    }

    private static int columns(int count) {
        return Math.clamp(count, 1, COLS);
    }

    private static int rows(int count) {
        return (count + columns(count) - 1) / columns(count);
    }

    private static boolean scrolls(int count) {
        return rows(count) > ROWS;
    }

    private static int gridWidth(int count) {
        if (count == 0) return 0;
        return columns(count) * SLOT + (scrolls(count) ? SCROLLBAR : 0);
    }

    private static int originX(int count) {
        return (WIDTH - (gridWidth(count) + GAP + ARROW_W + GAP + SLOT)) / 2;
    }

    private static int originY(int count) {
        return (HEIGHT - Math.min(rows(count), ROWS) * SLOT) / 2;
    }

    private static int arrowX(int count) {
        return originX(count) + gridWidth(count) + (count == 0 ? 0 : GAP);
    }

    private static int outputX(int count) {
        return arrowX(count) + ARROW_W + GAP + 1;
    }
}
