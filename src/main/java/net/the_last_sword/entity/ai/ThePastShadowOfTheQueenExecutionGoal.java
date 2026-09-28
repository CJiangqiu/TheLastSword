package net.the_last_sword.entity.ai;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import net.eca.api.EcaAPI;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.damagesource.AbsoluteDestructionDamageSource;
import net.the_last_sword.entity.ThePastShadowOfTheQueenEntity;
import net.the_last_sword.network.NetworkHandler;
import net.the_last_sword.network.QueenExecutionCameraPacket;
import net.the_last_sword.util.EntityUtil;
import net.the_last_sword.util.health.TrueHealthManager;

public class ThePastShadowOfTheQueenExecutionGoal extends DangerousSkillGoal<ThePastShadowOfTheQueenEntity> {
    private static final int WINDUP_DURATION = 30;
    private static final int FAILURE_DURATION = 30;
    private static final int SUCCESS_DURATION = 133;
    private static final int FIRST_DAMAGE_TICK = 23;
    private static final int LIE_DOWN_TICK = 49;
    private static final int SECOND_DAMAGE_TICK = 92;
    private static final int RELEASE_TICK = 130;
    private static final double TRIGGER_DISTANCE = 3.0;
    private static final double RANGE_WIDTH = 1.0;
    private static final double RANGE_HEIGHT = 4.0;
    private static final double RANGE_LENGTH = 3.0;

    private final ThePastShadowOfTheQueenEntity queen;
    private Phase phase = Phase.FINISHED;
    private int phaseTick;
    private long cooldownEnd;
    private Vec3 origin;
    private float yaw;
    private LivingEntity executionTarget;
    private Pose previousPose;
    private Pose previousForcedPose;
    private boolean poseApplied;
    private boolean stopping;
    private boolean cooldownCommitted;

    public ThePastShadowOfTheQueenExecutionGoal(ThePastShadowOfTheQueenEntity queen) {
        super(queen, RANGE_WIDTH, RANGE_LENGTH, RANGE_HEIGHT, WINDUP_DURATION);
        this.queen = queen;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = queen.getTarget();
        return queen.canAct() && queen.getAnimationState() == ThePastShadowOfTheQueenEntity.STATE_IDLE
                && queen.level().getGameTime() >= cooldownEnd
                && target != null && target.isAlive() && EntityUtil.canAttack(queen, target)
                && queen.distanceTo(target) <= TRIGGER_DISTANCE;
    }

    @Override
    public boolean canContinueToUse() {
        return phase != Phase.FINISHED && queen.canAct() && isExecutionState(queen.getAnimationState());
    }

    @Override
    protected void onDangerousSkillStart() {
        phase = Phase.WINDUP;
        phaseTick = 0;
        cooldownCommitted = false;
        EntityUtil.faceTarget(queen, queen.getTarget());
        origin = queen.position();
        yaw = queen.getYRot();
        queen.setLightningSpearVisible(false);
        queen.setAnimationState(ThePastShadowOfTheQueenEntity.STATE_EXECUTION);
        queen.setNoGravity(true);
        holdPose();
    }

    @Override
    protected void tickDangerousSkill() {
        if (!canContinueToUse()) {
            stop();
            return;
        }

        holdPose();
        ++phaseTick;
        switch (phase) {
            case WINDUP -> tickWindup();
            case FAILURE -> tickFailure();
            case SUCCESS -> tickSuccess();
            case FINISHED -> { }
        }
    }

    public void holdPose() {
        if (origin == null || !isExecutionState(queen.getAnimationState())) {
            return;
        }
        queen.getNavigation().stop();
        queen.setDeltaMovement(Vec3.ZERO);
        queen.setPos(origin.x, origin.y, origin.z);
        queen.setYRot(yaw);
        queen.setXRot(0.0F);
        queen.yHeadRot = yaw;
        queen.yBodyRot = yaw;
        queen.fallDistance = 0.0F;
    }

    private void tickWindup() {
        if (phaseTick < WINDUP_DURATION) {
            return;
        }

        LivingEntity target = selectExecutionTarget(getDangerousSkillTargets());
        phaseTick = 0;
        if (target == null) {
            phase = Phase.FAILURE;
            queen.setAnimationState(ThePastShadowOfTheQueenEntity.STATE_EXECUTION_FAIL);
            return;
        }

        phase = Phase.SUCCESS;
        executionTarget = target;
        lockDangerousSkillTarget(target);
        queen.setAnimationState(ThePastShadowOfTheQueenEntity.STATE_EXECUTION_SUCCESS);
    }

