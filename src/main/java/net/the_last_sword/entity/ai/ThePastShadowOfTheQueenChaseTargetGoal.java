package net.the_last_sword.entity.ai;

import java.util.EnumSet;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.the_last_sword.entity.ThePastShadowOfTheQueenEntity;
import net.the_last_sword.util.EntityUtil;

// 女皇在基础状态下接近目标，为后续攻击技能保留施放距离
public class ThePastShadowOfTheQueenChaseTargetGoal extends Goal {
    private static final double APPROACH_DISTANCE = 4.0;

    private final ThePastShadowOfTheQueenEntity queen;

    public ThePastShadowOfTheQueenChaseTargetGoal(ThePastShadowOfTheQueenEntity queen) {
        this.queen = queen;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return canChaseTarget();
    }

    @Override
    public boolean canContinueToUse() {
        return canChaseTarget();
    }

    private boolean canChaseTarget() {
        if (!queen.canAct() || queen.getAnimationState() != ThePastShadowOfTheQueenEntity.STATE_IDLE) {
            return false;
        }

        LivingEntity target = queen.getTarget();
        return target != null
                && target.isAlive()
                && EntityUtil.canAttack(queen, target)
                && queen.distanceTo(target) > APPROACH_DISTANCE;
    }

    @Override
    public void tick() {
        LivingEntity target = queen.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        queen.getLookControl().setLookAt(target, 30.0F, 30.0F);
        queen.getNavigation().moveTo(target, 1.0);
    }

    @Override
    public void stop() {
        queen.getNavigation().stop();
    }
}
