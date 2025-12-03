package net.the_last_sword.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.EntityTickList;
import net.the_last_sword.attack.AttackManager;
import net.the_last_sword.defence.DefenceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTickList.class)
public class EntityTickListMixin {

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void onEntityTickListAdd(Entity entity, CallbackInfo ci) {
        Integer remainingTime = AttackManager.getAllReviveBanTypes().get(entity.getClass());
        if (remainingTime != null && remainingTime > 0) {
            ci.cancel();
        }
    }

    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private void onEntityTickListRemove(Entity entity, CallbackInfo ci) {
        if (entity instanceof LivingEntity living) {
            int level = DefenceManager.hasDefenceRecord(living) ? DefenceManager.getDefenceLevel(living) : 0;
            if (level >= 2) {
                ci.cancel();
            }
        }
    }
}
