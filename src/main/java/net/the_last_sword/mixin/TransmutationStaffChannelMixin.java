package net.the_last_sword.mixin;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.the_last_sword.compat.irons_spellbooks.TransmutationStaffCasting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "io.redspace.ironsspellbooks.capabilities.magic.MagicManager", remap = false)
public abstract class TransmutationStaffChannelMixin {
    // 每刻效果与直接施法共享起手快照，且不依赖调用方的合成方法名称。
    @Redirect(method = "*", at = @At(value = "INVOKE",
        target = "Lio/redspace/ironsspellbooks/api/spells/AbstractSpell;onServerCastTick(Lnet/minecraft/world/level/Level;ILnet/minecraft/world/entity/LivingEntity;Lio/redspace/ironsspellbooks/api/magic/MagicData;)V"), remap = false)
    private void theLastSword$channel(AbstractSpell spell, Level level, int spellLevel,
                                      LivingEntity caster, MagicData data) {
        TransmutationStaffCasting.withPower(caster, data,
            () -> spell.onServerCastTick(level, spellLevel, caster, data));
    }
}
