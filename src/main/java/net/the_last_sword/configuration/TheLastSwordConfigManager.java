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
import java.util.Map;
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

        if ("jar".equals(uri.getScheme())) {
            try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Map.of())) {
                Path jarPath = fileSystem.getPath(resourcePath);
                walkAndCopy(jarPath, jarPath, targetDir);
            }
        } else {
            Path sourcePath = Paths.get(uri);
            walkAndCopy(sourcePath, sourcePath, targetDir);
        }
    }

    //递归遍历并复制文件
    private static void walkAndCopy(Path start, Path base, Path targetBase) throws IOException {
        try (Stream<Path> stream = Files.walk(start)) {
            stream.forEach(source -> copyPath(source, base, targetBase));
        }
    }

    //复制单个路径
    private static void copyPath(Path source, Path base, Path targetBase) {
        try {
            Path relative = base.relativize(source);
            Path target = targetBase.resolve(relative.toString());

            if (Files.isDirectory(source)) {
                Files.createDirectories(target);
            } else if (Files.notExists(target)) {
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
