package net.the_last_sword.entity.ai;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.TheLastEndSwordWraithEntity;
import net.the_last_sword.util.EntityUtil;

import java.util.EnumSet;
import java.util.List;

//月华一击技能Goal
public class SwordWraithMoonLightStrikeGoal extends Goal {
    private final TheLastEndSwordWraithEntity wraith;
    private int animationTick;
    private long lastUseTime;
    private double startY;
    private boolean previousNoGravity;
    private boolean airborne;

    private static final int ANIMATION_LENGTH = 100;
    private static final int ASCENT_START_TICK = 25;
    private static final int ASCENT_END_TICK = 30;
    private static final int DESCENT_START_TICK = 90;
    private static final int STRIKE_TICK = 95;
    private static final double JUMP_HEIGHT = 8.0D;
    private static final double VERTICAL_STEP = JUMP_HEIGHT / (ASCENT_END_TICK - ASCENT_START_TICK);
    private static final double LAUNCH_STRENGTH = 1.2;

    public SwordWraithMoonLightStrikeGoal(TheLastEndSwordWraithEntity wraith) {
        this.wraith = wraith;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!wraith.canAct()) {
            return false;
        }
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

        return wraith.level().getGameTime() - lastUseTime >= TheLastSwordConfiguration.getSkillMoonLightStrikeCooldownSafely();
    }

    @Override
    public void start() {
        animationTick = ANIMATION_LENGTH;
        wraith.setAnimationState(TheLastEndSwordWraithEntity.STATE_MOON_LIGHT_STRIKE);
        wraith.getNavigation().stop();
        lastUseTime = wraith.level().getGameTime();
        startY = wraith.getY();
        previousNoGravity = wraith.isNoGravity();
        airborne = false;
    }

    @Override
    public void tick() {
        if (animationTick <= 0) {
            return;
        }

        int relativeFrame = ANIMATION_LENGTH - animationTick;
        if (relativeFrame >= ASCENT_START_TICK && relativeFrame < ASCENT_END_TICK) {
            ascend();
        } else if (relativeFrame >= ASCENT_END_TICK && relativeFrame < DESCENT_START_TICK) {
            hover();
        } else if (relativeFrame >= DESCENT_START_TICK && relativeFrame < STRIKE_TICK) {
            descend();
        }

        if (relativeFrame == STRIKE_TICK) {
            finishAirMovement();
            executeMoonLightStrike();
        }

        animationTick--;

        LivingEntity target = wraith.getTarget();
        if (target != null && target.isAlive()) {
            wraith.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        if (animationTick == 0) {
            finishAirMovement();
            wraith.setAnimationState(TheLastEndSwordWraithEntity.STATE_IDLE);
        }
    }

    @Override
    public boolean canContinueToUse() {
        return animationTick > 0;
    }

    @Override
    public void stop() {
        animationTick = 0;
        finishAirMovement();
        if (wraith.getAnimationState() == TheLastEndSwordWraithEntity.STATE_MOON_LIGHT_STRIKE) {
            wraith.setAnimationState(TheLastEndSwordWraithEntity.STATE_IDLE);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    //手动控制垂直阶段，避免重力使动画中的滞空位置持续下坠
    private void ascend() {
        beginAirMovement();
        double remainingHeight = startY + JUMP_HEIGHT - wraith.getY();
        if (remainingHeight > 0.0D) {
            wraith.move(MoverType.SELF, new Vec3(0.0D, Math.min(VERTICAL_STEP, remainingHeight), 0.0D));
        }
        wraith.setDeltaMovement(Vec3.ZERO);
        wraith.fallDistance = 0.0F;
    }

    private void hover() {
        beginAirMovement();
        wraith.setDeltaMovement(Vec3.ZERO);
        wraith.fallDistance = 0.0F;
    }

    private void descend() {
        beginAirMovement();
        double remainingHeight = wraith.getY() - startY;
        if (remainingHeight > 0.0D) {
            wraith.move(MoverType.SELF, new Vec3(0.0D, -Math.min(VERTICAL_STEP, remainingHeight), 0.0D));
        }
        wraith.setDeltaMovement(Vec3.ZERO);
        wraith.fallDistance = 0.0F;
    }

    private void beginAirMovement() {
        if (!airborne) {
            airborne = true;
            wraith.setNoGravity(true);
        }
    }

    private void finishAirMovement() {
        if (!airborne) {
            return;
        }
        airborne = false;
        wraith.setNoGravity(previousNoGravity);
        wraith.setDeltaMovement(Vec3.ZERO);
        wraith.fallDistance = 0.0F;
    }

    //执行月华一击
    private void executeMoonLightStrike() {
        double strikeRange = TheLastSwordConfiguration.getSkillMoonLightStrikeRangeSafely();
        float damageMultiplier = (float) TheLastSwordConfiguration.getSkillMoonLightStrikeDamageMultiplierSafely();
        float damage = (float) wraith.getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier;

        List<LivingEntity> targets = EntityUtil.getTargetsInSphere(wraith, strikeRange);
        for (LivingEntity target : targets) {
            boolean damaged = target.hurt(wraith.damageSources().mobAttack(wraith),
                    wraith.getDamageWithEndMark(target, damage));
            target.setDeltaMovement(target.getDeltaMovement().add(0, LAUNCH_STRENGTH, 0));
            target.playSound(SoundEvents.PLAYER_ATTACK_CRIT, 1.0F, 1.0F);
            if (damaged) {
                wraith.addEndMark();
            }
        }
    }
}
