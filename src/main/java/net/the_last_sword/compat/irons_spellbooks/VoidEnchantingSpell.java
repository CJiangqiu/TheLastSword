package net.the_last_sword.compat.irons_spellbooks;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.init.ModEffects;

import java.util.List;

public class VoidEnchantingSpell extends AbstractSpell {
    private static final ResourceLocation SPELL_ID =
        ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID, "void_enchanting");
    private static final int DURATION_TICKS = 30 * 20;
    private final DefaultConfig defaultConfig = new DefaultConfig()
        .setMinRarity(SpellRarity.COMMON)
        .setSchoolResource(ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID, "the_last_end"))
        .setMaxLevel(10)
        .setCooldownSeconds(30)
        .build();

    public VoidEnchantingSpell() {
        baseManaCost = 10;
        manaCostPerLevel = 10;
        castTime = 3 * 20;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return SPELL_ID;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.the_last_sword.void_enchanting", spellLevel,
            DURATION_TICKS / 20));
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity caster, CastSource castSource,
                       MagicData magicData) {
        // 效果等级直接对应法术等级，避免法术强度再次放大附魔等级。
        caster.addEffect(new MobEffectInstance(ModEffects.VOID_ENCHANTING.get(), DURATION_TICKS,
            spellLevel - 1));
        super.onCast(level, spellLevel, caster, castSource, magicData);
    }
}
