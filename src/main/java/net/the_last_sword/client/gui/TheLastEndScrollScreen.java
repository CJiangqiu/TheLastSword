package net.the_last_sword.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.init.ModItems;

import java.util.ArrayList;
import java.util.List;

//终焉卷轴教程书GUI
public class TheLastEndScrollScreen extends Screen {

    //卷轴背景纹理
    private static final ResourceLocation SCROLL_TEXTURE =
        new ResourceLocation(TheLastSwordMod.MOD_ID, "textures/screens/scroll_background.png");

    //GUI尺寸（与纹理一致）
    private static final int GUI_WIDTH = 430;
    private static final int GUI_HEIGHT = 200;

    //文本渲染区域（避开左右卷轴边缘）
    private static final int TEXT_START_X = 50;  //左侧留白
    private static final int TEXT_START_Y = 30;  //顶部留白
    private static final int TEXT_WIDTH = 330;   //文本宽度
    private static final int TEXT_HEIGHT = 130;  //文本高度
    private static final int LINE_HEIGHT = 11;   //行高
    private static final int MAX_LINES_PER_PAGE = 11; //每页最多显示的行数

    //当前页码（从0开始）
    private int currentPage = 0;
    //总页数（自动计算）
    private int totalPages;

    //章节数据缓存
    private final List<ChapterData> chapters = new ArrayList<>();
    private boolean chaptersInitialized = false; // 避免重复初始化

    //章节数据类
    private static class ChapterData {
        String titleKey;      // 标题翻译键
        String contentKey;    // 内容翻译键
        ItemStack icon;       // 章节图标
        int startPage;        // 起始页码
        int pageCount;        // 占用页数
        List<String> allLines; // 所有文本行（自动换行后）
    }

    //翻页按钮
    private Button previousButton;
    private Button nextButton;

    //章节按钮（第一页目录）
    private final java.util.List<Button> chapterButtons = new ArrayList<>();

    public TheLastEndScrollScreen() {
        super(Component.empty());
        // 章节初始化移到 init() 方法中执行
    }

    //初始化章节数据
    private void initializeChapters() {
        chapters.clear();

        //章节1：稀世奇材
        addChapter("gui.the_last_sword.scroll_book.chapter1.title",
                  "gui.the_last_sword.scroll_book.chapter1.content",
                  new ItemStack(ModItems.DRAGON_CRYSTAL.get()));

        //后续章节可以在这里添加
        //addChapter("gui.the_last_sword.scroll_book.chapter2.title", ...);
    }

    //添加章节并自动分页
    private void addChapter(String titleKey, String contentKey, ItemStack icon) {
        ChapterData chapter = new ChapterData();
        chapter.titleKey = titleKey;
        chapter.contentKey = contentKey;
        chapter.icon = icon;

        //获取章节内容并自动换行
        Component contentComp = Component.translatable(contentKey);
        String content = contentComp.getString();
        chapter.allLines = wrapText(content, TEXT_WIDTH);

        //计算起始页（第0页是目录）
        if (chapters.isEmpty()) {
            chapter.startPage = 1; //第一个章节从第1页开始
        } else {
            ChapterData lastChapter = chapters.get(chapters.size() - 1);
            chapter.startPage = lastChapter.startPage + lastChapter.pageCount;
        }

        //计算需要多少页
        //第一页：标题（3行高度） + 内容
        //后续页：只有内容
        int firstPageLines = MAX_LINES_PER_PAGE - 3; //第一页要留空间给标题和图标
        int remainingLines = chapter.allLines.size() - firstPageLines;

        if (remainingLines <= 0) {
            chapter.pageCount = 1; //一页就够了
        } else {
            chapter.pageCount = 1 + (int) Math.ceil((double) remainingLines / MAX_LINES_PER_PAGE);
        }

        chapters.add(chapter);
    }

    //计算总页数
    private int calculateTotalPages() {
        if (chapters.isEmpty()) {
            return 1; //至少有目录页
        }
        ChapterData lastChapter = chapters.get(chapters.size() - 1);
        return lastChapter.startPage + lastChapter.pageCount;
    }

