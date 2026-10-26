package dev.lopyluna.slag.mixin;

import net.createmod.ponder.impl.client.gui.PonderUI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PonderUI.class)
public abstract class PonderUIMixin {
    @ModifyVariable(method = "extractScaledRenderState", at = @At("HEAD"), argsOnly = true, name = "partialTicks")
    private float slag$partialTicks(float partialTicks) {
        return PonderUI.getPartialTicks();
    }
}
