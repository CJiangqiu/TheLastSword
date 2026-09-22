package net.the_last_sword.entity.ai;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.LostWraithEntity;
import net.the_last_sword.util.EntityUtil;
import net.the_last_sword.util.ParticleUtil;

import java.util.EnumSet;
import java.util.List;

// 迷失战魂拳击技能Goal
public class LostWraithPunchGoal extends Goal {
    private static final int ANIMATION_LENGTH = 30;
    private static final int SOUND_TICK = 10;
    private static final int DAMAGE_TICK = 15;
    private static final double ATTACK_DISTANCE = 4.0;
    private static final double TELEPORT_DISTANCE = 2.0;
    private static final double ATTACK_WIDTH = 3.0;
    private static final double ATTACK_LENGTH = 4.0;
    private static final double ATTACK_HEIGHT = 4.0;

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
        double distance = wraith.distanceTo(target);
        // 传送就绪时远距离可突进，但近距离仍必须允许普通拳击；否则会与
        // “大于4格才追击/释放远程技能”的条件形成没有任何Goal可运行的死区。
        return wraith.isPunchTeleportReady() || distance <= ATTACK_DISTANCE;
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
        // 仅远距离起手才使用传送；近距离直接出拳并保留传送就绪状态。
        LivingEntity target = wraith.getTarget();
        boolean shouldTeleport = wraith.isPunchTeleportReady()
            && target != null
            && target.isAlive()
            && wraith.distanceTo(target) > ATTACK_DISTANCE;
        if (shouldTeleport && teleportInFrontOfTarget()) {
            wraith.startPunchTeleportCooldown();
        }
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

        //伤害前一帧转身，确保拳击正面朝向目标
        if (relativeFrame == DAMAGE_TICK - 1) {
            LivingEntity target = wraith.getTarget();
            if (target != null && target.isAlive()) {
                EntityUtil.faceTarget(wraith, target);
            }
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
        float damage = (float) wraith.getAttributeValue(Attributes.ATTACK_DAMAGE);

        DamageSource damageSource = new DamageSource(
            wraith.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(DamageTypes.GENERIC),
            wraith, wraith);

        List<LivingEntity> targets = EntityUtil.getTargetsInFrontBox(
            wraith, ATTACK_WIDTH, ATTACK_LENGTH, ATTACK_HEIGHT);
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

    //传送到目标面朝方向的前方，让拳击主动贴近目标；传送成功返回true
    private boolean teleportInFrontOfTarget() {
        LivingEntity target = wraith.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
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
        Vec3 teleportPosition = EntityUtil.theLastEndSafeTeleport(wraith, desiredPosition, 4);
        if (teleportPosition == null) {
            return false;
        }

        ParticleUtil.spawnTeleportParticles(
            wraith.level(), oldPosition, teleportPosition, wraith.getBbHeight());
        wraith.level().playSound(null, oldPosition.x, oldPosition.y, oldPosition.z,
            SoundEvents.ENDERMAN_TELEPORT, wraith.getSoundSource(), 1.0F, 1.0F);
        wraith.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        EntityUtil.faceTarget(wraith, target);
        return true;
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

        if (useItem.is(Items.SHIELD)) {
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
