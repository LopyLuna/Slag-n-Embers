package dev.lopyluna.slag.mixin;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.annotation.Nullable;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {
    @Accessor("cullingFrustum")
    Frustum slag$getCullingFrustum();
    @Nullable
    @Accessor("capturedFrustum")
    Frustum slag$getCapturedFrustum();
}
