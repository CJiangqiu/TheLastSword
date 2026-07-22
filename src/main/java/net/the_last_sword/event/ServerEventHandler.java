package net.the_last_sword.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.summon.WraithSummonManager;
import net.the_last_sword.item.TheLastEndArmorItem;
import net.the_last_sword.item.TheLastEndSwordItems;
import net.the_last_sword.item.TheLastSword;
import net.the_last_sword.util.nbt.ItemModeHelper;
import net.the_last_sword.network.ArenaPreviewPacket;
import net.the_last_sword.network.ClearPreviewPacket;
import net.the_last_sword.network.NetworkHandler;
import net.the_last_sword.network.PreviewBlocksPacket;
import net.the_last_sword.network.SyncDragonCrystalRecipesPacket;
import net.the_last_sword.recipe.ConfigRecipeManager;
import net.the_last_sword.util.EntityUtil;
import net.the_last_sword.util.TheLastSwordLogger;

import java.util.*;
import java.util.Optional;

//服务器事件处理器
@Mod.EventBusSubscriber(modid = TheLastSwordMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEventHandler {

    //========== 龙魂灯灵魂收集系统 ==========

    //实体死亡时存储到魂石（捕获逻辑在剑灵管理器）
    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof Player killer)) {
            return;
        }
        WraithSummonManager.tryCaptureOnDeath(event.getEntity(), killer);
    }

    //兜底：实体被直接清除（不触发死亡事件）时尝试捕获
    @SubscribeEvent
    public static void onEntityForceRemoved(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity victim)) {
            return;
        }

        //仅处理被清除/击杀，排除区块卸载和维度切换
        Entity.RemovalReason reason = victim.getRemovalReason();
        if (reason != Entity.RemovalReason.DISCARDED && reason != Entity.RemovalReason.KILLED) {
            return;
        }

        WraithSummonManager.tryForceCapture(victim);
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
            arenaPreviewMap.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }
    }

    //服务器启动时加载配方
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        TheLastSwordLogger.info("Server starting, loading dragon crystal smithing recipes from config");
        ConfigRecipeManager.loadRecipes();
    }

    //登录时同步给单个玩家；/reload后重载config配方并同步给所有在线玩家
    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        ServerPlayer player = event.getPlayer();
        if (player != null) {
            syncDragonCrystalRecipes(player);
            return;
        }

        ConfigRecipeManager.reload();
        for (ServerPlayer onlinePlayer : event.getPlayerList().getPlayers()) {
            syncDragonCrystalRecipes(onlinePlayer);
        }
    }

    private static void syncDragonCrystalRecipes(ServerPlayer player) {
        NetworkHandler.sendToPlayer(
                new SyncDragonCrystalRecipesPacket(ConfigRecipeManager.getAllRecipes()),
                player
        );
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
        if (TheLastSwordConfiguration.getHighPerformanceMiningSafely() && world instanceof ServerLevel serverLevel) {
            executeMiningHighPerformance(serverLevel, player, preview);
        } else {
            executeMiningVanilla(world, player, preview);
        }
    }

    //原版兼容路径：逐方块 destroyBlock，掉落物逐个 spawn
    private static void executeMiningVanilla(Level world, Player player, MiningPreview preview) {
        boolean superDestroy = TheLastSwordConfiguration.getSuperDestroySafely();

        for (BlockPos pos : preview.blocksToDestroy) {
            BlockState state = world.getBlockState(pos);
            if (state.isAir()) continue;

            if (superDestroy) {
                if (state.getDestroySpeed(world, pos) < 0) {
                    //不可破坏方块：手动掉落方块物品后移除
                    Block.popResource(world, pos, new ItemStack(state.getBlock()));
                    world.removeBlock(pos, false);
                } else {
                    world.destroyBlock(pos, true, player);
                }
            } else if (state.getDestroySpeed(world, pos) >= 0) {
                world.destroyBlock(pos, true, player);
            }
        }
    }

    //高性能路径：跳过邻居/光照更新 + 掉落物聚合到黑色潜影盒
    private static void executeMiningHighPerformance(ServerLevel level, Player player, MiningPreview preview) {
        boolean superDestroy = TheLastSwordConfiguration.getSuperDestroySafely();
        ItemStack tool = player.getMainHandItem();
        int setFlags = Block.UPDATE_CLIENTS | Block.UPDATE_SUPPRESS_DROPS;

        List<ItemStack> collectedDrops = new ArrayList<>();
        BlockState airState = Blocks.AIR.defaultBlockState();

        for (BlockPos pos : preview.blocksToDestroy) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) continue;

            boolean indestructible = state.getDestroySpeed(level, pos) < 0;
            if (indestructible && !superDestroy) continue;

            BlockEntity be = level.getBlockEntity(pos);

            //收集战利品
            if (indestructible) {
                //不可破坏方块：直接掉方块本体
                collectedDrops.add(new ItemStack(state.getBlock()));
            } else {
                collectedDrops.addAll(Block.getDrops(state, level, pos, be, player, tool));
            }

            //容器内容：潜影盒交给战利品表（NBT 保留），其他容器手动抽走
            if (be instanceof Container container && !(be instanceof ShulkerBoxBlockEntity)) {
                for (int i = 0; i < container.getContainerSize(); i++) {
                    ItemStack item = container.getItem(i);
                    if (!item.isEmpty()) {
                        collectedDrops.add(item.copy());
                    }
                }
                container.clearContent();
            }

            //弱更新：跳过邻居刷新、跳过 onRemove 掉落
            level.setBlock(pos, airState, setFlags);
        }

        if (collectedDrops.isEmpty()) return;

        //合并堆叠后打包进黑色潜影盒
        List<ItemStack> shulkers = packIntoBlackShulkers(mergeStacks(collectedDrops));
        BlockPos dropPos = preview.centerPos;
        for (ItemStack shulker : shulkers) {
            Block.popResource(level, dropPos, shulker);
        }
    }

    //按物品+NBT合并堆叠
    private static List<ItemStack> mergeStacks(List<ItemStack> drops) {
        Map<net.minecraft.world.item.Item, List<ItemStack>> byItem = new HashMap<>();
        for (ItemStack drop : drops) {
            if (drop.isEmpty()) continue;
            List<ItemStack> bucket = byItem.computeIfAbsent(drop.getItem(), k -> new ArrayList<>());
            int remaining = drop.getCount();
            for (ItemStack existing : bucket) {
                if (remaining <= 0) break;
                if (!ItemStack.isSameItemSameTags(existing, drop)) continue;
                int canAdd = existing.getMaxStackSize() - existing.getCount();
                if (canAdd <= 0) continue;
                int take = Math.min(canAdd, remaining);
                existing.grow(take);
                remaining -= take;
            }
            while (remaining > 0) {
                ItemStack copy = drop.copy();
                int take = Math.min(remaining, copy.getMaxStackSize());
                copy.setCount(take);
                bucket.add(copy);
                remaining -= take;
            }
        }
        List<ItemStack> result = new ArrayList<>();
        for (List<ItemStack> bucket : byItem.values()) {
            result.addAll(bucket);
        }
        return result;
    }

    //打包到黑色潜影盒（每盒27格）
    private static List<ItemStack> packIntoBlackShulkers(List<ItemStack> merged) {
        List<ItemStack> shulkers = new ArrayList<>();
        ListTag items = new ListTag();
        int slot = 0;
        for (ItemStack stack : merged) {
            if (slot >= 27) {
                shulkers.add(buildBlackShulker(items));
                items = new ListTag();
                slot = 0;
            }
            CompoundTag itemTag = new CompoundTag();
            itemTag.putByte("Slot", (byte) slot);
            stack.save(itemTag);
            items.add(itemTag);
            slot++;
        }
        if (slot > 0) {
            shulkers.add(buildBlackShulker(items));
        }
        return shulkers;
    }

    //构造一个写好 BlockEntityTag.Items 的黑色潜影盒 ItemStack
    private static ItemStack buildBlackShulker(ListTag items) {
        ItemStack shulker = new ItemStack(Blocks.BLACK_SHULKER_BOX);
        CompoundTag blockEntityTag = new CompoundTag();
        blockEntityTag.put("Items", items);
        shulker.getOrCreateTag().put("BlockEntityTag", blockEntityTag);
        return shulker;
    }

    //左键挖掘不可破坏方块时手动掉落物品
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer().level().isClientSide) return;

        BlockState state = event.getState();
        if (state.getDestroySpeed(event.getLevel(), event.getPos()) >= 0) return;

        ItemStack held = event.getPlayer().getMainHandItem();
        if (!(held.getItem() instanceof TheLastSword)) return;
        if (ItemModeHelper.getMode(held) != 1) return;
        if (!TheLastSwordConfiguration.getSuperDestroySafely()) return;

        if (event.getLevel() instanceof Level level) {
            Block.popResource(level, event.getPos(), new ItemStack(state.getBlock()));
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
        if (!TheLastSwordConfiguration.getMiningPreviewEnabledSafely()) {
            return;
        }
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

    //========== 竞技场预览系统 ==========
    private static final ResourceLocation ARENA_STRUCTURE = new ResourceLocation(TheLastSwordMod.MOD_ID, "the_last_end_arena");
    private static final Map<UUID, ArenaPreview> arenaPreviewMap = new HashMap<>();

    //根据玩家水平朝向获取旋转
    private static Rotation getRotationFromPlayer(Player player) {
        float yaw = player.getYRot() % 360;
        if (yaw < 0) yaw += 360;
        if (yaw >= 315 || yaw < 45) return Rotation.NONE;          // 南
        if (yaw >= 45 && yaw < 135) return Rotation.CLOCKWISE_90;  // 西
        if (yaw >= 135 && yaw < 225) return Rotation.CLOCKWISE_180; // 北
        return Rotation.COUNTERCLOCKWISE_90;                         // 东
    }

    //计算旋转后的结构边界
    private static BoundingBox getRotatedBoundingBox(Vec3i size, BlockPos placePos, Rotation rotation) {
        BlockPos endPos = switch (rotation) {
            case NONE -> placePos.offset(size.getX() - 1, size.getY() - 1, size.getZ() - 1);
            case CLOCKWISE_90 -> placePos.offset(-(size.getZ() - 1), size.getY() - 1, size.getX() - 1);
            case CLOCKWISE_180 -> placePos.offset(-(size.getX() - 1), size.getY() - 1, -(size.getZ() - 1));
            case COUNTERCLOCKWISE_90 -> placePos.offset(size.getZ() - 1, size.getY() - 1, -(size.getX() - 1));
        };
        return BoundingBox.fromCorners(placePos, endPos);
    }

    //执行竞技场放置（带预览系统）
    public static void performArenaPlacement(Player player, Level world) {
        if (world.isClientSide || !(world instanceof ServerLevel serverLevel)) return;

        UUID playerId = player.getUUID();
        ArenaPreview existingPreview = arenaPreviewMap.get(playerId);

        if (existingPreview != null && !existingPreview.isExpired()) {
            //已有预览：确认放置
            executeArenaPlacement(serverLevel, player, existingPreview);
            arenaPreviewMap.remove(playerId);
            clearClientPreview(player);
        } else {
            //没有预览：创建预览
            createArenaPreview(serverLevel, player);
        }
    }

    //创建竞技场预览
    private static void createArenaPreview(ServerLevel serverLevel, Player player) {
        StructureTemplateManager templateManager = serverLevel.getStructureManager();
        Optional<StructureTemplate> optional = templateManager.get(ARENA_STRUCTURE);
        if (optional.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.the_last_sword.arena.load_failed"));
            return;
        }

        StructureTemplate template = optional.get();
        Vec3i size = template.getSize();
        Rotation rotation = getRotationFromPlayer(player);

        //在玩家前方3格处放置
        Vec3 lookVec = player.getViewVector(1.0F);
        BlockPos placePos = player.blockPosition().offset(
                (int) Math.round(lookVec.x * 3),
                0,
                (int) Math.round(lookVec.z * 3)
        );

        BoundingBox box = getRotatedBoundingBox(size, placePos, rotation);
        BlockPos minPos = new BlockPos(box.minX(), box.minY(), box.minZ());
        BlockPos maxPos = new BlockPos(box.maxX(), box.maxY(), box.maxZ());

        ArenaPreview preview = new ArenaPreview(placePos, rotation, minPos, maxPos);
        arenaPreviewMap.put(player.getUUID(), preview);

        //发送预览到客户端
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.sendToPlayer(new ArenaPreviewPacket(minPos, maxPos), serverPlayer);
        }
    }

    //执行竞技场放置
    private static void executeArenaPlacement(ServerLevel serverLevel, Player player, ArenaPreview preview) {
        StructureTemplateManager templateManager = serverLevel.getStructureManager();
        Optional<StructureTemplate> optional = templateManager.get(ARENA_STRUCTURE);
        if (optional.isEmpty()) return;

        StructureTemplate template = optional.get();
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(preview.rotation)
                .setMirror(Mirror.NONE)
                .setIgnoreEntities(false);

        template.placeInWorld(serverLevel, preview.placePos, preview.placePos, settings, serverLevel.getRandom(), 2);
        player.sendSystemMessage(Component.translatable("message.the_last_sword.arena.placed"));
    }

    //取消竞技场预览
    public static void cancelArenaPreview(Player player) {
        ArenaPreview preview = arenaPreviewMap.remove(player.getUUID());
        if (preview != null && !preview.isExpired()) {
            clearClientPreview(player);
        }
    }

    //竞技场预览数据类
    public static class ArenaPreview {
        public final BlockPos placePos;
        public final Rotation rotation;
        public final BlockPos minPos;
        public final BlockPos maxPos;
        public final long timestamp;

        public ArenaPreview(BlockPos placePos, Rotation rotation, BlockPos minPos, BlockPos maxPos) {
            this.placePos = placePos;
            this.rotation = rotation;
            this.minPos = minPos;
            this.maxPos = maxPos;
            this.timestamp = System.currentTimeMillis();
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > 10000; //10秒过期
        }
    }
}
