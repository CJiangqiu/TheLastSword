package net.the_last_sword.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.the_last_sword.configuration.DefenceConfig;
import net.the_last_sword.configuration.DefenceConfigData;
import net.the_last_sword.item.DragonArmorItem;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class DragonShieldRenderer {

    private static final float SPHERE_RADIUS = 1.8f;
    private static final float SHELL_ALPHA = 0.10f;
    private static final float ENERGY_ALPHA = 0.18f;
    private static final float ENERGY_WIDTH = 0.045f;
    private static final int SHELL_LATITUDE_SEGMENTS = 18;
    private static final int SHELL_LONGITUDE_SEGMENTS = 36;
    private static final int ENERGY_BANDS = 3;
    private static final int ENERGY_SEGMENTS = 72;
    private static final int IMPACT_DURATION_TICKS = 28;
    private static final int MAX_IMPACTS = 18;
    private static final int GEODESIC_FREQUENCY = 5;
    private static final float SHIELD_CELL_EDGE_WIDTH = 0.022f;
    private static final float SHIELD_CELL_SURFACE_OFFSET = 0.018f;
    private static final float IMPACT_BASE_ANGLE = 0.22f;
    private static final float IMPACT_MAX_ANGLE = 0.62f;
    private static final int RECENT_HIT_WINDOW_TICKS = 10;

    private static final RenderType SHIELD_RENDER_TYPE = RenderType.create(
            "the_last_sword_dragon_shield",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            2097152,
            true,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false)
    );

    private static final List<Impact> impacts = new ArrayList<>();
    private static final List<ShieldCell> shieldCells = new ArrayList<>();
    private static boolean cellsBuilt = false;

    public static void trigger(float directionX, float directionY, float directionZ) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Vector3f normal = new Vector3f(directionX, directionY, directionZ);
        if (normal.lengthSquared() < 0.0001f) return;
        normal.normalize();

        impacts.add(new Impact(normal, mc.level.getGameTime()));
        while (impacts.size() > MAX_IMPACTS) {
            impacts.remove(0);
        }
    }

    public static void render(PoseStack poseStack, MultiBufferSource buffer, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        DefenceConfigData.ShieldEffectMode mode = DefenceConfig.getPhasingModule().shieldEffect;
        if (mode == DefenceConfigData.ShieldEffectMode.DISABLED) return;
        if (!DragonArmorItem.isFullSet(player) || !DragonArmorItem.hasEnergyFullSet(player)) return;

        double x = Mth.lerp(partialTicks, player.xo, player.getX());
        double y = Mth.lerp(partialTicks, player.yo, player.getY()) + player.getBbHeight() / 2;
        double z = Mth.lerp(partialTicks, player.zo, player.getZ());

        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        poseStack.pushPose();
        poseStack.translate(x - cam.x, y - cam.y, z - cam.z);

        VertexConsumer vc = buffer.getBuffer(SHIELD_RENDER_TYPE);
        Matrix4f matrix = poseStack.last().pose();
        long gameTime = mc.level.getGameTime();
        float time = gameTime + partialTicks;

        buildShieldCells();
        renderTransparentShell(vc, matrix, 0x7A, 0x00, 0xFF, (int) (SHELL_ALPHA * 255));
        renderEnergyBands(vc, matrix, time);
        renderActivatedShieldCells(vc, matrix, gameTime, partialTicks);

        poseStack.popPose();
    }

    private static void renderTransparentShell(VertexConsumer vc, Matrix4f matrix, int r, int g, int b, int a) {
        for (int lat = 0; lat < SHELL_LATITUDE_SEGMENTS; lat++) {
            float theta1 = (float) (-Math.PI / 2.0 + Math.PI * lat / SHELL_LATITUDE_SEGMENTS);
            float theta2 = (float) (-Math.PI / 2.0 + Math.PI * (lat + 1) / SHELL_LATITUDE_SEGMENTS);

            for (int lon = 0; lon < SHELL_LONGITUDE_SEGMENTS; lon++) {
                float phi1 = (float) (Math.PI * 2.0 * lon / SHELL_LONGITUDE_SEGMENTS);
                float phi2 = (float) (Math.PI * 2.0 * (lon + 1) / SHELL_LONGITUDE_SEGMENTS);

                Vector3f p1 = spherePoint(theta1, phi1, SPHERE_RADIUS);
                Vector3f p2 = spherePoint(theta1, phi2, SPHERE_RADIUS);
                Vector3f p3 = spherePoint(theta2, phi2, SPHERE_RADIUS);
                Vector3f p4 = spherePoint(theta2, phi1, SPHERE_RADIUS);
                quad(vc, matrix, p1, p2, p3, p4, r, g, b, a);
            }
        }
    }

    private static void renderEnergyBands(VertexConsumer vc, Matrix4f matrix, float time) {
        float rotation = time * 0.012f;
        renderEnergyBand(vc, matrix, rotatedNormal(0.0f, 1.0f, 0.0f, rotation), time, 0);
        renderEnergyBand(vc, matrix, rotatedNormal(0.72f, 0.0f, 0.69f, rotation), time, 1);
        renderEnergyBand(vc, matrix, rotatedNormal(-0.72f, 0.0f, 0.69f, rotation), time, 2);
    }

    private static void renderEnergyBand(VertexConsumer vc, Matrix4f matrix, Vector3f normal, float time, int index) {
        Vector3f axisA = Math.abs(normal.y) < 0.95f
                ? new Vector3f(0, 1, 0).cross(normal).normalize()
                : new Vector3f(1, 0, 0).cross(normal).normalize();
        Vector3f axisB = new Vector3f(normal).cross(axisA).normalize();

        int alpha = (int) ((ENERGY_ALPHA * (0.72f + 0.28f * (float) Math.sin(time * 0.035f + index * 1.7f))) * 255);
        renderGreatCircleBand(vc, matrix, axisA, axisB, normal, ENERGY_WIDTH, 0xB0, 0x35, 0xFF, alpha);
    }

    private static Vector3f rotatedNormal(float x, float y, float z, float rotation) {
        float cos = (float) Math.cos(rotation);
        float sin = (float) Math.sin(rotation);
        return new Vector3f(
                x * cos - z * sin,
                y,
                x * sin + z * cos
        ).normalize();
    }

    private static void renderGreatCircleBand(VertexConsumer vc, Matrix4f matrix, Vector3f axisA, Vector3f axisB,
                                              Vector3f normal, float width, int r, int g, int b, int a) {
        for (int segment = 0; segment < ENERGY_SEGMENTS; segment++) {
            float t1 = (float) (Math.PI * 2.0 * segment / ENERGY_SEGMENTS);
            float t2 = (float) (Math.PI * 2.0 * (segment + 1) / ENERGY_SEGMENTS);

            Vector3f c1 = circlePoint(axisA, axisB, t1);
            Vector3f c2 = circlePoint(axisA, axisB, t2);
            Vector3f p1 = projectToRadius(new Vector3f(c1).add(new Vector3f(normal).mul(width)), SPHERE_RADIUS + 0.008f);
            Vector3f p2 = projectToRadius(new Vector3f(c2).add(new Vector3f(normal).mul(width)), SPHERE_RADIUS + 0.008f);
            Vector3f p3 = projectToRadius(new Vector3f(c2).sub(new Vector3f(normal).mul(width)), SPHERE_RADIUS + 0.008f);
            Vector3f p4 = projectToRadius(new Vector3f(c1).sub(new Vector3f(normal).mul(width)), SPHERE_RADIUS + 0.008f);

            quad(vc, matrix, p1, p2, p3, p4, r, g, b, a);
        }
    }

    private static Vector3f circlePoint(Vector3f axisA, Vector3f axisB, float angle) {
        return new Vector3f(axisA).mul((float) Math.cos(angle))
                .add(new Vector3f(axisB).mul((float) Math.sin(angle)))
                .normalize();
    }

    private static void buildShieldCells() {
        if (cellsBuilt) return;
        cellsBuilt = true;
        shieldCells.clear();

        float t = (float) ((1.0 + Math.sqrt(5.0)) / 2.0);
        Vector3f[] icoVerts = {
                new Vector3f(-1, t, 0), new Vector3f(1, t, 0), new Vector3f(-1, -t, 0), new Vector3f(1, -t, 0),
                new Vector3f(0, -1, t), new Vector3f(0, 1, t), new Vector3f(0, -1, -t), new Vector3f(0, 1, -t),
                new Vector3f(t, 0, -1), new Vector3f(t, 0, 1), new Vector3f(-t, 0, -1), new Vector3f(-t, 0, 1)
        };
        for (Vector3f vertex : icoVerts) {
            vertex.normalize();
        }

        int[][] icoFaces = {
                {0, 11, 5}, {0, 5, 1}, {0, 1, 7}, {0, 7, 10}, {0, 10, 11},
                {1, 5, 9}, {5, 11, 4}, {11, 10, 2}, {10, 7, 6}, {7, 1, 8},
                {3, 9, 4}, {3, 4, 2}, {3, 2, 6}, {3, 6, 8}, {3, 8, 9},
                {4, 9, 5}, {2, 4, 11}, {6, 2, 10}, {8, 6, 7}, {9, 8, 1}
        };

        List<Vector3f> vertices = new ArrayList<>();
        List<int[]> triangles = new ArrayList<>();
        Map<String, Integer> vertexIndex = new HashMap<>();

        for (int[] face : icoFaces) {
            Vector3f a = icoVerts[face[0]];
            Vector3f b = icoVerts[face[1]];
            Vector3f c = icoVerts[face[2]];
            int[][] grid = new int[GEODESIC_FREQUENCY + 1][GEODESIC_FREQUENCY + 1];

            for (int i = 0; i <= GEODESIC_FREQUENCY; i++) {
                for (int j = 0; j <= GEODESIC_FREQUENCY - i; j++) {
                    float wa = (GEODESIC_FREQUENCY - i - j) / (float) GEODESIC_FREQUENCY;
                    float wb = i / (float) GEODESIC_FREQUENCY;
                    float wc = j / (float) GEODESIC_FREQUENCY;
                    Vector3f point = new Vector3f(a).mul(wa)
                            .add(new Vector3f(b).mul(wb))
                            .add(new Vector3f(c).mul(wc))
                            .normalize();
                    String key = geodesicKey(point);
                    grid[i][j] = vertexIndex.computeIfAbsent(key, ignored -> {
                        vertices.add(point);
                        return vertices.size() - 1;
                    });
                }
            }

            for (int i = 0; i < GEODESIC_FREQUENCY; i++) {
                for (int j = 0; j < GEODESIC_FREQUENCY - i; j++) {
                    triangles.add(new int[]{grid[i][j], grid[i + 1][j], grid[i][j + 1]});
                    if (j < GEODESIC_FREQUENCY - i - 1) {
                        triangles.add(new int[]{grid[i + 1][j], grid[i + 1][j + 1], grid[i][j + 1]});
                    }
                }
            }
        }

        Vector3f[] triangleCenters = new Vector3f[triangles.size()];
        List<Integer>[] vertexTriangles = new List[vertices.size()];
        for (int i = 0; i < vertexTriangles.length; i++) {
            vertexTriangles[i] = new ArrayList<>();
        }

        for (int i = 0; i < triangles.size(); i++) {
            int[] triangle = triangles.get(i);
            Vector3f center = new Vector3f(vertices.get(triangle[0]))
                    .add(vertices.get(triangle[1]))
                    .add(vertices.get(triangle[2]))
                    .normalize();
            triangleCenters[i] = center;
            for (int vertex : triangle) {
                vertexTriangles[vertex].add(i);
            }
        }

        for (int i = 0; i < vertices.size(); i++) {
            List<Integer> adjacentTriangles = vertexTriangles[i];
            if (adjacentTriangles.size() < 5) continue;

            Vector3f normal = new Vector3f(vertices.get(i)).normalize();
            Vector3f[] cellVertices = new Vector3f[adjacentTriangles.size()];
            for (int j = 0; j < adjacentTriangles.size(); j++) {
                cellVertices[j] = new Vector3f(triangleCenters[adjacentTriangles.get(j)]);
            }
            sortAroundAxis(cellVertices, normal);

            Vector3f center = new Vector3f();
            for (Vector3f cellVertex : cellVertices) {
                center.add(cellVertex);
            }
            center.mul(1.0f / cellVertices.length).normalize().mul(SPHERE_RADIUS + SHIELD_CELL_SURFACE_OFFSET);

            for (int j = 0; j < cellVertices.length; j++) {
                cellVertices[j].normalize().mul(SPHERE_RADIUS + SHIELD_CELL_SURFACE_OFFSET);
            }

            shieldCells.add(new ShieldCell(normal, center, cellVertices));
        }
    }

    private static String geodesicKey(Vector3f point) {
        return String.format(Locale.ROOT, "%.6f,%.6f,%.6f", point.x, point.y, point.z);
    }

    private static void sortAroundAxis(Vector3f[] points, Vector3f axis) {
        Vector3f ref = new Vector3f(points[0]).sub(new Vector3f(axis).mul(axis.dot(points[0])));
        if (ref.lengthSquared() < 0.0001f) {
            ref = Math.abs(axis.x) < 0.9f
                    ? new Vector3f(1, 0, 0).cross(axis)
                    : new Vector3f(0, 1, 0).cross(axis);
        }
        ref.normalize();
        final Vector3f sortRef = ref;
        final Vector3f up = new Vector3f(axis).cross(sortRef).normalize();

        Arrays.sort(points, (p1, p2) -> {
            Vector3f d1 = new Vector3f(p1).sub(new Vector3f(axis).mul(axis.dot(p1)));
            Vector3f d2 = new Vector3f(p2).sub(new Vector3f(axis).mul(axis.dot(p2)));
            double a1 = Math.atan2(d1.dot(up), d1.dot(sortRef));
            double a2 = Math.atan2(d2.dot(up), d2.dot(sortRef));
            return Double.compare(a1, a2);
        });
    }

    private static void renderActivatedShieldCells(VertexConsumer vc, Matrix4f matrix, long gameTime, float partialTicks) {
        Iterator<Impact> iterator = impacts.iterator();
        int recentHits = 0;
        while (iterator.hasNext()) {
            Impact impact = iterator.next();
            float age = (gameTime - impact.startTick) + partialTicks;
            if (age >= IMPACT_DURATION_TICKS) {
                iterator.remove();
            } else if (age <= RECENT_HIT_WINDOW_TICKS) {
                recentHits++;
            }
        }

        if (impacts.isEmpty()) return;

        float activationAngle = Math.min(IMPACT_MAX_ANGLE, IMPACT_BASE_ANGLE + recentHits * 0.055f);
        float minDot = (float) Math.cos(activationAngle);

        for (ShieldCell cell : shieldCells) {
            float activation = 0.0f;
            for (Impact impact : impacts) {
                float age = (gameTime - impact.startTick) + partialTicks;
                float fade = 1.0f - age / IMPACT_DURATION_TICKS;
                float dot = cell.normal.dot(impact.normal);
                if (dot < minDot) continue;

                float proximity = (dot - minDot) / (1.0f - minDot);
                activation += fade * Mth.clamp(proximity, 0.0f, 1.0f);
            }

            activation = Mth.clamp(activation, 0.0f, 1.0f);
            if (activation <= 0.015f) continue;

            float pulse = 0.82f + 0.18f * (float) Math.sin((gameTime + partialTicks) * 0.55f);
            int fillA = (int) ((0.18f + 0.30f * activation) * activation * pulse * 255);
            int edgeA = (int) ((0.35f + 0.55f * activation) * activation * 255);
            renderShieldCell(vc, matrix, cell, fillA, edgeA);
        }
    }

    private static void renderShieldCell(VertexConsumer vc, Matrix4f matrix, ShieldCell cell, int fillA, int edgeA) {
        for (int i = 0; i < cell.vertices.length; i++) {
            Vector3f v1 = cell.vertices[i];
            Vector3f v2 = cell.vertices[(i + 1) % cell.vertices.length];
            quad(vc, matrix, cell.center, v1, v2, v2, 0x52, 0x00, 0xC8, fillA);
        }

        for (int i = 0; i < cell.vertices.length; i++) {
            renderEdge(vc, matrix, cell.vertices[i], cell.vertices[(i + 1) % cell.vertices.length],
                    SHIELD_CELL_EDGE_WIDTH, 0x9A, 0x20, 0xFF, edgeA);
        }
    }

    private static void renderEdge(VertexConsumer vc, Matrix4f matrix, Vector3f from, Vector3f to, float width,
                                   int r, int g, int b, int a) {
        Vector3f edge = new Vector3f(to).sub(from);
        if (edge.lengthSquared() < 0.0001f) return;
        edge.normalize();

        Vector3f radial = new Vector3f(from).add(to).normalize();
        Vector3f side = new Vector3f(radial).cross(edge);
        if (side.lengthSquared() < 0.0001f) return;
        side.normalize().mul(width);

        Vector3f p1 = projectToRadius(new Vector3f(from).add(side), SPHERE_RADIUS + SHIELD_CELL_SURFACE_OFFSET);
        Vector3f p2 = projectToRadius(new Vector3f(to).add(side), SPHERE_RADIUS + SHIELD_CELL_SURFACE_OFFSET);
        Vector3f p3 = projectToRadius(new Vector3f(to).sub(side), SPHERE_RADIUS + SHIELD_CELL_SURFACE_OFFSET);
        Vector3f p4 = projectToRadius(new Vector3f(from).sub(side), SPHERE_RADIUS + SHIELD_CELL_SURFACE_OFFSET);
        quad(vc, matrix, p1, p2, p3, p4, r, g, b, a);
    }

    private static Vector3f spherePoint(float theta, float phi, float radius) {
        float cosTheta = (float) Math.cos(theta);
        return new Vector3f(
                (float) Math.cos(phi) * cosTheta * radius,
                (float) Math.sin(theta) * radius,
                (float) Math.sin(phi) * cosTheta * radius
        );
    }

    private static Vector3f projectToRadius(Vector3f point, float radius) {
        return point.normalize().mul(radius);
    }

    private static void quad(VertexConsumer vc, Matrix4f matrix, Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4,
                             int r, int g, int b, int a) {
        vc.vertex(matrix, p1.x, p1.y, p1.z).color(r, g, b, a).endVertex();
        vc.vertex(matrix, p2.x, p2.y, p2.z).color(r, g, b, a).endVertex();
        vc.vertex(matrix, p3.x, p3.y, p3.z).color(r, g, b, a).endVertex();
        vc.vertex(matrix, p4.x, p4.y, p4.z).color(r, g, b, a).endVertex();
    }

    private record Impact(Vector3f normal, long startTick) {
    }

    private record ShieldCell(Vector3f normal, Vector3f center, Vector3f[] vertices) {
    }

    private DragonShieldRenderer() {
    }
}
