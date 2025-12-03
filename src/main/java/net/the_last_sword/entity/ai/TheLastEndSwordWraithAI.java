package net.the_last_sword.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.attack.AbsoluteDestructionDamageSource;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.TheLastEndSwordWraithEntity;
import net.the_last_sword.init.ModEffects;
import net.the_last_sword.util.EntityUtil;
import net.the_last_sword.util.ParticleUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class TheLastEndSwordWraithAI {

    // 目标追踪数据映射
    private static final Map<UUID, TargetTrackingData> TARGET_TRACKING = new ConcurrentHashMap<>();

    // 强制十字切标记映射
    private static final Map<UUID, Boolean> FORCE_CROSS_SLASH = new ConcurrentHashMap<>();

    // 战斗距离常量
    private static final double MAX_SEARCH_DISTANCE = 32.0;
    private static final double APPROACH_DISTANCE = 6.0;  // 靠近状态阈值
    private static final double DASH_SUCCESS_DISTANCE = 4.0;  // 冲刺成功阈值
    
    // 目标追踪常量
    private static final int MAX_STUCK_TICKS = 200;        // 10秒卡住后放弃
    private static final int MAX_NO_PROGRESS_TICKS = 300;  // 15秒无进展后放弃
    private static final double MIN_PROGRESS_DISTANCE = 0.5; // 最小进展距离

    private TheLastEndSwordWraithAI() {}
    
    // ==================== 目标追踪数据类 ====================
    
    private static class TargetTrackingData {
        final UUID targetId;
        int stuckTicks = 0;           // 卡住计时
        double lastDistance = -1;     // 上次距离
        int noProgressTicks = 0;      // 无进展计时
        Vec3 lastPosition;            // 终焉剑灵上次位置
        boolean hasLineOfSight = true; // 是否有视线
        
        TargetTrackingData(UUID targetId) {
            this.targetId = targetId;
        }
        
        boolean shouldAbandon() {
            return stuckTicks > MAX_STUCK_TICKS || noProgressTicks > MAX_NO_PROGRESS_TICKS;
        }
        
        void updateProgress(TheLastEndSwordWraithEntity wraith, LivingEntity target) {
            Vec3 currentPos = wraith.position();
            double currentDistance = wraith.distanceTo(target);
            
            // 检查是否卡住（位置基本不变）
            if (lastPosition != null) {
                double moveDistance = currentPos.distanceTo(lastPosition);
                if (moveDistance < 0.1) {
                    stuckTicks++;
                } else {
                    stuckTicks = 0;
                }
            }
            
            // 检查是否有进展（距离是否缩短）
            if (lastDistance > 0) {
                double progress = lastDistance - currentDistance;
                if (progress < MIN_PROGRESS_DISTANCE) {
                    noProgressTicks++;
                } else {
                    noProgressTicks = 0;
                }
            }
            
            // 更新视线状态
            hasLineOfSight = wraith.hasLineOfSight(target);
            
            // 更新追踪数据
            lastPosition = currentPos;
            lastDistance = currentDistance;
        }
    }

    /**
     * 获取战斗距离（使用实体的攻击距离属性）
     */
    private static double getCombatDistance(TheLastEndSwordWraithEntity wraith) {
        return wraith.getAttackRange();
    }

    public static void initialize(TheLastEndSwordWraithEntity wraith) {
        // 清除可能存在的旧状态
        FORCE_CROSS_SLASH.remove(wraith.getUUID());

        // 清除目标追踪数据
        TARGET_TRACKING.entrySet().removeIf(entry -> {
            TargetTrackingData data = entry.getValue();
            return data.lastPosition == null; // 清除未初始化的追踪数据
        });

        // 确保AI状态正确，但不干扰生成动画
        // 如果当前不是spawn动画，才重置为empty
        String currentAnimation = wraith.getSyncedAnimation();
        if (!currentAnimation.equals("spawn")) {
            wraith.setSkillTick(0);
            wraith.setAnimation("empty");
        }
        wraith.setAllowMoving(true); // 初始化时允许移动
    }

    public static void handleTick(TheLastEndSwordWraithEntity wraith) {
        if (wraith.level().isClientSide) return;

        // 步骤0：处理万物终焉持续效果（独立于其他状态）
        AllThingsEndActiveEffect.handleTick(wraith);

        // 步骤1：目标管理（扫描和更新目标列表）
        updateTargetList(wraith);
        updateCurrentTarget(wraith);

        // 步骤2：只有生成完成后才执行 AI
        if (!wraith.getIsSpawned()) {
            return;
        }

        // 步骤3：根据是否有敌人，选择 AI 分支
        LivingEntity target = wraith.getTarget();

        if (target == null || !target.isAlive()) {
            // ========== 基础 AI 分支 ==========
            executeBaseAI(wraith);
        } else {
            // ========== 战斗 AI 分支 ==========
            executeCombatAI(wraith);
        }
    }

    // ==================== AI 分支系统 ====================

    /**
     * 基础 AI 分支：无敌人时执行
     */
    private static void executeBaseAI(TheLastEndSwordWraithEntity wraith) {
        // 子 AI 1：跟随主人
        followOwner(wraith);

        // 子 AI 2：扫描敌人（已在 handleTick 的步骤1完成）
        // 如果扫描到敌人，下一个 tick 会进入战斗 AI
    }

    /**
     * 战斗 AI 分支：有敌人时执行
     */
    private static void executeCombatAI(TheLastEndSwordWraithEntity wraith) {
        // 检查战斗传送距离
        if (checkCombatTeleportDistance(wraith)) {
            return; // 传送后结束本 tick
        }

        // 技能执行处理
        if (isExecutingSkill(wraith)) {
            updateSkillTick(wraith);
            executeCurrentSkill(wraith);
            return;
        }

        // 导航和移动管理
        handleNavigationAndMovement(wraith);

        // 技能选择和启动
        tryStartNewSkill(wraith);
    }

    /**
     * 跟随主人逻辑（基础 AI 子系统）
     */
    private static void followOwner(TheLastEndSwordWraithEntity wraith) {
        LivingEntity owner = wraith.getOwner();
        if (owner == null || !owner.isAlive()) {
            return;
        }

        double distance = wraith.distanceTo(owner);

        // 超过 16 格立刻传送
        if (distance > 16.0) {
            forceTeleportToOwner(wraith, owner);
            return;
        }

        // 保持 2-4 格距离
        if (distance > 4.0) {
            // 导航到主人
            if (wraith.tickCount % 40 == 0) {
                wraith.getNavigation().moveTo(owner, 1.0);
            }
            wraith.setAllowMoving(true);
        } else if (distance < 2.0) {
            // 距离太近，停止移动
            wraith.getNavigation().stop();
            wraith.setAllowMoving(false);
        } else {
            // 在理想距离范围内（2-4格），保持当前状态
            wraith.setAllowMoving(true);
        }
    }

    //检查战斗传送距离并执行传送
    private static boolean checkCombatTeleportDistance(TheLastEndSwordWraithEntity wraith) {
        int configDistance = TheLastSwordConfiguration.getWraithCombatTeleportDistanceSafely();

        // 配置为 0 表示禁用此功能
        if (configDistance <= 0) {
            return false;
        }

        LivingEntity owner = wraith.getOwner();
        if (owner == null || !owner.isAlive()) {
            return false;
        }

        double distanceToOwner = wraith.distanceTo(owner);

        // 如果战斗中距离超过配置值
        if (distanceToOwner > configDistance) {
            // 结束战斗状态
            wraith.targetList.clear();
            wraith.setTarget(null);

            // 停止当前技能
            if (isExecutingSkill(wraith)) {
                endSkill(wraith);
            }

            // 传送到主人身边
            forceTeleportToOwner(wraith, owner);

            return true;
        }

        return false;
    }

    // ==================== 传送系统 ====================

    /**
     * 强制传送到主人身边
     */
    private static void forceTeleportToOwner(TheLastEndSwordWraithEntity wraith, LivingEntity owner) {
        // 清除战斗状态，准备传送
        clearCombatStateForOwnerReturn(wraith);
        
        // 寻找主人附近的安全位置
        Vec3 teleportPos = findSafeTeleportPosition(wraith, owner);
        
        // 播放传送效果
        spawnTeleportEffects(wraith, teleportPos);

        // 执行传送（使用新的VarHandle传送方法）
        EntityUtil.theLastEndTeleport(wraith, teleportPos.x, teleportPos.y, teleportPos.z);
    }
    
    /**
     * 清除战斗状态以便回到主人身边
     */
    private static void clearCombatStateForOwnerReturn(TheLastEndSwordWraithEntity wraith) {
        // 停止当前技能
        if (isExecutingSkill(wraith)) {
            endSkill(wraith);
        }

        // 清除目标列表和当前目标
        wraith.targetList.clear();
        wraith.setTarget(null);

        // 清除强制十字切标记
        FORCE_CROSS_SLASH.remove(wraith.getUUID());

        // 停止导航
        wraith.getNavigation().stop();

        // 允许移动
        wraith.setAllowMoving(true);
    }
    
    /**
     * 寻找主人附近的安全传送位置
     */
    private static Vec3 findSafeTeleportPosition(TheLastEndSwordWraithEntity wraith, LivingEntity owner) {
        Vec3 ownerPos = owner.position();

        // 尝试在主人周围寻找安全位置
        for (int attempts = 0; attempts < 8; attempts++) {
            double angle = (2 * Math.PI * attempts) / 8.0;
            double distance = 2.0 + (attempts * 0.5); // 2-5格距离

            double x = ownerPos.x + Math.cos(angle) * distance;
            double y = ownerPos.y;
            double z = ownerPos.z + Math.sin(angle) * distance;

            // 简单的安全检查（可以进一步优化）
            if (isSafeTeleportPosition(wraith, x, y, z)) {
                return new Vec3(x, y, z);
            }
        }

        // 如果找不到安全位置，传送到主人的位置
        return new Vec3(ownerPos.x, ownerPos.y, ownerPos.z);
    }

    /**
     * 检查传送位置是否安全
     */
    private static boolean isSafeTeleportPosition(TheLastEndSwordWraithEntity wraith, double x, double y, double z) {
        BlockPos pos = new BlockPos((int)x, (int)y, (int)z);
        BlockPos posAbove = pos.above();

        // 简单检查：位置和上方一格不能是固体方块
        return !wraith.level().getBlockState(pos).isSolid() &&
               !wraith.level().getBlockState(posAbove).isSolid();
    }
    
    /**
     * 生成传送特效
     */
    private static void spawnTeleportEffects(TheLastEndSwordWraithEntity wraith, Vec3 targetPos) {
        if (wraith.level().isClientSide) {
            return;
        }

        // 生成传送粒子
        Vec3 oldPos = wraith.position();
        ParticleUtil.spawnTeleportParticles(
            wraith.level(), oldPos, targetPos, wraith.getBbHeight()
        );

        // 播放传送声音
        wraith.level().playSound(
                null,
                wraith.getX(), wraith.getY(), wraith.getZ(),
                SoundEvents.ENDERMAN_TELEPORT,
                wraith.getSoundSource(),
                0.8F, 1.0F
        );
    }

    // ==================== 目标管理系统 ====================

    /**
     * 更新目标列表
     */
    private static void updateTargetList(TheLastEndSwordWraithEntity wraith) {
        List<LivingEntity> targetList = wraith.targetList;

        //如果目标列表被外部清空，强制立即扫描
        boolean forceRescan = targetList.isEmpty();

        //正常情况每20tick扫描一次，被清空时立即扫描
        if (!forceRescan && wraith.tickCount % 20 != 0) return;

        // 清理失效目标
        targetList.removeIf(target -> target == null || !target.isAlive() ||
                target.isRemoved() || wraith.distanceTo(target) > MAX_SEARCH_DISTANCE);

        // 搜索新目标
        AABB searchBox = new AABB(
                wraith.getX() - MAX_SEARCH_DISTANCE,
                wraith.getY() - MAX_SEARCH_DISTANCE,
                wraith.getZ() - MAX_SEARCH_DISTANCE,
                wraith.getX() + MAX_SEARCH_DISTANCE,
                wraith.getY() + MAX_SEARCH_DISTANCE,
                wraith.getZ() + MAX_SEARCH_DISTANCE
        );
        List<Entity> nearbyEntities = wraith.level().getEntities(wraith, searchBox);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity livingEntity) {
                if (EntityUtil.canAttack(wraith, livingEntity)) {
                    UUID newUUID = livingEntity.getUUID();
                    boolean exists = targetList.stream().anyMatch(existing ->
                            existing.getUUID().equals(newUUID));
                    if (!exists) {
                        targetList.add(livingEntity);
                    }
                }
            }
        }
    }

    /**
     * 更新当前目标（支持基于主人的优先级和智能放弃机制）
     */
    private static void updateCurrentTarget(TheLastEndSwordWraithEntity wraith) {
        List<LivingEntity> targetList = wraith.targetList;
        LivingEntity currentTarget = wraith.getTarget();
        UUID wraithId = wraith.getUUID();

        //防御机制：如果原版目标被外部清空但目标列表不为空，立即恢复目标
        if (currentTarget == null && !targetList.isEmpty()) {
            LivingEntity bestTarget = selectTargetWithOwnerPriority(wraith, targetList);
            if (bestTarget != null) {
                setNewTarget(wraith, bestTarget);
                return;
            }
        }

        // 检查当前目标是否应该被放弃
        if (currentTarget != null) {
            UUID targetId = currentTarget.getUUID();
            TargetTrackingData tracking = TARGET_TRACKING.get(targetId);
            
            if (tracking != null) {
                tracking.updateProgress(wraith, currentTarget);
                
                // 如果目标应该被放弃
                if (tracking.shouldAbandon()) {
                    abandonTarget(wraith, currentTarget);
                    currentTarget = null;
                }
            }
        }

        if (!targetList.isEmpty()) {
            // 获取基于主人优先级的最佳目标
            LivingEntity bestTarget = selectTargetWithOwnerPriority(wraith, targetList);
            
            // 如果找到了更优先的目标，或当前目标无效，则切换目标
            if (bestTarget != null && bestTarget != currentTarget) {
                if (currentTarget == null || !currentTarget.isAlive() || 
                    !targetList.contains(currentTarget) || 
                    shouldSwitchTarget(wraith, currentTarget, bestTarget)) {
                    
                    setNewTarget(wraith, bestTarget);
                }
            }
        } else {
            if (currentTarget != null) {
                abandonTarget(wraith, currentTarget);
            }
        }
    }
    
    /**
     * 设置新目标并开始追踪
     */
    private static void setNewTarget(TheLastEndSwordWraithEntity wraith, LivingEntity newTarget) {
        // 清除旧目标的追踪数据
        LivingEntity oldTarget = wraith.getTarget();
        if (oldTarget != null) {
            TARGET_TRACKING.remove(oldTarget.getUUID());
        }
        
        // 设置新目标
        wraith.setTarget(newTarget);
        
        // 开始追踪新目标
        TARGET_TRACKING.put(newTarget.getUUID(), new TargetTrackingData(newTarget.getUUID()));

        // TODO: 触发灾变Boss对话（等待Cataclysm联动实现）
        // try {
        //     TheLastEndSwordWraithEntityCataclysmTalk.onTargetSet(wraith, newTarget);
        // } catch (Exception e) {
        //     // 静默处理对话触发错误，不影响战斗逻辑
        //     TheLastSwordLogger.error("Error triggering cataclysm talk: {}", e.getMessage());
        // }

    }
    
    /**
     * 放弃目标
     */
    private static void abandonTarget(TheLastEndSwordWraithEntity wraith, LivingEntity target) {
        wraith.setTarget(null);
        TARGET_TRACKING.remove(target.getUUID());
        
        // 从目标列表中移除（暂时）
        wraith.targetList.remove(target);
        
        // 停止当前技能（如果正在执行）
        if (isExecutingSkill(wraith)) {
            endSkill(wraith);
        }
        
        // 重置导航
        wraith.getNavigation().stop();
        wraith.setAllowMoving(true);
    }
    
    /**
     * 基于主人优先级和可达性选择目标
     * 优先级：主人正在攻击的目标 > 正在攻击主人的目标 > 可达的最近目标
     */
    private static LivingEntity selectTargetWithOwnerPriority(TheLastEndSwordWraithEntity wraith, List<LivingEntity> targetList) {
        if (targetList.isEmpty()) return null;
        
        // 获取主人
        LivingEntity owner = wraith.getOwner();
        if (owner == null) {
            // 没有主人时，选择智能目标
            return selectSmartTarget(wraith, targetList);
        }
        
        // 优先级1：主人正在攻击的目标（如果可达）
        LivingEntity ownerTarget = owner.getLastHurtMob();
        if (ownerTarget != null && targetList.contains(ownerTarget) && ownerTarget.isAlive()) {
            if (isTargetAccessible(wraith, ownerTarget)) {
                return ownerTarget;
            }
        }
        
        // 优先级2：正在攻击主人的目标（如果可达）
        LivingEntity ownerAttacker = owner.getLastHurtByMob();
        if (ownerAttacker != null && targetList.contains(ownerAttacker) && ownerAttacker.isAlive()) {
            if (isTargetAccessible(wraith, ownerAttacker)) {
                return ownerAttacker;
            }
        }
        
        // 优先级3：智能选择可达的目标
        return selectSmartTarget(wraith, targetList);
    }
    
    /**
     * 智能目标选择：优先可达且有视线的目标
     */
    private static LivingEntity selectSmartTarget(TheLastEndSwordWraithEntity wraith, List<LivingEntity> candidates) {
        // 第一优先级：可达且有视线的目标
        LivingEntity reachableTarget = candidates.stream()
            .filter(target -> isTargetReachable(wraith, target) && wraith.hasLineOfSight(target))
            .min(Comparator.comparing(target -> wraith.distanceTo(target)))
            .orElse(null);
            
        if (reachableTarget != null) {
            return reachableTarget;
        }
        
        // 第二优先级：有视线的目标（可能需要传送攻击）
        LivingEntity visibleTarget = candidates.stream()
            .filter(target -> wraith.hasLineOfSight(target))
            .min(Comparator.comparing(target -> wraith.distanceTo(target)))
            .orElse(null);
            
        if (visibleTarget != null) {
            return visibleTarget;
        }
        
        // 最后：距离最近的目标（但可能无法到达）
        return getClosestTarget(wraith, candidates);
    }
    
    /**
     * 检查目标是否可访问（可导航到达且有视线）
     */
    private static boolean isTargetAccessible(TheLastEndSwordWraithEntity wraith, LivingEntity target) {
        return isTargetReachable(wraith, target) && wraith.hasLineOfSight(target);
    }
    
    /**
     * 检查目标是否可通过导航到达
     */
    private static boolean isTargetReachable(TheLastEndSwordWraithEntity wraith, LivingEntity target) {
        try {
            Path path = wraith.getNavigation().createPath(target, 0);
            return path != null && path.canReach();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取距离最近的目标
     */
    private static LivingEntity getClosestTarget(TheLastEndSwordWraithEntity wraith, List<LivingEntity> targetList) {
        LivingEntity closest = null;
        double closestDistance = Double.MAX_VALUE;
        
        for (LivingEntity target : targetList) {
            if (target == null || !target.isAlive()) continue;
            
            double distance = wraith.distanceTo(target);
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = target;
            }
        }
        
        return closest;
    }
    
    /**
     * 判断是否应该切换目标
     * 只有在新目标优先级明显更高时才切换，避免频繁切换
     */
    private static boolean shouldSwitchTarget(TheLastEndSwordWraithEntity wraith, LivingEntity currentTarget, LivingEntity newTarget) {
        if (currentTarget == null || !currentTarget.isAlive()) return true;
        
        LivingEntity owner = wraith.getOwner();
        if (owner == null) return false;
        
        // 如果新目标是主人正在攻击的目标，立即切换
        if (newTarget == owner.getLastHurtMob()) return true;
        
        // 如果新目标是正在攻击主人的目标，且当前目标不是主人攻击的目标，则切换
        if (newTarget == owner.getLastHurtByMob() && currentTarget != owner.getLastHurtMob()) {
            return true;
        }
        
        // 其他情况保持当前目标，避免频繁切换
        return false;
    }

    // ==================== 导航和移动系统 ====================

    /**
     * 处理导航和移动
     */
    private static void handleNavigationAndMovement(TheLastEndSwordWraithEntity wraith) {
        LivingEntity target = wraith.getTarget();
        if (target == null || !target.isAlive()) {
            wraith.getNavigation().stop();
            resetLookDirection(wraith);
            return;
        }

        double distance = wraith.distanceTo(target);
        double combatDistance = getCombatDistance(wraith); // 使用动态距离

        // 距离大于攻击距离时，允许移动并开始导航
        if (distance > combatDistance && distance <= MAX_SEARCH_DISTANCE) {
            wraith.setAllowMoving(true);

            // 每40ticks重新计算路径
            if (wraith.tickCount % 40 == 0) {
                wraith.getNavigation().moveTo(target, 1.0);
            }
        } else if (distance <= combatDistance) {
            // 到达战斗距离，停止导航，禁止移动
            wraith.getNavigation().stop();
            wraith.setAllowMoving(false);
        }

        // 朝向控制：只在合理距离内且目标有效时才看向目标
        if (distance <= 16.0) {
            wraith.getLookControl().setLookAt(
                    target.getX(),
                    wraith.getEyeY(), // 使用自己的眼部高度，不看向目标的Y坐标
                    target.getZ(),
                    10.0F, // 降低转向速度
                    10.0F  // 降低俯仰速度
            );
        }
    }

    /**
     * 重置朝向方向
     */
    private static void resetLookDirection(TheLastEndSwordWraithEntity wraith) {
        // 重置俯仰角为0（水平朝向）
        wraith.setXRot(0.0F);
    }

    // ==================== 技能系统 ====================

    /**
     * 是否正在执行技能
     */
    public static boolean isExecutingSkill(TheLastEndSwordWraithEntity wraith) {
        return wraith.getIsSpawned() &&
                wraith.getSkillTick() > 0 &&
                !wraith.getSyncedAnimation().equals("empty") &&
                !wraith.getSyncedAnimation().equals("idle");
    }

    /**
     * 更新技能计时（仅服务端）
     */
    private static void updateSkillTick(TheLastEndSwordWraithEntity wraith) {
        int currentTick = wraith.getSkillTick();
        wraith.setSkillTick(currentTick + 1);
    }

    /**
     * 执行当前技能（服务端技能tick完全独立于客户端动画）
     */
    private static void executeCurrentSkill(TheLastEndSwordWraithEntity wraith) {
        String currentSkill = wraith.getSyncedAnimation();
        int tick = wraith.getSkillTick();

        switch (currentSkill) {
            case "idle":
                endSkillAndStartNext(wraith);
                break;
            case "swift_dash":
                SwiftDashSkill.execute(wraith, tick);
                break;
            case "enchant":
                EnchantSkill.execute(wraith, tick);
                break;
            case "double_strike":
                DoubleStrikeSkill.execute(wraith, tick);
                break;
            case "cross_slash":
                CrossSlashSkill.execute(wraith, tick);
                break;
            case "block":
                BlockSkill.execute(wraith, tick);
                break;
            case "moon_light_strike":
                MoonLightStrikeSkill.execute(wraith, tick);
                break;
            case "end_of_all_things":
                EndOfAllThingsSkill.execute(wraith, tick);
                break;
            default:
                endSkillAndStartNext(wraith);
                break;
        }
    }

    /**
     * 尝试启动新技能
     */
    private static void tryStartNewSkill(TheLastEndSwordWraithEntity wraith) {
        LivingEntity target = wraith.getTarget();
        if (target == null || !target.isAlive()) return;

        double distance = wraith.distanceTo(target);
        UUID wraithId = wraith.getUUID();

        // 检查是否有强制十字切标记
        if (FORCE_CROSS_SLASH.getOrDefault(wraithId, false)) {
            startSkill(wraith, "cross_slash");
            FORCE_CROSS_SLASH.remove(wraithId);
            return;
        }

        // 靠近状态（距离 > 6）：执行冲刺技能
        if (distance > APPROACH_DISTANCE) {
            startSkill(wraith, "swift_dash");
            return;
        }

        // 到达状态（距离 ≤ 6）：按权重抽取技能
        selectSkillInCombat(wraith);
    }

    /**
     * 战斗中的技能选择（到达状态：距离 ≤ 6）
     */
    private static void selectSkillInCombat(TheLastEndSwordWraithEntity wraith) {
        boolean hasVoidEnchantment = wraith.hasEffect(ModEffects.VOID_ENCHANTING.get());

        Map<String, Double> skillWeights = new HashMap<>();

        // 万物终焉技能：只有13级且尚未激活万物终焉状态时可以使用
        if (wraith.getEndLevel() >= wraith.getAllThingsEndLevel() &&
            !wraith.isAllThingsEnd()) {
            double weight = TheLastSwordConfiguration.getSkillEndOfAllThingsWeightSafely();
            if (weight > 0) {
                skillWeights.put("end_of_all_things", weight);
            }
        }
        // 附魔技能：没有虚空附魔 Buff 时才能使用
        if (!hasVoidEnchantment) {
            double weight = TheLastSwordConfiguration.getSkillEnchantWeightSafely();
            if (weight > 0) {
                skillWeights.put("enchant", weight);
            }
        }

        // 其他技能（从配置读取权重，权重为0则不添加）
        double doubleStrikeWeight = TheLastSwordConfiguration.getSkillDoubleStrikeWeightSafely();
        if (doubleStrikeWeight > 0) {
            skillWeights.put("double_strike", doubleStrikeWeight);
        }

        double blockWeight = TheLastSwordConfiguration.getSkillBlockWeightSafely();
        if (blockWeight > 0) {
            skillWeights.put("block", blockWeight);
        }

        double moonLightStrikeWeight = TheLastSwordConfiguration.getSkillMoonLightStrikeWeightSafely();
        if (moonLightStrikeWeight > 0) {
            skillWeights.put("moon_light_strike", moonLightStrikeWeight);
        }

        double crossSlashWeight = TheLastSwordConfiguration.getSkillCrossSlashWeightSafely();
        if (crossSlashWeight > 0) {
            skillWeights.put("cross_slash", crossSlashWeight);
        }

        // 按权重随机选择
        double totalWeight = skillWeights.values().stream().mapToDouble(Double::doubleValue).sum();
        double randomValue = wraith.getRandom().nextDouble() * totalWeight;
        double currentWeight = 0.0;

        for (Map.Entry<String, Double> entry : skillWeights.entrySet()) {
            currentWeight += entry.getValue();
            if (randomValue < currentWeight) {
                startSkill(wraith, entry.getKey());
                return;
            }
        }
    }

    // ==================== 技能控制 ====================

    /**
     * 开始技能
     */
    private static void startSkill(TheLastEndSwordWraithEntity wraith, String skillName) {
        wraith.setSkillTick(1);
        wraith.setAnimation(skillName);

        // 除了冲刺技能外，其他技能都禁止移动
        if (!skillName.equals("swift_dash")) {
            wraith.setAllowMoving(false);
        }
    }

    /**
     * 结束技能并立即开始下一个技能（连续技能系统）
     */
    private static void endSkillAndStartNext(TheLastEndSwordWraithEntity wraith) {
        // 重置当前技能状态
        wraith.setSkillTick(0);
        wraith.setAnimation("empty");

        // 立即尝试启动下一个技能
        tryStartNewSkill(wraith);

        // 如果没有选择到新技能，则设置为空动画
        if (wraith.getSyncedAnimation().equals("empty")) {
            wraith.setAnimation("empty");
            // 允许移动（没有技能执行时）
            wraith.setAllowMoving(true);
        }
    }

    /**
     * 普通结束技能（非连续）
     */
    private static void endSkill(TheLastEndSwordWraithEntity wraith) {
        wraith.setSkillTick(0);
        wraith.setAnimation("empty");
        wraith.setAllowMoving(true);
    }

    // ==================== 清理方法 ====================

    public static void cleanupRemovedEntity(UUID entityId) {
        // 清理强制十字切标记
        FORCE_CROSS_SLASH.remove(entityId);

        // 清理目标追踪数据
        TARGET_TRACKING.entrySet().removeIf(entry -> {
            TargetTrackingData data = entry.getValue();
            return data.targetId.equals(entityId);
        });

        // TODO: 清理对话记录（等待Cataclysm联动实现）
        // try {
        //     TheLastEndSwordWraithEntityCataclysmTalk.cleanupTalkedBoss(entityId);
        // } catch (Exception e) {
        //     // 静默处理清理错误
        // }
    }



    public static void handleLevelChange(TheLastEndSwordWraithEntity wraith, int oldLevel, int newLevel) {
        if (isExecutingSkill(wraith)) {
            endSkill(wraith);
        }
    }

    public static void onAllThingsEndUnlocked(TheLastEndSwordWraithEntity wraith) {
        // 万物终焉解锁处理
    }

    public static void onAllThingsEndLost(TheLastEndSwordWraithEntity wraith) {
        if (wraith.getSyncedAnimation().equals("end_of_all_things")) {
            endSkill(wraith);
        }
        // 停止万物终焉持续效果
        AllThingsEndActiveEffect.forceStop(wraith.getUUID());
    }
    
    /**
     * 检查指定实体是否有激活的万物终焉效果 - 公共接口
     */
    public static boolean hasActiveAllThingsEndEffect(TheLastEndSwordWraithEntity wraith) {
        return AllThingsEndActiveEffect.isActive(wraith);
    }

    // ==================== 技能实现类 ====================

    private static class SwiftDashSkill {
        private static final int SKILL_DURATION = 60;
        private static final int DASH_START_TICK = 35;
        private static final int FINAL_STRIKE_TICK = 55; // 最后5tick造成伤害

        public static void execute(TheLastEndSwordWraithEntity wraith, int tick) {
            LivingEntity target = wraith.getTarget();

            if (tick >= DASH_START_TICK && target != null) {
                Vec3 wraithPos = wraith.position();
                Vec3 targetPos = target.position();

                //完整的三维冲刺
                Vec3 direction = new Vec3(
                    targetPos.x - wraithPos.x,
                    targetPos.y - wraithPos.y,
                    targetPos.z - wraithPos.z
                ).normalize();

                double dashSpeed = 1.5;
                wraith.setDeltaMovement(
                        direction.x * dashSpeed,
                        direction.y * dashSpeed,
                        direction.z * dashSpeed
                );
            }

            // 最后1秒造成范围伤害
            if (tick == FINAL_STRIKE_TICK) {
                executeFinalStrike(wraith);
            }

            if (tick >= SKILL_DURATION) {
                // 技能结束时检查距离，决定是否强制十字切
                if (target != null && target.isAlive()) {
                    double distance = wraith.distanceTo(target);
                    if (distance > DASH_SUCCESS_DISTANCE) {
                        // 冲刺后距离仍 > 4 格，设置强制十字切标记
                        FORCE_CROSS_SLASH.put(wraith.getUUID(), true);
                    }
                }

                endSkillAndStartNext(wraith);
            }
        }

        //执行突刺最后一击
        private static void executeFinalStrike(TheLastEndSwordWraithEntity wraith) {
            // 从配置读取参数
            double attackRange = TheLastSwordConfiguration.getSkillSwiftDashRangeSafely();
            float damageMultiplier = (float) TheLastSwordConfiguration.getSkillSwiftDashDamageMultiplierSafely();

            // 获取前方半圆范围内的目标
            List<LivingEntity> targets = EntityUtil.getTargetsInHemisphere(wraith, attackRange);

            // 计算伤害
            float damage = (float) wraith.getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier;

            // 对每个目标造成伤害
            for (LivingEntity target : targets) {
                target.invulnerableTime = 0;
                target.hurt(wraith.damageSources().mobAttack(wraith), damage);
            }

            // 播放攻击音效
            wraith.level().playSound(
                null,
                wraith.getX(), wraith.getY(), wraith.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP,
                wraith.getSoundSource(),
                1.0F, 1.0F
            );
        }
    }

    private static class EnchantSkill {
        private static final int SKILL_DURATION = 45;
        private static final int ENCHANT_START_TICK = 25;

        public static void execute(TheLastEndSwordWraithEntity wraith, int tick) {
            if (tick == ENCHANT_START_TICK) {
                applyVoidEnchantment(wraith);
                // 播放附魔声音
                playEnchantSound(wraith);
            }

            //粒子效果由虚空附魔Buff自身处理，这里不生成

            if (tick >= SKILL_DURATION) {
                endSkillAndStartNext(wraith);
            }
        }

        /**
         * 播放玩家附魔物品声音
         */
        private static void playEnchantSound(TheLastEndSwordWraithEntity wraith) {
            if (wraith.level().isClientSide) return;

            wraith.level().playSound(
                    null,
                    wraith.getX(),
                    wraith.getY(),
                    wraith.getZ(),
                    SoundEvents.ENCHANTMENT_TABLE_USE, // 附魔台使用声音
                    wraith.getSoundSource(),
                    1.0F, // 音量
                    1.0F  // 音调
            );
        }

        private static void applyVoidEnchantment(TheLastEndSwordWraithEntity wraith) {
            if (wraith.level().isClientSide) return;

            // 从配置读取持续时间
            int enchantDuration = TheLastSwordConfiguration.getSkillEnchantDurationSafely();

            int wraithLevel = wraith.getEndLevel();
            int enchantmentAmplifier = Math.max(0, wraithLevel - 1);

            MobEffectInstance voidEnchantment = new MobEffectInstance(
                    ModEffects.VOID_ENCHANTING.get(),
                    enchantDuration,
                    enchantmentAmplifier,
                    false,
                    TheLastSwordConfiguration.BUFF_VOID_ENCHANTMENT_PARTICLE_EFFECTS.get(),
                    true
            );

            wraith.addEffect(voidEnchantment);
        }
    }

    private static class DoubleStrikeSkill {
        private static final int SKILL_DURATION = 40;
        private static final int FIRST_STRIKE_TICK = 10;
        private static final int SECOND_STRIKE_TICK = 20;

        public static void execute(TheLastEndSwordWraithEntity wraith, int tick) {
            if (tick == FIRST_STRIKE_TICK) {
                executeFirstStrike(wraith);
            }

            if (tick == SECOND_STRIKE_TICK) {
                executeSecondStrike(wraith);
            }

            if (tick >= SKILL_DURATION) {
                endSkillAndStartNext(wraith);
            }
        }

        /**
         * 播放横扫之刃声音
         */
        private static void playSweepSound(TheLastEndSwordWraithEntity wraith) {
            if (wraith.level().isClientSide) return;

            wraith.level().playSound(
                    null,
                    wraith.getX(),
                    wraith.getY(),
                    wraith.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP,
                    wraith.getSoundSource(),
                    1.0F,
                    1.0F
            );
        }

        private static void executeFirstStrike(TheLastEndSwordWraithEntity wraith) {
            double attackRange = TheLastSwordConfiguration.getSkillDoubleStrikeRangeSafely();
            float damageMultiplier = (float) TheLastSwordConfiguration.getSkillDoubleStrikeDamageMultiplierSafely();

            // 播放横扫声音
            playSweepSound(wraith);

            // 范围攻击
            List<LivingEntity> targets = EntityUtil.getTargetsInHemisphere(wraith, attackRange);
            for (LivingEntity livingTarget : targets) {
                livingTarget.invulnerableTime = 0;
                float damage = (float) wraith.getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier;
                livingTarget.hurt(wraith.damageSources().mobAttack(wraith), damage);
            }

            // 生成第一击粒子效果
            ParticleUtil.spawnSweepParticles(wraith, true, attackRange);
        }

        private static void executeSecondStrike(TheLastEndSwordWraithEntity wraith) {
            double attackRange = TheLastSwordConfiguration.getSkillDoubleStrikeRangeSafely();
            float damageMultiplier = (float) TheLastSwordConfiguration.getSkillDoubleStrikeDamageMultiplierSafely();

            // 播放横扫声音
            playSweepSound(wraith);

            // 范围攻击
            List<LivingEntity> targets = EntityUtil.getTargetsInHemisphere(wraith, attackRange);
            for (LivingEntity livingTarget : targets) {
                livingTarget.invulnerableTime = 0;
                float damage = (float) wraith.getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier;
                DamageSource magicDamage = new DamageSource(
                        wraith.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                                .getHolderOrThrow(DamageTypes.MAGIC),
                        wraith,
                        wraith
                );
                livingTarget.hurt(magicDamage, damage);
            }

            // 生成第二击粒子效果
            ParticleUtil.spawnSweepParticles(wraith, false, attackRange);
        }
    }

    private static class CrossSlashSkill {
        private static final int SKILL_DURATION = 80;
        private static final int FIRST_SLASH_TICK = 55;
        private static final int SECOND_SLASH_TICK = 75;

        public static void execute(TheLastEndSwordWraithEntity wraith, int tick) {
            // 播放铁砧声音
            if (tick == 15) {
                playAnvilSound(wraith);
            }

            // 第一次攻击：传送并攻击
            if (tick == FIRST_SLASH_TICK) {
                executeSlash(wraith);
            }

            // 第二次攻击：再次传送并攻击
            if (tick == SECOND_SLASH_TICK) {
                executeSlash(wraith);
            }

            if (tick >= SKILL_DURATION) {
                endSkillAndStartNext(wraith);
            }
        }

        /**
         * 执行十字切攻击：传送到当前目标位置并攻击
         */
        private static void executeSlash(TheLastEndSwordWraithEntity wraith) {
            // 获取当前攻击目标
            LivingEntity target = wraith.getTarget();

            // 如果没有目标，直接返回
            if (target == null || !target.isAlive()) {
                return;
            }

            double attackRange = TheLastSwordConfiguration.getSkillCrossSlashRangeSafely();
            float damageMultiplier = (float) TheLastSwordConfiguration.getSkillCrossSlashDamageMultiplierSafely();

            // 1. 无条件传送到目标位置
            double targetX = target.getX();
            double targetY = target.getY();
            double targetZ = target.getZ();

            // 播放传送效果
            playTeleportSound(wraith);
            spawnTeleportParticles(wraith, targetX, targetY, targetZ);

            // 执行传送（使用新的VarHandle传送方法）
            EntityUtil.theLastEndTeleport(wraith, targetX, targetY, targetZ);

            // 2. 攻击当前目标
            target.invulnerableTime = 0;
            float damage = (float) wraith.getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier;
            // 使用智能API以兼容其他Mod的同时保证真伤效果
            AbsoluteDestructionDamageSource.applyAbsoluteDestructionIntelligently(target, wraith, damage);

            // 3. 攻击范围内的其他敌人（排除主目标避免重复伤害）
            List<LivingEntity> nearbyTargets = EntityUtil.getTargetsInHemisphere(wraith, attackRange);
            for (LivingEntity nearbyTarget : nearbyTargets) {
                if (!nearbyTarget.equals(target)) {
                    nearbyTarget.invulnerableTime = 0;
                    // 使用智能API以兼容其他Mod的同时保证真伤效果
                    AbsoluteDestructionDamageSource.applyAbsoluteDestructionIntelligently(nearbyTarget, wraith, damage);
                }
            }
        }

        /**
         * 播放铁砧使用声音
         */
        private static void playAnvilSound(TheLastEndSwordWraithEntity wraith) {
            if (wraith.level().isClientSide) return;

            wraith.level().playSound(
                    null,
                    wraith.getX(),
                    wraith.getY(),
                    wraith.getZ(),
                    SoundEvents.ANVIL_USE,
                    wraith.getSoundSource(),
                    1.0F,
                    1.0F
            );
        }

        /**
         * 播放末影人传送声音
         */
        private static void playTeleportSound(TheLastEndSwordWraithEntity wraith) {
            if (wraith.level().isClientSide) return;

            wraith.level().playSound(
                    null,
                    wraith.getX(),
                    wraith.getY(),
                    wraith.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT,
                    wraith.getSoundSource(),
                    1.0F,
                    1.0F
            );
        }

        /**
         * 生成末影人传送粒子
         */
        private static void spawnTeleportParticles(TheLastEndSwordWraithEntity wraith, double x, double y, double z) {
            Vec3 oldPos = wraith.position();
            Vec3 targetPos = new Vec3(x, y, z);

            //基础传送粒子
            ParticleUtil.spawnTeleportParticles(
                wraith.level(), oldPos, targetPos, wraith.getBbHeight()
            );

            //额外的反向传送门粒子
            ParticleUtil.spawnCrossSlashTeleportParticles(
                wraith.level(), targetPos, wraith.getBbHeight()
            );
        }
    }

    private static class BlockSkill {
        private static final int SKILL_DURATION = 15; // 0.75秒 = 15 tick
        private static final double KNOCKBACK_STRENGTH = 0.4; // 击退强度

        //执行格挡技能
        public static void execute(TheLastEndSwordWraithEntity wraith, int tick) {
            if (tick == 0) {
                wraith.setAllowMoving(false);
            }

            //每tick执行击退、伤害和偏转
            if (tick < SKILL_DURATION) {
                applyKnockbackAndDamage(wraith);
                deflectProjectiles(wraith);
            }

            //技能结束
            if (tick >= SKILL_DURATION) {
                wraith.setAllowMoving(true);
                endSkillAndStartNext(wraith);
            }
        }

        //对前方半圆范围内的目标施加击退和伤害
        private static void applyKnockbackAndDamage(TheLastEndSwordWraithEntity wraith) {
            // 从配置读取参数
            double range = TheLastSwordConfiguration.getSkillBlockRangeSafely();
            float damageMultiplier = (float) TheLastSwordConfiguration.getSkillBlockDamageMultiplierSafely();

            List<LivingEntity> targets = EntityUtil.getTargetsInHemisphere(wraith, range);

            // 计算伤害
            float damage = (float) wraith.getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier;

            for (LivingEntity target : targets) {
                //计算击退方向（从剑灵指向目标）
                Vec3 knockbackDir = new Vec3(
                        target.getX() - wraith.getX(),
                        0,
                        target.getZ() - wraith.getZ()
                ).normalize();

                //施加击退（向后推）
                target.setDeltaMovement(
                        target.getDeltaMovement().add(
                                knockbackDir.x * KNOCKBACK_STRENGTH,
                                0.1, // 轻微向上
                                knockbackDir.z * KNOCKBACK_STRENGTH
                        )
                );

                // 造成伤害（每tick一次）
                target.invulnerableTime = 0;
                target.hurt(wraith.damageSources().mobAttack(wraith), damage);
            }
        }

        //偏转前方的弹射物
        private static void deflectProjectiles(TheLastEndSwordWraithEntity wraith) {
            // 从配置读取范围
            double range = TheLastSwordConfiguration.getSkillBlockRangeSafely();

            AABB searchBox = new AABB(
                    wraith.getX() - range,
                    wraith.getY() - range,
                    wraith.getZ() - range,
                    wraith.getX() + range,
                    wraith.getY() + range,
                    wraith.getZ() + range
            );

            List<Entity> nearbyEntities = wraith.level().getEntities(wraith, searchBox);
            Vec3 wraithLook = wraith.getLookAngle();

            for (Entity entity : nearbyEntities) {
                if (entity instanceof Projectile projectile) {
                    //计算弹射物相对位置
                    Vec3 toProjectile = new Vec3(
                            projectile.getX() - wraith.getX(),
                            0,
                            projectile.getZ() - wraith.getZ()
                    ).normalize();

                    //检查是否在前方半圆
                    double dotProduct = wraithLook.x * toProjectile.x + wraithLook.z * toProjectile.z;

                    if (dotProduct > 0) {
                        //偏转弹射物向上
                        Vec3 currentVelocity = projectile.getDeltaMovement();
                        double speed = currentVelocity.length();

                        //随机偏转到上方
                        double randomX = (wraith.getRandom().nextDouble() - 0.5) * 0.5;
                        double randomZ = (wraith.getRandom().nextDouble() - 0.5) * 0.5;

                        projectile.setDeltaMovement(
                                randomX * speed,
                                speed * 0.8, // 主要向上
                                randomZ * speed
                        );
                    }
                }
            }
        }
    }

    private static class MoonLightStrikeSkill {
        private static final int SKILL_DURATION = 100;
        private static final int DAMAGE_TICK = 95;
        private static final double LAUNCH_STRENGTH = 1.2; // 向上击飞强度

        //执行月光打击技能
        public static void execute(TheLastEndSwordWraithEntity wraith, int tick) {
            if (tick == 0) {
                wraith.setAllowMoving(false);
            }

            //4.75秒时造成伤害和击飞
            if (tick == DAMAGE_TICK) {
                executeStrike(wraith);
            }

            //技能结束
            if (tick >= SKILL_DURATION) {
                wraith.setAllowMoving(true);
                endSkillAndStartNext(wraith);
            }
        }

        //执行月光打击
        private static void executeStrike(TheLastEndSwordWraithEntity wraith) {
            // 从配置读取参数
            double strikeRange = TheLastSwordConfiguration.getSkillMoonLightStrikeRangeSafely();
            float damageMultiplier = (float) TheLastSwordConfiguration.getSkillMoonLightStrikeDamageMultiplierSafely();

            //获取球体范围内的有效攻击目标
            List<LivingEntity> targets = EntityUtil.getTargetsInSphere(wraith, strikeRange);

            //计算伤害
            float damage = (float) wraith.getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier;

            //对每个目标造成伤害和击飞
            for (LivingEntity target : targets) {
                //造成物理伤害
                DamageSource damageSource = new DamageSource(
                    wraith.level().registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(DamageTypes.MOB_ATTACK),
                    wraith
                );
                target.hurt(damageSource, damage);

                //向上击飞
                target.setDeltaMovement(
                    target.getDeltaMovement().add(
                        0,
                        LAUNCH_STRENGTH, // 向上击飞
                        0
                    )
                );

                //播放击中音效
                target.playSound(SoundEvents.PLAYER_ATTACK_CRIT, 1.0F, 1.0F);
            }
        }
    }

    private static class EndOfAllThingsSkill {
        private static final int SKILL_DURATION = 190;           // 总持续时间 9.5秒
        private static final int PARTICLE_START_TICK = 10;       // 0.5秒后开始粒子效果
        private static final int PARTICLE_STABLE_END_TICK = 150; // 7.5秒时粒子稳定期结束
        private static final int PARTICLE_SHRINK_END_TICK = 170; // 8.5秒时粒子收缩完毕
        private static final int ACTIVATE_ALL_THINGS_END_TICK = 170; // 8.5秒时激活万物终焉

        public static void execute(TheLastEndSwordWraithEntity wraith, int tick) {
            // 0.5秒后开始粒子效果
            if (tick >= PARTICLE_START_TICK && tick <= PARTICLE_SHRINK_END_TICK) {
                spawnAllThingsEndParticles(wraith, tick);
            }

            // 8.5秒时激活万物终焉状态
            if (tick == ACTIVATE_ALL_THINGS_END_TICK) {
                wraith.setAllThingsEndState(true);
                // 启动万物终焉持续效果（13秒）
                AllThingsEndActiveEffect.start(wraith);
            }

            if (tick >= SKILL_DURATION) {
                endSkillAndStartNext(wraith);
            }
        }
        
        /**
         * 生成万物终焉粒子效果：两个圆圈（水平和垂直）
         */
        private static void spawnAllThingsEndParticles(TheLastEndSwordWraithEntity wraith, int tick) {
            // 使用实体位置
            Vec3 centerPos = wraith.position().add(0, wraith.getBbHeight() * 0.5, 0);
            double baseRadius = 2.0;

            if (tick <= PARTICLE_STABLE_END_TICK) {
                // 稳定期：保持2格半径的圆圈
                ParticleUtil.spawnAllThingsEndCircles(wraith.level(), centerPos, baseRadius);
            } else {
                // 收缩期：粒子从圆圈向圆心移动
                double shrinkProgress = (double)(tick - PARTICLE_STABLE_END_TICK) /
                                      (PARTICLE_SHRINK_END_TICK - PARTICLE_STABLE_END_TICK);
                ParticleUtil.spawnAllThingsEndShrinkingCircles(wraith.level(), centerPos, baseRadius, shrinkProgress);
            }

            // 在圆心位置生成额外的龙息粒子
            if (tick > PARTICLE_STABLE_END_TICK) {
                // 收缩期间，圆心粒子更频繁
                if (tick % 2 == 0) {
                    ParticleUtil.spawnAllThingsEndCenterParticles(wraith.level(), centerPos, true);
                }
            } else if (tick % 5 == 0) {
                ParticleUtil.spawnAllThingsEndCenterParticles(wraith.level(), centerPos, false);
            }
        }
    }




    // ══════════════════════════════════════════════════════════════════════════════════════════
    // All Things End Active Effect | 万物终焉持续效果
    // ══════════════════════════════════════════════════════════════════════════════════════════
    
    private static class AllThingsEndActiveEffect {
        private static final Map<UUID, Integer> ACTIVE_EFFECTS = new ConcurrentHashMap<>();
        private static final int DURATION = 260; // 13秒 (13 * 20 = 260 tick)

        /**
         * 启动万物终焉持续效果
         */
        public static void start(TheLastEndSwordWraithEntity wraith) {
            ACTIVE_EFFECTS.put(wraith.getUUID(), 0);
        }

        /**
         * 处理万物终焉持续效果 - 每tick调用
         */
        public static void handleTick(TheLastEndSwordWraithEntity wraith) {
            UUID id = wraith.getUUID();
            Integer currentTick = ACTIVE_EFFECTS.get(id);

            if (currentTick == null) {
                return; // 没有激活的万物终焉效果
            }

            int newTick = currentTick + 1;

            // 每秒执行一次判定（20 tick = 1秒）
            if (newTick % 20 == 0) {
                executeAllThingsEndEffect(wraith);
            }

            // 检查是否超过持续时间
            if (newTick >= DURATION) {
                end(wraith);
            } else {
                ACTIVE_EFFECTS.put(id, newTick);
            }
        }
        
        /**
         * 结束万物终焉持续效果
         */
        public static void end(TheLastEndSwordWraithEntity wraith) {
            ACTIVE_EFFECTS.remove(wraith.getUUID());
            wraith.setAllThingsEndState(false);
            wraith.updateDefenseLevel();
        }
        
        /**
         * 强制停止某个实体的万物终焉效果
         */
        public static void forceStop(UUID entityId) {
            ACTIVE_EFFECTS.remove(entityId);
        }
        
        /**
         * 检查实体是否有激活的万物终焉效果
         */
        public static boolean isActive(TheLastEndSwordWraithEntity wraith) {
            return ACTIVE_EFFECTS.containsKey(wraith.getUUID());
        }
        
        /**
         * 获取剩余时间（tick）
         */
        public static int getRemainingTime(TheLastEndSwordWraithEntity wraith) {
            Integer currentTick = ACTIVE_EFFECTS.get(wraith.getUUID());
            if (currentTick == null) {
                return 0;
            }
            return Math.max(0, DURATION - currentTick);
        }
        
        /**
         * 执行万物终焉效果：每秒对范围内的目标造成伤害并清空正面buff
         */
        private static void executeAllThingsEndEffect(TheLastEndSwordWraithEntity wraith) {
            if (wraith.level().isClientSide) {
                return;
            }

            // 从配置获取范围
            double effectRange = TheLastSwordConfiguration.getSkillEndOfAllThingsRangeSafely();
            List<LivingEntity> targets = EntityUtil.getTargetsInSphere(wraith, effectRange);

            // 从配置获取伤害倍率
            double damageMultiplier = TheLastSwordConfiguration.getSkillEndOfAllThingsDamageMultiplierSafely();
            double maxHealthPercentage = TheLastSwordConfiguration.getSkillEndOfAllThingsMaxHealthPercentageSafely();

            for (LivingEntity target : targets) {
                applyAllThingsEndEffect(wraith, target, damageMultiplier, maxHealthPercentage);
            }
        }

        /**
         * 对目标应用万物终焉效果
         */
        private static void applyAllThingsEndEffect(TheLastEndSwordWraithEntity wraith, LivingEntity target,
                                                     double damageMultiplier, double maxHealthPercentage) {
            if (wraith.level().isClientSide) {
                return;
            }

            // 1. 计算伤害 = 剑灵攻击力 × 倍率 + 目标最大生命值 × 百分比
            float baseAttackDamage = (float) wraith.getAttributeValue(Attributes.ATTACK_DAMAGE);
            float attackDamage = (float) (baseAttackDamage * damageMultiplier);
            float maxHealthDamage = (float) (target.getMaxHealth() * maxHealthPercentage);
            float totalDamage = attackDamage + maxHealthDamage;

            // 2. 造成绝对毁灭伤害
            AbsoluteDestructionDamageSource.applyAbsoluteDestructionIntelligently(target, wraith, totalDamage);

            // 3. 清空所有正面buff
            clearPositiveEffects(target);
        }

        /**
         * 清空目标的所有正面buff
         */
        private static void clearPositiveEffects(LivingEntity target) {
            // 获取所有活跃效果的副本
            var activeEffects = new ArrayList<>(target.getActiveEffects());

            for (MobEffectInstance effect : activeEffects) {
                // 只移除正面效果（非负面效果）
                if (effect.getEffect().isBeneficial()) {
                    target.removeEffect(effect.getEffect());
                }
            }
        }

    }
}