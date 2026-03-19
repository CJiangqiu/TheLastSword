package net.the_last_sword.entity.ai;

import java.util.EnumSet;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.entity.GuardianArcherEntity;
import net.the_last_sword.entity.GuardianOfSealedSpireEntity;

//弓箭守卫距离控制Goal
public class GuardianArcherMaintainDistanceGoal extends Goal {
    private static final double MIN_DISTANCE = 4.0;
    private static final double MAX_DISTANCE = 16.0;
    private static final int REPATH_TICKS = 10;

    private final GuardianArcherEntity archer;

    public GuardianArcherMaintainDistanceGoal(GuardianArcherEntity archer) {
        this.archer = archer;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = archer.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (archer.getAnimationState() != GuardianOfSealedSpireEntity.STATE_IDLE) {
            return false;
        }
        double distance = archer.distanceTo(target);
        return distance < MIN_DISTANCE || distance > MAX_DISTANCE;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = archer.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (archer.getAnimationState() != GuardianOfSealedSpireEntity.STATE_IDLE) {
            return false;
        }
        double distance = archer.distanceTo(target);
        return distance < MIN_DISTANCE || distance > MAX_DISTANCE;
    }

    @Override
    public void tick() {
        LivingEntity target = archer.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        archer.getLookControl().setLookAt(target, 30.0F, 30.0F);

        double distance = archer.distanceTo(target);
        if (distance < MIN_DISTANCE) {
            retreatFrom(target);
        } else if (distance > MAX_DISTANCE) {
            approachTarget(target);
        } else {
            archer.getNavigation().stop();
        }
    }

    @Override
    public void stop() {
        archer.getNavigation().stop();
    }

    private void retreatFrom(LivingEntity target) {
        Vec3 archerPos = archer.position();
        Vec3 targetPos = target.position();
        Vec3 direction = archerPos.subtract(targetPos).normalize();

        double retreatX = archerPos.x + direction.x * 3;
        double retreatZ = archerPos.z + direction.z * 3;

        archer.getNavigation().moveTo(retreatX, archerPos.y, retreatZ, 1.2);
    }

    private void approachTarget(LivingEntity target) {
        if (archer.tickCount % REPATH_TICKS == 0 || !archer.getNavigation().isInProgress()) {
            archer.getNavigation().moveTo(target, 1.0);
        }
    }
}
