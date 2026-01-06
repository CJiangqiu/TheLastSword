package net.the_last_sword.summon;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

//剑灵绑定数据持久化 - 玩家UUID → 剑灵UUID集合
@Mod.EventBusSubscriber(modid = "the_last_sword", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WraithSummonSavedData extends SavedData {

    private final Map<UUID, Set<UUID>> bindingsNBT = new HashMap<>();

    private WraithSummonSavedData() {}

    //获取实例
    public static WraithSummonSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                WraithSummonSavedData::load,
                WraithSummonSavedData::new,
                "the_last_sword_wraith_bindings"
        );
    }

    //从NBT加载
    private static WraithSummonSavedData load(CompoundTag root) {
        WraithSummonSavedData data = new WraithSummonSavedData();
        CompoundTag bindings = root.getCompound("Bindings");

        for (String playerUUIDStr : bindings.getAllKeys()) {
            try {
                UUID playerUUID = UUID.fromString(playerUUIDStr);
                Set<UUID> wraithSet = new HashSet<>();

                ListTag wraithList = bindings.getList(playerUUIDStr, Tag.TAG_STRING);
                for (int i = 0; i < wraithList.size(); i++) {
                    try {
                        UUID wraitheUUID = UUID.fromString(wraithList.getString(i));
                        wraithSet.add(wraitheUUID);
                    } catch (IllegalArgumentException ignored) {}
                }

                if (!wraithSet.isEmpty()) {
                    data.bindingsNBT.put(playerUUID, wraithSet);
                }
            } catch (IllegalArgumentException ignored) {}
        }

        return data;
    }

    //保存到NBT
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

        root.put("Bindings", bindings);
        return root;
    }

    //从Manager导出并持久化
    public static void persistData(ServerLevel level) {
        WraithSummonSavedData store = get(level);

        synchronized (WraithSummonManager.class) {
            store.bindingsNBT.clear();
            Map<UUID, Set<UUID>> latest = WraithSummonManager.exportBindings();
            store.bindingsNBT.putAll(latest);

            store.setDirty();
            WraithSummonManager.markSaved();
        }
    }

    //从存档加载到Manager
    public static void loadToManager(ServerLevel level) {
        WraithSummonSavedData store = get(level);

        synchronized (WraithSummonManager.class) {
            WraithSummonManager.importBindings(store.bindingsNBT);
            WraithSummonManager.markSaved();

            //调试日志：显示加载的绑定数据
            int totalBindings = 0;
            for (java.util.Set<java.util.UUID> wraiths : store.bindingsNBT.values()) {
                totalBindings += wraiths.size();
            }
            net.the_last_sword.util.TheLastSwordLogger.info("[WraithSummonData] Loaded {} players with {} total wraith bindings",
                store.bindingsNBT.size(), totalBindings);
        }
    }

    // ===== 事件钩子 =====

    //世界加载时恢复数据到Manager（只在主世界加载，避免其他维度用空数据覆盖）
    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load e) {
        if (e.getLevel() instanceof ServerLevel lvl && lvl.dimension() == net.minecraft.world.level.Level.OVERWORLD) {
            loadToManager(lvl);
        }
    }

    //玩家登出时持久化到主世界（如果有改动）
    @SubscribeEvent
    public static void onPlayerLoggedOut(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp && WraithSummonManager.needsSave()) {
            ServerLevel overworld = sp.server.getLevel(net.minecraft.world.level.Level.OVERWORLD);
            if (overworld != null) {
                persistData(overworld);
            }
        }
    }

    //服务器关闭前持久化到主世界
    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent e) {
        if (!WraithSummonManager.needsSave()) return;
        ServerLevel overworld = e.getServer().getLevel(net.minecraft.world.level.Level.OVERWORLD);
        if (overworld != null) {
            persistData(overworld);
        }
    }
}
