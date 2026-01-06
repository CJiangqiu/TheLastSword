package net.the_last_sword.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.damagesource.AbsoluteDestructionDamageSource;
import net.the_last_sword.entity.DragonLightingEntity;
import net.the_last_sword.entity.LostWraithEntity;
import net.the_last_sword.init.ModEffects;
import net.the_last_sword.init.ModEntities;
import net.the_last_sword.init.ModSounds;
import net.the_last_sword.util.EntityUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//迷失战魂AI系统
public class LostWraithAI extends TheLastEndAI {

    //AI常量
    private static final double MAX_SEARCH_DISTANCE = 32.0;
    private static final double COMBAT_DISTANCE = 4.0;  //战斗距离，≤4格时停止移动

    //耐心机制常量
    private static final double PATIENCE_DISTANCE = 4.0;   //超过此距离开始计时
    private static final int MAX_PATIENCE_TICKS = 240;     //12秒后强制技能

    //耐心机制数据
    private int patienceTicks = 0;
    private boolean forceEndStrike = false;

    //闪电技能目标位置记录
    private Vec3 lightningTargetPosition;

    //终焉一击拉拽数据
    private final List<TargetPositionData> pullTargets = new ArrayList<>();

    public LostWraithAI(LostWraithEntity entity) {
        super(entity);
    }

    @Override
    protected void registerSkills() {
        //注册5个技能
        addSkill(new EnchantSkill((LostWraithEntity) entity));
        addSkill(new PunchSkill((LostWraithEntity) entity));
        addSkill(new DragonFireBallSkill((LostWraithEntity) entity));
        addSkill(new SummonLightningSkill((LostWraithEntity) entity));
        addSkill(new EndStrikeSkill((LostWraithEntity) entity));
    }

