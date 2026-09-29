package net.the_last_sword.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.the_last_sword.dialogue.NpcDialogueManager;

import java.util.function.Supplier;

public record NpcDialogueChoicePacket(int optionIndex) {
    public static final int CLOSE_DIALOGUE = -1;

    public static void encode(NpcDialogueChoicePacket message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.optionIndex);
    }

    public static NpcDialogueChoicePacket decode(FriendlyByteBuf buffer) {
        return new NpcDialogueChoicePacket(buffer.readVarInt());
    }

    public static void handle(NpcDialogueChoicePacket message,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        context.enqueueWork(() -> {
            if (player != null) {
                NpcDialogueManager.select(player, message.optionIndex);
            }
        });
        context.setPacketHandled(true);
    }
}
