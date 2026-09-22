package net.the_last_sword.util.health;

import net.eca.api.EcaAPI;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.summon.WraithSummonManager;
import net.the_last_sword.util.EntityUtil;

/**
 * 管理所有生物的世界锚度。
 * 世界锚度是绝毁系统使用的攻击性生命条：未写入时默认等于实体最大生命值，
 * 绝毁伤害会削减该值，并将剩余值作为 ECA 禁疗上限。
 */
public final class WorldAnchorManager {

    public static final String NBT_ABSOLUTE_DESTRUCTION_WORLD_ANCHOR =
            "tlsAbsoluteDestructionWorldAnchor";
    private static final String NBT_HEAL_BAN_TIME = "tlsHealBanTime";
    private static final float UNSET_WORLD_ANCHOR = -1.0F;

    public static EntityDataAccessor<String> WORLD_ANCHOR;
    public static EntityDataAccessor<Integer> HEAL_BAN_TIME;

    private WorldAnchorManager() {
    }

    public static float getWorldAnchor(LivingEntity entity) {
        if (entity == null) {
            return 0.0F;
        }

        if (entity.level().isClientSide) {
            float syncedAnchor = getSyncedWorldAnchor(entity);
            if (syncedAnchor >= 0.0F) {
                return syncedAnchor;
            }
        }

        if (!entity.getPersistentData().contains(NBT_ABSOLUTE_DESTRUCTION_WORLD_ANCHOR, Tag.TAG_ANY_NUMERIC)) {
            float maxHealth = entity.getMaxHealth();
            return Float.isFinite(maxHealth) ? Math.max(0.0F, maxHealth) : 0.0F;
        }

        float worldAnchor = entity.getPersistentData().getFloat(NBT_ABSOLUTE_DESTRUCTION_WORLD_ANCHOR);
        return Float.isFinite(worldAnchor) ? Math.max(0.0F, worldAnchor) : 0.0F;
    }

    public static float getSyncedWorldAnchor(LivingEntity entity) {
        if (entity == null || WORLD_ANCHOR == null) {
            return UNSET_WORLD_ANCHOR;
        }
        try {
            String syncedValue = entity.getEntityData().get(WORLD_ANCHOR);
            if (syncedValue == null || syncedValue.isEmpty()) {
                return UNSET_WORLD_ANCHOR;
            }
            float worldAnchor = Float.parseFloat(syncedValue);
            return Float.isFinite(worldAnchor) ? worldAnchor : UNSET_WORLD_ANCHOR;
        } catch (RuntimeException ignored) {
            return UNSET_WORLD_ANCHOR;
        }
    }

    public static void setWorldAnchor(LivingEntity entity, float worldAnchor) {
        if (entity == null || !Float.isFinite(worldAnchor)) {
            return;
        }
        float safeAnchor = Math.max(0.0F, worldAnchor);
        entity.getPersistentData().putFloat(NBT_ABSOLUTE_DESTRUCTION_WORLD_ANCHOR, safeAnchor);
        setSyncedWorldAnchor(entity, safeAnchor);
    }

    public static void resetWorldAnchor(LivingEntity entity) {
        if (entity == null) {
            return;
        }
        entity.getPersistentData().remove(NBT_ABSOLUTE_DESTRUCTION_WORLD_ANCHOR);
        setSyncedWorldAnchor(entity, UNSET_WORLD_ANCHOR);
        clearHealBan(entity);
    }

