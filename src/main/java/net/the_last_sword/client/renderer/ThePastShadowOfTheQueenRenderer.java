package net.the_last_sword.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.the_last_sword.client.model.ThePastShadowOfTheQueenModel;
import net.the_last_sword.entity.ThePastShadowOfTheQueenEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ThePastShadowOfTheQueenRenderer extends GeoEntityRenderer<ThePastShadowOfTheQueenEntity> {
    public ThePastShadowOfTheQueenRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ThePastShadowOfTheQueenModel());
        this.shadowRadius = 0.5f;
    }

    @Override
    public RenderType getRenderType(ThePastShadowOfTheQueenEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }

    @Override
    public void preRender(PoseStack poseStack, ThePastShadowOfTheQueenEntity entity, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red,
                          float green, float blue, float alpha) {
        float scale = 1.0f;
        this.scaleHeight = scale;
        this.scaleWidth = scale;
        super.preRender(poseStack, entity, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    protected float getDeathMaxRotation(ThePastShadowOfTheQueenEntity entityLivingBaseIn) {
        return 0.0F;
    }

    //受伤变红效果
    @Override
    public int getPackedOverlay(ThePastShadowOfTheQueenEntity entity, float u) {
        return LivingEntityRenderer.getOverlayCoords(entity, 0);
    }
}
