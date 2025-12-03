package net.the_last_sword.defence;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.util.EntityUtil;
import net.the_last_sword.util.TheLastSwordLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

//防御系统运行时管理器
public final class DefenceManager {

    //运行时数据容器（所有维度共享）- 使用UUID作为Key
    private static final ConcurrentHashMap<UUID, DefenceRecord> records = new ConcurrentHashMap<>();

    //3级实体NBT存储（用于复活）
    private static final ConcurrentHashMap<UUID, CompoundTag> level3EntityNBT = new ConcurrentHashMap<>();

    //数据变更标记
    private static volatile boolean needsSave = false;

    //清理系统相关变量
    private static long lastCleanupTime = 0;
    private static final long CLEANUP_INTERVAL = 30 * 20; // 30秒 (600 ticks)

    //3级实体NBT更新相关变量
    private static long lastNBTUpdateTime = 0;
    private static final long NBT_UPDATE_INTERVAL = 30 * 20; // 30秒 (600 ticks)

    /* ==================== 防御记录内部类 ==================== */

    //防御记录数据类（公开静态内部类，允许外部直接访问）
    public static final class DefenceRecord {
        int level;              //防御等级
        String encodedHealth;   //编码后的当前生命值（字符串存储）
        String encodedMaxHealth;//编码后的最大生命值（字符串存储）

        //编码基数
        private static final float BASE = -1024.0f;

        DefenceRecord(int level, float health, float maxHealth) {
            this.level = level;
            this.encodedHealth = encode(health);
            this.encodedMaxHealth = encode(maxHealth);
        }

        //编码：真实血量 -> 存储字符串
        private String encode(float value) {
            float stored = BASE - value;
            return Float.toString(stored);
        }

        //解码：存储字符串 -> 真实血量
        private float decode(String encoded) {
            float stored = Float.parseFloat(encoded);
            return BASE - stored;
        }

        public float getHealth() {
            return decode(encodedHealth);
        }

        void setHealth(float health) {
            this.encodedHealth = encode(health);
        }

        float getMaxHealth() {
            return decode(encodedMaxHealth);
        }

        void setMaxHealth(float maxHealth) {
            this.encodedMaxHealth = encode(maxHealth);
        }

        //序列化到NBT
        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("Level", level);
            tag.putString("EncodedHealth", encodedHealth);
            tag.putString("EncodedMaxHealth", encodedMaxHealth);
            return tag;
        }

