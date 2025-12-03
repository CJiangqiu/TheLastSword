package net.the_last_sword.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityLookup;
import net.the_last_sword.attack.AttackManager;
import net.the_last_sword.defence.DefenceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityLookup.class)
public class EntityLookupMixin {

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void onEntityLookupAdd(EntityAccess entity, CallbackInfo ci) {
        if (entity instanceof Entity realEntity) {
            Integer remainingTime = AttackManager.getAllReviveBanTypes().get(realEntity.getClass());
            if (remainingTime != null && remainingTime > 0) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private void onEntityLookupRemove(EntityAccess entity, CallbackInfo ci) {
        if (entity instanceof LivingEntity living) {
            int level = DefenceManager.hasDefenceRecord(living) ? DefenceManager.getDefenceLevel(living) : 0;
            if (level >= 2) {
                ci.cancel();
            }
        }
    }
}
