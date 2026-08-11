package net.the_last_sword.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;

//死亡放射光柱 - 复刻末影龙死亡特效的几何生成（EnderDragonRenderer）
public class DeathRayLayer {

    //三角锥底边半宽系数
    private static final float HALF_SQRT_3 = (float) (Math.sqrt(3.0) / 2.0);

    //固定种子保证每帧光柱朝向一致
    private static final long RAY_SEED = 432L;

    //光柱数量上限
    private static final float MAX_RAY_COUNT = 60.0f;

    //进度超过该值后开始淡出
    private static final float FADE_START = 0.8f;

    private DeathRayLayer() {}

    //按死亡进度渲染放射光柱，progress 为 0~1
    public static void render(PoseStack poseStack, MultiBufferSource bufferSource,
                              float progress, float heightOffset, int red, int green, int blue) {
        if (progress <= 0.0f) {
            return;
        }

        float fade = Math.min(progress > FADE_START ? (progress - FADE_START) / (1.0f - FADE_START) : 0.0f, 1.0f);
        RandomSource random = RandomSource.create(RAY_SEED);
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.lightning());

        poseStack.pushPose();
        poseStack.translate(0.0f, heightOffset, 0.0f);

        int rayCount = (int) ((progress + progress * progress) / 2.0f * MAX_RAY_COUNT);
        int alpha = (int) (255.0f * (1.0f - fade));

        for (int i = 0; i < rayCount; i++) {
            poseStack.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0f));
            poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0f));
            poseStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0f));
            poseStack.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0f));
            poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0f));
            poseStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0f + progress * 90.0f));

            float length = random.nextFloat() * 20.0f + 5.0f + fade * 10.0f;
            float width = random.nextFloat() * 2.0f + 1.0f + fade * 2.0f;
            Matrix4f pose = poseStack.last().pose();

            center(buffer, pose, alpha);
            cornerA(buffer, pose, length, width, red, green, blue);
            cornerB(buffer, pose, length, width, red, green, blue);

            center(buffer, pose, alpha);
            cornerB(buffer, pose, length, width, red, green, blue);
            cornerC(buffer, pose, length, width, red, green, blue);

            center(buffer, pose, alpha);
            cornerC(buffer, pose, length, width, red, green, blue);
            cornerA(buffer, pose, length, width, red, green, blue);
        }

        poseStack.popPose();
    }

    //光柱根部，白色按进度淡出
    private static void center(VertexConsumer buffer, Matrix4f pose, int alpha) {
        buffer.vertex(pose, 0.0f, 0.0f, 0.0f).color(255, 255, 255, alpha).endVertex();
    }

    private static void cornerA(VertexConsumer buffer, Matrix4f pose, float length, float width,
                                int red, int green, int blue) {
        buffer.vertex(pose, -HALF_SQRT_3 * width, length, -0.5f * width).color(red, green, blue, 0).endVertex();
    }

    private static void cornerB(VertexConsumer buffer, Matrix4f pose, float length, float width,
                                int red, int green, int blue) {
        buffer.vertex(pose, HALF_SQRT_3 * width, length, -0.5f * width).color(red, green, blue, 0).endVertex();
    }

    private static void cornerC(VertexConsumer buffer, Matrix4f pose, float length, float width,
                                int red, int green, int blue) {
        buffer.vertex(pose, 0.0f, length, 1.0f * width).color(red, green, blue, 0).endVertex();
    }
}
