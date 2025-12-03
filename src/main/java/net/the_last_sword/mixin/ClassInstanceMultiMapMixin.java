package net.the_last_sword.mixin;

import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.the_last_sword.attack.AttackManager;
import net.the_last_sword.defence.DefenceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClassInstanceMultiMap.class)
public class ClassInstanceMultiMapMixin {

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void onClassInstanceMultiMapAdd(Object object, CallbackInfoReturnable<Boolean> cir) {
        if (object instanceof Entity entity) {
            Integer remainingTime = AttackManager.getAllReviveBanTypes().get(entity.getClass());
            if (remainingTime != null && remainingTime > 0) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private void onClassInstanceMultiMapRemove(Object object, CallbackInfoReturnable<Boolean> cir) {
        if (object instanceof LivingEntity living) {
            int level = DefenceManager.hasDefenceRecord(living) ? DefenceManager.getDefenceLevel(living) : 0;
            if (level >= 2) {
                cir.setReturnValue(false);
            }
        }
    }
}
