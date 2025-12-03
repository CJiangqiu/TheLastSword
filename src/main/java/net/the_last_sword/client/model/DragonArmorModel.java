package net.the_last_sword.client.model;

import net.minecraft.resources.ResourceLocation;
import net.the_last_sword.item.DragonArmorItem;
import software.bernie.geckolib.model.GeoModel;

public class DragonArmorModel extends GeoModel<DragonArmorItem> {

    @Override
    public ResourceLocation getAnimationResource(DragonArmorItem object) {
        return new ResourceLocation("the_last_sword", "animations/dragon_armor.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(DragonArmorItem object) {
        return new ResourceLocation("the_last_sword", "geo/dragon_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DragonArmorItem object) {
        return new ResourceLocation("the_last_sword", "textures/item/dragon_armor.png");
    }
}
