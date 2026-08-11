package net.the_last_sword.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.the_last_sword.client.layer.DragonCultistItemLayer;
import net.the_last_sword.client.model.DragonCultistModel;
import net.the_last_sword.entity.DragonCultistEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DragonCultistRenderer extends GeoEntityRenderer<DragonCultistEntity> {

    public DragonCultistRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DragonCultistModel());
        this.shadowRadius = 0.5f;
        this.addRenderLayer(new DragonCultistItemLayer(this));
    }

    @Override
    public RenderType getRenderType(DragonCultistEntity animatable, ResourceLocation texture,
            MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }

    @Override
    public void preRender(PoseStack poseStack, DragonCultistEntity entity, BakedGeoModel model,
            MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick,
            int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        float scale = 1.0f;
        this.scaleHeight = scale;
        this.scaleWidth = scale;
        super.preRender(poseStack, entity, model, bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    protected float getDeathMaxRotation(DragonCultistEntity entityLivingBaseIn) {
        return 0.0F;
    }

    @Override
    public int getPackedOverlay(DragonCultistEntity entity, float u) {
        return LivingEntityRenderer.getOverlayCoords(entity, 0);
    }
}
