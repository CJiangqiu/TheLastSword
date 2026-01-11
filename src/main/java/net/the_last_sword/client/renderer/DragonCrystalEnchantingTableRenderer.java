package net.the_last_sword.client.renderer;

import net.the_last_sword.block.entity.DragonCrystalEnchantingTableBlockEntity;
import net.the_last_sword.client.model.DragonCrystalEnchantingTableModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class DragonCrystalEnchantingTableRenderer extends GeoBlockRenderer<DragonCrystalEnchantingTableBlockEntity> {

    public DragonCrystalEnchantingTableRenderer() {
        super(new DragonCrystalEnchantingTableModel());
    }
}
