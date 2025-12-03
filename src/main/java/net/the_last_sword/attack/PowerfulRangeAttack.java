package net.the_last_sword.attack;

import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.entity.TheLastEndEntity;
import net.the_last_sword.test.TestEntity;
import net.the_last_sword.util.EntityUtil;

import java.util.function.Predicate;

//强力范围攻击：前两步仅服务端环境重置，第③步双端清场；排除施法者自身和创造模式玩家
public final class PowerfulRangeAttack {

    private PowerfulRangeAttack() {}

    //传入施法者source
    public static void execute(Level lvl, Entity source, Vec3 center) {

        //PowerfulRangeAttack专用过滤条件：排除施法者本身和创造模式玩家
        Predicate<Entity> targetFilter = e -> {
            if (e == source) return false;  //排除施法者自己
            if (e instanceof ServerPlayer sp && sp.gameMode.getGameModeForPlayer() == GameType.CREATIVE) {
                return false;  //排除创造模式玩家
            }
            return true;
        };

        boolean isServer = lvl instanceof ServerLevel;
        AABB box = new AABB(center, center).inflate(1024.0D);

        //①② 仅服务端做环境重置
        if (isServer) {
            ServerLevel server = (ServerLevel) lvl;

            server.getEntitiesOfClass(Entity.class, box, targetFilter)
                  .forEach(e -> {
                      if (e instanceof LivingEntity le) {
                          le.removeAllEffects();
                          le.setTicksFrozen(Integer.MAX_VALUE);
                          le.setInvisible(true);
                      }
                      e.getPersistentData().putBoolean("NoAI", true);
                  });

            server.setWeatherParameters(0, 0, false, false);
            server.setDayTime(6000L);
            for (ServerPlayer sp : server.getServer().getPlayerList().getPlayers()) {
                sp.connection.send(new ClientboundStopSoundPacket(null, SoundSource.MUSIC));
            }
        }

        //③ 双端清场（客户端走本地分支，服务端走深度分支）

        lvl.getEntitiesOfClass(Entity.class, box, targetFilter)
           .forEach(t -> {
               //内部实体使用safeRemove后门
               if (t instanceof TheLastEndEntity theLastEndEntity) {
                   theLastEndEntity.safeRemove();
                   return;
               }
               if (t instanceof TestEntity testEntity) {
                   testEntity.safeRemove();
                   return;
               }

               //其他实体使用常规清除
               if (isServer && t instanceof LivingEntity le) {
                   AttackManager.addReviveBan(le);
               }
               EntityUtil.theLastEndRemove(t, Entity.RemovalReason.KILLED);
           });
    }

}
