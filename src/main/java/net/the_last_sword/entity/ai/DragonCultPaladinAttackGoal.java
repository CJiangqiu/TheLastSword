package net.the_last_sword.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.DragonCultPaladinEntity;

import java.util.EnumSet;

// 圣骑士普攻Goal，超出范围则接近，attack动画0.55s与1.1s各结算一次伤害
public class DragonCultPaladinAttackGoal extends Goal {

    //attack 动画全长 1.3 秒
    private static final int ANIMATION_LENGTH = 26;

    //两段伤害帧（0.55s / 1.1s）
    private static final int FIRST_HIT_TICK = 11;
    private static final int SECOND_HIT_TICK = 22;

    private final DragonCultPaladinEntity paladin;
    private int animationTick;

    public DragonCultPaladinAttackGoal(DragonCultPaladinEntity paladin) {
        this.paladin = paladin;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!paladin.canAct()) {
            return false;
        }
        if (paladin.getAnimationState() != DragonCultPaladinEntity.STATE_IDLE) {
            return false;
        }
        LivingEntity target = paladin.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        if (animationTick > 0) {
            return true;
        }
        LivingEntity target = paladin.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        return paladin.distanceTo(target) <= paladin.getAttributeValue(Attributes.FOLLOW_RANGE);
    }

    @Override
    public void start() {
        animationTick = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = paladin.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        paladin.getLookControl().setLookAt(target, 30.0F, 30.0F);

        //攻击动画播放中
        if (animationTick > 0) {
            int frame = ANIMATION_LENGTH - animationTick;
            if (frame == FIRST_HIT_TICK || frame == SECOND_HIT_TICK) {
                dealDamage();
            }

            animationTick--;

            if (animationTick == 0) {
                paladin.setAnimationState(DragonCultPaladinEntity.STATE_IDLE);
            }
            return;
        }

        //进入范围起手，否则继续接近
        if (paladin.distanceTo(target) <= TheLastSwordConfiguration.getDragonCultPaladinAttackRangeSafely()) {
            animationTick = ANIMATION_LENGTH;
            paladin.setAnimationState(DragonCultPaladinEntity.STATE_ATTACK);
            paladin.getNavigation().stop();
            return;
        }

        paladin.getNavigation().moveTo(target, 1.0);
    }

    @Override
    public void stop() {
        animationTick = 0;
        if (paladin.getAnimationState() == DragonCultPaladinEntity.STATE_ATTACK) {
            paladin.setAnimationState(DragonCultPaladinEntity.STATE_IDLE);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private void dealDamage() {
        LivingEntity target = paladin.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        if (paladin.distanceTo(target) > TheLastSwordConfiguration.getDragonCultPaladinAttackRangeSafely()) {
            return;
        }

        float damage = (float) (paladin.getAttributeValue(Attributes.ATTACK_DAMAGE)
            * TheLastSwordConfiguration.getDragonCultPaladinAttackDamageMultiplierSafely());
        if (damage <= 0) {
            return;
        }

        //两段间隔小于原版无敌帧，需清零才能都吃到
        target.invulnerableTime = 0;
        target.hurt(paladin.damageSources().mobAttack(paladin), damage);
    }
}
