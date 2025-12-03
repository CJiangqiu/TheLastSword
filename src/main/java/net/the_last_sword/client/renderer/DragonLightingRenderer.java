package net.the_last_sword.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LightningBoltRenderer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LightningBolt;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import java.awt.*;

//龙之剑闪电渲染器 - 深紫色闪电
@OnlyIn(Dist.CLIENT)
public class DragonLightingRenderer extends LightningBoltRenderer {
    public DragonLightingRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(LightningBolt entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float[] afloat = new float[16];
        float[] afloat1 = new float[16];
        float f = 0.0F;
        float f1 = 0.0F;
        RandomSource randomsource = RandomSource.create(entity.seed);
        for (int i = 7; i >= 0; --i) {
            afloat[i] = f;
            afloat1[i] = f1;
            f += (float) (randomsource.nextInt(11) - 5);
            f1 += (float) (randomsource.nextInt(11) - 5);
        }
        VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.lightning());
        Matrix4f matrix4f = poseStack.last().pose();
        float time = (System.currentTimeMillis() % 10000) / 5000.0F;
        for (int j = 0; j < 4; ++j) {
            RandomSource randomsource1 = RandomSource.create(entity.seed);
            for (int k = 0; k < 3; ++k) {
                int l = 7;
                int i1 = 0;
                if (k > 0) {
                    l = 7 - k;
                }
                if (k > 0) {
                    i1 = l - 2;
                }
                float f2 = afloat[l] - f;
                float f3 = afloat1[l] - f1;
                for (int j1 = l; j1 >= i1; --j1) {
                    float f4 = f2;
                    float f5 = f3;
                    if (k == 0) {
                        f2 += (float) (randomsource1.nextInt(11) - 5);
                        f3 += (float) (randomsource1.nextInt(11) - 5);
                    } else {
                        f2 += (float) (randomsource1.nextInt(31) - 15);
                        f3 += (float) (randomsource1.nextInt(31) - 15);
                    }
                    float f10 = 0.1F + (float) j * 0.2F;
                    if (k == 0) {
                        f10 *= (float) j1 * 0.1F + 1.0F;
                    }
                    float f11 = 0.1F + (float) j * 0.2F;
                    if (k == 0) {
                        f11 *= ((float) j1 - 1.0F) * 0.1F + 1.0F;
                    }
                    float alpha = 1.0F - (float) j1 / 7.0F;
                    alpha *= 0.8F;
                    quad(matrix4f, vertexconsumer, f2, f3, j1, f4, f5, f10, f11, false, false, true, false, alpha, time);
                    quad(matrix4f, vertexconsumer, f2, f3, j1, f4, f5, f10, f11, true, false, true, true, alpha, time);
                    quad(matrix4f, vertexconsumer, f2, f3, j1, f4, f5, f10, f11, true, true, false, true, alpha, time);
                    quad(matrix4f, vertexconsumer, f2, f3, j1, f4, f5, f10, f11, false, true, false, false, alpha, time);
                }
            }
        }
    }

    private static void quad(Matrix4f matrix, VertexConsumer consumer, float x1, float z1, int y, float x2, float z2, float width1, float width2, boolean flag1, boolean flag2, boolean flag3, boolean flag4, float alpha, float time) {
        //参数配置
        float blackAmount = 0.5F; //黑色强度 (0.0-1.0)
        float purpleHue = 0.78F;  //深紫色色调 (0.75-0.85)

        //生成基础深紫色 (低饱和度+低亮度)
        int basePurple = Color.HSBtoRGB(purpleHue, 0.6F, 0.4F);

        //提取紫色RGB分量 (0-255)
        int pr = (basePurple >> 16) & 0xFF;
        int pg = (basePurple >> 8) & 0xFF;
        int pb = basePurple & 0xFF;

        //混合黑色
        float r = (pr / 255f) * (1 - blackAmount);
        float g = (pg / 255f) * (1 - blackAmount);
        float b = (pb / 255f) * (1 - blackAmount);

        //动态波动效果
        float pulse = 0.9F + 0.1F * Mth.sin(time * 2F);
        r *= pulse;
        g *= pulse;
        b *= pulse;

        consumer.vertex(matrix, x1 + (flag1 ? width2 : -width2), (float) (y * 16), z1 + (flag2 ? width2 : -width2))
                .color(r, g, b, alpha).endVertex();
        consumer.vertex(matrix, x2 + (flag1 ? width1 : -width1), (float) ((y + 1) * 16), z2 + (flag2 ? width1 : -width1))
                .color(r, g, b, alpha).endVertex();
        consumer.vertex(matrix, x2 + (flag3 ? width1 : -width1), (float) ((y + 1) * 16), z2 + (flag4 ? width1 : -width1))
                .color(r, g, b, alpha).endVertex();
        consumer.vertex(matrix, x1 + (flag3 ? width2 : -width2), (float) (y * 16), z1 + (flag4 ? width2 : -width2))
                .color(r, g, b, alpha).endVertex();
    }
}
