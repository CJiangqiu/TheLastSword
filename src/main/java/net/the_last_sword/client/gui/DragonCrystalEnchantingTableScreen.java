package net.the_last_sword.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.the_last_sword.client.gui.menu.DragonCrystalEnchantingTableMenu;
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
    private int scrollOffset = 0;
    private static final int VISIBLE_ROWS = 7;
    private static final int ROW_HEIGHT = 22;
    private static final int LIST_X = 295;
    private static final int LIST_Y = 15;
    private static final int LIST_WIDTH = 115;
    private static final int MAX_LEVEL = 255;
    private static final int SLIDER_WIDTH = 50;
    private static final int SLIDER_HEIGHT = 14;

    // 经验返还系数
    private static final int XP_PER_LEVEL_REMOVED = 10;

    // 拖拽状态
    private EnchantmentOption draggingOption = null;

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

        ItemStack stack = this.menu.getSlot(1).getItem();
        if (stack.isEmpty()) return;

        Map<Enchantment, Integer> existingEnchants = EnchantmentHelper.getEnchantments(stack);

        for (Enchantment ench : ForgeRegistries.ENCHANTMENTS) {
            // 只显示物品兼容的附魔 + 物品已有的附魔
            if (ench.canEnchant(stack) || existingEnchants.containsKey(ench)) {
                int currentLevel = existingEnchants.getOrDefault(ench, 0);
                enchantmentOptions.add(new EnchantmentOption(ench, currentLevel, currentLevel));
            }
        }

        enchantmentOptions.sort((a, b) -> {
            // 已有附魔排在前面
            if (a.originalLevel > 0 && b.originalLevel == 0) return -1;
            if (a.originalLevel == 0 && b.originalLevel > 0) return 1;
            return a.enchantment.getFullname(1).getString()
                    .compareTo(b.enchantment.getFullname(1).getString());
        });
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
                total += 10240L * diff;
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
                total += XP_PER_LEVEL_REMOVED * diff;
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
        Map<ResourceLocation, Integer> enchantChanges = new HashMap<>();
        for (EnchantmentOption opt : enchantmentOptions) {
            if (opt.level != opt.originalLevel) {
                ResourceLocation id = ForgeRegistries.ENCHANTMENTS.getKey(opt.enchantment);
                if (id != null) {
                    // 发送目标等级（服务端计算差值）
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
        int listY = this.topPos + LIST_Y;

        guiGraphics.fill(listX, listY, listX + LIST_WIDTH, listY + VISIBLE_ROWS * ROW_HEIGHT, 0x80000000);

        for (int i = 0; i < VISIBLE_ROWS && i + scrollOffset < enchantmentOptions.size(); i++) {
            EnchantmentOption opt = enchantmentOptions.get(i + scrollOffset);
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
            int sliderX = listX + LIST_WIDTH - SLIDER_WIDTH - 4;
            int sliderY = rowY + 4;
            renderLevelSlider(guiGraphics, sliderX, sliderY, opt, mouseX, mouseY);
        }

        // 滚动条
        if (enchantmentOptions.size() > VISIBLE_ROWS) {
            int scrollBarHeight = VISIBLE_ROWS * ROW_HEIGHT;
            int thumbHeight = Math.max(20, scrollBarHeight * VISIBLE_ROWS / enchantmentOptions.size());
            int maxScroll = enchantmentOptions.size() - VISIBLE_ROWS;
            int thumbY = listY + (scrollBarHeight - thumbHeight) * scrollOffset / maxScroll;

            guiGraphics.fill(listX + LIST_WIDTH - 3, listY, listX + LIST_WIDTH, listY + scrollBarHeight, 0x40000000);
            guiGraphics.fill(listX + LIST_WIDTH - 3, thumbY, listX + LIST_WIDTH, thumbY + thumbHeight, 0x80FFFFFF);
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

        // 等级文本
        String levelText = String.valueOf(opt.level);
        int textWidth = this.font.width(levelText);
        guiGraphics.drawString(this.font, levelText, x + (SLIDER_WIDTH - textWidth) / 2, y + 3, 0xFFFFFF, true);
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
        int listY = this.topPos + LIST_Y;

        for (int i = 0; i < VISIBLE_ROWS && i + scrollOffset < enchantmentOptions.size(); i++) {
            int rowY = listY + i * ROW_HEIGHT;
            if (mouseX >= listX && mouseX < listX + LIST_WIDTH && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT) {
                EnchantmentOption opt = enchantmentOptions.get(i + scrollOffset);
                List<Component> tooltip = new ArrayList<>();

                tooltip.add(opt.enchantment.getFullname(Math.max(1, opt.level)));

                if (opt.originalLevel > 0) {
                    tooltip.add(Component.translatable("gui.the_last_sword.dragon_crystal_enchanting_table.current_level", opt.originalLevel)
                            .withStyle(style -> style.withColor(0xFFFF00)));
                }

                int diff = opt.level - opt.originalLevel;
                if (diff > 0) {
                    long enchCost = 10240L * diff;
                    tooltip.add(Component.literal("Lv." + opt.originalLevel + " → Lv." + opt.level)
                            .withStyle(style -> style.withColor(0x00FF00)));
                    tooltip.add(Component.translatable("gui.the_last_sword.dragon_crystal_enchanting_table.enchant_cost", formatCost(enchCost))
                            .withStyle(style -> style.withColor(0xAAAAAA)));
                } else if (diff < 0) {
                    int xp = XP_PER_LEVEL_REMOVED * (-diff);
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
        int listX = this.leftPos + LIST_X;
        int listY = this.topPos + LIST_Y;

        if (mouseX >= listX && mouseX < listX + LIST_WIDTH) {
            for (int i = 0; i < VISIBLE_ROWS && i + scrollOffset < enchantmentOptions.size(); i++) {
                int rowY = listY + i * ROW_HEIGHT;
                if (mouseY >= rowY && mouseY < rowY + ROW_HEIGHT) {
                    EnchantmentOption opt = enchantmentOptions.get(i + scrollOffset);

                    int sliderX = listX + LIST_WIDTH - SLIDER_WIDTH - 4;
                    int sliderY = rowY + 4;
                    if (mouseX >= sliderX && mouseX < sliderX + SLIDER_WIDTH
                            && mouseY >= sliderY && mouseY < sliderY + SLIDER_HEIGHT) {
                        draggingOption = opt;
                        updateSliderValue(opt, mouseX, sliderX);
                        return true;
                    }
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingOption != null) {
            int listX = this.leftPos + LIST_X;
            int sliderX = listX + LIST_WIDTH - SLIDER_WIDTH - 4;
            updateSliderValue(draggingOption, mouseX, sliderX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingOption = null;
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
        int listY = this.topPos + LIST_Y;
        int listHeight = VISIBLE_ROWS * ROW_HEIGHT;

        if (mouseX >= listX && mouseX < listX + LIST_WIDTH && mouseY >= listY && mouseY < listY + listHeight) {
            int maxScroll = Math.max(0, enchantmentOptions.size() - VISIBLE_ROWS);
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) delta));
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int key, int b, int c) {
        if (key == 256) {
            this.minecraft.player.closeContainer();
            return true;
        }
        return super.keyPressed(key, b, c);
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
