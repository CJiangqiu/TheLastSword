package net.the_last_sword.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.the_last_sword.configuration.DefenceConfig;
import net.the_last_sword.configuration.DefenceConfigData.*;
import net.the_last_sword.network.DefenceConfigPacket;
import net.the_last_sword.network.NetworkHandler;

import java.util.ArrayList;
import java.util.List;

//盔甲部件配置界面：显示各个 Buff 的开关
public class ArmorPieceConfigScreen extends Screen {
    private final Screen parent;
    private final ArmorType armorType;
    private final ArmorSlot armorSlot;

    private final List<ConfigEntry> configEntries = new ArrayList<>();

    public enum ArmorType {
        DRAGON_CRYSTAL, DRAGON
    }

    public enum ArmorSlot {
        HELMET, CHESTPLATE, LEGGINGS, BOOTS, FULL_SET
    }

    public ArmorPieceConfigScreen(Screen parent, Component title, ArmorType armorType, ArmorSlot armorSlot) {
        super(title);
        this.parent = parent;
        this.armorType = armorType;
        this.armorSlot = armorSlot;
    }

    @Override
    protected void init() {
        super.init();
        configEntries.clear();

        int startY = 50;
        int checkboxHeight = 25;

        //根据盔甲类型和部件添加配置项
        if (armorType == ArmorType.DRAGON_CRYSTAL) {
            initDragonCrystalConfig(startY, checkboxHeight);
        } else {
            initDragonArmorConfig(startY, checkboxHeight);
        }

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

    //初始化龙水晶盔甲配置
    private void initDragonCrystalConfig(int startY, int checkboxHeight) {
        DragonCrystalArmorConfig config = DefenceConfig.getDragonCrystalArmorConfig();

        switch (armorSlot) {
            case HELMET -> {
                addCheckbox("gui.the_last_sword.config.night_vision",
                    config.helmet.enableNightVision, startY,
                    v -> config.helmet.enableNightVision = v);
                addCheckbox("gui.the_last_sword.config.water_breathing",
                    config.helmet.enableWaterBreathing, startY + checkboxHeight,
                    v -> config.helmet.enableWaterBreathing = v);
            }
            case CHESTPLATE -> {
                addCheckbox("gui.the_last_sword.config.damage_resistance",
                    config.chestplate.enableDamageResistance, startY,
                    v -> config.chestplate.enableDamageResistance = v);
                addCheckbox("gui.the_last_sword.config.strength",
                    config.chestplate.enableStrength, startY + checkboxHeight,
                    v -> config.chestplate.enableStrength = v);
            }
            case LEGGINGS -> {
                addCheckbox("gui.the_last_sword.config.regeneration",
                    config.leggings.enableRegeneration, startY,
                    v -> config.leggings.enableRegeneration = v);
                addCheckbox("gui.the_last_sword.config.jump_boost",
                    config.leggings.enableJumpBoost, startY + checkboxHeight,
                    v -> config.leggings.enableJumpBoost = v);
            }
            case BOOTS -> {
                addCheckbox("gui.the_last_sword.config.speed",
                    config.boots.enableSpeed, startY,
                    v -> config.boots.enableSpeed = v);
                addCheckbox("gui.the_last_sword.config.fire_resistance",
                    config.boots.enableFireResistance, startY + checkboxHeight,
                    v -> config.boots.enableFireResistance = v);
            }
            case FULL_SET -> {
                addCheckbox("gui.the_last_sword.config.crystal_guard",
                    config.fullSet.enableCrystalGuard, startY,
                    v -> config.fullSet.enableCrystalGuard = v);
            }
        }
    }

    //初始化龙之甲配置
    private void initDragonArmorConfig(int startY, int checkboxHeight) {
        DragonArmorConfig config = DefenceConfig.getDragonArmorConfig();

        switch (armorSlot) {
            case HELMET -> {
                addCheckbox("gui.the_last_sword.config.night_vision",
                    config.helmet.enableNightVision, startY,
                    v -> config.helmet.enableNightVision = v);
                addCheckbox("gui.the_last_sword.config.water_breathing",
                    config.helmet.enableWaterBreathing, startY + checkboxHeight,
                    v -> config.helmet.enableWaterBreathing = v);
            }
            case CHESTPLATE -> {
                addCheckbox("gui.the_last_sword.config.damage_resistance",
                    config.chestplate.enableDamageResistance, startY,
                    v -> config.chestplate.enableDamageResistance = v);
                addCheckbox("gui.the_last_sword.config.strength",
                    config.chestplate.enableStrength, startY + checkboxHeight,
                    v -> config.chestplate.enableStrength = v);
                addCheckbox("gui.the_last_sword.config.flight",
                    config.chestplate.enableFlight, startY + checkboxHeight * 2,
                    v -> config.chestplate.enableFlight = v);
            }
            case LEGGINGS -> {
                addCheckbox("gui.the_last_sword.config.regeneration",
                    config.leggings.enableRegeneration, startY,
                    v -> config.leggings.enableRegeneration = v);
                addCheckbox("gui.the_last_sword.config.jump_boost",
                    config.leggings.enableJumpBoost, startY + checkboxHeight,
                    v -> config.leggings.enableJumpBoost = v);
            }
            case BOOTS -> {
                addCheckbox("gui.the_last_sword.config.speed",
                    config.boots.enableSpeed, startY,
                    v -> config.boots.enableSpeed = v);
                addCheckbox("gui.the_last_sword.config.fire_resistance",
                    config.boots.enableFireResistance, startY + checkboxHeight,
                    v -> config.boots.enableFireResistance = v);
            }
            case FULL_SET -> {
                addCheckbox("gui.the_last_sword.config.saturation",
                    config.fullSet.enableSaturation, startY,
                    v -> config.fullSet.enableSaturation = v);
                addCheckbox("gui.the_last_sword.config.ice_fire_immunity",
                    config.fullSet.enableIceFireImmunity, startY + checkboxHeight,
                    v -> config.fullSet.enableIceFireImmunity = v);
                addCheckbox("gui.the_last_sword.config.phasing",
                    config.fullSet.enablePhasing, startY + checkboxHeight * 2,
                    v -> config.fullSet.enablePhasing = v);
                addCheckbox("gui.the_last_sword.config.damage_reduction",
                    config.fullSet.enableDamageReduction, startY + checkboxHeight * 3,
                    v -> config.fullSet.enableDamageReduction = v);
            }
        }
    }

    //添加复选框
    private void addCheckbox(String translationKey, boolean initialValue, int y, BooleanConsumer setter) {
        Checkbox checkbox = new Checkbox(
            this.width / 2 - 100, y,
            200, 20,
            Component.translatable(translationKey),
            initialValue
        );
        this.addRenderableWidget(checkbox);
        configEntries.add(new ConfigEntry(checkbox, setter));
    }

    //保存配置
    private void saveConfig() {
        for (ConfigEntry entry : configEntries) {
            entry.setter.accept(entry.checkbox.selected());
        }
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

    //配置条目：关联复选框和配置设置器
    private record ConfigEntry(Checkbox checkbox, BooleanConsumer setter) {}

    @FunctionalInterface
    private interface BooleanConsumer {
        void accept(boolean value);
    }
}
