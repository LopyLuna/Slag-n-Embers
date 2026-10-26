package dev.lopyluna.slag.mixin;

import net.createmod.ponder.api.client.scene.PonderScene;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PonderScene.SceneTransform.class)
public abstract class PonderSceneTransformMixin {
    @Shadow private double offset;
    @Shadow private int guiScale;

    @Inject(method = "updateScreenParams", at = @At("TAIL"))
    private void slag$pixelOffset(int width, int height, double offset, int guiScale, CallbackInfo ci) {
        this.offset *= this.guiScale;
    }
}
