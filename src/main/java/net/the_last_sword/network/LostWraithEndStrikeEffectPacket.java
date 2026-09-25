package net.the_last_sword.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.the_last_sword.client.renderer.LostWraithEndStrikeEffectRenderer;

import java.util.UUID;
import java.util.function.Supplier;

public record LostWraithEndStrikeEffectPacket(ResourceLocation dimension, int entityId, UUID entityUuid,
                                               Vec3 position, long startTick, long damageTick,
                                               long endTick, boolean active) {
    public static void encode(LostWraithEndStrikeEffectPacket packet, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(packet.dimension);
        buffer.writeVarInt(packet.entityId);
        buffer.writeUUID(packet.entityUuid);
        buffer.writeDouble(packet.position.x);
        buffer.writeDouble(packet.position.y);
        buffer.writeDouble(packet.position.z);
        buffer.writeLong(packet.startTick);
        buffer.writeLong(packet.damageTick);
        buffer.writeLong(packet.endTick);
        buffer.writeBoolean(packet.active);
    }

    public static LostWraithEndStrikeEffectPacket decode(FriendlyByteBuf buffer) {
        return new LostWraithEndStrikeEffectPacket(buffer.readResourceLocation(), buffer.readVarInt(),
                buffer.readUUID(), new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                buffer.readLong(), buffer.readLong(), buffer.readLong(), buffer.readBoolean());
    }

    public static void handle(LostWraithEndStrikeEffectPacket packet,
                              Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> LostWraithEndStrikeEffectRenderer.receive(packet)));
        context.get().setPacketHandled(true);
    }
}
