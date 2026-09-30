package net.the_last_sword.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderType.CompositeRenderType;
import net.minecraft.resources.ResourceLocation;

public final class PhasingBufferSource implements MultiBufferSource {
    private final MultiBufferSource delegate;

    private PhasingBufferSource(MultiBufferSource delegate) {
        this.delegate = delegate;
    }

    public static MultiBufferSource wrap(MultiBufferSource source) {
        return source instanceof PhasingBufferSource ? source : new PhasingBufferSource(source);
    }

    @Override
    public VertexConsumer getBuffer(RenderType type) {
        // 名牌、线框与附魔光效使用不同的顶点格式，保留其原有绘制方式。
        if (type.format() != DefaultVertexFormat.NEW_ENTITY) {
            return delegate.getBuffer(type);
        }
        return new PhasingVertexConsumer(delegate.getBuffer(translucentType(type)));
    }

    private static RenderType translucentType(RenderType type) {
        if (!(type instanceof CompositeRenderType composite)) {
            return type;
        }
        ResourceLocation texture = composite.state.textureState.cutoutTexture().orElse(null);
        if (texture == null) {
            return type;
        }
        if (type == RenderType.eyes(texture)) {
            return RenderType.entityTranslucentEmissive(texture);
        }
        // 仅替换标准不透明材质，保留自定义着色器及其渲染状态。
        if (type == RenderType.entitySolid(texture)
                || type == RenderType.entityCutout(texture)
                || type == RenderType.entityCutoutNoCull(texture, true)
                || type == RenderType.entityCutoutNoCull(texture, false)
                || type == RenderType.entityCutoutNoCullZOffset(texture, true)
                || type == RenderType.entityCutoutNoCullZOffset(texture, false)
                || type == RenderType.armorCutoutNoCull(texture)
                || type == RenderType.entitySmoothCutout(texture)
                || type == RenderType.entityDecal(texture)) {
            return RenderType.entityTranslucent(texture);
        }
        return type;
    }

    private record PhasingVertexConsumer(VertexConsumer delegate) implements VertexConsumer {
        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            delegate.vertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            delegate.color(red, green, blue, alpha / 2);
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            delegate.uv(u, v);
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            delegate.overlayCoords(u, v);
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            delegate.uv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            delegate.normal(x, y, z);
            return this;
        }

        @Override
        public void endVertex() {
            delegate.endVertex();
        }

        @Override
        public void defaultColor(int red, int green, int blue, int alpha) {
            delegate.defaultColor(red, green, blue, alpha / 2);
        }

        @Override
        public void unsetDefaultColor() {
            delegate.unsetDefaultColor();
        }
    }
}