    @Override
    protected void aiTick() {
        LostWraithEntity wraith = (LostWraithEntity) entity;

        //未激活时不执行AI
        if (!wraith.isSpawned()) {
            return;
        }

        //目标管理（每20tick检查一次）
        if (wraith.tickCount % 20 == 0) {
            findAndUpdateTarget(wraith);
        }

        //检查是否有有效目标
        LivingEntity target = wraith.getTarget();
        if (!isValidTarget(target)) {
            wraith.setTarget(null);
            wraith.getNavigation().stop();
            resetPatienceTracking();
            return;
        }

        //更新耐心机制
        updatePatienceTracking(wraith, target);

        //检查是否需要强制使用终焉一击
        if (patienceTicks >= MAX_PATIENCE_TICKS && !isUsingSkill()) {
            forceEndStrike = true;
            resetPatienceTracking();
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
        LostWraithEntity wraith = (LostWraithEntity) entity;
        if (wraith.distanceTo(target) > MAX_SEARCH_DISTANCE) {
            return false;
        }
        if (target instanceof Player player) {
            return !player.isCreative() && !player.isSpectator();
        }
        return true;
    }

    //寻找并更新目标
    private void findAndUpdateTarget(LostWraithEntity wraith) {
        LivingEntity currentTarget = wraith.getTarget();

        //验证当前目标
        if (!isValidTarget(currentTarget)) {
            wraith.setTarget(null);
            currentTarget = null;
        }

        //反击攻击者
        LivingEntity attacker = wraith.getLastHurtByMob();
        if (isValidTarget(attacker)) {
            wraith.setTarget(attacker);
            return;
        }

        //寻找怪物目标
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
    private void handleNavigation(LostWraithEntity wraith, LivingEntity target) {
        wraith.getLookControl().setLookAt(target, 30.0F, 30.0F);

        double distance = wraith.distanceTo(target);
        if (distance > COMBAT_DISTANCE) {
            //距离 > 4，继续靠近
            if (!wraith.getNavigation().isInProgress()) {
                wraith.getNavigation().moveTo(target, 1.0);
            }
        } else {
            //距离 ≤ 4，停止移动
            wraith.getNavigation().stop();
        }
    }

    //耐心机制更新
    private void updatePatienceTracking(LostWraithEntity wraith, LivingEntity target) {
        double currentDistance = wraith.distanceTo(target);

        //距离 > 4 格时累计耐心计时
        if (currentDistance > PATIENCE_DISTANCE) {
            patienceTicks++;
        } else {
            //距离 <= 4 格，重置耐心计时
            patienceTicks = 0;
        }
    }

    //重置耐心追踪
    private void resetPatienceTracking() {
        patienceTicks = 0;
    }

    //尝试开始新技能
    private void tryStartNewSkill(LostWraithEntity wraith, LivingEntity target) {
        //检查是否有强制终焉一击标记
        if (forceEndStrike) {
            Skill endStrike = getSkillByAnimation("end_strike");
            if (endStrike != null) {
                useSkill(endStrike);
            }
            forceEndStrike = false;
            return;
        }

        boolean hasVoidEnchantment = wraith.hasEffect(ModEffects.VOID_ENCHANTING.get());

        Map<String, Double> skillWeights = new HashMap<>();

        if (!hasVoidEnchantment) {
            skillWeights.put("enchant", 0.5);
        }
        skillWeights.put("punch", 0.5);
        skillWeights.put("dragon_fire_ball", 0.3);
        skillWeights.put("summon_lightning", 0.3);
        skillWeights.put("end_strike", 0.1);

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

    // ============ 工具方法 ============

    //传送到目标周围随机位置
    private boolean teleportToTarget(LostWraithEntity wraith, LivingEntity target) {
        if (target == null) return false;

        Vec3 teleportPos = findRandomPositionAroundTarget(wraith, target);
        if (teleportPos == null) return false;

        Vec3 oldPosition = wraith.position();

        //播放传送离开效果
        if (wraith.level() instanceof ServerLevel serverLevel) {
            spawnTeleportParticles(serverLevel, oldPosition);
            wraith.level().playSound(null, BlockPos.containing(oldPosition),
                SoundEvents.ENDERMAN_TELEPORT, wraith.getSoundSource(), 1.0F, 1.0F);
        }

        //执行传送
        EntityUtil.theLastEndTeleport(wraith, teleportPos.x, teleportPos.y, teleportPos.z);

        //面向目标
        EntityUtil.faceTarget(wraith, target);

        //播放传送到达效果
        if (wraith.level() instanceof ServerLevel serverLevel) {
            spawnTeleportParticles(serverLevel, teleportPos);
            wraith.level().playSound(null, BlockPos.containing(teleportPos),
                SoundEvents.ENDERMAN_TELEPORT, wraith.getSoundSource(), 1.0F, 1.0F);
        }

        return true;
    }

    //在目标周围随机1格位置传送
    private Vec3 findRandomPositionAroundTarget(LostWraithEntity wraith, LivingEntity target) {
        Vec3 targetPos = target.position();

        //随机角度
        double angle = wraith.getRandom().nextDouble() * 2 * Math.PI;
        double distance = 1.0;

        double x = targetPos.x + Math.cos(angle) * distance;
        double y = targetPos.y;
        double z = targetPos.z + Math.sin(angle) * distance;

        //检查是否安全，不安全就传送到目标位置
        if (isSafeTeleportPosition(wraith, x, y, z)) {
            return new Vec3(x, y, z);
        }

        return new Vec3(targetPos.x, targetPos.y, targetPos.z);
    }

    //检查传送位置是否安全
    private boolean isSafeTeleportPosition(LostWraithEntity wraith, double x, double y, double z) {
        BlockPos pos = BlockPos.containing(x, y, z);
        BlockPos posAbove = pos.above();

        //简单检查：位置和上方一格不能是固体方块
        return !wraith.level().getBlockState(pos).isSolid() &&
               !wraith.level().getBlockState(posAbove).isSolid();
    }

    //传送粒子效果
    private void spawnTeleportParticles(ServerLevel serverLevel, Vec3 position) {
        for (int i = 0; i < 32; i++) {
            double d0 = serverLevel.random.nextGaussian() * 0.02D;
            double d1 = serverLevel.random.nextGaussian() * 0.02D;
            double d2 = serverLevel.random.nextGaussian() * 0.02D;

            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                position.x + (serverLevel.random.nextDouble() - 0.5D) * 2.0D,
                position.y + serverLevel.random.nextDouble() * 2.0D,
                position.z + (serverLevel.random.nextDouble() - 0.5D) * 2.0D,
                1, d0, d1, d2, 0.1D);
        }
    }

    //检查目标是否在格挡
    private boolean isTargetBlocking(LivingEntity target, LivingEntity attacker) {
        if (!(target instanceof Player player)) {
            return false;
        }

        if (!target.isUsingItem()) {
            return false;
        }

        var useItem = target.getUseItem();
        boolean canBlock = false;

        if (useItem.is(net.minecraft.world.item.Items.SHIELD)) {
            canBlock = player.getCooldowns().getCooldownPercent(useItem.getItem(), 0.0f) == 0.0f;
        } else if (useItem.getUseAnimation() == UseAnim.BLOCK) {
            canBlock = player.getCooldowns().getCooldownPercent(useItem.getItem(), 0.0f) == 0.0f;
        }

        if (!canBlock) {
            return false;
        }

        Vec3 toAttacker = attacker.position().subtract(target.position()).normalize();
        Vec3 targetLook = target.getLookAngle();
        double dotProduct = targetLook.dot(toAttacker);

        return dotProduct > 0;
    }

    // ============ 内部技能类 ============

    //附魔技能 - 给自己添加虚空附魔
    private class EnchantSkill extends Skill {
        private final LostWraithEntity wraith;

        public EnchantSkill(LostWraithEntity entity) {
            super(entity, "enchant", 50);
            this.wraith = entity;
        }

        @Override
        protected void defineKeyframes() {
            addKeyframe(50, () -> {
                MobEffectInstance voidEnchant = new MobEffectInstance(
                    ModEffects.VOID_ENCHANTING.get(),
                    1200, 0, false, false
                );
                wraith.addEffect(voidEnchant);

                //播放附魔音效
                wraith.level().playSound(
                    null,
                    wraith.getX(), wraith.getY(), wraith.getZ(),
                    SoundEvents.ENCHANTMENT_TABLE_USE,
                    wraith.getSoundSource(),
                    1.0F, 1.0F
                );

                if (wraith.level() instanceof ServerLevel serverLevel) {
                    for (int i = 0; i < 20; i++) {
                        serverLevel.sendParticles(ParticleTypes.ENCHANT,
                            wraith.getX() + (wraith.getRandom().nextDouble() - 0.5) * 2,
                            wraith.getY() + wraith.getRandom().nextDouble() * 2,
                            wraith.getZ() + (wraith.getRandom().nextDouble() - 0.5) * 2,
                            1, 0, 0.1, 0, 1.0);
                    }
                }
            });
        }
    }

    //拳击技能 - 传送到目标面前攻击
    private class PunchSkill extends Skill {
        private final LostWraithEntity wraith;

        public PunchSkill(LostWraithEntity entity) {
            super(entity, "punch", 30);
            this.wraith = entity;
        }

        @Override
        protected void defineKeyframes() {
            addKeyframe(5, () -> {
                LivingEntity target = wraith.getTarget();
                if (target != null) {
                    teleportToTarget(wraith, target);
                }
            });

            addKeyframe(10, () -> {
                wraith.level().playSound(
                    null,
                    wraith.getX(), wraith.getY(), wraith.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP,
                    wraith.getSoundSource(),
                    1.0F, 1.0F
                );
            });

            addKeyframe(24, () -> {
                executePunchDamage();
            });
        }

        private void executePunchDamage() {
            Vec3 forward = wraith.getLookAngle();
            Vec3 pos = wraith.position();
            float damage = (float) wraith.getAttributeValue(Attributes.ATTACK_DAMAGE);

            DamageSource damageSource = new DamageSource(
                wraith.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                    .getHolderOrThrow(DamageTypes.GENERIC),
                wraith, wraith);

            for (int i = 1; i <= 4; i++) {
                for (int j = -1; j <= 1; j++) {
                    Vec3 right = forward.cross(new Vec3(0, 1, 0)).normalize();
                    Vec3 checkPos = pos.add(forward.scale(i)).add(right.scale(j));

                    AABB area = new AABB(checkPos.subtract(0.5, 0.5, 0.5), checkPos.add(0.5, 0.5, 0.5));
                    List<LivingEntity> targets = wraith.level().getEntitiesOfClass(
                        LivingEntity.class, area,
                        entity1 -> EntityUtil.canAttack(wraith, entity1)
                    );

                    for (LivingEntity target : targets) {
                        if (isTargetBlocking(target, wraith)) {
                            target.level().playSound(null, target.blockPosition(),
                                SoundEvents.SHIELD_BLOCK, target.getSoundSource(),
                                1.0F, 0.8F + target.level().random.nextFloat() * 0.4F);
                            Vec3 knockbackDir = target.position().subtract(wraith.position()).normalize();
                            target.knockback(0.5F, knockbackDir.x, knockbackDir.z);
                        } else {
                            target.hurt(damageSource, damage);
                        }
                    }
                }
            }
        }
    }

    //龙息弹技能
    private class DragonFireBallSkill extends Skill {
        private final LostWraithEntity wraith;

        public DragonFireBallSkill(LostWraithEntity entity) {
            super(entity, "dragon_fire_ball", 60);
            this.wraith = entity;
        }

        @Override
        protected void defineKeyframes() {
            addKeyframe(40, () -> {
                if (wraith.level() instanceof ServerLevel) {
                    LivingEntity target = wraith.getTarget();
                    if (target != null) {
                        Vec3 targetPos = target.position().add(0, target.getEyeHeight() * 0.5, 0);
                        Vec3 startPos = wraith.position().add(0, wraith.getEyeHeight(), 0);
                        Vec3 direction = targetPos.subtract(startPos).normalize();

                        DragonFireball dragonFireball = new DragonFireball(
                            wraith.level(), wraith,
                            direction.x, direction.y, direction.z
                        );
                        dragonFireball.setPos(startPos.x, startPos.y, startPos.z);
                        wraith.level().addFreshEntity(dragonFireball);

                        //播放末影龙咆哮和射击音效
                        wraith.level().playSound(null, wraith.blockPosition(),
                            SoundEvents.ENDER_DRAGON_GROWL, wraith.getSoundSource(), 1.0F, 1.0F);
                        wraith.level().playSound(null, wraith.blockPosition(),
                            SoundEvents.ENDER_DRAGON_SHOOT, wraith.getSoundSource(), 1.0F, 1.0F);
                    }
                }
            });
        }
    }

    //召唤闪电技能
    private class SummonLightningSkill extends Skill {
        private final LostWraithEntity wraith;

        public SummonLightningSkill(LostWraithEntity entity) {
            super(entity, "summon_lightning", 60);
            this.wraith = entity;
        }

        @Override
        protected void defineKeyframes() {
            addKeyframe(25, () -> {
                LivingEntity target = wraith.getTarget();
                lightningTargetPosition = target != null ? target.position() : wraith.position();
            });

            addKeyframe(40, () -> {
                if (wraith.level() instanceof ServerLevel serverLevel) {
                    Vec3 targetPos = lightningTargetPosition != null ? lightningTargetPosition : wraith.position();

                    DragonLightingEntity lightning = new DragonLightingEntity(
                        ModEntities.DRAGON_LIGHTING.get(), serverLevel
                    );
                    lightning.moveTo(targetPos.x, targetPos.y, targetPos.z);
                    serverLevel.addFreshEntity(lightning);

                    AABB area = new AABB(targetPos.subtract(3, 3, 3), targetPos.add(3, 3, 3));
                    List<LivingEntity> targets = wraith.level().getEntitiesOfClass(
                        LivingEntity.class, area,
                        entity1 -> EntityUtil.canAttack(wraith, entity1)
                    );

                    float damage = (float) wraith.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2.0f;
                    DamageSource damageSource = new DamageSource(
                        wraith.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                            .getHolderOrThrow(DamageTypes.LIGHTNING_BOLT),
                        wraith, wraith);

                    for (LivingEntity target : targets) {
                        target.hurt(damageSource, damage);
                    }

                    lightningTargetPosition = null;
                }
            });
        }
    }

    //终焉一击技能 - 拉拽并造成绝毁伤害
    private class EndStrikeSkill extends Skill {
        private final LostWraithEntity wraith;

        public EndStrikeSkill(LostWraithEntity entity) {
            super(entity, "end_strike", 80);
            this.wraith = entity;
        }

        @Override
        protected void defineKeyframes() {
            addKeyframe(5, () -> {
                LivingEntity target = wraith.getTarget();
                if (target != null) {
                    teleportToTarget(wraith, target);
                }
            });

            addKeyframe(10, () -> {
                wraith.level().playSound(null, wraith.blockPosition(),
                    ModSounds.ALARM.get(), wraith.getSoundSource(), 0.5F, 1.0F);
            });

            addKeyframe(30, () -> {
                Vec3 forward = wraith.getLookAngle();
                Vec3 centerPos = wraith.position().add(forward.scale(2));

                AABB area = new AABB(centerPos.subtract(2, 2, 2), centerPos.add(2, 2, 2));
                List<LivingEntity> targets = wraith.level().getEntitiesOfClass(
                    LivingEntity.class, area,
                    entity1 -> EntityUtil.canAttack(wraith, entity1)
                );

                pullTargets.clear();
                Vec3 pullTarget = wraith.position().add(forward.scale(1));

                for (LivingEntity target : targets) {
                    pullTargets.add(new TargetPositionData(target, pullTarget));
                }
            });

            //拉拽期间每tick更新
            for (int tick = 31; tick < 70; tick++) {
                addKeyframe(tick, () -> {
                    pullTargets.removeIf(data -> !data.target.isAlive());
                    for (TargetPositionData data : pullTargets) {
                        data.target.teleportTo(data.position.x, data.position.y, data.position.z);
                    }
                });
            }

            addKeyframe(70, () -> {
                float lostHealth = wraith.getTheLastEndMaxHealth() - wraith.getTheLastEndHealth();
                float damage = lostHealth * 0.1f;

                for (TargetPositionData data : pullTargets) {
                    if (data.target.isAlive()) {
                        if (isTargetBlocking(data.target, wraith)) {
                            data.target.level().playSound(null, data.target.blockPosition(),
                                SoundEvents.SHIELD_BLOCK, data.target.getSoundSource(),
                                1.0F, 0.8F + data.target.level().random.nextFloat() * 0.4F);

                            if (data.target instanceof Player player) {
                                player.getCooldowns().addCooldown(data.target.getUseItem().getItem(), 260);
                            }

                            Vec3 knockback = wraith.position().subtract(data.target.position()).normalize().scale(-0.3);
                            data.target.setDeltaMovement(data.target.getDeltaMovement().add(knockback));
                        } else {
                            AbsoluteDestructionDamageSource.applyAbsoluteDestructionIntelligently(data.target, wraith, damage);
                        }
                    }
                }
                pullTargets.clear();
            });
        }
    }

    // ============ 数据类 ============

    private static class TargetPositionData {
        public final LivingEntity target;
        public final Vec3 position;

        public TargetPositionData(LivingEntity target, Vec3 position) {
            this.target = target;
            this.position = position;
        }
    }
}
