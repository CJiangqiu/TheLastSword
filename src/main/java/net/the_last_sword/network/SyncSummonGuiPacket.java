package net.the_last_sword.network;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkEvent;
import net.the_last_sword.client.gui.menu.SummonWraithGuiMenu;

import java.util.function.Supplier;

//同步唤灵GUI魂石数据网络包（服务端→客户端）
public class SyncSummonGuiPacket {
    private final ItemStack soulStone;

    public SyncSummonGuiPacket(ItemStack soulStone) {
        this.soulStone = soulStone;
    }

    //编码
    public static void encode(SyncSummonGuiPacket msg, FriendlyByteBuf buf) {
        CompoundTag tag = new CompoundTag();
        msg.soulStone.save(tag);
        buf.writeNbt(tag);
    }

    //解码
    public static SyncSummonGuiPacket decode(FriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        ItemStack soulStone = ItemStack.EMPTY;
        if (tag != null) {
            soulStone = ItemStack.of(tag);
        }
        return new SyncSummonGuiPacket(soulStone);
    }

    //处理
    public static void handle(SyncSummonGuiPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                AbstractContainerMenu menu = minecraft.player.containerMenu;
                if (menu instanceof SummonWraithGuiMenu summonMenu) {
                    //更新GUI中的魂石槽位
                    if (summonMenu.get().containsKey(0) && summonMenu.get().get(0).container instanceof ItemStackHandler handler) {
                        handler.setStackInSlot(0, msg.soulStone);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
