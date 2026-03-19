package net.the_last_sword.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.the_last_sword.entity.TheLastEndSwordWraithEntity;

import java.util.EnumSet;

//追踪目标Goal
public class SwordWraithChaseTargetGoal extends Goal {
    private final TheLastEndSwordWraithEntity wraith;
    private static final double CHASE_DISTANCE = 4.0;

    public SwordWraithChaseTargetGoal(TheLastEndSwordWraithEntity wraith) {
        this.wraith = wraith;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (wraith.getAnimationState() != TheLastEndSwordWraithEntity.STATE_IDLE) {
            return false;
        }

        LivingEntity target = wraith.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        if (wraith.getAnimationState() != TheLastEndSwordWraithEntity.STATE_IDLE) {
            return false;
        }

        LivingEntity target = wraith.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public void tick() {
        LivingEntity target = wraith.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        double distance = wraith.distanceTo(target);

        //超过追踪距离时导航到目标
        if (distance > CHASE_DISTANCE) {
            if (wraith.tickCount % 10 == 0) {
                wraith.getNavigation().moveTo(target, 1.0);
            }
        } else {
            wraith.getNavigation().stop();
        }

        wraith.getLookControl().setLookAt(target, 30.0F, 30.0F);
    }

    @Override
    public void stop() {
        wraith.getNavigation().stop();
    }
}
