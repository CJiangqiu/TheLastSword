package net.the_last_sword.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.the_last_sword.defence.DefenceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Player.class)
public class PlayerMixin {

    //3级防御保护：无法被玩家攻击
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void theLastSword$preventAttackOnProtectedEntities(Entity target, CallbackInfo ci) {
        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        int defenseLevel = DefenceManager.hasDefenceRecord(livingTarget) ? DefenceManager.getDefenceLevel(livingTarget) : 0;
        if (defenseLevel >= 3) {
            ci.cancel();
        }
    }
}
