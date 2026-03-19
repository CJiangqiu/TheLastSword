package net.the_last_sword.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.entity.TheLastEndSwordWraithEntity;
import net.the_last_sword.util.EntityUtil;

import java.util.EnumSet;

//跟随主人Goal
public class SwordWraithFollowOwnerGoal extends Goal {
    private final TheLastEndSwordWraithEntity wraith;
    private static final double TELEPORT_DISTANCE = 16.0;  //超过此距离立即传送
    private static final double FOLLOW_DISTANCE = 4.0;     //保持此距离
    private static final double MIN_DISTANCE = 2.0;        //最小距离

    public SwordWraithFollowOwnerGoal(TheLastEndSwordWraithEntity wraith) {
        this.wraith = wraith;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        //只有在没有目标且主人存在时才跟随
        LivingEntity owner = wraith.getOwner();
        return wraith.getTarget() == null
            && owner != null
            && owner.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity owner = wraith.getOwner();
        return wraith.getTarget() == null
            && owner != null
            && owner.isAlive();
    }

    @Override
    public void tick() {
        LivingEntity owner = wraith.getOwner();
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

        //保持2-4格距离
        if (distance > FOLLOW_DISTANCE) {
            //导航到主人
            if (wraith.tickCount % 40 == 0) {
                wraith.getNavigation().moveTo(owner, 1.0);
            }
        } else if (distance < MIN_DISTANCE) {
            //距离太近，停止移动
            wraith.getNavigation().stop();
        }

        //看向主人
        wraith.getLookControl().setLookAt(owner, 10.0F, wraith.getMaxHeadXRot());
    }

    @Override
    public void stop() {
        wraith.getNavigation().stop();
    }
}
