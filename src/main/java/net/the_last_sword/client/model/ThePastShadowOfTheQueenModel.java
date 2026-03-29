package net.the_last_sword.client.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.the_last_sword.entity.ThePastShadowOfTheQueenEntity;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class ThePastShadowOfTheQueenModel extends GeoModel<ThePastShadowOfTheQueenEntity> {
    @Override
    public ResourceLocation getAnimationResource(ThePastShadowOfTheQueenEntity entity) {
        return new ResourceLocation("the_last_sword", "animations/the_past_shadow_of_the_queen.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(ThePastShadowOfTheQueenEntity entity) {
        return new ResourceLocation("the_last_sword", "geo/the_past_shadow_of_the_queen.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ThePastShadowOfTheQueenEntity entity) {
        return new ResourceLocation("the_last_sword", "textures/entities/the_past_shadow_of_the_queen.png");
    }

    @Override
    public void setCustomAnimations(ThePastShadowOfTheQueenEntity animatable, long instanceId, AnimationState animationState) {
        CoreGeoBone head = getAnimationProcessor().getBone("head");
        if (head != null) {
            EntityModelData entityData = (EntityModelData) animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
            head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
        }
    }
}
