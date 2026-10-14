package net.the_last_sword.util;

import net.the_last_sword.configuration.TheLastSwordConfiguration;

import java.util.List;

public final class InventoryProtectionPolicy {
    private static final StackWalker STACK_WALKER = StackWalker.getInstance();
    private static final List<String> PLATFORM_PACKAGES = List.of(
        "java", "sun", "jdk", "com.sun",
        "net.minecraft", "com.mojang", "net.minecraftforge", "cpw.mods",
        "org.spongepowered.asm", "net.eca"
    );

    private InventoryProtectionPolicy() {
    }

    public static boolean hasBlockedExternalCaller() {
        List<? extends String> whitelist = TheLastSwordConfiguration.getInventoryProtectionPackageWhitelistSafely();
        // 跳过判定方法和注入回调，保持原有五层调用范围。
        return STACK_WALKER.walk(frames -> frames.skip(2).limit(5)
            .map(StackWalker.StackFrame::getClassName)
            .anyMatch(className -> !matchesAnyPackage(className, PLATFORM_PACKAGES)
                && !matchesAnyPackage(className, whitelist)));
    }

    private static boolean matchesAnyPackage(String className, List<? extends String> packages) {
        for (String packageName : packages) {
            String prefix = packageName.endsWith(".") ? packageName : packageName + ".";
            // 必须跨过包名边界，避免同名前缀的其他包被误放行。
            if (className.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
