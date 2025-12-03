package net.the_last_sword.network;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import net.the_last_sword.client.gui.menu.SummonWraithGuiMenu;

import java.util.function.Supplier;

//打开唤灵GUI网络包（客户端→服务端）
public class OpenSummonGuiPacket {

    public OpenSummonGuiPacket() {
    }

    //编码
    public static void encode(OpenSummonGuiPacket msg, FriendlyByteBuf buf) {
        //无需编码任何数据
    }

    //解码
    public static OpenSummonGuiPacket decode(FriendlyByteBuf buf) {
        return new OpenSummonGuiPacket();
    }

    //处理
    public static void handle(OpenSummonGuiPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }

            //安全检查：防止任意区块生成
            if (!player.level().hasChunkAt(player.blockPosition())) {
                return;
            }

            //打开GUI
            BlockPos pos = player.blockPosition();
            NetworkHooks.openScreen(player, new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("gui.the_last_sword.summon_wraith_gui");
                }

                @Override
                public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                    buffer.writeBlockPos(pos);
                    return new SummonWraithGuiMenu(id, inventory, buffer);
                }
            }, pos);
        });
        ctx.get().setPacketHandled(true);
    }
}
