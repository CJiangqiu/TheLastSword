package net.the_last_sword.summon;

import net.eca.api.EcaAPI;
import net.eca.network.ClientRemovePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraftforge.registries.ForgeRegistries;
import net.eca.network.NetworkHandler;
import net.the_last_sword.damagesource.AbsoluteDestructionDamageSource;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.item.DragonCrystalSoulStone;
import net.the_last_sword.item.ISummonableItem;
import net.the_last_sword.entity.TheLastEndEntity;
import net.the_last_sword.entity.TheLastEndSwordWraithEntity;
import net.the_last_sword.util.EntityUtil;
import net.the_last_sword.util.TheLastSwordLogger;
import net.the_last_sword.util.nbt.ItemLevelHelper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//统一的剑灵召唤管理器（重构版）
public class WraithSummonManager {

    // ============ 魂石存储键 ============

    private static final String SOUL_STONE_KEY = "the_last_sword_soul_stone";

    // ============ 永久绑定表（运行时索引 + 持久化） ============

    //绑定表：玩家UUID → 剑灵UUID集合（永久绑定，只在firstSummon时添加）
    private static final Map<UUID, Set<UUID>> BINDINGS = new ConcurrentHashMap<>();

    //标记是否需要保存
    private static volatile boolean needsSave = false;

    //剑灵与主人的最大距离（传送阈值）
    private static final double MAX_OWNER_DISTANCE = 32.0;

    //跟随距离阈值
    private static final double FOLLOW_DISTANCE = 4.5;

    // ============ 玩家数据存取方法 ============

    //从玩家持久化数据读取魂石
    private static ItemStack getSoulStoneFromPlayer(Player player) {
        CompoundTag playerData = player.getPersistentData();
        if (playerData.contains(SOUL_STONE_KEY)) {
            return ItemStack.of(playerData.getCompound(SOUL_STONE_KEY));
        }
        return ItemStack.EMPTY;
    }

    //保存魂石到玩家持久化数据
    private static void saveSoulStoneToPlayer(Player player, ItemStack soulStone) {
        CompoundTag playerData = player.getPersistentData();
        if (soulStone.isEmpty()) {
            playerData.remove(SOUL_STONE_KEY);
        } else {
            CompoundTag soulStoneTag = new CompoundTag();
            soulStone.save(soulStoneTag);
            playerData.put(SOUL_STONE_KEY, soulStoneTag);
        }
    }

    // ============ 公共API方法 ============

    // ============ GUI数据存取方法 ============

    //GUI读取玩家的魂石数据
    public static ItemStack getPlayerSoulStone(Player player) {
        return getSoulStoneFromPlayer(player);
    }

    //GUI保存魂石数据到玩家
    public static void setPlayerSoulStone(Player player, ItemStack soulStone) {
        saveSoulStoneToPlayer(player, soulStone);
    }

    //玩家离线时自动唤回所有剑灵

    //统一的唤回API（全局搜索剑灵）
    public static boolean recallWraith(Player player, UUID wraitheUUID, ServerLevel level) {
        //1. 从玩家数据读取魂石
        ItemStack soulStone = getSoulStoneFromPlayer(player);
        CompoundTag nbt = soulStone.getTag();
        if (nbt == null) {
            return false;
        }

        //2. 全局搜索剑灵（遍历所有维度）
        LivingEntity wraith = null;
        for (ServerLevel serverLevel : level.getServer().getAllLevels()) {
            wraith = findEntityByUUID(serverLevel, wraitheUUID);
            if (wraith != null) {
                break;
            }
        }

        if (wraith == null) {
            //实体被外部强制删除，同步魂石状态（绑定保留）
            nbt.putBoolean("is_summoned", false);
            saveSoulStoneToPlayer(player, soulStone);
            return false;
        }

        //3. 禁用强加载并清除管理器记录（防御数据）
        if (wraith.level() instanceof ServerLevel sl) {
            EcaAPI.setForceLoading(wraith, sl, false);
        }
        EntityUtil.clearDefence(wraith);

        //3.5. 强制结束万物终焉状态
        if (wraith instanceof TheLastEndSwordWraithEntity swordWraith && swordWraith.isAllThingsEnd()) {
            TheLastEndSwordWraithEntity.AllThingsEndActiveEffect.end(swordWraith);
        }

        //4. 移除加成
        float healthBonus = nbt.getFloat("health_bonus");
        float attackBonus = nbt.getFloat("attack_bonus");
        removeBonusFromEntity(wraith, healthBonus, attackBonus);

        //5. 设置生命值为最大生命值（避免保存错误血量导致永远死亡）
        AttributeInstance maxHealthAttr = wraith.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            EntityUtil.theLastEndSetHealth(wraith, (float) maxHealthAttr.getValue());
        }

        //6. 保存当前状态NBT到魂石
        CompoundTag entityNBT = new CompoundTag();
        wraith.save(entityNBT);
        nbt.put("entity_nbt", entityNBT);
        nbt.putFloat("health_bonus", 0);
        nbt.putFloat("attack_bonus", 0);
        nbt.putBoolean("is_summoned", false);

