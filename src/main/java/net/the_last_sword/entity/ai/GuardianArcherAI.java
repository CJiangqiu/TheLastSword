package net.the_last_sword.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.entity.GuardianArcherEntity;
import net.the_last_sword.entity.GuardianOfSealedSpireEntity;
import net.the_last_sword.util.EntityUtil;

import java.util.Comparator;
import java.util.List;

//封印尖塔守卫弓箭手AI
public class GuardianArcherAI extends TheLastEndAI {

    //AI常量
    private static final double MIN_DISTANCE = 4.0;   //最小保持距离
    private static final double MAX_DISTANCE = 16.0;  //最大攻击距离
    private static final double MAX_SEARCH_DISTANCE = 32.0;

    public GuardianArcherAI(GuardianArcherEntity entity) {
        super(entity);
    }

    @Override
    protected void registerSkills() {
        //注册射击技能
        addSkill(new ShootSkill((GuardianArcherEntity) entity));
    }

    @Override
    protected void aiTick() {
        GuardianArcherEntity archer = (GuardianArcherEntity) entity;

        //目标管理（每20tick检查一次）
        if (archer.tickCount % 20 == 0) {
            findAndUpdateTarget(archer);
        }

        //检查是否有有效目标
        LivingEntity target = archer.getTarget();
        if (!isValidTarget(target)) {
            archer.setTarget(null);
            archer.getNavigation().stop();
            return;
        }

        double distance = archer.distanceTo(target);

        //面向目标
        archer.getLookControl().setLookAt(target, 30.0F, 30.0F);

        //移动逻辑：保持距离
        if (distance < MIN_DISTANCE) {
            //太近了，后退
            retreatFrom(archer, target);
        } else if (distance > MAX_DISTANCE) {
            //太远了，接近
            if (!archer.getNavigation().isInProgress()) {
                archer.getNavigation().moveTo(target, 1.0);
            }
        } else {
            //在射程内，停止移动并尝试射击
            archer.getNavigation().stop();
            tryStartShooting(archer);
        }
    }

    //后退逻辑
    private void retreatFrom(GuardianArcherEntity archer, LivingEntity target) {
        Vec3 archerPos = archer.position();
        Vec3 targetPos = target.position();
        Vec3 direction = archerPos.subtract(targetPos).normalize();

        //向远离目标的方向移动
        double retreatX = archerPos.x + direction.x * 3;
        double retreatZ = archerPos.z + direction.z * 3;

        archer.getNavigation().moveTo(retreatX, archerPos.y, retreatZ, 1.2);
    }

    //尝试开始射击
    private void tryStartShooting(GuardianArcherEntity archer) {
        //使用射击技能
        Skill shootSkill = getSkillByAnimation("shoot");
        if (shootSkill != null) {
            useSkill(shootSkill);
        }
    }

    //检查目标是否有效
    private boolean isValidTarget(LivingEntity target) {
        if (target == null || !target.isAlive() || target.isRemoved()) {
            return false;
        }
        //不攻击同类守卫
        if (target instanceof GuardianOfSealedSpireEntity) {
            return false;
        }
        if (target instanceof Player player) {
            return !player.isCreative() && !player.isSpectator();
        }
        return true;
    }

    //寻找并更新目标
    private void findAndUpdateTarget(GuardianArcherEntity archer) {
        LivingEntity currentTarget = archer.getTarget();

        //验证当前目标
        if (!isValidTarget(currentTarget) ||
            (currentTarget != null && archer.distanceTo(currentTarget) > MAX_SEARCH_DISTANCE)) {
            archer.setTarget(null);
            currentTarget = null;
        }

        //反击攻击者
        LivingEntity attacker = archer.getLastHurtByMob();
        if (isValidTarget(attacker) && archer.distanceTo(attacker) <= MAX_SEARCH_DISTANCE) {
            archer.setTarget(attacker);
            alertNearbyGuardians(archer, attacker);
            return;
        }

        //响应附近守卫的求援
        if (currentTarget == null) {
            LivingEntity allyTarget = findAllyTarget(archer);
            if (allyTarget != null) {
                archer.setTarget(allyTarget);
                return;
            }
        }

        //寻找玩家目标
        if (currentTarget == null) {
            List<Player> nearbyPlayers = archer.level().getEntitiesOfClass(
                Player.class,
                archer.getBoundingBox().inflate(MAX_SEARCH_DISTANCE),
                player -> player.isAlive() && !player.isSpectator() && !player.isCreative()
            );

            if (!nearbyPlayers.isEmpty()) {
                Player closestPlayer = nearbyPlayers.stream()
                    .min(Comparator.comparingDouble(p -> p.distanceToSqr(archer)))
                    .orElse(null);

                if (closestPlayer != null) {
                    archer.setTarget(closestPlayer);
                }
            }
        }
    }

    //向周围守卫求援
    private void alertNearbyGuardians(GuardianArcherEntity archer, LivingEntity attacker) {
        List<GuardianOfSealedSpireEntity> nearbyGuardians = archer.level().getEntitiesOfClass(
            GuardianOfSealedSpireEntity.class,
            archer.getBoundingBox().inflate(MAX_SEARCH_DISTANCE),
            g -> g != archer && g.isAlive() && !g.shouldLeave()
        );

        for (GuardianOfSealedSpireEntity ally : nearbyGuardians) {
            if (ally.getTarget() == null) {
                ally.setTarget(attacker);
            }
        }
    }

    //查找附近被攻击守卫的目标
    private LivingEntity findAllyTarget(GuardianArcherEntity archer) {
        List<GuardianOfSealedSpireEntity> nearbyGuardians = archer.level().getEntitiesOfClass(
            GuardianOfSealedSpireEntity.class,
            archer.getBoundingBox().inflate(MAX_SEARCH_DISTANCE),
            g -> g != archer && g.isAlive() && !g.shouldLeave() && g.getTarget() != null
        );

        for (GuardianOfSealedSpireEntity ally : nearbyGuardians) {
            LivingEntity allyTarget = ally.getTarget();
            if (isValidTarget(allyTarget) && archer.distanceTo(allyTarget) <= MAX_SEARCH_DISTANCE) {
                return allyTarget;
            }
        }
        return null;
    }

    // ============ 内部技能类 ============

    //射击技能
    private static class ShootSkill extends Skill {
        private static final int SHOOT_DURATION = 30;    //1.5秒
        private static final int SHOOT_FIRE_TICK = 20;   //1秒时发射箭矢

        public ShootSkill(GuardianArcherEntity entity) {
            super(entity, "shoot", SHOOT_DURATION);
        }

        @Override
        protected void defineKeyframes() {
            //1秒时发射箭矢
            addKeyframe(SHOOT_FIRE_TICK, () -> {
                GuardianArcherEntity archer = (GuardianArcherEntity) entity;
                LivingEntity target = archer.getTarget();

                if (target != null && target.isAlive()) {
                    //面向目标
                    EntityUtil.faceTarget(archer, target);

                    //发射箭矢
                    ItemStack bow = archer.getMainHandItem();
                    EntityUtil.shootArrow(archer, target, bow);
                }
            });
        }
    }
}
