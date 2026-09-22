package net.the_last_sword.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.the_last_sword.client.renderer.DangerousSkillPreviewRenderer;

import java.util.UUID;
import java.util.function.Supplier;

public record DangerousSkillPreviewPacket(ResourceLocation dimension, int entityId, UUID entityUuid,
                                         Vec3 origin, Vec3 forward, double width, double length,
                                         double height, long endTick, boolean active) {
    public static void encode(DangerousSkillPreviewPacket packet, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(packet.dimension);
        buffer.writeVarInt(packet.entityId);
        buffer.writeUUID(packet.entityUuid);
        buffer.writeDouble(packet.origin.x);
        buffer.writeDouble(packet.origin.y);
        buffer.writeDouble(packet.origin.z);
        buffer.writeDouble(packet.forward.x);
        buffer.writeDouble(packet.forward.z);
        buffer.writeDouble(packet.width);
        buffer.writeDouble(packet.length);
        buffer.writeDouble(packet.height);
        buffer.writeLong(packet.endTick);
        buffer.writeBoolean(packet.active);
    }

    public static DangerousSkillPreviewPacket decode(FriendlyByteBuf buffer) {
        return new DangerousSkillPreviewPacket(buffer.readResourceLocation(), buffer.readVarInt(),
                buffer.readUUID(), new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                new Vec3(buffer.readDouble(), 0.0, buffer.readDouble()), buffer.readDouble(),
                buffer.readDouble(), buffer.readDouble(), buffer.readLong(), buffer.readBoolean());
    }

    public static void handle(DangerousSkillPreviewPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> DangerousSkillPreviewRenderer.receive(packet)));
        context.get().setPacketHandled(true);
    }
}
