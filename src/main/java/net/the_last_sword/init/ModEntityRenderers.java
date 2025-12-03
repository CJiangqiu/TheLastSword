package net.the_last_sword.init;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.client.renderer.*;

//实体渲染器注册
@Mod.EventBusSubscriber(modid = TheLastSwordMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEntityRenderers {
    //注册实体渲染器
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.DRAGON_CRYSTAL_SWORD_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.DRAGON_SWORD_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.DRAGON_LIGHTING.get(), DragonLightingRenderer::new);
        event.registerEntityRenderer(ModEntities.THE_LAST_END_LIGHTING.get(), TheLastEndLightingRenderer::new);
        event.registerEntityRenderer(ModEntities.THE_LAST_END_SWORD_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.TEST_ENTITY.get(), TestEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.THE_LAST_END_SWORD_WRAITH.get(), TheLastEndSwordWraithRenderer::new);
    }
}
