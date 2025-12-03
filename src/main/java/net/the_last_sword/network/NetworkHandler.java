package net.the_last_sword.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.the_last_sword.TheLastSwordMod;
import net.minecraft.server.level.ServerPlayer;

//网络包管理器
public class NetworkHandler {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TheLastSwordMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    //注册所有网络包
    public static void register() {
        CHANNEL.messageBuilder(ChangeModePacket.class, id())
                .encoder(ChangeModePacket::encode)
                .decoder(ChangeModePacket::decode)
                .consumerMainThread(ChangeModePacket::handle)
                .add();

        CHANNEL.messageBuilder(TheLastEndRemoveClientPacket.class, id())
                .encoder(TheLastEndRemoveClientPacket::encode)
                .decoder(TheLastEndRemoveClientPacket::decode)
                .consumerMainThread(TheLastEndRemoveClientPacket::handle)
                .add();

        //挖掘预览系统网络包
        CHANNEL.messageBuilder(PreviewBlocksPacket.class, id())
                .encoder(PreviewBlocksPacket::encode)
                .decoder(PreviewBlocksPacket::decode)
                .consumerMainThread(PreviewBlocksPacket::handle)
                .add();

        CHANNEL.messageBuilder(ClearPreviewPacket.class, id())
                .encoder(ClearPreviewPacket::encode)
                .decoder(ClearPreviewPacket::decode)
                .consumerMainThread(ClearPreviewPacket::handle)
                .add();

        CHANNEL.messageBuilder(CancelPreviewPacket.class, id())
                .encoder(CancelPreviewPacket::encode)
                .decoder(CancelPreviewPacket::decode)
                .consumerMainThread(CancelPreviewPacket::handle)
                .add();

        //唤灵GUI系统网络包
        CHANNEL.messageBuilder(OpenSummonGuiPacket.class, id())
                .encoder(OpenSummonGuiPacket::encode)
                .decoder(OpenSummonGuiPacket::decode)
                .consumerMainThread(OpenSummonGuiPacket::handle)
                .add();

        CHANNEL.messageBuilder(SyncSummonGuiPacket.class, id())
                .encoder(SyncSummonGuiPacket::encode)
                .decoder(SyncSummonGuiPacket::decode)
                .consumerMainThread(SyncSummonGuiPacket::handle)
                .add();
    }

    //发送到服务端
    public static <MSG> void sendToServer(MSG message) {
        CHANNEL.sendToServer(message);
    }

    //发送到特定玩家
    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    //发送到追踪该实体的所有玩家（包括单人游戏）
    public static <MSG> void sendToTrackingClients(MSG message, Entity entity) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            CHANNEL.send(
                    PacketDistributor.TRACKING_ENTITY.with(() -> entity),
                    message
            );
        }
    }
}
