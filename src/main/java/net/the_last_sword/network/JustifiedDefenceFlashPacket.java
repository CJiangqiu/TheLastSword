package net.the_last_sword.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.the_last_sword.client.ClientPacketHandler;

import java.util.UUID;
import java.util.function.Supplier;

public record JustifiedDefenceFlashPacket(int entityId, UUID entityUuid) {

    public static void encode(JustifiedDefenceFlashPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entityId);
        buffer.writeUUID(packet.entityUuid);
    }

    public static JustifiedDefenceFlashPacket decode(FriendlyByteBuf buffer) {
        return new JustifiedDefenceFlashPacket(buffer.readVarInt(), buffer.readUUID());
    }

    public static void handle(JustifiedDefenceFlashPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPacketHandler.triggerJustifiedDefenceFlash(
                        packet.entityId, packet.entityUuid)));
        context.get().setPacketHandled(true);
    }
}
