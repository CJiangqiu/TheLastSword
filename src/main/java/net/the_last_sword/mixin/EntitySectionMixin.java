package net.the_last_sword.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySection;
import net.the_last_sword.attack.AttackManager;
import net.the_last_sword.defence.DefenceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntitySection.class)
public class EntitySectionMixin {

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void onEntitySectionAdd(EntityAccess entity, CallbackInfo ci) {
        if (entity instanceof Entity realEntity) {
            Integer remainingTime = AttackManager.getAllReviveBanTypes().get(realEntity.getClass());
            if (remainingTime != null && remainingTime > 0) {
                ci.cancel();
            }
        }
    }
    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private void onEntitySectionRemove(EntityAccess entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof LivingEntity living) {
            int level = DefenceManager.hasDefenceRecord(living) ? DefenceManager.getDefenceLevel(living) : 0;
            if (level >= 2) {
                cir.setReturnValue(false);
            }
        }
    }
}
