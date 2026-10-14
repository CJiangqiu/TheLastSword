package net.the_last_sword.compat.projecte.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.the_last_sword.TheLastSwordMod;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.model.GeoModel;

public class StaffOfUniversalTransmutationModel<T extends Item & GeoItem> extends GeoModel<T> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(
        TheLastSwordMod.MOD_ID, "geo/staff_of_universal_transmutation.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        TheLastSwordMod.MOD_ID, "textures/item/staff_of_universal_transmutation.png");
    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(
        TheLastSwordMod.MOD_ID, "animations/staff_of_universal_transmutation.animation.json");

    @Override
    public ResourceLocation getModelResource(T item) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(T item) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(T item) {
        return ANIMATION;
    }
}
