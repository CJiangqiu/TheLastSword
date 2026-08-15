package net.the_last_sword.entity.ai;

import java.util.EnumSet;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.GuardianArcherEntity;

//弓箭手守卫瞬移Goal：目标贴近时瞬移脱离，之后进入冷却
public class GuardianArcherTeleportGoal extends Goal {

    private final GuardianArcherEntity archer;

    public GuardianArcherTeleportGoal(GuardianArcherEntity archer) {
        this.archer = archer;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!archer.canAct() || !archer.isTeleportReady()) {
            return false;
        }

        LivingEntity target = archer.getTarget();
        return target != null && target.isAlive()
                && archer.distanceTo(target) <= TheLastSwordConfiguration.getGuardianArcherTeleportTriggerDistanceSafely();
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        archer.getNavigation().stop();
        archer.teleportAway();
    }
}
