package net.the_last_sword.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.client.shader.TheLastEndEffect;
import net.the_last_sword.client.shader.TheLastEndRenderTypes;
import net.the_last_sword.test.TestEntity;

//测试实体的 The Last End 星空剑刃特效渲染层
public class TestEntityTheLastEndLayer extends RenderLayer<TestEntity, HumanoidModel<TestEntity>> {

    //测试实体纹理（着色器会在黑色区域自动应用星空效果）
    private static final ResourceLocation TEXTURE =
        new ResourceLocation(TheLastSwordMod.MOD_ID, "textures/entity/test_entity.png");

    public TestEntityTheLastEndLayer(RenderLayerParent<TestEntity, HumanoidModel<TestEntity>> renderer) {
        super(renderer);
    }

    @Override
    public void render(
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        int packedLight,
        TestEntity entity,
        float limbSwing,
        float limbSwingAmount,
        float partialTick,
        float ageInTicks,
        float netHeadYaw,
        float headPitch
    ) {
        //检查着色器是否可用
        if (!TheLastEndEffect.isAvailable()) {
            //降级：使用半透明渲染
            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucentCull(TEXTURE));
            this.getParentModel().renderToBuffer(
                poseStack,
                vertexConsumer,
                15728880,  // 最大亮度
                LivingEntityRenderer.getOverlayCoords(entity, 0.0f),
                1.0f, 1.0f, 1.0f, 0.8f  // RGBA (80%透明度)
            );
            return;
        }

        //应用 The Last End uniforms
        TheLastEndEffect.applyUniforms();

        //使用 The Last End 实体效果渲染类型
        RenderType renderType = TheLastEndRenderTypes.createEntityEffect(TEXTURE);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);

        //渲染实体模型
        this.getParentModel().renderToBuffer(
            poseStack,
            vertexConsumer,
            packedLight,  // 使用原始光照
            LivingEntityRenderer.getOverlayCoords(entity, 0.0f),
            1.0f, 1.0f, 1.0f, 1.0f  // RGBA (完全不透明)
        );
    }
}
