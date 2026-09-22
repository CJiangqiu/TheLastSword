package net.the_last_sword.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// 龙甲整套能量状态同步包（服务端→客户端）
public record DragonArmorEnergyStatusPacket(long currentEnergy, long maxEnergy, long consumptionPerTick) {

    public static void encode(DragonArmorEnergyStatusPacket msg, FriendlyByteBuf buf) {
        buf.writeLong(msg.currentEnergy);
        buf.writeLong(msg.maxEnergy);
        buf.writeLong(msg.consumptionPerTick);
    }

    public static DragonArmorEnergyStatusPacket decode(FriendlyByteBuf buf) {
        return new DragonArmorEnergyStatusPacket(buf.readLong(), buf.readLong(), buf.readLong());
    }

    public static void handle(DragonArmorEnergyStatusPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> net.the_last_sword.client.ClientPacketHandler.updateDragonArmorEnergyStatus(
                        msg.currentEnergy, msg.maxEnergy, msg.consumptionPerTick)));
        ctx.get().setPacketHandled(true);
    }
}
