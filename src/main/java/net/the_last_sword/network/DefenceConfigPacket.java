package net.the_last_sword.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.the_last_sword.configuration.DefenceConfigData;
import net.the_last_sword.util.TheLastSwordLogger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

//同步防御配置到服务端的网络包
public class DefenceConfigPacket {

    private static final Gson GSON = new GsonBuilder().create();

    //服务端缓存：玩家UUID -> 配置数据
    private static final Map<UUID, DefenceConfigData> SERVER_CONFIG_CACHE = new ConcurrentHashMap<>();

    private final String configJson;

    public DefenceConfigPacket(DefenceConfigData configData) {
        this.configJson = GSON.toJson(configData);
    }

    public DefenceConfigPacket(FriendlyByteBuf buf) {
        this.configJson = buf.readUtf(32767); // 最大32KB
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(configJson, 32767);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                try {
                    DefenceConfigData configData = GSON.fromJson(configJson, DefenceConfigData.class);
                    if (configData != null) {
                        SERVER_CONFIG_CACHE.put(player.getUUID(), configData);
                        TheLastSwordLogger.debug("Synced defence config for player: " + player.getName().getString());
                    }
                } catch (Exception e) {
                    TheLastSwordLogger.error("Failed to parse defence config from client", e);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    //服务端获取玩家的配置
    public static DefenceConfigData getPlayerConfig(UUID playerUUID) {
        return SERVER_CONFIG_CACHE.getOrDefault(playerUUID, new DefenceConfigData());
    }

    //服务端获取玩家的配置（通过玩家对象）
    public static DefenceConfigData getPlayerConfig(ServerPlayer player) {
        return getPlayerConfig(player.getUUID());
    }

    //清除玩家配置缓存（玩家退出时调用）
    public static void clearCache(UUID playerUUID) {
        SERVER_CONFIG_CACHE.remove(playerUUID);
    }

    //清除所有缓存
    public static void clearAllCache() {
        SERVER_CONFIG_CACHE.clear();
    }
}
