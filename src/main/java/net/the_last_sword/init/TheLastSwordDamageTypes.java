package net.the_last_sword.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.the_last_sword.TheLastSwordMod;

/**
 * 本模组由数据包 JSON 注册的伤害类型对应的 Java 资源键。
 */
public final class TheLastSwordDamageTypes {

    public static final ResourceKey<DamageType> ABSOLUTE_DESTRUCTION = create("absolute_destruction");

    private TheLastSwordDamageTypes() {
    }

    private static ResourceKey<DamageType> create(String name) {
        return ResourceKey.create(
                Registries.DAMAGE_TYPE,
                new ResourceLocation(TheLastSwordMod.MOD_ID, name)
        );
    }
}
