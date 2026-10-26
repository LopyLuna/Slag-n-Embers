package dev.lopyluna.slag.mixin;

import dev.lopyluna.slag.content.blocks.multiblock.FluidMultiBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {
    @Inject(method = "getRenderer(Lnet/minecraft/world/level/block/entity/BlockEntity;)Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;", at = @At("HEAD"), cancellable = true)
    private <E extends BlockEntity> void skipParts(E blockEntity, CallbackInfoReturnable<BlockEntityRenderer<E, ?>> cir) {
        if (blockEntity instanceof FluidMultiBlockEntity multi && !multi.isController) cir.setReturnValue(null);
    }
}
