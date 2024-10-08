package net.thelastsword.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.thelastsword.capability.SwordCapability;
import net.thelastsword.item.DragonSword;
import net.thelastsword.item.TheLastSword;

import java.util.function.Supplier;

public class ChangeTheLastSwordModeMessage {
    public ChangeTheLastSwordModeMessage() {}

    public ChangeTheLastSwordModeMessage(FriendlyByteBuf buffer) {}

    public static void buffer(ChangeTheLastSwordModeMessage message, FriendlyByteBuf buffer) {}

    public static void handler(ChangeTheLastSwordModeMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
    NetworkEvent.Context context = contextSupplier.get();
    context.enqueueWork(() -> {
        Player player = context.getSender();
        if (player != null) {
            ItemStack itemstack = player.getMainHandItem();
            if (itemstack.getItem() instanceof TheLastSword || itemstack.getItem() instanceof DragonSword) {
                itemstack.getCapability(SwordCapability.MODE_CAPABILITY).ifPresent(modeCapability -> {
                    int mode = modeCapability.getMode();
                    mode = (mode + 1) % (itemstack.getItem() instanceof TheLastSword ? 3 : 2); // 切换模式
                    modeCapability.setMode(mode);

                    // 更新模式信息
                    itemstack.getOrCreateTag().putInt("Mode", mode);

                    // 更新tooltip
                    player.inventoryMenu.broadcastChanges(); // 强制刷新物品状态

                    // 发送消息给玩家
                    String modeText = getModeText(mode, itemstack.getItem() instanceof TheLastSword, itemstack.getItem() instanceof DragonSword);
                    player.displayClientMessage(Component.translatable("item_tooltip.the_last_sword.mode").append(Component.translatable(modeText)), true);

                    // 播放声音
                    if (!player.level().isClientSide()) {
                        player.level().playSound(null, player.blockPosition(), ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.ender_dragon.flap")), SoundSource.HOSTILE, 1, 1);
                    } else {
                        player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.ender_dragon.flap")), SoundSource.HOSTILE, 1, 1, false);
                    }
                });
            }
        }
    });
    context.setPacketHandled(true);
}


    private static String getModeText(int mode, boolean isTheLastSword, boolean isDragonSword) {
    if (isTheLastSword) {
        return switch (mode) {
            case 1 -> "item_tooltip.the_last_sword.powerful_mining_mode";
            case 2 -> "item_tooltip.the_last_sword.powerful_range_attack_mode";
            default -> "item_tooltip.the_last_sword.normal_mode";
        };
    } else if (isDragonSword) {
        return switch (mode) {
            case 1 -> "item_tooltip.the_last_sword.powerful_mining_mode";
            default -> "item_tooltip.the_last_sword.normal_mode";
        };
    } else {
        return "item_tooltip.the_last_sword.normal_mode";
    }
}

}
