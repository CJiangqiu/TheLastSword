package net.the_last_sword.attack;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.the_last_sword.util.TheLastSwordLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//攻击效果数据持久化类
public class AttackSavedData extends SavedData {

    //UUID → 序列化的攻击效果记录
    private final Map<UUID, CompoundTag> recordsNBT = new HashMap<>();

    private AttackSavedData() {}

    /* ================ 获取/创建实例 ================ */

    public static AttackSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                AttackSavedData::load,
                AttackSavedData::new,
                "the_last_sword_attack_effects"
        );
    }

    /* ================ 数据读取 ================ */

    private static AttackSavedData load(CompoundTag root) {
        AttackSavedData data = new AttackSavedData();

        if (root.contains("AttackRecords")) {
            CompoundTag recordsTag = root.getCompound("AttackRecords");
            for (String uuidStr : recordsTag.getAllKeys()) {
                try {
                    UUID entityId = UUID.fromString(uuidStr);
                    CompoundTag recordTag = recordsTag.getCompound(uuidStr);
                    data.recordsNBT.put(entityId, recordTag);
                } catch (IllegalArgumentException e) {
                    //忽略无效的UUID格式
                    TheLastSwordLogger.error("Invalid UUID in attack effects data: {}", uuidStr);
                }
            }
        }

        return data;
    }

    /* ================ 数据保存 ================ */

    @Override
    public CompoundTag save(CompoundTag root) {
        CompoundTag recordsTag = new CompoundTag();

        //保存所有攻击效果记录
        recordsNBT.forEach((uuid, recordData) -> {
            recordsTag.put(uuid.toString(), recordData);
        });

        root.put("AttackRecords", recordsTag);

        //添加版本信息和时间戳，便于未来升级
        root.putString("Version", "1.0.0");
        root.putLong("SaveTime", System.currentTimeMillis());

        return root;
    }

    //将 AttackEffectManager 的数据持久化到存档
    public static void persistData(ServerLevel level) {
        try {
            AttackSavedData store = get(level);

            synchronized (AttackManager.class) {
                store.recordsNBT.clear();
                Map<UUID, CompoundTag> latestData = AttackManager.exportRecords();
                store.recordsNBT.putAll(latestData);
                store.setDirty();
                AttackManager.markSaved();
            }
        } catch (Exception e) {
            TheLastSwordLogger.error("Failed to persist data to level: {}", level.dimension().location());
            e.printStackTrace();
        }
    }

    //从存档加载数据到 AttackEffectManager
    public static void loadToManager(ServerLevel level) {
        try {
            AttackSavedData store = get(level);

            synchronized (AttackManager.class) {
                AttackManager.importRecords(store.recordsNBT);
                AttackManager.markSaved();
            }
        } catch (Exception e) {
            TheLastSwordLogger.error("Failed to load data from level: {}", level.dimension().location());
            e.printStackTrace();
        }
    }

    /* ================ 维护和调试工具 ================ */

    //获取当前存储的记录数量
    public int getStoredRecordCount() {
        return recordsNBT.size();
    }

    //清理过期或无效的记录
    public void cleanupExpiredRecords() {
        long currentTime = System.currentTimeMillis();
        long maxAge = 7 * 24 * 60 * 60 * 1000L; // 7天过期时间

        recordsNBT.entrySet().removeIf(entry -> {
            CompoundTag tag = entry.getValue();
            if (tag.contains("LastUpdate")) {
                long lastUpdate = tag.getLong("LastUpdate");
                return (currentTime - lastUpdate) > maxAge;
            }
            return false; //如果没有时间戳，保留记录
        });

        setDirty();
    }

    //获取数据统计信息
    public String getStatistics() {
        int totalRecords = recordsNBT.size();
        int healNegatedCount = 0;

        for (CompoundTag tag : recordsNBT.values()) {
            if (tag.getBoolean("HealNegated")) {
                healNegatedCount++;
            }
        }

        return String.format("Total Records: %d, Heal Negated: %d",
                           totalRecords, healNegatedCount);
    }
}
