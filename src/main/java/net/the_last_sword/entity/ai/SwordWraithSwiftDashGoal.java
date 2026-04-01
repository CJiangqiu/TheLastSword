package net.the_last_sword.entity.ai;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.TheLastEndSwordWraithEntity;
import net.the_last_sword.util.EntityUtil;

import java.util.EnumSet;
import java.util.List;

//疾速突进技能Goal
public class SwordWraithSwiftDashGoal extends Goal {
    private final TheLastEndSwordWraithEntity wraith;
    private int animationTick;

    private long lastUseTime;

    private static final int ANIMATION_LENGTH = 60;
    private static final int DASH_START_TICK = 35;
    private static final int DASH_END_TICK = 55;
    private static final double DASH_SPEED = 1.5;
    private static final double TRIGGER_DISTANCE = 6.0;
    private static final double DASH_SUCCESS_DISTANCE = 4.0;
    private static final int COOLDOWN = 200; // 10秒

    public SwordWraithSwiftDashGoal(TheLastEndSwordWraithEntity wraith) {
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

        if (wraith.distanceTo(target) <= TRIGGER_DISTANCE) {
            return false;
        }
        return wraith.level().getGameTime() - lastUseTime >= COOLDOWN;
    }

    @Override
    public void start() {
        animationTick = ANIMATION_LENGTH;
        lastUseTime = wraith.level().getGameTime();
        wraith.setAnimationState(TheLastEndSwordWraithEntity.STATE_SWIFT_DASH);
        wraith.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (animationTick <= 0) {
            return;
        }

        LivingEntity target = wraith.getTarget();

        //持续冲刺阶段
        if (animationTick <= DASH_END_TICK && animationTick >= DASH_START_TICK) {
            applyContinuousDash(target);
        }

        //冲刺结束时执行最后一击
        if (animationTick == DASH_START_TICK) {
            executeFinalStrike();
        }

        animationTick--;

        if (target != null && target.isAlive()) {
            wraith.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        //动画结束时检查距离，决定是否强制十字切
        if (animationTick == 0) {
            if (target != null && target.isAlive() && wraith.distanceTo(target) > DASH_SUCCESS_DISTANCE) {
                TheLastEndSwordWraithEntity.FORCE_CROSS_SLASH.put(wraith.getUUID(), true);
            }
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
        if (wraith.getAnimationState() == TheLastEndSwordWraithEntity.STATE_SWIFT_DASH) {
            wraith.setAnimationState(TheLastEndSwordWraithEntity.STATE_IDLE);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    //持续冲刺逻辑
    private void applyContinuousDash(LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return;
        }

        Vec3 direction = target.position().subtract(wraith.position()).normalize();
        wraith.setDeltaMovement(
            direction.x * DASH_SPEED,
            Math.max(0.2, direction.y * DASH_SPEED),
            direction.z * DASH_SPEED
        );
    }

    //执行突刺最后一击
    private void executeFinalStrike() {
        double attackRange = TheLastSwordConfiguration.getSkillSwiftDashRangeSafely();
        float damageMultiplier = (float) TheLastSwordConfiguration.getSkillSwiftDashDamageMultiplierSafely();
        float damage = (float) wraith.getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier;

        List<LivingEntity> targets = EntityUtil.getTargetsInHemisphere(wraith, attackRange);
        for (LivingEntity target : targets) {
            target.invulnerableTime = 0;
            target.hurt(wraith.damageSources().mobAttack(wraith), damage);
            wraith.addEndMark();
        }

        wraith.level().playSound(null, wraith.getX(), wraith.getY(), wraith.getZ(),
            SoundEvents.PLAYER_ATTACK_SWEEP, wraith.getSoundSource(), 1.0F, 1.0F);
    }
}
