package net.the_last_sword.util;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.the_last_sword.summon.WraithSummonManager;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import net.minecraft.world.level.entity.EntityLookup;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.attack.AttackManager;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.defence.DefenceManager;
import net.the_last_sword.entity.TheLastEndEntity;
import net.the_last_sword.network.NetworkHandler;
import net.the_last_sword.network.TheLastEndRemoveClientPacket;
import net.the_last_sword.util.health.HealthFieldCache;
import net.the_last_sword.util.health.HealthGetterHook;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;


public class EntityUtil {

    //初始化与工具方法模块
    //VarHandle初始化 - 生命值模块
    private static final VarHandle DATA_ITEM_VALUE_HANDLE;
    private static final VarHandle DATA_ITEM_DIRTY_HANDLE;
    private static final VarHandle DEATH_TIME_HANDLE;
    private static final VarHandle DEAD_HANDLE;

    //VarHandle初始化 - 传送模块
    private static final VarHandle ENTITY_POSITION_HANDLE;
    private static final VarHandle ENTITY_X_OLD_HANDLE;
    private static final VarHandle ENTITY_Y_OLD_HANDLE;
    private static final VarHandle ENTITY_Z_OLD_HANDLE;
    private static final VarHandle ENTITY_BB_HANDLE;

    //VarHandle初始化 - 实体清除模块（服务端）
    private static final VarHandle SERVER_LEVEL_PLAYERS_HANDLE;
    private static final VarHandle SERVER_LEVEL_CHUNK_SOURCE_HANDLE;
    private static final VarHandle SERVER_LEVEL_ENTITY_TICK_LIST_HANDLE;
    private static final VarHandle SERVER_LEVEL_ENTITY_MANAGER_HANDLE;
    private static final VarHandle SERVER_LEVEL_NAVIGATING_MOBS_HANDLE;
    private static final VarHandle ENTITY_TICK_LIST_ACTIVE_HANDLE;
    private static final VarHandle ENTITY_TICK_LIST_PASSIVE_HANDLE;
    private static final VarHandle ENTITY_TICK_LIST_ITERATED_HANDLE;
    private static final VarHandle SERVER_CHUNK_CACHE_CHUNK_MAP_HANDLE;
    private static final VarHandle CHUNK_MAP_ENTITY_MAP_HANDLE;
    private static final VarHandle PERSISTENT_ENTITY_MANAGER_VISIBLE_STORAGE_HANDLE;
    private static final VarHandle PERSISTENT_ENTITY_MANAGER_KNOWN_UUIDS_HANDLE;
    private static final VarHandle PERSISTENT_ENTITY_MANAGER_SECTION_STORAGE_HANDLE;
    private static final VarHandle ENTITY_LOOKUP_BY_UUID_HANDLE;
    private static final VarHandle ENTITY_LOOKUP_BY_ID_HANDLE;
    private static final VarHandle ENTITY_SECTION_STORAGE_SECTIONS_HANDLE;
    private static final VarHandle ENTITY_SECTION_STORAGE_HANDLE;
    private static final VarHandle CLASS_INSTANCE_MULTI_MAP_BY_CLASS_HANDLE;

    //VarHandle初始化 - 实体清除模块（客户端）- 懒加载
    private static volatile boolean clientVarHandlesInitialized = false;
    private static VarHandle CLIENT_LEVEL_TICKING_ENTITIES_HANDLE;
    private static VarHandle CLIENT_LEVEL_ENTITY_STORAGE_HANDLE;
    private static VarHandle CLIENT_LEVEL_PLAYERS_HANDLE;
    private static VarHandle TRANSIENT_ENTITY_MANAGER_ENTITY_STORAGE_HANDLE;
    private static VarHandle TRANSIENT_ENTITY_MANAGER_SECTION_STORAGE_HANDLE;

    static {
        try {
            //生命值模块
            DATA_ITEM_VALUE_HANDLE = ObfuscationMappings.getVarHandle(SynchedEntityData.DataItem.class, "DataItem.value");
            DATA_ITEM_DIRTY_HANDLE = ObfuscationMappings.getVarHandle(SynchedEntityData.DataItem.class, "DataItem.dirty");
            DEATH_TIME_HANDLE = ObfuscationMappings.getVarHandle(LivingEntity.class, "LivingEntity.deathTime");
            DEAD_HANDLE = ObfuscationMappings.getVarHandle(LivingEntity.class, "LivingEntity.dead");

            //传送模块
            ENTITY_POSITION_HANDLE = ObfuscationMappings.getVarHandle(Entity.class, "Entity.position");
            ENTITY_X_OLD_HANDLE = ObfuscationMappings.getVarHandle(Entity.class, "Entity.xOld");
            ENTITY_Y_OLD_HANDLE = ObfuscationMappings.getVarHandle(Entity.class, "Entity.yOld");
            ENTITY_Z_OLD_HANDLE = ObfuscationMappings.getVarHandle(Entity.class, "Entity.zOld");
            ENTITY_BB_HANDLE = ObfuscationMappings.getVarHandle(Entity.class, "Entity.bb");

            //实体清除模块 - 服务端
            SERVER_LEVEL_PLAYERS_HANDLE = ObfuscationMappings.getVarHandle(ServerLevel.class, "ServerLevel.players");
            SERVER_LEVEL_CHUNK_SOURCE_HANDLE = ObfuscationMappings.getVarHandle(ServerLevel.class, "ServerLevel.chunkSource");
            SERVER_LEVEL_ENTITY_TICK_LIST_HANDLE = ObfuscationMappings.getVarHandle(ServerLevel.class, "ServerLevel.entityTickList");
            SERVER_LEVEL_ENTITY_MANAGER_HANDLE = ObfuscationMappings.getVarHandle(ServerLevel.class, "ServerLevel.entityManager");
            SERVER_LEVEL_NAVIGATING_MOBS_HANDLE = ObfuscationMappings.getVarHandle(ServerLevel.class, "ServerLevel.navigatingMobs");
            ENTITY_TICK_LIST_ACTIVE_HANDLE = ObfuscationMappings.getVarHandle(EntityTickList.class, "EntityTickList.active");
            ENTITY_TICK_LIST_PASSIVE_HANDLE = ObfuscationMappings.getVarHandle(EntityTickList.class, "EntityTickList.passive");
            ENTITY_TICK_LIST_ITERATED_HANDLE = ObfuscationMappings.getVarHandle(EntityTickList.class, "EntityTickList.iterated");
            SERVER_CHUNK_CACHE_CHUNK_MAP_HANDLE = ObfuscationMappings.getVarHandle(ServerChunkCache.class, "ServerChunkCache.chunkMap");
            CHUNK_MAP_ENTITY_MAP_HANDLE = ObfuscationMappings.getVarHandle(ChunkMap.class, "ChunkMap.entityMap");
            PERSISTENT_ENTITY_MANAGER_VISIBLE_STORAGE_HANDLE = ObfuscationMappings.getVarHandle(PersistentEntitySectionManager.class, "PersistentEntitySectionManager.visibleEntityStorage");
            PERSISTENT_ENTITY_MANAGER_KNOWN_UUIDS_HANDLE = ObfuscationMappings.getVarHandle(PersistentEntitySectionManager.class, "PersistentEntitySectionManager.knownUuids");
            PERSISTENT_ENTITY_MANAGER_SECTION_STORAGE_HANDLE = ObfuscationMappings.getVarHandle(PersistentEntitySectionManager.class, "PersistentEntitySectionManager.sectionStorage");
            ENTITY_LOOKUP_BY_UUID_HANDLE = ObfuscationMappings.getVarHandle(EntityLookup.class, "EntityLookup.byUuid");
            ENTITY_LOOKUP_BY_ID_HANDLE = ObfuscationMappings.getVarHandle(EntityLookup.class, "EntityLookup.byId");
            ENTITY_SECTION_STORAGE_SECTIONS_HANDLE = ObfuscationMappings.getVarHandle(EntitySectionStorage.class, "EntitySectionStorage.sections");
            ENTITY_SECTION_STORAGE_HANDLE = ObfuscationMappings.getVarHandle(EntitySection.class, "EntitySection.storage");
            CLASS_INSTANCE_MULTI_MAP_BY_CLASS_HANDLE = ObfuscationMappings.getVarHandle(ClassInstanceMultiMap.class, "ClassInstanceMultiMap.byClass");

        } catch (Exception e) {
            TheLastSwordLogger.error("Failed to initialize VarHandle for entity data access", e);
            throw new RuntimeException("Cannot initialize VarHandle", e);
        }
    }

    //初始化客户端VarHandle（懒加载，只在客户端环境调用）
    private static void initClientVarHandles() {
        if (clientVarHandlesInitialized) return;
        synchronized (EntityUtil.class) {
            if (clientVarHandlesInitialized) return;
            try {
                CLIENT_LEVEL_TICKING_ENTITIES_HANDLE = ObfuscationMappings.getVarHandle(ClientLevel.class, "ClientLevel.tickingEntities");
                CLIENT_LEVEL_ENTITY_STORAGE_HANDLE = ObfuscationMappings.getVarHandle(ClientLevel.class, "ClientLevel.entityStorage");
                CLIENT_LEVEL_PLAYERS_HANDLE = ObfuscationMappings.getVarHandle(ClientLevel.class, "ClientLevel.players");
                TRANSIENT_ENTITY_MANAGER_ENTITY_STORAGE_HANDLE = ObfuscationMappings.getVarHandle(TransientEntitySectionManager.class, "TransientEntitySectionManager.entityStorage");
                TRANSIENT_ENTITY_MANAGER_SECTION_STORAGE_HANDLE = ObfuscationMappings.getVarHandle(TransientEntitySectionManager.class, "TransientEntitySectionManager.sectionStorage");
                clientVarHandlesInitialized = true;
                TheLastSwordLogger.info("Client VarHandles initialized successfully");
            } catch (Exception e) {
                TheLastSwordLogger.error("Failed to initialize client VarHandles", e);
                throw new RuntimeException("Cannot initialize client VarHandles", e);
            }
        }
    }

