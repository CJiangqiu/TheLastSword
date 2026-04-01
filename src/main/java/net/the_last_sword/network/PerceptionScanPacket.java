package net.the_last_sword.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.the_last_sword.client.PerceptionScanData;
import net.the_last_sword.client.PerceptionScanData.ScanType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

// 感知扫描结果同步包（服务端→客户端）
public class PerceptionScanPacket {

    private final Map<Integer, ScanType> scannedEntities;
    private final int glowDurationSeconds;

    public PerceptionScanPacket(Map<Integer, ScanType> scannedEntities, int glowDurationSeconds) {
        this.scannedEntities = scannedEntities;
        this.glowDurationSeconds = glowDurationSeconds;
    }

    public static void encode(PerceptionScanPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.glowDurationSeconds);
        buf.writeInt(msg.scannedEntities.size());
        for (var entry : msg.scannedEntities.entrySet()) {
            buf.writeInt(entry.getKey());
            buf.writeByte(entry.getValue().ordinal());
        }
    }

    public static PerceptionScanPacket decode(FriendlyByteBuf buf) {
        int duration = buf.readInt();
        int size = buf.readInt();
        Map<Integer, ScanType> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            int entityId = buf.readInt();
            ScanType type = ScanType.values()[buf.readByte()];
            map.put(entityId, type);
        }
        return new PerceptionScanPacket(map, duration);
    }

    public static void handle(PerceptionScanPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> PerceptionScanData.update(msg.scannedEntities, msg.glowDurationSeconds));
        ctx.get().setPacketHandled(true);
    }
}
