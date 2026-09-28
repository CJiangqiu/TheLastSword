package net.the_last_sword.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.TheLastEndSwordWraithAppearance;
import net.the_last_sword.entity.TheLastEndEntity;
import net.the_last_sword.entity.TheLastEndSwordWraithEntity;
import net.the_last_sword.network.NetworkHandler;
import net.the_last_sword.network.OpenWraithAppearanceScreenPacket;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;

//剑之魂石 - 预设绑定终焉之剑剑灵的魂石
public class SwordSoulStone extends DragonCrystalSoulStone {

    public static final String APPEARANCE_KEY = "wraith_appearance";

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
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.sendToPlayer(new OpenWraithAppearanceScreenPacket(hand, getAppearance(stack)), serverPlayer);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public static TheLastEndSwordWraithAppearance getAppearance(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return TheLastEndSwordWraithAppearance.DEFAULT;
        }
        if (tag.contains(APPEARANCE_KEY)) {
            return TheLastEndSwordWraithAppearance.fromId(tag.getString(APPEARANCE_KEY));
        }
        if (tag.contains("entity_nbt")) {
            return TheLastEndSwordWraithAppearance.fromId(tag.getCompound("entity_nbt").getString("TEXTURE"));
        }
        return TheLastEndSwordWraithAppearance.DEFAULT;
    }

    public static void setAppearance(ItemStack stack, TheLastEndSwordWraithAppearance appearance) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(APPEARANCE_KEY, appearance.getId());
        if (tag.contains("entity_nbt")) {
            tag.getCompound("entity_nbt").putString("TEXTURE", appearance.getId());
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        if (Screen.hasControlDown()) {
            addPassiveSkills(tooltip, true);
            return;
        }
        if (Screen.hasShiftDown()) {
            addActiveSkills(tooltip, true);
            return;
        }

        addPassiveSkills(tooltip, false);
        addActiveSkills(tooltip, false);
        tooltip.add(Component.translatable("item_tooltip.the_last_sword.sword_soul_stone.ctrl"));
        tooltip.add(Component.translatable("item_tooltip.the_last_sword.sword_soul_stone.shift"));
    }

    private static void addPassiveSkills(List<Component> tooltip, boolean showDetails) {
        tooltip.add(Component.translatable("item_tooltip.the_last_sword.sword_soul_stone.passive_1")
                .withStyle(ChatFormatting.DARK_PURPLE));
        if (showDetails) {
            tooltip.add(Component.translatable("item_tooltip.the_last_sword.sword_soul_stone.passive_1_description")
                    .withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("item_tooltip.the_last_sword.sword_soul_stone.passive_2")
                .withStyle(ChatFormatting.DARK_PURPLE));
        if (showDetails) {
            tooltip.add(Component.translatable("item_tooltip.the_last_sword.sword_soul_stone.passive_2_description",
                    TheLastEndSwordWraithEntity.END_MARK_THRESHOLD,
                    TheLastEndEntity.RESURRECTION_LEVEL).withStyle(ChatFormatting.GRAY));
        }
    }

    private static void addActiveSkills(List<Component> tooltip, boolean showDetails) {
        addSkill(tooltip, showDetails, "swift_thrust",
                formatNumber(TheLastSwordConfiguration.getSkillSwiftDashRangeSafely()),
                formatPercentage(TheLastSwordConfiguration.getSkillSwiftDashDamageMultiplierSafely()),
                formatNumber(TheLastSwordConfiguration.getSkillSwiftDashCooldownSafely() / 20.0));
        addSkill(tooltip, showDetails, "double_slash",
                formatNumber(TheLastSwordConfiguration.getSkillDoubleStrikeRangeSafely()),
                formatPercentage(TheLastSwordConfiguration.getSkillDoubleStrikeDamageMultiplierSafely()),
                formatNumber(TheLastSwordConfiguration.getSkillDoubleStrikeCooldownSafely() / 20.0));
        addSkill(tooltip, showDetails, "cross_slash",
                formatNumber(TheLastSwordConfiguration.getSkillCrossSlashRangeSafely()),
                formatPercentage(TheLastSwordConfiguration.getSkillCrossSlashDamageMultiplierSafely()),
                formatNumber(TheLastSwordConfiguration.getSkillCrossSlashCooldownSafely() / 20.0));
        addSkill(tooltip, showDetails, "block",
                formatNumber(TheLastSwordConfiguration.getSkillBlockRangeSafely()),
                formatPercentage(TheLastSwordConfiguration.getSkillBlockDamageMultiplierSafely()),
                formatNumber(TheLastSwordConfiguration.getSkillBlockCooldownSafely() / 20.0));
        addSkill(tooltip, showDetails, "moonlit_strike",
                formatNumber(TheLastSwordConfiguration.getSkillMoonLightStrikeRangeSafely()),
                formatPercentage(TheLastSwordConfiguration.getSkillMoonLightStrikeDamageMultiplierSafely()),
                formatNumber(TheLastSwordConfiguration.getSkillMoonLightStrikeCooldownSafely() / 20.0));
        addSkill(tooltip, showDetails, "enchant",
                formatNumber(TheLastSwordConfiguration.getSkillEnchantDurationSafely() / 20.0),
                formatNumber(TheLastSwordConfiguration.getSkillEnchantCooldownSafely() / 20.0));
        addSkill(tooltip, showDetails, "end_of_all_things",
                formatNumber(TheLastSwordConfiguration.getSkillEndOfAllThingsRangeSafely()),
                formatPercentage(TheLastSwordConfiguration.getSkillEndOfAllThingsDamageMultiplierSafely()),
                formatPercentage(TheLastSwordConfiguration.getSkillEndOfAllThingsExecutionHealthThresholdSafely()));
    }

    private static void addSkill(List<Component> tooltip, boolean showDetails, String skill, Object... values) {
        String key = "item_tooltip.the_last_sword.sword_soul_stone.skill." + skill;
        tooltip.add(Component.translatable(key).withStyle(ChatFormatting.DARK_PURPLE));
        if (showDetails) {
            tooltip.add(Component.translatable(key + "_description", values).withStyle(ChatFormatting.GRAY));
        }
    }

    private static String formatNumber(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    private static String formatPercentage(double fraction) {
        return BigDecimal.valueOf(fraction).movePointRight(2).stripTrailingZeros().toPlainString();
    }
}
