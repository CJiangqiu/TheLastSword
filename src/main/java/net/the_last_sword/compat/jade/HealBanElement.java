package net.the_last_sword.compat.jade;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IDisplayHelper;

//Jade 浮标中的禁疗剩余时间元素: 9x9 凋零心图标 + "Ns" 文本, 风格对齐 JustifiedDefenceElement
public class HealBanElement extends Element {

    //原版 icons.png 中的凋零满心 (u=124, v=45, 9x9)
    private static final ResourceLocation ICON = new ResourceLocation("minecraft", "textures/gui/icons.png");
    private static final int ICON_U = 124;
    private static final int ICON_V = 45;
    private static final int ICON_SIZE = 9;
    private static final int ATLAS_SIZE = 256;
    private static final int LINE_HEIGHT = 10;

    private final String text;

    public HealBanElement(int remainingSeconds) {
        //前缀两空格对齐 HealthElement 的视觉间距
        this.text = String.format("  %ds", remainingSeconds);
    }

    @Override
    public Vec2 getSize() {
        Font font = Minecraft.getInstance().font;
        return new Vec2(ICON_SIZE + font.width(text), LINE_HEIGHT);
    }

    @Override
    public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
        //图标 9x9: 从原版 icons.png 采样凋零满心
        guiGraphics.blit(ICON, (int) x, (int) y, ICON_U, ICON_V, ICON_SIZE, ICON_SIZE, ATLAS_SIZE, ATLAS_SIZE);
        //文本走 Jade 主题渲染 (自带阴影/主题色一致性)
        IDisplayHelper.get().drawText(guiGraphics, text, x + ICON_SIZE, y, IThemeHelper.get().getNormalColor());
    }
}
