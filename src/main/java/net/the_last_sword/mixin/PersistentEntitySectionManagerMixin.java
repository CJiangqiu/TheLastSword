package net.the_last_sword.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.the_last_sword.util.EntityUtil;
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
        if (entity instanceof Entity realEntity && realEntity.level() instanceof ServerLevel level) {
            if (EntityUtil.isReviveBanned(level, realEntity.getType())) {
                cir.setReturnValue(false);
            }
        }
    }

    //阻止禁复活实体添加到实体管理器
    @Inject(method = "addNewEntity", at = @At("HEAD"), cancellable = true)
    private void onPersistentEntitySectionManagerAddNewEntity(EntityAccess entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Entity realEntity && realEntity.level() instanceof ServerLevel level) {
            if (EntityUtil.isReviveBanned(level, realEntity.getType())) {
                cir.setReturnValue(false);
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
            //维度切换时不阻止移除（否则会导致重复UUID）
            if (reason == Entity.RemovalReason.CHANGED_DIMENSION) {
                return;
            }
            if (this.entity instanceof LivingEntity living) {
                if (EntityUtil.hasProtection(living)) {
                    ci.cancel();
                }
            }
        }
    }
}
