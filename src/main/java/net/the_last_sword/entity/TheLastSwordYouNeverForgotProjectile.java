package net.the_last_sword.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.registries.ForgeRegistries;
import net.the_last_sword.damagesource.AbsoluteDestructionDamageSource;
import net.the_last_sword.init.ModEntities;
import net.the_last_sword.item.TheLastSwordYouNeverForgot;

// 隐藏武器弹射物 - 使用隐藏剑的自定义attack逻辑
public class TheLastSwordYouNeverForgotProjectile extends TheLastEndSwordProjectile {

    public TheLastSwordYouNeverForgotProjectile(EntityType<? extends TheLastEndSwordProjectile> type, Level world) {
        super(type, world);
    }

    public TheLastSwordYouNeverForgotProjectile(EntityType<? extends TheLastEndSwordProjectile> type, LivingEntity entity, Level world) {
        super(type, entity, world, entity.getUUID());
    }

    public TheLastSwordYouNeverForgotProjectile(PlayMessages.SpawnEntity packet, Level world) {
        this(ModEntities.THE_LAST_SWORD_YOU_NEVER_FORGOT_PROJECTILE.get(), world);
    }

    // 不造成物理伤害
    @Override
    protected void applyBaseDamage(Entity target) {
    }

    // 使用隐藏剑的attack方法
    @Override
    protected void applyExtraDamage(Entity target) {
        if (snapshotExtraDamage <= 0) return;
        if (target instanceof LivingEntity living) {
            TheLastSwordYouNeverForgot.attack(living, this.getOwner(), snapshotExtraDamage);
        } else if (this.getOwner() != null) {
            target.hurt(AbsoluteDestructionDamageSource.absoluteDestruction(this.getOwner()), snapshotExtraDamage);
        }
    }

    // 发射
    public static TheLastSwordYouNeverForgotProjectile shoot(Level world, LivingEntity shooter, RandomSource random, float extraDamage) {
        TheLastSwordYouNeverForgotProjectile projectile = new TheLastSwordYouNeverForgotProjectile(
                ModEntities.THE_LAST_SWORD_YOU_NEVER_FORGOT_PROJECTILE.get(), shooter, world);
        projectile.shoot(shooter.getViewVector(1).x, shooter.getViewVector(1).y, shooter.getViewVector(1).z, 4.0f, 0);
        projectile.setSilent(true);
        projectile.setSnapshotDamage(0, extraDamage);
        world.addFreshEntity(projectile);
        world.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.ender_dragon.shoot")),
                SoundSource.PLAYERS, 1,
                1f / (random.nextFloat() * 0.5f + 1) + 0.75f);
        return projectile;
    }
}
