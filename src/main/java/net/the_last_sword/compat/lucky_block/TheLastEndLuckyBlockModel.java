package net.the_last_sword.compat.lucky_block;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class TheLastEndLuckyBlockModel extends GeoModel<TheLastEndLuckyBlockEntity> {

    @Override
    public ResourceLocation getAnimationResource(TheLastEndLuckyBlockEntity animatable) {
        return new ResourceLocation("the_last_sword", "animations/the_last_end_lucky_block.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(TheLastEndLuckyBlockEntity animatable) {
        return new ResourceLocation("the_last_sword", "geo/the_last_end_lucky_block.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(TheLastEndLuckyBlockEntity animatable) {
        return new ResourceLocation("the_last_sword", "textures/block/the_last_end_lucky_block.png");
    }
}
