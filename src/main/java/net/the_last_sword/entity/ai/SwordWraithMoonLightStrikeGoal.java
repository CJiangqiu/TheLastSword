package net.the_last_sword.entity.ai;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.TheLastEndSwordWraithEntity;
import net.the_last_sword.util.EntityUtil;

import java.util.EnumSet;
import java.util.List;

//月光打击技能Goal
public class SwordWraithMoonLightStrikeGoal extends Goal {
    private final TheLastEndSwordWraithEntity wraith;
    private int animationTick;
    private long lastUseTime;

    private static final int ANIMATION_LENGTH = 100;
    private static final int STRIKE_TICK = 5;
    private static final double LAUNCH_STRENGTH = 1.2;
    private static final int COOLDOWN = 120;

    public SwordWraithMoonLightStrikeGoal(TheLastEndSwordWraithEntity wraith) {
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

        if (wraith.distanceTo(target) > 6.0) {
            return false;
        }

        return wraith.level().getGameTime() - lastUseTime >= COOLDOWN;
    }

    @Override
    public void start() {
        animationTick = ANIMATION_LENGTH;
        wraith.setAnimationState(TheLastEndSwordWraithEntity.STATE_MOON_LIGHT_STRIKE);
        wraith.getNavigation().stop();
        lastUseTime = wraith.level().getGameTime();
    }

    @Override
    public void tick() {
        animationTick--;

        if (animationTick == STRIKE_TICK) {
            executeMoonLightStrike();
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

    //执行月光打击
    private void executeMoonLightStrike() {
        double strikeRange = TheLastSwordConfiguration.getSkillMoonLightStrikeRangeSafely();
        float damageMultiplier = (float) TheLastSwordConfiguration.getSkillMoonLightStrikeDamageMultiplierSafely();
        float damage = (float) wraith.getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier;

        List<LivingEntity> targets = EntityUtil.getTargetsInSphere(wraith, strikeRange);
        for (LivingEntity target : targets) {
            target.hurt(wraith.damageSources().mobAttack(wraith), damage);
            target.setDeltaMovement(target.getDeltaMovement().add(0, LAUNCH_STRENGTH, 0));
            target.playSound(SoundEvents.PLAYER_ATTACK_CRIT, 1.0F, 1.0F);
            TheLastEndSwordWraithEntity.addEndMark(target);
        }
    }
}
