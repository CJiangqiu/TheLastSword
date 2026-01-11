package net.the_last_sword.configuration;

import net.minecraftforge.common.ForgeConfigSpec;

public class TheLastSwordConfiguration {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // ═══════════════════════════════════════════════════════════════════════════════════
    // Item Configuration | 物品配置
    // ═══════════════════════════════════════════════════════════════════════════════════

    // Sword Generic | 剑类通用配置
    public static ForgeConfigSpec.ConfigValue<Double> SWORD_INCREASE_VALUE;
    public static ForgeConfigSpec.ConfigValue<Double> SWORD_INCREASE_VALUE_HIGH_LEVEL;
    public static ForgeConfigSpec.ConfigValue<Boolean> SWORD_BLOCK_CANCEL_USE;

    // Armor Generic | 盔甲类通用配置（等级区间）
    public static ForgeConfigSpec.ConfigValue<Double> ARMOR_INCREASE_LOW_LEVEL;    // < 6级
    public static ForgeConfigSpec.ConfigValue<Double> ARMOR_INCREASE_HIGH_LEVEL;   // >= 6级
    public static ForgeConfigSpec.ConfigValue<Double> TOUGHNESS_INCREASE_LOW_LEVEL;
    public static ForgeConfigSpec.ConfigValue<Double> TOUGHNESS_INCREASE_HIGH_LEVEL;
    public static ForgeConfigSpec.ConfigValue<Double> HEALTH_INCREASE_LOW_LEVEL;
    public static ForgeConfigSpec.ConfigValue<Double> HEALTH_INCREASE_HIGH_LEVEL;
    public static ForgeConfigSpec.ConfigValue<Double> JUSTIFIED_DEFENCE_INCREASE_LOW_LEVEL;
    public static ForgeConfigSpec.ConfigValue<Double> JUSTIFIED_DEFENCE_INCREASE_HIGH_LEVEL;

    // Dragon Sword | 龙之剑配置
    public static ForgeConfigSpec.ConfigValue<Integer> DRAGON_SWORD_SUMMON_COOLDOWN;
    public static ForgeConfigSpec.ConfigValue<Double> DRAGON_SWORD_SHIELD_MULTIPLIER;

    // The Last End Sword | 最终之剑配置
    public static ForgeConfigSpec.ConfigValue<Double> THE_LAST_SWORD_ABSOLUTE_DESTRUCTION_MULTIPLIER;
    public static ForgeConfigSpec.ConfigValue<Double> THE_LAST_SWORD_SHIELD_MULTIPLIER;
    public static ForgeConfigSpec.ConfigValue<Boolean> THE_LAST_SWORD_ALLOW_FLYING;
    public static ForgeConfigSpec.ConfigValue<Boolean> THE_LAST_SWORD_SUPER_DESTROY;
    public static ForgeConfigSpec.ConfigValue<Integer> THE_LAST_SWORD_MINING_RADIUS;
    public static ForgeConfigSpec.ConfigValue<Integer> THE_LAST_SWORD_SUMMON_COOLDOWN;

    // Dragon Crystal Armor | 龙晶护甲配置
    public static ForgeConfigSpec.ConfigValue<Integer> DRAGON_CRYSTAL_ARMOR_CRYSTAL_GUARD_REFRESH_INTERVAL;

    // Dragon Armor | 龙之甲配置
    public static ForgeConfigSpec.ConfigValue<Integer> DRAGON_ARMOR_ENERGY_PER_LEVEL;
    public static ForgeConfigSpec.ConfigValue<Integer> DRAGON_ARMOR_ENERGY_COST_PER_PIECE;
    public static ForgeConfigSpec.ConfigValue<Integer> DRAGON_ARMOR_PHASING_ENERGY_COST;
    public static ForgeConfigSpec.ConfigValue<Integer> DRAGON_ARMOR_ENDER_CRYSTAL_CHARGE_RATE;
    public static ForgeConfigSpec.ConfigValue<Integer> DRAGON_ARMOR_ENDER_CRYSTAL_RANGE;

    // The Last End Scroll Configuration | 终焉卷轴配置
    public static ForgeConfigSpec.ConfigValue<Boolean> THE_LAST_END_SCROLL_ENABLE_PARTICLE_EFFECTS;

    // ═══════════════════════════════════════════════════════════════════════════════════
    // Entity Configuration | 实体配置
    // ═══════════════════════════════════════════════════════════════════════════════════

    // The Last End Entity | 终焉种族配置
    public static ForgeConfigSpec.ConfigValue<Boolean> THE_LAST_END_ENTITY_ENABLE_ALL_THINGS_END;

    // The Last End Sword Wraith | 终焉剑灵配置
    public static ForgeConfigSpec.ConfigValue<Boolean> THE_LAST_END_SWORD_WRAITH_ENABLE_BATTLE_MUSIC;
    public static ForgeConfigSpec.ConfigValue<Integer> THE_LAST_END_SWORD_WRAITH_COMBAT_TELEPORT_DISTANCE;

    // The Last End Sword Wraith Skills | 终焉剑灵技能配置
    // Swift Dash | 突刺
    public static ForgeConfigSpec.ConfigValue<Double> SKILL_SWIFT_DASH_DAMAGE_MULTIPLIER;
    public static ForgeConfigSpec.ConfigValue<Double> SKILL_SWIFT_DASH_RANGE;

    // Double Strike | 双连击
    public static ForgeConfigSpec.ConfigValue<Double> SKILL_DOUBLE_STRIKE_WEIGHT;
    public static ForgeConfigSpec.ConfigValue<Double> SKILL_DOUBLE_STRIKE_DAMAGE_MULTIPLIER;
    public static ForgeConfigSpec.ConfigValue<Double> SKILL_DOUBLE_STRIKE_RANGE;

    // Cross Slash | 十字切
    public static ForgeConfigSpec.ConfigValue<Double> SKILL_CROSS_SLASH_WEIGHT;
    public static ForgeConfigSpec.ConfigValue<Double> SKILL_CROSS_SLASH_DAMAGE_MULTIPLIER;
    public static ForgeConfigSpec.ConfigValue<Double> SKILL_CROSS_SLASH_RANGE;

