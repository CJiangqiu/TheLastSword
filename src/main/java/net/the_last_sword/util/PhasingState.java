package net.the_last_sword.util;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.the_last_sword.init.ModEffects;

public final class PhasingState {
    public static EntityDataAccessor<Boolean> PHASING;

    private PhasingState() {
    }

    public static boolean isPhasing(LivingEntity entity) {
        // 旁观客户端没有普通生物的完整效果列表，渲染需要使用同步标记。
        return entity.level().isClientSide
                ? entity.getEntityData().get(PHASING)
                : entity.hasEffect(ModEffects.PHASING.get());
    }

    public static void sync(LivingEntity entity) {
        if (!entity.level().isClientSide) {
            entity.getEntityData().set(PHASING, entity.hasEffect(ModEffects.PHASING.get()));
        }
    }
}
