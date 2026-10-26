package dev.lopyluna.slag.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.ponder.impl.client.gui.PonderSceneRenderState;
import net.createmod.ponder.impl.client.gui.PonderSceneRenderer;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PonderSceneRenderer.class)
public abstract class PonderSceneRendererMixin {
    @Inject(method = "renderToTexture(Lnet/createmod/ponder/impl/client/gui/PonderSceneRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V", at = @At("HEAD"))
    private void slag$levelLightmap(PonderSceneRenderState state, PoseStack poseStack, CallbackInfo ci) {
        ((GameRendererAccessor) Minecraft.getInstance().gameRenderer).slag$setUiLightmap(false);
    }

    @Inject(method = "renderToTexture(Lnet/createmod/ponder/impl/client/gui/PonderSceneRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V", at = @At("RETURN"))
    private void slag$uiLightmap(PonderSceneRenderState state, PoseStack poseStack, CallbackInfo ci) {
        ((GameRendererAccessor) Minecraft.getInstance().gameRenderer).slag$setUiLightmap(true);
    }
}
