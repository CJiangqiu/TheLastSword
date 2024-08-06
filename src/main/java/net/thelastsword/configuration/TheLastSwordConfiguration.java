package net.thelastsword.configuration;

import net.minecraftforge.common.ForgeConfigSpec;

public class TheLastSwordConfiguration {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<Double> INCREASE_VALUE;
    public static final ForgeConfigSpec.ConfigValue<Double> INCREASE_VALUE_HIGH_LEVEL;

    static {
        BUILDER.push("Increase Value");
        INCREASE_VALUE = BUILDER.comment("Damage increased per level of upgrade when level<6.").define("Increase Value", (double) 200);
        INCREASE_VALUE_HIGH_LEVEL = BUILDER.comment("Damage increased per level of upgrade when level>=6.").define("Increase Value High Level", (double) 1024);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
