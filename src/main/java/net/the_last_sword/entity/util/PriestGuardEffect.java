package net.the_last_sword.entity.util;

import net.eca.api.EcaAPI;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.util.EntityUtil;

import java.util.ArrayList;
import java.util.List;

public final class PriestGuardEffect {
    private static final int BUFF_PARTICLE_COUNT = 12;

    private PriestGuardEffect() {
    }

    public static void apply(LivingEntity source) {
        apply(source,
            TheLastSwordConfiguration.getDragonCultPriestGuardRadiusSafely(),
            TheLastSwordConfiguration.getDragonCultPriestGuardShieldGainSafely());
    }

    public static void apply(LivingEntity source, double radius, double shieldGain) {
        if (source == null || source.level().isClientSide) {
            return;
        }

        for (LivingEntity ally : collectAllies(source, radius)) {
            purify(ally);
            EntityUtil.grantTempShield(ally, shieldGain);
            spawnBuffParticles(ally);
        }

        source.level().playSound(null, source.blockPosition(),
            SoundEvents.BEACON_ACTIVATE, source.getSoundSource(), 1.0F, 1.5F);
    }

    public static boolean hasPurifiableAlly(LivingEntity source) {
        for (LivingEntity ally : collectAllies(source,
            TheLastSwordConfiguration.getDragonCultPriestGuardRadiusSafely())) {
            for (MobEffectInstance instance : ally.getActiveEffects()) {
                if (instance.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<LivingEntity> collectAllies(LivingEntity source, double radius) {
        List<LivingEntity> allies = new ArrayList<>();
        if (source == null) {
            return allies;
        }

        allies.add(source);
        AABB area = source.getBoundingBox().inflate(radius);
        for (LivingEntity entity : source.level().getEntitiesOfClass(LivingEntity.class, area)) {
            if (entity == source || !entity.isAlive()) {
                continue;
            }
            if (source.distanceToSqr(entity) <= radius * radius && EcaAPI.isFriendly(source, entity)) {
                allies.add(entity);
            }
        }
        return allies;
    }

    private static void spawnBuffParticles(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
            entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(),
            BUFF_PARTICLE_COUNT,
            entity.getBbWidth() * 0.5, entity.getBbHeight() * 0.4, entity.getBbWidth() * 0.5,
            0.0);
    }

    private static void purify(LivingEntity entity) {
        List<MobEffect> harmful = new ArrayList<>();
        for (MobEffectInstance instance : entity.getActiveEffects()) {
            if (instance.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                harmful.add(instance.getEffect());
            }
        }
        for (MobEffect effect : harmful) {
            entity.removeEffect(effect);
        }
    }
}
