package net.the_last_sword.mixin;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.the_last_sword.attack.AttackManager;
import net.the_last_sword.defence.DefenceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChunkMap.class, priority = 1024)
public class ChunkMapMixin {

    @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
    private void onChunkMapAddEntity(Entity entity, CallbackInfo ci) {
        Integer remainingTime = AttackManager.getAllReviveBanTypes().get(entity.getClass());
        if (remainingTime != null && remainingTime > 0) {
            ci.cancel();
        }
    }

    @Inject(method = "removeEntity", at = @At("HEAD"), cancellable = true)
    private void onChunkMapRemoveEntity(Entity entity, CallbackInfo ci) {
        if (entity instanceof LivingEntity living) {
            int level = DefenceManager.hasDefenceRecord(living) ? DefenceManager.getDefenceLevel(living) : 0;
            if (level >= 2) {
                ci.cancel();
            }
        }
    }
}
