package net.the_last_sword.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.configuration.QueenBlinkSettings;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.damagesource.AbsoluteDestructionDamageSource;
import net.the_last_sword.init.ModEntities;

public class QueenEnhancedBlade extends QueenBlinkBlade {
    private static final double FLIGHT_RANGE = 32.0;
    private static final EntityDataAccessor<Float> ROLL = SynchedEntityData.defineId(
            QueenEnhancedBlade.class, EntityDataSerializers.FLOAT);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ROLL, 0F);
    }

    public float bladeRoll() {
        return entityData.get(ROLL);
    }

    public void setBladeRoll(float roll) {
        entityData.set(ROLL, roll);
    }

    public QueenEnhancedBlade(EntityType<? extends QueenEnhancedBlade> type, Level level) {
        super(type, level);
    }

    public QueenEnhancedBlade(Level level, ThePastShadowOfTheQueenEntity queen) {
        this(level, queen, TheLastSwordConfiguration.getQueenBlinkSettings());
    }

    public QueenEnhancedBlade(Level level, ThePastShadowOfTheQueenEntity queen, QueenBlinkSettings settings) {
        super(ModEntities.QUEEN_ENHANCED_BLADE.get(), level, queen,
                new QueenBlinkSettings(settings.cooldown(), settings.damage(), settings.speed(),
                        1.0, 8.0, 3.0));
    }

    @Override
    protected double flightRange() {
        return FLIGHT_RANGE;
    }

    @Override
    protected DamageSource bladeDamageSource(LivingEntity owner) {
        return AbsoluteDestructionDamageSource.absoluteDestruction(owner);
    }

    private Vec3[] bladeAxes(Vec3 forward) {
        double angle = Math.toRadians(bladeRoll());
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
        Vec3 right = new Vec3(forward.z, 0, -forward.x);
        return new Vec3[] {right.scale(cos).add(0, sin, 0),
                right.scale(-sin).add(0, cos, 0), forward};
    }

    @Override
    protected AABB bounds(Vec3 forward) {
        Vec3[] axes = bladeAxes(forward);
        double[] half = {bladeWidth() * 0.5, bladeHeight() * 0.5, bladeLength() * 0.5};
        double x = 0, y = 0, z = 0;
        for (int i = 0; i < 3; i++) {
            x += Math.abs(axes[i].x) * half[i];
            y += Math.abs(axes[i].y) * half[i];
            z += Math.abs(axes[i].z) * half[i];
        }
        Vec3 center = position().add(0, half[1], 0);
        return new AABB(center.x - x, center.y - y, center.z - z,
                center.x + x, center.y + y, center.z + z);
    }

    @Override
    protected Vec3 bladeParticlePosition(Vec3 forward, double progress) {
        return position().add(0, bladeHeight() * 0.5, 0)
                .add(bladeAxes(forward)[1].scale((progress - 0.5) * bladeHeight()))
                .add(forward.scale((Math.sin(progress * Math.PI) - 0.5) * bladeLength()));
    }

    @Override
    protected double contactDistance(AABB box, Vec3 forward, double step) {
        if (bladeRoll() == 0F) return super.contactDistance(box, forward, step);
        Vec3[] local = bladeAxes(forward);
        Vec3[] world = {new Vec3(1, 0, 0), new Vec3(0, 1, 0), new Vec3(0, 0, 1)};
        Vec3[] axes = new Vec3[15];
        System.arraycopy(local, 0, axes, 0, 3);
        System.arraycopy(world, 0, axes, 3, 3);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) axes[6 + i * 3 + j] = local[i].cross(world[j]);
        }
        Vec3 offset = box.getCenter().subtract(position().add(0, bladeHeight() * 0.5, 0));
        double enter = 0, leave = step;
        // 连续三维分离轴检测使斜刀光的命中与挡墙距离匹配实际姿态。
        for (Vec3 axis : axes) {
            if (axis.lengthSqr() < 1.0E-12) continue;
            double radius = (box.getXsize() * Math.abs(axis.x) + box.getYsize() * Math.abs(axis.y)
                    + box.getZsize() * Math.abs(axis.z) + bladeWidth() * Math.abs(local[0].dot(axis))
                    + bladeHeight() * Math.abs(local[1].dot(axis))
                    + bladeLength() * Math.abs(local[2].dot(axis))) * 0.5;
            double center = offset.dot(axis);
            double velocity = forward.dot(axis);
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
        tag.putFloat("BladeRoll", bladeRoll());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setBladeRoll(tag.getFloat("BladeRoll"));
    }
}
