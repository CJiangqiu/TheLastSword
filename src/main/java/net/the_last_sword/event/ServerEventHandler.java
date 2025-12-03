package net.the_last_sword.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.item.TheLastEndArmorItem;
import net.the_last_sword.item.TheLastEndSwordItems;
import net.the_last_sword.attack.AttackManager;
import net.the_last_sword.attack.AttackSavedData;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.defence.DefenceManager;
import net.the_last_sword.defence.DefenceSavedData;
import net.the_last_sword.summon.WraithSummonManager;
import net.the_last_sword.network.ClearPreviewPacket;
import net.the_last_sword.network.NetworkHandler;
import net.the_last_sword.network.PreviewBlocksPacket;
import net.the_last_sword.recipe.ConfigRecipeManager;
import net.the_last_sword.util.TheLastSwordLogger;

import java.util.*;

//服务器事件处理器
@Mod.EventBusSubscriber(modid = TheLastSwordMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEventHandler {

    //========== 掉落物保护系统 ==========

    //当实体加入世界时检测TheLastEndSwordItems和TheLastEndArmorItem掉落物并添加发光效果和保护
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ItemEntity itemEntity) {
            if (itemEntity.getItem().getItem() instanceof TheLastEndSwordItems ||
                itemEntity.getItem().getItem() instanceof TheLastEndArmorItem) {
                itemEntity.setGlowingTag(true);  //白色发光描边
                itemEntity.setExtendedLifetime();  //延长存在时间
            }
        }
    }

    //========== 挖掘预览系统 ==========
    private static final Map<UUID, MiningPreview> playerPreviews = new HashMap<>();

    //服务器Tick - 清理过期预览
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            playerPreviews.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }
    }

    //服务器启动时加载配方
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        TheLastSwordLogger.info("Server starting, loading dragon crystal smithing recipes from config");
        ConfigRecipeManager.loadRecipes();
    }

    //服务器启动后加载所有维度的数据
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        event.getServer().getAllLevels().forEach(AttackSavedData::loadToManager);
        event.getServer().getAllLevels().forEach(DefenceSavedData::loadToManager);
    }

    //服务器关闭前保存所有世界的数据
    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        if (AttackManager.needsSave()) {
            event.getServer().getAllLevels().forEach(AttackSavedData::persistData);
        }
        if (DefenceManager.needsSave()) {
            event.getServer().getAllLevels().forEach(DefenceSavedData::persistData);
        }
        DefenceManager.clearAll();
    }

    //玩家离线时立即持久化数据并自动唤回所有剑灵
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp && sp.level() instanceof ServerLevel lvl) {
            //自动唤回所有剑灵
            WraithSummonManager.logoutRecall(sp, lvl);

            //持久化其他数据
            if (AttackManager.needsSave()) {
                AttackSavedData.persistData(lvl);
            }
            if (DefenceManager.needsSave()) {
                DefenceSavedData.persistData(lvl);
            }
        }
    }

    //世界加载时恢复数据到管理器
    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel lvl) {
            AttackSavedData.loadToManager(lvl);
            DefenceSavedData.loadToManager(lvl);
        }
    }

    //世界卸载时保存数据
    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel lvl) {
            if (AttackManager.needsSave()) {
                AttackSavedData.persistData(lvl);
            }
            if (DefenceManager.needsSave()) {
                DefenceSavedData.persistData(lvl);
            }
        }
    }

    //========== 挖掘预览系统核心方法 ==========

    //执行挖掘（带预览系统）
    public static void performMining(Player player, Level world) {
        if (world.isClientSide) return;

        UUID playerId = player.getUUID();
        MiningPreview existingPreview = playerPreviews.get(playerId);

        if (existingPreview != null && !existingPreview.isExpired()) {
            //已有有效预览：执行挖掘
            executeMining(world, player, existingPreview);
            playerPreviews.remove(playerId);
            clearClientPreview(player);
        } else {
            //没有预览：创建预览
            createPreview(world, player);
        }
    }

    //创建预览
    private static void createPreview(Level world, Player player) {
        BlockHitResult hitResult = (BlockHitResult) player.pick(20.0D, 0.0F, false);
        BlockPos targetPos = hitResult.getBlockPos();
        BlockState targetState = world.getBlockState(targetPos);

        //检查目标方块是否可破坏
        if (targetState.getDestroySpeed(world, targetPos) < 0 && !TheLastSwordConfiguration.getSuperDestroySafely()) {
            return;
        }

        Set<BlockPos> blocksToDestroy = new HashSet<>();
        int radius = TheLastSwordConfiguration.getMiningRadiusSafely();

        //计算范围内所有要破坏的方块
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos currentPos = targetPos.offset(x, y, z);
                    BlockState currentState = world.getBlockState(currentPos);

                    if (currentState.isAir()) continue;

                    if (!TheLastSwordConfiguration.getSuperDestroySafely()) {
                        if (currentState.getDestroySpeed(world, currentPos) >= 0) {
                            blocksToDestroy.add(currentPos);
                        }
                    } else {
                        blocksToDestroy.add(currentPos);
                    }
                }
            }
        }

        //创建预览并发送到客户端
        MiningPreview preview = new MiningPreview(targetPos, blocksToDestroy);
        playerPreviews.put(player.getUUID(), preview);
        sendPreviewToClient(player, blocksToDestroy);
    }

    //执行挖掘
    private static void executeMining(Level world, Player player, MiningPreview preview) {
        boolean superDestroy = TheLastSwordConfiguration.getSuperDestroySafely();

        for (BlockPos pos : preview.blocksToDestroy) {
            BlockState state = world.getBlockState(pos);

            if (superDestroy) {
                //超级破坏模式：破坏所有方块并掉落物品
                world.destroyBlock(pos, true, player);
            } else if (state.getDestroySpeed(world, pos) >= 0) {
                //普通模式：只破坏可破坏的方块
                world.destroyBlock(pos, true, player);
            }
        }
    }

    //取消预览
    public static void cancelMiningPreview(Player player) {
        UUID playerId = player.getUUID();
        MiningPreview preview = playerPreviews.remove(playerId);

        if (preview != null && !preview.isExpired()) {
            clearClientPreview(player);
        }
    }

    //发送预览到客户端
    private static void sendPreviewToClient(Player player, Set<BlockPos> blocks) {
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.sendToPlayer(new PreviewBlocksPacket(blocks), serverPlayer);
        }
    }

    //清除客户端预览
    private static void clearClientPreview(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.sendToPlayer(new ClearPreviewPacket(), serverPlayer);
        }
    }

    //挖掘预览数据类
    public static class MiningPreview {
        public final BlockPos centerPos;
        public final Set<BlockPos> blocksToDestroy;
        public final long timestamp;

        public MiningPreview(BlockPos centerPos, Set<BlockPos> blocksToDestroy) {
            this.centerPos = centerPos;
            this.blocksToDestroy = blocksToDestroy;
            this.timestamp = System.currentTimeMillis();
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > 10000; //10秒过期
        }
    }
}
