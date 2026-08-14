package net.the_last_sword.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.the_last_sword.item.PaperNote;

import java.util.function.Supplier;

//玩家点击「我已知晓」，服务端登记收集
public class ConfirmPaperNotePacket {

    private final String noteId;

    public ConfirmPaperNotePacket(String noteId) {
        this.noteId = noteId;
    }

    public static void encode(ConfirmPaperNotePacket message, FriendlyByteBuf buffer) {
        buffer.writeUtf(message.noteId);
    }

    public static ConfirmPaperNotePacket decode(FriendlyByteBuf buffer) {
        return new ConfirmPaperNotePacket(buffer.readUtf());
    }

    public static void handle(ConfirmPaperNotePacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && PaperNote.isValidNoteId(message.noteId)) {
                PaperNote.markCollected(player, message.noteId);
            }
        });
        context.setPacketHandled(true);
    }
}
