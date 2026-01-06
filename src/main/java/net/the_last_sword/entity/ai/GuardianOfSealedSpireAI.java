package net.the_last_sword.entity.ai;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.the_last_sword.entity.GuardianOfSealedSpireEntity;
import net.the_last_sword.util.EntityUtil;

import java.util.Comparator;
import java.util.List;

//封印尖塔守卫AI
public class GuardianOfSealedSpireAI extends TheLastEndAI {

    //AI常量
    private static final double MAX_SEARCH_DISTANCE = 32.0;
    private static final double COMBAT_DISTANCE = 2.0;
    private static final double ITEM_PICKUP_DISTANCE = 8.0;
    private static final double ITEM_REACH_DISTANCE = 1.5;

    public GuardianOfSealedSpireAI(GuardianOfSealedSpireEntity entity) {
        super(entity);
    }

    @Override
    protected void registerSkills() {
        //注册攻击技能
        addSkill(new AttackSkill((GuardianOfSealedSpireEntity) entity));
    }

    @Override
    protected void aiTick() {
        GuardianOfSealedSpireEntity guardian = (GuardianOfSealedSpireEntity) entity;

        //基础守卫没有武器时，优先捡武器（即使有目标）
        if (guardian.getClass() == GuardianOfSealedSpireEntity.class &&
            guardian.getMainHandItem().isEmpty()) {
            handleItemPickup(guardian);
            return;
        }

        //目标管理（每20tick检查一次）
        if (guardian.tickCount % 20 == 0) {
            findAndUpdateTarget(guardian);
        }

        //检查是否有有效目标
        LivingEntity target = guardian.getTarget();
        if (!isValidTarget(target)) {
            guardian.setTarget(null);
            guardian.getNavigation().stop();
            return;
        }

        //导航和移动
        handleNavigation(guardian, target);

        //尝试攻击
        tryStartAttack(guardian, target);
    }

    //检查目标是否有效
    private boolean isValidTarget(LivingEntity target) {
        if (target == null || !target.isAlive() || target.isRemoved()) {
            return false;
        }
        //不攻击同类守卫
        if (target instanceof GuardianOfSealedSpireEntity) {
            return false;
        }
        if (target instanceof Player player) {
            return !player.isCreative() && !player.isSpectator();
        }
        return true;
    }

    //寻找并更新目标
    private void findAndUpdateTarget(GuardianOfSealedSpireEntity guardian) {
        LivingEntity currentTarget = guardian.getTarget();

        //验证当前目标
        if (!isValidTarget(currentTarget) ||
            (currentTarget != null && guardian.distanceTo(currentTarget) > MAX_SEARCH_DISTANCE)) {
            guardian.setTarget(null);
            currentTarget = null;
        }

        //反击攻击者
        LivingEntity attacker = guardian.getLastHurtByMob();
        if (isValidTarget(attacker) && guardian.distanceTo(attacker) <= MAX_SEARCH_DISTANCE) {
            guardian.setTarget(attacker);
            alertNearbyGuardians(guardian, attacker);
            return;
        }

        //响应附近守卫的求援
        if (currentTarget == null) {
            LivingEntity allyTarget = findAllyTarget(guardian);
            if (allyTarget != null) {
                guardian.setTarget(allyTarget);
                return;
            }
        }

        //寻找玩家目标
        if (currentTarget == null) {
            List<Player> nearbyPlayers = guardian.level().getEntitiesOfClass(
                Player.class,
                guardian.getBoundingBox().inflate(MAX_SEARCH_DISTANCE),
                player -> player.isAlive() && !player.isSpectator() && !player.isCreative()
            );

            if (!nearbyPlayers.isEmpty()) {
                Player closestPlayer = nearbyPlayers.stream()
                    .min(Comparator.comparingDouble(p -> p.distanceToSqr(guardian)))
                    .orElse(null);

                if (closestPlayer != null) {
                    guardian.setTarget(closestPlayer);
                }
            }
        }
    }

    //向周围守卫求援
    private void alertNearbyGuardians(GuardianOfSealedSpireEntity guardian, LivingEntity attacker) {
        List<GuardianOfSealedSpireEntity> nearbyGuardians = guardian.level().getEntitiesOfClass(
            GuardianOfSealedSpireEntity.class,
            guardian.getBoundingBox().inflate(MAX_SEARCH_DISTANCE),
            g -> g != guardian && g.isAlive() && !g.shouldLeave()
        );

        for (GuardianOfSealedSpireEntity ally : nearbyGuardians) {
            if (ally.getTarget() == null) {
                ally.setTarget(attacker);
            }
        }
    }

