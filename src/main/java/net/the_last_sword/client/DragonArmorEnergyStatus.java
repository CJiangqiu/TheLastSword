package net.the_last_sword.client;

// 客户端缓存的龙甲整套能量状态
public final class DragonArmorEnergyStatus {
    private static volatile long currentEnergy;
    private static volatile long maxEnergy;
    private static volatile long consumptionPerTick;

    private DragonArmorEnergyStatus() {
    }

    public static void update(long current, long max, long consumption) {
        currentEnergy = Math.max(0L, current);
        maxEnergy = Math.max(0L, max);
        consumptionPerTick = Math.max(0L, consumption);
    }

    public static double getEnergyPercentage() {
        return maxEnergy <= 0L ? 0.0 : Math.min(100.0, currentEnergy * 100.0 / maxEnergy);
    }

    public static long getConsumptionPerTick() {
        return consumptionPerTick;
    }
}
