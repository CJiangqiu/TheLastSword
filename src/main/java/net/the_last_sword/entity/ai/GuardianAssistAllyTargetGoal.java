package net.the_last_sword.entity.ai;

import java.util.List;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.GuardianOfSealedSpireEntity;
import net.the_last_sword.entity.LostWraithEntity;
import net.the_last_sword.util.EntityUtil;

// 守卫协同锁定目标Goal
public class GuardianAssistAllyTargetGoal extends TargetGoal {
    private final GuardianOfSealedSpireEntity guardian;
    private LivingEntity allyTarget;

    public GuardianAssistAllyTargetGoal(GuardianOfSealedSpireEntity guardian) {
        super(guardian, false);
        this.guardian = guardian;
    }

    @Override
    public boolean canUse() {
        LivingEntity currentTarget = guardian.getTarget();
        if (isValidTarget(currentTarget)) {
            return false;
        }

        allyTarget = findAllyTarget();
        return allyTarget != null;
    }

    @Override
    public void start() {
        guardian.setTarget(allyTarget);
        super.start();
    }

    private LivingEntity findAllyTarget() {
        double maxSearchDistance = TheLastSwordConfiguration.getGuardianAssistAllyMaxSearchDistanceSafely();
        List<GuardianOfSealedSpireEntity> nearbyGuardians = guardian.level().getEntitiesOfClass(
            GuardianOfSealedSpireEntity.class,
            guardian.getBoundingBox().inflate(maxSearchDistance),
            ally -> ally != guardian && ally.isAlive() && !ally.isDying() && ally.getTarget() != null
        );

        LivingEntity nearestTarget = null;
        double nearestDistance = Double.MAX_VALUE;

        for (GuardianOfSealedSpireEntity ally : nearbyGuardians) {
            LivingEntity target = ally.getTarget();
            if (!isValidTarget(target)) {
                continue;
            }
            double distance = guardian.distanceTo(target);
            if (distance <= maxSearchDistance && distance < nearestDistance) {
                nearestDistance = distance;
                nearestTarget = target;
            }
        }

        return nearestTarget;
    }

    private boolean isValidTarget(LivingEntity target) {
        if (target == null || !target.isAlive() || target.isRemoved()) {
            return false;
        }
        // 不攻击同类守卫
        if (target instanceof GuardianOfSealedSpireEntity) {
            return false;
        }
        // 玩家检查
        if (target instanceof Player player) {
            return !player.isCreative() && !player.isSpectator();
        }
        // 迷失战魂是有效目标
        if (target instanceof LostWraithEntity) {
            return true;
        }
        return EntityUtil.canAttack(guardian, target);
    }
}
