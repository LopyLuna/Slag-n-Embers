package dev.lopyluna.slag.mixin;

import net.createmod.catnip.api.config.definition.ConfigValue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConfigValue.class)
public abstract class CatnipConfigValueMixin<T> {
    @Shadow @Final public T defaultValue;
    @Shadow private T value;

    @Inject(method = "assertInitialized", at = @At("HEAD"))
    private void slag$initDefault(CallbackInfo ci) {
        if (value == null) value = defaultValue;
    }
}
