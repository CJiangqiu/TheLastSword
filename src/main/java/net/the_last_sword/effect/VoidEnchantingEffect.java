package net.the_last_sword.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.configuration.TheLastSwordConfiguration;

//虚空附魔效果
//持有此效果的实体攻击其他实体时会造成额外的虚空伤害
//1级 = 20%额外虚空伤害，每级增加20%
//持续期间在实体脚下显示旋转法阵特效
public class VoidEnchantingEffect extends MobEffect {

    public VoidEnchantingEffect() {
        super(MobEffectCategory.BENEFICIAL, -13421773); // 深紫色
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);

        if (!entity.level().isClientSide
            && entity.level() instanceof ServerLevel serverLevel
            && TheLastSwordConfiguration.getVoidEnchantmentParticleEffectsSafely()) {
            spawnEnchantParticles(entity, serverLevel);
        }
    }

    @Override
    public boolean isInstantenous() {
        return false;
    }

    //生成附魔法阵粒子效果（五角星+圆环）
    public static void spawnEnchantParticles(LivingEntity entity, ServerLevel serverLevel) {
        double centerX = entity.getX();
        double centerY = entity.getY();
        double centerZ = entity.getZ();
        double radius = 1.5;
        float rotateAngle = (serverLevel.getGameTime() % 360) * 2;

        //生成旋转圆环
        for (int i = 0; i < 18; i++) {
            double angle = Math.toRadians(rotateAngle + i * 20);
            serverLevel.sendParticles(
                ParticleTypes.PORTAL,
                centerX + radius * Math.cos(angle),
                centerY,
                centerZ + radius * Math.sin(angle),
                2, 0.1, 0.1, 0.1, 0.02
            );
        }

        //生成旋转五角星
        int starPoints = 5;
        double starRadius = radius * 0.8;
        for (int i = 0; i < starPoints; i++) {
            double angle1 = Math.toRadians(rotateAngle + 72 * i);
            double angle2 = Math.toRadians(rotateAngle + 72 * i + 144);

            Vec3 start = new Vec3(
                centerX + starRadius * Math.cos(angle1),
                centerY,
                centerZ + starRadius * Math.sin(angle1)
            );

            Vec3 end = new Vec3(
                centerX + starRadius * Math.cos(angle2),
                centerY,
                centerZ + starRadius * Math.sin(angle2)
            );

            int steps = 8;
            for (int j = 0; j <= steps; j++) {
                double t = (double) j / steps;
                Vec3 pos = start.add(end.subtract(start).scale(t));

                serverLevel.sendParticles(
                    ParticleTypes.ENCHANT,
                    pos.x, pos.y, pos.z,
                    1, 0.0, 0.1, 0.0, 0.0
                );
            }
        }
    }
}
