package net.the_last_sword.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTickList;
import net.the_last_sword.util.EntityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTickList.class)
public class EntityTickListMixin {

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void onEntityTickListAdd(Entity entity, CallbackInfo ci) {
        if (entity == null) {
            ci.cancel();
            return;
        }
        if (entity.level() instanceof ServerLevel level) {
            if (EntityUtil.isReviveBanned(level, entity.getType())) {
                ci.cancel();
            }
        }
    }
}
