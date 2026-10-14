package net.the_last_sword.compat.irons_spellbooks;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.entity.DragonLightingEntity;
import net.the_last_sword.init.ModEntities;
import net.the_last_sword.util.EntityUtil;

import java.util.List;

public class JudgmentThunderSpell extends AbstractSpell {
    private static final ResourceLocation SPELL_ID =
        ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID, "judgment_thunder");
    private static final int TARGET_RANGE = 48;
    private static final double AREA_RADIUS = 3.0;
    private final DefaultConfig defaultConfig = new DefaultConfig()
        .setMinRarity(SpellRarity.COMMON)
        .setSchoolResource(ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID, "the_last_end"))
        .setMaxLevel(10)
        .setCooldownSeconds(20)
        .build();
    // 冷却接口没有等级参数，限定调用范围以隔离不同玩家的等级。
    private final ThreadLocal<Integer> cooldownLevel = new ThreadLocal<>();

    public JudgmentThunderSpell() {
        baseSpellPower = 20;
        spellPowerPerLevel = 20;
        baseManaCost = 60;
        manaCostPerLevel = 20;
        castTime = 50;
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
    public int getCastTime(int spellLevel) {
        return Math.max(1, super.getCastTime(spellLevel) - Math.max(0, spellLevel - 1) * 2);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
            Component.translatable("ui.the_last_sword.judgment_thunder.damage", getSpellPower(spellLevel, caster)),
            Component.translatable("ui.the_last_sword.judgment_thunder.range", TARGET_RANGE),
            Component.translatable("ui.the_last_sword.judgment_thunder.area"));
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity caster, MagicData magicData) {
        if (!(level instanceof ServerLevel serverLevel)
            || !Utils.preCastTargetHelper(level, caster, magicData, this, TARGET_RANGE, 0.25F, true,
                target -> target.isAlive() && EntityUtil.canAttack(caster, target))) {
            return false;
        }
        if (magicData.getAdditionalCastData() instanceof TargetEntityCastData targetData) {
            LivingEntity target = targetData.getTarget(serverLevel);
            if (target != null && target.isAlive() && EntityUtil.canAttack(caster, target)) {
                // 保存坐标而非目标引用，读条期间移动或死亡不会改变落点。
                magicData.setAdditionalCastData(new StrikePosition(target.position(), level.dimension()));
                return true;
            }
        }
        magicData.resetAdditionalCastData();
        return false;
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
        if (level instanceof ServerLevel serverLevel) {
            if (cooldownLevel.get() != null) {
                cooldownLevel.set(spellLevel);
            }
            if (magicData.getAdditionalCastData() instanceof StrikePosition strike
                && strike.dimension().equals(level.dimension())) {
                Vec3 position = strike.position();
                DragonLightingEntity lightning = new DragonLightingEntity(ModEntities.DRAGON_LIGHTING.get(), serverLevel);
                lightning.moveTo(position.x, position.y, position.z);
                serverLevel.addFreshEntity(lightning);

                AABB area = new AABB(position.subtract(AREA_RADIUS, AREA_RADIUS, AREA_RADIUS),
                    position.add(AREA_RADIUS, AREA_RADIUS, AREA_RADIUS));
                DamageSource damageSource = new DamageSource(
                    level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(DamageTypes.DRAGON_BREATH), lightning, caster);
                float damage = getSpellPower(spellLevel, caster);
                for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area,
                    entity -> entity.isAlive() && EntityUtil.canAttack(caster, entity))) {
                    // 沿用弹射物龙息段的结算方式，避免先前受击的无敌帧吞掉本次伤害。
                    target.invulnerableTime = 0;
                    target.hurt(damageSource, damage);
                }
            }
        }
        super.onCast(level, spellLevel, caster, source, magicData);
    }

    private record StrikePosition(Vec3 position, ResourceKey<Level> dimension) implements ICastData {
        @Override
        public void reset() {
        }
    }
}

