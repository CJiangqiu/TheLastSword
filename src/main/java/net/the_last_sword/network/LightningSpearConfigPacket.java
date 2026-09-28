package net.the_last_sword.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.the_last_sword.client.LightningSpearClientSettings;
import net.the_last_sword.configuration.LightningSpearSettings;

public record LightningSpearConfigPacket(LightningSpearSettings settings) {
    public static void encode(LightningSpearConfigPacket packet, FriendlyByteBuf buffer) {
        LightningSpearSettings settings = packet.settings();
        buffer.writeVarInt(settings.cooldownTicks());
        buffer.writeVarInt(settings.chargeTicks());
        buffer.writeDouble(settings.range());
        buffer.writeDouble(settings.burstSize());
        buffer.writeDouble(settings.damage());
        buffer.writeVarInt(settings.slowLevel());
        buffer.writeVarInt(settings.slowTicks());
    }

    public static LightningSpearConfigPacket decode(FriendlyByteBuf buffer) {
        return new LightningSpearConfigPacket(new LightningSpearSettings(buffer.readVarInt(), buffer.readVarInt(),
            buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readVarInt(), buffer.readVarInt()));
    }

    public static void handle(LightningSpearConfigPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
            () -> () -> LightningSpearClientSettings.receive(packet.settings())));
        context.get().setPacketHandled(true);
    }
}
