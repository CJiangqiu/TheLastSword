package net.the_last_sword.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.the_last_sword.util.TheLastSwordLogger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class DefenceConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static DefenceConfigData data = new DefenceConfigData();

    //加载配置文件
    public static void load() {
        Path configFile = TheLastSwordConfigManager.getDefenceConfigFile();

        if (!Files.exists(configFile)) {
            TheLastSwordLogger.info("Defence config file not found, creating default config");
            save();
            return;
        }

        try (Reader reader = new FileReader(configFile.toFile())) {
            DefenceConfigData loaded = GSON.fromJson(reader, DefenceConfigData.class);
            if (loaded != null) {
                data = loaded;
                TheLastSwordLogger.info("Defence config loaded successfully");
            }
        } catch (IOException e) {
            TheLastSwordLogger.error("Failed to load defence config", e);
        }
    }

    //保存配置文件
    public static void save() {
        Path configFile = TheLastSwordConfigManager.getDefenceConfigFile();

        try {
            Files.createDirectories(configFile.getParent());
        } catch (IOException e) {
            TheLastSwordLogger.error("Failed to create config directory", e);
            return;
        }

        try (Writer writer = new FileWriter(configFile.toFile())) {
            GSON.toJson(data, writer);
            TheLastSwordLogger.debug("Defence config saved successfully");
        } catch (IOException e) {
            TheLastSwordLogger.error("Failed to save defence config", e);
        }
    }

    //获取HUD元素偏移量
    public static DefenceConfigData.HudOffset getHudOffset(String elementId) {
        DefenceConfigData.HudElement element = data.hud.elements.get(elementId);
        if (element == null) {
            return new DefenceConfigData.HudOffset(0, 0);
        }
        return element.offset;
    }

    //设置HUD元素偏移量
    public static void setHudOffset(String elementId, int x, int y) {
        DefenceConfigData.HudElement element = data.hud.elements.get(elementId);
        if (element == null) {
            element = new DefenceConfigData.HudElement(x, y);
            data.hud.elements.put(elementId, element);
        } else {
            element.offset.x = x;
            element.offset.y = y;
        }
    }

    //获取配置数据（仅用于GUI）
    public static DefenceConfigData getData() {
        return data;
    }
}
