package net.thelastsword.init;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.thelastsword.client.render.*;
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class TheLastSwordModEntityRenderers {
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(TheLastSwordModEntities.DRAGON_CRYSTAL_SWORD_PROJECTILE.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(TheLastSwordModEntities.DRAGON_SWORD_PROJECTILE.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(TheLastSwordModEntities.THE_LAST_SWORD_POJECTILE.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(TheLastSwordModEntities.TEST_ENTITY.get(), TestEntityRenderer::new);

	}
}
