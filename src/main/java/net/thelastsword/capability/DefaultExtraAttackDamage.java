package net.thelastsword.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.thelastsword.configuration.TheLastSwordConfiguration;

public class DefaultExtraAttackDamage implements IExtraAttackDamage {
    private float extraAttackDamage;

    public DefaultExtraAttackDamage(int level) {
        double increaseValue = TheLastSwordConfiguration.INCREASE_VALUE.get();
        double increaseValueHighLevel = TheLastSwordConfiguration.INCREASE_VALUE_HIGH_LEVEL.get();
        this.extraAttackDamage = (float) (level * (level < 6 ? increaseValue : increaseValueHighLevel)); // 设置默认额外攻击力
    }

    @Override
    public void setExtraAttackDamage(float damage) {
        this.extraAttackDamage = damage;
    }

    @Override
    public float getExtraAttackDamage() {
        return extraAttackDamage;
    }

    @Override
    public Tag writeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putFloat("extraAttackDamage", extraAttackDamage);
        return nbt;
    }

    @Override
    public void readNBT(Tag nbt) {
        if (nbt instanceof CompoundTag) {
            CompoundTag compoundNBT = (CompoundTag) nbt;
            extraAttackDamage = compoundNBT.getFloat("extraAttackDamage");
        }
    }
}
