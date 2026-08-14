package net.the_last_sword.entity;

import net.eca.api.EcaAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.ai.DragonCultPriestBreathGoal;
import net.the_last_sword.entity.ai.DragonCultPriestGuardGoal;
import net.the_last_sword.entity.ai.DragonCultPriestKeepDistanceGoal;
import net.the_last_sword.entity.ai.DragonCultPriestLightningGoal;
import net.the_last_sword.entity.variant.NamedPriestVariant;
import net.the_last_sword.faction.DragonCultFaction;
import net.the_last_sword.init.ModItems;
import net.the_last_sword.init.ModEntities;
import net.the_last_sword.util.EntityUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.UUID;

public class DragonCultPriestEntity extends TheLastEndEntity {

    public static final String NAMED_VARIANT_ID_KEY = "TheLastSwordPriestNameId";
    private static final String APPLIED_NAMED_VARIANT_ID_KEY = "TheLastSwordAppliedPriestNameId";
    private static final UUID NAMED_HEALTH_MODIFIER_ID = UUID.fromString("fe91ed7e-95cd-4fbc-89c6-ae1239e82201");
    private static final UUID NAMED_ATTACK_MODIFIER_ID = UUID.fromString("fe91ed7e-95cd-4fbc-89c6-ae1239e82202");
    private static final UUID NAMED_ARMOR_MODIFIER_ID = UUID.fromString("fe91ed7e-95cd-4fbc-89c6-ae1239e82203");
    private static final UUID NAMED_TOUGHNESS_MODIFIER_ID = UUID.fromString("fe91ed7e-95cd-4fbc-89c6-ae1239e82204");
    private static final double TOUGHNESS_REFERENCE = 4.0;

    public enum PriestSkill {
        BREATH,
        LIGHTNING,
        GUARD
    }

    public static final int STATE_BREATH = 3;
    public static final int STATE_LIGHTNING = 4;
    public static final int STATE_GUARD = 5;

    //死亡动画 3 秒，末尾爆炸
    public static final int DEATH_ANIMATION_TICKS = 60;

    //悬浮调整的每tick垂直速度
    private static final double HOVER_ADJUST_SPEED = 0.08;

    //离地高度容差，避免在目标高度上下抖动
    private static final double HOVER_TOLERANCE = 0.5;

    //向下探测地面的最大深度
    private static final int HOVER_SCAN_DEPTH = 16;

    //AI 接管飞行时的悬浮修正抑制计时
    private int hoverSuppressTicks;

    public DragonCultPriestEntity(EntityType<? extends DragonCultPriestEntity> type, Level world) {
        super(type, world);
        setMaxUpStep(0.6f);
        xpReward = 200;
        setPersistenceRequired();
        //悬浮单位：飞行移动 + 无重力，高度由 tickHover 维持
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.setNoGravity(true);
    }