    private LivingEntity selectExecutionTarget(List<LivingEntity> targets) {
        LivingEntity currentTarget = queen.getTarget();
        if (currentTarget != null && targets.contains(currentTarget)) {
            return currentTarget;
        }
        return targets.stream()
                .filter(LivingEntity::isAlive)
                .min(Comparator.comparingDouble(queen::distanceToSqr))
                .orElse(null);
    }

    private void tickFailure() {
        if (phaseTick >= FAILURE_DURATION) {
            finishSkill();
        }
    }

    private void tickSuccess() {
        if (!isExecutionTargetAvailable()) {
            restoreExecutionTarget();
        }
        if (phaseTick == FIRST_DAMAGE_TICK) {
            dealExecutionDamage(TheLastSwordConfiguration.getQueenExecutionFirstDamageRatioSafely());
        }
        if (phaseTick == LIE_DOWN_TICK) {
            applyExecutionPose();
        }
        if (phaseTick == SECOND_DAMAGE_TICK) {
            dealExecutionDamage(TheLastSwordConfiguration.getQueenExecutionSecondDamageRatioSafely());
        }
        if (phaseTick == RELEASE_TICK) {
            restoreExecutionTarget();
        }
        if (phaseTick >= SUCCESS_DURATION) {
            finishSkill();
        }
    }

    private boolean isExecutionTargetAvailable() {
        return executionTarget != null && executionTarget.isAlive() && !executionTarget.isRemoved()
                && executionTarget.level() == queen.level();
    }

    private void dealExecutionDamage(double ratio) {
        if (!isExecutionTargetAvailable() || !EntityUtil.canAttack(queen, executionTarget)) {
            return;
        }
        float damage = TrueHealthManager.getMaxHealth(executionTarget) * (float) ratio;
        if (Float.isFinite(damage) && damage > 0.0F) {
            EcaAPI.hurt(executionTarget,
                    AbsoluteDestructionDamageSource.absoluteDestruction(queen), damage);
        }
    }

    private void applyExecutionPose() {
        if (!isExecutionTargetAvailable() || poseApplied) {
            return;
        }
        previousPose = executionTarget.getPose();
        if (executionTarget instanceof Player player) {
            previousForcedPose = player.getForcedPose();
            player.setForcedPose(Pose.SLEEPING);
        } else {
            executionTarget.setPose(Pose.SLEEPING);
        }
        if (executionTarget instanceof ServerPlayer serverPlayer) {
            NetworkHandler.sendToPlayer(new QueenExecutionCameraPacket(true, yaw + 180.0F), serverPlayer);
        }
        poseApplied = true;
    }

    private void restoreExecutionTarget() {
        if (executionTarget == null) {
            return;
        }
        if (poseApplied) {
            if (executionTarget instanceof Player player) {
                player.setForcedPose(previousForcedPose);
                if (previousForcedPose == null && executionTarget.isAlive()) {
                    executionTarget.setPose(previousPose != null ? previousPose : Pose.STANDING);
                }
            } else if (executionTarget.isAlive()) {
                executionTarget.setPose(previousPose != null ? previousPose : Pose.STANDING);
            }
            if (executionTarget instanceof ServerPlayer serverPlayer) {
                NetworkHandler.sendToPlayer(new QueenExecutionCameraPacket(false, 0.0F), serverPlayer);
            }
            poseApplied = false;
        }
        unlockDangerousSkillTarget(executionTarget);
        executionTarget = null;
        previousPose = null;
        previousForcedPose = null;
    }

    private void finishSkill() {
        phase = Phase.FINISHED;
        queen.setAnimationState(ThePastShadowOfTheQueenEntity.STATE_IDLE);
    }

    @Override
    protected void onDangerousSkillStop() {
        if (stopping) {
            return;
        }
        stopping = true;
        restoreExecutionTarget();
        if (!cooldownCommitted) {
            cooldownEnd = queen.level().getGameTime() + TheLastSwordConfiguration.getQueenExecutionCooldownSafely();
            cooldownCommitted = true;
        }
        phase = Phase.FINISHED;
        phaseTick = 0;
        if (origin != null) {
            queen.setNoGravity(false);
            queen.setDeltaMovement(Vec3.ZERO);
            origin = null;
        }
        if (isExecutionState(queen.getAnimationState())) {
            queen.setAnimationState(ThePastShadowOfTheQueenEntity.STATE_IDLE);
        }
        stopping = false;
    }

    private static boolean isExecutionState(int state) {
        return state == ThePastShadowOfTheQueenEntity.STATE_EXECUTION
                || state == ThePastShadowOfTheQueenEntity.STATE_EXECUTION_FAIL
                || state == ThePastShadowOfTheQueenEntity.STATE_EXECUTION_SUCCESS;
    }

    private enum Phase {
        WINDUP,
        FAILURE,
        SUCCESS,
        FINISHED
    }
}
