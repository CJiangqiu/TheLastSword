package net.the_last_sword.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Random;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class LightningSpearRenderUtil {
    private static final float HALF_LENGTH = 1.875F;
    private static final int STRAND_COUNT = 6;
    private static final float GENERATION_TICKS = 6.0F;
    private static final int CACHE_GENERATIONS = 64;
    private static final int MAX_ARC_POINTS = 33;
    private static final float[] STRAND_DURATIONS = new float[STRAND_COUNT];
    private static final float[] STRAND_PHASES = new float[STRAND_COUNT];
    private static final Vector3f[] SPINE = new Vector3f[9];
    private static final CachedStrand[][] STRAND_CACHE = new CachedStrand[STRAND_COUNT][CACHE_GENERATIONS];
    // 渲染回调在渲染线程串行提交顶点，临时坐标可在各条电弧之间复用。
    private static final Vector3f[] TRANSFORMED = new Vector3f[MAX_ARC_POINTS];
    private static final Vector3f[] SIDES = new Vector3f[MAX_ARC_POINTS];
    private static final Vector3f SCALE_VECTOR = new Vector3f();
    private static final RenderType ARC_RENDER_TYPE = RenderType.create(
            "the_last_sword_lightning_spear",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 65536, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    // 光晕与核心需要叠加，不能让透明边缘遮挡后续电弧。
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false));

    static {
        for (int strand = 0; strand < STRAND_COUNT; strand++) {
            Random timing = new Random(0x53A9BL + strand * 7919L);
            STRAND_DURATIONS[strand] = GENERATION_TICKS + timing.nextFloat() * 3.0F;
            STRAND_PHASES[strand] = timing.nextFloat();
        }
        Random random = new Random(0x71A5B39DL);
        for (int index = 0; index < SPINE.length; index++) {
            float progress = index / (float) (SPINE.length - 1);
            float spread = index == 0 || index == SPINE.length - 1 ? 0.0F : 0.065F;
            SPINE[index] = new Vector3f(signed(random) * spread,
                    Mth.lerp(progress, -HALF_LENGTH, HALF_LENGTH), signed(random) * spread);
        }
        for (int index = 0; index < MAX_ARC_POINTS; index++) {
            TRANSFORMED[index] = new Vector3f();
            SIDES[index] = new Vector3f();
        }
    }

    private LightningSpearRenderUtil() {
    }

    public static void render(PoseStack poseStack, MultiBufferSource bufferSource, float age) {
        VertexConsumer consumer = bufferSource.getBuffer(ARC_RENDER_TYPE);
        Matrix4f matrix = poseStack.last().pose();
        float scale = matrix.transformDirection(SCALE_VECTOR.set(1.0F, 0.0F, 0.0F)).length();
        for (int strand = 0; strand < STRAND_COUNT; strand++) {
            float time = age / STRAND_DURATIONS[strand] + STRAND_PHASES[strand];
            int generation = Mth.floor(time);
            float phase = time - generation;
            float fade = Mth.clamp(phase / 0.7F, 0.0F, 1.0F);
            float incoming = fade * fade * (3.0F - 2.0F * fade);
            renderStrand(matrix, consumer, generation, strand, incoming, scale);
            if (incoming < 1.0F) {
                renderStrand(matrix, consumer, generation - 1, strand, 1.0F - incoming, scale);
            }
        }
    }

    private static void renderStrand(Matrix4f matrix, VertexConsumer consumer, int generation, int strand,
                                     float alpha, float scale) {
        if (alpha <= 0.0F) {
            return;
        }
        int slot = Math.floorMod(generation, CACHE_GENERATIONS);
        CachedStrand cached = STRAND_CACHE[strand][slot];
        // 校验代数，避免不同年龄的投射物在环形槽位冲突时复用错误形状。
        if (cached == null || cached.generation() != generation) {
            cached = createStrand(generation, strand);
            STRAND_CACHE[strand][slot] = cached;
        }
        float brightness = alpha * cached.brightness();
        renderArc(matrix, consumer, cached.main(), brightness, scale);
        if (cached.branch() != null) {
            renderArc(matrix, consumer, cached.branch(), brightness * 0.7F, scale);
        }
    }

    private static CachedStrand createStrand(int generation, int strand) {
        // 同一代电弧固定折点，只改变亮度，避免随机路径被插值成摆动的波浪。
        Random random = new Random(0x71A5B39DL ^ (generation * 0x9E3779B97F4A7C15L) ^ (strand * 7919L));
        float start = strand == 0 ? 0.0F : random.nextFloat() * 0.32F;
        float end = strand == 0 ? 1.0F : 0.68F + random.nextFloat() * 0.32F;
        int segments = 20 + random.nextInt(13);
        Vector3f[] points = new Vector3f[segments + 1];
        float[] widths = new float[points.length];
        float baseWidth = strand == 0 ? 0.012F : 0.004F + random.nextFloat() * 0.005F;
        for (int index = 0; index <= segments; index++) {
            float progress = index / (float) segments;
            float jitter = index == 0 || index == segments ? 0.0F : signed(random) * 0.3F / segments;
            float position = Mth.lerp(progress + jitter, start, end);
            float spinePosition = position * (SPINE.length - 1);
            int anchor = Math.min((int) spinePosition, SPINE.length - 2);
            Vector3f point = new Vector3f(SPINE[anchor]).lerp(SPINE[anchor + 1], spinePosition - anchor);
            float taper = Math.min(1.0F, Math.min(progress, 1.0F - progress) * 9.0F);
            float spread = (strand == 0 ? 0.018F : 0.045F) * taper;
            point.add(signed(random) * spread, 0.0F, signed(random) * spread);
            points[index] = point;
            widths[index] = baseWidth * (0.55F + random.nextFloat() * 0.65F) * taper;
        }
        float brightness = 0.65F + random.nextFloat() * 0.35F;
        Arc main = new Arc(points, widths);
        Arc branch = strand < 4 ? createBranch(random, points, baseWidth) : null;
        return new CachedStrand(generation, main, branch, brightness);
    }

    private static Arc createBranch(Random random, Vector3f[] parent, float width) {
        Vector3f origin = parent[3 + random.nextInt(parent.length - 6)];
        int segments = 4 + random.nextInt(4);
        Vector3f[] points = new Vector3f[segments + 1];
        float[] widths = new float[points.length];
        float direction = random.nextFloat() * Mth.TWO_PI;
        float reach = 0.08F + random.nextFloat() * 0.16F;
        float rise = signed(random) * 0.32F;
        for (int index = 0; index <= segments; index++) {
            float progress = index / (float) segments;
            float jitter = index == 0 || index == segments ? 0.0F : 0.025F;
            points[index] = new Vector3f(origin).add(
                    Mth.cos(direction) * reach * progress + signed(random) * jitter,
                    rise * progress,
                    Mth.sin(direction) * reach * progress + signed(random) * jitter);
            widths[index] = width * 0.55F * (1.0F - progress);
        }
        return new Arc(points, widths);
    }

    private static float signed(Random random) {
        return random.nextFloat() * 2.0F - 1.0F;
    }

    public static BurstGeometry createBurst(float radius, long seed) {
        Random random = new Random(seed);
        Arc[] arcs = new Arc[24];
        for (int ray = 0; ray < 18; ray++) {
            // 六个面各分配三条电弧，保证特效覆盖立方体各侧，而非缩在内接球中。
            int face = ray / 3;
            float edge = face % 2 == 0 ? radius : -radius;
            float offsetA = ray % 3 == 0 ? 0.0F : signed(random) * radius;
            float offsetB = ray % 3 == 0 ? 0.0F : signed(random) * radius;
            Vector3f direction = switch (face / 2) {
                case 0 -> new Vector3f(edge, offsetA, offsetB);
                case 1 -> new Vector3f(offsetA, edge, offsetB);
                default -> new Vector3f(offsetA, offsetB, edge);
            };
            Vector3f[] points = new Vector3f[9];
            float[] widths = new float[points.length];
            for (int index = 0; index < points.length; index++) {
                float progress = index / (float) (points.length - 1);
                float jitter = index == 0 || index == points.length - 1 ? 0.0F : radius * 0.07F;
                points[index] = new Vector3f(direction).mul(progress).add(
                        signed(random) * jitter, signed(random) * jitter, signed(random) * jitter);
                points[index].set(Mth.clamp(points[index].x, -radius, radius),
                        Mth.clamp(points[index].y, -radius, radius),
                        Mth.clamp(points[index].z, -radius, radius));
                widths[index] = 0.012F * (1.0F - progress);
            }
            arcs[ray] = new Arc(points, widths);
            if (ray < 6) {
                arcs[18 + ray] = createBranch(random, points, 0.014F);
            }
        }
        return new BurstGeometry(arcs);
    }

    public static void renderBurst(PoseStack poseStack, MultiBufferSource bufferSource,
                                   BurstGeometry geometry, float alpha) {
        Matrix4f matrix = poseStack.last().pose();
        float scale = matrix.transformDirection(SCALE_VECTOR.set(1.0F, 0.0F, 0.0F)).length();
        VertexConsumer consumer = bufferSource.getBuffer(ARC_RENDER_TYPE);
        for (Arc arc : geometry.arcs) {
            renderArc(matrix, consumer, arc, alpha, scale);
        }
    }

    public static void finishBurstBatch(MultiBufferSource.BufferSource buffers) {
        buffers.endBatch(ARC_RENDER_TYPE);
    }

    public static final class BurstGeometry {
        private final Arc[] arcs;

        private BurstGeometry(Arc[] arcs) {
            this.arcs = arcs;
        }
    }

    private static void renderArc(Matrix4f matrix, VertexConsumer consumer, Arc arc, float alpha, float scale) {
        Vector3f[] points = arc.points();
        float[] widths = arc.widths();
        Vector3f[] transformed = TRANSFORMED;
        Vector3f[] sides = SIDES;
        for (int index = 0; index < points.length; index++) {
            matrix.transformPosition(points[index], transformed[index]);
        }
        // 在显示空间展开细线，避免交叉面片暴露厚度，折点共享边缘以减少接缝。
        for (int index = 0; index < points.length; index++) {
            Vector3f before = transformed[Math.max(0, index - 1)];
            Vector3f after = transformed[Math.min(points.length - 1, index + 1)];
            Vector3f side = sides[index].set(-(after.y - before.y), after.x - before.x, 0.0F);
            if (side.lengthSquared() < 0.00000001F) {
                side.set(1.0F, 0.0F, 0.0F);
            }
            sides[index] = side.normalize().mul(widths[index] * scale);
        }
        renderBand(consumer, transformed, sides, points.length, 8.0F, 1.0F, 0.94F, 0.65F, alpha * 0.12F);
        renderBand(consumer, transformed, sides, points.length, 3.0F, 1.0F, 0.98F, 0.84F, alpha * 0.4F);
        renderBand(consumer, transformed, sides, points.length, 1.0F, 1.0F, 1.0F, 0.98F, alpha);
    }

    private static void renderBand(VertexConsumer consumer, Vector3f[] points, Vector3f[] sides, int pointCount,
                                   float width, float red, float green, float blue, float alpha) {
        for (int index = 1; index < pointCount; index++) {
            Vector3f start = points[index - 1];
            Vector3f end = points[index];
            Vector3f startSide = sides[index - 1];
            Vector3f endSide = sides[index];
            for (int sign = -1; sign <= 1; sign += 2) {
                vertex(consumer, start, startSide, 0.0F, red, green, blue, alpha);
                vertex(consumer, start, startSide, sign * width, red, green, blue, 0.0F);
                vertex(consumer, end, endSide, sign * width, red, green, blue, 0.0F);
                vertex(consumer, end, endSide, 0.0F, red, green, blue, alpha);
            }
        }
    }

    private static void vertex(VertexConsumer consumer, Vector3f point, Vector3f side, float offset,
                                float red, float green, float blue, float alpha) {
        consumer.vertex(point.x + side.x * offset, point.y + side.y * offset, point.z)
                .color(red, green, blue, alpha).endVertex();
    }

    private record Arc(Vector3f[] points, float[] widths) {
    }

    private record CachedStrand(int generation, Arc main, Arc branch, float brightness) {
    }
}
