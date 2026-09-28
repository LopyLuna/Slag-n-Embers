package dev.lopyluna.slag.mixin;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.lopyluna.slag.content.blocks.multiblock.FluidMultiBlockEntity.ChunkBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@OnlyIn(Dist.CLIENT)
@Mixin(targets = "net.minecraft.client.renderer.chunk.RenderChunk")
public abstract class RenderChunkMixin {
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap;copyOf(Ljava/util/Map;)Lcom/google/common/collect/ImmutableMap;"))
    private ImmutableMap<BlockPos, BlockEntity> skipParts(Map<BlockPos, BlockEntity> map, Operation<ImmutableMap<BlockPos, BlockEntity>> original, @Local(argsOnly = true) LevelChunk chunk) {
        return chunk instanceof ChunkBlockEntities cache ? cache.slag$rendered(map) : original.call(map);
    }
}
