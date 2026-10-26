package dev.lopyluna.slag.content.blocks.melter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lopyluna.slag.content.temperature.Temperatures;
import dev.lopyluna.slag.content.temperature.Temperatures.Tiers;
import dev.lopyluna.slag.content.temperature.Temperatures.Type;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import javax.annotation.Nonnull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "unused"})
public class MeltingRecipeSer {
    private final MeltingRecipe.Factory factory;
    private final MapCodec<MeltingRecipe> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, MeltingRecipe> streamCodec;

    public MeltingRecipeSer(MeltingRecipe.Factory factory) {
        this.factory = factory;
        this.codec = RecordCodecBuilder.mapCodec((instance) -> {
            var recipe = instance.group(
                    Codec.STRING.optionalFieldOf("group", "").forGetter(Recipe::group),
                    ItemStackTemplate.CODEC.listOf().fieldOf("ingredients").forGetter(MeltingRecipe::getInputTemplates),
                    Ingredient.CODEC.optionalFieldOf("ingredient").forGetter(MeltingRecipe::getInput),
                    FluidStackTemplate.CODEC.listOf().fieldOf("result").forGetter(MeltingRecipe::getOutputTemplates),
                    Temperatures.TIER_CODEC.optionalFieldOf("temperature", Tiers.HEATED).forGetter(r -> r.temperature),
                    Temperatures.TYPE_CODEC.optionalFieldOf("heat_type").forGetter(r -> Optional.ofNullable(r.heatType)),
                    Codec.INT.optionalFieldOf("duration", 0).forGetter(MeltingRecipe::getDuration),
                    Codec.FLOAT.optionalFieldOf("speed", 1f).forGetter(MeltingRecipe::getSpeed));
            Objects.requireNonNull(factory);
            return recipe.apply(instance, factory::create);
        });
        this.streamCodec = StreamCodec.of(this::toNetwork, this::fromNetwork);
    }
    public @Nonnull MapCodec<MeltingRecipe> codec() {
        return codec;
    }
    public @Nonnull StreamCodec<RegistryFriendlyByteBuf, MeltingRecipe> streamCodec() {
        return streamCodec;
    }
    public RecipeSerializer<MeltingRecipe> serializer() {
        return new RecipeSerializer<>(codec, streamCodec);
    }
    @Override public String toString() {
        return "melting";
    }


    private MeltingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
        String s = buffer.readUtf();
        var inputs = ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer);
        var input = Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.decode(buffer);
        var fluidStack = FluidStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer);
        var temperature = buffer.readEnum(Tiers.class);
        var heatType = buffer.readOptional(buf -> buf.readEnum(Type.class));
        var duration = buffer.readVarInt();
        var speed = buffer.readFloat();
        return this.factory.create(s, inputs, input, fluidStack, temperature, heatType, duration, speed);
    }

    private void toNetwork(RegistryFriendlyByteBuf buffer, MeltingRecipe recipe) {
        buffer.writeUtf(recipe.group());
        ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.getInputTemplates());
        Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.encode(buffer, recipe.getInput());
        FluidStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.getOutputTemplates());
        buffer.writeEnum(recipe.temperature);
        buffer.writeOptional(Optional.ofNullable(recipe.heatType), FriendlyByteBuf::writeEnum);
        buffer.writeVarInt(recipe.duration);
        buffer.writeFloat(recipe.speed);
    }

    public MeltingRecipe create(String group, List<ItemStackTemplate> inputs, Optional<Ingredient> input, List<FluidStackTemplate> result, Tiers temperature, Optional<Type> heatType, int duration, float speed) {
        return this.factory.create(group, inputs, input, result, temperature, heatType, duration, speed);
    }
}
