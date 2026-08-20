package net.the_last_sword.client.renderer;

import net.the_last_sword.client.model.DragonCultistArmorModel;
import net.the_last_sword.item.DragonCultistArmorItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class DragonCultistArmorRenderer extends GeoArmorRenderer<DragonCultistArmorItem> {

    public DragonCultistArmorRenderer() {
        super(new DragonCultistArmorModel());
    }
}
