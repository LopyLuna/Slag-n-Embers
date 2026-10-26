package dev.lopyluna.slag.content.traits.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lopyluna.slag.content.AllUtils;
import dev.lopyluna.slag.content.traits.Trait;
import dev.lopyluna.slag.content.traits.TraitEffect;
import dev.lopyluna.slag.content.traits.Traits;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import javax.annotation.Nonnull;
import java.util.function.Supplier;

@SuppressWarnings("deprecation")
public record MiningEffect(Supplier<HolderSet<Block>> blocks, float speed, int cost) implements TraitEffect {
    public static final MapCodec<MiningEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            AllUtils.lazySetCodec(BuiltInRegistries.BLOCK).fieldOf("blocks").forGetter(MiningEffect::blocks),
            Codec.FLOAT.optionalFieldOf("speed", 1f).forGetter(MiningEffect::speed),
            Codec.INT.optionalFieldOf("durability_cost", 1).forGetter(MiningEffect::cost)
    ).apply(instance, MiningEffect::new));

    public static MiningEffect of(TagKey<Block> tag) {
        return new MiningEffect(AllUtils.lazyTag(BuiltInRegistries.BLOCK, tag), 1f, 1);
    }

    public static MiningEffect of(Block block) {
        var set = HolderSet.direct(block.builtInRegistryHolder());
        return new MiningEffect(() -> set, 1f, 1);
    }

    public MiningEffect speed(float speed) {
        return new MiningEffect(blocks, speed, cost);
    }

    public MiningEffect cost(int cost) {
        return new MiningEffect(blocks, speed, cost);
    }

    @Override
    public @Nonnull MapCodec<MiningEffect> codec() {
        return CODEC;
    }

    @Override
    public void collect(Trait trait, Traits.Builder builder) {
        builder.mining.add(this);
        builder.blockCost = Math.min(builder.blockCost, cost);
    }
}
