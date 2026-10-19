package net.the_last_sword.mixin;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Pseudo
@Mixin(targets = "io.redspace.ironsspellbooks.util.TooltipsUtils", remap = false)
public abstract class SpellCooldownTooltipMixin {
    // 保留原冷却计算调用，只修正本模组法术已经生成的冷却行。
    @Inject(method = "formatActiveSpellTooltip", at = @At("RETURN"), cancellable = true, remap = false)
    private static void theLastSword$activeCooldown(ItemStack stack, SpellData data, CastSource source,
            LocalPlayer player, CallbackInfoReturnable<List<MutableComponent>> callback) {
        Integer cooldown = theLastSword$cooldown(data.getSpell(), player, source, data.getLevel());
        if (cooldown == null) {
            return;
        }
        List<MutableComponent> lines = new ArrayList<>(callback.getReturnValue());
        for (int i = 0; i < lines.size(); i++) {
            MutableComponent replacement = theLastSword$cooldownLine(lines.get(i), cooldown);
            if (replacement != null) {
                lines.set(i, replacement);
            }
        }
        callback.setReturnValue(lines);
    }

    @Inject(method = "formatScrollTooltip", at = @At("RETURN"), cancellable = true, remap = false)
    private static void theLastSword$scrollCooldown(ItemStack stack, Player player,
            CallbackInfoReturnable<List<Component>> callback) {
        if (callback.getReturnValue().isEmpty() || !ISpellContainer.isSpellContainer(stack)) {
            return;
        }
        ISpellContainer container = ISpellContainer.get(stack);
        if (container.isEmpty()) {
            return;
        }
        SpellData data = container.getSpellAtIndex(0);
        Integer cooldown = theLastSword$cooldown(data.getSpell(), player, CastSource.SCROLL, data.getLevel());
        if (cooldown == null) {
            return;
        }
        List<Component> lines = new ArrayList<>(callback.getReturnValue());
        for (int i = 0; i < lines.size(); i++) {
            MutableComponent replacement = theLastSword$cooldownLine(lines.get(i), cooldown);
            if (replacement != null) {
                lines.set(i, replacement);
            }
        }
        callback.setReturnValue(lines);
    }

    @Unique
    private static MutableComponent theLastSword$cooldownLine(Component line, int cooldown) {
        if (!(line.getContents() instanceof TranslatableContents contents)
                || !contents.getKey().equals("tooltip.irons_spellbooks.cooldown_length_seconds")) {
            return null;
        }
        MutableComponent replacement = Component.translatableWithFallback(contents.getKey(),
            contents.getFallback(), Utils.timeFromTicks(cooldown, 2)).withStyle(line.getStyle());
        line.getSiblings().forEach(sibling -> replacement.append(sibling.copy()));
        return replacement;
    }

    @Unique
    private static Integer theLastSword$cooldown(AbstractSpell spell, Player player, CastSource source, int level) {
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
        return null;
    }
}
