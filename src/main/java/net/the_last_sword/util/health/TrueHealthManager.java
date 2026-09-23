package net.the_last_sword.util.health;

import net.eca.api.EcaAPI;
import net.minecraft.world.entity.LivingEntity;
import net.the_last_sword.util.EntityUtil;

/**
 * 统一管理受保护实体的权威生命值。
 * 真实血量属于防御语义，底层直接使用 ECA 的生命值与最大生命值锁定能力，
 * 不与承担绝毁攻击语义的世界锚度共享状态。
 */
public final class TrueHealthManager {

    private TrueHealthManager() {
    }

    public static float getHealth(LivingEntity entity) {
        if (entity == null) {
            return 0.0F;
        }
        Float lockedHealth = EcaAPI.getLockedHealth(entity);
        return lockedHealth != null ? lockedHealth : EcaAPI.getRealHealth(entity);
    }

    public static boolean setHealth(LivingEntity entity, float health) {
        if (entity == null || !Float.isFinite(health)) {
            return false;
        }

        float normalizedHealth = Math.max(0.0F, health);
        Float healLimit = EcaAPI.getHealBanValue(entity);
        float currentHealth = getHealth(entity);
        if (healLimit != null && normalizedHealth > currentHealth) {
            normalizedHealth = Math.max(currentHealth, Math.min(normalizedHealth, healLimit));
        }
        if (normalizedHealth > 0.0F) {
            EcaAPI.lockHealth(entity, normalizedHealth);
            Float lockedHealth = EcaAPI.getLockedHealth(entity);
            return lockedHealth != null && Math.abs(lockedHealth - normalizedHealth) <= 0.001F;
        }

        EcaAPI.unlockHealth(entity);
        return EcaAPI.setHealth(entity, 0.0F);
    }

    public static float getMaxHealth(LivingEntity entity) {
        if (entity == null) {
            return 0.0F;
        }
        Float lockedMaxHealth = EcaAPI.getLockedMaxHealth(entity);
        return lockedMaxHealth != null ? lockedMaxHealth : entity.getMaxHealth();
    }

    public static void setMaxHealth(LivingEntity entity, float maxHealth) {
        if (entity == null || !Float.isFinite(maxHealth)) {
            return;
        }
        if (maxHealth > 0.0F) {
            EcaAPI.lockMaxHealth(entity, maxHealth);
        } else {
            EcaAPI.unlockMaxHealth(entity);
        }
    }

    public static void register(LivingEntity entity, float maxHealth) {
        if (entity == null || EntityUtil.hasProtection(entity)) {
            return;
        }

        Float savedMaxHealth = EcaAPI.getLockedMaxHealth(entity);
        Float savedHealth = EcaAPI.getLockedHealth(entity);
        float trueMaxHealth = savedMaxHealth != null ? savedMaxHealth : maxHealth;
        float trueHealth = savedHealth != null ? savedHealth : trueMaxHealth;

        setMaxHealth(entity, trueMaxHealth);
        setHealth(entity, Math.min(trueHealth, trueMaxHealth));
        EntityUtil.setProtection(entity, true);
    }

    public static void clear(LivingEntity entity) {
        if (entity == null) {
            return;
        }

        float currentHealth = getHealth(entity);
        EntityUtil.setProtection(entity, false);
        EcaAPI.unlockHealth(entity);
        EcaAPI.unlockMaxHealth(entity);
        if (Float.isFinite(currentHealth) && currentHealth > 0.0F) {
            EcaAPI.setHealth(entity, currentHealth);
        }
    }
}
