package net.the_last_sword.util;

import net.eca.api.EcaAPI;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Team;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.sounds.SoundEvents;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.init.ModAttributes;
import net.the_last_sword.item.DragonArmorItem;
import net.the_last_sword.item.TheLastSword;
import net.the_last_sword.item.TheLastSwordYouNeverForgot;
import net.the_last_sword.summon.WraithSummonManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EntityUtil {

    private static final String NBT_WORLD_ANCHOR = "tlsWorldAnchor";
    private static final String NBT_WORLD_ANCHOR_MAX = "tlsWorldAnchorMax";
    private static final String NBT_HEAL_BAN_TIME = "tlsHealBanTime";
    private static final String NBT_IS_PROTECTED = "tlsIsProtected";

    // ==================== 实体数据定义（由 LivingEntityMixin 的 <clinit> 注入初始化） ====================

    //真实生命值——现世锚度（编码：anchor - 1024）
    public static EntityDataAccessor<String> WORLD_ANCHOR;

    //现世锚度上限（编码：anchorMax - 2048）
    public static EntityDataAccessor<String> WORLD_ANCHOR_MAX;

    //禁疗时间（tick）
    public static EntityDataAccessor<Integer> HEAL_BAN_TIME;

    //防御保护状态
    public static EntityDataAccessor<Boolean> IS_PROTECTED;

    // ==================== 实体数据 API ====================

    //获取真实血量
    public static float getWorldAnchor(LivingEntity entity) {
        if (entity == null) return 0.0f;
        String encoded = null;
        if (WORLD_ANCHOR != null) {
            try {
                encoded = entity.getEntityData().get(WORLD_ANCHOR);
            } catch (Exception ignored) {
            }
        }
        if (encoded == null || encoded.isEmpty()) {
            encoded = entity.getPersistentData().getString(NBT_WORLD_ANCHOR);
        }
        try {
            return Float.parseFloat(encoded) + 1024.0f;
        } catch (NumberFormatException e) {
            return 0.0f;
        }
    }

    //设置真实血量
    public static void setWorldAnchor(LivingEntity entity, float anchor) {
        if (entity == null) return;
        String encoded = Float.toString(anchor - 1024.0f);
        if (WORLD_ANCHOR != null) {
            try {
                entity.getEntityData().set(WORLD_ANCHOR, encoded);
                syncHealthLock(entity, anchor);
                return;
            } catch (Exception ignored) {
            }
        }
        entity.getPersistentData().putString(NBT_WORLD_ANCHOR, encoded);
        syncHealthLock(entity, anchor);
    }

    //保护系统已通过Mixin完整控制血量流程，无需ECA锁血
    private static void syncHealthLock(LivingEntity entity, float health) {
    }

    //获取最大真实血量上限
    public static float getWorldAnchorMax(LivingEntity entity) {
        if (entity == null) return 0.0f;
        String encoded = null;
        if (WORLD_ANCHOR_MAX != null) {
            try {
                encoded = entity.getEntityData().get(WORLD_ANCHOR_MAX);
            } catch (Exception ignored) {
            }
        }
        if (encoded == null || encoded.isEmpty()) {
            encoded = entity.getPersistentData().getString(NBT_WORLD_ANCHOR_MAX);
        }
        try {
            return Float.parseFloat(encoded) + 2048.0f;
        } catch (NumberFormatException e) {
            return 0.0f;
        }
    }

    //设置最大真实血量上限
    public static void setWorldAnchorMax(LivingEntity entity, float maxAnchor) {
        if (entity == null) return;
        String encoded = Float.toString(maxAnchor - 2048.0f);
        if (WORLD_ANCHOR_MAX != null) {
            try {
                entity.getEntityData().set(WORLD_ANCHOR_MAX, encoded);
                return;
            } catch (Exception ignored) {
            }
        }
        entity.getPersistentData().putString(NBT_WORLD_ANCHOR_MAX, encoded);
    }

    //获取禁疗时间（tick）
    public static int getHealBanTime(LivingEntity entity) {
        if (entity == null) return 0;
        if (HEAL_BAN_TIME != null) {
            try {
                return entity.getEntityData().get(HEAL_BAN_TIME);
            } catch (Exception ignored) {
            }
        }
        return entity.getPersistentData().getInt(NBT_HEAL_BAN_TIME);
    }

    //设置禁疗时间（tick）
    public static void setHealBanTime(LivingEntity entity, int ticks) {
        if (entity == null) return;
        int safeTicks = Math.max(0, ticks);
        if (HEAL_BAN_TIME != null) {
            try {
                entity.getEntityData().set(HEAL_BAN_TIME, safeTicks);
                return;
            } catch (Exception ignored) {
            }
        }
        entity.getPersistentData().putInt(NBT_HEAL_BAN_TIME, safeTicks);
    }

    //检查是否处于禁疗状态
    public static boolean isHealBanned(LivingEntity entity) {
        return getHealBanTime(entity) > 0;
    }

    //减少禁疗时间（每 tick 调用）
    public static void tickHealBanTime(LivingEntity entity) {
        if (entity == null) return;
        if (entity.tickCount % 20 != 0) {
            return;
        }
        int current = getHealBanTime(entity);
        if (current > 0) {
            int newTime = current - 1;
            setHealBanTime(entity, newTime);
            //禁疗结束时，解除 ECA 禁疗
            if (newTime == 0) {
                EcaAPI.unbanHealing(entity);
            }
        }
    }

    //清除禁疗状态
    public static void clearHealBan(LivingEntity entity) {
        setHealBanTime(entity, 0);
        EcaAPI.unbanHealing(entity);
    }

    //清除真实血量
    public static void clearWorldAnchor(LivingEntity entity) {
        if (entity == null) return;
        setWorldAnchor(entity, 0.0f);
        setWorldAnchorMax(entity, 0.0f);
    }

    // ==================== 防御保护 API ====================

    //检查实体是否受到保护
    public static boolean hasProtection(LivingEntity entity) {
        if (entity == null) return false;
        if (IS_PROTECTED != null) {
            try {
                return entity.getEntityData().get(IS_PROTECTED);
            } catch (Exception ignored) {
            }
        }
        return entity.getPersistentData().getBoolean(NBT_IS_PROTECTED);
    }

    //检查玩家背包中是否拥有最终之剑、隐藏最终之剑或龙之套装
    public static boolean hasInventoryProtection(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && isProtectionItem(stack)) {
                return true;
            }
        }
        for (ItemStack stack : player.getInventory().armor) {
            if (!stack.isEmpty() && isProtectionItem(stack)) {
                return true;
            }
        }
        if (!player.getInventory().offhand.isEmpty() && isProtectionItem(player.getInventory().offhand.get(0))) {
            return true;
        }
        return false;
    }

    private static boolean isProtectionItem(ItemStack stack) {
        return stack.getItem() instanceof TheLastSword
                || stack.getItem() instanceof DragonArmorItem
                || stack.getItem() instanceof TheLastSwordYouNeverForgot;
    }

    //设置实体的保护状态
    public static void setProtection(LivingEntity entity, boolean value) {
        if (entity == null) return;
        if (IS_PROTECTED != null) {
            try {
                entity.getEntityData().set(IS_PROTECTED, value);
                return;
            } catch (Exception ignored) {
            }
        }
        entity.getPersistentData().putBoolean(NBT_IS_PROTECTED, value);
    }

    //注册防御（如果没有保护才设置）
    public static void registerDefence(LivingEntity entity, float maxHealth) {
        if (entity == null) return;

        //已有保护，不重复注册
        if (hasProtection(entity)) {
            return;
        }

        //设置保护标志（TLS 主系统）
        setProtection(entity, true);

        //设置现世锚度（setWorldAnchor内部会自动同步ECA锁血）
        setWorldAnchor(entity, maxHealth);
        setWorldAnchorMax(entity, maxHealth);
    }

    //清除防御数据
    public static void clearDefence(LivingEntity entity) {
        if (entity == null) return;

        //清除保护标志（TLS 主系统）
        setProtection(entity, false);

        //清除现世锚度
        clearWorldAnchor(entity);
    }

    //强化免疫：清除火焰、冰冻和非增益效果（不清除生命吸收）
    public static void applyImmunity(LivingEntity entity) {
        if (entity == null) return;

        if (entity.isOnFire()) {
            entity.clearFire();
        }

        if (entity.isFullyFrozen() || entity.getTicksFrozen() > 0) {
            entity.setTicksFrozen(0);
        }

        java.util.Collection<net.minecraft.world.effect.MobEffectInstance> activeEffects = entity.getActiveEffects();
        if (!activeEffects.isEmpty()) {
            java.util.ArrayList<net.minecraft.world.effect.MobEffect> effectsToRemove = new java.util.ArrayList<>();
            for (net.minecraft.world.effect.MobEffectInstance effectInstance : activeEffects) {
                net.minecraft.world.effect.MobEffect effect = effectInstance.getEffect();
                if (!effect.isBeneficial() && effect != net.minecraft.world.effect.MobEffects.ABSORPTION) {
                    effectsToRemove.add(effect);
                }
            }
            for (net.minecraft.world.effect.MobEffect effect : effectsToRemove) {
                entity.removeEffect(effect);
            }
        }
    }

    // ==================== 生命值模块 ====================

    //获取实体真实生命值
    public static float TheLastEndGetHealth(LivingEntity entity) {
        return EcaAPI.getHealth(entity);
    }

    public static boolean theLastEndSetHealth(LivingEntity entity, float expectedHealth) {
        if (entity == null) return false;

        try {
            // 同步更新你们的实体数据系统
            setWorldAnchor(entity, expectedHealth);
            return EcaAPI.setHealth(entity, expectedHealth);

        } catch (Exception e) {
            return false;
        }
    }

    // ==================== 死亡模块 ====================

    //设置实体死亡状态
    public static void theLastEndSetDead(LivingEntity entity, DamageSource damageSource) {
        if (entity == null || damageSource == null) return;

        //添加实体类型到 ECA 禁生成表（玩家除外）
        if (!(entity instanceof Player) && entity.level() instanceof ServerLevel serverLevel) {
            int banTime = TheLastSwordConfiguration.getReviveBanTimeSafely();
            if (banTime > 0) {
                EcaAPI.banSpawn(serverLevel, entity.getType(), banTime);
            }
        }

        //粒子效果
        if (!entity.level().isClientSide && TheLastSwordConfiguration.getDeathParticleEffectSafely()) {
            ParticleUtil.spawnDeathParticles(entity);
        }

        //死亡消息
        if (TheLastSwordConfiguration.getDieMessageSafely()) {
            sendDeathMessage(entity, damageSource);
        }
        //清除防御系统，防止 die/tickDeath 被保护拦截
        clearDefence(entity);
        var shieldAttr = entity.getAttribute(ModAttributes.JUSTIFIED_DEFENCE.get());
        if (shieldAttr != null) {
            shieldAttr.setBaseValue(0.0);
        }
        var maxShieldAttr = entity.getAttribute(ModAttributes.MAX_JUSTIFIED_DEFENCE.get());
        if (maxShieldAttr != null) {
            maxShieldAttr.setBaseValue(0.0);
        }

        EcaAPI.kill(entity, damageSource);
    }

    public static void theLastEndRevive(LivingEntity entity) {
        EcaAPI.revive(entity);
    }

    //发送死亡消息
    private static void sendDeathMessage(LivingEntity entity, DamageSource damageSource) {
        try {
            if (!entity.level().isClientSide && entity.level().getServer() != null) {
                Level level = entity.level();
                Component deathMessage;

                ItemStack weapon = ItemStack.EMPTY;
                if (damageSource.getEntity() instanceof LivingEntity attacker) {
                    weapon = attacker.getMainHandItem();
                }

                if (!weapon.isEmpty()) {
                    deathMessage = Component.translatable(
                            "death.attack.absolute_destruction.item",
                            entity.getDisplayName(),
                            damageSource.getEntity() != null ? damageSource.getEntity().getDisplayName() : Component.literal("Unknown"),
                            weapon.getDisplayName()
                    );
                } else if (damageSource.getDirectEntity() instanceof Player) {
                    deathMessage = Component.translatable(
                            "death.attack.absolute_destruction.player",
                            entity.getDisplayName(),
                            damageSource.getDirectEntity().getDisplayName()
                    );
                } else if (damageSource.getEntity() != null) {
                    deathMessage = Component.translatable(
                            "death.attack.absolute_destruction.player",
                            entity.getDisplayName(),
                            damageSource.getEntity().getDisplayName()
                    );
                } else {
                    deathMessage = Component.translatable(
                            "death.attack.absolute_destruction",
                            entity.getDisplayName()
                    );
                }

                if (level.getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES)) {
                    PlayerList playerList = level.getServer().getPlayerList();
                    playerList.broadcastSystemMessage(deathMessage, false);
                }
            }
        } catch (Exception ignored) {
        }
    }

    // ==================== 传送模块 ====================

    public static boolean theLastEndTeleport(Entity entity, double x, double y, double z) {
        return EcaAPI.teleport(entity, x, y, z);
    }

    // ==================== 实体清除模块 ====================

    public static void theLastEndRemove(Entity entity, Entity.RemovalReason reason) {
        EcaAPI.remove(entity, reason);
    }

    public static void cleanupBossBar(Entity entity) {
        EcaAPI.cleanupBossBar(entity);
    }

    // ==================== 实体目标与队伍模块====================
    //综合判断是否可以攻击目标
    public static boolean canAttack(Entity attacker, Entity target) {
        if (target == null) {
            return false;
        }
        //原版队伍判断：同一队伍或友军队伍不攻击
        if (attacker != null) {
            Team attackerTeam = attacker.getTeam();
            Team targetTeam = target.getTeam();
            if (attackerTeam != null && targetTeam != null && attackerTeam.isAlliedTo(targetTeam)) {
                return false;
            }
        }

        //创造模式和旁观模式玩家豁免
        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return false;
        }

        if (areOriginalAllies(attacker, target)) return false;
        if (areWraithAllies(attacker, target)) return false;
        return true;
    }
    //判断两个实体是否是原版盟友关系（玩家组队、宠物关系）
    public static boolean areOriginalAllies(Entity attacker, Entity target) {
        if (attacker == null || target == null) return false;

        //同一实体
        if (attacker.getUUID().equals(target.getUUID())) return true;

        //情况1：玩家 vs 玩家
        if (attacker instanceof Player pa && target instanceof Player pt) {
            if (pa.getTeam() != null && pa.getTeam() == pt.getTeam()) {
                return true;
            }
            return false;
        }

        //情况2：玩家 vs 宠物
        if (attacker instanceof Player pa && target instanceof TamableAnimal tt) {
            if (tt.isOwnedBy(pa)) return true;

            LivingEntity owner = tt.getOwner();
            if (owner instanceof Player ownerPlayer) {
                if (pa.getTeam() != null && pa.getTeam() == ownerPlayer.getTeam()) {
                    return true;
                }
            }
            return false;
        }

        //情况3：宠物 vs 玩家
        if (attacker instanceof TamableAnimal ta && target instanceof Player pt) {
            if (ta.isOwnedBy(pt)) return true;

            LivingEntity owner = ta.getOwner();
            if (owner instanceof Player ownerPlayer) {
                if (ownerPlayer.getTeam() != null && ownerPlayer.getTeam() == pt.getTeam()) {
                    return true;
                }
            }
            return false;
        }

        //情况4：宠物 vs 宠物
        if (attacker instanceof TamableAnimal ta && target instanceof TamableAnimal tt) {
            LivingEntity aOwner = ta.getOwner();
            LivingEntity tOwner = tt.getOwner();

            if (aOwner != null && tOwner != null && aOwner.getUUID().equals(tOwner.getUUID())) {
                return true;
            }

            if (aOwner instanceof Player aOwnerPlayer && tOwner instanceof Player tOwnerPlayer) {
                if (aOwnerPlayer.getTeam() != null && aOwnerPlayer.getTeam() == tOwnerPlayer.getTeam()) {
                    return true;
                }
            }
            return false;
        }

        //情况5：任何实体 vs 剑灵
        if (target instanceof LivingEntity && WraithSummonManager.isWraith((LivingEntity) target)) {
            Player wraithOwner = WraithSummonManager.getOwner((LivingEntity) target, target.level());
            if (wraithOwner != null) {
                if (areOriginalAllies(attacker, wraithOwner)) {
                    return true;
                }
            }
        }

        return false;
    }

    //判断剑灵相关的盟友关系
    public static boolean areWraithAllies(Entity attacker, Entity target) {
        if (attacker == null || target == null) return false;

        if (!(attacker instanceof LivingEntity) || !WraithSummonManager.isWraith((LivingEntity) attacker)) {
            return false;
        }

        Player owner = WraithSummonManager.getOwner((LivingEntity) attacker, attacker.level());
        if (owner == null) return false;

        //不攻击主人
        if (target == owner) return true;

        //不攻击主人的队友
        if (target instanceof Player targetPlayer) {
            if (owner.getTeam() != null && owner.getTeam() == targetPlayer.getTeam()) {
                return true;
            }
        }

        //不攻击宠物
        if (target instanceof TamableAnimal tamable && tamable.isTame()) {
            LivingEntity tamableOwner = tamable.getOwner();
            if (tamableOwner != null) {
                if (tamableOwner == owner) return true;

                if (tamableOwner instanceof Player tamableOwnerPlayer) {
                    if (owner.getTeam() != null && owner.getTeam() == tamableOwnerPlayer.getTeam()) {
                        return true;
                    }
                }
            }
        }

        //不攻击剑灵
        if (target instanceof LivingEntity && WraithSummonManager.isWraith((LivingEntity) target)) {
            Player targetOwner = WraithSummonManager.getOwner((LivingEntity) target, target.level());
            if (targetOwner != null) {
                if (targetOwner == owner) return true;

                if (owner.getTeam() != null && owner.getTeam() == targetOwner.getTeam()) {
                    return true;
                }
            }
        }

        return false;
    }

    //查找实体的主人
    public static LivingEntity findOwner(Entity entity) {
        if (entity instanceof TamableAnimal tamable && tamable.isTame()) {
            return tamable.getOwner();
        }

        if (entity instanceof LivingEntity living && WraithSummonManager.isWraith(living)) {
            return WraithSummonManager.getOwner(living, entity.level());
        }

        return null;
    }



    //获取前方半圆范围内的有效攻击目标
    public static List<LivingEntity> getTargetsInHemisphere(LivingEntity attacker, double radius) {
        List<LivingEntity> validTargets = new ArrayList<>();

        AABB searchBox = new AABB(
                attacker.getX() - radius,
                attacker.getY() - radius,
                attacker.getZ() - radius,
                attacker.getX() + radius,
                attacker.getY() + radius,
                attacker.getZ() + radius
        );

        List<Entity> nearbyEntities = attacker.level().getEntities(attacker, searchBox);
        Vec3 lookVec = attacker.getLookAngle();

        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity livingEntity) {
                Vec3 toTarget = new Vec3(
                        livingEntity.getX() - attacker.getX(),
                        0,
                        livingEntity.getZ() - attacker.getZ()
                ).normalize();

                double dotProduct = lookVec.x * toTarget.x + lookVec.z * toTarget.z;

                if (dotProduct > 0 && canAttack(attacker, livingEntity)) {
                    validTargets.add(livingEntity);
                }
            }
        }

        return validTargets;
    }

    //让实体面向目标
    public static void faceTarget(LivingEntity entity, LivingEntity target) {
        if (entity == null || target == null || !target.isAlive()) return;

        double deltaX = target.getX() - entity.getX();
        double deltaZ = target.getZ() - entity.getZ();
        double deltaY = target.getY() + target.getBbHeight() * 0.5 - (entity.getY() + entity.getBbHeight() * 0.5);

        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float yaw = (float) (Math.atan2(-deltaX, deltaZ) * 180.0 / Math.PI);
        float pitch = (float) (Math.atan2(-deltaY, horizontalDistance) * 180.0 / Math.PI);

        entity.setYRot(yaw);
        entity.setXRot(pitch);
        entity.yHeadRot = yaw;
        entity.yBodyRot = yaw;
    }

    //获取球体范围内的有效攻击目标
    public static List<LivingEntity> getTargetsInSphere(LivingEntity attacker, double radius) {
        List<LivingEntity> validTargets = new ArrayList<>();

        AABB searchBox = new AABB(
                attacker.getX() - radius,
                attacker.getY() - radius,
                attacker.getZ() - radius,
                attacker.getX() + radius,
                attacker.getY() + radius,
                attacker.getZ() + radius
        );

        List<Entity> nearbyEntities = attacker.level().getEntities(attacker, searchBox);
        double radiusSquared = radius * radius;

        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity livingEntity) {
                double dx = livingEntity.getX() - attacker.getX();
                double dy = livingEntity.getY() - attacker.getY();
                double dz = livingEntity.getZ() - attacker.getZ();
                double distanceSquared = dx * dx + dy * dy + dz * dz;

                if (distanceSquared <= radiusSquared && canAttack(attacker, livingEntity)) {
                    validTargets.add(livingEntity);
                }
            }
        }

        return validTargets;
    }

    //发射附魔弓箭
    public static void shootArrow(LivingEntity shooter, LivingEntity target, ItemStack bow) {
        if (shooter == null || target == null || bow.isEmpty()) return;
        if (!(bow.getItem() instanceof BowItem)) return;

        //创建箭矢物品（如果弓有无限附魔，可以用空箭矢）
        ItemStack arrowStack = new ItemStack(Items.ARROW);

        //创建箭矢实体（power=1.0 表示满蓄力）
        AbstractArrow arrow = ProjectileUtil.getMobArrow(shooter, arrowStack, 1.0f);

        //应用弓的特殊效果（火矢等）
        arrow = ((BowItem)bow.getItem()).customArrow(arrow);

        //显式设置箭矢的发射者，防止误伤友军（必须在customArrow之后）
        arrow.setOwner(shooter);

        //读取弓的附魔并应用到箭矢
        applyBowEnchantments(bow, arrow);

        //计算目标位置和弹道
        double dx = target.getX() - shooter.getX();
        double dy = target.getY(0.3333333333333333) - arrow.getY();
        double dz = target.getZ() - shooter.getZ();
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        //发射箭矢（速度1.6，精度14）
        arrow.shoot(dx, dy + horizontalDistance * 0.2, dz, 1.6f, 14.0f);

        //播放射击音效
        shooter.playSound(SoundEvents.SKELETON_SHOOT, 1.0f, 1.0f / (shooter.getRandom().nextFloat() * 0.4f + 0.8f));

        //将箭矢添加到世界
        shooter.level().addFreshEntity(arrow);
    }

    //应用弓的附魔到箭矢
    private static void applyBowEnchantments(ItemStack bow, AbstractArrow arrow) {
        //力量附魔：增加箭矢伤害
        int powerLevel = bow.getEnchantmentLevel(Enchantments.POWER_ARROWS);
        if (powerLevel > 0) {
            arrow.setBaseDamage(arrow.getBaseDamage() + (double)powerLevel * 0.5 + 0.5);
        }

        //冲击附魔：增加击退
        int punchLevel = bow.getEnchantmentLevel(Enchantments.PUNCH_ARROWS);
        if (punchLevel > 0) {
            arrow.setKnockback(punchLevel);
        }

        //火矢附魔：点燃箭矢
        if (bow.getEnchantmentLevel(Enchantments.FLAMING_ARROWS) > 0) {
            arrow.setSecondsOnFire(100);
        }
    }

}
