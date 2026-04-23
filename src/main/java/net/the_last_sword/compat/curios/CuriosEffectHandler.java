package net.the_last_sword.compat.curios;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.the_last_sword.compat.CompatCheck;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.damagesource.AbsoluteDestructionDamageSource;
import net.the_last_sword.init.ModEffects;
import net.the_last_sword.init.ModItems;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.ArrayList;
import java.util.List;

//处理饰品的效果
@Mod.EventBusSubscriber(modid = "the_last_sword", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CuriosEffectHandler {

    //防止虚空伤害转换递归
    private static final ThreadLocal<Boolean> CONVERTING_DAMAGE = ThreadLocal.withInitial(() -> false);
    //防止给予者的痛苦追加绝毁伤害递归
    private static final ThreadLocal<Boolean> APPLYING_GIVERS_PAIN_DAMAGE = ThreadLocal.withInitial(() -> false);

    //龙水晶指环伤害加成（×1.5）
    @SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = true)
    public static void onLivingHurtDragonCrystalRing(LivingHurtEvent event) {
        if (!CompatCheck.isCuriosLoaded()) {
            return;
        }

        if (!(event.getSource().getEntity() instanceof Player attacker)) {
            return;
        }

        handleDragonCrystalRingDamageBonus(event, attacker);
    }

    //给予者的痛苦：随机共享负面效果 + 绝毁伤害（最后附加）
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onLivingHurtGiversPain(LivingHurtEvent event) {
        if (!CompatCheck.isCuriosLoaded()) {
            return;
        }

        if (!(event.getSource().getEntity() instanceof Player attacker)) {
            return;
        }

        LivingEntity target = event.getEntity();
        handleGiversPainAttack(event, attacker, target);
    }

    //处理给予者的痛苦：随机共享负面效果 + 绝对毁灭伤害
    private static void handleGiversPainAttack(LivingHurtEvent event, Player attacker, LivingEntity target) {
        if (!hasCurioEquipped(attacker, ModItems.THE_GIVERS_PAIN.get())) {
            return;
        }
        if (attacker.level().isClientSide) {
            return;
        }

        applyRandomSharedNegativeEffect(attacker, target);

        if (APPLYING_GIVERS_PAIN_DAMAGE.get()) {
            return;
        }

        float attackDamage = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float attackerLostHealth = Math.max(0.0f, attacker.getMaxHealth() - attacker.getHealth());
        float targetLostHealth = Math.max(0.0f, target.getMaxHealth() - target.getHealth());
        float absoluteDamage = (float) (
              attackDamage * TheLastSwordConfiguration.getCuriosGiversPainAttackDamageMultiplierSafely()
            + attackerLostHealth * TheLastSwordConfiguration.getCuriosGiversPainAttackerLostHealthMultiplierSafely()
            + targetLostHealth * TheLastSwordConfiguration.getCuriosGiversPainTargetLostHealthMultiplierSafely()
        );
        if (absoluteDamage <= 0.0f) {
            return;
        }

        try {
            APPLYING_GIVERS_PAIN_DAMAGE.set(true);
            AbsoluteDestructionDamageSource.applyAbsoluteDestruction(target, attacker, absoluteDamage);
        } finally {
            APPLYING_GIVERS_PAIN_DAMAGE.set(false);
        }
    }

    //给予自己和目标同一个随机负面效果（不与已有负面效果重复）
    private static void applyRandomSharedNegativeEffect(Player attacker, LivingEntity target) {
        List<MobEffect> availableEffects = new ArrayList<>();
        for (MobEffect effect : ForgeRegistries.MOB_EFFECTS.getValues()) {
            if (effect == null) {
                continue;
            }
            ResourceLocation effectId = ForgeRegistries.MOB_EFFECTS.getKey(effect);
            if (effectId == null || !"minecraft".equals(effectId.getNamespace())) {
                continue;
            }
            if (effect.getCategory() != MobEffectCategory.HARMFUL || effect.isInstantenous()) {
                continue;
            }
            if (!attacker.hasEffect(effect) && !target.hasEffect(effect)) {
                availableEffects.add(effect);
            }
        }
        if (availableEffects.isEmpty()) {
            return;
        }

        MobEffect selectedEffect =
            availableEffects.get(attacker.getRandom().nextInt(availableEffects.size()));
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

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onCriticalHit(CriticalHitEvent event) {
        if (!CompatCheck.isCuriosLoaded()) {
            return;
        }

        //只处理真正的暴击
        if (!event.isVanillaCritical()) {
            return;
        }

        Player player = event.getEntity();
        if (!hasCurioEquipped(player, ModItems.DRAGON_CRYSTAL_NECKLACE.get())) {
            return;
        }

        //计算暴击加成：base + 幸运值×perLuck
        double luck = player.getAttributeValue(Attributes.LUCK);
        float bonusMultiplier = (float) (
            TheLastSwordConfiguration.getCuriosDragonCrystalNecklaceCritChanceBaseSafely()
          + luck * TheLastSwordConfiguration.getCuriosDragonCrystalNecklaceCritChancePerLuckSafely()
        );

        //在原有暴击倍率基础上增加
        float newModifier = event.getDamageModifier() + bonusMultiplier;
        event.setDamageModifier(newModifier);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurtDragonCrystalCrown(LivingHurtEvent event) {
        if (!CompatCheck.isCuriosLoaded()) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        handleDragonCrystalCrownVoidConversion(event, player);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHurtDragonCrystalNecklace(LivingHurtEvent event) {
        if (!CompatCheck.isCuriosLoaded()) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        handleDragonCrystalNecklaceImmunity(event, player);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurtWingsThatCoverTheWorld(LivingHurtEvent event) {
        if (!CompatCheck.isCuriosLoaded()) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        handleWingsVoidDamageReduction(event, player);
    }

    //处理覆世之翼的虚空伤害减免（-90%）
    private static void handleWingsVoidDamageReduction(LivingHurtEvent event, Player player) {
        if (!hasCurioEquipped(player, ModItems.WINGS_THAT_COVER_THE_WORLD.get())) {
            return;
        }

        //检查是否是虚空伤害（掉出世界）
        if (isOutOfWorldDamage(event.getSource())) {
            float reduction = (float) TheLastSwordConfiguration.getCuriosWingsVoidDamageReductionSafely();
            event.setAmount(event.getAmount() * reduction);
        }
    }

    //检查是否是虚空伤害源
    private static boolean isOutOfWorldDamage(DamageSource source) {
        return source.is(DamageTypes.FELL_OUT_OF_WORLD);
    }

    //处理龙水晶指环的伤害加成（+50%伤害）
    private static void handleDragonCrystalRingDamageBonus(LivingHurtEvent event, Player attacker) {
        if (!hasCurioEquipped(attacker, ModItems.DRAGON_CRYSTAL_RING.get())) {
            return;
        }

        float originalDamage = event.getAmount();
        float multiplier = (float) TheLastSwordConfiguration.getCuriosDragonCrystalRingDamageMultiplierSafely();
        event.setAmount(originalDamage * multiplier);
    }

    //处理龙水晶项链的概率免疫伤害
    private static void handleDragonCrystalNecklaceImmunity(LivingHurtEvent event, Player player) {
        if (!hasCurioEquipped(player, ModItems.DRAGON_CRYSTAL_NECKLACE.get())) {
            return;
        }

        //计算免疫概率：base + 幸运值×perLuck，最高 max
        double luck = player.getAttributeValue(Attributes.LUCK);
        double totalChance = Math.min(
            TheLastSwordConfiguration.getCuriosDragonCrystalNecklaceImmunityChanceMaxSafely(),
            TheLastSwordConfiguration.getCuriosDragonCrystalNecklaceImmunityChanceBaseSafely()
              + luck * TheLastSwordConfiguration.getCuriosDragonCrystalNecklaceImmunityChancePerLuckSafely()
        );

        if (player.getRandom().nextDouble() < totalChance) {
            event.setCanceled(true);
        }
    }

    //处理龙水晶皇冠的虚空伤害转换
    private static void handleDragonCrystalCrownVoidConversion(LivingHurtEvent event, Player player) {
        //防止递归
        if (CONVERTING_DAMAGE.get()) {
            return;
        }

        //配置开关：虚空转换未启用时跳过
        if (!TheLastSwordConfiguration.getCuriosDragonCrystalCrownVoidConversionEnabledSafely()) {
            return;
        }

        if (!hasCurioEquipped(player, ModItems.DRAGON_CRYSTAL_CROWN.get())) {
            return;
        }

        //如果已经是虚空伤害，不再转换
        if (isOutOfWorldDamage(event.getSource())) {
            return;
        }

        //取消原伤害，转换为虚空伤害
        float damage = event.getAmount();
        event.setCanceled(true);

        //清除无敌帧，让虚空伤害能够生效
        player.invulnerableTime = 0;

        try {
            CONVERTING_DAMAGE.set(true);
            player.hurt(player.damageSources().fellOutOfWorld(), damage);
        } finally {
            CONVERTING_DAMAGE.set(false);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!CompatCheck.isCuriosLoaded()) {
            return;
        }

        Player player = event.player;
        if (player.level().isClientSide) {
            return;
        }

        //极限维生装置效果
        handleExtremeLifeSupportTick(player);

        //维度探索者跳跃提升
        handleDimensionExplorerTick(player);
    }

    //极限维生装置：生命恢复+抗性提升+饱和
    private static void handleExtremeLifeSupportTick(Player player) {
        if (!hasCurioEquipped(player, ModItems.EXTREME_LIFE_SUPPORT_DEVICE.get())) {
            return;
        }

        float maxHealth = player.getMaxHealth();
        float lostHealth = maxHealth - player.getHealth();
        float thresholdRatio = (float) TheLastSwordConfiguration.getCuriosExtremeLifeSupportTierThresholdSafely();
        int bonusLevel = (int) (lostHealth / (maxHealth * thresholdRatio));
        int duration = TheLastSwordConfiguration.getCuriosExtremeLifeSupportEffectDurationSafely();

        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration, bonusLevel, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, bonusLevel, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.SATURATION, duration, 0, false, false, true));
    }

    //维度探索者：跳跃提升
    private static void handleDimensionExplorerTick(Player player) {
        if (!hasCurioEquipped(player, ModItems.DIMENSION_EXPLORER.get())) {
            return;
        }

        int jumpAmplifier = TheLastSwordConfiguration.getCuriosDimensionExplorerJumpAmplifierSafely();
        player.addEffect(new MobEffectInstance(MobEffects.JUMP, 60, jumpAmplifier, false, false, true));
    }

    //维度探索者：死亡保护
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!CompatCheck.isCuriosLoaded()) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.level().isClientSide) {
            return;
        }

        if (player.getCooldowns().isOnCooldown(ModItems.DIMENSION_EXPLORER.get())) {
            return;
        }

        if (!hasCurioEquipped(player, ModItems.DIMENSION_EXPLORER.get())) {
            return;
        }

        int effectDuration = TheLastSwordConfiguration.getCuriosDimensionExplorerEffectDurationSafely();
        int cooldown = TheLastSwordConfiguration.getCuriosDimensionExplorerCooldownSafely();
        int hasteAmplifier = TheLastSwordConfiguration.getCuriosDimensionExplorerHasteAmplifierSafely();
        float emergencyHealth = (float) TheLastSwordConfiguration.getCuriosDimensionExplorerEmergencyHealHealthSafely();
        int emergencyFoodLevel = TheLastSwordConfiguration.getCuriosDimensionExplorerEmergencyFoodLevelSafely();

        //阻止死亡
        event.setCanceled(true);
        player.setHealth(emergencyHealth);

        //饱食度设为配置值
        FoodData foodData = player.getFoodData();
        foodData.setFoodLevel(emergencyFoodLevel);

        //虚化
        player.addEffect(new MobEffectInstance(ModEffects.PHASING.get(), effectDuration, 0, false, true, true));

        //急迫
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, effectDuration, hasteAmplifier, false, true, true));

        //进入冷却
        player.getCooldowns().addCooldown(ModItems.DIMENSION_EXPLORER.get(), cooldown);
    }

    //检查玩家是否装备了指定的Curios饰品
    public static boolean hasCurioEquipped(LivingEntity entity, Item item) {
        return CuriosApi.getCuriosInventory(entity).map(handler -> {
            var curios = handler.getCurios();
            for (var entry : curios.entrySet()) {
                IDynamicStackHandler stacks = entry.getValue().getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStackInSlot(i);
                    if (stack.getItem() == item) {
                        return true;
                    }
                }
            }
            return false;
        }).orElse(false);
    }
}
