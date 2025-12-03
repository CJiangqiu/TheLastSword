package net.the_last_sword.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.the_last_sword.defence.DefenceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = ClientLevel.class, priority = 1100)
public class ClientLevelMixin {

    @Inject(method = "removeEntity", at = @At("HEAD"), cancellable = true, require = 0)
    private void theLastSword$preventClientRemoval(int entityId, Entity.RemovalReason reason, CallbackInfo ci) {
        try {
            ClientLevel clientLevel = (ClientLevel) (Object) this;
            Entity entity = clientLevel.getEntity(entityId);

            if (entity instanceof LivingEntity livingEntity) {
                int level = DefenceManager.hasDefenceRecord(livingEntity) ? DefenceManager.getDefenceLevel(livingEntity) : 0;
                if (level >= 2) {
                    ci.cancel();
                }
            }
        } catch (Exception e) {
        }
    }

}
