package net.thelastsword.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.thelastsword.client.render.TheLastSwordRender;
import net.thelastsword.client.shader.TheLastSwordShaders;

@Mod.EventBusSubscriber(modid = "the_last_sword", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TheLastSwordClient {
    @SubscribeEvent
    public static void clientSetUp(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new TheLastSwordRender());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRegisterShaders(RegisterShadersEvent event) {
        TheLastSwordShaders.onRegisterShaders(event);
    }
}
