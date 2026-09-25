package net.the_last_sword.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.entity.TheLastEndSwordWraithEntity;
import net.the_last_sword.util.EntityUtil;

import java.util.EnumSet;

//跟随主人Goal
public class SwordWraithFollowOwnerGoal extends Goal {
    private final TheLastEndSwordWraithEntity wraith;
    private LivingEntity owner;
    private int pathRecalculationDelay;
    private float oldWaterCost;
    private static final double TELEPORT_DISTANCE = 16.0;  //超过此距离立即传送
    private static final double FOLLOW_DISTANCE = 4.0;     //保持此距离
    private static final double MIN_DISTANCE = 2.0;        //最小距离
    private static final double SPEED_MODIFIER = 1.2;

    public SwordWraithFollowOwnerGoal(TheLastEndSwordWraithEntity wraith) {
        this.wraith = wraith;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity currentOwner = wraith.getOwner();
        if (wraith.getTarget() != null || currentOwner == null || !currentOwner.isAlive()
                || currentOwner.isSpectator() || wraith.isPassenger()) {
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
        return wraith.getTarget() == null
                && owner != null
                && owner.isAlive()
                && !owner.isSpectator()
                && !wraith.isPassenger()
                && wraith.distanceToSqr(owner) > MIN_DISTANCE * MIN_DISTANCE;
    }

    @Override
    public void start() {
        pathRecalculationDelay = 0;
        oldWaterCost = wraith.getPathfindingMalus(BlockPathTypes.WATER);
        wraith.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
    }

    @Override
    public void tick() {
        if (owner == null || !owner.isAlive()) {
            return;
        }

        double distance = wraith.distanceTo(owner);

        //超过16格立刻传送
        if (distance > TELEPORT_DISTANCE) {
            Vec3 ownerPos = owner.position();
            EntityUtil.theLastEndTeleport(wraith, ownerPos.x, ownerPos.y, ownerPos.z);
            return;
        }

        if (--pathRecalculationDelay <= 0) {
            pathRecalculationDelay = adjustedTickDelay(10);
            wraith.getNavigation().moveTo(owner, SPEED_MODIFIER);
        }

        wraith.getLookControl().setLookAt(owner, 10.0F, wraith.getMaxHeadXRot());
    }

    @Override
    public void stop() {
        owner = null;
        wraith.getNavigation().stop();
        wraith.setPathfindingMalus(BlockPathTypes.WATER, oldWaterCost);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
