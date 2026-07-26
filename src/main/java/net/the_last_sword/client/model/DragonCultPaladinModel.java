package net.the_last_sword.client.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.the_last_sword.entity.DragonCultPaladinEntity;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class DragonCultPaladinModel extends GeoModel<DragonCultPaladinEntity> {

    @Override
    public ResourceLocation getAnimationResource(DragonCultPaladinEntity entity) {
        return new ResourceLocation("the_last_sword", "animations/dragon_cult_paladin.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(DragonCultPaladinEntity entity) {
        return new ResourceLocation("the_last_sword", "geo/dragon_cult_paladin.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DragonCultPaladinEntity entity) {
        return new ResourceLocation("the_last_sword", "textures/entities/dragon_cult_paladin.png");
    }

    @Override
    public void setCustomAnimations(DragonCultPaladinEntity animatable, long instanceId, AnimationState animationState) {
        CoreGeoBone head = getAnimationProcessor().getBone("head");
        if (head != null) {
            EntityModelData entityData = (EntityModelData) animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
            head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
        }
    }
}