    //查找附近被攻击守卫的目标
    private LivingEntity findAllyTarget(GuardianOfSealedSpireEntity guardian) {
        List<GuardianOfSealedSpireEntity> nearbyGuardians = guardian.level().getEntitiesOfClass(
            GuardianOfSealedSpireEntity.class,
            guardian.getBoundingBox().inflate(MAX_SEARCH_DISTANCE),
            g -> g != guardian && g.isAlive() && !g.shouldLeave() && g.getTarget() != null
        );

        for (GuardianOfSealedSpireEntity ally : nearbyGuardians) {
            LivingEntity allyTarget = ally.getTarget();
            if (isValidTarget(allyTarget) && guardian.distanceTo(allyTarget) <= MAX_SEARCH_DISTANCE) {
                return allyTarget;
            }
        }
        return null;
    }

    //导航处理
    private void handleNavigation(GuardianOfSealedSpireEntity guardian, LivingEntity target) {
        guardian.getLookControl().setLookAt(target, 30.0F, 30.0F);

        double distance = guardian.distanceTo(target);
        if (distance > COMBAT_DISTANCE) {
            if (!guardian.getNavigation().isInProgress()) {
                guardian.getNavigation().moveTo(target, 1.0);
            }
        } else {
            guardian.getNavigation().stop();
        }
    }

    //尝试开始攻击
    private void tryStartAttack(GuardianOfSealedSpireEntity guardian, LivingEntity target) {
        double distance = guardian.distanceTo(target);
        if (distance <= COMBAT_DISTANCE + 0.5) {
            //使用攻击技能
            Skill attackSkill = getSkillByAnimation("attack");
            if (attackSkill != null) {
                useSkill(attackSkill);
            }
        }
    }

    //物品拾取处理
    private void handleItemPickup(GuardianOfSealedSpireEntity guardian) {
        //只有基类才拾取物品
        if (guardian.getClass() != GuardianOfSealedSpireEntity.class) {
            return;
        }

        //每20tick检测一次
        if (guardian.tickCount % 20 != 0) return;

        //查找周围有效物品
        ItemEntity targetItem = findNearbyValidItem(guardian);
        if (targetItem == null) {
            return;
        }

        //导航到物品位置
        double distance = guardian.distanceTo(targetItem);
        if (distance > ITEM_REACH_DISTANCE) {
            guardian.getNavigation().moveTo(targetItem, 1.0);
        } else {
            //捡起物品
            pickupItem(guardian, targetItem);
        }
    }

    //查找周围有效的武器物品
    private ItemEntity findNearbyValidItem(GuardianOfSealedSpireEntity guardian) {
        List<ItemEntity> nearbyItems = guardian.level().getEntitiesOfClass(
            ItemEntity.class,
            guardian.getBoundingBox().inflate(ITEM_PICKUP_DISTANCE),
            item -> item.isAlive() && isValidWeaponItem(item.getItem())
        );

        if (nearbyItems.isEmpty()) {
            return null;
        }

        return nearbyItems.stream()
            .min(Comparator.comparingDouble(item -> item.distanceToSqr(guardian)))
            .orElse(null);
    }

    //检查是否是有效的武器物品
    private boolean isValidWeaponItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem() instanceof SwordItem ||
               stack.getItem() instanceof BowItem ||
               stack.getItem() instanceof AxeItem;
    }

    //捡起物品
    private void pickupItem(GuardianOfSealedSpireEntity guardian, ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        guardian.setItemSlot(EquipmentSlot.MAINHAND, stack.copy());
        itemEntity.discard();
        guardian.getNavigation().stop();
    }

    // ============ 内部技能类 ============

    //攻击技能
    private static class AttackSkill extends Skill {
        private static final int ATTACK_DURATION = 25;  //1.25秒
        private static final int ATTACK_DAMAGE_TICK = 15;  //0.75秒时造成伤害
        private static final double ATTACK_RANGE = 3.0;

        public AttackSkill(GuardianOfSealedSpireEntity entity) {
            super(entity, "attack", ATTACK_DURATION);
        }

        @Override
        protected void defineKeyframes() {
            //0.75秒时造成伤害
            addKeyframe(ATTACK_DAMAGE_TICK, () -> {
                GuardianOfSealedSpireEntity guardian = (GuardianOfSealedSpireEntity) entity;
                LivingEntity target = guardian.getTarget();

                if (target != null && target.isAlive()) {
                    double distance = guardian.distanceTo(target);
                    if (distance <= ATTACK_RANGE) {
                        //面向目标
                        EntityUtil.faceTarget(guardian, target);

                        //计算伤害
                        float damage = (float) guardian.getAttributeValue(Attributes.ATTACK_DAMAGE);

                        //创建伤害源
                        DamageSource damageSource = new DamageSource(
                            guardian.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                                .getHolderOrThrow(DamageTypes.MOB_ATTACK),
                            guardian, guardian);

                        //造成伤害
                        target.hurt(damageSource, damage);
                    }
                }
            });
        }
    }
}
