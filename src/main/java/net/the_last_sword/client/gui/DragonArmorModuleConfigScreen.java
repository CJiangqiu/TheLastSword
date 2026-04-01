package net.the_last_sword.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

// 龙之甲模块选择界面
public class DragonArmorModuleConfigScreen extends Screen {
    private final Screen parent;

    public DragonArmorModuleConfigScreen(Screen parent) {
        super(Component.translatable("gui.the_last_sword.dragon_armor_module.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 200;
        int buttonHeight = 20;
        int centerX = this.width / 2;
        int startY = this.height / 2 - 50;
        int spacing = 25;

        // 维生模块
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.the_last_sword.dragon_armor_module.life_support"),
            button -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new DragonArmorModuleDetailScreen(
                        this, DragonArmorModuleDetailScreen.ModuleType.LIFE_SUPPORT));
                }
            }
        ).bounds(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight).build());

        // 虚化模块
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.the_last_sword.dragon_armor_module.phasing"),
            button -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new DragonArmorModuleDetailScreen(
                        this, DragonArmorModuleDetailScreen.ModuleType.PHASING));
                }
            }
        ).bounds(centerX - buttonWidth / 2, startY + spacing, buttonWidth, buttonHeight).build());

        // 反重力模块
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.the_last_sword.dragon_armor_module.anti_gravity"),
            button -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new DragonArmorModuleDetailScreen(
                        this, DragonArmorModuleDetailScreen.ModuleType.ANTI_GRAVITY));
                }
            }
        ).bounds(centerX - buttonWidth / 2, startY + spacing * 2, buttonWidth, buttonHeight).build());

        // 感知模块
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.the_last_sword.dragon_armor_module.perception"),
            button -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new DragonArmorModuleDetailScreen(
                        this, DragonArmorModuleDetailScreen.ModuleType.PERCEPTION));
                }
            }
        ).bounds(centerX - buttonWidth / 2, startY + spacing * 3, buttonWidth, buttonHeight).build());

        // 返回按钮
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.back"),
            button -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(parent);
                }
            }
        ).bounds(centerX - buttonWidth / 2, startY + spacing * 4 + 10, buttonWidth, buttonHeight).build());
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
