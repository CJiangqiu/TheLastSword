package net.the_last_sword.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.the_last_sword.init.ModEntities;
import net.the_last_sword.util.EntityUtil;
import net.the_last_sword.util.QueenSummonRiftPlacement;

import java.util.Comparator;
import java.util.UUID;

public class EndCrystalRift extends Entity {
    public static final double TARGET_RANGE = 32;
    public static final float DAMAGE = 20;
    private static final EntityDataAccessor<Long> OPENED_AT =
        SynchedEntityData.defineId(EndCrystalRift.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Long> CLOSED_AT =
        SynchedEntityData.defineId(EndCrystalRift.class, EntityDataSerializers.LONG);
    private UUID ownerId;
    private int remainingCrystals;
    private int waitingTicks;
    private long nextShot;

    public EndCrystalRift(EntityType<? extends EndCrystalRift> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    public EndCrystalRift(Level level, LivingEntity owner, int count) {
        this(ModEntities.END_CRYSTAL_RIFT.get(), level);
        ownerId = owner.getUUID();
        remainingCrystals = Math.max(1, Math.min(10, count));
        moveTo(owner.getX(), owner.getY(), owner.getZ(), owner.getYRot(), 0);
        entityData.set(OPENED_AT, level.getGameTime());
        nextShot = level.getGameTime() + 40;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(OPENED_AT, 0L);
        entityData.define(CLOSED_AT, -1L);
    }

    public float getAnimationAge(float partialTick) {
        long closing = entityData.get(CLOSED_AT);
        // 延长完全展开阶段，让高等级和等待目标时仍能复用同一套开合动画。
        return closing >= 0 ? 140 + level().getGameTime() - closing + partialTick
            : Math.min(139, level().getGameTime() - entityData.get(OPENED_AT) + partialTick);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel serverLevel)) return;
        long now = level().getGameTime();
        long closing = entityData.get(CLOSED_AT);
        if (closing >= 0) {
            if (now - closing >= 10) discard();
            return;
        }
        Entity entity = ownerId == null ? null : serverLevel.getEntity(ownerId);
        if (!(entity instanceof LivingEntity owner) || !owner.isAlive() || remainingCrystals <= 0) {
            close();
            return;
        }
        if (now < nextShot) return;
        LivingEntity target = level().getEntitiesOfClass(LivingEntity.class,
                getBoundingBox().inflate(TARGET_RANGE), candidate -> candidate != owner && candidate.isAlive()
                    && candidate.distanceToSqr(position()) <= TARGET_RANGE * TARGET_RANGE
                    && EntityUtil.canAttack(owner, candidate))
            .stream().min(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(position())))
            .orElse(null);
        if (target == null) {
            if (++waitingTicks >= 200) close();
            return;
        }
        Vec3 origin = QueenSummonRiftPlacement.worldCenter(position(), getYRot());
        Vec3 direction = target.getBoundingBox().getCenter().subtract(origin).normalize();
        QueenSummonedProjectile crystal = new QueenSummonedProjectile(
            ModEntities.QUEEN_SUMMONED_PROJECTILE.get(), owner, level(), ownerId);
        crystal.setPos(origin.x, origin.y, origin.z);
        crystal.setLifetimeTicks(60);
        crystal.setSnapshotDamage(0, DAMAGE);
        crystal.shoot(direction.x, direction.y, direction.z, 4F, 0F);
        crystal.setSilent(true);
        if (level().addFreshEntity(crystal)) {
            level().playSound(null, origin.x, origin.y, origin.z,
                SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.PLAYERS, 1F, 1F);
            remainingCrystals--;
        } else if (++waitingTicks >= 200) {
            close();
        }
        nextShot = now + 20;
        if (remainingCrystals <= 0) close();
    }

    private void close() {
        entityData.set(CLOSED_AT, level().getGameTime());
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return getBoundingBox().inflate(6);
    }

    // 裂隙是短时施法效果，不跨区块卸载保存。
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
