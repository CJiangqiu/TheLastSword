package net.the_last_sword.entity.ai;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
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
    private static final int SOUND_TICK = 10;
    private static final int PULL_START_TICK = 30;
    private static final int PULL_END_TICK = 70;
    private static final int DAMAGE_TICK = 70;

    private final LostWraithEntity wraith;
    private int animationTick;
    private long cooldownEnd;
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
        if (wraith.level().getGameTime() < cooldownEnd) {
            return false;
        }
        return wraith.distanceTo(target)
            <= TheLastSwordConfiguration.getLostWraithPunchAttackDistanceSafely();
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
    }

    @Override
    public void tick() {
        if (animationTick <= 0) {
            return;
        }

        int relativeFrame = ANIMATION_LENGTH - animationTick;

        if (relativeFrame == SOUND_TICK
            && TheLastSwordConfiguration.getEntityDangerousSkillAlarmEnabledSafely()) {
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
            cooldownEnd = wraith.level().getGameTime()
                + TheLastSwordConfiguration.getLostWraithEndStrikeCooldownSafely();
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

    private void markPullTargets() {
        Vec3 forward = wraith.getLookAngle().multiply(1.0, 0.0, 1.0);
        if (forward.lengthSqr() < 1.0E-6) {
            return;
        }
        forward = forward.normalize();
        Vec3 centerPos = wraith.position().add(forward.scale(2));

        double pullRadius = TheLastSwordConfiguration.getLostWraithEndStrikePullRadiusSafely();
        AABB area = new AABB(centerPos.subtract(pullRadius, pullRadius, pullRadius), centerPos.add(pullRadius, pullRadius, pullRadius));
        List<LivingEntity> targets = wraith.level().getEntitiesOfClass(
            LivingEntity.class, area,
            entity1 -> EntityUtil.canAttack(wraith, entity1)
        );

        pullTargets.clear();
        Vec3 pullTarget = wraith.position().add(forward.scale(1));

        for (LivingEntity target : targets) {
            Vec3 safePullTarget = EntityUtil.findSafeTeleportPosition(target, pullTarget, 2);
            if (safePullTarget != null) {
                pullTargets.add(new TargetPositionData(target, safePullTarget));
            }
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
        float damage = lostHealth * (float) TheLastSwordConfiguration.getLostWraithEndStrikeDamageMultiplierSafely();

        for (TargetPositionData data : pullTargets) {
            if (data.target.isAlive()) {
                if (isTargetBlocking(data.target)) {
                    data.target.level().playSound(null, data.target.blockPosition(),
                        SoundEvents.SHIELD_BLOCK, data.target.getSoundSource(),
                        1.0F, 0.8F + data.target.level().random.nextFloat() * 0.4F);

                    if (data.target instanceof Player player) {
                        player.getCooldowns().addCooldown(data.target.getUseItem().getItem(),
                            TheLastSwordConfiguration.getLostWraithEndStrikeShieldCooldownSafely());
                    }

                    Vec3 knockback = wraith.position().subtract(data.target.position()).normalize().scale(-0.3);
                    data.target.setDeltaMovement(data.target.getDeltaMovement().add(knockback));
                } else {
                    AbsoluteDestructionDamageSource.applyAbsoluteDestruction(data.target, wraith, damage);
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
