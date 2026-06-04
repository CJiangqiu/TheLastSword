package net.the_last_sword.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
import net.the_last_sword.compat.curios.CuriosEffectHandler;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.init.ModBlocks;
import net.the_last_sword.init.ModItems;
import net.the_last_sword.item.DragonCrystalSoulStone;
import net.the_last_sword.summon.WraithSummonManager;
import net.the_last_sword.item.TheLastEndArmorItem;
import net.the_last_sword.item.TheLastEndSwordItems;
import net.the_last_sword.item.TheLastSword;
import net.the_last_sword.util.nbt.ItemModeHelper;
import net.the_last_sword.network.ArenaPreviewPacket;
import net.the_last_sword.network.ClearPreviewPacket;
import net.the_last_sword.network.NetworkHandler;
import net.the_last_sword.network.PreviewBlocksPacket;
import net.the_last_sword.recipe.ConfigRecipeManager;
import net.the_last_sword.util.EntityUtil;
import net.the_last_sword.util.TheLastSwordLogger;

import java.util.*;
import java.util.Optional;

//服务器事件处理器
@Mod.EventBusSubscriber(modid = TheLastSwordMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEventHandler {

    //========== 龙魂灯灵魂收集系统 ==========

    //成功捕获后写入实体的去重标记键
    private static final String SOUL_CAPTURED_KEY = "the_last_sword_soul_captured";

    //实体死亡时检查龙魂灯范围并存储到魂石
    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }

        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        LivingEntity victim = event.getEntity();
        if (victim instanceof Player) {
            return;
        }

        boolean hasPlaced = hasNearbyDragonSoulLantern(victim.level(), victim.blockPosition());
        if (!playerMatchesLantern(victim, player, hasPlaced)) {
            return;
        }

        //成功存入后打标记，供移除事件去重
        if (tryCaptureToSoulStone(victim, player)) {
            victim.getPersistentData().putBoolean(SOUL_CAPTURED_KEY, true);
        }
    }

    //兜底：实体被直接清除（不触发死亡事件）时尝试捕获
    @SubscribeEvent
    public static void onEntityForceRemoved(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity victim) || victim instanceof Player) {
            return;
        }

        //仅处理被清除/击杀，排除区块卸载和维度切换
        Entity.RemovalReason reason = victim.getRemovalReason();
        if (reason != Entity.RemovalReason.DISCARDED && reason != Entity.RemovalReason.KILLED) {
            return;
        }

        //排除自己的剑灵被移除，以及死亡线已成功捕获的实体
        if (WraithSummonManager.isWraith(victim)) {
            return;
        }
        if (victim.getPersistentData().getBoolean(SOUL_CAPTURED_KEY)) {
            return;
        }

        Player owner = findNearbyCaptureOwner(victim);
        if (owner == null) {
            return;
        }

        if (tryCaptureToSoulStone(victim, owner)) {
            victim.getPersistentData().putBoolean(SOUL_CAPTURED_KEY, true);
        }
    }

    //把实体存入 owner 的空魂石，成功返回 true
    private static boolean tryCaptureToSoulStone(LivingEntity victim, Player owner) {
        ItemStack emptySoulStone = findEmptySoulStone(owner);
        if (emptySoulStone.isEmpty()) {
            return false;
        }

        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(victim.getType());
        if (entityId == null) {
            return false;
        }

        CompoundTag nbt = emptySoulStone.getOrCreateTag();
        nbt.putString("wraith_entity_id", entityId.toString());

        //保存实体的完整NBT数据（包括装备、属性、自定义名称等）
        CompoundTag entityNBT = new CompoundTag();
        victim.save(entityNBT);

        //修正血量为最大生命值（避免保存清除时的0血）
        entityNBT.putFloat("Health", victim.getMaxHealth());

        nbt.put("entity_nbt", entityNBT);

        String entityName = victim.hasCustomName() ?
            victim.getCustomName().getString() :
            victim.getDisplayName().getString();

        owner.sendSystemMessage(Component.translatable(
            "message.the_last_sword.dragon_soul_lantern.stored_success", entityName));
        return true;
    }

    //判断玩家相对死亡点是否满足龙魂灯捕获条件（hasPlaced 由外层预算）
    private static boolean playerMatchesLantern(LivingEntity victim, Player player, boolean hasPlaced) {
        if (hasPlaced) {
            return true;
        }
        double equippedRange = TheLastSwordConfiguration.getDragonSoulLanternRangeEquippedSafely();
        return CuriosEffectHandler.hasCurioEquipped(player, ModItems.DRAGON_SOUL_LANTERN.get())
            && player.blockPosition().distSqr(victim.blockPosition()) <= equippedRange * equippedRange;
    }

    //以实体为中心，在龙魂灯范围内找最近的、满足条件且有空魂石的玩家
    private static Player findNearbyCaptureOwner(LivingEntity victim) {
        Level level = victim.level();
        BlockPos deathPos = victim.blockPosition();
        double equippedRange = TheLastSwordConfiguration.getDragonSoulLanternRangeEquippedSafely();
        double placedRange = TheLastSwordConfiguration.getDragonSoulLanternRangePlacedSafely();
        double searchRangeSqr = Math.max(equippedRange, placedRange) * Math.max(equippedRange, placedRange);

        //先用便宜条件筛出范围内带空魂石的玩家，按距离升序
        List<Player> candidates = new ArrayList<>();
        for (Player player : level.players()) {
            if (player.blockPosition().distSqr(deathPos) > searchRangeSqr) {
                continue;
            }
            if (findEmptySoulStone(player).isEmpty()) {
                continue;
            }
            candidates.add(player);
        }
        if (candidates.isEmpty()) {
            return null;
        }
        candidates.sort(Comparator.comparingDouble(p -> p.blockPosition().distSqr(deathPos)));

        //放置模式扫描较贵，仅在确有候选玩家时算一次
        boolean hasPlaced = hasNearbyDragonSoulLantern(level, deathPos);

        for (Player player : candidates) {
            if (playerMatchesLantern(victim, player, hasPlaced)) {
                return player;
            }
        }
        return null;
    }

    //检查附近是否有激活的龙魂灯笼（底部必须有黑曜石或哭泣的黑曜石）
    private static boolean hasNearbyDragonSoulLantern(Level level, BlockPos center) {
        double rangeConfig = TheLastSwordConfiguration.getDragonSoulLanternRangePlacedSafely();
        int range = (int) Math.ceil(rangeConfig);
        double rangeSqr = rangeConfig * rangeConfig;

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos checkPos = center.offset(x, y, z);

                    if (center.distSqr(checkPos) <= rangeSqr) {
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
            arenaPreviewMap.entrySet().removeIf(entry -> entry.getValue().isExpired());
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
