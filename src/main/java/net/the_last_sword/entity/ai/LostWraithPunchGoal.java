package net.the_last_sword.entity.ai;

import net.minecraft.core.registries.Registries;
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
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.LostWraithEntity;
import net.the_last_sword.util.EntityUtil;
import net.the_last_sword.util.ParticleUtil;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// 迷失战魂拳击技能Goal
public class LostWraithPunchGoal extends Goal {
    private static final int ANIMATION_LENGTH = 30;
    private static final int SOUND_TICK = 10;
    private static final int DAMAGE_TICK = 24;
    private static final double TELEPORT_DISTANCE = 2.0;

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
        return wraith.distanceTo(target) <= TheLastSwordConfiguration.getLostWraithPunchAttackDistanceSafely();
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
        teleportInFrontOfTarget();
    }

    @Override
    public void tick() {
        if (animationTick <= 0) {
            return;
        }

        int relativeFrame = ANIMATION_LENGTH - animationTick;

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

    private void executePunchDamage() {
        Vec3 forward = wraith.getLookAngle().multiply(1.0, 0.0, 1.0);
        if (forward.lengthSqr() < 1.0E-6) {
            LivingEntity target = wraith.getTarget();
            if (target != null) {
                forward = target.position().subtract(wraith.position()).multiply(1.0, 0.0, 1.0);
            }
        }
        if (forward.lengthSqr() < 1.0E-6) {
            return;
        }
        forward = forward.normalize();

        Vec3 pos = wraith.position();
        Vec3 right = new Vec3(-forward.z, 0.0, forward.x);
        float damage = (float) wraith.getAttributeValue(Attributes.ATTACK_DAMAGE);

        DamageSource damageSource = new DamageSource(
            wraith.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(DamageTypes.GENERIC),
            wraith, wraith);

        int forwardSteps = TheLastSwordConfiguration.getLostWraithPunchForwardStepsSafely();
        int sideHalfWidth = TheLastSwordConfiguration.getLostWraithPunchSideHalfWidthSafely();
        Set<LivingEntity> targetsHit = new HashSet<>();
        for (int i = 1; i <= forwardSteps; i++) {
            for (int j = -sideHalfWidth; j <= sideHalfWidth; j++) {
                Vec3 checkPos = pos.add(forward.scale(i)).add(right.scale(j));

                AABB area = new AABB(
                    checkPos.x - 0.75, pos.y - 0.5, checkPos.z - 0.75,
                    checkPos.x + 0.75, pos.y + wraith.getBbHeight() + 0.5, checkPos.z + 0.75
                );
                List<LivingEntity> targets = wraith.level().getEntitiesOfClass(
                    LivingEntity.class, area,
                    entity1 -> EntityUtil.canAttack(wraith, entity1)
                );

                for (LivingEntity target : targets) {
                    if (!targetsHit.add(target)) {
                        continue;
                    }
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

    //传送到目标面朝方向的前方，让拳击主动贴近目标
    private void teleportInFrontOfTarget() {
        LivingEntity target = wraith.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        Vec3 direction = target.getLookAngle().multiply(1.0, 0.0, 1.0);
        if (direction.lengthSqr() < 1.0E-6) {
            direction = wraith.position().subtract(target.position()).multiply(1.0, 0.0, 1.0);
        }
        if (direction.lengthSqr() < 1.0E-6) {
            direction = new Vec3(0.0, 0.0, 1.0);
        } else {
            direction = direction.normalize();
        }

        Vec3 oldPosition = wraith.position();
        Vec3 desiredPosition = target.position().add(direction.scale(TELEPORT_DISTANCE));
        Vec3 teleportPosition = EntityUtil.findSafeTeleportPosition(wraith, desiredPosition, 4);
        if (teleportPosition == null) {
            return;
        }
        if (!EntityUtil.theLastEndTeleport(
                wraith, teleportPosition.x, teleportPosition.y, teleportPosition.z)) {
            return;
        }

        ParticleUtil.spawnTeleportParticles(
            wraith.level(), oldPosition, teleportPosition, wraith.getBbHeight());
        wraith.level().playSound(null, oldPosition.x, oldPosition.y, oldPosition.z,
            SoundEvents.ENDERMAN_TELEPORT, wraith.getSoundSource(), 1.0F, 1.0F);
        wraith.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        EntityUtil.faceTarget(wraith, target);
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
