package net.the_last_sword.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.the_last_sword.configuration.DefenceConfig;
import net.the_last_sword.configuration.DefenceConfigData;
import net.the_last_sword.network.DefenceConfigPacket;
import net.the_last_sword.network.NetworkHandler;

// 肃正防御配置主界面：护盾特效 + HUD位置
public class JustifiedDefenceConfigScreen extends Screen {

    private final Screen parent;
    private DefenceConfigData.ShieldEffectMode currentShieldMode;
    private Button shieldModeButton;

    public JustifiedDefenceConfigScreen(Screen parent) {
        super(Component.translatable("gui.the_last_sword.defence_config.justified_defence"));
        this.parent = parent;
        this.currentShieldMode = DefenceConfig.getJustifiedDefence().overlayEffect;
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 200;
        int buttonHeight = 20;
        int centerX = this.width / 2;
        int startY = this.height / 2 - 25;

        //护盾特效模式
        shieldModeButton = Button.builder(
            getShieldModeText(currentShieldMode),
            button -> {
                currentShieldMode = switch (currentShieldMode) {
                    case ON_HIT, ALWAYS -> DefenceConfigData.ShieldEffectMode.DISABLED;
                    case DISABLED -> DefenceConfigData.ShieldEffectMode.ALWAYS;
                };
                button.setMessage(getShieldModeText(currentShieldMode));
                DefenceConfig.getJustifiedDefence().overlayEffect = currentShieldMode;
                DefenceConfig.save();
                NetworkHandler.sendToServer(new DefenceConfigPacket(DefenceConfig.getData()));
            }
        ).bounds(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(shieldModeButton);

        //HUD位置
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.the_last_sword.defence_config.justified_defence_position"),
            button -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new JustifiedDefenceOverlayPositionScreen(this));
                }
            }
        ).bounds(centerX - buttonWidth / 2, startY + 30, buttonWidth, buttonHeight).build());

        //返回
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.back"),
            button -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(parent);
                }
            }
        ).bounds(centerX - buttonWidth / 2, startY + 60, buttonWidth, buttonHeight).build());
    }

    private Component getShieldModeText(DefenceConfigData.ShieldEffectMode mode) {
        String key = switch (mode) {
            case ON_HIT, ALWAYS -> "gui.the_last_sword.module.phasing.shield.enabled";
            case DISABLED -> "gui.the_last_sword.module.phasing.shield.disabled";
        };
        return Component.translatable("gui.the_last_sword.defence_config.justified_defence.overlay",
                Component.translatable(key));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
