package dev.lopyluna.slag.content.jei;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.blocks.basin.BasinCastingRecipe;
import dev.lopyluna.slag.content.blocks.crucible.AlloyingRecipe;
import dev.lopyluna.slag.content.blocks.crucible_interface.client.InterfaceScreen;
import dev.lopyluna.slag.content.blocks.forge.DoubleSmeltingRecipe;
import dev.lopyluna.slag.content.blocks.forge.client.ForgeMenu;
import dev.lopyluna.slag.content.blocks.forge.client.ForgeScreen;
import dev.lopyluna.slag.content.blocks.melter.MeltingRecipe;
import dev.lopyluna.slag.content.blocks.melter.client.MelterMenu;
import dev.lopyluna.slag.content.blocks.melter.client.MelterScreen;
import dev.lopyluna.slag.content.blocks.table.TableCastingRecipe;
import dev.lopyluna.slag.content.items.modular.DataDynamicParts;
import dev.lopyluna.slag.content.jei.category.*;
import dev.lopyluna.slag.content.smithing.client.ModularSmithingScreen;
import dev.lopyluna.slag.content.temperature.Temperatures;
import dev.lopyluna.slag.content.traits.Traits;
import dev.lopyluna.slag.content.types.Incompatible;
import dev.lopyluna.slag.content.types.ModularType;
import dev.lopyluna.slag.content.utils.ItemResult;
import dev.lopyluna.slag.register.*;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.fluids.FluidStack;
import javax.annotation.Nonnull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@SuppressWarnings({"unused", "NullableProblems", "FieldCanBeLocal"})
@JeiPlugin
public class EmbersJEI implements IModPlugin {
    @Override
    public @Nonnull ResourceLocation getPluginUid() {
        return SlagEmbers.loc("main");
    }

    @Nullable private IRecipeCategory<RecipeHolder<DoubleSmeltingRecipe>> forgeCategory;
    @Nullable private IRecipeCategory<RecipeHolder<MeltingRecipe>> melterCategory;
    @Nullable private IRecipeCategory<RecipeHolder<TableCastingRecipe>> tableCastingCategory;
    @Nullable private IRecipeCategory<RecipeHolder<BasinCastingRecipe>> basinCastingCategory;
    @Nullable private IRecipeCategory<RecipeHolder<AlloyingRecipe>> alloyingCategory;
    @Nullable private IRecipeCategory<ModularType> modularCategory;
    public static IModIdHelper modIds;
    private List<HeatingCategory.Heater> heaters = List.of();
    private int heaterVersion = -1;

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var jeiHelpers = registration.getJeiHelpers();
        var guiHelper = jeiHelpers.getGuiHelper();
        modIds = jeiHelpers.getModIdHelper();

