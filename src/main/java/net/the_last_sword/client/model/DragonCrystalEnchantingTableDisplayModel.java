package net.the_last_sword.client.model;

import net.minecraft.resources.ResourceLocation;
import net.the_last_sword.item.display.DragonCrystalEnchantingTableDisplayItem;
import software.bernie.geckolib.model.GeoModel;

public class DragonCrystalEnchantingTableDisplayModel extends GeoModel<DragonCrystalEnchantingTableDisplayItem> {

    @Override
    public ResourceLocation getAnimationResource(DragonCrystalEnchantingTableDisplayItem animatable) {
        return new ResourceLocation("the_last_sword", "animations/dragon_crystal_enchanting_table.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(DragonCrystalEnchantingTableDisplayItem animatable) {
        return new ResourceLocation("the_last_sword", "geo/dragon_crystal_enchanting_table.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DragonCrystalEnchantingTableDisplayItem animatable) {
        return new ResourceLocation("the_last_sword", "textures/block/dragon_crystal_enchanting_table.png");
    }
}
