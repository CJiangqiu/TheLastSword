package net.the_last_sword.entity.ai;

import net.eca.api.EcaAPI;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.damagesource.AbsoluteDestructionDamageSource;
import net.the_last_sword.entity.LostWraithEntity;
import net.the_last_sword.util.health.TrueHealthManager;
import net.the_last_sword.util.EntityUtil;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

// 迷失战魂终焉一击技能Goal
public class LostWraithEndStrikeGoal extends DangerousSkillGoal<LostWraithEntity> {
    private static final int ANIMATION_LENGTH = 80;
    private static final int PULL_START_TICK = 30;
    private static final int PULL_END_TICK = 70;
    private static final int DAMAGE_TICK = 70;
    private static final double ATTACK_DISTANCE = 4.0;
    private static final double TARGET_WIDTH = 5.0;
    private static final double TARGET_LENGTH = 4.0;
    private static final double TARGET_HEIGHT = 4.0;

    private final LostWraithEntity wraith;
    private int animationTick;
    private long cooldownEnd;
    private final List<TargetPositionData> pullTargets = new ArrayList<>();

    public LostWraithEndStrikeGoal(LostWraithEntity wraith) {
        super(wraith, TARGET_WIDTH, TARGET_LENGTH, TARGET_HEIGHT, PULL_START_TICK);
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
        return wraith.distanceTo(target) <= ATTACK_DISTANCE;
    }

    @Override
    public boolean canContinueToUse() {
        return animationTick > 0;
    }

    @Override
    protected void onDangerousSkillStart() {
        animationTick = ANIMATION_LENGTH;
        wraith.setAnimationState(LostWraithEntity.STATE_END_STRIKE);
        wraith.getNavigation().stop();
        pullTargets.clear();
        EntityUtil.faceTarget(wraith, wraith.getTarget());
    }

    @Override
    protected void tickDangerousSkill() {
        if (animationTick <= 0) {
            return;
        }

        int relativeFrame = ANIMATION_LENGTH - animationTick;

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

        if (animationTick == 0) {
            wraith.setAnimationState(LostWraithEntity.STATE_IDLE);
            cooldownEnd = wraith.level().getGameTime()
                + TheLastSwordConfiguration.getLostWraithEndStrikeCooldownSafely();
        }
    }

    @Override
    protected void onDangerousSkillStop() {
        animationTick = 0;
        pullTargets.clear();
        if (wraith.getAnimationState() == LostWraithEntity.STATE_END_STRIKE) {
            wraith.setAnimationState(LostWraithEntity.STATE_IDLE);
        }
    }

    private void markPullTargets() {
        Vec3 forward = getDangerousSkillForward();
        List<LivingEntity> targets = getDangerousSkillTargets();

        pullTargets.clear();
        Vec3 pullTarget = getDangerousSkillOrigin().add(forward);

        for (LivingEntity target : targets) {
            Vec3 safePullTarget = EntityUtil.findSafeTeleportPosition(target, pullTarget, 2);
            if (safePullTarget != null) {
                pullTargets.add(new TargetPositionData(target, safePullTarget));
            }
        }
    }

    private void updatePull() {
        pullTargets.removeIf(data -> !data.target.isAlive() || !EntityUtil.canAttack(wraith, data.target));
        for (TargetPositionData data : pullTargets) {
            data.target.teleportTo(data.position.x, data.position.y, data.position.z);
        }
    }

    private void dealDamage() {
        float lostHealth = TrueHealthManager.getMaxHealth(wraith) - TrueHealthManager.getHealth(wraith);
        float damage = lostHealth * (float) TheLastSwordConfiguration.getLostWraithEndStrikeDamageMultiplierSafely();

        for (TargetPositionData data : pullTargets) {
            if (data.target.isAlive() && EntityUtil.canAttack(wraith, data.target)) {
                if (isTargetBlocking(data.target)) {
                    data.target.level().playSound(null, data.target.blockPosition(),
                        SoundEvents.SHIELD_BLOCK, data.target.getSoundSource(),
                        1.0F, 0.8F + data.target.level().random.nextFloat() * 0.4F);

                    if (data.target instanceof Player player) {
                        player.getCooldowns().addCooldown(data.target.getUseItem().getItem(),
                            TheLastSwordConfiguration.getLostWraithEndStrikeShieldCooldownSafely());
                    }

                    Vec3 knockback = getDangerousSkillOrigin().subtract(data.target.position()).normalize().scale(-0.3);
                    data.target.setDeltaMovement(data.target.getDeltaMovement().add(knockback));
                } else {
                    if (EntityUtil.canAttack(wraith, data.target)) {
                        EcaAPI.hurt(data.target, AbsoluteDestructionDamageSource.absoluteDestruction(wraith), damage);
                    }
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

        if (useItem.is(Items.SHIELD)) {
            canBlock = player.getCooldowns().getCooldownPercent(useItem.getItem(), 0.0f) == 0.0f;
        } else if (useItem.getUseAnimation() == UseAnim.BLOCK) {
            canBlock = player.getCooldowns().getCooldownPercent(useItem.getItem(), 0.0f) == 0.0f;
        }

        if (!canBlock) {
            return false;
        }

        Vec3 toAttacker = getDangerousSkillOrigin().subtract(target.position()).normalize();
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
