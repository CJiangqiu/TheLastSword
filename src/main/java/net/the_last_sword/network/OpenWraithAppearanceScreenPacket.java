package net.the_last_sword.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;
import net.the_last_sword.client.ClientPacketHandler;
import net.the_last_sword.entity.TheLastEndSwordWraithAppearance;

import java.util.function.Supplier;

public class OpenWraithAppearanceScreenPacket {
    private final InteractionHand hand;
    private final TheLastEndSwordWraithAppearance appearance;

    public OpenWraithAppearanceScreenPacket(InteractionHand hand, TheLastEndSwordWraithAppearance appearance) {
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
                buffer.readEnum(TheLastEndSwordWraithAppearance.class));
    }

    public static void handle(OpenWraithAppearanceScreenPacket message,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientPacketHandler.openWraithAppearanceScreen(message.hand, message.appearance));
        context.setPacketHandled(true);
    }
}
