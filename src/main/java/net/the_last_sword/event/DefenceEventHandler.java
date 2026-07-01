package net.the_last_sword.event;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
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
import net.the_last_sword.configuration.DefenceConfig;
import net.the_last_sword.configuration.DefenceConfigData;
import net.the_last_sword.configuration.DefenceConfigData.*;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.init.ModAttributes;
import net.the_last_sword.item.DragonArmorItem;
import net.the_last_sword.item.DragonCrystalArmorItem;
import net.the_last_sword.network.DefenceConfigPacket;
import net.the_last_sword.network.DragonShieldPacket;
import net.the_last_sword.network.NetworkHandler;
import net.the_last_sword.util.EntityUtil;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.the_last_sword.init.ModEffects;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.client.PerceptionScanData.ScanType;
import net.the_last_sword.network.NetworkHandler;
import net.the_last_sword.network.PerceptionScanPacket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

//防御系统事件处理器
public final class DefenceEventHandler {
    private DefenceEventHandler() {}

    //水晶守护计时器
    private static final ConcurrentHashMap<UUID, Boolean> CRYSTAL_GUARD_TIMERS = new ConcurrentHashMap<>();

    //感知扫描计时器（记录上次扫描的tick）
    private static final ConcurrentHashMap<UUID, Long> PERCEPTION_SCAN_TIMERS = new ConcurrentHashMap<>();

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
        //护盾死亡保护 (兜底非 actuallyHurt 路径: entity.kill()/setHealth(0)/直接 die 等, 代价 -2)
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onLivingDeath(LivingDeathEvent event) {
            LivingEntity entity = event.getEntity();
            if (entity.level().isClientSide()) return;

            double currentShield = DefenceEventHandler.getShieldValue(entity);
            if (currentShield > 0) {
                DefenceEventHandler.triggerShieldProtection(entity, event, 2);
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
            } else if (player instanceof ServerPlayer sp) {
                // 脱下龙套时清除感知扫描
                if (PERCEPTION_SCAN_TIMERS.containsKey(player.getUUID())) {
                    PERCEPTION_SCAN_TIMERS.remove(player.getUUID());
                    NetworkHandler.sendToPlayer(new PerceptionScanPacket(Map.of(), 0), sp);
                }
            }
        }

        //飞行状态恢复：在所有实体tick完毕后执行，将被外部清掉的flying拉回
        @SubscribeEvent
        public static void onLevelTickEnd(TickEvent.LevelTickEvent event) {
            if (event.phase != TickEvent.Phase.END || event.level.isClientSide()) return;

            for (Player player : event.level.players()) {
                if (player.isCreative() || player.isSpectator()) continue;
                CompoundTag data = player.getPersistentData();
                //三套飞行来源的NBT标记任一存在，即代表有有效飞行授权（标记本身已含各自配置开关）
                boolean hasFlightSource = data.getBoolean("TheLastSwordFly")
                        || data.getBoolean("DragonArmorFly")
                        || data.getBoolean("WingsFly");
                if (hasFlightSource
                        && data.getBoolean("PlayerFlightIntent")
                        && !player.getAbilities().flying) {
                    player.getAbilities().flying = true;
                }
            }
        }

        //龙之盔甲伤害减免（龙魂觉醒被动，全套固定生效）
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

            if (!isNonPlayerAttack && !isExplosion) return;

            DefenceConfigData playerConfig = DefenceConfigPacket.getPlayerConfig(player.getUUID());
            DragonShieldModule dragonShield = playerConfig.armor.dragonArmor.defence.dragonShield;
            boolean dragonShieldEnabled = dragonShield == null || dragonShield.enabled;
            boolean dragonShieldActive = dragonShieldEnabled && DragonArmorItem.hasEnergyFullSet(player);

            if (dragonShieldActive) {
                event.setAmount(0);
                if (DefenceConfig.getDragonShieldModule().shieldEffect != DefenceConfigData.ShieldEffectMode.DISABLED
                        && player instanceof net.minecraft.server.level.ServerPlayer sp) {
                    NetworkHandler.sendToPlayer(DragonShieldPacket.fromDamageSource(sp, source), sp);
                }
            } else {
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

        //客户端Tick事件 - 飞行速度（飞行速度计算在客户端）
        @SubscribeEvent
        public static void onClientPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase != TickEvent.Phase.END || !event.player.level().isClientSide()) return;

