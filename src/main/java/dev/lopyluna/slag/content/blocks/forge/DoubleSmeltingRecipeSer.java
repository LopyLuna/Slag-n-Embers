package dev.lopyluna.slag.content.blocks.forge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import javax.annotation.Nonnull;

import java.util.Objects;

public class DoubleSmeltingRecipeSer implements RecipeSerializer<DoubleSmeltingRecipe> {
    private final DoubleSmeltingRecipe.Factory factory;
    private final MapCodec<DoubleSmeltingRecipe> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, DoubleSmeltingRecipe> streamCodec;

    public DoubleSmeltingRecipeSer(DoubleSmeltingRecipe.Factory factory, int cookingTime) {
        this.factory = factory;
        this.codec = RecordCodecBuilder.mapCodec((instance) -> {
            var recipe = instance.group(
                    Codec.STRING.optionalFieldOf("group", "").forGetter(Recipe::getGroup),
                    Ingredient.CODEC_NONEMPTY.fieldOf("ingredientA").forGetter(DoubleSmeltingRecipe::getInputA),
                    Ingredient.CODEC_NONEMPTY.fieldOf("ingredientB").forGetter(DoubleSmeltingRecipe::getInputB),
                    ItemStack.CODEC.fieldOf("result").forGetter(DoubleSmeltingRecipe::getOutput),
                    Codec.FLOAT.fieldOf("experience").orElse(0.0F).forGetter(DoubleSmeltingRecipe::getExperience),
                    Codec.INT.fieldOf("cookingTime").orElse(cookingTime).forGetter(DoubleSmeltingRecipe::getCookingTime));

            Objects.requireNonNull(factory);
            return recipe.apply(instance, factory::create);
        });
        this.streamCodec = StreamCodec.of(this::toNetwork, this::fromNetwork);
    }


    @Override
    public String toString() {
        return "double_smelting";
    }

    @Override
    public @Nonnull MapCodec<DoubleSmeltingRecipe> codec() {
        return this.codec;
    }
    @Override
    public @Nonnull StreamCodec<RegistryFriendlyByteBuf, DoubleSmeltingRecipe> streamCodec() {
        return this.streamCodec;
    }


    private DoubleSmeltingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
        String s = buffer.readUtf();
        Ingredient ingredientA = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
        Ingredient ingredientB = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
        ItemStack itemstack = ItemStack.STREAM_CODEC.decode(buffer);
        float f = buffer.readFloat();
        int i = buffer.readVarInt();
        return this.factory.create(s, ingredientA, ingredientB, itemstack, f, i);
    }

    private void toNetwork(RegistryFriendlyByteBuf buffer, DoubleSmeltingRecipe recipe) {
        buffer.writeUtf(recipe.getGroup());
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.getInputA());
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.getInputB());
        ItemStack.STREAM_CODEC.encode(buffer, recipe.getOutput());
        buffer.writeFloat(recipe.getExperience());
        buffer.writeVarInt(recipe.getCookingTime());
    }

    public DoubleSmeltingRecipe create(String group, Ingredient ingredientA, Ingredient ingredientB, ItemStack result, float experience, int cookingTime) {
        return this.factory.create(group, ingredientA, ingredientB, result, experience, cookingTime);
    }
}