    // Block | 格挡
    public static ForgeConfigSpec.ConfigValue<Double> SKILL_BLOCK_WEIGHT;
    public static ForgeConfigSpec.ConfigValue<Double> SKILL_BLOCK_DAMAGE_MULTIPLIER;
    public static ForgeConfigSpec.ConfigValue<Double> SKILL_BLOCK_RANGE;

    // Moon Light Strike | 月华
    public static ForgeConfigSpec.ConfigValue<Double> SKILL_MOON_LIGHT_STRIKE_WEIGHT;
    public static ForgeConfigSpec.ConfigValue<Double> SKILL_MOON_LIGHT_STRIKE_DAMAGE_MULTIPLIER;
    public static ForgeConfigSpec.ConfigValue<Double> SKILL_MOON_LIGHT_STRIKE_RANGE;

    // Enchant | 虚空附魔
    public static ForgeConfigSpec.ConfigValue<Double> SKILL_ENCHANT_WEIGHT;
    public static ForgeConfigSpec.ConfigValue<Integer> SKILL_ENCHANT_DURATION;

    // End of All Things | 万物终焉
    public static ForgeConfigSpec.ConfigValue<Double> SKILL_END_OF_ALL_THINGS_WEIGHT;
    public static ForgeConfigSpec.ConfigValue<Double> SKILL_END_OF_ALL_THINGS_DAMAGE_MULTIPLIER;
    public static ForgeConfigSpec.ConfigValue<Double> SKILL_END_OF_ALL_THINGS_MAX_HEALTH_PERCENTAGE;
    public static ForgeConfigSpec.ConfigValue<Double> SKILL_END_OF_ALL_THINGS_RANGE;

    // Sword Wraith Generic | 通用剑灵配置
    public static ForgeConfigSpec.ConfigValue<Double> SWORD_WRAITH_HEALTH_PER_LEVEL;
    public static ForgeConfigSpec.ConfigValue<Double> SWORD_WRAITH_ATTACK_PER_LEVEL;
    public static ForgeConfigSpec.ConfigValue<Double> SWORD_WRAITH_HEALTH_PER_HIGH_LEVEL;
    public static ForgeConfigSpec.ConfigValue<Double> SWORD_WRAITH_ATTACK_PER_HIGH_LEVEL;
    public static ForgeConfigSpec.ConfigValue<Boolean> SWORD_WRAITH_FRIENDLY_FIRE_PROTECTION;
    public static ForgeConfigSpec.ConfigValue<Boolean> SWORD_WRAITH_AS_THE_LAST_END_ENTITY;
    public static ForgeConfigSpec.ConfigValue<Boolean> SWORD_WRAITH_ABSOLUTE_DESTRUCTION_DAMAGE;
    public static ForgeConfigSpec.ConfigValue<Double> SWORD_WRAITH_ABSOLUTE_DESTRUCTION_MULTIPLIER_LOW;
    public static ForgeConfigSpec.ConfigValue<Double> SWORD_WRAITH_ABSOLUTE_DESTRUCTION_MULTIPLIER_MID;
    public static ForgeConfigSpec.ConfigValue<Double> SWORD_WRAITH_ABSOLUTE_DESTRUCTION_MULTIPLIER_HIGH;

    // ═══════════════════════════════════════════════════════════════════════════════════
    // Attack Configuration | 攻击系统配置
    // ═══════════════════════════════════════════════════════════════════════════════════

    public static ForgeConfigSpec.ConfigValue<Boolean> ATTACK_ENABLE_RADICAL_LOGIC;

    // Absolute Destruction Damage | 绝对毁灭伤害配置
    public static ForgeConfigSpec.ConfigValue<Integer> ABSOLUTE_DESTRUCTION_HEAL_NEGATION_TIME;
    public static ForgeConfigSpec.ConfigValue<Integer> ABSOLUTE_DESTRUCTION_REVIVE_BAN_TIME;
    public static ForgeConfigSpec.ConfigValue<Boolean> ABSOLUTE_DESTRUCTION_DIE_MESSAGE;
    public static ForgeConfigSpec.ConfigValue<Boolean> ABSOLUTE_DESTRUCTION_PARTICLE_EFFECTS;
    public static ForgeConfigSpec.ConfigValue<Boolean> ABSOLUTE_DESTRUCTION_ENABLE_THE_LAST_END_SETDEAD;

    // ═══════════════════════════════════════════════════════════════════════════════════
    // Defence Configuration | 防御系统配置
    // ═══════════════════════════════════════════════════════════════════════════════════

    public static ForgeConfigSpec.ConfigValue<Integer> JUSTIFIED_DEFENCE_RECOVERY_TICK;
    public static ForgeConfigSpec.ConfigValue<Boolean> DEFENCE_ENABLE_RADICAL_LOGIC;
    public static ForgeConfigSpec.ConfigValue<Double> DEFENCE_CUSTOM_HEALTH_DAMAGE_REDUCTION;
    public static ForgeConfigSpec.ConfigValue<Double> DEFENCE_MAX_DAMAGE_PER_HIT;

    // ═══════════════════════════════════════════════════════════════════════════════════
    // Others Configuration | 其他配置
    // ═══════════════════════════════════════════════════════════════════════════════════

    // Buff Configuration | Buff配置
    public static ForgeConfigSpec.ConfigValue<Boolean> BUFF_VOID_ENCHANTMENT_PARTICLE_EFFECTS;
    public static ForgeConfigSpec.ConfigValue<Double> BUFF_VOID_ENCHANTMENT_DAMAGE_PERCENTAGE;
    public static ForgeConfigSpec.ConfigValue<Boolean> BUFF_PHASING_BREAK_BLOCKS_ON_END;

    // Ender Dragon Egg | 末影龙蛋配置
    public static ForgeConfigSpec.ConfigValue<Boolean> ENDER_DRAGON_EGG_DROP;
    public static ForgeConfigSpec.ConfigValue<Boolean> ENDER_DRAGON_EGG_MULTIPLE;
    public static ForgeConfigSpec.ConfigValue<Integer> ENDER_DRAGON_EGG_RADIUS;
    public static ForgeConfigSpec.ConfigValue<Integer> ENDER_DRAGON_EGG_AMOUNT;
    public static ForgeConfigSpec.ConfigValue<Boolean> ENDER_DRAGON_EGG_GIVE_TO_ABSENT_PLAYERS;

