package net.the_last_sword.compat.projecte;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.nbt.INBTProcessor;
import moze_intel.projecte.api.nbt.NBTProcessor;
import moze_intel.projecte.emc.EMCMappingHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.the_last_sword.util.nbt.ItemLevelHelper;

@NBTProcessor(priority = Integer.MAX_VALUE)
public class SmithingLevelEmcProcessor implements INBTProcessor {
    @Override
    public String getName() {
        return "DragonCrystalSmithingLevels";
    }

    @Override
    public String getDescription() {
        return "Preserves smithing levels and uses their mapped EMC before other modifiers.";
    }

    @Override
    public boolean hasPersistentNBT() {
        return true;
    }

    @Override
    public CompoundTag getPersistentNBT(ItemInfo info) {
        CompoundTag tag = info.getNBT();
        if (tag == null || !tag.contains(ItemLevelHelper.LEVEL_TAG, Tag.TAG_INT)) {
            return null;
        }
        CompoundTag persistent = new CompoundTag();
        persistent.putInt(ItemLevelHelper.LEVEL_TAG, tag.getInt(ItemLevelHelper.LEVEL_TAG));
        return persistent;
    }

    @Override
    public long recalculateEMC(ItemInfo info, long currentEMC) {
        if (EMCMappingHandler.hasEmcValue(info)) {
            return currentEMC;
        }
        ItemInfo normalized = normalizeLevel(info);
        // 无法推导的等级不能回退成零级价格。
        if (normalized != info && EMCMappingHandler.getStoredEmcValue(normalized) <= 0) {
            return 0;
        }
        // 基础值已在查询入口选择，不能覆盖此前执行的耐久修正。
        return currentEMC;
    }

    public static ItemInfo normalizeLevel(ItemInfo info) {
        CompoundTag tag = info.getNBT();
        if (tag == null || !tag.contains(ItemLevelHelper.LEVEL_TAG, Tag.TAG_INT)) {
            return info;
        }
        int level = tag.getInt(ItemLevelHelper.LEVEL_TAG);
        CompoundTag normalized = null;
        if (level != 0) {
            normalized = new CompoundTag();
            normalized.putInt(ItemLevelHelper.LEVEL_TAG, level);
        }
        return ItemInfo.fromItem(info.getItem(), normalized);
    }
}
