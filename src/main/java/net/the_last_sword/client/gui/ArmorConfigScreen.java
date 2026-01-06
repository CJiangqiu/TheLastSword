package net.the_last_sword.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.the_last_sword.init.ModItems;

//装备选择界面：选择龙水晶盔甲或龙之甲
public class ArmorConfigScreen extends Screen {
    private final Screen parent;

    //盔甲头盔物品堆（用于渲染图标）
    private ItemStack dragonCrystalHelmetStack;
    private ItemStack dragonHelmetStack;

    public ArmorConfigScreen(Screen parent) {
        super(Component.translatable("gui.the_last_sword.armor_config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        //初始化物品堆
        dragonCrystalHelmetStack = new ItemStack(ModItems.DRAGON_CRYSTAL_ARMOR_HELMET.get());
        dragonHelmetStack = new ItemStack(ModItems.DRAGON_ARMOR_HELMET.get());

        int buttonWidth = 200;
        int buttonHeight = 20;
        int centerX = this.width / 2;
        int startY = this.height / 2 - 40;

        //龙水晶盔甲按钮
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.the_last_sword.armor_config.dragon_crystal_armor"),
            button -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new DragonCrystalArmorConfigScreen(this));
                }
            }
        ).bounds(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight).build());

        //龙之甲按钮
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.the_last_sword.armor_config.dragon_armor"),
            button -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new DragonArmorConfigScreen(this));
                }
            }
        ).bounds(centerX - buttonWidth / 2, startY + 30, buttonWidth, buttonHeight).build());

        //返回按钮
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.back"),
            button -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(parent);
                }
            }
        ).bounds(centerX - buttonWidth / 2, startY + 70, buttonWidth, buttonHeight).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        //在按钮左侧渲染头盔图标
        int buttonWidth = 200;
        int centerX = this.width / 2;
        int startY = this.height / 2 - 40;
        int iconOffset = 2;  //图标与按钮的间距

        //龙水晶头盔图标
        graphics.renderItem(dragonCrystalHelmetStack,
            centerX - buttonWidth / 2 - 16 - iconOffset,
            startY + 2);

        //龙之甲头盔图标
        graphics.renderItem(dragonHelmetStack,
            centerX - buttonWidth / 2 - 16 - iconOffset,
            startY + 32);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
