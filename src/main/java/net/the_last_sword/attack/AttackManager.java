package net.the_last_sword.attack;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.the_last_sword.configuration.TheLastSwordConfiguration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//攻击效果管理器 - 统一管理禁疗和禁复活效果
public final class AttackManager {

    //禁疗表 (实体UUID -> 禁疗记录)
    private static final ConcurrentHashMap<UUID, HealNegationRecord> HEAL_NEGATION_MAP = new ConcurrentHashMap<>();

    //禁复活表 (实体类型 -> 剩余时间)
    private static final ConcurrentHashMap<Class<?>, Integer> REVIVE_BAN_MAP = new ConcurrentHashMap<>();

    //数据变更标记
    private static volatile boolean needsSave = false;

    //清理系统相关变量
    private static long lastCleanupTime = 0;
    private static final long CLEANUP_INTERVAL = 30 * 20; // 30秒 (600 ticks)

    public static boolean needsSave() { return needsSave; }
    public static void markSaved() { needsSave = false; }
    private static void markDirty() { needsSave = true; }

    /* ==================== 禁疗记录内部类 ==================== */

    //禁疗记录（私有静态内部类）
    private static final class HealNegationRecord {
        float lockedHealth;      //禁疗锁定的血量
        int remainingTime;       //剩余禁疗时间（秒），-1表示永久

        HealNegationRecord(float health, int time) {
            this.lockedHealth = health;
            this.remainingTime = time;
        }
    }

    /* ==================== 禁疗系统 ==================== */

    //设置实体禁疗状态
    public static void setHealNegation(LivingEntity entity, float controlledHealth) {
        if (entity == null || entity.getUUID() == null) {
            return;
        }

        int banTime = TheLastSwordConfiguration.getHealNegationTimeSafely();
        if (banTime <= 0) {
            return;
        }

        UUID id = entity.getUUID();
        HEAL_NEGATION_MAP.put(id, new HealNegationRecord(controlledHealth, banTime));

        markDirty();

        //立即保存到当前维度
        if (!entity.level().isClientSide && entity.level() instanceof ServerLevel serverLevel) {
            AttackSavedData.persistData(serverLevel);
        }
    }

    //快速检查是否有任何禁疗效果（性能优化）
    public static boolean isEmpty() {
        return HEAL_NEGATION_MAP.isEmpty();
    }

    //更新禁疗记录的生命值（允许受伤时调用）
    public static void updateHealNegationHealth(Entity entity, float newHealth) {
        if (entity == null || entity.getUUID() == null) {
            return;
        }

        HealNegationRecord record = HEAL_NEGATION_MAP.get(entity.getUUID());
        if (record != null) {
            //允许生命值下降，也允许直接设置（用于伤害同步）
            if (newHealth <= record.lockedHealth) {
                record.lockedHealth = newHealth;
                markDirty();
            }
        }
    }

    //清除实体的禁疗状态
    public static void clearHealNegation(Entity entity) {
        if (entity == null || entity.getUUID() == null) {
            return;
        }

        if (HEAL_NEGATION_MAP.remove(entity.getUUID()) != null) {
            markDirty();
        }
    }

    /* ==================== 禁复活系统 ==================== */

    //添加实体类型到禁复活列表
    public static void addReviveBan(Entity entity) {
        if (entity == null) {
            return;
        }

        int banTime = TheLastSwordConfiguration.getReviveBanTimeSafely();
        if (banTime <= 0) {
            return;
        }

        Class<?> entityClass = entity.getClass();

        //添加或重置禁复活时间（重复添加不叠加时长）
        REVIVE_BAN_MAP.put(entityClass, banTime);

        markDirty();
    }

    /* ==================== Tick更新系统 ==================== */

    //世界tick处理，更新倒计时并清理无效记录
    public static void worldTick(ServerLevel level) {
        if (level == null) {
            return;
        }

        long currentTime = level.getGameTime();

        //每秒更新一次倒计时（20 ticks = 1秒）
        if (currentTime % 20 == 0) {
            updateHealNegationTick();
            updateReviveBanTick();
        }

        //定期清理无效记录
        if (currentTime - lastCleanupTime >= CLEANUP_INTERVAL) {
            cleanupInvalidRecords(level);
            lastCleanupTime = currentTime;
        }
    }

    //禁疗倒计时更新
    private static void updateHealNegationTick() {
        if (HEAL_NEGATION_MAP.isEmpty()) return;

        boolean hasChanges = false;
        Iterator<Map.Entry<UUID, HealNegationRecord>> iterator = HEAL_NEGATION_MAP.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, HealNegationRecord> entry = iterator.next();
            HealNegationRecord record = entry.getValue();

            //永久禁疗（-1）不递减
            if (record.remainingTime == -1) {
                continue;
            }

            int newTime = record.remainingTime - 1;

            if (newTime <= 0) {
                iterator.remove();
                hasChanges = true;
            } else {
                record.remainingTime = newTime;
                hasChanges = true;
            }
        }

