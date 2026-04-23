package net.the_last_sword.compat.curios;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

//极限维生装置 - belt槽位
public class ExtremeLifeSupportDevice extends Item implements ICurioItem {

    public ExtremeLifeSupportDevice() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        String thresholdPercent = String.format("%.0f",
            TheLastSwordConfiguration.getCuriosExtremeLifeSupportTierThresholdSafely() * 100);
        tooltip.add(Component.translatable("item_tooltip.the_last_sword.extreme_life_support_device", thresholdPercent));
        appendCurrentValueLine(tooltip);
        tooltip.add(Component.translatable("item_tooltip_lore.the_last_sword.extreme_life_support_device")
            .withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    //基于客户端玩家当前生命比例显示损伤百分比与额外Buff等级
    @OnlyIn(Dist.CLIENT)
    private static void appendCurrentValueLine(List<Component> tooltip) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        float maxHealth = player.getMaxHealth();
        if (maxHealth <= 0) {
            return;
        }
        double lostRatio = 1.0 - (player.getHealth() / maxHealth);
        if (lostRatio < 0) {
            lostRatio = 0;
        }
        double threshold = TheLastSwordConfiguration.getCuriosExtremeLifeSupportTierThresholdSafely();
        int bonusLevel = threshold > 0 ? (int) (lostRatio / threshold) : 0;
        String lostStr = String.format("%.1f", lostRatio * 100);
        tooltip.add(Component.translatable("item_tooltip.the_last_sword.extreme_life_support_device.current",
            lostStr, bonusLevel));
    }
}
