package net.the_last_sword.configuration;

import net.minecraftforge.fml.loading.FMLPaths;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.util.TheLastSwordLogger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class TheLastSwordConfigManager {

    private static final Path THE_LAST_SWORD_DIR = FMLPaths.CONFIGDIR.get().resolve("the_last_sword");
    private static final Path RECIPES_DIR = THE_LAST_SWORD_DIR.resolve("dragon_crystal_smithing_recipes");
    private static final Path DEFENCE_CONFIG_FILE = THE_LAST_SWORD_DIR.resolve("defence_config.json");

    //初始化配置目录和默认配置文件
    public static void initializeConfig() {
        createDirectories();

        // 按文件级别补充：已有文件不覆盖，只补充缺失的新配方
        try {
            copyResourceDirectory("data/" + TheLastSwordMod.MOD_ID + "/dragon_crystal_smithing_recipes", RECIPES_DIR);
            TheLastSwordLogger.info("Dragon crystal smithing recipes synced to {}", RECIPES_DIR);
        } catch (Exception e) {
            TheLastSwordLogger.error("Failed to extract default recipes", e);
        }
    }

    //获取配方目录路径
    public static Path getRecipesDirectory() {
        return RECIPES_DIR;
    }

    //获取防御配置文件路径
    public static Path getDefenceConfigFile() {
        return DEFENCE_CONFIG_FILE;
    }

    //创建必要的目录
    private static void createDirectories() {
        try {
            Files.createDirectories(RECIPES_DIR);
            Files.createDirectories(THE_LAST_SWORD_DIR);
            TheLastSwordLogger.debug("Config directories created or already exist");
        } catch (IOException e) {
            TheLastSwordLogger.error("Failed to create config directories", e);
        }
    }

    //从JAR包中复制资源目录到目标目录
    private static void copyResourceDirectory(String resourcePath, Path targetDir) throws IOException, URISyntaxException {
        URL url = TheLastSwordConfigManager.class.getClassLoader().getResource(resourcePath);
        if (url == null) {
            TheLastSwordLogger.warn("Resource folder not found: {}", resourcePath);
            return;
        }

        URI uri = url.toURI();

        // 复制前先收集目标目录树中已存在的配方键（含嵌套子文件夹），用于按文件名去重
        Set<String> existingKeys = collectExistingRecipeKeys(targetDir);

        if ("jar".equals(uri.getScheme())) {
            try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Map.of())) {
                Path jarPath = fileSystem.getPath(resourcePath);
                walkAndCopy(jarPath, jarPath, targetDir, existingKeys);
            }
        } else {
            Path sourcePath = Paths.get(uri);
            walkAndCopy(sourcePath, sourcePath, targetDir, existingKeys);
        }
    }

    //递归收集目标目录树中所有已存在的配方键（含嵌套子文件夹）
    private static Set<String> collectExistingRecipeKeys(Path targetDir) {
        Set<String> keys = new HashSet<>();
        if (Files.notExists(targetDir)) {
            return keys;
        }
        try (Stream<Path> stream = Files.walk(targetDir)) {
            stream.filter(Files::isRegularFile)
                    .filter(TheLastSwordConfigManager::isRecipeFile)
                    .forEach(p -> keys.add(recipeKey(p.getFileName().toString())));
        } catch (IOException e) {
            TheLastSwordLogger.error("Failed to scan existing recipes for dedup", e);
        }
        return keys;
    }

    //判断是否为配方文件（含各种禁用命名：foo.disabled.json / foo.json.disabled / foo.json.disable）
    private static boolean isRecipeFile(Path p) {
        String n = p.getFileName().toString();
        return n.endsWith(".json") || n.endsWith(".disabled") || n.endsWith(".disable");
    }

    //归一化配方文件名为去重键：循环剥离 .json/.disabled/.disable 后缀（任意顺序），
    //使中插式(foo.disabled.json)与末尾追加式(foo.json.disabled)的禁用版都归一为同一键
    private static String recipeKey(String fileName) {
        String name = fileName;
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

    //递归遍历并复制文件
    private static void walkAndCopy(Path start, Path base, Path targetBase, Set<String> existingKeys) throws IOException {
        try (Stream<Path> stream = Files.walk(start)) {
            stream.forEach(source -> copyPath(source, base, targetBase, existingKeys));
        }
    }

    //复制单个路径
    private static void copyPath(Path source, Path base, Path targetBase, Set<String> existingKeys) {
        try {
            Path relative = base.relativize(source);
            Path target = targetBase.resolve(relative.toString());

            if (Files.isDirectory(source)) {
                Files.createDirectories(target);
                return;
            }

            // 同名去重：树内任意位置已有同名配方（含嵌套子文件夹、禁用版）则跳过，避免重复复制
            String fileName = target.getFileName().toString();
            if (fileName.endsWith(".json") && existingKeys.contains(recipeKey(fileName))) {
                return;
            }

            if (Files.notExists(target)) {
                Files.createDirectories(target.getParent());

                String resourcePath = base.resolve(relative).toString().replace('\\', '/');
                try (InputStream in = TheLastSwordConfigManager.class.getClassLoader().getResourceAsStream(resourcePath)) {
                    if (in != null) {
                        Files.copy(in, target);
                        TheLastSwordLogger.debug("Copied resource: {}", target.getFileName());
                    }
                }
            }
        } catch (IOException e) {
            TheLastSwordLogger.error("Error copying resource {} -> {}", source, targetBase, e);
        }
    }
}
