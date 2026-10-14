package net.the_last_sword.util.health;

import net.eca.api.EcaAPI;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.TheLastEndEntity;
import net.the_last_sword.item.DragonArmorItem;
import net.the_last_sword.item.TheLastSword;
import net.the_last_sword.item.TheLastSwordYouNeverForgot;
import net.the_last_sword.summon.WraithSummonManager;
import net.the_last_sword.test.UltraTestSwordItem;
import net.the_last_sword.util.EntityUtil;
import net.the_last_sword.util.PrimalSourceEquipment;

/**
 * 统一管理受保护实体的权威生命值。
 * 仅记录并锁定当前生命值，所有实体的最大生命值由属性系统管理，
 * 不与承担绝毁攻击语义的现世锚度共享状态。
 */
public final class TrueHealthManager {

    private TrueHealthManager() {
    }

    public static boolean shouldProtect(LivingEntity entity) {
        if (entity instanceof TheLastEndEntity endEntity && endEntity.isDying()) return false;
        boolean endRace = entity instanceof TheLastEndEntity
                || (TheLastSwordConfiguration.getSwordWraithAsTheLastEndEntitySafely()
                && WraithSummonManager.isWraith(entity));
        if (endRace || DragonArmorItem.isFullSet(entity)
                || PrimalSourceEquipment.isFullSet(entity)
                || isProtectionSword(entity.getMainHandItem())
                || isProtectionSword(entity.getOffhandItem())) return true;
        if (entity instanceof Player player) {
            for (ItemStack stack : player.getInventory().items) {
                if (isProtectionSword(stack)) return true;
            }
        }
        return false;
    }

    private static boolean isProtectionSword(ItemStack stack) {
        return stack.getItem() instanceof TheLastSword
                || stack.getItem() instanceof TheLastSwordYouNeverForgot
                || stack.getItem() instanceof UltraTestSwordItem;
    }

    public static float getHealth(LivingEntity entity) {
        if (entity == null) {
            return 0.0F;
        }
        Float lockedHealth = EcaAPI.getLockedHealth(entity);
        return lockedHealth != null ? lockedHealth : EcaAPI.getRealHealth(entity);
    }

    // 装备资格和残留标记不能阻止真实血量耗尽后的死亡流程。
    public static boolean hasLivingProtection(LivingEntity entity) {
        return entity != null && EntityUtil.hasProtection(entity)
                && !entity.dead && getHealth(entity) > 0.0F;
    }

    public static boolean setHealth(LivingEntity entity, float health) {
        if (entity == null || entity.level().isClientSide || !Float.isFinite(health)) {
            return false;
        }
        if (EntityUtil.hasProtection(entity) || shouldProtect(entity)) {
            releaseMaxHealthLock(entity);
        }

        float normalizedHealth = Math.max(0.0F, health);
        Float healLimit = EcaAPI.getHealBanValue(entity);
        float currentHealth = getHealth(entity);
        if (healLimit != null && normalizedHealth > currentHealth) {
            normalizedHealth = Math.max(currentHealth, Math.min(normalizedHealth, healLimit));
        }
        normalizedHealth = Math.max(0.0F, Math.min(normalizedHealth, entity.getMaxHealth()));
        if (normalizedHealth > 0.0F && shouldProtect(entity)) {
            EcaAPI.lockHealth(entity, normalizedHealth);
            EntityUtil.setProtection(entity, true);
            Float lockedHealth = EcaAPI.getLockedHealth(entity);
            return lockedHealth != null && Math.abs(lockedHealth - normalizedHealth) <= 0.001F;
        }

        // 先撤销死亡拦截，再同步解锁和血量，避免客户端收到零血量时仍被保护。
        EntityUtil.setProtection(entity, false);
        EcaAPI.unlockHealth(entity);
        return EcaAPI.setHealth(entity, normalizedHealth);
    }

    public static float getMaxHealth(LivingEntity entity) {
        if (entity == null) {
            return 0.0F;
        }
        return entity.getMaxHealth();
    }

    private static void releaseMaxHealthLock(LivingEntity entity) {
        if (EcaAPI.getLockedMaxHealth(entity) != null) {
            EcaAPI.unlockMaxHealth(entity);
        }
    }

    public static void register(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide || !shouldProtect(entity)) return;
        releaseMaxHealthLock(entity);
        float health = getHealth(entity);
        if (entity.dead || health <= 0.0F) {
            if (EntityUtil.hasProtection(entity)) clear(entity);
            return;
        }
        if (EntityUtil.hasProtection(entity) && EcaAPI.getLockedHealth(entity) != null) return;
        if (health > 0.0F) setHealth(entity, health);
    }

    // 统一检查所有来源，避免失去一种装备时误清除其他来源的保护。
    public static void syncProtection(LivingEntity entity) {
        if (entity.level().isClientSide) return;
        if (EntityUtil.hasProtection(entity) && (entity.dead || getHealth(entity) <= 0.0F)) {
            clear(entity);
        } else if (shouldProtect(entity)) {
            register(entity);
            clampHealth(entity);
        } else if (EntityUtil.hasProtection(entity)) {
            clear(entity);
        }
        entity.getPersistentData().remove("JustifiedDefenceProtection");
        entity.getPersistentData().remove("JustifiedDefenceClearDelay");
        entity.getPersistentData().remove("UltraTestSwordTLSDefence");
        entity.getPersistentData().remove("TheLastSwordYouNeverForgotDefence");
    }

    // 属性更新后只收敛超出上限的血量，增加上限不会获得额外治疗。
    public static void clampHealth(LivingEntity entity) {
        if (entity.level().isClientSide || !EntityUtil.hasProtection(entity)) return;
        float maxHealth = entity.getMaxHealth();
        if (Float.isFinite(maxHealth) && maxHealth > 0.0F && getHealth(entity) > maxHealth) {
            setHealth(entity, maxHealth);
        }
    }

    public static void clear(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide) {
            return;
        }

        float currentHealth = getHealth(entity);
        EntityUtil.setProtection(entity, false);
        EcaAPI.unlockHealth(entity);
        releaseMaxHealthLock(entity);
        currentHealth = Math.max(0.0F, Math.min(currentHealth, entity.getMaxHealth()));
        if (Float.isFinite(currentHealth) && currentHealth >= 0.0F) {
            EcaAPI.setHealth(entity, currentHealth);
        }
    }
}
