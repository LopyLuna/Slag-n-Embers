package dev.lopyluna.slag.content.jei.category;

import com.mojang.serialization.Codec;
import dev.lopyluna.slag.content.blocks.forge.DoubleSmeltingRecipe;
import dev.lopyluna.slag.content.jei.EmbersRecipesJEI;
import dev.lopyluna.slag.register.AllBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.library.util.RecipeUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

public class DoubleSmeltingCategory extends AbstractRecipeCategory<RecipeHolder<DoubleSmeltingRecipe>> {
    public DoubleSmeltingCategory(IGuiHelper guiHelper) {
        super(
            EmbersRecipesJEI.DOUBLE_SMELTING.get(),
            Component.translatableWithFallback(
                "gui.slag.category.double_smelting",
                "Double Smelting"
            ),
            guiHelper.createDrawableItemLike(AllBlocks.FORGE),
            82,
            54
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<DoubleSmeltingRecipe> holder, @NotNull IFocusGroup group) {
        var recipe = holder.value();

        builder.addInputSlot(1, 1)
                .setStandardSlotBackground()
                .addIngredients(recipe.getInputA());

        builder.addInputSlot(20, 1)
                .setStandardSlotBackground()
                .addIngredients(recipe.getInputB());

        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 1, 37).setStandardSlotBackground();

        builder.addOutputSlot(61, 19)
                .setOutputSlotBackground()
                .addItemStack(RecipeUtil.getResultItem(recipe));
    }

    @Override
    public void createRecipeExtras(@NotNull IRecipeExtrasBuilder builder, RecipeHolder<DoubleSmeltingRecipe> holder, @NotNull IFocusGroup group) {
        var recipe = holder.value();
        int cookTime = recipe.getCookingTime();
        if (cookTime <= 0) cookTime = 200;

        builder.addAnimatedRecipeArrow(cookTime).setPosition(26, 17);
        builder.addAnimatedRecipeFlame(300).setPosition(1, 20);

        addExperience(builder, holder);
        addCookTime(builder, holder);
    }

    protected void addExperience(IRecipeExtrasBuilder builder, RecipeHolder<DoubleSmeltingRecipe> holder) {
        var recipe = holder.value();
        float experience = recipe.getExperience();
        if (experience > 0) builder.addText(Component.translatable("gui.jei.category.smelting.experience", experience), getWidth() - 20, 10)
                .setPosition(0, 0, getWidth(), getHeight(), HorizontalAlignment.RIGHT, VerticalAlignment.TOP)
                .setTextAlignment(HorizontalAlignment.RIGHT)
                .setColor(0xFF808080);
    }

    protected void addCookTime(IRecipeExtrasBuilder builder, RecipeHolder<DoubleSmeltingRecipe> holder) {
        var recipe = holder.value();
        int cookTime = recipe.getCookingTime();
        if (cookTime <= 0) cookTime = 200;
        builder.addText(Component.translatable("gui.jei.category.smelting.time.seconds", cookTime / 20), getWidth() - 20, 10)
                .setPosition(0, 0, getWidth(), getHeight(), HorizontalAlignment.RIGHT, VerticalAlignment.BOTTOM)
                .setTextAlignment(HorizontalAlignment.RIGHT)
                .setTextAlignment(VerticalAlignment.BOTTOM)
                .setColor(0xFF808080);
    }

    @Override
    public boolean isHandled(RecipeHolder<DoubleSmeltingRecipe> holder) {
        var recipe = holder.value();
        if (recipe.isSpecial()) return false;
        if (recipe.getInputA().hasNoItems()) return false;
        return !recipe.getInputB().hasNoItems();
    }
    @Override
    public ResourceLocation getRegistryName(RecipeHolder<DoubleSmeltingRecipe> recipe) {
        return recipe.id();
    }
    @Override
    public @NotNull Codec<RecipeHolder<DoubleSmeltingRecipe>> getCodec(ICodecHelper helper, @NotNull IRecipeManager manager) {
        return helper.getRecipeHolderCodec();
    }
}
