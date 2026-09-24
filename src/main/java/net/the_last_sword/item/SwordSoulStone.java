package net.the_last_sword.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

//剑之魂石 - 预设绑定终焉之剑剑灵的魂石
public class SwordSoulStone extends DragonCrystalSoulStone {

    public SwordSoulStone() {
        super();
    }

    @Override
    public String getDefaultBoundEntityId() {
        return "the_last_sword:the_last_end_sword_wraith";
    }

    @Override
    public String getDefaultBoundEntityDisplayName() {
        return "entity.the_last_sword.the_last_end_sword_wraith";
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item_tooltip.the_last_sword.sword_soul_stone.passive_1")
                .withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("item_tooltip.the_last_sword.sword_soul_stone.passive_1_description")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item_tooltip.the_last_sword.sword_soul_stone.passive_2")
                .withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("item_tooltip.the_last_sword.sword_soul_stone.passive_2_description_1")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item_tooltip.the_last_sword.sword_soul_stone.passive_2_description_2")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item_tooltip.the_last_sword.sword_soul_stone.passive_2_description_3")
                .withStyle(ChatFormatting.GRAY));
    }
}
