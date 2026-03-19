package net.the_last_sword.entity.ai;

import java.util.EnumSet;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.the_last_sword.entity.LostWraithEntity;
import net.the_last_sword.util.EntityUtil;

// 迷失战魂追击目标Goal
public class LostWraithChaseTargetGoal extends Goal {
    private static final double APPROACH_DISTANCE = 4.0;
    private static final int REPATH_TICKS = 10;

    private final LostWraithEntity wraith;

    public LostWraithChaseTargetGoal(LostWraithEntity wraith) {
        this.wraith = wraith;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!wraith.canAct()) {
            return false;
        }
        if (wraith.getAnimationState() != LostWraithEntity.STATE_IDLE) {
            return false;
        }
        LivingEntity target = wraith.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (!EntityUtil.canAttack(wraith, target)) {
            return false;
        }
        return wraith.distanceTo(target) > APPROACH_DISTANCE;
    }

    @Override
    public boolean canContinueToUse() {
        if (!wraith.canAct()) {
            return false;
        }
        if (wraith.getAnimationState() != LostWraithEntity.STATE_IDLE) {
            return false;
        }
        LivingEntity target = wraith.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (!EntityUtil.canAttack(wraith, target)) {
            return false;
        }
        return wraith.distanceTo(target) > APPROACH_DISTANCE;
    }

    @Override
    public void tick() {
        LivingEntity target = wraith.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        wraith.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (wraith.tickCount % REPATH_TICKS == 0 || !wraith.getNavigation().isInProgress()) {
            wraith.getNavigation().moveTo(target, 1.0);
        }
    }

    @Override
    public void stop() {
        wraith.getNavigation().stop();
    }
}
