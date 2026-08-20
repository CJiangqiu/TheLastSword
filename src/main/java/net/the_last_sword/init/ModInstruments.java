package net.the_last_sword.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Instrument;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.the_last_sword.TheLastSwordMod;

public final class ModInstruments {

    //与原版山羊角一致的吹奏时长与传播距离
    public static final int USE_DURATION = 140;
    public static final float RANGE = 256.0F;

    private static final DeferredRegister<Instrument> INSTRUMENTS =
            DeferredRegister.create(Registries.INSTRUMENT, TheLastSwordMod.MOD_ID);

    //拜龙教号角，音色沿用原版袭击号角
    public static final RegistryObject<Instrument> DRAGON_CULT_HORN =
            INSTRUMENTS.register("dragon_cult_horn",
                    () -> new Instrument(SoundEvents.RAID_HORN, USE_DURATION, RANGE));

    //号角物品可用的乐器池
    public static final TagKey<Instrument> DRAGON_CULT_HORNS =
            TagKey.create(Registries.INSTRUMENT,
                    new ResourceLocation(TheLastSwordMod.MOD_ID, "dragon_cult_horns"));

    private ModInstruments() {
    }

    public static void register(IEventBus eventBus) {
        INSTRUMENTS.register(eventBus);
    }
}
