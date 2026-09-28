package net.the_last_sword.configuration;

public record LightningSpearSettings(int cooldownTicks, int chargeTicks, double range, double burstSize,
                                     double damage, int slowLevel, int slowTicks) {
}
