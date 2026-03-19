package net.the_last_sword.compat.curios;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
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
        tooltip.add(Component.translatable("item_tooltip.the_last_sword.extreme_life_support_device"));
        tooltip.add(Component.translatable("item_tooltip_lore.the_last_sword.extreme_life_support_device")
            .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
