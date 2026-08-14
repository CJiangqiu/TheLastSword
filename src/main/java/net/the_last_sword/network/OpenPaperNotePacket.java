package net.the_last_sword.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.the_last_sword.client.gui.PaperNoteScreen;

import java.util.function.Supplier;

//服务端确认纸条信息后打开客户端阅读GUI
public class OpenPaperNotePacket {

    private final String noteId;
    private final String nameKey;
    private final String guiContentKey;
    private final boolean collected;

    public OpenPaperNotePacket(String noteId, String nameKey, String guiContentKey, boolean collected) {
        this.noteId = noteId;
        this.nameKey = nameKey;
        this.guiContentKey = guiContentKey;
        this.collected = collected;
    }

    public static void encode(OpenPaperNotePacket message, FriendlyByteBuf buffer) {
        buffer.writeUtf(message.noteId);
        buffer.writeUtf(message.nameKey);
        buffer.writeUtf(message.guiContentKey);
        buffer.writeBoolean(message.collected);
    }

    public static OpenPaperNotePacket decode(FriendlyByteBuf buffer) {
        return new OpenPaperNotePacket(
            buffer.readUtf(),
            buffer.readUtf(),
            buffer.readUtf(),
            buffer.readBoolean()
        );
    }

    public static void handle(OpenPaperNotePacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> Minecraft.getInstance().setScreen(new PaperNoteScreen(
            message.noteId,
            message.nameKey,
            message.guiContentKey,
            message.collected
        )));
        context.setPacketHandled(true);
    }
}
