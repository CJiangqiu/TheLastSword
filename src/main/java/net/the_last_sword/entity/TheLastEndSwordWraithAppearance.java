package net.the_last_sword.entity;

import net.minecraft.resources.ResourceLocation;
import net.the_last_sword.TheLastSwordMod;

public enum TheLastEndSwordWraithAppearance {
    DEFAULT("the_last_end_sword_wraith", "gui.the_last_sword.sword_wraith_appearance.default",
            "entity.the_last_sword.the_last_end_sword_wraith"),
    THYSIA_TELENDRACON("thysia_telendracon", "gui.the_last_sword.sword_wraith_appearance.thysia_telendracon",
            "entity.the_last_sword.thysia_telendracon");

    private final String id;
    private final String nameKey;
    private final String entityNameKey;
    private final ResourceLocation model;
    private final ResourceLocation animation;
    private final ResourceLocation texture;

    TheLastEndSwordWraithAppearance(String id, String nameKey, String entityNameKey) {
        this.id = id;
        this.nameKey = nameKey;
        this.entityNameKey = entityNameKey;
        this.model = ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID, "geo/" + id + ".geo.json");
        this.animation = ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID, "animations/" + id + ".animation.json");
        this.texture = ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID, "textures/entities/" + id + ".png");
    }

    public String getId() {
        return id;
    }

    public String getNameKey() {
        return nameKey;
    }

    public String getEntityNameKey() {
        return entityNameKey;
    }

    public ResourceLocation getModel() {
        return model;
    }

    public ResourceLocation getAnimation() {
        return animation;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public TheLastEndSwordWraithAppearance previous() {
        TheLastEndSwordWraithAppearance[] appearances = values();
        return appearances[Math.floorMod(ordinal() - 1, appearances.length)];
    }

    public TheLastEndSwordWraithAppearance next() {
        TheLastEndSwordWraithAppearance[] appearances = values();
        return appearances[(ordinal() + 1) % appearances.length];
    }

    public static TheLastEndSwordWraithAppearance fromId(String id) {
        for (TheLastEndSwordWraithAppearance appearance : values()) {
            if (appearance.id.equals(id)) {
                return appearance;
            }
        }
        return DEFAULT;
    }
}
