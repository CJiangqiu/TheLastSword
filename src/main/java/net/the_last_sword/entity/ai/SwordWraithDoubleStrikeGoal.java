package net.the_last_sword.entity.ai;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.TheLastEndSwordWraithEntity;
import net.the_last_sword.util.EntityUtil;
import net.the_last_sword.util.ParticleUtil;

import java.util.EnumSet;
import java.util.List;

//双重打击技能Goal（基础攻击，无冷却）
public class SwordWraithDoubleStrikeGoal extends Goal {
    private final TheLastEndSwordWraithEntity wraith;
    private int animationTick;

    private static final int ANIMATION_LENGTH = 40;
    private static final int FIRST_STRIKE_TICK = 30;
    private static final int SECOND_STRIKE_TICK = 20;

    public SwordWraithDoubleStrikeGoal(TheLastEndSwordWraithEntity wraith) {
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

        //距离≤4时触发（基础攻击）
        return wraith.distanceTo(target) <= 4.0;
    }

    @Override
    public void start() {
        animationTick = ANIMATION_LENGTH;
        wraith.setAnimationState(TheLastEndSwordWraithEntity.STATE_DOUBLE_STRIKE);
        wraith.getNavigation().stop();
    }

    @Override
    public void tick() {
        animationTick--;

        if (animationTick == FIRST_STRIKE_TICK) {
            executeFirstStrike();
        }

        if (animationTick == SECOND_STRIKE_TICK) {
            executeSecondStrike();
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

    //第一击：物理伤害
    private void executeFirstStrike() {
        double attackRange = TheLastSwordConfiguration.getSkillDoubleStrikeRangeSafely();
        float damageMultiplier = (float) TheLastSwordConfiguration.getSkillDoubleStrikeDamageMultiplierSafely();

        wraith.level().playSound(null, wraith.getX(), wraith.getY(), wraith.getZ(),
            SoundEvents.PLAYER_ATTACK_SWEEP, wraith.getSoundSource(), 1.0F, 1.0F);
        ParticleUtil.spawnSweepParticles(wraith, true, attackRange);

        List<LivingEntity> targets = EntityUtil.getTargetsInHemisphere(wraith, attackRange);
        float damage = (float) wraith.getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier;
        for (LivingEntity target : targets) {
            target.invulnerableTime = 0;
            target.hurt(wraith.damageSources().mobAttack(wraith), damage);
            TheLastEndSwordWraithEntity.addEndMark(target);
        }
    }

    //第二击：魔法伤害
    private void executeSecondStrike() {
        double attackRange = TheLastSwordConfiguration.getSkillDoubleStrikeRangeSafely();
        float damageMultiplier = (float) TheLastSwordConfiguration.getSkillDoubleStrikeDamageMultiplierSafely();

        wraith.level().playSound(null, wraith.getX(), wraith.getY(), wraith.getZ(),
            SoundEvents.PLAYER_ATTACK_SWEEP, wraith.getSoundSource(), 1.0F, 1.0F);
        ParticleUtil.spawnSweepParticles(wraith, false, attackRange);

        List<LivingEntity> targets = EntityUtil.getTargetsInHemisphere(wraith, attackRange);
        float damage = (float) wraith.getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier;
        for (LivingEntity target : targets) {
            target.invulnerableTime = 0;
            DamageSource magicDamage = new DamageSource(
                wraith.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                    .getHolderOrThrow(DamageTypes.MAGIC),
                wraith, wraith
            );
            target.hurt(magicDamage, damage);
            TheLastEndSwordWraithEntity.addEndMark(target);
        }
    }
}
