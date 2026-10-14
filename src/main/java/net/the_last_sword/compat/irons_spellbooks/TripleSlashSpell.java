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
import net.the_last_sword.configuration.QueenBlinkSettings;
import net.the_last_sword.entity.QueenEnhancedBlade;
import net.the_last_sword.network.NetworkHandler;
import net.the_last_sword.network.QueenTripleSlashShakePacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class TripleSlashSpell extends AbstractSpell {
    private static final ResourceLocation SPELL_ID =
        ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID, "triple_slash");
    private final DefaultConfig defaultConfig = new DefaultConfig()
        .setMinRarity(SpellRarity.COMMON)
        .setSchoolResource(ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID, "the_last_end"))
        .setMaxLevel(10)
        .setCooldownSeconds(30)
        .build();
    // 冷却接口没有等级参数，限定调用范围以隔离不同玩家的等级。
    private final ThreadLocal<Integer> cooldownLevel = new ThreadLocal<>();

    public TripleSlashSpell() {
        baseSpellPower = 20;
        spellPowerPerLevel = 20;
        baseManaCost = 100;
        manaCostPerLevel = 60;
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
            Component.translatable("ui.the_last_sword.triple_slash.damage", getSpellPower(spellLevel, caster)),
            Component.translatable("ui.the_last_sword.triple_slash.sequence"),
            Component.translatable("ui.the_last_sword.triple_slash.range"));
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity caster, MagicData magicData) {
        super.onServerCastTick(level, spellLevel, caster, magicData);
        if (level instanceof ServerLevel serverLevel && magicData != null) {
            float progress = magicData.getCastCompletionPercent();
            if (progress >= 0.25F && progress <= 0.45F) {
                Vec3 point = caster.position().add(caster.getLookAngle().scale(2))
                    .add(0, 2 + (progress - 0.25) / 0.2 * 3, 0);
                serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH, point.x, point.y, point.z,
                    24, 0.3, 0.15, 0.3, 0.015);
            }
        }
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
        if (level instanceof ServerLevel serverLevel && validCaster(serverLevel, caster)) {
            if (cooldownLevel.get() != null) {
                cooldownLevel.set(spellLevel);
            }
            QueenBlinkSettings settings = TheLastSwordConfiguration.getQueenBlinkSettings();
            // 三段共用施法完成时的伤害快照，避免中途换装改变后续刀光。
            QueenBlinkSettings bladeSettings = new QueenBlinkSettings(settings.cooldown(),
                getSpellPower(spellLevel, caster), settings.speed(), 1.0, 8.0, 3.0);
            releaseBlade(serverLevel, caster, bladeSettings, 45F);
            long startTime = level.getGameTime();
            queueBlade(serverLevel, caster, bladeSettings, -45F, startTime + 20);
            queueBlade(serverLevel, caster, bladeSettings, 0F, startTime + 35);
        }
        super.onCast(level, spellLevel, caster, source, magicData);
    }

    private static boolean validCaster(ServerLevel level, LivingEntity caster) {
        return caster.isAlive() && !caster.isRemoved() && caster.level() == level
            && (!(caster instanceof ServerPlayer player)
                || level.getServer().getPlayerList().getPlayer(player.getUUID()) == player);
    }

    private static void queueBlade(ServerLevel level, LivingEntity caster, QueenBlinkSettings settings,
                                   float roll, long deadline) {
        TheLastSwordMod.queueServerWork(1, () -> {
            if (!validCaster(level, caster)) {
                return;
            }
            // 使用世界时间保持出刀间隔，避免刻末任务队列造成一刻偏移。
            if (level.getGameTime() < deadline) {
                queueBlade(level, caster, settings, roll, deadline);
                return;
            }
            releaseBlade(level, caster, settings, roll);
        });
    }

    private static void releaseBlade(ServerLevel level, LivingEntity caster, QueenBlinkSettings settings, float roll) {
        QueenEnhancedBlade blade = new QueenEnhancedBlade(level, caster, settings);
        blade.aimAlongView(caster);
        blade.setBladeRoll(roll);
        if (!level.addFreshEntity(blade)) {
            return;
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.ENDER_DRAGON_SHOOT,
            caster.getSoundSource(), 1F, 1F);
        if (TheLastSwordConfiguration.getQueenTripleSlashScreenShakeEnabledSafely()) {
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(caster) <= 32.0 * 32.0) {
                    NetworkHandler.sendToPlayer(new QueenTripleSlashShakePacket(), player);
                }
            }
        }
    }
}
