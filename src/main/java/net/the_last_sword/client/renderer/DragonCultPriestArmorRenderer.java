package net.the_last_sword.client.renderer;

import net.the_last_sword.client.model.DragonCultPriestArmorModel;
import net.the_last_sword.item.DragonCultPriestArmorItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class DragonCultPriestArmorRenderer extends GeoArmorRenderer<DragonCultPriestArmorItem> {

    public DragonCultPriestArmorRenderer() {
        super(new DragonCultPriestArmorModel());
    }
}
