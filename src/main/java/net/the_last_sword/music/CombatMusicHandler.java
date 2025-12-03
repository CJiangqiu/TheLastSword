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

        if (++musicCheckCounter < MUSIC_CHECK_INTERVAL) {
            return;
        }
        musicCheckCounter = 0;

        ClientLevel level = (ClientLevel) player.level();
        AABB searchArea = new AABB(
                player.getX() - 64,
                player.getY() - 32,
                player.getZ() - 64,
                player.getX() + 64,
                player.getY() + 32,
                player.getZ() + 64
        );

        //检测终焉剑灵（只有完成spawn状态的才计入）
        List<TheLastEndSwordWraithEntity> wraithEntities =
            level.getEntitiesOfClass(TheLastEndSwordWraithEntity.class, searchArea);
        wraithEntities.removeIf(entity -> !entity.getIsSpawned());

        //决定播放哪种音乐
        String requiredMusicType = "";
        if (!wraithEntities.isEmpty()) {
            requiredMusicType = "wraith";
        }

        if (requiredMusicType.isEmpty()) {
            //没有敌对实体，停止音乐
            if (isPlaying) {
                stopBattleMusic();
            }
        } else {
            //需要播放战斗音乐
            if (!isPlaying || !currentMusicType.equals(requiredMusicType)) {
                //如果没在播放或需要切换音乐类型
                if (isPlaying) {
                    stopBattleMusic();
                }
                playBattleMusic(requiredMusicType);
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
