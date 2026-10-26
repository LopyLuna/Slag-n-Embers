package dev.lopyluna.slag.content.blocks.forge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import javax.annotation.Nonnull;

import java.util.Objects;

@SuppressWarnings("unused")
public class DoubleSmeltingRecipeSer {
    private final DoubleSmeltingRecipe.Factory factory;
    private final MapCodec<DoubleSmeltingRecipe> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, DoubleSmeltingRecipe> streamCodec;

    public DoubleSmeltingRecipeSer(DoubleSmeltingRecipe.Factory factory, int cookingTime) {
        this.factory = factory;
        this.codec = RecordCodecBuilder.mapCodec((instance) -> {
            var recipe = instance.group(
                    Codec.STRING.optionalFieldOf("group", "").forGetter(Recipe::group),
                    Ingredient.CODEC.fieldOf("ingredientA").forGetter(DoubleSmeltingRecipe::getInputA),
                    Ingredient.CODEC.fieldOf("ingredientB").forGetter(DoubleSmeltingRecipe::getInputB),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(DoubleSmeltingRecipe::getOutputTemplate),
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

    public @Nonnull MapCodec<DoubleSmeltingRecipe> codec() {
        return this.codec;
    }
    public @Nonnull StreamCodec<RegistryFriendlyByteBuf, DoubleSmeltingRecipe> streamCodec() {
        return this.streamCodec;
    }
    public RecipeSerializer<DoubleSmeltingRecipe> serializer() {
        return new RecipeSerializer<>(codec, streamCodec);
    }


    private DoubleSmeltingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
        String s = buffer.readUtf();
        Ingredient ingredientA = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
        Ingredient ingredientB = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
        var itemstack = ItemStackTemplate.STREAM_CODEC.decode(buffer);
        float f = buffer.readFloat();
        int i = buffer.readVarInt();
        return this.factory.create(s, ingredientA, ingredientB, itemstack, f, i);
    }

    private void toNetwork(RegistryFriendlyByteBuf buffer, DoubleSmeltingRecipe recipe) {
        buffer.writeUtf(recipe.group());
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.getInputA());
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.getInputB());
        ItemStackTemplate.STREAM_CODEC.encode(buffer, recipe.getOutputTemplate());
        buffer.writeFloat(recipe.getExperience());
        buffer.writeVarInt(recipe.getCookingTime());
    }

    public DoubleSmeltingRecipe create(String group, Ingredient ingredientA, Ingredient ingredientB, ItemStackTemplate result, float experience, int cookingTime) {
        return this.factory.create(group, ingredientA, ingredientB, result, experience, cookingTime);
    }
}
