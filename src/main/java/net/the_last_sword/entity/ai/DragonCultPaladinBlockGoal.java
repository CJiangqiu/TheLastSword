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
    private boolean running;

    public DragonCultPaladinBlockGoal(DragonCultPaladinEntity paladin) {
        this.paladin = paladin;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!paladin.hasBlockRequest()) {
            return false;
        }
        if (!paladin.canAct() || paladin.level().getGameTime() < cooldownEnd) {
            //冷却期间受到的伤害不会在冷却结束后补触发格挡
            paladin.consumeBlockRequest();
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return animationTick > 0;
    }

    @Override
    public void start() {
        paladin.consumeBlockRequest();
        animationTick = ANIMATION_LENGTH;
        running = true;
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
            finishBlock();
        }
    }

    @Override
    public void stop() {
        finishBlock();
    }

    private void finishBlock() {
        animationTick = 0;
        paladin.setBlockImmune(false);
        if (paladin.getAnimationState() == DragonCultPaladinEntity.STATE_BLOCK) {
            paladin.setAnimationState(DragonCultPaladinEntity.STATE_IDLE);
        }
        if (running) {
            running = false;
            cooldownEnd = paladin.level().getGameTime()
                + TheLastSwordConfiguration.getDragonCultPaladinBlockCooldownSafely();
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
