package net.thelastsword.capability;

import net.minecraft.nbt.Tag;

public interface IMode {
    void setMode(int mode);
    int getMode();
    Tag writeNBT();
    void readNBT(Tag nbt);
}
