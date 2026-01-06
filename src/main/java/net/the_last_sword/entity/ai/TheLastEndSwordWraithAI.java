package net.the_last_sword.entity.ai;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.damagesource.AbsoluteDestructionDamageSource;
import net.the_last_sword.entity.TheLastEndSwordWraithEntity;
import net.the_last_sword.init.ModEffects;
import net.the_last_sword.util.EntityUtil;
import net.the_last_sword.util.ParticleUtil;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

//终焉剑灵AI系统
public class TheLastEndSwordWraithAI extends TheLastEndAI {

    //AI常量
    private static final double MAX_SEARCH_DISTANCE = 64.0;
    private static final double COMBAT_DISTANCE = 4.0;
    private static final double APPROACH_DISTANCE = 6.0;  //靠近状态阈值
    private static final double DASH_SUCCESS_DISTANCE = 4.0;  //冲刺成功阈值

    //强制十字切标记映射
    private static final Map<UUID, Boolean> FORCE_CROSS_SLASH = new ConcurrentHashMap<>();

    public TheLastEndSwordWraithAI(TheLastEndSwordWraithEntity entity) {
        super(entity);
    }

    @Override
    protected void registerSkills() {
        //注册7个技能
        addSkill(new SwiftDashSkill((TheLastEndSwordWraithEntity) entity));
        addSkill(new EnchantSkill((TheLastEndSwordWraithEntity) entity));
        addSkill(new DoubleStrikeSkill((TheLastEndSwordWraithEntity) entity));
        addSkill(new CrossSlashSkill((TheLastEndSwordWraithEntity) entity));
        addSkill(new BlockSkill((TheLastEndSwordWraithEntity) entity));
        addSkill(new MoonLightStrikeSkill((TheLastEndSwordWraithEntity) entity));
        addSkill(new EndOfAllThingsSkill((TheLastEndSwordWraithEntity) entity));
    }

    @Override
    protected void aiTick() {
        TheLastEndSwordWraithEntity wraith = (TheLastEndSwordWraithEntity) entity;

        //处理万物终焉持续效果（独立于其他状态）
        AllThingsEndActiveEffect.handleTick(wraith);

        //目标管理（每20tick检查一次）
        if (wraith.tickCount % 20 == 0) {
            findAndUpdateTarget(wraith);
        }

        //检查是否有有效目标
        LivingEntity target = wraith.getTarget();
        if (!isValidTarget(target)) {
            wraith.setTarget(null);
            wraith.getNavigation().stop();

            //无目标时执行跟随主人逻辑
            followOwner(wraith);
            return;
        }

        //检查战斗传送距离
        if (checkCombatTeleportDistance(wraith)) {
            return; //传送后结束本tick
        }

        //导航和移动
        handleNavigation(wraith, target);

        //技能选择
        tryStartNewSkill(wraith, target);
    }

    //检查目标是否有效
    private boolean isValidTarget(LivingEntity target) {
        if (target == null || !target.isAlive() || target.isRemoved()) {
            return false;
        }
        TheLastEndSwordWraithEntity wraith = (TheLastEndSwordWraithEntity) entity;
        if (wraith.distanceTo(target) > MAX_SEARCH_DISTANCE) {
            return false;
        }
        if (target instanceof Player player) {
            return !player.isCreative() && !player.isSpectator();
        }
        return true;
    }

