package net.the_last_sword.compat.lucky_block.lucky_event;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.the_last_sword.TheLastSwordMod;

import java.util.UUID;

//竞技场对战追踪 - 给参战双方的 persistentData 打 UUID 互指标签, 任一方死亡时清除双方标签; 若玩家击杀对手则给予 64 附魔金苹果
@Mod.EventBusSubscriber(modid = TheLastSwordMod.MOD_ID)
public final class ArenaBattleHandler {

    private static final String TAG_KEY = "TLSArenaOpponent";
    private static final int REWARD_COUNT = 64;

    private ArenaBattleHandler() {}

    public static void tagParticipants(LivingEntity a, LivingEntity b) {
        a.getPersistentData().putUUID(TAG_KEY, b.getUUID());
        b.getPersistentData().putUUID(TAG_KEY, a.getUUID());
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity dying = event.getEntity();
        CompoundTag dyingTag = dying.getPersistentData();
        if (!dyingTag.hasUUID(TAG_KEY)) return;

        UUID opponentUuid = dyingTag.getUUID(TAG_KEY);
        dyingTag.remove(TAG_KEY);

        if (!(dying.level() instanceof ServerLevel serverLevel)) return;
        Entity opponent = serverLevel.getEntity(opponentUuid);
        if (opponent != null) {
            opponent.getPersistentData().remove(TAG_KEY);
        }

        if (dying instanceof Player) return;
        if (!(opponent instanceof Player player)) return;

        ItemStack reward = new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, REWARD_COUNT);
        player.addItem(reward);
        if (!reward.isEmpty()) {
            player.drop(reward, false);
        }
    }
}
