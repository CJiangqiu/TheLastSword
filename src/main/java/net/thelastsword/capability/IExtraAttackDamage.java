package net.thelastsword.capability;

import net.minecraft.nbt.Tag;

public interface IExtraAttackDamage {
    void setExtraAttackDamage(float damage);
    float getExtraAttackDamage();
    Tag writeNBT();
    void readNBT(Tag nbt);
}
