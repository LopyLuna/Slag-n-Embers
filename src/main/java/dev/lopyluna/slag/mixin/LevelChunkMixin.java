package dev.lopyluna.slag.mixin;

import com.google.common.collect.ImmutableMap;
import dev.lopyluna.slag.content.blocks.multiblock.FluidMultiBlockEntity;
import dev.lopyluna.slag.content.blocks.multiblock.FluidMultiBlockEntity.ChunkBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin implements ChunkBlockEntities {
    @Unique private int slag$version;
    @Unique private int slag$cachedVersion = -1;
    @Unique private ImmutableMap<BlockPos, BlockEntity> slag$cached;

    @Inject(method = {"setBlockEntity", "removeBlockEntity", "clearAllBlockEntities"}, at = @At("HEAD"))
    private void slag$changed(CallbackInfo ci) {
        slag$version++;
    }

    @Override
    public void slag$touch() {
        slag$version++;
    }

    @Override
    public ImmutableMap<BlockPos, BlockEntity> slag$rendered(Map<BlockPos, BlockEntity> all) {
        var cached = slag$cached;
        if (cached != null && slag$cachedVersion == slag$version) return cached;
        var kept = ImmutableMap.<BlockPos, BlockEntity>builder();
        for (var entry : all.entrySet()) if (!(entry.getValue() instanceof FluidMultiBlockEntity multi) || multi.isController) kept.put(entry);
        slag$cachedVersion = slag$version;
        return slag$cached = kept.build();
    }
}
