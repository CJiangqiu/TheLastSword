package net.the_last_sword.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.client.gui.menu.DragonCrystalSmithingTableMenu;
import net.the_last_sword.client.recipe.ClientDragonCrystalRecipeCache;
import net.the_last_sword.init.ModItems;
import net.the_last_sword.init.ModMenus;
import net.the_last_sword.recipe.DragonCrystalSmithingRecipe;
import net.the_last_sword.util.nbt.ItemLevelHelper;

import java.util.List;

//JEI插件主类
@JeiPlugin
public class TheLastSwordJeiPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_UID = new ResourceLocation(TheLastSwordMod.MOD_ID, "jei_plugin");
    private static IJeiRuntime runtime;
    private static List<DragonCrystalSmithingRecipe> displayedRecipes = List.of();

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    //给本mod所有物品挂等级subtype判定器: 转移匹配(Ingredient context)按level区分; 配方查找(Recipe context)不区分, 保证查升级链能看到全部等级
    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        IIngredientSubtypeInterpreter<ItemStack> levelInterpreter = (stack, context) -> {
            if (context == UidContext.Recipe) {
                return IIngredientSubtypeInterpreter.NONE;
            }
            return "lvl:" + ItemLevelHelper.getLevel(stack);
        };
        ModItems.ITEMS.getEntries().forEach(ro ->
            registration.registerSubtypeInterpreter(ro.get(), levelInterpreter)
        );
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
            new DragonCrystalSmithingCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        //config配方以服务端同步为准，禁止客户端在JEI初始化阶段读取本地config
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        ClientDragonCrystalRecipeCache.setRecipeViewerListener(TheLastSwordJeiPlugin::replaceSyncedRecipes);
    }

    @Override
    public void onRuntimeUnavailable() {
        ClientDragonCrystalRecipeCache.clearRecipeViewerListener();
        runtime = null;
        displayedRecipes = List.of();
    }

    //客户端收到服务端配方或断开连接时，动态替换JEI中的对应配方
    public static void replaceSyncedRecipes(List<DragonCrystalSmithingRecipe> recipes) {
        if (runtime == null) {
            return;
        }

        var recipeManager = runtime.getRecipeManager();
        if (!displayedRecipes.isEmpty()) {
            recipeManager.hideRecipes(DragonCrystalSmithingCategory.RECIPE_TYPE, displayedRecipes);
        }

        List<DragonCrystalSmithingRecipe> sortedRecipes = new java.util.ArrayList<>(recipes);
        sortedRecipes.sort((a, b) -> {
            int levelComparison = Integer.compare(a.getInputLevel(), b.getInputLevel());
            return levelComparison != 0 ? levelComparison : a.getId().compareTo(b.getId());
        });
        if (!sortedRecipes.isEmpty()) {
            recipeManager.addRecipes(DragonCrystalSmithingCategory.RECIPE_TYPE, sortedRecipes);
        }
        displayedRecipes = List.copyOf(sortedRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        //只注册龙晶锻造台为催化剂
        registration.addRecipeCatalyst(
            new ItemStack(ModItems.DRAGON_CRYSTAL_SMITHING_TABLE.get()),
            DragonCrystalSmithingCategory.RECIPE_TYPE
        );
    }

    //JEI配方一键填充: 配方槽 0-2(模板/基础/附加), 背包槽 4-39(27主+9快捷); 基础handler按NBT等价判断, Level标签会参与匹配, 所以等级不符会被拒
    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(
            DragonCrystalSmithingTableMenu.class,
            ModMenus.DRAGON_CRYSTAL_SMITHING_TABLE.get(),
            DragonCrystalSmithingCategory.RECIPE_TYPE,
            0, 3,
            4, 36
        );
    }
}
