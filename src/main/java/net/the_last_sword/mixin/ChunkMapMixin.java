package net.the_last_sword.mixin;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.the_last_sword.util.EntityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChunkMap.class, priority = 1024)
public class ChunkMapMixin {

    @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
    private void onChunkMapAddEntity(Entity entity, CallbackInfo ci) {
        if (entity.level() instanceof ServerLevel level) {
            if (EntityUtil.isReviveBanned(level, entity.getType())) {
                ci.cancel();
            }
        }
    }

}
