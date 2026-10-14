package net.the_last_sword.compat.irons_spellbooks;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.events.SpellCooldownAddedEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.util.EntityUtil;

import java.util.List;

public class DoubleStrikeSpell extends AbstractSpell {
    private static final ResourceLocation SPELL_ID =
        ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID, "double_strike");
    private final DefaultConfig defaultConfig = new DefaultConfig()
        .setMinRarity(SpellRarity.COMMON)
        .setSchoolResource(ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID, "the_last_end"))
        .setMaxLevel(10)
        .setCooldownSeconds(5)
        .build();
    // 冷却接口没有等级参数；限定在本次调用内，避免共享法术实例串用玩家等级。
    private final ThreadLocal<Integer> cooldownLevel = new ThreadLocal<>();

    public DoubleStrikeSpell() {
        baseSpellPower = 20;
        spellPowerPerLevel = 10;
        baseManaCost = 50;
        manaCostPerLevel = 20;
        castTime = 10;
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
        float damage = getSpellPower(spellLevel, caster);
        return List.of(Component.translatable("ui.the_last_sword.double_strike.physical", damage),
            Component.translatable("ui.the_last_sword.double_strike.magic", damage));
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

    public static void onCooldownAdded(SpellCooldownAddedEvent.Pre event) {
        // 零时长条目仍会暂时阻止施法，因此不将它加入冷却表。
        if (event.getSpell() instanceof DoubleStrikeSpell && event.getEffectiveCooldown() <= 0) {
            event.setCanceled(true);
        }
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity caster, CastSource source, MagicData magicData) {
        if (!level.isClientSide) {
            if (cooldownLevel.get() != null) {
                // 采用施法事件最终确定的等级，让伤害与冷却一致。
                cooldownLevel.set(spellLevel);
            }
            float damage = getSpellPower(spellLevel, caster);
            double range = TheLastSwordConfiguration.getSkillDoubleStrikeRangeSafely();
            strike(caster, damage, range, true);
            long secondStrikeTime = level.getGameTime() + 10;
            TheLastSwordMod.queueServerWork(10,
                () -> finishSecondStrike(level, caster, damage, range, secondStrikeTime));
        }
        super.onCast(level, spellLevel, caster, source, magicData);
    }

    private static void finishSecondStrike(Level level, LivingEntity caster, float damage, double range,
                                           long secondStrikeTime) {
        if (!caster.isAlive() || caster.isRemoved() || caster.level() != level
            || (caster instanceof ServerPlayer player
                && player.getServer().getPlayerList().getPlayer(player.getUUID()) != player)) {
            return;
        }
        // 队列在刻末执行，需以世界时间保证完整间隔，避免第二击落入受伤无敌期。
        if (level.getGameTime() < secondStrikeTime) {
            TheLastSwordMod.queueServerWork(1,
                () -> finishSecondStrike(level, caster, damage, range, secondStrikeTime));
            return;
        }
        strike(caster, damage, range, false);
    }

    private static void strike(LivingEntity caster, float damage, double range, boolean physical) {
        Vec3 origin = caster.getEyePosition();
        Vec3 forward = caster.getLookAngle().normalize();
        caster.level().playSound(null, caster.getX(), caster.getY(), caster.getZ(),
            SoundEvents.PLAYER_ATTACK_SWEEP, caster.getSoundSource(), 1.0F, 1.0F);
        spawnSlashParticles(caster, origin, forward, range, physical);
        DamageSource damageSource;
        if (physical) {
            damageSource = caster instanceof Player player
                ? caster.damageSources().playerAttack(player) : caster.damageSources().mobAttack(caster);
        } else {
            damageSource = new DamageSource(caster.level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC), caster, caster);
        }
        AABB searchBox = new AABB(origin, origin).inflate(range);
        for (LivingEntity target : caster.level().getEntitiesOfClass(LivingEntity.class, searchBox,
            target -> target != caster && target.isAlive() && EntityUtil.canAttack(caster, target))) {
            AABB box = target.getBoundingBox();
            Vec3 offset = box.getCenter().subtract(origin);
            double projectedExtent = (Math.abs(forward.x) * box.getXsize()
                + Math.abs(forward.y) * box.getYsize() + Math.abs(forward.z) * box.getZsize()) * 0.5;
            // 使用碰撞箱与视线前方半球相交判定，避免大型目标因中心越界而漏判。
            if (box.distanceToSqr(origin) <= range * range && offset.dot(forward) + projectedExtent > 0) {
                target.hurt(damageSource, damage);
            }
        }
    }

    private static void spawnSlashParticles(LivingEntity caster, Vec3 origin, Vec3 forward,
                                            double range, boolean physical) {
        if (!(caster.level() instanceof ServerLevel level)) {
            return;
        }
        // 用水平角定义屏幕右方，视线垂直向上或向下时也能保持稳定的斩击平面。
        double yaw = Math.toRadians(caster.getYRot());
        Vec3 right = new Vec3(-Math.cos(yaw), 0, -Math.sin(yaw));
        Vec3 up = right.cross(forward).normalize();
        Vec3 diagonal = (physical ? right.add(up) : up.subtract(right)).normalize();
        Vec3 center = origin.add(forward.scale(range * 0.65));
        for (int i = 0; i <= 12; i++) {
            Vec3 position = center.add(diagonal.scale(range * 0.65 * (1.0 - i / 6.0)));
            level.sendParticles(ParticleTypes.SWEEP_ATTACK,
                position.x, position.y, position.z, 1, 0, 0, 0, 0);
            level.sendParticles(physical ? ParticleTypes.CRIT : ParticleTypes.ENCHANTED_HIT,
                position.x, position.y, position.z, physical ? 10 : 12, 0.1, 0.1, 0.1, 0.05);
        }
    }
}
