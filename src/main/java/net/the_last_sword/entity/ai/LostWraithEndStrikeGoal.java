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
import net.the_last_sword.network.LostWraithEndStrikeEffectPacket;
import net.the_last_sword.network.NetworkHandler;
import net.the_last_sword.util.health.TrueHealthManager;
import net.the_last_sword.util.EntityUtil;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

// 迷失战魂终焉一击技能Goal
public class LostWraithEndStrikeGoal extends DangerousSkillGoal<LostWraithEntity> {
    private static final int ANIMATION_LENGTH = 80;
    private static final int PULL_START_TICK = 30;
    private static final int DAMAGE_TICK = 70;
    private static final double ATTACK_DISTANCE = 4.0;
    private static final double TARGET_WIDTH = 5.0;
    private static final double TARGET_LENGTH = 4.0;
    private static final double TARGET_HEIGHT = 4.0;

    private final LostWraithEntity wraith;
    private int animationTick;
    private long cooldownEnd;
    private final List<LivingEntity> pullTargets = new ArrayList<>();
    private Vec3 effectPosition;
    private long effectStartTick;
    private long effectDamageTick;
    private long effectEndTick;
    private boolean effectActive;

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
        effectActive = false;
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
            startEndStrikeEffect();
        } else if (effectActive && relativeFrame < ANIMATION_LENGTH && relativeFrame % 5 == 0) {
            syncEndStrikeEffect(true);
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
        stopEndStrikeEffect();
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
                pullTargets.add(target);
                lockDangerousSkillTarget(target, safePullTarget);
            }
        }
    }

    private void startEndStrikeEffect() {
        Vec3 forward = getDangerousSkillForward();
        effectPosition = getDangerousSkillOrigin()
                .add(0.0, wraith.getBbHeight() * 0.5, 0.0)
                .add(forward);
        effectStartTick = wraith.level().getGameTime();
        effectDamageTick = effectStartTick + DAMAGE_TICK - PULL_START_TICK;
        effectEndTick = effectStartTick + ANIMATION_LENGTH - PULL_START_TICK;
        effectActive = true;
        syncEndStrikeEffect(true);
    }

    private void stopEndStrikeEffect() {
        if (!effectActive) {
            return;
        }
        syncEndStrikeEffect(false);
        effectActive = false;
    }

    private void syncEndStrikeEffect(boolean active) {
        NetworkHandler.sendToTrackingClients(new LostWraithEndStrikeEffectPacket(
                wraith.level().dimension().location(), wraith.getId(), wraith.getUUID(),
                effectPosition, effectStartTick, effectDamageTick, effectEndTick, active), wraith);
    }

    private void dealDamage() {
        float lostHealth = TrueHealthManager.getMaxHealth(wraith) - TrueHealthManager.getHealth(wraith);
        float damage = lostHealth * (float) TheLastSwordConfiguration.getLostWraithEndStrikeDamageMultiplierSafely();

        for (LivingEntity target : pullTargets) {
            if (target.isAlive() && EntityUtil.canAttack(wraith, target)) {
                if (isTargetBlocking(target)) {
                    target.level().playSound(null, target.blockPosition(),
                        SoundEvents.SHIELD_BLOCK, target.getSoundSource(),
                        1.0F, 0.8F + target.level().random.nextFloat() * 0.4F);

                    if (target instanceof Player player) {
                        player.getCooldowns().addCooldown(target.getUseItem().getItem(),
                            TheLastSwordConfiguration.getLostWraithEndStrikeShieldCooldownSafely());
                    }

                    Vec3 knockback = getDangerousSkillOrigin().subtract(target.position()).normalize().scale(-0.3);
                    target.setDeltaMovement(target.getDeltaMovement().add(knockback));
                } else {
                    if (EntityUtil.canAttack(wraith, target)) {
                        EcaAPI.hurt(target, AbsoluteDestructionDamageSource.absoluteDestruction(wraith), damage);
                    }
                }
            }
        }
        unlockAllDangerousSkillTargets();
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

}
