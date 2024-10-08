package net.thelastsword.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.thelastsword.damagetype.AbsoluteDestruction;
import net.thelastsword.entity.TestEntity;
import net.thelastsword.event.TheLastDead;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class AbsoluteDestructionLivingEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        float absoluteHealth = AbsoluteDestruction.getAbsoluteHealth(entity);
        if (absoluteHealth <= 0) {
            TheLastDead.theLastDead(entity);
        }
    }

    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private void onRemove(Entity.RemovalReason reason, CallbackInfo ci) {
        if ((Object) this instanceof TestEntity) {
            ci.cancel();
        }
    }

    @Inject(method = "setHealth", at = @At("HEAD"), cancellable = true)
    private void onSetHealth(float health, CallbackInfo ci) {
        if ((Object) this instanceof TestEntity) {
            ci.cancel();
        }
    }

    @Inject(method = "getMaxHealth", at = @At("HEAD"), cancellable = true)
    private void onGetMaxHealth(CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof TestEntity) {
            cir.setReturnValue(1024.0F); // 设置最大生命值为1024
        }
    }
}
