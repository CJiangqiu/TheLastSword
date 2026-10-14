package net.the_last_sword.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.the_last_sword.configuration.QueenBlinkSettings;
import net.the_last_sword.init.ModEntities;
import net.the_last_sword.util.EntityUtil;

public class QueenBlinkBlade extends Projectile {
    private static final double FLIGHT_RANGE = 16.0;
    private static final double FLIGHT_SPEED_SCALE = 0.5;
    private static final EntityDataAccessor<Float> WIDTH = SynchedEntityData.defineId(QueenBlinkBlade.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> HEIGHT = SynchedEntityData.defineId(QueenBlinkBlade.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> LENGTH = SynchedEntityData.defineId(QueenBlinkBlade.class, EntityDataSerializers.FLOAT);
    private double damage = 5;
    private double travelled;

    public QueenBlinkBlade(EntityType<? extends QueenBlinkBlade> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    public QueenBlinkBlade(Level level, LivingEntity queen, QueenBlinkSettings settings) {
        this(ModEntities.QUEEN_BLINK_BLADE.get(), level, queen, settings);
    }

    protected QueenBlinkBlade(EntityType<? extends QueenBlinkBlade> type, Level level,
                              LivingEntity queen, QueenBlinkSettings settings) {
        this(type, level);
        setOwner(queen);
        entityData.set(WIDTH, (float) settings.width());
        entityData.set(HEIGHT, (float) settings.height());
        entityData.set(LENGTH, (float) settings.length());
        damage = settings.damage();
        setYRot(queen.getYRot());
        Vec3 forward = forward();
        setPos(queen.position().add(forward.scale(settings.length() * 0.5 + 0.5)).add(0, 0.1, 0));
        setDeltaMovement(forward.scale(settings.speed() * FLIGHT_SPEED_SCALE));
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(WIDTH, 1F);
        entityData.define(HEIGHT, 8F);
        entityData.define(LENGTH, 3F);
    }

    public float bladeWidth() { return entityData.get(WIDTH); }
    public float bladeHeight() { return entityData.get(HEIGHT); }
    public float bladeLength() { return entityData.get(LENGTH); }

    protected Vec3 forward() {
        double yaw = Math.toRadians(getYRot());
        return new Vec3(-Math.sin(yaw), 0, Math.cos(yaw));
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return bounds(forward());
    }

    protected AABB bounds(Vec3 f) {
        double x = (Math.abs(f.x) * bladeLength() + Math.abs(f.z) * bladeWidth()) * 0.5;
        double z = (Math.abs(f.z) * bladeLength() + Math.abs(f.x) * bladeWidth()) * 0.5;
        return new AABB(getX() - x, getY(), getZ() - z, getX() + x, getY() + bladeHeight(), getZ() + z);
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 f = forward();
        if (level().isClientSide) {
            setPos(position().add(getDeltaMovement()));
            for (int i = 0; i < 6; i++) {
                double p = random.nextDouble();
                Vec3 point = bladeParticlePosition(f, p);
                level().addParticle(ParticleTypes.DRAGON_BREATH, point.x, point.y, point.z,
                        -f.x * 0.03, 0.01, -f.z * 0.03);
            }
            return;
        }
        if (!(getOwner() instanceof LivingEntity owner) || !owner.isAlive()) {
            discard();
            return;
        }
        double range = flightRange();
        double step = Math.min(getDeltaMovement().length(), Math.max(0, range - travelled));
        AABB broad = bounds(f).expandTowards(f.scale(step));
        // 一次查询只返回每个实体一次，穿透时也不会在同一 tick 重复结算。
        for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, broad,
                entity -> entity != owner && entity.isAlive() && EntityUtil.canAttack(owner, entity))) {
            double contact = contactDistance(target.getBoundingBox(), f, step);
            if (Double.isFinite(contact)) {
                target.invulnerableTime = 0;
                target.hurt(bladeDamageSource(owner), (float) damage);
            }
        }
        setPos(position().add(f.scale(step)));
        travelled += step;
        if (travelled >= range - 1.0E-6 || step <= 0) {
            discard();
        }
    }

    protected DamageSource bladeDamageSource(LivingEntity owner) {
        return new DamageSource(level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(DamageTypes.DRAGON_BREATH), this, owner);
    }

    protected double flightRange() {
        return FLIGHT_RANGE;
    }

    protected Vec3 bladeParticlePosition(Vec3 forward, double progress) {
        return position().add(forward.scale((Math.sin(progress * Math.PI) - 0.5) * bladeLength()))
                .add(0, progress * bladeHeight(), 0);
    }

    // 扫描整段位移，避免刀光在两刻之间越过目标而漏伤。
    protected double contactDistance(AABB box, Vec3 f, double step) {
        if (box.maxY <= getY() || box.minY >= getY() + bladeHeight()) return Double.POSITIVE_INFINITY;
        double dx = (box.minX + box.maxX) * 0.5 - getX();
        double dz = (box.minZ + box.maxZ) * 0.5 - getZ();
        double hx = box.getXsize() * 0.5;
        double hz = box.getZsize() * 0.5;
        double enter = 0;
        double leave = step;
        for (int axis = 0; axis < 4; axis++) {
            double ax = axis == 0 ? 1 : axis == 1 ? 0 : axis == 2 ? f.x : f.z;
            double az = axis == 0 ? 0 : axis == 1 ? 1 : axis == 2 ? f.z : -f.x;
            double center = dx * ax + dz * az;
            double velocity = f.x * ax + f.z * az;
            double radius = hx * Math.abs(ax) + hz * Math.abs(az)
                    + bladeLength() * 0.5 * Math.abs(velocity)
                    + bladeWidth() * 0.5 * Math.abs(f.z * ax - f.x * az);
            if (Math.abs(velocity) < 1.0E-9) {
                if (Math.abs(center) > radius) return Double.POSITIVE_INFINITY;
            } else {
                double a = (center - radius) / velocity;
                double b = (center + radius) / velocity;
                enter = Math.max(enter, Math.min(a, b));
                leave = Math.min(leave, Math.max(a, b));
                if (enter > leave) return Double.POSITIVE_INFINITY;
            }
        }
        return enter;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("BladeWidth", bladeWidth());
        tag.putFloat("BladeHeight", bladeHeight());
        tag.putFloat("BladeLength", bladeLength());
        tag.putDouble("Damage", damage);
        tag.putDouble("Travelled", travelled);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("BladeWidth")) entityData.set(WIDTH, tag.getFloat("BladeWidth"));
        if (tag.contains("BladeHeight")) entityData.set(HEIGHT, tag.getFloat("BladeHeight"));
        if (tag.contains("BladeLength")) entityData.set(LENGTH, tag.getFloat("BladeLength"));
        if (tag.contains("Damage")) damage = tag.getDouble("Damage");
        travelled = tag.getDouble("Travelled");
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
