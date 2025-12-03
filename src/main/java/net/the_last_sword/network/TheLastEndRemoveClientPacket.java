package net.the_last_sword.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.the_last_sword.util.EntityUtil;
import net.the_last_sword.util.TheLastSwordLogger;

import java.util.function.Supplier;

//客户端实体清除网络包
public class TheLastEndRemoveClientPacket {

    private final int entityId;

    public TheLastEndRemoveClientPacket(int entityId) {
        this.entityId = entityId;
    }

    //编码
    public static void encode(TheLastEndRemoveClientPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
    }

    //解码
    public static TheLastEndRemoveClientPacket decode(FriendlyByteBuf buf) {
        return new TheLastEndRemoveClientPacket(buf.readInt());
    }

    //处理（客户端）
    public static void handle(TheLastEndRemoveClientPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            ClientLevel clientLevel = minecraft.level;
            if (clientLevel != null) {
                Entity entity = clientLevel.getEntity(msg.entityId);
                if (entity != null) {
                    //先调用客户端清除回调
                    entity.onClientRemoval();

                    //再执行底层容器清除
                    EntityUtil.removeFromClientContainers(clientLevel, entity);
                    TheLastSwordLogger.debug("Client entity removal executed for entity ID: {}", msg.entityId);
                } else {
                    TheLastSwordLogger.debug("Client entity removal: entity not found (ID: {})", msg.entityId);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
