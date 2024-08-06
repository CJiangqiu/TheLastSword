package net.thelastsword.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EntityType;
import net.minecraft.util.RandomSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import net.thelastsword.init.TheLastSwordModEntities;
import net.thelastsword.init.TheLastSwordModItems;
import net.thelastsword.capability.ISwordLevel;
import net.thelastsword.capability.SwordCapability;
import net.thelastsword.configuration.TheLastSwordConfiguration;

@OnlyIn(value = Dist.CLIENT, _interface = ItemSupplier.class)
public class DragonSwordProjectile extends AbstractArrow implements ItemSupplier {
    public static final ItemStack PROJECTILE_ITEM = new ItemStack(TheLastSwordModItems.DRAGON_CRYSTAL.get());

    public DragonSwordProjectile(PlayMessages.SpawnEntity packet, Level world) {
        super(TheLastSwordModEntities.DRAGON_SWORD_PROJECTILE.get(), world);
    }

    public DragonSwordProjectile(EntityType<? extends DragonSwordProjectile> type, Level world) {
        super(type, world);
    }

    public DragonSwordProjectile(EntityType<? extends DragonSwordProjectile> type, double x, double y, double z, Level world) {
        super(type, x, y, z, world);
    }

    public DragonSwordProjectile(EntityType<? extends DragonSwordProjectile> type, LivingEntity entity, Level world) {
        super(type, entity, world);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ItemStack getItem() {
        return PROJECTILE_ITEM;
    }

    @Override
    protected ItemStack getPickupItem() {
        return PROJECTILE_ITEM;
    }

    @Override
    protected void doPostHurtEffects(LivingEntity entity) {
        super.doPostHurtEffects(entity);
        entity.setArrowCount(entity.getArrowCount() - 1);
    }

    @Override
    public void playerTouch(Player entity) {
        super.playerTouch(entity);
        if (entity == null || entity == this.getOwner())
            return;
        if (entity.getInventory().contains(new ItemStack(TheLastSwordModItems.DRAGON_SWORD.get()))) {
        } else {
            if (this.level() instanceof ServerLevel _level) {
                LightningBolt entityToSpawn = EntityType.LIGHTNING_BOLT.create(_level);
                entityToSpawn.moveTo(Vec3.atBottomCenterOf(BlockPos.containing(this.getX(), this.getY(), this.getZ())));
                _level.addFreshEntity(entityToSpawn);
            }

            entity.hurt(new DamageSource(this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.LIGHTNING_BOLT), this, this.getOwner()), 12);
        }
    }

    @Override
    public void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        Entity target = entityHitResult.getEntity();

        if (target == this.getOwner()) {
            return;
        }

        if (target instanceof LivingEntity) {
            LivingEntity livingTarget = (LivingEntity) target;

            // 获取剑等级和额外攻击力
            if (this.getOwner() instanceof Player) {
                Player player = (Player) this.getOwner();
                ItemStack mainHandItem = player.getMainHandItem();
                mainHandItem.getCapability(SwordCapability.SWORD_LEVEL_CAPABILITY).ifPresent(swordLevel -> {
                    int level = swordLevel.getLevel();
                    double increaseValue = TheLastSwordConfiguration.INCREASE_VALUE.get();
                    double increaseValueHighLevel = TheLastSwordConfiguration.INCREASE_VALUE_HIGH_LEVEL.get();
                    double extraDamage = (level >= 6 ? increaseValueHighLevel : increaseValue) * level;

                    // 对目标造成额外伤害
                    livingTarget.hurt(new DamageSource(this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC), this, this.getOwner()), (float) extraDamage);
                });
            }

            // 生成闪电
            if (this.level() instanceof ServerLevel _level) {
                LightningBolt entityToSpawn = EntityType.LIGHTNING_BOLT.create(_level);
                entityToSpawn.moveTo(Vec3.atBottomCenterOf(BlockPos.containing(this.getX(), this.getY(), this.getZ())));
                entityToSpawn.setVisualOnly(true);
                _level.addFreshEntity(entityToSpawn);
            }
            livingTarget.hurt(new DamageSource(this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.LIGHTNING_BOLT), this, this.getOwner()), 12);
        }
    }

    @Override
    public void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        if (this.level() instanceof ServerLevel _level) {
            LightningBolt entityToSpawn = EntityType.LIGHTNING_BOLT.create(_level);
            entityToSpawn.moveTo(Vec3.atBottomCenterOf(BlockPos.containing(blockHitResult.getBlockPos().getX(), blockHitResult.getBlockPos().getY(), blockHitResult.getBlockPos().getZ())));
            entityToSpawn.setVisualOnly(true);
            _level.addFreshEntity(entityToSpawn);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.inGround)
            this.discard();
    }

    public static DragonSwordProjectile shoot(Level world, LivingEntity entity, RandomSource source) {
        return shoot(world, entity, source, 2f, 5, 1);
    }

    public static DragonSwordProjectile shoot(Level world, LivingEntity entity, RandomSource random, float power, double damage, int knockback) {
        DragonSwordProjectile entityarrow = new DragonSwordProjectile(TheLastSwordModEntities.DRAGON_SWORD_PROJECTILE.get(), entity, world);
        entityarrow.shoot(entity.getViewVector(1).x, entity.getViewVector(1).y, entity.getViewVector(1).z, power * 2, 0);
        entityarrow.setSilent(true);
        entityarrow.setCritArrow(false);
        entityarrow.setBaseDamage(damage);
        entityarrow.setKnockback(knockback);
        world.addFreshEntity(entityarrow);
        world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.arrow.shoot")), SoundSource.PLAYERS, 1, 1f / (random.nextFloat() * 0.5f + 1) + (power / 2));
        return entityarrow;
    }

    public static DragonSwordProjectile shoot(LivingEntity entity, LivingEntity target) {
        DragonSwordProjectile entityarrow = new DragonSwordProjectile(TheLastSwordModEntities.DRAGON_SWORD_PROJECTILE.get(), entity, entity.level());
        double dx = target.getX() - entity.getX();
        double dy = target.getY() + target.getEyeHeight() - 1.1;
        double dz = target.getZ() - entity.getZ();
        entityarrow.shoot(dx, dy - entityarrow.getY() + Math.hypot(dx, dz) * 0.2F, dz, 2f * 2, 12.0F);
        entityarrow.setSilent(true);
        entityarrow.setBaseDamage(5);
        entityarrow.setKnockback(1);
        entityarrow.setCritArrow(false);
        entity.level().addFreshEntity(entityarrow);
        entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.arrow.shoot")), SoundSource.PLAYERS, 1, 1f / (RandomSource.create().nextFloat() * 0.5f + 1));
        return entityarrow;
    }
}
