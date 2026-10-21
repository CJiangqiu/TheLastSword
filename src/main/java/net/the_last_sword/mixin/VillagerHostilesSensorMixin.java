package net.the_last_sword.mixin;

import net.eca.util.faction.FactionUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.VillagerHostilesSensor;
import net.the_last_sword.faction.DragonCultFaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerHostilesSensor.class)
public abstract class VillagerHostilesSensorMixin {

    @Unique
    private static final double the_last_sword$THREAT_DISTANCE_SQUARED = 12.0D * 12.0D;

    @Inject(method = "isMatchingEntity", at = @At("HEAD"), cancellable = true)
    private void the_last_sword$recognizeDragonCult(LivingEntity villager, LivingEntity candidate,
                                                   CallbackInfoReturnable<Boolean> cir) {
        if (DragonCultFaction.isWildMember(candidate)) {
            // 保留原版的可见实体筛选和恐慌记忆，只补充缺少的威胁类型。
            cir.setReturnValue(candidate.distanceToSqr(villager) <= the_last_sword$THREAT_DISTANCE_SQUARED
                    && FactionUtil.canTarget(candidate, villager));
        }
    }
}
