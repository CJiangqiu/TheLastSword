package net.the_last_sword.mixin;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.the_last_sword.defence.DefenceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "interactLivingEntity", at = @At("HEAD"), cancellable = true)
    private void theLastSword$preventInteractionWithProtectedEntities(Player player, LivingEntity entity, net.minecraft.world.InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
            int defenseLevel = DefenceManager.hasDefenceRecord(entity) ? DefenceManager.getDefenceLevel(entity) : 0;
            if (defenseLevel >= 3) {
                cir.setReturnValue(InteractionResult.FAIL);
            }
    }
}
