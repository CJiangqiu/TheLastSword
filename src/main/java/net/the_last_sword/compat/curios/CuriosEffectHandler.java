package net.the_last_sword.compat.curios;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.the_last_sword.compat.CompatCheck;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.init.ModEffects;
import net.the_last_sword.init.ModItems;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

//处理饰品的效果
@Mod.EventBusSubscriber(modid = "the_last_sword", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CuriosEffectHandler {

    //龙水晶指环伤害加成（×1.5）
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHurtDragonCrystalRing(LivingHurtEvent event) {
        if (!CompatCheck.isCuriosLoaded()) {
            return;
        }

        if (!(event.getSource().getEntity() instanceof Player attacker)) {
            return;
        }

        handleDragonCrystalRingDamageBonus(event, attacker);
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

    //极限维生装置：生命恢复+抗性提升+饱和（消耗FE，电量不足则停止主动buff）
    private static void handleExtremeLifeSupportTick(Player player) {
        ItemStack device = getEquippedCurioStack(player, ModItems.EXTREME_LIFE_SUPPORT_DEVICE.get());
        if (device.isEmpty()) {
            return;
        }

        //扣电；电量不足则不施加主动buff（盔甲韧性走属性系统不受此影响）
        int energyCost = TheLastSwordConfiguration.getCuriosExtremeLifeSupportEnergyCostSafely();
        boolean powered = device.getCapability(ForgeCapabilities.ENERGY).map(energy -> {
            if (energy.extractEnergy(energyCost, true) < energyCost) {
                return false;
            }
            energy.extractEnergy(energyCost, false);
            return true;
        }).orElse(false);
        if (!powered) {
            return;
        }

        float maxHealth = player.getMaxHealth();
        float lostHealth = maxHealth - player.getHealth();
        float thresholdRatio = (float) TheLastSwordConfiguration.getCuriosExtremeLifeSupportTierThresholdSafely();
        int bonusLevel = (int) (lostHealth / (maxHealth * thresholdRatio));
        int duration = TheLastSwordConfiguration.getCuriosExtremeLifeSupportEffectDurationSafely();

        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration, bonusLevel, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, bonusLevel, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.SATURATION, duration, bonusLevel, false, false, true));
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
        int speedAmplifier = TheLastSwordConfiguration.getCuriosDimensionExplorerSpeedAmplifierSafely();
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

        //速度
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, effectDuration, speedAmplifier, false, true, true));

        //进入冷却
        player.getCooldowns().addCooldown(ModItems.DIMENSION_EXPLORER.get(), cooldown);
    }

    //获取玩家装备的指定Curios饰品ItemStack（未装备返回EMPTY）
    public static ItemStack getEquippedCurioStack(LivingEntity entity, Item item) {
        return CuriosApi.getCuriosInventory(entity).map(handler -> {
            var curios = handler.getCurios();
            for (var entry : curios.entrySet()) {
                IDynamicStackHandler stacks = entry.getValue().getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStackInSlot(i);
                    if (stack.getItem() == item) {
                        return stack;
                    }
                }
            }
            return ItemStack.EMPTY;
        }).orElse(ItemStack.EMPTY);
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
