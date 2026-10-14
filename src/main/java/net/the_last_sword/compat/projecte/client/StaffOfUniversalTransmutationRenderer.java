package net.the_last_sword.compat.projecte.client;

import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class StaffOfUniversalTransmutationRenderer<T extends Item & GeoItem> extends GeoItemRenderer<T> {
    public StaffOfUniversalTransmutationRenderer() {
        super(new StaffOfUniversalTransmutationModel<>());
    }
}