    public static void syncWorldAnchor(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide) {
            return;
        }
        float syncedAnchor = UNSET_WORLD_ANCHOR;
        if (entity.getPersistentData().contains(
                NBT_ABSOLUTE_DESTRUCTION_WORLD_ANCHOR, Tag.TAG_ANY_NUMERIC)) {
            float storedAnchor = entity.getPersistentData()
                    .getFloat(NBT_ABSOLUTE_DESTRUCTION_WORLD_ANCHOR);
            if (Float.isFinite(storedAnchor)) {
                syncedAnchor = Math.max(0.0F, storedAnchor);
            }
        }
        setSyncedWorldAnchor(entity, syncedAnchor);
    }

    private static void setSyncedWorldAnchor(LivingEntity entity, float worldAnchor) {
        if (WORLD_ANCHOR == null) {
            return;
        }
        try {
            String syncedValue = worldAnchor < 0.0F ? "" : Float.toString(worldAnchor);
            entity.getEntityData().set(WORLD_ANCHOR, syncedValue);
        } catch (Exception ignored) {
        }
    }

    public static boolean handleAbsoluteDestructionDamage(LivingEntity entity, DamageSource source, float amount) {
        if (entity == null || source == null || Float.isNaN(amount) || amount <= 0.0F) {
            return false;
        }

        Entity attacker = source.getEntity();
        if (attacker != null && !EntityUtil.canAttack(attacker, entity)) {
            return false;
        }

        double multiplier = TheLastSwordConfiguration
                .getAbsoluteDestructionWorldAnchorDamageMultiplierSafely();
        double worldAnchorDamage = amount * multiplier;
        if (Double.isNaN(worldAnchorDamage) || worldAnchorDamage < 0.0D) {
            return false;
        }

        float currentAnchor = getWorldAnchor(entity);
        float remainingAnchor = Double.isInfinite(worldAnchorDamage)
                ? 0.0F
                : Math.max(0.0F, (float) (currentAnchor - worldAnchorDamage));
        setWorldAnchor(entity, remainingAnchor);

        if (remainingAnchor <= 0.0F) {
            clearHealBan(entity);
            WraithSummonManager.tryForceCapture(entity);
            if (TheLastSwordConfiguration.getEnableTheLastEndSetDeadSafely()) {
                EntityUtil.theLastEndSetDead(entity, source);
            } else {
                EcaAPI.setHealth(entity, 0.0F);
            }
            return true;
        }

        applyExpectedHealthDamage(entity, amount);
        // 扣血可能同步完成死亡并重置锚度，不能随后重新挂上禁疗。
        if (entity instanceof Player && !entity.getPersistentData().contains(
                NBT_ABSOLUTE_DESTRUCTION_WORLD_ANCHOR, Tag.TAG_ANY_NUMERIC)) {
            return true;
        }
        applyHealBan(entity, remainingAnchor);
        return true;
    }

    private static void applyExpectedHealthDamage(LivingEntity entity, float amount) {
        float currentHealth = EcaAPI.getRealHealth(entity);
        if (!Float.isFinite(currentHealth) || currentHealth <= 0.0F) {
            return;
        }

        double expectedHealth = Math.max(0.0D, (double) currentHealth - amount);
        EcaAPI.setHealth(entity, (float) expectedHealth);
    }

    public static int getHealBanTime(LivingEntity entity) {
        if (entity == null) {
            return 0;
        }
        if (HEAL_BAN_TIME != null) {
            try {
                return entity.getEntityData().get(HEAL_BAN_TIME);
            } catch (Exception ignored) {
            }
        }
        return entity.getPersistentData().getInt(NBT_HEAL_BAN_TIME);
    }

    public static boolean isHealBanned(LivingEntity entity) {
        return getHealBanTime(entity) > 0;
    }

    public static void tickHealBanTime(LivingEntity entity) {
        if (entity == null || entity.tickCount % 20 != 0) {
            return;
        }

        int currentTime = getHealBanTime(entity);
        if (currentTime <= 0) {
            return;
        }

        int remainingTime = currentTime - 1;
        setHealBanTime(entity, remainingTime);
        if (remainingTime == 0) {
            EcaAPI.unbanHealing(entity);
        }
    }

    public static void clearHealBan(LivingEntity entity) {
        if (entity == null) {
            return;
        }
        setHealBanTime(entity, 0);
        entity.getPersistentData().remove(NBT_HEAL_BAN_TIME);
        EcaAPI.unbanHealing(entity);
    }

    private static void applyHealBan(LivingEntity entity, float worldAnchor) {
        int banTime = TheLastSwordConfiguration.getHealNegationTimeSafely();
        if (banTime <= 0) {
            return;
        }
        setHealBanTime(entity, banTime);
        EcaAPI.banHealing(entity, worldAnchor);
    }

    private static void setHealBanTime(LivingEntity entity, int seconds) {
        int safeSeconds = Math.max(0, seconds);
        if (HEAL_BAN_TIME != null) {
            try {
                entity.getEntityData().set(HEAL_BAN_TIME, safeSeconds);
                return;
            } catch (Exception ignored) {
            }
        }
        entity.getPersistentData().putInt(NBT_HEAL_BAN_TIME, safeSeconds);
    }
}
