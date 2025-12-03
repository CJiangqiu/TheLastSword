package net.the_last_sword.util;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

//粒子效果工具类
public class ParticleUtil {

    //生成召唤粒子效果（扩散球体 + 圆圈 + 六芒星组合）
    public static void spawnSummonParticles(Entity entity) {
        if (entity == null || entity.level().isClientSide || !(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();

        //扩散球体（传送门粒子）
        for (int i = 0; i < 200; i++) {
            double theta = Math.random() * 2 * Math.PI;
            double phi = Math.acos(2 * Math.random() - 1);
            double radius = 0.5 + Math.random() * 2.0;
            double offsetX = radius * Math.sin(phi) * Math.cos(theta);
            double offsetY = radius * Math.cos(phi);
            double offsetZ = radius * Math.sin(phi) * Math.sin(theta);

            double particleX = x + offsetX;
            double particleY = y + 1.0 + offsetY;
            double particleZ = z + offsetZ;

            double speedX = offsetX * 0.3;
            double speedY = offsetY * 0.3;
            double speedZ = offsetZ * 0.3;

            serverLevel.sendParticles(ParticleTypes.PORTAL,
                particleX, particleY, particleZ,
                1, speedX, speedY, speedZ, 0.1);
        }

        //圆圈（附魔粒子）
        for (int i = 0; i < 60; i++) {
            double angle = (2 * Math.PI * i) / 60;
            double circleX = x + 3.0 * Math.cos(angle);
            double circleZ = z + 3.0 * Math.sin(angle);
            serverLevel.sendParticles(ParticleTypes.ENCHANT, circleX, y + 0.1, circleZ, 1, 0, 0, 0, 0);
        }

        //六芒星（龙息粒子）
        double radius = 1.5;
        int pointsPerLine = 15;

        //正三角形顶点
        Vec3[] upTriangle = new Vec3[3];
        for (int i = 0; i < 3; i++) {
            double angle = Math.toRadians(90 + i * 120);
            upTriangle[i] = new Vec3(
                x + radius * Math.cos(angle),
                y + 0.2,
                z + radius * Math.sin(angle)
            );
        }

        //倒三角形顶点
        Vec3[] downTriangle = new Vec3[3];
        for (int i = 0; i < 3; i++) {
            double angle = Math.toRadians(-90 + i * 120);
            downTriangle[i] = new Vec3(
                x + radius * Math.cos(angle),
                y + 0.2,
                z + radius * Math.sin(angle)
            );
        }

        //绘制正三角形边
        for (int i = 0; i < 3; i++) {
            Vec3 start = upTriangle[i];
            Vec3 end = upTriangle[(i + 1) % 3];
            for (int j = 0; j <= pointsPerLine; j++) {
                double t = (double) j / pointsPerLine;
                double px = start.x + (end.x - start.x) * t;
                double py = start.y + (end.y - start.y) * t;
                double pz = start.z + (end.z - start.z) * t;
                serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH, px, py, pz, 1, 0, 0, 0, 0);
            }
        }

        //绘制倒三角形边
        for (int i = 0; i < 3; i++) {
            Vec3 start = downTriangle[i];
            Vec3 end = downTriangle[(i + 1) % 3];
            for (int j = 0; j <= pointsPerLine; j++) {
                double t = (double) j / pointsPerLine;
                double px = start.x + (end.x - start.x) * t;
                double py = start.y + (end.y - start.y) * t;
                double pz = start.z + (end.z - start.z) * t;
                serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH, px, py, pz, 1, 0, 0, 0, 0);
            }
        }
    }

