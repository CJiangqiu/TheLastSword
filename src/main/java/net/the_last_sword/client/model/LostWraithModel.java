package net.the_last_sword.client.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.the_last_sword.entity.LostWraithEntity;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class LostWraithModel extends GeoModel<LostWraithEntity> {
    @Override
    public ResourceLocation getAnimationResource(LostWraithEntity entity) {
        return new ResourceLocation("the_last_sword", "animations/lost_wraith.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(LostWraithEntity entity) {
        return new ResourceLocation("the_last_sword", "geo/lost_wraith.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(LostWraithEntity entity) {
        return new ResourceLocation("the_last_sword", "textures/entities/lost_wraith.png");
    }

    @Override
    public void setCustomAnimations(LostWraithEntity animatable, long instanceId, AnimationState animationState) {
        CoreGeoBone head = getAnimationProcessor().getBone("head");
        if (head != null) {
            EntityModelData entityData = (EntityModelData) animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
            head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
        }
    }
}
