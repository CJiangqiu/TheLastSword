package net.the_last_sword.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.the_last_sword.configuration.DefenceConfig;
import net.the_last_sword.network.DefenceConfigPacket;
import net.the_last_sword.network.NetworkHandler;

//龙之甲全局配置界面：HUD开关、飞行速度等
public class DragonArmorGlobalConfigScreen extends Screen {
    private final Screen parent;

    private Checkbox hudCheckbox;
    private FlySpeedSlider flySpeedSlider;

    public DragonArmorGlobalConfigScreen(Screen parent) {
        super(Component.translatable("gui.the_last_sword.dragon_armor_config.global_settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int startY = 60;
        int spacing = 30;

        //HUD 开关
        hudCheckbox = new Checkbox(
            this.width / 2 - 100, startY,
            200, 20,
            Component.translatable("gui.the_last_sword.config.enable_hud"),
            DefenceConfig.isDragonArmorHudEnabled()
        );
        this.addRenderableWidget(hudCheckbox);

        //飞行速度滑块
        flySpeedSlider = new FlySpeedSlider(
            this.width / 2 - 100, startY + spacing,
            200, 20,
            DefenceConfig.getDragonArmorFlySpeed()
        );
        this.addRenderableWidget(flySpeedSlider);

        //保存按钮
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.the_last_sword.config.save"),
            button -> {
                saveConfig();
                DefenceConfig.save();
                //发送配置到服务端
                NetworkHandler.sendToServer(new DefenceConfigPacket(DefenceConfig.getData()));
                if (this.minecraft != null) {
                    this.minecraft.setScreen(parent);
                }
            }
        ).bounds(this.width / 2 - 105, this.height - 50, 100, 20).build());

        //返回按钮
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.back"),
            button -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(parent);
                }
            }
        ).bounds(this.width / 2 + 5, this.height - 50, 100, 20).build());
    }

    //保存配置
    private void saveConfig() {
        DefenceConfig.setDragonArmorHudEnabled(hudCheckbox.selected());
        DefenceConfig.setDragonArmorFlySpeed(flySpeedSlider.getFlySpeed());
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

    //飞行速度滑块
    private static class FlySpeedSlider extends AbstractSliderButton {
        private static final float BASE_FLYING_SPEED = 0.05f; // 原版飞行速度基准
        private static final int MIN_PERCENTAGE = 0;          // 最小百分比 +0%
        private static final int MAX_PERCENTAGE = 1200;       // 最大百分比 +1200%

        public FlySpeedSlider(int x, int y, int width, int height, float initialFlySpeed) {
            super(x, y, width, height,
                Component.translatable("gui.the_last_sword.config.fly_speed", formatPercentage(flySpeedToPercentage(initialFlySpeed))),
                percentageToSliderValue(flySpeedToPercentage(initialFlySpeed))
            );
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.translatable(
                "gui.the_last_sword.config.fly_speed",
                formatPercentage(getCurrentPercentage())
            ));
        }

        @Override
        protected void applyValue() {
            //值在保存时应用
        }

        //获取当前配置的 flySpeed 绝对值
        public float getFlySpeed() {
            int percentage = getCurrentPercentage();
            return percentageToFlySpeed(percentage);
        }

        //获取当前滑块对应的百分比（整数）
        private int getCurrentPercentage() {
            return Math.round(Mth.lerp((float) this.value, MIN_PERCENTAGE, MAX_PERCENTAGE));
        }

        //百分比转 flySpeed
        private static float percentageToFlySpeed(int percentage) {
            return BASE_FLYING_SPEED * (1 + percentage / 100.0f);
        }

        //flySpeed 转百分比
        private static int flySpeedToPercentage(float flySpeed) {
            return Math.round((flySpeed / BASE_FLYING_SPEED - 1) * 100);
        }

        //百分比转滑块值（0.0 - 1.0）
        private static double percentageToSliderValue(int percentage) {
            return (percentage - MIN_PERCENTAGE) / (double) (MAX_PERCENTAGE - MIN_PERCENTAGE);
        }

        //格式化百分比显示（+XX%）
        private static String formatPercentage(int percentage) {
            return "+" + percentage + "%";
        }
    }
}
