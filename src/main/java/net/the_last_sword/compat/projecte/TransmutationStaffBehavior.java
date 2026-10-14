package net.the_last_sword.compat.projecte;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.init.ModItems;
import net.the_last_sword.init.ModKeyMappings;
import net.the_last_sword.util.nbt.ItemModeHelper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

public final class TransmutationStaffBehavior {
    public static final int MAX_CHARGE = 4;
    public static final int MODE_COUNT = 3;
    private static final String CHARGE_TAG = "Charge";

    private TransmutationStaffBehavior() {
    }

    public static int getTerminationUseTicks() {
        return TheLastSwordConfiguration.getTransmutationStaffTerminationChargeTicksSafely();
    }

    private static String number(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    public static boolean isStaff(ItemStack stack) {
        return ModItems.STAFF_OF_UNIVERSAL_TRANSMUTATION != null
            && stack.is(ModItems.STAFF_OF_UNIVERSAL_TRANSMUTATION.get());
    }

    public static void initialize(ItemStack stack) {
        ItemModeHelper.initializeMode(stack, 0, MODE_COUNT);
        if (ItemModeHelper.getMaxModes(stack) != MODE_COUNT) {
            ItemModeHelper.setMode(stack, Math.floorMod(ItemModeHelper.getMode(stack), MODE_COUNT), MODE_COUNT);
        }
    }

    public static boolean isStaffMode(ItemStack stack) {
        return ItemModeHelper.getMode(stack) == 0;
    }

    public static boolean isTermination(ItemStack stack) {
        return ItemModeHelper.getMode(stack) == 2;
    }

    public static boolean isTransmutation(ItemStack stack) {
        return ItemModeHelper.getMode(stack) == 1;
    }

    public static String modeKey(int mode) {
        if (mode == 2) {
            return "item_tooltip.the_last_sword.termination_mode";
        }
        return mode == 1 ? "item_tooltip.the_last_sword.transmutation_mode"
            : "item_tooltip.the_last_sword.staff_mode";
    }

    public static int getCharge(ItemStack stack) {
        return stack.hasTag() ? Mth.clamp(stack.getTag().getInt(CHARGE_TAG), 0, MAX_CHARGE) : 0;
    }

    public static float getProjectileDamage(ItemStack stack) {
        return (float) (TheLastSwordConfiguration.getTransmutationStaffDamagePerChargeSafely() * (getCharge(stack) + 1));
    }

    public static long getProjectileCost(ItemStack stack) {
        return TheLastSwordConfiguration.getTransmutationStaffBaseEmcCostSafely() << getCharge(stack);
    }

    public static InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack.getItem())) {
            return InteractionResultHolder.fail(stack);
        }
        if (isTermination(stack)) {
            return TransmutationStaffTermination.begin(level, player, hand);
        }
        if (isTransmutation(stack)) {
            if (player.isShiftKeyDown()) {
                return TransmutationStaffActions.openTable(level, player, hand);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        return TransmutationStaffActions.fireProjectile(level, player, hand);
    }

    public static InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() != null && context.getPlayer().getCooldowns().isOnCooldown(context.getItemInHand().getItem())) {
            return InteractionResult.FAIL;
        }
        if (isTermination(context.getItemInHand())) {
            return context.getPlayer() == null ? InteractionResult.FAIL
                : use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
        }
        if (!isTransmutation(context.getItemInHand())) {
            return InteractionResult.PASS;
        }
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.FAIL;
        }
        if (player.isShiftKeyDown()) {
            return TransmutationStaffActions.openTable(context.getLevel(), player, context.getHand()).getResult();
        }
        return TransmutationStaffActions.transmuteBlock(context);
    }

    public static void appendTooltip(ItemStack stack, List<Component> tooltip, boolean castingEnabled) {
        String projectileDamage = String.format(Locale.ROOT, "%.0f", getProjectileDamage(stack));
        if (!castingEnabled && isStaffMode(stack)) {
            tooltip.add(Component.translatable("item_tooltip.the_last_sword.transmutation_staff.projectile_damage",
                projectileDamage).withStyle(style -> style.withColor(TextColor.fromRgb(0xAA00FF))));
        }
        tooltip.add(Component.translatable("item_tooltip.the_last_sword.mode").append(" ")
            .append(Component.translatable(modeKey(ItemModeHelper.getMode(stack)))));

        if (!Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item_tooltip.the_last_sword.shift"));
        } else {
            if (isTermination(stack)) {
                tooltip.add(Component.translatable("item_tooltip.the_last_sword.transmutation_staff.termination",
                    number(getTerminationUseTicks() / 20.0),
                    number(TheLastSwordConfiguration.getTransmutationStaffTerminationRadiusSafely()),
                    number(TheLastSwordConfiguration.getTransmutationStaffTerminationEmcRatioSafely() * 100),
                    number(TheLastSwordConfiguration.getTransmutationStaffTerminationEmcDamageRatioSafely() * 100),
                    number(TheLastSwordConfiguration.getTransmutationStaffTerminationManaRatioSafely() * 100),
                    number(TheLastSwordConfiguration.getTransmutationStaffTerminationManaDamageRatioSafely()),
                    number(TheLastSwordConfiguration.getTransmutationStaffTerminationCooldownSafely() / 20.0)));
            } else if (isTransmutation(stack)) {
                tooltip.add(Component.translatable("item_tooltip.the_last_sword.transmutation_staff.right_click_block"));
                tooltip.add(Component.translatable("item_tooltip.the_last_sword.transmutation_staff.right_click_entity",
                    TheLastSwordConfiguration.getTransmutationStaffEntityEmcCostSafely()));
                tooltip.add(Component.translatable("item_tooltip.the_last_sword.transmutation_staff.sneak_right_click"));
            } else if (castingEnabled) {
                tooltip.add(Component.translatable("item_tooltip.the_last_sword.transmutation_staff.cast",
                    number(TheLastSwordConfiguration.getTransmutationStaffSpellPowerPerChargeSafely() * 100),
                    getCharge(stack) == 0 ? 0L : getProjectileCost(stack)));
            } else {
                tooltip.add(Component.translatable("item_tooltip.the_last_sword.transmutation_staff.projectile",
                    projectileDamage, getProjectileCost(stack)));
            }
        }

        tooltip.add(Component.translatable("item_tooltip.the_last_sword.mode_key").append(" ")
            .append(ModKeyMappings.CHANGE_SWORD_MODE.getKey().getDisplayName()));
    }
}
