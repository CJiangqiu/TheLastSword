package net.the_last_sword.raid;

import net.eca.api.RegisterRaid;
import net.eca.util.raid.RaidContext;
import net.eca.util.raid.RaidDefinition;
import net.eca.util.raid.RaidWave;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.the_last_sword.faction.DragonCultFaction;
import net.the_last_sword.init.ModEntities;
import net.the_last_sword.event.TheLastSwordQuestHandler;

import java.util.List;

@RegisterRaid
public class DragonCultRaid extends RaidDefinition {
    public static final String ID = "the_last_sword:dragon_cult_raid";
    private static final int HERO_OF_THE_VILLAGE_DURATION = 24 * 60 * 60 * 20;
    private static final int ENDER_EYE_REWARD = 16;
    private static final int RESULT_DISPLAY_TICKS = 12 * 20;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "raid.the_last_sword.dragon_cult_raid";
    }

    @Override
    public List<RaidWave> getWaves() {
        return List.of(
            new RaidWave()
                .addEntry(ModEntities.DRAGON_CULTIST.get(), 2),
            new RaidWave()
                .addEntry(ModEntities.DRAGON_CULT_PALADIN.get(), 1)
                .addEntry(ModEntities.DRAGON_CULTIST.get(), 3),
            new RaidWave()
                .addEntry(ModEntities.DRAGON_CULT_PRIEST.get(), 1)
                .addEntry(ModEntities.DRAGON_CULT_PALADIN.get(), 2)
                .addEntry(ModEntities.DRAGON_CULTIST.get(), 3)
        );
    }

    @Override
    public TagKey<Structure> getTargetStructureTag() {
        return StructureTags.VILLAGE;
    }

    @Override
    public String getRaiderFactionId() {
        return DragonCultFaction.ID;
    }

    @Override
    public BossEvent.BossBarColor getBossBarColor() {
        return BossEvent.BossBarColor.PURPLE;
    }

    @Override
    public int getCelebrationTicks() {
        return RESULT_DISPLAY_TICKS;
    }

    @Override
    public void onWaveStart(RaidContext context, int waveIndex) {
        context.getLevel().playSound(
            null,
            context.getCenter(),
            SoundEvents.RAID_HORN.value(),
            SoundSource.NEUTRAL,
            64.0F,
            1.0F
        );
    }

    @Override
    public void onVictory(RaidContext context) {
        for (var player : context.getNearbyPlayers()) {
            TheLastSwordQuestHandler.grant(player, TheLastSwordQuestHandler.DRAGON_CULT_RAID_VICTORY);
            player.addEffect(new MobEffectInstance(
                MobEffects.HERO_OF_THE_VILLAGE,
                HERO_OF_THE_VILLAGE_DURATION,
                0,
                false,
                true,
                true
            ));

            giveOrDrop(player, new ItemStack(Items.ENDER_EYE, ENDER_EYE_REWARD));
        }
    }

    private static void giveOrDrop(ServerPlayer player, ItemStack reward) {
        player.getInventory().add(reward);
        if (!reward.isEmpty()) {
            player.drop(reward, false);
        }
    }
}
