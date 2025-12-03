package net.the_last_sword.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.the_last_sword.client.shader.TheLastEndEffect;
import net.the_last_sword.client.shader.TheLastEndRenderTypes;
import net.the_last_sword.entity.TheLastEndSwordWraithEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

//终焉剑灵的万物终焉状态着色器渲染层
public class TheLastEndSwordWraithTheLastEndLayer extends GeoRenderLayer<TheLastEndSwordWraithEntity> {

    public TheLastEndSwordWraithTheLastEndLayer(GeoRenderer<TheLastEndSwordWraithEntity> entityRenderer) {
        super(entityRenderer);
    }

    @Override
    public void render(PoseStack poseStack, TheLastEndSwordWraithEntity animatable, BakedGeoModel bakedModel,
                       RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                       float partialTick, int packedLight, int packedOverlay) {

        //只在万物终焉状态下渲染
        if (!animatable.isAllThingsEnd()) {
            return;
        }

        //检查着色器是否可用
        if (!TheLastEndEffect.isAvailable()) {
            return;
        }

        //应用终焉着色器uniforms
        TheLastEndEffect.applyUniforms();

        //获取剑灵的原始纹理
        ResourceLocation texture = getRenderer().getTextureLocation(animatable);

        //使用终焉着色器渲染类型
        RenderType theLastEndRenderType = TheLastEndRenderTypes.createEntityEffect(texture);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(theLastEndRenderType);

        //渲染模型
        getRenderer().reRender(
            bakedModel,
            poseStack,
            bufferSource,
            animatable,
            theLastEndRenderType,
            vertexConsumer,
            partialTick,
            packedLight,
            OverlayTexture.NO_OVERLAY,
            1.0f, 1.0f, 1.0f, 1.0f
        );
    }
}
