package net.the_last_sword.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.configuration.DefenceConfig;
import net.the_last_sword.configuration.DefenceConfigData;
import net.the_last_sword.init.ModAttributes;

public class AttributeHudRenderer {
    public static final ResourceLocation ICON_FULL =
        new ResourceLocation(TheLastSwordMod.MOD_ID, "textures/screens/justified_defence_full.png");
    public static final ResourceLocation ICON_EMPTY =
        new ResourceLocation(TheLastSwordMod.MOD_ID, "textures/screens/justified_defence_empty.png");

    private static final int ICON_SIZE         = 9;
    private static final int ICON_SPACING      = -8;
    private static final int MAX_ICONS         = 10;
    private static final double POINTS_PER_ICON = 2.0;

    public static void renderAttributes(GuiGraphics gui, int screenWidth, int screenHeight, Minecraft mc) {
        Player player = mc.player;
        if (player == null || !shouldRender(player)) return;

        mc.getProfiler().push("attributeHud");

        //HUD右侧基准位置
        int baseX = screenWidth / 2 + 91 - ICON_SIZE;
        int baseY = screenHeight - getRightHeightOffset(mc) - 10;

        //读取配置偏移量
        DefenceConfigData.HudOffset offset = DefenceConfig.getHudOffset("justified_defence_overlay");
        int startX = baseX + offset.x;
        int startY = baseY + offset.y;

        //读取当前和最大护盾
        double currentShield = player.getAttributeValue(ModAttributes.JUSTIFIED_DEFENCE.get());
        double maxShield     = player.getAttributeValue(ModAttributes.MAX_JUSTIFIED_DEFENCE.get());

        if (currentShield <= 0) {
            mc.getProfiler().pop();
            return;
        }

        //计算满格、半格、空格数量
        int fullIcons = (int) Math.floor(currentShield / POINTS_PER_ICON);
        fullIcons = Math.min(fullIcons, MAX_ICONS);

        boolean halfIcon = fullIcons < MAX_ICONS
                        && (currentShield % POINTS_PER_ICON) >= 1.0;

        int emptyIcons = MAX_ICONS - fullIcons - (halfIcon ? 1 : 0);

        //计算从最左格开始的 X 坐标
        int step  = -ICON_SPACING; // 正间距
        int leftX = startX + ICON_SPACING * (MAX_ICONS - 1);

        RenderSystem.enableBlend();

        //渲染满格（从左到右）
        for (int i = 0; i < fullIcons; i++) {
            int x = leftX + i * step;
            gui.blit(ICON_FULL, x, startY, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        }

        //渲染半格
        if (halfIcon) {
            int x = leftX + fullIcons * step;
            int halfWidth = ICON_SIZE / 2;
            gui.blit(ICON_FULL, x, startY, 0, 0, halfWidth, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        }

        //渲染空格
        int offset_icons = fullIcons + (halfIcon ? 1 : 0);
        for (int i = 0; i < emptyIcons; i++) {
            int x = leftX + (offset_icons + i) * step;
            gui.blit(ICON_EMPTY, x, startY, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        }

        //超出提示（右侧，左移4px，下移2px）
        double visibleCapacity = MAX_ICONS * POINTS_PER_ICON;
        if (currentShield > visibleCapacity) {
            double overflowPoints = currentShield - visibleCapacity;
            double rawIcons       = overflowPoints / POINTS_PER_ICON;
            int wholeIcons        = (int) Math.floor(rawIcons);
            boolean halfOverflow  = (overflowPoints % POINTS_PER_ICON) >= 1.0;

            String text = halfOverflow
                ? "+" + wholeIcons + ".5"
                : "+" + wholeIcons;

            int textX = leftX + MAX_ICONS * step + ICON_SIZE - 4;
            int textY = startY + 2;
            gui.drawString(mc.font, text, textX, textY, 0xFFFFFF, true);
        }

        RenderSystem.disableBlend();
        mc.getProfiler().pop();
    }

    private static boolean shouldRender(Player player) {
        return !player.isCreative() && !player.isSpectator();
    }

    public static int getRightHeightOffset(Minecraft mc) {
        return ((ForgeGui) mc.gui).rightHeight;
    }
}
