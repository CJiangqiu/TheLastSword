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

    private static final int CURRENT_VERSION = 2;

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
                boolean migrated = patchMissingFields(loaded);
                data = loaded;
                if (migrated || data.configVersion < CURRENT_VERSION) {
                    data.configVersion = CURRENT_VERSION;
                    save();
                    TheLastSwordLogger.info("Defence config migrated to version {}", CURRENT_VERSION);
                }
                TheLastSwordLogger.info("Defence config loaded successfully");
            }
        } catch (IOException e) {
            TheLastSwordLogger.error("Failed to load defence config", e);
        }
    }

    // 补丁 GSON 反序列化时旧 JSON 缺失字段导致的 null
    public static boolean patchMissingFields(DefenceConfigData d) {
        boolean patched = false;
        DefenceConfigData defaults = new DefenceConfigData();

        if (d.justifiedDefence == null) {
            d.justifiedDefence = defaults.justifiedDefence;
            patched = true;
        }

        // 迁移旧版 hud.elements 中的肃正防御位置到 justifiedDefence
        DefenceConfigData.HudElement oldPos = d.hud.elements.remove("justified_defence_overlay");
        if (oldPos != null) {
            d.justifiedDefence.overlayPosition = oldPos.offset;
            patched = true;
        }

        if (d.armor.dragonArmor.defence == null) {
            d.armor.dragonArmor.defence = defaults.armor.dragonArmor.defence;
            patched = true;
        }
        if (d.armor.dragonArmor.phasing != null) {
            d.armor.dragonArmor.defence.phasing = d.armor.dragonArmor.phasing;
            d.armor.dragonArmor.phasing = null;
            patched = true;
        }
        if (d.armor.dragonArmor.dragonShield != null) {
            d.armor.dragonArmor.defence.dragonShield = d.armor.dragonArmor.dragonShield;
            d.armor.dragonArmor.dragonShield = null;
            patched = true;
        }
        if (d.armor.dragonArmor.defence.phasing == null) {
            d.armor.dragonArmor.defence.phasing = defaults.armor.dragonArmor.defence.phasing;
            patched = true;
        }
        if (d.armor.dragonArmor.defence.phasing.shieldEffect == null) {
            d.armor.dragonArmor.defence.phasing.shieldEffect = defaults.armor.dragonArmor.defence.phasing.shieldEffect;
            patched = true;
        }
        if (d.armor.dragonArmor.defence.dragonShield == null) {
            d.armor.dragonArmor.defence.dragonShield = defaults.armor.dragonArmor.defence.dragonShield;
            patched = true;
        } else if (d.armor.dragonArmor.defence.dragonShield.enableDragonAura == null) {
            d.armor.dragonArmor.defence.dragonShield.enableDragonAura = defaults.armor.dragonArmor.defence.dragonShield.enableDragonAura;
            patched = true;
        }
        return patched;
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
        if ("justified_defence_overlay".equals(elementId)) {
            return data.justifiedDefence.overlayPosition;
        }
        DefenceConfigData.HudElement element = data.hud.elements.get(elementId);
        if (element == null) {
            return new DefenceConfigData.HudOffset(0, 0);
        }
        return element.offset;
    }

    //设置HUD元素偏移量
    public static void setHudOffset(String elementId, int x, int y) {
        if ("justified_defence_overlay".equals(elementId)) {
            data.justifiedDefence.overlayPosition.x = x;
            data.justifiedDefence.overlayPosition.y = y;
            return;
        }
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

    // ==================== 龙水晶盔甲配置 API ====================

    public static DefenceConfigData.DragonCrystalArmorConfig getDragonCrystalArmorConfig() {
        return data.armor.dragonCrystalArmor;
    }

    public static DefenceConfigData.DragonCrystalHelmetConfig getDragonCrystalHelmet() {
        return data.armor.dragonCrystalArmor.helmet;
    }

    public static DefenceConfigData.DragonCrystalChestplateConfig getDragonCrystalChestplate() {
        return data.armor.dragonCrystalArmor.chestplate;
    }

    public static DefenceConfigData.DragonCrystalLeggingsConfig getDragonCrystalLeggings() {
        return data.armor.dragonCrystalArmor.leggings;
    }

    public static DefenceConfigData.DragonCrystalBootsConfig getDragonCrystalBoots() {
        return data.armor.dragonCrystalArmor.boots;
    }

    public static DefenceConfigData.DragonCrystalFullSetConfig getDragonCrystalFullSet() {
        return data.armor.dragonCrystalArmor.fullSet;
    }

    // ==================== 龙之甲配置 API ====================

    public static DefenceConfigData.DragonArmorConfig getDragonArmorConfig() {
        return data.armor.dragonArmor;
    }

    public static DefenceConfigData.DragonArmorHelmetConfig getDragonArmorHelmet() {
        return data.armor.dragonArmor.helmet;
    }

    public static DefenceConfigData.DragonArmorChestplateConfig getDragonArmorChestplate() {
        return data.armor.dragonArmor.chestplate;
    }

    public static DefenceConfigData.DragonArmorLeggingsConfig getDragonArmorLeggings() {
        return data.armor.dragonArmor.leggings;
    }

    public static DefenceConfigData.DragonArmorBootsConfig getDragonArmorBoots() {
        return data.armor.dragonArmor.boots;
    }

    // ==================== 龙之甲模块 API ====================

    public static DefenceConfigData.LifeSupportModule getLifeSupportModule() {
        return data.armor.dragonArmor.lifeSupport;
    }

    public static DefenceConfigData.JustifiedDefenceSettings getJustifiedDefence() {
        return data.justifiedDefence;
    }

    public static DefenceConfigData.PhasingModule getPhasingModule() {
        return data.armor.dragonArmor.defence.phasing;
    }

    public static DefenceConfigData.AntiGravityModule getAntiGravityModule() {
        return data.armor.dragonArmor.antiGravity;
    }

    public static DefenceConfigData.PerceptionModule getPerceptionModule() {
        return data.armor.dragonArmor.perception;
    }

    public static DefenceConfigData.DragonShieldModule getDragonShieldModule() {
        return data.armor.dragonArmor.defence.dragonShield;
    }

    public static DefenceConfigData.DefenceModule getDragonArmorDefenceModule() {
        return data.armor.dragonArmor.defence;
    }
}
