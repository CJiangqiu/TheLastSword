package net.the_last_sword.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.the_last_sword.entity.TheLastEndSwordWraithEntity;
import net.the_last_sword.util.EntityUtil;

//主人攻击目标时同步攻击
public class SwordWraithOwnerHurtTargetGoal extends TargetGoal {
    private final TheLastEndSwordWraithEntity wraith;
    private LivingEntity ownerTarget;
    private int timestamp;

    public SwordWraithOwnerHurtTargetGoal(TheLastEndSwordWraithEntity wraith) {
        super(wraith, false);
        this.wraith = wraith;
    }

    @Override
    public boolean canUse() {
        LivingEntity owner = wraith.getOwner();
        if (owner == null || !owner.isAlive()) {
            return false;
        }

        //获取主人正在攻击的目标
        LivingEntity target = owner.getLastHurtMob();
        int newTimestamp = owner.getLastHurtMobTimestamp();

        //检查是否是新的攻击
        if (target != null && newTimestamp != this.timestamp) {
            this.timestamp = newTimestamp;
            this.ownerTarget = target;

            //验证目标是否有效
            if (isValidTarget(target)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void start() {
        wraith.setTarget(this.ownerTarget);
        super.start();
    }

    //检查目标是否有效
    private boolean isValidTarget(LivingEntity target) {
        if (target == null || !target.isAlive() || target.isRemoved()) {
            return false;
        }
        if (wraith.distanceTo(target) > 64.0) {
            return false;
        }
        if (target instanceof Player player) {
            return !player.isCreative() && !player.isSpectator();
        }
        return EntityUtil.canAttack(wraith, target);
    }
}
