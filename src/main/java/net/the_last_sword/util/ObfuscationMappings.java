package net.the_last_sword.util;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 混淆映射管理器 - 统一管理反射和VarHandle的混淆名映射
 */
public class ObfuscationMappings {

    public static final String MINECRAFT_VERSION = "1.20.1";
    public static final String FORGE_VERSION = "47.2.0";

    //缓存以避免重复查找
    private static final Map<String, Field> FIELD_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Method> METHOD_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, VarHandle> VARHANDLE_CACHE = new ConcurrentHashMap<>();

    //字段映射表: 版本 -> 字段标识 -> 混淆名
    private static final Map<String, Map<String, String>> FIELD_MAPPINGS = new HashMap<>();

    static {
        Map<String, String> v1_20_1_mappings = new HashMap<>();

        //Entity字段
        v1_20_1_mappings.put("Entity.entityData", "f_19804_");
        v1_20_1_mappings.put("Entity.position", "f_19825_");
        v1_20_1_mappings.put("Entity.xOld", "f_19790_");
        v1_20_1_mappings.put("Entity.yOld", "f_19791_");
        v1_20_1_mappings.put("Entity.zOld", "f_19792_");
        v1_20_1_mappings.put("Entity.bb", "f_19828_");

        //LivingEntity字段
        v1_20_1_mappings.put("LivingEntity.hurtTime", "f_20915_");
        v1_20_1_mappings.put("LivingEntity.deathTime", "f_20919_");
        v1_20_1_mappings.put("LivingEntity.dead", "f_20890_");

        //SynchedEntityData字段
        v1_20_1_mappings.put("SynchedEntityData.itemsById", "f_135345_");
        v1_20_1_mappings.put("SynchedEntityData.isDirty", "f_135348_");

        //SynchedEntityData.DataItem字段
        v1_20_1_mappings.put("DataItem.value", "f_135391_");
        v1_20_1_mappings.put("DataItem.dirty", "f_135392_");

        //ServerLevel字段
        v1_20_1_mappings.put("ServerLevel.players", "f_8546_");
        v1_20_1_mappings.put("ServerLevel.chunkSource", "f_8547_");
        v1_20_1_mappings.put("ServerLevel.entityTickList", "f_143243_");
        v1_20_1_mappings.put("ServerLevel.entityManager", "f_143244_");
        v1_20_1_mappings.put("ServerLevel.navigatingMobs", "f_143246_");

        //EntityTickList字段
        v1_20_1_mappings.put("EntityTickList.active", "f_156903_");
        v1_20_1_mappings.put("EntityTickList.passive", "f_156904_");
        v1_20_1_mappings.put("EntityTickList.iterated", "f_156905_");

        //ServerChunkCache字段
        v1_20_1_mappings.put("ServerChunkCache.chunkMap", "f_8325_");

        //ChunkMap字段
        v1_20_1_mappings.put("ChunkMap.entityMap", "f_140150_");

        //PersistentEntitySectionManager字段
        v1_20_1_mappings.put("PersistentEntitySectionManager.visibleEntityStorage", "f_157494_");
        v1_20_1_mappings.put("PersistentEntitySectionManager.knownUuids", "f_157491_");
        v1_20_1_mappings.put("PersistentEntitySectionManager.sectionStorage", "f_157495_");

        //EntityLookup字段
        v1_20_1_mappings.put("EntityLookup.byUuid", "f_156808_");
        v1_20_1_mappings.put("EntityLookup.byId", "f_156807_");

        //EntitySectionStorage字段
        v1_20_1_mappings.put("EntitySectionStorage.sections", "f_156852_");

        //EntitySection字段
        v1_20_1_mappings.put("EntitySection.storage", "f_156827_");

        //ClassInstanceMultiMap字段
        v1_20_1_mappings.put("ClassInstanceMultiMap.byClass", "f_13527_");

        //ClientLevel字段 - 客户端实体清除模块
        v1_20_1_mappings.put("ClientLevel.tickingEntities", "f_171630_");
        v1_20_1_mappings.put("ClientLevel.entityStorage", "f_171631_");
        v1_20_1_mappings.put("ClientLevel.players", "f_104566_");

        //TransientEntitySectionManager字段
        v1_20_1_mappings.put("TransientEntitySectionManager.entityStorage", "f_157637_");
        v1_20_1_mappings.put("TransientEntitySectionManager.sectionStorage", "f_157638_");

        FIELD_MAPPINGS.put("1.20.1-47.2.0", v1_20_1_mappings);
    }

    //方法映射表: 版本 -> 方法标识 -> 混淆名
    private static final Map<String, Map<String, String>> METHOD_MAPPINGS = new HashMap<>();

    static {
        Map<String, String> v1_20_1_methods = new HashMap<>();

        //LivingEntity方法
        v1_20_1_methods.put("LivingEntity.dropAllDeathLoot", "m_6668_");
        v1_20_1_methods.put("LivingEntity.getRecordMaxHp", "m_21233_");
        v1_20_1_methods.put("LivingEntity.actuallyHurt", "m_6475_");

        //Entity方法
        v1_20_1_methods.put("Entity.setRemoved", "m_142467_");

        METHOD_MAPPINGS.put("1.20.1-47.2.0", v1_20_1_methods);
    }

