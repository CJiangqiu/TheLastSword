package net.the_last_sword.client.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.the_last_sword.entity.DragonCultistEntity;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class DragonCultistModel extends GeoModel<DragonCultistEntity> {

    @Override
    public ResourceLocation getAnimationResource(DragonCultistEntity entity) {
        return new ResourceLocation("the_last_sword", "animations/dragon_cultist.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(DragonCultistEntity entity) {
        return new ResourceLocation("the_last_sword", "geo/dragon_cultist.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DragonCultistEntity entity) {
        return new ResourceLocation("the_last_sword", "textures/entities/dragon_cultist.png");
    }

    @Override
    public void setCustomAnimations(DragonCultistEntity animatable, long instanceId, AnimationState animationState) {
        CoreGeoBone head = getAnimationProcessor().getBone("head");
        if (head != null) {
            EntityModelData entityData = (EntityModelData) animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
            head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
        }
    }
}
