package net.the_last_sword.client.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.the_last_sword.entity.TheLastEndSwordWraithEntity;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class TheLastEndSwordWraithModel extends GeoModel<TheLastEndSwordWraithEntity> {
    @Override
    public ResourceLocation getAnimationResource(TheLastEndSwordWraithEntity entity) {
        return entity.getAppearance().getAnimation();
    }

    @Override
    public ResourceLocation getModelResource(TheLastEndSwordWraithEntity entity) {
        return entity.getAppearance().getModel();
    }

    @Override
    public ResourceLocation getTextureResource(TheLastEndSwordWraithEntity entity) {
        return entity.getAppearance().getTexture();
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
