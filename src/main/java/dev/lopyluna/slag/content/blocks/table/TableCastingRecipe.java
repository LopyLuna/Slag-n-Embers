package dev.lopyluna.slag.content.blocks.table;

import dev.lopyluna.slag.content.blocks.casting.CastItem;
import dev.lopyluna.slag.content.blocks.casting.CastingInput;
import dev.lopyluna.slag.content.utils.FluidInput;
import dev.lopyluna.slag.content.utils.ItemResult;
import dev.lopyluna.slag.register.AllRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import javax.annotation.Nonnull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@SuppressWarnings({"unused", "OptionalUsedAsFieldOrParameterType"})
@ParametersAreNonnullByDefault
public class TableCastingRecipe implements Recipe<CastingInput> {
    protected final RecipeType<?> type;
    protected final String group;

    private final @Nullable TagKey<Item> castType;
    private final SizedFluidIngredient input;
    private final @Nullable CastItem castItem;
    private final int duration;
    private final float speed;
    private final ItemResult output;

    public TableCastingRecipe(String group, Optional<TagKey<Item>> castType, SizedFluidIngredient inputs, Optional<CastItem> castItem, int duration, float speed, ItemResult output) {
        this(AllRecipes.TABLE_CASTING.get(), group, castType, inputs, castItem, duration, speed, output);
    }
    public TableCastingRecipe(RecipeType<?> type, String group, Optional<TagKey<Item>> castType, SizedFluidIngredient inputs, Optional<CastItem> castItem, int duration, float speed, ItemResult output) {
        this.type = type;
        this.group = group;
        this.castType = castType.orElse(null);
        this.input = inputs;
        this.castItem = castItem.orElse(null);
        this.duration = duration;
        this.speed = speed;
        this.output = output;
    }

    public @Nullable TagKey<Item> getCastType() {
        return castType;
    }
    public Optional<TagKey<Item>> getCast() {
        return Optional.ofNullable(castType);
    }
    public SizedFluidIngredient getInput() {
        return input;
    }
    public @Nullable CastItem getCastItem() {
        return castItem;
    }
    public Optional<CastItem> getItem() {
        return Optional.ofNullable(castItem);
    }
    public int getDuration() {
        return duration;
    }
    public float getSpeed() {
        return speed;
    }
    public ItemStack getOutput() {
        return output.stack();
    }
    public ItemResult getResult() {
        return output;
    }

    @Override
    public boolean matches(CastingInput fluidInput, Level level) {
        return FluidInput.test(input, fluidInput.fluid());
    }

    public boolean hasEnoughFluid(FluidStack stack) {
        return input.test(stack);
    }

    @Override
    public @Nonnull ItemStack assemble(CastingInput fluidInput, HolderLookup.Provider provider) {
        return output.stack().copy();
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return false;
    }

    @Override
    public @Nonnull ItemStack getResultItem(HolderLookup.Provider provider) {
        return output.stack().copy();
    }

    @Override
    public @Nonnull RecipeSerializer<?> getSerializer() {
        return AllRecipes.TABLE_CASTING_SER.get();
    }

    @Override
    public @Nonnull RecipeType<?> getType() {
        return type;
    }

    public static class Type implements RecipeType<TableCastingRecipe> {
        private Type() {
        }
        public static final Type INSTANCE = new Type();
        @Override
        public String toString() {
            return "table_casting";
        }
    }

    public interface Factory {
        TableCastingRecipe create(String var1, Optional<TagKey<Item>> var2, SizedFluidIngredient var3, Optional<CastItem> var4, int var5, float var6, ItemResult var7);
    }

}