            Player player = event.player;
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);

            //检查是否穿戴龙之甲胸甲
            if (chestplate.isEmpty() || !(chestplate.getItem() instanceof DragonArmorItem.Chestplate)) {
                //不穿龙甲时恢复默认速度
                if (player.getAbilities().getFlyingSpeed() != 0.05f) {
                    player.getAbilities().setFlyingSpeed(0.05f);
                }
                return;
            }

            //客户端直接读本地配置（飞行速度是客户端行为）
            if (!DefenceConfig.getDragonArmorChestplate().enableFlight) {
                player.getAbilities().setFlyingSpeed(0.05f);
                return;
            }

            //检查能量
            boolean hasEnergy = chestplate.getCapability(ForgeCapabilities.ENERGY)
                    .map(energy -> energy.getEnergyStored() > 0)
                    .orElse(false);

            //有电时应用配置速度，无电时恢复默认
            float targetSpeed = hasEnergy ? DefenceConfig.getAntiGravityModule().flySpeed : 0.05f;
            if (player.getAbilities().getFlyingSpeed() != targetSpeed) {
                player.getAbilities().setFlyingSpeed(targetSpeed);
            }

            //飞行惯性控制：关闭时松开按键立即停止
            if (player.getAbilities().flying && !DefenceConfig.getAntiGravityModule().enableInertia
                    && player instanceof LocalPlayer localPlayer) {
                boolean noInput = localPlayer.input.forwardImpulse == 0
                    && localPlayer.input.leftImpulse == 0
                    && !localPlayer.input.jumping
                    && !localPlayer.input.shiftKeyDown;
                if (noInput) {
                    player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                }
            }
        }
    }

    //护盾保护触发：取消事件，设置满血，按 cost 扣除护盾
    private static void triggerShieldProtection(LivingEntity entity, Event event, int cost) {
        event.setCanceled(true);
        EntityUtil.theLastEndSetHealth(entity, entity.getMaxHealth());
        double currentShield = getShieldValue(entity);
        setShieldValue(entity, Math.max(0, currentShield - cost));
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

        //获取玩家同步的配置
        DefenceConfigData playerConfig = DefenceConfigPacket.getPlayerConfig(player.getUUID());
        DragonCrystalArmorConfig config = playerConfig.armor.dragonCrystalArmor;

        //头盔效果：夜视 + 水下呼吸
        if (!helmet.isEmpty() && helmet.getItem() instanceof DragonCrystalArmorItem.Helmet) {
            if (config.helmet.enableNightVision) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 240, 2, false, false));
            }
            if (config.helmet.enableWaterBreathing) {
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 240, 2, false, false));
            }
        }

        //胸甲效果：伤害抗性 + 力量
        if (!chestplate.isEmpty() && chestplate.getItem() instanceof DragonCrystalArmorItem.Chestplate) {
            if (config.chestplate.enableDamageResistance) {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 240, 2, false, false));
            }
            if (config.chestplate.enableStrength) {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 2, false, false));
            }
        }

        //护腿效果：生命恢复 + 跳跃提升
        if (!leggings.isEmpty() && leggings.getItem() instanceof DragonCrystalArmorItem.Leggings) {
            if (config.leggings.enableRegeneration) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 240, 2, false, false));
            }
            if (config.leggings.enableJumpBoost) {
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 240, 0, false, false));
            }
        }

        //靴子效果：速度 + 火焰抗性
        if (!boots.isEmpty() && boots.getItem() instanceof DragonCrystalArmorItem.Boots) {
            if (config.boots.enableSpeed) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 240, 0, false, false));
            }
            if (config.boots.enableFireResistance) {
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 240, 2, false, false));
            }
        }
    }

    //水晶守护技能：给予吸收伤害效果（等于最大生命值），按配置时间刷新
    private static void applyCrystalGuard(Player player) {
        //检查玩家配置是否启用
        DefenceConfigData playerConfig = DefenceConfigPacket.getPlayerConfig(player.getUUID());
        if (!playerConfig.armor.dragonCrystalArmor.fullSet.enableCrystalGuard) {
            return;
        }

        UUID id = player.getUUID();

        // 只在首次穿戴时给予护盾并启动定时器
        if (!CRYSTAL_GUARD_TIMERS.getOrDefault(id, false)) {
            CRYSTAL_GUARD_TIMERS.put(id, true);
            int maxHealth = (int) player.getMaxHealth();
            player.setAbsorptionAmount(maxHealth);

            int refreshInterval = TheLastSwordConfiguration.getCrystalGuardRefreshIntervalSafely();

            TheLastSwordMod.queueServerWork(refreshInterval, () -> {
                CRYSTAL_GUARD_TIMERS.put(id, false);
                if (DragonCrystalArmorItem.isFullSet(player)) {
                    int currentMaxHealth = (int) player.getMaxHealth();
                    // 冷却结束时，护盾不满则刷新
                    if (player.getAbsorptionAmount() < currentMaxHealth) {
                        player.setAbsorptionAmount(currentMaxHealth);
                        Level level = player.level();
                        BlockPos pos = player.blockPosition();
                        level.playSound(null, pos,
                            ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.amethyst_block.chime")),
                            SoundSource.PLAYERS, 2, 1);
                    }
                    // 重新启动下一轮定时器
                    applyCrystalGuard(player);
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
        //获取玩家同步的配置
        DefenceConfigData playerConfig = DefenceConfigPacket.getPlayerConfig(player.getUUID());
        DragonArmorConfig config = playerConfig.armor.dragonArmor;
        boolean enhancedBuff = config.lifeSupport.enhancedBuff;
        int enhanceCost = net.the_last_sword.configuration.TheLastSwordConfiguration.getDragonArmorBuffEnhanceCostSafely();

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) continue;

            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty() || !(stack.getItem() instanceof DragonArmorItem)) continue;

            //检查能量并决定是否增强
            boolean enhanced = false;
            if (enhancedBuff) {
                boolean hasEnergy = stack.getCapability(ForgeCapabilities.ENERGY)
                        .map(energy -> energy.getEnergyStored() > 0)
                        .orElse(false);
                if (hasEnergy) {
                    enhanced = true;
                    //增强Buff消耗该件装备的能量
                    stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(energy -> {
                        energy.extractEnergy(enhanceCost, false);
                    });
                }
            }

            //基础等级3，增强时+1变成4
            int baseLevel = enhanced ? 4 : 3;
            //速度和跳跃的基础等级是2，增强时+1变成3
            int speedJumpLevel = enhanced ? 3 : 2;

            switch (slot) {
                case HEAD -> {
                    if (config.helmet.enableNightVision)
                        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 240, baseLevel, false, false));
                    if (config.helmet.enableWaterBreathing)
                        player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 240, baseLevel, false, false));
                }
                case CHEST -> {
                    if (config.chestplate.enableDamageResistance)
                        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 240, baseLevel, false, false));
                    if (config.chestplate.enableStrength)
                        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, baseLevel, false, false));
                }
                case LEGS -> {
                    if (config.leggings.enableRegeneration)
                        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 240, baseLevel, false, false));
                    if (config.leggings.enableJumpBoost)
                        player.addEffect(new MobEffectInstance(MobEffects.JUMP, 240, speedJumpLevel, false, false));
                }
                case FEET -> {
                    if (config.boots.enableSpeed)
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 240, speedJumpLevel, false, false));
                    if (config.boots.enableFireResistance)
                        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 240, baseLevel, false, false));
                }
            }
        }
    }

    //龙之盔甲全套效果
    private static void handleDragonArmorFullSetEffects(Player player) {
        //获取玩家同步的配置
        DefenceConfigData playerConfig = DefenceConfigPacket.getPlayerConfig(player.getUUID());
        LifeSupportModule lifeSupport = playerConfig.armor.dragonArmor.lifeSupport;
        PhasingModule phasingModule = playerConfig.armor.dragonArmor.defence.phasing;
        DragonShieldModule dragonShield = playerConfig.armor.dragonArmor.defence.dragonShield;
        boolean dragonShieldEnabled = dragonShield == null || dragonShield.enabled;
        boolean dragonAuraEnabled = dragonShield == null || dragonShield.enableDragonAura == null || dragonShield.enableDragonAura;

        //检查全套是否都有电
        boolean allHaveEnergy = true;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) continue;
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty() || !(stack.getItem() instanceof DragonArmorItem)) continue;
            boolean hasEnergy = stack.getCapability(ForgeCapabilities.ENERGY)
                    .map(energy -> energy.getEnergyStored() > 0)
                    .orElse(false);
            if (!hasEnergy) {
                allHaveEnergy = false;
                break;
            }
        }

        //维生模块：饱和V（需要全套有电且配置开启）
        if (allHaveEnergy && lifeSupport.enableSaturation) {
            player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 240, 4, false, false));
            int saturationCost = net.the_last_sword.configuration.TheLastSwordConfiguration.getDragonArmorSaturationCostSafely();
            drainAllArmorEnergy(player, saturationCost);
        }

        //维生模块：冰火不侵（需要全套有电且配置开启）
        if (allHaveEnergy && lifeSupport.enableIceFireImmunity) {
            player.setTicksFrozen(0);
            player.clearFire();
            int iceFireCost = net.the_last_sword.configuration.TheLastSwordConfiguration.getDragonArmorIceFireImmunityCostSafely();
            drainAllArmorEnergy(player, iceFireCost);
        }

        //虚化模块（需要全套有电且模块开启）
        if (allHaveEnergy && phasingModule.enabled) {
            boolean activate = (phasingModule.activationMode == DefenceConfigData.PhasingActivationMode.ALWAYS)
                || player.getAbilities().flying;
            if (activate) {
                player.addEffect(new MobEffectInstance(ModEffects.PHASING.get(), 240, 0, false, false));
                int phasingCost = net.the_last_sword.configuration.TheLastSwordConfiguration.getDragonArmorPhasingCostSafely();
                drainAllArmorEnergy(player, phasingCost);
            }
        }

        //感知模块扫描
        if (allHaveEnergy && dragonShieldEnabled) {
            drainAllArmorEnergy(player, 1);
        }

        if (allHaveEnergy && dragonAuraEnabled) {
            repelNearbyEntities(player);
            drainAllArmorEnergy(player, 1);
        }

        PerceptionModule perceptionModule = playerConfig.armor.dragonArmor.perception;
        if (allHaveEnergy && perceptionModule.scanEntities && player instanceof ServerPlayer sp) {
            performPerceptionScan(sp, perceptionModule);
        }
    }

    //感知模块扫描
    private static void performPerceptionScan(ServerPlayer player, PerceptionModule config) {
        UUID id = player.getUUID();
        long currentTick = player.level().getGameTime();
        long intervalTicks = config.scanIntervalSeconds * 20L;
        long lastScan = PERCEPTION_SCAN_TIMERS.getOrDefault(id, 0L);

        if (currentTick - lastScan < intervalTicks) return;
        PERCEPTION_SCAN_TIMERS.put(id, currentTick);

        double range = TheLastSwordConfiguration.getPerceptionScanRangeSafely();
        AABB scanBox = player.getBoundingBox().inflate(range);
        List<LivingEntity> entities = player.level().getEntitiesOfClass(LivingEntity.class, scanBox,
                e -> e != player && e.isAlive());

        Map<Integer, ScanType> scanResult = new HashMap<>();
        for (LivingEntity entity : entities) {
            scanResult.put(entity.getId(), classifyEntity(entity));
        }

        int glowDuration = TheLastSwordConfiguration.getPerceptionGlowDurationSafely();
        NetworkHandler.sendToPlayer(new PerceptionScanPacket(scanResult, glowDuration), player);
    }

    //判断实体类型
    private static void repelNearbyEntities(Player player) {
        final double radius = 3.0;
        final double radiusSqr = radius * radius;
        Vec3 center = player.position().add(0.0, player.getBbHeight() * 0.5, 0.0);
        AABB fieldBox = player.getBoundingBox().inflate(radius);
        List<Entity> entities = player.level().getEntities(player, fieldBox,
                entity -> entity.isAlive()
                        && entity != player
                        && !(entity instanceof ItemEntity)
                        && !(entity instanceof ExperienceOrb)
                        && !entity.isPassengerOfSameVehicle(player));

        for (Entity entity : entities) {
            Vec3 targetCenter = entity.position().add(0.0, entity.getBbHeight() * 0.5, 0.0);
            Vec3 offset = targetCenter.subtract(center);
            double distanceSqr = offset.lengthSqr();
            if (distanceSqr > radiusSqr) continue;

            Vec3 direction = distanceSqr < 1.0E-4
                    ? player.getLookAngle().reverse()
                    : offset.normalize();
            double distance = Math.sqrt(Math.max(distanceSqr, 1.0E-4));
            double strength = entity instanceof Projectile ? 1.15 : 0.45 + (radius - distance) * 0.12;
            Vec3 repel = direction.scale(strength);

            if (entity instanceof Projectile) {
                entity.setDeltaMovement(repel);
            } else {
                Vec3 current = entity.getDeltaMovement();
                entity.setDeltaMovement(current.x * 0.35 + repel.x, Math.max(current.y, 0.08), current.z * 0.35 + repel.z);
                entity.fallDistance = 0.0f;
            }
            entity.hasImpulse = true;
            if (player instanceof ServerPlayer sp && DefenceConfig.getDragonShieldModule().shieldEffect != DefenceConfigData.ShieldEffectMode.DISABLED) {
                NetworkHandler.sendToPlayer(new DragonShieldPacket(true,
                        (float) direction.x, (float) direction.y, (float) direction.z), sp);
            }
        }
    }

    private static ScanType classifyEntity(LivingEntity entity) {
        if (entity instanceof Animal || entity instanceof AbstractVillager) {
            return ScanType.FRIENDLY;
        }
        if (entity instanceof Mob mob && mob.getType().getCategory() == MobCategory.MONSTER) {
            return ScanType.HOSTILE;
        }
        return ScanType.NEUTRAL;
    }

    //全套每件扣电
    private static void drainAllArmorEnergy(Player player, int costPerPiece) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) continue;
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty() || !(stack.getItem() instanceof DragonArmorItem)) continue;
            stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(energy -> {
                energy.extractEnergy(costPerPiece, false);
            });
        }
    }

    //龙之盔甲飞行能力处理（服务端：只处理飞行能力，不处理速度）
    private static void handleDragonArmorFlying(Player player) {
        //获取玩家同步的配置检查飞行是否启用
        DefenceConfigData playerConfig = DefenceConfigPacket.getPlayerConfig(player.getUUID());
        if (!playerConfig.armor.dragonArmor.chestplate.enableFlight) {
            //如果飞行被禁用，移除龙之甲赋予的飞行能力
            if (player.getPersistentData().getBoolean("DragonArmorFly")) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.getPersistentData().remove("DragonArmorFly");
                player.getPersistentData().remove("DragonArmorFlyAnim");
                player.getPersistentData().remove("PlayerFlightIntent");
                player.onUpdateAbilities();
            }
            return;
        }

        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);

        //检查是否穿戴龙之甲胸甲
        boolean wearingDragonChestplate = !chestplate.isEmpty() &&
                chestplate.getItem() instanceof DragonArmorItem.Chestplate;

        //如果穿戴龙之甲胸甲且不是创造模式或旁观模式，启用飞行
        if (wearingDragonChestplate && !player.isCreative() && !player.isSpectator()) {
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.getPersistentData().putBoolean("DragonArmorFly", true);
                player.onUpdateAbilities();
            }
        } else if (!player.isCreative() && !player.isSpectator()) {
            //只移除龙之甲自己赋予的飞行能力
            if (player.getPersistentData().getBoolean("DragonArmorFly")) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.getPersistentData().remove("DragonArmorFly");
                player.getPersistentData().remove("DragonArmorFlyAnim");
                player.getPersistentData().remove("PlayerFlightIntent");
                player.onUpdateAbilities();
            }
        }
    }
}
