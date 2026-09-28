package net.the_last_sword.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.the_last_sword.client.ClientPacketHandler;

public record QueenExecutionCameraPacket(boolean active, float yaw) {
    public static void encode(QueenExecutionCameraPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.active());
        buffer.writeFloat(packet.yaw());
    }

    public static QueenExecutionCameraPacket decode(FriendlyByteBuf buffer) {
        return new QueenExecutionCameraPacket(buffer.readBoolean(), buffer.readFloat());
    }

    public static void handle(QueenExecutionCameraPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPacketHandler.setQueenExecutionCamera(packet.active(), packet.yaw())));
        context.get().setPacketHandled(true);
    }
}
