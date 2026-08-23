package net.the_last_sword.test;

import net.eca.api.EcaAPI;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.the_last_sword.entity.TheLastEndEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//测试内容通用工具：目标选取、环境重置、冻结、禁生成、清除
public final class TestUtil {

    private TestUtil() {}

    //当前维度全部实体，排除源自身与创造模式玩家
    public static List<Entity> selectTargets(Entity source) {
        List<Entity> targets = new ArrayList<>();
        for (Entity entity : EcaAPI.getEntities(source.level())) {
            if (entity == source) continue;
            if (entity instanceof Player player && player.isCreative()) continue;
            targets.add(entity);
        }
        return targets;
    }

    //环境重置：晴天正午、停音乐、清全局效果
    public static void resetEnvironment(ServerLevel serverLevel) {
        serverLevel.setWeatherParameters(0, 0, false, false);
        serverLevel.setDayTime(6000L);
        for (ServerPlayer player : serverLevel.getServer().getPlayerList().getPlayers()) {
            player.connection.send(new ClientboundStopSoundPacket(null, SoundSource.MUSIC));
        }
        EcaAPI.clearAllGlobalEffects(serverLevel);
    }

    //冻结目标并返回其涉及的实体类型
    public static Set<EntityType<?>> freeze(List<Entity> targets) {
        Set<EntityType<?>> targetTypes = new HashSet<>();
        for (Entity target : targets) {
            targetTypes.add(target.getType());
            if (target instanceof LivingEntity living) {
                living.removeAllEffects();
                living.setTicksFrozen(Integer.MAX_VALUE);
                living.setInvisible(true);
            }
            target.getPersistentData().putBoolean("NoAI", true);
        }
        return targetTypes;
    }

    //对给定类型永久禁生成
    public static void banSpawnPermanent(ServerLevel serverLevel, Set<EntityType<?>> types) {
        for (EntityType<?> type : types) {
            if (!EcaAPI.isSpawnBanned(serverLevel, type)) {
                EcaAPI.banSpawn(serverLevel, type, Integer.MAX_VALUE);
            }
        }
    }

    //ECA清除：先退出复活追踪再解无敌，最后移除
    public static void clear(List<Entity> targets) {
        for (Entity target : targets) {
            EcaAPI.removeResurrectionTarget(target);
            if (target instanceof LivingEntity living) {
                EcaAPI.setInvulnerable(living, false);
            }
            EcaAPI.remove(target, Entity.RemovalReason.KILLED);
            if (!target.isRemoved()) {
                EcaAPI.memoryRemove(target, Entity.RemovalReason.KILLED);
            }
        }
    }

    //让本维度的测试实体与终焉种走各自后门退场
    public static void safeRemoveTestTargets(ServerLevel serverLevel) {
        for (Entity entity : EcaAPI.getEntities(serverLevel)) {
            if (entity instanceof TestEntity testEntity) {
                testEntity.safeRemove();
            } else if (entity instanceof TheLastEndEntity endEntity) {
                endEntity.safeRemove();
            }
        }
    }
}