    //获取字段（支持版本映射和缓存）
    public static Field getField(Class<?> clazz, String fieldKey) {
        String cacheKey = clazz.getName() + "." + fieldKey;

        return FIELD_CACHE.computeIfAbsent(cacheKey, k -> {
            try {
                String currentVersion = MINECRAFT_VERSION + "-" + FORGE_VERSION;
                Map<String, String> mappings = FIELD_MAPPINGS.get(currentVersion);

                if (mappings == null) {
                    throw new RuntimeException("No field mappings found for version: " + currentVersion);
                }

                String obfuscatedName = mappings.get(fieldKey);
                if (obfuscatedName == null) {
                    throw new RuntimeException("No mapping found for field: " + fieldKey);
                }

                Field field = ObfuscationReflectionHelper.findField(clazz, obfuscatedName);
                field.setAccessible(true);
                return field;

            } catch (Exception e) {
                throw new RuntimeException("Failed to get field: " + fieldKey + " for class: " + clazz.getName(), e);
            }
        });
    }

    //通过字段名直接获取字段（遍历查找，适用于开发环境字段名未混淆的情况）
    public static Field getFieldByName(Class<?> clazz, String fieldName) {
        String cacheKey = clazz.getName() + "." + fieldName;

        return FIELD_CACHE.computeIfAbsent(cacheKey, k -> {
            try {
                //遍历所有字段查找匹配的字段名
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.getName().equals(fieldName)) {
                        field.setAccessible(true);
                        return field;
                    }
                }
                throw new RuntimeException("Field not found: " + fieldName);

            } catch (Exception e) {
                throw new RuntimeException("Failed to get field by name: " + fieldName + " in class: " + clazz.getName(), e);
            }
        });
    }

    //通过字段名获取VarHandle
    public static VarHandle getVarHandleByFieldName(Class<?> clazz, String fieldName) {
        String cacheKey = clazz.getName() + "." + fieldName + ".varhandle";

        return VARHANDLE_CACHE.computeIfAbsent(cacheKey, k -> {
            try {
                Field field = getFieldByName(clazz, fieldName);
                MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup());
                return lookup.unreflectVarHandle(field);

            } catch (Exception e) {
                throw new RuntimeException("Failed to get VarHandle for field: " + fieldName + " in class: " + clazz.getName(), e);
            }
        });
    }

    //获取VarHandle（支持混淆映射和缓存）
    public static VarHandle getVarHandle(Class<?> clazz, String fieldKey) {
        String cacheKey = clazz.getName() + "." + fieldKey;

        return VARHANDLE_CACHE.computeIfAbsent(cacheKey, k -> {
            try {
                //1. 通过混淆映射获取Field
                Field field = getField(clazz, fieldKey);

                //2. 通过Field创建VarHandle
                MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup());
                return lookup.unreflectVarHandle(field);

            } catch (Exception e) {
                throw new RuntimeException("Failed to get VarHandle for: " + fieldKey + " in class: " + clazz.getName(), e);
            }
        });
    }

    //获取方法（支持版本映射和缓存）
    public static Method getMethod(Class<?> clazz, String methodKey, Class<?>... parameterTypes) {
        String cacheKey = clazz.getName() + "." + methodKey + "_" + Arrays.toString(parameterTypes);

        return METHOD_CACHE.computeIfAbsent(cacheKey, k -> {
            try {
                String currentVersion = MINECRAFT_VERSION + "-" + FORGE_VERSION;
                Map<String, String> mappings = METHOD_MAPPINGS.get(currentVersion);

                if (mappings == null) {
                    throw new RuntimeException("No method mappings found for version: " + currentVersion);
                }

                String obfuscatedName = mappings.get(methodKey);
                if (obfuscatedName == null) {
                    throw new RuntimeException("No mapping found for method: " + methodKey);
                }

                Method method = ObfuscationReflectionHelper.findMethod(clazz, obfuscatedName, parameterTypes);
                method.setAccessible(true);
                return method;

            } catch (Exception e) {
                throw new RuntimeException("Failed to get method: " + methodKey + " for class: " + clazz.getName(), e);
            }
        });
    }

    //设置字段值
    public static void setField(Object target, Class<?> clazz, String fieldKey, Object value) {
        try {
            Field field = getField(clazz, fieldKey);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldKey, e);
        }
    }

    //获取字段值
    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object target, Class<?> clazz, String fieldKey) {
        try {
            Field field = getField(clazz, fieldKey);
            return (T) field.get(target);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get field: " + fieldKey, e);
        }
    }

    //调用方法
    @SuppressWarnings("unchecked")
    public static <T> T invokeMethod(Object target, Class<?> clazz, String methodKey, Object... args) {
        try {
            Class<?>[] paramTypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                paramTypes[i] = args[i].getClass();
            }

            Method method = getMethod(clazz, methodKey, paramTypes);
            return (T) method.invoke(target, args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke method: " + methodKey, e);
        }
    }

    //常用快捷方法
    public static class DataItemFields {
        public static VarHandle getValueHandle() {
            return getVarHandle(SynchedEntityData.DataItem.class, "DataItem.value");
        }

        public static VarHandle getDirtyHandle() {
            return getVarHandle(SynchedEntityData.DataItem.class, "DataItem.dirty");
        }
    }
}
