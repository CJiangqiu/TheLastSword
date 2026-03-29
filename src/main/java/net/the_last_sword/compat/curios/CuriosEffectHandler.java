package net.the_last_sword.compat.curios;

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
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.the_last_sword.compat.CompatCheck;
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
    private static final int GIVERS_PAIN_EFFECT_DURATION = 120;
    private static final int GIVERS_PAIN_EFFECT_AMPLIFIER = 0;

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDamageDragonCrystalRing(LivingDamageEvent event) {
        if (!CompatCheck.isCuriosLoaded()) {
            return;
        }

        if (!(event.getSource().getEntity() instanceof Player attacker)) {
            return;
        }

        handleDragonCrystalRingDamageBonus(event, attacker);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamageGiversPain(LivingDamageEvent event) {
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
    private static void handleGiversPainAttack(LivingDamageEvent event, Player attacker, LivingEntity target) {
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
        float absoluteDamage = attackDamage + attackerLostHealth + targetLostHealth;
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
            GIVERS_PAIN_EFFECT_DURATION,
            GIVERS_PAIN_EFFECT_AMPLIFIER,
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

        //计算暴击加成：20% + 幸运值×20%
        double luck = player.getAttributeValue(Attributes.LUCK);
        float bonusMultiplier = (float) (0.2 + luck * 0.2);

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
            event.setAmount(event.getAmount() * 0.1f);
        }
    }

    //检查是否是虚空伤害源
    private static boolean isOutOfWorldDamage(DamageSource source) {
        return source.is(DamageTypes.FELL_OUT_OF_WORLD);
    }

    //处理龙水晶指环的伤害加成（+50%伤害）
    private static void handleDragonCrystalRingDamageBonus(LivingDamageEvent event, Player attacker) {
        if (!hasCurioEquipped(attacker, ModItems.DRAGON_CRYSTAL_RING.get())) {
            return;
        }

        float originalDamage = event.getAmount();
        event.setAmount(originalDamage * 1.5f);
    }

    //处理龙水晶项链的概率免疫伤害
    private static void handleDragonCrystalNecklaceImmunity(LivingHurtEvent event, Player player) {
        if (!hasCurioEquipped(player, ModItems.DRAGON_CRYSTAL_NECKLACE.get())) {
            return;
        }

        //计算免疫概率：20% + 幸运值×20%，最高90%
        double luck = player.getAttributeValue(Attributes.LUCK);
        double totalChance = Math.min(0.9, 0.2 + luck * 0.2);

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

    private static final int DIMENSION_EXPLORER_COOLDOWN = 600;  //30秒
    private static final int DIMENSION_EXPLORER_EFFECT_DURATION = 260;  //13秒

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
        int bonusLevel = (int) (lostHealth / (maxHealth * 0.2f));

        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, bonusLevel, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, bonusLevel, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 60, 0, false, false, true));
    }

    //维度探索者：跳跃提升II
    private static void handleDimensionExplorerTick(Player player) {
        if (!hasCurioEquipped(player, ModItems.DIMENSION_EXPLORER.get())) {
            return;
        }

        player.addEffect(new MobEffectInstance(MobEffects.JUMP, 60, 1, false, false, true));
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

        //阻止死亡
        event.setCanceled(true);
        player.setHealth(1.0F);

        //饱食度设为1
        FoodData foodData = player.getFoodData();
        foodData.setFoodLevel(1);

        //虚化13秒
        player.addEffect(new MobEffectInstance(ModEffects.PHASING.get(), DIMENSION_EXPLORER_EFFECT_DURATION, 0, false, true, true));

        //急迫III 13秒
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, DIMENSION_EXPLORER_EFFECT_DURATION, 2, false, true, true));

        //进入冷却30秒
        player.getCooldowns().addCooldown(ModItems.DIMENSION_EXPLORER.get(), DIMENSION_EXPLORER_COOLDOWN);
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
