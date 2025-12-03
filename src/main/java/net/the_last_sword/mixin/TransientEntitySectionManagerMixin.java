package net.the_last_sword.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import net.the_last_sword.attack.AttackManager;
import net.the_last_sword.defence.DefenceManager;
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
        if (entity instanceof Entity realEntity) {
            Integer remainingTime = AttackManager.getAllReviveBanTypes().get(realEntity.getClass());
            if (remainingTime != null && remainingTime > 0) {
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
            if (this.entity instanceof LivingEntity living) {
                int level = DefenceManager.hasDefenceRecord(living) ? DefenceManager.getDefenceLevel(living) : 0;
                if (level >= 2) {
                    ci.cancel();
                }
            }
        }
    }
}
