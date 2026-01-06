package net.the_last_sword.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.world.entity.Entity;
import net.the_last_sword.util.EntityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClassInstanceMultiMap.class)
public class ClassInstanceMultiMapMixin {

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void onClassInstanceMultiMapAdd(Object object, CallbackInfoReturnable<Boolean> cir) {
        if (object instanceof Entity entity && entity.level() instanceof ServerLevel level) {
            if (EntityUtil.isReviveBanned(level, entity.getType())) {
                cir.setReturnValue(false);
            }
        }
    }
}
