package net.the_last_sword.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.the_last_sword.entity.DragonCultistEntity;

import java.util.EnumSet;

// 教徒近战Goal，HP>50%时靠近目标，≤3格触发攻击动画
public class DragonCultistMeleeAttackGoal extends Goal {
    private static final int ANIMATION_LENGTH = 15;
    private static final int DAMAGE_TICK = 10;
    private static final float MELEE_RANGE = 3.0f;
    private static final float ATTACK_RANGE = 3.0f;

    private final DragonCultistEntity cultist;
    private int animationTick;
    private boolean damaged;

    public DragonCultistMeleeAttackGoal(DragonCultistEntity cultist) {
        this.cultist = cultist;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!cultist.canAct()) {
            return false;
        }
        LivingEntity target = cultist.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        // HP≤50%时不近战，交给魔法Goal
        if (getHpRatio() <= 0.5f) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (animationTick > 0) {
            return true;
        }
        LivingEntity target = cultist.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (getHpRatio() <= 0.5f) {
            return false;
        }
        return cultist.distanceTo(target) <= cultist.getAttributeValue(Attributes.FOLLOW_RANGE);
    }

    @Override
    public void start() {
        animationTick = 0;
        damaged = false;
    }

    @Override
    public void tick() {
        LivingEntity target = cultist.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        // 正在播放攻击动画
        if (animationTick > 0) {
            int relativeFrame = ANIMATION_LENGTH - animationTick;
            if (relativeFrame == DAMAGE_TICK && !damaged) {
                dealDamage();
                damaged = true;
            }
            animationTick--;
            cultist.getLookControl().setLookAt(target, 30.0F, 30.0F);

            if (animationTick == 0) {
                cultist.setAnimationState(DragonCultistEntity.STATE_IDLE);
            }
            return;
        }

        float dist = cultist.distanceTo(target);

        // 距离≤3格，触发攻击
        if (dist <= ATTACK_RANGE) {
            animationTick = ANIMATION_LENGTH;
            damaged = false;
            cultist.setAnimationState(DragonCultistEntity.STATE_ATTACK);
            cultist.getNavigation().stop();
            cultist.getLookControl().setLookAt(target, 30.0F, 30.0F);
            return;
        }

        // 靠近目标
        cultist.getNavigation().moveTo(target, 1.0);
        cultist.getLookControl().setLookAt(target, 30.0F, 30.0F);
    }

    @Override
    public void stop() {
        animationTick = 0;
        damaged = false;
        if (cultist.getAnimationState() == DragonCultistEntity.STATE_ATTACK) {
            cultist.setAnimationState(DragonCultistEntity.STATE_IDLE);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private void dealDamage() {
        LivingEntity target = cultist.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        if (cultist.distanceTo(target) > ATTACK_RANGE + 1.0f) {
            return;
        }
        target.invulnerableTime = 0;
        cultist.doHurtTarget(target);
    }

    private float getHpRatio() {
        float max = cultist.getWorldAnchorMax();
        if (max <= 0) return 1.0f;
        return cultist.getWorldAnchor() / max;
    }
}
