package net.the_last_sword.test;

import net.eca.api.EcaAPI;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.the_last_sword.configuration.TheLastSwordConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PowerfulRangeAttack {

    private PowerfulRangeAttack() {}

    public static void execute(Entity source) {
        if (!(source.level() instanceof ServerLevel serverLevel)) return;

        // 以触发者为中心 1024 格，排除自身和创造模式玩家
        AABB area = source.getBoundingBox().inflate(1024);
        List<Entity> targets = new ArrayList<>();
        for (Entity e : EcaAPI.getEntities(serverLevel, area)) {
            if (e == source) continue;
            if (e instanceof ServerPlayer sp && sp.isCreative()) continue;
            targets.add(e);
        }

        // 1. 环境重置
        serverLevel.setWeatherParameters(0, 0, false, false);
        serverLevel.setDayTime(6000L);
        for (ServerPlayer sp : serverLevel.getServer().getPlayerList().getPlayers()) {
            sp.connection.send(new ClientboundStopSoundPacket(null, SoundSource.MUSIC));
        }
        EcaAPI.clearAllGlobalEffects(serverLevel);

        // 2. 杂项设置（冻结、隐身、NoAI）+ 收集类型
        Set<EntityType<?>> targetTypes = new HashSet<>();
        for (Entity target : targets) {
            targetTypes.add(target.getType());
            if (target instanceof LivingEntity le) {
                le.removeAllEffects();
                le.setTicksFrozen(Integer.MAX_VALUE);
                le.setInvisible(true);
            }
            target.getPersistentData().putBoolean("NoAI", true);
        }

        // 3. ECA 禁复活
        int reviveBanSeconds = Math.max(0, TheLastSwordConfiguration.getReviveBanTimeSafely());
        if (reviveBanSeconds > 0) {
            for (EntityType<?> entityType : targetTypes) {
                EcaAPI.banSpawn(serverLevel, entityType, reviveBanSeconds);
            }
        }

        // 4-6. 逐目标处理
        for (Entity target : targets) {
            // 4. 关闭 ECA 无敌
            if (target instanceof LivingEntity living) {
                EcaAPI.setInvulnerable(living, false);
            }
            // 5. ECA 清除
            EcaAPI.remove(target, Entity.RemovalReason.KILLED);
            // 6. 若实体仍存在，内存清除
            if (!target.isRemoved()) {
                EcaAPI.memoryRemove(target, Entity.RemovalReason.KILLED);
            }
        }
    }
}
