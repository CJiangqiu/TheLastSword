package net.the_last_sword.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.compat.CompatCheck;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.init.ModItems;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//终焉卷轴教程书GUI
public class TheLastEndScrollScreen extends Screen {

    //卷轴背景纹理
    private static final ResourceLocation SCROLL_TEXTURE =
        new ResourceLocation(TheLastSwordMod.MOD_ID, "textures/screens/scroll_background.png");

    //章节图片纹理
    private static final ResourceLocation CHAPTER_RECIPE_1_1_TEXTURE =
        new ResourceLocation(TheLastSwordMod.MOD_ID, "textures/screens/chapter_recipe_1_1.png");
    private static final ResourceLocation CHAPTER_RECIPE_2_1_TEXTURE =
        new ResourceLocation(TheLastSwordMod.MOD_ID, "textures/screens/chapter_recipe_2_1.png");
    private static final ResourceLocation CHAPTER_RECIPE_2_2_TEXTURE =
        new ResourceLocation(TheLastSwordMod.MOD_ID, "textures/screens/chapter_recipe_2_2.png");
    private static final ResourceLocation CHAPTER_RECIPE_3_1_TEXTURE =
        new ResourceLocation(TheLastSwordMod.MOD_ID, "textures/screens/chapter_recipe_3_1.png");
    private static final ResourceLocation CHAPTER_RECIPE_3_2_TEXTURE =
        new ResourceLocation(TheLastSwordMod.MOD_ID, "textures/screens/chapter_recipe_3_2.png");
    private static final ResourceLocation CHAPTER_RECIPE_3_3_TEXTURE =
        new ResourceLocation(TheLastSwordMod.MOD_ID, "textures/screens/chapter_recipe_3_3.png");
    private static final ResourceLocation CHAPTER_RECIPE_3_4_TEXTURE =
        new ResourceLocation(TheLastSwordMod.MOD_ID, "textures/screens/chapter_recipe_3_4.png");
    private static final ResourceLocation CHAPTER_RECIPE_4_1_TEXTURE =
        new ResourceLocation(TheLastSwordMod.MOD_ID, "textures/screens/chapter_recipe_4_1.png");
    private static final ResourceLocation CHAPTER_RECIPE_4_2_TEXTURE =
        new ResourceLocation(TheLastSwordMod.MOD_ID, "textures/screens/chapter_recipe_4_2.png");
    private static final ResourceLocation CHAPTER_RECIPE_5_1_TEXTURE =
        new ResourceLocation(TheLastSwordMod.MOD_ID, "textures/screens/chapter_recipe_5_1.png");
    private static final ResourceLocation CHAPTER_RECIPE_5_2_TEXTURE =
        new ResourceLocation(TheLastSwordMod.MOD_ID, "textures/screens/chapter_recipe_5_2.png");
    private static final ResourceLocation CHAPTER_RECIPE_5_3_TEXTURE =
        new ResourceLocation(TheLastSwordMod.MOD_ID, "textures/screens/chapter_recipe_5_3.png");

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
        List<ResourceLocation> recipeImages; // 配方图片列表
        Map<Integer, List<Integer>> recipeImagePageMap; // 配方页索引 -> 该页要渲染的图片索引列表
    }

    //粒子数据类
    private static class ScrollParticle {
        float x, y;           // 当前位置
        float velocityX, velocityY; // 速度
        int color;            // 颜色（ARGB格式）
        int maxLife;          // 最大生命周期
        int life;             // 当前生命周期
        float size;           // 粒子大小

        ScrollParticle(float x, float y, float velocityX, float velocityY, int color, int maxLife, float size) {
            this.x = x;
            this.y = y;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.color = color;
            this.maxLife = maxLife;
            this.life = maxLife;
            this.size = size;
        }

        //更新粒子状态
        void update() {
            x += velocityX;
            y += velocityY;
            life--;
        }

        //检查粒子是否存活
        boolean isAlive() {
            return life > 0;
        }

        //获取当前透明度（0-255）
        int getAlpha() {
            return (int) (255 * ((float) life / maxLife));
        }
    }

    //翻页按钮
    private Button previousButton;
    private Button nextButton;

    //章节按钮（第一页目录）
    private final java.util.List<Button> chapterButtons = new ArrayList<>();

    //按钮粒子系统变量
    private final List<ScrollParticle> previousButtonParticles = new ArrayList<>();
    private final List<ScrollParticle> nextButtonParticles = new ArrayList<>();
    private final List<ScrollParticle> clickParticles = new ArrayList<>(); // 点击产生的临时粒子
    private boolean buttonParticlesInitialized = false; //按钮粒子初始化状态

    //粒子系统变量
    private final List<ScrollParticle> particles = new ArrayList<>();
    private boolean particleAnimationPlayed = false; //是否已播放过粒子动画
    private int animationTimer = 0; //动画计时器
    private static final int ANIMATION_DURATION = 60; //动画持续时间（60 ticks = 3秒）

    public TheLastEndScrollScreen() {
        super(Component.empty());
        // 章节初始化移到 init() 方法中执行
    }

    //初始化章节数据
    private void initializeChapters() {
        chapters.clear();

        //章节1：稀世奇材（附带配方图片）
        addChapter("gui.the_last_sword.scroll_book.chapter1.title",
                  "gui.the_last_sword.scroll_book.chapter1.content",
                  new ItemStack(ModItems.DRAGON_CRYSTAL.get()),
                  CHAPTER_RECIPE_1_1_TEXTURE);

        //章节2：铸剑台（附带两张配方图片）
        addChapter("gui.the_last_sword.scroll_book.chapter2.title",
                  "gui.the_last_sword.scroll_book.chapter2.content",
                  new ItemStack(ModItems.DRAGON_CRYSTAL_UPGRADE_TEMPLATE.get()),
                  CHAPTER_RECIPE_2_1_TEXTURE,
                  CHAPTER_RECIPE_2_2_TEXTURE);

        //章节3：传说之剑
        addChapter("gui.the_last_sword.scroll_book.chapter3.title",
                  "gui.the_last_sword.scroll_book.chapter3.content",
                  new ItemStack(ModItems.DRAGON_CRYSTAL_SWORD.get()),
                  CHAPTER_RECIPE_3_1_TEXTURE,
                  CHAPTER_RECIPE_3_2_TEXTURE,
                  CHAPTER_RECIPE_3_3_TEXTURE,
                  CHAPTER_RECIPE_3_4_TEXTURE);

        //章节4：龙之躯壳
        addChapter("gui.the_last_sword.scroll_book.chapter4.title",
                  "gui.the_last_sword.scroll_book.chapter4.content",
                  new ItemStack(ModItems.DRAGON_CRYSTAL_ARMOR_HELMET.get()),
                  CHAPTER_RECIPE_4_1_TEXTURE,
                  CHAPTER_RECIPE_4_2_TEXTURE);

        //章节5：龙与魂
        addChapter("gui.the_last_sword.scroll_book.chapter5.title",
                  "gui.the_last_sword.scroll_book.chapter5.content",
                  new ItemStack(ModItems.DRAGON_CRYSTAL_SOUL_STONE.get()),
                  CHAPTER_RECIPE_5_1_TEXTURE,
                  CHAPTER_RECIPE_5_2_TEXTURE,
                  CHAPTER_RECIPE_5_3_TEXTURE);

        //终章：虚空之下
        addChapter("gui.the_last_sword.scroll_book.chapter6.title",
                  "gui.the_last_sword.scroll_book.chapter6.content",
                  new ItemStack(ModItems.THE_LAST_SWORD.get()));
    }

    //添加章节并自动分页
    private void addChapter(String titleKey, String contentKey, ItemStack icon) {
        addChapter(titleKey, contentKey, icon, new ResourceLocation[0]);
    }

    //添加章节并自动分页（支持多张配方图片）
    private void addChapter(String titleKey, String contentKey, ItemStack icon, ResourceLocation... recipeImages) {
        ChapterData chapter = new ChapterData();
        chapter.titleKey = titleKey;
        chapter.contentKey = contentKey;
        chapter.icon = icon;
        chapter.recipeImages = new ArrayList<>();
        if (recipeImages != null && recipeImages.length > 0) {
            for (ResourceLocation img : recipeImages) {
                if (img != null) {
                    chapter.recipeImages.add(img);
                }
            }
        }

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
        //最后N页（如果有配方图片）：每张图片单独显示一页
        int firstPageLines = MAX_LINES_PER_PAGE - 3; //第一页要留空间给标题和图标
        int remainingLines = chapter.allLines.size() - firstPageLines;

        if (remainingLines <= 0) {
            chapter.pageCount = 1; //一页就够了
        } else {
            chapter.pageCount = 1 + (int) Math.ceil((double) remainingLines / MAX_LINES_PER_PAGE);
        }

        //如果有配方图片，计算需要多少页（从上往下排列，放不下再换页）
        if (!chapter.recipeImages.isEmpty()) {
            chapter.recipeImagePageMap = new java.util.HashMap<>();
            int recipePagesNeeded = calculateRecipePages(chapter);
            chapter.pageCount += recipePagesNeeded;
        }

        chapters.add(chapter);
    }

    //计算配方图片需要多少页，并构建每页的图片索引映射
    private int calculateRecipePages(ChapterData chapter) {
        if (chapter.recipeImages.isEmpty()) {
            return 0;
        }

        try {
            //加载所有图片尺寸（缩放后的高度）
            List<Integer> imageHeights = new ArrayList<>();
            for (ResourceLocation img : chapter.recipeImages) {
                var resource = Minecraft.getInstance().getResourceManager().getResource(img);
                if (resource.isPresent()) {
                    try (var inputStream = resource.get().open()) {
                        var image = com.mojang.blaze3d.platform.NativeImage.read(inputStream);
                        int scaledHeight = image.getHeight() / 2; //缩放1/2后的高度
                        imageHeights.add(scaledHeight);
                        image.close();
                    }
                } else {
                    imageHeights.add(110); //默认高度
                }
            }

            //从上往下累加图片，计算分页
            int currentPageIndex = 0;
            int currentPageHeight = 0;
            List<Integer> currentPageImages = new ArrayList<>();

            for (int i = 0; i < chapter.recipeImages.size(); i++) {
                int imageHeight = imageHeights.get(i);

                //检查是否能放在当前页
                if (currentPageHeight + imageHeight <= TEXT_HEIGHT) {
                    //能放下，加入当前页
                    currentPageImages.add(i);
                    currentPageHeight += imageHeight;
                } else {
                    //放不下，保存当前页并开始新页
                    if (!currentPageImages.isEmpty()) {
                        chapter.recipeImagePageMap.put(currentPageIndex, new ArrayList<>(currentPageImages));
                        currentPageIndex++;
                    }
                    currentPageImages.clear();
                    currentPageImages.add(i);
                    currentPageHeight = imageHeight;
                }
            }

            //保存最后一页
            if (!currentPageImages.isEmpty()) {
                chapter.recipeImagePageMap.put(currentPageIndex, currentPageImages);
                currentPageIndex++;
            }

            return currentPageIndex; //返回总页数
        } catch (Exception e) {
            //如果加载失败，使用默认分页（每张图片一页）
            for (int i = 0; i < chapter.recipeImages.size(); i++) {
                chapter.recipeImagePageMap.put(i, java.util.Collections.singletonList(i));
            }
            return chapter.recipeImages.size();
        }
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
            button -> {
                previousPage();
                if (TheLastSwordConfiguration.getTheLastEndScrollEnableParticleEffectsSafely()) {
                    spawnClickParticles(button);
                }
            }
        ).bounds(pageNumberX - buttonWidth - buttonSpacing, pageNumberY - 5, buttonWidth, buttonHeight).build();

        //下一页按钮（页码右侧）
        this.nextButton = Button.builder(
            Component.literal("▶"),
            button -> {
                nextPage();
                if (TheLastSwordConfiguration.getTheLastEndScrollEnableParticleEffectsSafely()) {
                    spawnClickParticles(button);
                }
            }
        ).bounds(pageNumberX + this.font.width(pageNumber) + buttonSpacing, pageNumberY - 5, buttonWidth, buttonHeight).build();

        this.addRenderableWidget(previousButton);
        this.addRenderableWidget(nextButton);

        updateButtons();

        //如果是第0页且没播放过主动画，生成主粒子
        if (TheLastSwordConfiguration.getTheLastEndScrollEnableParticleEffectsSafely() && currentPage == 0 && !particleAnimationPlayed) {
            spawnParticles(guiLeft, guiTop);
            particleAnimationPlayed = true;
            animationTimer = 0;
        }

        //初始化按钮粒子效果
        if (TheLastSwordConfiguration.getTheLastEndScrollEnableParticleEffectsSafely() && !buttonParticlesInitialized) {
            spawnButtonParticles();
            buttonParticlesInitialized = true;
        }
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
                button -> {
                    jumpToChapter(chapterIndex);
                    if (TheLastSwordConfiguration.getTheLastEndScrollEnableParticleEffectsSafely()) {
                        spawnClickParticles(button);
                    }
                }
            ).bounds(startX + i * (buttonWidth + buttonSpacingX), startY, buttonWidth, buttonHeight).build();

            chapterButtons.add(btn);
            this.addRenderableWidget(btn);
        }

        //第二行：3个按钮
        for (int i = 3; i < 6; i++) {
            final int chapterIndex = i;
            Button btn = Button.builder(
                Component.literal((i + 1) + ". ").append(Component.translatable("gui.the_last_sword.the_last_end_scroll.chapter_" + (i + 1))),
                button -> {
                    jumpToChapter(chapterIndex);
                    if (TheLastSwordConfiguration.getTheLastEndScrollEnableParticleEffectsSafely()) {
                        spawnClickParticles(button);
                    }
                }
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

        //渲染粒子（在内容之上）
        if (TheLastSwordConfiguration.getTheLastEndScrollEnableParticleEffectsSafely() && !particles.isEmpty()) {
            renderParticles(graphics);
        }

        //渲染按钮周围的粒子
        if (TheLastSwordConfiguration.getTheLastEndScrollEnableParticleEffectsSafely()) {
            renderButtonParticles(graphics);
        }

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

        for (int i = 0; i < chapters.size(); i++) {
            ChapterData chapter = chapters.get(i);
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

        //判断是否是配方图片页
        int recipePagesCount = currentChapter.recipeImagePageMap != null ? currentChapter.recipeImagePageMap.size() : 0;
        int textPageCount = currentChapter.pageCount - recipePagesCount;
        boolean isRecipePage = recipePagesCount > 0 && pageOffsetInChapter >= textPageCount;

        //如果是配方图片页，渲染该页的所有图片（从上往下排列）
        if (isRecipePage) {
            int recipePageIndex = pageOffsetInChapter - textPageCount;
            if (currentChapter.recipeImagePageMap.containsKey(recipePageIndex)) {
                List<Integer> imageIndices = currentChapter.recipeImagePageMap.get(recipePageIndex);
                renderRecipePageImages(graphics, guiLeft, guiTop, currentChapter, imageIndices);
            }
            return;
        }

        //否则正常渲染文本内容
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

        //如果是第6章且未加载tlsuv，渲染兼容性提示
        if (chapters.indexOf(currentChapter) == 5 && !CompatCheck.isTLSUVLoaded()) {
            Component hintText = Component.translatable("gui.the_last_sword.scroll_book.chapter6.compat_hint");
            //在文本下方留一些间距后渲染
            int hintY = textY + (lineCount * LINE_HEIGHT) + 10;
            graphics.drawString(this.font, hintText, textX, hintY, 0x3F2A1D, false);
        }
    }

    //渲染配方图片页（从上往下排列多张图片，左右居中）
    private void renderRecipePageImages(GuiGraphics graphics, int guiLeft, int guiTop, ChapterData chapter, List<Integer> imageIndices) {
        try {
            int currentY = guiTop + TEXT_START_Y; //从顶部开始

            for (int imageIndex : imageIndices) {
                if (imageIndex >= 0 && imageIndex < chapter.recipeImages.size()) {
                    ResourceLocation recipeTexture = chapter.recipeImages.get(imageIndex);

                    //动态获取图片原始尺寸
                    var resource = Minecraft.getInstance().getResourceManager().getResource(recipeTexture);
                    if (resource.isEmpty()) {
                        continue;
                    }

                    int originalWidth;
                    int originalHeight;

                    try (var inputStream = resource.get().open()) {
                        var image = com.mojang.blaze3d.platform.NativeImage.read(inputStream);
                        originalWidth = image.getWidth();
                        originalHeight = image.getHeight();
                        image.close();
                    }

                    //显示尺寸（原图等比缩放1/2）
                    int displayWidth = originalWidth / 2;
                    int displayHeight = originalHeight / 2;

                    //计算水平居中位置
                    int imageX = guiLeft + TEXT_START_X + (TEXT_WIDTH - displayWidth) / 2;

                    //渲染图片（等比缩放1/2）
                    RenderSystem.setShaderTexture(0, recipeTexture);
                    RenderSystem.setShader(GameRenderer::getPositionTexShader);
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();

                    graphics.blit(recipeTexture, imageX, currentY, displayWidth, displayHeight,
                                 0, 0, originalWidth, originalHeight, originalWidth, originalHeight);

                    RenderSystem.disableBlend();

                    //向下移动，准备渲染下一张图片
                    currentY += displayHeight;
                }
            }
        } catch (Exception e) {
            //如果加载失败，静默处理
        }
    }

    //文本自动换行（支持中英文混合和颜色代码，英文按单词边界换行）
    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return lines;
        }

        StringBuilder currentLine = new StringBuilder();
        StringBuilder currentWord = new StringBuilder(); //当前正在构建的单词
        String activeColor = ""; //当前激活的颜色代码
        boolean inWord = false; //是否在英文单词中

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            //检测颜色代码（§ + 一个字符）
            if (c == '§' && i + 1 < text.length()) {
                char colorCode = text.charAt(i + 1);
                //添加颜色代码到当前单词
                currentWord.append(c).append(colorCode);
                //更新当前激活的颜色
                if (colorCode == 'r') {
                    activeColor = ""; //重置颜色
                } else {
                    activeColor = "§" + colorCode;
                }
                i++; //跳过颜色代码的第二个字符
                continue;
            }

            //判断是否是英文字符或数字
            boolean isEnglishChar = (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9');

            if (isEnglishChar) {
                //英文字符加入当前单词
                currentWord.append(c);
                inWord = true;
            } else if (c == ' ') {
                //空格：结束当前单词，尝试加入当前行
                currentWord.append(c);
                String testLine = currentLine.toString() + currentWord.toString();

                if (this.font.width(testLine) <= maxWidth) {
                    //能放下，加入当前行
                    currentLine.append(currentWord);
                } else {
                    //放不下，换行
                    if (currentLine.length() > 0) {
                        lines.add(currentLine.toString().trim()); //去除行尾空格
                        currentLine = new StringBuilder();
                        if (!activeColor.isEmpty()) {
                            currentLine.append(activeColor);
                        }
                    }
                    //将单词加入新行（去除前导空格）
                    currentLine.append(currentWord.toString().trim());
                }
                currentWord = new StringBuilder();
                inWord = false;
            } else {
                //其他字符（中文、标点等）：先结束当前单词，然后逐字符处理
                if (currentWord.length() > 0) {
                    //先处理缓存的单词
                    String testLine = currentLine.toString() + currentWord.toString();
                    if (this.font.width(testLine) <= maxWidth) {
                        currentLine.append(currentWord);
                    } else {
                        if (currentLine.length() > 0) {
                            lines.add(currentLine.toString().trim());
                            currentLine = new StringBuilder();
                            if (!activeColor.isEmpty()) {
                                currentLine.append(activeColor);
                            }
                        }
                        currentLine.append(currentWord.toString().trim());
                    }
                    currentWord = new StringBuilder();
                }

                //处理当前字符
                String testLine = currentLine.toString() + c;
                if (this.font.width(testLine) <= maxWidth) {
                    currentLine.append(c);
                } else {
                    //中文字符可以直接换行
                    if (currentLine.length() > 0) {
                        lines.add(currentLine.toString());
                        currentLine = new StringBuilder();
                        if (!activeColor.isEmpty()) {
                            currentLine.append(activeColor);
                        }
                    }
                    currentLine.append(c);
                }
                inWord = false;
            }
        }

        //处理最后的单词
        if (currentWord.length() > 0) {
            String testLine = currentLine.toString() + currentWord.toString();
            if (this.font.width(testLine) <= maxWidth) {
                currentLine.append(currentWord);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString().trim());
                    currentLine = new StringBuilder();
                    if (!activeColor.isEmpty()) {
                        currentLine.append(activeColor);
                    }
                }
                currentLine.append(currentWord.toString().trim());
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

    //生成按钮粒子效果
    private void spawnButtonParticles() {
        java.util.Random random = new java.util.Random();

        //按钮粒子颜色（黑紫色调）
        int[] colors = {
            0x000000,  //纯黑色
            0x1A0A14,  //深黑色
            0x2D1B28,  //暗紫黑
            0x4B0082,  //深紫色
            0x800080,  //标准紫色
            0x9932CC   //深紫罗兰色
        };

        //生成上一页按钮周围的粒子（较少的粒子）
        if (previousButton != null) {
            int buttonX = previousButton.getX();
            int buttonY = previousButton.getY();
            int buttonWidth = previousButton.getWidth();
            int buttonHeight = previousButton.getHeight();

            //在按钮周围生成粒子
            int particleCount = 50; //按钮周围的粒子数量
            for (int i = 0; i < particleCount; i++) {
                //随机位置（在按钮周围区域）
                float x = buttonX - 10 + random.nextFloat() * (buttonWidth + 20);
                float y = buttonY - 10 + random.nextFloat() * (buttonHeight + 20);

                //随机速度（向外扩散）
                float centerX = buttonX + buttonWidth / 2.0f;
                float centerY = buttonY + buttonHeight / 2.0f;
                float dirX = x - centerX;
                float dirY = y - centerY;
                float dist = (float) Math.sqrt(dirX * dirX + dirY * dirY);
                if (dist > 0) {
                    dirX /= dist;
                    dirY /= dist;
                }
                float velocityX = dirX * 0.5f + (random.nextFloat() - 0.5f) * 0.3f;
                float velocityY = dirY * 0.5f + (random.nextFloat() - 0.5f) * 0.3f;

                //随机颜色
                int color = colors[random.nextInt(colors.length)];

                //生命周期（60-90 ticks，较短的持续时间）
                int maxLife = 60 + random.nextInt(31);

                //随机大小
                float size = 1.0f + random.nextFloat() * 1.2f;

                previousButtonParticles.add(new ScrollParticle(x, y, velocityX, velocityY, color, maxLife, size));
            }
        }

        //生成下一页按钮周围的粒子（较少的粒子）
        if (nextButton != null) {
            int buttonX = nextButton.getX();
            int buttonY = nextButton.getY();
            int buttonWidth = nextButton.getWidth();
            int buttonHeight = nextButton.getHeight();

            //在按钮周围生成粒子
            int particleCount = 50; //按钮周围的粒子数量
            for (int i = 0; i < particleCount; i++) {
                //随机位置（在按钮周围区域）
                float x = buttonX - 10 + random.nextFloat() * (buttonWidth + 20);
                float y = buttonY - 10 + random.nextFloat() * (buttonHeight + 20);

                //随机速度（向外扩散）
                float centerX = buttonX + buttonWidth / 2.0f;
                float centerY = buttonY + buttonHeight / 2.0f;
                float dirX = x - centerX;
                float dirY = y - centerY;
                float dist = (float) Math.sqrt(dirX * dirX + dirY * dirY);
                if (dist > 0) {
                    dirX /= dist;
                    dirY /= dist;
                }
                float velocityX = dirX * 0.5f + (random.nextFloat() - 0.5f) * 0.3f;
                float velocityY = dirY * 0.5f + (random.nextFloat() - 0.5f) * 0.3f;

                //随机颜色
                int color = colors[random.nextInt(colors.length)];

                //生命周期（60-90 ticks，较短的持续时间）
                int maxLife = 60 + random.nextInt(31);

                //随机大小
                float size = 1.0f + random.nextFloat() * 1.2f;

                nextButtonParticles.add(new ScrollParticle(x, y, velocityX, velocityY, color, maxLife, size));
            }
        }
    }

    //生成粒子（覆盖整个卷轴）
    private void spawnParticles(int guiLeft, int guiTop) {
        particles.clear();
        java.util.Random random = new java.util.Random();

        //粒子颜色（黑紫色调）
        int[] colors = {
            0x000000,  //纯黑色
            0x1A0A14,  //深黑色
            0x2D1B28,  //暗紫黑
            0x4B0082,  //深紫色
            0x800080,  //标准紫色
            0x9932CC   //深紫罗兰色
        };

        //在整个卷轴区域生成密集粒子（完整覆盖）
        int particleCount = 1500; //增加粒子数量实现更密集覆盖
        for (int i = 0; i < particleCount; i++) {
            //随机位置（覆盖整个卷轴）
            float x = guiLeft + random.nextFloat() * GUI_WIDTH;
            float y = guiTop + random.nextFloat() * GUI_HEIGHT;

            //从右向左的速度（带随机偏移）
            float velocityX = -1.5f - random.nextFloat() * 2.0f; //向左飞散
            float velocityY = (random.nextFloat() - 0.5f) * 1.5f; //随机Y轴偏移

            //随机颜色
            int color = colors[random.nextInt(colors.length)];

            //生命周期（60-120 ticks，延长持续时间）
            int maxLife = 60 + random.nextInt(61);

            //随机大小（更小更密集）
            float size = 1.0f + random.nextFloat() * 1.5f;

            particles.add(new ScrollParticle(x, y, velocityX, velocityY, color, maxLife, size));
        }
    }

    //渲染和更新粒子
    private void renderParticles(GuiGraphics graphics) {
        //更新粒子状态
        particles.removeIf(particle -> {
            particle.update();
            return !particle.isAlive();
        });

        //渲染粒子
        for (ScrollParticle particle : particles) {
            int alpha = particle.getAlpha();
            //组合颜色（ARGB格式）
            int colorWithAlpha = (alpha << 24) | (particle.color & 0x00FFFFFF);

            //绘制粒子（使用fill方法绘制矩形）
            graphics.fill(
                (int) particle.x,
                (int) particle.y,
                (int) (particle.x + particle.size),
                (int) (particle.y + particle.size),
                colorWithAlpha
            );
        }

        //增加动画计时器
        animationTimer++;
    }

    //渲染按钮周围的粒子
    private void renderButtonParticles(GuiGraphics graphics) {
        //更新上一页按钮粒子状态
        previousButtonParticles.removeIf(particle -> {
            particle.update();
            return !particle.isAlive();
        });

        //渲染上一页按钮粒子
        for (ScrollParticle particle : previousButtonParticles) {
            int alpha = particle.getAlpha();
            //组合颜色（ARGB格式）
            int colorWithAlpha = (alpha << 24) | (particle.color & 0x00FFFFFF);

            //绘制粒子（使用fill方法绘制矩形）
            graphics.fill(
                (int) particle.x,
                (int) particle.y,
                (int) (particle.x + particle.size),
                (int) (particle.y + particle.size),
                colorWithAlpha
            );
        }

        //更新下一页按钮粒子状态
        nextButtonParticles.removeIf(particle -> {
            particle.update();
            return !particle.isAlive();
        });

        //渲染下一页按钮粒子
        for (ScrollParticle particle : nextButtonParticles) {
            int alpha = particle.getAlpha();
            //组合颜色（ARGB格式）
            int colorWithAlpha = (alpha << 24) | (particle.color & 0x00FFFFFF);

            //绘制粒子（使用fill方法绘制矩形）
            graphics.fill(
                (int) particle.x,
                (int) particle.y,
                (int) (particle.x + particle.size),
                (int) (particle.y + particle.size),
                colorWithAlpha
            );
        }

        //更新点击产生的临时粒子状态
        clickParticles.removeIf(particle -> {
            particle.update();
            return !particle.isAlive();
        });

        //渲染点击产生的临时粒子
        for (ScrollParticle particle : clickParticles) {
            int alpha = particle.getAlpha();
            //组合颜色（ARGB格式）
            int colorWithAlpha = (alpha << 24) | (particle.color & 0x00FFFFFF);

            //绘制粒子（使用fill方法绘制矩形）
            graphics.fill(
                (int) particle.x,
                (int) particle.y,
                (int) (particle.x + particle.size),
                (int) (particle.y + particle.size),
                colorWithAlpha
            );
        }
    }

    //生成按钮点击时的粒子效果
    private void spawnClickParticles(Button button) {
        java.util.Random random = new java.util.Random();

        //点击粒子颜色（黑紫色调）
        int[] colors = {
            0x000000,  //纯黑色
            0x1A0A14,  //深黑色
            0x2D1B28,  //暗紫黑
            0x4B0082,  //深紫色
            0x800080,  //标准紫色
            0x9932CC   //深紫罗兰色
        };

        int buttonX = button.getX();
        int buttonY = button.getY();
        int buttonWidth = button.getWidth();
        int buttonHeight = button.getHeight();

        //在按钮周围生成粒子（100个）
        int particleCount = 100; //点击时的粒子数量
        for (int i = 0; i < particleCount; i++) {
            //随机位置（在按钮周围区域，主要是按钮范围内）
            float x = buttonX + random.nextFloat() * buttonWidth;
            float y = buttonY + random.nextFloat() * buttonHeight;

            //向外扩散的速度（从按钮中心向外）
            float centerX = buttonX + buttonWidth / 2.0f;
            float centerY = buttonY + buttonHeight / 2.0f;
            float dirX = x - centerX;
            float dirY = y - centerY;
            float dist = (float) Math.sqrt(dirX * dirX + dirY * dirY);
            if (dist > 0) {
                dirX /= dist;
                dirY /= dist;
            }
            float velocityX = dirX * 1.0f + (random.nextFloat() - 0.5f) * 0.5f; //向外扩散
            float velocityY = dirY * 1.0f + (random.nextFloat() - 0.5f) * 0.5f;

            //随机颜色
            int color = colors[random.nextInt(colors.length)];

            //生命周期（30 ticks，约1.5秒）
            int maxLife = 30;

            //随机大小
            float size = 1.0f + random.nextFloat() * 1.0f;

            clickParticles.add(new ScrollParticle(x, y, velocityX, velocityY, color, maxLife, size));
        }
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
