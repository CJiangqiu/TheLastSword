package net.the_last_sword.entity.variant;

import net.minecraft.util.RandomSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * NBT-driven named Ender Dragon variants.
 *
 * <p>The numeric ids are part of the save/command interface and must remain stable.</p>
 */
public enum NamedDragonVariant {
    AKATOSH(1, "akatosh", 1.0, 1.0, 1.0, 1.0, 0.0, 2.0),
    ALDUIN(2, "alduin", 1.0, 1.0, 1.0, 1.0, 0.0, 2.0),
    PLACIDUSAX(3, "placidusax", 1.0, 1.0, 1.0, 1.0, 0.0, 0.0),
    PAARTHURNAX(4, "paarthurnax", 0.5, 0.5, 0.5, 0.5, 0.5, 0.0),
    ODAHVIING(5, "odahviing", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
    DURNEHVIIR(6, "durnehviir", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
    BAYLE(7, "bayle", -0.5, 3.0, 0.0, 0.0, 0.0, 0.0),
    GREYOLL(8, "greyoll", 3.0, -0.5, -0.5, -0.5, -0.5, 0.0);

    private static final Map<Integer, NamedDragonVariant> BY_ID;

    static {
        Map<Integer, NamedDragonVariant> variants = new HashMap<>();
        for (NamedDragonVariant variant : values()) {
            if (variants.put(variant.id, variant) != null) {
                throw new IllegalStateException("Duplicate named dragon id: " + variant.id);
            }
        }
        BY_ID = Collections.unmodifiableMap(variants);
    }

    private final int id;
    private final String translationKey;
    private final double healthBonus;
    private final double attackBonus;
    private final double armorBonus;
    private final double armorToughnessBonus;
    private final double flyingSpeedBonus;
    private final double justifiedDefenceBonus;

    NamedDragonVariant(int id, String nameKey, double healthBonus, double attackBonus,
                       double armorBonus, double armorToughnessBonus, double flyingSpeedBonus,
                       double justifiedDefenceBonus) {
        this.id = id;
        this.translationKey = "named_dragon.the_last_sword." + nameKey;
        this.healthBonus = healthBonus;
        this.attackBonus = attackBonus;
        this.armorBonus = armorBonus;
        this.armorToughnessBonus = armorToughnessBonus;
        this.flyingSpeedBonus = flyingSpeedBonus;
        this.justifiedDefenceBonus = justifiedDefenceBonus;
    }

    public int getId() {
        return id;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public double getHealthBonus() {
        return healthBonus;
    }

    public double getAttackBonus() {
        return attackBonus;
    }

    public double getArmorBonus() {
        return armorBonus;
    }

    public double getArmorToughnessBonus() {
        return armorToughnessBonus;
    }

    public double getFlyingSpeedBonus() {
        return flyingSpeedBonus;
    }

    public double getJustifiedDefenceBonus() {
        return justifiedDefenceBonus;
    }

    public static NamedDragonVariant byId(int id) {
        return BY_ID.get(id);
    }

    public static NamedDragonVariant random(RandomSource random) {
        NamedDragonVariant[] variants = values();
        return variants[random.nextInt(variants.length)];
    }
}
