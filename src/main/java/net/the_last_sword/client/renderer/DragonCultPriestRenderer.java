package net.the_last_sword.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.the_last_sword.client.layer.DeathRayLayer;
import net.the_last_sword.client.model.DragonCultPriestModel;
import net.the_last_sword.entity.DragonCultPriestEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DragonCultPriestRenderer extends GeoEntityRenderer<DragonCultPriestEntity> {

    //拜龙教紫，替代末影龙原本的品红
    private static final int RAY_RED = 155;
    private static final int RAY_GREEN = 89;
    private static final int RAY_BLUE = 208;

    public DragonCultPriestRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DragonCultPriestModel());
        this.shadowRadius = 0.5f;
    }

    @Override
    public RenderType getRenderType(DragonCultPriestEntity animatable, ResourceLocation texture,
            MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }

    @Override
    public void preRender(PoseStack poseStack, DragonCultPriestEntity entity, BakedGeoModel model,
            MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick,
            int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        float scale = 1.0f;
        this.scaleHeight = scale;
        this.scaleWidth = scale;
        super.preRender(poseStack, entity, model, bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, red, green, blue, alpha);
    }

    //模型渲染完毕后叠加死亡放射光柱
    @Override
    public void render(DragonCultPriestEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        int deathTick = entity.getDeathTick();
        if (deathTick <= 0) {
            return;
        }

        float progress = Math.min((deathTick + partialTick) / DragonCultPriestEntity.DEATH_ANIMATION_TICKS, 1.0f);
        DeathRayLayer.render(poseStack, bufferSource, progress, entity.getBbHeight() * 0.5f,
                RAY_RED, RAY_GREEN, RAY_BLUE);
    }

    @Override
    protected float getDeathMaxRotation(DragonCultPriestEntity entityLivingBaseIn) {
        return 0.0F;
    }

    @Override
    public int getPackedOverlay(DragonCultPriestEntity entity, float u) {
        return LivingEntityRenderer.getOverlayCoords(entity, 0);
    }
}
