package net.the_last_sword.util;

/**
 * 实体查询上下文管理器
 */
public class EntityQueryContext {

    private static final String MOD_PACKAGE = "net.the_last_sword";
    private static final String VANILLA_PACKAGE = "net.minecraft";

    /**
     * 检查当前调用栈是否来自mod内部
     */
    public static boolean isInternalModQuery() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();

        // 跳过前几个框架方法，查找真正的调用者
        for (int i = 3; i < Math.min(stack.length, 10); i++) {
            String className = stack[i].getClassName();
            if (className.startsWith(MOD_PACKAGE)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查当前调用栈是否来自原版代码
     */
    public static boolean isVanillaQuery() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();

        // 跳过前几个框架方法，查找真正的调用者
        for (int i = 3; i < Math.min(stack.length, 15); i++) {
            String className = stack[i].getClassName();
            if (className.startsWith(VANILLA_PACKAGE)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查当前调用栈是否允许访问防御实体（mod内部或原版）
     */
    public static boolean isAllowedToAccessProtectedEntities() {
        return isInternalModQuery() || isVanillaQuery();
    }

}
