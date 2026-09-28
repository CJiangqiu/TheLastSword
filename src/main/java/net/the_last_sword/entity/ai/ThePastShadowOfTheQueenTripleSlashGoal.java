package net.the_last_sword.entity.ai;

import java.util.EnumSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.QueenEnhancedBlade;
import net.the_last_sword.entity.ThePastShadowOfTheQueenEntity;
import net.the_last_sword.network.NetworkHandler;
import net.the_last_sword.network.QueenTripleSlashShakePacket;
import net.the_last_sword.util.EntityUtil;

public class ThePastShadowOfTheQueenTripleSlashGoal extends Goal {
    private static final int DURATION = 108;
    private static final double SCREEN_SHAKE_RADIUS = 32.0;
    private final ThePastShadowOfTheQueenEntity queen;
    private int tick;
    private long cooldownEnd;
    private Vec3 origin;
    private Vec3 forward;
    private float yaw;

    public ThePastShadowOfTheQueenTripleSlashGoal(ThePastShadowOfTheQueenEntity queen) {
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
        cooldownEnd = queen.level().getGameTime() + TheLastSwordConfiguration.getQueenTripleSlashCooldownSafely();
        EntityUtil.faceTarget(queen, queen.getTarget());
        yaw = queen.getYRot();
        double radians = Math.toRadians(yaw);
        forward = new Vec3(-Math.sin(radians), 0, Math.cos(radians));
        origin = queen.position();
        queen.setLightningSpearVisible(false);
        queen.setAnimationState(ThePastShadowOfTheQueenEntity.STATE_TRIPLE_SLASH);
        queen.setNoGravity(true);
        holdPose();
    }

    // AI 与实体移动结束后都恢复起手姿态，防止控制器和推挤改变预告方向。
    public void holdPose() {
        if (origin == null || queen.getAnimationState() != ThePastShadowOfTheQueenEntity.STATE_TRIPLE_SLASH) return;
        queen.getNavigation().stop();
        queen.setDeltaMovement(Vec3.ZERO);
        queen.setPos(origin.x, origin.y, origin.z);
        queen.setYRot(yaw);
        queen.setXRot(0);
        queen.yHeadRot = yaw;
        queen.yBodyRot = yaw;
        queen.getLookControl().setLookAt(origin.x + forward.x * 8, queen.getEyeY(),
                origin.z + forward.z * 8);
        queen.fallDistance = 0;
    }

    @Override
    public void tick() {
        if (!canContinueToUse()) {
            stop();
            return;
        }
        holdPose();
        ++tick;
        if (queen.level() instanceof ServerLevel server) {
            if (tick >= 15 && tick <= 27) {
                Vec3 point = origin.add(forward.scale(2)).add(0, 2 + (tick - 15) / 12.0 * 3, 0);
                server.sendParticles(ParticleTypes.DRAGON_BREATH, point.x, point.y, point.z,
                        24, 0.3, 0.15, 0.3, 0.015);
            }
            if (tick == 45) releaseBlade(45F);
            if (tick == 65) releaseBlade(-45F);
            if (tick == 80) releaseBlade(0F);
        }
        if (tick >= DURATION) stop();
    }

    private void releaseBlade(float roll) {
        QueenEnhancedBlade blade = new QueenEnhancedBlade(queen.level(), queen);
        blade.setBladeRoll(roll);
        if (queen.level().addFreshEntity(blade)) {
            queen.level().playSound(null, queen.blockPosition(), SoundEvents.ENDER_DRAGON_SHOOT,
                    SoundSource.HOSTILE, 1F, 1F);
            shakeNearbyPlayers();
        }
    }

    private void shakeNearbyPlayers() {
        if (!(queen.level() instanceof ServerLevel server)
                || !TheLastSwordConfiguration.getQueenTripleSlashScreenShakeEnabledSafely()) {
            return;
        }
        double radiusSquared = SCREEN_SHAKE_RADIUS * SCREEN_SHAKE_RADIUS;
        for (ServerPlayer player : server.players()) {
            if (player.distanceToSqr(queen) <= radiusSquared) {
                NetworkHandler.sendToPlayer(new QueenTripleSlashShakePacket(), player);
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return tick < DURATION && queen.canAct()
                && queen.getAnimationState() == ThePastShadowOfTheQueenEntity.STATE_TRIPLE_SLASH;
    }

    @Override
    public void stop() {
        tick = DURATION;
        if (origin != null) {
            queen.setNoGravity(false);
            queen.setDeltaMovement(Vec3.ZERO);
            origin = null;
        }
        if (queen.getAnimationState() == ThePastShadowOfTheQueenEntity.STATE_TRIPLE_SLASH) {
            queen.setAnimationState(ThePastShadowOfTheQueenEntity.STATE_IDLE);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
