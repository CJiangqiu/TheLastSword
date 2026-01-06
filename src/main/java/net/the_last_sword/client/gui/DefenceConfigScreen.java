package net.the_last_sword.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class DefenceConfigScreen extends Screen {

    public DefenceConfigScreen() {
        super(Component.translatable("gui.the_last_sword.defence_config.title"));
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 200;
        int buttonHeight = 20;
        int centerX = this.width / 2;
        int startY = this.height / 2 - 30;

        //按钮1：装备配置
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.the_last_sword.defence_config.equipment_settings"),
            button -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new ArmorConfigScreen(this));
                }
            }
        ).bounds(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight).build());

        //按钮2：肃正防御叠加层位置
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.the_last_sword.defence_config.justified_defence_position"),
            button -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new JustifiedDefenceOverlayPositionScreen(this));
                }
            }
        ).bounds(centerX - buttonWidth / 2, startY + 30, buttonWidth, buttonHeight).build());

        //关闭按钮
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.done"),
            button -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(null);
                }
            }
        ).bounds(centerX - buttonWidth / 2, startY + 60, buttonWidth, buttonHeight).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
