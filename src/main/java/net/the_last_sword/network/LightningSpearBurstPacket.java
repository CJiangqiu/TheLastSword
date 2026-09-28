package net.the_last_sword.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.the_last_sword.client.renderer.LightningSpearBurstRenderer;

public record LightningSpearBurstPacket(ResourceLocation dimension, Vec3 position, float radius, long seed) {
    public static void encode(LightningSpearBurstPacket packet, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(packet.dimension());
        buffer.writeDouble(packet.position().x);
        buffer.writeDouble(packet.position().y);
        buffer.writeDouble(packet.position().z);
        buffer.writeFloat(packet.radius());
        buffer.writeLong(packet.seed());
    }

    public static LightningSpearBurstPacket decode(FriendlyByteBuf buffer) {
        return new LightningSpearBurstPacket(buffer.readResourceLocation(),
                new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                buffer.readFloat(), buffer.readLong());
    }

    public static void handle(LightningSpearBurstPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> LightningSpearBurstRenderer.receive(packet)));
        context.get().setPacketHandled(true);
    }
}
