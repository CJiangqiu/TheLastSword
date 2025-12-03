package net.the_last_sword.defence;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.the_last_sword.util.TheLastSwordLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//防御系统持久化层
public class DefenceSavedData extends SavedData {
    private static final String DATA_NAME = "the_last_sword_defence";

    //NBT序列化格式存储
    private final Map<UUID, CompoundTag> recordsNBT = new HashMap<>();

    //3级实体NBT存储
    private final Map<UUID, CompoundTag> level3EntityNBT = new HashMap<>();

    private DefenceSavedData() {
    }

    //获取或创建SavedData实例
    public static DefenceSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            DefenceSavedData::load,
            DefenceSavedData::new,
            DATA_NAME
        );
    }

    //从NBT加载
    private static DefenceSavedData load(CompoundTag root) {
        DefenceSavedData data = new DefenceSavedData();

        //加载防御记录
        if (root.contains("DefenceRecords")) {
            CompoundTag recordsTag = root.getCompound("DefenceRecords");
            for (String uuidStr : recordsTag.getAllKeys()) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    CompoundTag recordTag = recordsTag.getCompound(uuidStr);
                    data.recordsNBT.put(uuid, recordTag);
                } catch (IllegalArgumentException e) {
                    TheLastSwordLogger.error("Invalid UUID in defence data: {}", uuidStr);
                }
            }
        }

        //加载3级实体NBT
        if (root.contains("Level3EntityNBT")) {
            CompoundTag level3Tag = root.getCompound("Level3EntityNBT");
            for (String uuidStr : level3Tag.getAllKeys()) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    CompoundTag entityTag = level3Tag.getCompound(uuidStr);
                    data.level3EntityNBT.put(uuid, entityTag);
                } catch (IllegalArgumentException e) {
                    TheLastSwordLogger.error("Invalid UUID in level 3 entity data: {}", uuidStr);
                }
            }
        }

        return data;
    }

    //保存到NBT
    @Override
    public CompoundTag save(CompoundTag root) {
        //保存防御记录
        CompoundTag recordsTag = new CompoundTag();
        recordsNBT.forEach((uuid, tag) -> {
            recordsTag.put(uuid.toString(), tag);
        });
        root.put("DefenceRecords", recordsTag);

        //保存3级实体NBT
        CompoundTag level3Tag = new CompoundTag();
        level3EntityNBT.forEach((uuid, tag) -> {
            level3Tag.put(uuid.toString(), tag);
        });
        root.put("Level3EntityNBT", level3Tag);

        return root;
    }

    //从磁盘加载数据到Manager
    public static void loadToManager(ServerLevel level) {
        try {
            DefenceSavedData store = get(level);

            DefenceManager.importRecords(store.recordsNBT);
            DefenceManager.importLevel3NBT(store.level3EntityNBT);

            TheLastSwordLogger.info("Loaded defence data from {}", level.dimension().location());
        } catch (Exception e) {
            TheLastSwordLogger.error("Failed to load defence data from {}", level.dimension().location());
            e.printStackTrace();
        }
    }

    //将Manager数据保存到磁盘
    public static void persistData(ServerLevel level) {
        try {
            DefenceSavedData store = get(level);

            store.recordsNBT.clear();
            store.recordsNBT.putAll(DefenceManager.exportRecords());

            store.level3EntityNBT.clear();
            store.level3EntityNBT.putAll(DefenceManager.exportLevel3NBT());

            store.setDirty();
            DefenceManager.markSaved();

            TheLastSwordLogger.info("Persisted defence data to {}", level.dimension().location());
        } catch (Exception e) {
            TheLastSwordLogger.error("Failed to persist defence data to {}", level.dimension().location());
            e.printStackTrace();
        }
    }
}
