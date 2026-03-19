package net.the_last_sword.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.damagesource.AbsoluteDestructionDamageSource;
import net.the_last_sword.entity.LostWraithEntity;
import net.the_last_sword.init.ModSounds;
import net.the_last_sword.util.EntityUtil;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

// 迷失战魂终焉一击技能Goal
public class LostWraithEndStrikeGoal extends Goal {
    private static final int ANIMATION_LENGTH = 80;
    private static final int TELEPORT_TICK = 5;
    private static final int SOUND_TICK = 10;
    private static final int PULL_START_TICK = 30;
    private static final int PULL_END_TICK = 70;
    private static final int DAMAGE_TICK = 70;

    private final LostWraithEntity wraith;
    private int animationTick;
    private final List<TargetPositionData> pullTargets = new ArrayList<>();

    public LostWraithEndStrikeGoal(LostWraithEntity wraith) {
        this.wraith = wraith;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!wraith.canAct()) {
            return false;
        }
        if (wraith.getAnimationState() != LostWraithEntity.STATE_IDLE) {
            return false;
        }
        LivingEntity target = wraith.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        return wraith.consumeForceEndStrike();
    }

    @Override
    public boolean canContinueToUse() {
        return animationTick > 0;
    }

    @Override
    public void start() {
        animationTick = ANIMATION_LENGTH;
        wraith.setAnimationState(LostWraithEntity.STATE_END_STRIKE);
        wraith.getNavigation().stop();
        pullTargets.clear();
        wraith.resetPatience();
    }

    @Override
    public void tick() {
        if (animationTick <= 0) {
            return;
        }

        int relativeFrame = ANIMATION_LENGTH - animationTick;

        if (relativeFrame == TELEPORT_TICK) {
            teleportToTarget();
        }

        if (relativeFrame == SOUND_TICK) {
            wraith.level().playSound(null, wraith.blockPosition(),
                ModSounds.ALARM.get(), wraith.getSoundSource(), 0.5F, 1.0F);
        }

        if (relativeFrame == PULL_START_TICK) {
            markPullTargets();
        }

        if (relativeFrame > PULL_START_TICK && relativeFrame < PULL_END_TICK) {
            updatePull();
        }

        if (relativeFrame == DAMAGE_TICK) {
            dealDamage();
        }

        animationTick--;

        LivingEntity target = wraith.getTarget();
        if (target != null && target.isAlive()) {
            wraith.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        if (animationTick == 0) {
            wraith.setAnimationState(LostWraithEntity.STATE_IDLE);
        }
    }

    @Override
    public void stop() {
        animationTick = 0;
        pullTargets.clear();
        if (wraith.getAnimationState() == LostWraithEntity.STATE_END_STRIKE) {
            wraith.setAnimationState(LostWraithEntity.STATE_IDLE);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private void teleportToTarget() {
        LivingEntity target = wraith.getTarget();
        if (target == null) return;

        Vec3 teleportPos = findRandomPositionAroundTarget(target);
        if (teleportPos == null) return;

        Vec3 oldPosition = wraith.position();

        if (wraith.level() instanceof ServerLevel serverLevel) {
            spawnTeleportParticles(serverLevel, oldPosition);
            wraith.level().playSound(null, BlockPos.containing(oldPosition),
                SoundEvents.ENDERMAN_TELEPORT, wraith.getSoundSource(), 1.0F, 1.0F);
        }

        EntityUtil.theLastEndTeleport(wraith, teleportPos.x, teleportPos.y, teleportPos.z);
        EntityUtil.faceTarget(wraith, target);

        if (wraith.level() instanceof ServerLevel serverLevel) {
            spawnTeleportParticles(serverLevel, teleportPos);
            wraith.level().playSound(null, BlockPos.containing(teleportPos),
                SoundEvents.ENDERMAN_TELEPORT, wraith.getSoundSource(), 1.0F, 1.0F);
        }
    }

    private Vec3 findRandomPositionAroundTarget(LivingEntity target) {
        Vec3 targetPos = target.position();
        double angle = wraith.getRandom().nextDouble() * 2 * Math.PI;
        double distance = 1.0;

        double x = targetPos.x + Math.cos(angle) * distance;
        double y = targetPos.y;
        double z = targetPos.z + Math.sin(angle) * distance;

        if (isSafeTeleportPosition(x, y, z)) {
            return new Vec3(x, y, z);
        }
        return new Vec3(targetPos.x, targetPos.y, targetPos.z);
    }

    private boolean isSafeTeleportPosition(double x, double y, double z) {
        BlockPos pos = BlockPos.containing(x, y, z);
        BlockPos posAbove = pos.above();
        return !wraith.level().getBlockState(pos).isSolid() &&
               !wraith.level().getBlockState(posAbove).isSolid();
    }

    private void spawnTeleportParticles(ServerLevel serverLevel, Vec3 position) {
        for (int i = 0; i < 32; i++) {
            double d0 = serverLevel.random.nextGaussian() * 0.02D;
            double d1 = serverLevel.random.nextGaussian() * 0.02D;
            double d2 = serverLevel.random.nextGaussian() * 0.02D;

            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                position.x + (serverLevel.random.nextDouble() - 0.5D) * 2.0D,
                position.y + serverLevel.random.nextDouble() * 2.0D,
                position.z + (serverLevel.random.nextDouble() - 0.5D) * 2.0D,
                1, d0, d1, d2, 0.1D);
        }
    }

    private void markPullTargets() {
        Vec3 forward = wraith.getLookAngle();
        Vec3 centerPos = wraith.position().add(forward.scale(2));

        AABB area = new AABB(centerPos.subtract(2, 2, 2), centerPos.add(2, 2, 2));
        List<LivingEntity> targets = wraith.level().getEntitiesOfClass(
            LivingEntity.class, area,
            entity1 -> EntityUtil.canAttack(wraith, entity1)
        );

        pullTargets.clear();
        Vec3 pullTarget = wraith.position().add(forward.scale(1));

        for (LivingEntity target : targets) {
            pullTargets.add(new TargetPositionData(target, pullTarget));
        }
    }

    private void updatePull() {
        pullTargets.removeIf(data -> !data.target.isAlive());
        for (TargetPositionData data : pullTargets) {
            data.target.teleportTo(data.position.x, data.position.y, data.position.z);
        }
    }

    private void dealDamage() {
        float lostHealth = wraith.getWorldAnchorMax() - wraith.getWorldAnchor();
        float damage = lostHealth * 0.1f;

        for (TargetPositionData data : pullTargets) {
            if (data.target.isAlive()) {
                if (isTargetBlocking(data.target)) {
                    data.target.level().playSound(null, data.target.blockPosition(),
                        SoundEvents.SHIELD_BLOCK, data.target.getSoundSource(),
                        1.0F, 0.8F + data.target.level().random.nextFloat() * 0.4F);

                    if (data.target instanceof Player player) {
                        player.getCooldowns().addCooldown(data.target.getUseItem().getItem(), 260);
                    }

                    Vec3 knockback = wraith.position().subtract(data.target.position()).normalize().scale(-0.3);
                    data.target.setDeltaMovement(data.target.getDeltaMovement().add(knockback));
                } else {
                    AbsoluteDestructionDamageSource.applyAbsoluteDestructionIntelligently(data.target, wraith, damage);
                }
            }
        }
        pullTargets.clear();
    }

    private boolean isTargetBlocking(LivingEntity target) {
        if (!(target instanceof Player player)) {
            return false;
        }
        if (!target.isUsingItem()) {
            return false;
        }

        var useItem = target.getUseItem();
        boolean canBlock = false;

        if (useItem.is(net.minecraft.world.item.Items.SHIELD)) {
            canBlock = player.getCooldowns().getCooldownPercent(useItem.getItem(), 0.0f) == 0.0f;
        } else if (useItem.getUseAnimation() == net.minecraft.world.item.UseAnim.BLOCK) {
            canBlock = player.getCooldowns().getCooldownPercent(useItem.getItem(), 0.0f) == 0.0f;
        }

        if (!canBlock) {
            return false;
        }

        Vec3 toAttacker = wraith.position().subtract(target.position()).normalize();
        Vec3 targetLook = target.getLookAngle();
        double dotProduct = targetLook.dot(toAttacker);
        return dotProduct > 0;
    }

    private static class TargetPositionData {
        public final LivingEntity target;
        public final Vec3 position;

        public TargetPositionData(LivingEntity target, Vec3 position) {
            this.target = target;
            this.position = position;
        }
    }
}
