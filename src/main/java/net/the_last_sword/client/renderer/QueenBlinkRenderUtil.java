package net.the_last_sword.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.util.QueenSummonRiftPlacement;
import net.the_last_sword.TheLastSwordMod;
import org.joml.Matrix4f;

public final class QueenBlinkRenderUtil {
    private static final int BLADE_SEGMENTS = 24;
    private static final RenderType GLOW = type("queen_blink_glow", true);
    private static final RenderType DARK = type("queen_blink_rift", false);
    private static final RenderType RIFT = RenderType.entityTranslucent(
            ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID, "textures/entities/queen_rift.png"));

    private QueenBlinkRenderUtil() {}

    private static RenderType type(String name, boolean glow) {
        return RenderType.create(name, DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS,
                4096, false, true, RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                        .setTransparencyState(glow ? RenderStateShard.LIGHTNING_TRANSPARENCY : RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        .setCullState(RenderStateShard.NO_CULL).createCompositeState(false));
    }

    public static void rift(PoseStack pose, MultiBufferSource buffers, float age, float yaw) {
        if (age < 5 || age >= 50) return;
        float opening = Mth.clamp((age - 5) / 10, 0, 1);
        float fade = Mth.clamp((50 - age) / 5, 0, 1);
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));
        pose.translate(0, 1.75, 1);
        drawRift(pose, buffers, opening, fade, false);
        pose.popPose();
    }

    public static void summonRift(PoseStack pose, MultiBufferSource buffers, float age, float yaw) {
        if (age < 20 || age >= 150) return;
        float opening = Mth.clamp((age - 20) / 10, 0, 1);
        float fade = Mth.clamp((150 - age) / 10, 0, 1);
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));
        Vec3 center = QueenSummonRiftPlacement.LOCAL_CENTER;
        pose.translate(center.x, center.y, center.z);
        pose.mulPose(Axis.XP.rotationDegrees(90));
        drawRift(pose, buffers, opening, fade, true);
        pose.popPose();
    }

    private static void drawRift(PoseStack pose, MultiBufferSource buffers, float opening, float fade,
                                 boolean centered) {
        if (opening <= 0 || fade <= 0) return;
        float right = centered ? 1.6F * opening : 1.6F;
        float left = right - 3.2F * opening;
        float height = 1.4F * opening * fade;
        VertexConsumer buffer = buffers.getBuffer(RIFT);
        // 非加色透明混合保留黑色内芯，完整 UV 随几何缩放形成开合。
        riftVertex(pose, buffer, left, -height, 0, 1, fade);
        riftVertex(pose, buffer, right, -height, 1, 1, fade);
        riftVertex(pose, buffer, right, height, 1, 0, fade);
        riftVertex(pose, buffer, left, height, 0, 0, fade);
    }

    private static void riftVertex(PoseStack pose, VertexConsumer buffer, float x, float y,
                                   float u, float v, float alpha) {
        buffer.vertex(pose.last().pose(), x, y, 0).color(1F, 1F, 1F, alpha).uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                .normal(pose.last().normal(), 0, 0, 1).endVertex();
    }

    public static void blade(PoseStack pose, MultiBufferSource buffers, float width, float height, float length) {
        blade(pose, buffers, width, height, length, false);
    }

    public static void blade(PoseStack pose, MultiBufferSource buffers, float width, float height, float length,
                             boolean enhanced) {
        Matrix4f matrix = pose.last().pose();
        // 单一连续面避免透明薄片相互叠色，保留清晰的剑气轮廓。
        VertexConsumer core = buffers.getBuffer(DARK);
        for (int i = 0; i < BLADE_SEGMENTS; i++) {
            float p = i / (float) BLADE_SEGMENTS;
            float q = (i + 1) / (float) BLADE_SEGMENTS;
            float a = Mth.sin(p * Mth.PI);
            float b = Mth.sin(q * Mth.PI);
            float halfWidth0 = width * a * 0.5F;
            float halfWidth1 = width * b * 0.5F;
            float z0 = (a - 0.5F) * length;
            float z1 = (b - 0.5F) * length;
            quad(matrix, core, -halfWidth0, p * height, z0, halfWidth0, p * height, z0,
                    halfWidth1, q * height, z1, -halfWidth1, q * height, z1,
                    enhanced ? 0.055F : 0.18F, enhanced ? 0.008F : 0.025F,
                    enhanced ? 0.09F : 0.32F, enhanced ? 0.82F : 0.72F);
        }

        // 两条窄亮边勾勒整体形状，不再用多层几何模拟厚度。
        VertexConsumer glow = buffers.getBuffer(GLOW);
        float edgeWidth = Math.max(0.035F, width * 0.1F);
        for (int i = 0; i < BLADE_SEGMENTS; i++) {
            float p = i / (float) BLADE_SEGMENTS;
            float q = (i + 1) / (float) BLADE_SEGMENTS;
            float a = Mth.sin(p * Mth.PI);
            float b = Mth.sin(q * Mth.PI);
            float left0 = -width * a * 0.5F;
            float left1 = -width * b * 0.5F;
            float right0 = -left0;
            float right1 = -left1;
            float inset0 = Math.min(edgeWidth, width * a * 0.25F);
            float inset1 = Math.min(edgeWidth, width * b * 0.25F);
            float z0 = (a - 0.5F) * length;
            float z1 = (b - 0.5F) * length;
            float red = enhanced ? 0.72F : 0.82F;
            float green = enhanced ? 0.1F : 0.38F;
            float alpha = enhanced ? 0.72F : 0.58F;
            quad(matrix, glow, left0, p * height, z0, left0 + inset0, p * height, z0,
                    left1 + inset1, q * height, z1, left1, q * height, z1,
                    red, green, 1F, alpha);
            quad(matrix, glow, right0 - inset0, p * height, z0, right0, p * height, z0,
                    right1, q * height, z1, right1 - inset1, q * height, z1,
                    red, green, 1F, alpha);
        }
    }

    private static void quad(Matrix4f matrix, VertexConsumer consumer,
                             float x0, float y0, float z0, float x1, float y1, float z1,
                             float x2, float y2, float z2, float x3, float y3, float z3,
                             float r, float g, float b, float a) {
        consumer.vertex(matrix, x0, y0, z0).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, x1, y1, z1).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, x2, y2, z2).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, x3, y3, z3).color(r, g, b, a).endVertex();
    }
}
