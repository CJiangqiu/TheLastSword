package net.the_last_sword.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.DragonCultPaladinEntity;

import java.util.EnumSet;

// 圣骑士格挡Goal，block动画0.25s~0.8s免疫伤害
public class DragonCultPaladinBlockGoal extends Goal {

    //block 动画全长 0.8 秒
    private static final int ANIMATION_LENGTH = 16;

    //免伤起止帧（0.25s / 0.8s）
    private static final int IMMUNE_START_TICK = 5;
    private static final int IMMUNE_END_TICK = 16;

    private final DragonCultPaladinEntity paladin;
    private int animationTick;
    private long cooldownEnd;

    public DragonCultPaladinBlockGoal(DragonCultPaladinEntity paladin) {
        this.paladin = paladin;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!paladin.canAct()) {
            return false;
        }
        if (paladin.getAnimationState() != DragonCultPaladinEntity.STATE_IDLE) {
            return false;
        }
        if (paladin.level().getGameTime() < cooldownEnd) {
            return false;
        }

        LivingEntity target = paladin.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        return paladin.distanceTo(target) <= TheLastSwordConfiguration.getDragonCultPaladinBlockRangeSafely();
    }

    @Override
    public boolean canContinueToUse() {
        return animationTick > 0;
    }

    @Override
    public void start() {
        animationTick = ANIMATION_LENGTH;
        paladin.setAnimationState(DragonCultPaladinEntity.STATE_BLOCK);
        paladin.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (animationTick <= 0) {
            return;
        }

        int frame = ANIMATION_LENGTH - animationTick;
        paladin.setBlockImmune(frame >= IMMUNE_START_TICK && frame <= IMMUNE_END_TICK);

        animationTick--;

        LivingEntity target = paladin.getTarget();
        if (target != null && target.isAlive()) {
            paladin.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        if (animationTick == 0) {
            paladin.setAnimationState(DragonCultPaladinEntity.STATE_IDLE);
            cooldownEnd = paladin.level().getGameTime()
                + TheLastSwordConfiguration.getDragonCultPaladinBlockCooldownSafely();
        }
    }

    @Override
    public void stop() {
        animationTick = 0;
        //中断时必须复位，否则会一直免伤
        paladin.setBlockImmune(false);
        if (paladin.getAnimationState() == DragonCultPaladinEntity.STATE_BLOCK) {
            paladin.setAnimationState(DragonCultPaladinEntity.STATE_IDLE);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
