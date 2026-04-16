package net.the_last_sword.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.the_last_sword.item.DragonSword;
import net.the_last_sword.item.TheLastSword;
import net.the_last_sword.test.UltraTestSwordItem;
import net.the_last_sword.util.nbt.ItemModeHelper;

import java.util.function.Supplier;

//模式切换网络包
public class ChangeModePacket {

    public ChangeModePacket() {
    }

    //解码
    public static ChangeModePacket decode(FriendlyByteBuf buf) {
        return new ChangeModePacket();
    }

    //编码
    public static void encode(ChangeModePacket msg, FriendlyByteBuf buf) {
    }

    //处理
    public static void handle(ChangeModePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ItemStack stack = player.getMainHandItem();
            if (stack.isEmpty()) return;

            //检查物品是否支持模式切换（通过检查是否有Mode NBT）
            if (!stack.hasTag() || !stack.getTag().contains("the_last_sword.mode")) {
                return;
            }

            //获取当前模式和最大模式数
            int currentMode = ItemModeHelper.getMode(stack);
            int maxModes = ItemModeHelper.getMaxModes(stack);

            if (maxModes <= 1) {
                return; //只有一个模式不需要切换
            }

            //切换到下一个模式
            ItemModeHelper.cycleMode(stack, maxModes);
            int newMode = ItemModeHelper.getMode(stack);

            //刷新容器，让手持物品 slot 更新到客户端
            player.inventoryMenu.broadcastChanges();

            //获取模式翻译键
            String modeKey = getModeTranslationKey(stack, newMode);

            //客户端聊天提示
            player.displayClientMessage(
                    Component.translatable("item_tooltip.the_last_sword.mode")
                            .append(Component.translatable(modeKey)),
                    true
            );

            //播放音效
            ResourceLocation snd = new ResourceLocation("entity.ender_dragon.flap");
            player.level().playSound(null, player.blockPosition(),
                    ForgeRegistries.SOUND_EVENTS.getValue(snd),
                    SoundSource.PLAYERS, 1f, 1f);
        });
        ctx.get().setPacketHandled(true);
    }

    //根据物品类型和模式ID获取翻译键
    private static String getModeTranslationKey(ItemStack stack, int mode) {
        //检查是否是最终之剑
        if (stack.getItem() instanceof TheLastSword) {
            return switch (mode) {
                case 0 -> "item_tooltip.the_last_sword.normal_mode";
                case 1 -> "item_tooltip.the_last_sword.powerful_mining_mode";
                case 2 -> "item_tooltip.the_last_sword.summon_entity_mode";
                default -> "item_tooltip.the_last_sword.normal_mode";
            };
        }

        //检查是否是龙之剑
        if (stack.getItem() instanceof DragonSword) {
            return switch (mode) {
                case 0 -> "item_tooltip.the_last_sword.normal_mode";
                case 1 -> "item_tooltip.the_last_sword.summon_entity_mode";
                default -> "item_tooltip.the_last_sword.normal_mode";
            };
        }

        //检查是否是究极测试剑
        if (stack.getItem() instanceof UltraTestSwordItem) {
            return switch (mode) {
                case 0 -> "item_tooltip.the_last_sword.powerful_range_attack_mode";
                case 1 -> "item_tooltip.the_last_sword.mob_battle_mode";
                default -> "item_tooltip.the_last_sword.powerful_range_attack_mode";
            };
        }

        //默认返回普通模式
        return "item_tooltip.the_last_sword.normal_mode";
    }
}
