package dev.lopyluna.slag.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import javax.annotation.Nonnull;

public class SolidBufferSource implements MultiBufferSource {
    private final MultiBufferSource parent;

    public SolidBufferSource(MultiBufferSource parent) {
        this.parent = parent;
    }

    @Override
    public @Nonnull VertexConsumer getBuffer(@Nonnull RenderType type) {
        RenderType rt = type;
        if (type == Sheets.cutoutBlockSheet() || type == Sheets.translucentItemSheet() || type == Sheets.translucentCullBlockSheet()) rt = Sheets.solidBlockSheet();
        return new SolidVertexConsumer(parent.getBuffer(rt));
    }

    @MethodsReturnNonnullByDefault
        private record SolidVertexConsumer(VertexConsumer d) implements VertexConsumer {
        @Override public VertexConsumer addVertex(float x, float y, float z) { return d.addVertex(x, y, z); }
        @Override public VertexConsumer setColor(int r, int g, int b, int a) { return d.setColor(r, g, b, 255); }
        @Override public VertexConsumer setUv(float u, float v) { return d.setUv(u, v); }
        @Override public VertexConsumer setUv1(int u, int v) { return d.setUv1(u, v); }
        @Override public VertexConsumer setUv2(int u, int v) { return d.setUv2(u, v); }
        @Override public VertexConsumer setNormal(float x, float y, float z) { return d.setNormal(x, y, z); }
    }
}
