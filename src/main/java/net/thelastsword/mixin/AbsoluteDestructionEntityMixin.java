package net.thelastsword.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.thelastsword.event.TheLastDead;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.thelastsword.damagetype.AbsoluteDestruction;
@Mixin(LivingEntity.class)
public abstract class AbsoluteDestructionEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        float absoluteHealth = AbsoluteDestruction.getAbsoluteHealth(entity);
        if (absoluteHealth <= 0) {
            TheLastDead.theLastDead(entity);
        }
    }
}
