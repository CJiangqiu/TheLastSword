package net.the_last_sword.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.entity.LostWraithEntity;
import net.the_last_sword.util.EntityUtil;

import java.util.EnumSet;
import java.util.List;

// 迷失战魂拳击技能Goal
public class LostWraithPunchGoal extends Goal {
    private static final int ANIMATION_LENGTH = 30;
    private static final int TELEPORT_TICK = 5;
    private static final int SOUND_TICK = 10;
    private static final int DAMAGE_TICK = 24;
    private static final double ATTACK_DISTANCE = 4.0;

    private final LostWraithEntity wraith;
    private int animationTick;

    public LostWraithPunchGoal(LostWraithEntity wraith) {
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
        if (wraith.isForceEndStrike()) {
            return false;
        }
        return wraith.distanceTo(target) <= ATTACK_DISTANCE;
    }

    @Override
    public boolean canContinueToUse() {
        return animationTick > 0;
    }

    @Override
    public void start() {
        animationTick = ANIMATION_LENGTH;
        wraith.setAnimationState(LostWraithEntity.STATE_PUNCH);
        wraith.getNavigation().stop();
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
            wraith.level().playSound(null,
                wraith.getX(), wraith.getY(), wraith.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP,
                wraith.getSoundSource(), 1.0F, 1.0F);
        }

        if (relativeFrame == DAMAGE_TICK) {
            executePunchDamage();
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
        if (wraith.getAnimationState() == LostWraithEntity.STATE_PUNCH) {
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

    private void executePunchDamage() {
        Vec3 forward = wraith.getLookAngle();
        Vec3 pos = wraith.position();
        float damage = (float) wraith.getAttributeValue(Attributes.ATTACK_DAMAGE);

        DamageSource damageSource = new DamageSource(
            wraith.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(DamageTypes.GENERIC),
            wraith, wraith);

        for (int i = 1; i <= 4; i++) {
            for (int j = -1; j <= 1; j++) {
                Vec3 right = forward.cross(new Vec3(0, 1, 0)).normalize();
                Vec3 checkPos = pos.add(forward.scale(i)).add(right.scale(j));

                AABB area = new AABB(checkPos.subtract(0.5, 0.5, 0.5), checkPos.add(0.5, 0.5, 0.5));
                List<LivingEntity> targets = wraith.level().getEntitiesOfClass(
                    LivingEntity.class, area,
                    entity1 -> EntityUtil.canAttack(wraith, entity1)
                );

                for (LivingEntity target : targets) {
                    if (isTargetBlocking(target)) {
                        target.level().playSound(null, target.blockPosition(),
                            SoundEvents.SHIELD_BLOCK, target.getSoundSource(),
                            1.0F, 0.8F + target.level().random.nextFloat() * 0.4F);
                        Vec3 knockbackDir = target.position().subtract(wraith.position()).normalize();
                        target.knockback(0.5F, knockbackDir.x, knockbackDir.z);
                    } else {
                        target.hurt(damageSource, damage);
                    }
                }
            }
        }
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
        } else if (useItem.getUseAnimation() == UseAnim.BLOCK) {
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
}
