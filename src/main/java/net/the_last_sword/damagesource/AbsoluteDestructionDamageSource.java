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
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.init.ModAttributes;
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

    public static boolean applyAbsoluteDestructionIntelligently(LivingEntity entity, DamageSource damageSource, float damageAmount) {
        //获取当前血量
        float originalHealth = entity.getHealth();

        //步骤1: 被肃正防御护盾抵挡
        var justifiedDefenceAttribute = entity.getAttribute(ModAttributes.JUSTIFIED_DEFENCE.get());
        if (justifiedDefenceAttribute != null && justifiedDefenceAttribute.getValue() > 0) {
            justifiedDefenceAttribute.setBaseValue(justifiedDefenceAttribute.getValue() - 1.0D);
            return false;
        }

        //步骤2: 异常血量斩杀
        if (Float.isNaN(originalHealth) || Float.isInfinite(originalHealth) || originalHealth <= 0.0F) {
            if (TheLastSwordConfiguration.getEnableTheLastEndSetDeadSafely()) {
                EntityUtil.theLastEndSetDead(entity, damageSource);
                return true;
            }
        }

        float expectedHealth = originalHealth - damageAmount;
        entity.invulnerableTime=0;
        entity.hurt(damageSource, damageAmount);

        //步骤3: 检查实际vs预期生命值
        float actualHealth = entity.getHealth();

        //步骤4: 如果不一致，设置生命值
        if (actualHealth != expectedHealth) {
            EntityUtil.theLastEndSetHealth(entity, expectedHealth);

            if (expectedHealth <= 0) {
                if (TheLastSwordConfiguration.getEnableTheLastEndSetDeadSafely()) {
                    EntityUtil.theLastEndSetDead(entity, damageSource);
                    return true;
                }
            }
        }

        //步骤5: 设置禁疗状态
        int banTime = TheLastSwordConfiguration.getHealNegationTimeSafely();
        if (banTime > 0) {
            EntityUtil.setHealBanTime(entity, banTime);
        }
        return true;
    }

    public static boolean applyAbsoluteDestructionIntelligently(LivingEntity target, Entity attacker, ItemStack weapon, float damageAmount) {
        //检查盟友关系（包含剑灵系统）
        if (!EntityUtil.canAttack(attacker, target)) {
            return false;
        }
        DamageSource damageSource = absoluteDestruction(attacker, weapon);
        return applyAbsoluteDestructionIntelligently(target, damageSource, damageAmount);
    }

    public static boolean applyAbsoluteDestructionIntelligently(LivingEntity target, Entity attacker, float damageAmount) {
        //检查盟友关系（包含剑灵系统）
        if (!EntityUtil.canAttack(attacker, target)) {
            return false;
        }
        DamageSource damageSource = absoluteDestruction(attacker);
        return applyAbsoluteDestructionIntelligently(target, damageSource, damageAmount);
    }
}