    //飞行寻路，允许穿门、不主动落水
    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level world) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, world);
        navigation.setCanFloat(false);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    //飞行实体不受摔落伤害
    @Override
    public boolean causeFallDamage(float distance, float multiplier, @NotNull DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, @NotNull BlockState state, @NotNull BlockPos pos) {
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(1, new DragonCultPriestGuardGoal(this));
        this.goalSelector.addGoal(2, new DragonCultPriestLightningGoal(this));
        this.goalSelector.addGoal(3, new DragonCultPriestBreathGoal(this));
        //维持 3~8 格交战距离，过近则后撤；不加随机游荡，否则会与之抢移动控制权
        this.goalSelector.addGoal(4, new DragonCultPriestKeepDistanceGoal(this));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this)
                .setAlertOthers(DragonCultistEntity.class, DragonCultPaladinEntity.class, DragonCultPriestEntity.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    @Override
    public String getIdleAnimationName() {
        return "idle";
    }

    @Override
    public String getWalkAnimationName() {
        return "walk";
    }

    @Override
    public String getDeathAnimationName() {
        return "death";
    }

    @Override
    public String getSpawnAnimationName() {
        return "idle";
    }

    @Override
    public boolean hasSpawnAnimation() {
        return false;
    }

    @Override
    public String getSkillAnimationName(int attackState) {
        if (attackState == STATE_BREATH) {
            return "attack";
        }
        if (attackState == STATE_LIGHTNING) {
            return "heavy_attack";
        }
        if (attackState == STATE_GUARD) {
            return "guard";
        }
        return "";
    }

    @Override
    public int getDeathAnimationDuration() {
        return DEATH_ANIMATION_TICKS;
    }

    @Override
    protected void onDeathStart() {
        if (!level().isClientSide) {
            level().playSound(null, blockPosition(), SoundEvents.ENDER_DRAGON_DEATH,
                    SoundSource.HOSTILE, 4.0F, 1.0F);
        }
    }

    //死亡动画放完后播放爆炸特效，再走原有的掉落与移除
    @Override
    protected void onDeathEnd() {
        if (level() instanceof ServerLevel serverLevel) {
            float power = (float) TheLastSwordConfiguration.getDragonCultPriestDeathExplosionPowerSafely();
            if (power > 0) {
                double effectY = getY() + getBbHeight() * 0.5;
                serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                        getX(), effectY, getZ(), 1, 0, 0, 0, 0);
                serverLevel.playSound(null, getX(), effectY, getZ(),
                        SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 4.0F,
                        (1.0F + (getRandom().nextFloat() - getRandom().nextFloat()) * 0.2F) * 0.7F);
            }
        }
        super.onDeathEnd();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FLYING_SPEED, 0.25)
                .add(Attributes.MAX_HEALTH, 200)
                .add(Attributes.ARMOR, 0)
                .add(Attributes.ARMOR_TOUGHNESS, 0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0)
                .add(Attributes.ATTACK_DAMAGE, 2)
                .add(Attributes.FOLLOW_RANGE, 32);
    }

    @Override
    public SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_SKELETON_AMBIENT;
    }

    @Override
    public SoundEvent getHurtSound(@NotNull DamageSource ds) {
        return SoundEvents.WITHER_SKELETON_HURT;
    }

    @Override
    public SoundEvent getDeathSound() {
        return SoundEvents.ENDER_DRAGON_DEATH;
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    @Override
    protected float getDamageLimit() {
        return (float) TheLastSwordConfiguration.getDragonCultPriestDamageLimitSafely();
    }

    @Override
    protected int getHurtResistTime() {
        return TheLastSwordConfiguration.getDragonCultPriestHurtResistTimeSafely();
    }

    //免疫龙息，避免被自己与同族的吐息灼伤
    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (source.is(DamageTypes.DRAGON_BREATH)) {
            return false;
        }

        NamedPriestVariant variant = getNamedVariant();
        if (variant == NamedPriestVariant.OTAR) {
            amount *= 0.7F;
        }

        boolean hurt = super.hurt(source, amount);
        if (hurt && variant == NamedPriestVariant.KROSIS && getRandom().nextFloat() < 0.30F) {
            addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60));
        }
        if (hurt && variant == NamedPriestVariant.MIRAAK && source.getEntity() instanceof LivingEntity attacker) {
            tryMiraakConversion(attacker);
        }
        return hurt;
    }

    @Override
    public boolean canBeAffected(@NotNull MobEffectInstance effect) {
        return !(getNamedVariant() == NamedPriestVariant.HEVNORAAK
                && effect.getEffect().getCategory() == MobEffectCategory.HARMFUL)
                && super.canBeAffected(effect);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty,
            MobSpawnType reason, @Nullable SpawnGroupData livingdata, @Nullable CompoundTag tag) {
        SpawnGroupData result = super.finalizeSpawn(world, difficulty, reason, livingdata, tag);

        if (!level().isClientSide) {
            CompoundTag data = getPersistentData();
            if (!data.contains(NAMED_VARIANT_ID_KEY)) {
                int variantId = TheLastSwordConfiguration.getDragonCultPriestNamedEnableSafely()
                        ? NamedPriestVariant.random(getRandom()).getId()
                        : 0;
                data.putInt(NAMED_VARIANT_ID_KEY, variantId);
            }
            syncNamedVariant(true);

            float maxHealth = (float) getAttributeValue(Attributes.MAX_HEALTH);
            setWorldAnchorMax(maxHealth);
            setWorldAnchor(maxHealth);

            if (!EntityUtil.hasProtection(this)) {
                EntityUtil.registerDefence(this, maxHealth);
            }

            setTheLastEndLevel(1);
            setAnimationState(STATE_IDLE);

            EcaAPI.joinFaction(this, DragonCultFaction.ID);

            equipGear();
        }

        return result;
    }

    private void equipGear() {
        setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
        setDropChance(EquipmentSlot.HEAD, 0.0F);

        setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE));
        setDropChance(EquipmentSlot.CHEST, 0.0F);

        setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.GOLDEN_LEGGINGS));
        setDropChance(EquipmentSlot.LEGS, 0.0F);

        setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.GOLDEN_BOOTS));
        setDropChance(EquipmentSlot.FEET, 0.0F);

        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.PRIEST_STAFF.get()));
        setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    @Override
    protected float getStandingEyeHeight(@NotNull Pose pose, @NotNull EntityDimensions dimensions) {
        return dimensions.height * 0.85f;
    }

    @Override
    public void tick() {
        super.tick();
        this.refreshDimensions();
        tickHover();

        if (!level().isClientSide && isAlive()) {
            syncNamedVariant(false);
            tickNamedAbility();
        }
    }

    public NamedPriestVariant getNamedVariant() {
        return NamedPriestVariant.byId(getPersistentData().getInt(NAMED_VARIANT_ID_KEY));
    }

    public double getSkillDamageMultiplier() {
        return getNamedVariant() == NamedPriestVariant.NAHKRIIN ? 1.5 : 1.0;
    }

    public int scaleSkillCooldown(int cooldown) {
        return getNamedVariant() == NamedPriestVariant.MOROKEI
                ? Math.max(1, (int) Math.ceil(cooldown * 0.5))
                : cooldown;
    }

    public void onSkillCast(PriestSkill skill) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        NamedPriestVariant variant = getNamedVariant();
        if (variant == NamedPriestVariant.VOKUN && getRandom().nextFloat() < 0.10F) {
            summonCultist(serverLevel);
        } else if (variant == NamedPriestVariant.KONAHRIK) {
            if (skill == PriestSkill.GUARD && getRandom().nextFloat() < 0.10F) {
                float healed = Math.min(getWorldAnchorMax(), getWorldAnchor() + getWorldAnchorMax() * 0.5F);
                EntityUtil.theLastEndSetHealth(this, healed);
            }
            if (getRandom().nextFloat() < 0.01F) {
                summonPriest(serverLevel);
            }
        }
    }

    public void onSuccessfulAttack(LivingEntity target) {
        if (getNamedVariant() == NamedPriestVariant.MIRAAK) {
            tryMiraakConversion(target);
        }
    }

    @Override
    public void knockback(double strength, double x, double z) {
        if (getNamedVariant() == NamedPriestVariant.RAHGOT) {
            return;
        }

        strength *= 1.0 - getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        if (strength <= 0.0) {
            return;
        }
        Vec3 direction = new Vec3(x, 0.0, z).normalize().scale(strength);
        Vec3 movement = getDeltaMovement();
        setDeltaMovement(movement.x * 0.5 - direction.x,
                onGround() ? Math.min(0.4, movement.y * 0.5 + strength) : movement.y,
                movement.z * 0.5 - direction.z);
        hasImpulse = true;
    }

    @Override
    public void setInvisible(boolean invisible) {
        if (getNamedVariant() == NamedPriestVariant.KROSIS) {
            setSharedFlag(5, invisible);
        }
    }

    private void syncNamedVariant(boolean force) {
        CompoundTag data = getPersistentData();
        int requestedId = data.getInt(NAMED_VARIANT_ID_KEY);
        int appliedId = data.contains(APPLIED_NAMED_VARIANT_ID_KEY)
                ? data.getInt(APPLIED_NAMED_VARIANT_ID_KEY) : Integer.MIN_VALUE;
        if (!force && requestedId == appliedId) {
            return;
        }

        float oldMax = Math.max(1.0F, getWorldAnchorMax());
        float healthRatio = Math.max(0.0F, Math.min(1.0F, getWorldAnchor() / oldMax));
        removeNamedModifier(Attributes.MAX_HEALTH, NAMED_HEALTH_MODIFIER_ID);
        removeNamedModifier(Attributes.ATTACK_DAMAGE, NAMED_ATTACK_MODIFIER_ID);
        removeNamedModifier(Attributes.ARMOR, NAMED_ARMOR_MODIFIER_ID);
        removeNamedModifier(Attributes.ARMOR_TOUGHNESS, NAMED_TOUGHNESS_MODIFIER_ID);

        NamedPriestVariant variant = NamedPriestVariant.byId(requestedId);
        if (variant == NamedPriestVariant.MIRAAK) {
            addNamedModifier(Attributes.MAX_HEALTH, NAMED_HEALTH_MODIFIER_ID, "named_priest_health", 1.0,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            addNamedModifier(Attributes.ATTACK_DAMAGE, NAMED_ATTACK_MODIFIER_ID, "named_priest_attack", 1.0,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            addNamedModifier(Attributes.ARMOR, NAMED_ARMOR_MODIFIER_ID, "named_priest_armor", 1.0,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            AttributeInstance toughness = getAttribute(Attributes.ARMOR_TOUGHNESS);
            if (toughness != null) {
                double amount = toughness.getBaseValue() == 0.0 ? TOUGHNESS_REFERENCE : 1.0;
                AttributeModifier.Operation operation = toughness.getBaseValue() == 0.0
                        ? AttributeModifier.Operation.ADDITION : AttributeModifier.Operation.MULTIPLY_TOTAL;
                toughness.addPermanentModifier(new AttributeModifier(
                        NAMED_TOUGHNESS_MODIFIER_ID, "named_priest_toughness", amount, operation));
            }
        }

        data.putInt(APPLIED_NAMED_VARIANT_ID_KEY, requestedId);
        setCustomName(variant == null ? null : Component.translatable(variant.getTranslationKey()));
        setCustomNameVisible(variant != null);
        if (variant != NamedPriestVariant.KROSIS) {
            setSharedFlag(5, false);
        }

        if (getWorldAnchorMax() > 0.0F) {
            float newMax = (float) getAttributeValue(Attributes.MAX_HEALTH);
            setWorldAnchorMax(newMax);
            setWorldAnchor(newMax * healthRatio);
        }
    }

    private void addNamedModifier(Attribute attribute, UUID id, String name, double amount,
                                  AttributeModifier.Operation operation) {
        AttributeInstance instance = getAttribute(attribute);
        if (instance != null) {
            instance.addPermanentModifier(new AttributeModifier(id, name, amount, operation));
        }
    }

    private void removeNamedModifier(Attribute attribute, UUID id) {
        AttributeInstance instance = getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(id);
        }
    }

    private void tickNamedAbility() {
        NamedPriestVariant variant = getNamedVariant();
        if (variant == NamedPriestVariant.HEVNORAAK && tickCount % 20 == 0) {
            for (MobEffectInstance effect : new ArrayList<>(getActiveEffects())) {
                if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                    removeEffect(effect.getEffect());
                }
            }
        } else if (variant == NamedPriestVariant.VOLSUNG && tickCount % 10 == 0) {
            for (AbstractVillager villager : level().getEntitiesOfClass(AbstractVillager.class,
                    getBoundingBox().inflate(8.0), Entity::isAlive)) {
                villager.getNavigation().moveTo(this, 1.0);
            }
        } else if (variant == NamedPriestVariant.KONAHRIK && tickCount % 20 == 0) {
            createKonahrikBreath();
        }
    }

    private void createKonahrikBreath() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 center = position().add(0.0, getBbHeight() * 0.5, 0.0);
        serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH, center.x, center.y, center.z,
                36, 1.5, 1.5, 1.5, 0.0);
        AABB area = new AABB(center.subtract(1.5, 1.5, 1.5), center.add(1.5, 1.5, 1.5));
        DamageSource source = new DamageSource(
                level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(DamageTypes.DRAGON_BREATH), this, this);
        float damage = (float) (getAttributeValue(Attributes.ATTACK_DAMAGE)
                * TheLastSwordConfiguration.getDragonCultPriestBreathDamageMultiplierSafely());
        for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, area,
                entity -> EntityUtil.canAttack(this, entity))) {
            if (target.hurt(source, damage)) {
                onSuccessfulAttack(target);
            }
        }
    }

    private void tryMiraakConversion(LivingEntity target) {
        if (target == this || !target.isAlive() || getRandom().nextFloat() >= 0.01F) {
            return;
        }
        if (target instanceof Player player) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 2));
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 2));
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 2));
            return;
        }

        boolean tamed = false;
        if (target instanceof TamableAnimal tamable) {
            tamable.setOwnerUUID(getUUID());
            tamable.setTame(true);
            tamed = true;
        } else if (target instanceof AbstractHorse horse) {
            horse.setOwnerUUID(getUUID());
            horse.setTamed(true);
            tamed = true;
        }
        if (!tamed) {
            return;
        }

        String faction = EcaAPI.getEntityFaction(this);
        if (faction != null && !faction.isBlank()) {
            EcaAPI.joinFaction(target, faction);
        }
        if (target instanceof Mob mob) {
            mob.setTarget(null);
        }
    }

    private void summonCultist(ServerLevel serverLevel) {
        DragonCultistEntity cultist = ModEntities.DRAGON_CULTIST.get().create(serverLevel);
        if (cultist != null) {
            placeSummon(cultist, serverLevel);
        }
    }

    private void summonPriest(ServerLevel serverLevel) {
        DragonCultPriestEntity priest = ModEntities.DRAGON_CULT_PRIEST.get().create(serverLevel);
        if (priest != null) {
            placeSummon(priest, serverLevel);
        }
    }

    private void placeSummon(Mob summon, ServerLevel serverLevel) {
        double x = getX() + (getRandom().nextDouble() - 0.5) * 4.0;
        double z = getZ() + (getRandom().nextDouble() - 0.5) * 4.0;
        summon.moveTo(x, getY(), z, getRandom().nextFloat() * 360.0F, 0.0F);
        summon.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(summon.blockPosition()),
                MobSpawnType.MOB_SUMMONED, null, null);
        if (getTarget() != null) {
            summon.setTarget(getTarget());
        }
        serverLevel.addFreshEntity(summon);
    }

    //AI 主动飞行期间暂停悬浮修正，避免两边争抢垂直速度
    public void suppressHover(int ticks) {
        this.hoverSuppressTicks = Math.max(this.hoverSuppressTicks, ticks);
    }

    //维持离地高度，贴地时上浮、悬空过高时缓慢下沉
    private void tickHover() {
        if (level().isClientSide) {
            return;
        }

        //每tick重设，避免存档加载或外部逻辑把无重力状态改回去
        setNoGravity(true);

        if (hoverSuppressTicks > 0) {
            hoverSuppressTicks--;
            return;
        }

        double target = TheLastSwordConfiguration.getDragonCultPriestHoverHeightSafely();
        if (target <= 0) {
            return;
        }

        double clearance = getGroundClearance();
        Vec3 motion = getDeltaMovement();

        if (clearance < target) {
            setDeltaMovement(motion.x, Math.min(motion.y + HOVER_ADJUST_SPEED, HOVER_ADJUST_SPEED), motion.z);
        } else if (clearance > target + HOVER_TOLERANCE) {
            setDeltaMovement(motion.x, Math.max(motion.y - HOVER_ADJUST_SPEED, -HOVER_ADJUST_SPEED), motion.z);
        } else if (Math.abs(motion.y) > 0.01) {
            setDeltaMovement(motion.x, motion.y * 0.5, motion.z);
        }
    }

    //向下探测最近的可站立方块，返回离地高度
    public double getGroundClearance() {
        BlockPos pos = blockPosition();
        for (int i = 0; i < HOVER_SCAN_DEPTH; i++) {
            BlockPos below = pos.below();
            if (!level().getBlockState(below).getCollisionShape(level(), below).isEmpty()) {
                return getY() - below.getY() - 1.0;
            }
            pos = below;
        }
        return HOVER_SCAN_DEPTH;
    }
}
