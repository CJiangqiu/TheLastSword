package net.the_last_sword.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

//客户端发送给服务器的取消预览包
public class CancelPreviewPacket {

    public CancelPreviewPacket() {}

    //解码
    public static CancelPreviewPacket decode(FriendlyByteBuf buf) {
        return new CancelPreviewPacket();
    }

    //编码
    public void encode(FriendlyByteBuf buf) {}

    //处理
    public static void handle(CancelPreviewPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                //尝试取消玩家的挖掘预览
                net.the_last_sword.event.ServerEventHandler.cancelMiningPreview(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
