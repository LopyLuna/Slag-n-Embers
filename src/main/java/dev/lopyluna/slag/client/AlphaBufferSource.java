package dev.lopyluna.slag.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.util.Mth;

import javax.annotation.Nonnull;

public class AlphaBufferSource implements MultiBufferSource {
    private final MultiBufferSource parent;
    private final int alphaMul;

    public AlphaBufferSource(MultiBufferSource parent, float alpha01) {
        this.parent = parent;
        this.alphaMul = Mth.clamp((int)(alpha01 * 255f), 0, 255);
    }

    @Override
    public @Nonnull VertexConsumer getBuffer(@Nonnull RenderType type) {
        RenderType rt = (type == Sheets.cutoutBlockSheet()) ? Sheets.translucentItemSheet() : type;
        return new AlphaVertexConsumer(parent.getBuffer(rt), alphaMul);
    }

    @MethodsReturnNonnullByDefault
    private static class AlphaVertexConsumer implements VertexConsumer {
        private final VertexConsumer d;
        private final int aMul;
        AlphaVertexConsumer(VertexConsumer delegate, int aMul) { this.d = delegate; this.aMul = aMul; }
        @Override public VertexConsumer addVertex(float x, float y, float z) { return d.addVertex(x, y, z); }
        @Override public VertexConsumer setColor(int r, int g, int b, int a) { return d.setColor(r, g, b, aMul); }
        @Override public VertexConsumer setUv(float u, float v) { return d.setUv(u, v); }
        @Override public VertexConsumer setUv1(int u, int v) { return d.setUv1(u, v); }
        @Override public VertexConsumer setUv2(int u, int v) { return d.setUv2(u, v); }
        @Override public VertexConsumer setNormal(float x, float y, float z) { return d.setNormal(x, y, z); }
    }
}