        //7. 删除实体
        EntityUtil.theLastEndRemove(wraith, Entity.RemovalReason.DISCARDED);

        //7.5. 强制发送客户端删除包（确保客户端实体被正确清除）
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.sendToPlayer(new ClientRemovePacket(wraith.getId(), List.of()), serverPlayer);
        }

        //8. 保存魂石到玩家数据（绑定保留，只更新魂石状态）
        saveSoulStoneToPlayer(player, soulStone);

        return true;
    }

    //玩家登出时唤回所有剑灵
    public static void logoutRecall(Player player, ServerLevel level) {
        UUID playerUUID = player.getUUID();
        Set<UUID> wraithUUIDs = getWraithsByOwner(playerUUID);

        TheLastSwordLogger.info("[WraithSummon] Player {} logout triggered, found {} bound wraiths",
            player.getName().getString(), wraithUUIDs.size());

        if (wraithUUIDs.isEmpty()) {
            TheLastSwordLogger.info("[WraithSummon] No bound wraiths found for player {}", player.getName().getString());
            return;
        }

        //获取魂石中记录的剑灵UUID（用于判断是否需要保存NBT）
        ItemStack soulStone = getSoulStoneFromPlayer(player);
        CompoundTag soulStoneNbt = soulStone.getTag();
        UUID soulStoneWraithUUID = null;
        if (soulStoneNbt != null && soulStoneNbt.contains("wraith_uuid")) {
            try {
                soulStoneWraithUUID = UUID.fromString(soulStoneNbt.getString("wraith_uuid"));
            } catch (IllegalArgumentException e) {
                TheLastSwordLogger.warn("[WraithSummon] Invalid UUID format in soul stone");
            }
        }

        //遍历并唤回所有已召唤的剑灵
        for (UUID wraitheUUID : wraithUUIDs) {
            //全局搜索剑灵（遍历所有维度）
            LivingEntity wraith = null;
            for (ServerLevel serverLevel : level.getServer().getAllLevels()) {
                wraith = findEntityByUUID(serverLevel, wraitheUUID);
                if (wraith != null) {
                    break;
                }
            }

            if (wraith == null) {
                //剑灵不存在，跳过
                continue;
            }

            TheLastSwordLogger.info("[WraithSummon] Player {} logout, cleaning wraith {}",
                player.getName().getString(), wraitheUUID);

            //判断是否为魂石中的剑灵
            if (wraitheUUID.equals(soulStoneWraithUUID)) {
                //魂石中的剑灵：执行完整唤回（保存NBT）
                boolean success = recallWraith(player, wraitheUUID, level);
                if (success) {
                    TheLastSwordLogger.info("[WraithSummon] Soul stone wraith {} recalled successfully", wraitheUUID);
                } else {
                    TheLastSwordLogger.warn("[WraithSummon] Soul stone wraith {} recall failed", wraitheUUID);
                }
            } else {
                //非魂石剑灵：直接删除（不保存NBT）
                if (wraith.level() instanceof ServerLevel sl) {
                    EcaAPI.setForceLoading(wraith, sl, false);
                }
                EntityUtil.clearDefence(wraith);
                EntityUtil.theLastEndRemove(wraith, Entity.RemovalReason.DISCARDED);

                //发送客户端删除包
                if (player instanceof ServerPlayer serverPlayer) {
                    NetworkHandler.sendToPlayer(new ClientRemovePacket(wraith.getId(), List.of()), serverPlayer);
                }

                TheLastSwordLogger.info("[WraithSummon] Non-soul-stone wraith {} removed", wraitheUUID);
            }
        }
    }

    /**
     * 召唤API（统一入口）
     */
    public static boolean summonWraith(Player player, ItemStack weaponStack, Level level) {
        //从玩家持久化数据读取魂石
        ItemStack soulStone = getSoulStoneFromPlayer(player);
        if (soulStone.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.the_last_sword.no_soul_stone").withStyle(ChatFormatting.RED));
            return false;
        }

        CompoundTag nbt = soulStone.getOrCreateTag();

        //获取实体类型
        String entityId = null;

        //方法1：检查预设魂石的默认实体类型
        if (soulStone.getItem() instanceof DragonCrystalSoulStone soulStoneItem) {
            entityId = soulStoneItem.getDefaultBoundEntityId();
        }

        //方法2：从NBT读取实体类型
        if (entityId == null && nbt.contains("wraith_entity_id")) {
            entityId = nbt.getString("wraith_entity_id");
        }

        if (entityId == null) {
            player.sendSystemMessage(Component.translatable("message.the_last_sword.soul_stone_empty").withStyle(ChatFormatting.RED));
            return false;
        }

        //检查是否有UUID（判断是否已初始化）
        if (!nbt.contains("wraith_uuid")) {
            //首次召唤（初始化）
            return firstSummon(player, weaponStack, level, soulStone, entityId);
        } else {
            //有UUID，检查 is_summoned 字段
            boolean isSummoned = nbt.getBoolean("is_summoned");
            if (isSummoned) {
                //已召唤 → 唤回
                UUID wraitheUUID = UUID.fromString(nbt.getString("wraith_uuid"));

                //执行完整的唤回逻辑（仅服务端）
                boolean success = recallWraith(player, wraitheUUID, (ServerLevel) level);
                if (success) {
                    applyCooldown(player, weaponStack);
                    player.sendSystemMessage(Component.translatable("message.the_last_sword.recall_success").withStyle(ChatFormatting.GREEN));
                } else {
                    player.sendSystemMessage(Component.translatable("message.the_last_sword.recall_failed").withStyle(ChatFormatting.YELLOW));
                }
                return success;
            } else {
                //未召唤 → 再次召唤
                return reSummon(player, weaponStack, level, soulStone, entityId);
            }
        }
    }

    // ============ 首次召唤流程 ============

    private static boolean firstSummon(Player player, ItemStack weaponStack, Level level, ItemStack soulStone, String entityId) {
        CompoundTag nbt = soulStone.getOrCreateTag();

        //1. 创建实体（优先从捕获的NBT恢复）
        EntityType<?> entityType = getEntityTypeFromString(entityId);
        if (entityType == null) {
            player.sendSystemMessage(Component.translatable("message.the_last_sword.summon_failed").withStyle(ChatFormatting.RED));
            return false;
        }

        LivingEntity wraith;
        if (nbt != null && nbt.contains("entity_nbt")) {
            //从捕获的NBT恢复实体（保留装备、属性等）
            CompoundTag entityNBT = nbt.getCompound("entity_nbt");
            Entity entity = entityType.create(level);
            if (entity instanceof LivingEntity living) {
                living.load(entityNBT);  //加载NBT数据
                wraith = living;
            } else {
                player.sendSystemMessage(Component.translatable("message.the_last_sword.summon_failed").withStyle(ChatFormatting.RED));
                return false;
            }
        } else {
            //创建全新实体
            Entity entity = entityType.create(level);
            if (!(entity instanceof LivingEntity)) {
                player.sendSystemMessage(Component.translatable("message.the_last_sword.summon_failed").withStyle(ChatFormatting.RED));
                return false;
            }
            wraith = (LivingEntity) entity;
        }

        //2. 生成新UUID并设置
        UUID wraitheUUID = UUID.randomUUID();
        wraith.setUUID(wraitheUUID);

        //3. 设置位置
        Vec3 spawnPos = findSafeSpawnPosition(player);
        wraith.setPos(spawnPos.x, spawnPos.y, spawnPos.z);

        //4. 添加到世界
        boolean addSuccess = level.addFreshEntity(wraith);
        if (!addSuccess) {
            player.sendSystemMessage(Component.translatable("message.the_last_sword.summon_blocked").withStyle(ChatFormatting.RED));
            return false;
        }

        //4.5. 终焉种实体初始化
        if (wraith instanceof TheLastEndEntity theLastEnd) {
            float maxHealth = (float) wraith.getAttributeValue(Attributes.MAX_HEALTH);
            theLastEnd.setWorldAnchorMax(maxHealth);
            theLastEnd.setWorldAnchor(maxHealth);
            theLastEnd.setAnimationState(TheLastEndEntity.STATE_SPAWNING);
        }

        //5. 设置生命值为最大生命值（实体刚生成后立即设置满血）
        AttributeInstance maxHealthAttr = wraith.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            EntityUtil.theLastEndSetHealth(wraith, (float) maxHealthAttr.getValue());
        }

        //6. 清除Boss血条
        EntityUtil.cleanupBossBar(wraith);

        //7. 设置名字
        setWraithCustomName(wraith, player);

        //8. 设置主人（根据类型分支）
        setupOwnership(wraith, player);

        //9. 清除非可驯服实体的原生AI
        clearNativeAI(wraith);

        //10. 设置生命值为最大生命值（保存NBT前确保满血）
        AttributeInstance maxHealthAttrBeforeSave = wraith.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttrBeforeSave != null) {
            EntityUtil.theLastEndSetHealth(wraith, (float) maxHealthAttrBeforeSave.getValue());
        }

        //11. 保存基础NBT到魂石（此时没有加成污染）
        CompoundTag entityNBT = new CompoundTag();
        wraith.save(entityNBT);
        nbt.putString("wraith_entity_id", entityId);
        nbt.putString("wraith_uuid", wraitheUUID.toString());
        nbt.put("entity_nbt", entityNBT);
        nbt.putInt("weapon_level", ItemLevelHelper.getLevel(weaponStack));
        nbt.putBoolean("is_summoned", true);

        //12. 计算加成
        float healthBonus = calculateHealthBonus(weaponStack);
        float attackBonus = calculateAttackBonus(weaponStack);

        //13. 应用加成到实体
        applyBonusToEntity(wraith, healthBonus, attackBonus);

        //14. 记录加成数值到魂石
        nbt.putFloat("health_bonus", healthBonus);
        nbt.putFloat("attack_bonus", attackBonus);

        //15. 注册防御等级
        registerWraithDefense(wraith, weaponStack);

        //15.5. 启用强加载（防止剑灵因远离玩家导致区块卸载）
        if (level instanceof ServerLevel serverLevel) {
            EcaAPI.setForceLoading(wraith, serverLevel, true);
        }

        //16. 添加到永久绑定表（只在首次召唤时添加）
        addBinding(player.getUUID(), wraitheUUID);

        //17. 保存魂石到玩家数据
        saveSoulStoneToPlayer(player, soulStone);

        //18. 发送召唤消息
        sendSummonMessage(player, wraith);

        return true;
    }

    // ============ 再次召唤流程 ============

    private static boolean reSummon(Player player, ItemStack weaponStack, Level level, ItemStack soulStone, String entityId) {
        CompoundTag nbt = soulStone.getOrCreateTag();
        UUID wraitheUUID = UUID.fromString(nbt.getString("wraith_uuid"));
        CompoundTag entityNBT = nbt.getCompound("entity_nbt");

        //1. 从NBT加载实体
        Entity entity = EntityType.loadEntityRecursive(entityNBT, level, (e) -> e);
        if (!(entity instanceof LivingEntity)) {
            player.sendSystemMessage(Component.translatable("message.the_last_sword.summon_failed").withStyle(ChatFormatting.RED));
            return false;
        }
        LivingEntity wraith = (LivingEntity) entity;

        //1.5. 强制设置UUID为魂石中记录的UUID（避免UUID不匹配）
        wraith.setUUID(wraitheUUID);

        //2. 全局搜索是否有重复UUID的实体，如果有则清除
        for (ServerLevel serverLevel : ((ServerLevel) level).getServer().getAllLevels()) {
            LivingEntity existingWraith = findEntityByUUID(serverLevel, wraitheUUID);
            if (existingWraith != null) {
                //先清除保护状态，否则 Mixin 会阻止移除
                EntityUtil.clearDefence(existingWraith);
                EntityUtil.theLastEndRemove(existingWraith, Entity.RemovalReason.DISCARDED);

                //2.5. 强制发送客户端删除包（确保客户端旧实体被正确清除）
                if (player instanceof ServerPlayer serverPlayer) {
                    NetworkHandler.sendToPlayer(new ClientRemovePacket(existingWraith.getId(), List.of()), serverPlayer);
                }

                break;
            }
        }

        //3. 设置新位置
        Vec3 spawnPos = findSafeSpawnPosition(player);
        wraith.setPos(spawnPos.x, spawnPos.y, spawnPos.z);

        //4. 添加到世界
        boolean addSuccess = level.addFreshEntity(wraith);
        if (!addSuccess) {
            player.sendSystemMessage(Component.translatable("message.the_last_sword.summon_blocked").withStyle(ChatFormatting.RED));
            return false;
        }

        //4.5. 终焉种实体初始化
        if (wraith instanceof TheLastEndEntity theLastEnd) {
            float maxHealth = (float) wraith.getAttributeValue(Attributes.MAX_HEALTH);
            theLastEnd.setWorldAnchorMax(maxHealth);
            theLastEnd.setWorldAnchor(maxHealth);
            theLastEnd.setAnimationState(TheLastEndEntity.STATE_SPAWNING);
        }

        //5. 设置生命值为最大生命值（实体刚生成后立即设置满血）
        AttributeInstance maxHealthAttr = wraith.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            EntityUtil.theLastEndSetHealth(wraith, (float) maxHealthAttr.getValue());
        }

        //6. 清除Boss血条
        EntityUtil.cleanupBossBar(wraith);

        //7. 重新设置名称和主人
        setWraithCustomName(wraith, player);
        setupOwnership(wraith, player);

        //8. 清除非可驯服实体的原生AI
        clearNativeAI(wraith);

        //9. 计算并应用加成
        float healthBonus = calculateHealthBonus(weaponStack);
        float attackBonus = calculateAttackBonus(weaponStack);
        applyBonusToEntity(wraith, healthBonus, attackBonus);

        //10. 注册防御等级
        registerWraithDefense(wraith, weaponStack);

        //10.5. 启用强加载
        if (level instanceof ServerLevel serverLevel) {
            EcaAPI.setForceLoading(wraith, serverLevel, true);
        }

        //11. 更新魂石加成记录
        nbt.putFloat("health_bonus", healthBonus);
        nbt.putFloat("attack_bonus", attackBonus);
        nbt.putInt("weapon_level", ItemLevelHelper.getLevel(weaponStack));
        nbt.putBoolean("is_summoned", true);

        //12. 保存魂石到玩家数据（绑定已在firstSummon时添加，无需重复）
        saveSoulStoneToPlayer(player, soulStone);

        //13. 发送召唤消息
        sendSummonMessage(player, wraith);

        return true;
    }

    // ============ 永久绑定表管理 ============

    //添加绑定（只在firstSummon时调用）
    public static void addBinding(UUID playerUUID, UUID wraitheUUID) {
        BINDINGS.computeIfAbsent(playerUUID, k -> ConcurrentHashMap.newKeySet()).add(wraitheUUID);
        needsSave = true;
    }

    //移除绑定（预留API，暂不使用）
    public static void removeBinding(UUID playerUUID, UUID wraitheUUID) {
        Set<UUID> wraiths = BINDINGS.get(playerUUID);
        if (wraiths != null) {
            wraiths.remove(wraitheUUID);
            if (wraiths.isEmpty()) {
                BINDINGS.remove(playerUUID);
            }
            needsSave = true;
        }
    }

    //根据玩家查剑灵列表
    public static Set<UUID> getWraithsByOwner(UUID playerUUID) {
        return BINDINGS.getOrDefault(playerUUID, Collections.emptySet());
    }

    //根据剑灵查主人（遍历绑定表）
    public static UUID getOwnerByWraith(UUID wraitheUUID) {
        if (wraitheUUID == null) {
            return null;
        }
        for (Map.Entry<UUID, Set<UUID>> entry : BINDINGS.entrySet()) {
            if (entry.getValue().contains(wraitheUUID)) {
                return entry.getKey();
            }
        }
        return null;
    }

    //判断实体是否为剑灵
    public static boolean isWraith(UUID entityUUID) {
        if (entityUUID == null) {
            return false;
        }
        for (Set<UUID> wraiths : BINDINGS.values()) {
            if (wraiths.contains(entityUUID)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isWraith(LivingEntity entity) {
        return entity != null && isWraith(entity.getUUID());
    }

    //获取剑灵的主人UUID（兼容旧API）
    public static UUID getOwnerUUID(UUID wraitheUUID) {
        return getOwnerByWraith(wraitheUUID);
    }

    //获取剑灵的主人
    public static Player getOwner(LivingEntity wraith, Level level) {
        if (wraith == null || level == null) {
            return null;
        }

        UUID ownerUUID = getOwnerUUID(wraith.getUUID());
        if (ownerUUID == null) {
            return null;
        }

        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getServer().getPlayerList().getPlayer(ownerUUID);
        }
        return null;
    }

    // ============ 持久化接口 ============

    //导出绑定表（供SavedData使用）
    public static Map<UUID, Set<UUID>> exportBindings() {
        Map<UUID, Set<UUID>> result = new HashMap<>();
        for (Map.Entry<UUID, Set<UUID>> entry : BINDINGS.entrySet()) {
            result.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return result;
    }

    //导入绑定表（供SavedData使用）
    public static void importBindings(Map<UUID, Set<UUID>> bindings) {
        BINDINGS.clear();
        if (bindings != null) {
            for (Map.Entry<UUID, Set<UUID>> entry : bindings.entrySet()) {
                BINDINGS.put(entry.getKey(), ConcurrentHashMap.newKeySet());
                BINDINGS.get(entry.getKey()).addAll(entry.getValue());
            }
        }
    }

    //是否需要保存
    public static boolean needsSave() {
        return needsSave;
    }

    //标记已保存
    public static void markSaved() {
        needsSave = false;
    }

    // ============ 内部工具方法 ============

    //在世界中查找实体（根据UUID）
    private static LivingEntity findEntityByUUID(Level level, UUID entityUUID) {
        if (entityUUID == null) {
            return null;
        }

        if (level instanceof ServerLevel serverLevel) {
            //服务端：遍历所有实体
            for (Entity entity : serverLevel.getAllEntities()) {
                if (entity instanceof LivingEntity living && entity.getUUID().equals(entityUUID)) {
                    return living;
                }
            }
        } else if (level instanceof ClientLevel clientLevel) {
            //客户端：遍历渲染中的实体
            for (Entity entity : clientLevel.entitiesForRendering()) {
                if (entity instanceof LivingEntity living && entity.getUUID().equals(entityUUID)) {
                    return living;
                }
            }
        }

        return null;
    }

    //从字符串获取实体类型
    private static EntityType<?> getEntityTypeFromString(String entityTypeStr) {
        try {
            ResourceLocation rl = new ResourceLocation(entityTypeStr);
            return ForgeRegistries.ENTITY_TYPES.getValue(rl);
        } catch (Exception e) {
            return null;
        }
    }

    //寻找安全的生成位置
    private static Vec3 findSafeSpawnPosition(Player owner) {
        Vec3 ownerPos = owner.position();
        double angle = owner.level().random.nextDouble() * 2 * Math.PI;
        double distance = 2.0 + owner.level().random.nextDouble();
        double x = ownerPos.x + Math.cos(angle) * distance;
        double z = ownerPos.z + Math.sin(angle) * distance;
        return new Vec3(x, ownerPos.y, z);
    }

    //计算生命加成
    private static float calculateHealthBonus(ItemStack weaponStack) {
        int level = ItemLevelHelper.getLevel(weaponStack);
        if (level >= 6) {
            return level * (float) TheLastSwordConfiguration.getSwordWraithHealthPerHighLevelSafely();
        } else {
            return level * (float) TheLastSwordConfiguration.getSwordWraithHealthPerLevelSafely();
        }
    }

    //计算攻击加成
    private static float calculateAttackBonus(ItemStack weaponStack) {
        int level = ItemLevelHelper.getLevel(weaponStack);
        if (level >= 6) {
            return level * (float) TheLastSwordConfiguration.getSwordWraithAttackPerHighLevelSafely();
        } else {
            return level * (float) TheLastSwordConfiguration.getSwordWraithAttackPerLevelSafely();
        }
    }

    //应用加成到实体
    private static void applyBonusToEntity(LivingEntity entity, float healthBonus, float attackBonus) {
        if (healthBonus > 0) {
            AttributeInstance healthAttr = entity.getAttribute(Attributes.MAX_HEALTH);
            if (healthAttr != null) {
                double newMaxHealth = healthAttr.getBaseValue() + healthBonus;
                healthAttr.setBaseValue(newMaxHealth);

                //同步真实生命值（必须用 newMaxHealth，因为 getMaxHealth 会返回旧值）
                EntityUtil.setWorldAnchor(entity, (float) newMaxHealth);
                EntityUtil.setWorldAnchorMax(entity, (float) newMaxHealth);

                entity.setHealth((float) newMaxHealth);
            }
        }

        if (attackBonus > 0) {
            AttributeInstance attackAttr = entity.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attackAttr != null) {
                double newAttack = attackAttr.getBaseValue() + attackBonus;
                attackAttr.setBaseValue(newAttack);
            }
        }
    }

    //从实体移除加成
    private static void removeBonusFromEntity(LivingEntity entity, float healthBonus, float attackBonus) {
        if (healthBonus > 0) {
            AttributeInstance healthAttr = entity.getAttribute(Attributes.MAX_HEALTH);
            if (healthAttr != null) {
                double newMaxHealth = Math.max(1.0, healthAttr.getBaseValue() - healthBonus);
                healthAttr.setBaseValue(newMaxHealth);

                //同步真实生命值（必须用 newMaxHealth，因为 getMaxHealth 会返回旧值）
                EntityUtil.setWorldAnchor(entity, (float) newMaxHealth);
                EntityUtil.setWorldAnchorMax(entity, (float) newMaxHealth);

                entity.setHealth((float) newMaxHealth);
            }
        }

        if (attackBonus > 0) {
            AttributeInstance attackAttr = entity.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attackAttr != null) {
                double newAttack = Math.max(0.0, attackAttr.getBaseValue() - attackBonus);
                attackAttr.setBaseValue(newAttack);
            }
        }
    }

    //设置剑灵自定义名称
    private static void setWraithCustomName(LivingEntity wraith, Player owner) {
        try {
            Component originalName = wraith.getType().getDescription();

            Component newName = Component.translatable(
                "entity.the_last_sword.sword_wraith",
                owner.getDisplayName().getString(),
                originalName.getString()
            );

            wraith.setCustomName(newName);
            wraith.setCustomNameVisible(true);
        } catch (Exception e) {
            TheLastSwordLogger.error("Failed to set wraith custom name", e);
        }
    }

    //设置主人
    private static void setupOwnership(LivingEntity wraith, Player owner) {
        if (wraith instanceof TamableAnimal tamable) {
            tamable.setOwnerUUID(owner.getUUID());
            tamable.setTame(true);
        } else if (wraith instanceof AbstractHorse horse) {
            horse.setOwnerUUID(owner.getUUID());
            horse.setTamed(true);
        }
    }

    //清除非可驯服实体的原生AI
    private static void clearNativeAI(LivingEntity wraith) {
        if (wraith instanceof TamableAnimal) {
            return;
        }

        if (wraith instanceof Mob mob) {
            mob.targetSelector.getAvailableGoals().clear();
        }
    }

    //注册剑灵防御
    private static void registerWraithDefense(LivingEntity wraith, ItemStack weaponStack) {
        if (wraith == null || weaponStack == null) {
            return;
        }

        //注册防御追踪
        if (TheLastSwordConfiguration.getSwordWraithAsTheLastEndEntitySafely()) {
            EntityUtil.registerDefence(wraith, wraith.getMaxHealth());
        }
    }

    //发送召唤消息
    private static void sendSummonMessage(Player player, LivingEntity entity) {
        Component entityName = entity.hasCustomName() ? entity.getCustomName() : entity.getDisplayName();
        player.sendSystemMessage(Component.translatable("message.the_last_sword.summon_success", entityName));
    }

    //应用冷却（创造模式不添加冷却）
    private static void applyCooldown(Player player, ItemStack weaponStack) {
        if (player.isCreative()) {
            return;
        }

        //使用接口获取冷却时间
        if (weaponStack.getItem() instanceof ISummonableItem summonable) {
            int cooldownTicks = summonable.getSummonCooldownTicks();
            player.getCooldowns().addCooldown(weaponStack.getItem(), cooldownTicks);
        }
    }

    // ============ 事件委托方法（由ServerEventHandler调用） ============

    //实体离开世界：检测异常删除（被杀死等），只更新魂石状态
    public static void handleEntityLeaveLevel(Entity entity, Level level) {
        //跳过仍然存活的实体（区块卸载、reSummon清理旧实体等临时状态）
        if (entity instanceof LivingEntity living && living.getHealth() > 0) {
            return;
        }

        UUID wraitheUUID = entity.getUUID();

        //检查是否是剑灵（使用绑定表查询）
        UUID ownerUUID = getOwnerByWraith(wraitheUUID);
        if (ownerUUID == null) {
            return;
        }

        //更新玩家魂石状态（设置 is_summoned = false），绑定保留
        if (level instanceof ServerLevel serverLevel) {
            Player owner = serverLevel.getServer().getPlayerList().getPlayer(ownerUUID);
            if (owner != null) {
                ItemStack soulStone = getSoulStoneFromPlayer(owner);
                CompoundTag nbt = soulStone.getTag();
                if (nbt != null && nbt.contains("wraith_uuid")) {
                    String storedUUID = nbt.getString("wraith_uuid");
                    if (storedUUID.equals(wraitheUUID.toString())) {
                        nbt.putBoolean("is_summoned", false);
                        saveSoulStoneToPlayer(owner, soulStone);
                    }
                }
            }
        }
    }

    //实体刻更新：处理剑灵AI
    public static void handleLivingEntityTick(LivingEntity entity) {
        UUID wraitheUUID = entity.getUUID();

        //检查是否在绑定表中
        UUID ownerUUID = getOwnerByWraith(wraitheUUID);
        if (ownerUUID == null) {
            return;
        }

        //获取主人
        Player owner = entity.level().getServer().getPlayerList().getPlayer(ownerUUID);
        if (owner == null) {
            return;
        }

        //处理AI
        if (entity instanceof Mob mob) {
            handleWraithAI(mob, owner);
        }
    }

    //剑灵造成伤害：添加绝毁伤害
    public static void handleWraithDamage(LivingEntity target, LivingEntity attacker) {
        try {
            //检查攻击者是否为剑灵
            if (!isWraith(attacker)) {
                return;
            }

            //检查配置
            if (!TheLastSwordConfiguration.getSwordWraithAsTheLastEndEntitySafely() ||
                !TheLastSwordConfiguration.getSwordWraithAbsoluteDestructionDamageSafely()) {
                return;
            }

            //获取剑灵的武器等级（从魂石读取）
            UUID ownerUUID = getOwnerUUID(attacker.getUUID());
            if (ownerUUID == null) {
                return;
            }

            Player owner = attacker.level().getServer().getPlayerList().getPlayer(ownerUUID);
            if (owner == null) {
                return;
            }

            ItemStack soulStone = getSoulStoneFromPlayer(owner);
            CompoundTag nbt = soulStone.getTag();
            if (nbt == null) {
                return;
            }

            int weaponLevel = nbt.getInt("weapon_level");

            //根据武器等级计算绝毁伤害倍率
            double damageMultiplier = calculateAbsoluteDestructionMultiplier(weaponLevel);
            if (damageMultiplier <= 0) {
                return;
            }

            //获取剑灵的攻击力
            AttributeInstance attackAttr = attacker.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attackAttr == null) {
                return;
            }
            double attackDamage = attackAttr.getValue();

            //计算额外绝毁伤害
            float absoluteDestructionDamage = (float) (attackDamage * damageMultiplier);

            //应用额外绝毁伤害
            if (absoluteDestructionDamage > 0) {
                AbsoluteDestructionDamageSource.applyAbsoluteDestruction(target, attacker, absoluteDestructionDamage);
            }
        } catch (Throwable t) {
            try {
                TheLastSwordLogger.error("Wraith damage handler error", t);
            } catch (Throwable ignored) {
            }
        }
    }

    //计算绝毁伤害倍率
    private static double calculateAbsoluteDestructionMultiplier(int weaponLevel) {
        if (weaponLevel <= 5) {
            return TheLastSwordConfiguration.getSwordWraithAbsoluteDestructionMultiplierLowSafely();
        } else if (weaponLevel <= 12) {
            return TheLastSwordConfiguration.getSwordWraithAbsoluteDestructionMultiplierMidSafely();
        } else {
            return TheLastSwordConfiguration.getSwordWraithAbsoluteDestructionMultiplierHighSafely();
        }
    }

    // ============ 剑灵AI逻辑 ============

    //处理剑灵AI
    public static void handleWraithAI(Mob wraith, Player owner) {
        //所有剑灵类型都进行目标过滤
        filterInvalidTargets(wraith, owner);

        //可驯服实体：使用原生跟随AI
        if (wraith instanceof TamableAnimal) {
            return;
        }

        //非可驯服实体：自定义跟随AI
        handleNonTamableWraithAI(wraith, owner);
    }

    //目标过滤（所有剑灵类型都需要）
    private static void filterInvalidTargets(Mob wraith, Player owner) {
        //1. 优先攻击主人正在攻击的目标
        LivingEntity ownerTarget = owner.getLastHurtMob();
        if (ownerTarget != null && ownerTarget.isAlive() &&
            isValidTarget(ownerTarget, wraith, owner) &&
            !isSameOwnerWraith(ownerTarget, owner) &&
            EntityUtil.canAttack(wraith, ownerTarget)) {
            wraith.setTarget(ownerTarget);
            return;
        }

        //2. 检查当前目标是否有效
        LivingEntity currentTarget = wraith.getTarget();
        if (currentTarget != null &&
            (!isValidTarget(currentTarget, wraith, owner) ||
             isSameOwnerWraith(currentTarget, owner) ||
             !EntityUtil.canAttack(wraith, currentTarget))) {
            wraith.setTarget(null);
            currentTarget = null;
        }

        //3. 目标选择（仅在无目标时）
        if (currentTarget == null) {
            LivingEntity newTarget = selectTarget(wraith, owner);
            if (newTarget != null && !isSameOwnerWraith(newTarget, owner)) {
                wraith.setTarget(newTarget);
            }
        }
    }

    //非可驯服实体AI
    private static void handleNonTamableWraithAI(Mob wraith, Player owner) {
        //跟随和传送（仅在无目标时）
        if (wraith.getTarget() == null) {
            double distance = wraith.distanceTo(owner);

            if (distance > MAX_OWNER_DISTANCE) {
                teleportToOwner(wraith, owner);
            } else if (distance > FOLLOW_DISTANCE) {
                if (wraith.getNavigation().isDone() || wraith.tickCount % 20 == 0) {
                    wraith.getNavigation().moveTo(owner, 1.2);
                }
            } else {
                wraith.getNavigation().stop();
            }
        }
    }

    //目标选择
    private static LivingEntity selectTarget(Mob wraith, Player owner) {
        //优先级1：攻击主人的实体
        LivingEntity target = findAttackingOwnerTarget(wraith, owner);
        if (target != null) return target;

        //优先级2：主人正在攻击的目标
        target = owner.getLastHurtMob();
        if (target != null && target.isAlive() &&
            isValidTarget(target, wraith, owner) &&
            EntityUtil.canAttack(wraith, target)) {
            return target;
        }

        //优先级3：跟随范围内的敌对实体
        double followRange = wraith.getAttributeValue(Attributes.FOLLOW_RANGE);
        for (LivingEntity entity : wraith.level().getEntitiesOfClass(LivingEntity.class,
                wraith.getBoundingBox().inflate(followRange))) {
            if (entity.getType().getCategory() != MobCategory.MONSTER) {
                continue;
            }

            if (entity != wraith && entity.isAlive() &&
                isValidTarget(entity, wraith, owner) &&
                EntityUtil.canAttack(wraith, entity)) {
                return entity;
            }
        }

        return null;
    }

    //寻找攻击主人的目标
    private static LivingEntity findAttackingOwnerTarget(Mob wraith, Player owner) {
        for (LivingEntity entity : wraith.level().getEntitiesOfClass(LivingEntity.class,
                wraith.getBoundingBox().inflate(16.0))) {
            if (entity instanceof Mob mob && mob.getTarget() == owner &&
                isValidTarget(entity, wraith, owner) &&
                EntityUtil.canAttack(wraith, entity)) {
                return entity;
            }
        }
        return null;
    }

    //检查目标是否有效
    private static boolean isValidTarget(LivingEntity target, Mob wraith, Player owner) {
        if (target == null || !target.isAlive()) return false;
        if (target == owner) return false;
        if (target == wraith) return false;
        if (isWraith(target.getUUID())) return false;
        return EntityUtil.canAttack(owner, target);
    }

    //检查目标是否是同一个主人的剑灵
    private static boolean isSameOwnerWraith(LivingEntity target, Player owner) {
        if (!isWraith(target)) {
            return false;
        }
        UUID targetOwnerUUID = getOwnerUUID(target.getUUID());
        return targetOwnerUUID != null && targetOwnerUUID.equals(owner.getUUID());
    }

    //传送到主人身边
    private static void teleportToOwner(Mob wraith, Player owner) {
        Vec3 ownerPos = owner.position();
        Vec3 teleportPos = findSafeTeleportPosition(wraith.level(), ownerPos);
        if (teleportPos != null) {
            wraith.teleportTo(teleportPos.x, teleportPos.y, teleportPos.z);
        } else {
            wraith.teleportTo(ownerPos.x, ownerPos.y, ownerPos.z);
        }
    }

    //寻找安全的传送位置
    private static Vec3 findSafeTeleportPosition(Level level, Vec3 centerPos) {
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4;
            double x = centerPos.x + Math.cos(angle) * 2;
            double z = centerPos.z + Math.sin(angle) * 2;
            double y = centerPos.y;

            if (level.getBlockState(new BlockPos((int)x, (int)y, (int)z)).isAir() &&
                level.getBlockState(new BlockPos((int)x, (int)y+1, (int)z)).isAir()) {
                return new Vec3(x, y, z);
            }
        }
        return null;
    }
}
