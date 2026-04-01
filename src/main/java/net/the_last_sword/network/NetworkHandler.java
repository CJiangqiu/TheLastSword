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

        //防御配置同步网络包
        CHANNEL.messageBuilder(DefenceConfigPacket.class, id())
                .encoder(DefenceConfigPacket::encode)
                .decoder(DefenceConfigPacket::new)
                .consumerMainThread(DefenceConfigPacket::handle)
                .add();

        //附魔应用网络包
        CHANNEL.messageBuilder(EnchantmentApplyPacket.class, id())
                .encoder(EnchantmentApplyPacket::encode)
                .decoder(EnchantmentApplyPacket::decode)
                .consumerMainThread(EnchantmentApplyPacket::handle)
                .add();

        //附魔台能量数据同步包
        CHANNEL.messageBuilder(EnchantingTableDataPacket.class, id())
                .encoder(EnchantingTableDataPacket::encode)
                .decoder(EnchantingTableDataPacket::decode)
                .consumerMainThread(EnchantingTableDataPacket::handle)
                .add();

        //感知扫描结果同步包
        CHANNEL.messageBuilder(PerceptionScanPacket.class, id())
                .encoder(PerceptionScanPacket::encode)
                .decoder(PerceptionScanPacket::decode)
                .consumerMainThread(PerceptionScanPacket::handle)
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
