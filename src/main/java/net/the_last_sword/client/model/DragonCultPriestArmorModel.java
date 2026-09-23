package net.the_last_sword.client.model;

import net.minecraft.resources.ResourceLocation;
import net.the_last_sword.item.DragonCultPriestArmorItem;
import software.bernie.geckolib.model.GeoModel;

public class DragonCultPriestArmorModel extends GeoModel<DragonCultPriestArmorItem> {

    @Override
    public ResourceLocation getAnimationResource(DragonCultPriestArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath("the_last_sword", "animations/dragon_cult_priest_armor.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(DragonCultPriestArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath("the_last_sword", "geo/dragon_cult_priest_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DragonCultPriestArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath("the_last_sword", "textures/item/dragon_cult_priest_armor.png");
    }
}
