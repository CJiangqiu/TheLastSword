package net.the_last_sword.configuration;

import java.util.HashMap;
import java.util.Map;

public class DefenceConfigData {
    public int configVersion = 1;
    public HudSettings hud = new HudSettings();
    public ArmorSettings armor = new ArmorSettings();

    // ==================== HUD 设置 ====================

    public static class HudSettings {
        public Map<String, HudElement> elements = createDefaultElements();

        private static Map<String, HudElement> createDefaultElements() {
            Map<String, HudElement> map = new HashMap<>();
            map.put("justified_defence_overlay", new HudElement(0, 0));
            return map;
        }
    }

    public static class HudElement {
        public HudOffset offset;

        public HudElement(int x, int y) {
            this.offset = new HudOffset(x, y);
        }
    }

    public static class HudOffset {
        public int x;
        public int y;

        public HudOffset(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    // ==================== 盔甲设置 ====================

    public static class ArmorSettings {
        public DragonCrystalArmorConfig dragonCrystalArmor = new DragonCrystalArmorConfig();
        public DragonArmorConfig dragonArmor = new DragonArmorConfig();
    }

    // ==================== 龙水晶盔甲配置 ====================

    public static class DragonCrystalArmorConfig {
        public DragonCrystalHelmetConfig helmet = new DragonCrystalHelmetConfig();
        public DragonCrystalChestplateConfig chestplate = new DragonCrystalChestplateConfig();
        public DragonCrystalLeggingsConfig leggings = new DragonCrystalLeggingsConfig();
        public DragonCrystalBootsConfig boots = new DragonCrystalBootsConfig();
        public DragonCrystalFullSetConfig fullSet = new DragonCrystalFullSetConfig();
    }

    public static class DragonCrystalHelmetConfig {
        public boolean enableNightVision = true;
        public boolean enableWaterBreathing = true;
    }

    public static class DragonCrystalChestplateConfig {
        public boolean enableDamageResistance = true;
        public boolean enableStrength = true;
    }

    public static class DragonCrystalLeggingsConfig {
        public boolean enableRegeneration = true;
        public boolean enableJumpBoost = true;
    }

    public static class DragonCrystalBootsConfig {
        public boolean enableSpeed = true;
        public boolean enableFireResistance = true;
    }

    public static class DragonCrystalFullSetConfig {
        public boolean enableCrystalGuard = true;
    }

    // ==================== 龙之甲配置 ====================

    public static class DragonArmorConfig {
        public DragonArmorHelmetConfig helmet = new DragonArmorHelmetConfig();
        public DragonArmorChestplateConfig chestplate = new DragonArmorChestplateConfig();
        public DragonArmorLeggingsConfig leggings = new DragonArmorLeggingsConfig();
        public DragonArmorBootsConfig boots = new DragonArmorBootsConfig();
        public DragonArmorFullSetConfig fullSet = new DragonArmorFullSetConfig();
        public boolean enableHud = true;
        public float flySpeed = 0.05f;
    }

    public static class DragonArmorHelmetConfig {
        public boolean enableNightVision = true;
        public boolean enableWaterBreathing = true;
    }

    public static class DragonArmorChestplateConfig {
        public boolean enableDamageResistance = true;
        public boolean enableStrength = true;
        public boolean enableFlight = true;
    }

    public static class DragonArmorLeggingsConfig {
        public boolean enableRegeneration = true;
        public boolean enableJumpBoost = true;
    }

    public static class DragonArmorBootsConfig {
        public boolean enableSpeed = true;
        public boolean enableFireResistance = true;
    }

    public static class DragonArmorFullSetConfig {
        public boolean enableSaturation = true;
        public boolean enableIceFireImmunity = true;
        public boolean enablePhasing = true;
        public boolean enableDamageReduction = true;
    }
}
