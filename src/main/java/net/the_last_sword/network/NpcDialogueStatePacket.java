package net.the_last_sword.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.the_last_sword.client.ClientPacketHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class NpcDialogueStatePacket {
    private static final int MAX_ID_LENGTH = 128;
    private static final int MAX_READ_OPTION_COUNT = 256;
    private final int entityId;
    private final String dialogueId;
    private final String nodeId;
    private final Set<String> readOptionIds;
    private final boolean close;

    private NpcDialogueStatePacket(int entityId, String dialogueId, String nodeId,
                                   Set<String> readOptionIds, boolean close) {
        this.entityId = entityId;
        this.dialogueId = dialogueId;
        this.nodeId = nodeId;
        this.readOptionIds = Set.copyOf(readOptionIds);
        this.close = close;
    }

    public static NpcDialogueStatePacket open(int entityId, String dialogueId, String nodeId,
                                              Set<String> readOptionIds) {
        return new NpcDialogueStatePacket(entityId, dialogueId, nodeId, readOptionIds, false);
    }

    public static NpcDialogueStatePacket close() {
        return new NpcDialogueStatePacket(-1, "", "", Set.of(), true);
    }

    public static void encode(NpcDialogueStatePacket message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.close);
        if (!message.close) {
            buffer.writeVarInt(message.entityId);
            buffer.writeUtf(message.dialogueId, MAX_ID_LENGTH);
            buffer.writeUtf(message.nodeId, MAX_ID_LENGTH);
            buffer.writeVarInt(message.readOptionIds.size());
            for (String optionId : message.readOptionIds) {
                buffer.writeUtf(optionId, MAX_ID_LENGTH);
            }
        }
    }

    public static NpcDialogueStatePacket decode(FriendlyByteBuf buffer) {
        if (buffer.readBoolean()) {
            return close();
        }
        int entityId = buffer.readVarInt();
        String dialogueId = buffer.readUtf(MAX_ID_LENGTH);
        String nodeId = buffer.readUtf(MAX_ID_LENGTH);
        int readOptionCount = buffer.readVarInt();
        if (readOptionCount < 0 || readOptionCount > MAX_READ_OPTION_COUNT) {
            throw new IllegalArgumentException("Invalid read dialogue option count: " + readOptionCount);
        }
        Set<String> readOptionIds = new HashSet<>();
        for (int i = 0; i < readOptionCount; i++) {
            readOptionIds.add(buffer.readUtf(MAX_ID_LENGTH));
        }
        return open(entityId, dialogueId, nodeId, readOptionIds);
    }

    public static void handle(NpcDialogueStatePacket message,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientPacketHandler.updateNpcDialogue(
                message.entityId, message.dialogueId, message.nodeId,
                message.readOptionIds, message.close));
        context.setPacketHandled(true);
    }
}
