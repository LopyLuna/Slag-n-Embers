package dev.lopyluna.slag.content.jei.category;

import com.mojang.serialization.Codec;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.AllUtils;
import dev.lopyluna.slag.content.blocks.melter.MelterBE;
import dev.lopyluna.slag.content.blocks.melter.MeltingRecipe;
import dev.lopyluna.slag.content.jei.EmbersJEI;
import dev.lopyluna.slag.content.jei.EmbersRecipesJEI;
import dev.lopyluna.slag.register.AllBlocks;
import dev.lopyluna.slag.register.AllLangs;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import javax.annotation.Nonnull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

import static dev.lopyluna.slag.content.blocks.crucible_interface.client.InterfaceScreen.createLang;

@SuppressWarnings("all")
@ParametersAreNonnullByDefault
public class MeltingCategory extends AbstractRecipeCategory<RecipeHolder<MeltingRecipe>> {
    private static final int MIN_HEIGHT = 3;

    private final IDrawable tankBackground;
    private final IDrawable tankOverlay;
    private final IDrawable validHeaterSlot;

    public MeltingCategory(IGuiHelper guiHelper) {
        super(EmbersRecipesJEI.MELTING.get(), Component.translatableWithFallback("gui.slag.category.melting", "Melting"), guiHelper.createDrawableItemLike(AllBlocks.MELTER), 123, 54);

        ResourceLocation backgroundTexture = SlagEmbers.loc("textures/gui/jei.png");
        this.tankBackground = guiHelper.createDrawable(backgroundTexture, 0, 0, 32, 56);
        this.tankOverlay = guiHelper.createDrawable(backgroundTexture, 32, 0, 32, 56);
        this.validHeaterSlot = guiHelper.createDrawable(backgroundTexture, 64, 0, 20, 20);
    }

    @SuppressWarnings("removal")
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<MeltingRecipe> holder, IFocusGroup focuses) {
        var recipe = holder.value();

        List<ItemStack> inputs = new ArrayList<>(List.of(recipe.getInput().getItems()));
        inputs.addAll(recipe.getInputs());
        inputs.removeIf(s -> s.is(Items.BARRIER));

        builder.addInputSlot(20, 1)
                .setStandardSlotBackground()
                .addItemStacks(inputs);

        builder.addSlot(RecipeIngredientRole.CATALYST, 20, 38)
                .setBackground(validHeaterSlot, -2, -2)
                .addItemStacks(AllUtils.getHeaterStacks(recipe.temperature, recipe.heatType))
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(AllLangs.requires(recipe.temperature, recipe.heatType).withStyle(ChatFormatting.GRAY)));

        var fluids = getResultFluids(recipe);

        var tooltipFlag = Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
        var advance = tooltipFlag.advanced();

        int totalMb = getTotalAmount(fluids);
        if (totalMb == 0) return;

        int tankHeight = 48;

        int filledHeight = Math.max(getFilledHeight(totalMb, tankHeight), Math.min(tankHeight, fluids.size() * MIN_HEIGHT));
        int[] heights = getHeights(fluids, totalMb, filledHeight);

        int yCur = 3 + (tankHeight - filledHeight);

        for (int i = 0; i < fluids.size(); i++) {
            var fluid = fluids.get(i);
            int fluidHeight = heights[i];

            var fluidSlot = builder.addOutputSlot(83, yCur)
                    .setFluidRenderer(fluid.getAmount(), false, 24, fluidHeight)
                    .addFluidStack(fluid.getFluid(), fluid.getAmount());

            if (i == 0) fluidSlot.setOverlay(tankOverlay, -4, -4 - (yCur - 3))
                    .setBackground(tankBackground, -4, -4 - (yCur - 3));

            fluidSlot.addRichTooltipCallback((s, t) -> {
                t.clear();
                var tooltips = new ArrayList<Component>();
                tooltips.add(fluid.getDisplayName());
                createLang(fluid, tooltips).run();
                if (advance) {
                    var loc = BuiltInRegistries.FLUID.getKey(fluid.getFluid());
                    tooltips.add(Component.literal(loc.toString()).withStyle(ChatFormatting.DARK_GRAY));
                    var helper = EmbersJEI.modIds;
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
            });

            yCur += fluidHeight;
        }
    }

    private static int[] getHeights(List<FluidStack> fluids, int totalMb, int filledHeight) {
        var heights = new int[fluids.size()];
        var sum = 0;
        for (var i = 0; i < heights.length; i++) {
            heights[i] = Math.max(MIN_HEIGHT, Math.round((float) fluids.get(i).getAmount() * filledHeight / totalMb));
            sum += heights[i];
        }
        while (sum != filledHeight) {
            var step = sum > filledHeight ? -1 : 1;
            var best = -1;
            for (var i = 0; i < heights.length; i++) {
                if (step < 0 && heights[i] <= MIN_HEIGHT) continue;
                if (best < 0 || heights[i] > heights[best]) best = i;
            }
            if (best < 0) break;
            heights[best] += step;
            sum += step;
        }
        return heights;
    }

    private static int getFilledHeight(int totalMb, int tankHeight) {
        int maxCapacity = 2500;
        int tankCapacity;

        if (totalMb >= maxCapacity) {
            tankCapacity = maxCapacity;
        } else {
            float t = Math.min(1.0f, (float) totalMb / (float) maxCapacity);

            float adjustedT = (float) Math.pow(t, 1.5);
            float easedFill = 1.0f - (float) Math.cos(adjustedT * Math.PI / 2.0);
            if (easedFill < 0.01f) easedFill = 0.01f;

            tankCapacity = (int) (totalMb / easedFill);
            tankCapacity = Math.min(tankCapacity, maxCapacity);
        }

        float fillRatio = Math.min(1.0f, (float) totalMb / (float) tankCapacity);
        return Math.max(2, (int) (tankHeight * fillRatio));
    }

    public String getFormattedModNameForModIdWithoutDisplay(IModIdHelper helper, String modId) {
        return helper.getFormattedModNameForModId(modId);
    }

    public String getFormattedModNameForModId(IModIdHelper helper, String modId) {
        if (!helper.isDisplayingModNameEnabled()) return "";
        return helper.getFormattedModNameForModId(modId);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<MeltingRecipe> holder, IFocusGroup focuses) {
        var recipe = holder.value();
        var total = 0;
        for (var fluid : recipe.getOutputs()) total += fluid.getAmount();
        builder.addAnimatedRecipeArrow(MelterBE.meltingTicks(total, recipe.duration, recipe.speed)).setPosition(49, 17);
        builder.addAnimatedRecipeFlame(999999).setPosition(21, 20);
    }


    @Override public boolean isHandled(RecipeHolder<MeltingRecipe> holder) {
        var recipe = holder.value();
        if (recipe.isSpecial()) return false;
        return !recipe.getInput().hasNoItems() || !recipe.getInputs().isEmpty();
    }
    @Override public ResourceLocation getRegistryName(RecipeHolder<MeltingRecipe> recipe) {
        return recipe.id();
    }
    @Override public @Nonnull Codec<RecipeHolder<MeltingRecipe>> getCodec(ICodecHelper helper, @Nonnull IRecipeManager manager) {
        return helper.getRecipeHolderCodec();
    }

    public static int getTotalAmount(List<FluidStack> fluids) {
        var i = 0;
        for (var fluid : fluids) i += fluid.getAmount();
        return i;
    }

    public static List<FluidStack> getResultFluids(MeltingRecipe recipe) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) throw new NullPointerException("level must not be null.");
        RegistryAccess registryAccess = level.registryAccess();
        return recipe.getResultFluids(registryAccess);
    }
}
