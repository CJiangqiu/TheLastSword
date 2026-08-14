package net.the_last_sword.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.the_last_sword.client.gui.TheLastEndScrollScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

//服务端确认卷轴记录的被毁村庄坐标与已收集纸条后打开客户端GUI
public class OpenLastEndScrollPacket {

    //已收集纸条条目（名字键+内容键）
    public record PaperNoteEntry(String nameKey, String contentKey) {
    }

    private final boolean hasLocation;
    private final int x;
    private final int z;
    private final List<PaperNoteEntry> collectedNotes;

    public OpenLastEndScrollPacket(boolean hasLocation, int x, int z, List<PaperNoteEntry> collectedNotes) {
        this.hasLocation = hasLocation;
        this.x = x;
        this.z = z;
        this.collectedNotes = collectedNotes;
    }

    public static void encode(OpenLastEndScrollPacket message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.hasLocation);
        if (message.hasLocation) {
            buffer.writeInt(message.x);
            buffer.writeInt(message.z);
        }
        buffer.writeCollection(message.collectedNotes, (buf, entry) -> {
            buf.writeUtf(entry.nameKey());
            buf.writeUtf(entry.contentKey());
        });
    }

    public static OpenLastEndScrollPacket decode(FriendlyByteBuf buffer) {
        boolean hasLocation = buffer.readBoolean();
        int x = hasLocation ? buffer.readInt() : 0;
        int z = hasLocation ? buffer.readInt() : 0;
        List<PaperNoteEntry> collectedNotes = buffer.readCollection(ArrayList::new,
            buf -> new PaperNoteEntry(buf.readUtf(), buf.readUtf()));
        return new OpenLastEndScrollPacket(hasLocation, x, z, collectedNotes);
    }

    public static void handle(OpenLastEndScrollPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> Minecraft.getInstance().setScreen(
            new TheLastEndScrollScreen(message.hasLocation, message.x, message.z, message.collectedNotes)
        ));
        context.setPacketHandled(true);
    }
}
