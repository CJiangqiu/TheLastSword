package net.the_last_sword.client;

import net.minecraft.util.Mth;

public final class QueenTripleSlashScreenShake {
    private static final int DURATION_TICKS = 6;
    private static final float YAW_AMPLITUDE = 0.7F;
    private static final float PITCH_AMPLITUDE = 0.45F;
    private static int remainingTicks;

    private QueenTripleSlashScreenShake() {
    }

    public static void start() {
        remainingTicks = DURATION_TICKS;
    }

    public static void tick() {
        if (remainingTicks > 0) {
            --remainingTicks;
        }
    }

    public static float getYawOffset(float partialTick) {
        float progress = elapsed(partialTick);
        return Mth.sin(progress * 3.4F) * YAW_AMPLITUDE * fade(partialTick);
    }

    public static float getPitchOffset(float partialTick) {
        float progress = elapsed(partialTick);
        return Mth.cos(progress * 4.1F) * PITCH_AMPLITUDE * fade(partialTick);
    }

    public static boolean isActive() {
        return remainingTicks > 0;
    }

    public static void clear() {
        remainingTicks = 0;
    }

    private static float elapsed(float partialTick) {
        return DURATION_TICKS - remainingTicks + partialTick;
    }

    private static float fade(float partialTick) {
        return Mth.clamp((remainingTicks - partialTick) / DURATION_TICKS, 0.0F, 1.0F);
    }
}
