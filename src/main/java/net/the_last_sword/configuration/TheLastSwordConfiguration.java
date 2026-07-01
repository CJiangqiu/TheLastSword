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

    // Dragon Crystal Sword | 龙水晶剑配置
    public static ForgeConfigSpec.ConfigValue<Boolean> DRAGON_CRYSTAL_SWORD_CAN_MINE;

    // Dragon Sword | 龙之剑配置
    public static ForgeConfigSpec.ConfigValue<Integer> DRAGON_SWORD_SUMMON_COOLDOWN;
    public static ForgeConfigSpec.ConfigValue<Boolean> DRAGON_SWORD_NORMAL_MODE_CAN_MINE;
    public static ForgeConfigSpec.ConfigValue<Boolean> DRAGON_SWORD_SUMMON_MODE_CAN_MINE;

    // Dragon Soul Lantern | 龙魂灯配置
    public static ForgeConfigSpec.ConfigValue<Integer> DRAGON_SOUL_LANTERN_SUMMON_COOLDOWN;
    public static ForgeConfigSpec.ConfigValue<Double> DRAGON_SOUL_LANTERN_RANGE_PLACED;
    public static ForgeConfigSpec.ConfigValue<Double> DRAGON_SOUL_LANTERN_RANGE_EQUIPPED;

    // The Last End Sword | 最终之剑配置
    public static ForgeConfigSpec.ConfigValue<Boolean> THE_LAST_SWORD_ALLOW_FLYING;
    public static ForgeConfigSpec.ConfigValue<Boolean> THE_LAST_SWORD_SUPER_DESTROY;
    public static ForgeConfigSpec.ConfigValue<Integer> THE_LAST_SWORD_MINING_RADIUS;
    public static ForgeConfigSpec.ConfigValue<Boolean> THE_LAST_SWORD_ENABLE_MINING_PREVIEW;
    public static ForgeConfigSpec.ConfigValue<Boolean> THE_LAST_SWORD_HIGH_PERFORMANCE_MINING;
    public static ForgeConfigSpec.ConfigValue<Boolean> THE_LAST_SWORD_NORMAL_MODE_CAN_MINE;
    public static ForgeConfigSpec.ConfigValue<Boolean> THE_LAST_SWORD_SUMMON_MODE_CAN_MINE;
    public static ForgeConfigSpec.ConfigValue<Double> THE_LAST_SWORD_PERCENTAGE_DAMAGE;
    public static ForgeConfigSpec.ConfigValue<Boolean> THE_LAST_SWORD_ENABLE_STRONG_INVENTORY_PROTECTION;

    // Dragon Crystal Armor | 龙晶护甲配置
    public static ForgeConfigSpec.ConfigValue<Integer> DRAGON_CRYSTAL_ARMOR_CRYSTAL_GUARD_REFRESH_INTERVAL;

    // Dragon Armor | 龙之甲配置
    public static ForgeConfigSpec.ConfigValue<Integer> DRAGON_ARMOR_ENERGY_PER_LEVEL;
    public static ForgeConfigSpec.ConfigValue<Integer> DRAGON_ARMOR_BUFF_ENHANCE_COST;
    public static ForgeConfigSpec.ConfigValue<Integer> DRAGON_ARMOR_SATURATION_COST;
    public static ForgeConfigSpec.ConfigValue<Integer> DRAGON_ARMOR_ICE_FIRE_IMMUNITY_COST;
    public static ForgeConfigSpec.ConfigValue<Integer> DRAGON_ARMOR_PHASING_COST;
    public static ForgeConfigSpec.ConfigValue<Integer> DRAGON_ARMOR_ENDER_CRYSTAL_CHARGE_RATE;
    public static ForgeConfigSpec.ConfigValue<Integer> DRAGON_ARMOR_ENDER_CRYSTAL_RANGE;
    public static ForgeConfigSpec.ConfigValue<Integer> DRAGON_ARMOR_PERCEPTION_GLOW_DURATION;
    public static ForgeConfigSpec.ConfigValue<Integer> DRAGON_ARMOR_PERCEPTION_SCAN_RANGE;

    // The Last End Scroll Configuration | 终焉卷轴配置
    public static ForgeConfigSpec.ConfigValue<Boolean> THE_LAST_END_SCROLL_ENABLE_PARTICLE_EFFECTS;

    // Disposable Energy Battery | 一次性能量电池
    public static ForgeConfigSpec.ConfigValue<Integer> DISPOSABLE_ENERGY_BATTERY_RESTORE_AMOUNT;

    // Ancient Energy Core | 远古能量核心
    public static ForgeConfigSpec.ConfigValue<Integer> ANCIENT_ENERGY_CORE_MAX_ENERGY;
    public static ForgeConfigSpec.ConfigValue<Integer> ANCIENT_ENERGY_CORE_CHARGE_RATE;

    // Curios - The Giver's Pain | 给予者的痛苦
    public static ForgeConfigSpec.ConfigValue<Double> CURIOS_GIVERS_PAIN_ATTACK_DAMAGE_BONUS;
    public static ForgeConfigSpec.ConfigValue<Double> CURIOS_GIVERS_PAIN_ATTACK_SPEED_BONUS;
    public static ForgeConfigSpec.ConfigValue<Double> CURIOS_GIVERS_PAIN_ATTACK_DAMAGE_MULTIPLIER;
    public static ForgeConfigSpec.ConfigValue<Double> CURIOS_GIVERS_PAIN_ATTACKER_LOST_HEALTH_MULTIPLIER;
    public static ForgeConfigSpec.ConfigValue<Double> CURIOS_GIVERS_PAIN_TARGET_LOST_HEALTH_MULTIPLIER;
    public static ForgeConfigSpec.ConfigValue<Integer> CURIOS_GIVERS_PAIN_EFFECT_DURATION;
    public static ForgeConfigSpec.ConfigValue<Integer> CURIOS_GIVERS_PAIN_EFFECT_AMPLIFIER;

    // Curios - Dragon Crystal Ring | 龙水晶指环
    public static ForgeConfigSpec.ConfigValue<Double> CURIOS_DRAGON_CRYSTAL_RING_DAMAGE_MULTIPLIER;

    // Curios - Dragon Crystal Necklace | 龙水晶项链
    public static ForgeConfigSpec.ConfigValue<Double> CURIOS_DRAGON_CRYSTAL_NECKLACE_CRIT_CHANCE_BASE;
    public static ForgeConfigSpec.ConfigValue<Double> CURIOS_DRAGON_CRYSTAL_NECKLACE_CRIT_CHANCE_PER_LUCK;
    public static ForgeConfigSpec.ConfigValue<Double> CURIOS_DRAGON_CRYSTAL_NECKLACE_IMMUNITY_CHANCE_BASE;
    public static ForgeConfigSpec.ConfigValue<Double> CURIOS_DRAGON_CRYSTAL_NECKLACE_IMMUNITY_CHANCE_PER_LUCK;
    public static ForgeConfigSpec.ConfigValue<Double> CURIOS_DRAGON_CRYSTAL_NECKLACE_IMMUNITY_CHANCE_MAX;

    // Curios - Dragon Crystal Crown | 龙水晶王冠
    public static ForgeConfigSpec.ConfigValue<Boolean> CURIOS_DRAGON_CRYSTAL_CROWN_VOID_CONVERSION_ENABLED;

    // Curios - Wings That Cover The World | 覆世之翼
    public static ForgeConfigSpec.ConfigValue<Double> CURIOS_WINGS_VOID_DAMAGE_REDUCTION;
    public static ForgeConfigSpec.ConfigValue<Boolean> CURIOS_WINGS_ICE_FIRE_IMMUNITY_ENABLED;

    // Curios - Extreme Life Support Device | 极限维生装置
    public static ForgeConfigSpec.ConfigValue<Double> CURIOS_EXTREME_LIFE_SUPPORT_TIER_THRESHOLD;
    public static ForgeConfigSpec.ConfigValue<Integer> CURIOS_EXTREME_LIFE_SUPPORT_EFFECT_DURATION;
    public static ForgeConfigSpec.ConfigValue<Double> CURIOS_EXTREME_LIFE_SUPPORT_ARMOR_TOUGHNESS;
    public static ForgeConfigSpec.ConfigValue<Integer> CURIOS_EXTREME_LIFE_SUPPORT_ENERGY_COST;
    public static ForgeConfigSpec.ConfigValue<Integer> CURIOS_EXTREME_LIFE_SUPPORT_MAX_ENERGY;

    // Curios - Dimension Explorer | 维度探索者
    public static ForgeConfigSpec.ConfigValue<Integer> CURIOS_DIMENSION_EXPLORER_COOLDOWN;
    public static ForgeConfigSpec.ConfigValue<Integer> CURIOS_DIMENSION_EXPLORER_EFFECT_DURATION;
    public static ForgeConfigSpec.ConfigValue<Double> CURIOS_DIMENSION_EXPLORER_EMERGENCY_HEAL_HEALTH;
    public static ForgeConfigSpec.ConfigValue<Integer> CURIOS_DIMENSION_EXPLORER_EMERGENCY_FOOD_LEVEL;
    public static ForgeConfigSpec.ConfigValue<Integer> CURIOS_DIMENSION_EXPLORER_JUMP_AMPLIFIER;
    public static ForgeConfigSpec.ConfigValue<Integer> CURIOS_DIMENSION_EXPLORER_HASTE_AMPLIFIER;
    public static ForgeConfigSpec.ConfigValue<Integer> CURIOS_DIMENSION_EXPLORER_SPEED_AMPLIFIER;

    // ═══════════════════════════════════════════════════════════════════════════════════
    // Entity Configuration | 实体配置
    // ═══════════════════════════════════════════════════════════════════════════════════

    // The Last End Entity | 终焉种族配置
    public static ForgeConfigSpec.ConfigValue<Boolean> THE_LAST_END_ENTITY_ENABLE_ALL_THINGS_END;

    // The Last End Sword Wraith | 终焉剑灵配置
    public static ForgeConfigSpec.ConfigValue<Boolean> THE_LAST_END_SWORD_WRAITH_ENABLE_BATTLE_MUSIC;

    // Lost Wraith | 迷失战魂配置
    public static ForgeConfigSpec.ConfigValue<Boolean> LOST_WRAITH_ENABLE_CUSTOM_BOSS_BAR;
    public static ForgeConfigSpec.ConfigValue<Double> LOST_WRAITH_DAMAGE_LIMIT;
    public static ForgeConfigSpec.ConfigValue<Integer> LOST_WRAITH_HURT_RESIST_TIME;

    // Lost Wraith Skills | 迷失战魂技能
    // Patience | 耐心
    public static ForgeConfigSpec.ConfigValue<Double> LOST_WRAITH_PATIENCE_TRIGGER_DISTANCE;
    public static ForgeConfigSpec.ConfigValue<Integer> LOST_WRAITH_PATIENCE_TIMEOUT;
    // Enchant | 虚空附魔
    public static ForgeConfigSpec.ConfigValue<Integer> LOST_WRAITH_ENCHANT_DURATION;
    public static ForgeConfigSpec.ConfigValue<Integer> LOST_WRAITH_ENCHANT_AMPLIFIER;
    // Dragon Fireball | 龙息弹
    public static ForgeConfigSpec.ConfigValue<Double> LOST_WRAITH_DRAGON_FIREBALL_MIN_DISTANCE;
    public static ForgeConfigSpec.ConfigValue<Integer> LOST_WRAITH_DRAGON_FIREBALL_COOLDOWN;
    // Summon Lightning | 召唤闪电
    public static ForgeConfigSpec.ConfigValue<Double> LOST_WRAITH_LIGHTNING_MIN_DISTANCE;
    public static ForgeConfigSpec.ConfigValue<Integer> LOST_WRAITH_LIGHTNING_COOLDOWN;
    public static ForgeConfigSpec.ConfigValue<Double> LOST_WRAITH_LIGHTNING_AOE_RADIUS;
    public static ForgeConfigSpec.ConfigValue<Double> LOST_WRAITH_LIGHTNING_DAMAGE_MULTIPLIER;
    // Punch | 拳击
    public static ForgeConfigSpec.ConfigValue<Double> LOST_WRAITH_PUNCH_ATTACK_DISTANCE;
    public static ForgeConfigSpec.ConfigValue<Integer> LOST_WRAITH_PUNCH_FORWARD_STEPS;
    public static ForgeConfigSpec.ConfigValue<Integer> LOST_WRAITH_PUNCH_SIDE_HALF_WIDTH;
    // End Strike | 终焉一击
    public static ForgeConfigSpec.ConfigValue<Integer> LOST_WRAITH_END_STRIKE_COOLDOWN;
    public static ForgeConfigSpec.ConfigValue<Double> LOST_WRAITH_END_STRIKE_PULL_RADIUS;
    public static ForgeConfigSpec.ConfigValue<Double> LOST_WRAITH_END_STRIKE_DAMAGE_MULTIPLIER;
    public static ForgeConfigSpec.ConfigValue<Integer> LOST_WRAITH_END_STRIKE_SHIELD_COOLDOWN;

    // Guardian Of Sealed Spire | 封印尖塔守卫配置
    public static ForgeConfigSpec.ConfigValue<Double> GUARDIAN_DAMAGE_LIMIT;
    public static ForgeConfigSpec.ConfigValue<Integer> GUARDIAN_HURT_RESIST_TIME;

    // Guardian Skills | 守卫共享技能
    // Melee Attack | 近战攻击
    public static ForgeConfigSpec.ConfigValue<Double> GUARDIAN_MELEE_ATTACK_RANGE;
    // Chase Target | 追击
    public static ForgeConfigSpec.ConfigValue<Double> GUARDIAN_CHASE_APPROACH_DISTANCE;
    // Pickup Weapon | 拾取武器
    public static ForgeConfigSpec.ConfigValue<Double> GUARDIAN_PICKUP_DISTANCE;
    public static ForgeConfigSpec.ConfigValue<Integer> GUARDIAN_PICKUP_SCAN_INTERVAL;
    // Assist Ally | 协同
    public static ForgeConfigSpec.ConfigValue<Double> GUARDIAN_ASSIST_ALLY_MAX_SEARCH_DISTANCE;

    // Guardian Archer | 弓箭守卫
    public static ForgeConfigSpec.ConfigValue<Double> GUARDIAN_ARCHER_RANGED_MIN_DISTANCE;
    public static ForgeConfigSpec.ConfigValue<Double> GUARDIAN_ARCHER_RANGED_MAX_DISTANCE;
    public static ForgeConfigSpec.ConfigValue<Double> GUARDIAN_ARCHER_MAINTAIN_MIN_DISTANCE;
    public static ForgeConfigSpec.ConfigValue<Double> GUARDIAN_ARCHER_MAINTAIN_MAX_DISTANCE;

    // Guardian Berserker | 狂战士守卫
    public static ForgeConfigSpec.ConfigValue<Double> GUARDIAN_BERSERKER_LIFESTEAL_RATIO;

    // Projectile | 弹射物配置
    // Dragon Sword Projectile | 龙之剑弹射物
    public static ForgeConfigSpec.ConfigValue<Double> DRAGON_SWORD_PROJECTILE_EXTRA_DAMAGE_MULTIPLIER;
    // Dragon Crystal Sword Projectile | 龙水晶剑弹射物
    public static ForgeConfigSpec.ConfigValue<Double> DRAGON_CRYSTAL_SWORD_PROJECTILE_EXTRA_DAMAGE_MULTIPLIER;
    // The Last End Sword Projectile | 最终之剑弹射物
    public static ForgeConfigSpec.ConfigValue<Double> THE_LAST_END_SWORD_PROJECTILE_EXTRA_DAMAGE_MULTIPLIER;
    public static ForgeConfigSpec.ConfigValue<Double> THE_LAST_END_SWORD_PROJECTILE_AOE_RADIUS;

    // The Last End Sword Wraith Skills | 终焉剑灵技能配置
    // Swift Dash | 突刺
    public static ForgeConfigSpec.ConfigValue<Double> SKILL_SWIFT_DASH_DAMAGE_MULTIPLIER;
    public static ForgeConfigSpec.ConfigValue<Double> SKILL_SWIFT_DASH_RANGE;

    // Double Strike | 双连击
    public static ForgeConfigSpec.ConfigValue<Double> SKILL_DOUBLE_STRIKE_DAMAGE_MULTIPLIER;
    public static ForgeConfigSpec.ConfigValue<Double> SKILL_DOUBLE_STRIKE_RANGE;

    // Cross Slash | 十字切
    public static ForgeConfigSpec.ConfigValue<Double> SKILL_CROSS_SLASH_DAMAGE_MULTIPLIER;
    public static ForgeConfigSpec.ConfigValue<Double> SKILL_CROSS_SLASH_RANGE;

    // Block | 格挡
    public static ForgeConfigSpec.ConfigValue<Double> SKILL_BLOCK_DAMAGE_MULTIPLIER;
    public static ForgeConfigSpec.ConfigValue<Double> SKILL_BLOCK_RANGE;

    // Moon Light Strike | 月华
    public static ForgeConfigSpec.ConfigValue<Double> SKILL_MOON_LIGHT_STRIKE_DAMAGE_MULTIPLIER;
    public static ForgeConfigSpec.ConfigValue<Double> SKILL_MOON_LIGHT_STRIKE_RANGE;

    // Enchant | 虚空附魔
    public static ForgeConfigSpec.ConfigValue<Integer> SKILL_ENCHANT_DURATION;

    // End of All Things | 万物终焉
    public static ForgeConfigSpec.ConfigValue<Double> SKILL_END_OF_ALL_THINGS_DAMAGE_MULTIPLIER;
    public static ForgeConfigSpec.ConfigValue<Double> SKILL_END_OF_ALL_THINGS_MAX_HEALTH_PERCENTAGE;
    public static ForgeConfigSpec.ConfigValue<Double> SKILL_END_OF_ALL_THINGS_RANGE;

    // Sword Wraith Generic | 通用剑灵配置
    public static ForgeConfigSpec.ConfigValue<Double> SWORD_WRAITH_HEALTH_PER_LEVEL;
    public static ForgeConfigSpec.ConfigValue<Double> SWORD_WRAITH_ATTACK_PER_LEVEL;
    public static ForgeConfigSpec.ConfigValue<Double> SWORD_WRAITH_HEALTH_PER_HIGH_LEVEL;
    public static ForgeConfigSpec.ConfigValue<Double> SWORD_WRAITH_ATTACK_PER_HIGH_LEVEL;
    public static ForgeConfigSpec.ConfigValue<Boolean> SWORD_WRAITH_AS_THE_LAST_END_ENTITY;
    public static ForgeConfigSpec.ConfigValue<Boolean> SWORD_WRAITH_ABSOLUTE_DESTRUCTION_DAMAGE;
    public static ForgeConfigSpec.ConfigValue<Double> SWORD_WRAITH_ABSOLUTE_DESTRUCTION_MULTIPLIER_LOW;
    public static ForgeConfigSpec.ConfigValue<Double> SWORD_WRAITH_ABSOLUTE_DESTRUCTION_MULTIPLIER_MID;
    public static ForgeConfigSpec.ConfigValue<Double> SWORD_WRAITH_ABSOLUTE_DESTRUCTION_MULTIPLIER_HIGH;

    // ═══════════════════════════════════════════════════════════════════════════════════
    // Block Configuration | 方块配置
    // ═══════════════════════════════════════════════════════════════════════════════════

    // Dragon Crystal Enchanting Table | 龙水晶附魔台
    public static ForgeConfigSpec.ConfigValue<Integer> ENCHANTING_TABLE_ENERGY_CAPACITY;
    public static ForgeConfigSpec.ConfigValue<Integer> ENCHANTING_TABLE_ENERGY_RECEIVE_RATE;
    public static ForgeConfigSpec.ConfigValue<Integer> ENCHANTING_TABLE_ENERGY_EXTRACT_RATE;
    public static ForgeConfigSpec.ConfigValue<Integer> ENCHANTING_TABLE_CRYSTAL_POWER_TIME;
    public static ForgeConfigSpec.ConfigValue<Integer> ENCHANTING_TABLE_ENERGY_PER_TICK;
    public static ForgeConfigSpec.ConfigValue<Integer> ENCHANTING_TABLE_ITEM_CHARGE_RATE;
    public static ForgeConfigSpec.ConfigValue<Integer> ENCHANTING_TABLE_ENCHANT_ENERGY_COST;
    public static ForgeConfigSpec.ConfigValue<Integer> ENCHANTING_TABLE_REMOVE_XP_RETURN;

    // ═══════════════════════════════════════════════════════════════════════════════════
    // Attack Configuration | 攻击系统配置
    // ═══════════════════════════════════════════════════════════════════════════════════

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
            .comment(
                "Damage increased per level of upgrade when level<6.",
                "等级小于 6 时，每升一级增加的伤害值。"
            )
            .define("Increase Value", 20.0);
        SWORD_INCREASE_VALUE_HIGH_LEVEL = BUILDER
            .comment(
                "Damage increased per level of upgrade when level>=6.",
                "等级大于等于 6 时，每升一级增加的伤害值。"
            )
            .define("Increase Value High Level", 200.0);
        SWORD_BLOCK_CANCEL_USE = BUILDER
            .comment(
                "Cancel sword right-click usage by blocking with shield",
                "副手持盾格挡时取消剑的右键功能"
            )
            .define("Block Cancel Use", true);
        BUILDER.pop();

        // Dragon Crystal Sword Settings | 龙水晶剑设置
        BUILDER.push("Dragon Crystal Sword");
        DRAGON_CRYSTAL_SWORD_CAN_MINE = BUILDER
            .comment(
                "Allow Dragon Crystal Sword to mine blocks",
                "允许龙水晶剑挖掘方块"
            )
            .define("Can Mine", true);
        BUILDER.pop();

        // Dragon Sword Settings | 龙之剑设置
        BUILDER.push("Dragon Sword");
        DRAGON_SWORD_SUMMON_COOLDOWN = BUILDER
            .comment(
                "Cooldown time for Dragon Sword summoning mode in ticks (20 ticks = 1 second)",
                "龙之剑唤灵模式的冷却时间（tick；20 tick = 1 秒）"
            )
            .defineInRange("Summon Cooldown", 600, 0, Integer.MAX_VALUE);
        DRAGON_SWORD_NORMAL_MODE_CAN_MINE = BUILDER
            .comment(
                "Allow Dragon Sword to mine blocks in normal mode",
                "允许龙之剑在普通模式下挖掘方块"
            )
            .define("Normal Mode Can Mine", true);
        DRAGON_SWORD_SUMMON_MODE_CAN_MINE = BUILDER
            .comment(
                "Allow Dragon Sword to mine blocks in summon mode",
                "允许龙之剑在唤灵模式下挖掘方块"
            )
            .define("Summon Mode Can Mine", true);
        BUILDER.pop();

        // Dragon Soul Lantern Settings | 龙魂灯设置
        BUILDER.push("Dragon Soul Lantern");
        DRAGON_SOUL_LANTERN_SUMMON_COOLDOWN = BUILDER
            .comment(
                "Cooldown time for Dragon Soul Lantern summoning in ticks (20 ticks = 1 second)",
                "龙魂灯召唤的冷却时间（tick；20 tick = 1 秒）"
            )
            .defineInRange("Summon Cooldown", 1200, 0, Integer.MAX_VALUE);
        DRAGON_SOUL_LANTERN_RANGE_PLACED = BUILDER
            .comment(
                "Soul collection range when Dragon Soul Lantern is placed as a block (blocks)",
                "龙魂灯作为方块放置时的灵魂收集范围（格）"
            )
            .defineInRange("Range Placed", 16.0, 1.0, 64.0);
        DRAGON_SOUL_LANTERN_RANGE_EQUIPPED = BUILDER
            .comment(
                "Soul collection range when Dragon Soul Lantern is worn as a curio in belt slot (blocks)",
                "龙魂灯作为饰品佩戴在腰带槽位时的灵魂收集范围（格）"
            )
            .defineInRange("Range Equipped", 8.0, 1.0, 64.0);
        BUILDER.pop();

        // The Last End Sword Settings | 最终之剑设置
        BUILDER.push("The Last Sword");
        THE_LAST_SWORD_ALLOW_FLYING = BUILDER
            .comment(
                "Allows players to fly when holding The Last Sword",
                "允许玩家持有最终之剑时飞行"
            )
            .define("Allow Flying", true);
        THE_LAST_SWORD_SUPER_DESTROY = BUILDER
            .comment(
                "Allow The Last Sword in Powerful Mining Mode to break indestructible blocks (bedrock, barriers, etc.) via both left-click and right-click area mining.",
                "允许最终之剑在强力挖掘模式下破坏不可破坏的方块（基岩、屏障等），左键和右键范围挖掘均生效。"
            )
            .define("Super Destroy", true);
        THE_LAST_SWORD_MINING_RADIUS = BUILDER
            .comment(
                "Mining radius for Powerful Mining Mode",
                "强力挖掘模式的挖掘半径"
            )
            .defineInRange("Mining Radius", 3, 1, 12);
        THE_LAST_SWORD_ENABLE_MINING_PREVIEW = BUILDER
            .comment(
                "Render a preview outline of the powerful mining area before breaking blocks",
                "在执行强力挖掘前渲染范围预览方块边框"
            )
            .define("Enable Mining Preview", true);
        THE_LAST_SWORD_HIGH_PERFORMANCE_MINING = BUILDER
            .comment(
                "High performance mining mode: pack all drops into black shulker boxes and skip neighbor/lighting updates during area mining. Greatly reduces lag at large radius.",
                "高性能挖掘模式：将范围挖掘的所有掉落物打包进黑色潜影盒整体掉落，并跳过邻居方块/光照更新。可大幅缓解大范围挖掘的卡顿。"
            )
            .define("High Performance Mining", true);
        THE_LAST_SWORD_NORMAL_MODE_CAN_MINE = BUILDER
            .comment(
                "Allow The Last Sword to mine blocks in normal mode",
                "允许最终之剑在普通模式下挖掘方块"
            )
            .define("Normal Mode Can Mine", true);
        THE_LAST_SWORD_SUMMON_MODE_CAN_MINE = BUILDER
            .comment(
                "Allow The Last Sword to mine blocks in summon mode",
                "允许最终之剑在唤灵模式下挖掘方块"
            )
            .define("Summon Mode Can Mine", true);
        THE_LAST_SWORD_PERCENTAGE_DAMAGE = BUILDER
            .comment(
                "Percentage of target's max health added to extra damage (0.0 = disabled, 1.0 = 100%)",
                "在额外伤害基础上追加的目标最大生命值百分比（0.0 = 禁用，1.0 = 100%）"
            )
            .defineInRange("Percentage Damage", 0.13, 0.0, 1.0);
        THE_LAST_SWORD_ENABLE_STRONG_INVENTORY_PROTECTION = BUILDER
            .comment(
                "Enable strong inventory protection. When enabled, blocks external mods from writing/removing items in the inventory and replacing equipment slots, preventing forced disarm/replace. Disabled by default for compatibility with backpack-style mods (avoids item loss). Death-drop / /clear / respawn-restore protections are always active.",
                "启用强力背包保护。开启后将阻断外部 mod 对玩家背包/装备槽的写入与取出，防止强制缴械或替换装备。默认关闭以兼容背包类 mod，避免物品丢失。死亡掉落保护、/clear 保护、重生恢复始终生效。"
            )
            .define("Enable Strong Inventory Protection", false);
        BUILDER.pop();

        BUILDER.pop(); // End Sword

        // Armor Configuration | 护甲配置
        BUILDER.push("Armor");

        // Generic Armor Settings | 通用盔甲设置
        BUILDER.push("Generic");
        ARMOR_INCREASE_LOW_LEVEL = BUILDER
            .comment(
                "Armor value increased per level when level<6.",
                "等级小于 6 时，每升一级增加的护甲值。"
            )
            .define("Armor Increase Low Level", 1.0);
        ARMOR_INCREASE_HIGH_LEVEL = BUILDER
            .comment(
                "Armor value increased per level when level>=6.",
                "等级大于等于 6 时，每升一级增加的护甲值。"
            )
            .define("Armor Increase High Level", 2.0);
        TOUGHNESS_INCREASE_LOW_LEVEL = BUILDER
            .comment(
                "Armor toughness increased per level when level<6.",
                "等级小于 6 时，每升一级增加的盔甲韧性。"
            )
            .define("Toughness Increase Low Level", 1.0);
        TOUGHNESS_INCREASE_HIGH_LEVEL = BUILDER
            .comment(
                "Armor toughness increased per level when level>=6.",
                "等级大于等于 6 时，每升一级增加的盔甲韧性。"
            )
            .define("Toughness Increase High Level", 2.0);
        HEALTH_INCREASE_LOW_LEVEL = BUILDER
            .comment(
                "Max health increased per level when level<6.",
                "等级小于 6 时，每升一级增加的最大生命值。"
            )
            .define("Health Increase Low Level", 2.0);
        HEALTH_INCREASE_HIGH_LEVEL = BUILDER
            .comment(
                "Max health increased per level when level>=6.",
                "等级大于等于 6 时，每升一级增加的最大生命值。"
            )
            .define("Health Increase High Level", 4.0);
        JUSTIFIED_DEFENCE_INCREASE_LOW_LEVEL = BUILDER
            .comment(
                "Justified defence increased per level when level<6.",
                "等级小于 6 时，每升一级增加的肃正防御值。"
            )
            .define("Justified Defence Increase Low Level", 1.0);
        JUSTIFIED_DEFENCE_INCREASE_HIGH_LEVEL = BUILDER
            .comment(
                "Justified defence increased per level when level>=6.",
                "等级大于等于 6 时，每升一级增加的肃正防御值。"
            )
            .define("Justified Defence Increase High Level", 2.0);
        BUILDER.pop();

        // Dragon Crystal Armor Settings | 龙水晶盔甲设置
        BUILDER.push("Dragon Crystal Armor");
        DRAGON_CRYSTAL_ARMOR_CRYSTAL_GUARD_REFRESH_INTERVAL = BUILDER
            .comment(
                "Crystal Guard refresh interval in ticks.",
                "水晶守护效果的刷新间隔（tick）。"
            )
            .define("Crystal Guard Refresh Interval", 600);
        BUILDER.pop();

        // Dragon Armor Settings | 龙之战甲设置
        BUILDER.push("Dragon Armor");
        DRAGON_ARMOR_ENERGY_PER_LEVEL = BUILDER
            .comment(
                "Energy capacity increase per level (FE)",
                "Total capacity = Base (1,048,576) + Level × This value",
                "每升一级增加的能量容量（FE）",
                "总容量 = 基础值（1,048,576）+ 等级 × 此值"
            )
            .defineInRange("Energy Per Level", 102400, 0, Integer.MAX_VALUE);
        DRAGON_ARMOR_BUFF_ENHANCE_COST = BUILDER
            .comment(
                "Energy cost per piece per tick for buff level enhancement (Life Support Module)",
                "维生模块增强 Buff 等级时，每件装备每 tick 消耗的能量"
            )
            .defineInRange("Buff Enhance Cost Per Piece", 2, 0, Integer.MAX_VALUE);
        DRAGON_ARMOR_SATURATION_COST = BUILDER
            .comment(
                "Energy cost per piece per tick for Saturation effect (Life Support Module, full set)",
                "维生模块（穿戴全套）维持饱和效果时，每件装备每 tick 消耗的能量"
            )
            .defineInRange("Saturation Cost Per Piece", 2, 0, Integer.MAX_VALUE);
        DRAGON_ARMOR_ICE_FIRE_IMMUNITY_COST = BUILDER
            .comment(
                "Energy cost per piece per tick for Ice/Fire Immunity (Life Support Module, full set)",
                "维生模块（穿戴全套）冰火不侵效果时，每件装备每 tick 消耗的能量"
            )
            .defineInRange("Ice Fire Immunity Cost Per Piece", 2, 0, Integer.MAX_VALUE);
        DRAGON_ARMOR_PHASING_COST = BUILDER
            .comment(
                "Energy cost per piece per tick for Phasing effect (Phasing Module, full set)",
                "虚化模块（穿戴全套）启动虚化时，每件装备每 tick 消耗的能量"
            )
            .defineInRange("Phasing Cost Per Piece", 20, 0, Integer.MAX_VALUE);
        DRAGON_ARMOR_ENDER_CRYSTAL_CHARGE_RATE = BUILDER
            .comment(
                "Energy charge rate from End Crystals per tick (FE)",
                "End Crystals will charge all items with energy capability",
                "末影水晶每 tick 充能的能量速率（FE）",
                "末影水晶将为所有具备能量功能的物品充能"
            )
            .defineInRange("Ender Crystal Charge Rate", 200, 0, Integer.MAX_VALUE);
        DRAGON_ARMOR_ENDER_CRYSTAL_RANGE = BUILDER
            .comment(
                "Detection range for End Crystal charging (blocks)",
                "Players within this range wearing Dragon Armor will be charged",
                "末影水晶充能的检测范围（格）",
                "穿戴龙之战甲且处于此范围内的玩家将被充能"
            )
            .defineInRange("Ender Crystal Range", 8, 2, 32);
        DRAGON_ARMOR_PERCEPTION_GLOW_DURATION = BUILDER
            .comment(
                "Perception scan glow duration in seconds",
                "How long scanned entities remain highlighted",
                "感知模块扫描的发光持续时间（秒）",
                "被扫描的实体保持高亮显示的时长"
            )
            .defineInRange("Perception Glow Duration", 10, 1, 120);
        DRAGON_ARMOR_PERCEPTION_SCAN_RANGE = BUILDER
            .comment(
                "Perception scan range in blocks",
                "Maximum range for scanning nearby entities",
                "感知模块的扫描范围（格）",
                "扫描周围实体的最大范围"
            )
            .defineInRange("Perception Scan Range", 32, 2, 64);
        BUILDER.pop();

        BUILDER.pop(); // End Armor

        // Curios Configuration | 饰品配置
        BUILDER.push("Curios");

        // The Giver's Pain | 给予者的痛苦
        BUILDER.push("The Giver's Pain");
        CURIOS_GIVERS_PAIN_ATTACK_DAMAGE_BONUS = BUILDER
            .comment(
                "Worn attack damage bonus, MULTIPLY_TOTAL (1.0 = +100%)",
                "穿戴时攻击力加成，按最终值乘算（1.0 = +100%）"
            )
            .defineInRange("Worn Attack Damage Bonus", 1.0, 0.0, Double.MAX_VALUE);
        CURIOS_GIVERS_PAIN_ATTACK_SPEED_BONUS = BUILDER
            .comment(
                "Worn attack speed bonus, MULTIPLY_TOTAL (1.0 = +100%)",
                "穿戴时攻击速度加成，按最终值乘算（1.0 = +100%）"
            )
            .defineInRange("Worn Attack Speed Bonus", 1.0, 0.0, Double.MAX_VALUE);
        CURIOS_GIVERS_PAIN_ATTACK_DAMAGE_MULTIPLIER = BUILDER
            .comment(
                "Damage formula multiplier for attacker's attack damage component",
                "伤害公式中：攻击者攻击力部分的乘数"
            )
            .defineInRange("Damage Formula Attack Multiplier", 1.0, 0.0, Double.MAX_VALUE);
        CURIOS_GIVERS_PAIN_ATTACKER_LOST_HEALTH_MULTIPLIER = BUILDER
            .comment(
                "Damage formula multiplier for attacker's lost health component",
                "伤害公式中：攻击者已损失生命值部分的乘数"
            )
            .defineInRange("Damage Formula Attacker Lost Health Multiplier", 1.0, 0.0, Double.MAX_VALUE);
        CURIOS_GIVERS_PAIN_TARGET_LOST_HEALTH_MULTIPLIER = BUILDER
            .comment(
                "Damage formula multiplier for target's lost health component",
                "伤害公式中：目标已损失生命值部分的乘数"
            )
            .defineInRange("Damage Formula Target Lost Health Multiplier", 1.0, 0.0, Double.MAX_VALUE);
        CURIOS_GIVERS_PAIN_EFFECT_DURATION = BUILDER
            .comment(
                "Duration (ticks) of the random shared negative effect",
                "随机共享负面效果的持续时间（tick）"
            )
            .defineInRange("Pain Effect Duration", 120, 1, Integer.MAX_VALUE);
        CURIOS_GIVERS_PAIN_EFFECT_AMPLIFIER = BUILDER
            .comment(
                "Amplifier of the random shared negative effect",
                "随机共享负面效果的等级"
            )
            .defineInRange("Pain Effect Amplifier", 0, 0, Integer.MAX_VALUE);
        BUILDER.pop();

        // Dragon Crystal Ring | 龙水晶指环
        BUILDER.push("Dragon Crystal Ring");
        CURIOS_DRAGON_CRYSTAL_RING_DAMAGE_MULTIPLIER = BUILDER
            .comment(
                "Outgoing damage multiplier (1.5 = +50%)",
                "对敌人造成伤害的乘数（1.5 = +50%）"
            )
            .defineInRange("Damage Multiplier", 1.5, 0.0, Double.MAX_VALUE);
        BUILDER.pop();

        // Dragon Crystal Necklace | 龙水晶项链
        BUILDER.push("Dragon Crystal Necklace");
        CURIOS_DRAGON_CRYSTAL_NECKLACE_CRIT_CHANCE_BASE = BUILDER
            .comment(
                "Base extra critical hit damage bonus (0.2 = 20%)",
                "暴击时额外伤害的基础加成（0.2 = 20%）"
            )
            .defineInRange("Crit Chance Base", 0.2, 0.0, 10.0);
        CURIOS_DRAGON_CRYSTAL_NECKLACE_CRIT_CHANCE_PER_LUCK = BUILDER
            .comment(
                "Extra critical hit damage bonus per point of luck (0.2 = 20% per luck)",
                "每点幸运值增加的暴击额外伤害加成（0.2 = 每点幸运 +20%）"
            )
            .defineInRange("Crit Chance Per Luck", 0.2, 0.0, 10.0);
        CURIOS_DRAGON_CRYSTAL_NECKLACE_IMMUNITY_CHANCE_BASE = BUILDER
            .comment(
                "Base damage immunity chance (0.2 = 20%)",
                "免疫伤害的基础概率（0.2 = 20%）"
            )
            .defineInRange("Immunity Chance Base", 0.2, 0.0, 1.0);
        CURIOS_DRAGON_CRYSTAL_NECKLACE_IMMUNITY_CHANCE_PER_LUCK = BUILDER
            .comment(
                "Damage immunity chance added per point of luck",
                "每点幸运值增加的免疫伤害概率"
            )
            .defineInRange("Immunity Chance Per Luck", 0.2, 0.0, 1.0);
        CURIOS_DRAGON_CRYSTAL_NECKLACE_IMMUNITY_CHANCE_MAX = BUILDER
            .comment(
                "Maximum cap for damage immunity chance (0.9 = 90%)",
                "免疫伤害概率的上限（0.9 = 90%）"
            )
            .defineInRange("Immunity Chance Max", 0.9, 0.0, 1.0);
        BUILDER.pop();

        // Dragon Crystal Crown | 龙水晶王冠
        BUILDER.push("Dragon Crystal Crown");
        CURIOS_DRAGON_CRYSTAL_CROWN_VOID_CONVERSION_ENABLED = BUILDER
            .comment(
                "Enable converting all incoming damage to void damage",
                "启用将所有受到的伤害转换为虚空伤害"
            )
            .define("Void Damage Conversion Enabled", true);
        BUILDER.pop();

        // Wings That Cover The World | 覆世之翼
        BUILDER.push("Wings That Cover The World");
        CURIOS_WINGS_VOID_DAMAGE_REDUCTION = BUILDER
            .comment(
                "Void damage remaining multiplier (0.1 = take 10% / -90%)",
                "受到虚空伤害的剩余乘数（0.1 = 仅承受 10% / 减伤 90%）"
            )
            .defineInRange("Void Damage Reduction Multiplier", 0.1, 0.0, 1.0);
        CURIOS_WINGS_ICE_FIRE_IMMUNITY_ENABLED = BUILDER
            .comment(
                "Worn immunity to fire and freezing",
                "穿戴时免疫火焰与冰冻"
            )
            .define("Ice Fire Immunity Enabled", true);
        BUILDER.pop();

        // Extreme Life Support Device | 极限维生装置
        BUILDER.push("Extreme Life Support Device");
        CURIOS_EXTREME_LIFE_SUPPORT_TIER_THRESHOLD = BUILDER
            .comment(
                "Lost health fraction per effect tier (0.2 = +1 tier per 20% max HP lost)",
                "每提升一级所需的损失生命值比例（0.2 = 每损失 20% 最大生命值 +1 级）"
            )
            .defineInRange("Tier Threshold Ratio", 0.2, 0.01, 1.0);
        CURIOS_EXTREME_LIFE_SUPPORT_EFFECT_DURATION = BUILDER
            .comment(
                "Duration (ticks) of each regeneration/resistance/saturation refresh",
                "生命恢复 / 抗性提升 / 饱和效果每次刷新的持续时间（tick）"
            )
            .defineInRange("Effect Duration", 60, 1, Integer.MAX_VALUE);
        CURIOS_EXTREME_LIFE_SUPPORT_ARMOR_TOUGHNESS = BUILDER
            .comment(
                "Worn armor toughness bonus (flat addition)",
                "穿戴时盔甲韧性加成（直接相加）"
            )
            .defineInRange("Worn Armor Toughness", 100.0, 0.0, Double.MAX_VALUE);
        CURIOS_EXTREME_LIFE_SUPPORT_ENERGY_COST = BUILDER
            .comment(
                "Energy consumed per tick while worn (FE)",
                "穿戴时每 tick 消耗的能量（FE）"
            )
            .defineInRange("Energy Cost Per Tick", 1, 0, Integer.MAX_VALUE);
        CURIOS_EXTREME_LIFE_SUPPORT_MAX_ENERGY = BUILDER
            .comment(
                "Maximum energy storage (FE)",
                "最大能量存储上限（FE）"
            )
            .defineInRange("Max Energy", 102400, 0, Integer.MAX_VALUE);
        BUILDER.pop();

        // Dimension Explorer | 维度探索者
        BUILDER.push("Dimension Explorer");
        CURIOS_DIMENSION_EXPLORER_COOLDOWN = BUILDER
            .comment(
                "Death-save cooldown in ticks (1200 ticks = 60s)",
                "死亡守护的冷却时间（tick；1200 tick = 60 秒）"
            )
            .defineInRange("Cooldown", 1200, 0, Integer.MAX_VALUE);
        CURIOS_DIMENSION_EXPLORER_EFFECT_DURATION = BUILDER
            .comment(
                "Duration (ticks) of Phasing & Haste after death-save (260 ticks = 13s)",
                "死亡守护触发后虚化与急迫的持续时间（tick；260 tick = 13 秒）"
            )
            .defineInRange("Effect Duration", 260, 1, Integer.MAX_VALUE);
        CURIOS_DIMENSION_EXPLORER_EMERGENCY_HEAL_HEALTH = BUILDER
            .comment(
                "Health restored after death-save",
                "死亡守护触发后恢复至的生命值"
            )
            .defineInRange("Emergency Heal Health", 1.0, 0.1, Double.MAX_VALUE);
        CURIOS_DIMENSION_EXPLORER_EMERGENCY_FOOD_LEVEL = BUILDER
            .comment(
                "Food level set after death-save",
                "死亡守护触发后设置的饱食度"
            )
            .defineInRange("Emergency Food Level", 1, 0, 20);
        CURIOS_DIMENSION_EXPLORER_JUMP_AMPLIFIER = BUILDER
            .comment(
                "Passive Jump Boost amplifier (0 = Jump Boost I)",
                "被动跳跃提升的等级（0 = 跳跃提升 I）"
            )
            .defineInRange("Jump Boost Amplifier", 1, 0, 255);
        CURIOS_DIMENSION_EXPLORER_HASTE_AMPLIFIER = BUILDER
            .comment(
                "Haste amplifier after death-save (0 = Haste I)",
                "死亡守护触发后的急迫等级（0 = 急迫 I）"
            )
            .defineInRange("Haste Amplifier", 2, 0, 255);
        CURIOS_DIMENSION_EXPLORER_SPEED_AMPLIFIER = BUILDER
            .comment(
                "Speed amplifier after death-save (0 = Speed I)",
                "死亡守护触发后的速度等级（0 = 速度 I）"
            )
            .defineInRange("Speed Amplifier", 2, 0, 255);
        BUILDER.pop();

        BUILDER.pop(); // End Curios

        // The Last End Scroll Configuration | 终焉卷轴配置
        BUILDER.push("The Last End Scroll");
        THE_LAST_END_SCROLL_ENABLE_PARTICLE_EFFECTS = BUILDER
            .comment(
                "Enable particle effects in The Last End Scroll GUI",
                "启用终焉卷轴 GUI 中的粒子效果"
            )
            .define("Enable Particle Effects", true);
        BUILDER.pop();

        // Disposable Energy Battery | 一次性能量电池
        BUILDER.push("Disposable Energy Battery");
        DISPOSABLE_ENERGY_BATTERY_RESTORE_AMOUNT = BUILDER
            .comment(
                "FE restored to each non-full energy item on use; also the battery's own max energy",
                "使用时为每个未充满的FE物品恢复的能量，同时也是电池自身的最大能量上限"
            )
            .defineInRange("Restore Amount", 102400, 0, Integer.MAX_VALUE);
        BUILDER.pop();

        // Ancient Energy Core | 远古能量核心
        BUILDER.push("Ancient Energy Core");
        ANCIENT_ENERGY_CORE_MAX_ENERGY = BUILDER
            .comment(
                "Maximum energy capacity (FE) of the Ancient Energy Core",
                "远古能量核心的最大能量上限（FE）"
            )
            .defineInRange("Max Energy", 4194304, 0, Integer.MAX_VALUE);
        ANCIENT_ENERGY_CORE_CHARGE_RATE = BUILDER
            .comment(
                "Energy transferred per tick (FE/tick) when charging items from the Ancient Energy Core",
                "远古能量核心每tick向其他物品转移的能量（FE/tick）"
            )
            .defineInRange("Charge Rate", 4096, 0, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.pop(); // End Item

        // ═══════════════════════════════════════════════════════════════════════════════
        // Block Configuration | 方块配置
        // ═══════════════════════════════════════════════════════════════════════════════
        BUILDER.push("Block");

        BUILDER.push("Dragon Crystal Enchanting Table");
        ENCHANTING_TABLE_ENERGY_CAPACITY = BUILDER
            .comment(
                "Maximum energy capacity (FE)",
                "最大能量容量（FE）"
            )
            .define("Energy Capacity", 1073741824);
        ENCHANTING_TABLE_ENERGY_RECEIVE_RATE = BUILDER
            .comment(
                "Maximum energy receive rate (FE/t)",
                "最大能量接收速率（FE/t）"
            )
            .define("Energy Receive Rate", 9126);
        ENCHANTING_TABLE_ENERGY_EXTRACT_RATE = BUILDER
            .comment(
                "Maximum energy extract rate (FE/t)",
                "最大能量输出速率（FE/t）"
            )
            .define("Energy Extract Rate", 4096);
        ENCHANTING_TABLE_CRYSTAL_POWER_TIME = BUILDER
            .comment(
                "Power generation time per dragon crystal (ticks)",
                "每颗龙水晶可发电的时长（tick）"
            )
            .define("Crystal Power Time", 1800);
        ENCHANTING_TABLE_ENERGY_PER_TICK = BUILDER
            .comment(
                "Energy generated per tick when burning dragon crystal (FE/t)",
                "燃烧龙水晶时每 tick 产生的能量（FE/t）"
            )
            .define("Energy Per Tick", 10240);
        ENCHANTING_TABLE_ITEM_CHARGE_RATE = BUILDER
            .comment(
                "Maximum energy transferred per tick to the item in the charge slot (FE/t)",
                "每 tick 向充电槽位物品充能的最大速率（FE/t）"
            )
            .define("Item Charge Rate", 5120);
        ENCHANTING_TABLE_ENCHANT_ENERGY_COST = BUILDER
            .comment(
                "Energy cost per enchantment level (FE)",
                "每级附魔消耗的能量（FE）"
            )
            .define("Enchant Energy Cost", 10240);
        ENCHANTING_TABLE_REMOVE_XP_RETURN = BUILDER
            .comment(
                "XP returned per enchantment level removed",
                "每移除一级附魔返还的经验值"
            )
            .define("Remove XP Return", 10);
        BUILDER.pop();

        BUILDER.pop(); // End Block

        // ═══════════════════════════════════════════════════════════════════════════════
        // Entity Configuration | 实体配置
        // ═══════════════════════════════════════════════════════════════════════════════
        BUILDER.push("Entity");

        // The Last End Entity Settings | 终焉实体设置
        BUILDER.push("The Last End Entity");
        THE_LAST_END_ENTITY_ENABLE_ALL_THINGS_END = BUILDER
            .comment(
                "Enable automatic All Things End state activation",
                "启用万物终焉状态的自动激活"
            )
            .define("Enable All Things End Auto Trigger", true);
        BUILDER.pop();

        // The Last End Sword Wraith Settings | 终焉剑灵设置
        BUILDER.push("The Last End Sword Wraith");
        THE_LAST_END_SWORD_WRAITH_ENABLE_BATTLE_MUSIC = BUILDER
            .comment(
                "Enable battle music when The Last End Sword Wraith in battle",
                "终焉剑灵进入战斗时启用战斗音乐"
            )
            .define("Enable Spawn Music", true);

        // Skills Configuration | 技能配置
        BUILDER.push("Skills");

        // Swift Dash | 突刺
        BUILDER.push("Swift Dash");
        BUILDER.comment(
            "Automatically used when distance to target > 6 blocks (no weight config needed)",
            "当与目标的距离 > 6 格时自动使用（无需权重配置）"
        );
        SKILL_SWIFT_DASH_DAMAGE_MULTIPLIER = BUILDER
            .comment(
                "Damage multiplier for final strike (0.5 = 50% attack damage)",
                "最后一击的伤害乘数（0.5 = 攻击力的 50%）"
            )
            .defineInRange("Damage Multiplier", 0.5, 0.0, 10.0);
        SKILL_SWIFT_DASH_RANGE = BUILDER
            .comment(
                "Attack range for final strike in blocks",
                "最后一击的攻击范围（格）"
            )
            .defineInRange("Range", 4.0, 0.0, 32.0);
        BUILDER.pop();

        // Double Strike | 双连击
        BUILDER.push("Double Strike");
        SKILL_DOUBLE_STRIKE_DAMAGE_MULTIPLIER = BUILDER
            .comment(
                "Damage multiplier (1.0 = 100% attack damage)",
                "伤害乘数（1.0 = 攻击力的 100%）"
            )
            .defineInRange("Damage Multiplier", 1.0, 0.0, 10.0);
        SKILL_DOUBLE_STRIKE_RANGE = BUILDER
            .comment(
                "Attack range in blocks",
                "攻击范围（格）"
            )
            .defineInRange("Range", 4.0, 0.0, 32.0);
        BUILDER.pop();

        // Cross Slash | 十字切
        BUILDER.push("Cross Slash");
        SKILL_CROSS_SLASH_DAMAGE_MULTIPLIER = BUILDER
            .comment(
                "Damage multiplier (1.5 = 150% attack damage)",
                "伤害乘数（1.5 = 攻击力的 150%）"
            )
            .defineInRange("Damage Multiplier", 1.5, 0.0, 10.0);
        SKILL_CROSS_SLASH_RANGE = BUILDER
            .comment(
                "Attack range in blocks",
                "攻击范围（格）"
            )
            .defineInRange("Range", 6.0, 0.0, 32.0);
        BUILDER.pop();

        // Block | 格挡
        BUILDER.push("Block");
        SKILL_BLOCK_DAMAGE_MULTIPLIER = BUILDER
            .comment(
                "Damage multiplier for knockback strike (0.5 = 50% attack damage)",
                "击退攻击的伤害乘数（0.5 = 攻击力的 50%）"
            )
            .defineInRange("Damage Multiplier", 0.5, 0.0, 10.0);
        SKILL_BLOCK_RANGE = BUILDER
            .comment(
                "Range for knockback and projectile deflection in blocks",
                "击退及弹射物反弹的范围（格）"
            )
            .defineInRange("Range", 5.0, 0.0, 32.0);
        BUILDER.pop();

        // Moon Light Strike | 月华
        BUILDER.push("Moon Light Strike");
        SKILL_MOON_LIGHT_STRIKE_DAMAGE_MULTIPLIER = BUILDER
            .comment(
                "Damage multiplier (1.5 = 150% attack damage)",
                "伤害乘数（1.5 = 攻击力的 150%）"
            )
            .defineInRange("Damage Multiplier", 1.5, 0.0, 10.0);
        SKILL_MOON_LIGHT_STRIKE_RANGE = BUILDER
            .comment(
                "Attack range in blocks (sphere radius)",
                "攻击范围（格；球形半径）"
            )
            .defineInRange("Range", 6.0, 0.0, 32.0);
        BUILDER.pop();

        // Enchant | 虚空附魔
        BUILDER.push("Enchant");
        SKILL_ENCHANT_DURATION = BUILDER
            .comment(
                "Buff duration in ticks (600 ticks = 30 seconds)",
                "Buff 持续时间（tick；600 tick = 30 秒）"
            )
            .defineInRange("Duration", 600, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        // End of All Things | 万物终焉
        BUILDER.push("End of All Things");
        SKILL_END_OF_ALL_THINGS_DAMAGE_MULTIPLIER = BUILDER
            .comment(
                "Damage multiplier for periodic damage (1.0 = 100% attack damage per second)",
                "周期性伤害的乘数（1.0 = 每秒攻击力的 100%）"
            )
            .defineInRange("Damage Multiplier", 1.0, 0.0, 10.0);
        SKILL_END_OF_ALL_THINGS_MAX_HEALTH_PERCENTAGE = BUILDER
            .comment(
                "Additional damage as percentage of target's max health (0.05 = 5% max health per second)",
                "按目标最大生命值百分比追加的伤害（0.05 = 每秒最大生命值的 5%）"
            )
            .defineInRange("Max Health Percentage Damage", 0.05, 0.0, 1.0);
        SKILL_END_OF_ALL_THINGS_RANGE = BUILDER
            .comment(
                "Effect range in blocks (sphere radius)",
                "效果范围（格；球形半径）"
            )
            .defineInRange("Range", 32.0, 0.0, 128.0);
        BUILDER.pop();

        BUILDER.pop(); // End Skills

        BUILDER.pop(); // End The Last End Sword Wraith

        // Lost Wraith Settings | 迷失战魂设置
        BUILDER.push("Lost Wraith");
        LOST_WRAITH_ENABLE_CUSTOM_BOSS_BAR = BUILDER
            .comment(
                "Enable custom boss bar for Lost Wraith",
                "If disabled, vanilla purple boss bar will be used instead",
                "启用迷失战魂的自定义 Boss 血条",
                "若关闭，则改用原版紫色 Boss 血条"
            )
            .define("Enable Custom Boss Bar", true);
        LOST_WRAITH_DAMAGE_LIMIT = BUILDER
            .comment(
                "Maximum damage Lost Wraith can take per hit",
                "迷失战魂每次承受的最大伤害"
            )
            .defineInRange("Damage Limit", 10.0, 0.0, Double.MAX_VALUE);
        LOST_WRAITH_HURT_RESIST_TIME = BUILDER
            .comment(
                "Invulnerable ticks after being hurt (20 ticks = 1 second)",
                "受伤后的无敌时间（tick；20 tick = 1 秒）"
            )
            .defineInRange("Hurt Resist Time", 10, 0, Integer.MAX_VALUE);

        // Skills Configuration | 技能配置
        BUILDER.push("Skills");

        // Patience | 耐心
        BUILDER.push("Patience");
        BUILDER.comment(
            "Accumulate patience when target stays beyond trigger distance; timeout forces End Strike",
            "目标停留在触发距离外时累积耐心；超时则强制释放终焉一击"
        );
        LOST_WRAITH_PATIENCE_TRIGGER_DISTANCE = BUILDER
            .comment(
                "Accumulate patience when target is beyond this distance in blocks",
                "目标距离超过此值（格）时累积耐心"
            )
            .defineInRange("Trigger Distance", 4.0, 0.0, 32.0);
        LOST_WRAITH_PATIENCE_TIMEOUT = BUILDER
            .comment(
                "Ticks of continuous patience before End Strike is forced (20 ticks = 1 second)",
                "持续累积多少 tick 后强制释放终焉一击（20 tick = 1 秒）"
            )
            .defineInRange("Timeout", 240, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        // Enchant | 虚空附魔
        BUILDER.push("Enchant");
        LOST_WRAITH_ENCHANT_DURATION = BUILDER
            .comment(
                "Void Enchanting buff duration in ticks (1200 ticks = 60 seconds)",
                "虚空附魔 Buff 的持续时间（tick；1200 tick = 60 秒）"
            )
            .defineInRange("Duration", 1200, 1, Integer.MAX_VALUE);
        LOST_WRAITH_ENCHANT_AMPLIFIER = BUILDER
            .comment(
                "Void Enchanting buff amplifier (0 = level I)",
                "虚空附魔 Buff 的等级（0 = I 级）"
            )
            .defineInRange("Amplifier", 0, 0, 255);
        BUILDER.pop();

        // Dragon Fireball | 龙息弹
        BUILDER.push("Dragon Fireball");
        LOST_WRAITH_DRAGON_FIREBALL_MIN_DISTANCE = BUILDER
            .comment(
                "Minimum distance to target before this skill can be used in blocks",
                "释放此技能所需与目标的最小距离（格）"
            )
            .defineInRange("Min Distance", 4.0, 0.0, 32.0);
        LOST_WRAITH_DRAGON_FIREBALL_COOLDOWN = BUILDER
            .comment(
                "Cooldown between uses in ticks (20 ticks = 1 second)",
                "技能使用间隔的冷却时间（tick；20 tick = 1 秒）"
            )
            .defineInRange("Cooldown", 100, 0, Integer.MAX_VALUE);
        BUILDER.pop();

        // Summon Lightning | 召唤闪电
        BUILDER.push("Summon Lightning");
        LOST_WRAITH_LIGHTNING_MIN_DISTANCE = BUILDER
            .comment(
                "Minimum distance to target before this skill can be used in blocks",
                "释放此技能所需与目标的最小距离（格）"
            )
            .defineInRange("Min Distance", 4.0, 0.0, 32.0);
        LOST_WRAITH_LIGHTNING_COOLDOWN = BUILDER
            .comment(
                "Cooldown between uses in ticks (20 ticks = 1 second)",
                "技能使用间隔的冷却时间（tick；20 tick = 1 秒）"
            )
            .defineInRange("Cooldown", 80, 0, Integer.MAX_VALUE);
        LOST_WRAITH_LIGHTNING_AOE_RADIUS = BUILDER
            .comment(
                "AOE radius around strike point in blocks",
                "落点周围的范围伤害半径（格）"
            )
            .defineInRange("AOE Radius", 3.0, 0.0, 32.0);
        LOST_WRAITH_LIGHTNING_DAMAGE_MULTIPLIER = BUILDER
            .comment(
                "Damage multiplier for entities within AOE (2.0 = 200% attack damage)",
                "范围内实体所受伤害的乘数（2.0 = 攻击力的 200%）"
            )
            .defineInRange("Damage Multiplier", 2.0, 0.0, 10.0);
        BUILDER.pop();

        // Punch | 拳击
        BUILDER.push("Punch");
        LOST_WRAITH_PUNCH_ATTACK_DISTANCE = BUILDER
            .comment(
                "Distance within which this skill can trigger in blocks",
                "可触发此技能的距离范围（格）"
            )
            .defineInRange("Attack Distance", 4.0, 0.0, 32.0);
        LOST_WRAITH_PUNCH_FORWARD_STEPS = BUILDER
            .comment(
                "Number of forward blocks checked for knockback",
                "向前检测击退的格数"
            )
            .defineInRange("Forward Steps", 4, 1, 32);
        LOST_WRAITH_PUNCH_SIDE_HALF_WIDTH = BUILDER
            .comment(
                "Half width of knockback cone (1 means 3-wide cone)",
                "击退锥形的一半宽度（1 表示宽度为 3 的锥形）"
            )
            .defineInRange("Side Half Width", 1, 0, 32);
        BUILDER.pop();

        // End Strike | 终焉一击
        BUILDER.push("End Strike");
        LOST_WRAITH_END_STRIKE_COOLDOWN = BUILDER
            .comment(
                "Normal cooldown between uses in ticks (600 ticks = 30 seconds)",
                "技能正常的冷却时间（tick；600 tick = 30 秒）"
            )
            .defineInRange("Cooldown", 600, 0, Integer.MAX_VALUE);
        LOST_WRAITH_END_STRIKE_PULL_RADIUS = BUILDER
            .comment(
                "Radius within which entities are pulled in blocks",
                "拉拽实体的范围半径（格）"
            )
            .defineInRange("Pull Radius", 2.0, 0.0, 32.0);
        LOST_WRAITH_END_STRIKE_DAMAGE_MULTIPLIER = BUILDER
            .comment(
                "Damage as a percentage of caster's lost health (0.1 = 10% lost health)",
                "按施法者已损失生命值百分比计算的伤害（0.1 = 已损失生命值的 10%）"
            )
            .defineInRange("Damage Multiplier", 0.10, 0.0, 10.0);
        LOST_WRAITH_END_STRIKE_SHIELD_COOLDOWN = BUILDER
            .comment(
                "Short cooldown when triggered by shield block (260 ticks = 13 seconds)",
                "由盾牌格挡触发时的短冷却时间（260 tick = 13 秒）"
            )
            .defineInRange("Shield Cooldown", 260, 0, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.pop(); // End Skills

        BUILDER.pop(); // End Lost Wraith

        // Guardian Of Sealed Spire Settings | 封印尖塔守卫设置
        BUILDER.push("Guardian Of Sealed Spire");
        GUARDIAN_DAMAGE_LIMIT = BUILDER
            .comment(
                "Maximum damage guardian can take per hit",
                "封印尖塔守卫每次承受的最大伤害"
            )
            .defineInRange("Damage Limit", 10.0, 0.0, Double.MAX_VALUE);
        GUARDIAN_HURT_RESIST_TIME = BUILDER
            .comment(
                "Invulnerable ticks after being hurt (20 ticks = 1 second)",
                "受伤后的无敌时间（tick；20 tick = 1 秒）"
            )
            .defineInRange("Hurt Resist Time", 10, 0, Integer.MAX_VALUE);

        // Skills Configuration | 技能配置
        BUILDER.push("Skills");

        // Melee Attack | 近战攻击
        BUILDER.push("Melee Attack");
        GUARDIAN_MELEE_ATTACK_RANGE = BUILDER
            .comment(
                "Attack range in blocks",
                "近战攻击范围（格）"
            )
            .defineInRange("Attack Range", 3.0, 0.0, 32.0);
        BUILDER.pop();

        // Chase Target | 追击
        BUILDER.push("Chase Target");
        GUARDIAN_CHASE_APPROACH_DISTANCE = BUILDER
            .comment(
                "Start chasing when target is beyond this distance in blocks",
                "目标距离超过此值（格）时开始追击"
            )
            .defineInRange("Approach Distance", 2.5, 0.0, 32.0);
        BUILDER.pop();

        // Pickup Weapon | 拾取武器
        BUILDER.push("Pickup Weapon");
        GUARDIAN_PICKUP_DISTANCE = BUILDER
            .comment(
                "Scan radius for nearby weapon items in blocks",
                "扫描周围武器掉落物的半径（格）"
            )
            .defineInRange("Pickup Distance", 8.0, 0.0, 64.0);
        GUARDIAN_PICKUP_SCAN_INTERVAL = BUILDER
            .comment(
                "Ticks between scans for pickup candidates (20 ticks = 1 second)",
                "拾取目标扫描的间隔（tick；20 tick = 1 秒）"
            )
            .defineInRange("Scan Interval", 20, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        // Assist Ally | 协同
        BUILDER.push("Assist Ally");
        GUARDIAN_ASSIST_ALLY_MAX_SEARCH_DISTANCE = BUILDER
            .comment(
                "Maximum distance to search for ally targets in blocks",
                "搜索盟友目标的最大距离（格）"
            )
            .defineInRange("Max Search Distance", 32.0, 0.0, 128.0);
        BUILDER.pop();

        BUILDER.pop(); // End Skills

        // Guardian Archer Settings | 弓箭守卫设置（基类子节）
        BUILDER.push("Guardian Archer");

        // Skills Configuration | 技能配置
        BUILDER.push("Skills");

        // Ranged Attack | 远程攻击
        BUILDER.push("Ranged Attack");
        GUARDIAN_ARCHER_RANGED_MIN_DISTANCE = BUILDER
            .comment(
                "Minimum distance to target to use ranged attack in blocks",
                "使用远程攻击所需与目标的最小距离（格）"
            )
            .defineInRange("Min Distance", 4.0, 0.0, 64.0);
        GUARDIAN_ARCHER_RANGED_MAX_DISTANCE = BUILDER
            .comment(
                "Maximum distance to target to use ranged attack in blocks",
                "使用远程攻击所需与目标的最大距离（格）"
            )
            .defineInRange("Max Distance", 16.0, 0.0, 64.0);
        BUILDER.pop();

        // Maintain Distance | 保持距离
        BUILDER.push("Maintain Distance");
        GUARDIAN_ARCHER_MAINTAIN_MIN_DISTANCE = BUILDER
            .comment(
                "Retreat when target is closer than this distance in blocks",
                "目标距离小于此值（格）时撤退"
            )
            .defineInRange("Min Distance", 4.0, 0.0, 64.0);
        GUARDIAN_ARCHER_MAINTAIN_MAX_DISTANCE = BUILDER
            .comment(
                "Approach when target is beyond this distance in blocks",
                "目标距离超过此值（格）时靠近"
            )
            .defineInRange("Max Distance", 16.0, 0.0, 64.0);
        BUILDER.pop();

        BUILDER.pop(); // End Skills

        BUILDER.pop(); // End Guardian Archer

        // Guardian Berserker Settings | 狂战士守卫设置（基类子节）
        BUILDER.push("Guardian Berserker");
        GUARDIAN_BERSERKER_LIFESTEAL_RATIO = BUILDER
            .comment(
                "Lifesteal ratio on melee hit (0.05 = 5% of attack damage)",
                "近战命中时的吸血比例（0.05 = 攻击力的 5%）"
            )
            .defineInRange("Lifesteal Ratio", 0.05, 0.0, 10.0);
        BUILDER.pop();

        BUILDER.pop(); // End Guardian Of Sealed Spire

        // Projectile Settings | 弹射物设置
        BUILDER.push("Projectile");

        // Dragon Sword Projectile | 龙之剑弹射物
        BUILDER.push("Dragon Sword Projectile");
        DRAGON_SWORD_PROJECTILE_EXTRA_DAMAGE_MULTIPLIER = BUILDER
            .comment(
                "Multiplier applied to extra damage (1.0 = no change)",
                "应用于额外伤害的乘数（1.0 = 不变）"
            )
            .defineInRange("Extra Damage Multiplier", 1.0, 0.0, 100.0);
        BUILDER.pop();

        // Dragon Crystal Sword Projectile | 龙水晶剑弹射物
        BUILDER.push("Dragon Crystal Sword Projectile");
        DRAGON_CRYSTAL_SWORD_PROJECTILE_EXTRA_DAMAGE_MULTIPLIER = BUILDER
            .comment(
                "Multiplier applied to extra damage (1.0 = no change)",
                "应用于额外伤害的乘数（1.0 = 不变）"
            )
            .defineInRange("Extra Damage Multiplier", 1.0, 0.0, 100.0);
        BUILDER.pop();

        // The Last End Sword Projectile | 最终之剑弹射物
        BUILDER.push("The Last End Sword Projectile");
        THE_LAST_END_SWORD_PROJECTILE_EXTRA_DAMAGE_MULTIPLIER = BUILDER
            .comment(
                "Multiplier applied to extra damage (1.0 = no change)",
                "应用于额外伤害的乘数（1.0 = 不变）"
            )
            .defineInRange("Extra Damage Multiplier", 1.0, 0.0, 100.0);
        THE_LAST_END_SWORD_PROJECTILE_AOE_RADIUS = BUILDER
            .comment(
                "AOE damage radius in blocks",
                "范围伤害的半径（格）"
            )
            .defineInRange("AOE Radius", 2.5, 0.0, 64.0);
        BUILDER.pop();

        BUILDER.pop(); // End Projectile

        // Sword Wraith Generic Settings | 通用剑灵设置
        BUILDER.push("Sword Wraith");
        SWORD_WRAITH_HEALTH_PER_LEVEL = BUILDER
            .comment(
                "Health increase per sword level (Level 1-5)",
                "剑灵随武器等级（1-5 级）每级增加的生命值"
            )
            .defineInRange("Health Per Level", 100.0, 0.0, Double.MAX_VALUE);
        SWORD_WRAITH_ATTACK_PER_LEVEL = BUILDER
            .comment(
                "Attack damage increase per sword level (Level 1-5)",
                "剑灵随武器等级（1-5 级）每级增加的攻击力"
            )
            .defineInRange("Attack Per Level", 5.0, 0.0, Double.MAX_VALUE);
        SWORD_WRAITH_HEALTH_PER_HIGH_LEVEL = BUILDER
            .comment(
                "Health increase per sword level (Level 6+)",
                "剑灵随武器等级（6 级及以上）每级增加的生命值"
            )
            .defineInRange("Health Per High Level", 200.0, 0.0, Double.MAX_VALUE);
        SWORD_WRAITH_ATTACK_PER_HIGH_LEVEL = BUILDER
            .comment(
                "Attack damage increase per sword level (Level 6+)",
                "剑灵随武器等级（6 级及以上）每级增加的攻击力"
            )
            .defineInRange("Attack Per High Level", 10.0, 0.0, Double.MAX_VALUE);
        SWORD_WRAITH_AS_THE_LAST_END_ENTITY = BUILDER
            .comment(
                "Treat sword wraiths as The Last End Entities",
                "将剑灵视为终焉实体"
            )
            .define("Sword Wraith As End Entity", true);
        SWORD_WRAITH_ABSOLUTE_DESTRUCTION_DAMAGE = BUILDER
            .comment(
                "Enable absolute destruction damage for wraiths",
                "为剑灵启用绝毁伤害"
            )
            .define("Sword Wraith Absolute Destruction Damage", true);
        SWORD_WRAITH_ABSOLUTE_DESTRUCTION_MULTIPLIER_LOW = BUILDER
            .comment(
                "Absolute destruction damage multiplier for weapon levels 1-5",
                "武器等级 1-5 时的绝毁伤害乘数"
            )
            .defineInRange("Absolute Destruction Multiplier (Lv1-5)", 0.10, 0.0, 10.0);
        SWORD_WRAITH_ABSOLUTE_DESTRUCTION_MULTIPLIER_MID = BUILDER
            .comment(
                "Absolute destruction damage multiplier for weapon levels 6-12",
                "武器等级 6-12 时的绝毁伤害乘数"
            )
            .defineInRange("Absolute Destruction Multiplier (Lv6-12)", 0.50, 0.0, 10.0);
        SWORD_WRAITH_ABSOLUTE_DESTRUCTION_MULTIPLIER_HIGH = BUILDER
            .comment(
                "Absolute destruction damage multiplier for weapon level 13+",
                "武器等级 13 级及以上时的绝毁伤害乘数"
            )
            .defineInRange("Absolute Destruction Multiplier (Lv13+)", 1.00, 0.0, 10.0);
        BUILDER.pop();

        BUILDER.pop(); // End Entity

        // ═══════════════════════════════════════════════════════════════════════════════
        // Attack Configuration | 攻击系统配置
        // ═══════════════════════════════════════════════════════════════════════════════
        BUILDER.push("Attack");


        // Absolute Destruction Damage Settings | 绝毁伤害设置
        BUILDER.push("Absolute Destruction Damage");
        ABSOLUTE_DESTRUCTION_HEAL_NEGATION_TIME = BUILDER
            .comment(
                "Time (in seconds) to prevent healing. Set to 0 to disable heal negation",
                "禁止治疗的时间（秒）。设置为 0 则禁用此功能"
            )
            .defineInRange("Heal Negation Time", 30, 0, Integer.MAX_VALUE);
        ABSOLUTE_DESTRUCTION_REVIVE_BAN_TIME = BUILDER
            .comment(
                "Time (in seconds) to prevent respawning. Set to 0 to disable revive ban",
                "禁止复活的时间（秒）。设置为 0 则禁用此功能"
            )
            .defineInRange("Revive Ban Time", 5, 0, Integer.MAX_VALUE);
        ABSOLUTE_DESTRUCTION_DIE_MESSAGE = BUILDER
            .comment(
                "Enable or disable message when The Last End setDead",
                "启用或禁用绝毁判定死亡时的提示消息"
            )
            .define("Enable Die Message", true);
        ABSOLUTE_DESTRUCTION_PARTICLE_EFFECTS = BUILDER
            .comment(
                "Enable or disable particle effects when The Last End Death",
                "启用或禁用绝毁死亡时的粒子效果"
            )
            .define("Enable Particle Effects", true);
        ABSOLUTE_DESTRUCTION_ENABLE_THE_LAST_END_SETDEAD = BUILDER
            .comment(
                "Enable The Last End SetDead for entities with abnormal health (NaN, Infinite, or <= 0)",
                "对生命值异常（NaN、无穷大或 <= 0）的实体启用绝毁判定死亡"
            )
            .define("Enable The Last End SetDead", true);
        BUILDER.pop();

        BUILDER.pop(); // End Attack

        // ═══════════════════════════════════════════════════════════════════════════════
        // Defence Configuration | 防御系统配置
        // ═══════════════════════════════════════════════════════════════════════════════
        BUILDER.push("Defence");

        JUSTIFIED_DEFENCE_RECOVERY_TICK = BUILDER
            .comment(
                "Recovery interval for Justified Defence Shield in ticks (100 ticks = 5 seconds)",
                "肃正防御护盾的恢复间隔（tick；100 tick = 5 秒）"
            )
            .defineInRange("Justified Defence Recovery Tick", 100, 1, Integer.MAX_VALUE);

        DEFENCE_CUSTOM_HEALTH_DAMAGE_REDUCTION = BUILDER
            .comment(
                "Maximum percentage of health that can be lost per hit for defence system",
                "防御系统：每次受伤可损失的最大生命值百分比"
            )
            .defineInRange("Custom Health Damage Reduction", 0.05, 0.0, 1.0);

        DEFENCE_MAX_DAMAGE_PER_HIT = BUILDER
            .comment(
                "Maximum absolute damage that can be dealt per hit for defence system",
                "防御系统：每次受伤的最大绝对伤害值"
            )
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
            .comment(
                "Control whether to render particle effects",
                "控制是否渲染粒子效果"
            )
            .define("Particle Effects", true);
        BUFF_VOID_ENCHANTMENT_DAMAGE_PERCENTAGE = BUILDER
            .comment(
                "The percentage increase in damage by Buff",
                "虚空附魔 Buff 增加的伤害百分比"
            )
            .define("Damage Percentage", 0.2);
        BUILDER.pop();

        BUILDER.push("Phasing");
        BUFF_PHASING_BREAK_BLOCKS_ON_END = BUILDER
            .comment(
                "Whether to break suffocating blocks when Phasing effect ends",
                "虚化效果结束时是否破坏会令角色窒息的方块"
            )
            .define("Break Blocks On End", true);
        BUILDER.pop();

        BUILDER.pop(); // End Buff

        // Ender Dragon Egg Settings | 末影龙蛋设置
        BUILDER.push("Ender Dragon Egg");
        ENDER_DRAGON_EGG_DROP = BUILDER
            .comment(
                "Controls whether the Ender Dragon drops a dragon egg upon death",
                "控制末影龙死亡时是否掉落龙蛋"
            )
            .define("Drop Dragon Egg", true);
        ENDER_DRAGON_EGG_MULTIPLE = BUILDER
            .comment(
                "Controls whether multiple dragon eggs are given to players",
                "控制是否给予玩家多颗龙蛋"
            )
            .define("Multiple Dragon Eggs", true);
        ENDER_DRAGON_EGG_RADIUS = BUILDER
            .comment(
                "Radius to check for players to give dragon eggs",
                "检测可获得龙蛋的玩家的范围"
            )
            .define("Dragon Egg Radius", 64);
        ENDER_DRAGON_EGG_AMOUNT = BUILDER
            .comment(
                "Amount of dragon eggs to give per player",
                "每位玩家获得的龙蛋数量"
            )
            .define("Dragon Egg Amount", 1);
        ENDER_DRAGON_EGG_GIVE_TO_ABSENT_PLAYERS = BUILDER
            .comment(
                "Give dragon eggs to players who caused damage but are not present at death",
                "向曾造成伤害但死亡时不在场的玩家发放龙蛋"
            )
            .define("Give Egg To Absent Players", true);
        BUILDER.pop();

        // Stronger Ender Dragon Settings | 更强的末影龙设置
        BUILDER.push("Stronger Ender Dragon");
        ENDER_DRAGON_MAX_LEVEL = BUILDER
            .comment(
                "Maximum level for Ender Dragon",
                "末影龙的最大等级"
            )
            .define("Maximum Dragon Level", 13);
        ENDER_DRAGON_HEALTH_INCREASE_VALUE = BUILDER
            .comment(
                "Health increase per level when level < 6",
                "等级 < 6 时每升一级增加的生命值"
            )
            .define("Health Increase Value", 200.0);
        ENDER_DRAGON_HEALTH_INCREASE_VALUE_HIGH_LEVEL = BUILDER
            .comment(
                "Health increase per level when level >= 6",
                "等级 >= 6 时每升一级增加的生命值"
            )
            .define("Health Increase Value High Level", 1024.0);
        ENDER_DRAGON_ARMOR_INCREASE_VALUE = BUILDER
            .comment(
                "Armor increase per level when level < 6",
                "等级 < 6 时每升一级增加的护甲值"
            )
            .define("Armor Increase Value", 2.0);
        ENDER_DRAGON_ARMOR_INCREASE_VALUE_HIGH_LEVEL = BUILDER
            .comment(
                "Armor increase per level when level >= 6",
                "等级 >= 6 时每升一级增加的护甲值"
            )
            .define("Armor Increase Value High Level", 4.0);
        ENDER_DRAGON_ATTACK_INCREASE_VALUE = BUILDER
            .comment(
                "Attack increase per level when level < 6",
                "等级 < 6 时每升一级增加的攻击力"
            )
            .define("Attack Increase Value", 6.0);
        ENDER_DRAGON_ATTACK_INCREASE_VALUE_HIGH_LEVEL = BUILDER
            .comment(
                "Attack increase per level when level >= 6",
                "等级 >= 6 时每升一级增加的攻击力"
            )
            .define("Attack Increase Value High Level", 12.0);
        BUILDER.pop();

        // Compat Mods Configuration | 联动Mod配置
        BUILDER.push("Compat Mods");
        BUILDER.push("Cataclysm");
        COMPAT_CATACLYSM_ENABLE = BUILDER
            .comment(
                "Enable Cataclysm mod compatibility",
                "启用与 Cataclysm 模组的兼容"
            )
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

    //龙水晶剑配置
    public static boolean getDragonCrystalSwordCanMineSafely() {
        return safeGet(DRAGON_CRYSTAL_SWORD_CAN_MINE, true);
    }

    //龙之剑配置
    public static int getDragonSwordSummonCooldownSafely() {
        return safeGet(DRAGON_SWORD_SUMMON_COOLDOWN, 600);
    }

    public static boolean getDragonSwordNormalModeCanMineSafely() {
        return safeGet(DRAGON_SWORD_NORMAL_MODE_CAN_MINE, true);
    }

    public static boolean getDragonSwordSummonModeCanMineSafely() {
        return safeGet(DRAGON_SWORD_SUMMON_MODE_CAN_MINE, true);
    }

    //龙魂灯配置
    public static double getDragonSoulLanternRangePlacedSafely() {
        return safeGet(DRAGON_SOUL_LANTERN_RANGE_PLACED, 16.0);
    }

    public static double getDragonSoulLanternRangeEquippedSafely() {
        return safeGet(DRAGON_SOUL_LANTERN_RANGE_EQUIPPED, 8.0);
    }

    public static int getDragonSoulLanternSummonCooldownSafely() {
        return safeGet(DRAGON_SOUL_LANTERN_SUMMON_COOLDOWN, 1200);
    }

    //最终之剑配置
    public static boolean getAllowFlyingSafely() {
        return safeGet(THE_LAST_SWORD_ALLOW_FLYING, true);
    }

    public static boolean getSuperDestroySafely() {
        return safeGet(THE_LAST_SWORD_SUPER_DESTROY, true);
    }

    public static int getMiningRadiusSafely() {
        return safeGet(THE_LAST_SWORD_MINING_RADIUS, 3);
    }

    public static boolean getMiningPreviewEnabledSafely() {
        return safeGet(THE_LAST_SWORD_ENABLE_MINING_PREVIEW, true);
    }

    public static boolean getHighPerformanceMiningSafely() {
        return safeGet(THE_LAST_SWORD_HIGH_PERFORMANCE_MINING, true);
    }

    public static boolean getTheLastSwordNormalModeCanMineSafely() {
        return safeGet(THE_LAST_SWORD_NORMAL_MODE_CAN_MINE, true);
    }

    public static boolean getTheLastSwordSummonModeCanMineSafely() {
        return safeGet(THE_LAST_SWORD_SUMMON_MODE_CAN_MINE, true);
    }

    public static double getTheLastSwordPercentageDamageSafely() {
        return safeGet(THE_LAST_SWORD_PERCENTAGE_DAMAGE, 0.13);
    }

    public static boolean getEnableStrongInventoryProtectionSafely() {
        return safeGet(THE_LAST_SWORD_ENABLE_STRONG_INVENTORY_PROTECTION, false);
    }

    //龙晶护甲配置
    public static int getCrystalGuardRefreshIntervalSafely() {
        return safeGet(DRAGON_CRYSTAL_ARMOR_CRYSTAL_GUARD_REFRESH_INTERVAL, 600);
    }

    //龙之甲配置
    public static int getDragonArmorEnergyPerLevelSafely() {
        return safeGet(DRAGON_ARMOR_ENERGY_PER_LEVEL, 102400);
    }

    public static int getDragonArmorBuffEnhanceCostSafely() {
        return safeGet(DRAGON_ARMOR_BUFF_ENHANCE_COST, 2);
    }

    public static int getDragonArmorSaturationCostSafely() {
        return safeGet(DRAGON_ARMOR_SATURATION_COST, 2);
    }

    public static int getDragonArmorIceFireImmunityCostSafely() {
        return safeGet(DRAGON_ARMOR_ICE_FIRE_IMMUNITY_COST, 2);
    }

    public static int getDragonArmorPhasingCostSafely() {
        return safeGet(DRAGON_ARMOR_PHASING_COST, 20);
    }

    public static int getDragonArmorEnderCrystalChargeRateSafely() {
        return safeGet(DRAGON_ARMOR_ENDER_CRYSTAL_CHARGE_RATE, 200);
    }

    public static int getDragonArmorEnderCrystalRangeSafely() {
        return safeGet(DRAGON_ARMOR_ENDER_CRYSTAL_RANGE, 8);
    }

    public static int getPerceptionGlowDurationSafely() {
        return safeGet(DRAGON_ARMOR_PERCEPTION_GLOW_DURATION, 10);
    }

    public static int getPerceptionScanRangeSafely() {
        return safeGet(DRAGON_ARMOR_PERCEPTION_SCAN_RANGE, 32);
    }

    //终焉卷轴配置
    public static boolean getTheLastEndScrollEnableParticleEffectsSafely() {
        return safeGet(THE_LAST_END_SCROLL_ENABLE_PARTICLE_EFFECTS, true);
    }

    //一次性能量电池配置
    public static int getDisposableEnergyBatteryRestoreAmountSafely() {
        return safeGet(DISPOSABLE_ENERGY_BATTERY_RESTORE_AMOUNT, 102400);
    }

    //远古能量核心配置
    public static int getAncientEnergyCoreMaxEnergySafely() {
        return safeGet(ANCIENT_ENERGY_CORE_MAX_ENERGY, 4194304);
    }

    public static int getAncientEnergyCoreChargeRateSafely() {
        return safeGet(ANCIENT_ENERGY_CORE_CHARGE_RATE, 4096);
    }

    //防御系统配置
    public static int getJustifiedDefenceRecoveryTickSafely() {
        return safeGet(JUSTIFIED_DEFENCE_RECOVERY_TICK, 100);
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
        return safeGet(BUFF_VOID_ENCHANTMENT_PARTICLE_EFFECTS, true);
    }

    public static double getVoidEnchantmentDamagePercentageSafely() {
        return safeGet(BUFF_VOID_ENCHANTMENT_DAMAGE_PERCENTAGE, 0.2);
    }

    public static boolean getPhasingBreakBlocksOnEndSafely() {
        return safeGet(BUFF_PHASING_BREAK_BLOCKS_ON_END, true);
    }

    //迷失战魂配置
    public static boolean getLostWraithEnableCustomBossBarSafely() {
        return safeGet(LOST_WRAITH_ENABLE_CUSTOM_BOSS_BAR, true);
    }

    public static double getLostWraithDamageLimitSafely() {
        return safeGet(LOST_WRAITH_DAMAGE_LIMIT, 10.0);
    }

    public static int getLostWraithHurtResistTimeSafely() {
        return safeGet(LOST_WRAITH_HURT_RESIST_TIME, 10);
    }

    //迷失战魂技能配置
    //耐心
    public static double getLostWraithPatienceTriggerDistanceSafely() {
        return safeGet(LOST_WRAITH_PATIENCE_TRIGGER_DISTANCE, 4.0);
    }

    public static int getLostWraithPatienceTimeoutSafely() {
        return safeGet(LOST_WRAITH_PATIENCE_TIMEOUT, 240);
    }

    //虚空附魔
    public static int getLostWraithEnchantDurationSafely() {
        return safeGet(LOST_WRAITH_ENCHANT_DURATION, 1200);
    }

    public static int getLostWraithEnchantAmplifierSafely() {
        return safeGet(LOST_WRAITH_ENCHANT_AMPLIFIER, 0);
    }

    //龙息弹
    public static double getLostWraithDragonFireballMinDistanceSafely() {
        return safeGet(LOST_WRAITH_DRAGON_FIREBALL_MIN_DISTANCE, 4.0);
    }

    public static int getLostWraithDragonFireballCooldownSafely() {
        return safeGet(LOST_WRAITH_DRAGON_FIREBALL_COOLDOWN, 100);
    }

    //召唤闪电
    public static double getLostWraithLightningMinDistanceSafely() {
        return safeGet(LOST_WRAITH_LIGHTNING_MIN_DISTANCE, 4.0);
    }

    public static int getLostWraithLightningCooldownSafely() {
        return safeGet(LOST_WRAITH_LIGHTNING_COOLDOWN, 80);
    }

    public static double getLostWraithLightningAoeRadiusSafely() {
        return safeGet(LOST_WRAITH_LIGHTNING_AOE_RADIUS, 3.0);
    }

    public static double getLostWraithLightningDamageMultiplierSafely() {
        return safeGet(LOST_WRAITH_LIGHTNING_DAMAGE_MULTIPLIER, 2.0);
    }

    //拳击
    public static double getLostWraithPunchAttackDistanceSafely() {
        return safeGet(LOST_WRAITH_PUNCH_ATTACK_DISTANCE, 4.0);
    }

    public static int getLostWraithPunchForwardStepsSafely() {
        return safeGet(LOST_WRAITH_PUNCH_FORWARD_STEPS, 4);
    }

    public static int getLostWraithPunchSideHalfWidthSafely() {
        return safeGet(LOST_WRAITH_PUNCH_SIDE_HALF_WIDTH, 1);
    }

    //终焉一击
    public static int getLostWraithEndStrikeCooldownSafely() {
        return safeGet(LOST_WRAITH_END_STRIKE_COOLDOWN, 600);
    }

    public static double getLostWraithEndStrikePullRadiusSafely() {
        return safeGet(LOST_WRAITH_END_STRIKE_PULL_RADIUS, 2.0);
    }

    public static double getLostWraithEndStrikeDamageMultiplierSafely() {
        return safeGet(LOST_WRAITH_END_STRIKE_DAMAGE_MULTIPLIER, 0.10);
    }

    public static int getLostWraithEndStrikeShieldCooldownSafely() {
        return safeGet(LOST_WRAITH_END_STRIKE_SHIELD_COOLDOWN, 260);
    }

    //封印尖塔守卫配置
    public static double getGuardianDamageLimitSafely() {
        return safeGet(GUARDIAN_DAMAGE_LIMIT, 10.0);
    }

    public static int getGuardianHurtResistTimeSafely() {
        return safeGet(GUARDIAN_HURT_RESIST_TIME, 10);
    }

    //守卫共享技能配置
    public static double getGuardianMeleeAttackRangeSafely() {
        return safeGet(GUARDIAN_MELEE_ATTACK_RANGE, 3.0);
    }

    public static double getGuardianChaseApproachDistanceSafely() {
        return safeGet(GUARDIAN_CHASE_APPROACH_DISTANCE, 2.5);
    }

    public static double getGuardianPickupDistanceSafely() {
        return safeGet(GUARDIAN_PICKUP_DISTANCE, 8.0);
    }

    public static int getGuardianPickupScanIntervalSafely() {
        return safeGet(GUARDIAN_PICKUP_SCAN_INTERVAL, 20);
    }

    public static double getGuardianAssistAllyMaxSearchDistanceSafely() {
        return safeGet(GUARDIAN_ASSIST_ALLY_MAX_SEARCH_DISTANCE, 32.0);
    }

    //弓箭守卫配置
    public static double getGuardianArcherRangedMinDistanceSafely() {
        return safeGet(GUARDIAN_ARCHER_RANGED_MIN_DISTANCE, 4.0);
    }

    public static double getGuardianArcherRangedMaxDistanceSafely() {
        return safeGet(GUARDIAN_ARCHER_RANGED_MAX_DISTANCE, 16.0);
    }

    public static double getGuardianArcherMaintainMinDistanceSafely() {
        return safeGet(GUARDIAN_ARCHER_MAINTAIN_MIN_DISTANCE, 4.0);
    }

    public static double getGuardianArcherMaintainMaxDistanceSafely() {
        return safeGet(GUARDIAN_ARCHER_MAINTAIN_MAX_DISTANCE, 16.0);
    }

    //狂战士守卫配置
    public static double getGuardianBerserkerLifestealRatioSafely() {
        return safeGet(GUARDIAN_BERSERKER_LIFESTEAL_RATIO, 0.05);
    }

    //弹射物配置
    public static double getDragonSwordProjectileExtraDamageMultiplierSafely() {
        return safeGet(DRAGON_SWORD_PROJECTILE_EXTRA_DAMAGE_MULTIPLIER, 1.0);
    }

    public static double getDragonCrystalSwordProjectileExtraDamageMultiplierSafely() {
        return safeGet(DRAGON_CRYSTAL_SWORD_PROJECTILE_EXTRA_DAMAGE_MULTIPLIER, 1.0);
    }

    public static double getTheLastEndSwordProjectileExtraDamageMultiplierSafely() {
        return safeGet(THE_LAST_END_SWORD_PROJECTILE_EXTRA_DAMAGE_MULTIPLIER, 1.0);
    }

    public static double getTheLastEndSwordProjectileAoeRadiusSafely() {
        return safeGet(THE_LAST_END_SWORD_PROJECTILE_AOE_RADIUS, 2.5);
    }

    //终焉剑灵技能配置
    //突刺
    public static double getSkillSwiftDashDamageMultiplierSafely() {
        return safeGet(SKILL_SWIFT_DASH_DAMAGE_MULTIPLIER, 0.5);
    }

    public static double getSkillSwiftDashRangeSafely() {
        return safeGet(SKILL_SWIFT_DASH_RANGE, 4.0);
    }

    public static double getSkillDoubleStrikeDamageMultiplierSafely() {
        return safeGet(SKILL_DOUBLE_STRIKE_DAMAGE_MULTIPLIER, 1.0);
    }

    public static double getSkillDoubleStrikeRangeSafely() {
        return safeGet(SKILL_DOUBLE_STRIKE_RANGE, 4.0);
    }

    public static double getSkillCrossSlashDamageMultiplierSafely() {
        return safeGet(SKILL_CROSS_SLASH_DAMAGE_MULTIPLIER, 1.5);
    }

    public static double getSkillCrossSlashRangeSafely() {
        return safeGet(SKILL_CROSS_SLASH_RANGE, 6.0);
    }

    public static double getSkillBlockDamageMultiplierSafely() {
        return safeGet(SKILL_BLOCK_DAMAGE_MULTIPLIER, 0.5);
    }

    public static double getSkillBlockRangeSafely() {
        return safeGet(SKILL_BLOCK_RANGE, 5.0);
    }

    public static double getSkillMoonLightStrikeDamageMultiplierSafely() {
        return safeGet(SKILL_MOON_LIGHT_STRIKE_DAMAGE_MULTIPLIER, 1.5);
    }

    public static double getSkillMoonLightStrikeRangeSafely() {
        return safeGet(SKILL_MOON_LIGHT_STRIKE_RANGE, 6.0);
    }

    public static int getSkillEnchantDurationSafely() {
        return safeGet(SKILL_ENCHANT_DURATION, 600);
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

    //龙水晶附魔台配置
    public static int getEnchantingTableEnergyCapacitySafely() {
        return safeGet(ENCHANTING_TABLE_ENERGY_CAPACITY, 1073741824);
    }

    public static int getEnchantingTableEnergyReceiveRateSafely() {
        return safeGet(ENCHANTING_TABLE_ENERGY_RECEIVE_RATE, 9126);
    }

    public static int getEnchantingTableEnergyExtractRateSafely() {
        return safeGet(ENCHANTING_TABLE_ENERGY_EXTRACT_RATE, 4096);
    }

    public static int getEnchantingTableCrystalPowerTimeSafely() {
        return safeGet(ENCHANTING_TABLE_CRYSTAL_POWER_TIME, 1800);
    }

    public static int getEnchantingTableEnergyPerTickSafely() {
        return safeGet(ENCHANTING_TABLE_ENERGY_PER_TICK, 10240);
    }

    public static int getEnchantingTableItemChargeRateSafely() {
        return safeGet(ENCHANTING_TABLE_ITEM_CHARGE_RATE, 5120);
    }

    public static int getEnchantingTableEnchantEnergyCostSafely() {
        return safeGet(ENCHANTING_TABLE_ENCHANT_ENERGY_COST, 10240);
    }

    public static int getEnchantingTableRemoveXpReturnSafely() {
        return safeGet(ENCHANTING_TABLE_REMOVE_XP_RETURN, 10);
    }

    //饰品 - 给予者的痛苦
    public static double getCuriosGiversPainAttackDamageBonusSafely() {
        return safeGet(CURIOS_GIVERS_PAIN_ATTACK_DAMAGE_BONUS, 1.0);
    }

    public static double getCuriosGiversPainAttackSpeedBonusSafely() {
        return safeGet(CURIOS_GIVERS_PAIN_ATTACK_SPEED_BONUS, 1.0);
    }

    public static double getCuriosGiversPainAttackDamageMultiplierSafely() {
        return safeGet(CURIOS_GIVERS_PAIN_ATTACK_DAMAGE_MULTIPLIER, 1.0);
    }

    public static double getCuriosGiversPainAttackerLostHealthMultiplierSafely() {
        return safeGet(CURIOS_GIVERS_PAIN_ATTACKER_LOST_HEALTH_MULTIPLIER, 1.0);
    }

    public static double getCuriosGiversPainTargetLostHealthMultiplierSafely() {
        return safeGet(CURIOS_GIVERS_PAIN_TARGET_LOST_HEALTH_MULTIPLIER, 1.0);
    }

    public static int getCuriosGiversPainEffectDurationSafely() {
        return safeGet(CURIOS_GIVERS_PAIN_EFFECT_DURATION, 120);
    }

    public static int getCuriosGiversPainEffectAmplifierSafely() {
        return safeGet(CURIOS_GIVERS_PAIN_EFFECT_AMPLIFIER, 0);
    }

    //饰品 - 龙水晶指环
    public static double getCuriosDragonCrystalRingDamageMultiplierSafely() {
        return safeGet(CURIOS_DRAGON_CRYSTAL_RING_DAMAGE_MULTIPLIER, 1.5);
    }

    //饰品 - 龙水晶项链
    public static double getCuriosDragonCrystalNecklaceCritChanceBaseSafely() {
        return safeGet(CURIOS_DRAGON_CRYSTAL_NECKLACE_CRIT_CHANCE_BASE, 0.2);
    }

    public static double getCuriosDragonCrystalNecklaceCritChancePerLuckSafely() {
        return safeGet(CURIOS_DRAGON_CRYSTAL_NECKLACE_CRIT_CHANCE_PER_LUCK, 0.2);
    }

    public static double getCuriosDragonCrystalNecklaceImmunityChanceBaseSafely() {
        return safeGet(CURIOS_DRAGON_CRYSTAL_NECKLACE_IMMUNITY_CHANCE_BASE, 0.2);
    }

    public static double getCuriosDragonCrystalNecklaceImmunityChancePerLuckSafely() {
        return safeGet(CURIOS_DRAGON_CRYSTAL_NECKLACE_IMMUNITY_CHANCE_PER_LUCK, 0.2);
    }

    public static double getCuriosDragonCrystalNecklaceImmunityChanceMaxSafely() {
        return safeGet(CURIOS_DRAGON_CRYSTAL_NECKLACE_IMMUNITY_CHANCE_MAX, 0.9);
    }

    //饰品 - 龙水晶王冠
    public static boolean getCuriosDragonCrystalCrownVoidConversionEnabledSafely() {
        return safeGet(CURIOS_DRAGON_CRYSTAL_CROWN_VOID_CONVERSION_ENABLED, true);
    }

    //饰品 - 覆世之翼
    public static double getCuriosWingsVoidDamageReductionSafely() {
        return safeGet(CURIOS_WINGS_VOID_DAMAGE_REDUCTION, 0.1);
    }

    public static boolean getCuriosWingsIceFireImmunityEnabledSafely() {
        return safeGet(CURIOS_WINGS_ICE_FIRE_IMMUNITY_ENABLED, true);
    }

    //饰品 - 极限维生装置
    public static double getCuriosExtremeLifeSupportTierThresholdSafely() {
        return safeGet(CURIOS_EXTREME_LIFE_SUPPORT_TIER_THRESHOLD, 0.2);
    }

    public static int getCuriosExtremeLifeSupportEffectDurationSafely() {
        return safeGet(CURIOS_EXTREME_LIFE_SUPPORT_EFFECT_DURATION, 60);
    }

    public static double getCuriosExtremeLifeSupportArmorToughnessSafely() {
        return safeGet(CURIOS_EXTREME_LIFE_SUPPORT_ARMOR_TOUGHNESS, 100.0);
    }

    public static int getCuriosExtremeLifeSupportEnergyCostSafely() {
        return safeGet(CURIOS_EXTREME_LIFE_SUPPORT_ENERGY_COST, 1);
    }

    public static int getCuriosExtremeLifeSupportMaxEnergySafely() {
        return safeGet(CURIOS_EXTREME_LIFE_SUPPORT_MAX_ENERGY, 102400);
    }

    //饰品 - 维度探索者
    public static int getCuriosDimensionExplorerCooldownSafely() {
        return safeGet(CURIOS_DIMENSION_EXPLORER_COOLDOWN, 1200);
    }

    public static int getCuriosDimensionExplorerEffectDurationSafely() {
        return safeGet(CURIOS_DIMENSION_EXPLORER_EFFECT_DURATION, 260);
    }

    public static double getCuriosDimensionExplorerEmergencyHealHealthSafely() {
        return safeGet(CURIOS_DIMENSION_EXPLORER_EMERGENCY_HEAL_HEALTH, 1.0);
    }

    public static int getCuriosDimensionExplorerEmergencyFoodLevelSafely() {
        return safeGet(CURIOS_DIMENSION_EXPLORER_EMERGENCY_FOOD_LEVEL, 1);
    }

    public static int getCuriosDimensionExplorerJumpAmplifierSafely() {
        return safeGet(CURIOS_DIMENSION_EXPLORER_JUMP_AMPLIFIER, 1);
    }

    public static int getCuriosDimensionExplorerHasteAmplifierSafely() {
        return safeGet(CURIOS_DIMENSION_EXPLORER_HASTE_AMPLIFIER, 2);
    }

    public static int getCuriosDimensionExplorerSpeedAmplifierSafely() {
        return safeGet(CURIOS_DIMENSION_EXPLORER_SPEED_AMPLIFIER, 2);
    }

}
