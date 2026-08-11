package net.the_last_sword.entity.ai;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.DragonCultistEntity;

import java.util.EnumSet;

// 教徒魔法远程Goal，HP>50%且≥8格发射火球+靠近，HP≤50%持续火球+远离
public class DragonCultistMagicGoal extends Goal {
    private static final int ANIMATION_LENGTH = 35;
    private static final int FIRE_TICK = 25;
    private static final double CHARGE_DISTANCE = 1.1D;
    private static final double MIN_CHARGE_RADIUS = 0.12D;
    private static final double MAX_CHARGE_RADIUS = 0.42D;
    private static final int CHARGE_PARTICLE_COUNT = 8;

    private final DragonCultistEntity cultist;
    private int animationTick;
    private long cooldownEnd;
    private boolean fired;
    private boolean lowHpMode; // HP≤50%时持续运行

    public DragonCultistMagicGoal(DragonCultistEntity cultist) {
        this.cultist = cultist;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!cultist.canAct()) {
            return false;
        }
        if (cultist.getAnimationState() != DragonCultistEntity.STATE_IDLE) {
            return false;
        }
        LivingEntity target = cultist.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (isOnCooldown()) {
            return false;
        }

        float hpRatio = getHpRatio();
        if (hpRatio <= 0.5f) {
            return true; // 低血量时冷却好了就用
        }

        // 高血量时只在远处用
        return cultist.distanceTo(target) >= TheLastSwordConfiguration.getDragonCultistMagicMinDistanceSafely();
    }

    @Override
    public boolean canContinueToUse() {
        // 播放动画中
        if (animationTick > 0) {
            return true;
        }
        // 低血量模式：持续循环（施法→冷却+后退→施法）
        if (lowHpMode) {
            LivingEntity target = cultist.getTarget();
            return target != null && target.isAlive();
        }
        return false;
    }

    @Override
    public void start() {
        float hpRatio = getHpRatio();
        lowHpMode = hpRatio <= 0.5f;
        beginCast();
    }

    @Override
    public void tick() {
        // 播放施法动画
        if (animationTick > 0) {
            int relativeFrame = ANIMATION_LENGTH - animationTick;

            LivingEntity target = cultist.getTarget();
            if (target != null && target.isAlive()) {
                cultist.getLookControl().setLookAt(target, 30.0F, 30.0F);
                if (relativeFrame < FIRE_TICK) {
                    spawnChargeParticles(target, relativeFrame);
                }
            }

            if (relativeFrame == FIRE_TICK && !fired) {
                fireGhastFireball();
                fired = true;
            }

            animationTick--;

            if (animationTick == 0) {
                cultist.setAnimationState(DragonCultistEntity.STATE_IDLE);
                cooldownEnd = cultist.level().getGameTime()
                + TheLastSwordConfiguration.getDragonCultistMagicCooldownSafely();
            }
            return;
        }

        // 低血量模式：冷却期间后退
        if (lowHpMode) {
            LivingEntity target = cultist.getTarget();
            if (target == null || !target.isAlive()) {
                return;
            }
            retreat(target);

            // 冷却完毕，再次施法
            if (!isOnCooldown() && cultist.getAnimationState() == DragonCultistEntity.STATE_IDLE) {
                beginCast();
            }
        }
    }

    @Override
    public void stop() {
        animationTick = 0;
        fired = false;
        lowHpMode = false;
        if (cultist.getAnimationState() == DragonCultistEntity.STATE_MAGIC) {
            cultist.setAnimationState(DragonCultistEntity.STATE_IDLE);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private void beginCast() {
        animationTick = ANIMATION_LENGTH;
        fired = false;
        cultist.setAnimationState(DragonCultistEntity.STATE_MAGIC);
        cultist.getNavigation().stop();
    }

    private void retreat(LivingEntity target) {
        Vec3 away = cultist.position().subtract(target.position());
        if (away.lengthSqr() < 0.01) {
            away = new Vec3(1, 0, 0);
        }
        away = away.normalize().scale(4.0);
        Vec3 retreatPos = cultist.position().add(away);
        cultist.getNavigation().moveTo(retreatPos.x, retreatPos.y, retreatPos.z, 1.0);
    }

    private boolean isOnCooldown() {
        return cultist.level().getGameTime() < cooldownEnd;
    }

    private float getHpRatio() {
        float max = cultist.getWorldAnchorMax();
        if (max <= 0) return 1.0f;
        return cultist.getWorldAnchor() / max;
    }

    private void spawnChargeParticles(LivingEntity target, int relativeFrame) {
        if (!(cultist.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 eyePos = cultist.getEyePosition();
        Vec3 direction = getCastDirection(target, eyePos);
        Vec3 center = eyePos.add(direction.scale(CHARGE_DISTANCE));
        double progress = Math.min(1.0D, relativeFrame / (double) FIRE_TICK);
        double radius = MIN_CHARGE_RADIUS
            + (MAX_CHARGE_RADIUS - MIN_CHARGE_RADIUS) * progress;

        for (int i = 0; i < CHARGE_PARTICLE_COUNT; i++) {
            double y = cultist.getRandom().nextDouble() * 2.0D - 1.0D;
            double angle = cultist.getRandom().nextDouble() * Math.PI * 2.0D;
            double horizontal = Math.sqrt(1.0D - y * y);
            Vec3 offset = new Vec3(
                Math.cos(angle) * horizontal,
                y,
                Math.sin(angle) * horizontal
            ).scale(radius);

            // A zero count uses the offsets as the exact velocity of one particle.
            serverLevel.sendParticles(
                ParticleTypes.FLAME,
                center.x + offset.x, center.y + offset.y, center.z + offset.z,
                0,
                -offset.x, -offset.y, -offset.z,
                0.045D
            );
        }
        serverLevel.sendParticles(ParticleTypes.FLAME, center.x, center.y, center.z,
            1, 0.02D, 0.02D, 0.02D, 0.0D);
    }

    private Vec3 getCastDirection(LivingEntity target, Vec3 origin) {
        Vec3 targetPos = target.position().add(0, target.getEyeHeight() * 0.5D, 0);
        Vec3 direction = targetPos.subtract(origin);
        return direction.lengthSqr() < 1.0E-6D ? cultist.getLookAngle() : direction.normalize();
    }

    private void fireGhastFireball() {
        if (!(cultist.level() instanceof ServerLevel)) {
            return;
        }

        LivingEntity target = cultist.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        Vec3 eyePos = cultist.getEyePosition();
        Vec3 direction = getCastDirection(target, eyePos);
        Vec3 launchPos = eyePos.add(direction.scale(CHARGE_DISTANCE));

        LargeFireball fireball = new LargeFireball(
            cultist.level(), cultist,
            direction.x, direction.y, direction.z,
            0
        );
        fireball.setOwner(cultist);
        fireball.setPos(launchPos.x, launchPos.y, launchPos.z);
        cultist.level().addFreshEntity(fireball);

        cultist.level().playSound(null, cultist.blockPosition(),
            SoundEvents.GHAST_SHOOT, cultist.getSoundSource(), 1.0F, 1.0F);
    }
}
