package net.the_last_sword.client.model;

import net.minecraft.resources.ResourceLocation;
import net.the_last_sword.block.entity.DragonCrystalEnchantingTableBlockEntity;
import software.bernie.geckolib.model.GeoModel;

public class DragonCrystalEnchantingTableModel extends GeoModel<DragonCrystalEnchantingTableBlockEntity> {

    @Override
    public ResourceLocation getAnimationResource(DragonCrystalEnchantingTableBlockEntity animatable) {
        return new ResourceLocation("the_last_sword", "animations/dragon_crystal_enchanting_table.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(DragonCrystalEnchantingTableBlockEntity animatable) {
        return new ResourceLocation("the_last_sword", "geo/dragon_crystal_enchanting_table.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DragonCrystalEnchantingTableBlockEntity animatable) {
        return new ResourceLocation("the_last_sword", "textures/block/dragon_crystal_enchanting_table.png");
    }
}
