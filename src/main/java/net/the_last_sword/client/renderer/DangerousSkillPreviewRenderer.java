package net.the_last_sword.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.network.DangerousSkillPreviewPacket;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DangerousSkillPreviewRenderer {
    private static final Map<UUID, DangerousSkillPreviewPacket> PREVIEWS = new HashMap<>();
    private static ClientLevel previewLevel;
    private static final RenderType FILL = RenderType.create("dangerous_skill_preview",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false));

    private DangerousSkillPreviewRenderer() {
    }

    public static void receive(DangerousSkillPreviewPacket packet) {
        tick();
        if (previewLevel == null || !previewLevel.dimension().location().equals(packet.dimension())) {
            return;
        }
        if (packet.active()) {
            PREVIEWS.put(packet.entityUuid(), packet);
        } else {
            PREVIEWS.remove(packet.entityUuid());
        }
    }

    public static void clear() {
        PREVIEWS.clear();
        previewLevel = null;
    }

    public static void tick() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != previewLevel) {
            clear();
            previewLevel = level;
        }
        if (level != null) {
            PREVIEWS.values().removeIf(packet -> {
                Entity entity = level.getEntity(packet.entityId());
                return level.getGameTime() >= packet.endTick() || entity == null || !entity.isAlive()
                        || !entity.getUUID().equals(packet.entityUuid());
            });
        }
    }

    public static void render(PoseStack poseStack, Camera camera, float partialTick) {
        tick();
        if (previewLevel == null || PREVIEWS.isEmpty()
                || !TheLastSwordConfiguration.getEntityDangerousSkillRangePreviewEnabledSafely()) {
            return;
        }
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        float alpha = 0.65F + 0.2F * (float) Math.sin((previewLevel.getGameTime() + partialTick) * 0.4);
        for (DangerousSkillPreviewPacket packet : PREVIEWS.values()) {
            poseStack.pushPose();
            Vec3 offset = packet.origin().subtract(camera.getPosition());
            poseStack.translate(offset.x, offset.y, offset.z);
            Matrix4f matrix = poseStack.last().pose();
            Vec3 side = new Vec3(-packet.forward().z, 0.0, packet.forward().x).scale(packet.width() * 0.5);
            Vec3 end = packet.forward().scale(packet.length());
            Vec3[] corners = {side, side.add(end), end.subtract(side), side.scale(-1.0)};
            VertexConsumer fill = buffers.getBuffer(FILL);
            for (Vec3 corner : corners) {
                // 略微抬高底面，避免与平地表面深度冲突。
                fill.vertex(matrix, (float) corner.x, 0.025F, (float) corner.z)
                        .color(1.0F, 0.1F, 0.1F, alpha * 0.2F).endVertex();
            }
            buffers.endBatch(FILL);
            VertexConsumer lines = buffers.getBuffer(RenderType.lines());
            Vec3 up = new Vec3(0.0, packet.height(), 0.0);
            for (int index = 0; index < 4; index++) {
                Vec3 start = corners[index];
                Vec3 next = corners[(index + 1) % 4];
                line(lines, poseStack.last(), start, next, alpha);
                line(lines, poseStack.last(), start.add(up), next.add(up), alpha);
                line(lines, poseStack.last(), start, start.add(up), alpha);
            }
            buffers.endBatch(RenderType.lines());
            poseStack.popPose();
        }
    }

    private static void line(VertexConsumer buffer, PoseStack.Pose pose, Vec3 start, Vec3 end, float alpha) {
        Matrix4f matrix = pose.pose();
        Vec3 normal = end.subtract(start).normalize();
        buffer.vertex(matrix, (float) start.x, (float) start.y, (float) start.z)
                .color(1.0F, 0.1F, 0.1F, alpha)
                .normal(pose.normal(), (float) normal.x, (float) normal.y, (float) normal.z).endVertex();
        buffer.vertex(matrix, (float) end.x, (float) end.y, (float) end.z)
                .color(1.0F, 0.1F, 0.1F, alpha)
                .normal(pose.normal(), (float) normal.x, (float) normal.y, (float) normal.z).endVertex();
    }
}
