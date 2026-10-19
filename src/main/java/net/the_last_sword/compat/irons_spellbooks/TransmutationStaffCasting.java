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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Supplier;

public final class TransmutationStaffCasting {
    private static final UUID POWER_MODIFIER = UUID.fromString("c53ed61a-c6ae-40ef-9f42-dd9e9817512b");
    private static final Map<MagicData, Integer> CAST_CHARGES = new WeakHashMap<>();
    private static final ThreadLocal<Deque<PowerScope>> POWER_SCOPES = new ThreadLocal<>();

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

    public static boolean requiresPowerScope(ItemStack stack) {
        return CompatCheck.isTransmutationLoaded() && TransmutationStaffBehavior.isStaff(stack)
            && TransmutationStaffBehavior.isStaffMode(stack) && TransmutationStaffBehavior.getCharge(stack) > 0;
    }

    public static boolean hasPower(MagicData data) {
        return CAST_CHARGES.getOrDefault(data, 0) > 0;
    }

    public static boolean isPowerScopeActive() {
        return POWER_SCOPES.get() != null;
    }

    public static <T> T withPowerScope(Supplier<T> action) {
        if (isPowerScopeActive()) {
            return action.get();
        }
        Deque<PowerScope> scopes = new ArrayDeque<>();
        POWER_SCOPES.set(scopes);
        try {
            return action.get();
        } finally {
            // 调用中断或抛出异常时，也要恢复尚未离开的结算范围。
            try {
                while (!scopes.isEmpty()) {
                    scopes.pop().close();
                }
            } finally {
                POWER_SCOPES.remove();
            }
        }
    }

    public static void beginPower(LivingEntity caster, MagicData data) {
        Deque<PowerScope> scopes = POWER_SCOPES.get();
        if (scopes == null) {
            return;
        }
        int charge = CAST_CHARGES.getOrDefault(data, 0);
        AttributeInstance attribute = caster.getAttribute(AttributeRegistry.SPELL_POWER.get());
        if (charge == 0 || attribute == null) {
            scopes.push(new PowerScope(null, null));
            return;
        }
        AttributeModifier previous = attribute.getModifier(POWER_MODIFIER);
        if (previous != null) {
            attribute.removeModifier(POWER_MODIFIER);
        }
        attribute.addTransientModifier(new AttributeModifier(POWER_MODIFIER,
            "Transmutation charge spell power", charge * TheLastSwordConfiguration.getTransmutationStaffSpellPowerPerChargeSafely(), AttributeModifier.Operation.MULTIPLY_BASE));
        scopes.push(new PowerScope(attribute, previous));
    }

    public static void endPower() {
        Deque<PowerScope> scopes = POWER_SCOPES.get();
        if (scopes != null && !scopes.isEmpty()) {
            scopes.pop().close();
        }
    }

    private record PowerScope(AttributeInstance attribute, AttributeModifier previous) {
        private void close() {
            if (attribute == null) {
                return;
            }
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