    //生成终焉死亡粒子效果（传送门 + 末地烛）
    public static void spawnDeathParticles(Entity entity) {
        if (entity == null || entity.level().isClientSide || !(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        double x = entity.getX();
        double y = entity.getY() + entity.getBbHeight() / 2.0;
        double z = entity.getZ();

        //传送门粒子（12个）
        for (int i = 0; i < 12; i++) {
            serverLevel.sendParticles(
                ParticleTypes.PORTAL,
                x, y, z,
                1,
                (Math.random() - 0.5) * 0.5,
                (Math.random() - 0.5) * 0.5,
                (Math.random() - 0.5) * 0.5,
                0.1
            );
        }

        //末地烛粒子（16个）
        for (int i = 0; i < 16; i++) {
            serverLevel.sendParticles(
                ParticleTypes.END_ROD,
                x, y, z,
                1,
                (Math.random() - 0.5) * 0.5,
                (Math.random() - 0.5) * 0.5,
                (Math.random() - 0.5) * 0.5,
                0.05
            );
        }
    }

    //生成传送粒子效果（起点 + 终点传送门粒子）
    public static void spawnTeleportParticles(Level level, Vec3 from, Vec3 to, double entityHeight) {
        if (level == null || level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        //起点传送门粒子
        serverLevel.sendParticles(
            ParticleTypes.PORTAL,
            from.x, from.y + entityHeight * 0.5, from.z,
            20, 0.3, 0.3, 0.3, 0.1
        );

        //终点传送门粒子
        serverLevel.sendParticles(
            ParticleTypes.PORTAL,
            to.x, to.y + entityHeight * 0.5, to.z,
            20, 0.3, 0.3, 0.3, 0.1
        );
    }

    //生成末影死亡粒子效果（反向传送门 + 龙息 + 末地烛）
    public static void spawnEndDeathParticles(Level level, Vec3 pos, double entityHeight) {
        if (level == null || level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        double y = pos.y + entityHeight * 0.5;

        //反向传送门粒子爆发
        serverLevel.sendParticles(
            ParticleTypes.REVERSE_PORTAL,
            pos.x, y, pos.z,
            30, 0.5, 0.5, 0.5, 0.2
        );

        //龙息粒子
        serverLevel.sendParticles(
            ParticleTypes.DRAGON_BREATH,
            pos.x, y, pos.z,
            20, 0.3, 0.3, 0.3, 0.1
        );

        //末地烛粒子
        serverLevel.sendParticles(
            ParticleTypes.END_ROD,
            pos.x, y, pos.z,
            15, 0.4, 0.4, 0.4, 0.05
        );
    }

    //生成附魔粒子效果（附魔台 + 附魔击中 + 女巫粒子环 + 传送门）
    public static void spawnEnchantingParticles(Entity entity, int tickCount) {
        if (entity == null || entity.level().isClientSide || !(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 entityPos = entity.position();

        //计算左侧粒子位置
        double leftAngle = Math.toRadians(entity.getYRot() + 90);
        double particleX = entityPos.x + Math.cos(leftAngle) * 1.0;
        double particleY = entityPos.y + 0.8;
        double particleZ = entityPos.z + Math.sin(leftAngle) * 1.0;

        //附魔台粒子
        serverLevel.sendParticles(ParticleTypes.ENCHANT,
                particleX, particleY, particleZ,
                20, 0.3, 0.3, 0.3, 0.02);

        //附魔击中粒子
        serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT,
                particleX, particleY, particleZ,
                10, 0.2, 0.2, 0.2, 0.05);

        //女巫粒子环
        int ringParticles = 8;
        double ringRadius = 1.5;
        double ringHeight = entityPos.y + 1.0;
        for (int i = 0; i < ringParticles; i++) {
            double angle = (2 * Math.PI * i) / ringParticles + (tickCount * 0.1);
            double ringX = entityPos.x + Math.cos(angle) * ringRadius;
            double ringZ = entityPos.z + Math.sin(angle) * ringRadius;
            serverLevel.sendParticles(ParticleTypes.WITCH,
                    ringX, ringHeight, ringZ,
                    1, 0.0, 0.0, 0.0, 0.0);
        }

        //传送门粒子
        serverLevel.sendParticles(ParticleTypes.PORTAL,
                entityPos.x, entityPos.y + 1.0, entityPos.z,
                5, 0.1, 0.1, 0.1, 0.01);
    }

    //生成横扫粒子轨迹（第一击：左到右，第二击：右到左）
    public static void spawnSweepParticles(Entity entity, boolean isFirstStrike, double attackRange) {
        if (entity == null || entity.level().isClientSide || !(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        float yaw = entity.getYRot();
        double yawRad = Math.toRadians(yaw);
        Vec3 forward = new Vec3(-Math.sin(yawRad), 0.0, Math.cos(yawRad)).normalize();
        Vec3 right = new Vec3(forward.z, 0.0, -forward.x).normalize();
        Vec3 up = new Vec3(0.0, 1.0, 0.0);

        Vec3 startOffset, endOffset;

        if (isFirstStrike) {
            //第一击：从左到右
            startOffset = forward.subtract(right).add(up).scale(attackRange);
            endOffset = forward.add(right).subtract(up).scale(attackRange);
        } else {
            //第二击：从右到左
            startOffset = forward.add(right).add(up).scale(attackRange);
            endOffset = forward.subtract(right).subtract(up).scale(attackRange);
        }

        //绘制粒子轨迹
        int steps = 12;
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            double px = entity.getX() + lerp(t, startOffset.x, endOffset.x);
            double py = entity.getY() + lerp(t, startOffset.y, endOffset.y);
            double pz = entity.getZ() + lerp(t, startOffset.z, endOffset.z);

            //横扫攻击粒子
            serverLevel.sendParticles(
                    ParticleTypes.SWEEP_ATTACK,
                    px, py, pz, 1, 0.0, 0.0, 0.0, 0.0
            );

            //第一击：暴击粒子，第二击：附魔击中粒子
            if (isFirstStrike) {
                serverLevel.sendParticles(
                        ParticleTypes.CRIT,
                        px, py, pz, 10, 0.1, 0.1, 0.1, 0.05
                );
            } else {
                serverLevel.sendParticles(
                        ParticleTypes.ENCHANTED_HIT,
                        px, py, pz, 12, 0.4, 0.4, 0.4, 0.05
                );
            }
        }
    }

    //生成十字切传送粒子（反向传送门）
    public static void spawnCrossSlashTeleportParticles(Level level, Vec3 pos, double entityHeight) {
        if (level == null || level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        //反向传送门粒子
        serverLevel.sendParticles(
                ParticleTypes.REVERSE_PORTAL,
                pos.x, pos.y + entityHeight * 0.5, pos.z,
                15, 0.3, 0.3, 0.3, 0.05
        );
    }

    //生成万物终焉粒子圆圈（稳定期）
    public static void spawnAllThingsEndCircles(Level level, Vec3 centerPos, double radius) {
        if (level == null || level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        int particleCount = Math.max(8, (int)(radius * 16));

        //水平圆圈（末地烛粒子）
        for (int i = 0; i < particleCount; i++) {
            double angle = (2 * Math.PI * i) / particleCount;
            double x = centerPos.x + radius * Math.cos(angle);
            double z = centerPos.z + radius * Math.sin(angle);
            serverLevel.sendParticles(ParticleTypes.END_ROD, x, centerPos.y, z, 1, 0, 0, 0, 0);
        }

        //垂直圆圈（墨囊粒子）
        for (int i = 0; i < particleCount; i++) {
            double angle = (2 * Math.PI * i) / particleCount;
            double x = centerPos.x + radius * Math.cos(angle);
            double y = centerPos.y + radius * Math.sin(angle);
            serverLevel.sendParticles(ParticleTypes.SQUID_INK, x, y, centerPos.z, 1, 0, 0, 0, 0);
        }
    }

    //生成万物终焉收缩粒子圆圈
    public static void spawnAllThingsEndShrinkingCircles(Level level, Vec3 center, double originalRadius, double shrinkProgress) {
        if (level == null || level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        int particleCount = Math.max(8, (int)(originalRadius * 16));

        //水平圆圈收缩
        for (int i = 0; i < particleCount; i++) {
            double angle = (2 * Math.PI * i) / particleCount;

            //计算原始位置
            double originalX = center.x + Math.cos(angle) * originalRadius;
            double originalZ = center.z + Math.sin(angle) * originalRadius;
            double originalY = center.y;

            //计算当前位置（向圆心收缩）
            double currentX = originalX + (center.x - originalX) * shrinkProgress;
            double currentY = originalY + (center.y - originalY) * shrinkProgress;
            double currentZ = originalZ + (center.z - originalZ) * shrinkProgress;

            serverLevel.sendParticles(ParticleTypes.END_ROD, currentX, currentY, currentZ, 1, 0.0, 0.0, 0.0, 0.0);
        }

        //垂直圆圈收缩
        for (int i = 0; i < particleCount; i++) {
            double angle = (2 * Math.PI * i) / particleCount;

            //计算原始位置
            double originalX = center.x + Math.cos(angle) * originalRadius;
            double originalY = center.y + Math.sin(angle) * originalRadius;
            double originalZ = center.z;

            //计算当前位置（向圆心收缩）
            double currentX = originalX + (center.x - originalX) * shrinkProgress;
            double currentY = originalY + (center.y - originalY) * shrinkProgress;
            double currentZ = originalZ + (center.z - originalZ) * shrinkProgress;

            serverLevel.sendParticles(ParticleTypes.SQUID_INK, currentX, currentY, currentZ, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    //生成万物终焉圆心龙息粒子
    public static void spawnAllThingsEndCenterParticles(Level level, Vec3 centerPos, boolean intensive) {
        if (level == null || level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        //根据是否为收缩期调整粒子密度
        int particleCount = intensive ? 8 : 5;
        double spread = intensive ? 0.2 : 0.1;
        double speed = intensive ? 0.05 : 0.02;

        serverLevel.sendParticles(
            ParticleTypes.DRAGON_BREATH,
            centerPos.x, centerPos.y, centerPos.z,
            particleCount, spread, spread, spread, speed
        );
    }

    //线性插值工具方法
    private static double lerp(double t, double start, double end) {
        return start + t * (end - start);
    }
}
