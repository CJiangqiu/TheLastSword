package net.the_last_sword.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.TheLastEndEntity;
import net.the_last_sword.entity.TheLastEndSwordWraithEntity;
import net.the_last_sword.util.ParticleUtil;

import java.util.EnumSet;

//万物终焉技能Goal
public class SwordWraithEndOfAllThingsGoal extends Goal {
    private final TheLastEndSwordWraithEntity wraith;
    private int animationTick;
    private double startY;
    private boolean previousNoGravity;
    private boolean airborne;

    private static final int ANIMATION_LENGTH = 190;
    private static final int ACTIVATE_TICK = 20;
    private static final int ASCENT_START_TICK = 80;
    private static final int ASCENT_END_TICK = 85;
    private static final int DESCENT_START_TICK = 120;
    private static final int DESCENT_END_TICK = 125;
    private static final double JUMP_HEIGHT = 8.0D;
    private static final double VERTICAL_STEP = JUMP_HEIGHT / (ASCENT_END_TICK - ASCENT_START_TICK);

    // 粒子圆环时间点（animationTick倒计时值）
    private static final int CIRCLE_1S = ANIMATION_LENGTH - 20;   // 170
    private static final int CIRCLE_2S = ANIMATION_LENGTH - 40;   // 150
    private static final int CIRCLE_3S = ANIMATION_LENGTH - 60;   // 130
    private static final int CIRCLE_7_5S = ANIMATION_LENGTH - 150; // 40
    // 8.5秒开始收束，animationTick <= 20
    private static final int SHRINK_START = ANIMATION_LENGTH - 170; // 20

    public SwordWraithEndOfAllThingsGoal(TheLastEndSwordWraithEntity wraith) {
        this.wraith = wraith;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!wraith.canAct()) {
            return false;
        }
        if (wraith.getAnimationState() != TheLastEndSwordWraithEntity.STATE_IDLE) {
            return false;
        }
        if (wraith.isAllThingsEnd()) {
            return false;
        }
        if (wraith.getTheLastEndLevel() < TheLastEndEntity.RESURRECTION_LEVEL) {
            return false;
        }

        //自身终焉标记≥13时触发
        return wraith.getEndMark() >= TheLastEndSwordWraithEntity.END_MARK_THRESHOLD;
    }

    @Override
    public void start() {
        animationTick = ANIMATION_LENGTH;
        wraith.setAnimationState(TheLastEndSwordWraithEntity.STATE_END_OF_ALL_THINGS);
        wraith.getNavigation().stop();
        startY = wraith.getY();
        previousNoGravity = wraith.isNoGravity();
        airborne = false;
    }

    @Override
    public void tick() {
        if (animationTick <= 0) {
            return;
        }

        //激活万物终焉状态并启动持续效果
        if (animationTick == ACTIVATE_TICK) {
            wraith.setAllThingsEnd(true);
            TheLastEndSwordWraithEntity.AllThingsEndActiveEffect.start(wraith);
        }

        int relativeFrame = ANIMATION_LENGTH - animationTick;
        if (relativeFrame >= ASCENT_START_TICK && relativeFrame < ASCENT_END_TICK) {
            ascend();
        } else if (relativeFrame >= ASCENT_END_TICK && relativeFrame < DESCENT_START_TICK) {
            hover();
        } else if (relativeFrame >= DESCENT_START_TICK && relativeFrame < DESCENT_END_TICK) {
            descend();
        } else if (relativeFrame == DESCENT_END_TICK) {
            finishAirMovement();
        }

        //动画阶段粒子效果
        spawnAnimationParticles();

        animationTick--;

        LivingEntity target = wraith.getTarget();
        if (target != null && target.isAlive()) {
            wraith.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        if (animationTick == 0) {
            finishAirMovement();
            wraith.setAnimationState(TheLastEndSwordWraithEntity.STATE_IDLE);
        }
    }

    private void spawnAnimationParticles() {
        double radius = TheLastSwordConfiguration.getSkillEndOfAllThingsRangeSafely();
        Vec3 centerPos = wraith.position();

        //收束阶段：8.5秒起，每4tick生成收束粒子
        if (animationTick <= SHRINK_START) {
            if (animationTick % 4 == 0) {
                double shrinkProgress = (SHRINK_START - animationTick) / 20.0;
                ParticleUtil.spawnAllThingsEndShrinkingCircles(wraith.level(), centerPos, radius, shrinkProgress);
                ParticleUtil.spawnAllThingsEndCenterParticles(wraith.level(), centerPos, true);
            }
            return;
        }

        //特定时间点生成扩散圆环
        if (animationTick == CIRCLE_1S || animationTick == CIRCLE_2S
                || animationTick == CIRCLE_3S || animationTick == CIRCLE_7_5S) {
            ParticleUtil.spawnAllThingsEndCircles(wraith.level(), centerPos, radius);
            ParticleUtil.spawnAllThingsEndCenterParticles(wraith.level(), centerPos, false);
        }
    }

    @Override
    public boolean canContinueToUse() {
        return animationTick > 0;
    }

    @Override
    public void stop() {
        animationTick = 0;
        finishAirMovement();
        if (wraith.getAnimationState() == TheLastEndSwordWraithEntity.STATE_END_OF_ALL_THINGS) {
            wraith.setAnimationState(TheLastEndSwordWraithEntity.STATE_IDLE);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    //手动控制垂直阶段，避免重力使动画中的滞空位置持续下坠
    private void ascend() {
        beginAirMovement();
        double remainingHeight = startY + JUMP_HEIGHT - wraith.getY();
        if (remainingHeight > 0.0D) {
            wraith.move(MoverType.SELF, new Vec3(0.0D, Math.min(VERTICAL_STEP, remainingHeight), 0.0D));
        }
        wraith.setDeltaMovement(Vec3.ZERO);
        wraith.fallDistance = 0.0F;
    }

    private void hover() {
        beginAirMovement();
        wraith.setDeltaMovement(Vec3.ZERO);
        wraith.fallDistance = 0.0F;
    }

    private void descend() {
        beginAirMovement();
        double remainingHeight = wraith.getY() - startY;
        if (remainingHeight > 0.0D) {
            wraith.move(MoverType.SELF, new Vec3(0.0D, -Math.min(VERTICAL_STEP, remainingHeight), 0.0D));
        }
        wraith.setDeltaMovement(Vec3.ZERO);
        wraith.fallDistance = 0.0F;
    }

    private void beginAirMovement() {
        if (!airborne) {
            airborne = true;
            wraith.setNoGravity(true);
        }
    }

    private void finishAirMovement() {
        if (!airborne) {
            return;
        }
        airborne = false;
        wraith.setNoGravity(previousNoGravity);
        wraith.setDeltaMovement(Vec3.ZERO);
        wraith.fallDistance = 0.0F;
    }
}
