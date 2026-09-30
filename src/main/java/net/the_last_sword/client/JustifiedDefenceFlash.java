package net.the_last_sword.client;

import net.minecraft.world.entity.LivingEntity;
import net.the_last_sword.configuration.DefenceConfig;
import net.the_last_sword.configuration.DefenceConfigData;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class JustifiedDefenceFlash {
    private static final int DURATION_TICKS = 10;
    private static final float MAX_WHITE_OVERLAY = 0.30F;
    private static final Map<UUID, Integer> REMAINING_TICKS = new HashMap<>();

    private JustifiedDefenceFlash() {
    }

    public static void trigger(LivingEntity entity) {
        REMAINING_TICKS.put(entity.getUUID(), DURATION_TICKS);
    }

    public static void tick() {
        Iterator<Map.Entry<UUID, Integer>> iterator = REMAINING_TICKS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = iterator.next();
            int remaining = entry.getValue() - 1;
            if (remaining <= 0) {
                iterator.remove();
            } else {
                entry.setValue(remaining);
            }
        }
    }

    public static float getWhiteOverlayProgress(LivingEntity entity) {
        if (DefenceConfig.getJustifiedDefence().overlayEffect
                == DefenceConfigData.ShieldEffectMode.DISABLED) {
            return 0.0F;
        }
        int remaining = REMAINING_TICKS.getOrDefault(entity.getUUID(), 0);
        return MAX_WHITE_OVERLAY * remaining / DURATION_TICKS;
    }

    public static void clear() {
        REMAINING_TICKS.clear();
    }
}
