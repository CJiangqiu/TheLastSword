package net.the_last_sword.damagesource;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.the_last_sword.init.TheLastSwordDamageTypes;

//绝对毁灭伤害源
public class AbsoluteDestructionDamageSource extends DamageSource {

    private final ItemStack weapon;
    private final boolean levelBonus;

    public AbsoluteDestructionDamageSource(Holder<DamageType> damageType, Entity attacker, ItemStack weapon) {
        this(damageType, attacker, weapon, false);
    }

    private AbsoluteDestructionDamageSource(Holder<DamageType> damageType, Entity attacker, ItemStack weapon,
                                            boolean levelBonus) {
        super(damageType, attacker, attacker);
        this.weapon = weapon;
        this.levelBonus = levelBonus;
    }

    public ItemStack getWeapon() {
        return weapon;
    }

    public boolean isLevelBonus() {
        return levelBonus;
    }

    // ==================== 工厂方法 ====================

    /**
     * 创建绝对毁灭伤害源 - 完整版本
     */
    public static DamageSource absoluteDestruction(Entity attacker, ItemStack weapon) {
        Registry<DamageType> reg = attacker.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        Holder<DamageType> holder = reg.getHolderOrThrow(TheLastSwordDamageTypes.ABSOLUTE_DESTRUCTION);
        return new AbsoluteDestructionDamageSource(holder, attacker, weapon);
    }

    /**
     * 创建绝对毁灭伤害源 - 简化版本（只需要攻击者）
     */
    public static DamageSource absoluteDestruction(Entity attacker) {
        return absoluteDestruction(attacker, attacker instanceof LivingEntity living ?
            living.getMainHandItem() : ItemStack.EMPTY);
    }

    //等级附伤使用独立标记，阻止伤害重入时再次派生自身
    public static DamageSource levelBonus(LivingEntity attacker) {
        Registry<DamageType> reg = attacker.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        Holder<DamageType> holder = reg.getHolderOrThrow(TheLastSwordDamageTypes.ABSOLUTE_DESTRUCTION);
        return new AbsoluteDestructionDamageSource(holder, attacker, attacker.getMainHandItem(), true);
    }

    /**
     * 创建绝对毁灭伤害源 - 无攻击者版本（用于环境伤害等）
     */
    public static DamageSource absoluteDestruction(Level level) {
        Registry<DamageType> reg = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        Holder<DamageType> holder = reg.getHolderOrThrow(TheLastSwordDamageTypes.ABSOLUTE_DESTRUCTION);
        return new AbsoluteDestructionDamageSource(holder, null, ItemStack.EMPTY);
    }
}
