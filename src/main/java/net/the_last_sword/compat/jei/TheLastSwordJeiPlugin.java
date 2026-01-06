package net.the_last_sword.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.init.ModItems;
import net.the_last_sword.recipe.ConfigRecipeManager;
import net.the_last_sword.recipe.DragonCrystalSmithingRecipe;

import java.util.List;

//JEI插件主类
@JeiPlugin
public class TheLastSwordJeiPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_UID = new ResourceLocation(TheLastSwordMod.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
            new DragonCrystalSmithingCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        //从ConfigRecipeManager获取所有配方，按inputLevel排序
        List<DragonCrystalSmithingRecipe> recipes = ConfigRecipeManager.getAllRecipes();
        recipes.sort((a, b) -> Integer.compare(a.getInputLevel(), b.getInputLevel()));
        registration.addRecipes(DragonCrystalSmithingCategory.RECIPE_TYPE, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        //只注册龙晶锻造台为催化剂
        registration.addRecipeCatalyst(
            new ItemStack(ModItems.DRAGON_CRYSTAL_SMITHING_TABLE.get()),
            DragonCrystalSmithingCategory.RECIPE_TYPE
        );
    }
}
