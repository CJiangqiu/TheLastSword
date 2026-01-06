package net.the_last_sword.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySection;
import net.the_last_sword.util.EntityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntitySection.class)
public class EntitySectionMixin {

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void onEntitySectionAdd(EntityAccess entity, CallbackInfo ci) {
        if (entity instanceof Entity realEntity && realEntity.level() instanceof ServerLevel level) {
            if (EntityUtil.isReviveBanned(level, realEntity.getType())) {
                ci.cancel();
            }
        }
    }

}
