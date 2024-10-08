package net.thelastsword.configuration;

import net.minecraftforge.common.ForgeConfigSpec;

public class TheLastSwordConfiguration {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<Double> INCREASE_VALUE;
    public static final ForgeConfigSpec.ConfigValue<Double> INCREASE_VALUE_HIGH_LEVEL;
    public static final ForgeConfigSpec.ConfigValue<Boolean> INVENTORY_PROTECTION;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ALLOW_FLYING;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ATTACK_VILLAGERS;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ATTACK_ANIMALS;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ATTACK_TAMED;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ATTACK_PLAYERS;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ATTACK_GOLEMS;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ATTACK_NEUTRAL;
    public static final ForgeConfigSpec.ConfigValue<Integer> RANGE_ATTACK_COOLDOWN;
    public static final ForgeConfigSpec.ConfigValue<Double> RANGE_ATTACK_RANGE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> DROP_DRAGON_EGG;
    public static final ForgeConfigSpec.ConfigValue<Boolean> MULTIPLE_DRAGON_EGGS;
    public static final ForgeConfigSpec.ConfigValue<Double> ABSOLUTE_DESTRUCTION_MULTIPLIER; // 新增配置项

    static {
        BUILDER.push("Increase Value");
        INCREASE_VALUE = BUILDER.comment("Damage increased per level of upgrade when level<6.")
                                .define("Increase Value", 200.0);
        INCREASE_VALUE_HIGH_LEVEL = BUILDER.comment("Damage increased per level of upgrade when level>=6.")
                                           .define("Increase Value High Level", 1024.0);
        BUILDER.pop();

        BUILDER.push("Modes");
        INVENTORY_PROTECTION = BUILDER.comment("Controls the inventory protection feature of The Last Sword, which prevents death drops.")
                                      .define("Inventory Protection", true);
        ALLOW_FLYING = BUILDER.comment("Allows players to fly when holding The Last Sword.")
                              .define("Allow Flying", true);
        ATTACK_VILLAGERS = BUILDER.comment("Allows attacking villagers.")
                                  .define("Attack Villagers", false);
        ATTACK_ANIMALS = BUILDER.comment("Allows attacking animals.")
                                .define("Attack Animals", true);
        ATTACK_TAMED = BUILDER.comment("Allows attacking tamed entities.")
                              .define("Attack Tamed", false);
        ATTACK_PLAYERS = BUILDER.comment("Allows attacking players.")
                                .define("Attack Players", false);
        ATTACK_GOLEMS = BUILDER.comment("Allows attacking golems.")
                               .define("Attack Golems", false);
        ATTACK_NEUTRAL = BUILDER.comment("Allows attacking neutral mobs.")
                                .define("Attack Neutral", false);
        RANGE_ATTACK_COOLDOWN = BUILDER.comment("Cooldown time for range attack.")
                                       .define("Range Attack Cooldown", 20);
        RANGE_ATTACK_RANGE = BUILDER.comment("Range for powerful range attack.")
                                    .define("Range Attack Range", 64.0);
        BUILDER.pop();

        BUILDER.push("Dragon Egg Options");
        DROP_DRAGON_EGG = BUILDER.comment("Controls whether the Ender Dragon drops a dragon egg upon death.")
                                 .define("Drop Dragon Egg", true);
        MULTIPLE_DRAGON_EGGS = BUILDER.comment("Controls whether multiple dragon eggs are given to players who dealt significant damage.")
                                      .define("Multiple Dragon Eggs", true);
        BUILDER.pop();

        BUILDER.push("Absolute Destruction");
        ABSOLUTE_DESTRUCTION_MULTIPLIER = BUILDER.comment("Multiplier for absolute destruction damage.")
                                                 .define("Absolute Destruction Multiplier", 0.1);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
