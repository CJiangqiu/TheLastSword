package net.the_last_sword.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.the_last_sword.attack.AttackManager;
import net.the_last_sword.defence.DefenceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PersistentEntitySectionManager.class)
public class PersistentEntitySectionManagerMixin {

    //阻止禁复活实体添加到实体管理器
    @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
    private void onPersistentEntitySectionManagerAddEntity(EntityAccess entity, boolean flag, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Entity realEntity) {
            Integer remainingTime = AttackManager.getAllReviveBanTypes().get(realEntity.getClass());
            if (remainingTime != null && remainingTime > 0) {
                cir.setReturnValue(false);
            }
        }
    }

    //阻止禁复活实体添加到实体管理器
    @Inject(method = "addNewEntity", at = @At("HEAD"), cancellable = true)
    private void onPersistentEntitySectionManagerAddNewEntity(EntityAccess entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Entity realEntity) {
            Integer remainingTime = AttackManager.getAllReviveBanTypes().get(realEntity.getClass());
            if (remainingTime != null && remainingTime > 0) {
                cir.setReturnValue(false);
            }
        }
    }

    //阻止防御等级≥2的实体通过unloadEntity被移除
    @Inject(method = "unloadEntity", at = @At("HEAD"), cancellable = true)
    private void onPersistentEntitySectionManagerUnloadEntity(EntityAccess entity, CallbackInfo ci) {
        if (entity instanceof LivingEntity living) {
            int level = DefenceManager.hasDefenceRecord(living) ? DefenceManager.getDefenceLevel(living) : 0;
            if (level >= 2) {
                ci.cancel();
            }
        }
    }

    //阻止防御等级≥2的实体通过stopTicking被移除
    @Inject(method = "stopTicking", at = @At("HEAD"), cancellable = true)
    private void onPersistentEntitySectionManagerStopTicking(EntityAccess entity, CallbackInfo ci) {
        if (entity instanceof LivingEntity living) {
            int level = DefenceManager.hasDefenceRecord(living) ? DefenceManager.getDefenceLevel(living) : 0;
            if (level >= 2) {
                ci.cancel();
            }
        }
    }

    //阻止防御等级≥2的实体通过stopTracking被移除
    @Inject(method = "stopTracking", at = @At("HEAD"), cancellable = true)
    private void onPersistentEntitySectionManagerStopTracking(EntityAccess entity, CallbackInfo ci) {
        if (entity instanceof LivingEntity living) {
            int level = DefenceManager.hasDefenceRecord(living) ? DefenceManager.getDefenceLevel(living) : 0;
            if (level >= 2) {
                ci.cancel();
            }
        }
    }

    @Mixin(PersistentEntitySectionManager.Callback.class)
    public static class CallbackMixin {
        @Final
        @Shadow
        private EntityAccess entity;

        @Inject(method = "onRemove", at = @At("HEAD"), cancellable = true)
        private void onCallbackOnRemove(Entity.RemovalReason reason, CallbackInfo ci) {
            if (this.entity instanceof LivingEntity living) {
                int level = DefenceManager.hasDefenceRecord(living) ? DefenceManager.getDefenceLevel(living) : 0;
                if (level >= 2) {
                    ci.cancel();
                }
            }
        }
    }
}
