package net.the_last_sword.configuration;

import net.minecraftforge.fml.loading.FMLPaths;
import net.the_last_sword.util.TheLastSwordLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TheLastSwordConfigManager {

    private static final Path THE_LAST_SWORD_DIR = FMLPaths.CONFIGDIR.get().resolve("the_last_sword");
    private static final Path RECIPES_DIR = THE_LAST_SWORD_DIR.resolve("dragon_crystal_smithing_recipes");
    private static final Path DEFENCE_CONFIG_FILE = THE_LAST_SWORD_DIR.resolve("defence_config.json");

    //官方配方保留在模组资源中，config目录只存放覆盖项与自定义配方
    public static void initializeConfig() {
        createDirectories();
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

}
