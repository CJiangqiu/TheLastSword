package net.the_last_sword.compat.lucky_block;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class TheLastEndLuckyBlockRenderer extends GeoBlockRenderer<TheLastEndLuckyBlockEntity> {

    public TheLastEndLuckyBlockRenderer() {
        super(new TheLastEndLuckyBlockModel());
    }

    //使用Translucent+Cull：保留alpha混合实现Blockbench黑色透视效果，开启背面剔除避免重合面Z-fight
    @Override
    public RenderType getRenderType(TheLastEndLuckyBlockEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucentCull(getTextureLocation(animatable));
    }
}
