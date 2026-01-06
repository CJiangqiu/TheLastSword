package net.the_last_sword.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.the_last_sword.TheLastSwordMod;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, TheLastSwordMod.MOD_ID);

    //终焉剑灵战斗音乐
    public static final RegistryObject<SoundEvent> THE_LAST_END_SWORD_WRAITH =
            SOUNDS.register("the_last_end_sword_wraith",
                    () -> SoundEvent.createVariableRangeEvent(
                            new ResourceLocation(TheLastSwordMod.MOD_ID, "the_last_end_sword_wraith")));

    //迷失战魂战斗音乐
    public static final RegistryObject<SoundEvent> LOST_WRAITH =
            SOUNDS.register("lost_wraith",
                    () -> SoundEvent.createVariableRangeEvent(
                            new ResourceLocation(TheLastSwordMod.MOD_ID, "lost_wraith")));

    //危险技能提醒音效
    public static final RegistryObject<SoundEvent> ALARM =
            SOUNDS.register("alarm",
                    () -> SoundEvent.createVariableRangeEvent(
                            new ResourceLocation(TheLastSwordMod.MOD_ID, "alarm")));
}
