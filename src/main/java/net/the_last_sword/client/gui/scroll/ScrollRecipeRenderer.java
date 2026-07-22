package net.the_last_sword.client.gui.scroll;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.the_last_sword.client.recipe.ClientDragonCrystalRecipeCache;
import net.the_last_sword.mixin.SmithingTransformRecipeAccessor;
import net.the_last_sword.recipe.DragonCrystalSmithingRecipe;
import net.the_last_sword.util.nbt.ItemLevelHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//把游戏已解析的配方对象转换为卷轴中的槽位布局
public final class ScrollRecipeRenderer {

    public static final int CRAFTING_DISPLAY_HEIGHT = 60;
    //锻造配方只有一排槽位；22像素足以容纳18像素槽位和21像素高的箭头图元
    public static final int SMITHING_DISPLAY_HEIGHT = 22;
    private static final int GRID_SIZE = 3;
    private static final int ARROW_SIZE = 32;
    private static final int GAP = 10;
    private static final int ARROW_COLOR = 0xFF8A8A8A;

    private ScrollRecipeRenderer() {
    }

    public static int getDisplayHeight(ResourceLocation recipeId) {
        if (ClientDragonCrystalRecipeCache.getRecipe(recipeId).isPresent()
                || recipeId.getPath().startsWith("config/")) {
            return SMITHING_DISPLAY_HEIGHT;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            Optional<? extends Recipe<?>> recipe = minecraft.level.getRecipeManager().byKey(recipeId);
            if (recipe.isPresent()
                    && (recipe.get() instanceof SmithingTransformRecipe
                    || recipe.get() instanceof DragonCrystalSmithingRecipe)) {
                return SMITHING_DISPLAY_HEIGHT;
            }
        }
        return CRAFTING_DISPLAY_HEIGHT;
    }

    public static ItemStack render(ResourceLocation recipeId, GuiGraphics graphics, Font font,
                                   int areaX, int areaY, int areaWidth, int mouseX, int mouseY) {
        Minecraft minecraft = Minecraft.getInstance();

        Optional<DragonCrystalSmithingRecipe> dragonRecipe =
                ClientDragonCrystalRecipeCache.getRecipe(recipeId);
        if (dragonRecipe.isPresent()) {
            return renderDragonSmithing(dragonRecipe.get(), graphics, font, areaX, areaY,
                    areaWidth, mouseX, mouseY);
        }

        if (minecraft.level == null) {
            return renderMissing(recipeId, graphics, font, areaX, areaY);
        }

        Optional<? extends Recipe<?>> recipe = minecraft.level.getRecipeManager().byKey(recipeId);
        if (recipe.isEmpty()) {
            return renderMissing(recipeId, graphics, font, areaX, areaY);
        }

        Recipe<?> value = recipe.get();
        if (value instanceof ShapedRecipe shapedRecipe) {
            return renderCrafting(shapedRecipe.getIngredients(), shapedRecipe.getWidth(), shapedRecipe.getHeight(),
                    shapedRecipe.getResultItem(minecraft.level.registryAccess()), graphics, font,
                    areaX, areaY, areaWidth, mouseX, mouseY, false);
        }
        if (value instanceof ShapelessRecipe shapelessRecipe) {
            return renderCrafting(shapelessRecipe.getIngredients(), GRID_SIZE, GRID_SIZE,
                    shapelessRecipe.getResultItem(minecraft.level.registryAccess()), graphics, font,
                    areaX, areaY, areaWidth, mouseX, mouseY, true);
        }
        if (value instanceof SmithingTransformRecipe smithingRecipe) {
            SmithingTransformRecipeAccessor accessor = (SmithingTransformRecipeAccessor) smithingRecipe;
            return renderSmithing(accessor.theLastSword$getTemplate(), accessor.theLastSword$getBase(),
                    accessor.theLastSword$getAddition(),
                    smithingRecipe.getResultItem(minecraft.level.registryAccess()), graphics, font,
                    areaX, areaY, areaWidth, mouseX, mouseY, 0, 0);
        }
        if (value instanceof DragonCrystalSmithingRecipe dragonSmithingRecipe) {
            return renderDragonSmithing(dragonSmithingRecipe, graphics, font, areaX, areaY,
                    areaWidth, mouseX, mouseY);
        }

        graphics.drawString(font,
                Component.translatable("gui.the_last_sword.scroll_book.recipe_unsupported", recipeId),
                areaX, areaY + 24, 0x8B3030, false);
        return ItemStack.EMPTY;
    }

