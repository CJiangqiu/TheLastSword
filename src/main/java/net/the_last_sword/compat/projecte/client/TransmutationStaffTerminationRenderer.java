package net.the_last_sword.compat.projecte.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.compat.projecte.TransmutationStaffBehavior;
import net.the_last_sword.compat.projecte.TransmutationStaffField;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.compat.projecte.TransmutationStaffTerminationPacket;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = TheLastSwordMod.MOD_ID, value = Dist.CLIENT)
public final class TransmutationStaffTerminationRenderer {
    private static final int FADE_TICKS = TransmutationStaffField.FADE_TICKS;
    private static final List<Release> RELEASES = new ArrayList<>();
    private static final List<Vec3> SPHERE = createSphere();
    private static final List<SurfaceGlyph> SURFACE_GLYPHS = createSurfaceGlyphs();
    private static final int GLYPH_SEGMENTS = 8;
    private static ClientLevel effectLevel;

    private TransmutationStaffTerminationRenderer() {
    }

    public static void receive(TransmutationStaffTerminationPacket packet) {
        update();
        if (effectLevel == null || !effectLevel.dimension().location().equals(packet.dimension())) {
            return;
        }
        if (RELEASES.size() >= 64) {
            RELEASES.remove(0);
        }
        RELEASES.add(new Release(packet.position(), effectLevel.getGameTime(), packet.radius()));
    }

    private static void update() {
        ClientLevel level = Minecraft.getInstance().level;
        if (effectLevel != level) {
            RELEASES.clear();
            effectLevel = level;
        }
        if (level != null) {
            RELEASES.removeIf(release -> level.getGameTime() - release.tick() >= FADE_TICKS);
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            update();
        }
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        update();
        if (effectLevel == null) {
            return;
        }
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        float partialTick = event.getPartialTick();
        for (Player player : effectLevel.players()) {
            ItemStack stack = player.getUseItem();
            if (!player.isAlive() || !player.isUsingItem() || !TransmutationStaffBehavior.isStaff(stack)
                || !TransmutationStaffBehavior.isTermination(stack)) {
                continue;
            }
            float progress = Mth.clamp((player.getTicksUsingItem() + partialTick)
                / TransmutationStaffBehavior.getTerminationUseTicks(), 0, 1);
            Vec3 center = new Vec3(Mth.lerp(partialTick, player.xo, player.getX()),
                Mth.lerp(partialTick, player.yo, player.getY()), Mth.lerp(partialTick, player.zo, player.getZ()));
            renderField(event, buffers, center, progress, 1.0F,
                (float) TheLastSwordConfiguration.getTransmutationStaffTerminationRadiusSafely());
        }
        for (Release release : RELEASES) {
            float age = effectLevel.getGameTime() - release.tick() + partialTick;
            renderField(event, buffers, release.position(), 1.0F, 1.0F - Mth.clamp(age / FADE_TICKS, 0, 1), release.radius());
        }
        buffers.endBatch(FieldRenderTypes.CIRCLE);
        buffers.endBatch(FieldRenderTypes.SPHERE);
    }

    private static void renderField(RenderLevelStageEvent event, MultiBufferSource buffers,
                                    Vec3 center, float progress, float opacity, float maximumRadius) {
        float radius = maximumRadius * progress;
        if (radius <= 0 || opacity <= 0) {
            return;
        }
        PoseStack poses = event.getPoseStack();
        Vec3 offset = center.subtract(event.getCamera().getPosition());
        poses.pushPose();
        poses.translate(offset.x, offset.y, offset.z);
        poses.pushPose();
        poses.translate(0, 0.04, 0);
        poses.mulPose(Axis.YP.rotationDegrees((effectLevel.getGameTime() % 360) + event.getPartialTick()));
        Matrix4f circleMatrix = poses.last().pose();
        VertexConsumer circle = buffers.getBuffer(FieldRenderTypes.CIRCLE);
        int alpha = (int) (220 * opacity);
        circle.vertex(circleMatrix, -radius, 0, -radius).color(155, 12, 40, alpha).uv(0, 0).endVertex();
        circle.vertex(circleMatrix, -radius, 0, radius).color(155, 12, 40, alpha).uv(0, 1).endVertex();
        circle.vertex(circleMatrix, radius, 0, radius).color(155, 12, 40, alpha).uv(1, 1).endVertex();
        circle.vertex(circleMatrix, radius, 0, -radius).color(155, 12, 40, alpha).uv(1, 0).endVertex();
        poses.popPose();
        poses.scale(radius, radius, radius);
        Matrix4f sphereMatrix = poses.last().pose();
        VertexConsumer sphere = buffers.getBuffer(FieldRenderTypes.SPHERE);
        int sphereAlpha = (int) ((30 + 25 * progress) * opacity);
        for (Vec3 vertex : SPHERE) {
            sphere.vertex(sphereMatrix, (float) vertex.x, (float) vertex.y, (float) vertex.z)
                .color(135, 35, 225, sphereAlpha).endVertex();
        }
        renderSurfaceGlyphs(sphereMatrix, buffers.getBuffer(FieldRenderTypes.CIRCLE), progress, opacity);
        poses.popPose();
    }

