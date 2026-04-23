package net.the_last_sword.entity.ai;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.goal.Goal;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.LostWraithEntity;
import net.the_last_sword.init.ModEffects;

import java.util.EnumSet;

// 迷失战魂附魔技能Goal
public class LostWraithEnchantGoal extends Goal {
    private static final int ANIMATION_LENGTH = 50;
    private static final int EFFECT_TICK = 49;

    private final LostWraithEntity wraith;
    private int animationTick;

    public LostWraithEnchantGoal(LostWraithEntity wraith) {
        this.wraith = wraith;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!wraith.canAct()) {
            return false;
        }
        if (wraith.getAnimationState() != LostWraithEntity.STATE_IDLE) {
            return false;
        }
        if (wraith.getTarget() == null) {
            return false;
        }
        if (wraith.isForceEndStrike()) {
            return false;
        }
        return !wraith.hasEffect(ModEffects.VOID_ENCHANTING.get());
    }

    @Override
    public boolean canContinueToUse() {
        return animationTick > 0;
    }

    @Override
    public void start() {
        animationTick = ANIMATION_LENGTH;
        wraith.setAnimationState(LostWraithEntity.STATE_ENCHANT);
        wraith.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (animationTick <= 0) {
            return;
        }

        int relativeFrame = ANIMATION_LENGTH - animationTick;

        if (relativeFrame == EFFECT_TICK) {
            applyEnchantment();
        }

        animationTick--;

        if (animationTick == 0) {
            wraith.setAnimationState(LostWraithEntity.STATE_IDLE);
        }
    }

    @Override
    public void stop() {
        animationTick = 0;
        if (wraith.getAnimationState() == LostWraithEntity.STATE_ENCHANT) {
            wraith.setAnimationState(LostWraithEntity.STATE_IDLE);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private void applyEnchantment() {
        MobEffectInstance voidEnchant = new MobEffectInstance(
            ModEffects.VOID_ENCHANTING.get(),
            TheLastSwordConfiguration.getLostWraithEnchantDurationSafely(),
            TheLastSwordConfiguration.getLostWraithEnchantAmplifierSafely(),
            false, false
        );
        wraith.addEffect(voidEnchant);

        wraith.level().playSound(null,
            wraith.getX(), wraith.getY(), wraith.getZ(),
            SoundEvents.ENCHANTMENT_TABLE_USE,
            wraith.getSoundSource(), 1.0F, 1.0F);

        if (wraith.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 20; i++) {
                serverLevel.sendParticles(ParticleTypes.ENCHANT,
                    wraith.getX() + (wraith.getRandom().nextDouble() - 0.5) * 2,
                    wraith.getY() + wraith.getRandom().nextDouble() * 2,
                    wraith.getZ() + (wraith.getRandom().nextDouble() - 0.5) * 2,
                    1, 0, 0.1, 0, 1.0);
            }
        }
    }
}
