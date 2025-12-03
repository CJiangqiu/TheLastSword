package net.the_last_sword.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.the_last_sword.defence.DefenceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.EntityCallbacks.class)
public class ClientEntityCallbacksMixin {

    @Inject(method = "onTrackingEnd*", at = @At("HEAD"), cancellable = true)
    private void theLastSword$preventTrackingEnd(Entity entity, CallbackInfo ci) {
        if (entity instanceof LivingEntity livingEntity) {
            int level = DefenceManager.hasDefenceRecord(livingEntity) ? DefenceManager.getDefenceLevel(livingEntity) : 0;
            if (level >= 2) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "onTickingEnd*", at = @At("HEAD"), cancellable = true)
    private void theLastSword$preventTickingEnd(Entity entity, CallbackInfo ci) {
        if (entity instanceof LivingEntity livingEntity) {
            int level = DefenceManager.hasDefenceRecord(livingEntity) ? DefenceManager.getDefenceLevel(livingEntity) : 0;
            if (level >= 2) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "onDestroyed*", at = @At("HEAD"), cancellable = true)
    private void theLastSword$preventDestroyed(Entity entity, CallbackInfo ci) {
        if (entity instanceof LivingEntity livingEntity) {
            int level = DefenceManager.hasDefenceRecord(livingEntity) ? DefenceManager.getDefenceLevel(livingEntity) : 0;
            if (level >= 2) {
                ci.cancel();
            }
        }
    }
}
