package net.the_last_sword.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import net.the_last_sword.util.EntityUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TransientEntitySectionManager.class)
public class TransientEntitySectionManagerMixin {

    //阻止禁复活实体添加到临时实体管理器
    @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
    private void onTransientEntitySectionManagerAddEntity(EntityAccess entity, CallbackInfo ci) {
        if (entity instanceof Entity realEntity && realEntity.level() instanceof ServerLevel level) {
            if (EntityUtil.isReviveBanned(level, realEntity.getType())) {
                ci.cancel();
            }
        }
    }

    @Mixin(TransientEntitySectionManager.Callback.class)
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
