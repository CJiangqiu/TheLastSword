package net.the_last_sword.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.init.ModItems;
import net.the_last_sword.recipe.DragonCrystalSmithingRecipe;
import net.the_last_sword.util.nbt.ItemLevelHelper;

import java.util.Arrays;

//龙晶锻造JEI配方类别
public class DragonCrystalSmithingCategory implements IRecipeCategory<DragonCrystalSmithingRecipe> {

    public static final ResourceLocation UID = new ResourceLocation(TheLastSwordMod.MOD_ID, "dragon_crystal_smithing");
    public static final RecipeType<DragonCrystalSmithingRecipe> RECIPE_TYPE =
        RecipeType.create(TheLastSwordMod.MOD_ID, "dragon_crystal_smithing", DragonCrystalSmithingRecipe.class);

    //箭头贴图（32x32 灰底深色箭头）
    private static final ResourceLocation ARROW_TEXTURE = new ResourceLocation(TheLastSwordMod.MOD_ID, "textures/screens/arrow.png");

    //画布尺寸 132x44; 透明底, 槽框由 JEI 自绘, 箭头用贴图
    private final IDrawable background;
    private final IDrawable slotBackground;
    private final IDrawable arrow;
    private final IDrawable icon;
    private final Component title;

    public DragonCrystalSmithingCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(132, 44);
        this.slotBackground = guiHelper.getSlotDrawable();
        this.arrow = guiHelper.drawableBuilder(ARROW_TEXTURE, 0, 0, 32, 32)
            .setTextureSize(32, 32)
            .build();
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
            new ItemStack(ModItems.DRAGON_CRYSTAL_SMITHING_TABLE.get()));
        this.title = Component.translatable("jei.category.the_last_sword.dragon_crystal_smithing");
    }

    @Override
    public RecipeType<DragonCrystalSmithingRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @SuppressWarnings("removal")
    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DragonCrystalSmithingRecipe recipe, IFocusGroup focuses) {
        //模板槽位
        builder.addSlot(RecipeIngredientRole.INPUT, 7, 11)
            .setBackground(slotBackground, -1, -1)
            .addIngredients(recipe.getTemplate());

        //输入槽位（带等级）
        ItemStack[] inputItems = recipe.getInput().getItems();
        ItemStack[] leveledInputItems = new ItemStack[inputItems.length];
        for (int i = 0; i < inputItems.length; i++) {
            leveledInputItems[i] = createStackWithLevel(inputItems[i], recipe.getInputLevel());
        }
        builder.addSlot(RecipeIngredientRole.INPUT, 26, 11)
            .setBackground(slotBackground, -1, -1)
            .addItemStacks(Arrays.asList(leveledInputItems));

        //附加材料槽位
        builder.addSlot(RecipeIngredientRole.INPUT, 44, 11)
            .setBackground(slotBackground, -1, -1)
            .addIngredients(recipe.getAddition());

        //输出槽位（带等级）
        ItemStack resultStack = createStackWithLevel(recipe.getResultItem(null), recipe.getOutputLevel());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 98, 11)
            .setBackground(slotBackground, -1, -1)
            .addItemStack(resultStack);
    }

    //箭头贴图: 附加槽右缘(x=62) 到 输出槽左缘(x=97) 之间居中, 垂直对齐槽中心
    @Override
    public void draw(DragonCrystalSmithingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 64, 4);
    }

    //创建带有指定等级的ItemStack
    private ItemStack createStackWithLevel(ItemStack baseStack, int level) {
        if (baseStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack newStack = baseStack.copy();
        newStack.setCount(1);
        ItemLevelHelper.setLevel(newStack, level);
        return newStack;
    }
}
