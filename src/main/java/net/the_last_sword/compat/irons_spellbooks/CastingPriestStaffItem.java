package net.the_last_sword.compat.irons_spellbooks;

import io.redspace.ironsspellbooks.item.weapons.StaffItem;
import io.redspace.ironsspellbooks.item.weapons.StaffTier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.the_last_sword.item.PriestStaffItem;

import java.util.List;

public class CastingPriestStaffItem extends StaffItem {
    public CastingPriestStaffItem() {
        super(PriestStaffItem.createProperties(), new StaffTier(2.0F, -2.4F));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            return PriestStaffItem.useGuard(level, player, hand);
        }
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        return super.use(level, player, hand);
    }

    @Override
    public int getEnchantmentValue() {
        return 32;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 32;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        PriestStaffItem.appendStaffTooltip(tooltip, true);
    }
}
