package net.thelastsword.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.IntTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static net.thelastsword.capability.SwordCapability.SWORD_LEVEL_CAPABILITY;

public class SwordLevelProvider implements ICapabilitySerializable<IntTag> {
    private final ISwordLevel instance;
    private final LazyOptional<ISwordLevel> holder;

    public SwordLevelProvider(int defaultLevel) {
        this.instance = new DefaultSwordLevel(defaultLevel);
        this.holder = LazyOptional.of(() -> instance);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == SWORD_LEVEL_CAPABILITY ? holder.cast() : LazyOptional.empty();
    }

    @Override
    public IntTag serializeNBT() {
        return IntTag.valueOf(instance.getLevel());
    }

    @Override
    public void deserializeNBT(IntTag nbt) {
        instance.setLevel(nbt.getAsInt());
    }
}