    //寻找并更新目标（智能选择，考虑主人优先级）
    private void findAndUpdateTarget(TheLastEndSwordWraithEntity wraith) {
        LivingEntity currentTarget = wraith.getTarget();

        //验证当前目标
        if (!isValidTarget(currentTarget)) {
            wraith.setTarget(null);
            currentTarget = null;
        }

        //反击攻击者（最高优先级）
        LivingEntity attacker = wraith.getLastHurtByMob();
        if (isValidTarget(attacker)) {
            wraith.setTarget(attacker);
            return;
        }

        //获取主人
        LivingEntity owner = wraith.getOwner();

        //优先级1：主人正在攻击的目标
        if (owner != null) {
            LivingEntity ownerTarget = owner.getLastHurtMob();
            if (isValidTarget(ownerTarget)) {
                wraith.setTarget(ownerTarget);
                return;
            }

            //优先级2：正在攻击主人的目标
            LivingEntity ownerAttacker = owner.getLastHurtByMob();
            if (isValidTarget(ownerAttacker)) {
                wraith.setTarget(ownerAttacker);
                return;
            }
        }

        //优先级3：最近的怪物
        if (currentTarget == null) {
            List<LivingEntity> nearbyMonsters = wraith.level().getEntitiesOfClass(
                LivingEntity.class,
                wraith.getBoundingBox().inflate(MAX_SEARCH_DISTANCE),
                entity -> entity.isAlive()
                    && entity instanceof net.minecraft.world.entity.monster.Monster
                    && EntityUtil.canAttack(wraith, entity)
            );

            if (!nearbyMonsters.isEmpty()) {
                LivingEntity closestMonster = nearbyMonsters.stream()
                    .min(Comparator.comparingDouble(m -> m.distanceToSqr(wraith)))
                    .orElse(null);

                if (closestMonster != null) {
                    wraith.setTarget(closestMonster);
                }
            }
        }
    }

    //导航处理
    private void handleNavigation(TheLastEndSwordWraithEntity wraith, LivingEntity target) {
        wraith.getLookControl().setLookAt(target, 30.0F, 30.0F);

        double distance = wraith.distanceTo(target);
        if (distance > COMBAT_DISTANCE) {
            if (!wraith.getNavigation().isInProgress()) {
                wraith.getNavigation().moveTo(target, 1.0);
            }
        } else {
            wraith.getNavigation().stop();
        }
    }

    //尝试开始新技能
    private void tryStartNewSkill(TheLastEndSwordWraithEntity wraith, LivingEntity target) {
        //万物终焉状态下，只使用万物终焉技能
        if (wraith.isAllThingsEnd()) {
            Skill endSkill = getSkillByAnimation("end_of_all_things");
            if (endSkill != null) {
                useSkill(endSkill);
            }
            return;
        }

        UUID wraithId = wraith.getUUID();

        //检查是否有强制十字切标记
        if (FORCE_CROSS_SLASH.getOrDefault(wraithId, false)) {
            Skill crossSlashSkill = getSkillByAnimation("cross_slash");
            if (crossSlashSkill != null) {
                useSkill(crossSlashSkill);
                FORCE_CROSS_SLASH.remove(wraithId);
            }
            return;
        }

        double distance = wraith.distanceTo(target);

        //靠近状态（距离 > 6）：强制使用疾速突进
        if (distance > APPROACH_DISTANCE) {
            Skill dashSkill = getSkillByAnimation("swift_dash");
            if (dashSkill != null) {
                useSkill(dashSkill);
            }
            return;
        }

        //到达状态（距离 ≤ 6）：按权重选择其他技能
        boolean hasVoidEnchantment = wraith.hasEffect(ModEffects.VOID_ENCHANTING.get());

        Map<String, Double> skillWeights = new HashMap<>();

        //附魔技能：没有虚空附魔 Buff 时才能使用
        if (!hasVoidEnchantment) {
            skillWeights.put("enchant", 0.3);
        }
        skillWeights.put("double_strike", 0.5);
        skillWeights.put("cross_slash", 0.4);
        skillWeights.put("block", 0.2);
        skillWeights.put("moon_light_strike", 0.3);

        String selectedSkill = selectSkillByWeight(skillWeights);
        if (selectedSkill != null) {
            Skill skill = getSkillByAnimation(selectedSkill);
            if (skill != null) {
                useSkill(skill);
            }
        }
    }

    //按权重选择技能
    private String selectSkillByWeight(Map<String, Double> weights) {
        if (weights.isEmpty()) return null;

        double totalWeight = weights.values().stream().mapToDouble(Double::doubleValue).sum();
        double randomValue = Math.random() * totalWeight;
        double currentWeight = 0;

        for (Map.Entry<String, Double> entry : weights.entrySet()) {
            currentWeight += entry.getValue();
            if (randomValue <= currentWeight) {
                return entry.getKey();
            }
        }

        return weights.keySet().iterator().next();
    }

