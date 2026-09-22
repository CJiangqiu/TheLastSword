package net.the_last_sword.compat.jade;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IDisplayHelper;

//实体信息浮标中的禁疗剩余时间使用世界锚度图标，便于区分普通负面效果
public class HealBanElement extends Element {

    private static final ResourceLocation ICON =
            new ResourceLocation("the_last_sword", "textures/screens/world_anchor_full.png");
    private static final int ICON_SIZE = 9;
    private static final int LINE_HEIGHT = 10;

    private final String text;

    public HealBanElement(int remainingSeconds) {
        //前缀两空格用于对齐原有生命值行的视觉间距
        this.text = String.format("  %ds", remainingSeconds);
    }

    @Override
    public Vec2 getSize() {
        Font font = Minecraft.getInstance().font;
        return new Vec2(ICON_SIZE + font.width(text), LINE_HEIGHT);
    }

    @Override
    public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
        guiGraphics.blit(ICON, (int) x, (int) y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        //沿用浮标主题渲染，保持阴影和文字颜色一致
        IDisplayHelper.get().drawText(guiGraphics, text, x + ICON_SIZE, y, IThemeHelper.get().getNormalColor());
    }
}