        registration.addRecipeCategories(forgeCategory = new DoubleSmeltingCategory(guiHelper));
        registration.addRecipeCategories(melterCategory = new MeltingCategory(guiHelper));
        registration.addRecipeCategories(tableCastingCategory = new TableCastingCategory(guiHelper));
        registration.addRecipeCategories(basinCastingCategory = new BasinCastingCategory(guiHelper));
        registration.addRecipeCategories(alloyingCategory = new AlloyingCategory(guiHelper));
        registration.addRecipeCategories(modularCategory = new ModularCategory(guiHelper));
        registration.addRecipeCategories(new HeatingCategory(guiHelper));
    }

    @Override
    public void registerRecipes(@Nonnull IRecipeRegistration registration) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        safely("double smelting", () -> registration.addRecipes(EmbersRecipesJEI.DOUBLE_SMELTING.get(), handled(level, AllRecipes.DOUBLE_SMELTING.get(), forgeCategory)));
        safely("melting", () -> registration.addRecipes(EmbersRecipesJEI.MELTING.get(), handled(level, AllRecipes.MELTING.get(), melterCategory)));
        safely("table casting", () -> registration.addRecipes(EmbersRecipesJEI.TABLE_CASTING.get(), getTableCastingRecipes(tableCastingCategory, level)));
        safely("basin casting", () -> registration.addRecipes(EmbersRecipesJEI.BASIN_CASTING.get(), handled(level, AllRecipes.BASIN_CASTING.get(), basinCastingCategory)));
        safely("alloying", () -> registration.addRecipes(EmbersRecipesJEI.ALLOYING.get(), handled(level, AllRecipes.ALLOYING.get(), alloyingCategory)));
        safely("modular", () -> registration.addRecipes(EmbersRecipesJEI.MODULAR, getModularRecipes()));
        heaters = HeatingCategory.heaters();
        heaterVersion = Temperatures.version;
        registration.addRecipes(EmbersRecipesJEI.HEATING, heaters);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(ForgeScreen.class, 78, 32, 28, 23, EmbersRecipesJEI.DOUBLE_SMELTING.get(), RecipeTypes.FUELING);
        registration.addRecipeClickArea(MelterScreen.class, 78, 32, 28, 23, EmbersRecipesJEI.MELTING.get());

        var im = registration.getJeiHelpers().getIngredientManager();
        registration.addGuiContainerHandler(MelterScreen.class, new IGuiContainerHandler<>() {
            @Override
            public @Nonnull Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(MelterScreen screen, double mouseX, double mouseY) {
                return clickable(im, screen.ingredient, screen.ingredientArea);
            }
        });
        registration.addGuiContainerHandler(InterfaceScreen.class, new IGuiContainerHandler<>() {
            @Override
            public @Nonnull Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(InterfaceScreen screen, double mouseX, double mouseY) {
                return clickable(im, screen.ingredient, screen.ingredientArea);
            }
        });
        registration.addGuiContainerHandler(ModularSmithingScreen.class, new IGuiContainerHandler<>() {
            @Override
            public @Nonnull List<Rect2i> getGuiExtraAreas(ModularSmithingScreen screen) {
                return screen.extraAreas();
            }
        });
    }

    private static Optional<IClickableIngredient<?>> clickable(IIngredientManager im, @Nullable Object ingredient, @Nullable Rect2i area) {
        if (ingredient == null || area == null) return Optional.empty();
        return im.createClickableIngredient(ingredient, area, true).map(i -> i);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(ForgeMenu.class, AllMenuTypes.FORGE.get(), EmbersRecipesJEI.DOUBLE_SMELTING.get(), 0, 2, 4, 36);
        registration.addRecipeTransferHandler(MelterMenu.class, AllMenuTypes.MELTER.get(), EmbersRecipesJEI.MELTING.get(), 0, 1, 1, 36);
    }

    @Override
    public void registerAdvanced(IAdvancedRegistration registration) {
        registration.addTypedRecipeManagerPlugin(EmbersRecipesJEI.MODULAR, new ModularRecipes());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(AllBlocks.FORGE, RecipeTypes.FUELING);
        registration.addRecipeCatalyst(AllBlocks.FORGE, EmbersRecipesJEI.DOUBLE_SMELTING.get());
        registration.addRecipeCatalyst(AllBlocks.MELTER, EmbersRecipesJEI.MELTING.get());
        registration.addRecipeCatalyst(AllBlocks.TABLE, EmbersRecipesJEI.TABLE_CASTING.get());
        registration.addRecipeCatalyst(AllItems.SANDSTONE_MOLD, EmbersRecipesJEI.TABLE_CASTING.get());
        registration.addRecipeCatalyst(AllItems.TERRACOTTA_MOLD, EmbersRecipesJEI.TABLE_CASTING.get());
        registration.addRecipeCatalyst(AllItems.CAST_IRON_MOLD, EmbersRecipesJEI.TABLE_CASTING.get());
        registration.addRecipeCatalyst(AllBlocks.BASIN, EmbersRecipesJEI.BASIN_CASTING.get());
        registration.addRecipeCatalyst(AllBlocks.CRUCIBLE, EmbersRecipesJEI.ALLOYING.get());
        registration.addRecipeCatalyst(new ItemStack(Items.SMITHING_TABLE), EmbersRecipesJEI.MODULAR);
        registration.addRecipeCatalyst(AllItems.MODULAR_ITEM, EmbersRecipesJEI.MODULAR);
        registration.addRecipeCatalyst(AllBlocks.MELTER, EmbersRecipesJEI.HEATING);
        registration.addRecipeCatalyst(AllBlocks.CRUCIBLE, EmbersRecipesJEI.HEATING);
    }

    public List<ModularType> getModularRecipes() {
        return AllDynamicTypes.getAllModulars().stream()
                .filter(modular -> !ModularCategory.results(modular).isEmpty())
                .sorted(Comparator.comparingInt((ModularType modular) -> modular.sortOrder).thenComparing(modular -> modular.id.toString()))
                .toList();
    }

    public List<RecipeHolder<TableCastingRecipe>> getTableCastingRecipes(IRecipeCategory<RecipeHolder<TableCastingRecipe>> category, ClientLevel level) {
        var recipes = new ArrayList<RecipeHolder<TableCastingRecipe>>();
        for (var holder : handled(level, AllRecipes.TABLE_CASTING.get(), category)) {
            var recipe = holder.value();
            var item = recipe.getCastItem();
            if (item == null || !item.imprint()) {
                recipes.add(holder);
                continue;
            }
            for (var cast : AllTags.CASTS) {
                if (TableCastingCategory.castStacks(cast, item.input()).isEmpty()) continue;
                var output = recipe.getOutput().copy();
                output.set(AllDataComponents.CAST_TYPE, cast);
                var id = holder.id().withSuffix("/" + cast.location().getPath().replace('/', '_'));
                recipes.add(new RecipeHolder<>(id, new TableCastingRecipe(recipe.getGroup(), recipe.getCast(), recipe.getInput(), recipe.getItem(), recipe.getDuration(), recipe.getSpeed(), ItemResult.of(output))));
            }
        }
        return recipes;
    }
    private static <C extends RecipeInput, T extends Recipe<C>> List<RecipeHolder<T>> handled(ClientLevel level, RecipeType<T> type, IRecipeCategory<RecipeHolder<T>> category) {
        var out = new ArrayList<RecipeHolder<T>>();
        for (var holder : level.getRecipeManager().getAllRecipesFor(type)) {
            try {
                if (category.isHandled(holder)) out.add(holder);
            } catch (RuntimeException | LinkageError e) {
                SlagEmbers.LOGGER.error("Skipping broken recipe {} in JEI", holder.id(), e);
            }
        }
        return out;
    }

    private static void safely(String name, Runnable task) {
        try {
            task.run();
        } catch (RuntimeException | LinkageError e) {
            SlagEmbers.LOGGER.error("Couldn't add {} recipes to JEI", name, e);
        }
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration reg) {
        reg.registerSubtypeInterpreter(AllItems.DYNAMIC_PART.get(), EmbersSubtypeInterpreters.PART_INSTANCE);
        reg.registerSubtypeInterpreter(AllItems.MODULAR_ITEM.get(), EmbersSubtypeInterpreters.MODULAR_INSTANCE);
        reg.registerSubtypeInterpreter(AllItems.SANDSTONE_MOLD.get(), EmbersSubtypeInterpreters.MOLD_INSTANCE);
        reg.registerSubtypeInterpreter(AllItems.TERRACOTTA_MOLD.get(), EmbersSubtypeInterpreters.MOLD_INSTANCE);
        reg.registerSubtypeInterpreter(AllItems.CAST_IRON_MOLD.get(), EmbersSubtypeInterpreters.MOLD_INSTANCE);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime rt) {
        Temperatures.onChange = () -> Minecraft.getInstance().execute(() -> refreshHeaters(rt));
        refreshHeaters(rt);
        var im = rt.getIngredientManager();

        var variants = new ArrayList<ItemStack>();

        var materials = AllDynamicTypes.getAllMaterials().stream()
                .sorted(Comparator.comparingInt(type -> type.sortOrder)).toList();
        var parts = AllDynamicTypes.getAllParts().stream()
                .sorted(Comparator.comparingInt(type -> type.sortOrder)).toList();
        var modulars = AllDynamicTypes.getAllModulars().stream()
                .sorted(Comparator.comparingInt(type -> type.sortOrder)).toList();

        for (var material : materials) for (var part : parts) {
            if (!Incompatible.compatible(material, part)) continue;
            var item = AllItems.DYNAMIC_PART.get();
            var stack = item.getDefaultInstance();

            item.setMaterialType(stack, material);
            item.setPartType(stack, part);

            variants.add(stack);
        }

        for (var material : materials) for (var modular : modulars) {
            var result = modular.getResultStack();
            if (!result.isEmpty()) continue;
            var baseTool = AllItems.MODULAR_ITEM.asStack();
            var toolParts = new ArrayList<ItemStack>();
            for (var part : AllDynamicTypes.getAllPartsFromModular(modular)) {
                var dynamicPart = AllItems.DYNAMIC_PART.get();
                var stack = dynamicPart.getDefaultInstance();
                dynamicPart.setMaterialType(stack, material);
                dynamicPart.setPartType(stack, part);
                stack.set(AllDataComponents.BUILT, modular.id);
                toolParts.add(stack);
            }

            for (var stack : modular.finalSegmentStacks) {
                var copy = stack.copy();
                copy.set(AllDataComponents.BUILT, modular.id);
                toolParts.add(copy);
            }

            if (!Incompatible.compatible(toolParts, modular)) continue;
            baseTool.set(AllDataComponents.DYNAMIC_PARTS, new DataDynamicParts(toolParts));
            baseTool.set(AllDataComponents.MODULAR_TYPE, modular.id);
            Traits.of(baseTool).applyComponents(baseTool);

            variants.add(baseTool);
        }

        for (var mold : AllItems.MOLDS) for (var cast : AllTags.CASTS) {
            var stack = mold.asStack();
            stack.set(AllDataComponents.CAST_TYPE, cast);
            variants.add(stack);
        }

        var known = ItemStackLinkedSet.createTypeAndComponentsSet();
        known.addAll(im.getAllItemStacks());
        variants.removeIf(stack -> !known.add(stack));
        if (!variants.isEmpty()) im.addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, variants);

        var fluids = new ArrayList<FluidStack>();
        var buckets = new ArrayList<ItemStack>();
        for (var fluid : AllFluids.updateHidden()) {
            var source = fluid.get().getSource();
            fluids.add(new FluidStack(source, 1000));
            buckets.add(new ItemStack(source.getBucket()));
        }
        if (!fluids.isEmpty()) im.removeIngredientsAtRuntime(NeoForgeTypes.FLUID_STACK, fluids);
        if (!buckets.isEmpty()) im.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, buckets);
    }

    @Override
    public void onRuntimeUnavailable() {
        Temperatures.onChange = () -> {};
    }

    private void refreshHeaters(IJeiRuntime rt) {
        if (heaterVersion == Temperatures.version) return;
        var manager = rt.getRecipeManager();
        if (!heaters.isEmpty()) manager.hideRecipes(EmbersRecipesJEI.HEATING, heaters);
        heaters = HeatingCategory.heaters();
        heaterVersion = Temperatures.version;
        if (!heaters.isEmpty()) manager.addRecipes(EmbersRecipesJEI.HEATING, heaters);
    }
}