    private static ItemStack renderCrafting(NonNullList<Ingredient> ingredients, int recipeWidth, int recipeHeight,
                                            ItemStack result, GuiGraphics graphics, Font font,
                                            int areaX, int areaY, int areaWidth, int mouseX, int mouseY,
                                            boolean shapeless) {
        int gridWidth = GRID_SIZE * RecipeSlotElement.SIZE;
        int totalWidth = gridWidth + GAP + ARROW_SIZE + GAP + RecipeSlotElement.SIZE;
        int startX = areaX + (areaWidth - totalWidth) / 2;
        int startY = areaY + (CRAFTING_DISPLAY_HEIGHT - gridWidth) / 2;
        List<RecipeSlotElement> slots = new ArrayList<>();

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int column = 0; column < GRID_SIZE; column++) {
                slots.add(new RecipeSlotElement(
                        startX + column * RecipeSlotElement.SIZE,
                        startY + row * RecipeSlotElement.SIZE,
                        ItemStack.EMPTY
                ));
            }
        }

        if (shapeless) {
            placeShapelessIngredients(slots, ingredients);
        } else {
            int offsetX = Math.max(0, (GRID_SIZE - recipeWidth) / 2);
            int offsetY = Math.max(0, (GRID_SIZE - recipeHeight) / 2);
            for (int row = 0; row < recipeHeight; row++) {
                for (int column = 0; column < recipeWidth; column++) {
                    int ingredientIndex = row * recipeWidth + column;
                    int slotIndex = (row + offsetY) * GRID_SIZE + column + offsetX;
                    replaceSlot(slots, slotIndex, displayStack(ingredients.get(ingredientIndex), slotIndex));
                }
            }
        }

        int arrowX = startX + gridWidth + GAP;
        int arrowY = areaY + (CRAFTING_DISPLAY_HEIGHT - ARROW_SIZE) / 2;
        renderArrow(graphics, arrowX, arrowY);

        int resultX = arrowX + ARROW_SIZE + GAP;
        int resultY = areaY + (CRAFTING_DISPLAY_HEIGHT - RecipeSlotElement.SIZE) / 2;
        slots.add(new RecipeSlotElement(resultX, resultY, result.copy()));
        return renderSlots(slots, graphics, font, mouseX, mouseY);
    }

    private static void placeShapelessIngredients(List<RecipeSlotElement> slots,
                                                  NonNullList<Ingredient> ingredients) {
        int count = Math.min(ingredients.size(), GRID_SIZE * GRID_SIZE);
        int startIndex;
        if (count == 1) {
            startIndex = 4;
        } else if (count <= 3) {
            startIndex = 3 + (3 - count) / 2;
        } else {
            startIndex = Math.max(0, (9 - count) / 2);
        }
        for (int i = 0; i < count; i++) {
            int slotIndex = startIndex + i;
            replaceSlot(slots, slotIndex, displayStack(ingredients.get(i), slotIndex));
        }
    }

    private static void replaceSlot(List<RecipeSlotElement> slots, int index, ItemStack stack) {
        RecipeSlotElement oldSlot = slots.get(index);
        slots.set(index, new RecipeSlotElement(oldSlot.getX(), oldSlot.getY(), stack));
    }

    private static ItemStack renderDragonSmithing(DragonCrystalSmithingRecipe recipe, GuiGraphics graphics,
                                                   Font font, int areaX, int areaY, int areaWidth,
                                                   int mouseX, int mouseY) {
        ItemStack result = recipe.getResultItem(null).copy();
        ItemLevelHelper.setLevel(result, recipe.getOutputLevel());
        return renderSmithing(recipe.getTemplate(), recipe.getInput(), recipe.getAddition(), result,
                graphics, font, areaX, areaY, areaWidth, mouseX, mouseY,
                recipe.getInputLevel(), recipe.getOutputLevel());
    }

    private static ItemStack renderSmithing(Ingredient template, Ingredient base, Ingredient addition,
                                            ItemStack result, GuiGraphics graphics, Font font,
                                            int areaX, int areaY, int areaWidth, int mouseX, int mouseY,
                                            int inputLevel, int outputLevel) {
        int inputsWidth = 3 * RecipeSlotElement.SIZE;
        int totalWidth = inputsWidth + GAP + ARROW_SIZE + GAP + RecipeSlotElement.SIZE;
        int startX = areaX + (areaWidth - totalWidth) / 2;
        int slotY = areaY + (SMITHING_DISPLAY_HEIGHT - RecipeSlotElement.SIZE) / 2;
        List<RecipeSlotElement> slots = new ArrayList<>();

        ItemStack inputStack = displayStack(base, 1);
        if (!inputStack.isEmpty()) {
            ItemLevelHelper.setLevel(inputStack, inputLevel);
        }
        slots.add(new RecipeSlotElement(startX, slotY, displayStack(template, 0)));
        slots.add(new RecipeSlotElement(startX + RecipeSlotElement.SIZE, slotY, inputStack));
        slots.add(new RecipeSlotElement(startX + 2 * RecipeSlotElement.SIZE, slotY, displayStack(addition, 2)));

        int arrowX = startX + inputsWidth + GAP;
        int arrowY = areaY + (SMITHING_DISPLAY_HEIGHT - ARROW_SIZE) / 2;
        renderArrow(graphics, arrowX, arrowY);

        ItemStack outputStack = result.copy();
        if (!outputStack.isEmpty() && outputLevel != 0) {
            ItemLevelHelper.setLevel(outputStack, outputLevel);
        }
        slots.add(new RecipeSlotElement(
                arrowX + ARROW_SIZE + GAP,
                slotY,
                outputStack
        ));
        return renderSlots(slots, graphics, font, mouseX, mouseY);
    }

    private static ItemStack displayStack(Ingredient ingredient, int salt) {
        if (ingredient == null || ingredient.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack[] candidates = ingredient.getItems();
        if (candidates.length == 0) {
            return ItemStack.EMPTY;
        }
        int cycle = (int) (System.currentTimeMillis() / 1000L);
        return candidates[Math.floorMod(cycle + salt, candidates.length)].copy();
    }

    private static ItemStack renderSlots(List<RecipeSlotElement> slots, GuiGraphics graphics,
                                         Font font, int mouseX, int mouseY) {
        ItemStack hoveredStack = ItemStack.EMPTY;
        for (RecipeSlotElement slot : slots) {
            slot.render(graphics, font);
            if (slot.isHovered(mouseX, mouseY)) {
                hoveredStack = slot.getStack();
            }
        }
        return hoveredStack;
    }

    private static void renderArrow(GuiGraphics graphics, int x, int y) {
        int centerY = y + ARROW_SIZE / 2;

        //箭杆
        graphics.fill(x + 2, centerY - 3, x + 21, centerY + 4, ARROW_COLOR);

        //实心三角箭头：从宽底逐列收束到尖端
        for (int column = 0; column < 11; column++) {
            int halfHeight = 10 - column;
            int columnX = x + 20 + column;
            graphics.fill(columnX, centerY - halfHeight, columnX + 1, centerY + halfHeight + 1, ARROW_COLOR);
        }
    }

    private static ItemStack renderMissing(ResourceLocation recipeId, GuiGraphics graphics,
                                           Font font, int areaX, int areaY) {
        graphics.drawString(font,
                Component.translatable("gui.the_last_sword.scroll_book.recipe_missing", recipeId),
                areaX, areaY + 24, 0x8B3030, false);
        return ItemStack.EMPTY;
    }

}