    //跟随主人逻辑
    private void followOwner(TheLastEndSwordWraithEntity wraith) {
        LivingEntity owner = wraith.getOwner();
        if (owner == null || !owner.isAlive()) {
            return;
        }

        double distance = wraith.distanceTo(owner);

        //超过16格立刻传送
        if (distance > 16.0) {
            Vec3 ownerPos = owner.position();
            wraith.teleportTo(ownerPos.x, ownerPos.y, ownerPos.z);
            return;
        }

        //保持2-4格距离
        if (distance > 4.0) {
            //导航到主人
            if (wraith.tickCount % 40 == 0) {
                wraith.getNavigation().moveTo(owner, 1.0);
            }
        } else if (distance < 2.0) {
            //距离太近，停止移动
            wraith.getNavigation().stop();
        }
    }

    //检查战斗传送距离并执行传送
    private boolean checkCombatTeleportDistance(TheLastEndSwordWraithEntity wraith) {
        LivingEntity owner = wraith.getOwner();
        if (owner == null || !owner.isAlive()) {
            return false;
        }

        double distanceToOwner = wraith.distanceTo(owner);
        double configDistance = TheLastSwordConfiguration.getWraithCombatTeleportDistanceSafely();

        //如果战斗中距离超过配置值
        if (distanceToOwner > configDistance) {
            //结束战斗状态
            wraith.targetList.clear();
            wraith.setTarget(null);

            //停止当前技能（需要访问基类的currentSkill）
            //由于currentSkill是基类的protected字段，这里通过isUsingSkill检查
            if (isUsingSkill()) {
                //强制结束技能
                entity.setSkillTick(0);
                entity.setAnimation(entity.getIdleAnimationName());
            }

            //传送到主人身边
            Vec3 ownerPos = owner.position();
            wraith.teleportTo(ownerPos.x, ownerPos.y, ownerPos.z);

            return true;
        }

        return false;
    }

    // ============ 内部技能类 ============

    //疾速突进技能
    private static class SwiftDashSkill extends Skill {
        private final TheLastEndSwordWraithEntity wraith;

        public SwiftDashSkill(TheLastEndSwordWraithEntity entity) {
            super(entity, "swift_dash", 60);
            this.wraith = entity;
        }

        @Override
        protected void defineKeyframes() {
            //35-55tick之间持续设置冲刺速度（共20tick = 1秒）
            for (int tick = 35; tick <= 55; tick++) {
                addKeyframe(tick, this::applyContinuousDash);
            }

            addKeyframe(55, () -> {
                executeFinalStrike();
            });

            //技能结束时检查距离，决定是否强制十字切
            addKeyframe(60, () -> {
                LivingEntity target = wraith.getTarget();
                if (target != null && target.isAlive()) {
                    double distance = wraith.distanceTo(target);
                    if (distance > DASH_SUCCESS_DISTANCE) {
                        //冲刺后距离仍 > 4 格，设置强制十字切标记
                        FORCE_CROSS_SLASH.put(wraith.getUUID(), true);
                    }
                }
            });
        }

        //持续冲刺逻辑（每tick调用）
        private void applyContinuousDash() {
            LivingEntity target = wraith.getTarget();
            if (target != null && target.isAlive()) {
                Vec3 wraithPos = wraith.position();
                Vec3 targetPos = target.position();

                //计算方向（三维）
                Vec3 direction = new Vec3(
                    targetPos.x - wraithPos.x,
                    targetPos.y - wraithPos.y,
                    targetPos.z - wraithPos.z
                ).normalize();

                double dashSpeed = 1.5;
                wraith.setDeltaMovement(
                    direction.x * dashSpeed,
                    Math.max(0.2, direction.y * dashSpeed),  //Y轴最小0.2，避免下坠
                    direction.z * dashSpeed
                );
            }
        }

