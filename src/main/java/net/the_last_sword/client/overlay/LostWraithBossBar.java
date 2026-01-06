package net.the_last_sword.client.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.the_last_sword.entity.LostWraithEntity;

import java.util.ArrayList;
import java.util.List;

//迷失战魂Boss血条UI - 自动检测并渲染附近的迷失战魂
@Mod.EventBusSubscriber(modid = "the_last_sword", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class LostWraithBossBar {

    //检测范围
    private static final double DETECTION_RANGE = 64.0;

    //Boss条尺寸配置
    private static final int BAR_WIDTH = 320;
    private static final int BAR_HEIGHT = 16;
    private static final int BORDER_WIDTH = 2;

    //颜色配置 (ARGB)
    private static final int BORDER_COLOR = 0xFF8A2BE2;
    private static final int HEALTH_COLOR_START = 0xFF000000;
    private static final int HEALTH_COLOR_END = 0xFF4B0082;
    private static final int BACKGROUND_COLOR = 0xFF1A1A1A;

    //Boss条数据（非静态，每次渲染时重新收集）
    private static class BossBarData {
        public final LostWraithEntity entity;
        public final String name;
        public final float currentHealth;
        public final float maxHealth;

        public BossBarData(LostWraithEntity entity) {
            this.entity = entity;
            this.name = Component.translatable("entity.the_last_sword.lost_wraith").getString();
            this.currentHealth = entity.getTheLastEndHealth();
            this.maxHealth = entity.getTheLastEndMaxHealth();
        }
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay().id().toString().equals("minecraft:hotbar")) {
            renderBossBars(event.getGuiGraphics());
        }
    }

    private static void renderBossBars(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        //自动检测玩家附近的迷失战魂
        List<BossBarData> activeBosses = findNearbyLostWraiths(mc);
        if (activeBosses.isEmpty()) {
            return;
        }

        //渲染所有Boss条
        int yOffset = 0;
        for (BossBarData data : activeBosses) {
            renderSingleBossBar(guiGraphics, data, yOffset);
            yOffset += BAR_HEIGHT + 10;
        }
    }

    //自动检测附近的迷失战魂
    private static List<BossBarData> findNearbyLostWraiths(Minecraft mc) {
        List<BossBarData> bosses = new ArrayList<>();

        if (mc.player == null || mc.level == null) {
            return bosses;
        }

        //在玩家周围64格范围内查找
        AABB searchBox = mc.player.getBoundingBox().inflate(DETECTION_RANGE);
        List<Entity> entities = mc.level.getEntities(mc.player, searchBox);

        for (Entity entity : entities) {
            if (entity instanceof LostWraithEntity wraith) {
                //只显示已激活且存活的迷失战魂
                if (wraith.isAlive() && !wraith.isRemoved() && wraith.isSpawned() && !wraith.shouldLeave()) {
                    bosses.add(new BossBarData(wraith));
                }
            }
        }

        return bosses;
    }

    private static void renderSingleBossBar(GuiGraphics guiGraphics, BossBarData data, int yOffset) {
        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();

        int barX = (screenWidth - BAR_WIDTH) / 2;
        int barY = 20 + yOffset;

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        float healthPercent = data.maxHealth > 0 ? Math.max(0, Math.min(1, data.currentHealth / data.maxHealth)) : 0;
        int healthWidth = (int) (healthPercent * (BAR_WIDTH - 2 * BORDER_WIDTH));

        guiGraphics.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, BORDER_COLOR);

        guiGraphics.fill(
            barX + BORDER_WIDTH,
            barY + BORDER_WIDTH,
            barX + BAR_WIDTH - BORDER_WIDTH,
            barY + BAR_HEIGHT - BORDER_WIDTH,
            BACKGROUND_COLOR
        );

        if (healthWidth > 0) {
            renderGradientHealth(guiGraphics,
                barX + BORDER_WIDTH,
                barY + BORDER_WIDTH,
                healthWidth,
                BAR_HEIGHT - 2 * BORDER_WIDTH
            );
        }

        Component nameComponent = Component.literal(data.name);
        int textWidth = mc.font.width(nameComponent);
        int textX = barX + (BAR_WIDTH - textWidth) / 2;
        int textY = barY - 12;

        guiGraphics.drawString(mc.font, nameComponent, textX, textY, 0xFFFFFF);

        String healthText = String.format("%.0f / %.0f", data.currentHealth, data.maxHealth);
        Component healthComponent = Component.literal(healthText);
        int healthTextWidth = mc.font.width(healthComponent);
        int healthTextX = barX + (BAR_WIDTH - healthTextWidth) / 2;
        int healthTextY = barY + (BAR_HEIGHT - mc.font.lineHeight) / 2;

        guiGraphics.drawString(mc.font, healthComponent, healthTextX, healthTextY, 0xFFFFFF);

        poseStack.popPose();
    }

    //渲染渐变血量条
    private static void renderGradientHealth(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        for (int i = 0; i < width; i++) {
            float progress = (float) i / width;
            int color = interpolateColor(HEALTH_COLOR_START, HEALTH_COLOR_END, progress);
            guiGraphics.fill(x + i, y, x + i + 1, y + height, color);
        }
    }

    //颜色插值
    private static int interpolateColor(int color1, int color2, float progress) {
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int a = (int) (a1 + (a2 - a1) * progress);
        int r = (int) (r1 + (r2 - r1) * progress);
        int g = (int) (g1 + (g2 - g1) * progress);
        int b = (int) (b1 + (b2 - b1) * progress);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
