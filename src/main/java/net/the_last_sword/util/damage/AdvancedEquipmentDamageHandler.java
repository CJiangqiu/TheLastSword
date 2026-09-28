package net.the_last_sword.util.damage;

import net.eca.api.EcaAPI;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.compat.CompatCheck;
import net.the_last_sword.compat.curios.CuriosEffectHandler;
import net.the_last_sword.configuration.DefenceConfig;
import net.the_last_sword.configuration.DefenceConfigData;
import net.the_last_sword.configuration.DefenceConfigData.DragonShieldModule;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.damagesource.AbsoluteDestructionDamageSource;
import net.the_last_sword.init.ModItems;
import net.the_last_sword.item.DragonArmorItem;
import net.the_last_sword.network.DefenceConfigPacket;
import net.the_last_sword.network.DragonShieldPacket;
import net.the_last_sword.network.NetworkHandler;
import net.the_last_sword.util.EntityUtil;

import java.util.ArrayList;
import java.util.List;

//高级装备必须共用固定顺序，避免绝毁与普通伤害出现不同结果
@Mod.EventBusSubscriber(modid = TheLastSwordMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AdvancedEquipmentDamageHandler {

    private static final String THE_LAST_SWORD_DEFENCE = "TheLastSwordDefence";
    private static final ThreadLocal<Boolean> APPLYING_PAIN_DAMAGE = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Boolean> CONVERTING_DAMAGE = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<EventBypass> EVENT_BYPASS = new ThreadLocal<>();

    private AdvancedEquipmentDamageHandler() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onLivingHurt(LivingHurtEvent event) {
        EventBypass bypass = EVENT_BYPASS.get();
        if (bypass != null && bypass.matches(event)) {
            EVENT_BYPASS.remove();
            return;
        }
        if (event.isCanceled()) {
            return;
        }
        DamageResult result = process(event.getEntity(), event.getSource(), event.getAmount());
        if (result.canceled()) {
            event.setCanceled(true);
            return;
        }
        event.setAmount(result.amount());
    }

    public static DamageResult processThenPostEvent(LivingEntity target, DamageSource source, float amount) {
        DamageResult result = process(target, source, amount);
        if (result.canceled()) {
            return result;
        }

        float eventDamage;
        EVENT_BYPASS.set(new EventBypass(target, source));
        try {
            eventDamage = ForgeHooks.onLivingHurt(target, source, result.amount());
        } finally {
            EVENT_BYPASS.remove();
        }
        if (!Float.isFinite(eventDamage) || eventDamage <= 0.0F) {
            return DamageResult.canceled(false);
        }
        return DamageResult.continueWith(eventDamage);
    }

    public static DamageResult process(LivingEntity target, DamageSource source, float amount) {
        if (target == null || source == null || !Float.isFinite(amount) || amount <= 0.0F) {
            return DamageResult.canceled(false);
        }

        //转换伤害已经完成前四段处理，只允许最终的虚空减伤继续执行
        if (CONVERTING_DAMAGE.get()) {
            return DamageResult.continueWith(applyWingsReduction(target, source, amount));
        }

        if (exceedsTheLastSwordLimit(target, amount)) {
            return DamageResult.canceled(false);
        }

        float reducedDamage = applyDragonArmorReduction(target, source, amount);
        if (reducedDamage <= 0.0F) {
            return DamageResult.canceled(false);
        }

        applyPainEffect(source, target);

        DamageResult conversion = convertCrownDamage(target, source, reducedDamage);
        if (conversion != null) {
            return conversion;
        }

        return DamageResult.continueWith(applyWingsReduction(target, source, reducedDamage));
    }

    public static boolean isConvertingDamage() {
        return CONVERTING_DAMAGE.get();
    }

    private static boolean exceedsTheLastSwordLimit(LivingEntity target, float amount) {
        if (!target.getPersistentData().getBoolean(THE_LAST_SWORD_DEFENCE)) {
            return false;
        }
        float threshold = target.getMaxHealth()
                * (float) TheLastSwordConfiguration.getDefenceCustomHealthDamageReductionSafely();
        return amount > threshold;
    }

    private static float applyDragonArmorReduction(LivingEntity target, DamageSource source, float amount) {
        if (!(target instanceof Player player) || !DragonArmorItem.isFullSet(player)) {
            return amount;
        }

        boolean nonPlayerAttack = source.getEntity() != null && !(source.getEntity() instanceof Player);
        boolean explosion = source.is(DamageTypes.EXPLOSION)
                || source.is(DamageTypes.PLAYER_EXPLOSION)
                || source.is(DamageTypes.FIREWORKS)
                || source.is(DamageTypes.BAD_RESPAWN_POINT);
        if (!nonPlayerAttack && !explosion) {
            return amount;
        }

        DefenceConfigData playerConfig = DefenceConfigPacket.getPlayerConfig(player.getUUID());
        DragonShieldModule dragonShield = playerConfig.armor.dragonArmor.defence.dragonShield;
        boolean shieldEnabled = dragonShield == null || dragonShield.enabled;
        boolean shieldActive = shieldEnabled && DragonArmorItem.hasEnergyFullSet(player);
        if (!shieldActive) {
            return amount * 0.1F;
        }

        if (DefenceConfig.getDragonShieldModule().shieldEffect != DefenceConfigData.ShieldEffectMode.DISABLED
                && player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.sendToPlayer(DragonShieldPacket.fromDamageSource(serverPlayer, source), serverPlayer);
        }
        return 0.0F;
    }

    private static void applyPainEffect(DamageSource source, LivingEntity target) {
        if (!CompatCheck.isCuriosLoaded()
                || APPLYING_PAIN_DAMAGE.get()
                || !(source.getEntity() instanceof Player attacker)
                || !CuriosEffectHandler.hasCurioEquipped(attacker, ModItems.THE_GIVERS_PAIN.get())
                || attacker.level().isClientSide
                || !EntityUtil.canAttack(attacker, target)) {
            return;
        }

        applyRandomSharedNegativeEffect(attacker, target);
        float attackDamage = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float attackerLostHealth = Math.max(0.0F, attacker.getMaxHealth() - attacker.getHealth());
        float targetLostHealth = Math.max(0.0F, target.getMaxHealth() - target.getHealth());
        float absoluteDamage = (float) (
                attackDamage * TheLastSwordConfiguration.getCuriosGiversPainAttackDamageMultiplierSafely()
                        + attackerLostHealth
                        * TheLastSwordConfiguration.getCuriosGiversPainAttackerLostHealthMultiplierSafely()
                        + targetLostHealth
                        * TheLastSwordConfiguration.getCuriosGiversPainTargetLostHealthMultiplierSafely()
        );
        if (!Float.isFinite(absoluteDamage) || absoluteDamage <= 0.0F) {
            return;
        }

        try {
            APPLYING_PAIN_DAMAGE.set(true);
            EcaAPI.hurt(target, AbsoluteDestructionDamageSource.absoluteDestruction(attacker), absoluteDamage);
        } finally {
            APPLYING_PAIN_DAMAGE.set(false);
        }
    }

    private static void applyRandomSharedNegativeEffect(Player attacker, LivingEntity target) {
        List<MobEffect> availableEffects = new ArrayList<>();
        for (MobEffect effect : ForgeRegistries.MOB_EFFECTS.getValues()) {
            ResourceLocation effectId = ForgeRegistries.MOB_EFFECTS.getKey(effect);
            if (effectId == null
                    || !"minecraft".equals(effectId.getNamespace())
                    || effect.getCategory() != MobEffectCategory.HARMFUL
                    || effect.isInstantenous()
                    || attacker.hasEffect(effect)
                    || target.hasEffect(effect)) {
                continue;
            }
            availableEffects.add(effect);
        }
        if (availableEffects.isEmpty()) {
            return;
        }

        MobEffect selectedEffect = availableEffects.get(attacker.getRandom().nextInt(availableEffects.size()));
        MobEffectInstance instance = new MobEffectInstance(
                selectedEffect,
                TheLastSwordConfiguration.getCuriosGiversPainEffectDurationSafely(),
                TheLastSwordConfiguration.getCuriosGiversPainEffectAmplifierSafely(),
                false,
                true,
                true
        );
        attacker.addEffect(new MobEffectInstance(instance));
        target.addEffect(new MobEffectInstance(instance));
    }

    private static DamageResult convertCrownDamage(LivingEntity target, DamageSource source, float amount) {
        if (!CompatCheck.isCuriosLoaded()
                || !(target instanceof Player player)
                || source.is(DamageTypes.FELL_OUT_OF_WORLD)
                || !TheLastSwordConfiguration.getCuriosDragonCrystalCrownVoidConversionEnabledSafely()
                || !CuriosEffectHandler.hasCurioEquipped(player, ModItems.DRAGON_CRYSTAL_CROWN.get())) {
            return null;
        }

        player.invulnerableTime = 0;
        boolean applied;
        try {
            CONVERTING_DAMAGE.set(true);
            applied = player.hurt(player.damageSources().fellOutOfWorld(), amount);
        } finally {
            CONVERTING_DAMAGE.set(false);
        }
        return DamageResult.canceled(applied);
    }

    private static float applyWingsReduction(LivingEntity target, DamageSource source, float amount) {
        if (!CompatCheck.isCuriosLoaded()
                || !(target instanceof Player player)
                || !source.is(DamageTypes.FELL_OUT_OF_WORLD)
                || !CuriosEffectHandler.hasCurioEquipped(player, ModItems.WINGS_THAT_COVER_THE_WORLD.get())) {
            return amount;
        }
        return amount * (float) TheLastSwordConfiguration.getCuriosWingsVoidDamageReductionSafely();
    }

    public record DamageResult(float amount, boolean canceled, boolean handledResult) {

        public static DamageResult continueWith(float amount) {
            return new DamageResult(amount, false, false);
        }

        public static DamageResult canceled(boolean handledResult) {
            return new DamageResult(0.0F, true, handledResult);
        }
    }

    private record EventBypass(LivingEntity target, DamageSource source) {

        private boolean matches(LivingHurtEvent event) {
            return event.getEntity() == target && event.getSource() == source;
        }
    }
}
