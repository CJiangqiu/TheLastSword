package net.thelastsword.damagetype;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.thelastsword.event.TheLastDead;
import net.thelastsword.event.JustifiedDefense;

public class AbsoluteDestruction extends DamageSource {

    public static final DamageSource ABSOLUTE_DESTRUCTION = new AbsoluteDestruction(Holder.direct(new DamageType("absolute_destruction", 0)));

    public AbsoluteDestruction(Holder<DamageType> damageType) {
        super(damageType);
    }

    public static void applyAbsoluteDestruction(Entity entity, float amount) {
        if (JustifiedDefense.hasJustifiedDefense(entity)) {
            JustifiedDefense.removeJustifiedDefense(entity);
            return; // 抵消绝对毁灭伤害并移除肃正防御效果
        }

        float absoluteHealth = getAbsoluteHealth(entity);
        absoluteHealth -= amount;
        setAbsoluteHealth(entity, absoluteHealth);

        // 检查绝对生命值是否小于等于0
        if (absoluteHealth <= 0) {
            TheLastDead.theLastDead(entity); // 调用自定义死亡方法
        }
    }

    public static float getAbsoluteHealth(Entity entity) { // 修改为public
        // 尝试从NBT数据中获取绝对生命值
        CompoundTag nbt = entity.getPersistentData();
        if (nbt.contains("absolute_health")) {
            return nbt.getFloat("absolute_health");
        }

        // 如果没有绝对生命值，初始化为实体的当前生命值
        float currentHealth = entity instanceof LivingEntity ? ((LivingEntity) entity).getHealth() : 1.0F;
        nbt.putFloat("absolute_health", currentHealth);
        return currentHealth;
    }

    public static void setAbsoluteHealth(Entity entity, float health) { // 修改为public
        // 尝试将绝对生命值存储到NBT数据中
        CompoundTag nbt = entity.getPersistentData();
        nbt.putFloat("absolute_health", health);
    }
}
