package net.the_last_sword.compat.irons_spellbooks;

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.DeferredRegister;
import net.the_last_sword.TheLastSwordMod;

public final class SpellIntegration {
    private static final DeferredRegister<SchoolType> SCHOOLS =
        DeferredRegister.create(SchoolRegistry.SCHOOL_REGISTRY_KEY, TheLastSwordMod.MOD_ID);
    private static final DeferredRegister<AbstractSpell> SPELLS =
        DeferredRegister.create(SpellRegistry.SPELL_REGISTRY_KEY, TheLastSwordMod.MOD_ID);

    private SpellIntegration() {
    }

    public static void register(IEventBus eventBus) {
        SCHOOLS.register("the_last_end", () -> new SchoolType(
            ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID, "the_last_end"),
            TagKey.create(Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID, "the_last_end_focus")),
            Component.translatable("school.the_last_sword.the_last_end").withStyle(ChatFormatting.DARK_PURPLE),
            () -> null,
            () -> null,
            () -> SoundEvents.ENCHANTMENT_TABLE_USE,
            DamageTypes.FELL_OUT_OF_WORLD));
        SPELLS.register("void_enchanting", VoidEnchantingSpell::new);
        SPELLS.register("phasing", PhasingSpell::new);
        SPELLS.register("double_strike", DoubleStrikeSpell::new);
        SPELLS.register("blink", BlinkSpell::new);
        SPELLS.register("group_protection", GroupProtectionSpell::new);
        SPELLS.register("judgment_thunder", JudgmentThunderSpell::new);
        SPELLS.register("triple_slash", TripleSlashSpell::new);
        SPELLS.register("the_last_end_crystal", TheLastEndCrystalSpell::new);
        MinecraftForge.EVENT_BUS.addListener(DoubleStrikeSpell::onCooldownAdded);
        SCHOOLS.register(eventBus);
        SPELLS.register(eventBus);
    }

    public static void addScrolls(CreativeModeTab.Output output) {
        for (var entry : SPELLS.getEntries()) {
            AbstractSpell spell = entry.get();
            if (!spell.isEnabled()) {
                continue;
            }
            for (int level = spell.getMinLevel(); level <= spell.getMaxLevel(); level++) {
                ItemStack scroll = new ItemStack(ItemRegistry.SCROLL.get());
                ISpellContainer.createScrollContainer(spell, level, scroll);
                output.accept(scroll);
            }
        }
    }
}
