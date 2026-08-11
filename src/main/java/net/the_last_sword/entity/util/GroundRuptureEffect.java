package net.the_last_sword.entity.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.entity.GroundRuptureFragmentEntity;

public final class GroundRuptureEffect {
    private static final int RING_COUNT = 4;
    private static final int RING_INTERVAL = 2;

    private GroundRuptureEffect() {
    }

    public static void spawn(ServerLevel level, Vec3 center, RandomSource sourceRandom) {
        long effectSeed = sourceRandom.nextLong();
        for (int ring = 0; ring < RING_COUNT; ring++) {
            int scheduledRing = ring;
            TheLastSwordMod.queueServerWork(ring * RING_INTERVAL, () ->
                spawnRing(level, center, scheduledRing,
                    RandomSource.create(effectSeed + scheduledRing * 31L)));
        }
    }

    private static void spawnRing(ServerLevel level, Vec3 center, int ring, RandomSource random) {
        int fragmentCount = 3 + ring * 2;
        double baseRadius = ring == 0 ? 0.15D : 0.35D + ring * 0.72D;

        for (int i = 0; i < fragmentCount; i++) {
            double angle = Math.PI * 2.0D * i / fragmentCount + random.nextDouble() * 0.55D;
            double radius = baseRadius + random.nextDouble() * (ring == 0 ? 0.3D : 0.45D);
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            GroundSample sample = findGround(level, x, center.y, z);
            if (sample == null) {
                continue;
            }

            float scale = (float) Math.max(0.24D,
                (0.62D - ring * 0.09D) * (0.82D + random.nextDouble() * 0.34D));
            double outwardSpeed = 0.025D + ring * 0.014D;
            double upwardSpeed = 0.25D - ring * 0.03D + random.nextDouble() * 0.055D;
            Vec3 velocity = new Vec3(
                Math.cos(angle) * outwardSpeed,
                upwardSpeed,
                Math.sin(angle) * outwardSpeed
            );

            level.addFreshEntity(new GroundRuptureFragmentEntity(
                level,
                sample.state(),
                new Vec3(x, sample.surfaceY() + 0.06D, z),
                velocity,
                scale,
                random.nextInt()
            ));

            level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, sample.state()),
                x, sample.surfaceY() + 0.12D, z,
                2 + ring,
                0.12D + ring * 0.04D, 0.08D, 0.12D + ring * 0.04D,
                0.055D + ring * 0.01D
            );
        }
    }

    private static GroundSample findGround(ServerLevel level, double x, double centerY, double z) {
        BlockPos.MutableBlockPos cursor = BlockPos.containing(x, centerY + 1.5D, z).mutable();
        for (int depth = 0; depth < 6; depth++) {
            BlockState state = level.getBlockState(cursor);
            VoxelShape collision = state.getCollisionShape(level, cursor);
            if (!state.isAir() && state.getRenderShape() == RenderShape.MODEL && !collision.isEmpty()) {
                return new GroundSample(state, cursor.getY() + collision.max(Direction.Axis.Y));
            }
            cursor.move(Direction.DOWN);
        }
        return null;
    }

    private record GroundSample(BlockState state, double surfaceY) {
    }
}
