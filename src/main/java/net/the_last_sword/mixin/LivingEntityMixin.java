package net.the_last_sword.mixin;

import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.the_last_sword.damagesource.AbsoluteDestructionDamageSource;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.init.ModAttributes;
import net.the_last_sword.summon.WraithSummonManager;
import net.the_last_sword.util.EntityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;

@Mixin(value = LivingEntity.class, priority = 1024)
public class LivingEntityMixin {

    //静态初始化注入：在原版 defineId 调用后紧接着定义我们的 EntityDataAccessor
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void the_last_sword$onClinit(CallbackInfo ci) {
        EntityUtil.WORLD_ANCHOR = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.STRING);//江秋特制神秘文本血
        EntityUtil.WORLD_ANCHOR_MAX = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.STRING);//最大生命值
        EntityUtil.HEAL_BAN_TIME = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.INT);
        EntityUtil.IS_PROTECTED = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BOOLEAN);
    }

    //注册实体数据（在每个实例的 defineSynchedData 中调用）
    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void the_last_sword$onDefineSynchedData(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        entity.getEntityData().define(EntityUtil.WORLD_ANCHOR, "-1024.0");//负数加密
        entity.getEntityData().define(EntityUtil.WORLD_ANCHOR_MAX, "-2048.0");
        entity.getEntityData().define(EntityUtil.HEAL_BAN_TIME, 0);
        entity.getEntityData().define(EntityUtil.IS_PROTECTED, false);
    }

    //检查是否是绝对毁灭伤害源
    @Unique
    private boolean the_last_sword$isAbsoluteDestructionDamage(DamageSource source) {
        return source instanceof AbsoluteDestructionDamageSource;
    }

    //肃正防御护盾消费: 存在护盾则扣除指定代价, 返回是否成功消费
    @Unique
    private boolean the_last_sword$consumeShield(LivingEntity entity, int cost) {
        AttributeInstance shieldAttr = entity.getAttribute(ModAttributes.JUSTIFIED_DEFENCE.get());
        if (shieldAttr == null || shieldAttr.getValue() <= 0) return false;
        shieldAttr.setBaseValue(Math.max(0.0, shieldAttr.getValue() - cost));
        return true;
    }


    //肃正防御护盾 > 0 时注册保护，= 0 时清除（不影响剑的保护）
    @Unique
    private void the_last_sword$handleJustifiedDefenceProtection(LivingEntity entity) {
        AttributeInstance curAttr = entity.getAttribute(ModAttributes.JUSTIFIED_DEFENCE.get());
        if (curAttr == null) return;

        if (curAttr.getValue() > 0) {
            if (!EntityUtil.hasProtection(entity)) {
                float realHealth = entity.getHealth(); // hasProtection=false 时取原版血量
                EntityUtil.setProtection(entity, true);
                EntityUtil.setWorldAnchor(entity, realHealth);
                EntityUtil.setWorldAnchorMax(entity, entity.getMaxHealth());
                entity.getPersistentData().putBoolean("JustifiedDefenceProtection", true);
            }
        } else if (entity.getPersistentData().getBoolean("JustifiedDefenceProtection")) {
            entity.getPersistentData().remove("JustifiedDefenceProtection");
            if (!entity.getPersistentData().getBoolean("TheLastSwordDefence")) {
                float anchor = EntityUtil.getWorldAnchor(entity);
                EntityUtil.clearDefence(entity);
                if (anchor >= 0 && anchor < entity.getHealth()) {
                    entity.setHealth(anchor);
                }
            }
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

    //防御系统的强化免疫系统
    @Unique
    private void the_last_sword$handleDefenceProtection(LivingEntity entity) {
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

        the_last_sword$handleShieldRegeneration(entity);

        //肃正防御存在时自动注册保护
        the_last_sword$handleJustifiedDefenceProtection(entity);

        //每 tick 更新禁疗计时器
        EntityUtil.tickHealBanTime(entity);

        //防御系统tick逻辑
        if (EntityUtil.hasProtection(entity)) {
            float realHealth = EntityUtil.getWorldAnchor(entity);
            if (realHealth <= 0 ) {
                EntityUtil.setProtection(entity, false);
            }
        }
    }

    //tick 结束时（ECA 禁疗已接管强制血量逻辑）
    @Inject(method = "tick", at = @At("TAIL"))
    private void the_last_sword$onTickEnd(CallbackInfo ci) {
    }

    @Inject(method = "tickDeath", at = @At("HEAD"), cancellable = true)
    private void onLivingEntityTickDeath(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (EntityUtil.hasProtection(entity)) {
            ci.cancel();
        }
    }

    //hurt：盟友判断 + 护盾吸收 + 剑灵绝毁附加
    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void onLivingEntityHurt(DamageSource damageSource, float damageAmount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity.level().isClientSide) {
            return;
        }

        //统一使用 canAttack 判断盟友关系（包含剑灵的所有盟友关系）
        if (damageSource.getEntity() instanceof LivingEntity attacker) {
            if (!EntityUtil.canAttack(attacker, entity)) {
                cir.setReturnValue(false);
                return;
            }
        }

        //肃正防御护盾优先吸收（受伤 -1, 彻底无敌式抵挡）
        if (the_last_sword$consumeShield(entity, 1)) {
            cir.setReturnValue(false);
            return;
        }

        //剑灵附加绝毁伤害（Mixin检测，绕过事件取消问题）
        if (!the_last_sword$isAbsoluteDestructionDamage(damageSource)
                && damageSource.getEntity() instanceof LivingEntity wraithAttacker) {
            WraithSummonManager.handleWraithDamage(entity, wraithAttacker);
        }
    }

    //actuallyHurt：护盾吸收 + 限伤逻辑
    @Inject(method = "actuallyHurt", at = @At("HEAD"), cancellable = true)
    private void onLivingEntityActuallyHurt(DamageSource damageSource, float damageAmount, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity.level().isClientSide) {
            return;
        }

        //肃正防御护盾优先吸收 (兜底直接调 actuallyHurt 绕过 hurt 的路径)
        if (the_last_sword$consumeShield(entity, 1)) {
            ci.cancel();
            return;
        }

        if (!EntityUtil.hasProtection(entity)) {
            return;
        }

        //限伤计算
        float maxHealth = (float) entity.getAttributeValue(Attributes.MAX_HEALTH);
        float damageReductionRatio = (float) TheLastSwordConfiguration.getDefenceCustomHealthDamageReductionSafely();
        float maxDamagePerHit = (float) TheLastSwordConfiguration.getDefenceMaxDamagePerHitSafely();
        float damageLimit = Math.min(maxHealth * damageReductionRatio, maxDamagePerHit);
        float realDamage = Math.min(damageAmount, damageLimit);

        //扣除自定义血量
        if (realDamage > 0.0f) {
            float currentHealth = EntityUtil.getWorldAnchor(entity);
            EntityUtil.setWorldAnchor(entity, currentHealth - realDamage);
        }

        //取消原版扣血
        ci.cancel();
    }

    @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
    private void onLivingEntityHeal(float healAmount, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity.level().isClientSide) {
            return;
        }

        //检查是否受保护
        if (EntityUtil.hasProtection(entity)) {
            if (healAmount > 0) {
                float currentHealth = EntityUtil.getWorldAnchor(entity);
                float maxHealth = (float) entity.getAttributeValue(Attributes.MAX_HEALTH);
                float newHealth = Math.min(currentHealth + healAmount, maxHealth);
                EntityUtil.setWorldAnchor(entity, newHealth);
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

        //检查是否受保护
        if (EntityUtil.hasProtection(entity)) {
            float currentHealth = EntityUtil.getWorldAnchor(entity);
            if (health > currentHealth) {
                float maxHealth = (float) entity.getAttributeValue(Attributes.MAX_HEALTH);
                float newHealth = Math.min(health, maxHealth);
                EntityUtil.setWorldAnchor(entity, newHealth);
            }
            ci.cancel();
        }
    }

    @Inject(method = "getHealth", at = @At("RETURN"), cancellable = true)
    private void onLivingEntityGetHealth(CallbackInfoReturnable<Float> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        //防御系统返回真实血量
        if (EntityUtil.hasProtection(entity)) {
            float trueHealth = EntityUtil.getWorldAnchor(entity);
            cir.setReturnValue(trueHealth);
        }
    }

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void onLivingEntityDie(DamageSource damageSource, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        //肃正防御兜底：死亡时仍有护盾则消耗一层并恢复满血
        if (the_last_sword$consumeShield(entity, 1)) {
            EntityUtil.theLastEndSetHealth(entity, entity.getMaxHealth());
            ci.cancel();
            return;
        }

        if (EntityUtil.hasProtection(entity)) {
            ci.cancel();
        }
    }

    @Inject(method = "die", at = @At("RETURN"))
    private void onLivingEntityDieReturn(DamageSource damageSource, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        EntityUtil.clearHealBan(entity);
    }


}
