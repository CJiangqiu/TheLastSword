package net.the_last_sword.util;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public final class TheLastSwordLogger {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PREFIX = "[TheLastSword] ";

    public static Logger getLogger() {
        return LOGGER;
    }

    public static void info(String msg) {
        LOGGER.info(PREFIX + msg);
    }

    public static void info(String fmt, Object... args) {
        LOGGER.info(PREFIX + fmt, args);
    }

    public static void warn(String msg) {
        LOGGER.warn(PREFIX + msg);
    }

    public static void warn(String fmt, Object... args) {
        LOGGER.warn(PREFIX + fmt, args);
    }

    public static void error(String msg) {
        LOGGER.error(PREFIX + msg);
    }

    public static void error(String fmt, Object... args) {
        LOGGER.error(PREFIX + fmt, args);
    }

    public static void error(String msg, Throwable throwable) {
        LOGGER.error(PREFIX + msg, throwable);
    }

    public static void debug(String msg) {
        LOGGER.debug(PREFIX + msg);
    }

    public static void debug(String fmt, Object... args) {
        LOGGER.debug(PREFIX + fmt, args);
    }

    private TheLastSwordLogger() {}
}
