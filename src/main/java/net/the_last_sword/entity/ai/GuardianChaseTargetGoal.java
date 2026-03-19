package net.the_last_sword.entity.ai;

import java.util.EnumSet;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.the_last_sword.entity.GuardianOfSealedSpireEntity;

// 守卫追击目标Goal
public class GuardianChaseTargetGoal extends Goal {
    private static final double APPROACH_DISTANCE = 2.5;
    private static final int REPATH_TICKS = 10;

    private final GuardianOfSealedSpireEntity guardian;

    public GuardianChaseTargetGoal(GuardianOfSealedSpireEntity guardian) {
        this.guardian = guardian;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!guardian.canAct()) {
            return false;
        }
        // 正在攻击时不追击
        if (guardian.getAnimationState() != GuardianOfSealedSpireEntity.STATE_IDLE) {
            return false;
        }
        LivingEntity target = guardian.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        return guardian.distanceTo(target) > APPROACH_DISTANCE;
    }

    @Override
    public boolean canContinueToUse() {
        if (!guardian.canAct()) {
            return false;
        }
        if (guardian.getAnimationState() != GuardianOfSealedSpireEntity.STATE_IDLE) {
            return false;
        }
        LivingEntity target = guardian.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        return guardian.distanceTo(target) > APPROACH_DISTANCE;
    }

    @Override
    public void tick() {
        LivingEntity target = guardian.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        guardian.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (guardian.tickCount % REPATH_TICKS == 0 || !guardian.getNavigation().isInProgress()) {
            guardian.getNavigation().moveTo(target, 1.0);
        }
    }

    @Override
    public void stop() {
        guardian.getNavigation().stop();
    }
}