    private static void renderSurfaceGlyphs(Matrix4f matrix, VertexConsumer buffer, float progress, float opacity) {
        for (SurfaceGlyph glyph : SURFACE_GLYPHS) {
            float growth = Mth.clamp((progress - glyph.delay()) / (0.85F - glyph.delay()), 0, 1);
            growth = growth * growth * (3 - 2 * growth);
            int alpha = (int) (230 * opacity * growth);
            if (alpha == 0) {
                continue;
            }
            double size = glyph.size() * growth;
            for (int row = 0; row < GLYPH_SEGMENTS; row++) {
                float v0 = (float) row / GLYPH_SEGMENTS;
                float v1 = (float) (row + 1) / GLYPH_SEGMENTS;
                for (int column = 0; column < GLYPH_SEGMENTS; column++) {
                    float u0 = (float) column / GLYPH_SEGMENTS;
                    float u1 = (float) (column + 1) / GLYPH_SEGMENTS;
                    glyphVertex(matrix, buffer, glyph, size, u0, v0, alpha);
                    glyphVertex(matrix, buffer, glyph, size, u0, v1, alpha);
                    glyphVertex(matrix, buffer, glyph, size, u1, v1, alpha);
                    glyphVertex(matrix, buffer, glyph, size, u1, v0, alpha);
                }
            }
        }
    }

    private static void glyphVertex(Matrix4f matrix, VertexConsumer buffer, SurfaceGlyph glyph,
                                    double size, float u, float v, int alpha) {
        // 将贴图网格投影到球面，避免大法阵的边缘穿入球体。
        Vec3 point = glyph.normal().add(glyph.horizontal().scale((u * 2 - 1) * size))
            .add(glyph.vertical().scale((v * 2 - 1) * size)).normalize().scale(1.005);
        buffer.vertex(matrix, (float) point.x, (float) point.y, (float) point.z)
            .color(155, 12, 40, alpha).uv(u, v).endVertex();
    }

    private static List<SurfaceGlyph> createSurfaceGlyphs() {
        List<SurfaceGlyph> glyphs = new ArrayList<>();
        double angleStep = Math.PI * (3 - Math.sqrt(5));
        for (int index = 0; index < 20; index++) {
            double y = 1 - 2 * (index + 0.5) / 20;
            double ringRadius = Math.sqrt(1 - y * y);
            double angle = index * angleStep;
            Vec3 normal = new Vec3(ringRadius * Math.cos(angle), y, ringRadius * Math.sin(angle));
            Vec3 reference = Math.abs(y) > 0.9 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
            Vec3 horizontal = reference.cross(normal).normalize();
            Vec3 vertical = normal.cross(horizontal).normalize();
            glyphs.add(new SurfaceGlyph(normal, horizontal, vertical,
                0.14 + (index % 4) * 0.045, (index % 5) * 0.055F));
        }
        return List.copyOf(glyphs);
    }

    private static List<Vec3> createSphere() {
        List<Vec3> vertices = new ArrayList<>();
        for (int latitude = 0; latitude < 24; latitude++) {
            double lower = Math.PI * latitude / 24;
            double upper = Math.PI * (latitude + 1) / 24;
            for (int longitude = 0; longitude < 48; longitude++) {
                double left = Math.PI * 2 * longitude / 48;
                double right = Math.PI * 2 * (longitude + 1) / 48;
                vertices.add(point(lower, left));
                vertices.add(point(upper, left));
                vertices.add(point(upper, right));
                vertices.add(point(lower, right));
            }
        }
        return List.copyOf(vertices);
    }

    private static Vec3 point(double latitude, double longitude) {
        return new Vec3(Math.sin(latitude) * Math.cos(longitude), Math.cos(latitude),
            Math.sin(latitude) * Math.sin(longitude));
    }

    private record Release(Vec3 position, long tick, float radius) {
    }

    private record SurfaceGlyph(Vec3 normal, Vec3 horizontal, Vec3 vertical, double size, float delay) {
    }

    private static final class FieldRenderTypes extends RenderType {
        private static final RenderType CIRCLE = create("termination_circle", DefaultVertexFormat.POSITION_COLOR_TEX,
            VertexFormat.Mode.QUADS, 256, false, true, CompositeState.builder()
                .setShaderState(POSITION_COLOR_TEX_SHADER)
                .setTextureState(new TextureStateShard(ResourceLocation.fromNamespaceAndPath(
                    "projecte", "textures/models/yuecircle.png"), false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL)
                .setWriteMaskState(COLOR_WRITE).createCompositeState(false));
        private static final RenderType SPHERE = create("termination_sphere", DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS, 262144, false, true, CompositeState.builder()
                .setShaderState(POSITION_COLOR_SHADER).setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setCullState(NO_CULL).setWriteMaskState(COLOR_WRITE).createCompositeState(false));

        private FieldRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int size,
                                 boolean crumbling, boolean sorting, Runnable setup, Runnable clear) {
            super(name, format, mode, size, crumbling, sorting, setup, clear);
        }
    }
}
