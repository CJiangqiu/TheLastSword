package net.the_last_sword.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.configuration.LightningSpearSettings;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.init.ModEntities;
import net.the_last_sword.network.LightningSpearBurstPacket;
import net.the_last_sword.network.NetworkHandler;
import net.the_last_sword.util.EntityUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LightningSpearProjectile extends AbstractArrow {
    private double flightDistance;
    private boolean detonated;

    public LightningSpearProjectile(EntityType<? extends LightningSpearProjectile> type, Level level) {
        super(type, level);
        setNoGravity(true);
        pickup = Pickup.DISALLOWED;
    }

    public LightningSpearProjectile(Level level, LivingEntity owner) {
        super(ModEntities.LIGHTNING_SPEAR_PROJECTILE.get(), owner, level);
        setNoGravity(true);
        pickup = Pickup.DISALLOWED;
    }

    @Override
    public void tick() {
        boolean reachesLimit = false;
        Vec3 previousPosition = position();
        if (!level().isClientSide) {
            setNoGravity(true);
            if (inGround) {
                detonate(position());
                return;
            }
            double remaining = TheLastSwordConfiguration.getLightningSpearSettings().range() - flightDistance;
            if (remaining <= 0.000001D) {
                discard();
                return;
            }
            Vec3 movement = getDeltaMovement();
            double step = movement.length();
            if (step >= remaining) {
                // 在碰撞检测前截短轨迹，避免命中射程之外的目标。
                setDeltaMovement(movement.scale(remaining / step));
                reachesLimit = true;
            }
        }
        super.tick();
        if (!level().isClientSide && !isRemoved()) {
            flightDistance += position().distanceTo(previousPosition);
            if (reachesLimit) {
                discard();
            }
        }
    }

    @Override
    @Nullable
    protected EntityHitResult findHitEntity(Vec3 startPosition, Vec3 endPosition) {
        // 保留射线与碰撞箱的交点，范围爆发不能使用目标脚下的位置。
        return ProjectileUtil.getEntityHitResult(this, startPosition, endPosition,
                getBoundingBox().expandTowards(endPosition.subtract(startPosition)).inflate(1.0D),
                this::canHitEntity, startPosition.distanceToSqr(endPosition));
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult hitResult) {
        detonate(hitResult.getLocation());
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult hitResult) {
        detonate(hitResult.getLocation());
    }

    private void detonate(Vec3 center) {
        if (!(level() instanceof ServerLevel serverLevel) || detonated || isRemoved()) {
            return;
        }
        detonated = true;
        LightningSpearSettings settings = TheLastSwordConfiguration.getLightningSpearSettings();
        Entity owner = getOwner();
        double halfSize = settings.burstSize() * 0.5D;
        AABB area = new AABB(center, center).inflate(halfSize);
        if (owner != null) {
            DamageSource source = new DamageSource(
                    serverLevel.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                            .getHolderOrThrow(DamageTypes.LIGHTNING_BOLT), this, owner);
            for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, area,
                    entity -> entity.isAlive() && entity != owner && EntityUtil.canAttack(owner, entity))) {
                boolean damaged = target.hurt(source, (float) settings.damage());
                if (settings.slowTicks() > 0) {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                            settings.slowTicks(), settings.slowLevel() - 1), owner);
                }
                if (damaged && owner instanceof LivingEntity livingOwner) {
                    livingOwner.setLastHurtMob(target);
                }
            }
        }
        LightningSpearBurstPacket packet = new LightningSpearBurstPacket(
                serverLevel.dimension().location(), center, (float) halfSize, random.nextLong());
        for (ServerPlayer player : serverLevel.players()) {
            if (player.distanceToSqr(center) <= 128.0D * 128.0D) {
                NetworkHandler.sendToPlayer(packet, player);
            }
        }
        serverLevel.playSound(null, center.x, center.y, center.z,
                SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.0F, 1.2F);
        discard();
    }

    @Override
    protected @NotNull ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public void playerTouch(@NotNull Player player) {
        // 技能投射物不对应可回收物品，旧存档也不能通过拾取复制长矛。
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        double savedDistance = tag.getDouble("FlightDistance");
        flightDistance = Double.isFinite(savedDistance) ? Math.max(0.0D, savedDistance) : 0.0D;
        pickup = Pickup.DISALLOWED;
        setNoGravity(true);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble("FlightDistance", flightDistance);
    }

    @Override
    protected float getWaterInertia() {
        return 0.99F;
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }
}
