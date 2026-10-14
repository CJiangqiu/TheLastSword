package net.the_last_sword.compat.irons_spellbooks;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.the_last_sword.compat.CompatCheck;
import net.the_last_sword.compat.projecte.TransmutationStaffActions;
import net.the_last_sword.compat.projecte.TransmutationStaffBehavior;

import net.the_last_sword.configuration.TheLastSwordConfiguration;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class TransmutationStaffCasting {
    private static final UUID POWER_MODIFIER = UUID.fromString("c53ed61a-c6ae-40ef-9f42-dd9e9817512b");
    private static final Map<MagicData, Integer> CAST_CHARGES = new WeakHashMap<>();

    private TransmutationStaffCasting() {
    }

    public static boolean prepare(Player player, ItemStack stack, MagicData data) {
        clear(data);
        if (!CompatCheck.isTransmutationLoaded() || !TransmutationStaffBehavior.isStaff(stack)
            || !TransmutationStaffBehavior.isStaffMode(stack)) {
            return true;
        }
        int charge = TransmutationStaffBehavior.getCharge(stack);
        if (charge == 0) {
            return true;
        }
        if (!TransmutationStaffActions.consumeCastingFuel(player, stack)) {
            return false;
        }
        // 锁定起手档位，引导期间调档不会改变已付费的加成。
        CAST_CHARGES.put(data, charge);
        return true;
    }

    public static void withPower(LivingEntity caster, MagicData data, Runnable action) {
        int charge = CAST_CHARGES.getOrDefault(data, 0);
        AttributeInstance attribute = caster.getAttribute(AttributeRegistry.SPELL_POWER.get());
        if (charge == 0 || attribute == null) {
            action.run();
            return;
        }
        AttributeModifier previous = attribute.getModifier(POWER_MODIFIER);
        if (previous != null) {
            attribute.removeModifier(POWER_MODIFIER);
        }
        attribute.addTransientModifier(new AttributeModifier(POWER_MODIFIER,
            "Transmutation charge spell power", charge * TheLastSwordConfiguration.getTransmutationStaffSpellPowerPerChargeSafely(), AttributeModifier.Operation.MULTIPLY_BASE));
        try {
            action.run();
        } finally {
            // 属性只在法术结算期间存在，中断、异常或换手都不会留下常驻加成。
            attribute.removeModifier(POWER_MODIFIER);
            if (previous != null) {
                attribute.addTransientModifier(previous);
            }
        }
    }

    public static void clear(MagicData data) {
        CAST_CHARGES.remove(data);
    }
}
