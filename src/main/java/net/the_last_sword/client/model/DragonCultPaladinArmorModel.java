package net.the_last_sword.client.model;

import net.minecraft.resources.ResourceLocation;
import net.the_last_sword.item.DragonCultPaladinArmorItem;
import software.bernie.geckolib.model.GeoModel;

public class DragonCultPaladinArmorModel extends GeoModel<DragonCultPaladinArmorItem> {

    @Override
    public ResourceLocation getAnimationResource(DragonCultPaladinArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath("the_last_sword", "animations/dragon_cult_paladin_armor.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(DragonCultPaladinArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath("the_last_sword", "geo/dragon_cult_paladin_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DragonCultPaladinArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath("the_last_sword", "textures/item/dragon_cult_paladin_armor.png");
    }
}
