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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

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

        try (Stream<Path> stream = Files.walk(recipesDir)) {
            int count = 0;
            List<Path> jsonFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(ConfigRecipeManager::isJsonFile)
                    .filter(p -> !p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".disabled.json"))
                    .toList();
            for (Path recipePath : jsonFiles) {
                try {
                    loadRecipeFile(recipesDir, recipePath);
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
    private static void loadRecipeFile(Path baseDir, Path recipePath) throws IOException {
        // 用相对路径生成唯一ID，支持嵌套文件夹。
        // 合法的小写路径保持原ID；含大写、中文或其他字符的路径使用稳定哈希ID，
        // 避免 ResourceLocation 拒绝这些文件名，但仍保留原文件路径用于日志定位。
        String relativePath = removeJsonExtension(baseDir.relativize(recipePath).toString()
                .replace('\\', '/'));

        String content = Files.readString(recipePath);
        JsonObject json = GSON.fromJson(content, JsonObject.class);

        ResourceLocation id = createRecipeId(relativePath);
        DragonCrystalSmithingSerializer serializer = new DragonCrystalSmithingSerializer();
        DragonCrystalSmithingRecipe recipe = serializer.fromJson(id, json);

        RECIPES.add(recipe);
        TheLastSwordLogger.debug("Loaded recipe: {} (file: {})", id, relativePath);
    }

    private static boolean isJsonFile(Path path) {
        return path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json");
    }

    private static String removeJsonExtension(String path) {
        return path.substring(0, path.length() - ".json".length());
    }

    private static ResourceLocation createRecipeId(String relativePath) {
        // "encoded" is reserved for escaped paths so that a normal path cannot collide with one.
        if (isValidResourcePath(relativePath) && !relativePath.startsWith("encoded/")) {
            return ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID, "config/" + relativePath);
        }

        return ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID,
                "config/encoded/" + sha256(relativePath));
    }

    private static boolean isValidResourcePath(String path) {
        if (path.isEmpty()) {
            return false;
        }
        for (int i = 0; i < path.length(); i++) {
            char character = path.charAt(i);
            boolean valid = character >= 'a' && character <= 'z'
                    || character >= '0' && character <= '9'
                    || character == '_'
                    || character == '-'
                    || character == '.'
                    || character == '/';
            if (!valid) {
                return false;
            }
        }
        return true;
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(digest.length * 2);
            for (byte valueByte : digest) {
                result.append(String.format(Locale.ROOT, "%02x", valueByte & 0xff));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
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
