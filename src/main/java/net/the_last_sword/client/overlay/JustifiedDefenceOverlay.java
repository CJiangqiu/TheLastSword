package net.the_last_sword.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.configuration.DefenceConfig;
import net.the_last_sword.configuration.DefenceConfigData;
import net.the_last_sword.init.ModAttributes;

//肃正防御护盾 HUD 渲染器
public class JustifiedDefenceOverlay {

    //护盾图标材质
    public static final ResourceLocation ICON_FULL =
        new ResourceLocation(TheLastSwordMod.MOD_ID, "textures/screens/justified_defence_full.png");
    public static final ResourceLocation ICON_EMPTY =
        new ResourceLocation(TheLastSwordMod.MOD_ID, "textures/screens/justified_defence_empty.png");

    //图标配置
    private static final int ICON_SIZE = 9;          // 图标大小 9x9
    private static final int ICON_SPACING = -8;      // 图标间距（负数表示重叠）
    private static final int MAX_ICONS = 10;         // 最多显示10个图标
    private static final double POINTS_PER_ICON = 2.0; // 每个图标代表2点护盾

    //在饥饿值渲染之前渲染护盾（由 ClientEventHandler 调用）
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay().id().equals(VanillaGuiOverlay.FOOD_LEVEL.id())) {
            renderJustifiedDefence(
                event.getGuiGraphics(),
                event.getGuiGraphics().guiWidth(),
                event.getGuiGraphics().guiHeight(),
                Minecraft.getInstance()
            );
        }
    }

    //渲染肃正防御护盾
    private static void renderJustifiedDefence(GuiGraphics gui, int screenWidth, int screenHeight, Minecraft mc) {
        Player player = mc.player;
        if (player == null || !shouldRender(player)) return;

        mc.getProfiler().push("justifiedDefenceHud");

        //HUD 右侧基准位置（与饥饿值对齐）
        int baseX = screenWidth / 2 + 91 - ICON_SIZE;
        int baseY = screenHeight - getRightHeightOffset(mc) - 10;

        //使用新的 DefenceConfig 系统获取位置偏移
        DefenceConfigData.HudOffset offset = DefenceConfig.getHudOffset("justified_defence_overlay");
        int startX = baseX + offset.x;
        int startY = baseY + offset.y;

        //读取当前和最大护盾值
        double currentShield = player.getAttributeValue(ModAttributes.JUSTIFIED_DEFENCE.get());
        double maxShield = player.getAttributeValue(ModAttributes.MAX_JUSTIFIED_DEFENCE.get());

        //没有护盾则不渲染
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
        int step = -ICON_SPACING; // 正间距
        int leftX = startX + ICON_SPACING * (MAX_ICONS - 1);

        RenderSystem.enableBlend();

        //渲染满格图标（从左到右）
        for (int i = 0; i < fullIcons; i++) {
            int x = leftX + i * step;
            gui.blit(ICON_FULL, x, startY, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        }

        //渲染半格图标
        if (halfIcon) {
            int x = leftX + fullIcons * step;
            int halfWidth = ICON_SIZE / 2;
            gui.blit(ICON_FULL, x, startY, 0, 0, halfWidth, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        }

        //渲染空格图标
        int offset_icons = fullIcons + (halfIcon ? 1 : 0);
        for (int i = 0; i < emptyIcons; i++) {
            int x = leftX + (offset_icons + i) * step;
            gui.blit(ICON_EMPTY, x, startY, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        }

        //超出显示上限的护盾值提示（右侧文字）
        double visibleCapacity = MAX_ICONS * POINTS_PER_ICON;
        if (currentShield > visibleCapacity) {
            double overflowPoints = currentShield - visibleCapacity;
            double rawIcons = overflowPoints / POINTS_PER_ICON;
            int wholeIcons = (int) Math.floor(rawIcons);
            boolean halfOverflow = (overflowPoints % POINTS_PER_ICON) >= 1.0;

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

    //判断是否应该渲染护盾（创造模式和旁观模式不渲染）
    private static boolean shouldRender(Player player) {
        return !player.isCreative() && !player.isSpectator();
    }

    //获取右侧HUD的高度偏移
    private static int getRightHeightOffset(Minecraft mc) {
        return ((ForgeGui) mc.gui).rightHeight;
    }
}
