package dev.lopyluna.slag.content.blocks.table;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lopyluna.slag.content.AllUtils;
import dev.lopyluna.slag.content.blocks.casting.CastItem;
import dev.lopyluna.slag.content.utils.FluidInput;
import dev.lopyluna.slag.content.utils.ItemResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import javax.annotation.Nonnull;

import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class TableCastingRecipeSer implements RecipeSerializer<TableCastingRecipe> {
    private static final StreamCodec<ByteBuf, Optional<TagKey<Item>>> CAST_CODEC = ByteBufCodecs.optional(AllUtils.tagKeyStreamCodec(Registries.ITEM));
    private static final StreamCodec<RegistryFriendlyByteBuf, Optional<CastItem>> ITEM_CODEC = ByteBufCodecs.optional(CastItem.STREAM_CODEC);
    private final TableCastingRecipe.Factory factory;
    private final MapCodec<TableCastingRecipe> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, TableCastingRecipe> streamCodec;

    public TableCastingRecipeSer(TableCastingRecipe.Factory factory) {
        this.factory = factory;
        this.codec = RecordCodecBuilder.mapCodec((instance) -> {
            var recipe = instance.group(
                    Codec.STRING.optionalFieldOf("group", "").forGetter(Recipe::getGroup),
                    TagKey.codec(Registries.ITEM).optionalFieldOf("cast").forGetter(TableCastingRecipe::getCast),
                    FluidInput.CODEC.fieldOf("ingredient").forGetter(TableCastingRecipe::getInput),
                    CastItem.CODEC.optionalFieldOf("cast_item").forGetter(TableCastingRecipe::getItem),
                    Codec.INT.optionalFieldOf("duration", 0).forGetter(TableCastingRecipe::getDuration),
                    Codec.FLOAT.optionalFieldOf("speed", 1f).forGetter(TableCastingRecipe::getSpeed),
                    ItemResult.CODEC.fieldOf("result").forGetter(TableCastingRecipe::getResult));
            Objects.requireNonNull(factory);
            return recipe.apply(instance, factory::create);
        });
        this.streamCodec = StreamCodec.of(this::toNetwork, this::fromNetwork);
    }
    @Override public @Nonnull MapCodec<TableCastingRecipe> codec() {
        return codec;
    }
    @Override public @Nonnull StreamCodec<RegistryFriendlyByteBuf, TableCastingRecipe> streamCodec() {
        return streamCodec;
    }
    @Override public String toString() {
        return "table_casting";
    }


    private TableCastingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
        String s = buffer.readUtf();
        var input = SizedFluidIngredient.STREAM_CODEC.decode(buffer);
        var type = CAST_CODEC.decode(buffer);
        var castItem = ITEM_CODEC.decode(buffer);
        var duration = buffer.readVarInt();
        var speed = buffer.readFloat();
        var output = ItemResult.STREAM_CODEC.decode(buffer);
        return this.factory.create(s, type, input, castItem, duration, speed, output);
    }

    private void toNetwork(RegistryFriendlyByteBuf buffer, TableCastingRecipe recipe) {
        buffer.writeUtf(recipe.getGroup());
        SizedFluidIngredient.STREAM_CODEC.encode(buffer, recipe.getInput());
        CAST_CODEC.encode(buffer, recipe.getCast());
        ITEM_CODEC.encode(buffer, recipe.getItem());
        buffer.writeVarInt(recipe.getDuration());
        buffer.writeFloat(recipe.getSpeed());
        ItemResult.STREAM_CODEC.encode(buffer, recipe.getResult());
    }

    public TableCastingRecipe create(String group, Optional<TagKey<Item>> type, SizedFluidIngredient input, Optional<CastItem> castItem, int duration, float speed, ItemResult result) {
        return this.factory.create(group, type, input, castItem, duration, speed, result);
    }
}
