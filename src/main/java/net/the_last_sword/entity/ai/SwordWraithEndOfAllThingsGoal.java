package net.the_last_sword.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.the_last_sword.entity.TheLastEndSwordWraithEntity;

import java.util.EnumSet;

//万物终焉技能Goal
public class SwordWraithEndOfAllThingsGoal extends Goal {
    private final TheLastEndSwordWraithEntity wraith;
    private int animationTick;

    private static final int ANIMATION_LENGTH = 190;
    private static final int ACTIVATE_TICK = 20;

    public SwordWraithEndOfAllThingsGoal(TheLastEndSwordWraithEntity wraith) {
        this.wraith = wraith;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (wraith.getAnimationState() != TheLastEndSwordWraithEntity.STATE_IDLE) {
            return false;
        }

        LivingEntity target = wraith.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }

        //目标的终焉标记≥13时触发
        return TheLastEndSwordWraithEntity.getEndMark(target) >= TheLastEndSwordWraithEntity.END_MARK_THRESHOLD;
    }

    @Override
    public void start() {
        animationTick = ANIMATION_LENGTH;
        wraith.setAnimationState(TheLastEndSwordWraithEntity.STATE_END_OF_ALL_THINGS);
        wraith.getNavigation().stop();
    }

    @Override
    public void tick() {
        animationTick--;

        //激活万物终焉状态并启动持续效果
        if (animationTick == ACTIVATE_TICK) {
            wraith.setAllThingsEnd(true);
            TheLastEndSwordWraithEntity.AllThingsEndActiveEffect.start(wraith);
        }
    }

    @Override
    public boolean canContinueToUse() {
        return animationTick > 0;
    }

    @Override
    public void stop() {
        wraith.setAnimationState(TheLastEndSwordWraithEntity.STATE_IDLE);
    }
}
