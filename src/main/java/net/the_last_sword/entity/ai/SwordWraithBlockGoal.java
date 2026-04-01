package net.the_last_sword.entity.ai;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.TheLastEndSwordWraithEntity;
import net.the_last_sword.util.EntityUtil;

import java.util.EnumSet;
import java.util.List;

//格挡技能Goal
public class SwordWraithBlockGoal extends Goal {
    private final TheLastEndSwordWraithEntity wraith;
    private int animationTick;
    private long lastUseTime;

    private static final int ANIMATION_LENGTH = 15;
    private static final double KNOCKBACK_STRENGTH = 0.4;
    private static final int COOLDOWN = 200; // 10秒

    public SwordWraithBlockGoal(TheLastEndSwordWraithEntity wraith) {
        this.wraith = wraith;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!wraith.canAct()) {
            return false;
        }
        if (wraith.getAnimationState() != TheLastEndSwordWraithEntity.STATE_IDLE) {
            return false;
        }

        LivingEntity target = wraith.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }

        if (wraith.distanceTo(target) > 4.0) {
            return false;
        }

        return wraith.level().getGameTime() - lastUseTime >= COOLDOWN;
    }

    @Override
    public void start() {
        animationTick = ANIMATION_LENGTH;
        wraith.setAnimationState(TheLastEndSwordWraithEntity.STATE_BLOCK);
        wraith.getNavigation().stop();
        lastUseTime = wraith.level().getGameTime();
    }

    @Override
    public void tick() {
        if (animationTick <= 0) {
            return;
        }

        applyKnockbackAndDamage();
        deflectProjectiles();

        animationTick--;

        LivingEntity target = wraith.getTarget();
        if (target != null && target.isAlive()) {
            wraith.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        if (animationTick == 0) {
            wraith.setAnimationState(TheLastEndSwordWraithEntity.STATE_IDLE);
        }
    }

    @Override
    public boolean canContinueToUse() {
        return animationTick > 0;
    }

    @Override
    public void stop() {
        animationTick = 0;
        if (wraith.getAnimationState() == TheLastEndSwordWraithEntity.STATE_BLOCK) {
            wraith.setAnimationState(TheLastEndSwordWraithEntity.STATE_IDLE);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    //对前方半圆范围内的目标施加击退和伤害
    private void applyKnockbackAndDamage() {
        double range = TheLastSwordConfiguration.getSkillBlockRangeSafely();
        float damageMultiplier = (float) TheLastSwordConfiguration.getSkillBlockDamageMultiplierSafely();
        float damage = (float) wraith.getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier;

        List<LivingEntity> targets = EntityUtil.getTargetsInHemisphere(wraith, range);
        for (LivingEntity target : targets) {
            Vec3 knockbackDir = new Vec3(
                target.getX() - wraith.getX(), 0, target.getZ() - wraith.getZ()
            ).normalize();

            target.setDeltaMovement(target.getDeltaMovement().add(
                knockbackDir.x * KNOCKBACK_STRENGTH, 0.1, knockbackDir.z * KNOCKBACK_STRENGTH
            ));

            target.invulnerableTime = 0;
            target.hurt(wraith.damageSources().mobAttack(wraith), damage);
            wraith.addEndMark();
        }
    }

    //偏转前方的弹射物
    private void deflectProjectiles() {
        double range = TheLastSwordConfiguration.getSkillBlockRangeSafely();
        AABB searchBox = wraith.getBoundingBox().inflate(range);
        Vec3 wraithLook = wraith.getLookAngle();

        List<Entity> nearbyEntities = wraith.level().getEntities(wraith, searchBox);
        for (net.minecraft.world.entity.Entity entity : nearbyEntities) {
            if (entity instanceof Projectile projectile) {
                Vec3 toProjectile = new Vec3(
                    projectile.getX() - wraith.getX(), 0, projectile.getZ() - wraith.getZ()
                ).normalize();

                double dotProduct = wraithLook.x * toProjectile.x + wraithLook.z * toProjectile.z;
                if (dotProduct > 0) {
                    double speed = projectile.getDeltaMovement().length();
                    double randomX = (wraith.getRandom().nextDouble() - 0.5) * 0.5;
                    double randomZ = (wraith.getRandom().nextDouble() - 0.5) * 0.5;
                    projectile.setDeltaMovement(randomX * speed, speed * 0.8, randomZ * speed);
                }
            }
        }
    }
}
