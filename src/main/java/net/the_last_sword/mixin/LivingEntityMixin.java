package net.the_last_sword.mixin;

import net.the_last_sword.init.ModEffects;

import net.eca.api.EcaAPI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.tags.DamageTypeTags;
import net.the_last_sword.configuration.DefenceConfig;
import net.the_last_sword.configuration.DefenceConfigData;
import net.the_last_sword.damagesource.AbsoluteDestructionDamageSource;
import net.the_last_sword.entity.TheLastEndEntity;
import net.the_last_sword.entity.TheLastEndSwordWraithEntity;
import net.the_last_sword.event.DefenceEventHandler;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.init.ModAttributes;
import net.the_last_sword.init.ModEnchantments;
import net.the_last_sword.init.TheLastSwordDamageTypes;
import net.the_last_sword.item.DragonArmorItem;
import net.the_last_sword.network.DragonShieldPacket;
import net.the_last_sword.network.NetworkHandler;
import net.the_last_sword.summon.WraithSummonManager;
import net.the_last_sword.util.EntityUtil;
import net.the_last_sword.util.PhasingState;
import net.the_last_sword.util.damage.AdvancedEquipmentDamageHandler;
import net.the_last_sword.util.damage.AdvancedEquipmentDamageHandler.DamageResult;
import net.the_last_sword.util.health.TrueHealthManager;
import net.the_last_sword.util.health.PresentWorldAnchorManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;

@Mixin(value = LivingEntity.class, priority = 1024)
public class LivingEntityMixin {

    @Shadow
    protected Player lastHurtByPlayer;

    @Shadow
    protected int lastHurtByPlayerTime;

    @Shadow
    private DamageSource lastDamageSource;

    @Shadow
    private long lastDamageStamp;

    //伤害源被重建时仍需阻止同一调用链重复派生等级附伤。
    @Unique
    private static final ThreadLocal<Boolean> the_last_sword$APPLYING_LEVEL_BONUS = new ThreadLocal<>();

    @Unique
    private double the_last_sword$justifiedDefenceRecoveryProgress;

