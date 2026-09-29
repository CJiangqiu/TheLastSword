package net.the_last_sword.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.the_last_sword.client.ClientPacketHandler;

import java.util.function.Supplier;

public class NpcDialogueStatePacket {
    private static final int MAX_ID_LENGTH = 128;
    private final int entityId;
    private final String dialogueId;
    private final String nodeId;
    private final boolean close;

    private NpcDialogueStatePacket(int entityId, String dialogueId, String nodeId, boolean close) {
        this.entityId = entityId;
        this.dialogueId = dialogueId;
        this.nodeId = nodeId;
        this.close = close;
    }

    public static NpcDialogueStatePacket open(int entityId, String dialogueId, String nodeId) {
        return new NpcDialogueStatePacket(entityId, dialogueId, nodeId, false);
    }

    public static NpcDialogueStatePacket close() {
        return new NpcDialogueStatePacket(-1, "", "", true);
    }

    public static void encode(NpcDialogueStatePacket message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.close);
        if (!message.close) {
            buffer.writeVarInt(message.entityId);
            buffer.writeUtf(message.dialogueId, MAX_ID_LENGTH);
            buffer.writeUtf(message.nodeId, MAX_ID_LENGTH);
        }
    }

    public static NpcDialogueStatePacket decode(FriendlyByteBuf buffer) {
        if (buffer.readBoolean()) {
            return close();
        }
        return open(buffer.readVarInt(), buffer.readUtf(MAX_ID_LENGTH), buffer.readUtf(MAX_ID_LENGTH));
    }

    public static void handle(NpcDialogueStatePacket message,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientPacketHandler.updateNpcDialogue(
                message.entityId, message.dialogueId, message.nodeId, message.close));
        context.setPacketHandled(true);
    }
}
