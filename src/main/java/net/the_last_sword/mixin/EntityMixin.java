package net.the_last_sword.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.the_last_sword.defence.DefenceManager;
import net.the_last_sword.init.ModEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Entity.class, priority = 1024)
public class EntityMixin {

    @Inject(method = "kill", at = @At("HEAD"), cancellable = true)
    private void theLastSword$preventKill(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self instanceof LivingEntity livingEntity) {
            int level = DefenceManager.hasDefenceRecord(livingEntity) ? DefenceManager.getDefenceLevel(livingEntity) : 0;
            if (level >= 1) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "discard", at = @At("HEAD"), cancellable = true)
    private void theLastSword$preventDiscard(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self instanceof LivingEntity livingEntity) {
            int level = DefenceManager.hasDefenceRecord(livingEntity) ? DefenceManager.getDefenceLevel(livingEntity) : 0;
            if (level >= 1) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private void theLastSword$preventRemove(Entity.RemovalReason reason, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof LivingEntity living)) {
            return;
        }
        int level = DefenceManager.hasDefenceRecord(living) ? DefenceManager.getDefenceLevel(living) : 0;
        if (level >= 1) {
            if (!(living instanceof Player && reason == Entity.RemovalReason.CHANGED_DIMENSION)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "setRemoved", at = @At("HEAD"), cancellable = true)
    private void theLastSword$preventSetRemoved(Entity.RemovalReason reason, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof LivingEntity living)) {
            return;
        }
        int level = DefenceManager.hasDefenceRecord(living) ? DefenceManager.getDefenceLevel(living) : 0;
        if (level >= 1) {
            if (!(living instanceof Player && reason == Entity.RemovalReason.CHANGED_DIMENSION)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "setLevelCallback", at = @At("HEAD"), cancellable = true)
    private void theLastSword$preventSetLevelCallback(EntityInLevelCallback callback, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof LivingEntity living)) {
            return;
        }

        int level = DefenceManager.hasDefenceRecord(living) ? DefenceManager.getDefenceLevel(living) : 0;
        if (level >= 2) {
            if (callback == EntityInLevelCallback.NULL) {
                ci.cancel();
            }
        }
    }

    //虚化效果：不判定为在墙中
    @Inject(method = "isInWall", at = @At("HEAD"), cancellable = true)
    private void theLastSword$phasingNotInWall(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (self instanceof LivingEntity livingEntity) {
            if (livingEntity.hasEffect(ModEffects.PHASING.get())) {
                cir.setReturnValue(false);
            }
        }
    }
}
