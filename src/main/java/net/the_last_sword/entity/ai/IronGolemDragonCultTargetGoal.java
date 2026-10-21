package net.the_last_sword.entity.ai;

import net.eca.util.faction.FactionUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.the_last_sword.faction.DragonCultFaction;

public final class IronGolemDragonCultTargetGoal extends NearestAttackableTargetGoal<Mob> {

    public IronGolemDragonCultTargetGoal(IronGolem golem) {
        super(golem, Mob.class, 5, false, false,
                target -> DragonCultFaction.isWildMember(target) && FactionUtil.canTarget(golem, target));
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = mob.getTarget();
        // 正在追击的拜龙教被收服或改变阵营后，不能沿用旧目标继续攻击。
        return DragonCultFaction.isWildMember(target) && FactionUtil.canTarget(mob, target)
                && super.canContinueToUse();
    }
}
