package net.the_last_sword.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.network.LostWraithEndStrikeEffectPacket;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class LostWraithEndStrikeEffectRenderer {
    private static final float HALF_SQRT_THREE = (float) (Math.sqrt(3.0) / 2.0);
    private static final int MAX_RAY_COUNT = 32;
    private static final Map<UUID, LostWraithEndStrikeEffectPacket> EFFECTS = new HashMap<>();
    private static ClientLevel effectLevel;

    private LostWraithEndStrikeEffectRenderer() {
    }

    public static void receive(LostWraithEndStrikeEffectPacket packet) {
        tick();
        if (effectLevel == null || !effectLevel.dimension().location().equals(packet.dimension())) {
            return;
        }
        if (packet.active()) {
            EFFECTS.put(packet.entityUuid(), packet);
        } else {
            EFFECTS.remove(packet.entityUuid());
        }
    }

    public static void clear() {
        EFFECTS.clear();
        effectLevel = null;
    }

    public static void tick() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != effectLevel) {
            clear();
            effectLevel = level;
        }
        if (level != null) {
            EFFECTS.values().removeIf(packet -> {
                Entity entity = level.getEntity(packet.entityId());
                return level.getGameTime() >= packet.endTick() || entity == null || !entity.isAlive()
                        || !entity.getUUID().equals(packet.entityUuid());
            });
        }
    }

    public static void render(PoseStack poseStack, Camera camera, float partialTick) {
        tick();
        if (effectLevel == null || EFFECTS.isEmpty()) {
            return;
        }

        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer rays = buffers.getBuffer(RenderType.lightning());
        double renderTick = effectLevel.getGameTime() + partialTick;
        for (LostWraithEndStrikeEffectPacket packet : EFFECTS.values()) {
            renderEffect(poseStack, camera, rays, packet, renderTick);
        }
        buffers.endBatch(RenderType.lightning());
    }

    private static void renderEffect(PoseStack poseStack, Camera camera, VertexConsumer rays,
                                     LostWraithEndStrikeEffectPacket packet, double renderTick) {
        float charge = progress(renderTick, packet.startTick(), packet.damageTick());
        float fade = progress(renderTick, packet.damageTick(), packet.endTick());
        float visibility = 1.0F - fade;
        if (visibility <= 0.0F) {
            return;
        }

        int rayCount = Math.max(1, Mth.ceil((charge + charge * charge) * 0.5F * MAX_RAY_COUNT));
        float size = (0.45F + charge * 0.55F) * visibility;
        int centerAlpha = Mth.clamp((int) ((0.4F + charge * 0.6F) * visibility * 255.0F), 0, 255);
        RandomSource random = RandomSource.create(432L ^ packet.entityUuid().getMostSignificantBits()
                ^ packet.entityUuid().getLeastSignificantBits());

        poseStack.pushPose();
        Vec3 offset = packet.position().subtract(camera.getPosition());
        poseStack.translate(offset.x, offset.y, offset.z);
        for (int index = 0; index < rayCount; index++) {
            poseStack.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0F
                    + (float) (renderTick - packet.startTick()) * 4.5F));
            float length = (random.nextFloat() * 2.4F + 1.2F) * size;
            float radius = (random.nextFloat() * 0.5F + 0.25F) * size;
            Matrix4f matrix = poseStack.last().pose();
            centerVertex(rays, matrix, centerAlpha);
            outerVertexLeft(rays, matrix, length, radius);
            outerVertexRight(rays, matrix, length, radius);
            centerVertex(rays, matrix, centerAlpha);
            outerVertexRight(rays, matrix, length, radius);
            outerVertexBack(rays, matrix, length, radius);
            centerVertex(rays, matrix, centerAlpha);
            outerVertexBack(rays, matrix, length, radius);
            outerVertexLeft(rays, matrix, length, radius);
        }
        poseStack.popPose();
    }

    private static float progress(double tick, long startTick, long endTick) {
        if (endTick <= startTick) {
            return tick >= endTick ? 1.0F : 0.0F;
        }
        return Mth.clamp((float) ((tick - startTick) / (endTick - startTick)), 0.0F, 1.0F);
    }

    private static void centerVertex(VertexConsumer buffer, Matrix4f matrix, int alpha) {
        buffer.vertex(matrix, 0.0F, 0.0F, 0.0F).color(255, 255, 255, alpha).endVertex();
    }

    private static void outerVertexLeft(VertexConsumer buffer, Matrix4f matrix, float length, float radius) {
        buffer.vertex(matrix, -HALF_SQRT_THREE * radius, length, -0.5F * radius)
                .color(255, 0, 255, 0).endVertex();
    }

    private static void outerVertexRight(VertexConsumer buffer, Matrix4f matrix, float length, float radius) {
        buffer.vertex(matrix, HALF_SQRT_THREE * radius, length, -0.5F * radius)
                .color(255, 0, 255, 0).endVertex();
    }

    private static void outerVertexBack(VertexConsumer buffer, Matrix4f matrix, float length, float radius) {
        buffer.vertex(matrix, 0.0F, length, radius).color(255, 0, 255, 0).endVertex();
    }
}