    @Override
    protected void init() {
        super.init();

        // 首次初始化章节数据（只执行一次）
        if (!chaptersInitialized) {
            initializeChapters();
            this.totalPages = calculateTotalPages();
            chaptersInitialized = true;
        }

        //清空之前的章节按钮
        chapterButtons.clear();

        //计算GUI在屏幕中的位置（居中）
        int guiLeft = (this.width - GUI_WIDTH) / 2;
        int guiTop = (this.height - GUI_HEIGHT) / 2;

        //如果是第一页（目录页），创建章节按钮
        if (currentPage == 0) {
            createChapterButtons(guiLeft, guiTop);
        }

        //计算页码位置
        String pageNumber = (currentPage + 1) + " / " + totalPages;
        int pageNumberX = guiLeft + (GUI_WIDTH / 2) - (this.font.width(pageNumber) / 2);
        int pageNumberY = guiTop + GUI_HEIGHT - 25;

        //按钮尺寸
        int buttonWidth = 40;
        int buttonHeight = 20;
        int buttonSpacing = 10; //按钮与页码的间距

        //上一页按钮（页码左侧）
        this.previousButton = Button.builder(
            Component.literal("◀"),
            button -> previousPage()
        ).bounds(pageNumberX - buttonWidth - buttonSpacing, pageNumberY - 5, buttonWidth, buttonHeight).build();

        //下一页按钮（页码右侧）
        this.nextButton = Button.builder(
            Component.literal("▶"),
            button -> nextPage()
        ).bounds(pageNumberX + this.font.width(pageNumber) + buttonSpacing, pageNumberY - 5, buttonWidth, buttonHeight).build();

        this.addRenderableWidget(previousButton);
        this.addRenderableWidget(nextButton);

        updateButtons();
    }

    //创建章节目录按钮
    private void createChapterButtons(int guiLeft, int guiTop) {
        int buttonWidth = 90;
        int buttonHeight = 20;
        int buttonSpacingX = 10; //按钮之间的水平间距
        int buttonSpacingY = 25; //按钮之间的垂直间距（增大）

        //起始位置（文本区域内居中）
        int startX = guiLeft + TEXT_START_X + 10;
        int startY = guiTop + TEXT_START_Y + 20;

        //第一行：3个按钮
        for (int i = 0; i < 3; i++) {
            final int chapterIndex = i;
            Button btn = Button.builder(
                Component.literal((i + 1) + ". ").append(Component.translatable("gui.the_last_sword.the_last_end_scroll.chapter_" + (i + 1))),
                button -> jumpToChapter(chapterIndex)
            ).bounds(startX + i * (buttonWidth + buttonSpacingX), startY, buttonWidth, buttonHeight).build();

            chapterButtons.add(btn);
            this.addRenderableWidget(btn);
        }

        //第二行：3个按钮
        for (int i = 3; i < 6; i++) {
            final int chapterIndex = i;
            Button btn = Button.builder(
                Component.literal((i + 1) + ". ").append(Component.translatable("gui.the_last_sword.the_last_end_scroll.chapter_" + (i + 1))),
                button -> jumpToChapter(chapterIndex)
            ).bounds(startX + (i - 3) * (buttonWidth + buttonSpacingX), startY + buttonHeight + buttonSpacingY, buttonWidth, buttonHeight).build();

            chapterButtons.add(btn);
            this.addRenderableWidget(btn);
        }
    }

