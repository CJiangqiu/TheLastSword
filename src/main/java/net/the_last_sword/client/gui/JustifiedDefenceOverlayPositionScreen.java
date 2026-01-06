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
        super(Component.translatable("gui.the_last_sword.justified_defence_position.title"));
        this.parent = parent;

        //加载当前配置
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
                //保存配置
                DefenceConfig.setHudOffset("justified_defence_overlay", offsetX, offsetY);
                DefenceConfig.save();
                //发送配置到服务端
                NetworkHandler.sendToServer(new DefenceConfigPacket(DefenceConfig.getData()));

                //返回上一界面
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

        //标题
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        //提示信息
        graphics.drawCenteredString(
            this.font,
            Component.translatable("gui.the_last_sword.justified_defence_position.hint"),
            this.width / 2,
            40,
            0xAAAAAA
        );

        //渲染HUD预览（模拟护盾显示）
        renderJustifiedDefencePreview(graphics);

        //显示当前偏移量
        graphics.drawString(
            this.font,
            Component.literal("Offset: X=" + offsetX + ", Y=" + offsetY),
            10,
            10,
            0xFFFFFF
        );

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    //渲染肃正防御护盾预览（真实图标）
    private void renderJustifiedDefencePreview(GuiGraphics graphics) {
        int screenWidth = this.width;
        int screenHeight = this.height;

        //HUD右侧基准位置（与实际HUD渲染一致）
        int baseX = screenWidth / 2 + 91 - ICON_SIZE;
        int baseY = screenHeight - 49;

        //应用偏移
        int startX = baseX + offsetX;
        int startY = baseY + offsetY;

        //计算图标布局参数
        int step = -ICON_SPACING;  // 正间距
        int leftX = startX + ICON_SPACING * (MAX_ICONS - 1);

        //计算整体边界框
        int totalWidth = (MAX_ICONS - 1) * step + ICON_SIZE;
        int boundingBoxX = leftX - 2;
        int boundingBoxY = startY - 2;
        int boundingBoxWidth = totalWidth + 4;
        int boundingBoxHeight = ICON_SIZE + 4;

        //渲染半透明边框（表示可拖拽区域）
        graphics.fill(boundingBoxX, boundingBoxY,
                     boundingBoxX + boundingBoxWidth,
                     boundingBoxY + boundingBoxHeight,
                     dragging ? 0x80FFFF00 : 0x80FFFFFF);  // 拖拽时高亮黄色

        RenderSystem.enableBlend();

        //渲染10个满防御图标（从左到右）
        for (int i = 0; i < MAX_ICONS; i++) {
            int x = leftX + i * step;
            graphics.blit(ICON_FULL, x, startY, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        }

        RenderSystem.disableBlend();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {  // 左键
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

            //检查是否点击在图标区域
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
