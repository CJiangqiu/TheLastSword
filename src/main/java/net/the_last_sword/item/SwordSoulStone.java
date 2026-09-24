package net.the_last_sword.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.TheLastEndEntity;
import net.the_last_sword.entity.TheLastEndSwordWraithEntity;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
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
        tooltip.add(Component.translatable("item_tooltip.the_last_sword.sword_soul_stone.passive_2_description",
                TheLastEndSwordWraithEntity.END_MARK_THRESHOLD,
                TheLastEndEntity.RESURRECTION_LEVEL).withStyle(ChatFormatting.GRAY));

        addSkill(tooltip, "swift_thrust",
                formatNumber(TheLastSwordConfiguration.getSkillSwiftDashRangeSafely()),
                formatPercentage(TheLastSwordConfiguration.getSkillSwiftDashDamageMultiplierSafely()),
                formatNumber(TheLastSwordConfiguration.getSkillSwiftDashCooldownSafely() / 20.0));
        addSkill(tooltip, "double_slash",
                formatNumber(TheLastSwordConfiguration.getSkillDoubleStrikeRangeSafely()),
                formatPercentage(TheLastSwordConfiguration.getSkillDoubleStrikeDamageMultiplierSafely()),
                formatNumber(TheLastSwordConfiguration.getSkillDoubleStrikeCooldownSafely() / 20.0));
        addSkill(tooltip, "cross_slash",
                formatNumber(TheLastSwordConfiguration.getSkillCrossSlashRangeSafely()),
                formatPercentage(TheLastSwordConfiguration.getSkillCrossSlashDamageMultiplierSafely()),
                formatNumber(TheLastSwordConfiguration.getSkillCrossSlashCooldownSafely() / 20.0));
        addSkill(tooltip, "block",
                formatNumber(TheLastSwordConfiguration.getSkillBlockRangeSafely()),
                formatPercentage(TheLastSwordConfiguration.getSkillBlockDamageMultiplierSafely()),
                formatNumber(TheLastSwordConfiguration.getSkillBlockCooldownSafely() / 20.0));
        addSkill(tooltip, "moonlit_strike",
                formatNumber(TheLastSwordConfiguration.getSkillMoonLightStrikeRangeSafely()),
                formatPercentage(TheLastSwordConfiguration.getSkillMoonLightStrikeDamageMultiplierSafely()),
                formatNumber(TheLastSwordConfiguration.getSkillMoonLightStrikeCooldownSafely() / 20.0));
        addSkill(tooltip, "enchant",
                formatNumber(TheLastSwordConfiguration.getSkillEnchantDurationSafely() / 20.0),
                formatNumber(TheLastSwordConfiguration.getSkillEnchantCooldownSafely() / 20.0));
        addSkill(tooltip, "end_of_all_things",
                formatNumber(TheLastSwordConfiguration.getSkillEndOfAllThingsRangeSafely()),
                formatPercentage(TheLastSwordConfiguration.getSkillEndOfAllThingsDamageMultiplierSafely()));
    }

    private static void addSkill(List<Component> tooltip, String skill, Object... values) {
        String key = "item_tooltip.the_last_sword.sword_soul_stone.skill." + skill;
        tooltip.add(Component.translatable(key).withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable(key + "_description", values).withStyle(ChatFormatting.GRAY));
    }

    private static String formatNumber(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    private static String formatPercentage(double fraction) {
        return BigDecimal.valueOf(fraction).movePointRight(2).stripTrailingZeros().toPlainString();
    }
}
