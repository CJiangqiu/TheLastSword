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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 合并模组内置配方与config覆盖配方。
 */
public class ConfigRecipeManager {

    private static final String RESOURCE_DIRECTORY =
            "data/" + TheLastSwordMod.MOD_ID + "/dragon_crystal_smithing_recipes";
    private static final Pattern LEGACY_SWORD_NAME =
            Pattern.compile("^(dragon_crystal_smithing_sword_level_)(\\d+)$");
    private static final Pattern LEGACY_ARMOR_NAME =
            Pattern.compile("^(dragon_crystal_smithing_armor_level_)(\\d+)(_.+)$");
    private static final Gson GSON = new Gson();
    private static final DragonCrystalSmithingSerializer SERIALIZER = new DragonCrystalSmithingSerializer();
    private static final List<DragonCrystalSmithingRecipe> RECIPES = new ArrayList<>();
    private static boolean initialized = false;

    /**
     * 先加载内置配方，再应用config中的覆盖、禁用与自定义配方。
     */
    public static void loadRecipes() {
        RECIPES.clear();
        initialized = false;

        Map<String, DragonCrystalSmithingRecipe> mergedRecipes = new LinkedHashMap<>();
        loadBuiltInRecipes(mergedRecipes);
        int builtInCount = mergedRecipes.size();
        loadConfigRecipes(mergedRecipes);

        RECIPES.addAll(mergedRecipes.values());
        initialized = true;
        TheLastSwordLogger.info("Loaded {} built-in and {} final dragon crystal smithing recipes",
                builtInCount, RECIPES.size());
    }

    private static void loadBuiltInRecipes(Map<String, DragonCrystalSmithingRecipe> recipes) {
        URL resourceUrl = ConfigRecipeManager.class.getClassLoader().getResource(RESOURCE_DIRECTORY);
        if (resourceUrl == null) {
            TheLastSwordLogger.warn("Built-in recipe folder not found: {}", RESOURCE_DIRECTORY);
            return;
        }

        try {
            URI uri = resourceUrl.toURI();
            if ("jar".equals(uri.getScheme())) {
                loadBuiltInRecipesFromJar(uri, recipes);
            } else {
                Path directory = Paths.get(uri);
                loadRecipeDirectory(directory, (path, relativePath) -> {
                    String key = recipeKey(path.getFileName().toString());
                    recipes.put(key, readRecipe(path, createRecipeId(removeJsonExtension(relativePath))));
                });
            }
        } catch (IOException | URISyntaxException exception) {
            TheLastSwordLogger.error("Failed to load built-in dragon crystal smithing recipes", exception);
        }
    }

    private static void loadBuiltInRecipesFromJar(URI uri,
                                                   Map<String, DragonCrystalSmithingRecipe> recipes)
            throws IOException {
        FileSystem fileSystem;
        boolean closeFileSystem = false;
        try {
            fileSystem = FileSystems.newFileSystem(uri, Map.of());
            closeFileSystem = true;
        } catch (FileSystemAlreadyExistsException exception) {
            fileSystem = FileSystems.getFileSystem(uri);
        }

        try {
            Path directory = fileSystem.getPath("/" + RESOURCE_DIRECTORY);
            loadRecipeDirectory(directory, (path, relativePath) -> {
                String key = recipeKey(path.getFileName().toString());
                recipes.put(key, readRecipe(path, createRecipeId(removeJsonExtension(relativePath))));
            });
        } finally {
            if (closeFileSystem) {
                fileSystem.close();
            }
        }
    }

    private static void loadConfigRecipes(Map<String, DragonCrystalSmithingRecipe> recipes) {
        Path recipesDirectory = TheLastSwordConfigManager.getRecipesDirectory();
        if (Files.notExists(recipesDirectory)) {
            return;
        }

        Set<String> officialKeys = Set.copyOf(recipes.keySet());
        try (Stream<Path> stream = Files.walk(recipesDirectory)) {
            List<Path> configFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(ConfigRecipeManager::isRecipeFile)
                    .sorted(Comparator
                            .comparing((Path path) -> isDisabledFile(path.getFileName().toString()))
                            .thenComparing(Path::toString))
                    .toList();
            for (Path configFile : configFiles) {
                try {
                    applyConfigRecipe(recipesDirectory, configFile, officialKeys, recipes);
                } catch (Exception exception) {
                    TheLastSwordLogger.error("Failed to load recipe from file: {}", configFile, exception);
                }
            }
        } catch (IOException exception) {
            TheLastSwordLogger.error("Failed to read recipes directory", exception);
        }
    }

    private static void applyConfigRecipe(Path baseDirectory, Path recipePath, Set<String> officialKeys,
                                          Map<String, DragonCrystalSmithingRecipe> recipes) throws IOException {
        String fileName = recipePath.getFileName().toString();
        String key = recipeKey(fileName);
        String customKey = createCustomKey(baseDirectory, recipePath);
        boolean disabled = isDisabledFile(fileName);
        JsonObject json;
        try {
            json = readJson(recipePath);
        } catch (IOException | RuntimeException exception) {
            if (disabled && officialKeys.contains(key)) {
                recipes.remove(key);
                TheLastSwordLogger.info("Disabled dragon crystal smithing recipe: {}", key);
                return;
            }
            throw exception;
        }
        boolean modernFormat = hasTemplateInputLevel(json);
        String officialKey = modernFormat && officialKeys.contains(key)
                ? key
                : findLegacyOfficialKey(key, json, officialKeys);

        if (disabled) {
            String disabledKey = officialKey != null ? officialKey : customKey;
            if (recipes.remove(disabledKey) != null) {
                TheLastSwordLogger.info("Disabled dragon crystal smithing recipe: {}", disabledKey);
            }
            return;
        }

        if (officialKey != null && !modernFormat) {
            TheLastSwordLogger.info("Ignored outdated built-in recipe copy in config: {}", recipePath);
            return;
        }

        if (officialKey != null) {
            DragonCrystalSmithingRecipe officialRecipe = recipes.get(officialKey);
            ResourceLocation id = officialRecipe != null
                    ? officialRecipe.getId()
                    : createRecipeId(officialKey);
            recipes.put(officialKey, SERIALIZER.fromJson(id, json));
            TheLastSwordLogger.info("Overrode built-in dragon crystal smithing recipe: {}", officialKey);
            return;
        }

        String relativePath = removeJsonExtension(baseDirectory.relativize(recipePath).toString()
                .replace('\\', '/'));
        ResourceLocation id = createRecipeId(relativePath);
        recipes.put(customKey, SERIALIZER.fromJson(id, json));
        TheLastSwordLogger.debug("Loaded custom recipe: {} (file: {})", id, relativePath);
    }

