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
                            ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID, "the_last_end_sword_wraith")));

    //迷失战魂战斗音乐
    public static final RegistryObject<SoundEvent> LOST_WRAITH =
            SOUNDS.register("lost_wraith",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID, "lost_wraith")));

    public static final RegistryObject<SoundEvent> THE_PAST_SHADOW_OF_THE_QUEEN =
            SOUNDS.register("the_past_shadow_of_the_queen",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID, "the_past_shadow_of_the_queen")));

    public static final RegistryObject<SoundEvent> WOUND_OF_TIME =
            SOUNDS.register("wound_of_time",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID, "wound_of_time")));

    //拜龙教袭击开始音乐
    public static final RegistryObject<SoundEvent> DRAGON_CULT_IS_COMING =
            SOUNDS.register("dragon_cult_is_coming",
                    () -> SoundEvent.createFixedRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID, "dragon_cult_is_coming"), 64.0F));

    //危险技能提醒音效
    public static final RegistryObject<SoundEvent> ALARM =
            SOUNDS.register("alarm",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID, "alarm")));

    public static final RegistryObject<SoundEvent> EXECUTION_1 =
            SOUNDS.register("execution_1",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID, "execution_1")));

    public static final RegistryObject<SoundEvent> EXECUTION_2 =
            SOUNDS.register("execution_2",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID, "execution_2")));
}
