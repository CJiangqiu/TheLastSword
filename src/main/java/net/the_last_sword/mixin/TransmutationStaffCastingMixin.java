package net.the_last_sword.mixin;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.the_last_sword.compat.irons_spellbooks.TransmutationStaffCasting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "io.redspace.ironsspellbooks.api.spells.AbstractSpell", remap = false)
public abstract class TransmutationStaffCastingMixin {
    // 常规施法条件与取消事件通过后才扣费，引导的后续结算不再重复扣费。
    @Inject(method = "attemptInitiateCast", at = @At(value = "INVOKE",
        target = "Lio/redspace/ironsspellbooks/api/magic/MagicData;initiateCast(Lio/redspace/ironsspellbooks/api/spells/AbstractSpell;IILio/redspace/ironsspellbooks/api/spells/CastSource;Ljava/lang/String;)V"),
        cancellable = true, remap = false)
    private void theLastSword$prepare(ItemStack stack, int spellLevel, Level level, Player player,
                                      CastSource source, boolean triggerCooldown, String slot,
                                      CallbackInfoReturnable<Boolean> callback) {
        if (!TransmutationStaffCasting.prepare(player, stack, MagicData.getPlayerMagicData(player))) {
            callback.setReturnValue(false);
        }
    }

    @Redirect(method = "attemptInitiateCast", at = @At(value = "INVOKE",
        target = "Lio/redspace/ironsspellbooks/api/spells/AbstractSpell;onServerPreCast(Lnet/minecraft/world/level/Level;ILnet/minecraft/world/entity/LivingEntity;Lio/redspace/ironsspellbooks/api/magic/MagicData;)V"), remap = false)
    private void theLastSword$preCast(AbstractSpell spell, Level level, int spellLevel,
                                      LivingEntity caster, MagicData data) {
        TransmutationStaffCasting.withPower(caster, data,
            () -> spell.onServerPreCast(level, spellLevel, caster, data));
    }

    @Redirect(method = "castSpell", at = @At(value = "INVOKE",
        target = "Lio/redspace/ironsspellbooks/api/spells/AbstractSpell;onCast(Lnet/minecraft/world/level/Level;ILnet/minecraft/world/entity/LivingEntity;Lio/redspace/ironsspellbooks/api/spells/CastSource;Lio/redspace/ironsspellbooks/api/magic/MagicData;)V"), remap = false)
    private void theLastSword$cast(AbstractSpell spell, Level level, int spellLevel,
                                   LivingEntity caster, CastSource source, MagicData data) {
        TransmutationStaffCasting.withPower(caster, data,
            () -> spell.onCast(level, spellLevel, caster, source, data));
    }

    @Inject(method = "onServerCastComplete", at = @At("RETURN"), remap = false)
    private void theLastSword$complete(Level level, int spellLevel, LivingEntity caster,
                                       MagicData data, boolean cancelled, CallbackInfo callback) {
        TransmutationStaffCasting.clear(data);
    }
}
