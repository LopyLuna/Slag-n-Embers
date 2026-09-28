package dev.lopyluna.slag.register;

import com.mojang.serialization.MapCodec;
import dev.lopyluna.slag.content.blocks.basin.BasinCastingRecipe;
import dev.lopyluna.slag.content.blocks.basin.BasinCastingRecipeSer;
import dev.lopyluna.slag.content.blocks.crucible.AlloyingRecipe;
import dev.lopyluna.slag.content.blocks.forge.DoubleSmeltingRecipe;
import dev.lopyluna.slag.content.blocks.forge.DoubleSmeltingRecipeSer;
import dev.lopyluna.slag.content.blocks.melter.MeltingRecipe;
import dev.lopyluna.slag.content.blocks.melter.MeltingRecipeSer;
import dev.lopyluna.slag.content.blocks.table.TableCastingRecipe;
import dev.lopyluna.slag.content.blocks.table.TableCastingRecipeSer;
import dev.lopyluna.slag.content.conditions.CastOnlyCondition;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredHolder;

import static dev.lopyluna.slag.SlagEmbers.REGISTER;

@SuppressWarnings("unused")
public class AllRecipes {
    public static final DeferredHolder<RecipeSerializer<?>, DoubleSmeltingRecipeSer> DOUBLE_SMELTING_SER = REGISTER.recipeSer()
            .register("double_smelting", () -> new DoubleSmeltingRecipeSer(DoubleSmeltingRecipe::new, 200));
    public static final DeferredHolder<RecipeSerializer<?>, AlloyingRecipe.Serializer> ALLOYING_SER = REGISTER.recipeSer()
            .register("alloying", () -> new AlloyingRecipe.Serializer(AlloyingRecipe::new));
    public static final DeferredHolder<RecipeSerializer<?>, MeltingRecipeSer> MELTING_SER = REGISTER.recipeSer()
            .register("melting", () -> new MeltingRecipeSer(MeltingRecipe::new));
    public static final DeferredHolder<RecipeSerializer<?>, BasinCastingRecipeSer> BASIN_CASTING_SER = REGISTER.recipeSer()
            .register("basin_casting", () -> new BasinCastingRecipeSer(BasinCastingRecipe::new));
    public static final DeferredHolder<RecipeSerializer<?>, TableCastingRecipeSer> TABLE_CASTING_SER = REGISTER.recipeSer()
            .register("table_casting", () -> new TableCastingRecipeSer(TableCastingRecipe::new));


    public static final DeferredHolder<RecipeType<?>, DoubleSmeltingRecipe.Type> DOUBLE_SMELTING = REGISTER.recipes()
            .register("double_smelting", () -> DoubleSmeltingRecipe.Type.INSTANCE);
    public static final DeferredHolder<RecipeType<?>, AlloyingRecipe.Type> ALLOYING = REGISTER.recipes()
            .register("alloying", () -> AlloyingRecipe.Type.INSTANCE);
    public static final DeferredHolder<RecipeType<?>, MeltingRecipe.Type> MELTING = REGISTER.recipes()
            .register("melting", () -> MeltingRecipe.Type.INSTANCE);
    public static final DeferredHolder<RecipeType<?>, BasinCastingRecipe.Type> BASIN_CASTING = REGISTER.recipes()
            .register("basin_casting", () -> BasinCastingRecipe.Type.INSTANCE);
    public static final DeferredHolder<RecipeType<?>, TableCastingRecipe.Type> TABLE_CASTING = REGISTER.recipes()
            .register("table_casting", () -> TableCastingRecipe.Type.INSTANCE);

    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<CastOnlyCondition>> CAST_ONLY = REGISTER.conditions()
            .register("cast_only", () -> CastOnlyCondition.CODEC);

    public static void register() {}
}