    //生命值关键词白名单
    private static final Set<String> HEALTH_KEYWORDS = Set.of("health", "heal", "hp", "life", "vital");

    //生命值修改黑名单
    private static final Set<String> HEALTH_BLACKLIST_KEYWORDS = Set.of(
            "ai", "goal", "target", "brain", "memory", "sensor", "skill", "ability", "spell", "cast",
            "animation", "swing", "cooldown", "duration", "delay", "timer", "tick", "time",
            "age", "lifetime", "deathtime", "hurttime", "invulnerabletime", "hurt",
            //The Last Sword Mod 自身的 EntityDataAccessor 字段
            "level", "all_things_end", "is_spawned", "allow_moving", "shoot", "texture"
    );

    //VarHandle缓存（用于阶段2.5的字段访问优化）
    private static final Map<String, VarHandle> VAR_HANDLE_CACHE = new ConcurrentHashMap<>();

    //EntityDataAccessor缓存（用于阶段2的扫描优化）
    private static final Map<Class<?>, List<EntityDataAccessor<?>>> NEARBY_ACCESSOR_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, List<EntityDataAccessor<?>>> KEYWORD_ACCESSOR_CACHE = new ConcurrentHashMap<>();

    //检查字段名是否匹配健康白名单
    private static boolean matchesHealthWhitelist(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) return false;
        String lowerName = fieldName.toLowerCase();
        return HEALTH_KEYWORDS.stream().anyMatch(lowerName::contains);
    }

    //检查字段名是否匹配健康黑名单
    private static boolean matchesHealthBlacklist(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) return false;
        String lowerName = fieldName.toLowerCase();
        return HEALTH_BLACKLIST_KEYWORDS.stream().anyMatch(lowerName::contains);
    }

    //通过 dataId 查找字段名（复用现有遍历结构）
    private static String findFieldNameByDataId(Class<?> entityClass, int dataId) {
        for (Class<?> clazz = entityClass; clazz != null && clazz != Entity.class; clazz = clazz.getSuperclass()) {
            for (Field field : clazz.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    if (!EntityDataAccessor.class.isAssignableFrom(field.getType())) continue;
                    if (!Modifier.isStatic(field.getModifiers())) continue;

                    EntityDataAccessor<?> accessor = (EntityDataAccessor<?>) field.get(null);
                    if (accessor != null && accessor.getId() == dataId) {
                        return field.getName();
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }

    //判断是否应该修改 EntityData
    private static boolean shouldModifyEntityData(LivingEntity entity, int dataId) {
        // 1. 避免原版核心数据（ID <= 15）
        if (dataId <= 15) {
            return false;
        }

        // 2. 查找字段名
        String fieldName = findFieldNameByDataId(entity.getClass(), dataId);

        // 3. 如果找到字段名，检查黑名单
        if (fieldName != null && matchesHealthBlacklist(fieldName)) {
            return false;
        }

        // 4. 通过所有检查，允许修改
        return true;
    }

    //同步实体数据到客户端
    private static void safeSyncEntityDataToClients(Entity entity, ServerLevel serverLevel) {
        serverLevel.getChunkSource().broadcastAndSend(entity,
                new ClientboundSetEntityDataPacket(
                        entity.getId(), entity.getEntityData().getNonDefaultValues()
                )
        );
    }

    //清理外部 mod 添加的数值型 EntityData
    @SuppressWarnings("unchecked")
    public static void clearExternalEntityData(LivingEntity entity) {
        if (entity == null) return;

        try {
            SynchedEntityData entityData = entity.getEntityData();
            Map<Integer, SynchedEntityData.DataItem<?>> itemsById = entityData.itemsById;

            if (itemsById == null) return;

            boolean hasCleared = false;
            int vanillaHealthId = LivingEntity.DATA_HEALTH_ID.getId();

            // 遍历所有 DataItem，清除外部 mod 添加的数据
            for (Map.Entry<Integer, SynchedEntityData.DataItem<?>> entry : itemsById.entrySet()) {
                try {
                    int dataId = entry.getKey();
                    SynchedEntityData.DataItem<?> dataItem = entry.getValue();

                    // 跳过原版生命值
                    if (dataId == vanillaHealthId) continue;

                    // 检查是否应该修改此字段（ID>15 + 黑名单检查）
                    if (!shouldModifyEntityData(entity, dataId)) continue;

                    // 通过 DataItem 获取 accessor
                    Object accessor = dataItem.getAccessor();
                    if (!(accessor instanceof EntityDataAccessor)) continue;

                    EntityDataAccessor<?> entityDataAccessor = (EntityDataAccessor<?>) accessor;
                    Object currentValue = dataItem.getValue();

                    // 只清除 Integer/Float/Double 这 3 种数值类型
                    if (currentValue instanceof Integer) {
                        EntityDataAccessor<Integer> intAccessor = (EntityDataAccessor<Integer>) entityDataAccessor;
                        entityData.set(intAccessor, 0);
                        hasCleared = true;
                    } else if (currentValue instanceof Float) {
                        EntityDataAccessor<Float> floatAccessor = (EntityDataAccessor<Float>) entityDataAccessor;
                        entityData.set(floatAccessor, 0.0F);
                        hasCleared = true;
                    } else if (currentValue instanceof Double) {
                        EntityDataAccessor<Double> doubleAccessor = (EntityDataAccessor<Double>) entityDataAccessor;
                        entityData.set(doubleAccessor, 0.0D);
                        hasCleared = true;
                    }

                } catch (Exception ignored) {
                    // 忽略单个条目的异常，继续处理其他条目
                }
            }

            // 如果有数据被清除，同步到客户端
            if (hasCleared) {
                if (!entity.level().isClientSide && entity.level() instanceof ServerLevel serverLevel) {
                    safeSyncEntityDataToClients(entity, serverLevel);
                }
            }

        } catch (Exception e) {
            TheLastSwordLogger.error("External entity data clearing failed: {}", e.getMessage());
        }
    }

    //实体生命值模块
    //获取实体真实生命值，路径: entity → entityData.itemsById → DataItem → VarHandle.get(value)
    public static float TheLastEndGetHealth(LivingEntity entity) {
        if (entity == null) return 0.0f;

        try {
            SynchedEntityData.DataItem<?> dataItem = entity.getEntityData().itemsById.get(LivingEntity.DATA_HEALTH_ID.getId());
            return dataItem == null ? 0.0f : (Float) DATA_ITEM_VALUE_HANDLE.get(dataItem);
        } catch (Exception e) {
            TheLastSwordLogger.warn("Failed to get health via VarHandle for {}, falling back to getRecordHp()", entity.getType().getDescriptionId());
            return entity.getHealth();
        }
    }

    //设置实体生命值 - The Last End Set Health 完整阶段流程
    //阶段1：直接设置原版血量 DATA_HEALTH_ID
    //阶段2：修改所有符合条件的 EntityDataAccessor 和字段
    //       数值近似 OR 关键词白名单
    //阶段2.5：激进模式 - 修改所有非黑名单的数值字段
    //阶段3：字节码反向追踪
    //阶段1、2、2.5直接执行，在进入阶段3前验证一次
    public static boolean theLastEndSetHealth(LivingEntity entity, float expectedHealth) {
        if (entity == null) return false;

        try {
            //阶段1：修改原版血量 DATA_HEALTH_ID
            setBasicHealth(entity, expectedHealth);

            //玩家实体只执行阶段1
            if (entity instanceof Player) {
                return true;
            }

            //终焉实体早退：直接修改管理器，避免误伤自己的生命周期
            if (entity instanceof TheLastEndEntity && DefenceManager.hasDefenceRecord(entity) && DefenceManager.getDefenceLevel(entity) >= 1) {
                DefenceManager.modifyHealth(entity, expectedHealth);
                return true;
            }

            //阶段2：修改所有符合条件的 EntityDataAccessor 和实例字段（数值近似 OR 关键词）
            setHealthViaPhase2(entity, expectedHealth);

            //阶段2.5：激进模式（如果启用）
            boolean radicalEnabled = TheLastSwordConfiguration.getAttackEnableRadicalLogicSafely();
            if (radicalEnabled) {
                scanAndModifyAllInstanceFields(entity, expectedHealth);
            }

            //在进入阶段3前验证：如果前面阶段已经成功，跳过阶段3
            boolean phase2Success = verifyHealthChange(entity, expectedHealth);
            if (phase2Success) {
                return true;
            }

            //阶段3：字节码反向追踪（最后手段）
            setHealthViaPhase3(entity, expectedHealth);

            //最终验证
            return verifyHealthChange(entity, expectedHealth);

        } catch (Exception e) {
            return false;
        }
    }

    //验证血量修改是否成功
    //使用 entity.getRecordHp() 获取实际血量（经过所有 Mixin/Hook 的最终返回值）
    private static boolean verifyHealthChange(LivingEntity entity, float expectedHealth) {
        try {
            float actualHealth = entity.getHealth();
            //允许10.0的误差
            return Math.abs(actualHealth - expectedHealth) <= 10.0f;
        } catch (Exception e) {
            return false;
        }
    }

    //第1阶段：直接设置原版血量（DATA_HEALTH_ID）
    private static void setBasicHealth(LivingEntity entity, float expectedHealth) {
        try {
            SynchedEntityData entityData = entity.getEntityData();
            SynchedEntityData.DataItem<?> dataItem = entityData.itemsById.get(LivingEntity.DATA_HEALTH_ID.getId());
            if (dataItem == null) return;
            DATA_ITEM_VALUE_HANDLE.set(dataItem, expectedHealth);
            entity.onSyncedDataUpdated(LivingEntity.DATA_HEALTH_ID);
            DATA_ITEM_DIRTY_HANDLE.set(dataItem, true);
            entityData.isDirty = true;
        } catch (Exception ignored) {
        }
    }

    //同步原版 DATA_HEALTH_ID（不验证，只修改）
    private static void syncDataHealthId(LivingEntity entity, float expectedHealth) {
        try {
            SynchedEntityData entityData = entity.getEntityData();
            SynchedEntityData.DataItem<?> dataItem = entityData.itemsById.get(LivingEntity.DATA_HEALTH_ID.getId());
            if (dataItem == null) return;
            DATA_ITEM_VALUE_HANDLE.set(dataItem, expectedHealth);
            entity.onSyncedDataUpdated(LivingEntity.DATA_HEALTH_ID);
            DATA_ITEM_DIRTY_HANDLE.set(dataItem, true);
            entityData.isDirty = true;
        } catch (Exception ignored) {
        }
    }


    //第2阶段：修改所有符合条件的 EntityDataAccessor 和实例字段
    //条件：数值近似 OR 关键词白名单，支持嵌套扫描
    private static void setHealthViaPhase2(LivingEntity entity, float expectedHealth) {
        try {
            float currentHealth = entity.getHealth();

            //1. 修改所有数值近似的 EntityDataAccessor
            List<EntityDataAccessor<?>> nearbyAccessors = findNearbyNumericAccessors(entity);
            for (EntityDataAccessor<?> acc : nearbyAccessors) {
                setAccessorValue(entity, acc, expectedHealth);
            }

            //2. 修改所有关键词匹配的 EntityDataAccessor
            List<EntityDataAccessor<?>> keywordAccessors = findHealthKeywordAccessors(entity);
            for (EntityDataAccessor<?> acc : keywordAccessors) {
                setAccessorValue(entity, acc, expectedHealth);
            }

            //3. 扫描并修改所有符合条件的实例字段（数值近似 OR 关键词，支持嵌套）
            scanAndModifyIntelligentFields(entity, currentHealth, expectedHealth);

        } catch (Exception ignored) {
        }
    }

    //扫描并修改符合智能条件的字段（数值近似 OR 关键词，支持嵌套）
    private static void scanAndModifyIntelligentFields(LivingEntity entity, float currentHealth, float expectedHealth) {
        Set<Object> scannedObjects = new HashSet<>();
        scanAndModifyIntelligentFieldsRecursive(entity, entity.getClass(), currentHealth, expectedHealth, scannedObjects, 0);
    }

    //递归扫描并修改符合条件的字段
    private static void scanAndModifyIntelligentFieldsRecursive(Object targetObject, Class<?> targetClass,
                                                                float currentHealth, float expectedHealth,
                                                                Set<Object> scannedObjects, int nestingLevel) {
        final int MAX_NESTING_LEVEL = 3;
        if (nestingLevel > MAX_NESTING_LEVEL) {
            return;
        }

        if (nestingLevel > 0 && scannedObjects.contains(targetObject)) {
            return;
        }

        if (nestingLevel > 0) {
            scannedObjects.add(targetObject);
        }

        try {
            Class<?> currentClass = targetClass;
            int inheritanceLevel = 0;
            final int MAX_INHERITANCE_LEVEL = 5;

            while (currentClass != null && inheritanceLevel < MAX_INHERITANCE_LEVEL) {
                Field[] fields = currentClass.getDeclaredFields();

                for (Field field : fields) {
                    try {
                        if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                            continue;
                        }

                        String fieldName = field.getName();
                        Class<?> fieldType = field.getType();

                        if (matchesHealthBlacklist(fieldName)) {
                            continue;
                        }

                        field.setAccessible(true);

                        //处理数值类型
                        if (fieldType == float.class || fieldType == Float.class ||
                                fieldType == double.class || fieldType == Double.class ||
                                fieldType == int.class || fieldType == Integer.class) {

                            Object value = field.get(targetObject);
                            if (value instanceof Number) {
                                float fieldValue = ((Number) value).floatValue();

                                //条件1：数值近似匹配 OR 条件2：关键词匹配
                                if (Math.abs(fieldValue - currentHealth) <= 10.0f || matchesHealthWhitelist(fieldName)) {
                                    setFieldViaVarHandle(targetObject, field, expectedHealth);
                                }
                            }
                        }
                        //嵌套扫描
                        else if (!fieldType.isPrimitive() &&
                                !fieldType.getName().startsWith("java.") &&
                                !fieldType.getName().startsWith("javax.") &&
                                !fieldType.getName().startsWith("jdk.") &&
                                !fieldType.getName().startsWith("sun.") &&
                                !fieldType.getName().startsWith("net.minecraft.")) {  //排除 Minecraft 内置类

                            Object nestedObject = field.get(targetObject);
                            if (nestedObject != null && !scannedObjects.contains(nestedObject)) {
                                scanAndModifyIntelligentFieldsRecursive(nestedObject, nestedObject.getClass(),
                                        currentHealth, expectedHealth, scannedObjects, nestingLevel + 1);
                            }
                        }

                    } catch (Exception ignored) {
                    }
                }

                currentClass = currentClass.getSuperclass();
                inheritanceLevel++;

                if (currentClass == LivingEntity.class || currentClass == null) {
                    break;
                }
            }

        } catch (Exception ignored) {
        }
    }


    //使用 VarHandle 设置字段值（根据类型自动选择），失败时降级到反射
    private static boolean setFieldViaVarHandle(Object targetObject, Field field, float value) {
        try {
            Class<?> fieldType = field.getType();
            Class<?> declaringClass = field.getDeclaringClass();

            boolean success = false;

            if (fieldType == float.class || fieldType == Float.class) {
                success = setFloatFieldViaVarHandle(targetObject, declaringClass, field, value);
            } else if (fieldType == double.class || fieldType == Double.class) {
                success = setDoubleFieldViaVarHandle(targetObject, declaringClass, field, (double) value);
            } else if (fieldType == int.class || fieldType == Integer.class) {
                success = setIntFieldViaVarHandle(targetObject, declaringClass, field, (int) value);
            }

            if (!success) {
                try {
                    field.setAccessible(true);
                    if (fieldType == float.class || fieldType == Float.class) {
                        field.setFloat(targetObject, value);
                    } else if (fieldType == double.class || fieldType == Double.class) {
                        field.setDouble(targetObject, (double) value);
                    } else if (fieldType == int.class || fieldType == Integer.class) {
                        field.setInt(targetObject, (int) value);
                    }
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }

            return success;
        } catch (Exception e) {
            return false;
        }
    }

    //第2.5阶段：扫描并修改所有实例字段
    private static int scanAndModifyAllInstanceFields(LivingEntity entity, float expectedHealth) {
        Set<Object> scannedObjects = new HashSet<>();
        int totalModified = 0;
        float currentHealth = entity.getHealth();

        Class<?> currentClass = entity.getClass();
        int inheritanceLevel = 0;
        final int MAX_INHERITANCE_LEVEL = 5;

        while (currentClass != null && inheritanceLevel < MAX_INHERITANCE_LEVEL) {
            int modified = scanAndModifyFieldsInClass(entity, currentClass, currentHealth, expectedHealth, scannedObjects, 0);
            totalModified += modified;

            currentClass = currentClass.getSuperclass();
            inheritanceLevel++;

            if (currentClass == LivingEntity.class || currentClass == null) {
                break;
            }
        }

        return totalModified;
    }

    //扫描并修改指定类的所有数值字段
    private static int scanAndModifyFieldsInClass(Object targetObject, Class<?> targetClass,
                                                  float currentHealth, float expectedHealth, Set<Object> scannedObjects, int nestingLevel) {
        final int MAX_NESTING_LEVEL = 3;
        if (nestingLevel > MAX_NESTING_LEVEL) {
            return 0;
        }

        //只在嵌套层级>0时才检查是否已扫描
        if (nestingLevel > 0 && scannedObjects.contains(targetObject)) {
            return 0;
        }

        //只在嵌套层级>0时才添加到已扫描列表
        if (nestingLevel > 0) {
            scannedObjects.add(targetObject);
        }

        int modifiedCount = 0;

        try {
            Field[] fields = targetClass.getDeclaredFields();

            for (Field field : fields) {
                try {
                    String fieldName = field.getName();
                    Class<?> fieldType = field.getType();
                    int modifiers = field.getModifiers();

                    //跳过静态字段
                    if (Modifier.isStatic(modifiers)) {
                        continue;
                    }

                    //跳过final字段
                    if (Modifier.isFinal(modifiers)) {
                        continue;
                    }

                    //处理数值类型字段
                    if (fieldType == double.class || fieldType == Double.class) {
                        field.setAccessible(true);
                        if (shouldModifyNumericField(targetObject, field, fieldName, currentHealth, expectedHealth)) {
                            if (setDoubleFieldViaVarHandle(targetObject, field.getDeclaringClass(), field, (double) expectedHealth)) {
                                modifiedCount++;
                            }
                        }
                    }
                    else if (fieldType == float.class || fieldType == Float.class) {
                        field.setAccessible(true);
                        if (shouldModifyNumericField(targetObject, field, fieldName, currentHealth, expectedHealth)) {
                            if (setFloatFieldViaVarHandle(targetObject, field.getDeclaringClass(), field, expectedHealth)) {
                                modifiedCount++;
                            }
                        }
                    }
                    else if (fieldType == int.class || fieldType == Integer.class) {
                        field.setAccessible(true);
                        if (shouldModifyNumericField(targetObject, field, fieldName, currentHealth, expectedHealth)) {
                            if (setIntFieldViaVarHandle(targetObject, field.getDeclaringClass(), field, (int) expectedHealth)) {
                                modifiedCount++;
                            }
                        }
                    }
                    //嵌套扫描：如果字段是对象类型，递归扫描
                    else if (!fieldType.isPrimitive() &&
                            !fieldType.getName().startsWith("java.") &&
                            !fieldType.getName().startsWith("javax.") &&
                            !fieldType.getName().startsWith("jdk.") &&
                            !fieldType.getName().startsWith("sun.") &&
                            !fieldType.getName().startsWith("net.minecraft.")) {
                        field.setAccessible(true);
                        Object nestedObject = field.get(targetObject);
                        if (nestedObject != null && !scannedObjects.contains(nestedObject)) {
                            modifiedCount += scanAndModifyFieldsInClass(nestedObject, nestedObject.getClass(),
                                    currentHealth, expectedHealth, scannedObjects, nestingLevel + 1);
                        }
                    }

                } catch (Exception ignored) {
                }
            }

        } catch (Exception e) {
            TheLastSwordLogger.error("[Phase2.5] FATAL: Exception in scanAndModifyFieldsInClass for {}: {}",
                    targetClass.getSimpleName(), e.toString());
        }

        return modifiedCount;
    }

    //判断是否应该修改数值字段（激进模式2.5）
    private static boolean shouldModifyNumericField(Object targetObject, Field field, String fieldName,
                                                    float currentHealth, float expectedHealth) {
        try {
            //激进模式：只要不在黑名单，就修改所有数值字段
            //这样可以覆盖所有自定义血量系统，无论字段名是什么
            if (matchesHealthBlacklist(fieldName)) {
                return false;
            }

            //只要字段有值，就修改
            Object currentValue = field.get(targetObject);
            if (currentValue instanceof Number) {
                return true;
            }

            return false;

        } catch (Exception e) {
            return false;
        }
    }

    //使用VarHandle设置float字段（带缓存优化）
    private static boolean setFloatFieldViaVarHandle(Object targetObject, Class<?> targetClass, Field field, float value) {
        try {
            String cacheKey = targetClass.getName() + "#" + field.getName() + "#float";
            VarHandle handle = VAR_HANDLE_CACHE.computeIfAbsent(cacheKey, k -> {
                try {
                    return MethodHandles.privateLookupIn(targetClass, MethodHandles.lookup())
                            .findVarHandle(targetClass, field.getName(), float.class);
                } catch (Exception e) {
                    return null;
                }
            });
            if (handle != null) {
                handle.set(targetObject, value);
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    //使用VarHandle设置double字段（带缓存优化）
    private static boolean setDoubleFieldViaVarHandle(Object targetObject, Class<?> targetClass, Field field, double value) {
        try {
            String cacheKey = targetClass.getName() + "#" + field.getName() + "#double";
            VarHandle handle = VAR_HANDLE_CACHE.computeIfAbsent(cacheKey, k -> {
                try {
                    return MethodHandles.privateLookupIn(targetClass, MethodHandles.lookup())
                            .findVarHandle(targetClass, field.getName(), double.class);
                } catch (Exception e) {
                    return null;
                }
            });
            if (handle != null) {
                handle.set(targetObject, value);
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    //使用VarHandle设置int字段（带缓存优化）
    private static boolean setIntFieldViaVarHandle(Object targetObject, Class<?> targetClass, Field field, int value) {
        try {
            String cacheKey = targetClass.getName() + "#" + field.getName() + "#int";
            VarHandle handle = VAR_HANDLE_CACHE.computeIfAbsent(cacheKey, k -> {
                try {
                    return MethodHandles.privateLookupIn(targetClass, MethodHandles.lookup())
                            .findVarHandle(targetClass, field.getName(), int.class);
                } catch (Exception e) {
                    return null;
                }
            });
            if (handle != null) {
                handle.set(targetObject, value);
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    //查找数值接近生命值的数据访问器（数值近似匹配）
    private static List<EntityDataAccessor<?>> findNearbyNumericAccessors(LivingEntity entity) {
        Class<?> entityClass = entity.getClass();

        //检查缓存
        List<EntityDataAccessor<?>> cached = NEARBY_ACCESSOR_CACHE.get(entityClass);
        if (cached != null) {
            return cached;
        }

        //缓存未命中，执行扫描
        List<EntityDataAccessor<?>> result = new ArrayList<>();
        float entityHealth = entity.getHealth();
        int vanillaHealthId = LivingEntity.DATA_HEALTH_ID.getId();

        for (Class<?> clazz = entityClass; clazz != null && clazz != Entity.class; clazz = clazz.getSuperclass()) {
            for (Field field : clazz.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    if (!EntityDataAccessor.class.isAssignableFrom(field.getType())) continue;
                    if (!Modifier.isStatic(field.getModifiers())) continue;

                    EntityDataAccessor<?> accessor = (EntityDataAccessor<?>) field.get(null);

                    //跳过原版DATA_HEALTH_ID
                    if (accessor.getId() == vanillaHealthId) continue;

                    Object value = entity.getEntityData().get(accessor);
                    //匹配所有数字类型（Float/Int/Double）
                    if (value instanceof Number) {
                        float numericValue = ((Number) value).floatValue();
                        //数值相近getHealth()（误差10.0f）
                        if (Math.abs(numericValue - entityHealth) <= 10.0f) {
                            result.add(accessor);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }

        //缓存结果
        NEARBY_ACCESSOR_CACHE.put(entityClass, result);
        return result;
    }

    //查找包含生命值关键词的数据访问器（关键词匹配）
    //扫描包括LivingEntity，以捕获Mixin注入的字段
    private static List<EntityDataAccessor<?>> findHealthKeywordAccessors(LivingEntity entity) {
        Class<?> entityClass = entity.getClass();

        //检查缓存
        List<EntityDataAccessor<?>> cached = KEYWORD_ACCESSOR_CACHE.get(entityClass);
        if (cached != null) {
            return cached;
        }

        //缓存未命中，执行扫描
        List<EntityDataAccessor<?>> result = new ArrayList<>();
        int vanillaHealthId = LivingEntity.DATA_HEALTH_ID.getId();

        for (Class<?> clazz = entityClass; clazz != null && clazz != Entity.class; clazz = clazz.getSuperclass()) {
            for (Field field : clazz.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    if (!EntityDataAccessor.class.isAssignableFrom(field.getType())) continue;
                    if (!Modifier.isStatic(field.getModifiers())) continue;

                    EntityDataAccessor<?> accessor = (EntityDataAccessor<?>) field.get(null);

                    //跳过原版DATA_HEALTH_ID
                    if (accessor.getId() == vanillaHealthId) continue;

                    String fieldName = field.getName();
                    //白名单关键词匹配
                    if (matchesHealthWhitelist(fieldName)) {
                        Object value = entity.getEntityData().get(accessor);
                        //匹配所有数字类型（Float/Int/Double）
                        if (value instanceof Number) {
                            result.add(accessor);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }

        //缓存结果
        KEYWORD_ACCESSOR_CACHE.put(entityClass, result);
        return result;
    }

    //类型安全地设置EntityDataAccessor的值
    @SuppressWarnings("unchecked")
    private static void setAccessorValue(LivingEntity entity, EntityDataAccessor<?> accessor, float expectedHealth) {
        try {
            if (accessor.getSerializer() == EntityDataSerializers.FLOAT) {
                EntityDataAccessor<Float> floatAccessor = (EntityDataAccessor<Float>) accessor;
                entity.getEntityData().set(floatAccessor, expectedHealth);
            } else if (accessor.getSerializer() == EntityDataSerializers.INT) {
                EntityDataAccessor<Integer> intAccessor = (EntityDataAccessor<Integer>) accessor;
                entity.getEntityData().set(intAccessor, (int) expectedHealth);
            } else {
                Object currentValue = entity.getEntityData().get(accessor);
                if (currentValue instanceof Double) {
                    EntityDataAccessor<Double> doubleAccessor = (EntityDataAccessor<Double>) accessor;
                    entity.getEntityData().set(doubleAccessor, (double) expectedHealth);
                }
            }
        } catch (Exception ignored) {
        }
    }

    //第3阶段：字节码反向追踪修改血量
    //通过触发getHealth()调用，让HealthGetterHook分析字节码并创建缓存
    //然后使用缓存中的writePath直接修改底层存储
    private static void setHealthViaPhase3(LivingEntity entity, float expectedHealth) {
        try {
            Class<?> entityClass = entity.getClass();

            //检查是否已有缓存
            HealthFieldCache cache = HealthGetterHook.getCache(entityClass);

            if (cache == null) {
                //没有缓存，直接触发分析
                HealthGetterHook.triggerAnalysis(entity);
                //再次尝试获取缓存
                cache = HealthGetterHook.getCache(entityClass);
                if (cache == null) {
                    TheLastSwordLogger.warn("[Phase3] Analysis failed for {}, no cache created", entityClass.getSimpleName());
                    return;
                }
            }

            //应用逆向公式
            float valueToWrite = expectedHealth;
            if (cache.reverseTransform != null) {
                //限制最低为0，防止逆向公式计算出的值超出字段范围
                float clampedHealth = Math.max(0.0f, expectedHealth);
                valueToWrite = cache.reverseTransform.apply(clampedHealth);
            }

            //检测到容器，使用主动扫描
            if (cache.containerDetected) {
                scanAndModifyHealthContainer(entity, valueToWrite, cache);

                //同步修改原版 DATA_HEALTH_ID，确保原版伤害系统正常工作
                syncDataHealthId(entity, expectedHealth);

                //如果实体处于禁疗状态，同步更新禁疗记录
                if (AttackManager.isHealNegated(entity)) {
                    AttackManager.updateHealNegationHealth(entity, expectedHealth);
                }

                return;
            }

            //字段访问路径
            if (cache.writePath != null) {
                cache.writePath.apply(entity, valueToWrite);

                //同步修改原版 DATA_HEALTH_ID
                syncDataHealthId(entity, expectedHealth);
                //如果实体处于禁疗状态，同步更新禁疗记录
                if (AttackManager.isHealNegated(entity)) {
                    AttackManager.updateHealNegationHealth(entity, expectedHealth);
                }

                return;
            }

            //No available modification strategy - 正常情况，某些实体有特殊的血量管理方式

        } catch (Exception e) {
            TheLastSwordLogger.error("[Phase3] Exception during modification", e);
        }
    }

    //主动扫描容器修改血量
    private static void scanAndModifyHealthContainer(LivingEntity entity, float expectedHealth, HealthFieldCache cache) {
        try {
            //获取容器类和方法
            Class<?> containerClass = Class.forName(cache.containerClass);
            Method getterMethod = containerClass.getDeclaredMethod(cache.containerGetterMethod);
            getterMethod.setAccessible(true);

            //调用 getter 获取 Map 实例
            Object mapInstance = getterMethod.invoke(null);
            if (mapInstance == null) {
                TheLastSwordLogger.warn("[Phase3Scan] Container instance is null");
                return;
            }

            if (!(mapInstance instanceof Map)) {
                TheLastSwordLogger.warn("[Phase3Scan] Container is not a Map: {}", mapInstance.getClass().getName());
                return;
            }

            Map<?, ?> healthMap = (Map<?, ?>) mapInstance;

            //直接使用当前实体作为 key 查找
            if (healthMap.containsKey(entity)) {
                //使用 VarHandle 直接修改 Entry.value
                modifyMapValueViaVarHandle(healthMap, entity, expectedHealth);
                return;
            }

            //如果直接 key 查找失败，尝试其他可能的 key
            Object[] possibleKeys = buildPossibleKeys(entity);

            for (Object key : possibleKeys) {
                if (key == null || key == entity) continue;

                if (healthMap.containsKey(key)) {
                    modifyMapValueViaVarHandle(healthMap, key, expectedHealth);
                    return;
                }
            }

            TheLastSwordLogger.warn("[Phase3Scan] No matching key found in map for entity {}", entity);

        } catch (Exception e) {
            TheLastSwordLogger.error("[Phase3Scan] Exception during scan", e);
        }
    }

    //构建所有可能的 key
    private static Object[] buildPossibleKeys(LivingEntity entity) {
        try {
            return new Object[]{
                    entity,
                    entity.getUUID(),
                    entity.getId(),
                    entity.getClass().getName(),
                    entity.getClass().getSimpleName()
            };
        } catch (Exception e) {
            return new Object[]{entity};
        }
    }

    //使用 VarHandle 直接修改 Map 中的值
    private static boolean modifyMapValueViaVarHandle(Map<?, ?> map, Object key, float newValue) {
        try {
            //根据 Map 类型选择不同的 VarHandle
            if (map instanceof WeakHashMap) {
                return modifyWeakHashMapValue((WeakHashMap<?, ?>) map, key, newValue);
            } else if (map instanceof HashMap) {
                return modifyHashMapValue((HashMap<?, ?>) map, key, newValue);
            } else {
                TheLastSwordLogger.warn("[Phase3Scan] Unsupported map type: {}", map.getClass().getName());
                return false;
            }
        } catch (Exception e) {
            TheLastSwordLogger.error("[Phase3Scan] Failed to modify map value", e);
            return false;
        }
    }

    //修改 HashMap 的值（使用 VarHandle）
    private static boolean modifyHashMapValue(HashMap<?, ?> map, Object key, float newValue) {
        try {
            //初始化 VarHandle
            HealthGetterHook.initHashMapVarHandles(map.getClass());

            //使用 VarHandle 获取 table
            Object[] table = (Object[]) HealthGetterHook.getHashMapTable(map);
            if (table == null || table.length == 0) {
                return false;
            }

            //遍历整个 table 数组
            for (int i = 0; i < table.length; i++) {
                Object node = table[i];

                //遍历当前 bucket 的链表
                while (node != null) {
                    Object nodeKey = HealthGetterHook.getHashMapNodeKey(node);

                    if (key.equals(nodeKey) || key == nodeKey) {
                        //使用 VarHandle 修改 value
                        HealthGetterHook.setHashMapNodeValue(node, newValue);
                        return true;
                    }

                    //移动到下一个节点
                    node = HealthGetterHook.getHashMapNodeNext(node);
                }
            }

            return false;
        } catch (Exception e) {
            TheLastSwordLogger.error("[Phase3Scan] HashMap modification failed", e);
            return false;
        }
    }

    //修改 WeakHashMap 的值（使用 VarHandle）
    private static boolean modifyWeakHashMapValue(WeakHashMap<?, ?> map, Object key, float newValue) {
        try {
            //初始化 VarHandle
            HealthGetterHook.initHashMapVarHandles(map.getClass());

            //使用 VarHandle 获取 table
            Object[] table = (Object[]) HealthGetterHook.getWeakHashMapTable(map);
            if (table == null || table.length == 0) {
                return false;
            }

            //遍历整个 table 数组
            for (int i = 0; i < table.length; i++) {
                Object entry = table[i];

                //遍历当前 bucket 的链表
                while (entry != null) {
                    Object entryKey = HealthGetterHook.getWeakHashMapEntryKey(entry);

                    if (key.equals(entryKey) || key == entryKey) {
                        //使用 VarHandle 修改 value
                        HealthGetterHook.setWeakHashMapEntryValue(entry, newValue);
                        return true;
                    }

                    //移动到下一个节点
                    entry = HealthGetterHook.getWeakHashMapEntryNext(entry);
                }
            }

            return false;
        } catch (Exception e) {
            TheLastSwordLogger.error("[Phase3Scan] WeakHashMap modification failed", e);
            return false;
        }
    }

    //HashMap hash 函数
    private static int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }


    //实体死亡模块
    //设置实体死亡状态
    public static void theLastEndSetDead(LivingEntity entity, DamageSource damageSource) {
        if (entity == null||damageSource == null) {
            return;
        }
        theLastEndSetHealth(entity, 0.0f);

        //根据DamageSource设置伤害相关字段
        Entity sourceEntity = damageSource.getEntity();
        if (sourceEntity != null) {
            //设置lastHurtByMob（用于反击目标）
            if (sourceEntity instanceof LivingEntity livingSource) {
                entity.setLastHurtByMob(livingSource);
            }

            //仅当攻击者是玩家时设置lastHurtByPlayer（用于掉落物和经验）
            if (sourceEntity instanceof Player player) {
                entity.setLastHurtByPlayer(player);
            }
        }

        //添加实体类型到禁复活表（重复添加不叠加时长）
        AttackManager.addReviveBan(entity);

        entity.die(damageSource);
        entity.setPose(Pose.DYING);
        if (!entity.level().isClientSide && TheLastSwordConfiguration.getDeathParticleEffectSafely()) {
            ParticleUtil.spawnDeathParticles(entity);
        }
        if (TheLastSwordConfiguration.getDieMessageSafely()) {
            sendDeathMessage(entity, damageSource);
        }
        triggerKillAdvancement(entity, damageSource);
        entity.dropAllDeathLoot(damageSource);
        setDeathFieldsViaVarHandle(entity, damageSource);
        theLastEndRemove(entity,Entity.RemovalReason.KILLED);
    }
    //复活实体（清除死亡状态）
    public static void theLastEndRevive(LivingEntity entity) {
        if (entity == null) {
            return;
        }
        try {
            DEAD_HANDLE.set(entity, false);
            DEATH_TIME_HANDLE.set(entity, 0);

        } catch (Exception e) {
            TheLastSwordLogger.error("Failed to revive entity via VarHandle", e);
        }
    }

    //使用VarHandle设置死亡相关字段
    private static void setDeathFieldsViaVarHandle(LivingEntity entity, DamageSource damageSource) {
        try {
            DEAD_HANDLE.set(entity, true);
            DEATH_TIME_HANDLE.set(entity, 0);
        } catch (Exception e) {
            TheLastSwordLogger.error("Failed to set death fields via VarHandle", e);
        }
    }



    //触发击杀成就
    private static void triggerKillAdvancement(LivingEntity entity, DamageSource damageSource) {
        try {
            ServerPlayer killerPlayer = null;

            if (damageSource.getEntity() instanceof ServerPlayer player) {
                killerPlayer = player;
            } else {
                Entity sourceEntity = damageSource.getEntity();
                if (sourceEntity != null) {
                    LivingEntity owner = findOwner(sourceEntity);
                    if (owner instanceof ServerPlayer serverPlayer) {
                        killerPlayer = serverPlayer;
                    }
                }
            }

            if (killerPlayer != null) {
                CriteriaTriggers.PLAYER_KILLED_ENTITY.trigger(killerPlayer, entity, damageSource);
            }
        } catch (Exception ignored) {
        }
    }

    //发送死亡消息
    private static void sendDeathMessage(LivingEntity entity, DamageSource damageSource) {
        try {
            if (!entity.level().isClientSide && entity.level().getServer() != null) {
                Level level = entity.level();
                Component deathMessage;

                ItemStack weapon = ItemStack.EMPTY;
                if (damageSource.getEntity() instanceof LivingEntity attacker) {
                    weapon = attacker.getMainHandItem();
                }

                if (!weapon.isEmpty()) {
                    deathMessage = Component.translatable(
                            "death.attack.absolute_destruction.item",
                            entity.getDisplayName(),
                            damageSource.getEntity() != null ? damageSource.getEntity().getDisplayName() : Component.literal("Unknown"),
                            weapon.getDisplayName()
                    );
                } else if (damageSource.getDirectEntity() instanceof Player) {
                    deathMessage = Component.translatable(
                            "death.attack.absolute_destruction.player",
                            entity.getDisplayName(),
                            damageSource.getDirectEntity().getDisplayName()
                    );
                } else if (damageSource.getEntity() != null) {
                    deathMessage = Component.translatable(
                            "death.attack.absolute_destruction.player",
                            entity.getDisplayName(),
                            damageSource.getEntity().getDisplayName()
                    );
                } else {
                    deathMessage = Component.translatable(
                            "death.attack.absolute_destruction",
                            entity.getDisplayName()
                    );
                }

                if (level.getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES)) {
                    PlayerList playerList = level.getServer().getPlayerList();
                    playerList.broadcastSystemMessage(deathMessage, false);
                }
            }
        } catch (Exception ignored) {
        }
    }

    //实体传送模块

    //使用VarHandle直接传送实体到指定位置
    public static boolean theLastEndTeleport(Entity entity, double x, double y, double z) {
        if (entity == null) return false;

        try {
            Vec3 newPosition = new Vec3(x, y, z);

            //修改核心位置字段(使用VarHandle)
            ENTITY_POSITION_HANDLE.set(entity, newPosition);
            ENTITY_X_OLD_HANDLE.set(entity, x);
            ENTITY_Y_OLD_HANDLE.set(entity, y);
            ENTITY_Z_OLD_HANDLE.set(entity, z);

            //更新碰撞箱
            AABB newBoundingBox = entity.getDimensions(entity.getPose()).makeBoundingBox(x, y, z);
            ENTITY_BB_HANDLE.set(entity, newBoundingBox);

            //同步到客户端
            if (!entity.level().isClientSide && entity.level() instanceof ServerLevel serverLevel) {
                syncTeleportToClient(entity, serverLevel);
            }

            return true;
        } catch (Exception e) {
            TheLastSwordLogger.error("Teleport failed for {}: {}", entity.getType().getDescriptionId(), e.getMessage());
            return false;
        }
    }

    //向客户端同步传送
    private static void syncTeleportToClient(Entity entity, ServerLevel serverLevel) {
        try {
            ClientboundTeleportEntityPacket packet = new ClientboundTeleportEntityPacket(entity);
            serverLevel.getChunkSource().broadcast(entity, packet);

            //玩家特殊处理
            if (entity instanceof ServerPlayer player) {
                player.connection.teleport(
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        entity.getYRot(),
                        entity.getXRot()
                );
            }
        } catch (Exception e) {
            TheLastSwordLogger.error("Failed to sync teleport to clients: {}", e.getMessage());
        }
    }

    //实体清除模块

    //完整的实体清除方法
    public static void theLastEndRemove(Entity entity, Entity.RemovalReason reason) {
        if (entity == null) return;

        try {
            cleanupAI(entity);
            cleanupBossBar(entity);
            if (!entity.level().isClientSide) {
                entity.onRemovedFromWorld();
            }
            entity.setRemoved(reason);
            entity.stopRiding();
            removeAllPassengers(entity);
            if (!entity.level().isClientSide) {
                entity.levelCallback = EntityInLevelCallback.NULL;
            }
            if (!entity.level().isClientSide && entity.level() instanceof ServerLevel serverLevel) {
                NetworkHandler.sendToTrackingClients(
                        new TheLastEndRemoveClientPacket(entity.getId()),
                        entity
                );

                removeFromServerContainers(serverLevel, entity);
            } else if (entity.level().isClientSide && entity.level() instanceof ClientLevel clientLevel) {
                removeFromClientContainers(clientLevel, entity);
            }

        } catch (Exception e) {
            TheLastSwordLogger.error("Entity removal failed: {}", e.getMessage());
        }
    }

    //AI系统清理
    private static void cleanupAI(Entity entity) {
        if (entity instanceof Mob mob) {
            if (mob.goalSelector != null) {
                mob.goalSelector.removeAllGoals(goal -> true);
            }
            if (mob.targetSelector != null) {
                mob.targetSelector.removeAllGoals(goal -> true);
            }
            mob.setTarget(null);
            if (mob.getNavigation() != null) {
                mob.getNavigation().stop();
            }
        }
    }

    //Boss血条清理
    public static void cleanupBossBar(Entity entity) {
        if (entity == null) return;

        for (Class<?> clazz = entity.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;

                field.setAccessible(true);
                try {
                    Object fieldValue = field.get(entity);
                    if (fieldValue instanceof ServerBossEvent serverBossEvent) {
                        serverBossEvent.removeAllPlayers();
                        serverBossEvent.setVisible(false);
                    }
                } catch (IllegalAccessException ignored) {
                }
            }
        }
    }

    //清除所有乘客的骑乘关系
    private static void removeAllPassengers(Entity entity) {
        List<Entity> passengers = entity.getPassengers();
        for (int i = passengers.size() - 1; i >= 0; i--) {
            Entity passenger = passengers.get(i);
            if (passenger != null) {
                passenger.stopRiding();
            }
        }
    }

    //底层容器清除 - 服务端（顺序与老版本一致）
    private static void removeFromServerContainers(ServerLevel serverLevel, Entity entity) {
        int entityId = entity.getId();
        UUID entityUUID = entity.getUUID();

        try {
            //1. ChunkMap
            removeFromChunkMapEntityMap(serverLevel, entityId);
            //2. ServerLevel Lists (players/navigatingMobs)
            removeFromPlayersOrMobs(serverLevel, entity);
            //3. EntityLookup (byUuid + byId)
            removeFromEntityLookup(serverLevel, entityId, entityUUID);
            //4. KnownUuids
            removeFromKnownUuids(serverLevel, entityUUID);
            //5. EntityTickList (active + passive)
            removeFromEntityTickList(serverLevel, entityId);
            //6. EntitySectionStorage (ClassInstanceMultiMap)
            removeFromEntitySectionStorage(serverLevel, entity);

        } catch (Exception e) {
            TheLastSwordLogger.error("Failed to remove from server containers: {}", e.getMessage());
        }
    }

    //从 EntityTickList.active 和 passive 移除
    //使用官方的双缓冲机制，防止迭代期间修改导致 NPE
    private static void removeFromEntityTickList(ServerLevel serverLevel, int entityId) {
        try {
            Object entityTickList = SERVER_LEVEL_ENTITY_TICK_LIST_HANDLE.get(serverLevel);
            if (entityTickList == null) return;

            //获取 active、passive、iterated 字段
            Int2ObjectLinkedOpenHashMap<Entity> active = (Int2ObjectLinkedOpenHashMap<Entity>) ENTITY_TICK_LIST_ACTIVE_HANDLE.get(entityTickList);
            Int2ObjectLinkedOpenHashMap<Entity> passive = (Int2ObjectLinkedOpenHashMap<Entity>) ENTITY_TICK_LIST_PASSIVE_HANDLE.get(entityTickList);

            if (active == null || passive == null) return;

            //检查是否正在迭代 active（通过 iterated 字段判断）
            Object iterated = ENTITY_TICK_LIST_ITERATED_HANDLE.get(entityTickList);

            if (iterated == active) {
                //正在迭代 active，需要双缓冲切换
                //1. 清空 passive
                passive.clear();

                //2. 复制 active 到 passive（排除要删除的实体）
                for (Int2ObjectMap.Entry<Entity> entry : active.int2ObjectEntrySet()) {
                    int id = entry.getIntKey();
                    if (id != entityId) {  //跳过要删除的实体
                        passive.put(id, entry.getValue());
                    }
                }

                //3. 交换 active 和 passive
                ENTITY_TICK_LIST_ACTIVE_HANDLE.set(entityTickList, passive);
                ENTITY_TICK_LIST_PASSIVE_HANDLE.set(entityTickList, active);
            } else {
                //未在迭代，直接删除
                active.remove(entityId);
            }

        } catch (Exception ignored) {
        }
    }

    //从 EntityLookup (byUuid + byId) 移除
    private static void removeFromEntityLookup(ServerLevel serverLevel, int entityId, UUID entityUUID) {
        try {
            Object entityManager = SERVER_LEVEL_ENTITY_MANAGER_HANDLE.get(serverLevel);
            if (entityManager == null) return;

            Object visibleEntityStorage = PERSISTENT_ENTITY_MANAGER_VISIBLE_STORAGE_HANDLE.get(entityManager);
            if (visibleEntityStorage == null) return;

            //移除 byUuid
            Map<UUID, Entity> byUuid = (Map<UUID, Entity>) ENTITY_LOOKUP_BY_UUID_HANDLE.get(visibleEntityStorage);
            if (byUuid != null) {
                byUuid.remove(entityUUID);  //调用 HashMap 自己的 remove
            }

            //移除 byId
            Int2ObjectLinkedOpenHashMap<Entity> byId = (Int2ObjectLinkedOpenHashMap<Entity>) ENTITY_LOOKUP_BY_ID_HANDLE.get(visibleEntityStorage);
            if (byId != null) {
                byId.remove(entityId);  //调用 FastUtil 容器自己的 remove
            }
        } catch (Exception ignored) {
        }
    }

    //从 EntitySectionStorage → EntitySection.storage 移除
    private static void removeFromEntitySectionStorage(ServerLevel serverLevel, Entity entity) {
        try {
            Object entityManager = SERVER_LEVEL_ENTITY_MANAGER_HANDLE.get(serverLevel);
            if (entityManager == null) return;

            Object sectionStorage = PERSISTENT_ENTITY_MANAGER_SECTION_STORAGE_HANDLE.get(entityManager);
            if (sectionStorage == null) return;

            Long2ObjectMap<?> sections = (Long2ObjectMap<?>) ENTITY_SECTION_STORAGE_SECTIONS_HANDLE.get(sectionStorage);
            if (sections == null) return;

            //遍历所有 EntitySection
            for (Object section : sections.values()) {
                if (section == null) continue;

                Object storage = ENTITY_SECTION_STORAGE_HANDLE.get(section);
                if (storage == null) continue;

                //获取 ClassInstanceMultiMap.byClass
                Map<Class<?>, List<?>> byClass = (Map<Class<?>, List<?>>) CLASS_INSTANCE_MULTI_MAP_BY_CLASS_HANDLE.get(storage);
                if (byClass == null) continue;

                //遍历所有类型的 List，移除匹配的实体
                for (Map.Entry<Class<?>, List<?>> entry : byClass.entrySet()) {
                    Class<?> clazz = entry.getKey();
                    List<?> list = entry.getValue();

                    if (clazz.isInstance(entity)) {
                        list.remove(entity);  //调用 List 自己的 remove
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    //从 ChunkMap.entityMap 移除
    private static void removeFromChunkMapEntityMap(ServerLevel serverLevel, int entityId) {
        try {
            Object chunkSource = SERVER_LEVEL_CHUNK_SOURCE_HANDLE.get(serverLevel);
            if (chunkSource == null) return;

            Object chunkMap = SERVER_CHUNK_CACHE_CHUNK_MAP_HANDLE.get(chunkSource);
            if (chunkMap == null) return;

            Int2ObjectOpenHashMap<?> entityMap = (Int2ObjectOpenHashMap<?>) CHUNK_MAP_ENTITY_MAP_HANDLE.get(chunkMap);
            if (entityMap != null) {
                entityMap.remove(entityId);  //调用 FastUtil 容器自己的 remove
            }
        } catch (Exception ignored) {
        }
    }

    //从 PersistentEntitySectionManager.knownUuids 移除
    private static void removeFromKnownUuids(ServerLevel serverLevel, UUID entityUUID) {
        try {
            Object entityManager = SERVER_LEVEL_ENTITY_MANAGER_HANDLE.get(serverLevel);
            if (entityManager == null) return;

            Set<UUID> knownUuids = (Set<UUID>) PERSISTENT_ENTITY_MANAGER_KNOWN_UUIDS_HANDLE.get(entityManager);
            if (knownUuids != null) {
                knownUuids.remove(entityUUID);  //调用 Set 自己的 remove
            }
        } catch (Exception ignored) {
        }
    }

    //从 ServerLevel.players 或 navigatingMobs 移除
    private static void removeFromPlayersOrMobs(ServerLevel serverLevel, Entity entity) {
        try {
            //如果是玩家，从 players 列表移除
            if (entity instanceof ServerPlayer) {
                List<ServerPlayer> players = (List<ServerPlayer>) SERVER_LEVEL_PLAYERS_HANDLE.get(serverLevel);
                if (players != null) {
                    players.remove(entity);  //调用 List 自己的 remove
                }
            }

            //如果是 Mob，从 navigatingMobs 集合移除
            if (entity instanceof Mob) {
                ObjectOpenHashSet<Mob> navigatingMobs = (ObjectOpenHashSet<Mob>) SERVER_LEVEL_NAVIGATING_MOBS_HANDLE.get(serverLevel);
                if (navigatingMobs != null) {
                    navigatingMobs.remove(entity);  //调用 FastUtil 容器自己的 remove
                }
            }
        } catch (Exception ignored) {
        }
    }

    //底层容器清除 - 客户端
    public static void removeFromClientContainers(ClientLevel clientLevel, Entity entity) {
        //懒加载初始化客户端VarHandle
        initClientVarHandles();

        int entityId = entity.getId();
        UUID entityUUID = entity.getUUID();

        try {
            //高优先级容器
            removeFromClientEntityTickList(clientLevel, entityId);
            removeFromClientEntityLookup(clientLevel, entityId, entityUUID);
            removeFromClientEntitySectionStorage(clientLevel, entity);

            //中优先级容器
            removeFromClientPlayers(clientLevel, entity);

        } catch (Exception e) {
            TheLastSwordLogger.error("Failed to remove from client containers: {}", e.getMessage());
        }
    }

    //从客户端 EntityTickList.active 和 passive 移除
    private static void removeFromClientEntityTickList(ClientLevel clientLevel, int entityId) {
        try {
            Object entityTickList = CLIENT_LEVEL_TICKING_ENTITIES_HANDLE.get(clientLevel);
            if (entityTickList == null) return;

            Int2ObjectLinkedOpenHashMap<Entity> active = (Int2ObjectLinkedOpenHashMap<Entity>) ENTITY_TICK_LIST_ACTIVE_HANDLE.get(entityTickList);
            if (active != null) {
                active.remove(entityId);
            }

            Int2ObjectLinkedOpenHashMap<Entity> passive = (Int2ObjectLinkedOpenHashMap<Entity>) ENTITY_TICK_LIST_PASSIVE_HANDLE.get(entityTickList);
            if (passive != null) {
                passive.remove(entityId);
            }
        } catch (Exception ignored) {
        }
    }

    //从客户端 EntityLookup (byUuid + byId) 移除
    private static void removeFromClientEntityLookup(ClientLevel clientLevel, int entityId, UUID entityUUID) {
        try {
            Object entityStorage = CLIENT_LEVEL_ENTITY_STORAGE_HANDLE.get(clientLevel);
            if (entityStorage == null) return;

            Object entityLookup = TRANSIENT_ENTITY_MANAGER_ENTITY_STORAGE_HANDLE.get(entityStorage);
            if (entityLookup == null) return;

            //移除 byUuid
            Map<UUID, Entity> byUuid = (Map<UUID, Entity>) ENTITY_LOOKUP_BY_UUID_HANDLE.get(entityLookup);
            if (byUuid != null) {
                byUuid.remove(entityUUID);  //调用 HashMap 自己的 remove
            }

            //移除 byId
            Int2ObjectLinkedOpenHashMap<Entity> byId = (Int2ObjectLinkedOpenHashMap<Entity>) ENTITY_LOOKUP_BY_ID_HANDLE.get(entityLookup);
            if (byId != null) {
                byId.remove(entityId);  //调用 FastUtil 容器自己的 remove
            }
        } catch (Exception ignored) {
        }
    }

    //从客户端 EntitySectionStorage 移除
    private static void removeFromClientEntitySectionStorage(ClientLevel clientLevel, Entity entity) {
        try {
            Object entityStorage = CLIENT_LEVEL_ENTITY_STORAGE_HANDLE.get(clientLevel);
            if (entityStorage == null) return;

            Object sectionStorage = TRANSIENT_ENTITY_MANAGER_SECTION_STORAGE_HANDLE.get(entityStorage);
            if (sectionStorage == null) return;

            Long2ObjectMap<?> sections = (Long2ObjectMap<?>) ENTITY_SECTION_STORAGE_SECTIONS_HANDLE.get(sectionStorage);
            if (sections == null) return;

            //遍历所有 EntitySection
            for (Object section : sections.values()) {
                if (section == null) continue;

                Object storage = ENTITY_SECTION_STORAGE_HANDLE.get(section);
                if (storage == null) continue;

                //获取 ClassInstanceMultiMap.byClass
                Map<Class<?>, List<?>> byClass = (Map<Class<?>, List<?>>) CLASS_INSTANCE_MULTI_MAP_BY_CLASS_HANDLE.get(storage);
                if (byClass == null) continue;

                //遍历所有类型的 List，移除匹配的实体
                for (Map.Entry<Class<?>, List<?>> entry : byClass.entrySet()) {
                    Class<?> clazz = entry.getKey();
                    List<?> list = entry.getValue();

                    if (clazz.isInstance(entity)) {
                        list.remove(entity);  //调用 List 自己的 remove
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    //从客户端 players 列表移除
    private static void removeFromClientPlayers(ClientLevel clientLevel, Entity entity) {
        try {
            if (entity instanceof Player) {
                List<?> players = (List<?>) CLIENT_LEVEL_PLAYERS_HANDLE.get(clientLevel);
                if (players != null) {
                    players.remove(entity);  //调用 List 自己的 remove
                }
            }
        } catch (Exception ignored) {
        }
    }


    //实体目标与队伍模块

    //1.根据配置判断是否应该攻击目标
    public static boolean shouldAttack(Entity entity) {
        if (entity instanceof Player)        return TheLastSwordConfiguration.getAttackPlayersSafely();
        if (entity instanceof TamableAnimal) return TheLastSwordConfiguration.getAttackTamedSafely();
        if (entity instanceof Villager)      return TheLastSwordConfiguration.getAttackVillagersSafely();
        if (entity instanceof IronGolem)     return TheLastSwordConfiguration.getAttackGolemsSafely();
        if (entity instanceof Animal)        return TheLastSwordConfiguration.getAttackAnimalsSafely();
        if (entity instanceof NeutralMob)    return TheLastSwordConfiguration.getAttackNeutralSafely();
        return true;
    }

    //2.判断两个实体是否是原版盟友关系（玩家组队、宠物关系）
    public static boolean areOriginalAllies(Entity attacker, Entity target) {
        if (attacker == null || target == null) return false;

        //同一实体
        if (attacker.getUUID().equals(target.getUUID())) return true;

        //情况1：玩家 vs 玩家
        if (attacker instanceof Player pa && target instanceof Player pt) {
            if (pa.getTeam() != null && pa.getTeam() == pt.getTeam()) {
                return true;
            }
            return false;
        }

        //情况2：玩家 vs 宠物
        if (attacker instanceof Player pa && target instanceof TamableAnimal tt) {
            //玩家攻击自己的宠物
            if (tt.isOwnedBy(pa)) return true;

            //玩家攻击队友的宠物
            LivingEntity owner = tt.getOwner();
            if (owner instanceof Player ownerPlayer) {
                if (pa.getTeam() != null && pa.getTeam() == ownerPlayer.getTeam()) {
                    return true;
                }
            }
            return false;
        }

        //情况3：宠物 vs 玩家
        if (attacker instanceof TamableAnimal ta && target instanceof Player pt) {
            //宠物攻击自己的主人
            if (ta.isOwnedBy(pt)) return true;

            //宠物攻击主人的队友
            LivingEntity owner = ta.getOwner();
            if (owner instanceof Player ownerPlayer) {
                if (ownerPlayer.getTeam() != null && ownerPlayer.getTeam() == pt.getTeam()) {
                    return true;
                }
            }
            return false;
        }

        //情况4：宠物 vs 宠物
        if (attacker instanceof TamableAnimal ta && target instanceof TamableAnimal tt) {
            LivingEntity aOwner = ta.getOwner();
            LivingEntity tOwner = tt.getOwner();

            //同一主人的宠物
            if (aOwner != null && tOwner != null && aOwner.getUUID().equals(tOwner.getUUID())) {
                return true;
            }

            //主人之间是队友
            if (aOwner instanceof Player aOwnerPlayer && tOwner instanceof Player tOwnerPlayer) {
                if (aOwnerPlayer.getTeam() != null && aOwnerPlayer.getTeam() == tOwnerPlayer.getTeam()) {
                    return true;
                }
            }
            return false;
        }

        //情况5：任何实体 vs 剑灵（通过剑灵的主人间接判断）
        if (target instanceof LivingEntity && WraithSummonManager.isWraith((LivingEntity) target)) {
            Player wraithOwner = WraithSummonManager.getOwner((LivingEntity) target, target.level());
            if (wraithOwner != null) {
                //递归检查：攻击者和剑灵的主人是否是盟友
                if (areOriginalAllies(attacker, wraithOwner)) {
                    return true;
                }
            }
        }

        //其他情况不是盟友
        return false;
    }

    //3.判断剑灵相关的盟友关系
    public static boolean areWraithAllies(Entity attacker, Entity target) {
        if (attacker == null || target == null) return false;

        //只处理剑灵作为攻击者的情况
        if (!(attacker instanceof LivingEntity) || !WraithSummonManager.isWraith((LivingEntity) attacker)) {
            return false;
        }

        Player owner = WraithSummonManager.getOwner((LivingEntity) attacker, attacker.level());
        if (owner == null) return false;

        //第1步：不攻击主人
        if (target == owner) return true;

        //第2步：不攻击主人的队友（玩家）
        if (target instanceof Player targetPlayer) {
            if (owner.getTeam() != null && owner.getTeam() == targetPlayer.getTeam()) {
                return true;
            }
        }

        //第3步：不攻击宠物（同主人的宠物 + 主人队友的宠物）
        if (target instanceof TamableAnimal tamable && tamable.isTame()) {
            LivingEntity tamableOwner = tamable.getOwner();
            if (tamableOwner != null) {
                //同主人的宠物
                if (tamableOwner == owner) return true;

                //主人队友的宠物
                if (tamableOwner instanceof Player tamableOwnerPlayer) {
                    if (owner.getTeam() != null && owner.getTeam() == tamableOwnerPlayer.getTeam()) {
                        return true;
                    }
                }
            }
        }

        //第4步：不攻击剑灵（同主人的其他剑灵 + 主人队友的剑灵）
        if (target instanceof LivingEntity && WraithSummonManager.isWraith((LivingEntity) target)) {
            Player targetOwner = WraithSummonManager.getOwner((LivingEntity) target, target.level());
            if (targetOwner != null) {
                //同主人的其他剑灵
                if (targetOwner == owner) return true;

                //主人队友的剑灵
                if (owner.getTeam() != null && owner.getTeam() == targetOwner.getTeam()) {
                    return true;
                }
            }
        }

        return false;
    }

    //4.查找实体的主人
    public static LivingEntity findOwner(Entity entity) {
        //如果是被驯服的宠物，返回其主人
        if (entity instanceof TamableAnimal tamable && tamable.isTame()) {
            return tamable.getOwner();
        }

        //剑灵系统：检查剑灵的主人
        if (entity instanceof LivingEntity living && WraithSummonManager.isWraith(living)) {
            return WraithSummonManager.getOwner(living, entity.level());
        }

        return null;
    }

    //5.综合判断是否可以攻击目标
    public static boolean canAttack(Entity attacker, Entity target) {
        //第一步：配置文件过滤
        if (!shouldAttack(target)) {
            return false;
        }

        //第二步：原版盟友关系判断
        if (areOriginalAllies(attacker, target)) {
            return false;
        }

        //第三步：剑灵盟友关系判断
        if (areWraithAllies(attacker, target)) {
            return false;
        }

        //通过所有检查，可以攻击
        return true;
    }

    //5.获取前方半圆范围内的有效攻击目标
    public static List<LivingEntity> getTargetsInHemisphere(LivingEntity attacker, double radius) {
        List<LivingEntity> validTargets = new ArrayList<>();

        //创建搜索范围
        AABB searchBox = new AABB(
                attacker.getX() - radius,
                attacker.getY() - radius,
                attacker.getZ() - radius,
                attacker.getX() + radius,
                attacker.getY() + radius,
                attacker.getZ() + radius
        );

        //获取范围内所有实体
        List<Entity> nearbyEntities = attacker.level().getEntities(attacker, searchBox);

        //获取攻击者的视线方向
        Vec3 lookVec = attacker.getLookAngle();

        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity livingEntity) {
                //计算从攻击者到目标的方向向量
                Vec3 toTarget = new Vec3(
                        livingEntity.getX() - attacker.getX(),
                        0,
                        livingEntity.getZ() - attacker.getZ()
                ).normalize();

                //点乘判断是否在前方180度范围内（点乘 > 0 表示夹角 < 90度，即前方半圆）
                double dotProduct = lookVec.x * toTarget.x + lookVec.z * toTarget.z;

                if (dotProduct > 0 && canAttack(attacker, livingEntity)) {
                    validTargets.add(livingEntity);
                }
            }
        }

        return validTargets;
    }

    //6.获取球体范围内的有效攻击目标
    public static List<LivingEntity> getTargetsInSphere(LivingEntity attacker, double radius) {
        List<LivingEntity> validTargets = new ArrayList<>();

        //创建搜索范围
        AABB searchBox = new AABB(
                attacker.getX() - radius,
                attacker.getY() - radius,
                attacker.getZ() - radius,
                attacker.getX() + radius,
                attacker.getY() + radius,
                attacker.getZ() + radius
        );

        //获取范围内所有实体
        List<Entity> nearbyEntities = attacker.level().getEntities(attacker, searchBox);

        double radiusSquared = radius * radius;

        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity livingEntity) {
                //计算实际距离的平方（避免开方提升性能）
                double dx = livingEntity.getX() - attacker.getX();
                double dy = livingEntity.getY() - attacker.getY();
                double dz = livingEntity.getZ() - attacker.getZ();
                double distanceSquared = dx * dx + dy * dy + dz * dz;

                //球体范围判断
                if (distanceSquared <= radiusSquared && canAttack(attacker, livingEntity)) {
                    validTargets.add(livingEntity);
                }
            }
        }

        return validTargets;
    }


}
