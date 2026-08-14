package net.the_last_sword.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.network.ConfirmPaperNotePacket;
import net.the_last_sword.network.NetworkHandler;

import java.util.List;

//剧情纸条阅读GUI
public class PaperNoteScreen extends Screen {

    //纸条背景纹理（512高清贴图渲染到256逻辑尺寸）
    private static final ResourceLocation PAPER_TEXTURE =
        new ResourceLocation(TheLastSwordMod.MOD_ID, "textures/screens/elder_paper_gui.png");

    private static final int GUI_WIDTH = 256;
    private static final int GUI_HEIGHT = 256;

    private static final int CONTENT_START_X = 40;   //正文起始（相对GUI左上）
    private static final int CONTENT_START_Y = 52;   //标题下方
    private static final int TITLE_Y = 32;
    private static final int CONTENT_WIDTH = 176;
    private static final int CONTENT_HEIGHT = 150;
    private static final int LINE_HEIGHT = 11;
    private static final int LINES_PER_PAGE = CONTENT_HEIGHT / LINE_HEIGHT;

    private static final int ACK_BUTTON_WIDTH = 100;
    private static final int ACK_BUTTON_HEIGHT = 20;

    private final String noteId;
    private final String nameKey;
    private final boolean collected;

    private List<String> allLines;
    private int totalPages = 1;
    private int currentPage = 0;

    private Button previousButton;
    private Button nextButton;
    private Button acknowledgeButton;
    private boolean initialized = false;

    public PaperNoteScreen(String noteId, String nameKey, String guiContentKey, boolean collected) {
        super(Component.translatable(guiContentKey));
        this.noteId = noteId;
        this.nameKey = nameKey;
        this.collected = collected;
    }

    @Override
    protected void init() {
        super.init();
        if (!initialized) {
            //取出GUI内容文本并自动换行、分页
            this.allLines = TextWrapUtil.wrapText(this.font, this.title.getString(), CONTENT_WIDTH);
            this.totalPages = Math.max(1, (allLines.size() + LINES_PER_PAGE - 1) / LINES_PER_PAGE);
            this.initialized = true;
        }
        createNavigationButtons();
        refreshAcknowledgeButton();
    }

    //创建翻页按钮与页码
    private void createNavigationButtons() {
        int guiLeft = (this.width - GUI_WIDTH) / 2;
        int guiTop = (this.height - GUI_HEIGHT) / 2;

        this.previousButton = Button.builder(Component.literal("◀"), button -> previousPage())
            .bounds(guiLeft + 30, guiTop + GUI_HEIGHT - 32, 40, 20).build();
        this.nextButton = Button.builder(Component.literal("▶"), button -> nextPage())
            .bounds(guiLeft + GUI_WIDTH - 70, guiTop + GUI_HEIGHT - 32, 40, 20).build();

        this.addRenderableWidget(previousButton);
        this.addRenderableWidget(nextButton);
        updateButtons();
    }

    //「我已知晓」按钮跟随末页文本末尾动态定位
    private void refreshAcknowledgeButton() {
        if (acknowledgeButton != null) {
            this.removeWidget(acknowledgeButton);
            acknowledgeButton = null;
        }
        if (currentPage != totalPages - 1) {
            return;
        }

        int guiLeft = (this.width - GUI_WIDTH) / 2;
        int guiTop = (this.height - GUI_HEIGHT) / 2;

        //按钮Y=当前页文本行数渲染结束位置+间距，放不下时收进GUI底部
        int startLine = currentPage * LINES_PER_PAGE;
        int linesOnPage = Math.min(LINES_PER_PAGE, allLines.size() - startLine);
        int buttonY = Math.min(guiTop + CONTENT_START_Y + linesOnPage * LINE_HEIGHT + 12,
            guiTop + GUI_HEIGHT - 34);

        Component label = Component.translatable(collected
            ? "gui.the_last_sword.paper_note.collected"
            : "gui.the_last_sword.paper_note.acknowledge");
        acknowledgeButton = Button.builder(label, button -> {
            if (!collected) {
                NetworkHandler.sendToServer(new ConfirmPaperNotePacket(noteId));
            }
            this.onClose();
        }).bounds(guiLeft + (GUI_WIDTH - ACK_BUTTON_WIDTH) / 2, buttonY,
            ACK_BUTTON_WIDTH, ACK_BUTTON_HEIGHT).build();
        acknowledgeButton.active = !collected;

        this.addRenderableWidget(acknowledgeButton);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        int guiLeft = (this.width - GUI_WIDTH) / 2;
        int guiTop = (this.height - GUI_HEIGHT) / 2;

        //渲染纸条背景（512高清贴图缩放至256）
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(PAPER_TEXTURE, guiLeft, guiTop, 0, 0, GUI_WIDTH, GUI_HEIGHT, 512, 512);

        //标题
        graphics.drawString(this.font, Component.translatable(nameKey),
            guiLeft + CONTENT_START_X, guiTop + TITLE_Y, 0x3F2A1D, false);

        //正文（当前页行范围）
        int startLine = currentPage * LINES_PER_PAGE;
        int endLine = Math.min(allLines.size(), startLine + LINES_PER_PAGE);
        int lineCount = 0;
        for (int i = startLine; i < endLine; i++) {
            graphics.drawString(this.font, Component.literal(allLines.get(i)),
                guiLeft + CONTENT_START_X, guiTop + CONTENT_START_Y + lineCount * LINE_HEIGHT,
                0x3F2A1D, false);
            lineCount++;
        }

        //页码
        String pageNumber = (currentPage + 1) + " / " + totalPages;
        int pageNumberX = guiLeft + (GUI_WIDTH - this.font.width(pageNumber)) / 2;
        graphics.drawString(this.font, pageNumber, pageNumberX, guiTop + GUI_HEIGHT - 25, 0x3F2A1D, false);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    //上一页
    private void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            updateButtons();
            refreshAcknowledgeButton();
        }
    }

    //下一页
    private void nextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            updateButtons();
            refreshAcknowledgeButton();
        }
    }

    //更新翻页按钮状态
    private void updateButtons() {
        if (previousButton != null) {
            previousButton.active = currentPage > 0;
        }
        if (nextButton != null) {
            nextButton.active = currentPage < totalPages - 1;
        }
    }
}
