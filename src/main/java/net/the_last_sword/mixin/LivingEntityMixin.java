package net.the_last_sword.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.the_last_sword.attack.AbsoluteDestructionDamageSource;
import net.the_last_sword.attack.AttackManager;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.defence.DefenceManager;
import net.the_last_sword.init.ModAttributes;
import net.the_last_sword.init.ModEffects;
import net.the_last_sword.util.EntityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@Mixin(value = LivingEntity.class, priority = 1024)
public class LivingEntityMixin {

    //直接获取防御等级
    @Unique
    private static int getDefenceLevel(LivingEntity entity) {
        return DefenceManager.getDefenceLevel(entity);
    }

    //检查是否是绝对毁灭伤害源
    @Unique
    private boolean the_last_sword$isAbsoluteDestructionDamage(DamageSource source) {
        return source instanceof AbsoluteDestructionDamageSource;
    }

    //虚化效果防御同步
    @Unique
    private void the_last_sword$syncPhasingDefence(LivingEntity entity) {
        boolean hasPhasing = entity.hasEffect(ModEffects.PHASING.get());
        int level = getDefenceLevel(entity);

        if (hasPhasing && level < 3) {
            DefenceManager.register(entity, 3);
        }
    }

    //护盾自动回复系统（两层分支优化）
    @Unique
    private void the_last_sword$handleShieldRegeneration(LivingEntity entity) {
        AttributeInstance maxAttr = entity.getAttribute(ModAttributes.MAX_JUSTIFIED_DEFENCE.get());
        if (maxAttr == null) return;
        double max = maxAttr.getValue();

        AttributeInstance curAttr = entity.getAttribute(ModAttributes.JUSTIFIED_DEFENCE.get());
        if (curAttr == null) return;
        double cur = curAttr.getValue();

        //第一层：max <= 0 的情况（2个分支）
        if (max <= 0) {
            //分支1: cur == 0 → 直接 return（99%普通实体快速退出）
            if (cur <= 0) {
                return;
            }
            //分支2: cur > 0 → 限制为0
            curAttr.setBaseValue(0.0);
            return;
        }

        //第二层：max > 0 的情况（3个分支）
        //分支1: cur == max → 直接 return（护盾已满）
        if (Math.abs(cur - max) < 0) {
            return;
        }

        //分支2: cur > max → 校准（限制不超过最大值）
        if (cur > max) {
            curAttr.setBaseValue(max);
            return;
        }

        //分支3: cur < max → 执行回复逻辑
        int interval = TheLastSwordConfiguration.getJustifiedDefenceRecoveryTickSafely();
        if (interval > 0 && entity.tickCount % interval == 0) {
            curAttr.setBaseValue(Math.min(cur + 1, max));
        }
    }

    //处理禁疗系统
    @Unique
    private void the_last_sword$handleHealNegationSystem(LivingEntity entity) {
        if (AttackManager.isHealNegated(entity)) {
            float expectedHealth = AttackManager.getLockedHealth(entity);
            EntityUtil.theLastEndSetHealth(entity, expectedHealth);
        }
    }

