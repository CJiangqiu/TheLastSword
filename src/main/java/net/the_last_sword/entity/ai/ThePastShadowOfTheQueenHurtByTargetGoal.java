package net.the_last_sword.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.the_last_sword.entity.ThePastShadowOfTheQueenEntity;
import net.the_last_sword.util.EntityUtil;

// 女皇仅反击可攻击的伤害来源，避免将友方或无效实体设为目标
public class ThePastShadowOfTheQueenHurtByTargetGoal extends HurtByTargetGoal {
    private final ThePastShadowOfTheQueenEntity queen;

    public ThePastShadowOfTheQueenHurtByTargetGoal(ThePastShadowOfTheQueenEntity queen) {
        super(queen);
        this.queen = queen;
    }

    @Override
    public boolean canUse() {
        LivingEntity attacker = queen.getLastHurtByMob();
        return queen.canAct()
                && attacker != null
                && EntityUtil.canAttack(queen, attacker)
                && super.canUse();
    }
}
