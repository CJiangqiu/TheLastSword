package net.the_last_sword.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.the_last_sword.configuration.DefenceConfig;
import net.the_last_sword.configuration.DefenceConfigData;
import net.the_last_sword.network.DefenceConfigPacket;
import net.the_last_sword.network.NetworkHandler;

import java.util.ArrayList;
import java.util.List;

// 龙之甲模块详细配置界面
public class DragonArmorModuleDetailScreen extends Screen {
    private final Screen parent;
    private final ModuleType moduleType;

    private final List<ConfigEntry> configEntries = new ArrayList<>();
    private Button activationModeButton;
    private DefenceConfigData.PhasingActivationMode currentActivationMode;
    private Button shieldModeButton;
    private DefenceConfigData.ShieldEffectMode currentShieldMode;

    public enum ModuleType {
        LIFE_SUPPORT("gui.the_last_sword.dragon_armor_module.life_support"),
        PHASING("gui.the_last_sword.dragon_armor_module.phasing"),
        ANTI_GRAVITY("gui.the_last_sword.dragon_armor_module.anti_gravity"),
        PERCEPTION("gui.the_last_sword.dragon_armor_module.perception"),
        DRAGON_SHIELD("gui.the_last_sword.dragon_armor_module.dragon_shield");

        private final String titleKey;

        ModuleType(String titleKey) {
            this.titleKey = titleKey;
        }

        public Component getTitle() {
            return Component.translatable(titleKey);
        }
    }

    public DragonArmorModuleDetailScreen(Screen parent, ModuleType moduleType) {
        super(moduleType.getTitle());
        this.parent = parent;
        this.moduleType = moduleType;
    }

