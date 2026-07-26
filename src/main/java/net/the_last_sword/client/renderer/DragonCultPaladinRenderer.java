package net.the_last_sword.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.the_last_sword.client.model.DragonCultPaladinModel;
import net.the_last_sword.entity.DragonCultPaladinEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DragonCultPaladinRenderer extends GeoEntityRenderer<DragonCultPaladinEntity> {

    public DragonCultPaladinRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DragonCultPaladinModel());
        this.shadowRadius = 0.6f;
    }

    @Override
    public RenderType getRenderType(DragonCultPaladinEntity animatable, ResourceLocation texture,
            MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }

    @Override
    public void preRender(PoseStack poseStack, DragonCultPaladinEntity entity, BakedGeoModel model,
            MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick,
            int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        float scale = 1.0f;
        this.scaleHeight = scale;
        this.scaleWidth = scale;
        super.preRender(poseStack, entity, model, bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    protected float getDeathMaxRotation(DragonCultPaladinEntity entityLivingBaseIn) {
        return 0.0F;
    }

    @Override
    public int getPackedOverlay(DragonCultPaladinEntity entity, float u) {
        return LivingEntityRenderer.getOverlayCoords(entity, 0);
    }
}
