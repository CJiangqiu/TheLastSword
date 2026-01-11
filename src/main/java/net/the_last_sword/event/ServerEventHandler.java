package net.the_last_sword.event;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.compat.curios.CuriosEffectHandler;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.init.ModBlocks;
import net.the_last_sword.init.ModItems;
import net.the_last_sword.item.DragonCrystalSoulStone;
import net.the_last_sword.item.TheLastEndArmorItem;
import net.the_last_sword.item.TheLastEndSwordItems;
import net.the_last_sword.network.ClearPreviewPacket;
import net.the_last_sword.network.NetworkHandler;
import net.the_last_sword.network.PreviewBlocksPacket;
import net.the_last_sword.recipe.ConfigRecipeManager;
import net.the_last_sword.util.EntityUtil;
import net.the_last_sword.util.TheLastSwordLogger;

import java.util.*;

//服务器事件处理器
@Mod.EventBusSubscriber(modid = TheLastSwordMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEventHandler {

    //========== 龙魂灯灵魂收集系统 ==========
    private static final double LANTERN_RANGE_PLACED = 16.0;  //放置模式范围
    private static final double LANTERN_RANGE_EQUIPPED = 8.0; //佩戴模式范围

    //实体死亡时检查龙魂灯笼范围并存储到魂石
    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }

        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        Level level = event.getEntity().level();
        BlockPos deathPos = event.getEntity().blockPosition();

        //优先检查佩戴模式：玩家装备龙魂灯在腰带槽位
        boolean hasEquippedLantern = CuriosEffectHandler.hasCurioEquipped(player, ModItems.DRAGON_SOUL_LANTERN.get());
        boolean inEquippedRange = player.blockPosition().distSqr(deathPos) <= LANTERN_RANGE_EQUIPPED * LANTERN_RANGE_EQUIPPED;

        //检查放置模式：16格范围内有激活的龙魂灯方块
        boolean hasPlacedLantern = hasNearbyDragonSoulLantern(level, deathPos);

        //两种模式都不满足，不收集灵魂
        if (!((hasEquippedLantern && inEquippedRange) || hasPlacedLantern)) {
            return;
        }

        ItemStack emptySoulStone = findEmptySoulStone(player);
        if (emptySoulStone.isEmpty()) {
            return;
        }

        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType());
        if (entityId != null) {
            CompoundTag nbt = emptySoulStone.getOrCreateTag();
            nbt.putString("wraith_entity_id", entityId.toString());

            //保存实体的完整NBT数据（包括装备、属性、自定义名称等）
            CompoundTag entityNBT = new CompoundTag();
            event.getEntity().save(entityNBT);

            //修正血量为最大生命值（避免保存死亡时的0血）
            float maxHealth = event.getEntity().getMaxHealth();
            entityNBT.putFloat("Health", maxHealth);

            nbt.put("entity_nbt", entityNBT);

            String entityName = event.getEntity().hasCustomName() ?
                event.getEntity().getCustomName().getString() :
                event.getEntity().getDisplayName().getString();

            player.sendSystemMessage(Component.translatable(
                "message.the_last_sword.dragon_soul_lantern.stored_success", entityName));
        }
    }

    //检查附近是否有激活的龙魂灯笼（底部必须有黑曜石或哭泣的黑曜石）
    private static boolean hasNearbyDragonSoulLantern(Level level, BlockPos center) {
        int range = (int) Math.ceil(LANTERN_RANGE_PLACED);

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos checkPos = center.offset(x, y, z);

                    if (center.distSqr(checkPos) <= LANTERN_RANGE_PLACED * LANTERN_RANGE_PLACED) {
                        if (level.getBlockState(checkPos).getBlock() == ModBlocks.DRAGON_SOUL_LANTERN.get()) {
                            if (isLanternActivated(level, checkPos)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    //检查龙魂灯笼是否激活（底部是否有黑曜石或哭泣的黑曜石）
    private static boolean isLanternActivated(Level level, BlockPos lanternPos) {
        BlockPos belowPos = lanternPos.below();
        BlockState belowState = level.getBlockState(belowPos);
        return belowState.is(Blocks.OBSIDIAN) || belowState.is(Blocks.CRYING_OBSIDIAN);
    }

    //查找玩家背包中的空魂石
    private static ItemStack findEmptySoulStone(Player player) {
        if (player.getMainHandItem().getItem() instanceof DragonCrystalSoulStone) {
            ItemStack stack = player.getMainHandItem();
            if (!DragonCrystalSoulStone.hasStoredEntity(stack)) {
                return stack;
            }
        }

        if (player.getOffhandItem().getItem() instanceof DragonCrystalSoulStone) {
            ItemStack stack = player.getOffhandItem();
            if (!DragonCrystalSoulStone.hasStoredEntity(stack)) {
                return stack;
            }
        }

        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof DragonCrystalSoulStone) {
                if (!DragonCrystalSoulStone.hasStoredEntity(stack)) {
                    return stack;
                }
            }
        }

        return ItemStack.EMPTY;
    }

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

    //玩家重生时清除防御数据，让物品重新注册
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();

        //清除所有防御数据，物品会在inventoryTick中重新注册
        EntityUtil.clearDefence(player);
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
