package net.the_last_sword.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.the_last_sword.client.gui.menu.DragonCrystalEnchantingTableMenu;

import java.util.function.Supplier;

// 龙水晶附魔台能量数据同步包（服务端→客户端）
public class EnchantingTableDataPacket {

    private final int containerId;
    private final int energy;
    private final int maxEnergy;
    private final int totalPowerTime;

    public EnchantingTableDataPacket(int containerId, int energy, int maxEnergy, int totalPowerTime) {
        this.containerId = containerId;
        this.energy = energy;
        this.maxEnergy = maxEnergy;
        this.totalPowerTime = totalPowerTime;
    }

    public static void encode(EnchantingTableDataPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.containerId);
        buf.writeInt(msg.energy);
        buf.writeInt(msg.maxEnergy);
        buf.writeInt(msg.totalPowerTime);
    }

    public static EnchantingTableDataPacket decode(FriendlyByteBuf buf) {
        return new EnchantingTableDataPacket(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
    }

    public static void handle(EnchantingTableDataPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = Minecraft.getInstance().player;
            if (player != null && player.containerMenu instanceof DragonCrystalEnchantingTableMenu menu
                    && menu.containerId == msg.containerId) {
                menu.setClientEnergy(msg.energy);
                menu.setClientMaxEnergy(msg.maxEnergy);
                menu.setClientTotalPowerTime(msg.totalPowerTime);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
