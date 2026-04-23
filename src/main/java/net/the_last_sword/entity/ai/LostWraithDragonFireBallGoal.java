package net.the_last_sword.entity.ai;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.LostWraithEntity;
import net.the_last_sword.util.EntityUtil;

import java.util.EnumSet;

// 迷失战魂龙息弹技能Goal
public class LostWraithDragonFireBallGoal extends Goal {
    private static final int ANIMATION_LENGTH = 60;
    private static final int FIRE_TICK = 40;

    private final LostWraithEntity wraith;
    private int animationTick;
    private int cooldown;

    public LostWraithDragonFireBallGoal(LostWraithEntity wraith) {
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
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        LivingEntity target = wraith.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (wraith.isForceEndStrike()) {
            return false;
        }
        return wraith.distanceTo(target) > TheLastSwordConfiguration.getLostWraithDragonFireballMinDistanceSafely();
    }

    @Override
    public boolean canContinueToUse() {
        return animationTick > 0;
    }

    @Override
    public void start() {
        animationTick = ANIMATION_LENGTH;
        wraith.setAnimationState(LostWraithEntity.STATE_DRAGON_FIREBALL);
        wraith.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (animationTick <= 0) {
            return;
        }

        int relativeFrame = ANIMATION_LENGTH - animationTick;

        if (relativeFrame == FIRE_TICK) {
            fireDragonFireball();
        }

        animationTick--;

        LivingEntity target = wraith.getTarget();
        if (target != null && target.isAlive()) {
            wraith.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        if (animationTick == 0) {
            wraith.setAnimationState(LostWraithEntity.STATE_IDLE);
            cooldown = TheLastSwordConfiguration.getLostWraithDragonFireballCooldownSafely();
        }
    }

    @Override
    public void stop() {
        animationTick = 0;
        if (wraith.getAnimationState() == LostWraithEntity.STATE_DRAGON_FIREBALL) {
            wraith.setAnimationState(LostWraithEntity.STATE_IDLE);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private void fireDragonFireball() {
        if (!(wraith.level() instanceof ServerLevel)) {
            return;
        }

        LivingEntity target = wraith.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        Vec3 targetPos = target.position().add(0, target.getEyeHeight() * 0.5, 0);
        Vec3 startPos = wraith.position().add(0, wraith.getEyeHeight(), 0);
        Vec3 direction = targetPos.subtract(startPos).normalize();

        DragonFireball dragonFireball = new DragonFireball(
            wraith.level(), wraith,
            direction.x, direction.y, direction.z
        );
        dragonFireball.setPos(startPos.x, startPos.y, startPos.z);
        wraith.level().addFreshEntity(dragonFireball);

        wraith.level().playSound(null, wraith.blockPosition(),
            SoundEvents.ENDER_DRAGON_GROWL, wraith.getSoundSource(), 1.0F, 1.0F);
        wraith.level().playSound(null, wraith.blockPosition(),
            SoundEvents.ENDER_DRAGON_SHOOT, wraith.getSoundSource(), 1.0F, 1.0F);
    }
}
