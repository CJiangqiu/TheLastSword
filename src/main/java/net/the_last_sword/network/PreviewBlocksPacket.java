package net.the_last_sword.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

//服务器发送给客户端的挖掘预览方块包
public class PreviewBlocksPacket {
    private final Set<BlockPos> blocks;

    public PreviewBlocksPacket(Set<BlockPos> blocks) {
        this.blocks = new HashSet<>(blocks);
    }

    //解码
    public static PreviewBlocksPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Set<BlockPos> blocks = new HashSet<>();
        for (int i = 0; i < size; i++) {
            blocks.add(buf.readBlockPos());
        }
        return new PreviewBlocksPacket(blocks);
    }

    //编码
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(blocks.size());
        for (BlockPos pos : blocks) {
            buf.writeBlockPos(pos);
        }
    }

    //处理
    public static void handle(PreviewBlocksPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            //客户端接收：设置预览方块
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != null) {
                net.the_last_sword.event.ClientEventHandler.setMiningPreviewBlocks(msg.blocks);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
