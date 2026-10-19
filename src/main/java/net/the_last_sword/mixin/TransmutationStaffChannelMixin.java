package net.the_last_sword.mixin;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.the_last_sword.compat.irons_spellbooks.TransmutationStaffCasting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "io.redspace.ironsspellbooks.capabilities.magic.MagicManager", remap = false)
public abstract class TransmutationStaffChannelMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, remap = false)
    private void theLastSword$scopeChannel(Level level, CallbackInfo callback) {
        if (TransmutationStaffCasting.isPowerScopeActive() || level.players().stream()
                .noneMatch(player -> TransmutationStaffCasting.hasPower(MagicData.getPlayerMagicData(player)))) {
            return;
        }
        MagicManager manager = (MagicManager) (Object) this;
        TransmutationStaffCasting.withPowerScope(() -> {
            manager.tick(level);
            return null;
        });
        callback.cancel();
    }

    // 每刻效果与直接施法共享起手快照，且不依赖调用方的合成方法名称。
    @Inject(method = "*", at = @At(value = "INVOKE",
        target = "Lio/redspace/ironsspellbooks/api/spells/AbstractSpell;onServerCastTick(Lnet/minecraft/world/level/Level;ILnet/minecraft/world/entity/LivingEntity;Lio/redspace/ironsspellbooks/api/magic/MagicData;)V"), remap = false)
    private void theLastSword$channel(boolean doManaRegen, Player player, CallbackInfo callback) {
        TransmutationStaffCasting.beginPower(player, MagicData.getPlayerMagicData(player));
    }

    @Inject(method = "*", at = @At(value = "INVOKE",
        target = "Lio/redspace/ironsspellbooks/api/spells/AbstractSpell;onServerCastTick(Lnet/minecraft/world/level/Level;ILnet/minecraft/world/entity/LivingEntity;Lio/redspace/ironsspellbooks/api/magic/MagicData;)V",
        shift = At.Shift.AFTER), remap = false)
    private void theLastSword$afterChannel(boolean doManaRegen, Player player, CallbackInfo callback) {
        TransmutationStaffCasting.endPower();
    }
}