        //执行突刺最后一击
        private void executeFinalStrike() {
            //从配置读取参数
            double attackRange = TheLastSwordConfiguration.getSkillSwiftDashRangeSafely();
            float damageMultiplier = (float) TheLastSwordConfiguration.getSkillSwiftDashDamageMultiplierSafely();

            //获取前方半圆范围内的目标
            List<LivingEntity> targets = EntityUtil.getTargetsInHemisphere(wraith, attackRange);

            //计算伤害
            float damage = (float) wraith.getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier;

            //对每个目标造成伤害
            for (LivingEntity target : targets) {
                target.invulnerableTime = 0;
                target.hurt(wraith.damageSources().mobAttack(wraith), damage);
            }

            //播放攻击音效
            wraith.level().playSound(
                null,
                wraith.getX(), wraith.getY(), wraith.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP,
                wraith.getSoundSource(),
                1.0F, 1.0F
            );
        }
    }

    //附魔技能
    private static class EnchantSkill extends Skill {
        private final TheLastEndSwordWraithEntity wraith;

        public EnchantSkill(TheLastEndSwordWraithEntity entity) {
            super(entity, "enchant", 45);
            this.wraith = entity;
        }

        @Override
        protected void defineKeyframes() {
            addKeyframe(25, () -> {
                applyVoidEnchantment();
                //播放附魔声音
                wraith.level().playSound(null, wraith.blockPosition(),
                    SoundEvents.ENCHANTMENT_TABLE_USE, wraith.getSoundSource(), 1.0F, 1.0F);
            });
        }

