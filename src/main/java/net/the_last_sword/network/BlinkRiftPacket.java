package net.the_last_sword.network;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.the_last_sword.client.renderer.BlinkRiftRenderer;

public record BlinkRiftPacket(ResourceLocation dimension, UUID caster, Vec3 position, float yaw, int phase) {
    public static void encode(BlinkRiftPacket packet, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(packet.dimension());
        buffer.writeUUID(packet.caster());
        buffer.writeDouble(packet.position().x);
        buffer.writeDouble(packet.position().y);
        buffer.writeDouble(packet.position().z);
        buffer.writeFloat(packet.yaw());
        buffer.writeVarInt(packet.phase());
    }

    public static BlinkRiftPacket decode(FriendlyByteBuf buffer) {
        return new BlinkRiftPacket(buffer.readResourceLocation(), buffer.readUUID(),
            new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
            buffer.readFloat(), buffer.readVarInt());
    }

    public static void handle(BlinkRiftPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
            () -> () -> BlinkRiftRenderer.receive(packet)));
        context.get().setPacketHandled(true);
    }
}
