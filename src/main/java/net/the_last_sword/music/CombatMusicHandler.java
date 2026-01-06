package net.the_last_sword.music;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.LostWraithEntity;
import net.the_last_sword.entity.TheLastEndSwordWraithEntity;
import net.the_last_sword.init.ModSounds;

import java.util.List;

@Mod.EventBusSubscriber(modid = TheLastSwordMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class CombatMusicHandler {
    private static boolean isPlaying = false;
    private static SoundInstance currentMusic;
    private static String currentMusicType = "";

    private static int musicCheckCounter = 0;
    private static final int MUSIC_CHECK_INTERVAL = 20;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || mc.level == null || mc.isPaused()) {
            return;
        }

        if (!TheLastSwordConfiguration.THE_LAST_END_SWORD_WRAITH_ENABLE_BATTLE_MUSIC.get()) {
            if (isPlaying) stopBattleMusic();
            return;
        }

        //每20tick检测一次
        musicCheckCounter++;
        if (musicCheckCounter < MUSIC_CHECK_INTERVAL) {
            return;
        }
        musicCheckCounter = 0;

        //检测附近的Boss实体
        ClientLevel level = mc.level;
        AABB searchArea = player.getBoundingBox().inflate(64.0);
        String targetMusicType = null;

        //检测迷失战魂
        List<LostWraithEntity> nearbyLostWraiths = level.getEntitiesOfClass(
            LostWraithEntity.class,
            searchArea,
            wraith -> wraith.isSpawned() && wraith.isAlive() && !wraith.shouldLeave()
        );
        if (!nearbyLostWraiths.isEmpty()) {
            targetMusicType = "lost_wraith";
        }

        //检测终焉剑灵（后检测，会覆盖迷失战魂）
        List<TheLastEndSwordWraithEntity> nearbySwordWraiths = level.getEntitiesOfClass(
            TheLastEndSwordWraithEntity.class,
            searchArea,
            wraith -> wraith.isSpawned() && wraith.isAlive() && !wraith.shouldLeave()
        );
        if (!nearbySwordWraiths.isEmpty()) {
            targetMusicType = "wraith";
        }

        //根据检测结果播放或停止音乐
        if (targetMusicType != null) {
            //有Boss，播放对应音乐
            if (!isPlaying || !currentMusicType.equals(targetMusicType)) {
                stopBattleMusic();
                playBattleMusic(targetMusicType);
            }
        } else {
            //没有Boss，停止音乐
            if (isPlaying) {
                stopBattleMusic();
            }
        }
    }

    //播放战斗音乐
    public static void playBattleMusic(String musicType) {
        if (isPlaying) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        //根据音乐类型选择对应的音乐
        SoundEvent musicEvent;
        switch (musicType) {
            case "wraith":
                musicEvent = ModSounds.THE_LAST_END_SWORD_WRAITH.get();
                break;
            case "lost_wraith":
                musicEvent = ModSounds.LOST_WRAITH.get();
                break;
            default:
                return;
        }

        currentMusic = new SimpleSoundInstance(
                musicEvent.getLocation(),
                SoundSource.MUSIC,
                1.0F, 1.0F,
                SoundInstance.createUnseededRandom(),
                true,
                0,
                SoundInstance.Attenuation.NONE,
                player.getX(), player.getY(), player.getZ(),
                false
        );

        mc.getSoundManager().play(currentMusic);
        isPlaying = true;
        currentMusicType = musicType;
    }

    //停止战斗音乐
    private static void stopBattleMusic() {
        Minecraft mc = Minecraft.getInstance();
        if (currentMusic != null) {
            mc.getSoundManager().stop(currentMusic);
            currentMusic = null;
        }
        isPlaying = false;
        currentMusicType = "";
    }
}
