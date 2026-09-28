package net.the_last_sword.entity.ai;

import java.util.EnumSet;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.LightningSpearProjectile;
import net.the_last_sword.entity.ThePastShadowOfTheQueenEntity;
import net.the_last_sword.util.EntityUtil;

// 技能前段固定女皇的位置，使升空与悬停不受重力和寻路干扰
public class ThePastShadowOfTheQueenLightningSpearGoal extends Goal {
    private static final int ANIMATION_LENGTH = 48;
    private static final int ASCENT_DURATION = 5;
    private static final int HOVER_DURATION = 30;
    private static final int SPEAR_APPEAR_TICK = 15;
    private static final int SPEAR_RELEASE_TICK = 30;
    private static final float SPEAR_SPEED = 2.5F;
    private static final double ASCENT_HEIGHT = 3.0D;
    private static final double VERTICAL_STEP = ASCENT_HEIGHT / ASCENT_DURATION;

    private final ThePastShadowOfTheQueenEntity queen;
    private int animationTick;
    private long cooldownEnd;
    private double startY;
    private boolean airMovementActive;

    public ThePastShadowOfTheQueenLightningSpearGoal(ThePastShadowOfTheQueenEntity queen) {
        this.queen = queen;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!queen.canAct() || queen.getAnimationState() != ThePastShadowOfTheQueenEntity.STATE_IDLE) {
            return false;
        }
        if (queen.level().getGameTime() < cooldownEnd) {
            return false;
        }

        LivingEntity target = queen.getTarget();
        return target != null && target.isAlive() && EntityUtil.canAttack(queen, target);
    }

    @Override
    public void start() {
        animationTick = 0;
        queen.setLightningSpearVisible(false);
        cooldownEnd = queen.level().getGameTime()
                + TheLastSwordConfiguration.getThePastShadowOfTheQueenLightningSpearCooldownSafely();
        startY = queen.getY();
        airMovementActive = true;
        queen.setAnimationState(ThePastShadowOfTheQueenEntity.STATE_LIGHTNING_SPEAR);
        queen.trySendSkillTalk("lightning_spear");
        queen.setNoGravity(true);
        queen.getNavigation().stop();
        queen.setDeltaMovement(Vec3.ZERO);
        queen.fallDistance = 0.0F;
    }

    @Override
    public void tick() {
        if (!queen.canAct() || queen.getAnimationState() != ThePastShadowOfTheQueenEntity.STATE_LIGHTNING_SPEAR) {
            animationTick = ANIMATION_LENGTH;
            finishSkill();
            return;
        }
        if (animationTick < ASCENT_DURATION) {
            ascend();
        } else if (animationTick < ASCENT_DURATION + HOVER_DURATION) {
            hover();
        } else {
            beginFalling();
            queen.fallDistance = 0.0F;
        }

        LivingEntity target = queen.getTarget();
        if (target != null && target.isAlive()) {
            queen.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        animationTick++;
        if (animationTick == SPEAR_APPEAR_TICK) {
            queen.setLightningSpearVisible(true);
        } else if (animationTick == SPEAR_RELEASE_TICK) {
            queen.setLightningSpearVisible(false);
            releaseSpear();
        }
        if (animationTick >= ANIMATION_LENGTH) {
            finishSkill();
        }
    }

    private void releaseSpear() {
        if (queen.level().isClientSide) {
            return;
        }
        LivingEntity target = queen.getTarget();
        if (target == null || !target.isAlive() || !EntityUtil.canAttack(queen, target)) {
            return;
        }
        // 服务端使用右手附近的实体相对位置，不依赖客户端骨骼数据。
        float yaw = queen.yBodyRot * Mth.DEG_TO_RAD;
        Vec3 origin = queen.position().add(
                -Mth.cos(yaw) * 0.45D - Mth.sin(yaw) * 0.35D,
                queen.getEyeHeight() * 0.85D,
                -Mth.sin(yaw) * 0.45D + Mth.cos(yaw) * 0.35D);
        Vec3 direction = target.getBoundingBox().getCenter().subtract(origin);
        if (direction.lengthSqr() < 0.000001D) {
            return;
        }
        LightningSpearProjectile spear = new LightningSpearProjectile(queen.level(), queen);
        spear.setPos(origin.x, origin.y, origin.z);
        spear.shoot(direction.x, direction.y, direction.z, SPEAR_SPEED, 0.0F);
        if (queen.level().addFreshEntity(spear)) {
            queen.level().playSound(null, origin.x, origin.y, origin.z,
                    SoundEvents.TRIDENT_THROW, SoundSource.HOSTILE, 1.0F, 1.0F);
        }
    }

    private void ascend() {
        double remainingHeight = startY + ASCENT_HEIGHT - queen.getY();
        if (remainingHeight > 0.0D) {
            queen.move(MoverType.SELF, new Vec3(0.0D, Math.min(VERTICAL_STEP, remainingHeight), 0.0D));
        }
        queen.setDeltaMovement(Vec3.ZERO);
        queen.fallDistance = 0.0F;
    }

    private void hover() {
        queen.setDeltaMovement(Vec3.ZERO);
        queen.fallDistance = 0.0F;
    }

    private void beginFalling() {
        if (!airMovementActive) {
            return;
        }
        airMovementActive = false;
        queen.setNoGravity(false);
        queen.setDeltaMovement(Vec3.ZERO);
    }

    private void finishSkill() {
        queen.setLightningSpearVisible(false);
        beginFalling();
        if (queen.getAnimationState() == ThePastShadowOfTheQueenEntity.STATE_LIGHTNING_SPEAR) {
            queen.setAnimationState(ThePastShadowOfTheQueenEntity.STATE_IDLE);
        }
    }

    @Override
    public boolean canContinueToUse() {
        return animationTick < ANIMATION_LENGTH && queen.canAct()
                && queen.getAnimationState() == ThePastShadowOfTheQueenEntity.STATE_LIGHTNING_SPEAR;
    }

    @Override
    public void stop() {
        animationTick = ANIMATION_LENGTH;
        finishSkill();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
