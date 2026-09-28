package net.the_last_sword.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.the_last_sword.client.ClientPacketHandler;

public record QueenTripleSlashShakePacket() {
    public static void encode(QueenTripleSlashShakePacket packet, FriendlyByteBuf buffer) {
    }

    public static QueenTripleSlashShakePacket decode(FriendlyByteBuf buffer) {
        return new QueenTripleSlashShakePacket();
    }

    public static void handle(QueenTripleSlashShakePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> ClientPacketHandler::triggerQueenTripleSlashScreenShake));
        context.get().setPacketHandled(true);
    }
}
