package net.the_last_sword.init;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.effect.PhasingEffect;
import net.the_last_sword.effect.VoidEnchantingEffect;

public class ModEffects {

    public static final DeferredRegister<MobEffect> EFFECTS =
        DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, TheLastSwordMod.MOD_ID);

    //虚空附魔效果
    public static final RegistryObject<MobEffect> VOID_ENCHANTING = EFFECTS.register(
        "void_enchanting",
        VoidEnchantingEffect::new
    );

    //虚化效果
    public static final RegistryObject<MobEffect> PHASING = EFFECTS.register(
        "phasing",
        PhasingEffect::new
    );

    public static void register(IEventBus eventBus) {
        EFFECTS.register(eventBus);
    }
}
