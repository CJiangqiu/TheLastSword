package net.the_last_sword.entity.ai;

import java.util.EnumSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.ThePastShadowOfTheQueenEntity;
import net.the_last_sword.init.ModEffects;
import net.the_last_sword.util.EntityUtil;

public class ThePastShadowOfTheQueenEnchantGoal extends Goal {
    private static final int DURATION = 128;
    private static final int SHIELD_TICK = 37;
    private static final int ENCHANT_TICK = 105;
    private static final int PARTICLE_POINTS = 12;
    private static final double PARTICLE_RADIUS = 3.0;
    private final ThePastShadowOfTheQueenEntity queen;
    private int tick;
    private long cooldownEnd;

    public ThePastShadowOfTheQueenEnchantGoal(ThePastShadowOfTheQueenEntity queen) {
        this.queen = queen;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = queen.getTarget();
        return queen.canAct() && queen.getAnimationState() == ThePastShadowOfTheQueenEntity.STATE_IDLE
                && queen.level().getGameTime() >= cooldownEnd
                && target != null && target.isAlive() && EntityUtil.canAttack(queen, target)
                && !queen.hasEffect(ModEffects.VOID_ENCHANTING.get());
    }

    @Override
    public void start() {
        tick = 0;
        cooldownEnd = queen.level().getGameTime() + TheLastSwordConfiguration.getQueenEnchantCooldownSafely();
        queen.setLightningSpearVisible(false);
        queen.setAnimationState(ThePastShadowOfTheQueenEntity.STATE_ENCHANT);
        queen.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (!canContinueToUse()) {
            stop();
            return;
        }

        queen.getNavigation().stop();
        Vec3 movement = queen.getDeltaMovement();
        queen.setDeltaMovement(0, movement.y, 0);
        ++tick;

        if (!queen.level().isClientSide && tick == SHIELD_TICK) {
            EntityUtil.grantTempShield(queen, 2.0);
        }
        if (queen.level() instanceof ServerLevel serverLevel && tick >= SHIELD_TICK && tick <= ENCHANT_TICK) {
            spawnParticleRing(serverLevel);
        }
        if (!queen.level().isClientSide && tick == ENCHANT_TICK) {
            applyVoidEnchantment();
        }
        if (tick >= DURATION) {
            stop();
        }
    }

    private void spawnParticleRing(ServerLevel serverLevel) {
        double phase = tick * Math.PI / PARTICLE_POINTS;
        for (int i = 0; i < PARTICLE_POINTS; i++) {
            double angle = Math.PI * 2.0 * i / PARTICLE_POINTS + phase;
            double x = queen.getX() + Math.cos(angle) * PARTICLE_RADIUS;
            double z = queen.getZ() + Math.sin(angle) * PARTICLE_RADIUS;
            ParticleOptions particle = i % 2 == 0 ? ParticleTypes.ENCHANT : ParticleTypes.DRAGON_BREATH;
            serverLevel.sendParticles(particle, x, queen.getY() + 0.1, z, 1, 0, 0, 0, 0);
        }
    }

    private void applyVoidEnchantment() {
        int amplifier = Math.max(0, queen.getTheLastEndLevel() - 1);
        queen.addEffect(new MobEffectInstance(
                ModEffects.VOID_ENCHANTING.get(),
                600,
                amplifier,
                false,
                TheLastSwordConfiguration.getVoidEnchantmentParticleEffectsSafely(),
                true));
        queen.level().playSound(null, queen.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE,
                queen.getSoundSource(), 1.0F, 1.0F);
    }

    @Override
    public boolean canContinueToUse() {
        return tick < DURATION && queen.canAct()
                && queen.getAnimationState() == ThePastShadowOfTheQueenEntity.STATE_ENCHANT;
    }

    @Override
    public void stop() {
        tick = DURATION;
        if (queen.getAnimationState() == ThePastShadowOfTheQueenEntity.STATE_ENCHANT) {
            queen.setAnimationState(ThePastShadowOfTheQueenEntity.STATE_IDLE);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
