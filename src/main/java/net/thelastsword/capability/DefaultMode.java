package net.thelastsword.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class DefaultMode implements IMode {
    private int mode;

    public DefaultMode(int defaultMode) {
        this.mode = defaultMode; // 设置默认模式
    }

    @Override
    public void setMode(int mode) {
        this.mode = mode;
    }

    @Override
    public int getMode() {
        return mode;
    }

    @Override
    public Tag writeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("mode", mode);
        return nbt;
    }

    @Override
    public void readNBT(Tag nbt) {
        if (nbt instanceof CompoundTag) {
            CompoundTag compoundNBT = (CompoundTag) nbt;
            mode = compoundNBT.getInt("mode");
        }
    }
}
