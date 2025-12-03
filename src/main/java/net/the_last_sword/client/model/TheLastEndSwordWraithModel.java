package net.the_last_sword.client.model;

import software.bernie.geckolib.model.data.EntityModelData;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.constant.DataTickets;
import net.the_last_sword.entity.TheLastEndSwordWraithEntity;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;

public class TheLastEndSwordWraithModel extends GeoModel<TheLastEndSwordWraithEntity> {
    @Override
    public ResourceLocation getAnimationResource(TheLastEndSwordWraithEntity entity) {
        return new ResourceLocation("the_last_sword", "animations/the_last_end_sword_wraith.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(TheLastEndSwordWraithEntity entity) {
        return new ResourceLocation("the_last_sword", "geo/the_last_end_sword_wraith.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(TheLastEndSwordWraithEntity entity) {
        return new ResourceLocation("the_last_sword", "textures/entities/" + entity.getTexture() + ".png");
    }

    @Override
    public void setCustomAnimations(TheLastEndSwordWraithEntity animatable, long instanceId, AnimationState animationState) {
        CoreGeoBone head = getAnimationProcessor().getBone("head");
        if (head != null) {
            EntityModelData entityData = (EntityModelData) animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
            head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
        }

    }
}
