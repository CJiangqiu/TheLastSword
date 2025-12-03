package net.the_last_sword.event;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.init.ModEffects;
import net.the_last_sword.util.EntityUtil;
import net.the_last_sword.util.TheLastSwordLogger;

//Effect 事件处理器 - 处理所有 MobEffect 相关的事件逻辑
@Mod.EventBusSubscriber(modid = TheLastSwordMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EffectEventHandler {

    //虚化效果 - 伤害免疫
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPhasingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;

        if (entity.hasEffect(ModEffects.PHASING.get())) {
            event.setCanceled(true);
        }
    }

    //虚化效果 - 死亡保护
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPhasingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;

        if (entity.hasEffect(ModEffects.PHASING.get())) {
            event.setCanceled(true);
            EntityUtil.theLastEndSetHealth(entity, entity.getMaxHealth());
        }
    }

    //虚空附魔 - 额外虚空伤害
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onVoidEnchantingHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        DamageSource damageSource = event.getSource();

        if (!(damageSource.getEntity() instanceof LivingEntity attacker)) {
            return;
        }

        if (!attacker.hasEffect(ModEffects.VOID_ENCHANTING.get())) {
            return;
        }

        //防止无限递归（虚空伤害触发虚空伤害）
        if (damageSource.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            return;
        }

        int amplifier = attacker.getEffect(ModEffects.VOID_ENCHANTING.get()).getAmplifier();
        double attackDamage = attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float voidDamageMultiplier = (float) ((amplifier + 1) * TheLastSwordConfiguration.getVoidEnchantmentDamagePercentageSafely());
        float voidDamage = (float) (attackDamage * voidDamageMultiplier);

        DamageSource voidDamageSource = createVoidDamageSource(attacker);

        if (!attacker.level().isClientSide && attacker.level() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().execute(() -> {
                if (victim.isAlive()) {
                    applyVoidDamage(victim, voidDamageSource, voidDamage, serverLevel);
                }
            });
        }
    }

    //创建虚空伤害源
    private static DamageSource createVoidDamageSource(LivingEntity attacker) {
        return new DamageSource(
            attacker.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(DamageTypes.FELL_OUT_OF_WORLD),
            attacker,
            attacker
        );
    }

    //应用虚空伤害并生成特效
    private static void applyVoidDamage(LivingEntity victim, DamageSource voidDamageSource,
                                        float damage, ServerLevel serverLevel) {
        try {
            victim.invulnerableTime = 0;
            boolean damaged = victim.hurt(voidDamageSource, damage);
            if (damaged) {
                spawnVoidEnchantingEffects(victim, serverLevel);
            }
        } catch (Exception e) {
            TheLastSwordLogger.error("Error applying void enchanting damage: {}", e.getMessage());
        }
    }

    //生成虚空附魔视觉特效
    private static void spawnVoidEnchantingEffects(LivingEntity victim, ServerLevel serverLevel) {
        double centerX = victim.getX();
        double centerY = victim.getY() + victim.getBbHeight() * 0.5;
        double centerZ = victim.getZ();

        int particleCount = 16;
        double maxRadius = 1.5;

        for (int i = 0; i < particleCount; i++) {
            double theta = Math.random() * Math.PI * 2;
            double phi = Math.random() * Math.PI;
            double radius = 0.5 + Math.random() * maxRadius;

            double x = centerX + radius * Math.sin(phi) * Math.cos(theta);
            double y = centerY + radius * Math.cos(phi);
            double z = centerZ + radius * Math.sin(phi) * Math.sin(theta);

            double velX = (x - centerX) * 0.15;
            double velY = (y - centerY) * 0.15;
            double velZ = (z - centerZ) * 0.15;

            serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH,
                x, y, z,
                1, velX, velY, velZ, 0.0);
        }

        serverLevel.sendParticles(ParticleTypes.PORTAL,
            centerX, centerY, centerZ,
            3, 0.1, 0.1, 0.1, 0.02);
    }
}
