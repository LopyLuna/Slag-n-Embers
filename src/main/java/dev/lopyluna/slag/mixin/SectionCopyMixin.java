package dev.lopyluna.slag.mixin;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.lopyluna.slag.content.blocks.multiblock.FluidMultiBlockEntity.ChunkBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(targets = "net.minecraft.client.renderer.chunk.SectionCopy")
public abstract class SectionCopyMixin {
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap;copyOf(Ljava/util/Map;)Lcom/google/common/collect/ImmutableMap;"))
    private ImmutableMap<BlockPos, BlockEntity> skipParts(Map<BlockPos, BlockEntity> map, Operation<ImmutableMap<BlockPos, BlockEntity>> original, @Local(argsOnly = true, name = "levelChunk") LevelChunk levelChunk) {
        return levelChunk instanceof ChunkBlockEntities cache ? cache.slag$rendered(map) : original.call(map);
    }
}
