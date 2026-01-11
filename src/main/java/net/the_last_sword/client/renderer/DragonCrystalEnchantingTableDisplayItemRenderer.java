package net.the_last_sword.client.renderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.the_last_sword.client.model.DragonCrystalEnchantingTableDisplayModel;
import net.the_last_sword.item.display.DragonCrystalEnchantingTableDisplayItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class DragonCrystalEnchantingTableDisplayItemRenderer extends GeoItemRenderer<DragonCrystalEnchantingTableDisplayItem> {

    public DragonCrystalEnchantingTableDisplayItemRenderer() {
        super(new DragonCrystalEnchantingTableDisplayModel());
    }

    @Override
    public RenderType getRenderType(DragonCrystalEnchantingTableDisplayItem animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }
}
