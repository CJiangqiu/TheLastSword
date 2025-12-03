package net.the_last_sword.init;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.the_last_sword.TheLastSwordMod;

public class ModAttributes {

    public static final DeferredRegister<Attribute> ATTRIBUTES =
        DeferredRegister.create(ForgeRegistries.ATTRIBUTES, TheLastSwordMod.MOD_ID);

    //肃正防御护盾当前值
    public static final RegistryObject<Attribute> JUSTIFIED_DEFENCE = ATTRIBUTES.register(
        "justified_defence",
        () -> new RangedAttribute(
            "attribute.the_last_sword.justified_defence",
            0.0,
            0.0,
            Double.MAX_VALUE
        ).setSyncable(true)
    );

    //肃正防御护盾上限
    public static final RegistryObject<Attribute> MAX_JUSTIFIED_DEFENCE = ATTRIBUTES.register(
        "max_justified_defence",
        () -> new RangedAttribute(
            "attribute.the_last_sword.max_justified_defence",
            0.0,
            0.0,
            Double.MAX_VALUE
        ).setSyncable(true)
    );

    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }
}
