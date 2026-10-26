package dev.lopyluna.slag.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

@SuppressWarnings("NullableProblems")
public record SolidBufferSource(VertexConsumer d) implements VertexConsumer {
    public static final RenderType SOLID = RenderTypes.solidMovingBlock();

    @Override public VertexConsumer addVertex(float x, float y, float z) { d.addVertex(x, y, z); return this; }
    @Override public VertexConsumer setColor(int r, int g, int b, int a) { d.setColor(r, g, b, 255); return this; }
    @Override public VertexConsumer setColor(int color) { d.setColor(color | 0xFF000000); return this; }
    @Override public VertexConsumer setUv(float u, float v) { d.setUv(u, v); return this; }
    @Override public VertexConsumer setUv1(int u, int v) { d.setUv1(u, v); return this; }
    @Override public VertexConsumer setUv2(int u, int v) { d.setUv2(u, v); return this; }
    @Override public VertexConsumer setNormal(float x, float y, float z) { d.setNormal(x, y, z); return this; }
    @Override public VertexConsumer setLineWidth(float width) { d.setLineWidth(width); return this; }
}
