package net.the_last_sword.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

//服务器发送给客户端的清除挖掘预览包
public class ClearPreviewPacket {

    public ClearPreviewPacket() {}

    //解码
    public static ClearPreviewPacket decode(FriendlyByteBuf buf) {
        return new ClearPreviewPacket();
    }

    //编码
    public void encode(FriendlyByteBuf buf) {}

    //处理
    public static void handle(ClearPreviewPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> net.the_last_sword.client.ClientPacketHandler.clearPreviews()));
        ctx.get().setPacketHandled(true);
    }
}
