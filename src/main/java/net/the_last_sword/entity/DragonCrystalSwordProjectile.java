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
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.registries.ForgeRegistries;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.init.ModEntities;
import net.the_last_sword.util.nbt.ItemLevelHelper;

import java.util.UUID;

/**
 * 龙水晶剑弹射物
 * 击中实体时造成物理伤害和闪电伤害，并生成闪电特效
 */
public class DragonCrystalSwordProjectile extends TheLastEndSwordItemsProjectile {
    public static final ItemStack PROJECTILE_ITEM = new ItemStack(Items.DIAMOND);

    //用于网络生成实体的构造器
    public DragonCrystalSwordProjectile(PlayMessages.SpawnEntity packet, Level world) {
        super(ModEntities.DRAGON_CRYSTAL_SWORD_PROJECTILE.get(), world);
    }

    public DragonCrystalSwordProjectile(EntityType<? extends DragonCrystalSwordProjectile> type, Level world) {
        super(type, world);
    }

    public DragonCrystalSwordProjectile(EntityType<? extends DragonCrystalSwordProjectile> type, LivingEntity entity, Level world, UUID shooterUUID) {
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

    //造成额外魔法伤害（使用发射时快照）
    @Override
    protected void applyExtraDamage(Entity target) {
        if (this.getOwner() instanceof LivingEntity owner && snapshotExtraDamage > 0) {
            DamageSource magicDamageSource = new DamageSource(
                this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                    .getHolderOrThrow(DamageTypes.MAGIC),
                this, owner
            );
            target.hurt(magicDamageSource, snapshotExtraDamage);
        }
    }

    //生成闪电视觉效果
    @Override
    protected void applyVisualEffect(Entity target) {
        if (this.level() instanceof ServerLevel serverLevel) {
            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(serverLevel);
            if (lightning != null) {
                lightning.moveTo(Vec3.atBottomCenterOf(BlockPos.containing(this.getX(), this.getY(), this.getZ())));
                lightning.setVisualOnly(true);
                serverLevel.addFreshEntity(lightning);
            }
        }
    }

    //发射弹射物的静态方法
    public static DragonCrystalSwordProjectile shoot(Level world, LivingEntity entity, RandomSource random, UUID shooterUUID, float damage) {
        DragonCrystalSwordProjectile projectile = new DragonCrystalSwordProjectile(
                ModEntities.DRAGON_CRYSTAL_SWORD_PROJECTILE.get(),
                entity,
                world,
                shooterUUID
        );

        Vec3 viewVector = entity.getViewVector(1.0F);
        projectile.shoot(viewVector.x, viewVector.y, viewVector.z, 3f, 0);
        projectile.setSilent(true);

        //发射时计算伤害快照
        ItemStack weapon = entity.getMainHandItem();
        projectile.applyWeaponEnchantments(weapon);
        float baseDamage = damage + projectile.enchantBonusDamage;
        int level = ItemLevelHelper.getLevel(weapon);
        double configValue = (level < 6)
                ? TheLastSwordConfiguration.getIncreaseValueSafely()
                : TheLastSwordConfiguration.getIncreaseValueHighLevelSafely();
        float extraDamage = (float) (level * configValue
                * TheLastSwordConfiguration.getDragonCrystalSwordProjectileExtraDamageMultiplierSafely());
        projectile.setSnapshotDamage(baseDamage, extraDamage);

        world.addFreshEntity(projectile);
        world.playSound(
            null,
            entity.getX(), entity.getY(), entity.getZ(),
            ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.arrow.shoot")),
            SoundSource.PLAYERS,
            1,
            1f / (random.nextFloat() * 0.5f + 1) + 0.5f
        );

        return projectile;
    }
}
