package net.the_last_sword.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.the_last_sword.entity.SwordWraithAppearance;
import net.the_last_sword.entity.TheLastEndSwordWraithEntity;
import net.the_last_sword.item.SwordSoulStone;
import net.the_last_sword.summon.WraithSummonManager;

import java.util.UUID;
import java.util.function.Supplier;

public class SetWraithAppearancePacket {
    private final InteractionHand hand;
    private final SwordWraithAppearance appearance;

    public SetWraithAppearancePacket(InteractionHand hand, SwordWraithAppearance appearance) {
        this.hand = hand;
        this.appearance = appearance;
    }

    public static void encode(SetWraithAppearancePacket message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.hand);
        buffer.writeEnum(message.appearance);
    }

    public static SetWraithAppearancePacket decode(FriendlyByteBuf buffer) {
        return new SetWraithAppearancePacket(
                buffer.readEnum(InteractionHand.class),
                buffer.readEnum(SwordWraithAppearance.class));
    }

    public static void handle(SetWraithAppearancePacket message,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> apply(message, context.getSender()));
        context.setPacketHandled(true);
    }

    private static void apply(SetWraithAppearancePacket message, ServerPlayer player) {
        if (player == null) {
            return;
        }
        ItemStack stack = player.getItemInHand(message.hand);
        if (!(stack.getItem() instanceof SwordSoulStone)) {
            return;
        }

        SwordSoulStone.setAppearance(stack, message.appearance);
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        if (stack.getTag() == null || !stack.getTag().contains("wraith_uuid")) {
            return;
        }

        UUID wraithUuid;
        try {
            wraithUuid = UUID.fromString(stack.getTag().getString("wraith_uuid"));
        } catch (IllegalArgumentException exception) {
            return;
        }

        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(wraithUuid);
            if (entity instanceof TheLastEndSwordWraithEntity wraith) {
                wraith.setAppearance(message.appearance);
                WraithSummonManager.setWraithCustomName(wraith, player);
                return;
            }
        }
    }
}
