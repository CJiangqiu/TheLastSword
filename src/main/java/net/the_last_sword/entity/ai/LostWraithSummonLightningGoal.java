package net.the_last_sword.entity.ai;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.DragonLightingEntity;
import net.the_last_sword.entity.LostWraithEntity;
import net.the_last_sword.init.ModEntities;
import net.the_last_sword.util.EntityUtil;

import java.util.EnumSet;
import java.util.List;

// 迷失战魂召唤闪电技能Goal
public class LostWraithSummonLightningGoal extends Goal {
    private static final int ANIMATION_LENGTH = 60;
    private static final int MARK_TICK = 25;
    private static final int LIGHTNING_TICK = 40;

    private final LostWraithEntity wraith;
    private int animationTick;
    private int cooldown;
    private Vec3 lightningTargetPosition;

    public LostWraithSummonLightningGoal(LostWraithEntity wraith) {
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
        return wraith.distanceTo(target) > TheLastSwordConfiguration.getLostWraithLightningMinDistanceSafely();
    }

    @Override
    public boolean canContinueToUse() {
        return animationTick > 0;
    }

    @Override
    public void start() {
        animationTick = ANIMATION_LENGTH;
        wraith.setAnimationState(LostWraithEntity.STATE_LIGHTNING);
        wraith.getNavigation().stop();
        lightningTargetPosition = null;
    }

    @Override
    public void tick() {
        if (animationTick <= 0) {
            return;
        }

        int relativeFrame = ANIMATION_LENGTH - animationTick;

        if (relativeFrame == MARK_TICK) {
            markTargetPosition();
        }

        if (relativeFrame == LIGHTNING_TICK) {
            summonLightning();
        }

        animationTick--;

        LivingEntity target = wraith.getTarget();
        if (target != null && target.isAlive()) {
            wraith.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        if (animationTick == 0) {
            wraith.setAnimationState(LostWraithEntity.STATE_IDLE);
            cooldown = TheLastSwordConfiguration.getLostWraithLightningCooldownSafely();
        }
    }

    @Override
    public void stop() {
        animationTick = 0;
        lightningTargetPosition = null;
        if (wraith.getAnimationState() == LostWraithEntity.STATE_LIGHTNING) {
            wraith.setAnimationState(LostWraithEntity.STATE_IDLE);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private void markTargetPosition() {
        LivingEntity target = wraith.getTarget();
        lightningTargetPosition = target != null ? target.position() : wraith.position();
    }

    private void summonLightning() {
        if (!(wraith.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 targetPos = lightningTargetPosition != null ? lightningTargetPosition : wraith.position();

        DragonLightingEntity lightning = new DragonLightingEntity(
            ModEntities.DRAGON_LIGHTING.get(), serverLevel
        );
        lightning.moveTo(targetPos.x, targetPos.y, targetPos.z);
        serverLevel.addFreshEntity(lightning);

        double aoeRadius = TheLastSwordConfiguration.getLostWraithLightningAoeRadiusSafely();
        AABB area = new AABB(targetPos.subtract(aoeRadius, aoeRadius, aoeRadius), targetPos.add(aoeRadius, aoeRadius, aoeRadius));
        List<LivingEntity> targets = wraith.level().getEntitiesOfClass(
            LivingEntity.class, area,
            entity1 -> EntityUtil.canAttack(wraith, entity1)
        );

        float damage = (float) wraith.getAttributeValue(Attributes.ATTACK_DAMAGE)
            * (float) TheLastSwordConfiguration.getLostWraithLightningDamageMultiplierSafely();
        DamageSource damageSource = new DamageSource(
            wraith.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(DamageTypes.LIGHTNING_BOLT),
            wraith, wraith);

        for (LivingEntity target : targets) {
            target.hurt(damageSource, damage);
        }
    }
}
