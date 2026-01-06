package net.the_last_sword.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.the_last_sword.init.ModItems;

//龙水晶盔甲部件选择界面
public class DragonCrystalArmorConfigScreen extends Screen {
    private final Screen parent;

    //部件物品堆（用于渲染图标）
    private ItemStack helmetStack;
    private ItemStack chestplateStack;
    private ItemStack leggingsStack;
    private ItemStack bootsStack;

    public DragonCrystalArmorConfigScreen(Screen parent) {
        super(Component.translatable("gui.the_last_sword.dragon_crystal_armor_config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        //初始化物品堆
        helmetStack = new ItemStack(ModItems.DRAGON_CRYSTAL_ARMOR_HELMET.get());
        chestplateStack = new ItemStack(ModItems.DRAGON_CRYSTAL_ARMOR_CHESTPLATE.get());
        leggingsStack = new ItemStack(ModItems.DRAGON_CRYSTAL_ARMOR_LEGGINGS.get());
        bootsStack = new ItemStack(ModItems.DRAGON_CRYSTAL_ARMOR_BOOTS.get());

        int buttonSize = 40;
        int spacing = 10;
        int totalWidth = buttonSize * 4 + spacing * 3;
        int startX = (this.width - totalWidth) / 2;
        int startY = this.height / 2 - 30;

        //头盔按钮
        this.addRenderableWidget(Button.builder(
            Component.empty(),
            button -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new ArmorPieceConfigScreen(
                        this,
                        Component.translatable("gui.the_last_sword.dragon_crystal_armor_config.helmet"),
                        ArmorPieceConfigScreen.ArmorType.DRAGON_CRYSTAL,
                        ArmorPieceConfigScreen.ArmorSlot.HELMET
                    ));
                }
            }
        ).bounds(startX, startY, buttonSize, buttonSize).build());

        //胸甲按钮
        this.addRenderableWidget(Button.builder(
            Component.empty(),
            button -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new ArmorPieceConfigScreen(
                        this,
                        Component.translatable("gui.the_last_sword.dragon_crystal_armor_config.chestplate"),
                        ArmorPieceConfigScreen.ArmorType.DRAGON_CRYSTAL,
                        ArmorPieceConfigScreen.ArmorSlot.CHESTPLATE
                    ));
                }
            }
        ).bounds(startX + buttonSize + spacing, startY, buttonSize, buttonSize).build());

        //护腿按钮
        this.addRenderableWidget(Button.builder(
            Component.empty(),
            button -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new ArmorPieceConfigScreen(
                        this,
                        Component.translatable("gui.the_last_sword.dragon_crystal_armor_config.leggings"),
                        ArmorPieceConfigScreen.ArmorType.DRAGON_CRYSTAL,
                        ArmorPieceConfigScreen.ArmorSlot.LEGGINGS
                    ));
                }
            }
        ).bounds(startX + (buttonSize + spacing) * 2, startY, buttonSize, buttonSize).build());

        //靴子按钮
        this.addRenderableWidget(Button.builder(
            Component.empty(),
            button -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new ArmorPieceConfigScreen(
                        this,
                        Component.translatable("gui.the_last_sword.dragon_crystal_armor_config.boots"),
                        ArmorPieceConfigScreen.ArmorType.DRAGON_CRYSTAL,
                        ArmorPieceConfigScreen.ArmorSlot.BOOTS
                    ));
                }
            }
        ).bounds(startX + (buttonSize + spacing) * 3, startY, buttonSize, buttonSize).build());

        //全套效果配置按钮
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.the_last_sword.armor_config.full_set_effects"),
            button -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new ArmorPieceConfigScreen(
                        this,
                        Component.translatable("gui.the_last_sword.dragon_crystal_armor_config.full_set"),
                        ArmorPieceConfigScreen.ArmorType.DRAGON_CRYSTAL,
                        ArmorPieceConfigScreen.ArmorSlot.FULL_SET
                    ));
                }
            }
        ).bounds(this.width / 2 - 100, startY + 60, 200, 20).build());

        //返回按钮
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.back"),
            button -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(parent);
                }
            }
        ).bounds(this.width / 2 - 100, startY + 90, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        //在按钮上渲染物品图标
        int buttonSize = 40;
        int spacing = 10;
        int totalWidth = buttonSize * 4 + spacing * 3;
        int startX = (this.width - totalWidth) / 2;
        int startY = this.height / 2 - 30;
        int iconOffset = (buttonSize - 16) / 2;

        graphics.renderItem(helmetStack, startX + iconOffset, startY + iconOffset);
        graphics.renderItem(chestplateStack, startX + buttonSize + spacing + iconOffset, startY + iconOffset);
        graphics.renderItem(leggingsStack, startX + (buttonSize + spacing) * 2 + iconOffset, startY + iconOffset);
        graphics.renderItem(bootsStack, startX + (buttonSize + spacing) * 3 + iconOffset, startY + iconOffset);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
