package net.the_last_sword.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

//服务器发送给客户端的竞技场预览方框包
public class ArenaPreviewPacket {
    private final BlockPos minPos;
    private final BlockPos maxPos;

    public ArenaPreviewPacket(BlockPos minPos, BlockPos maxPos) {
        this.minPos = minPos;
        this.maxPos = maxPos;
    }

    //编码
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(minPos);
        buf.writeBlockPos(maxPos);
    }

    //解码
    public static ArenaPreviewPacket decode(FriendlyByteBuf buf) {
        return new ArenaPreviewPacket(buf.readBlockPos(), buf.readBlockPos());
    }

    //处理
    public static void handle(ArenaPreviewPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != null) {
                net.the_last_sword.event.ClientEventHandler.setArenaPreview(msg.minPos, msg.maxPos);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
