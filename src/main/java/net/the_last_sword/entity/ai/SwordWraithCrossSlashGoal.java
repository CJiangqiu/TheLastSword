package net.the_last_sword.entity.ai;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.damagesource.AbsoluteDestructionDamageSource;
import net.the_last_sword.entity.TheLastEndSwordWraithEntity;
import net.the_last_sword.util.EntityUtil;
import net.the_last_sword.util.ParticleUtil;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

//十字切技能Goal
public class SwordWraithCrossSlashGoal extends Goal {
    private final TheLastEndSwordWraithEntity wraith;
    private int animationTick;
    private long lastUseTime;

    private static final int ANIMATION_LENGTH = 80;
    private static final int ANVIL_SOUND_TICK = 65;
    private static final int FIRST_SLASH_TICK = 25;
    private static final int SECOND_SLASH_TICK = 5;
    private static final int COOLDOWN = 200; // 10秒

    public SwordWraithCrossSlashGoal(TheLastEndSwordWraithEntity wraith) {
        this.wraith = wraith;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!wraith.canAct()) {
            return false;
        }
        if (wraith.getAnimationState() != TheLastEndSwordWraithEntity.STATE_IDLE) {
            return false;
        }

        LivingEntity target = wraith.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }

        //FORCE标记直接触发（无视冷却和距离）
        if (TheLastEndSwordWraithEntity.FORCE_CROSS_SLASH.getOrDefault(wraith.getUUID(), false)) {
            TheLastEndSwordWraithEntity.FORCE_CROSS_SLASH.remove(wraith.getUUID());
            return true;
        }

        //常规触发：无距离限制 + 冷却结束
        return wraith.level().getGameTime() - lastUseTime >= COOLDOWN;
    }

    @Override
    public void start() {
        animationTick = ANIMATION_LENGTH;
        wraith.setAnimationState(TheLastEndSwordWraithEntity.STATE_CROSS_SLASH);
        wraith.getNavigation().stop();
        lastUseTime = wraith.level().getGameTime();
    }

    @Override
    public void tick() {
        if (animationTick <= 0) {
            return;
        }

        if (animationTick == ANVIL_SOUND_TICK) {
            wraith.level().playSound(null, wraith.blockPosition(),
                SoundEvents.ANVIL_USE, wraith.getSoundSource(), 1.0F, 1.0F);
        }

        if (animationTick == FIRST_SLASH_TICK) {
            executeSlash();
        }

        if (animationTick == SECOND_SLASH_TICK) {
            executeSlash();
        }

        animationTick--;

        LivingEntity target = wraith.getTarget();
        if (target != null && target.isAlive()) {
            wraith.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        if (animationTick == 0) {
            wraith.setAnimationState(TheLastEndSwordWraithEntity.STATE_IDLE);
        }
    }

    @Override
    public boolean canContinueToUse() {
        return animationTick > 0;
    }

    @Override
    public void stop() {
        animationTick = 0;
        if (wraith.getAnimationState() == TheLastEndSwordWraithEntity.STATE_CROSS_SLASH) {
            wraith.setAnimationState(TheLastEndSwordWraithEntity.STATE_IDLE);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    //执行十字切攻击：传送到目标并攻击
    private void executeSlash() {
        LivingEntity target = wraith.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        //传送到目标周围
        double angle = wraith.getRandom().nextDouble() * 2 * Math.PI;
        double offsetX = Math.cos(angle) * 1.0;
        double offsetZ = Math.sin(angle) * 1.0;

        wraith.level().playSound(null, wraith.getX(), wraith.getY(), wraith.getZ(),
            SoundEvents.ENDERMAN_TELEPORT, wraith.getSoundSource(), 1.0F, 1.0F);
        ParticleUtil.spawnCrossSlashTeleportParticles(wraith.level(), wraith.position(), wraith.getBbHeight());

        Vec3 teleportPos = new Vec3(target.getX() + offsetX, target.getY(), target.getZ() + offsetZ);
        EntityUtil.theLastEndTeleport(wraith, teleportPos.x, teleportPos.y, teleportPos.z);
        EntityUtil.faceTarget(wraith, target);

        ParticleUtil.spawnCrossSlashTeleportParticles(wraith.level(), teleportPos, wraith.getBbHeight());

        //攻击主目标
        double attackRange = TheLastSwordConfiguration.getSkillCrossSlashRangeSafely();
        float damageMultiplier = (float) TheLastSwordConfiguration.getSkillCrossSlashDamageMultiplierSafely();
        float damage = (float) wraith.getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier;

        target.invulnerableTime = 0;
        AbsoluteDestructionDamageSource.applyAbsoluteDestruction(target, wraith, damage);
        wraith.addEndMark();

        //攻击范围内的其他敌人
        List<LivingEntity> nearbyTargets = EntityUtil.getTargetsInHemisphere(wraith, attackRange);
        for (LivingEntity nearbyTarget : nearbyTargets) {
            if (!nearbyTarget.equals(target)) {
                nearbyTarget.invulnerableTime = 0;
                AbsoluteDestructionDamageSource.applyAbsoluteDestruction(nearbyTarget, wraith, damage);
                wraith.addEndMark();
            }
        }
    }
}
