package net.the_last_sword.util.nbt;

import net.minecraft.world.item.ItemStack;

/**
 * 物品等级辅助类
 * 当前版本(1.20.1): 使用NBT存储
 * 迁移到1.21+时: 只需修改此类使用DataComponents
 */
public class ItemLevelHelper {
    private static final String LEVEL_TAG = "the_last_sword.level";

    //设置物品等级
    public static void setLevel(ItemStack stack, int level) {
        if (stack.isEmpty()) {
            return;
        }
        stack.getOrCreateTag().putInt(LEVEL_TAG, level);
    }

    //获取物品等级，如果没有等级数据则返回0
    public static int getLevel(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return 0;
        }
        return stack.getTag().getInt(LEVEL_TAG);
    }

    //检查物品是否有等级数据
    public static boolean hasLevel(ItemStack stack) {
        return !stack.isEmpty() && stack.hasTag() && stack.getTag().contains(LEVEL_TAG);
    }

    //移除物品的等级数据
    public static void removeLevel(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return;
        }
        stack.getTag().remove(LEVEL_TAG);
    }

    //确保等级已初始化，如果没有则设置为默认值
    public static void ensureInitialized(ItemStack stack, int defaultLevel) {
        if (!hasLevel(stack)) {
            setLevel(stack, defaultLevel);
        }
    }
}
