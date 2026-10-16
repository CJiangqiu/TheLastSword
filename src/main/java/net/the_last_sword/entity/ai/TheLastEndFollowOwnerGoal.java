package net.the_last_sword.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.entity.TheLastEndEntity;
import net.the_last_sword.util.EntityUtil;

import java.util.EnumSet;

//跟随主人Goal
public class TheLastEndFollowOwnerGoal extends Goal {
    private final TheLastEndEntity wraith;
    private LivingEntity owner;
    private int pathRecalculationDelay;
    private float oldWaterCost;
    private static final double TELEPORT_DISTANCE = 16.0;
    private static final double FOLLOW_DISTANCE = 4.0;     //保持此距离
    private static final double MIN_DISTANCE = 2.0;        //最小距离
    private static final double SPEED_MODIFIER = 1.2;

    public TheLastEndFollowOwnerGoal(TheLastEndEntity wraith) {
        this.wraith = wraith;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!canFollow()) return false;
        LivingEntity currentOwner = wraith.getOwner();
        if (currentOwner == null || !currentOwner.isAlive() || currentOwner.isSpectator()
                || currentOwner.level() != wraith.level()) {
            return false;
        }
        if (wraith.distanceToSqr(currentOwner) <= FOLLOW_DISTANCE * FOLLOW_DISTANCE) {
            return false;
        }
        owner = currentOwner;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return canFollow()
                && owner != null
                && owner.isAlive()
                && !owner.isSpectator()
                && owner.level() == wraith.level()
                && owner.getUUID().equals(wraith.getOwnerUUID())
                && wraith.distanceToSqr(owner) > MIN_DISTANCE * MIN_DISTANCE;
    }

    private boolean canFollow() {
        return wraith.isTame() && wraith.isAlive() && wraith.canAct()
                && wraith.getAnimationState() == TheLastEndEntity.STATE_IDLE
                && wraith.getTarget() == null && !wraith.isOrderedToSit()
                && !wraith.isPassenger() && !wraith.isLeashed();
    }

    @Override
    public void start() {
        pathRecalculationDelay = 0;
        oldWaterCost = wraith.getPathfindingMalus(BlockPathTypes.WATER);
        wraith.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
    }

    @Override
    public void tick() {
        if (!canContinueToUse()) {
            return;
        }

        if (--pathRecalculationDelay <= 0) {
            pathRecalculationDelay = adjustedTickDelay(10);
            if (wraith.distanceToSqr(owner) > TELEPORT_DISTANCE * TELEPORT_DISTANCE) {
                teleportNearOwner();
            } else {
                wraith.getNavigation().moveTo(owner, SPEED_MODIFIER);
            }
        }

        wraith.getLookControl().setLookAt(owner, 10.0F, wraith.getMaxHeadXRot());
    }

    @Override
    public void stop() {
        owner = null;
        wraith.getNavigation().stop();
        wraith.setPathfindingMalus(BlockPathTypes.WATER, oldWaterCost);
    }

    private void teleportNearOwner() {
        // 大体型实体需要按自身碰撞箱寻找落点，失败后等待下一次寻路周期。
        for (int attempt = 0; attempt < 10; attempt++) {
            int x = wraith.getRandom().nextInt(7) - 3;
            int z = wraith.getRandom().nextInt(7) - 3;
            if (Math.abs(x) < 2 && Math.abs(z) < 2) continue;
            Vec3 desired = owner.position().add(x, 0, z);
            if (EntityUtil.theLastEndSafeTeleport(wraith, desired, 2) != null) {
                wraith.getNavigation().stop();
                wraith.setDeltaMovement(Vec3.ZERO);
                wraith.fallDistance = 0;
                return;
            }
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
