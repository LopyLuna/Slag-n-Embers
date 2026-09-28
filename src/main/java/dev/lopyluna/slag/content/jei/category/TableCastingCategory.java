package dev.lopyluna.slag.content.jei.category;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.blocks.casting.CastingBE;
import dev.lopyluna.slag.content.blocks.table.TableBE;
import dev.lopyluna.slag.content.blocks.table.TableCastingRecipe;
import dev.lopyluna.slag.content.jei.EmbersRecipesJEI;
import dev.lopyluna.slag.content.types.Incompatible;
import dev.lopyluna.slag.content.utils.FluidInput;
import dev.lopyluna.slag.register.AllBlocks;
import dev.lopyluna.slag.register.AllDataComponents;
import dev.lopyluna.slag.register.AllDynamicTypes;
import dev.lopyluna.slag.register.AllItems;
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
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

import static dev.lopyluna.slag.content.blocks.crucible_interface.client.InterfaceScreen.createLang;

@ParametersAreNonnullByDefault
public class TableCastingCategory extends AbstractRecipeCategory<RecipeHolder<TableCastingRecipe>> {
    private final IDrawable tankBackground;
    private final IDrawable tankOverlay;
    private final IDrawable castingTable;

    public TableCastingCategory(IGuiHelper guiHelper) {
        super(
                EmbersRecipesJEI.TABLE_CASTING.get(),
                Component.translatableWithFallback("gui.slag.category.table_casting", "Table Casting"),
                guiHelper.createDrawableItemLike(AllBlocks.TABLE),
                123, 54);

        ResourceLocation backgroundTexture = SlagEmbers.loc("textures/gui/jei.png");
        this.tankBackground = guiHelper.createDrawable(backgroundTexture, 0, 0, 32, 56);
        this.tankOverlay = guiHelper.createDrawable(backgroundTexture, 32, 0, 32, 56);
        this.castingTable = guiHelper.createDrawable(backgroundTexture, 84, 0, 20, 20);
    }


    @SuppressWarnings("removal")
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<TableCastingRecipe> holder, IFocusGroup focuses) {
        var recipe = holder.value();
        var cast = recipe.getCastType();
        var item = recipe.getCastItem();
        var molds = new ArrayList<ItemStack>();
        if (cast != null) for (var mold : AllItems.MOLDS) {
            var stack = mold.asStack();
            stack.set(AllDataComponents.CAST_TYPE, cast);
            molds.add(stack);
        }

        builder.addOutputSlot(86, 1)
                .setStandardSlotBackground()
                .addItemStack(recipe.getOutput());

        var tableSlot = builder.addSlot(item != null && molds.isEmpty() ? RecipeIngredientRole.INPUT : RecipeIngredientRole.CATALYST, 86, 38)
                .setBackground(castingTable, -2, -2);
        if (!molds.isEmpty()) tableSlot.addItemStacks(molds);
        else if (item == null) tableSlot.addItemStack(AllBlocks.TABLE.asStack());

        if (item != null) {
            var itemSlot = molds.isEmpty() ? tableSlot : builder.addInputSlot(49, 36).setStandardSlotBackground();
            var type = item.imprint() ? recipe.getOutput().get(AllDataComponents.CAST_TYPE) : null;
            var stacks = type == null ? List.<ItemStack>of() : castStacks(type, item.input());
            if (stacks.isEmpty()) itemSlot.addIngredients(item.input());
            else itemSlot.addItemStacks(stacks);
            itemSlot.addRichTooltipCallback((s, t) -> t.add(item.describe().copy().withStyle(ChatFormatting.GRAY)));
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
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<TableCastingRecipe> holder, IFocusGroup focuses) {
        var recipe = holder.value();
        builder.addAnimatedRecipeArrow(CastingBE.castingTicks(recipe.getInput().amount(), TableBE.COOLING_RATE, recipe.getDuration(), recipe.getSpeed())).setPosition(49, 17);
    }

    public static List<ItemStack> castStacks(TagKey<Item> cast, Ingredient input) {
        var stacks = new ArrayList<ItemStack>();
        for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(cast)) {
            var stack = new ItemStack(holder);
            if (input.test(stack)) stacks.add(stack);
        }
        var item = AllItems.DYNAMIC_PART.get();
        for (var part : AllDynamicTypes.getAllParts()) {
            if (!cast.equals(AllItems.getCast(part))) continue;
            for (var material : AllDynamicTypes.getAllMaterials()) {
                if (!Incompatible.compatible(material, part)) continue;
                var stack = item.getDefaultInstance();
                item.setMaterialType(stack, material);
                item.setPartType(stack, part);
                stacks.add(stack);
            }
        }
        return stacks;
    }

    public String getFormattedModNameForModIdWithoutDisplay(IModIdHelper helper, String modId) {
        return helper.getFormattedModNameForModId(modId);
    }

    public String getFormattedModNameForModId(IModIdHelper helper, String modId) {
        if (!helper.isDisplayingModNameEnabled()) return "";
        return helper.getFormattedModNameForModId(modId);
    }
}
