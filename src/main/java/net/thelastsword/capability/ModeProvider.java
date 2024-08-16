package net.thelastsword.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.IntTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static net.thelastsword.capability.SwordCapability.MODE_CAPABILITY;

public class ModeProvider implements ICapabilitySerializable<IntTag> {
    private final IMode instance;
    private final LazyOptional<IMode> holder;

    public ModeProvider(int defaultMode) {
        this.instance = new DefaultMode(defaultMode);
        this.holder = LazyOptional.of(() -> instance);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == MODE_CAPABILITY ? holder.cast() : LazyOptional.empty();
    }

    @Override
    public IntTag serializeNBT() {
        return IntTag.valueOf(instance.getMode());
    }

    @Override
    public void deserializeNBT(IntTag nbt) {
        instance.setMode(nbt.getAsInt());
    }
}
