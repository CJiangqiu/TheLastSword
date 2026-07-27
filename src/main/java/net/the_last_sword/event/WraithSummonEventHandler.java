package net.the_last_sword.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.summon.WraithSummonManager;

//剑灵召唤系统事件处理器
@Mod.EventBusSubscriber(modid = TheLastSwordMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WraithSummonEventHandler {

    private static final String SOUL_STONE_KEY = "the_last_sword_soul_stone";

    // ========== 玩家生命周期事件 ==========

    //玩家离线时自动唤回所有剑灵
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp && sp.level() instanceof ServerLevel lvl) {
            WraithSummonManager.logoutRecall(sp, lvl);
        }
    }

    //玩家切换维度时自动唤回所有剑灵（延迟到下一tick执行，避免干扰维度切换流程）
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp && sp.level() instanceof ServerLevel lvl) {
            lvl.getServer().execute(() -> {
                if (sp.isAlive() && sp.level() instanceof ServerLevel currentLevel) {
                    WraithSummonManager.logoutRecall(sp, currentLevel);
                }
            });
        }
    }

    //玩家实体克隆时复制魂石数据（维度切换、死亡重生时触发）
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getOriginal().getPersistentData().contains(SOUL_STONE_KEY)) {
            event.getEntity().getPersistentData().put(
                SOUL_STONE_KEY,
                event.getOriginal().getPersistentData().getCompound(SOUL_STONE_KEY).copy()
            );
        }
    }

    // ========== 剑灵实体事件 ==========

    //实体离开世界时更新剑灵状态（须先于 ECA 的自动退营执行，否则查不到主人）
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        WraithSummonManager.handleEntityLeaveLevel(event.getEntity(), event.getLevel());
    }

    //实体Tick处理剑灵AI
    @SubscribeEvent
    public static void onLivingEntityTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        WraithSummonManager.handleLivingEntityTick(event.getEntity());
    }

}