        if (hasChanges) {
            markDirty();
        }
    }

    //禁复活倒计时更新
    private static void updateReviveBanTick() {
        if (REVIVE_BAN_MAP.isEmpty()) return;

        boolean hasChanges = false;
        Iterator<Map.Entry<Class<?>, Integer>> iterator = REVIVE_BAN_MAP.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Class<?>, Integer> entry = iterator.next();
            int newTime = entry.getValue() - 1;

            if (newTime <= 0) {
                iterator.remove();
                hasChanges = true;
            } else {
                entry.setValue(newTime);
                hasChanges = true;
            }
        }

        if (hasChanges) {
            markDirty();
        }
    }

    //清理无效的攻击效果记录
    private static void cleanupInvalidRecords(ServerLevel level) {
        if (HEAL_NEGATION_MAP.isEmpty()) {
            return;
        }

        var server = level.getServer();
        int cleanedCount = 0;

        //创建需要移除的UUID列表
        List<UUID> toRemove = new ArrayList<>();

        for (UUID entityId : HEAL_NEGATION_MAP.keySet()) {
            boolean entityExists = false;

            //检查所有维度中是否存在该实体
            for (ServerLevel serverLevel : server.getAllLevels()) {
                Entity entity = serverLevel.getEntity(entityId);
                if (entity instanceof LivingEntity && !entity.isRemoved()) {
                    entityExists = true;
                    break;
                }
            }

            if (!entityExists) {
                toRemove.add(entityId);
            }
        }

        //移除无效记录
        for (UUID entityId : toRemove) {
            HEAL_NEGATION_MAP.remove(entityId);
            cleanedCount++;
        }

        if (cleanedCount > 0) {
            markDirty();
        }
    }


    //导出所有记录到NBT格式
    public static Map<UUID, CompoundTag> exportRecords() {
        Map<UUID, CompoundTag> result = new HashMap<>();

        //导出禁疗记录
        for (Map.Entry<UUID, HealNegationRecord> entry : HEAL_NEGATION_MAP.entrySet()) {
            UUID entityId = entry.getKey();
            HealNegationRecord record = entry.getValue();

            CompoundTag tag = new CompoundTag();
            tag.putFloat("LockedHealth", record.lockedHealth);
            tag.putInt("RemainingTime", record.remainingTime);

            result.put(entityId, tag);
        }

        //导出禁复活类型数据
        CompoundTag reviveBanTag = new CompoundTag();
        for (Map.Entry<Class<?>, Integer> entry : REVIVE_BAN_MAP.entrySet()) {
            reviveBanTag.putInt(entry.getKey().getName(), entry.getValue());
        }

        //使用特殊UUID存储禁复活数据
        if (!reviveBanTag.isEmpty()) {
            UUID specialId = new UUID(0L, 0L); // 特殊UUID用于存储全局数据
            CompoundTag specialTag = new CompoundTag();
            specialTag.put("ReviveBanTypes", reviveBanTag);
            result.put(specialId, specialTag);
        }

        return result;
    }

    //从NBT格式导入记录
    public static void importRecords(Map<UUID, CompoundTag> data) {
        if (data == null) {
            return;
        }

        //不要清空 HEAL_NEGATION_MAP，而是合并数据
        REVIVE_BAN_MAP.clear();

        for (Map.Entry<UUID, CompoundTag> entry : data.entrySet()) {
            UUID entityId = entry.getKey();
            CompoundTag tag = entry.getValue();

            if (entityId == null || tag == null) {
                continue;
            }

            //检查是否是特殊的全局数据
            if (entityId.equals(new UUID(0L, 0L))) {
                //导入禁复活类型数据
                if (tag.contains("ReviveBanTypes")) {
                    CompoundTag reviveBanTag = tag.getCompound("ReviveBanTypes");
                    for (String className : reviveBanTag.getAllKeys()) {
                        try {
                            Class<?> clazz = Class.forName(className);
                            int time = reviveBanTag.getInt(className);
                            REVIVE_BAN_MAP.put(clazz, time);
                        } catch (ClassNotFoundException e) {
                            //忽略无法找到的类
                        }
                    }
                }
                continue;
            }

            //导入禁疗记录
            if (tag.contains("LockedHealth") && tag.contains("RemainingTime")) {
                float lockedHealth = tag.getFloat("LockedHealth");
                int remainingTime = tag.getInt("RemainingTime");
                HEAL_NEGATION_MAP.put(entityId, new HealNegationRecord(lockedHealth, remainingTime));
            }
        }
    }

    //清理指定实体的所有攻击效果
    public static void clearAll(Entity entity) {
        if (entity != null && entity.getUUID() != null) {
            HEAL_NEGATION_MAP.remove(entity.getUUID());
            markDirty();
        }
    }

    /* ==================== 安全访问API ==================== */

    //检查实体是否被禁疗
    public static boolean isHealNegated(Entity entity) {
        if (entity == null || entity.getUUID() == null) {
            return false;
        }
        HealNegationRecord record = HEAL_NEGATION_MAP.get(entity.getUUID());
        return record != null && (record.remainingTime > 0 || record.remainingTime == -1);
    }

    //获取禁疗时锁定的生命值
    public static float getLockedHealth(Entity entity) {
        if (entity == null || entity.getUUID() == null) {
            return 0.0f;
        }
        HealNegationRecord record = HEAL_NEGATION_MAP.get(entity.getUUID());
        return record != null ? record.lockedHealth : 0.0f;
    }

    //获取禁疗剩余时间
    public static int getRemainingTime(Entity entity) {
        if (entity == null || entity.getUUID() == null) {
            return 0;
        }
        HealNegationRecord record = HEAL_NEGATION_MAP.get(entity.getUUID());
        return record != null ? record.remainingTime : 0;
    }

    //获取所有禁复活类型（直接返回引用，仅用于读取）
    public static Map<Class<?>, Integer> getAllReviveBanTypes() {
        return REVIVE_BAN_MAP;
    }

    //防止实例化
    private AttackManager() {}
}
