package net.the_last_sword.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.configuration.DefenceConfig;
import net.the_last_sword.configuration.DefenceConfigData;
import net.the_last_sword.network.DefenceConfigPacket;
import net.the_last_sword.network.NetworkHandler;

public class JustifiedDefenceOverlayPositionScreen extends Screen {
    private static final ResourceLocation ICON_FULL =
        new ResourceLocation(TheLastSwordMod.MOD_ID, "textures/screens/justified_defence_full.png");

    private static final int ICON_SIZE = 9;
    private static final int ICON_SPACING = -8;
    private static final int MAX_ICONS = 10;

    private final Screen parent;
    private int offsetX;
    private int offsetY;
    private boolean dragging = false;
    private int dragStartX;
    private int dragStartY;

    public JustifiedDefenceOverlayPositionScreen(Screen parent) {
        super(Component.translatable("gui.the_last_sword.defence_config.justified_defence_position"));
        this.parent = parent;

        DefenceConfigData.HudOffset offset = DefenceConfig.getHudOffset("justified_defence_overlay");
        this.offsetX = offset.x;
        this.offsetY = offset.y;
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 200;
        int buttonHeight = 20;
        int centerX = this.width / 2;
        int bottomY = this.height - 30;

        //保存按钮
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.the_last_sword.justified_defence_position.save"),
            button -> {
                DefenceConfig.setHudOffset("justified_defence_overlay", offsetX, offsetY);
                DefenceConfig.save();
                NetworkHandler.sendToServer(new DefenceConfigPacket(DefenceConfig.getData()));

                if (this.minecraft != null) {
                    this.minecraft.setScreen(parent);
                }
            }
        ).bounds(centerX - buttonWidth - 5, bottomY, buttonWidth, buttonHeight).build());

        //重置按钮
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.the_last_sword.justified_defence_position.reset"),
            button -> {
                offsetX = 0;
                offsetY = 0;
            }
        ).bounds(centerX + 5, bottomY, buttonWidth, buttonHeight).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        graphics.drawCenteredString(
            this.font,
            Component.translatable("gui.the_last_sword.justified_defence_position.hint"),
            this.width / 2,
            40,
            0xAAAAAA
        );

        renderJustifiedDefencePreview(graphics);

        graphics.drawString(
            this.font,
            Component.literal("Offset: X=" + offsetX + ", Y=" + offsetY),
            10,
            10,
            0xFFFFFF
        );

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderJustifiedDefencePreview(GuiGraphics graphics) {
        int screenWidth = this.width;
        int screenHeight = this.height;

        int baseX = screenWidth / 2 + 91 - ICON_SIZE;
        int baseY = screenHeight - 49;

        int startX = baseX + offsetX;
        int startY = baseY + offsetY;

        int step = -ICON_SPACING;
        int leftX = startX + ICON_SPACING * (MAX_ICONS - 1);

        int totalWidth = (MAX_ICONS - 1) * step + ICON_SIZE;
        int boundingBoxX = leftX - 2;
        int boundingBoxY = startY - 2;
        int boundingBoxWidth = totalWidth + 4;
        int boundingBoxHeight = ICON_SIZE + 4;

        graphics.fill(boundingBoxX, boundingBoxY,
                     boundingBoxX + boundingBoxWidth,
                     boundingBoxY + boundingBoxHeight,
                     dragging ? 0x80FFFF00 : 0x80FFFFFF);

        RenderSystem.enableBlend();

        for (int i = 0; i < MAX_ICONS; i++) {
            int x = leftX + i * step;
            graphics.blit(ICON_FULL, x, startY, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        }

        RenderSystem.disableBlend();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int screenWidth = this.width;
            int screenHeight = this.height;

            int baseX = screenWidth / 2 + 91 - ICON_SIZE;
            int baseY = screenHeight - 49;

            int startX = baseX + offsetX;
            int startY = baseY + offsetY;

            int step = -ICON_SPACING;
            int leftX = startX + ICON_SPACING * (MAX_ICONS - 1);
            int totalWidth = (MAX_ICONS - 1) * step + ICON_SIZE;

            int boundingBoxX = leftX - 2;
            int boundingBoxY = startY - 2;
            int boundingBoxWidth = totalWidth + 4;
            int boundingBoxHeight = ICON_SIZE + 4;

            if (mouseX >= boundingBoxX && mouseX <= boundingBoxX + boundingBoxWidth &&
                mouseY >= boundingBoxY && mouseY <= boundingBoxY + boundingBoxHeight) {
                dragging = true;
                dragStartX = (int) mouseX - offsetX;
                dragStartY = (int) mouseY - offsetY;
                return true;
            }
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
        if (dragging) {
            offsetX = (int) mouseX - dragStartX;
            offsetY = (int) mouseY - dragStartY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
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