    @Override
    protected void init() {
        super.init();
        configEntries.clear();

        int startY = 50;
        int spacing = 25;

        switch (moduleType) {
            case LIFE_SUPPORT -> initLifeSupport(startY, spacing);
            case PHASING -> initPhasing(startY, spacing);
            case ANTI_GRAVITY -> initAntiGravity(startY, spacing);
            case PERCEPTION -> initPerception(startY, spacing);
            case DRAGON_SHIELD -> initDragonShield(startY, spacing);
        }

        // 保存按钮
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.the_last_sword.config.save"),
            button -> {
                saveConfig();
                DefenceConfig.save();
                NetworkHandler.sendToServer(new DefenceConfigPacket(DefenceConfig.getData()));
                if (this.minecraft != null) {
                    this.minecraft.setScreen(parent);
                }
            }
        ).bounds(this.width / 2 - 105, this.height - 50, 100, 20).build());

        // 返回按钮
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.back"),
            button -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(parent);
                }
            }
        ).bounds(this.width / 2 + 5, this.height - 50, 100, 20).build());
    }

    // 维生模块：增强Buff、饱和、冰火不侵、伤害减免
    private void initLifeSupport(int startY, int spacing) {
        DefenceConfigData.LifeSupportModule module = DefenceConfig.getLifeSupportModule();
        addCheckbox("gui.the_last_sword.module.life_support.enhanced_buff",
            module.enhancedBuff, startY,
            v -> module.enhancedBuff = v);
        addCheckbox("gui.the_last_sword.module.life_support.saturation",
            module.enableSaturation, startY + spacing,
            v -> module.enableSaturation = v);
        addCheckbox("gui.the_last_sword.module.life_support.ice_fire_immunity",
            module.enableIceFireImmunity, startY + spacing * 2,
            v -> module.enableIceFireImmunity = v);
    }

    // 虚化模块：是否消耗电量启动虚化、启动方式、护盾特效
    private void initPhasing(int startY, int spacing) {
        DefenceConfigData.PhasingModule module = DefenceConfig.getPhasingModule();

        addCheckbox("gui.the_last_sword.module.phasing.enabled",
            module.enabled, startY,
            v -> module.enabled = v);

        currentActivationMode = module.activationMode;
        activationModeButton = Button.builder(
            getActivationModeText(currentActivationMode),
            button -> {
                currentActivationMode = (currentActivationMode == DefenceConfigData.PhasingActivationMode.ALWAYS)
                    ? DefenceConfigData.PhasingActivationMode.FLY_ONLY
                    : DefenceConfigData.PhasingActivationMode.ALWAYS;
                button.setMessage(getActivationModeText(currentActivationMode));
            }
        ).bounds(this.width / 2 - 100, startY + spacing, 200, 20).build();
        this.addRenderableWidget(activationModeButton);

    }

    // 反重力模块：飞行速度、飞行惯性
    private void initAntiGravity(int startY, int spacing) {
        DefenceConfigData.AntiGravityModule module = DefenceConfig.getAntiGravityModule();

        FlySpeedSlider flySpeedSlider = new FlySpeedSlider(
            this.width / 2 - 100, startY,
            200, 20,
            module.flySpeed
        );
        this.addRenderableWidget(flySpeedSlider);
        configEntries.add(new ConfigEntry(null, v -> {}, flySpeedSlider, null));

        addCheckbox("gui.the_last_sword.module.anti_gravity.enable_inertia",
            module.enableInertia, startY + spacing,
            v -> module.enableInertia = v);
    }

    // 感知模块：HUD开关、扫描生物、扫描间隔
    private void initPerception(int startY, int spacing) {
        DefenceConfigData.PerceptionModule module = DefenceConfig.getPerceptionModule();

        addCheckbox("gui.the_last_sword.module.perception.enable_hud",
            module.enableHud, startY,
            v -> module.enableHud = v);

        addCheckbox("gui.the_last_sword.module.perception.scan_entities",
            module.scanEntities, startY + spacing,
            v -> module.scanEntities = v);

        ScanIntervalSlider scanSlider = new ScanIntervalSlider(
            this.width / 2 - 100, startY + spacing * 2,
            200, 20,
            module.scanIntervalSeconds
        );
        this.addRenderableWidget(scanSlider);
        configEntries.add(new ConfigEntry(null, v -> {}, null, scanSlider));
    }

    private void initDragonShield(int startY, int spacing) {
        DefenceConfigData.DragonShieldModule module = DefenceConfig.getDragonShieldModule();

        addCheckbox("gui.the_last_sword.module.dragon_shield.enabled",
            module.enabled, startY,
            v -> module.enabled = v);
        addCheckbox("gui.the_last_sword.module.dragon_shield.dragon_aura",
            module.enableDragonAura, startY + spacing,
            v -> module.enableDragonAura = v);

        currentShieldMode = module.shieldEffect;
        shieldModeButton = Button.builder(
            getShieldModeText(currentShieldMode),
            button -> {
                currentShieldMode = switch (currentShieldMode) {
                    case ALWAYS -> DefenceConfigData.ShieldEffectMode.DISABLED;
                    case ON_HIT, DISABLED -> DefenceConfigData.ShieldEffectMode.ALWAYS;
                };
                button.setMessage(getShieldModeText(currentShieldMode));
            }
        ).bounds(this.width / 2 - 100, startY + spacing * 2, 200, 20).build();
        this.addRenderableWidget(shieldModeButton);
    }

    private void addCheckbox(String translationKey, boolean initialValue, int y, BooleanConsumer setter) {
        Checkbox checkbox = new Checkbox(
            this.width / 2 - 100, y,
            200, 20,
            Component.translatable(translationKey),
            initialValue
        );
        this.addRenderableWidget(checkbox);
        configEntries.add(new ConfigEntry(checkbox, setter, null, null));
    }

    // 保存所有配置
    private void saveConfig() {
        for (ConfigEntry entry : configEntries) {
            if (entry.checkbox != null) {
                entry.setter.accept(entry.checkbox.selected());
            }
        }

        switch (moduleType) {
            case PHASING -> {
                DefenceConfig.getPhasingModule().activationMode = currentActivationMode;
            }
            case DRAGON_SHIELD -> {
                DefenceConfig.getDragonShieldModule().shieldEffect = currentShieldMode;
            }
            case ANTI_GRAVITY -> {
                for (ConfigEntry entry : configEntries) {
                    if (entry.flySpeedSlider != null) {
                        DefenceConfig.getAntiGravityModule().flySpeed = entry.flySpeedSlider.getFlySpeed();
                    }
                }
            }
            case PERCEPTION -> {
                for (ConfigEntry entry : configEntries) {
                    if (entry.scanIntervalSlider != null) {
                        DefenceConfig.getPerceptionModule().scanIntervalSeconds = entry.scanIntervalSlider.getInterval();
                    }
                }
            }
            default -> {}
        }
    }

    private Component getActivationModeText(DefenceConfigData.PhasingActivationMode mode) {
        String key = (mode == DefenceConfigData.PhasingActivationMode.ALWAYS)
            ? "gui.the_last_sword.module.phasing.mode.always"
            : "gui.the_last_sword.module.phasing.mode.fly_only";
        return Component.translatable("gui.the_last_sword.module.phasing.activation_mode",
            Component.translatable(key));
    }

    private Component getShieldModeText(DefenceConfigData.ShieldEffectMode mode) {
        String key = switch (mode) {
            case ON_HIT, ALWAYS -> "gui.the_last_sword.module.dragon_shield.shield.enabled";
            case DISABLED -> "gui.the_last_sword.module.dragon_shield.shield.disabled";
        };
        return Component.translatable("gui.the_last_sword.module.dragon_shield.shield",
                Component.translatable(key));
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

    private record ConfigEntry(Checkbox checkbox, BooleanConsumer setter,
                               FlySpeedSlider flySpeedSlider, ScanIntervalSlider scanIntervalSlider) {}

    @FunctionalInterface
    private interface BooleanConsumer {
        void accept(boolean value);
    }

    // 飞行速度滑块
    private static class FlySpeedSlider extends AbstractSliderButton {
        private static final float BASE_FLYING_SPEED = 0.05f;
        private static final int MIN_PERCENTAGE = 0;
        private static final int MAX_PERCENTAGE = 1200;

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
        protected void applyValue() {}

        public float getFlySpeed() {
            return percentageToFlySpeed(getCurrentPercentage());
        }

        private int getCurrentPercentage() {
            return Math.round(Mth.lerp((float) this.value, MIN_PERCENTAGE, MAX_PERCENTAGE));
        }

        private static float percentageToFlySpeed(int percentage) {
            return BASE_FLYING_SPEED * (1 + percentage / 100.0f);
        }

        private static int flySpeedToPercentage(float flySpeed) {
            return Math.round((flySpeed / BASE_FLYING_SPEED - 1) * 100);
        }

        private static double percentageToSliderValue(int percentage) {
            return (percentage - MIN_PERCENTAGE) / (double) (MAX_PERCENTAGE - MIN_PERCENTAGE);
        }

        private static String formatPercentage(int percentage) {
            return "+" + percentage + "%";
        }
    }

    // 扫描间隔滑块（秒为单位）
    private static class ScanIntervalSlider extends AbstractSliderButton {
        private static final int MIN_SECONDS = 1;
        private static final int MAX_SECONDS = 120;

        public ScanIntervalSlider(int x, int y, int width, int height, int initialSeconds) {
            super(x, y, width, height,
                Component.translatable("gui.the_last_sword.module.perception.scan_interval", initialSeconds),
                secondsToSliderValue(initialSeconds)
            );
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.translatable(
                "gui.the_last_sword.module.perception.scan_interval",
                getInterval()
            ));
        }

        @Override
        protected void applyValue() {}

        public int getInterval() {
            return Math.round(Mth.lerp((float) this.value, MIN_SECONDS, MAX_SECONDS));
        }

        private static double secondsToSliderValue(int seconds) {
            return (seconds - MIN_SECONDS) / (double) (MAX_SECONDS - MIN_SECONDS);
        }
    }
}
