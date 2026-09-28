package net.the_last_sword.util;

import net.minecraft.world.phys.Vec3;

public final class QueenSummonRiftPlacement {
    // 以举剑姿态在 Y+5 平面的交点为基准，前方偏移放大 25%，供裂缝与出生点共用。
    public static final Vec3 LOCAL_CENTER = new Vec3(-0.054, 5.0, 0.75125);

    private QueenSummonRiftPlacement() {}

    public static Vec3 worldCenter(Vec3 origin, float yaw) {
        double radians = Math.toRadians(yaw);
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);
        return origin.add(LOCAL_CENTER.x * cos - LOCAL_CENTER.z * sin, LOCAL_CENTER.y,
                LOCAL_CENTER.x * sin + LOCAL_CENTER.z * cos);
    }
}
