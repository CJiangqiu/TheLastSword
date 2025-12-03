package net.the_last_sword.event;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.init.ModAttributes;
import net.the_last_sword.item.DragonArmorItem;
import net.the_last_sword.item.DragonCrystalArmorItem;
import net.the_last_sword.util.EntityUtil;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.the_last_sword.init.ModEffects;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

//防御系统事件处理器
public final class DefenceEventHandler {
    private DefenceEventHandler() {}

    //水晶守护计时器
    private static final ConcurrentHashMap<UUID, Boolean> CRYSTAL_GUARD_TIMERS = new ConcurrentHashMap<>();

    @Mod.EventBusSubscriber(modid = TheLastSwordMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        //为所有生物实体附加肃正防御属性
        @SubscribeEvent
        public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
            event.getTypes().forEach(entityType -> {
                event.add(entityType, ModAttributes.JUSTIFIED_DEFENCE.get(), 0.0);
                event.add(entityType, ModAttributes.MAX_JUSTIFIED_DEFENCE.get(), 0.0);
            });
        }
    }

    @Mod.EventBusSubscriber(modid = TheLastSwordMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeBusEvents {
        //护盾伤害保护
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onLivingHurt(LivingHurtEvent event) {
            LivingEntity entity = event.getEntity();
            if (entity.level().isClientSide()) return;

            double currentShield = DefenceEventHandler.getShieldValue(entity);
            if (currentShield > 0) {
                DefenceEventHandler.triggerShieldProtection(entity, event);
            }
        }

        //护盾死亡保护
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onLivingDeath(LivingDeathEvent event) {
            LivingEntity entity = event.getEntity();
            if (entity.level().isClientSide()) return;

            double currentShield = DefenceEventHandler.getShieldValue(entity);
            if (currentShield > 0) {
                DefenceEventHandler.triggerShieldProtection(entity, event);
            }
        }

        //玩家Tick事件 - 盔甲效果
        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;

            Player player = event.player;

            //龙水晶盔甲效果
            applyDragonCrystalArmorEffects(player);
            if (DragonCrystalArmorItem.isFullSet(player)) {
                applyCrystalGuard(player);
            }

            //龙之盔甲效果
            applyDragonArmorEffects(player);
            handleDragonArmorFlying(player);
            if (DragonArmorItem.isFullSet(player)) {
                handleDragonArmorFullSetEffects(player);
            }
        }

        //龙之盔甲伤害减免
        @SubscribeEvent(priority = EventPriority.HIGH)
        public static void onDragonArmorHurt(LivingHurtEvent event) {
            if (!(event.getEntity() instanceof Player player)) return;
            if (!DragonArmorItem.isFullSet(player)) return;

            DamageSource source = event.getSource();

            //检查是否为非玩家攻击或爆炸伤害
            boolean isNonPlayerAttack = source.getEntity() != null && !(source.getEntity() instanceof Player);
            boolean isExplosion = source.is(DamageTypes.EXPLOSION) ||
                                  source.is(DamageTypes.PLAYER_EXPLOSION) ||
                                  source.is(DamageTypes.FIREWORKS) ||
                                  source.is(DamageTypes.BAD_RESPAWN_POINT);

            if (isNonPlayerAttack || isExplosion) {
                //减少90%伤害
                event.setAmount(event.getAmount() * 0.1f);
            }
        }

        //装备更换事件 - 血量同步
        @SubscribeEvent
        public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
            if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) return;

            boolean fromDragonCrystal = !event.getFrom().isEmpty() &&
                                       event.getFrom().getItem() instanceof DragonCrystalArmorItem;
            boolean toDragonCrystal = !event.getTo().isEmpty() &&
                                     event.getTo().getItem() instanceof DragonCrystalArmorItem;

            if (!fromDragonCrystal && !toDragonCrystal) return;

            //直接同步血量
            syncHealthToMaxHealth(player);
        }
    }

    //自动回复护盾值
    public static void autoRegenerate(LivingEntity entity) {
        AttributeInstance maxA = entity.getAttribute(ModAttributes.MAX_JUSTIFIED_DEFENCE.get());
        AttributeInstance curA = entity.getAttribute(ModAttributes.JUSTIFIED_DEFENCE.get());
        if (maxA == null || curA == null) return;

        double max = maxA.getValue();
        double cur = curA.getValue();
        int interval = TheLastSwordConfiguration.getJustifiedDefenceRecoveryTickSafely();

        //TODO: 临时护盾检测（等待WraithSummonManager实现）
        boolean hasTemporaryShield = false;
        //if (entity instanceof Player player) {
        //    hasTemporaryShield = WraithSummonManager.hasTemporaryShield(player);
        //}

        if (max <= 0.001) {
            if (cur > 0.001 && !hasTemporaryShield) {
                curA.setBaseValue(0.0);
            }
            return;
        }

        if (cur > max) {
            if (!hasTemporaryShield) {
                curA.setBaseValue(max);
            }
        } else if (interval > 0 && entity.tickCount % interval == 0 && cur < max) {
            double newValue = Math.min(cur + 1, max);
            curA.setBaseValue(newValue);
        }
    }

    //护盾保护触发：取消伤害/死亡，设置满血，护盾-1
    private static void triggerShieldProtection(LivingEntity entity, Event event) {
        event.setCanceled(true);
        EntityUtil.theLastEndSetHealth(entity, entity.getMaxHealth());
        double currentShield = getShieldValue(entity);
        setShieldValue(entity, currentShield - 1);
    }

    //获取护盾值
    public static double getShieldValue(LivingEntity entity) {
        AttributeInstance a = entity.getAttribute(ModAttributes.JUSTIFIED_DEFENCE.get());
        return a != null ? a.getValue() : 0;
    }

    //设置护盾值
    public static void setShieldValue(LivingEntity entity, double value) {
        AttributeInstance a = entity.getAttribute(ModAttributes.JUSTIFIED_DEFENCE.get());
        if (a != null) {
            a.setBaseValue(Math.max(0, value));
        }
    }

    //龙水晶盔甲单件效果
    private static void applyDragonCrystalArmorEffects(Player player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);

        //头盔效果：夜视 + 水下呼吸
        if (!helmet.isEmpty() && helmet.getItem() instanceof DragonCrystalArmorItem.Helmet) {
            if (TheLastSwordConfiguration.getEnableNightVisionSafely()) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 240, 2, false, false));
            }
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 240, 2, false, false));
        }

        //胸甲效果：伤害抗性 + 力量
        if (!chestplate.isEmpty() && chestplate.getItem() instanceof DragonCrystalArmorItem.Chestplate) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 240, 2, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 2, false, false));
        }

        //护腿效果：生命恢复 + 跳跃提升
        if (!leggings.isEmpty() && leggings.getItem() instanceof DragonCrystalArmorItem.Leggings) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 240, 2, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 240, 0, false, false));
        }

        //靴子效果：速度 + 火焰抗性
        if (!boots.isEmpty() && boots.getItem() instanceof DragonCrystalArmorItem.Boots) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 240, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 240, 2, false, false));
        }
    }

    //水晶守护技能：给予吸收伤害效果（等于最大生命值），按配置时间刷新
    private static void applyCrystalGuard(Player player) {
        UUID id = player.getUUID();
        int maxHealth = (int) player.getMaxHealth();
        player.setAbsorptionAmount(maxHealth);

        if (!CRYSTAL_GUARD_TIMERS.getOrDefault(id, false)) {
            CRYSTAL_GUARD_TIMERS.put(id, true);
            int refreshInterval = TheLastSwordConfiguration.getCrystalGuardRefreshIntervalSafely();

            TheLastSwordMod.queueServerWork(refreshInterval, () -> {
                if (DragonCrystalArmorItem.isFullSet(player)) {
                    int currentMaxHealth = (int) player.getMaxHealth();
                    if (player.getAbsorptionAmount() < currentMaxHealth) {
                        player.setAbsorptionAmount(currentMaxHealth);
                        Level level = player.level();
                        BlockPos pos = player.blockPosition();
                        level.playSound(null, pos,
                            ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.amethyst_block.chime")),
                            SoundSource.PLAYERS, 2, 1);
                    }
                    applyCrystalGuard(player);
                } else {
                    CRYSTAL_GUARD_TIMERS.put(id, false);
                }
            });
        }
    }

    //同步玩家当前生命值到最大生命值上限
    private static void syncHealthToMaxHealth(Player player) {
        float currentHealth = player.getHealth();
        float maxHealth = player.getMaxHealth();

        if (currentHealth > maxHealth) {
            player.setHealth(maxHealth);
        }
    }

    // ==================== 龙之盔甲效果 ====================

    //龙之盔甲单件效果
    private static void applyDragonArmorEffects(Player player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) continue;

            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty() || !(stack.getItem() instanceof DragonArmorItem)) continue;

            //检查能量
            boolean hasEnergy = stack.getCapability(ForgeCapabilities.ENERGY)
                    .map(energy -> energy.getEnergyStored() > 0)
                    .orElse(false);

            //基础等级3，有能量时+1变成4
            int baseLevel = hasEnergy ? 4 : 3;
            //速度和跳跃的基础等级是2，有能量时+1变成3
            int speedJumpLevel = hasEnergy ? 3 : 2;

            switch (slot) {
                case HEAD -> {
                    if (TheLastSwordConfiguration.getDragonArmorEnableNightVisionSafely())
                        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 240, baseLevel, false, false));
                    player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 240, baseLevel, false, false));
                }
                case CHEST -> {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 240, baseLevel, false, false));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, baseLevel, false, false));
                }
                case LEGS -> {
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 240, baseLevel, false, false));
                    player.addEffect(new MobEffectInstance(MobEffects.JUMP, 240, speedJumpLevel, false, false));
                }
                case FEET -> {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 240, speedJumpLevel, false, false));
                    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 240, baseLevel, false, false));
                }
            }
        }
    }

    //龙之盔甲全套效果
    private static void handleDragonArmorFullSetEffects(Player player) {
        int fullSetWithEnergy = 0;

        //检查每件装备的能量并消耗
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) continue;

            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty() || !(stack.getItem() instanceof DragonArmorItem)) continue;

            //检查能量
            boolean hasEnergy = stack.getCapability(ForgeCapabilities.ENERGY)
                    .map(energy -> energy.getEnergyStored() > 0)
                    .orElse(false);

            if (hasEnergy) {
                fullSetWithEnergy++;
            }

            //能量消耗：每件盔甲每刻消耗配置值
            int costPerPiece = TheLastSwordConfiguration.getDragonArmorEnergyCostPerPieceSafely();
            stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(energy -> {
                energy.extractEnergy(costPerPiece, false);
            });
        }

        //全套效果1：饱和V + 冰火不侵（需要4件都有能量）
        if (fullSetWithEnergy == 4) {
            player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 240, 4, false, false));
            //清除冻伤
            player.setTicksFrozen(0);
            //清除燃烧
            player.clearFire();
        }

        //全套效果2：虚化（需要全套有电且处于飞行状态）
        if (fullSetWithEnergy == 4 && player.getAbilities().flying) {
            //给予虚化效果
            player.addEffect(new MobEffectInstance(ModEffects.PHASING.get(), 240, 0, false, false));

            //虚化额外能量消耗：胸甲额外消耗配置值
            int phasingCost = TheLastSwordConfiguration.getDragonArmorPhasingEnergyCostSafely();
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            chestplate.getCapability(ForgeCapabilities.ENERGY).ifPresent(energy -> {
                energy.extractEnergy(phasingCost, false);
            });
        }
    }

    //龙之盔甲飞行能力处理
    private static void handleDragonArmorFlying(Player player) {
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);

        //检查是否穿戴龙之甲胸甲
        boolean wearingDragonChestplate = !chestplate.isEmpty() &&
                chestplate.getItem() instanceof DragonArmorItem.Chestplate;

        //如果穿戴龙之甲胸甲且不是创造模式或旁观模式，启用飞行
        if (wearingDragonChestplate && !player.isCreative() && !player.isSpectator()) {
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.getPersistentData().putBoolean("DragonArmorFly", true); // 标记是龙之甲赋予的
                player.onUpdateAbilities();
            }
        } else if (!player.isCreative() && !player.isSpectator()) {
            //只移除龙之甲自己赋予的飞行能力
            if (player.getPersistentData().getBoolean("DragonArmorFly")) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.getPersistentData().remove("DragonArmorFly");
                player.getPersistentData().remove("DragonArmorFlyAnim"); // 清除动画状态标记
                player.onUpdateAbilities();
            }
        }
    }
}
