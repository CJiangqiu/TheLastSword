package net.the_last_sword.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.the_last_sword.client.DragonArmorEnergyStatus;
import net.the_last_sword.client.overlay.DragonArmorOverlay;
import net.the_last_sword.configuration.DefenceConfig;
import net.the_last_sword.configuration.DefenceConfigData;
import net.the_last_sword.network.DefenceConfigPacket;
import net.the_last_sword.network.NetworkHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// 龙甲电量显示位置调整界面
public class DragonArmorEnergyDisplayPositionScreen extends Screen {
    private final Screen parent;
    private int offsetX;
    private int offsetY;
    private boolean dragging;
    private int dragStartX;
    private int dragStartY;

    public DragonArmorEnergyDisplayPositionScreen(Screen parent) {
        super(Component.translatable("gui.the_last_sword.energy_display_position.title"));
        this.parent = parent;

        DefenceConfigData.HudOffset offset = DefenceConfig.getHudOffset("dragon_armor_energy_display");
        this.offsetX = offset.x;
        this.offsetY = offset.y;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int bottomY = this.height - 30;
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.the_last_sword.energy_display_position.save"),
            button -> {
                DefenceConfig.setHudOffset("dragon_armor_energy_display", offsetX, offsetY);
                DefenceConfig.save();
                NetworkHandler.sendToServer(new DefenceConfigPacket(DefenceConfig.getData()));
                if (this.minecraft != null) {
                    this.minecraft.setScreen(parent);
                }
            }
        ).bounds(centerX - 105, bottomY, 100, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.the_last_sword.energy_display_position.reset"),
            button -> {
                offsetX = 0;
                offsetY = 0;
            }
        ).bounds(centerX + 5, bottomY, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        graphics.drawCenteredString(
            this.font,
            Component.translatable("gui.the_last_sword.energy_display_position.hint"),
            this.width / 2,
            20,
            0xAAAAAA
        );
        renderEnergyDisplayPreview(graphics);
        graphics.drawString(
            this.font,
            Component.literal("Offset: X=" + offsetX + ", Y=" + offsetY),
            10,
            this.height - 45,
            0xFFFFFF
        );
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderEnergyDisplayPreview(GuiGraphics graphics) {
        List<Component> lines = getPreviewLines();
        int width = lines.stream().mapToInt(this.font::width).max().orElse(0);
        int height = lines.size() * this.font.lineHeight + (lines.size() - 1) * 2;
        int x = this.width / 2 - width / 2 + offsetX;
        int y = DragonArmorOverlay.ENERGY_DISPLAY_BASE_Y + offsetY;

        graphics.fill(x - 2, y - 2, x + width + 2, y + height + 2,
                dragging ? 0x80FFFF00 : 0x80FFFFFF);
        for (int i = 0; i < lines.size(); i++) {
            Component line = lines.get(i);
            graphics.drawString(this.font, line,
                    this.width / 2 - this.font.width(line) / 2 + offsetX,
                    y + i * (this.font.lineHeight + 2), 0xFFFFFF, true);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isInsidePreview(mouseX, mouseY)) {
            dragging = true;
            dragStartX = (int) mouseX - offsetX;
            dragStartY = (int) mouseY - offsetY;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging) {
            dragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && dragging) {
            offsetX = (int) mouseX - dragStartX;
            offsetY = (int) mouseY - dragStartY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private boolean isInsidePreview(double mouseX, double mouseY) {
        List<Component> lines = getPreviewLines();
        int width = lines.stream().mapToInt(this.font::width).max().orElse(0);
        int height = lines.size() * this.font.lineHeight + (lines.size() - 1) * 2;
        int x = this.width / 2 - width / 2 + offsetX;
        int y = DragonArmorOverlay.ENERGY_DISPLAY_BASE_Y + offsetY;
        return mouseX >= x - 2 && mouseX <= x + width + 2
                && mouseY >= y - 2 && mouseY <= y + height + 2;
    }

    private List<Component> getPreviewLines() {
        DefenceConfigData.PerceptionModule perception = DefenceConfig.getPerceptionModule();
        List<Component> lines = new ArrayList<>(2);
        if (perception.showArmorEnergy) {
            lines.add(getEnergyText());
        }
        if (perception.showEnergyConsumption) {
            lines.add(getConsumptionText());
        }
        // 两个细分项均关闭时仍提供可拖拽的预览。
        if (lines.isEmpty()) {
            lines.add(getEnergyText());
            lines.add(getConsumptionText());
        }
        return lines;
    }

    private Component getEnergyText() {
        String percentage = String.format(Locale.ROOT, "%.1f",
                DragonArmorEnergyStatus.getEnergyPercentage());
        return Component.translatable("gui.the_last_sword.perception_hud.current_energy", percentage);
    }

    private Component getConsumptionText() {
        return Component.translatable("gui.the_last_sword.perception_hud.total_consumption",
                DragonArmorEnergyStatus.getConsumptionPerTick());
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
