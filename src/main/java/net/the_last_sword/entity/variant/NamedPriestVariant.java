package net.the_last_sword.entity.variant;

import net.minecraft.util.RandomSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Stable NBT ids for named Dragon Cult Priest variants. */
public enum NamedPriestVariant {
    HEVNORAAK(1, "hevnoraak"),
    KROSIS(2, "krosis"),
    MOROKEI(3, "morokei"),
    NAHKRIIN(4, "nahkriin"),
    OTAR(5, "otar"),
    RAHGOT(6, "rahgot"),
    VOKUN(7, "vokun"),
    VOLSUNG(8, "volsung"),
    KONAHRIK(9, "konahrik"),
    MIRAAK(10, "miraak");

    private static final Map<Integer, NamedPriestVariant> BY_ID;

    static {
        Map<Integer, NamedPriestVariant> variants = new HashMap<>();
        for (NamedPriestVariant variant : values()) {
            if (variants.put(variant.id, variant) != null) {
                throw new IllegalStateException("Duplicate named priest id: " + variant.id);
            }
        }
        BY_ID = Collections.unmodifiableMap(variants);
    }

    private final int id;
    private final String translationKey;

    NamedPriestVariant(int id, String nameKey) {
        this.id = id;
        this.translationKey = "named_priest.the_last_sword." + nameKey;
    }

    public int getId() {
        return id;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public static NamedPriestVariant byId(int id) {
        return BY_ID.get(id);
    }

    public static NamedPriestVariant random(RandomSource random) {
        NamedPriestVariant[] variants = values();
        return variants[random.nextInt(variants.length)];
    }
}
