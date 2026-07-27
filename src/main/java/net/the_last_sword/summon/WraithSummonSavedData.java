package net.the_last_sword.summon;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.the_last_sword.util.TheLastSwordLogger;

import java.util.*;

//旧版剑灵绑定表的一次性迁移 - 绑定关系已由 ECA 阵营接管，此处只负责导入历史存档
@Mod.EventBusSubscriber(modid = "the_last_sword", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WraithSummonSavedData extends SavedData {

    private static final String DATA_NAME = "the_last_sword_wraith_bindings";
    private static final String NBT_BINDINGS = "Bindings";
    private static final String NBT_MIGRATED = "Migrated";

    private final Map<UUID, Set<UUID>> bindingsNBT = new HashMap<>();
    private boolean migrated = false;

    private WraithSummonSavedData() {}

    //获取实例
    public static WraithSummonSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                WraithSummonSavedData::load,
                WraithSummonSavedData::new,
                DATA_NAME
        );
    }

    //从NBT加载
    private static WraithSummonSavedData load(CompoundTag root) {
        WraithSummonSavedData data = new WraithSummonSavedData();
        data.migrated = root.getBoolean(NBT_MIGRATED);

        CompoundTag bindings = root.getCompound(NBT_BINDINGS);
        for (String playerUUIDStr : bindings.getAllKeys()) {
            try {
                UUID playerUUID = UUID.fromString(playerUUIDStr);
                Set<UUID> wraithSet = new HashSet<>();

                ListTag wraithList = bindings.getList(playerUUIDStr, Tag.TAG_STRING);
                for (int i = 0; i < wraithList.size(); i++) {
                    try {
                        wraithSet.add(UUID.fromString(wraithList.getString(i)));
                    } catch (IllegalArgumentException ignored) {}
                }

                if (!wraithSet.isEmpty()) {
                    data.bindingsNBT.put(playerUUID, wraithSet);
                }
            } catch (IllegalArgumentException ignored) {}
        }

        return data;
    }

    //保存到NBT（原绑定数据保留，仅追加迁移标记，便于回滚）
    @Override
    public CompoundTag save(CompoundTag root) {
        CompoundTag bindings = new CompoundTag();

        for (Map.Entry<UUID, Set<UUID>> entry : bindingsNBT.entrySet()) {
            ListTag wraithList = new ListTag();
            for (UUID wraitheUUID : entry.getValue()) {
                wraithList.add(StringTag.valueOf(wraitheUUID.toString()));
            }
            bindings.put(entry.getKey().toString(), wraithList);
        }

        root.put(NBT_BINDINGS, bindings);
        root.putBoolean(NBT_MIGRATED, migrated);
        return root;
    }

    //将旧绑定表导入 ECA 阵营，只执行一次
    private static void migrateToFaction(ServerLevel level) {
        WraithSummonSavedData store = get(level);
        if (store.migrated || store.bindingsNBT.isEmpty()) {
            return;
        }

        int wraithCount = 0;
        for (Map.Entry<UUID, Set<UUID>> entry : store.bindingsNBT.entrySet()) {
            UUID ownerUUID = entry.getKey();

            //玩家离线，显示名先用 UUID 占位，下次召唤时由 ensureFaction 补正为玩家名
            WraithFaction.ensureFaction(ownerUUID, ownerUUID.toString(), level);

            for (UUID wraitheUUID : entry.getValue()) {
                //旧表未记录实体类型，留空不影响归属判定
                if (WraithFaction.bind(ownerUUID, wraitheUUID, "", level)) {
                    wraithCount++;
                }
            }
        }

        store.migrated = true;
        store.setDirty();

        TheLastSwordLogger.info("[WraithSummonData] Migrated {} players with {} wraith bindings to ECA factions",
            store.bindingsNBT.size(), wraithCount);
    }

    // ===== 事件钩子 =====

    //世界加载时执行迁移（只在主世界，阵营数据统一存于主世界）
    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load e) {
        if (e.getLevel() instanceof ServerLevel lvl && lvl.dimension() == Level.OVERWORLD) {
            migrateToFaction(lvl);
        }
    }
}
