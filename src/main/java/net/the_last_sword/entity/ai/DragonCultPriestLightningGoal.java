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
import net.the_last_sword.entity.DragonCultPriestEntity;
import net.the_last_sword.entity.DragonLightingEntity;
import net.the_last_sword.init.ModEntities;
import net.the_last_sword.util.EntityUtil;

import java.util.EnumSet;
import java.util.List;

// 祭司落雷Goal，heavy_attack动画1.75s记录目标位置，2.5s召唤龙之闪电
public class DragonCultPriestLightningGoal extends Goal {

    //heavy_attack 动画全长 3 秒
    private static final int ANIMATION_LENGTH = 60;

    //记录位置与落雷帧（1.75s / 2.5s）
    private static final int MARK_TICK = 35;
    private static final int STRIKE_TICK = 50;

    private final DragonCultPriestEntity priest;
    private int animationTick;
    private long cooldownEnd;
    private Vec3 strikePosition;

    public DragonCultPriestLightningGoal(DragonCultPriestEntity priest) {
        this.priest = priest;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!priest.canAct()) {
            return false;
        }
        if (priest.getAnimationState() != DragonCultPriestEntity.STATE_IDLE) {
            return false;
        }
        if (priest.level().getGameTime() < cooldownEnd) {
            return false;
        }

        //落雷不限距离，任何距离都可施放
        LivingEntity target = priest.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return animationTick > 0;
    }

    @Override
    public void start() {
        animationTick = ANIMATION_LENGTH;
        strikePosition = null;
        priest.setAnimationState(DragonCultPriestEntity.STATE_LIGHTNING);
        priest.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (animationTick <= 0) {
            return;
        }

        int frame = ANIMATION_LENGTH - animationTick;
        if (frame == MARK_TICK) {
            markTargetPosition();
        } else if (frame == STRIKE_TICK) {
            summonLightning();
        }

        animationTick--;

        //记录位置前保持追踪，记录后锁定不再转向
        LivingEntity target = priest.getTarget();
        if (frame < MARK_TICK && target != null && target.isAlive()) {
            priest.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        if (animationTick == 0) {
            priest.setAnimationState(DragonCultPriestEntity.STATE_IDLE);
            cooldownEnd = priest.level().getGameTime()
                + priest.scaleSkillCooldown(TheLastSwordConfiguration.getDragonCultPriestLightningCooldownSafely());
        }
    }

    @Override
    public void stop() {
        animationTick = 0;
        strikePosition = null;
        if (priest.getAnimationState() == DragonCultPriestEntity.STATE_LIGHTNING) {
            priest.setAnimationState(DragonCultPriestEntity.STATE_IDLE);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private void markTargetPosition() {
        LivingEntity target = priest.getTarget();
        strikePosition = target != null ? target.position() : priest.position();
    }

    private void summonLightning() {
        if (!(priest.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        priest.onSkillCast(DragonCultPriestEntity.PriestSkill.LIGHTNING);

        Vec3 pos = strikePosition != null ? strikePosition : priest.position();

        DragonLightingEntity lightning = new DragonLightingEntity(
            ModEntities.DRAGON_LIGHTING.get(), serverLevel
        );
        lightning.moveTo(pos.x, pos.y, pos.z);
        serverLevel.addFreshEntity(lightning);

        double aoeRadius = TheLastSwordConfiguration.getDragonCultPriestLightningAoeRadiusSafely();
        AABB area = new AABB(pos.subtract(aoeRadius, aoeRadius, aoeRadius), pos.add(aoeRadius, aoeRadius, aoeRadius));
        List<LivingEntity> targets = priest.level().getEntitiesOfClass(
            LivingEntity.class, area,
            entity -> EntityUtil.canAttack(priest, entity)
        );

        DamageSource damageSource = new DamageSource(
            priest.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(DamageTypes.LIGHTNING_BOLT),
            priest, priest);

        float damage = (float) (priest.getAttributeValue(Attributes.ATTACK_DAMAGE)
            * TheLastSwordConfiguration.getDragonCultPriestLightningDamageMultiplierSafely()
            * priest.getSkillDamageMultiplier());
        for (LivingEntity target : targets) {
            if (target.hurt(damageSource, damage)) {
                priest.onSuccessfulAttack(target);
            }
        }
    }
}
