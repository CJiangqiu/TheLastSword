package net.the_last_sword.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.network.OpenLastEndScrollPacket.PaperNoteEntry;

import java.util.List;

//终焉卷轴「旅途见闻」章节GUI：左侧已收集纸条列表（附滑块），右侧显示内容
public class JourneyLogsScreen extends Screen {

    //与卷轴GUI共用统一背景
    private static final ResourceLocation SCROLL_TEXTURE =
        new ResourceLocation(TheLastSwordMod.MOD_ID, "textures/screens/scroll_background.png");

    private static final int GUI_WIDTH = 430;
    private static final int GUI_HEIGHT = 200;

    private static final int LIST_X = 50;        //列表起始（相对GUI左上）
    private static final int LIST_Y = 50;
    private static final int LIST_WIDTH = 110;
    private static final int ITEM_HEIGHT = 18;
    private static final int VISIBLE_ITEMS = 5;

    private static final int CONTENT_X = 175;    //内容起始（相对GUI左上）
    private static final int CONTENT_Y = 50;
    private static final int CONTENT_WIDTH = 220;
    private static final int CONTENT_HEIGHT = 110;
    private static final int LINE_HEIGHT = 11;
    private static final int LINES_PER_PAGE = CONTENT_HEIGHT / LINE_HEIGHT;

    private final List<PaperNoteEntry> notes;
    private final Screen parent;

    private int scrollOffset = 0;
    private int selectedIndex = -1;
    private List<String> contentLines = List.of();
    private int contentPage = 0;
    private int contentTotalPages = 1;

    private boolean draggingScrollbar = false;
    private int scrollDragStartY = 0;
    private int scrollDragStartOffset = 0;

    private Button previousButton;
    private Button nextButton;

    public JourneyLogsScreen(List<PaperNoteEntry> notes, Screen parent) {
        super(Component.translatable("gui.the_last_sword.the_last_end_scroll.chapter_7"));
        this.notes = notes;
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        int guiLeft = (this.width - GUI_WIDTH) / 2;
        int guiTop = (this.height - GUI_HEIGHT) / 2;

        //内容翻页按钮
        this.previousButton = Button.builder(Component.literal("◀"), button -> flipContentPage(-1))
            .bounds(guiLeft + CONTENT_X + 30, guiTop + CONTENT_Y + CONTENT_HEIGHT + 8, 30, 20).build();
        this.nextButton = Button.builder(Component.literal("▶"), button -> flipContentPage(1))
            .bounds(guiLeft + CONTENT_X + CONTENT_WIDTH - 60, guiTop + CONTENT_Y + CONTENT_HEIGHT + 8, 30, 20).build();

        this.addRenderableWidget(previousButton);
        this.addRenderableWidget(nextButton);

        updateButtons();
    }

    //选中纸条并加载其内容
    private void selectNote(int index) {
        if (index == selectedIndex) {
            return;
        }
        selectedIndex = index;
        contentLines = TextWrapUtil.wrapText(this.font,
            Component.translatable(notes.get(index).contentKey()).getString(), CONTENT_WIDTH);
        contentTotalPages = Math.max(1, (contentLines.size() + LINES_PER_PAGE - 1) / LINES_PER_PAGE);
        contentPage = 0;
        updateButtons();
    }

    //翻转右侧内容页
    private void flipContentPage(int delta) {
        contentPage = Math.max(0, Math.min(contentPage + delta, contentTotalPages - 1));
        updateButtons();
    }

