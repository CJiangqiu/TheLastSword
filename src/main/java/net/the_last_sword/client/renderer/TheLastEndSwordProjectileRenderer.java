package net.the_last_sword.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.the_last_sword.entity.TheLastEndSwordProjectile;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.Deque;

//最终之剑弹射物渲染器 - 末影水晶模型 + 红蓝螺旋拖尾
@OnlyIn(Dist.CLIENT)
public class TheLastEndSwordProjectileRenderer extends EntityRenderer<TheLastEndSwordProjectile> {

    private static final ResourceLocation END_CRYSTAL_LOCATION = new ResourceLocation("textures/entity/end_crystal/end_crystal.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(END_CRYSTAL_LOCATION);
    private static final float SIN_45 = (float) Math.sin(0.7853981633974483);

    //拖尾参数
    private static final float TRAIL_WIDTH = 0.15f;
    private static final int FULL_BRIGHT = 0xF000F0;

    private final ModelPart cube;
    private final ModelPart glass;

    public TheLastEndSwordProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        ModelPart root = context.bakeLayer(ModelLayers.END_CRYSTAL);
        this.glass = root.getChild("glass");
        this.cube = root.getChild("cube");
    }

    @Override
    public void render(TheLastEndSwordProjectile entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        //渲染末影水晶模型
        renderCrystal(entity, partialTicks, poseStack, buffer, packedLight);

        //渲染拖尾
        renderTrail(entity, partialTicks, poseStack, buffer);

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    //渲染末影水晶模型
    private void renderCrystal(TheLastEndSwordProjectile entity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float time = (entity.tickCount + partialTicks) * 3.0f;
        float bobbing = Mth.sin((entity.tickCount + partialTicks) * 0.2f) / 2.0f + 0.5f;
        bobbing = (bobbing * bobbing + bobbing) * 0.4f;
        float yOffset = bobbing - 1.4f;

        VertexConsumer vertexConsumer = buffer.getBuffer(RENDER_TYPE);
        int overlay = OverlayTexture.NO_OVERLAY;

        poseStack.scale(2.0f, 2.0f, 2.0f);
        poseStack.translate(0.0f, -1.0f, 0.0f);

        poseStack.mulPose(Axis.YP.rotationDegrees(time));
        poseStack.translate(0.0f, 1.5f + yOffset / 2.0f, 0.0f);
        poseStack.mulPose(new Quaternionf().setAngleAxis(1.0471976f, SIN_45, 0.0f, SIN_45));
        this.glass.render(poseStack, vertexConsumer, packedLight, overlay);

        poseStack.scale(0.875f, 0.875f, 0.875f);
        poseStack.mulPose(new Quaternionf().setAngleAxis(1.0471976f, SIN_45, 0.0f, SIN_45));
        poseStack.mulPose(Axis.YP.rotationDegrees(time));
        this.glass.render(poseStack, vertexConsumer, packedLight, overlay);

        poseStack.scale(0.875f, 0.875f, 0.875f);
        poseStack.mulPose(new Quaternionf().setAngleAxis(1.0471976f, SIN_45, 0.0f, SIN_45));
        poseStack.mulPose(Axis.YP.rotationDegrees(time));
        this.cube.render(poseStack, vertexConsumer, packedLight, overlay);

        poseStack.popPose();
    }

    //渲染红蓝双螺旋拖尾
    private void renderTrail(TheLastEndSwordProjectile entity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer) {
        Deque<Vec3> trail = entity.getTrailPositions();
        if (trail.size() < 2) return;

        //当前插值位置
        Vec3 currentPos = entity.position();
        Vec3 entityRenderPos = new Vec3(
                Mth.lerp(partialTicks, entity.xOld, entity.getX()),
                Mth.lerp(partialTicks, entity.yOld, entity.getY()),
                Mth.lerp(partialTicks, entity.zOld, entity.getZ())
        );

        Vec3[] positions = trail.toArray(new Vec3[0]);
        int count = positions.length;

        VertexConsumer trailConsumer = buffer.getBuffer(RenderType.LIGHTNING);
        Matrix4f matrix = poseStack.last().pose();

        float time = (entity.tickCount + partialTicks) * 0.3f;

        for (int i = 0; i < count - 1; i++) {
            //进度 0.0（头部）到 1.0（尾部）
            float progress = (float) i / (count - 1);
            float nextProgress = (float) (i + 1) / (count - 1);

            //透明度从头到尾衰减
            float alpha = (1.0f - progress) * 0.6f;
            float nextAlpha = (1.0f - nextProgress) * 0.6f;

            //颜色：红线从红渐变到紫，蓝线从蓝渐变到紫
            float redR = Mth.lerp(progress, 1.0f, 0.6f);
            float redG = Mth.lerp(progress, 0.1f, 0.0f);
            float redB = Mth.lerp(progress, 0.2f, 0.8f);

            float blueR = Mth.lerp(progress, 0.2f, 0.6f);
            float blueG = Mth.lerp(progress, 0.1f, 0.0f);
            float blueB = Mth.lerp(progress, 1.0f, 0.8f);

            float nextRedR = Mth.lerp(nextProgress, 1.0f, 0.6f);
            float nextRedG = Mth.lerp(nextProgress, 0.1f, 0.0f);
            float nextRedB = Mth.lerp(nextProgress, 0.2f, 0.8f);

            float nextBlueR = Mth.lerp(nextProgress, 0.2f, 0.6f);
            float nextBlueG = Mth.lerp(nextProgress, 0.1f, 0.0f);
            float nextBlueB = Mth.lerp(nextProgress, 1.0f, 0.8f);

            //相对于实体渲染位置的偏移
            Vec3 pos = positions[i].subtract(entityRenderPos);
            Vec3 nextPos = positions[i + 1].subtract(entityRenderPos);

            //螺旋偏移：两条线相位差π
            float angle1 = time + progress * 6.0f;
            float angle2 = angle1 + (float) Math.PI;

            float width = TRAIL_WIDTH * (1.0f - progress * 0.5f);

            //红色螺旋线的两个顶点偏移
            float r1OffX = Mth.cos(angle1) * width;
            float r1OffY = Mth.sin(angle1) * width;
            float r1NextOffX = Mth.cos(time + nextProgress * 6.0f) * TRAIL_WIDTH * (1.0f - nextProgress * 0.5f);
            float r1NextOffY = Mth.sin(time + nextProgress * 6.0f) * TRAIL_WIDTH * (1.0f - nextProgress * 0.5f);

            //蓝色螺旋线
            float r2OffX = Mth.cos(angle2) * width;
            float r2OffY = Mth.sin(angle2) * width;
            float r2NextOffX = Mth.cos(time + nextProgress * 6.0f + (float) Math.PI) * TRAIL_WIDTH * (1.0f - nextProgress * 0.5f);
            float r2NextOffY = Mth.sin(time + nextProgress * 6.0f + (float) Math.PI) * TRAIL_WIDTH * (1.0f - nextProgress * 0.5f);

            //红色螺旋四边形
            trailConsumer.vertex(matrix, (float) pos.x + r1OffX, (float) pos.y + r1OffY, (float) pos.z)
                    .color(redR, redG, redB, alpha).uv2(FULL_BRIGHT).endVertex();
            trailConsumer.vertex(matrix, (float) nextPos.x + r1NextOffX, (float) nextPos.y + r1NextOffY, (float) nextPos.z)
                    .color(nextRedR, nextRedG, nextRedB, nextAlpha).uv2(FULL_BRIGHT).endVertex();
            trailConsumer.vertex(matrix, (float) nextPos.x - r1NextOffX, (float) nextPos.y - r1NextOffY, (float) nextPos.z)
                    .color(nextRedR, nextRedG, nextRedB, nextAlpha).uv2(FULL_BRIGHT).endVertex();
            trailConsumer.vertex(matrix, (float) pos.x - r1OffX, (float) pos.y - r1OffY, (float) pos.z)
                    .color(redR, redG, redB, alpha).uv2(FULL_BRIGHT).endVertex();

            //蓝色螺旋四边形
            trailConsumer.vertex(matrix, (float) pos.x + r2OffX, (float) pos.y + r2OffY, (float) pos.z)
                    .color(blueR, blueG, blueB, alpha).uv2(FULL_BRIGHT).endVertex();
            trailConsumer.vertex(matrix, (float) nextPos.x + r2NextOffX, (float) nextPos.y + r2NextOffY, (float) nextPos.z)
                    .color(nextBlueR, nextBlueG, nextBlueB, nextAlpha).uv2(FULL_BRIGHT).endVertex();
            trailConsumer.vertex(matrix, (float) nextPos.x - r2NextOffX, (float) nextPos.y - r2NextOffY, (float) nextPos.z)
                    .color(nextBlueR, nextBlueG, nextBlueB, nextAlpha).uv2(FULL_BRIGHT).endVertex();
            trailConsumer.vertex(matrix, (float) pos.x - r2OffX, (float) pos.y - r2OffY, (float) pos.z)
                    .color(blueR, blueG, blueB, alpha).uv2(FULL_BRIGHT).endVertex();
        }
    }

    @Override
    public ResourceLocation getTextureLocation(TheLastEndSwordProjectile entity) {
        return END_CRYSTAL_LOCATION;
    }
}
