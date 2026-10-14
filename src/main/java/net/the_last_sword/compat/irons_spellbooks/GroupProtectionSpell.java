package net.the_last_sword.compat.irons_spellbooks;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.util.PriestGuardEffect;

import java.util.List;

public class GroupProtectionSpell extends AbstractSpell {
    private static final ResourceLocation SPELL_ID =
        ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID, "group_protection");
    private final DefaultConfig defaultConfig = new DefaultConfig()
        .setMinRarity(SpellRarity.COMMON)
        .setSchoolResource(ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID, "the_last_end"))
        .setMaxLevel(10)
        .setCooldownSeconds(20)
        .build();
    // 冷却接口没有等级参数，限定调用范围以隔离不同玩家的等级。
    private final ThreadLocal<Integer> cooldownLevel = new ThreadLocal<>();

    public GroupProtectionSpell() {
        baseManaCost = 50;
        manaCostPerLevel = 20;
        castTime = 60;
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
        return List.of(
            Component.translatable("ui.the_last_sword.group_protection.radius", getRadius()),
            Component.translatable("ui.the_last_sword.group_protection.shield", getShieldGain(spellLevel)),
            Component.translatable("ui.the_last_sword.group_protection.purify"));
    }

    private static double getRadius() {
        return TheLastSwordConfiguration.getDragonCultPriestGuardRadiusSafely();
    }

    private static int getShieldGain(int spellLevel) {
        // 护盾按抵挡次数计数，只随等级增长，不叠加法术强度倍率。
        return Math.max(1, spellLevel);
    }

    @Override
    public int getSpellCooldown() {
        Integer spellLevel = cooldownLevel.get();
        return Math.max(0, super.getSpellCooldown() - (spellLevel == null ? 0 : Math.max(0, spellLevel - 1) * 20));
    }

    public int getEffectiveCooldown(int spellLevel, Player player, CastSource source) {
        Integer previousLevel = cooldownLevel.get();
        cooldownLevel.set(spellLevel);
        try {
            return MagicManager.getEffectiveSpellCooldown(this, player, source);
        } finally {
            restoreCooldownLevel(previousLevel);
        }
    }

    @Override
    public void castSpell(Level level, int spellLevel, ServerPlayer player, CastSource source,
                          boolean triggerCooldown) {
        Integer previousLevel = cooldownLevel.get();
        cooldownLevel.set(spellLevel);
        try {
            super.castSpell(level, spellLevel, player, source, triggerCooldown);
        } finally {
            restoreCooldownLevel(previousLevel);
        }
    }

    private void restoreCooldownLevel(Integer previousLevel) {
        if (previousLevel == null) {
            cooldownLevel.remove();
        } else {
            cooldownLevel.set(previousLevel);
        }
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity caster, CastSource source, MagicData magicData) {
        if (!level.isClientSide) {
            if (cooldownLevel.get() != null) {
                cooldownLevel.set(spellLevel);
            }
            PriestGuardEffect.apply(caster, getRadius(), getShieldGain(spellLevel));
        }
        super.onCast(level, spellLevel, caster, source, magicData);
    }
}
