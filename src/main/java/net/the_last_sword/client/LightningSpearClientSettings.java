package net.the_last_sword.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.configuration.LightningSpearSettings;
import net.the_last_sword.configuration.TheLastSwordConfiguration;

@Mod.EventBusSubscriber(modid = TheLastSwordMod.MOD_ID, value = Dist.CLIENT)
public final class LightningSpearClientSettings {
    private static LightningSpearSettings serverSettings;

    private LightningSpearClientSettings() {
    }

    public static LightningSpearSettings get() {
        return serverSettings == null ? TheLastSwordConfiguration.getLightningSpearSettings() : serverSettings;
    }

    public static void receive(LightningSpearSettings settings) {
        serverSettings = settings;
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        serverSettings = null;
    }
}
