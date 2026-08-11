package net.the_last_sword.event;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemHandlerHelper;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.entity.DragonLightingEntity;
import net.the_last_sword.entity.variant.NamedDragonVariant;
import net.the_last_sword.init.ModAttributes;
import net.the_last_sword.init.ModEffects;
import net.the_last_sword.init.ModEntities;
import net.the_last_sword.util.EntityUtil;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber
public class EnderDragonEvent {
    public static final String NAMED_VARIANT_ID_KEY = "TheLastSwordDragonNameId";

    private static final String CHALLENGE_COUNT_KEY = "EnderDragonChallengeCount";
    private static final String PLAYER_UUID_LIST_KEY = "DragonAttackers";
    private static final String MODIFIER_ADDED_KEY = "ModifiersAdded";
    private static final String NAMED_VARIANT_ROLLED_KEY = "TheLastSwordDragonNameRolled";
    private static final String APPLIED_NAMED_VARIANT_ID_KEY = "TheLastSwordAppliedDragonNameId";
    private static final String DRAGON_LEVEL_KEY = "TheLastSwordDragonLevel";
    private static final String NEXT_AKATOSH_HEAL_CHECK_KEY = "TheLastSwordAkatoshNextHealCheck";
    private static final String AKATOSH_HEAL_COOLDOWN_END_KEY = "TheLastSwordAkatoshHealCooldownEnd";
    private static final String NEXT_FIREBALL_RAIN_KEY = "TheLastSwordDragonNextFireballRain";
    private static final String PLACIDUSAX_THRESHOLD_MASK_KEY = "TheLastSwordPlacidusaxThresholdMask";
    private static final String PLACIDUSAX_LIGHTNING_END_KEY = "TheLastSwordPlacidusaxLightningEnd";
    private static final String PLACIDUSAX_NEXT_LIGHTNING_KEY = "TheLastSwordPlacidusaxNextLightning";
    private static final String ALDUIN_KILL_COUNT_KEY = "TheLastSwordAlduinKillCount";

    private static final int AKATOSH_HEAL_CHECK_INTERVAL = 30 * 20;
    private static final int AKATOSH_HEAL_COOLDOWN = 60 * 20;
    private static final int ALDUIN_FIREBALL_RAIN_INTERVAL = 30 * 20;
    private static final int ODAHVIING_FIREBALL_RAIN_INTERVAL = 60 * 20;
    private static final int FIREBALL_RAIN_COUNT = 16;
    private static final double NAMED_DRAGON_ABILITY_RADIUS = 64.0;
    private static final int DURNEHVIIR_EFFECT_DURATION = 3 * 20;
    private static final int PLACIDUSAX_PHASE_DURATION = 6 * 20;
    private static final int PLACIDUSAX_LIGHTNING_INTERVAL = 20;
    private static final double NAMED_DRAGON_TOUGHNESS_REFERENCE = 4.0;

