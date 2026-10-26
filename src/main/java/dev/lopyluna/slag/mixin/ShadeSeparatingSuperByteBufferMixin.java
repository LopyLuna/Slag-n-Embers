package dev.lopyluna.slag.mixin;

import net.createmod.catnip.api.client.render.ShadeSeparatingSuperByteBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ShadeSeparatingSuperByteBuffer.class)
public abstract class ShadeSeparatingSuperByteBufferMixin {
    @Redirect(method = "renderInto", at = @At(value = "INVOKE", target = "Lnet/createmod/catnip/api/client/render/SuperByteBuffer;maxLight(II)I", ordinal = 0))
    private int slag$customLight(int template, int custom) {
        return custom;
    }
}
