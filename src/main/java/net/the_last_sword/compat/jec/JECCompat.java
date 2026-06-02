package net.the_last_sword.compat.jec;

import net.the_last_sword.compat.CompatCheck;
import net.the_last_sword.util.TheLastSwordLogger;

import java.lang.reflect.Method;

//Just Enough Characters 兼容：反射调用拼音匹配，避免硬编译依赖；未安装或调用失败时退回普通包含判断
public class JECCompat {

    private static Method matchContains;
    private static boolean resolved;

    //惰性解析 Match.contains(String, String)，仅在 JEC 存在时尝试
    private static Method resolve() {
        if (!resolved) {
            resolved = true;
            if (CompatCheck.isJecLoaded()) {
                try {
                    matchContains = Class.forName("me.towdium.jecharacters.utils.Match")
                            .getMethod("contains", String.class, CharSequence.class);
                } catch (ReflectiveOperationException e) {
                    TheLastSwordLogger.warn("JEC is loaded but Match.contains was not found, falling back to plain contains");
                }
            }
        }
        return matchContains;
    }

    //支持拼音匹配，未安装 JEC 时退回普通包含判断
    public static boolean contains(String text, String query) {
        Method m = resolve();
        if (m != null) {
            try {
                return (boolean) m.invoke(null, text, query);
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return text.toLowerCase().contains(query.toLowerCase());
    }
}
