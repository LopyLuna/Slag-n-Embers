package dev.lopyluna.slag.content.jei.category;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.blocks.basin.BasinBE;
import dev.lopyluna.slag.content.blocks.basin.BasinCastingRecipe;
import dev.lopyluna.slag.content.blocks.casting.CastingBE;
import dev.lopyluna.slag.content.jei.EmbersRecipesJEI;
import dev.lopyluna.slag.content.utils.FluidInput;
import dev.lopyluna.slag.register.AllBlocks;
import dev.lopyluna.slag.register.AllDataComponents;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.common.Internal;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeHolder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

import static dev.lopyluna.slag.content.blocks.crucible_interface.client.InterfaceScreen.createLang;

@ParametersAreNonnullByDefault
public class BasinCastingCategory extends AbstractRecipeCategory<RecipeHolder<BasinCastingRecipe>> {
    private final IDrawable tankBackground;
    private final IDrawable tankOverlay;

    public BasinCastingCategory(IGuiHelper guiHelper) {
        super(
                EmbersRecipesJEI.BASIN_CASTING.get(),
                Component.translatableWithFallback("gui.slag.category.basin_casting", "Basin Casting"),
                guiHelper.createDrawableItemLike(AllBlocks.BASIN),
                123, 54);

        ResourceLocation backgroundTexture = SlagEmbers.loc("textures/gui/jei.png");
        this.tankBackground = guiHelper.createDrawable(backgroundTexture, 0, 0, 32, 56);
        this.tankOverlay = guiHelper.createDrawable(backgroundTexture, 32, 0, 32, 56);
    }


    @SuppressWarnings("removal")
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<BasinCastingRecipe> holder, IFocusGroup focuses) {
        var recipe = holder.value();

        builder.addOutputSlot(86, 1)
                .setStandardSlotBackground()
                .addItemStack(recipe.getOutput());

        var item = recipe.getCastItem();
        var basinSlot = builder.addSlot(item == null ? RecipeIngredientRole.RENDER_ONLY : RecipeIngredientRole.INPUT, 86, 38)
                .setStandardSlotBackground();
        if (item == null) basinSlot.addItemStack(AllBlocks.BASIN.asStack());
        else {
            var type = item.imprint() ? recipe.getOutput().get(AllDataComponents.CAST_TYPE) : null;
            var stacks = type == null ? List.<ItemStack>of() : TableCastingCategory.castStacks(type, item.input());
            if (stacks.isEmpty()) basinSlot.addIngredients(item.input());
            else basinSlot.addItemStacks(stacks);
            basinSlot.addRichTooltipCallback((s, t) -> t.add(item.describe().copy().withStyle(ChatFormatting.GRAY)));
        }

        var input = recipe.getInput();

        builder.addInputSlot(12, 3)
                .setFluidRenderer(1000, false, 24, 48)
                .setOverlay(tankOverlay, -4, -4)
                .setBackground(tankBackground, -4, -4)
                .addIngredients(NeoForgeTypes.FLUID_STACK, List.of(input.getFluids()))
                .addRichTooltipCallback((s, t) -> {
                    var fluid = s.getDisplayedIngredient(NeoForgeTypes.FLUID_STACK).orElseGet(() -> FluidInput.first(input));
                    var tooltipFlag = Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
                    t.clear();
                    var tooltips = new ArrayList<Component>();
                    tooltips.add(fluid.getDisplayName());
                    createLang(fluid, tooltips).run();
                    if (tooltipFlag.advanced()) {
                        var loc = BuiltInRegistries.FLUID.getKey(fluid.getFluid());
                        tooltips.add(Component.literal(loc.toString()).withStyle(ChatFormatting.DARK_GRAY));
                        var helper = Internal.getJeiRuntime().getJeiHelpers().getModIdHelper();
                        tooltips.add(Component.literal(getFormattedModNameForModIdWithoutDisplay(helper, loc.getNamespace())).withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.ITALIC));
                        var name = getRegistryName(holder);
                        if (name != null) {
                            tooltips.add(Component.translatable("jei.tooltip.recipe.id", Component.literal(name.toString())).withStyle(ChatFormatting.DARK_GRAY));

                            var modID = name.getNamespace();
                            if (!modID.equals(getRecipeType().getUid().getNamespace())) {
                                var mod = getFormattedModNameForModId(helper, name.getNamespace());
                                if (!mod.isEmpty()) tooltips.add(Component.translatable("jei.tooltip.recipe.by", mod).withStyle(ChatFormatting.GRAY));
                            }
                        }
                    }
                    t.addAll(tooltips);
                })
        ;
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<BasinCastingRecipe> holder, IFocusGroup focuses) {
        var recipe = holder.value();
        builder.addAnimatedRecipeArrow(CastingBE.castingTicks(recipe.getInput().amount(), BasinBE.COOLING_RATE, recipe.getDuration(), recipe.getSpeed())).setPosition(49, 17);
    }

    public String getFormattedModNameForModIdWithoutDisplay(IModIdHelper helper, String modId) {
        return helper.getFormattedModNameForModId(modId);
    }

    public String getFormattedModNameForModId(IModIdHelper helper, String modId) {
        if (!helper.isDisplayingModNameEnabled()) return "";
        return helper.getFormattedModNameForModId(modId);
    }
}
