package net.thelastsword.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static net.thelastsword.capability.SwordCapability.EXTRA_ATTACK_DAMAGE_CAPABILITY;

public class ExtraAttackDamageProvider implements ICapabilitySerializable<CompoundTag> {
    private final IExtraAttackDamage instance;
    private final LazyOptional<IExtraAttackDamage> holder;

    public ExtraAttackDamageProvider(int defaultLevel) {
        this.instance = new DefaultExtraAttackDamage(defaultLevel);
        this.holder = LazyOptional.of(() -> instance);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == EXTRA_ATTACK_DAMAGE_CAPABILITY ? holder.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return (CompoundTag) instance.writeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.readNBT(nbt);
    }
}
