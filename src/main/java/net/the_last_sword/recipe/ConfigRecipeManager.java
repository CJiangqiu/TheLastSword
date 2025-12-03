package net.the_last_sword.recipe;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.configuration.TheLastSwordConfigManager;
import net.the_last_sword.util.TheLastSwordLogger;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 配方配置管理器
 * 负责从config目录加载龙晶锻造台配方
 */
public class ConfigRecipeManager {

    private static final Gson GSON = new Gson();
    private static final List<DragonCrystalSmithingRecipe> RECIPES = new ArrayList<>();
    private static boolean initialized = false;

    /**
     * 加载配方文件
     * 在服务器启动时调用
     */
    public static void loadRecipes() {
        RECIPES.clear();
        initialized = false;

        Path recipesDir = TheLastSwordConfigManager.getRecipesDirectory();
        if (!Files.exists(recipesDir)) {
            TheLastSwordLogger.warn("Recipes directory does not exist: {}", recipesDir);
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(recipesDir, "*.json")) {
            int count = 0;
            for (Path recipePath : stream) {
                try {
                    loadRecipeFile(recipePath);
                    count++;
                } catch (Exception e) {
                    TheLastSwordLogger.error("Failed to load recipe from file: {}", recipePath, e);
                }
            }
            TheLastSwordLogger.info("Loaded {} dragon crystal smithing recipes from config", count);
            initialized = true;
        } catch (IOException e) {
            TheLastSwordLogger.error("Failed to read recipes directory", e);
        }
    }

    /**
     * 从文件加载单个配方
     */
    private static void loadRecipeFile(Path recipePath) throws IOException {
        String fileName = recipePath.getFileName().toString();
        String recipeId = fileName.replace(".json", "");

        String content = Files.readString(recipePath);
        JsonObject json = GSON.fromJson(content, JsonObject.class);

        ResourceLocation id = new ResourceLocation(TheLastSwordMod.MOD_ID, "config/" + recipeId);
        DragonCrystalSmithingSerializer serializer = new DragonCrystalSmithingSerializer();
        DragonCrystalSmithingRecipe recipe = serializer.fromJson(id, json);

        RECIPES.add(recipe);
        TheLastSwordLogger.debug("Loaded recipe: {}", id);
    }

    //查找匹配的配方，如果没有则返回Optional.empty()
    public static Optional<DragonCrystalSmithingRecipe> findMatchingRecipe(Container container, Level level) {
        if (!initialized) {
            TheLastSwordLogger.warn("Recipe manager not initialized, loading recipes now");
            loadRecipes();
        }

        TheLastSwordLogger.debug("Searching for matching recipe. Total loaded recipes: {}", RECIPES.size());

        for (DragonCrystalSmithingRecipe recipe : RECIPES) {
            TheLastSwordLogger.debug("Testing recipe: {} (inputLevel: {})", recipe.getId(), recipe.getInputLevel());
            if (recipe.matches(container, level)) {
                TheLastSwordLogger.debug("Found matching recipe: {}", recipe.getId());
                return Optional.of(recipe);
            }
        }

        TheLastSwordLogger.debug("No matching recipe found");
        return Optional.empty();
    }

    //获取所有已加载的配方（用于JEI显示）
    public static List<DragonCrystalSmithingRecipe> getAllRecipes() {
        if (!initialized) {
            loadRecipes();
        }
        return new ArrayList<>(RECIPES);
    }

    /**
     * 重新加载配方
     * 用于/reload命令或配置文件改变时
     */
    public static void reload() {
        TheLastSwordLogger.info("Reloading dragon crystal smithing recipes from config");
        loadRecipes();
    }

    /**
     * 检查是否已初始化
     */
    public static boolean isInitialized() {
        return initialized;
    }
}
