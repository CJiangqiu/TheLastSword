package net.the_last_sword.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Pose;

public final class QueenExecutionCamera {
    private static boolean active;
    private static boolean poseCaptured;
    private static Pose previousForcedPose;
    private static float yaw;

    private QueenExecutionCamera() {
    }

    public static void setActive(boolean active, float yaw) {
        if (active) {
            QueenExecutionCamera.yaw = yaw;
            QueenExecutionCamera.active = true;
            applyPose();
        } else {
            clear();
        }
    }

    public static void tick() {
        if (active) {
            applyPose();
        }
    }

    private static void applyPose() {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        if (!poseCaptured) {
            previousForcedPose = player.getForcedPose();
            poseCaptured = true;
        }
        player.setForcedPose(Pose.SLEEPING);
    }

    public static void clear() {
        var player = Minecraft.getInstance().player;
        if (player != null && poseCaptured) {
            player.setForcedPose(previousForcedPose);
        }
        active = false;
        poseCaptured = false;
        previousForcedPose = null;
        yaw = 0.0F;
    }

    public static boolean isActive() {
        return active;
    }

    public static float getYaw() {
        return yaw;
    }
}
