package net.the_last_sword.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.DragonCultPriestEntity;
import net.the_last_sword.entity.util.PriestGuardEffect;

import java.util.EnumSet;

// 祭司庇佑Goal，guard动画结束时净化自身与周围友方的负面效果并各加1点肃正防御护盾
public class DragonCultPriestGuardGoal extends Goal {

    //guard 动画全长 3 秒，效果在结束时结算
    private static final int ANIMATION_LENGTH = 60;

    private final DragonCultPriestEntity priest;
    private int animationTick;
    private long cooldownEnd;

    public DragonCultPriestGuardGoal(DragonCultPriestEntity priest) {
        this.priest = priest;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!priest.canAct()) {
            return false;
        }
        if (priest.getAnimationState() != DragonCultPriestEntity.STATE_IDLE) {
            return false;
        }
        if (priest.level().getGameTime() < cooldownEnd) {
            return false;
        }

        LivingEntity target = priest.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }

        //有人需要净化，或自身已被压制到低血量
        return PriestGuardEffect.hasPurifiableAlly(priest)
            || getHpRatio() < TheLastSwordConfiguration.getDragonCultPriestGuardLowHpRatioSafely();
    }

    @Override
    public boolean canContinueToUse() {
        return animationTick > 0;
    }

    @Override
    public void start() {
        animationTick = ANIMATION_LENGTH;
        priest.setAnimationState(DragonCultPriestEntity.STATE_GUARD);
        priest.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (animationTick <= 0) {
            return;
        }

        animationTick--;

        if (animationTick == 0) {
            priest.onSkillCast(DragonCultPriestEntity.PriestSkill.GUARD);
            PriestGuardEffect.apply(priest);
            priest.setAnimationState(DragonCultPriestEntity.STATE_IDLE);
            cooldownEnd = priest.level().getGameTime()
                + priest.scaleSkillCooldown(TheLastSwordConfiguration.getDragonCultPriestGuardCooldownSafely());
        }
    }

    @Override
    public void stop() {
        animationTick = 0;
        if (priest.getAnimationState() == DragonCultPriestEntity.STATE_GUARD) {
            priest.setAnimationState(DragonCultPriestEntity.STATE_IDLE);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private float getHpRatio() {
        float max = priest.getWorldAnchorMax();
        if (max <= 0) {
            return 1.0f;
        }
        return priest.getWorldAnchor() / max;
    }
}