    private static final UUID NAMED_HEALTH_MODIFIER_ID = modifierId("named_dragon_health");
    private static final UUID NAMED_ATTACK_MODIFIER_ID = modifierId("named_dragon_attack");
    private static final UUID NAMED_ARMOR_MODIFIER_ID = modifierId("named_dragon_armor");
    private static final UUID NAMED_ARMOR_TOUGHNESS_MODIFIER_ID = modifierId("named_dragon_armor_toughness");
    private static final UUID NAMED_FLYING_SPEED_MODIFIER_ID = modifierId("named_dragon_flying_speed");
    private static final UUID NAMED_MAX_SHIELD_MODIFIER_ID = modifierId("named_dragon_max_shield");
    private static final UUID ALDUIN_KILL_ATTACK_MODIFIER_ID = modifierId("alduin_kill_attack");

    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        if (!event.has(EntityType.ENDER_DRAGON, Attributes.ATTACK_DAMAGE)) {
            event.add(EntityType.ENDER_DRAGON, Attributes.ATTACK_DAMAGE, 10.0);
        }
        if (!event.has(EntityType.ENDER_DRAGON, Attributes.FLYING_SPEED)) {
            event.add(EntityType.ENDER_DRAGON, Attributes.FLYING_SPEED, 0.6);
        }
    }

    // 持久化存储挑战次数和攻击者列表
    public static class ChallengeData extends SavedData {
        private int challengeCount;
        private final Set<UUID> attackerUUIDs = new HashSet<>();

        public ChallengeData() {
            this.challengeCount = 0;
        }

        public ChallengeData(CompoundTag tag) {
            this.challengeCount = tag.getInt(CHALLENGE_COUNT_KEY);
            ListTag uuidList = tag.getList(PLAYER_UUID_LIST_KEY, Tag.TAG_COMPOUND);
            for (int i = 0; i < uuidList.size(); i++) {
                CompoundTag uuidTag = uuidList.getCompound(i);
                this.attackerUUIDs.add(uuidTag.getUUID("UUID"));
            }
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            tag.putInt(CHALLENGE_COUNT_KEY, this.challengeCount);
            ListTag uuidList = new ListTag();
            for (UUID uuid : this.attackerUUIDs) {
                CompoundTag uuidTag = new CompoundTag();
                uuidTag.putUUID("UUID", uuid);
                uuidList.add(uuidTag);
            }
            tag.put(PLAYER_UUID_LIST_KEY, uuidList);
            return tag;
        }

        public int getChallengeCount() {
            return challengeCount;
        }

        public void incrementChallengeCount() {
            this.challengeCount++;
            this.setDirty();
        }

        public Set<UUID> getAttackerUUIDs() {
            return attackerUUIDs;
        }

        public void addAttacker(UUID playerUUID) {
            this.attackerUUIDs.add(playerUUID);
            this.setDirty();
        }

        public void clearAttackers() {
            this.attackerUUIDs.clear();
            this.setDirty();
        }

        public static ChallengeData load(CompoundTag tag) {
            return new ChallengeData(tag);
        }
    }

    // 获取挑战数据（仅限末地）
    private static ChallengeData getChallengeData(ServerLevel level) {
        if (level.dimension() == Level.END) {
            DimensionDataStorage storage = level.getDataStorage();
            return storage.computeIfAbsent(ChallengeData::load, ChallengeData::new, "dragon_challenges");
        }
        return null;
    }

    // 玩家攻击末影龙时记录UUID
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityAttacked(LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        if (event.getEntity() instanceof EnderDragon attackedDragon) {
            NamedDragonVariant attackedVariant = getNamedVariant(attackedDragon);
            int cancelChance = attackedVariant == NamedDragonVariant.AKATOSH
                    ? 10
                    : attackedVariant == NamedDragonVariant.PLACIDUSAX ? 1 : 0;
            if (cancelChance > 0 && attackedDragon.getRandom().nextInt(100) < cancelChance) {
                event.setCanceled(true);
                return;
            }
        }

        LivingEntity target = event.getEntity();
        if (event.getSource().getEntity() instanceof EnderDragon attackingDragon
                && event.getSource().is(DamageTypes.MOB_ATTACK)
                && getNamedVariant(attackingDragon) == NamedDragonVariant.BAYLE) {
            target.setSecondsOnFire(60);
        }

        if (event.getEntity() instanceof EnderDragon dragon
                && event.getSource().getEntity() instanceof Player player) {
            ServerLevel level = (ServerLevel) dragon.level();
            ChallengeData data = getChallengeData(level);
            if (data != null) {
                data.addAttacker(player.getUUID());
            }
        }
    }

    // 末影龙死亡时发放龙蛋并增加挑战次数
    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        if (event.getSource().getEntity() instanceof EnderDragon killer
                && event.getEntity() != killer
                && getNamedVariant(killer) == NamedDragonVariant.ALDUIN) {
            incrementAlduinKillCount(killer);
        }

        if (!(event.getEntity() instanceof EnderDragon dragon)) return;

        ServerLevel level = (ServerLevel) dragon.level();
        ChallengeData data = getChallengeData(level);
        if (data == null) return;

        data.incrementChallengeCount();

        Set<UUID> attackerUUIDs = new HashSet<>(data.getAttackerUUIDs());
        Set<UUID> remainingUUIDs = new HashSet<>(attackerUUIDs);
        data.clearAttackers();

        if (!TheLastSwordConfiguration.ENDER_DRAGON_EGG_DROP.get()) return;

        int radius = TheLastSwordConfiguration.ENDER_DRAGON_EGG_RADIUS.get();
        int amount = TheLastSwordConfiguration.ENDER_DRAGON_EGG_AMOUNT.get();
        boolean giveAbsent = TheLastSwordConfiguration.ENDER_DRAGON_EGG_GIVE_TO_ABSENT_PLAYERS.get();
        boolean multiple = TheLastSwordConfiguration.ENDER_DRAGON_EGG_MULTIPLE.get();

        // 给在场玩家发放龙蛋
        for (Player player : level.players()) {
            if (!attackerUUIDs.contains(player.getUUID())) continue;

            double distance = player.distanceToSqr(dragon.getX(), dragon.getY(), dragon.getZ());
            if (distance > radius * radius) continue;

            ItemStack dragonEgg = new ItemStack(Blocks.DRAGON_EGG, amount);
            ItemHandlerHelper.giveItemToPlayer(player, dragonEgg);
            remainingUUIDs.remove(player.getUUID());

            // 非多人模式下只给第一个在场玩家
            if (!multiple) break;
        }

        // 给未在场玩家发放龙蛋（仅多人模式下生效）
        if (giveAbsent && multiple) {
            for (UUID uuid : remainingUUIDs) {
                Player absentPlayer = level.getServer().getPlayerList().getPlayer(uuid);
                if (absentPlayer != null) {
                    ItemStack dragonEgg = new ItemStack(Blocks.DRAGON_EGG, amount);
                    ItemHandlerHelper.giveItemToPlayer(absentPlayer, dragonEgg);
                }
            }
        }
    }

    // 末影龙生成时根据挑战次数强化属性
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof EnderDragon dragon)) return;

        ServerLevel level = (ServerLevel) event.getLevel();
        if (level.dimension() != Level.END) return;

        ChallengeData data = getChallengeData(level);
        if (data == null || data.getChallengeCount() <= 0) return;

        CompoundTag dragonData = dragon.getPersistentData();
        if (dragonData.getBoolean(MODIFIER_ADDED_KEY)) return;

        int challengeCount = data.getChallengeCount();
        int cappedLevel = Math.min(challengeCount, TheLastSwordConfiguration.ENDER_DRAGON_MAX_LEVEL.get());

        dragonData.putInt(DRAGON_LEVEL_KEY, cappedLevel);
        rollNamedVariantIfNeeded(dragon, dragonData);

        applyDragonUpgrades(dragon, cappedLevel);
        dragonData.putBoolean(MODIFIER_ADDED_KEY, true);
        syncNamedVariant(dragon, true);
    }

    // 每 tick 只比较两个整数，使 /data merge 修改公开 NBT 后可以立即切换命名变体。
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof EnderDragon dragon)) return;

        CompoundTag data = dragon.getPersistentData();
        int requestedId = data.getInt(NAMED_VARIANT_ID_KEY);
        int appliedId = data.getInt(APPLIED_NAMED_VARIANT_ID_KEY);
        if (requestedId != appliedId) {
            syncNamedVariant(dragon, false);
        }

        tickNamedDragonAbilities(dragon);
    }

    private static void rollNamedVariantIfNeeded(EnderDragon dragon, CompoundTag data) {
        if (data.getBoolean(NAMED_VARIANT_ROLLED_KEY) || data.contains(NAMED_VARIANT_ID_KEY, Tag.TAG_INT)) {
            return;
        }

        int chance = TheLastSwordConfiguration.ENDER_DRAGON_NAMED_CHANCE.get();
        int variantId = 0;
        if (chance > 0 && dragon.getRandom().nextInt(100) < chance) {
            variantId = NamedDragonVariant.random(dragon.getRandom()).getId();
        }

        data.putInt(NAMED_VARIANT_ID_KEY, variantId);
        data.putBoolean(NAMED_VARIANT_ROLLED_KEY, true);
    }

    private static void syncNamedVariant(EnderDragon dragon, boolean forceNameUpdate) {
        CompoundTag data = dragon.getPersistentData();
        migrateLegacyNamedShield(dragon);
        int requestedId = data.getInt(NAMED_VARIANT_ID_KEY);
        int appliedId = data.getInt(APPLIED_NAMED_VARIANT_ID_KEY);
        NamedDragonVariant oldVariant = NamedDragonVariant.byId(appliedId);
        NamedDragonVariant newVariant = NamedDragonVariant.byId(requestedId);

        if (requestedId != appliedId) {
            double oldMaxHealth = dragon.getMaxHealth();
            double healthRatio = oldMaxHealth > 0.0 ? dragon.getHealth() / oldMaxHealth : 1.0;
            removeNamedAttributeModifiers(dragon);
            applyNamedAttributeModifiers(dragon, newVariant);
            updateNamedShield(dragon, oldVariant, newVariant);
            dragon.setHealth((float) Math.max(1.0, dragon.getMaxHealth() * healthRatio));
            data.putInt(APPLIED_NAMED_VARIANT_ID_KEY, requestedId);
            prepareNamedAbilityState(dragon, oldVariant, newVariant);
        }

        if (forceNameUpdate || requestedId != appliedId) {
            updateDragonName(dragon, newVariant, data.getInt(DRAGON_LEVEL_KEY));
        }
    }

    private static void updateDragonName(EnderDragon dragon, NamedDragonVariant variant, int level) {
        Component baseName = variant == null
                ? dragon.getType().getDescription()
                : Component.translatable(variant.getTranslationKey());
        dragon.setCustomName(Component.translatable(
                "named_dragon.the_last_sword.with_level", baseName, level));
    }

    private static void removeNamedAttributeModifiers(EnderDragon dragon) {
        removeAttributeModifier(dragon, Attributes.MAX_HEALTH, NAMED_HEALTH_MODIFIER_ID);
        removeAttributeModifier(dragon, Attributes.ATTACK_DAMAGE, NAMED_ATTACK_MODIFIER_ID);
        removeAttributeModifier(dragon, Attributes.ARMOR, NAMED_ARMOR_MODIFIER_ID);
        removeAttributeModifier(dragon, Attributes.ARMOR_TOUGHNESS, NAMED_ARMOR_TOUGHNESS_MODIFIER_ID);
        removeAttributeModifier(dragon, Attributes.FLYING_SPEED, NAMED_FLYING_SPEED_MODIFIER_ID);
        removeAttributeModifier(dragon, ModAttributes.MAX_JUSTIFIED_DEFENCE.get(), NAMED_MAX_SHIELD_MODIFIER_ID);
        removeAttributeModifier(dragon, Attributes.ATTACK_DAMAGE, ALDUIN_KILL_ATTACK_MODIFIER_ID);
    }

    private static void applyNamedAttributeModifiers(EnderDragon dragon, NamedDragonVariant variant) {
        if (variant == null) return;

        addNamedAttributeModifier(dragon, Attributes.MAX_HEALTH, variant.getHealthBonus(),
                NAMED_HEALTH_MODIFIER_ID, "named_dragon_health");
        addNamedAttributeModifier(dragon, Attributes.ATTACK_DAMAGE, variant.getAttackBonus(),
                NAMED_ATTACK_MODIFIER_ID, "named_dragon_attack");
        addNamedAttributeModifier(dragon, Attributes.ARMOR, variant.getArmorBonus(),
                NAMED_ARMOR_MODIFIER_ID, "named_dragon_armor");
        addNamedArmorToughnessModifier(dragon, variant.getArmorToughnessBonus());
        addNamedAttributeModifier(dragon, Attributes.FLYING_SPEED, variant.getFlyingSpeedBonus(),
                NAMED_FLYING_SPEED_MODIFIER_ID, "named_dragon_flying_speed");

        if (variant == NamedDragonVariant.ALDUIN) {
            applyAlduinKillBonus(dragon);
        }
    }

    private static void prepareNamedAbilityState(EnderDragon dragon, NamedDragonVariant oldVariant,
                                                 NamedDragonVariant newVariant) {
        CompoundTag data = dragon.getPersistentData();
        long gameTime = dragon.level().getGameTime();

        if (oldVariant == NamedDragonVariant.PLACIDUSAX && newVariant != NamedDragonVariant.PLACIDUSAX) {
            dragon.removeEffect(ModEffects.PHASING.get());
        }
        if (newVariant == NamedDragonVariant.AKATOSH) {
            data.putLong(NEXT_AKATOSH_HEAL_CHECK_KEY, gameTime + AKATOSH_HEAL_CHECK_INTERVAL);
            data.putLong(AKATOSH_HEAL_COOLDOWN_END_KEY, 0L);
        }
        if (newVariant == NamedDragonVariant.ALDUIN) {
            data.putLong(NEXT_FIREBALL_RAIN_KEY, gameTime + ALDUIN_FIREBALL_RAIN_INTERVAL);
        } else if (newVariant == NamedDragonVariant.ODAHVIING) {
            data.putLong(NEXT_FIREBALL_RAIN_KEY, gameTime + ODAHVIING_FIREBALL_RAIN_INTERVAL);
        }
        if (newVariant == NamedDragonVariant.PLACIDUSAX && oldVariant != NamedDragonVariant.PLACIDUSAX) {
            data.putInt(PLACIDUSAX_THRESHOLD_MASK_KEY, 0);
            data.putLong(PLACIDUSAX_LIGHTNING_END_KEY, 0L);
            data.putLong(PLACIDUSAX_NEXT_LIGHTNING_KEY, 0L);
        }
    }

    private static void tickNamedDragonAbilities(EnderDragon dragon) {
        if (!dragon.isAlive()) return;
        NamedDragonVariant variant = getNamedVariant(dragon);
        if (variant == null) return;

        long gameTime = dragon.level().getGameTime();
        CompoundTag data = dragon.getPersistentData();
        switch (variant) {
            case AKATOSH -> tickAkatosh(dragon, data, gameTime);
            case ALDUIN -> tickFireballRain(dragon, data, gameTime, ALDUIN_FIREBALL_RAIN_INTERVAL);
            case ODAHVIING -> tickFireballRain(dragon, data, gameTime, ODAHVIING_FIREBALL_RAIN_INTERVAL);
            case DURNEHVIIR -> {
                if (dragon.tickCount % 20 == 0) {
                    applyDurnehviirAura(dragon);
                }
            }
            case PLACIDUSAX -> {
                triggerPlacidusaxThresholds(dragon, dragon.getHealth());
                tickPlacidusaxLightning(dragon, data, gameTime);
            }
            default -> {
            }
        }
    }

    private static void tickAkatosh(EnderDragon dragon, CompoundTag data, long gameTime) {
        long nextCheck = data.getLong(NEXT_AKATOSH_HEAL_CHECK_KEY);
        if (nextCheck <= 0) {
            data.putLong(NEXT_AKATOSH_HEAL_CHECK_KEY, gameTime + AKATOSH_HEAL_CHECK_INTERVAL);
            return;
        }
        if (gameTime < nextCheck) return;

        long cooldownEnd = data.getLong(AKATOSH_HEAL_COOLDOWN_END_KEY);
        if (gameTime < cooldownEnd) {
            data.putLong(NEXT_AKATOSH_HEAL_CHECK_KEY, cooldownEnd);
            return;
        }

        if (dragon.getRandom().nextInt(100) == 0) {
            EntityUtil.theLastEndSetHealth(dragon, dragon.getMaxHealth());
            long nextAvailableTime = gameTime + AKATOSH_HEAL_COOLDOWN;
            data.putLong(AKATOSH_HEAL_COOLDOWN_END_KEY, nextAvailableTime);
            data.putLong(NEXT_AKATOSH_HEAL_CHECK_KEY, nextAvailableTime);
        } else {
            data.putLong(NEXT_AKATOSH_HEAL_CHECK_KEY, gameTime + AKATOSH_HEAL_CHECK_INTERVAL);
        }
    }

    private static void tickFireballRain(EnderDragon dragon, CompoundTag data, long gameTime,
                                         int interval) {
        long nextRain = data.getLong(NEXT_FIREBALL_RAIN_KEY);
        if (nextRain <= 0) {
            data.putLong(NEXT_FIREBALL_RAIN_KEY, gameTime + interval);
            return;
        }
        if (gameTime < nextRain) return;

        data.putLong(NEXT_FIREBALL_RAIN_KEY, gameTime + interval);
        summonFireballRain(dragon);
    }

    private static void summonFireballRain(EnderDragon dragon) {
        if (!(dragon.level() instanceof ServerLevel serverLevel)) return;

        AABB area = areaAround(dragon, NAMED_DRAGON_ABILITY_RADIUS);
        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(
                LivingEntity.class, area,
                target -> target != dragon && target.isAlive()
                        && target.distanceToSqr(dragon) <= NAMED_DRAGON_ABILITY_RADIUS * NAMED_DRAGON_ABILITY_RADIUS
                        && EntityUtil.canAttack(dragon, target));

        for (int i = 0; i < FIREBALL_RAIN_COUNT; i++) {
            LivingEntity target = targets.isEmpty()
                    ? null
                    : targets.get(dragon.getRandom().nextInt(targets.size()));
            double targetX = target == null
                    ? dragon.getX() + (dragon.getRandom().nextDouble() - 0.5) * 48.0
                    : target.getX() + (dragon.getRandom().nextDouble() - 0.5) * 8.0;
            double targetY = target == null ? dragon.getY() - 8.0 : target.getY();
            double targetZ = target == null
                    ? dragon.getZ() + (dragon.getRandom().nextDouble() - 0.5) * 48.0
                    : target.getZ() + (dragon.getRandom().nextDouble() - 0.5) * 8.0;
            Vec3 spawn = new Vec3(targetX, Math.max(dragon.getY() + 12.0, targetY + 24.0), targetZ);
            Vec3 direction = new Vec3(targetX, targetY, targetZ).subtract(spawn).normalize();

            LargeFireball fireball = new LargeFireball(
                    serverLevel, dragon, direction.x, direction.y, direction.z, 1);
            fireball.setOwner(dragon);
            fireball.setPos(spawn.x, spawn.y, spawn.z);
            serverLevel.addFreshEntity(fireball);
        }
        serverLevel.playSound(null, dragon.blockPosition(), SoundEvents.GHAST_SHOOT,
                dragon.getSoundSource(), 4.0F, 0.7F);
    }

    private static void applyDurnehviirAura(EnderDragon dragon) {
        AABB area = areaAround(dragon, 16.0);
        List<LivingEntity> targets = dragon.level().getEntitiesOfClass(
                LivingEntity.class, area,
                target -> target != dragon && target.isAlive() && target.distanceToSqr(dragon) <= 16.0 * 16.0);
        for (LivingEntity target : targets) {
            target.addEffect(new MobEffectInstance(
                    MobEffects.WEAKNESS, DURNEHVIIR_EFFECT_DURATION, 4, true, true, true), dragon);
            target.addEffect(new MobEffectInstance(
                    MobEffects.POISON, DURNEHVIIR_EFFECT_DURATION, 4, true, true, true), dragon);
        }
    }

    private static void triggerPlacidusaxThresholds(EnderDragon dragon, float healthAfterDamage) {
        if (dragon.getMaxHealth() <= 0.0F) return;
        CompoundTag data = dragon.getPersistentData();
        int mask = data.getInt(PLACIDUSAX_THRESHOLD_MASK_KEY);
        float healthRatio = healthAfterDamage / dragon.getMaxHealth();
        float[] thresholds = {0.50F, 0.25F, 0.05F};

        for (int i = 0; i < thresholds.length; i++) {
            int bit = 1 << i;
            if ((mask & bit) == 0 && healthRatio <= thresholds[i]) {
                mask |= bit;
                startPlacidusaxLightning(dragon);
            }
        }
        data.putInt(PLACIDUSAX_THRESHOLD_MASK_KEY, mask);
    }

    private static void startPlacidusaxLightning(EnderDragon dragon) {
        long gameTime = dragon.level().getGameTime();
        CompoundTag data = dragon.getPersistentData();
        dragon.addEffect(new MobEffectInstance(
                ModEffects.PHASING.get(), PLACIDUSAX_PHASE_DURATION, 0, false, true, true));
        data.putLong(PLACIDUSAX_LIGHTNING_END_KEY, gameTime + PLACIDUSAX_PHASE_DURATION);
        data.putLong(PLACIDUSAX_NEXT_LIGHTNING_KEY, gameTime);
    }

    private static void tickPlacidusaxLightning(EnderDragon dragon, CompoundTag data, long gameTime) {
        long stormEnd = data.getLong(PLACIDUSAX_LIGHTNING_END_KEY);
        if (stormEnd <= 0 || gameTime >= stormEnd) return;

        long nextStrike = data.getLong(PLACIDUSAX_NEXT_LIGHTNING_KEY);
        if (gameTime < nextStrike) return;
        data.putLong(PLACIDUSAX_NEXT_LIGHTNING_KEY, gameTime + PLACIDUSAX_LIGHTNING_INTERVAL);
        summonPlacidusaxLightning(dragon);
    }

    private static void summonPlacidusaxLightning(EnderDragon dragon) {
        if (!(dragon.level() instanceof ServerLevel serverLevel)) return;
        AABB area = areaAround(dragon, NAMED_DRAGON_ABILITY_RADIUS);
        List<LivingEntity> targets = new ArrayList<>(serverLevel.getEntitiesOfClass(
                LivingEntity.class, area,
                target -> target != dragon && target.isAlive()
                        && target.distanceToSqr(dragon) <= NAMED_DRAGON_ABILITY_RADIUS * NAMED_DRAGON_ABILITY_RADIUS
                        && EntityUtil.canAttack(dragon, target)));
        if (targets.isEmpty()) return;

        DamageSource source = new DamageSource(
                serverLevel.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(DamageTypes.LIGHTNING_BOLT),
                dragon, dragon);
        float damage = (float) Math.max(1.0, getDragonAttackDamage(dragon));
        LivingEntity target = targets.get(dragon.getRandom().nextInt(targets.size()));
        DragonLightingEntity lightning = new DragonLightingEntity(
                ModEntities.DRAGON_LIGHTING.get(), serverLevel);
        lightning.moveTo(target.getX(), target.getY(), target.getZ());
        serverLevel.addFreshEntity(lightning);
        target.invulnerableTime = 0;
        target.hurt(source, damage);
    }

    private static void incrementAlduinKillCount(EnderDragon dragon) {
        CompoundTag data = dragon.getPersistentData();
        int kills = Math.min(1000, data.getInt(ALDUIN_KILL_COUNT_KEY) + 1);
        data.putInt(ALDUIN_KILL_COUNT_KEY, kills);
        applyAlduinKillBonus(dragon);
    }

    private static void applyAlduinKillBonus(EnderDragon dragon) {
        AttributeInstance attack = dragon.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack == null) return;
        attack.removeModifier(ALDUIN_KILL_ATTACK_MODIFIER_ID);
        int kills = dragon.getPersistentData().getInt(ALDUIN_KILL_COUNT_KEY);
        if (kills > 0) {
            attack.addPermanentModifier(new AttributeModifier(
                    ALDUIN_KILL_ATTACK_MODIFIER_ID, "alduin_kill_attack", kills,
                    AttributeModifier.Operation.MULTIPLY_BASE));
        }
    }

    private static NamedDragonVariant getNamedVariant(EnderDragon dragon) {
        return NamedDragonVariant.byId(dragon.getPersistentData().getInt(NAMED_VARIANT_ID_KEY));
    }

    private static AABB areaAround(EnderDragon dragon, double radius) {
        return new AABB(
                dragon.getX() - radius, dragon.getY() - radius, dragon.getZ() - radius,
                dragon.getX() + radius, dragon.getY() + radius, dragon.getZ() + radius);
    }

    public static float scaleDragonContactDamage(EnderDragon dragon, float vanillaDamage) {
        AttributeInstance attack = dragon.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack == null) {
            return (float) (vanillaDamage * getFallbackAttackMultiplier(dragon));
        }
        double baseAttack = attack.getBaseValue();
        if (baseAttack <= 0.0) return vanillaDamage;
        return (float) (vanillaDamage * Math.max(0.0, attack.getValue()) / baseAttack);
    }

    private static double getDragonAttackDamage(EnderDragon dragon) {
        AttributeInstance attack = dragon.getAttribute(Attributes.ATTACK_DAMAGE);
        return attack == null ? 10.0 * getFallbackAttackMultiplier(dragon) : attack.getValue();
    }

    private static double getFallbackAttackMultiplier(EnderDragon dragon) {
        NamedDragonVariant variant = getNamedVariant(dragon);
        double multiplier = variant == null ? 1.0 : Math.max(0.0, 1.0 + variant.getAttackBonus());
        if (variant == NamedDragonVariant.ALDUIN) {
            multiplier *= 1.0 + Math.max(0, dragon.getPersistentData().getInt(ALDUIN_KILL_COUNT_KEY));
        }
        return multiplier;
    }

    private static void updateNamedShield(EnderDragon dragon, NamedDragonVariant oldVariant,
                                          NamedDragonVariant newVariant) {
        double oldBonus = oldVariant == null ? 0.0 : oldVariant.getJustifiedDefenceBonus();
        double newBonus = newVariant == null ? 0.0 : newVariant.getJustifiedDefenceBonus();
        if (newBonus > 0.0 && oldBonus <= 0.0) {
            EntityUtil.grantTempShield(dragon, newBonus);
        }
    }

    private static void migrateLegacyNamedShield(EnderDragon dragon) {
        AttributeInstance maxShield = dragon.getAttribute(ModAttributes.MAX_JUSTIFIED_DEFENCE.get());
        if (maxShield == null || maxShield.getModifier(NAMED_MAX_SHIELD_MODIFIER_ID) == null) {
            return;
        }

        maxShield.removeModifier(NAMED_MAX_SHIELD_MODIFIER_ID);
        AttributeInstance currentShield = dragon.getAttribute(ModAttributes.JUSTIFIED_DEFENCE.get());
        if (currentShield != null && currentShield.getValue() > 0.0) {
            dragon.getPersistentData().putBoolean(EntityUtil.NBT_TEMP_JUSTIFIED_DEFENCE, true);
        }
    }

    private static void addNamedAttributeModifier(EnderDragon dragon, Attribute attribute, double value,
                                                   UUID id, String name) {
        addAttributeModifier(dragon, attribute, value, id, name,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    private static void addNamedArmorToughnessModifier(EnderDragon dragon, double bonus) {
        if (bonus == 0.0) return;
        AttributeInstance toughness = dragon.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (toughness == null) return;

        if (toughness.getBaseValue() <= 0.0) {
            double fallbackAmount = NAMED_DRAGON_TOUGHNESS_REFERENCE * bonus;
            if (fallbackAmount > 0.0) {
                toughness.addPermanentModifier(new AttributeModifier(
                        NAMED_ARMOR_TOUGHNESS_MODIFIER_ID, "named_dragon_armor_toughness",
                        fallbackAmount, AttributeModifier.Operation.ADDITION));
            }
        } else {
            toughness.addPermanentModifier(new AttributeModifier(
                    NAMED_ARMOR_TOUGHNESS_MODIFIER_ID, "named_dragon_armor_toughness",
                    bonus, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    private static void addAttributeModifier(EnderDragon dragon, Attribute attribute, double value,
                                             UUID id, String name, AttributeModifier.Operation operation) {
        if (value == 0.0) return;
        AttributeInstance attr = dragon.getAttribute(attribute);
        if (attr != null) {
            attr.addPermanentModifier(new AttributeModifier(id, name, value, operation));
        }
    }

    private static void removeAttributeModifier(EnderDragon dragon, Attribute attribute, UUID id) {
        AttributeInstance attr = dragon.getAttribute(attribute);
        if (attr != null) {
            attr.removeModifier(id);
        }
    }

    private static UUID modifierId(String name) {
        return UUID.nameUUIDFromBytes(("the_last_sword:" + name).getBytes(StandardCharsets.UTF_8));
    }

    // 根据等级强化末影龙属性
    private static void applyDragonUpgrades(EnderDragon dragon, int cappedLevel) {
        double healthIncrease = cappedLevel * (cappedLevel < 6 ?
                TheLastSwordConfiguration.ENDER_DRAGON_HEALTH_INCREASE_VALUE.get() :
                TheLastSwordConfiguration.ENDER_DRAGON_HEALTH_INCREASE_VALUE_HIGH_LEVEL.get());

        AttributeInstance maxHealthAttr = dragon.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            double newMaxHealth = maxHealthAttr.getBaseValue() + healthIncrease;
            maxHealthAttr.setBaseValue(newMaxHealth);
            dragon.setHealth((float) newMaxHealth);
        }

        double armorIncrease = cappedLevel * (cappedLevel < 6 ?
                TheLastSwordConfiguration.ENDER_DRAGON_ARMOR_INCREASE_VALUE.get() :
                TheLastSwordConfiguration.ENDER_DRAGON_ARMOR_INCREASE_VALUE_HIGH_LEVEL.get());
        addAttributeModifier(dragon, Attributes.ARMOR, armorIncrease, "dragon_armor_boost");

        double attackIncrease = cappedLevel * (cappedLevel < 6 ?
                TheLastSwordConfiguration.ENDER_DRAGON_ATTACK_INCREASE_VALUE.get() :
                TheLastSwordConfiguration.ENDER_DRAGON_ATTACK_INCREASE_VALUE_HIGH_LEVEL.get());
        addAttributeModifier(dragon, Attributes.ATTACK_DAMAGE, attackIncrease, "dragon_attack_boost");
    }

    // 添加属性修饰符
    private static void addAttributeModifier(EnderDragon dragon, Attribute attribute, double value, String name) {
        AttributeInstance attr = dragon.getAttribute(attribute);
        if (attr != null) {
            UUID modifierId = UUID.nameUUIDFromBytes(name.getBytes());
            attr.addPermanentModifier(new AttributeModifier(
                    modifierId, name, value, AttributeModifier.Operation.ADDITION));
        }
    }
}
