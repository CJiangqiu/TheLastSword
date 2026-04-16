package net.the_last_sword.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
    private double baseDamage;

    //用于网络生成实体的构造器
    public DragonCrystalSwordProjectile(PlayMessages.SpawnEntity packet, Level world) {
        super(ModEntities.DRAGON_CRYSTAL_SWORD_PROJECTILE.get(), world);
        this.baseDamage = 12f; //默认伤害值
    }

    public DragonCrystalSwordProjectile(EntityType<? extends DragonCrystalSwordProjectile> type, Level world) {
        super(type, world);
        this.baseDamage = 12f; //默认伤害值
    }

    public DragonCrystalSwordProjectile(EntityType<? extends DragonCrystalSwordProjectile> type, LivingEntity entity, Level world, UUID shooterUUID) {
        super(type, entity, world, shooterUUID);
        this.baseDamage = 12f; //默认伤害值，实际伤害由shoot方法设置
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ItemStack getItem() {
        return PROJECTILE_ITEM;
    }

    //造成基础物理伤害（通用物理伤害类型）
    @Override
    protected void applyBaseDamage(LivingEntity target) {
        DamageSource projectileDamageSource = new DamageSource(
            this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(DamageTypes.GENERIC),
            this,
            this.getOwner()
        );
        target.hurt(projectileDamageSource, (float) this.baseDamage + enchantBonusDamage);
    }

    //造成额外魔法伤害
    @Override
    protected void applyExtraDamage(LivingEntity target) {
        if (this.getOwner() instanceof Player player) {
            ItemStack mainHandItem = player.getMainHandItem();
            int level = ItemLevelHelper.getLevel(mainHandItem);

            double configValue = (level < 6)
                    ? TheLastSwordConfiguration.getIncreaseValueSafely()
                    : TheLastSwordConfiguration.getIncreaseValueHighLevelSafely();
            float extraDamage = (float) (level * configValue);

            if (extraDamage > 0) {
                DamageSource magicDamageSource = new DamageSource(
                    this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(DamageTypes.MAGIC),
                    this,
                    this.getOwner()
                );
                target.hurt(magicDamageSource, extraDamage);
            }
        }
    }

    //生成闪电视觉效果
    @Override
    protected void applyVisualEffect(LivingEntity target) {
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
        projectile.baseDamage = damage;
        //应用武器附魔
        projectile.applyWeaponEnchantments(entity.getMainHandItem());

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
