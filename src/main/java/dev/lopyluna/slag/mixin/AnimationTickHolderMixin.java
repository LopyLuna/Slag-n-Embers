package dev.lopyluna.slag.mixin;

import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnimationTickHolder.class)
public abstract class AnimationTickHolderMixin {
    @Inject(method = "getGuiPartialTicks", at = @At("HEAD"), cancellable = true)
    private static void slag$unpaused(CallbackInfoReturnable<Float> cir) {
        if (Minecraft.getInstance().getDeltaTracker() instanceof DeltaTrackerTimerAccessor timer) cir.setReturnValue(timer.slag$residual());
    }
}