        private void applyVoidEnchantment() {
            if (wraith.level().isClientSide) return;

            //从配置读取持续时间
            int enchantDuration = TheLastSwordConfiguration.getSkillEnchantDurationSafely();

            int wraithLevel = wraith.getTheLastEndLevel();
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

    //双重打击技能
    private static class DoubleStrikeSkill extends Skill {
        private final TheLastEndSwordWraithEntity wraith;

        public DoubleStrikeSkill(TheLastEndSwordWraithEntity entity) {
            super(entity, "double_strike", 40);
            this.wraith = entity;
        }

        @Override
        protected void defineKeyframes() {
            addKeyframe(10, () -> {
                executeFirstStrike();
            });

            addKeyframe(20, () -> {
                executeSecondStrike();
            });
        }

        //第一击：物理伤害
        private void executeFirstStrike() {
            double attackRange = TheLastSwordConfiguration.getSkillDoubleStrikeRangeSafely();
            float damageMultiplier = (float) TheLastSwordConfiguration.getSkillDoubleStrikeDamageMultiplierSafely();

            //播放横扫声音
            wraith.level().playSound(null, wraith.getX(), wraith.getY(), wraith.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, wraith.getSoundSource(), 1.0F, 1.0F);

            //生成第一击横扫粒子（从左到右）
            ParticleUtil.spawnSweepParticles(wraith, true, attackRange);

            //范围攻击
            List<LivingEntity> targets = EntityUtil.getTargetsInHemisphere(wraith, attackRange);
            for (LivingEntity livingTarget : targets) {
                livingTarget.invulnerableTime = 0;
                float damage = (float) wraith.getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier;
                livingTarget.hurt(wraith.damageSources().mobAttack(wraith), damage);
            }
        }

        //第二击：魔法伤害
        private void executeSecondStrike() {
            double attackRange = TheLastSwordConfiguration.getSkillDoubleStrikeRangeSafely();
            float damageMultiplier = (float) TheLastSwordConfiguration.getSkillDoubleStrikeDamageMultiplierSafely();

            //播放横扫声音
            wraith.level().playSound(null, wraith.getX(), wraith.getY(), wraith.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, wraith.getSoundSource(), 1.0F, 1.0F);

            //生成第二击横扫粒子（从右到左）
            ParticleUtil.spawnSweepParticles(wraith, false, attackRange);

            //范围攻击
            List<LivingEntity> targets = EntityUtil.getTargetsInHemisphere(wraith, attackRange);
            for (LivingEntity livingTarget : targets) {
                livingTarget.invulnerableTime = 0;
                float damage = (float) wraith.getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier;
                //使用魔法伤害
                DamageSource magicDamage = new DamageSource(
                    wraith.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(DamageTypes.MAGIC),
                    wraith,
                    wraith
                );
                livingTarget.hurt(magicDamage, damage);
            }
        }
    }

    //十字斩技能
    private static class CrossSlashSkill extends Skill {
        private final TheLastEndSwordWraithEntity wraith;

        public CrossSlashSkill(TheLastEndSwordWraithEntity entity) {
            super(entity, "cross_slash", 80);
            this.wraith = entity;
        }

        @Override
        protected void defineKeyframes() {
            //播放铁砧声音
            addKeyframe(15, () -> {
                wraith.level().playSound(null, wraith.blockPosition(),
                    SoundEvents.ANVIL_USE, wraith.getSoundSource(), 1.0F, 1.0F);
            });

            //第一次传送攻击
            addKeyframe(55, () -> {
                executeSlash();
            });

            //第二次传送攻击
            addKeyframe(75, () -> {
                executeSlash();
            });
        }

        //执行十字切攻击：传送到当前目标位置并攻击
        private void executeSlash() {
            LivingEntity target = wraith.getTarget();

            //如果没有目标，直接返回
            if (target == null || !target.isAlive()) {
                return;
            }

            //1. 无条件传送到目标位置
            double targetX = target.getX();
            double targetY = target.getY();
            double targetZ = target.getZ();

            //播放传送声音
            wraith.level().playSound(null, wraith.getX(), wraith.getY(), wraith.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, wraith.getSoundSource(), 1.0F, 1.0F);

            //生成传送粒子效果（传送前）
            ParticleUtil.spawnCrossSlashTeleportParticles(wraith.level(),
                wraith.position(), wraith.getBbHeight());

            //执行传送（使用EntityUtil的传送方法）
            EntityUtil.theLastEndTeleport(wraith, targetX, targetY, targetZ);

            //生成传送粒子效果（传送后）
            ParticleUtil.spawnCrossSlashTeleportParticles(wraith.level(),
                new Vec3(targetX, targetY, targetZ), wraith.getBbHeight());

            //2. 从配置读取参数
            double attackRange = TheLastSwordConfiguration.getSkillCrossSlashRangeSafely();
            float damageMultiplier = (float) TheLastSwordConfiguration.getSkillCrossSlashDamageMultiplierSafely();

            //3. 攻击当前目标
            target.invulnerableTime = 0;
            float damage = (float) wraith.getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier;
            //使用绝对毁灭伤害（直接扣血）
            AbsoluteDestructionDamageSource.applyAbsoluteDestructionIntelligently(target, wraith, damage);

            //4. 攻击范围内的其他敌人（排除主目标避免重复伤害）
            List<LivingEntity> nearbyTargets = EntityUtil.getTargetsInHemisphere(wraith, attackRange);
            for (LivingEntity nearbyTarget : nearbyTargets) {
                if (!nearbyTarget.equals(target)) {
                    nearbyTarget.invulnerableTime = 0;
                    AbsoluteDestructionDamageSource.applyAbsoluteDestructionIntelligently(nearbyTarget, wraith, damage);
                }
            }
        }

    }

    //格挡技能
    private static class BlockSkill extends Skill {
        private final TheLastEndSwordWraithEntity wraith;
        private static final double KNOCKBACK_STRENGTH = 0.4; //击退强度

        public BlockSkill(TheLastEndSwordWraithEntity entity) {
            super(entity, "block", 15);  //0.75秒
            this.wraith = entity;
        }

        @Override
        protected void defineKeyframes() {
            //每tick执行击退、伤害和偏转（0-14 tick）
            for (int tick = 0; tick < 15; tick++) {
                addKeyframe(tick, () -> {
                    applyKnockbackAndDamage();
                    deflectProjectiles();
                });
            }
        }

        //对前方半圆范围内的目标施加击退和伤害
        private void applyKnockbackAndDamage() {
            //从配置读取参数
            double range = TheLastSwordConfiguration.getSkillBlockRangeSafely();
            float damageMultiplier = (float) TheLastSwordConfiguration.getSkillBlockDamageMultiplierSafely();

            List<LivingEntity> targets = EntityUtil.getTargetsInHemisphere(wraith, range);

            //计算伤害
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

                //造成伤害（每tick一次）
                target.invulnerableTime = 0;
                target.hurt(wraith.damageSources().mobAttack(wraith), damage);
            }
        }

        //偏转前方的弹射物
        private void deflectProjectiles() {
            //从配置读取范围
            double range = TheLastSwordConfiguration.getSkillBlockRangeSafely();

            AABB searchBox = new AABB(
                    wraith.getX() - range,
                    wraith.getY() - range,
                    wraith.getZ() - range,
                    wraith.getX() + range,
                    wraith.getY() + range,
                    wraith.getZ() + range
            );

            List<net.minecraft.world.entity.Entity> nearbyEntities = wraith.level().getEntities(wraith, searchBox);
            Vec3 wraithLook = wraith.getLookAngle();

            for (net.minecraft.world.entity.Entity entity : nearbyEntities) {
                if (entity instanceof net.minecraft.world.entity.projectile.Projectile projectile) {
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

    //月光斩技能
    private static class MoonLightStrikeSkill extends Skill {
        private final TheLastEndSwordWraithEntity wraith;
        private static final double LAUNCH_STRENGTH = 1.2; //向上击飞强度

        public MoonLightStrikeSkill(TheLastEndSwordWraithEntity entity) {
            super(entity, "moon_light_strike", 100);
            this.wraith = entity;
        }

        @Override
        protected void defineKeyframes() {
            addKeyframe(95, () -> {
                executeMoonLightStrike();
            });
        }

        private void executeMoonLightStrike() {
            //从配置读取参数
            double strikeRange = TheLastSwordConfiguration.getSkillMoonLightStrikeRangeSafely();
            float damageMultiplier = (float) TheLastSwordConfiguration.getSkillMoonLightStrikeDamageMultiplierSafely();

            //获取球体范围内的有效攻击目标
            List<LivingEntity> targets = EntityUtil.getTargetsInSphere(wraith, strikeRange);

            //计算伤害
            float damage = (float) wraith.getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier;

            //对每个目标造成伤害和击飞
            for (LivingEntity target : targets) {
                //造成物理伤害
                target.hurt(wraith.damageSources().mobAttack(wraith), damage);

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

    //万物终焉技能
    private static class EndOfAllThingsSkill extends Skill {
        private final TheLastEndSwordWraithEntity wraith;
        private static final int ACTIVATE_TICK = 170;  //激活万物终焉的时机

        public EndOfAllThingsSkill(TheLastEndSwordWraithEntity entity) {
            super(entity, "end_of_all_things", 190);
            this.wraith = entity;
        }

        @Override
        protected void defineKeyframes() {
            //170 tick激活万物终焉状态并启动持续效果
            addKeyframe(ACTIVATE_TICK, () -> {
                wraith.setAllThingsEnd(true);
                //启动13秒持续效果
                AllThingsEndActiveEffect.start(wraith);
            });
        }
    }

    // ══════════════════════════════════════════════════════════════════════════════════════════
    // All Things End Active Effect | 万物终焉持续效果
    // ══════════════════════════════════════════════════════════════════════════════════════════

    private static class AllThingsEndActiveEffect {
        private static final Map<UUID, Integer> ACTIVE_EFFECTS = new ConcurrentHashMap<>();
        private static final int DURATION = 260; // 13秒 (13 * 20 = 260 tick)
        private static final int SHRINK_START = 240; // 12秒后开始收缩

        //启动万物终焉持续效果
        public static void start(TheLastEndSwordWraithEntity wraith) {
            ACTIVE_EFFECTS.put(wraith.getUUID(), 0);
        }

        //处理万物终焉持续效果 - 每tick调用
        public static void handleTick(TheLastEndSwordWraithEntity wraith) {
            UUID id = wraith.getUUID();
            Integer currentTick = ACTIVE_EFFECTS.get(id);

            if (currentTick == null) {
                return; // 没有激活的万物终焉效果
            }

            int newTick = currentTick + 1;

            //每秒执行一次判定（20 tick = 1秒）
            if (newTick % 20 == 0) {
                executeAllThingsEndEffect(wraith);
            }

            //每4tick生成一次粒子
            if (newTick % 4 == 0) {
                spawnAllThingsEndParticles(wraith, newTick);
            }

            //检查是否超过持续时间
            if (newTick >= DURATION) {
                end(wraith);
            } else {
                ACTIVE_EFFECTS.put(id, newTick);
            }
        }

        //结束万物终焉持续效果
        public static void end(TheLastEndSwordWraithEntity wraith) {
            ACTIVE_EFFECTS.remove(wraith.getUUID());
            wraith.setAllThingsEnd(false);
        }

        //执行万物终焉效果：每秒对范围内的目标造成伤害并清空正面buff
        private static void executeAllThingsEndEffect(TheLastEndSwordWraithEntity wraith) {
            if (wraith.level().isClientSide) {
                return;
            }

            //从配置获取范围
            double effectRange = TheLastSwordConfiguration.getSkillEndOfAllThingsRangeSafely();
            List<LivingEntity> targets = EntityUtil.getTargetsInSphere(wraith, effectRange);

            //从配置获取伤害倍率
            double damageMultiplier = TheLastSwordConfiguration.getSkillEndOfAllThingsDamageMultiplierSafely();
            double maxHealthPercentage = TheLastSwordConfiguration.getSkillEndOfAllThingsMaxHealthPercentageSafely();

            for (LivingEntity target : targets) {
                applyAllThingsEndEffect(wraith, target, damageMultiplier, maxHealthPercentage);
            }
        }

        //对目标应用万物终焉效果
        private static void applyAllThingsEndEffect(TheLastEndSwordWraithEntity wraith, LivingEntity target,
                                                     double damageMultiplier, double maxHealthPercentage) {
            if (wraith.level().isClientSide) {
                return;
            }

            //1. 计算伤害 = 剑灵攻击力 × 倍率 + 目标最大生命值 × 百分比
            float baseAttackDamage = (float) wraith.getAttributeValue(Attributes.ATTACK_DAMAGE);
            float attackDamage = (float) (baseAttackDamage * damageMultiplier);
            float maxHealthDamage = (float) (target.getMaxHealth() * maxHealthPercentage);
            float totalDamage = attackDamage + maxHealthDamage;

            //2. 造成绝对毁灭伤害（使用绝毁API）
            target.invulnerableTime = 0;
            AbsoluteDestructionDamageSource.applyAbsoluteDestructionIntelligently(target, wraith, totalDamage);

            //3. 清空所有正面buff
            clearPositiveEffects(target);
        }

        //清空目标的所有正面buff
        private static void clearPositiveEffects(LivingEntity target) {
            var activeEffects = new java.util.ArrayList<>(target.getActiveEffects());

            for (MobEffectInstance effect : activeEffects) {
                //只移除正面效果（非负面效果）
                if (effect.getEffect().isBeneficial()) {
                    target.removeEffect(effect.getEffect());
                }
            }
        }

        //生成万物终焉粒子效果
        private static void spawnAllThingsEndParticles(TheLastEndSwordWraithEntity wraith, int currentTick) {
            //获取范围
            double radius = TheLastSwordConfiguration.getSkillEndOfAllThingsRangeSafely();
            Vec3 centerPos = wraith.position();

            if (currentTick < SHRINK_START) {
                //稳定期（0-240 tick）：显示稳定圆圈和温和中心粒子
                ParticleUtil.spawnAllThingsEndCircles(wraith.level(), centerPos, radius);
                ParticleUtil.spawnAllThingsEndCenterParticles(wraith.level(), centerPos, false);
            } else {
                //收缩期（240-260 tick）：显示收缩圆圈和强化中心粒子
                int shrinkTick = currentTick - SHRINK_START;
                double shrinkProgress = shrinkTick / 20.0; // 0.0 到 1.0
                ParticleUtil.spawnAllThingsEndShrinkingCircles(wraith.level(), centerPos, radius, shrinkProgress);
                ParticleUtil.spawnAllThingsEndCenterParticles(wraith.level(), centerPos, true);
            }
        }
    }
}
