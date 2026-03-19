package net.the_last_sword.entity.ai;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.TheLastEndSwordWraithEntity;
import net.the_last_sword.init.ModEffects;

import java.util.EnumSet;

//附魔技能Goal
public class SwordWraithEnchantGoal extends Goal {
    private final TheLastEndSwordWraithEntity wraith;
    private int animationTick;

    private static final int ANIMATION_LENGTH = 45;
    private static final int ENCHANT_TICK = 20;

    public SwordWraithEnchantGoal(TheLastEndSwordWraithEntity wraith) {
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

        //没有虚空附魔buff时触发
        return !wraith.hasEffect(ModEffects.VOID_ENCHANTING.get());
    }

    @Override
    public void start() {
        animationTick = ANIMATION_LENGTH;
        wraith.setAnimationState(TheLastEndSwordWraithEntity.STATE_ENCHANT);
        wraith.getNavigation().stop();
    }

    @Override
    public void tick() {
        animationTick--;

        if (animationTick == ENCHANT_TICK) {
            applyVoidEnchantment();
            wraith.level().playSound(null, wraith.blockPosition(),
                SoundEvents.ENCHANTMENT_TABLE_USE, wraith.getSoundSource(), 1.0F, 1.0F);
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

    //应用虚空附魔效果
    private void applyVoidEnchantment() {
        if (wraith.level().isClientSide) {
            return;
        }

        int enchantDuration = TheLastSwordConfiguration.getSkillEnchantDurationSafely();
        int enchantmentAmplifier = Math.max(0, wraith.getTheLastEndLevel() - 1);

        MobEffectInstance voidEnchantment = new MobEffectInstance(
            ModEffects.VOID_ENCHANTING.get(),
            enchantDuration,
            enchantmentAmplifier,
            false,
            TheLastSwordConfiguration.getVoidEnchantmentParticleEffectsSafely(),
            true
        );

        wraith.addEffect(voidEnchantment);
    }
}
