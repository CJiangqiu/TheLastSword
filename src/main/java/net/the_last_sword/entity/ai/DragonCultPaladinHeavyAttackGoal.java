package net.the_last_sword.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.DragonCultPaladinEntity;
import net.the_last_sword.entity.util.GroundRuptureEffect;
import net.the_last_sword.init.ModSounds;

import java.util.EnumSet;

// 圣骑士重击Goal，heavy_attack动画1.65s结算高倍伤害并破盾
public class DragonCultPaladinHeavyAttackGoal extends Goal {

    //heavy_attack 动画全长 2.6 秒
    private static final int ANIMATION_LENGTH = 52;

    //伤害帧（1.65s）
    private static final int DAMAGE_TICK = 33;
    private static final double KNOCKBACK_STRENGTH = 0.4D;

    private final DragonCultPaladinEntity paladin;
    private int animationTick;
    private long cooldownEnd;

    public DragonCultPaladinHeavyAttackGoal(DragonCultPaladinEntity paladin) {
        this.paladin = paladin;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!paladin.canAct()) {
            return false;
        }
        if (paladin.getAnimationState() != DragonCultPaladinEntity.STATE_IDLE) {
            return false;
        }
        if (paladin.level().getGameTime() < cooldownEnd) {
            return false;
        }

        LivingEntity target = paladin.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        return paladin.distanceTo(target) <= TheLastSwordConfiguration.getDragonCultPaladinHeavyAttackRangeSafely();
    }

    @Override
    public boolean canContinueToUse() {
        return animationTick > 0;
    }

    @Override
    public void start() {
        animationTick = ANIMATION_LENGTH;
        paladin.setAnimationState(DragonCultPaladinEntity.STATE_HEAVY_ATTACK);
        paladin.getNavigation().stop();
        if (TheLastSwordConfiguration.getEntityDangerousSkillAlarmEnabledSafely()) {
            paladin.level().playSound(null, paladin.blockPosition(),
                ModSounds.ALARM.get(), paladin.getSoundSource(), 0.5F, 1.0F);
        }
    }

    @Override
    public void tick() {
        if (animationTick <= 0) {
            return;
        }

        int frame = ANIMATION_LENGTH - animationTick;
        if (frame == DAMAGE_TICK) {
            triggerHeavyImpact();
            dealDamage();
        }
        animationTick--;

        LivingEntity target = paladin.getTarget();
        if (target != null && target.isAlive()) {
            paladin.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        if (animationTick == 0) {
            paladin.setAnimationState(DragonCultPaladinEntity.STATE_IDLE);
            cooldownEnd = paladin.level().getGameTime()
                + TheLastSwordConfiguration.getDragonCultPaladinHeavyAttackCooldownSafely();
        }
    }

    @Override
    public void stop() {
        animationTick = 0;
        if (paladin.getAnimationState() == DragonCultPaladinEntity.STATE_HEAVY_ATTACK) {
            paladin.setAnimationState(DragonCultPaladinEntity.STATE_IDLE);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private void dealDamage() {
        LivingEntity target = paladin.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        if (paladin.distanceTo(target) > TheLastSwordConfiguration.getDragonCultPaladinHeavyAttackRangeSafely()) {
            return;
        }

        //先破盾再结算，否则伤害会被格挡吃掉
        disableBlocking(target);

        float damage = (float) (paladin.getAttributeValue(Attributes.ATTACK_DAMAGE)
            * TheLastSwordConfiguration.getDragonCultPaladinHeavyAttackDamageMultiplierSafely());
        if (damage <= 0) {
            return;
        }

        target.invulnerableTime = 0;
        if (target.hurt(paladin.damageSources().mobAttack(paladin), damage)) {
            target.knockback(KNOCKBACK_STRENGTH,
                paladin.getX() - target.getX(), paladin.getZ() - target.getZ());
        }
    }

    private void triggerHeavyImpact() {
        Vec3 impactCenter = getImpactCenter();
        paladin.level().playSound(null, BlockPos.containing(impactCenter),
            SoundEvents.GENERIC_EXPLODE, paladin.getSoundSource(), 1.0F, 1.0F);
        if (paladin.level() instanceof ServerLevel serverLevel) {
            GroundRuptureEffect.spawn(serverLevel, impactCenter, paladin.getRandom());
        }
    }

    private Vec3 getImpactCenter() {
        LivingEntity target = paladin.getTarget();
        double attackRange = TheLastSwordConfiguration.getDragonCultPaladinHeavyAttackRangeSafely();
        if (target != null && target.isAlive() && paladin.distanceTo(target) <= attackRange) {
            return target.position();
        }

        Vec3 forward = paladin.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        if (forward.lengthSqr() < 1.0E-6D) {
            forward = new Vec3(0.0D, 0.0D, 1.0D);
        } else {
            forward = forward.normalize();
        }
        double impactDistance = Math.min(2.0D,
            TheLastSwordConfiguration.getDragonCultPaladinHeavyAttackRangeSafely());
        return paladin.position().add(forward.scale(impactDistance));
    }

    //中断格挡，玩家额外给正在使用的物品挂冷却（isBlocking 通用判断，模组盾牌同样生效）
    private void disableBlocking(LivingEntity target) {
        if (!target.isBlocking()) {
            return;
        }

        ItemStack blockingStack = target.getUseItem();
        target.stopUsingItem();

        if (!(target instanceof Player player)) {
            return;
        }

        int disableTime = TheLastSwordConfiguration.getDragonCultPaladinHeavyAttackShieldDisableTimeSafely();
        if (disableTime > 0) {
            player.getCooldowns().addCooldown(blockingStack.getItem(), disableTime);
        }
        //事件30：播放盾牌破防音效与动作
        player.level().broadcastEntityEvent(player, (byte) 30);
    }
}
