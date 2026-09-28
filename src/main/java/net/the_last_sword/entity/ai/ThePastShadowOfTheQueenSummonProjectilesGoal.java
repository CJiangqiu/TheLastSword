package net.the_last_sword.entity.ai;

import java.util.EnumSet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.QueenSummonedProjectile;
import net.the_last_sword.entity.ThePastShadowOfTheQueenEntity;
import net.the_last_sword.init.ModEntities;
import net.the_last_sword.util.EntityUtil;
import net.the_last_sword.util.QueenSummonRiftPlacement;

public class ThePastShadowOfTheQueenSummonProjectilesGoal extends Goal {
    private static final int DURATION = 176;
    private final ThePastShadowOfTheQueenEntity queen;
    private int tick;
    private long cooldownEnd;
    private Vec3 origin;
    private Vec3 forward;
    private Vec3 lastDirection;
    private float yaw;

    public ThePastShadowOfTheQueenSummonProjectilesGoal(ThePastShadowOfTheQueenEntity queen) {
        this.queen = queen;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = queen.getTarget();
        return queen.canAct() && queen.getAnimationState() == ThePastShadowOfTheQueenEntity.STATE_IDLE
                && queen.level().getGameTime() >= cooldownEnd
                && target != null && target.isAlive() && EntityUtil.canAttack(queen, target);
    }

    @Override
    public void start() {
        tick = 0;
        cooldownEnd = queen.level().getGameTime() + TheLastSwordConfiguration.getQueenSummonProjectilesCooldownSafely();
        EntityUtil.faceTarget(queen, queen.getTarget());
        yaw = queen.getYRot();
        double radians = Math.toRadians(yaw);
        forward = new Vec3(-Math.sin(radians), 0, Math.cos(radians));
        lastDirection = forward;
        origin = queen.position();
        queen.setLightningSpearVisible(false);
        queen.setSummonYaw(yaw);
        queen.setSummonTick(0);
        queen.setAnimationState(ThePastShadowOfTheQueenEntity.STATE_SUMMON_PROJECTILES);
        queen.trySendSkillTalk("summon_projectiles");
        queen.setNoGravity(true);
        holdPose();
    }

    // 发射只改变水晶的方向，女皇与裂缝保留起手姿态。
    public void holdPose() {
        if (origin == null || queen.getAnimationState() != ThePastShadowOfTheQueenEntity.STATE_SUMMON_PROJECTILES) return;
        queen.getNavigation().stop();
        queen.setDeltaMovement(Vec3.ZERO);
        queen.setPos(origin.x, origin.y, origin.z);
        queen.setYRot(yaw);
        queen.setXRot(0);
        queen.yHeadRot = yaw;
        queen.yBodyRot = yaw;
        queen.getLookControl().setLookAt(origin.x + forward.x * 8, queen.getEyeY(), origin.z + forward.z * 8);
        queen.fallDistance = 0;
    }

    @Override
    public void tick() {
        if (!canContinueToUse()) {
            stop();
            return;
        }
        holdPose();
        queen.setSummonTick(++tick);
        if (!queen.level().isClientSide && tick >= 40 && tick <= 140 && tick % 20 == 0) {
            releaseCrystal();
        }
        if (tick >= DURATION) stop();
    }

    private void releaseCrystal() {
        Vec3 position = QueenSummonRiftPlacement.worldCenter(origin, yaw);
        LivingEntity target = queen.getTarget();
        if (target != null && target.isAlive() && EntityUtil.canAttack(queen, target)) {
            Vec3 direction = target.getBoundingBox().getCenter().subtract(position);
            if (direction.lengthSqr() > 1.0E-8) lastDirection = direction.normalize();
        }
        // 目标失效时沿上次方向完成召唤，避免改变六次发射的节奏。
        QueenSummonedProjectile crystal = new QueenSummonedProjectile(
                ModEntities.QUEEN_SUMMONED_PROJECTILE.get(), queen, queen.level(), queen.getUUID());
        crystal.setPos(position.x, position.y, position.z);
        crystal.setLifetimeTicks(60);
        float damage = (float) (queen.getAttributeValue(Attributes.ATTACK_DAMAGE)
                * TheLastSwordConfiguration.getQueenSummonProjectilesDamageMultiplierSafely());
        crystal.setSnapshotDamage(0, damage);
        crystal.shoot(lastDirection.x, lastDirection.y, lastDirection.z, 4F, 0F);
        crystal.setSilent(true);
        if (queen.level().addFreshEntity(crystal)) {
            queen.level().playSound(null, position.x, position.y, position.z,
                    SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.HOSTILE, 1F, 1F);
        }
    }

    @Override
    public boolean canContinueToUse() {
        return tick < DURATION && queen.canAct()
                && queen.getAnimationState() == ThePastShadowOfTheQueenEntity.STATE_SUMMON_PROJECTILES;
    }

    @Override
    public void stop() {
        tick = DURATION;
        queen.setSummonTick(-1);
        if (origin != null) {
            queen.setNoGravity(false);
            queen.setDeltaMovement(Vec3.ZERO);
            origin = null;
        }
        if (queen.getAnimationState() == ThePastShadowOfTheQueenEntity.STATE_SUMMON_PROJECTILES) {
            queen.setAnimationState(ThePastShadowOfTheQueenEntity.STATE_IDLE);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
