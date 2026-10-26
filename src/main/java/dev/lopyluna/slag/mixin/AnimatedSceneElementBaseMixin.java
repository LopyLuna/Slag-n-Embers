package dev.lopyluna.slag.mixin;

import net.createmod.ponder.impl.client.element.AnimatedSceneElementBase;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnimatedSceneElementBase.class)
public abstract class AnimatedSceneElementBaseMixin {
    @Inject(method = "lightCoordsFromFade", at = @At("HEAD"), cancellable = true)
    private void slag$darkerFade(float fade, CallbackInfoReturnable<Integer> cir) {
        if (fade >= 1) return;
        var light = (int) Mth.lerp(fade, 3, 15);
        cir.setReturnValue(LightCoordsUtil.pack(0, light));
    }
}