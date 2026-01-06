package net.the_last_sword.client.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.the_last_sword.entity.GuardianOfSealedSpireEntity;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class GuardianOfSealedSpireModel extends GeoModel<GuardianOfSealedSpireEntity> {

    @Override
    public ResourceLocation getAnimationResource(GuardianOfSealedSpireEntity entity) {
        return new ResourceLocation("the_last_sword", "animations/guardian_of_sealed_spire.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(GuardianOfSealedSpireEntity entity) {
        return new ResourceLocation("the_last_sword", "geo/guardian_of_sealed_spire.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GuardianOfSealedSpireEntity entity) {
        return new ResourceLocation("the_last_sword", "textures/entities/guardian_of_sealed_spire.png");
    }

    @Override
    public void setCustomAnimations(GuardianOfSealedSpireEntity animatable, long instanceId, AnimationState animationState) {
        CoreGeoBone head = getAnimationProcessor().getBone("head");
        if (head != null) {
            EntityModelData entityData = (EntityModelData) animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
            head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
        }
    }
}