    //静态初始化注入：在原版 defineId 调用后紧接着定义我们的 EntityDataAccessor
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void the_last_sword$onClinit(CallbackInfo ci) {
        PresentWorldAnchorManager.PRESENT_WORLD_ANCHOR = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.STRING);
        PresentWorldAnchorManager.HEAL_BAN_TIME = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.INT);
        EntityUtil.IS_PROTECTED = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BOOLEAN);
        PhasingState.PHASING = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BOOLEAN);
    }

    //注册实体数据（在每个实例的 defineSynchedData 中调用）
    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void the_last_sword$onDefineSynchedData(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        entity.getEntityData().define(PresentWorldAnchorManager.PRESENT_WORLD_ANCHOR, "");
        entity.getEntityData().define(PresentWorldAnchorManager.HEAL_BAN_TIME, 0);
        entity.getEntityData().define(EntityUtil.IS_PROTECTED, false);
        entity.getEntityData().define(PhasingState.PHASING, false);
    }

    // 读档后恢复派生状态，让首次追踪的客户端也能获取正确同步值。
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void the_last_sword$syncPresentWorldAnchorAfterLoad(CompoundTag tag, CallbackInfo ci) {
        PresentWorldAnchorManager.syncPresentWorldAnchor((LivingEntity) (Object) this);
        PhasingState.sync((LivingEntity) (Object) this);
    }

    // 等效果列表完成到期与移除处理后同步，避免清除全部效果时读到移除前的状态。
    @Inject(method = "tickEffects", at = @At("TAIL"))
    private void the_last_sword$syncPhasingState(CallbackInfo ci) {
        PhasingState.sync((LivingEntity) (Object) this);
    }

    //检查是否是绝对毁灭伤害源
    @Unique
    private boolean the_last_sword$isAbsoluteDestructionDamage(DamageSource source) {
        return source.is(TheLastSwordDamageTypes.ABSOLUTE_DESTRUCTION);
    }

    //等级附伤重入时不得再次派生自身
    @Unique
    private boolean the_last_sword$isLevelBonus(DamageSource source) {
        return source instanceof AbsoluteDestructionDamageSource absoluteDamage
                && absoluteDamage.isLevelBonus();
    }

    //绝毁接管原版 hurt 后必须保留击杀归属，否则玩家限定掉落与延迟奖励无法识别攻击者。
    @Unique
    private void the_last_sword$recordAbsoluteDamageSource(LivingEntity target, DamageSource source,
                                                            float damageAmount) {
        Entity attacker = source.getEntity();
        if (attacker instanceof LivingEntity livingAttacker && !source.is(DamageTypeTags.NO_ANGER)) {
            target.setLastHurtByMob(livingAttacker);
        }

        Player playerCredit = null;
        if (attacker instanceof Player player) {
            playerCredit = player;
        } else if (attacker instanceof TamableAnimal tamable
                && tamable.isTame()
                && tamable.getOwner() instanceof Player owner) {
            playerCredit = owner;
        }
        if (playerCredit == null) {
            playerCredit = WraithSummonManager.getWraithOwnerFromDamageSource(source, target.level());
        }
        if (playerCredit != null) {
            lastHurtByPlayer = playerCredit;
            lastHurtByPlayerTime = 100;
        }

        lastDamageSource = source;
        lastDamageStamp = target.level().getGameTime();
        target.getCombatTracker().recordDamage(source, damageAmount);
    }

    @Unique
    private void the_last_sword$applyLevelBonus(LivingEntity target, DamageSource source,
                                                           float damageAmount) {
        if (Boolean.TRUE.equals(the_last_sword$APPLYING_LEVEL_BONUS.get())
                || the_last_sword$isLevelBonus(source)
                || !(source.getEntity() instanceof LivingEntity attacker)
                || !Float.isFinite(damageAmount)
                || damageAmount <= 0.0F
                || !EntityUtil.canAttack(attacker, target)) {
            return;
        }

        int level = attacker instanceof TheLastEndEntity endEntity
                ? endEntity.getTheLastEndLevel()
                : TheLastSwordConfiguration.getSwordWraithAsTheLastEndEntitySafely()
                && TheLastSwordConfiguration.getSwordWraithAbsoluteDestructionDamageSafely()
                ? WraithSummonManager.getWraithLevel(attacker) : 0;
        if (level < 6) {
            return;
        }
        float bonusDamage = damageAmount * level / 100.0F;
        if (Float.isFinite(bonusDamage) && bonusDamage > 0.0F) {
            the_last_sword$APPLYING_LEVEL_BONUS.set(true);
            try {
                EcaAPI.hurt(target, AbsoluteDestructionDamageSource.levelBonus(attacker), bonusDamage);
            } finally {
                the_last_sword$APPLYING_LEVEL_BONUS.remove();
            }
        }
    }

    //伤害来源是否为终焉实体或剑灵
    @Unique
    private boolean the_last_sword$isTheLastEndAttacker(DamageSource source) {
        Entity attacker = source.getEntity();
        if (attacker instanceof TheLastEndEntity) {
            return true;
        }
        return attacker instanceof LivingEntity living && WraithSummonManager.isWraith(living);
    }

    //肃正防御护盾消费: 存在护盾则扣除指定代价, 返回是否成功消费
    @Unique
    private boolean the_last_sword$consumeShield(LivingEntity entity, int cost) {
        AttributeInstance shieldAttr = entity.getAttribute(ModAttributes.JUSTIFIED_DEFENCE.get());
        if (shieldAttr == null || shieldAttr.getValue() <= 0) return false;
        DefenceEventHandler.setShieldValue(entity, shieldAttr.getValue() - cost);
        return true;
    }

    @Unique
    private void the_last_sword$showShieldEffect(LivingEntity entity, DamageSource source) {
        if (entity instanceof ServerPlayer serverPlayer
                && DragonArmorItem.isFullSet(serverPlayer)
                && DragonArmorItem.hasEnergyFullSet(serverPlayer)
                && DefenceConfig.getDragonShieldModule().shieldEffect
                != DefenceConfigData.ShieldEffectMode.DISABLED) {
            NetworkHandler.sendToPlayer(DragonShieldPacket.fromDamageSource(serverPlayer, source), serverPlayer);
        }
    }

    //注册等级只控制自然附魔，附伤必须使用物品保存的真实等级。
    @Unique
    private void the_last_sword$applyWorldSeveranceEnchantment(LivingEntity target, DamageSource source,
                                                                float damageAmount) {
        if (!(source.getEntity() instanceof LivingEntity attacker)
                || !Float.isFinite(damageAmount)
                || damageAmount <= 0.0F
                || !EntityUtil.canAttack(attacker, target)) {
            return;
        }

        ItemStack weapon = attacker.getMainHandItem();
        int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(
                ModEnchantments.WORLD_SEVERANCE.get(), weapon);
        if (enchantmentLevel <= 0) {
            return;
        }

        float bonusDamage = damageAmount * enchantmentLevel * 0.1F;
        if (Float.isFinite(bonusDamage) && bonusDamage > 0.0F) {
            EcaAPI.hurt(target, AbsoluteDestructionDamageSource.absoluteDestruction(attacker, weapon), bonusDamage);
        }
    }


    //肃正防御护盾 > 0 时注册保护，= 0 时清除（不影响剑的保护）
    @Unique
    private void the_last_sword$handleJustifiedDefenceProtection(LivingEntity entity) {
        AttributeInstance curAttr = entity.getAttribute(ModAttributes.JUSTIFIED_DEFENCE.get());
        if (curAttr == null) return;

        if (curAttr.getValue() > 0) {
            entity.getPersistentData().remove("JustifiedDefenceClearDelay");
            if (!EntityUtil.hasProtection(entity)) {
                float realHealth = entity.getHealth();
                TrueHealthManager.setMaxHealth(entity, entity.getMaxHealth());
                TrueHealthManager.setHealth(entity, realHealth);
                EntityUtil.setProtection(entity, true);
                entity.getPersistentData().putBoolean("JustifiedDefenceProtection", true);
            }
        } else if (entity.getPersistentData().getBoolean("JustifiedDefenceProtection")) {
            int delay = entity.getPersistentData().getInt("JustifiedDefenceClearDelay");
            if (delay <= 0) {
                entity.getPersistentData().putInt("JustifiedDefenceClearDelay", 5);
            } else if (delay == 1) {
                entity.getPersistentData().remove("JustifiedDefenceClearDelay");
                entity.getPersistentData().remove("JustifiedDefenceProtection");
                if (!entity.getPersistentData().getBoolean("TheLastSwordDefence")) {
                    float trueHealth = TrueHealthManager.getHealth(entity);
                    TrueHealthManager.clear(entity);
                    if (trueHealth >= 0 && trueHealth < entity.getHealth()) {
                        entity.setHealth(trueHealth);
                    }
                }
            } else {
                entity.getPersistentData().putInt("JustifiedDefenceClearDelay", delay - 1);
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

        //第一层：max <= 0 的情况（3个分支）
        if (max <= 0) {
            the_last_sword$justifiedDefenceRecoveryProgress = 0.0;
            //分支1: cur == 0 → 直接 return（99%普通实体快速退出）
            if (cur <= 0) {
                //临时护盾已耗尽，清除标记恢复常规约束
                if (entity.getPersistentData().getBoolean(EntityUtil.NBT_TEMP_JUSTIFIED_DEFENCE)) {
                    entity.getPersistentData().remove(EntityUtil.NBT_TEMP_JUSTIFIED_DEFENCE);
                }
                return;
            }
            //分支2: 临时护盾不设上限，豁免清零且不参与回复
            if (entity.getPersistentData().getBoolean(EntityUtil.NBT_TEMP_JUSTIFIED_DEFENCE)) {
                return;
            }
            //分支3: cur > 0 → 限制为0
            curAttr.setBaseValue(0.0);
            return;
        }

        //第二层：max > 0 的情况（3个分支）
        //分支1: cur == max → 直接 return（护盾已满）
        if (Math.abs(cur - max) < 0.001) {
            the_last_sword$justifiedDefenceRecoveryProgress = 0.0;
            return;
        }

        //分支2: cur > max → 校准（限制不超过最大值）
        if (cur > max) {
            curAttr.setBaseValue(max);
            the_last_sword$justifiedDefenceRecoveryProgress = 0.0;
            return;
        }

        //分支3: cur < max → 按“点/tick”属性累积恢复进度
        AttributeInstance recoverySpeedAttr = entity.getAttribute(ModAttributes.JUSTIFIED_DEFENCE_RECOVERY_SPEED.get());
        double recoverySpeed = recoverySpeedAttr == null ? 0.01 : recoverySpeedAttr.getValue();
        if (recoverySpeed <= 0.0) return;

        the_last_sword$justifiedDefenceRecoveryProgress += recoverySpeed;
        double recoveredPoints = Math.floor(the_last_sword$justifiedDefenceRecoveryProgress + 1.0E-9);
        if (recoveredPoints < 1.0) return;

        double newValue = Math.min(cur + recoveredPoints, max);
        curAttr.setBaseValue(newValue);
        the_last_sword$justifiedDefenceRecoveryProgress -= recoveredPoints;
        if (newValue >= max) {
            //满盾时不储存恢复进度，避免受击后立即回盾
            the_last_sword$justifiedDefenceRecoveryProgress = 0.0;
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
                if (!effect.isBeneficial() && effect != MobEffects.ABSORPTION
                        && effect != ModEffects.WORLD_SEVERANCE.get()) {
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

        if (entity.isDeadOrDying()) {
            the_last_sword$justifiedDefenceRecoveryProgress = 0.0;
        } else {
            the_last_sword$handleShieldRegeneration(entity);

            //肃正防御存在时自动注册保护
            the_last_sword$handleJustifiedDefenceProtection(entity);
        }

        //每 tick 更新禁疗计时器
        PresentWorldAnchorManager.tickHealBanTime(entity);

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

    //底层系统必须先于原版免疫帧和事件完成绝毁、肃正防御与终焉附伤
    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void onLivingEntityHurt(DamageSource damageSource, float damageAmount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity.level().isClientSide) {
            return;
        }

        if (the_last_sword$isAbsoluteDestructionDamage(damageSource)) {
            Entity attacker = damageSource.getEntity();
            if (attacker != null && !EntityUtil.canAttack(attacker, entity)) {
                cir.setReturnValue(false);
                return;
            }

            //普通伤害不能抢占绝毁冷却，否则同一次武器攻击的物理部分会吞掉绝毁伤害
            if (entity instanceof TheLastEndEntity theLastEnd
                    && theLastEnd.getAbsoluteDestructionHurtResistTick() > 0) {
                cir.setReturnValue(false);
                return;
            }

            if (the_last_sword$consumeShield(entity, 1)) {
                the_last_sword$showShieldEffect(entity, damageSource);
                cir.setReturnValue(false);
                return;
            }

            DamageResult result = AdvancedEquipmentDamageHandler.process(entity, damageSource, damageAmount);
            if (result.canceled()) {
                cir.setReturnValue(result.handledResult());
                return;
            }

            the_last_sword$recordAbsoluteDamageSource(entity, damageSource, result.amount());
            cir.setReturnValue(PresentWorldAnchorManager.handleAbsoluteDestructionDamage(
                    entity, damageSource, result.amount()));
            return;
        }

        if (the_last_sword$consumeShield(entity, 1)) {
            the_last_sword$showShieldEffect(entity, damageSource);
            cir.setReturnValue(false);
            return;
        }

        the_last_sword$applyWorldSeveranceEnchantment(entity, damageSource, damageAmount);

        if (!AdvancedEquipmentDamageHandler.isConvertingDamage()) {
            the_last_sword$applyLevelBonus(entity, damageSource, damageAmount);
        }

        if (the_last_sword$isTheLastEndAttacker(damageSource)) {
            entity.invulnerableTime = 0;
        }
    }

    //普通保护和非终焉种剑灵共用真实生命结算，终焉种保留自身覆写
    @Inject(method = "actuallyHurt", at = @At("HEAD"), cancellable = true)
    private void onLivingEntityActuallyHurt(DamageSource damageSource, float damageAmount, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity.level().isClientSide) {
            return;
        }

        if (entity instanceof TheLastEndEntity) {
            return;
        }

        boolean ordinaryWraith = TheLastSwordConfiguration.getSwordWraithAsTheLastEndEntitySafely()
                && WraithSummonManager.isWraith(entity);
        if (!ordinaryWraith && !EntityUtil.hasProtection(entity)) {
            return;
        }

        float realDamage = damageAmount;
        if (ordinaryWraith) {
            float maxHealth = (float) entity.getAttributeValue(Attributes.MAX_HEALTH);
            float ratio = (float) TheLastSwordConfiguration.getDefenceCustomHealthDamageReductionSafely();
            float maxDamage = (float) TheLastSwordConfiguration.getDefenceMaxDamagePerHitSafely();
            float damageLimit = Math.min(maxHealth * ratio, maxDamage);
            int level = WraithSummonManager.getWraithLevel(entity);
            if (level <= 5) {
                realDamage = Math.min(realDamage, damageLimit);
            } else if (realDamage > damageLimit) {
                ci.cancel();
                return;
            }
        }

        DamageResult equipmentResult = AdvancedEquipmentDamageHandler.processThenPostEvent(
                entity, damageSource, realDamage);
        if (equipmentResult.canceled()) {
            ci.cancel();
            return;
        }
        realDamage = equipmentResult.amount();

        float currentHealth = TrueHealthManager.getHealth(entity);
        float newHealth = currentHealth - realDamage;
        TrueHealthManager.setHealth(entity, newHealth);
        if (newHealth <= 0.0F) {
            WraithSummonManager.stopWraithResurrection(entity);
            TrueHealthManager.clear(entity);
            EcaAPI.setHealth(entity, 0.0F);
        }

        ci.cancel();
    }

    @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
    private void onLivingEntityHeal(float healAmount, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity.level().isClientSide) {
            return;
        }

        PresentWorldAnchorManager.restorePresentWorldAnchorFromHeal(entity, healAmount);

        //检查是否受保护
        if (EntityUtil.hasProtection(entity)) {
            if (healAmount > 0) {
                float currentHealth = TrueHealthManager.getHealth(entity);
                float maxHealth = (float) entity.getAttributeValue(Attributes.MAX_HEALTH);
                float newHealth = Math.min(currentHealth + healAmount, maxHealth);
                Float healLimit = EcaAPI.getHealBanValue(entity);
                if (healLimit != null) {
                    newHealth = Math.min(newHealth, healLimit);
                }
                if (newHealth > currentHealth) {
                    TrueHealthManager.setHealth(entity, newHealth);
                }
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

        if (entity instanceof TheLastEndSwordWraithEntity && EntityUtil.hasProtection(entity)
                && health > TrueHealthManager.getHealth(entity)) {
            ci.cancel();
            return;
        }

        //检查是否受保护
        if (EntityUtil.hasProtection(entity)) {
            float currentHealth = TrueHealthManager.getHealth(entity);
            if (health > currentHealth) {
                float maxHealth = (float) entity.getAttributeValue(Attributes.MAX_HEALTH);
                float newHealth = Math.min(health, maxHealth);
                Float healLimit = EcaAPI.getHealBanValue(entity);
                if (healLimit != null) {
                    newHealth = Math.min(newHealth, healLimit);
                }
                if (newHealth > currentHealth) {
                    TrueHealthManager.setHealth(entity, newHealth);
                }
            }
            ci.cancel();
        }
    }

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void onLivingEntityDie(DamageSource damageSource, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        //肃正防御兜底：死亡时仍有护盾则消耗一层并恢复满血
        if (the_last_sword$consumeShield(entity, 1)) {
            TrueHealthManager.setHealth(entity, entity.getMaxHealth());
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
        PresentWorldAnchorManager.clearHealBan(entity);
    }


}
