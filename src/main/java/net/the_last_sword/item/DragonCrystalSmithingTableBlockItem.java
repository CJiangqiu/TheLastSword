package net.the_last_sword.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.the_last_sword.init.ModBlocks;

import javax.annotation.Nullable;
import java.util.List;

public class DragonCrystalSmithingTableBlockItem extends BlockItem {

    public DragonCrystalSmithingTableBlockItem() {
        super(ModBlocks.DRAGON_CRYSTAL_SMITHING_TABLE.get(),
            new Properties()
                .rarity(Rarity.UNCOMMON)
                .fireResistant()
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item_tooltip_lore.the_last_sword.dragon_crystal_smithing_table")
            .withStyle(ChatFormatting.GRAY));
    }
}
