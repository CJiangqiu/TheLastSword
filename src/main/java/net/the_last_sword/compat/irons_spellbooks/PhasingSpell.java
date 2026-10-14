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

public class PhasingSpell extends AbstractSpell {
    private static final ResourceLocation SPELL_ID =
        ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID, "phasing");
    private final DefaultConfig defaultConfig = new DefaultConfig()
        .setMinRarity(SpellRarity.COMMON)
        .setSchoolResource(ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID, "the_last_end"))
        .setMaxLevel(10)
        .setCooldownSeconds(30)
        .build();

    public PhasingSpell() {
        baseManaCost = 50;
        manaCostPerLevel = 10;
        castTime = 20;
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
        return List.of(Component.translatable("ui.the_last_sword.phasing", getDurationTicks(spellLevel) / 20));
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity caster, CastSource castSource,
                       MagicData magicData) {
        caster.addEffect(new MobEffectInstance(ModEffects.PHASING.get(), getDurationTicks(spellLevel), 0));
        super.onCast(level, spellLevel, caster, castSource, magicData);
    }

    private static int getDurationTicks(int spellLevel) {
        // 法术等级只延长虚化时间，不改变效果等级或叠加法术强度倍率。
        return (5 + spellLevel - 1) * 20;
    }
}
