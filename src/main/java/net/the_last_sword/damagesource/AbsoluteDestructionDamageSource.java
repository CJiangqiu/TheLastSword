package net.the_last_sword.damagesource;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.eca.api.EcaAPI;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.TheLastEndEntity;
import net.the_last_sword.init.ModAttributes;
import net.the_last_sword.summon.WraithSummonManager;
import net.the_last_sword.util.EntityUtil;

//绝对毁灭伤害源
public class AbsoluteDestructionDamageSource extends DamageSource {

    private final Entity attacker;
    private final ItemStack weapon;

    public AbsoluteDestructionDamageSource(Holder<DamageType> damageType, Entity attacker, ItemStack weapon) {
        super(damageType, attacker, attacker);
        this.attacker = attacker;
        this.weapon = weapon;
    }

    public ItemStack getWeapon() {
        return weapon;
    }

    @Override
    public Entity getDirectEntity() {
        return attacker;
    }

    @Override
    public Entity getEntity() {
        return attacker;
    }

    @Override
    public boolean scalesWithDifficulty() {
        return false;
    }

    @Override
    public boolean isCreativePlayer() {
        return false;
    }

    // ==================== 工厂方法 ====================

    /**
     * 创建绝对毁灭伤害源 - 完整版本
     */
    public static DamageSource absoluteDestruction(Entity attacker, ItemStack weapon) {
        Registry<DamageType> reg = attacker.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        ResourceKey<DamageType> key = ResourceKey.create(Registries.DAMAGE_TYPE,
                new ResourceLocation(TheLastSwordMod.MOD_ID, "absolute_destruction"));
        Holder<DamageType> holder = reg.getHolderOrThrow(key);
        return new AbsoluteDestructionDamageSource(holder, attacker, weapon);
    }

    /**
     * 创建绝对毁灭伤害源 - 简化版本（只需要攻击者）
     */
    public static DamageSource absoluteDestruction(Entity attacker) {
        return absoluteDestruction(attacker, attacker instanceof LivingEntity living ?
            living.getMainHandItem() : ItemStack.EMPTY);
    }

    /**
     * 创建绝对毁灭伤害源 - 无攻击者版本（用于环境伤害等）
     */
    public static DamageSource absoluteDestruction(Level level) {
        Registry<DamageType> reg = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        ResourceKey<DamageType> key = ResourceKey.create(Registries.DAMAGE_TYPE,
                new ResourceLocation(TheLastSwordMod.MOD_ID, "absolute_destruction"));
        Holder<DamageType> holder = reg.getHolderOrThrow(key);
        return new AbsoluteDestructionDamageSource(holder, null, ItemStack.EMPTY);
    }


    // ==================== 智能绝毁伤害应用API ====================

    public static boolean applyAbsoluteDestruction(LivingEntity entity, DamageSource damageSource, float damageAmount) {

        //盟友关系兜底：attacker 非空时走 canAttack，环境伤害（attacker 为空）放行
        Entity attacker = damageSource.getEntity();
        if (attacker != null && !EntityUtil.canAttack(attacker, entity)) {
            return false;
        }

        //被肃正防御护盾抵挡
        var justifiedDefenceAttribute = entity.getAttribute(ModAttributes.JUSTIFIED_DEFENCE.get());
        if (justifiedDefenceAttribute != null && justifiedDefenceAttribute.getValue() > 0) {
            justifiedDefenceAttribute.setBaseValue(justifiedDefenceAttribute.getValue() - 1.0D);
            return false;
        }

        // 终焉种实体：直接修改世界锚度
        if (entity instanceof TheLastEndEntity theLastEnd) {
            float currentAnchor = theLastEnd.getWorldAnchor();
            float newAnchor = currentAnchor - damageAmount;
            entity.hurt(damageSource, 0.01f);
            theLastEnd.setWorldAnchor(newAnchor);
            if (newAnchor <= 0 && !theLastEnd.isDying()) {
                WraithSummonManager.tryForceCapture(entity);
                theLastEnd.triggerDeath();
            }
            return true;
        }

        float originalHealth = entity.getHealth();
        // 异常血量斩杀
        if (Float.isNaN(originalHealth) || Float.isInfinite(originalHealth) || originalHealth <= 0.0F) {
            WraithSummonManager.tryForceCapture(entity);
            if (TheLastSwordConfiguration.getEnableTheLastEndSetDeadSafely()) {
                EntityUtil.theLastEndSetDead(entity, damageSource);
            } else {
                EntityUtil.theLastEndSetHealth(entity, 0);
            }
            return true;
        }

        float expectedHealth = originalHealth - damageAmount;

        //预期血量判定
        if (expectedHealth <= 0) {
            WraithSummonManager.tryForceCapture(entity);
            if (TheLastSwordConfiguration.getEnableTheLastEndSetDeadSafely()) {
                EntityUtil.theLastEndSetDead(entity, damageSource);
                return true;
            }
        }

        entity.invulnerableTime=0;
        entity.hurt(damageSource, damageAmount);
        float actualHealth = entity.getHealth();
        //预期血量>0但实际不一致，ECA校正血量
        if (actualHealth != expectedHealth) {
            EntityUtil.theLastEndSetHealth(entity, expectedHealth);
        }

        //禁疗
        int banTime = TheLastSwordConfiguration.getHealNegationTimeSafely();
        if (banTime > 0) {
            EntityUtil.setHealBanTime(entity, banTime);
            EcaAPI.banHealing(entity, expectedHealth);
        }
        return true;
    }

    public static boolean applyAbsoluteDestruction(LivingEntity target, Entity attacker, ItemStack weapon, float damageAmount) {
        //检查盟友关系（包含剑灵系统）
        if (!EntityUtil.canAttack(attacker, target)) {
            return false;
        }
        DamageSource damageSource = absoluteDestruction(attacker, weapon);
        return applyAbsoluteDestruction(target, damageSource, damageAmount);
    }

    public static boolean applyAbsoluteDestruction(LivingEntity target, Entity attacker, float damageAmount) {
        //检查盟友关系（包含剑灵系统）
        if (!EntityUtil.canAttack(attacker, target)) {
            return false;
        }
        DamageSource damageSource = absoluteDestruction(attacker);
        return applyAbsoluteDestruction(target, damageSource, damageAmount);
    }
}
