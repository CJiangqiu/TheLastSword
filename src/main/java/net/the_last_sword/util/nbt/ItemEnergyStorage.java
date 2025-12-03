package net.the_last_sword.util.nbt;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.energy.EnergyStorage;

import java.util.function.IntSupplier;

/**
 * 基于NBT的物品能量存储
 * 用于龙之盔甲的Forge Energy系统
 * 支持动态最大能量（根据物品等级）
 */
public class ItemEnergyStorage extends EnergyStorage {

    private static final String ENERGY_TAG = "the_last_sword.energy";
    private final ItemStack stack;
    private final IntSupplier maxEnergySupplier;

    public ItemEnergyStorage(ItemStack stack, IntSupplier maxEnergySupplier) {
        super(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
        this.stack = stack;
        this.maxEnergySupplier = maxEnergySupplier;
        this.energy = getEnergyFromNBT();
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive()) {
            return 0;
        }

        int currentEnergy = getEnergyFromNBT();
        int maxEnergy = getMaxEnergyStored();
        int energyReceived = Math.min(maxEnergy - currentEnergy, Math.min(this.maxReceive, maxReceive));

        if (!simulate && energyReceived > 0) {
            this.energy = currentEnergy + energyReceived;
            saveEnergyToNBT();
        }
        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!canExtract()) {
            return 0;
        }

        int currentEnergy = getEnergyFromNBT();
        int energyExtracted = Math.min(currentEnergy, Math.min(this.maxExtract, maxExtract));

        if (!simulate && energyExtracted > 0) {
            this.energy = currentEnergy - energyExtracted;
            saveEnergyToNBT();
        }
        return energyExtracted;
    }

    @Override
    public int getEnergyStored() {
        this.energy = getEnergyFromNBT();
        return this.energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return maxEnergySupplier.getAsInt();
    }

    private int getEnergyFromNBT() {
        if (stack.isEmpty() || !stack.hasTag()) {
            return 0;
        }
        return stack.getTag().getInt(ENERGY_TAG);
    }

    private void saveEnergyToNBT() {
        if (stack.isEmpty()) {
            return;
        }
        stack.getOrCreateTag().putInt(ENERGY_TAG, this.energy);
    }
}
