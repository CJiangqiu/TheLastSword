package net.the_last_sword.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.configuration.LightningSpearSettings;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.network.LightningSpearConfigPacket;
import net.the_last_sword.network.NetworkHandler;

@Mod.EventBusSubscriber(modid = TheLastSwordMod.MOD_ID)
public final class LightningSpearConfigSync {
    private static LightningSpearSettings lastSettings;
    private static int ticks;

    private LightningSpearConfigSync() {
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            NetworkHandler.sendToPlayer(new LightningSpearConfigPacket(
                TheLastSwordConfiguration.getLightningSpearSettings()), player);
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ++ticks < 20) {
            return;
        }
        ticks = 0;
        LightningSpearSettings settings = TheLastSwordConfiguration.getLightningSpearSettings();
        if (settings.equals(lastSettings)) {
            return;
        }
        lastSettings = settings;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                NetworkHandler.sendToPlayer(new LightningSpearConfigPacket(settings), player);
            }
        }
    }

    @SubscribeEvent
    public static void onStop(ServerStoppedEvent event) {
        lastSettings = null;
        ticks = 0;
    }
}
