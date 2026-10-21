package net.the_last_sword.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.the_last_sword.entity.TheLastEndEntity;
import net.the_last_sword.summon.WraithSummonManager;
import net.the_last_sword.util.DragonCultDisguise;
import net.the_last_sword.util.EntityUtil;

import java.util.function.Predicate;

public final class DragonCultTargetGoals {

    private DragonCultTargetGoals() {}

    public static void register(TheLastEndEntity cultist, Class<?>... ignoredAlertTypes) {
        cultist.targetSelector.addGoal(1, new CultHurtByTargetGoal(cultist).setAlertOthers(ignoredAlertTypes));
        cultist.targetSelector.addGoal(2, new WildTargetGoal<>(cultist, Player.class,
                DragonCultDisguise.NOT_DISGUISED_PLAYER));
        cultist.targetSelector.addGoal(3, new WildTargetGoal<>(cultist, AbstractVillager.class, target -> true));
        cultist.targetSelector.addGoal(3, new WildTargetGoal<>(cultist, IronGolem.class, target -> true));
    }

    private static final class WildTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
        private final TheLastEndEntity cultist;

        private WildTargetGoal(TheLastEndEntity cultist, Class<T> targetType, Predicate<LivingEntity> filter) {
            super(cultist, targetType, 10, true, false,
                    target -> EntityUtil.canAttack(cultist, target) && filter.test(target));
            this.cultist = cultist;
        }

        private boolean isWild() {
            return !cultist.isTame() && !WraithSummonManager.isWraith(cultist);
        }

        @Override
        public boolean canUse() {
            return isWild() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            // 绑定身份可能在 Goal 运行期间改变，持续条件也必须停止野生敌意。
            return isWild() && EntityUtil.canAttack(cultist, cultist.getTarget()) && super.canContinueToUse();
        }
    }

    private static final class CultHurtByTargetGoal extends HurtByTargetGoal {
        private CultHurtByTargetGoal(TheLastEndEntity cultist) {
            super(cultist);
        }

        @Override
        public boolean canUse() {
            return EntityUtil.canAttack(mob, mob.getLastHurtByMob()) && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return EntityUtil.canAttack(mob, mob.getTarget()) && super.canContinueToUse();
        }

        @Override
        protected void alertOther(Mob ally, LivingEntity target) {
            // 野生同类的求援不能替主人授权剑灵攻击受保护目标。
            if (EntityUtil.canAttack(ally, target)) {
                super.alertOther(ally, target);
            }
        }
    }
}
