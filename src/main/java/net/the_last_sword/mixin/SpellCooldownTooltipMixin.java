package net.the_last_sword.mixin;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.the_last_sword.compat.irons_spellbooks.DoubleStrikeSpell;
import net.the_last_sword.compat.irons_spellbooks.BlinkSpell;
import net.the_last_sword.compat.irons_spellbooks.GroupProtectionSpell;
import net.the_last_sword.compat.irons_spellbooks.JudgmentThunderSpell;
import net.the_last_sword.compat.irons_spellbooks.TripleSlashSpell;
import net.the_last_sword.compat.irons_spellbooks.TheLastEndCrystalSpell;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "io.redspace.ironsspellbooks.util.TooltipsUtils", remap = false)
public abstract class SpellCooldownTooltipMixin {
    @Redirect(method = "formatActiveSpellTooltip", at = @At(value = "INVOKE",
        target = "Lio/redspace/ironsspellbooks/capabilities/magic/MagicManager;getEffectiveSpellCooldown(Lio/redspace/ironsspellbooks/api/spells/AbstractSpell;Lnet/minecraft/world/entity/player/Player;Lio/redspace/ironsspellbooks/api/spells/CastSource;)I"), remap = false)
    private static int theLastSword$activeCooldown(AbstractSpell spell, Player player, CastSource source,
                                                  ItemStack stack, SpellData data, CastSource castSource,
                                                  LocalPlayer localPlayer) {
        return theLastSword$cooldown(spell, player, source, data.getLevel());
    }

    @Redirect(method = "formatScrollTooltip", at = @At(value = "INVOKE",
        target = "Lio/redspace/ironsspellbooks/capabilities/magic/MagicManager;getEffectiveSpellCooldown(Lio/redspace/ironsspellbooks/api/spells/AbstractSpell;Lnet/minecraft/world/entity/player/Player;Lio/redspace/ironsspellbooks/api/spells/CastSource;)I"), remap = false)
    private static int theLastSword$scrollCooldown(AbstractSpell spell, Player player, CastSource source,
                                                  ItemStack stack, Player tooltipPlayer) {
        return theLastSword$cooldown(spell, player, source, ISpellContainer.get(stack).getSpellAtIndex(0).getLevel());
    }

    private static int theLastSword$cooldown(AbstractSpell spell, Player player, CastSource source, int level) {
        if (spell instanceof DoubleStrikeSpell doubleStrike) {
            return doubleStrike.getEffectiveCooldown(spell.getLevelFor(level, player), player, source);
        }
        if (spell instanceof BlinkSpell blink) {
            return blink.getEffectiveCooldown(spell.getLevelFor(level, player), player, source);
        }
        if (spell instanceof GroupProtectionSpell protection) {
            return protection.getEffectiveCooldown(spell.getLevelFor(level, player), player, source);
        }
        if (spell instanceof JudgmentThunderSpell thunder) {
            return thunder.getEffectiveCooldown(spell.getLevelFor(level, player), player, source);
        }
        if (spell instanceof TripleSlashSpell tripleSlash) {
            return tripleSlash.getEffectiveCooldown(spell.getLevelFor(level, player), player, source);
        }
        if (spell instanceof TheLastEndCrystalSpell crystal) {
            return crystal.getEffectiveCooldown(spell.getLevelFor(level, player), player, source);
        }
        return MagicManager.getEffectiveSpellCooldown(spell, player, source);
    }
}
