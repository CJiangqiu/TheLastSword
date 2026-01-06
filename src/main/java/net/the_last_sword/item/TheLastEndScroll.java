package net.the_last_sword.item;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.the_last_sword.client.gui.TheLastEndScrollScreen;

import javax.annotation.Nullable;
import java.util.List;

public class TheLastEndScroll extends Item {

    public TheLastEndScroll() {
        super(new Item.Properties()
            .stacksTo(1)
            .fireResistant()
            .rarity(Rarity.EPIC)
        );
    }

    //右键打开教程书GUI
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (level.isClientSide) {
            openScrollBookScreen();
        }

        return InteractionResultHolder.success(itemStack);
    }

    //打开教程书界面（仅客户端）
    @OnlyIn(Dist.CLIENT)
    private void openScrollBookScreen() {
        Minecraft.getInstance().setScreen(new TheLastEndScrollScreen());
    }

    //添加物品描述
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item_tooltip_lore.the_last_sword.the_last_end_scroll")
            .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
