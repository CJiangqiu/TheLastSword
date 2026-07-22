package net.the_last_sword.client.gui.scroll;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

//卷轴配方物品框：18x18外框，内部保留原版16x16物品尺寸
public final class RecipeSlotElement {

    public static final int SIZE = 18;
    private static final int BORDER_COLOR = 0xFFFFFFFF;
    private static final int BACKGROUND_COLOR = 0xFF8F8F8F;

    private final int x;
    private final int y;
    private final ItemStack stack;

    public RecipeSlotElement(int x, int y, ItemStack stack) {
        this.x = x;
        this.y = y;
        this.stack = stack;
    }

    public void render(GuiGraphics graphics, Font font) {
        graphics.fill(x, y, x + SIZE, y + SIZE, BORDER_COLOR);
        graphics.fill(x + 1, y + 1, x + SIZE - 1, y + SIZE - 1, BACKGROUND_COLOR);
        if (!stack.isEmpty()) {
            graphics.renderItem(stack, x + 1, y + 1);
            graphics.renderItemDecorations(font, stack, x + 1, y + 1);
        }
    }

    public boolean isHovered(int mouseX, int mouseY) {
        return !stack.isEmpty()
                && mouseX >= x && mouseX < x + SIZE
                && mouseY >= y && mouseY < y + SIZE;
    }

    public ItemStack getStack() {
        return stack;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
