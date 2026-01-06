package net.the_last_sword.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.init.ModEffects;
import net.eca.api.EcaAPI;

//虚化效果：穿墙、透视、免疫伤害（通过ECA无敌）
public class PhasingEffect extends MobEffect {

    public PhasingEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xC0C0C0); // 浅灰色
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(entity, attributeMap, amplifier);

        //虚化效果设置ECA无敌
        if (!entity.level().isClientSide) {
            EcaAPI.setInvulnerable(entity, true);
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);

        //只在buff真正结束时执行清理（检查实体是否还有虚化效果）
        if (!entity.hasEffect(ModEffects.PHASING.get())) {
            //移除ECA无敌
            if (!entity.level().isClientSide) {
                EcaAPI.setInvulnerable(entity, false);
            }

            //检查配置是否启用虚化结束时破坏方块功能
            if (TheLastSwordConfiguration.getPhasingBreakBlocksOnEndSafely()) {
                //确保只破坏一次：检查标记
                if (!entity.getPersistentData().getBoolean("PhasingBlocksBroken")) {
                    entity.getPersistentData().putBoolean("PhasingBlocksBroken", true);
                    breakBlocksInEntityBoundingBox(entity);
                    //破坏完成后移除标记，为下次虚化做准备
                    entity.getPersistentData().remove("PhasingBlocksBroken");
                }
            }
        }
    }

    //破坏实体碰撞箱范围内会导致窒息的固体方块
    private void breakBlocksInEntityBoundingBox(LivingEntity entity) {
        Level level = entity.level();
        if (level.isClientSide) return;

        AABB boundingBox = entity.getBoundingBox();

        BlockPos minPos = new BlockPos(
            (int) Math.floor(boundingBox.minX),
            (int) Math.floor(boundingBox.minY),
            (int) Math.floor(boundingBox.minZ)
        );
        BlockPos maxPos = new BlockPos(
            (int) Math.floor(boundingBox.maxX),
            (int) Math.floor(boundingBox.maxY),
            (int) Math.floor(boundingBox.maxZ)
        );

        for (BlockPos pos : BlockPos.betweenClosed(minPos, maxPos)) {
            var blockState = level.getBlockState(pos);

            if (!blockState.isAir() && blockState.isSuffocating(level, pos)) {
                level.destroyBlock(pos, true);
            }
        }
    }

    @Override
    public boolean isInstantenous() {
        return false;
    }
}
