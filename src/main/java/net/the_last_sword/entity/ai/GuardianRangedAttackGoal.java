package net.the_last_sword.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.the_last_sword.entity.GuardianArcherEntity;
import net.the_last_sword.entity.GuardianOfSealedSpireEntity;
import net.the_last_sword.util.EntityUtil;

import java.util.EnumSet;

// 守卫弓箭手远程攻击Goal
public class GuardianRangedAttackGoal extends Goal {
    private static final int ANIMATION_LENGTH = 30;
    private static final int FIRE_TICK = 22;
    private static final double MIN_DISTANCE = 4.0;
    private static final double MAX_DISTANCE = 16.0;

    private final GuardianArcherEntity archer;
    private int animationTick;
    private boolean fired;

    public GuardianRangedAttackGoal(GuardianArcherEntity archer) {
        this.archer = archer;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!archer.canAct()) {
            return false;
        }
        LivingEntity target = archer.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        double distance = archer.distanceTo(target);
        return distance >= MIN_DISTANCE && distance <= MAX_DISTANCE;
    }

    @Override
    public boolean canContinueToUse() {
        return animationTick > 0;
    }

    @Override
    public void start() {
        animationTick = ANIMATION_LENGTH;
        fired = false;
        archer.setAnimationState(GuardianOfSealedSpireEntity.STATE_ATTACK);
        archer.getNavigation().stop();

        LivingEntity target = archer.getTarget();
        if (target != null) {
            EntityUtil.faceTarget(archer, target);
        }
    }

    @Override
    public void tick() {
        if (animationTick <= 0) {
            return;
        }

        int relativeFrame = ANIMATION_LENGTH - animationTick;

        if (relativeFrame == FIRE_TICK && !fired) {
            shootArrow();
            fired = true;
        }

        animationTick--;

        LivingEntity target = archer.getTarget();
        if (target != null && target.isAlive()) {
            archer.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        // 动画结束时回到待机
        if (animationTick == 0) {
            archer.setAnimationState(GuardianOfSealedSpireEntity.STATE_IDLE);
        }
    }

    @Override
    public void stop() {
        animationTick = 0;
        fired = false;
        if (archer.getAnimationState() == GuardianOfSealedSpireEntity.STATE_ATTACK) {
            archer.setAnimationState(GuardianOfSealedSpireEntity.STATE_IDLE);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private void shootArrow() {
        LivingEntity target = archer.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        EntityUtil.faceTarget(archer, target);
        ItemStack bow = archer.getMainHandItem();
        EntityUtil.shootArrow(archer, target, bow);
    }
}
