package net.the_last_sword.entity.ai;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.DragonCultPriestEntity;
import net.the_last_sword.util.EntityUtil;

import java.util.EnumSet;

// 祭司龙息Goal，attack动画0.65s起在身前3x3x3持续吐息，1.5s止
public class DragonCultPriestBreathGoal extends Goal {

    //attack 动画全长 2 秒
    private static final int ANIMATION_LENGTH = 40;

    //吐息起止帧（0.65s / 1.5s）
    private static final int BREATH_START_TICK = 13;
    private static final int BREATH_END_TICK = 30;

    //粒子在网格点周围的随机散布幅度
    private static final double PARTICLE_SPREAD = 0.25;

    private final DragonCultPriestEntity priest;
    private int animationTick;
    private long cooldownEnd;

    public DragonCultPriestBreathGoal(DragonCultPriestEntity priest) {
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

        LivingEntity target = priest.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        return priest.distanceTo(target) <= TheLastSwordConfiguration.getDragonCultPriestBreathMaxDistanceSafely();
    }

    @Override
    public boolean canContinueToUse() {
        return animationTick > 0;
    }

    @Override
    public void start() {
        animationTick = ANIMATION_LENGTH;
        priest.setAnimationState(DragonCultPriestEntity.STATE_BREATH);
        priest.getNavigation().stop();

        priest.level().playSound(null, priest.blockPosition(),
            SoundEvents.ENDER_DRAGON_GROWL, priest.getSoundSource(), 1.0F, 1.2F);
    }

    @Override
    public void tick() {
        if (animationTick <= 0) {
            return;
        }

        //吐息期间逐tick铺满粒子并结算伤害
        int frame = ANIMATION_LENGTH - animationTick;
        if (frame == BREATH_START_TICK) {
            priest.onSkillCast(DragonCultPriestEntity.PriestSkill.BREATH);
        }
        if (frame >= BREATH_START_TICK && frame <= BREATH_END_TICK) {
            spawnBreathParticles();
            hurtTargetsInBreath();
        }

        animationTick--;

        LivingEntity target = priest.getTarget();
        if (target != null && target.isAlive()) {
            priest.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        if (animationTick == 0) {
            priest.setAnimationState(DragonCultPriestEntity.STATE_IDLE);
            cooldownEnd = priest.level().getGameTime()
                + priest.scaleSkillCooldown(TheLastSwordConfiguration.getDragonCultPriestBreathCooldownSafely());
        }
    }

    @Override
    public void stop() {
        animationTick = 0;
        if (priest.getAnimationState() == DragonCultPriestEntity.STATE_BREATH) {
            priest.setAnimationState(DragonCultPriestEntity.STATE_IDLE);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    //按网格铺满整个立方体，使龙息在视觉上填满作用区域
    private void spawnBreathParticles() {
        if (!(priest.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        double size = TheLastSwordConfiguration.getDragonCultPriestBreathSizeSafely();
        double half = size * 0.5;
        int steps = Math.max(1, Mth.ceil(size));
        double step = size / steps;
        Vec3 center = breathCenter();

        for (int x = 0; x < steps; x++) {
            for (int y = 0; y < steps; y++) {
                for (int z = 0; z < steps; z++) {
                    double px = center.x - half + step * (x + 0.5);
                    double py = center.y - half + step * (y + 0.5);
                    double pz = center.z - half + step * (z + 0.5);
                    serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH, px, py, pz,
                            1, PARTICLE_SPREAD, PARTICLE_SPREAD, PARTICLE_SPREAD, 0.0);
                }
            }
        }
    }

    //每tick对吐息立方体内的可攻击目标造成按攻击力换算的龙息伤害
    private void hurtTargetsInBreath() {
        if (priest.level().isClientSide) {
            return;
        }

        double half = TheLastSwordConfiguration.getDragonCultPriestBreathSizeSafely() * 0.5;
        Vec3 center = breathCenter();
        AABB area = new AABB(center.subtract(half, half, half), center.add(half, half, half));

        float damage = (float) (priest.getAttributeValue(Attributes.ATTACK_DAMAGE)
            * TheLastSwordConfiguration.getDragonCultPriestBreathDamageMultiplierSafely()
            * priest.getSkillDamageMultiplier());
        if (damage <= 0) {
            return;
        }

        DamageSource damageSource = new DamageSource(
            priest.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(DamageTypes.DRAGON_BREATH),
            priest, priest);

        for (LivingEntity target : priest.level().getEntitiesOfClass(LivingEntity.class, area,
                entity -> EntityUtil.canAttack(priest, entity))) {
            //龙息为持续伤害，绕开无敌帧才能每tick生效
            target.invulnerableTime = 0;
            if (target.hurt(damageSource, damage)) {
                priest.onSuccessfulAttack(target);
            }
        }
    }

    //立方体中心：身前半个边长处，与身体中段等高
    private Vec3 breathCenter() {
        double size = TheLastSwordConfiguration.getDragonCultPriestBreathSizeSafely();
        Vec3 forward = Vec3.directionFromRotation(0.0F, priest.getYRot()).normalize();
        return priest.position()
            .add(forward.scale(size * 0.5))
            .add(0, priest.getBbHeight() * 0.5, 0);
    }
}
