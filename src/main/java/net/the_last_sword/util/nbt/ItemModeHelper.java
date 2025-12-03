package net.the_last_sword.util.nbt;

import net.minecraft.world.item.ItemStack;

/**
 * 物品模式切换辅助类
 * 当前版本(1.20.1): 使用NBT存储
 * 迁移到1.21+时: 只需修改此类使用DataComponents
 *
 * 注意：模式的最大数量和翻译键由具体物品类定义
 * 此Helper只负责存储和读取当前模式值
 */
public class ItemModeHelper {
    private static final String MODE_TAG = "the_last_sword.mode";
    private static final String MAX_MODES_TAG = "the_last_sword.max_modes";

    //设置当前模式（用于取模防止越界）
    public static void setMode(ItemStack stack, int mode, int maxModes) {
        if (stack.isEmpty() || maxModes <= 0) {
            return;
        }
        stack.getOrCreateTag().putInt(MODE_TAG, mode % maxModes);
        stack.getOrCreateTag().putInt(MAX_MODES_TAG, maxModes);
    }

    //获取当前模式，如果没有则返回0
    public static int getMode(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return 0;
        }
        return stack.getTag().getInt(MODE_TAG);
    }

    //获取最大模式数，如果没有则返回1
    public static int getMaxModes(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return 1;
        }
        int maxModes = stack.getTag().getInt(MAX_MODES_TAG);
        return maxModes > 0 ? maxModes : 1;
    }

    //检查物品是否有模式数据
    public static boolean hasMode(ItemStack stack) {
        return !stack.isEmpty() && stack.hasTag() && stack.getTag().contains(MODE_TAG);
    }

    //切换到下一个模式（循环）
    public static void cycleMode(ItemStack stack, int maxModes) {
        if (stack.isEmpty() || maxModes <= 0) {
            return;
        }
        int currentMode = getMode(stack);
        int nextMode = (currentMode + 1) % maxModes;
        setMode(stack, nextMode, maxModes);
    }

    //初始化模式系统
    public static void initializeMode(ItemStack stack, int defaultMode, int maxModes) {
        if (stack.isEmpty() || maxModes <= 0) {
            return;
        }
        if (!hasMode(stack)) {
            setMode(stack, defaultMode, maxModes);
        }
    }

    //移除模式数据
    public static void removeMode(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return;
        }
        stack.getTag().remove(MODE_TAG);
        stack.getTag().remove(MAX_MODES_TAG);
    }
}
