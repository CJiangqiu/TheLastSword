package net.the_last_sword.entity.ai;

import java.util.EnumSet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.QueenBlinkBlade;
import net.the_last_sword.entity.ThePastShadowOfTheQueenEntity;
import net.the_last_sword.util.EntityUtil;
import net.the_last_sword.util.ParticleUtil;

public class ThePastShadowOfTheQueenBlinkGoal extends Goal {
    private static final int DURATION = 76;
    private static final double TELEPORT_DISTANCE = 1.0;
    private static final int TELEPORT_VERTICAL_SEARCH_RANGE = 4;
    private final ThePastShadowOfTheQueenEntity queen;
    private int tick;
    private long cooldownEnd;
    private Vec3 blinkDestination;

    public ThePastShadowOfTheQueenBlinkGoal(ThePastShadowOfTheQueenEntity queen) {
        this.queen = queen;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = queen.getTarget();
        return queen.canAct() && queen.getAnimationState() == ThePastShadowOfTheQueenEntity.STATE_IDLE
                && queen.level().getGameTime() >= cooldownEnd && target != null && target.isAlive()
                && EntityUtil.canAttack(queen, target);
    }

    @Override
    public void start() {
        tick = 0;
        blinkDestination = null;
        cooldownEnd = queen.level().getGameTime() + TheLastSwordConfiguration.getQueenBlinkSettings().cooldown();
        LivingEntity target = queen.getTarget();
        if (target != null) {
            EntityUtil.faceTarget(queen, target);
        }
        queen.setAnimationState(ThePastShadowOfTheQueenEntity.STATE_BLINK);
        queen.setBlinkTick(0);
        queen.setLightningSpearVisible(false);
        queen.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (!canContinueToUse()) {
            stop();
            return;
        }
        queen.getNavigation().stop();
        ++tick;
        if (tick == 45) {
            // 安全落点检查必须使用女皇恢复后的正常碰撞尺寸。
            queen.setBlinkTick(tick);
        }
        if (!queen.level().isClientSide) {
            if (tick == 20) {
                recordBlinkDestination();
            } else if (tick == 45) {
                teleportToBlinkDestination();
            }
        }
        queen.setBlinkTick(tick);
        if (tick == 65 && !queen.level().isClientSide) {
            LivingEntity target = queen.getTarget();
            if (target != null && target.isAlive() && EntityUtil.canAttack(queen, target)) {
                EntityUtil.faceTarget(queen, target);
            }
            QueenBlinkBlade blade = new QueenBlinkBlade(queen.level(), queen,
                    TheLastSwordConfiguration.getQueenBlinkSettings());
            if (queen.level().addFreshEntity(blade)) {
                queen.level().playSound(null, queen.blockPosition(), SoundEvents.ENDER_DRAGON_SHOOT,
                        SoundSource.HOSTILE, 1.0F, 1.0F);
            }
        }
        if (tick >= DURATION) {
            stop();
        }
    }

    private void recordBlinkDestination() {
        LivingEntity target = queen.getTarget();
        if (target == null || !target.isAlive() || !EntityUtil.canAttack(queen, target)) {
            return;
        }

        Vec3 direction = target.getLookAngle().multiply(1.0, 0.0, 1.0);
        if (direction.lengthSqr() < 1.0E-6) {
            direction = queen.position().subtract(target.position()).multiply(1.0, 0.0, 1.0);
        }
        if (direction.lengthSqr() < 1.0E-6) {
            direction = new Vec3(0.0, 0.0, 1.0);
        } else {
            direction = direction.normalize();
        }

        blinkDestination = target.position().add(direction.scale(TELEPORT_DISTANCE));
    }

    private void teleportToBlinkDestination() {
        if (blinkDestination == null) {
            return;
        }

        Vec3 origin = queen.position();
        Vec3 teleportPosition = EntityUtil.theLastEndSafeTeleport(
                queen, blinkDestination, TELEPORT_VERTICAL_SEARCH_RANGE);
        if (teleportPosition != null) {
            queen.fallDistance = 0;
            queen.level().playSound(null, origin.x, origin.y, origin.z,
                    SoundEvents.ENDERMAN_TELEPORT, queen.getSoundSource(), 1.0F, 1.0F);
            queen.level().playSound(null, teleportPosition.x, teleportPosition.y, teleportPosition.z,
                    SoundEvents.ENDERMAN_TELEPORT, queen.getSoundSource(), 1.0F, 1.0F);
            ParticleUtil.spawnTeleportParticles(queen.level(), origin, teleportPosition, queen.getBbHeight());
            LivingEntity target = queen.getTarget();
            if (target != null && target.isAlive() && EntityUtil.canAttack(queen, target)) {
                EntityUtil.faceTarget(queen, target);
            }
        }
        blinkDestination = null;
    }

    @Override
    public boolean canContinueToUse() {
        return tick < DURATION && queen.canAct()
                && queen.getAnimationState() == ThePastShadowOfTheQueenEntity.STATE_BLINK;
    }

    @Override
    public void stop() {
        tick = DURATION;
        blinkDestination = null;
        queen.setBlinkTick(-1);
        if (queen.getAnimationState() == ThePastShadowOfTheQueenEntity.STATE_BLINK) {
            queen.setAnimationState(ThePastShadowOfTheQueenEntity.STATE_IDLE);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
