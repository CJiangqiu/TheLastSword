package net.the_last_sword.compat.projecte.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.compat.projecte.PrimalSourceArmorItem;
import software.bernie.geckolib.model.GeoModel;

public class PrimalSourceArmorModel<T extends Item & PrimalSourceArmorItem> extends GeoModel<T> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(
            TheLastSwordMod.MOD_ID, "geo/primal_source_armor.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            TheLastSwordMod.MOD_ID, "textures/item/primal_source_armor_1.png");
    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(
            TheLastSwordMod.MOD_ID, "animations/primal_source_armor.animation.json");

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
