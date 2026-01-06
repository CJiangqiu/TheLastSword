package net.the_last_sword.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
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

    //使用实际的JEI背景图片
    private static final ResourceLocation TEXTURE =
        new ResourceLocation(TheLastSwordMod.MOD_ID, "textures/screens/jei_dragon_crystal_smithing_table.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    public DragonCrystalSmithingCategory(IGuiHelper guiHelper) {
        //背景图尺寸 132x44
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 132, 44);
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
            .addIngredients(recipe.getTemplate());

        //输入槽位（带等级）
        ItemStack[] inputItems = recipe.getInput().getItems();
        ItemStack[] leveledInputItems = new ItemStack[inputItems.length];
        for (int i = 0; i < inputItems.length; i++) {
            leveledInputItems[i] = createStackWithLevel(inputItems[i], recipe.getInputLevel());
        }
        builder.addSlot(RecipeIngredientRole.INPUT, 26, 11)
            .addItemStacks(Arrays.asList(leveledInputItems));

        //附加材料槽位
        builder.addSlot(RecipeIngredientRole.INPUT, 44, 11)
            .addIngredients(recipe.getAddition());

        //输出槽位（带等级）
        ItemStack resultStack = createStackWithLevel(recipe.getResultItem(null), recipe.getOutputLevel());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 98, 11)
            .addItemStack(resultStack);
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