    //防御等级≥2的强化免疫系统
    @Unique
    private void the_last_sword$handleDefenceLevel2Protection(LivingEntity entity) {
        if (entity.isOnFire()) {
            entity.clearFire();
        }

        if (entity.isFullyFrozen() || entity.getTicksFrozen() > 0) {
            entity.setTicksFrozen(0);
        }

        Collection<MobEffectInstance> activeEffects = entity.getActiveEffects();
        if (!activeEffects.isEmpty()) {
            ArrayList<MobEffect> effectsToRemove = new ArrayList<>();

            for (MobEffectInstance effectInstance : activeEffects) {
                MobEffect effect = effectInstance.getEffect();
                if (!effect.isBeneficial() && effect != MobEffects.ABSORPTION) {
                    effectsToRemove.add(effect);
                }
            }

            for (MobEffect effect : effectsToRemove) {
                entity.removeEffect(effect);
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onLivingEntityTick(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity.level().isClientSide || entity.tickCount <= 0) {
            return;
        }

        the_last_sword$syncPhasingDefence(entity);

        the_last_sword$handleShieldRegeneration(entity);

        the_last_sword$handleHealNegationSystem(entity);

        int defenceLevel = getDefenceLevel(entity);
        if (defenceLevel >= 1) {
            DefenceManager.pushToEntity(entity);
            EntityUtil.theLastEndRevive(entity);
            if (defenceLevel >= 2) {
                the_last_sword$handleDefenceLevel2Protection(entity);
            }
        }
    }

    @Inject(method = "tickDeath", at = @At("HEAD"), cancellable = true)
    private void onLivingEntityTickDeath(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (getDefenceLevel(entity) >= 1) {
            ci.cancel();
        }
    }

    @Inject(method = "actuallyHurt", at = @At("HEAD"), cancellable = true)
    private void onLivingEntityActuallyHurt(DamageSource damageSource, float damageAmount, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity.level().isClientSide) {
            return;
        }

        //统一使用 canAttack 判断盟友关系（包含剑灵的所有盟友关系）
        if (damageSource.getEntity() instanceof LivingEntity attacker) {
            if (!EntityUtil.canAttack(attacker, entity)) {
                ci.cancel();
                return;
            }
        }

        if (AttackManager.isHealNegated(entity) && !the_last_sword$isAbsoluteDestructionDamage(damageSource)) {
            float currentHealNegationHealth = AttackManager.getLockedHealth(entity);
            float expectedHealthAfterDamage = currentHealNegationHealth - damageAmount;
            AttackManager.updateHealNegationHealth(entity, expectedHealthAfterDamage);
        }

        int defenceLevel = getDefenceLevel(entity);
        if (defenceLevel < 1) {
            return;
        }

        float maxHealth = (float) entity.getAttributeValue(Attributes.MAX_HEALTH);
        float damageReductionRatio = (float) TheLastSwordConfiguration.getDefenceCustomHealthDamageReductionSafely();
        float maxDamagePerHit = (float) TheLastSwordConfiguration.getDefenceMaxDamagePerHitSafely();
        float damageLimit = Math.min(maxHealth * damageReductionRatio, maxDamagePerHit);

        float realDamage;

        if (defenceLevel >= 3) {
            realDamage = 0.0f;
        } else if (defenceLevel == 2) {
            if (damageAmount > damageLimit) {
                realDamage = 0.0f;
            } else {
                realDamage = damageAmount;
            }
        } else {
            realDamage = Math.min(damageAmount, damageLimit);
        }

        if (realDamage > 0.0f) {
            float currentHealth = DefenceManager.getHealth(entity);
            float newHealth = currentHealth - realDamage;
            DefenceManager.modifyHealth(entity, newHealth);
        }

        ci.cancel();
    }

    @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
    private void onLivingEntityHeal(float healAmount, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity.level().isClientSide) {
            return;
        }

        if (AttackManager.isHealNegated(entity)) {
            ci.cancel();
            return;
        }

        if (getDefenceLevel(entity) >= 1) {
            if (healAmount > 0) {
                float currentHealth = DefenceManager.getHealth(entity);
                float maxHealth = (float) entity.getAttributeValue(Attributes.MAX_HEALTH);
                float newHealth = Math.min(currentHealth + healAmount, maxHealth);
                DefenceManager.modifyHealth(entity, newHealth);
            }
            ci.cancel();
        }
    }

    @Inject(method = "setHealth", at = @At("HEAD"), cancellable = true)
    private void onLivingEntitySetHealth(float health, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity.level().isClientSide) {
            return;
        }

        if (AttackManager.isHealNegated(entity)) {
            float currentHealNegationHealth = AttackManager.getLockedHealth(entity);
            if (health > currentHealNegationHealth) {
                ci.cancel();
                return;
            }
            if (health < currentHealNegationHealth) {
                AttackManager.updateHealNegationHealth(entity, health);
            }
            return;
        }

        if (getDefenceLevel(entity) >= 1) {
            float currentHealth = DefenceManager.getHealth(entity);
            if (health > currentHealth) {
                float maxHealth = (float) entity.getAttributeValue(Attributes.MAX_HEALTH);
                float newHealth = Math.min(health, maxHealth);
                DefenceManager.modifyHealth(entity, newHealth);
            }
            ci.cancel();
        }
    }

    @Inject(method = "getHealth", at = @At("RETURN"), cancellable = true)
    private void onLivingEntityGetHealth(CallbackInfoReturnable<Float> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity.level().isClientSide) {
            return;
        }

        if (AttackManager.isHealNegated(entity)) {
            float healNegationHealth = AttackManager.getLockedHealth(entity);
            cir.setReturnValue(healNegationHealth);
            return;
        }

        if (getDefenceLevel(entity) >= 1) {
            float trueHealth = DefenceManager.getHealth(entity);
            cir.setReturnValue(trueHealth);
        }
    }

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void onLivingEntityDie(DamageSource damageSource, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (getDefenceLevel(entity) >= 1) {
            ci.cancel();
        }
    }

    @Inject(method = "die", at = @At("RETURN"))
    private void onLivingEntityDieReturn(DamageSource damageSource, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        AttackManager.clearHealNegation(entity);
        AttackManager.clearAll(entity);
    }
}
