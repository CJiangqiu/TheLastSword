package net.the_last_sword.client.layer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.entity.LostWraithEntity;
import net.the_last_sword.entity.TheLastEndEntity;
import org.joml.Matrix4f;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtils;

public final class LostWraithSealLayer extends GeoRenderLayer<LostWraithEntity> {
    private static final int LINKS = 12;
    private static final int SEGMENTS = 12;
    private static final int SIDES = 4;
    private static final Vec3[][] LINK_VERTICES = createLinks();
    private static final RenderType SEAL = RenderType.create(
            "lost_wraith_seal", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS,
            32768, false, true, RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .setCullState(RenderStateShard.NO_CULL).createCompositeState(false));

    public LostWraithSealLayer(GeoRenderer<LostWraithEntity> renderer) {
        super(renderer);
    }

    @Override
    public void renderForBone(PoseStack poseStack, LostWraithEntity entity, GeoBone bone,
            RenderType renderType, MultiBufferSource buffers, VertexConsumer buffer,
            float partialTick, int packedLight, int packedOverlay) {
        // 激活动画开始即解除封印，不等待战斗就绪状态。
        if (entity.getAnimationState() != TheLastEndEntity.STATE_UNSPAWNED || bone.isHidden()) {
            return;
        }
        boolean left = "left_hand".equals(bone.getName());
        if (!left && !"right_hand".equals(bone.getName())) {
            return;
        }

        poseStack.pushPose();
        RenderUtils.translateToPivotPoint(poseStack, bone);
        // 模型两侧前臂的枢轴不对称，分别对齐护腕中心，避免链环穿入手臂。
        poseStack.translate(left ? -1.0 / 16.0 : 2.8 / 16.0, -6.0 / 16.0,
                left ? -2.5 / 16.0 : 0.0);
        Matrix4f pose = poseStack.last().pose();
        VertexConsumer vertices = buffers.getBuffer(SEAL);
        float pulse = 0.88F + 0.12F * Mth.sin((entity.tickCount + partialTick) * 0.07F);
        for (Vec3[] link : LINK_VERTICES) {
            for (int segment = 0; segment < SEGMENTS; segment++) {
                for (int side = 0; side < SIDES; side++) {
                    float shade = side == 0 ? 1.0F : side == 2 ? 0.55F : 0.78F;
                    int nextSegment = (segment + 1) % SEGMENTS;
                    int nextSide = (side + 1) % SIDES;
                    vertex(vertices, pose, link[segment * SIDES + side], shade, pulse);
                    vertex(vertices, pose, link[nextSegment * SIDES + side], shade, pulse);
                    vertex(vertices, pose, link[nextSegment * SIDES + nextSide], shade, pulse);
                    vertex(vertices, pose, link[segment * SIDES + nextSide], shade, pulse);
                }
            }
        }
        poseStack.popPose();
        // 骨骼递归还会继续使用原模型缓冲，切回其顶点格式后再交还渲染流程。
        if (renderType != null) {
            buffers.getBuffer(renderType);
        }
    }

    private static void vertex(VertexConsumer buffer, Matrix4f pose, Vec3 point, float shade, float pulse) {
        buffer.vertex(pose, (float) point.x, (float) point.y, (float) point.z)
                .color(0.72F * shade * pulse, 0.22F * shade * pulse, shade * pulse, 0.9F)
                .endVertex();
    }

    private static Vec3[][] createLinks() {
        Vec3[][] links = new Vec3[LINKS][SEGMENTS * SIDES];
        for (int link = 0; link < LINKS; link++) {
            double angle = Math.PI * 2 * link / LINKS;
            Vec3 radial = new Vec3(Math.cos(angle), 0, Math.sin(angle));
            Vec3 tangent = new Vec3(-Math.sin(angle), 0, Math.cos(angle));
            double tilt = link % 2 == 0 ? Math.PI / 4 : -Math.PI / 4;
            Vec3 across = new Vec3(0, Math.cos(tilt), 0).add(radial.scale(Math.sin(tilt)));
            Vec3 normal = tangent.cross(across);
            Vec3 center = radial.scale(0.34);
            for (int segment = 0; segment < SEGMENTS; segment++) {
                double phase = Math.PI * 2 * segment / SEGMENTS;
                Vec3 ring = center.add(tangent.scale(0.115 * Math.cos(phase)))
                        .add(across.scale(0.065 * Math.sin(phase)));
                Vec3 outward = tangent.scale(Math.cos(phase) / 0.115)
                        .add(across.scale(Math.sin(phase) / 0.065)).normalize();
                for (int side = 0; side < SIDES; side++) {
                    double tube = Math.PI * 2 * side / SIDES;
                    links[link][segment * SIDES + side] = ring
                            .add(outward.scale(0.014 * Math.cos(tube)))
                            .add(normal.scale(0.014 * Math.sin(tube)));
                }
            }
        }
        return links;
    }
}
