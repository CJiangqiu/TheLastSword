package net.the_last_sword.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.the_last_sword.client.gui.menu.DragonCrystalEnchantingTableMenu;
import net.the_last_sword.compat.jec.JECCompat;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.network.EnchantmentApplyPacket;
import net.the_last_sword.network.NetworkHandler;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DragonCrystalEnchantingTableScreen extends AbstractContainerScreen<DragonCrystalEnchantingTableMenu> {

    private final static HashMap<String, Object> guistate = DragonCrystalEnchantingTableMenu.guistate;
    private final Level world;
    private final int x, y, z;
    private final Player entity;

    private static final ResourceLocation texture = new ResourceLocation("the_last_sword", "textures/screens/dragon_crystal_enchanting_table_gui.png");
    private static final ResourceLocation barTexture = new ResourceLocation("the_last_sword", "textures/screens/dragon_crystal_enchanting_table_gui_bar.png");

    // 附魔系统
    private final List<EnchantmentOption> enchantmentOptions = new ArrayList<>();
    private final List<EnchantmentOption> filteredOptions = new ArrayList<>();
    private int scrollOffset = 0;
    private boolean draggingScrollbar = false;
    private int scrollDragStartY = 0;
    private int scrollDragStartOffset = 0;
    private static final int VISIBLE_ROWS = 6;
    private static final int ROW_HEIGHT = 22;
    private static final int LIST_X = 295;
    private static final int SEARCH_BOX_HEIGHT = 16;
    private static final int SEARCH_BOX_MARGIN = 4;
    private static final int LIST_Y_BASE = 15;
    private static final int LIST_Y_OFFSET = SEARCH_BOX_HEIGHT + SEARCH_BOX_MARGIN;
    private static final int LIST_WIDTH = 115;
    private static final int MAX_LEVEL = 255;
    private static final int SLIDER_WIDTH = 50;
    private static final int SLIDER_HEIGHT = 14;
    private static final int LEVEL_INPUT_WIDTH = 24;
    private static final int LEVEL_INPUT_HEIGHT = 12;

    // 拖拽状态
    private EnchantmentOption draggingOption = null;

    // 搜索框
    private EditBox searchBox;

    // 等级输入框
    private EditBox levelEditBox;
    private EnchantmentOption editingOption = null;

    private Button applyButton;
    private ItemStack lastSlotItem = ItemStack.EMPTY;

    public DragonCrystalEnchantingTableScreen(DragonCrystalEnchantingTableMenu container, Inventory inventory, Component text) {
        super(container, inventory, text);
        this.world = container.world;
        this.x = container.x;
        this.y = container.y;
        this.z = container.z;
        this.entity = container.entity;
        this.imageWidth = 420;
        this.imageHeight = 200;
    }

    @Override
    public void init() {
        super.init();

        // 搜索框
        searchBox = new EditBox(this.font, this.leftPos + LIST_X, this.topPos + LIST_Y_BASE, LIST_WIDTH - 4, SEARCH_BOX_HEIGHT,
                Component.translatable("gui.the_last_sword.dragon_crystal_enchanting_table.search"));
        searchBox.setMaxLength(50);
        searchBox.setBordered(true);
        searchBox.setTextColor(0xFFFFFF);
        searchBox.setResponder(text -> {
            scrollOffset = 0;
            updateFilteredList();
        });
        this.addRenderableWidget(searchBox);

        // 等级输入框（初始隐藏）
        levelEditBox = new EditBox(this.font, 0, 0, LEVEL_INPUT_WIDTH, LEVEL_INPUT_HEIGHT, Component.empty());
        levelEditBox.setMaxLength(3);
        levelEditBox.setBordered(false);
        levelEditBox.setTextColor(0xFFFFFF);
        levelEditBox.setVisible(false);
        levelEditBox.setFilter(s -> s.isEmpty() || s.matches("\\d{0,3}"));
        this.addRenderableWidget(levelEditBox);

        applyButton = Button.builder(
                Component.translatable("gui.the_last_sword.dragon_crystal_enchanting_table.enchant"),
                button -> applyEnchantments()
        ).bounds(this.leftPos + LIST_X, this.topPos + 175, 60, 20).build();
        this.addRenderableWidget(applyButton);

        updateEnchantmentList();
    }

    @Override
    public void containerTick() {
        super.containerTick();

        ItemStack currentItem = this.menu.getSlot(1).getItem();
        if (!ItemStack.isSameItemSameTags(currentItem, lastSlotItem)) {
            lastSlotItem = currentItem.copy();
            updateEnchantmentList();
        }

        updateApplyButtonState();
    }

    // 更新可用附魔列表
    private void updateEnchantmentList() {
        enchantmentOptions.clear();
        scrollOffset = 0;
        commitLevelEdit();

        ItemStack stack = this.menu.getSlot(1).getItem();
        if (stack.isEmpty()) {
            filteredOptions.clear();
            return;
        }

        Map<Enchantment, Integer> existingEnchants = EnchantmentHelper.getEnchantments(stack);

        for (Enchantment ench : ForgeRegistries.ENCHANTMENTS) {
            if (ench.canEnchant(stack) || existingEnchants.containsKey(ench)) {
                int currentLevel = existingEnchants.getOrDefault(ench, 0);
                enchantmentOptions.add(new EnchantmentOption(ench, currentLevel, currentLevel));
            }
        }

        enchantmentOptions.sort((a, b) -> {
            if (a.originalLevel > 0 && b.originalLevel == 0) return -1;
            if (a.originalLevel == 0 && b.originalLevel > 0) return 1;
            return a.enchantment.getFullname(1).getString()
                    .compareTo(b.enchantment.getFullname(1).getString());
        });

        updateFilteredList();
    }

    // 根据搜索框内容过滤附魔列表
    private void updateFilteredList() {
        filteredOptions.clear();
        String query = searchBox != null ? searchBox.getValue().trim().toLowerCase() : "";

        for (EnchantmentOption opt : enchantmentOptions) {
            if (query.isEmpty()) {
                filteredOptions.add(opt);
            } else {
                String name = opt.enchantment.getFullname(1).getString();
                if (JECCompat.contains(name, query)) {
                    filteredOptions.add(opt);
                }
            }
        }
    }

    // 更新按钮状态
    private void updateApplyButtonState() {
        if (applyButton == null) return;

        boolean hasChanges = enchantmentOptions.stream().anyMatch(opt -> opt.level != opt.originalLevel);
        long energyCost = calculateEnergyCost();
        boolean hasEnoughEnergy = energyCost <= this.menu.getEnergy();
        boolean hasItem = !this.menu.getSlot(1).getItem().isEmpty();

        applyButton.active = hasChanges && hasEnoughEnergy && hasItem;
    }

    // 计算增加附魔的FE消耗
    private long calculateEnergyCost() {
        long total = 0;
        for (EnchantmentOption opt : enchantmentOptions) {
            int diff = opt.level - opt.originalLevel;
            if (diff > 0) {
                total += (long) TheLastSwordConfiguration.getEnchantingTableEnchantEnergyCostSafely() * diff;
            }
        }
        return total;
    }

    // 计算减少附魔返还的经验
    private int calculateXpReturn() {
        int total = 0;
        for (EnchantmentOption opt : enchantmentOptions) {
            int diff = opt.originalLevel - opt.level;
            if (diff > 0) {
                total += TheLastSwordConfiguration.getEnchantingTableRemoveXpReturnSafely() * diff;
            }
        }
        return total;
    }

    // 格式化大数字
    private String formatCost(long cost) {
        if (cost >= 1_000_000_000L) {
            return String.format("%.2fB", cost / 1_000_000_000.0);
        } else if (cost >= 1_000_000L) {
            return String.format("%.2fM", cost / 1_000_000.0);
        } else if (cost >= 1_000L) {
            return String.format("%.2fK", cost / 1_000.0);
        } else {
            return String.format("%,d", cost);
        }
    }

    // 应用附魔变更
    private void applyEnchantments() {
        commitLevelEdit();

        Map<ResourceLocation, Integer> enchantChanges = new HashMap<>();
        for (EnchantmentOption opt : enchantmentOptions) {
            if (opt.level != opt.originalLevel) {
                ResourceLocation id = ForgeRegistries.ENCHANTMENTS.getKey(opt.enchantment);
                if (id != null) {
                    enchantChanges.put(id, opt.level);
                }
            }
        }

        if (!enchantChanges.isEmpty()) {
            NetworkHandler.sendToServer(new EnchantmentApplyPacket(
                    new BlockPos(this.x, this.y, this.z),
                    enchantChanges
            ));
        }
    }

    // 提交等级输入框的值
    private void commitLevelEdit() {
        if (editingOption == null) return;

        String text = levelEditBox.getValue().trim();
        if (!text.isEmpty()) {
            try {
                int val = Integer.parseInt(text);
                editingOption.level = Math.max(0, Math.min(MAX_LEVEL, val));
            } catch (NumberFormatException ignored) {
            }
        }
        editingOption = null;
        levelEditBox.setVisible(false);
        levelEditBox.setFocused(false);
    }

    // 开始编辑某个附魔的等级
    private void startLevelEdit(EnchantmentOption opt, int editX, int editY) {
        commitLevelEdit();
        editingOption = opt;
        levelEditBox.setX(editX);
        levelEditBox.setY(editY);
        levelEditBox.setVisible(true);
        levelEditBox.setValue(String.valueOf(opt.level));
        levelEditBox.setFocused(true);
        levelEditBox.moveCursorToEnd();
        this.setFocused(levelEditBox);
    }

    // 获取列表实际起始Y坐标
    private int getListY() {
        return this.topPos + LIST_Y_BASE + LIST_Y_OFFSET;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        renderEnchantmentList(guiGraphics, mouseX, mouseY);
        renderCostInfo(guiGraphics);

        // 能量条悬停提示
        int barX = this.leftPos + 56;
        int barY = this.topPos + 15;
        if (mouseX >= barX && mouseX < barX + 32 && mouseY >= barY && mouseY < barY + 128) {
            int energy = this.menu.getEnergy();
            int maxEnergy = this.menu.getMaxEnergy();
            int totalPowerTime = this.menu.getTotalPowerTime();

            List<Component> tooltipLines = new ArrayList<>();
            tooltipLines.add(Component.literal(String.format("%,d / %,d FE", energy, maxEnergy)));
            if (totalPowerTime > 0) {
                tooltipLines.add(Component.translatable("gui.the_last_sword.dragon_crystal_enchanting_table.power_time", String.format("%,d", totalPowerTime)));
            }
            guiGraphics.renderComponentTooltip(this.font, tooltipLines, mouseX, mouseY);
        }

        renderEnchantmentTooltips(guiGraphics, mouseX, mouseY);
    }

    // 渲染附魔列表
    private void renderEnchantmentList(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int listX = this.leftPos + LIST_X;
        int listY = getListY();

        guiGraphics.fill(listX, listY, listX + LIST_WIDTH, listY + VISIBLE_ROWS * ROW_HEIGHT, 0x80000000);

        for (int i = 0; i < VISIBLE_ROWS && i + scrollOffset < filteredOptions.size(); i++) {
            EnchantmentOption opt = filteredOptions.get(i + scrollOffset);
            int rowY = listY + i * ROW_HEIGHT;

            boolean hovered = mouseX >= listX && mouseX < listX + LIST_WIDTH
                    && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT;

            // 行背景：有变更时高亮
            if (opt.level > opt.originalLevel) {
                guiGraphics.fill(listX, rowY, listX + LIST_WIDTH, rowY + ROW_HEIGHT, 0x4000FF00);
            } else if (opt.level < opt.originalLevel) {
                guiGraphics.fill(listX, rowY, listX + LIST_WIDTH, rowY + ROW_HEIGHT, 0x40FF6600);
            } else if (hovered) {
                guiGraphics.fill(listX, rowY, listX + LIST_WIDTH, rowY + ROW_HEIGHT, 0x40FFFFFF);
            }

            // 附魔名称
            String enchName = opt.enchantment.getFullname(1).getString().replace(" I", "");
            if (enchName.length() > 8) {
                enchName = enchName.substring(0, 6) + "..";
            }
            int nameColor;
            if (opt.level > opt.originalLevel) {
                nameColor = 0x00FF00;
            } else if (opt.level < opt.originalLevel) {
                nameColor = 0xFF6600;
            } else if (opt.originalLevel > 0) {
                nameColor = 0xFFFF00;
            } else {
                nameColor = 0xFFFFFF;
            }
            guiGraphics.drawString(this.font, enchName, listX + 4, rowY + 7, nameColor, false);

            // 滑块
            int sliderX = listX + LIST_WIDTH - SLIDER_WIDTH - LEVEL_INPUT_WIDTH - 8;
            int sliderY = rowY + 4;
            renderLevelSlider(guiGraphics, sliderX, sliderY, opt, mouseX, mouseY);

            // 等级输入区域（非编辑状态显示文本，编辑状态由EditBox渲染）
            int inputX = listX + LIST_WIDTH - LEVEL_INPUT_WIDTH - 4;
            int inputY = rowY + 5;
            if (editingOption != opt) {
                guiGraphics.fill(inputX, inputY, inputX + LEVEL_INPUT_WIDTH, inputY + LEVEL_INPUT_HEIGHT, 0xFF303030);
                guiGraphics.fill(inputX + 1, inputY + 1, inputX + LEVEL_INPUT_WIDTH - 1, inputY + LEVEL_INPUT_HEIGHT - 1, 0xFF1A1A1A);
                String levelText = String.valueOf(opt.level);
                int textWidth = this.font.width(levelText);
                int textColor = opt.level != opt.originalLevel ? 0x55FF55 : 0xCCCCCC;
                guiGraphics.drawString(this.font, levelText, inputX + (LEVEL_INPUT_WIDTH - textWidth) / 2, inputY + 2, textColor, false);
            }
        }

        // 滚动条
        if (filteredOptions.size() > VISIBLE_ROWS) {
            int scrollBarHeight = VISIBLE_ROWS * ROW_HEIGHT;
            int thumbHeight = Math.max(20, scrollBarHeight * VISIBLE_ROWS / filteredOptions.size());
            int maxScroll = filteredOptions.size() - VISIBLE_ROWS;
            int thumbY = listY + (scrollBarHeight - thumbHeight) * scrollOffset / maxScroll;

            guiGraphics.fill(listX + LIST_WIDTH + 2, listY, listX + LIST_WIDTH + 5, listY + scrollBarHeight, 0x40000000);
            guiGraphics.fill(listX + LIST_WIDTH + 2, thumbY, listX + LIST_WIDTH + 5, thumbY + thumbHeight, 0xFF555555);
        }
    }

    // 渲染等级滑块
    private void renderLevelSlider(GuiGraphics guiGraphics, int x, int y, EnchantmentOption opt, int mouseX, int mouseY) {
        guiGraphics.fill(x, y, x + SLIDER_WIDTH, y + SLIDER_HEIGHT, 0xFF000000);
        guiGraphics.fill(x + 1, y + 1, x + SLIDER_WIDTH - 1, y + SLIDER_HEIGHT - 1, 0xFF404040);

        // 滑块填充
        float ratio = (float) opt.level / MAX_LEVEL;
        int fillWidth = (int) ((SLIDER_WIDTH - 2) * ratio);
        if (fillWidth > 0) {
            int fillColor = opt.level > opt.originalLevel ? 0xFF00AA00 : (opt.level < opt.originalLevel ? 0xFFCC6600 : 0xFF666666);
            guiGraphics.fill(x + 1, y + 1, x + 1 + fillWidth, y + SLIDER_HEIGHT - 1, fillColor);
        }

        // 原始等级标记线
        if (opt.originalLevel > 0) {
            float origRatio = (float) opt.originalLevel / MAX_LEVEL;
            int markX = x + 1 + (int) ((SLIDER_WIDTH - 2) * origRatio);
            guiGraphics.fill(markX, y, markX + 1, y + SLIDER_HEIGHT, 0xFFFFFF00);
        }

        // 滑块手柄
        int handleX = x + 1 + (int) ((SLIDER_WIDTH - 6) * ratio);
        boolean sliderHovered = mouseX >= x && mouseX < x + SLIDER_WIDTH && mouseY >= y && mouseY < y + SLIDER_HEIGHT;
        int handleColor = sliderHovered || draggingOption == opt ? 0xFFFFFFFF : 0xFFCCCCCC;
        guiGraphics.fill(handleX, y, handleX + 4, y + SLIDER_HEIGHT, handleColor);
    }

    // 渲染消耗信息
    private void renderCostInfo(GuiGraphics guiGraphics) {
        int textX = this.leftPos + LIST_X + 65;
        int textY = this.topPos + 175;

        long energyCost = calculateEnergyCost();
        int xpReturn = calculateXpReturn();

        // FE消耗
        if (energyCost > 0) {
            String costStr = formatCost(energyCost) + " FE";
            int color = energyCost <= this.menu.getEnergy() ? 0x00FF00 : 0xFF0000;
            guiGraphics.drawString(this.font, Component.translatable("gui.the_last_sword.dragon_crystal_enchanting_table.cost", costStr), textX, textY, color, false);
            textY += 10;
        }

        // 经验返还
        if (xpReturn > 0) {
            guiGraphics.drawString(this.font, Component.translatable("gui.the_last_sword.dragon_crystal_enchanting_table.xp_return", xpReturn), textX, textY, 0x80FF80, false);
        }
    }

    // 渲染附魔项悬停提示
    private void renderEnchantmentTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int listX = this.leftPos + LIST_X;
        int listY = getListY();

        for (int i = 0; i < VISIBLE_ROWS && i + scrollOffset < filteredOptions.size(); i++) {
            int rowY = listY + i * ROW_HEIGHT;
            if (mouseX >= listX && mouseX < listX + LIST_WIDTH && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT) {
                EnchantmentOption opt = filteredOptions.get(i + scrollOffset);
                List<Component> tooltip = new ArrayList<>();

                tooltip.add(opt.enchantment.getFullname(Math.max(1, opt.level)));

                String descKey = opt.enchantment.getDescriptionId() + ".desc";
                if (Language.getInstance().has(descKey)) {
                    tooltip.add(Component.translatable(descKey).withStyle(style -> style.withColor(0xAAAAAA)));
                }

                if (opt.originalLevel > 0) {
                    tooltip.add(Component.translatable("gui.the_last_sword.dragon_crystal_enchanting_table.current_level", opt.originalLevel)
                            .withStyle(style -> style.withColor(0xFFFF00)));
                }

                int diff = opt.level - opt.originalLevel;
                if (diff > 0) {
                    long enchCost = (long) TheLastSwordConfiguration.getEnchantingTableEnchantEnergyCostSafely() * diff;
                    tooltip.add(Component.literal("Lv." + opt.originalLevel + " → Lv." + opt.level)
                            .withStyle(style -> style.withColor(0x00FF00)));
                    tooltip.add(Component.translatable("gui.the_last_sword.dragon_crystal_enchanting_table.enchant_cost", formatCost(enchCost))
                            .withStyle(style -> style.withColor(0xAAAAAA)));
                } else if (diff < 0) {
                    int xp = TheLastSwordConfiguration.getEnchantingTableRemoveXpReturnSafely() * (-diff);
                    String target = opt.level == 0
                            ? Component.translatable("gui.the_last_sword.dragon_crystal_enchanting_table.remove").getString()
                            : "Lv." + opt.level;
                    tooltip.add(Component.literal("Lv." + opt.originalLevel + " → " + target)
                            .withStyle(style -> style.withColor(0xFF6600)));
                    tooltip.add(Component.translatable("gui.the_last_sword.dragon_crystal_enchanting_table.xp_return", xp)
                            .withStyle(style -> style.withColor(0x80FF80)));
                }

                guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
                break;
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

        guiGraphics.fill(this.leftPos + 21, this.topPos + 43, this.leftPos + 21 + 16, this.topPos + 43 + 16, 0x80FF0000);
        guiGraphics.fill(this.leftPos + 201, this.topPos + 43, this.leftPos + 201 + 16, this.topPos + 43 + 16, 0x8000FF00);

        guiGraphics.blit(barTexture, this.leftPos + 56, this.topPos + 15, 0, 0, 32, 128, 32, 128);

        int energy = this.menu.getEnergy();
        int maxEnergy = this.menu.getMaxEnergy();
        if (maxEnergy > 0 && energy > 0) {
            float energyPercent = (float) energy / maxEnergy;
            int barHeight = (int) (128 * energyPercent);
            int barStartY = this.topPos + 15 + (128 - barHeight);
            renderEndPortalEffect(guiGraphics, this.leftPos + 56, barStartY, this.leftPos + 56 + 32, this.topPos + 15 + 128);
        }

        RenderSystem.disableBlend();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 搜索框点击
        if (searchBox != null && searchBox.isMouseOver(mouseX, mouseY)) {
            this.setFocused(searchBox);
            searchBox.setFocused(true);
            if (editingOption != null) {
                commitLevelEdit();
            }
            return searchBox.mouseClicked(mouseX, mouseY, button);
        }

        // 等级输入框点击
        if (levelEditBox != null && levelEditBox.isVisible() && levelEditBox.isMouseOver(mouseX, mouseY)) {
            this.setFocused(levelEditBox);
            levelEditBox.setFocused(true);
            return levelEditBox.mouseClicked(mouseX, mouseY, button);
        }

        int listX = this.leftPos + LIST_X;
        int listY = getListY();

        // 竖向滚动条拖拽开始
        if (filteredOptions.size() > VISIBLE_ROWS) {
            int scrollBarHeight = VISIBLE_ROWS * ROW_HEIGHT;
            if (mouseX >= listX + LIST_WIDTH + 2 && mouseX < listX + LIST_WIDTH + 5
                    && mouseY >= listY && mouseY < listY + scrollBarHeight) {
                draggingScrollbar = true;
                scrollDragStartY = (int) mouseY;
                scrollDragStartOffset = scrollOffset;
                return true;
            }
        }

        if (mouseX >= listX && mouseX < listX + LIST_WIDTH) {
            for (int i = 0; i < VISIBLE_ROWS && i + scrollOffset < filteredOptions.size(); i++) {
                int rowY = listY + i * ROW_HEIGHT;
                if (mouseY >= rowY && mouseY < rowY + ROW_HEIGHT) {
                    EnchantmentOption opt = filteredOptions.get(i + scrollOffset);

                    // 等级输入区域点击
                    int inputX = listX + LIST_WIDTH - LEVEL_INPUT_WIDTH - 4;
                    int inputY = rowY + 5;
                    if (mouseX >= inputX && mouseX < inputX + LEVEL_INPUT_WIDTH
                            && mouseY >= inputY && mouseY < inputY + LEVEL_INPUT_HEIGHT) {
                        startLevelEdit(opt, inputX, inputY);
                        return true;
                    }

                    // 滑块区域点击
                    int sliderX = listX + LIST_WIDTH - SLIDER_WIDTH - LEVEL_INPUT_WIDTH - 8;
                    int sliderY = rowY + 4;
                    if (mouseX >= sliderX && mouseX < sliderX + SLIDER_WIDTH
                            && mouseY >= sliderY && mouseY < sliderY + SLIDER_HEIGHT) {
                        commitLevelEdit();
                        draggingOption = opt;
                        updateSliderValue(opt, mouseX, sliderX);
                        return true;
                    }
                    return true;
                }
            }
        }

        // 点击列表外区域，提交编辑
        if (editingOption != null) {
            commitLevelEdit();
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScrollbar && filteredOptions.size() > VISIBLE_ROWS) {
            int scrollBarHeight = VISIBLE_ROWS * ROW_HEIGHT;
            int maxScroll = filteredOptions.size() - VISIBLE_ROWS;
            int thumbHeight = Math.max(20, scrollBarHeight * VISIBLE_ROWS / filteredOptions.size());
            int scrollRange = scrollBarHeight - thumbHeight;
            if (scrollRange > 0) {
                int dragDelta = (int) mouseY - scrollDragStartY;
                scrollOffset = Math.max(0, Math.min(maxScroll, scrollDragStartOffset + dragDelta * maxScroll / scrollRange));
            }
            return true;
        }
        if (draggingOption != null) {
            int listX = this.leftPos + LIST_X;
            int sliderX = listX + LIST_WIDTH - SLIDER_WIDTH - LEVEL_INPUT_WIDTH - 8;
            updateSliderValue(draggingOption, mouseX, sliderX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingOption = null;
        draggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    // 根据鼠标位置更新滑块值（范围0~255）
    private void updateSliderValue(EnchantmentOption opt, double mouseX, int sliderX) {
        double ratio = (mouseX - sliderX - 1) / (SLIDER_WIDTH - 2);
        ratio = Math.max(0, Math.min(1, ratio));
        opt.level = (int) (ratio * MAX_LEVEL);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int listX = this.leftPos + LIST_X;
        int listY = getListY();
        int listHeight = VISIBLE_ROWS * ROW_HEIGHT;

        if (mouseX >= listX && mouseX < listX + LIST_WIDTH + 5 && mouseY >= listY && mouseY < listY + listHeight) {
            int maxScroll = Math.max(0, filteredOptions.size() - VISIBLE_ROWS);
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) delta));
            // 滚动时关闭编辑框
            if (editingOption != null) {
                commitLevelEdit();
            }
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int key, int b, int c) {
        if (key == 256) {
            // 有输入框处于选中状态时，ESC 先清空内容并取消选中，不关闭界面
            if (editingOption != null && levelEditBox.isFocused()) {
                levelEditBox.setValue("");
                editingOption = null;
                levelEditBox.setVisible(false);
                levelEditBox.setFocused(false);
                this.setFocused(null);
                return true;
            }
            if (searchBox != null && searchBox.isFocused()) {
                searchBox.setValue("");
                searchBox.setFocused(false);
                this.setFocused(null);
                return true;
            }
            this.minecraft.player.closeContainer();
            return true;
        }

        // 等级输入框回车确认
        if (editingOption != null && levelEditBox.isFocused()) {
            if (key == 257 || key == 335) {
                commitLevelEdit();
                return true;
            }
            return levelEditBox.keyPressed(key, b, c);
        }

        // 搜索框获得焦点时拦截按键
        if (searchBox != null && searchBox.isFocused()) {
            return searchBox.keyPressed(key, b, c);
        }

        return super.keyPressed(key, b, c);
    }

    @Override
    public boolean charTyped(char ch, int modifiers) {
        if (editingOption != null && levelEditBox.isFocused()) {
            return levelEditBox.charTyped(ch, modifiers);
        }
        if (searchBox != null && searchBox.isFocused()) {
            return searchBox.charTyped(ch, modifiers);
        }
        return super.charTyped(ch, modifiers);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    private void renderEndPortalEffect(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        MultiBufferSource.BufferSource bufferSource = guiGraphics.bufferSource();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.endPortal());
        Matrix4f matrix = poseStack.last().pose();

        vertexConsumer.vertex(matrix, x1, y2, 0).endVertex();
        vertexConsumer.vertex(matrix, x2, y2, 0).endVertex();
        vertexConsumer.vertex(matrix, x2, y1, 0).endVertex();
        vertexConsumer.vertex(matrix, x1, y1, 0).endVertex();

        bufferSource.endBatch();
        poseStack.popPose();
    }

    // 附魔选项数据类
    private static class EnchantmentOption {
        final Enchantment enchantment;
        int level;
        final int originalLevel;

        EnchantmentOption(Enchantment enchantment, int level, int originalLevel) {
            this.enchantment = enchantment;
            this.level = level;
            this.originalLevel = originalLevel;
        }
    }
}