    // Stronger Ender Dragon | 更强的末影龙配置
    public static ForgeConfigSpec.ConfigValue<Integer> ENDER_DRAGON_MAX_LEVEL;
    public static ForgeConfigSpec.ConfigValue<Double> ENDER_DRAGON_HEALTH_INCREASE_VALUE;
    public static ForgeConfigSpec.ConfigValue<Double> ENDER_DRAGON_HEALTH_INCREASE_VALUE_HIGH_LEVEL;
    public static ForgeConfigSpec.ConfigValue<Double> ENDER_DRAGON_ARMOR_INCREASE_VALUE;
    public static ForgeConfigSpec.ConfigValue<Double> ENDER_DRAGON_ARMOR_INCREASE_VALUE_HIGH_LEVEL;
    public static ForgeConfigSpec.ConfigValue<Double> ENDER_DRAGON_ATTACK_INCREASE_VALUE;
    public static ForgeConfigSpec.ConfigValue<Double> ENDER_DRAGON_ATTACK_INCREASE_VALUE_HIGH_LEVEL;

    // Compat Mods | 联动Mod配置
    public static ForgeConfigSpec.ConfigValue<Boolean> COMPAT_CATACLYSM_ENABLE;

    static {
        // ═══════════════════════════════════════════════════════════════════════════════
        // Item Configuration | 物品配置
        // ═══════════════════════════════════════════════════════════════════════════════
        BUILDER.push("Item");

        // Sword Configuration | 剑配置
        BUILDER.push("Sword");

        // Generic Sword Settings | 通用剑设置
        BUILDER.push("Generic");
        SWORD_INCREASE_VALUE = BUILDER
            .comment("Damage increased per level of upgrade when level<6.")
            .define("Increase Value", 20.0);
        SWORD_INCREASE_VALUE_HIGH_LEVEL = BUILDER
            .comment("Damage increased per level of upgrade when level>=6.")
            .define("Increase Value High Level", 200.0);
        SWORD_BLOCK_CANCEL_USE = BUILDER
            .comment("Cancel sword right-click usage by blocking with shield")
            .define("Block Cancel Use", true);
        BUILDER.pop();

        // Dragon Sword Settings | 龙之剑设置
        BUILDER.push("Dragon Sword");
        DRAGON_SWORD_SUMMON_COOLDOWN = BUILDER
            .comment("Cooldown time for Dragon Sword summoning mode in ticks (20 ticks = 1 second)")
            .defineInRange("Summon Cooldown", 1200, 0, Integer.MAX_VALUE);
        DRAGON_SWORD_SHIELD_MULTIPLIER = BUILDER
            .comment("Multiplier for the absorption shield when recalling Dragon Sword Wraith")
            .define("Shield Multiplier", 0.05);
        BUILDER.pop();

        // The Last End Sword Settings | 最终之剑设置
        BUILDER.push("The Last Sword");
        THE_LAST_SWORD_ABSOLUTE_DESTRUCTION_MULTIPLIER = BUILDER
            .comment("Multiplier for absolute destruction damage")
            .define("Absolute Destruction Multiplier", 0.5);
        THE_LAST_SWORD_SHIELD_MULTIPLIER = BUILDER
            .comment("Multiplier for the absorption shield when recalling wraith")
            .define("Shield Multiplier", 0.05);
        THE_LAST_SWORD_ALLOW_FLYING = BUILDER
            .comment("Allows players to fly when holding The Last Sword")
            .define("Allow Flying", true);
        THE_LAST_SWORD_SUPER_DESTROY = BUILDER
            .comment("Force destruction of all blocks")
            .define("Super Destroy", true);
        THE_LAST_SWORD_MINING_RADIUS = BUILDER
            .comment("Mining radius for Powerful Mining Mode")
            .defineInRange("Mining Radius", 3, 1, 12);
        THE_LAST_SWORD_SUMMON_COOLDOWN = BUILDER
            .comment("Cooldown time for summoning mode in ticks (20 ticks = 1 second)")
            .defineInRange("Summon Cooldown", 600, 0, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.pop(); // End Sword

        // Armor Configuration | 护甲配置
        BUILDER.push("Armor");

        // Generic Armor Settings | 通用盔甲设置
        BUILDER.push("Generic");
        ARMOR_INCREASE_LOW_LEVEL = BUILDER
            .comment("Armor value increased per level when level<6.")
            .define("Armor Increase Low Level", 1.0);
        ARMOR_INCREASE_HIGH_LEVEL = BUILDER
            .comment("Armor value increased per level when level>=6.")
            .define("Armor Increase High Level", 2.0);
        TOUGHNESS_INCREASE_LOW_LEVEL = BUILDER
            .comment("Armor toughness increased per level when level<6.")
            .define("Toughness Increase Low Level", 1.0);
        TOUGHNESS_INCREASE_HIGH_LEVEL = BUILDER
            .comment("Armor toughness increased per level when level>=6.")
            .define("Toughness Increase High Level", 2.0);
        HEALTH_INCREASE_LOW_LEVEL = BUILDER
            .comment("Max health increased per level when level<6.")
            .define("Health Increase Low Level", 2.0);
        HEALTH_INCREASE_HIGH_LEVEL = BUILDER
            .comment("Max health increased per level when level>=6.")
            .define("Health Increase High Level", 4.0);
        JUSTIFIED_DEFENCE_INCREASE_LOW_LEVEL = BUILDER
            .comment("Justified defence increased per level when level<6.")
            .define("Justified Defence Increase Low Level", 1.0);
        JUSTIFIED_DEFENCE_INCREASE_HIGH_LEVEL = BUILDER
            .comment("Justified defence increased per level when level>=6.")
            .define("Justified Defence Increase High Level", 2.0);
        BUILDER.pop();

        // Dragon Crystal Armor Settings | 龙晶护甲设置
        BUILDER.push("Dragon Crystal Armor");
        DRAGON_CRYSTAL_ARMOR_CRYSTAL_GUARD_REFRESH_INTERVAL = BUILDER
            .comment("Crystal Guard refresh interval in ticks (1200 ticks = 60 seconds)")
            .define("Crystal Guard Refresh Interval", 1200);
        BUILDER.pop();

        // Dragon Armor Settings | 龙之甲设置
        BUILDER.push("Dragon Armor");
        DRAGON_ARMOR_ENERGY_PER_LEVEL = BUILDER
            .comment("Energy capacity increase per level (FE)",
                     "Total capacity = Base (1,048,576) + Level × This value")
            .defineInRange("Energy Per Level", 102400, 0, Integer.MAX_VALUE);
        DRAGON_ARMOR_ENERGY_COST_PER_PIECE = BUILDER
            .comment("Energy consumption per armor piece per tick (1 tick = 1/20 second)")
            .defineInRange("Energy Cost Per Piece", 1024, 0, Integer.MAX_VALUE);
        DRAGON_ARMOR_PHASING_ENERGY_COST = BUILDER
            .comment("Additional energy consumption per tick when in Phasing state (flying)")
            .defineInRange("Phasing Energy Cost", 1024, 0, Integer.MAX_VALUE);
        DRAGON_ARMOR_ENDER_CRYSTAL_CHARGE_RATE = BUILDER
            .comment("Energy charge rate from End Crystals per tick (FE)",
                     "End Crystals will charge all items with energy capability")
            .defineInRange("Ender Crystal Charge Rate", 200, 0, Integer.MAX_VALUE);
        DRAGON_ARMOR_ENDER_CRYSTAL_RANGE = BUILDER
            .comment("Detection range for End Crystal charging (blocks)",
                     "Players within this range wearing Dragon Armor will be charged")
            .defineInRange("Ender Crystal Range", 8, 2, 32);
        BUILDER.pop();

        // The Last End Scroll Configuration | 终焉卷轴配置
        BUILDER.push("The Last End Scroll");
        THE_LAST_END_SCROLL_ENABLE_PARTICLE_EFFECTS = BUILDER
            .comment("Enable particle effects in The Last End Scroll GUI")
            .define("Enable Particle Effects", true);
        BUILDER.pop();

        BUILDER.pop(); // End Armor
        BUILDER.pop(); // End Item

        // ═══════════════════════════════════════════════════════════════════════════════
        // Entity Configuration | 实体配置
        // ═══════════════════════════════════════════════════════════════════════════════
        BUILDER.push("Entity");

        // The Last End Entity Settings | 终焉实体设置
        BUILDER.push("The Last End Entity");
        THE_LAST_END_ENTITY_ENABLE_ALL_THINGS_END = BUILDER
            .comment("Enable automatic All Things End state activation")
            .define("Enable All Things End Auto Trigger", true);
        BUILDER.pop();

        // The Last End Sword Wraith Settings | 终焉剑灵设置
        BUILDER.push("The Last End Sword Wraith");
        THE_LAST_END_SWORD_WRAITH_ENABLE_BATTLE_MUSIC = BUILDER
            .comment("Enable battle music when The Last End Sword Wraith in battle")
            .define("Enable Spawn Music", true);
        THE_LAST_END_SWORD_WRAITH_COMBAT_TELEPORT_DISTANCE = BUILDER
            .comment("Force teleport to owner when combat distance exceeds this value (blocks)",
                     "Set to 0 to disable this feature")
            .defineInRange("Combat Teleport Distance", 64, 0, 256);

        // Skills Configuration | 技能配置
        BUILDER.push("Skills");

        // Swift Dash | 突刺
        BUILDER.push("Swift Dash");
        BUILDER.comment("Automatically used when distance to target > 6 blocks (no weight config needed)");
        SKILL_SWIFT_DASH_DAMAGE_MULTIPLIER = BUILDER
            .comment("Damage multiplier for final strike (0.5 = 50% attack damage)")
            .defineInRange("Damage Multiplier", 0.5, 0.0, 10.0);
        SKILL_SWIFT_DASH_RANGE = BUILDER
            .comment("Attack range for final strike in blocks")
            .defineInRange("Range", 4.0, 0.0, 32.0);
        BUILDER.pop();

        // Double Strike | 双连击
        BUILDER.push("Double Strike");
        SKILL_DOUBLE_STRIKE_WEIGHT = BUILDER
            .comment("Skill selection weight (0.0 = disabled, 0.5 = ~50% probability when alone)")
            .defineInRange("Weight", 0.2, 0.0, 1.0);
        SKILL_DOUBLE_STRIKE_DAMAGE_MULTIPLIER = BUILDER
            .comment("Damage multiplier (1.0 = 100% attack damage)")
            .defineInRange("Damage Multiplier", 1.0, 0.0, 10.0);
        SKILL_DOUBLE_STRIKE_RANGE = BUILDER
            .comment("Attack range in blocks")
            .defineInRange("Range", 4.0, 0.0, 32.0);
        BUILDER.pop();

        // Cross Slash | 十字切
        BUILDER.push("Cross Slash");
        SKILL_CROSS_SLASH_WEIGHT = BUILDER
            .comment("Skill selection weight (0.0 = disabled, 0.5 = ~50% probability when alone)")
            .defineInRange("Weight", 0.15, 0.0, 1.0);
        SKILL_CROSS_SLASH_DAMAGE_MULTIPLIER = BUILDER
            .comment("Damage multiplier (1.5 = 150% attack damage)")
            .defineInRange("Damage Multiplier", 1.5, 0.0, 10.0);
        SKILL_CROSS_SLASH_RANGE = BUILDER
            .comment("Attack range in blocks")
            .defineInRange("Range", 6.0, 0.0, 32.0);
        BUILDER.pop();

        // Block | 格挡
        BUILDER.push("Block");
        SKILL_BLOCK_WEIGHT = BUILDER
            .comment("Skill selection weight (0.0 = disabled, 0.5 = ~50% probability when alone)")
            .defineInRange("Weight", 0.2, 0.0, 1.0);
        SKILL_BLOCK_DAMAGE_MULTIPLIER = BUILDER
            .comment("Damage multiplier for knockback strike (0.5 = 50% attack damage)")
            .defineInRange("Damage Multiplier", 0.5, 0.0, 10.0);
        SKILL_BLOCK_RANGE = BUILDER
            .comment("Range for knockback and projectile deflection in blocks")
            .defineInRange("Range", 5.0, 0.0, 32.0);
        BUILDER.pop();

        // Moon Light Strike | 月华
        BUILDER.push("Moon Light Strike");
        SKILL_MOON_LIGHT_STRIKE_WEIGHT = BUILDER
            .comment("Skill selection weight (0.0 = disabled, 0.5 = ~50% probability when alone)")
            .defineInRange("Weight", 0.15, 0.0, 1.0);
        SKILL_MOON_LIGHT_STRIKE_DAMAGE_MULTIPLIER = BUILDER
            .comment("Damage multiplier (1.5 = 150% attack damage)")
            .defineInRange("Damage Multiplier", 1.5, 0.0, 10.0);
        SKILL_MOON_LIGHT_STRIKE_RANGE = BUILDER
            .comment("Attack range in blocks (sphere radius)")
            .defineInRange("Range", 6.0, 0.0, 32.0);
        BUILDER.pop();

        // Enchant | 虚空附魔
        BUILDER.push("Enchant");
        SKILL_ENCHANT_WEIGHT = BUILDER
            .comment("Skill selection weight (0.0 = disabled, 0.5 = ~50% probability when alone)")
            .defineInRange("Weight", 0.25, 0.0, 1.0);
        SKILL_ENCHANT_DURATION = BUILDER
            .comment("Buff duration in ticks (600 ticks = 30 seconds)")
            .defineInRange("Duration", 600, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        // End of All Things | 万物终焉
        BUILDER.push("End of All Things");
        SKILL_END_OF_ALL_THINGS_WEIGHT = BUILDER
            .comment("Skill selection weight (0.0 = disabled, 0.5 = ~50% probability when alone)",
                     "Only usable at level 13 when All Things End is not active")
            .defineInRange("Weight", 0.05, 0.0, 1.0);
        SKILL_END_OF_ALL_THINGS_DAMAGE_MULTIPLIER = BUILDER
            .comment("Damage multiplier for periodic damage (1.0 = 100% attack damage per second)")
            .defineInRange("Damage Multiplier", 1.0, 0.0, 10.0);
        SKILL_END_OF_ALL_THINGS_MAX_HEALTH_PERCENTAGE = BUILDER
            .comment("Additional damage as percentage of target's max health (0.05 = 5% max health per second)")
            .defineInRange("Max Health Percentage Damage", 0.05, 0.0, 1.0);
        SKILL_END_OF_ALL_THINGS_RANGE = BUILDER
            .comment("Effect range in blocks (sphere radius)")
            .defineInRange("Range", 32.0, 0.0, 128.0);
        BUILDER.pop();

        BUILDER.pop(); // End Skills

        BUILDER.pop();

        // Sword Wraith Generic Settings | 通用剑灵设置
        BUILDER.push("Sword Wraith");
        SWORD_WRAITH_HEALTH_PER_LEVEL = BUILDER
            .comment("Health increase per sword level (Level 1-5)")
            .defineInRange("Health Per Level", 100.0, 0.0, Double.MAX_VALUE);
        SWORD_WRAITH_ATTACK_PER_LEVEL = BUILDER
            .comment("Attack damage increase per sword level (Level 1-5)")
            .defineInRange("Attack Per Level", 5.0, 0.0, Double.MAX_VALUE);
        SWORD_WRAITH_HEALTH_PER_HIGH_LEVEL = BUILDER
            .comment("Health increase per sword level (Level 6+)")
            .defineInRange("Health Per High Level", 200.0, 0.0, Double.MAX_VALUE);
        SWORD_WRAITH_ATTACK_PER_HIGH_LEVEL = BUILDER
            .comment("Attack damage increase per sword level (Level 6+)")
            .defineInRange("Attack Per High Level", 10.0, 0.0, Double.MAX_VALUE);
        SWORD_WRAITH_FRIENDLY_FIRE_PROTECTION = BUILDER
            .comment("Enable friendly fire protection for sword wraiths")
            .define("Friendly Fire Protection", true);
        SWORD_WRAITH_AS_THE_LAST_END_ENTITY = BUILDER
            .comment("Treat sword wraiths as The Last End Entities")
            .define("Sword Wraith As End Entity", true);
        SWORD_WRAITH_ABSOLUTE_DESTRUCTION_DAMAGE = BUILDER
            .comment("Enable absolute destruction damage for wraiths")
            .define("Sword Wraith Absolute Destruction Damage", true);
        SWORD_WRAITH_ABSOLUTE_DESTRUCTION_MULTIPLIER_LOW = BUILDER
            .comment("Absolute destruction damage multiplier for weapon levels 1-5")
            .defineInRange("Absolute Destruction Multiplier (Lv1-5)", 0.10, 0.0, 10.0);
        SWORD_WRAITH_ABSOLUTE_DESTRUCTION_MULTIPLIER_MID = BUILDER
            .comment("Absolute destruction damage multiplier for weapon levels 6-12")
            .defineInRange("Absolute Destruction Multiplier (Lv6-12)", 0.50, 0.0, 10.0);
        SWORD_WRAITH_ABSOLUTE_DESTRUCTION_MULTIPLIER_HIGH = BUILDER
            .comment("Absolute destruction damage multiplier for weapon level 13+")
            .defineInRange("Absolute Destruction Multiplier (Lv13+)", 1.00, 0.0, 10.0);
        BUILDER.pop();

        BUILDER.pop(); // End Entity

        // ═══════════════════════════════════════════════════════════════════════════════
        // Attack Configuration | 攻击系统配置
        // ═══════════════════════════════════════════════════════════════════════════════
        BUILDER.push("Attack");

        ATTACK_ENABLE_RADICAL_LOGIC = BUILDER
            .comment("Enable radical logic for attack system, which increases attack processing strength but may cause performance overhead and mod conflicts")
            .define("Enable Radical Logic", false);

        // Absolute Destruction Damage Settings | 绝对毁灭伤害设置
        BUILDER.push("Absolute Destruction Damage");
        ABSOLUTE_DESTRUCTION_HEAL_NEGATION_TIME = BUILDER
            .comment("Time (in seconds) to prevent healing. Set to 0 to disable heal negation")
            .defineInRange("Heal Negation Time", 30, 0, Integer.MAX_VALUE);
        ABSOLUTE_DESTRUCTION_REVIVE_BAN_TIME = BUILDER
            .comment("Time (in seconds) to prevent respawning. Set to 0 to disable revive ban")
            .defineInRange("Revive Ban Time", 5, 0, Integer.MAX_VALUE);
        ABSOLUTE_DESTRUCTION_DIE_MESSAGE = BUILDER
            .comment("Enable or disable message when The Last End setDead")
            .define("Enable Die Message", true);
        ABSOLUTE_DESTRUCTION_PARTICLE_EFFECTS = BUILDER
            .comment("Enable or disable particle effects when The Last End Death")
            .define("Enable Particle Effects", true);
        ABSOLUTE_DESTRUCTION_ENABLE_THE_LAST_END_SETDEAD = BUILDER
            .comment("Enable The Last End SetDead for entities with abnormal health (NaN, Infinite, or <= 0)")
            .define("Enable The Last End SetDead", true);
        BUILDER.pop();

        BUILDER.pop(); // End Attack

        // ═══════════════════════════════════════════════════════════════════════════════
        // Defence Configuration | 防御系统配置
        // ═══════════════════════════════════════════════════════════════════════════════
        BUILDER.push("Defence");

        JUSTIFIED_DEFENCE_RECOVERY_TICK = BUILDER
            .comment("Recovery interval for Justified Defence Shield in ticks (100 ticks = 5 seconds)")
            .defineInRange("Justified Defence Recovery Tick", 100, 1, Integer.MAX_VALUE);

        DEFENCE_ENABLE_RADICAL_LOGIC = BUILDER
            .comment("Enable radical logic for defence system, which increases protection strength but may cause performance overhead and mod conflicts")
            .define("Enable Radical Logic", false);

        DEFENCE_CUSTOM_HEALTH_DAMAGE_REDUCTION = BUILDER
            .comment("Maximum percentage of health that can be lost per hit for defence system")
            .defineInRange("Custom Health Damage Reduction", 0.05, 0.0, 1.0);

        DEFENCE_MAX_DAMAGE_PER_HIT = BUILDER
            .comment("Maximum absolute damage that can be dealt per hit for defence system")
            .defineInRange("Max Damage Per Hit", 20.0, 0.0, Double.MAX_VALUE);

        BUILDER.pop(); // End Defence

        // ═══════════════════════════════════════════════════════════════════════════════
        // Others Configuration | 其他配置
        // ═══════════════════════════════════════════════════════════════════════════════
        BUILDER.push("Others");

        // Buff Configuration | Buff配置
        BUILDER.push("Buff");

        BUILDER.push("Void Enchanting");
        BUFF_VOID_ENCHANTMENT_PARTICLE_EFFECTS = BUILDER
            .comment("Control whether to render particle effects")
            .define("Particle Effects", false);
        BUFF_VOID_ENCHANTMENT_DAMAGE_PERCENTAGE = BUILDER
            .comment("The percentage increase in damage by Buff")
            .define("Damage Percentage", 0.2);
        BUILDER.pop();

        BUILDER.push("Phasing");
        BUFF_PHASING_BREAK_BLOCKS_ON_END = BUILDER
            .comment("Whether to break suffocating blocks when Phasing effect ends")
            .define("Break Blocks On End", true);
        BUILDER.pop();

        BUILDER.pop(); // End Buff

        // Ender Dragon Egg Settings | 末影龙蛋设置
        BUILDER.push("Ender Dragon Egg");
        ENDER_DRAGON_EGG_DROP = BUILDER
            .comment("Controls whether the Ender Dragon drops a dragon egg upon death")
            .define("Drop Dragon Egg", true);
        ENDER_DRAGON_EGG_MULTIPLE = BUILDER
            .comment("Controls whether multiple dragon eggs are given to players")
            .define("Multiple Dragon Eggs", true);
        ENDER_DRAGON_EGG_RADIUS = BUILDER
            .comment("Radius to check for players to give dragon eggs")
            .define("Dragon Egg Radius", 64);
        ENDER_DRAGON_EGG_AMOUNT = BUILDER
            .comment("Amount of dragon eggs to give per player")
            .define("Dragon Egg Amount", 1);
        ENDER_DRAGON_EGG_GIVE_TO_ABSENT_PLAYERS = BUILDER
            .comment("Give dragon eggs to players who caused damage but are not present at death")
            .define("Give Egg To Absent Players", true);
        BUILDER.pop();

        // Stronger Ender Dragon Settings | 更强的末影龙设置
        BUILDER.push("Stronger Ender Dragon");
        ENDER_DRAGON_MAX_LEVEL = BUILDER
            .comment("Maximum level for Ender Dragon")
            .define("Maximum Dragon Level", 13);
        ENDER_DRAGON_HEALTH_INCREASE_VALUE = BUILDER
            .comment("Health increase per level when level < 6")
            .define("Health Increase Value", 200.0);
        ENDER_DRAGON_HEALTH_INCREASE_VALUE_HIGH_LEVEL = BUILDER
            .comment("Health increase per level when level >= 6")
            .define("Health Increase Value High Level", 1024.0);
        ENDER_DRAGON_ARMOR_INCREASE_VALUE = BUILDER
            .comment("Armor increase per level when level < 6")
            .define("Armor Increase Value", 2.0);
        ENDER_DRAGON_ARMOR_INCREASE_VALUE_HIGH_LEVEL = BUILDER
            .comment("Armor increase per level when level >= 6")
            .define("Armor Increase Value High Level", 4.0);
        ENDER_DRAGON_ATTACK_INCREASE_VALUE = BUILDER
            .comment("Attack increase per level when level < 6")
            .define("Attack Increase Value", 6.0);
        ENDER_DRAGON_ATTACK_INCREASE_VALUE_HIGH_LEVEL = BUILDER
            .comment("Attack increase per level when level >= 6")
            .define("Attack Increase Value High Level", 12.0);
        BUILDER.pop();

        // Compat Mods Configuration | 联动Mod配置
        BUILDER.push("Compat Mods");
        BUILDER.push("Cataclysm");
        COMPAT_CATACLYSM_ENABLE = BUILDER
            .comment("Enable Cataclysm mod compatibility")
            .define("Enable Cataclysm Compat", true);
        BUILDER.pop();
        BUILDER.pop(); // End Compat Mods

        BUILDER.pop(); // End Others

        SPEC = BUILDER.build();
    }
    // ═══════════════════════════════════════════════════════════════════════════════
    // Safe Config Access Methods | 安全的配置访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    //通用的安全配置获取方法（避免配置未加载时的空指针异常）
    private static <T> T safeGet(ForgeConfigSpec.ConfigValue<T> configValue, T defaultValue) {
        try {
            return configValue != null ? configValue.get() : defaultValue;
        } catch (IllegalStateException | NullPointerException e) {
            return defaultValue;
        }
    }

    //剑类配置
    public static double getIncreaseValueSafely() {
        return safeGet(SWORD_INCREASE_VALUE, 20.0);
    }

    public static double getIncreaseValueHighLevelSafely() {
        return safeGet(SWORD_INCREASE_VALUE_HIGH_LEVEL, 200.0);
    }

    public static boolean getBlockCancelUseSafely() {
        return safeGet(SWORD_BLOCK_CANCEL_USE, true);
    }

    //盔甲类通用配置（等级区间）
    public static double getArmorIncreaseLowLevelSafely() {
        return safeGet(ARMOR_INCREASE_LOW_LEVEL, 0.5);
    }

    public static double getArmorIncreaseHighLevelSafely() {
        return safeGet(ARMOR_INCREASE_HIGH_LEVEL, 1.0);
    }

    public static double getToughnessIncreaseLowLevelSafely() {
        return safeGet(TOUGHNESS_INCREASE_LOW_LEVEL, 0.25);
    }

    public static double getToughnessIncreaseHighLevelSafely() {
        return safeGet(TOUGHNESS_INCREASE_HIGH_LEVEL, 0.5);
    }

    public static double getJustifiedDefenceIncreaseLowLevelSafely() {
        return safeGet(JUSTIFIED_DEFENCE_INCREASE_LOW_LEVEL, 1.0);
    }

    public static double getJustifiedDefenceIncreaseHighLevelSafely() {
        return safeGet(JUSTIFIED_DEFENCE_INCREASE_HIGH_LEVEL, 2.0);
    }

    public static double getHealthIncreaseLowLevelSafely() {
        return safeGet(HEALTH_INCREASE_LOW_LEVEL, 2.0);
    }

    public static double getHealthIncreaseHighLevelSafely() {
        return safeGet(HEALTH_INCREASE_HIGH_LEVEL, 4.0);
    }

    //龙之剑配置
    public static int getDragonSwordSummonCooldownSafely() {
        return safeGet(DRAGON_SWORD_SUMMON_COOLDOWN, 1200);
    }

    public static double getDragonSwordShieldMultiplierSafely() {
        return safeGet(DRAGON_SWORD_SHIELD_MULTIPLIER, 0.05);
    }

    //最终之剑配置
    public static double getAbsoluteDestructionMultiplierSafely() {
        return safeGet(THE_LAST_SWORD_ABSOLUTE_DESTRUCTION_MULTIPLIER, 0.5);
    }

    public static double getTheLastSwordShieldMultiplierSafely() {
        return safeGet(THE_LAST_SWORD_SHIELD_MULTIPLIER, 0.05);
    }

    public static boolean getAllowFlyingSafely() {
        return safeGet(THE_LAST_SWORD_ALLOW_FLYING, true);
    }

    public static boolean getSuperDestroySafely() {
        return safeGet(THE_LAST_SWORD_SUPER_DESTROY, true);
    }

    public static int getMiningRadiusSafely() {
        return safeGet(THE_LAST_SWORD_MINING_RADIUS, 3);
    }

    public static int getTheLastSwordSummonCooldownSafely() {
        return safeGet(THE_LAST_SWORD_SUMMON_COOLDOWN, 600);
    }

    //龙晶护甲配置
    public static int getCrystalGuardRefreshIntervalSafely() {
        return safeGet(DRAGON_CRYSTAL_ARMOR_CRYSTAL_GUARD_REFRESH_INTERVAL, 1200);
    }

    //龙之甲配置
    public static int getDragonArmorEnergyPerLevelSafely() {
        return safeGet(DRAGON_ARMOR_ENERGY_PER_LEVEL, 102400);
    }

    public static int getDragonArmorEnergyCostPerPieceSafely() {
        return safeGet(DRAGON_ARMOR_ENERGY_COST_PER_PIECE, 1024);
    }

    public static int getDragonArmorPhasingEnergyCostSafely() {
        return safeGet(DRAGON_ARMOR_PHASING_ENERGY_COST, 1024);
    }

    public static int getDragonArmorEnderCrystalChargeRateSafely() {
        return safeGet(DRAGON_ARMOR_ENDER_CRYSTAL_CHARGE_RATE, 200);
    }

    public static int getDragonArmorEnderCrystalRangeSafely() {
        return safeGet(DRAGON_ARMOR_ENDER_CRYSTAL_RANGE, 8);
    }

    //终焉卷轴配置
    public static boolean getTheLastEndScrollEnableParticleEffectsSafely() {
        return safeGet(THE_LAST_END_SCROLL_ENABLE_PARTICLE_EFFECTS, true);
    }

    //攻击系统配置
    public static boolean getAttackEnableRadicalLogicSafely() {
        return safeGet(ATTACK_ENABLE_RADICAL_LOGIC, false);
    }

    //防御系统配置
    public static int getJustifiedDefenceRecoveryTickSafely() {
        return safeGet(JUSTIFIED_DEFENCE_RECOVERY_TICK, 100);
    }

    public static boolean getDefenceEnableRadicalLogicSafely() {
        return safeGet(DEFENCE_ENABLE_RADICAL_LOGIC, false);
    }

    public static double getDefenceCustomHealthDamageReductionSafely() {
        return safeGet(DEFENCE_CUSTOM_HEALTH_DAMAGE_REDUCTION, 0.05);
    }

    public static double getDefenceMaxDamagePerHitSafely() {
        return safeGet(DEFENCE_MAX_DAMAGE_PER_HIT, 20.0);
    }

    //剑灵配置
    public static double getSwordWraithHealthPerLevelSafely() {
        return safeGet(SWORD_WRAITH_HEALTH_PER_LEVEL, 100.0);
    }

    public static double getSwordWraithAttackPerLevelSafely() {
        return safeGet(SWORD_WRAITH_ATTACK_PER_LEVEL, 5.0);
    }

    public static double getSwordWraithHealthPerHighLevelSafely() {
        return safeGet(SWORD_WRAITH_HEALTH_PER_HIGH_LEVEL, 200.0);
    }

    public static double getSwordWraithAttackPerHighLevelSafely() {
        return safeGet(SWORD_WRAITH_ATTACK_PER_HIGH_LEVEL, 10.0);
    }

    public static boolean getSwordWraithFriendlyFireProtectionSafely() {
        return safeGet(SWORD_WRAITH_FRIENDLY_FIRE_PROTECTION, true);
    }

    public static boolean getSwordWraithAsTheLastEndEntitySafely() {
        return safeGet(SWORD_WRAITH_AS_THE_LAST_END_ENTITY, true);
    }

    public static boolean getSwordWraithAbsoluteDestructionDamageSafely() {
        return safeGet(SWORD_WRAITH_ABSOLUTE_DESTRUCTION_DAMAGE, true);
    }

    public static double getSwordWraithAbsoluteDestructionMultiplierLowSafely() {
        return safeGet(SWORD_WRAITH_ABSOLUTE_DESTRUCTION_MULTIPLIER_LOW, 0.10);
    }

    public static double getSwordWraithAbsoluteDestructionMultiplierMidSafely() {
        return safeGet(SWORD_WRAITH_ABSOLUTE_DESTRUCTION_MULTIPLIER_MID, 0.50);
    }

    public static double getSwordWraithAbsoluteDestructionMultiplierHighSafely() {
        return safeGet(SWORD_WRAITH_ABSOLUTE_DESTRUCTION_MULTIPLIER_HIGH, 1.00);
    }

    //绝对毁灭伤害配置
    public static int getHealNegationTimeSafely() {
        return safeGet(ABSOLUTE_DESTRUCTION_HEAL_NEGATION_TIME, 30);
    }

    public static int getReviveBanTimeSafely() {
        return safeGet(ABSOLUTE_DESTRUCTION_REVIVE_BAN_TIME, 5);
    }

    public static boolean getDeathParticleEffectSafely() {
        return safeGet(ABSOLUTE_DESTRUCTION_PARTICLE_EFFECTS, true);
    }

    public static boolean getEnableTheLastEndSetDeadSafely() {
        return safeGet(ABSOLUTE_DESTRUCTION_ENABLE_THE_LAST_END_SETDEAD, true);
    }

    public static boolean getDieMessageSafely() {
        return safeGet(ABSOLUTE_DESTRUCTION_DIE_MESSAGE, true);
    }

    //Buff配置
    public static boolean getVoidEnchantmentParticleEffectsSafely() {
        return safeGet(BUFF_VOID_ENCHANTMENT_PARTICLE_EFFECTS, false);
    }

    public static double getVoidEnchantmentDamagePercentageSafely() {
        return safeGet(BUFF_VOID_ENCHANTMENT_DAMAGE_PERCENTAGE, 0.2);
    }

    public static boolean getPhasingBreakBlocksOnEndSafely() {
        return safeGet(BUFF_PHASING_BREAK_BLOCKS_ON_END, true);
    }

    //终焉剑灵配置
    public static int getWraithCombatTeleportDistanceSafely() {
        return safeGet(THE_LAST_END_SWORD_WRAITH_COMBAT_TELEPORT_DISTANCE, 64);
    }

    //终焉剑灵技能配置
    //突刺
    public static double getSkillSwiftDashDamageMultiplierSafely() {
        return safeGet(SKILL_SWIFT_DASH_DAMAGE_MULTIPLIER, 0.5);
    }

    public static double getSkillSwiftDashRangeSafely() {
        return safeGet(SKILL_SWIFT_DASH_RANGE, 4.0);
    }

    //双连击
    public static double getSkillDoubleStrikeWeightSafely() {
        return safeGet(SKILL_DOUBLE_STRIKE_WEIGHT, 0.2);
    }

    public static double getSkillDoubleStrikeDamageMultiplierSafely() {
        return safeGet(SKILL_DOUBLE_STRIKE_DAMAGE_MULTIPLIER, 1.0);
    }

    public static double getSkillDoubleStrikeRangeSafely() {
        return safeGet(SKILL_DOUBLE_STRIKE_RANGE, 4.0);
    }

    //十字切
    public static double getSkillCrossSlashWeightSafely() {
        return safeGet(SKILL_CROSS_SLASH_WEIGHT, 0.15);
    }

    public static double getSkillCrossSlashDamageMultiplierSafely() {
        return safeGet(SKILL_CROSS_SLASH_DAMAGE_MULTIPLIER, 1.5);
    }

    public static double getSkillCrossSlashRangeSafely() {
        return safeGet(SKILL_CROSS_SLASH_RANGE, 6.0);
    }

    //格挡
    public static double getSkillBlockWeightSafely() {
        return safeGet(SKILL_BLOCK_WEIGHT, 0.2);
    }

    public static double getSkillBlockDamageMultiplierSafely() {
        return safeGet(SKILL_BLOCK_DAMAGE_MULTIPLIER, 0.5);
    }

    public static double getSkillBlockRangeSafely() {
        return safeGet(SKILL_BLOCK_RANGE, 5.0);
    }

    //月华
    public static double getSkillMoonLightStrikeWeightSafely() {
        return safeGet(SKILL_MOON_LIGHT_STRIKE_WEIGHT, 0.15);
    }

    public static double getSkillMoonLightStrikeDamageMultiplierSafely() {
        return safeGet(SKILL_MOON_LIGHT_STRIKE_DAMAGE_MULTIPLIER, 1.5);
    }

    public static double getSkillMoonLightStrikeRangeSafely() {
        return safeGet(SKILL_MOON_LIGHT_STRIKE_RANGE, 6.0);
    }

    //虚空附魔
    public static double getSkillEnchantWeightSafely() {
        return safeGet(SKILL_ENCHANT_WEIGHT, 0.25);
    }

    public static int getSkillEnchantDurationSafely() {
        return safeGet(SKILL_ENCHANT_DURATION, 600);
    }

    //万物终焉
    public static double getSkillEndOfAllThingsWeightSafely() {
        return safeGet(SKILL_END_OF_ALL_THINGS_WEIGHT, 0.05);
    }

    public static double getSkillEndOfAllThingsDamageMultiplierSafely() {
        return safeGet(SKILL_END_OF_ALL_THINGS_DAMAGE_MULTIPLIER, 1.0);
    }

    public static double getSkillEndOfAllThingsMaxHealthPercentageSafely() {
        return safeGet(SKILL_END_OF_ALL_THINGS_MAX_HEALTH_PERCENTAGE, 0.05);
    }

    public static double getSkillEndOfAllThingsRangeSafely() {
        return safeGet(SKILL_END_OF_ALL_THINGS_RANGE, 32.0);
    }

}
