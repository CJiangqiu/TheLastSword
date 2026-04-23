package net.the_last_sword.compat.curios;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

//给予者的痛苦 - hands槽位
public class TheGiversPain extends Item implements ICurioItem {

    public TheGiversPain() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        String effectSeconds = String.format("%.1f",
            TheLastSwordConfiguration.getCuriosGiversPainEffectDurationSafely() / 20.0);
        String attackMult = String.format("%.2f",
            TheLastSwordConfiguration.getCuriosGiversPainAttackDamageMultiplierSafely());
        String attackerMult = String.format("%.2f",
            TheLastSwordConfiguration.getCuriosGiversPainAttackerLostHealthMultiplierSafely());
        String targetMult = String.format("%.2f",
            TheLastSwordConfiguration.getCuriosGiversPainTargetLostHealthMultiplierSafely());
        tooltip.add(Component.translatable("item_tooltip.the_last_sword.the_givers_pain",
            effectSeconds, attackMult, attackerMult, targetMult));
        tooltip.add(Component.translatable("item_tooltip_lore.the_last_sword.the_givers_pain")
            .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
