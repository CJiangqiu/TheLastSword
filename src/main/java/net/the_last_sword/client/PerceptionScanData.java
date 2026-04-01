package net.the_last_sword.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 客户端感知扫描数据，存储被标记实体的描边颜色（带过期时间）
public class PerceptionScanData {

    public enum ScanType {
        HOSTILE(0xFF0000),
        FRIENDLY(0x00FF00),
        NEUTRAL(0xFFFF00);

        public final int color;

        ScanType(int color) {
            this.color = color;
        }
    }

    private static final Map<Integer, ScanType> SCANNED_ENTITIES = new ConcurrentHashMap<>();
    private static long expireTimeMillis = 0;

    public static void update(Map<Integer, ScanType> data, int glowDurationSeconds) {
        SCANNED_ENTITIES.clear();
        SCANNED_ENTITIES.putAll(data);
        expireTimeMillis = System.currentTimeMillis() + glowDurationSeconds * 1000L;
    }

    public static void clear() {
        SCANNED_ENTITIES.clear();
        expireTimeMillis = 0;
    }

    public static boolean isScanned(int entityId) {
        if (System.currentTimeMillis() > expireTimeMillis) {
            SCANNED_ENTITIES.clear();
            return false;
        }
        return SCANNED_ENTITIES.containsKey(entityId);
    }

    public static int getColor(int entityId) {
        ScanType type = SCANNED_ENTITIES.get(entityId);
        return type != null ? type.color : 0xFFFFFF;
    }
}