    //更新翻页按钮可用状态
    private void updateButtons() {
        if (previousButton != null) {
            previousButton.active = selectedIndex >= 0 && contentPage > 0;
            nextButton.active = selectedIndex >= 0 && contentPage < contentTotalPages - 1;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        int guiLeft = (this.width - GUI_WIDTH) / 2;
        int guiTop = (this.height - GUI_HEIGHT) / 2;

        //渲染卷轴背景
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(SCROLL_TEXTURE, guiLeft, guiTop, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);

        //标题
        graphics.drawString(this.font, Component.translatable("gui.the_last_sword.the_last_end_scroll.chapter_7"),
            guiLeft + LIST_X, guiTop + 22, 0x3F2A1D, false);

        if (notes.isEmpty()) {
            graphics.drawCenteredString(this.font, Component.translatable("gui.the_last_sword.journey_logs.empty"),
                guiLeft + GUI_WIDTH / 2, guiTop + 95, 0x3F2A1D);
        } else {
            renderNoteList(graphics, guiLeft, guiTop);
            renderNoteContent(graphics, guiLeft, guiTop);
            renderScrollBar(graphics, guiLeft, guiTop);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    //渲染左侧纸条名字列表
    private void renderNoteList(GuiGraphics graphics, int guiLeft, int guiTop) {
        int end = Math.min(notes.size(), scrollOffset + VISIBLE_ITEMS);
        for (int i = scrollOffset; i < end; i++) {
            int y = guiTop + LIST_Y + (i - scrollOffset) * ITEM_HEIGHT;
            if (i == selectedIndex) {
                graphics.fill(guiLeft + LIST_X - 6, y - 1, guiLeft + LIST_X + LIST_WIDTH, y + ITEM_HEIGHT - 1, 0x663F2A1D);
            }
            String name = Component.translatable(notes.get(i).nameKey()).getString();
            graphics.drawString(this.font, Component.literal(this.font.plainSubstrByWidth(name, LIST_WIDTH)),
                guiLeft + LIST_X, y + 5, 0x3F2A1D, false);
        }
    }

    //渲染列表右侧滚动条（附魔台GUI同款）
    private void renderScrollBar(GuiGraphics graphics, int guiLeft, int guiTop) {
        if (notes.size() <= VISIBLE_ITEMS) {
            return;
        }
        int barHeight = VISIBLE_ITEMS * ITEM_HEIGHT;
        int thumbHeight = Math.max(20, barHeight * VISIBLE_ITEMS / notes.size());
        int maxScroll = notes.size() - VISIBLE_ITEMS;
        int thumbY = guiTop + LIST_Y + (barHeight - thumbHeight) * scrollOffset / maxScroll;

        graphics.fill(guiLeft + LIST_X + LIST_WIDTH + 2, guiTop + LIST_Y,
            guiLeft + LIST_X + LIST_WIDTH + 5, guiTop + LIST_Y + barHeight, 0x40000000);
        graphics.fill(guiLeft + LIST_X + LIST_WIDTH + 2, thumbY,
            guiLeft + LIST_X + LIST_WIDTH + 5, thumbY + thumbHeight, 0xFF555555);
    }

    //渲染右侧纸条内容
    private void renderNoteContent(GuiGraphics graphics, int guiLeft, int guiTop) {
        if (selectedIndex < 0) {
            return;
        }
        //列表与内容之间的分隔线
        graphics.fill(guiLeft + CONTENT_X - 12, guiTop + CONTENT_Y, guiLeft + CONTENT_X - 11,
            guiTop + CONTENT_Y + CONTENT_HEIGHT, 0x503F2A1D);

        int startLine = contentPage * LINES_PER_PAGE;
        int endLine = Math.min(contentLines.size(), startLine + LINES_PER_PAGE);
        int lineCount = 0;
        for (int i = startLine; i < endLine; i++) {
            graphics.drawString(this.font, Component.literal(contentLines.get(i)),
                guiLeft + CONTENT_X, guiTop + CONTENT_Y + lineCount * LINE_HEIGHT, 0x3F2A1D, false);
            lineCount++;
        }

        //页码
        String pageNumber = (contentPage + 1) + " / " + contentTotalPages;
        int pageNumberX = guiLeft + CONTENT_X + (CONTENT_WIDTH - this.font.width(pageNumber)) / 2;
        graphics.drawString(this.font, pageNumber, pageNumberX,
            guiTop + CONTENT_Y + CONTENT_HEIGHT + 14, 0x3F2A1D, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int guiLeft = (this.width - GUI_WIDTH) / 2;
        int guiTop = (this.height - GUI_HEIGHT) / 2;

        //滚动条拖拽开始
        if (notes.size() > VISIBLE_ITEMS) {
            int barHeight = VISIBLE_ITEMS * ITEM_HEIGHT;
            if (mouseX >= guiLeft + LIST_X + LIST_WIDTH + 2 && mouseX < guiLeft + LIST_X + LIST_WIDTH + 5
                    && mouseY >= guiTop + LIST_Y && mouseY < guiTop + LIST_Y + barHeight) {
                draggingScrollbar = true;
                scrollDragStartY = (int) mouseY;
                scrollDragStartOffset = scrollOffset;
                return true;
            }
        }

        //列表项点击命中
        double relX = mouseX - (guiLeft + LIST_X);
        double relY = mouseY - (guiTop + LIST_Y);
        if (relX >= -6 && relX <= LIST_WIDTH && relY >= 0) {
            int idx = (int) (relY / ITEM_HEIGHT);
            if (idx >= 0 && idx < VISIBLE_ITEMS) {
                int noteIndex = scrollOffset + idx;
                if (noteIndex >= 0 && noteIndex < notes.size()) {
                    selectNote(noteIndex);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScrollbar && notes.size() > VISIBLE_ITEMS) {
            int barHeight = VISIBLE_ITEMS * ITEM_HEIGHT;
            int thumbHeight = Math.max(20, barHeight * VISIBLE_ITEMS / notes.size());
            int maxScroll = notes.size() - VISIBLE_ITEMS;
            int scrollRange = barHeight - thumbHeight;
            if (scrollRange > 0) {
                int dragDelta = (int) mouseY - scrollDragStartY;
                scrollOffset = Math.max(0, Math.min(maxScroll, scrollDragStartOffset + dragDelta * maxScroll / scrollRange));
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int guiLeft = (this.width - GUI_WIDTH) / 2;
        int guiTop = (this.height - GUI_HEIGHT) / 2;

        //鼠标悬停在列表与滚动条区域时滚轮滚动
        if (mouseX >= guiLeft + LIST_X && mouseX < guiLeft + LIST_X + LIST_WIDTH + 5
                && mouseY >= guiTop + LIST_Y && mouseY < guiTop + LIST_Y + VISIBLE_ITEMS * ITEM_HEIGHT) {
            int maxScroll = Math.max(0, notes.size() - VISIBLE_ITEMS);
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) delta));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    //关闭时返回卷轴目录
    @Override
    public void onClose() {
        if (parent != null) {
            Minecraft.getInstance().setScreen(parent);
        } else {
            super.onClose();
        }
    }
}
