package net.the_last_sword.compat.projecte;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.the_last_sword.compat.projecte.client.TransmutationStaffTerminationRenderer;

public record TransmutationStaffTerminationPacket(ResourceLocation dimension, Vec3 position, float radius) {
    public static void encode(TransmutationStaffTerminationPacket packet, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(packet.dimension());
        buffer.writeDouble(packet.position().x);
        buffer.writeDouble(packet.position().y);
        buffer.writeDouble(packet.position().z);
        buffer.writeFloat(packet.radius());
    }

    public static TransmutationStaffTerminationPacket decode(FriendlyByteBuf buffer) {
        return new TransmutationStaffTerminationPacket(buffer.readResourceLocation(),
            new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()), buffer.readFloat());
    }

    public static void handle(TransmutationStaffTerminationPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
            () -> () -> TransmutationStaffTerminationRenderer.receive(packet)));
        context.get().setPacketHandled(true);
    }
}
