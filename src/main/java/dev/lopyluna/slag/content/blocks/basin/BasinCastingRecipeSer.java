package dev.lopyluna.slag.content.blocks.basin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lopyluna.slag.content.blocks.casting.CastItem;
import dev.lopyluna.slag.content.utils.FluidInput;
import dev.lopyluna.slag.content.utils.ItemResult;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import javax.annotation.Nonnull;

import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class BasinCastingRecipeSer implements RecipeSerializer<BasinCastingRecipe> {
    private static final StreamCodec<RegistryFriendlyByteBuf, Optional<CastItem>> ITEM_CODEC = ByteBufCodecs.optional(CastItem.STREAM_CODEC);
    private final BasinCastingRecipe.Factory factory;
    private final MapCodec<BasinCastingRecipe> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, BasinCastingRecipe> streamCodec;

    public BasinCastingRecipeSer(BasinCastingRecipe.Factory factory) {
        this.factory = factory;
        this.codec = RecordCodecBuilder.mapCodec((instance) -> {
            var recipe = instance.group(
                    Codec.STRING.optionalFieldOf("group", "").forGetter(Recipe::getGroup),
                    FluidInput.CODEC.fieldOf("ingredient").forGetter(BasinCastingRecipe::getInput),
                    CastItem.CODEC.optionalFieldOf("cast_item").forGetter(BasinCastingRecipe::getItem),
                    Codec.INT.optionalFieldOf("duration", 0).forGetter(BasinCastingRecipe::getDuration),
                    Codec.FLOAT.optionalFieldOf("speed", 1f).forGetter(BasinCastingRecipe::getSpeed),
                    ItemResult.CODEC.fieldOf("result").forGetter(BasinCastingRecipe::getResult));
            Objects.requireNonNull(factory);
            return recipe.apply(instance, factory::create);
        });
        this.streamCodec = StreamCodec.of(this::toNetwork, this::fromNetwork);
    }
    @Override public @Nonnull MapCodec<BasinCastingRecipe> codec() {
        return codec;
    }
    @Override public @Nonnull StreamCodec<RegistryFriendlyByteBuf, BasinCastingRecipe> streamCodec() {
        return streamCodec;
    }
    @Override public String toString() {
        return "basin_casting";
    }


    private BasinCastingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
        String s = buffer.readUtf();
        var input = SizedFluidIngredient.STREAM_CODEC.decode(buffer);
        var castItem = ITEM_CODEC.decode(buffer);
        var duration = buffer.readVarInt();
        var speed = buffer.readFloat();
        var output = ItemResult.STREAM_CODEC.decode(buffer);
        return this.factory.create(s, input, castItem, duration, speed, output);
    }

    private void toNetwork(RegistryFriendlyByteBuf buffer, BasinCastingRecipe recipe) {
        buffer.writeUtf(recipe.getGroup());
        SizedFluidIngredient.STREAM_CODEC.encode(buffer, recipe.getInput());
        ITEM_CODEC.encode(buffer, recipe.getItem());
        buffer.writeVarInt(recipe.getDuration());
        buffer.writeFloat(recipe.getSpeed());
        ItemResult.STREAM_CODEC.encode(buffer, recipe.getResult());
    }

    public BasinCastingRecipe create(String group, SizedFluidIngredient input, Optional<CastItem> castItem, int duration, float speed, ItemResult result) {
        return this.factory.create(group, input, castItem, duration, speed, result);
    }
}
