package net.the_last_sword.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.init.ModEntities;
import net.the_last_sword.init.ModItems;
import net.the_last_sword.util.nbt.ItemLevelHelper;
import net.minecraftforge.network.PlayMessages;

import java.util.UUID;

/**
 * 龙之剑弹射物
 * 击中实体时造成物理伤害和龙息伤害，并生成闪电特效
 */
public class DragonSwordProjectile extends TheLastEndSwordItemsProjectile {
    public static final ItemStack PROJECTILE_ITEM = new ItemStack(ModItems.DRAGON_CRYSTAL.get());

    //用于网络生成实体的构造器
    public DragonSwordProjectile(PlayMessages.SpawnEntity packet, Level world) {
        super(ModEntities.DRAGON_SWORD_PROJECTILE.get(), world);
    }

    public DragonSwordProjectile(EntityType<? extends DragonSwordProjectile> type, Level world) {
        super(type, world);
    }

    public DragonSwordProjectile(EntityType<? extends DragonSwordProjectile> type, LivingEntity entity, Level world, UUID shooterUUID) {
        super(type, entity, world, shooterUUID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ItemStack getItem() {
        return PROJECTILE_ITEM;
    }

    //造成基础物理伤害（使用发射时快照）
    @Override
    protected void applyBaseDamage(Entity target) {
        if (this.getOwner() instanceof LivingEntity owner && snapshotBaseDamage > 0) {
            DamageSource projectileDamageSource = new DamageSource(
                this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                    .getHolderOrThrow(DamageTypes.GENERIC),
                this, owner
            );
            target.hurt(projectileDamageSource, snapshotBaseDamage);
        }
    }

    //造成额外龙息伤害（使用发射时快照）
    @Override
    protected void applyExtraDamage(Entity target) {
        if (this.getOwner() instanceof LivingEntity owner && snapshotExtraDamage > 0) {
            DamageSource dragonBreathDamageSource = new DamageSource(
                this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                    .getHolderOrThrow(DamageTypes.DRAGON_BREATH),
                this, owner
            );
            target.hurt(dragonBreathDamageSource, snapshotExtraDamage);
        }
    }

    //生成龙之闪电视觉效果
    @Override
    protected void applyVisualEffect(Entity target) {
        if (this.level() instanceof ServerLevel serverLevel) {
            DragonLightingEntity lightning = new DragonLightingEntity(ModEntities.DRAGON_LIGHTING.get(), serverLevel);
            lightning.moveTo(Vec3.atBottomCenterOf(BlockPos.containing(this.getX(), this.getY(), this.getZ())));
            lightning.setVisualOnly(true);
            serverLevel.addFreshEntity(lightning);
        }
    }

    //发射弹射物的静态方法
    public static DragonSwordProjectile shoot(Level world, LivingEntity entity, RandomSource random, UUID shooterUUID, float damage) {
        return shoot(world, entity, random, shooterUUID, damage, entity.getMainHandItem());
    }

    public static DragonSwordProjectile shoot(Level world, LivingEntity entity, RandomSource random, UUID shooterUUID,
                                               float damage, ItemStack weapon) {
        DragonSwordProjectile projectile = new DragonSwordProjectile(
                ModEntities.DRAGON_SWORD_PROJECTILE.get(),
                entity,
                world,
                shooterUUID
        );

        Vec3 viewVector = entity.getViewVector(1.0F);
        projectile.shoot(viewVector.x, viewVector.y, viewVector.z, 4f, 0);
        projectile.setSilent(true);

        //发射时计算伤害快照
        projectile.applyWeaponEnchantments(weapon);
        float baseDamage = damage + projectile.enchantBonusDamage;
        int level = ItemLevelHelper.getLevel(weapon);
        double configValue = (level < 6)
                ? TheLastSwordConfiguration.getIncreaseValueSafely()
                : TheLastSwordConfiguration.getIncreaseValueHighLevelSafely();
        float extraDamage = (float) (level * configValue
                * TheLastSwordConfiguration.getDragonSwordProjectileExtraDamageMultiplierSafely());
        projectile.setSnapshotDamage(baseDamage, extraDamage);

        world.addFreshEntity(projectile);
        world.playSound(
            null,
            entity.getX(), entity.getY(), entity.getZ(),
            ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.arrow.shoot")),
            SoundSource.PLAYERS,
            1,
            1f / (random.nextFloat() * 0.5f + 1) + 2f / 2
        );

        return projectile;
    }
    @Override
    protected float getGravity() {
        return 0.01f;
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        //生成龙之闪电视觉效果
        if (this.level() instanceof ServerLevel serverLevel) {
            DragonLightingEntity lightning = new DragonLightingEntity(ModEntities.DRAGON_LIGHTING.get(), serverLevel);
            BlockPos pos = blockHitResult.getBlockPos();
            lightning.moveTo(Vec3.atBottomCenterOf(pos));
            lightning.setVisualOnly(true);
            serverLevel.addFreshEntity(lightning);
        }
        this.hasHitGround = true;
    }
}
