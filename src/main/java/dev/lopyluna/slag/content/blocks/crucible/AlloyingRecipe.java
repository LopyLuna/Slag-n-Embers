package dev.lopyluna.slag.content.blocks.crucible;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lopyluna.slag.content.temperature.Temperatures;
import dev.lopyluna.slag.content.temperature.Temperatures.Tiers;
import dev.lopyluna.slag.content.utils.FluidInput;
import dev.lopyluna.slag.register.AllRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import javax.annotation.Nonnull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "unused"})
@ParametersAreNonnullByDefault
public class AlloyingRecipe implements Recipe<AlloyingRecipe.AlloyRecipeInput> {

    protected final RecipeType<?> type;
    protected final String group;

    private final List<SizedFluidIngredient> inputs;
    private final FluidStack output;
    public final Tiers temperature;
    public final @Nullable Temperatures.Type heatType;
    public final boolean strict;
    public final int duration;
    public final float speed;

    public AlloyingRecipe(String group, List<SizedFluidIngredient> inputs, FluidStack output, Tiers temperature, Optional<Temperatures.Type> heatType, boolean strict, int duration, float speed) {
        this(AllRecipes.ALLOYING.get(), group, inputs, output, temperature, heatType, strict, duration, speed);
    }
    public AlloyingRecipe(RecipeType<?> type, String group, List<SizedFluidIngredient> inputs, FluidStack output, Tiers temperature, Optional<Temperatures.Type> heatType, boolean strict, int duration, float speed) {
        this.type = type;
        this.group = group;
        this.inputs = inputs;
        this.output = output;
        this.temperature = temperature;
        this.heatType = heatType.orElse(null);
        this.strict = strict;
        this.duration = duration;
        this.speed = speed;
    }

    public int getDuration() {
        return duration;
    }

    public float getSpeed() {
        return speed;
    }

    @Override
    public boolean matches(AlloyRecipeInput alloyRecipeInput, Level level) {
        var fluids = alloyRecipeInput.fluids;
        if (fluids.size() != inputs.size()) return false;
        for (var input : inputs) if (fluids.stream().noneMatch(input::test)) return false;
        return true;
    }

    public FluidStack assembleWithFluid(AlloyRecipeInput alloyRecipeInput, HolderLookup.Provider provider) {
        return output.copy();
    }
    public FluidStack getResultFluid(HolderLookup.Provider provider) {
        return output.copy();
    }

    @Override
    public @Nonnull String getGroup() {
        return group;
    }
    public List<SizedFluidIngredient> getInputs() {
        return inputs;
    }
    public FluidStack getOutput() {
        return output;
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return false;
    }
    @Override
    public @Nonnull ItemStack assemble(AlloyRecipeInput alloyRecipeInput, HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }
    @Override
    public @Nonnull ItemStack getResultItem(HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }

    @Override
    public @Nonnull RecipeSerializer<?> getSerializer() {
        return AllRecipes.ALLOYING_SER.get();
    }

    @Override
    public @Nonnull RecipeType<?> getType() {
        return type;
    }

    public static class Type implements RecipeType<AlloyingRecipe> {
        private Type() {
        }
        public static final AlloyingRecipe.Type INSTANCE = new AlloyingRecipe.Type();
        @Override
        public String toString() {
            return "alloying";
        }
    }

    public interface Factory {
        AlloyingRecipe create(String var1, List<SizedFluidIngredient> var2, FluidStack var3, Tiers var4, Optional<Temperatures.Type> var5, boolean var6, int var7, float var8);
    }

    public record AlloyRecipeInput(List<FluidStack> fluids) implements RecipeInput {
        @Override
        public @Nonnull ItemStack getItem(int i) {
            return ItemStack.EMPTY;
        }

        @Override
        public int size() {
            return fluids.size();
        }

        @Override
        public boolean isEmpty() {
            return fluids.isEmpty();
        }

        @Override
        public int hashCode() {
            return hashStackList(fluids);
        }

        public static int hashStackList(List<FluidStack> list) {
            int i = 0;
            for(var f : list) i = i * 31 + FluidStack.hashFluidAndComponents(f);
            return i;
        }
    }

    public static class Serializer implements RecipeSerializer<AlloyingRecipe> {
        private final AlloyingRecipe.Factory factory;
        private final MapCodec<AlloyingRecipe> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, AlloyingRecipe> streamCodec;

        public Serializer(AlloyingRecipe.Factory factory) {
            this.factory = factory;
            this.codec = RecordCodecBuilder.mapCodec((instance) -> {
                var recipe = instance.group(
                        Codec.STRING.optionalFieldOf("group", "").forGetter(Recipe::getGroup),
                        FluidInput.LIST_CODEC.fieldOf("ingredients").forGetter(AlloyingRecipe::getInputs),
                        FluidStack.CODEC.fieldOf("result").forGetter(AlloyingRecipe::getOutput),
                        Temperatures.TIER_CODEC.optionalFieldOf("temperature", Tiers.HEATED).forGetter(r -> r.temperature),
                        Temperatures.TYPE_CODEC.optionalFieldOf("heat_type").forGetter(r -> Optional.ofNullable(r.heatType)),
                        Codec.BOOL.optionalFieldOf("strict_temperature", false).forGetter(r -> r.strict),
                        Codec.INT.optionalFieldOf("duration", 0).forGetter(AlloyingRecipe::getDuration),
                        Codec.FLOAT.optionalFieldOf("speed", 1f).forGetter(AlloyingRecipe::getSpeed));
                Objects.requireNonNull(factory);
                return recipe.apply(instance, factory::create);
            });
            this.streamCodec = StreamCodec.of(this::toNetwork, this::fromNetwork);
        }

        @Override
        public @Nonnull MapCodec<AlloyingRecipe> codec() {
            return this.codec;
        }
        @Override
        public @Nonnull StreamCodec<RegistryFriendlyByteBuf, AlloyingRecipe> streamCodec() {
            return this.streamCodec;
        }

        @Override
        public String toString() {
            return "alloying";
        }

        private AlloyingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            String s = buffer.readUtf();
            var inputs = SizedFluidIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer);
            FluidStack fluidStack = FluidStack.STREAM_CODEC.decode(buffer);
            var temperature = buffer.readEnum(Tiers.class);
            var heatType = buffer.readOptional(buf -> buf.readEnum(Temperatures.Type.class));
            var strict = buffer.readBoolean();
            var duration = buffer.readVarInt();
            var speed = buffer.readFloat();
            return this.factory.create(s, inputs, fluidStack, temperature, heatType, strict, duration, speed);
        }

        private void toNetwork(RegistryFriendlyByteBuf buffer, AlloyingRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            SizedFluidIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.getInputs());
            FluidStack.STREAM_CODEC.encode(buffer, recipe.getOutput());
            buffer.writeEnum(recipe.temperature);
            buffer.writeOptional(Optional.ofNullable(recipe.heatType), FriendlyByteBuf::writeEnum);
            buffer.writeBoolean(recipe.strict);
            buffer.writeVarInt(recipe.duration);
            buffer.writeFloat(recipe.speed);
        }

        public AlloyingRecipe create(String group, List<SizedFluidIngredient> inputs, FluidStack result, Tiers temperature, Optional<Temperatures.Type> heatType, boolean strict, int duration, float speed) {
            return this.factory.create(group, inputs, result, temperature, heatType, strict, duration, speed);
        }
    }
}
