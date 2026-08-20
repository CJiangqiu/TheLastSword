package net.the_last_sword.client.renderer;

import net.the_last_sword.client.model.DragonCultPaladinArmorModel;
import net.the_last_sword.item.DragonCultPaladinArmorItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class DragonCultPaladinArmorRenderer extends GeoArmorRenderer<DragonCultPaladinArmorItem> {

    public DragonCultPaladinArmorRenderer() {
        super(new DragonCultPaladinArmorModel());
    }
}