        //从NBT反序列化
        static DefenceRecord load(CompoundTag tag) {
            int level = tag.getInt("Level");
            String encodedHealth = tag.getString("EncodedHealth");
            String encodedMaxHealth = tag.getString("EncodedMaxHealth");

            DefenceRecord record = new DefenceRecord(level, 0, 0);
            record.encodedHealth = encodedHealth;
            record.encodedMaxHealth = encodedMaxHealth;
            return record;
        }
    }

    public static boolean needsSave() {
        return needsSave;
    }

    public static void markSaved() {
        needsSave = false;
    }

    private static void markDirty() {
        needsSave = true;
    }

    //从NBT数据导入到运行时容器
    public static void importRecords(Map<UUID, CompoundTag> data) {
        if (data == null) return;

        for (Map.Entry<UUID, CompoundTag> entry : data.entrySet()) {
            UUID uuid = entry.getKey();
            CompoundTag tag = entry.getValue();
            DefenceRecord record = DefenceRecord.load(tag);
            records.put(uuid, record);
        }

        TheLastSwordLogger.info("Imported {} defence records to runtime", data.size());
    }

    //导出运行时数据为NBT格式
    public static Map<UUID, CompoundTag> exportRecords() {
        Map<UUID, CompoundTag> result = new HashMap<>();

        for (Map.Entry<UUID, DefenceRecord> entry : records.entrySet()) {
            UUID uuid = entry.getKey();
            DefenceRecord record = entry.getValue();
            CompoundTag tag = record.save();
            result.put(uuid, tag);
        }

        return result;
    }

    //导入3级实体NBT
    public static void importLevel3NBT(Map<UUID, CompoundTag> data) {
        if (data == null) return;
        level3EntityNBT.putAll(data);
        TheLastSwordLogger.info("Imported {} level 3 entity NBT to runtime", data.size());
    }

    //导出3级实体NBT
    public static Map<UUID, CompoundTag> exportLevel3NBT() {
        return new HashMap<>(level3EntityNBT);
    }

    //清空所有运行时记录
    public static void clearAll() {
        records.clear();
        level3EntityNBT.clear();
        TheLastSwordLogger.info("Cleared all defence records from runtime");
    }

    //注册实体到防御系统
    public static void register(LivingEntity entity, int level) {
        if (entity == null || entity.level().isClientSide) {
            return;
        }

        UUID uuid = entity.getUUID();

        if (records.containsKey(uuid)) {
            DefenceRecord record = records.get(uuid);
            if (record.level != level) {
                record.level = level;
                markDirty();

                //等级变化时，如果是3级且激进模式开启，保存NBT
                if (level == 3 && TheLastSwordConfiguration.DEFENCE_ENABLE_RADICAL_LOGIC.get()) {
                    saveLevel3EntityNBT(entity);
                }
            }
        } else {
            float health = EntityUtil.TheLastEndGetHealth(entity);
            float maxHealth = (float) entity.getAttribute(Attributes.MAX_HEALTH).getValue();

            DefenceRecord record = new DefenceRecord(level, health, maxHealth);
            records.put(uuid, record);
            markDirty();

            //新注册时，如果是3级且激进模式开启，保存NBT
            if (level == 3 && TheLastSwordConfiguration.DEFENCE_ENABLE_RADICAL_LOGIC.get()) {
                saveLevel3EntityNBT(entity);
            }
        }
    }

    //保存3级实体NBT
    private static void saveLevel3EntityNBT(LivingEntity entity) {
        if (entity == null) return;

        //玩家实体不保存NBT
        if (entity instanceof Player) {
            return;
        }

        try {
            CompoundTag nbt = new CompoundTag();
            entity.save(nbt);  //保存完整实体数据（包括物种、位置、属性等）

            //保存维度信息
            if (entity.level() instanceof ServerLevel serverLevel) {
                nbt.putString("Dimension", serverLevel.dimension().location().toString());
            }

            level3EntityNBT.put(entity.getUUID(), nbt);
            TheLastSwordLogger.info("Saved level 3 entity NBT: {}", entity.getUUID());
        } catch (Exception e) {
            TheLastSwordLogger.error("Failed to save level 3 entity NBT", e);
        }
    }

    //修改记录生命值
    public static void modifyHealth(LivingEntity entity, float newHealth) {
        if (entity == null) return;
        DefenceRecord record = records.get(entity.getUUID());
        if (record != null) {
            record.setHealth(newHealth);
            markDirty();
        }
    }

    //修改记录最大生命值
    public static void modifyMaxHealth(LivingEntity entity, float newMaxHealth) {
        if (entity == null) return;
        DefenceRecord record = records.get(entity.getUUID());
        if (record != null) {
            record.setMaxHealth(newMaxHealth);
            markDirty();
        }
    }

    //将记录数据推送到实体
    public static void pushToEntity(LivingEntity entity) {
        if (entity == null) return;

        DefenceRecord record = records.get(entity.getUUID());
        if (record == null) return;

        entity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(record.getMaxHealth());
        EntityUtil.theLastEndSetHealth(entity, record.getHealth());

        //激进模式：清除外部mod的数值型EntityData
        if (TheLastSwordConfiguration.getDefenceEnableRadicalLogicSafely()) {
            EntityUtil.clearExternalEntityData(entity);
        }
    }

    /* ==================== 安全访问API ==================== */

    //获取所有记录（直接返回引用，仅用于读取，不要修改！）
    public static Map<UUID, DefenceRecord> getAllRecords() {
        return records;
    }

    //检查实体是否有防御记录
    public static boolean hasDefenceRecord(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        return records.containsKey(entity.getUUID());
    }

    //通过UUID直接检查是否有防御记录（用于UUID验证）
    public static boolean hasDefenceRecordByUUID(UUID uuid) {
        if (uuid == null) {
            return false;
        }
        return records.containsKey(uuid);
    }

    //直接获取防御记录（允许外部直接访问record，避免方法调用的返回值问题）
    public static DefenceRecord getRecord(LivingEntity entity) {
        if (entity == null) {
            return null;
        }
        return records.get(entity.getUUID());
    }

    //通过UUID直接获取防御记录
    public static DefenceRecord getRecordByUUID(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return records.get(uuid);
    }

    //获取实体的当前生命值（没有记录时返回0）
    public static float getHealth(LivingEntity entity) {
        DefenceRecord record = getRecord(entity);
        if (record != null) {
            return record.getHealth();
        }
        return 0.0f;
    }

    //获取实体的最大生命值（没有记录时返回0）
    public static float getMaxHealth(LivingEntity entity) {
        if (entity == null) return 0.0f;
        DefenceRecord record = records.get(entity.getUUID());
        return record != null ? record.getMaxHealth() : 0.0f;
    }

    //获取实体的防御等级（没有记录时返回0是合理的）
    public static int getDefenceLevel(LivingEntity entity) {
        if (entity == null) return 0;
        DefenceRecord record = records.get(entity.getUUID());
        return record != null ? record.level : 0;
    }

    //获取所有有防御记录的实体UUID列表（用于遍历显示）
    public static java.util.Set<UUID> getAllDefenceEntityIds() {
        return new java.util.HashSet<>(records.keySet());
    }

    //清除防御记录（通过实体）
    public static void clear(LivingEntity entity) {
        if (entity != null && records.remove(entity.getUUID()) != null) {
            markDirty();
            TheLastSwordLogger.info("Cleared defence record for {}", entity.getUUID());
        }
    }

    //定期清理无效的防御记录
    public static void tick(net.minecraft.server.MinecraftServer server, long currentTime) {
        //定期清理无效记录
        if (currentTime - lastCleanupTime >= CLEANUP_INTERVAL) {
            cleanupInvalidRecords(server);
            lastCleanupTime = currentTime;
        }

        //定期更新3级实体NBT（仅在激进模式下）
        if (TheLastSwordConfiguration.DEFENCE_ENABLE_RADICAL_LOGIC.get()) {
            if (currentTime - lastNBTUpdateTime >= NBT_UPDATE_INTERVAL) {
                updateLevel3EntityNBT(server);
                lastNBTUpdateTime = currentTime;
            }

            //每tick检查3级实体是否被移除并复活
            checkAndReviveLevel3Entities(server);
        }
    }

    //定期更新3级实体NBT
    private static void updateLevel3EntityNBT(net.minecraft.server.MinecraftServer server) {
        int updatedCount = 0;

        for (Map.Entry<UUID, DefenceRecord> entry : records.entrySet()) {
            UUID uuid = entry.getKey();
            DefenceRecord record = entry.getValue();

            //只更新3级实体
            if (record.level != 3) {
                continue;
            }

            //查找实体
            LivingEntity entity = findEntityByUUID(server, uuid);
            if (entity == null || entity instanceof Player) {
                continue;
            }

            //保存NBT
            saveLevel3EntityNBT(entity);
            updatedCount++;
        }

        if (updatedCount > 0) {
            TheLastSwordLogger.info("Updated {} level 3 entity NBT", updatedCount);
        }
    }

    //检查并复活3级实体
    private static void checkAndReviveLevel3Entities(net.minecraft.server.MinecraftServer server) {
        for (Map.Entry<UUID, DefenceRecord> entry : records.entrySet()) {
            UUID uuid = entry.getKey();
            DefenceRecord record = entry.getValue();

            //只检查3级实体且血量大于0的
            if (record.level != 3 || record.getHealth() <= 0) {
                continue;
            }

            //查找实体
            LivingEntity entity = findEntityByUUID(server, uuid);

            //检查实体是否被移除
            if (entity == null || (entity.isRemoved() && !(entity instanceof Player))) {
                CompoundTag nbt = level3EntityNBT.get(uuid);
                //确保NBT存在且不是玩家实体
                if (nbt != null && !isPlayerEntity(nbt)) {
                    reviveLevel3Entity(uuid, server);
                }
            }
        }
    }

    //通过UUID查找实体
    private static LivingEntity findEntityByUUID(net.minecraft.server.MinecraftServer server, UUID uuid) {
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(uuid);
            if (entity instanceof LivingEntity livingEntity) {
                return livingEntity;
            }
        }
        return null;
    }

    //检查NBT是否为玩家实体
    private static boolean isPlayerEntity(CompoundTag nbt) {
        String entityId = nbt.getString("id");
        return "minecraft:player".equals(entityId);
    }

    //复活3级实体
    private static void reviveLevel3Entity(UUID uuid, net.minecraft.server.MinecraftServer server) {
        CompoundTag nbt = level3EntityNBT.get(uuid);
        if (nbt == null) {
            TheLastSwordLogger.warn("Cannot revive level 3 entity {}: NBT not found", uuid);
            return;
        }

        try {
            //从NBT恢复实体
            EntityType<?> entityType = EntityType.byString(nbt.getString("id")).orElse(null);

            if (entityType == null) {
                TheLastSwordLogger.error("Cannot revive level 3 entity {}: Unknown entity type", uuid);
                return;
            }

            //获取实体原来的维度
            String dimensionKey = nbt.getString("Dimension");
            ServerLevel targetLevel = null;

            if (!dimensionKey.isEmpty()) {
                ResourceLocation dimLocation = new ResourceLocation(dimensionKey);
                ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, dimLocation);
                targetLevel = server.getLevel(dimKey);
            }

            //如果找不到维度，使用主世界
            if (targetLevel == null) {
                targetLevel = server.overworld();
            }

            //创建实体
            Entity entity = entityType.create(targetLevel);
            if (entity instanceof LivingEntity livingEntity) {
                //加载NBT数据
                entity.load(nbt);

                //添加到世界
                targetLevel.addFreshEntity(entity);

                TheLastSwordLogger.info("Revived level 3 entity: {} in dimension {}",
                    uuid, targetLevel.dimension().location());
            } else {
                TheLastSwordLogger.error("Cannot revive level 3 entity {}: Not a LivingEntity", uuid);
            }
        } catch (Exception e) {
            TheLastSwordLogger.error("Failed to revive level 3 entity {}", uuid, e);
        }
    }

    //清理无效的防御记录
    private static void cleanupInvalidRecords(net.minecraft.server.MinecraftServer server) {
        if (records.isEmpty()) {
            return;
        }

        java.util.List<UUID> toRemove = new java.util.ArrayList<>();

        for (UUID uuid : records.keySet()) {
            //查找实体
            LivingEntity entity = findEntityByUUID(server, uuid);

            //检查实体是否已被移除或不存在
            if (entity == null || entity.isRemoved()) {
                toRemove.add(uuid);
            }
        }

        //移除无效记录
        for (UUID uuid : toRemove) {
            records.remove(uuid);
        }

        if (!toRemove.isEmpty()) {
            markDirty();
            TheLastSwordLogger.info("Cleaned up {} invalid defence records", toRemove.size());
        }
    }

    private DefenceManager() {}
}
