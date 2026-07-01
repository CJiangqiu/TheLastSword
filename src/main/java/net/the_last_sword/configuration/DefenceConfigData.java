package net.the_last_sword.configuration;

import java.util.HashMap;
import java.util.Map;

public class DefenceConfigData {
    public int configVersion = 2;
    public HudSettings hud = new HudSettings();
    public ArmorSettings armor = new ArmorSettings();
    public JustifiedDefenceSettings justifiedDefence = new JustifiedDefenceSettings();

    // ==================== HUD 设置 ====================

    public static class HudSettings {
        public Map<String, HudElement> elements = createDefaultElements();

        private static Map<String, HudElement> createDefaultElements() {
            return new HashMap<>();
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
        public LifeSupportModule lifeSupport = new LifeSupportModule();
        public AntiGravityModule antiGravity = new AntiGravityModule();
        public PerceptionModule perception = new PerceptionModule();
        public DefenceModule defence = new DefenceModule();
        @Deprecated
        public PhasingModule phasing;
        @Deprecated
        public DragonShieldModule dragonShield;
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

    // ==================== 龙之甲模块配置 ====================

    // 维生模块
    public static class LifeSupportModule {
        public boolean enhancedBuff = true;
        public boolean enableSaturation = true;
        public boolean enableIceFireImmunity = true;
    }

    // 虚化模块
    public static class DefenceModule {
        public PhasingModule phasing = new PhasingModule();
        public DragonShieldModule dragonShield = new DragonShieldModule();
    }

    public static class PhasingModule {
        public boolean enabled = true;
        public PhasingActivationMode activationMode = PhasingActivationMode.ALWAYS;
        @Deprecated
        public ShieldEffectMode shieldEffect;
    }

    public enum PhasingActivationMode {
        ALWAYS, FLY_ONLY
    }

    public enum ShieldEffectMode {
        ON_HIT, ALWAYS, DISABLED
    }

    // 反重力模块
    public static class AntiGravityModule {
        public float flySpeed = 0.05f;
        public boolean enableInertia = true;
    }

    // ==================== 肃正防御配置 ====================

    public static class JustifiedDefenceSettings {
        public ShieldEffectMode overlayEffect = ShieldEffectMode.ALWAYS;
        public HudOffset overlayPosition = new HudOffset(0, 0);
    }

    // 感知模块
    public static class PerceptionModule {
        public boolean enableHud = true;
        public boolean scanEntities = false;
        public int scanIntervalSeconds = 5;
        public int scanRange = 32;
    }

    public static class DragonShieldModule {
        public boolean enabled = true;
        public Boolean enableDragonAura = true;
        public ShieldEffectMode shieldEffect = ShieldEffectMode.ALWAYS;
    }
}
