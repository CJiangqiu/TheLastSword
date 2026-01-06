package net.the_last_sword.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class DragonCrystal extends Item {

    public DragonCrystal() {
        super(new Item.Properties()
            .stacksTo(64)
            .fireResistant()
            .rarity(Rarity.UNCOMMON)
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item_tooltip_lore.the_last_sword.dragon_crystal")
            .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
