package net.the_last_sword.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;
import net.the_last_sword.client.ClientPacketHandler;
import net.the_last_sword.entity.SwordWraithAppearance;

import java.util.function.Supplier;

public class OpenWraithAppearanceScreenPacket {
    private final InteractionHand hand;
    private final SwordWraithAppearance appearance;

    public OpenWraithAppearanceScreenPacket(InteractionHand hand, SwordWraithAppearance appearance) {
        this.hand = hand;
        this.appearance = appearance;
    }

    public static void encode(OpenWraithAppearanceScreenPacket message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.hand);
        buffer.writeEnum(message.appearance);
    }

    public static OpenWraithAppearanceScreenPacket decode(FriendlyByteBuf buffer) {
        return new OpenWraithAppearanceScreenPacket(
                buffer.readEnum(InteractionHand.class),
                buffer.readEnum(SwordWraithAppearance.class));
    }

    public static void handle(OpenWraithAppearanceScreenPacket message,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientPacketHandler.openWraithAppearanceScreen(message.hand, message.appearance));
        context.setPacketHandled(true);
    }
}
