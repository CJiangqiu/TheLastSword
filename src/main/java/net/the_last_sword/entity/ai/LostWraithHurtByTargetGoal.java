package net.the_last_sword.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.the_last_sword.entity.LostWraithEntity;
import net.the_last_sword.util.EntityUtil;

// 迷失战魂受伤反击Goal
public class LostWraithHurtByTargetGoal extends HurtByTargetGoal {
    private final LostWraithEntity wraith;

    public LostWraithHurtByTargetGoal(LostWraithEntity wraith) {
        super(wraith);
        this.wraith = wraith;
    }

    @Override
    public boolean canUse() {
        LivingEntity attacker = wraith.getLastHurtByMob();
        return attacker != null
            && EntityUtil.canAttack(wraith, attacker)
            && super.canUse();
    }
}
