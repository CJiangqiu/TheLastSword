package net.the_last_sword.entity.ai;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.DragonCultPriestEntity;

import java.util.EnumSet;

// 祭司距离控制Goal，维持在目标 3~8 格，过近则面向目标倒退后撤
public class DragonCultPriestKeepDistanceGoal extends Goal {

    //靠近时的每tick位移，比后撤快才能拉近距离
    private static final double APPROACH_SPEED = 0.16;

    //垂直方向每tick最大调整量，避免俯冲过猛
    private static final double VERTICAL_STEP = 0.08;

    private final DragonCultPriestEntity priest;

    public DragonCultPriestKeepDistanceGoal(DragonCultPriestEntity priest) {
        this.priest = priest;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return isOutOfBand();
    }

    @Override
    public boolean canContinueToUse() {
        return isOutOfBand();
    }

    @Override
    public void tick() {
        LivingEntity target = priest.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        priest.getLookControl().setLookAt(target, 30.0F, 30.0F);

        double distance = priest.distanceTo(target);
        if (distance < TheLastSwordConfiguration.getDragonCultPriestKeepMinDistanceSafely()) {
            retreatFrom(target);
        } else if (distance > TheLastSwordConfiguration.getDragonCultPriestKeepMaxDistanceSafely()) {
            approachTarget(target);
        } else {
            priest.getNavigation().stop();
        }
    }

    @Override
    public void stop() {
        priest.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    //目标有效、处于待机、且距离越界时才接管移动
    private boolean isOutOfBand() {
        if (!priest.canAct()) {
            return false;
        }
        LivingEntity target = priest.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (priest.getAnimationState() != DragonCultPriestEntity.STATE_IDLE) {
            return false;
        }
        double distance = priest.distanceTo(target);
        return distance < TheLastSwordConfiguration.getDragonCultPriestKeepMinDistanceSafely()
            || distance > TheLastSwordConfiguration.getDragonCultPriestKeepMaxDistanceSafely();
    }

    //面朝目标倒退：直接给位移而非寻路，避免转身把后背露给对手
    private void retreatFrom(LivingEntity target) {
        moveHorizontally(target, -TheLastSwordConfiguration.getDragonCultPriestRetreatSpeedSafely());
    }

    //飞向目标：同样直接给位移，飞行寻路对无重力悬浮单位不可靠
    private void approachTarget(LivingEntity target) {
        moveHorizontally(target, APPROACH_SPEED);
    }

    /*
     * 沿水平方向推进，speed 为正表示靠近、为负表示远离。
     * 垂直分量跟随目标高度并受最低离地高度约束，使祭司能跨越落差而不会贴到地面。
     */
    private void moveHorizontally(LivingEntity target, double speed) {
        priest.getNavigation().stop();
        priest.suppressHover(2);

        Vec3 toTarget = target.position().subtract(priest.position());
        double horizontal = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
        if (horizontal < 1.0E-4) {
            return;
        }

        double vx = toTarget.x / horizontal * speed;
        double vz = toTarget.z / horizontal * speed;

        priest.setDeltaMovement(vx, verticalStep(target), vz);
        faceTarget(target);
    }

    //跟随目标高度，但离地不低于悬浮高度
    private double verticalStep(LivingEntity target) {
        double minHover = TheLastSwordConfiguration.getDragonCultPriestHoverHeightSafely();
        double clearance = priest.getGroundClearance();
        if (clearance < minHover) {
            return VERTICAL_STEP;
        }

        double dy = Mth.clamp(target.getY() - priest.getY(), -VERTICAL_STEP, VERTICAL_STEP);
        return dy < 0 ? Math.max(dy, -(clearance - minHover)) : dy;
    }

    //锁定身体与头部朝向，倒退时不会因移动方向而转身
    private void faceTarget(LivingEntity target) {
        double dx = target.getX() - priest.getX();
        double dz = target.getZ() - priest.getZ();
        float yaw = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;

        priest.setYRot(yaw);
        priest.yBodyRot = yaw;
        priest.yHeadRot = yaw;
    }
}
