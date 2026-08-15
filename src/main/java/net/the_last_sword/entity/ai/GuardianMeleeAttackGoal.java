package net.the_last_sword.entity.ai;

import java.util.EnumSet;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.GuardianOfSealedSpireEntity;
import net.the_last_sword.util.EntityUtil;

// 守卫近战攻击Goal
public class GuardianMeleeAttackGoal extends Goal {
    private static final int ANIMATION_LENGTH = 25;
    private static final int DAMAGE_TICK = 15;
    private static final double HIT_RANGE_GRACE = 1.0;

    private final GuardianOfSealedSpireEntity guardian;
    private int animationTick;
    private boolean damaged;

    public GuardianMeleeAttackGoal(GuardianOfSealedSpireEntity guardian) {
        this.guardian = guardian;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!guardian.canAct()) {
            return false;
        }
        LivingEntity target = guardian.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        return guardian.distanceTo(target) <= TheLastSwordConfiguration.getGuardianMeleeAttackRangeSafely();
    }

    @Override
    public boolean canContinueToUse() {
        return animationTick > 0;
    }

    @Override
    public void start() {
        animationTick = ANIMATION_LENGTH;
        damaged = false;
        guardian.setAnimationState(GuardianOfSealedSpireEntity.STATE_ATTACK);
        guardian.getNavigation().stop();

        LivingEntity target = guardian.getTarget();
        if (target != null) {
            EntityUtil.faceTarget(guardian, target);
        }
    }

    @Override
    public void tick() {
        if (animationTick <= 0) {
            return;
        }

        int relativeFrame = ANIMATION_LENGTH - animationTick;

        if (relativeFrame == DAMAGE_TICK && !damaged) {
            dealDamage();
            damaged = true;
        }

        animationTick--;

        LivingEntity target = guardian.getTarget();
        if (target != null && target.isAlive()) {
            guardian.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        // 动画结束时检查是否继续攻击
        if (animationTick == 0) {
            if (target != null && target.isAlive() && guardian.distanceTo(target) <= TheLastSwordConfiguration.getGuardianMeleeAttackRangeSafely()) {
                // 目标仍在范围内，继续攻击
                animationTick = ANIMATION_LENGTH;
                damaged = false;
                EntityUtil.faceTarget(guardian, target);
            } else {
                // 目标不在范围，回到待机
                guardian.setAnimationState(GuardianOfSealedSpireEntity.STATE_IDLE);
            }
        }
    }

    @Override
    public void stop() {
        animationTick = 0;
        damaged = false;
        if (guardian.getAnimationState() == GuardianOfSealedSpireEntity.STATE_ATTACK) {
            guardian.setAnimationState(GuardianOfSealedSpireEntity.STATE_IDLE);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private void dealDamage() {
        LivingEntity target = guardian.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        double distance = guardian.distanceTo(target);
        double hitRange = TheLastSwordConfiguration.getGuardianMeleeAttackRangeSafely()
            + HIT_RANGE_GRACE;
        if (distance > hitRange) {
            return;
        }

        EntityUtil.faceTarget(guardian, target);
        target.invulnerableTime = 0;
        guardian.doHurtTarget(target);
    }
}
