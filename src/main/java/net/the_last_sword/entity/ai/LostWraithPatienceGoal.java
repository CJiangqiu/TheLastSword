package net.the_last_sword.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.LostWraithEntity;

// 迷失战魂耐心机制Goal
public class LostWraithPatienceGoal extends Goal {
    private final LostWraithEntity wraith;

    public LostWraithPatienceGoal(LostWraithEntity wraith) {
        this.wraith = wraith;
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = wraith.getTarget();
        if (!wraith.canAct() || target == null || !target.isAlive()) {
            wraith.resetPatience();
            wraith.clearForceEndStrike();
            return;
        }

        // 技能施放中不累计耐心
        if (wraith.getAnimationState() != LostWraithEntity.STATE_IDLE) {
            return;
        }

        double distance = wraith.distanceTo(target);
        if (distance > TheLastSwordConfiguration.getLostWraithPatienceTriggerDistanceSafely()) {
            int patienceTick = wraith.incrementPatienceTick();
            if (patienceTick >= TheLastSwordConfiguration.getLostWraithPatienceTimeoutSafely()) {
                wraith.setForceEndStrike(true);
                wraith.resetPatience();
            }
        } else {
            wraith.resetPatience();
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