    //跳转到指定章节
    private void jumpToChapter(int chapterIndex) {
        if (chapterIndex >= 0 && chapterIndex < chapters.size()) {
            currentPage = chapters.get(chapterIndex).startPage;
            //重新初始化界面（移除章节按钮，刷新翻页按钮状态）
            this.clearWidgets();
            this.init();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        //渲染暗色背景
        this.renderBackground(graphics);

        //计算GUI位置
        int guiLeft = (this.width - GUI_WIDTH) / 2;
        int guiTop = (this.height - GUI_HEIGHT) / 2;

        //渲染卷轴背景纹理
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // 参数: ResourceLocation, x, y, u, v, width, height, textureWidth, textureHeight
        graphics.blit(SCROLL_TEXTURE, guiLeft, guiTop, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);

        RenderSystem.disableBlend();

        //渲染当前页面内容
        renderPageContent(graphics, guiLeft, guiTop);

        //渲染页码（底部中央，往上提5像素）
        String pageNumber = (currentPage + 1) + " / " + totalPages;
        int pageNumberX = guiLeft + (GUI_WIDTH / 2) - (this.font.width(pageNumber) / 2);
        int pageNumberY = guiTop + GUI_HEIGHT - 25;
        graphics.drawString(this.font, pageNumber, pageNumberX, pageNumberY, 0x3F2A1D, false);

        //渲染按钮等其他组件
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    //渲染页面内容
    private void renderPageContent(GuiGraphics graphics, int guiLeft, int guiTop) {
        //如果是第一页（目录页），不渲染文本内容（由按钮代替）
        if (currentPage == 0) {
            return;
        }

        //找到当前页属于哪个章节
        ChapterData currentChapter = null;
        int pageOffsetInChapter = 0;

        for (ChapterData chapter : chapters) {
            int chapterEndPage = chapter.startPage + chapter.pageCount - 1;
            if (currentPage >= chapter.startPage && currentPage <= chapterEndPage) {
                currentChapter = chapter;
                pageOffsetInChapter = currentPage - chapter.startPage;
                break;
            }
        }

        //如果没有找到章节，返回
        if (currentChapter == null) {
            return;
        }

        int textX = guiLeft + TEXT_START_X;
        int textY = guiTop + TEXT_START_Y;

        //如果是章节起始页（偏移为0），渲染标题和图标
        if (pageOffsetInChapter == 0) {
            if (!currentChapter.icon.isEmpty()) {
                graphics.renderItem(currentChapter.icon, textX, textY);
                //标题从物品图标右侧开始
                Component title = Component.translatable(currentChapter.titleKey);
                graphics.drawString(this.font, title, textX + 20, textY + 4, 0x3F2A1D, false);
                //正文从下一行开始
                textY += 25;
            } else {
                //只有标题没有图标
                Component title = Component.translatable(currentChapter.titleKey);
                graphics.drawString(this.font, title, textX, textY, 0x3F2A1D, false);
                textY += 15;
            }
        }

        //计算当前页应该显示的行范围
        int startLineIndex;
        int linesToShow;

        if (pageOffsetInChapter == 0) {
            //第一页：显示前 MAX_LINES_PER_PAGE - 3 行
            startLineIndex = 0;
            linesToShow = MAX_LINES_PER_PAGE - 3;
        } else {
            //后续页：从上一页结束的地方继续
            int firstPageLines = MAX_LINES_PER_PAGE - 3;
            startLineIndex = firstPageLines + (pageOffsetInChapter - 1) * MAX_LINES_PER_PAGE;
            linesToShow = MAX_LINES_PER_PAGE;
        }

        //渲染文本行
        int lineCount = 0;
        for (int i = startLineIndex; i < currentChapter.allLines.size() && lineCount < linesToShow; i++) {
            String line = currentChapter.allLines.get(i);
            graphics.drawString(this.font, Component.literal(line), textX, textY + (lineCount * LINE_HEIGHT), 0x3F2A1D, false);
            lineCount++;
        }
    }

    //文本自动换行（支持中英文混合）
    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return lines;
        }

        StringBuilder currentLine = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            //测试加入当前字符后的宽度
            String testLine = currentLine.toString() + c;

            if (this.font.width(testLine) <= maxWidth) {
                currentLine.append(c);
            } else {
                //超过宽度，换行
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                    currentLine.append(c);
                }
            }
        }

        //添加最后一行
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    //上一页
    private void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            //重新初始化界面（刷新章节按钮显示）
            this.clearWidgets();
            this.init();
        }
    }

    //下一页
    private void nextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            //重新初始化界面（刷新章节按钮显示）
            this.clearWidgets();
            this.init();
        }
    }

    //更新按钮状态
    private void updateButtons() {
        if (previousButton != null) {
            previousButton.active = currentPage > 0;
        }
        if (nextButton != null) {
            nextButton.active = currentPage < totalPages - 1;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false; //不暂停游戏
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        //ESC键关闭GUI
        if (keyCode == 256) { //GLFW.GLFW_KEY_ESCAPE
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
