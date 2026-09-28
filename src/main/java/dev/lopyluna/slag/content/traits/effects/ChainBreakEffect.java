package dev.lopyluna.slag.content.traits.effects;

import com.mojang.serialization.MapCodec;
import dev.lopyluna.slag.content.traits.Trait;
import dev.lopyluna.slag.content.traits.TraitEffect;
import dev.lopyluna.slag.content.traits.Traits;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.event.level.BlockEvent;
import javax.annotation.Nonnull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public record ChainBreakEffect(HolderSet<Block> blocks) implements TraitEffect {
    public static final MapCodec<ChainBreakEffect> CODEC = RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("blocks").xmap(ChainBreakEffect::new, ChainBreakEffect::blocks);

    private static boolean breaking;

    public static ChainBreakEffect of(TagKey<Block> tag) {
        return new ChainBreakEffect(BuiltInRegistries.BLOCK.getOrCreateTag(tag));
    }

    @Override
    public @Nonnull MapCodec<ChainBreakEffect> codec() {
        return CODEC;
    }

    @Override
    public void collect(Trait trait, Traits.Builder builder) {
        builder.blockBreak(trait, this);
    }

    @Override
    public void onBlockBreak(Trait trait, ItemStack stack, BlockEvent.BreakEvent event) {
        if (breaking || !(event.getPlayer() instanceof ServerPlayer player) || player.isShiftKeyDown()) return;
        var state = event.getState();
        if (!state.is(blocks) || !stack.isCorrectToolForDrops(state)) return;
        var found = find(player.level(), event.getPos(), state.getBlock(), (int) trait.value());
        if (found.isEmpty()) return;
        breaking = true;
        try {
            for (var pos : found) {
                if (stack.isEmpty() || player.getMainHandItem() != stack) break;
                player.gameMode.destroyBlock(pos);
            }
        } finally {
            breaking = false;
        }
    }

    private static List<BlockPos> find(Level level, BlockPos origin, Block block, int max) {
        var found = new ArrayList<BlockPos>();
        if (max <= 0) return found;
        var visited = new LongOpenHashSet();
        var queue = new ArrayDeque<BlockPos>();
        visited.add(origin.asLong());
        queue.add(origin);
        while (!queue.isEmpty()) {
            var at = queue.poll();
            for (var x = -1; x <= 1; x++) for (var y = -1; y <= 1; y++) for (var z = -1; z <= 1; z++) {
                var next = at.offset(x, y, z);
                if (!visited.add(next.asLong()) || !level.isLoaded(next) || !level.getBlockState(next).is(block)) continue;
                found.add(next);
                if (found.size() >= max) return found;
                queue.add(next);
            }
        }
        return found;
    }
}
