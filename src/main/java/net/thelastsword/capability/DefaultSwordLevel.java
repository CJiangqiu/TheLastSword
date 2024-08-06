package net.thelastsword.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class DefaultSwordLevel implements ISwordLevel {
    private int level;

    public DefaultSwordLevel(int defaultLevel) {
        this.level = defaultLevel; // 设置默认剑等级
    }

    @Override
    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public Tag writeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("level", level);
        return nbt;
    }

    @Override
    public void readNBT(Tag nbt) {
        if (nbt instanceof CompoundTag) {
            CompoundTag compoundNBT = (CompoundTag) nbt;
            level = compoundNBT.getInt("level");
        }
    }
}