    private static String createCustomKey(Path baseDirectory, Path recipePath) {
        Path relativePath = baseDirectory.relativize(recipePath);
        Path parent = relativePath.getParent();
        String fileKey = recipeKey(recipePath.getFileName().toString());
        String directory = parent != null ? parent.toString().replace('\\', '/').toLowerCase(Locale.ROOT) : "";
        return "custom/" + (directory.isEmpty() ? "" : directory + "/") + fileKey;
    }

    private static String findLegacyOfficialKey(String key, JsonObject json, Set<String> officialKeys) {
        if (hasTemplateInputLevel(json)) {
            return officialKeys.contains(key) ? key : null;
        }

        Integer outputLevel = getOutputLevel(json);
        if (outputLevel != null) {
            Matcher swordMatcher = LEGACY_SWORD_NAME.matcher(key);
            if (swordMatcher.matches()) {
                String migratedKey = swordMatcher.group(1) + outputLevel;
                if (officialKeys.contains(migratedKey)) {
                    return migratedKey;
                }
            }

            Matcher armorMatcher = LEGACY_ARMOR_NAME.matcher(key);
            if (armorMatcher.matches()) {
                String migratedKey = armorMatcher.group(1) + outputLevel + armorMatcher.group(3);
                if (officialKeys.contains(migratedKey)) {
                    return migratedKey;
                }
            }
        }
        return officialKeys.contains(key) ? key : null;
    }

    private static Integer getOutputLevel(JsonObject json) {
        if (!json.has("output") || !json.get("output").isJsonObject()) {
            return null;
        }
        JsonObject output = json.getAsJsonObject("output");
        return output.has("outputLevel") ? output.get("outputLevel").getAsInt() : null;
    }

    private static boolean hasTemplateInputLevel(JsonObject json) {
        return json.has("template")
                && json.get("template").isJsonObject()
                && json.getAsJsonObject("template").has("inputLevel");
    }

    private static void loadRecipeDirectory(Path directory, RecipePathConsumer consumer) throws IOException {
        try (Stream<Path> stream = Files.walk(directory)) {
            List<Path> jsonFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(ConfigRecipeManager::isJsonFile)
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
            for (Path recipePath : jsonFiles) {
                String relativePath = directory.relativize(recipePath).toString().replace('\\', '/');
                try {
                    consumer.accept(recipePath, relativePath);
                } catch (Exception exception) {
                    TheLastSwordLogger.error("Failed to load built-in recipe: {}", relativePath, exception);
                }
            }
        }
    }

    private static DragonCrystalSmithingRecipe readRecipe(Path recipePath, ResourceLocation id)
            throws IOException {
        return SERIALIZER.fromJson(id, readJson(recipePath));
    }

    private static JsonObject readJson(Path recipePath) throws IOException {
        return GSON.fromJson(Files.readString(recipePath), JsonObject.class);
    }

    private static boolean isRecipeFile(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".json") || name.endsWith(".disabled") || name.endsWith(".disable");
    }

    private static boolean isJsonFile(Path path) {
        return path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json");
    }

    private static boolean isDisabledFile(String fileName) {
        String name = fileName.toLowerCase(Locale.ROOT);
        return name.contains(".disabled") || name.endsWith(".disable");
    }

    private static String recipeKey(String fileName) {
        String name = fileName.toLowerCase(Locale.ROOT);
        boolean changed = true;
        while (changed) {
            changed = false;
            for (String suffix : new String[]{".json", ".disabled", ".disable"}) {
                if (name.endsWith(suffix)) {
                    name = name.substring(0, name.length() - suffix.length());
                    changed = true;
                }
            }
        }
        return name;
    }

    private static String removeJsonExtension(String path) {
        return path.substring(0, path.length() - ".json".length());
    }

    private static ResourceLocation createRecipeId(String relativePath) {
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

        for (DragonCrystalSmithingRecipe recipe : RECIPES) {
            if (recipe.matches(container, level)) {
                return Optional.of(recipe);
            }
        }
        return Optional.empty();
    }

    //获取服务端最终合并后的配方
    public static List<DragonCrystalSmithingRecipe> getAllRecipes() {
        if (!initialized) {
            loadRecipes();
        }
        return new ArrayList<>(RECIPES);
    }

    /**
     * 重新合并内置配方与config配方。
     */
    public static void reload() {
        TheLastSwordLogger.info("Reloading dragon crystal smithing recipes");
        loadRecipes();
    }

    public static boolean isInitialized() {
        return initialized;
    }

    @FunctionalInterface
    private interface RecipePathConsumer {
        void accept(Path recipePath, String relativePath) throws IOException;
    }
}
