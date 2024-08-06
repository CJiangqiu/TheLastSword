package net.thelastsword.capability;

import net.minecraft.nbt.Tag;

public interface ISwordLevel {
    void setLevel(int level);
    int getLevel();
    Tag writeNBT();
    void readNBT(Tag nbt);
}
