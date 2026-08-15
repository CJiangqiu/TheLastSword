package net.the_last_sword.entity.ai;

import java.util.EnumSet;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Items;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.GuardianSaberEntity;

//剑士守卫格挡Goal：进入原版副手举盾状态，并在动画定型后持续防御
public class GuardianSaberBlockGoal extends Goal {

    private final GuardianSaberEntity saber;
    private int animationTick;
    private long cooldownEnd;
    private boolean running;

    public GuardianSaberBlockGoal(GuardianSaberEntity saber) {
        this.saber = saber;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!saber.hasBlockRequest()) {
            return false;
        }
        if (!saber.canAct()
                || saber.level().getGameTime() < cooldownEnd
                || saber.isShieldDisabled()
                || !saber.getOffhandItem().is(Items.SHIELD)) {
            //冷却或盾牌不可用时，本次受伤不排队到以后触发
            saber.consumeBlockRequest();
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return animationTick > 0
                && !saber.isShieldDisabled()
                && saber.isUsingItem()
                && saber.getUseItem().is(Items.SHIELD);
    }

    @Override
    public void start() {
        saber.consumeBlockRequest();
        animationTick = TheLastSwordConfiguration.getGuardianSaberBlockDurationSafely();
        running = true;
        saber.setAnimationState(GuardianSaberEntity.STATE_BLOCK);
        saber.getNavigation().stop();
        saber.startUsingItem(InteractionHand.OFF_HAND);
    }

    @Override
    public void tick() {
        if (animationTick <= 0) {
            return;
        }

        LivingEntity target = saber.getTarget();
        if (target != null && target.isAlive()) {
            saber.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        animationTick--;
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
        saber.stopUsingItem();
        if (saber.getAnimationState() == GuardianSaberEntity.STATE_BLOCK) {
            saber.setAnimationState(GuardianSaberEntity.STATE_IDLE);
        }
        if (running) {
            running = false;
            cooldownEnd = saber.level().getGameTime()
                    + TheLastSwordConfiguration.getGuardianSaberBlockCooldownSafely();
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
