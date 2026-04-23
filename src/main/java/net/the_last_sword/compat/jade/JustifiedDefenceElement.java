package net.the_last_sword.compat.jade;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.the_last_sword.TheLastSwordMod;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IDisplayHelper;

import java.math.RoundingMode;
import java.text.DecimalFormat;

//Jade 浮标中的肃正防御元素: 9x9 盾图标 + "当前/上限" 文本, 风格对齐 HealthElement/ArmorElement
public class JustifiedDefenceElement extends Element {

    //复用 HUD 叠加层的护盾图标
    private static final ResourceLocation ICON = new ResourceLocation(TheLastSwordMod.MOD_ID, "textures/screens/justified_defence_full.png");
    private static final int ICON_SIZE = 9;
    private static final int LINE_HEIGHT = 10;

    //对齐 Jade 原版 dfCommas: "0.##" + ROUND_DOWN
    private static final DecimalFormat FORMAT = new DecimalFormat("0.##");
    static {
        FORMAT.setRoundingMode(RoundingMode.DOWN);
    }

    private final String text;

    public JustifiedDefenceElement(double current, double max) {
        //前缀两空格对齐 HealthElement 的视觉间距
        this.text = String.format("  %s/%s", FORMAT.format(current), FORMAT.format(max));
    }

    @Override
    public Vec2 getSize() {
        Font font = Minecraft.getInstance().font;
        return new Vec2(ICON_SIZE + font.width(text), LINE_HEIGHT);
    }

    @Override
    public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
        //图标 9x9
        guiGraphics.blit(ICON, (int) x, (int) y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        //文本走 Jade 主题渲染 (自带阴影/主题色一致性)
        IDisplayHelper.get().drawText(guiGraphics, text, x + ICON_SIZE, y, IThemeHelper.get().getNormalColor());
    }
}
