package net.the_last_sword.compat.cataclysm;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.the_last_sword.compat.CompatCheck;
import net.the_last_sword.init.ModItems;
import top.theillusivec4.curios.api.CuriosApi;

//处理灾变奖牌的特殊效果
@Mod.EventBusSubscriber(modid = "the_last_sword", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CataclysmEventHandler {

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onLivingHurtHarbinger(LivingHurtEvent event) {
        if (!CompatCheck.isCataclysmLoaded()) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        handleHarbingerMedalProjectileImmunity(event, player);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingHurtAncientRemnant(LivingHurtEvent event) {
        if (!CompatCheck.isCataclysmLoaded()) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        handleAncientRemnantMedalCounterAttack(event, player);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCriticalHit(CriticalHitEvent event) {
        if (!CompatCheck.isCataclysmLoaded()) {
            return;
        }

        if (event.getDamageModifier() <= 1.0f) {
            return;
        }

        handleEnderGuardianMedalVoidPunch(event.getEntity(), event.getTarget());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!CompatCheck.isCataclysmLoaded()) {
            return;
        }

        if (event.getSource().getEntity() instanceof Player attacker) {
            LivingEntity targetEntity = event.getEntity();
            handleIgnisMedalBattleWill(attacker, targetEntity);
            handleNetheriteMonstrosityMedalPowerCell(attacker, targetEntity);
            handleLeviathanMedalAbyssalRoar(attacker, targetEntity);
            handleMaledictusMedalCurse(attacker, targetEntity);
            handleScyllaMedalStorm(attacker, targetEntity);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!CompatCheck.isCataclysmLoaded()) {
            return;
        }

        if (event.getSource().getEntity() instanceof Player attacker) {
            handleIgnisMedalDamageBonus(event, attacker);
            handleMaledictusMedalDamageBonus(event, attacker);
        }
    }

    //处理先驱者奖章的弹射物免疫效果
    private static void handleHarbingerMedalProjectileImmunity(LivingHurtEvent event, Player player) {
        float currentHealth = player.getHealth();
        float maxHealth = player.getMaxHealth();
        if (currentHealth > (maxHealth * 0.5f)) {
            return;
        }

        if (!hasMedalInAnySlot(player, CataclysmMedals.TheHarbingerMedal.class)) {
            return;
        }

        DamageSource damageSource = event.getSource();
        if (damageSource.getDirectEntity() instanceof Projectile) {
            event.setCanceled(true);
        }
    }

    //处理末影守卫奖章的虚空重拳效果
    private static void handleEnderGuardianMedalVoidPunch(Player attacker, net.minecraft.world.entity.Entity target) {
        if (!hasMedalInAnySlot(attacker, CataclysmMedals.EnderGuardianMedal.class) ||
                !(target instanceof LivingEntity livingTarget)) {
            return;
        }

        ItemStack mainHandShield = livingTarget.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offHandShield = livingTarget.getItemInHand(InteractionHand.OFF_HAND);

        boolean hasShieldWithoutCooldown = false;
        ItemStack shieldToBreak = null;

        if (mainHandShield.getItem() instanceof ShieldItem && !hasShieldCooldown(livingTarget, mainHandShield)) {
            hasShieldWithoutCooldown = true;
            shieldToBreak = mainHandShield;
        } else if (offHandShield.getItem() instanceof ShieldItem && !hasShieldCooldown(livingTarget, offHandShield)) {
            hasShieldWithoutCooldown = true;
            shieldToBreak = offHandShield;
        }

        if (hasShieldWithoutCooldown && shieldToBreak != null) {
            applyShieldCooldown(shieldToBreak, livingTarget, 60);
        } else {
            if (!isAlreadyStunned(livingTarget)) {
                applyCataclysmStunEffect(livingTarget, 60);
            }
        }
    }

    //处理焰魔奖章的战意效果
    private static void handleIgnisMedalBattleWill(Player attacker, LivingEntity target) {
        if (!hasMedalInAnySlot(attacker, CataclysmMedals.IgnisMedal.class)) {
            return;
        }

        if (!target.isOnFire()) {
            return;
        }

        MobEffect blazingBrandEffect = getBlazingBrandEffect();
        if (blazingBrandEffect == null) {
            return;
        }

        int currentLevel = 0;
        if (target.hasEffect(blazingBrandEffect)) {
            currentLevel = target.getEffect(blazingBrandEffect).getAmplifier();
        }

        int newLevel = Math.min(currentLevel + 1, 4);
        target.addEffect(new MobEffectInstance(blazingBrandEffect, 60, newLevel));

        float healAmount = 2.0f * (newLevel + 1);
        attacker.heal(healAmount);
    }

    //获取炽热烙印效果
    private static MobEffect getBlazingBrandEffect() {
        try {
            return ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("cataclysm", "blazing_brand"));
        } catch (Exception ignored) {
            return null;
        }
    }

    //检查目标是否已经处于眩晕状态
    private static boolean isAlreadyStunned(LivingEntity target) {
        try {
            MobEffect stunEffect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("cataclysm", "stun"));
            return stunEffect != null && target.hasEffect(stunEffect);
        } catch (Exception ignored) {
            return false;
        }
    }

    //检查盾牌是否在冷却中
    private static boolean hasShieldCooldown(LivingEntity entity, ItemStack shield) {
        if (entity instanceof Player player) {
            return player.getCooldowns().isOnCooldown(shield.getItem());
        } else {
            CompoundTag entityData = entity.getPersistentData();
            if (entityData.contains("shield_cooldown_end")) {
                long cooldownEnd = entityData.getLong("shield_cooldown_end");
                return entity.level().getGameTime() < cooldownEnd;
            }
        }
        return false;
    }

    //为盾牌添加冷却时间
    private static void applyShieldCooldown(ItemStack shield, LivingEntity entity, int cooldownTicks) {
        if (entity instanceof Player player) {
            player.getCooldowns().addCooldown(shield.getItem(), cooldownTicks);
        }
        CompoundTag entityData = entity.getPersistentData();
        entityData.putLong("shield_cooldown_end", entity.level().getGameTime() + cooldownTicks);
    }

    //应用灾变的眩晕效果
    private static void applyCataclysmStunEffect(LivingEntity target, int durationTicks) {
        try {
            MobEffect stunEffect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("cataclysm", "stun"));
            if (stunEffect != null) {
                target.addEffect(new MobEffectInstance(stunEffect, durationTicks, 0));
            }
        } catch (Exception ignored) {
        }
    }

    //处理下界合金巨兽奖章的动力电池效果
    private static void handleNetheriteMonstrosityMedalPowerCell(Player attacker, LivingEntity target) {
        if (!hasMedalInAnySlot(attacker, CataclysmMedals.NetheriteMonstrosityMedal.class)) {
            return;
        }

        if (!target.isOnFire()) {
            target.setSecondsOnFire(3);
        }

        if (!target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) ||
                target.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getAmplifier() < 2) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
        }
    }

    //处理利维坦奖章的深渊咆哮效果
    private static void handleLeviathanMedalAbyssalRoar(Player attacker, LivingEntity target) {
        if (!hasMedalInAnySlot(attacker, CataclysmMedals.TheLeviathanMedal.class)) {
            return;
        }

        MobEffect abyssalBurnEffect = getAbyssalBurnEffect();
        if (abyssalBurnEffect == null) {
            return;
        }

        boolean isUnderwater = attacker.isUnderWater();
        int effectLevel = isUnderwater ? 4 : 2;
        int duration = isUnderwater ? 200 : 100;

        target.addEffect(new MobEffectInstance(abyssalBurnEffect, duration, effectLevel));
    }

    //获取深渊烧灼效果
    private static MobEffect getAbyssalBurnEffect() {
        try {
            return ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("cataclysm", "abyssal_burn"));
        } catch (Exception ignored) {
            return null;
        }
    }

    //处理远古遗魂奖章的反击效果
    private static void handleAncientRemnantMedalCounterAttack(LivingHurtEvent event, Player player) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) {
            return;
        }

        //检查背包中的奖章
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof CataclysmMedals.AncientRemnantMedal medal) {
                medal.onOwnerAttacked(player, stack, attacker);
            }
        }

        //检查 Curios 槽位中的奖章
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            handler.findCurios(stack -> stack.getItem() instanceof CataclysmMedals.AncientRemnantMedal)
                    .forEach(result -> {
                        CataclysmMedals.AncientRemnantMedal medal = (CataclysmMedals.AncientRemnantMedal) result.stack().getItem();
                        medal.onOwnerAttacked(player, result.stack(), attacker);
                    });
        });
    }

    //处理咒翼灵骸奖章的诅咒效果
    private static void handleMaledictusMedalCurse(Player attacker, LivingEntity target) {
        if (!hasMedalInAnySlot(attacker, CataclysmMedals.MaledictusMedal.class)) {
            return;
        }

        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 2));
    }

    //处理咒翼灵骸奖章的伤害加成
    private static void handleMaledictusMedalDamageBonus(LivingDamageEvent event, Player attacker) {
        if (!hasMedalInAnySlot(attacker, CataclysmMedals.MaledictusMedal.class)) {
            return;
        }

        int debuffCount = 0;
        for (MobEffectInstance effect : attacker.getActiveEffects()) {
            if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                debuffCount++;
            }
        }

        if (debuffCount > 0) {
            float damageMultiplier = 1.0f + (debuffCount * 0.1f);
            event.setAmount(event.getAmount() * damageMultiplier);
        }
    }

    //处理焰魔奖章的伤害加成效果
    private static void handleIgnisMedalDamageBonus(LivingDamageEvent event, Player attacker) {
        if (!hasMedalInAnySlot(attacker, CataclysmMedals.IgnisMedal.class)) {
            return;
        }

        float damageMultiplier = 1.5f;
        event.setAmount(event.getAmount() * damageMultiplier);
    }

    //处理斯库拉奖章的风暴效果
    private static void handleScyllaMedalStorm(Player attacker, LivingEntity target) {
        //检查背包中的奖章
        for (ItemStack stack : attacker.getInventory().items) {
            if (stack.getItem() instanceof CataclysmMedals.ScyllaMedal medal) {
                medal.onOwnerAttack(attacker, stack, target);
            }
        }

        //检查 Curios 槽位中的奖章
        CuriosApi.getCuriosInventory(attacker).ifPresent(handler -> {
            handler.findCurios(stack -> stack.getItem() instanceof CataclysmMedals.ScyllaMedal)
                    .forEach(result -> {
                        CataclysmMedals.ScyllaMedal medal = (CataclysmMedals.ScyllaMedal) result.stack().getItem();
                        medal.onOwnerAttack(attacker, result.stack(), target);
                    });
        });
    }

    //检查玩家背包和饰品槽中是否有指定类型的奖章
    private static boolean hasMedalInAnySlot(Player player, Class<? extends CataclysmMedal> medalClass) {
        //检查背包
        boolean hasInInventory = player.getInventory().items.stream()
                .anyMatch(stack -> medalClass.isInstance(stack.getItem()));
        if (hasInInventory) {
            return true;
        }

        //检查 Curios 槽位
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> !handler.findCurios(stack -> medalClass.isInstance(stack.getItem())).isEmpty())
                .orElse(false);
    }

    // ============ Boss掉落处理 ============

    //处理灾变Boss死亡时掉落对应奖章
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!CompatCheck.isCataclysmLoaded()) {
            return;
        }

        String entityClassName = event.getEntity().getClass().getSimpleName();
        ItemStack medalToDrop = getMedalForBoss(entityClassName);

        if (medalToDrop != null && !medalToDrop.isEmpty()) {
            ItemEntity medalDrop = new ItemEntity(
                event.getEntity().level(),
                event.getEntity().getX(),
                event.getEntity().getY(),
                event.getEntity().getZ(),
                medalToDrop
            );

            //发光效果
            medalDrop.setGlowingTag(true);
            //延长生命周期
            medalDrop.setExtendedLifetime();
            //向上飞起
            medalDrop.setDeltaMovement(medalDrop.getDeltaMovement().multiply(0.0, 1.5, 0.0));

            event.getEntity().level().addFreshEntity(medalDrop);
        }
    }

    //根据Boss类型返回对应的奖牌物品
    private static ItemStack getMedalForBoss(String bossClassName) {
        return switch (bossClassName) {
            case "Ancient_Remnant_Entity" ->
                ModItems.ANCIENT_REMNANT_MEDAL != null ? new ItemStack(ModItems.ANCIENT_REMNANT_MEDAL.get()) : null;
            case "Ender_Guardian_Entity" ->
                ModItems.ENDER_GUARDIAN_MEDAL != null ? new ItemStack(ModItems.ENDER_GUARDIAN_MEDAL.get()) : null;
            case "Ignis_Entity" ->
                ModItems.IGNIS_MEDAL != null ? new ItemStack(ModItems.IGNIS_MEDAL.get()) : null;
            case "Maledictus_Entity" ->
                ModItems.MALEDICTUS_MEDAL != null ? new ItemStack(ModItems.MALEDICTUS_MEDAL.get()) : null;
            case "Netherite_Monstrosity_Entity" ->
                ModItems.NETHERITE_MONSTROSITY_MEDAL != null ? new ItemStack(ModItems.NETHERITE_MONSTROSITY_MEDAL.get()) : null;
            case "The_Harbinger_Entity" ->
                ModItems.THE_HARBINGER_MEDAL != null ? new ItemStack(ModItems.THE_HARBINGER_MEDAL.get()) : null;
            case "The_Leviathan_Entity" ->
                ModItems.THE_LEVIATHAN_MEDAL != null ? new ItemStack(ModItems.THE_LEVIATHAN_MEDAL.get()) : null;
            case "Scylla_Entity" ->
                ModItems.SCYLLA_MEDAL != null ? new ItemStack(ModItems.SCYLLA_MEDAL.get()) : null;
            default -> null;
        };
    }
}
