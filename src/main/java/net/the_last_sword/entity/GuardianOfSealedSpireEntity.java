package net.the_last_sword.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.ai.GuardianAssistAllyTargetGoal;
import net.the_last_sword.entity.ai.GuardianChaseTargetGoal;
import net.the_last_sword.entity.ai.GuardianMeleeAttackGoal;
import net.the_last_sword.entity.ai.GuardianPickupWeaponGoal;
import net.the_last_sword.init.ModEntities;
import net.the_last_sword.util.EntityUtil;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// 封印尖塔守卫
public class GuardianOfSealedSpireEntity extends TheLastEndEntity {

    // 技能状态常量
    public static final int STATE_ATTACK = 3;

    // 守卫变种类型
    public enum GuardianType {
        SABER,
        ARCHER,
        BERSERKER
    }

    private static final EntityDataAccessor<Integer> GUARDIAN_TYPE =
            SynchedEntityData.defineId(GuardianOfSealedSpireEntity.class, EntityDataSerializers.INT);

    public GuardianOfSealedSpireEntity(EntityType<? extends GuardianOfSealedSpireEntity> type, Level world) {
        super(type, world);
        setMaxUpStep(0.6f);
        xpReward = 10;
        setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(GUARDIAN_TYPE, 0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(1, new GuardianPickupWeaponGoal(this));
        this.goalSelector.addGoal(2, new GuardianMeleeAttackGoal(this));
        this.goalSelector.addGoal(3, new GuardianChaseTargetGoal(this));
        this.goalSelector.addGoal(5, new FloatGoal(this));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(GuardianOfSealedSpireEntity.class));
        this.targetSelector.addGoal(2, new GuardianAssistAllyTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LostWraithEntity.class, true));
    }

    public GuardianType getGuardianType() {
        int typeId = this.entityData.get(GUARDIAN_TYPE);
        return GuardianType.values()[Math.min(typeId, GuardianType.values().length - 1)];
    }

    public void setGuardianType(GuardianType type) {
        this.entityData.set(GUARDIAN_TYPE, type.ordinal());
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
        return "spawn";
    }

    @Override
    public String getSkillAnimationName(int attackState) {
        if (attackState == STATE_ATTACK) {
            return "attack";
        }
        return "";
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.MAX_HEALTH, 50)
                .add(Attributes.ARMOR, 0)
                .add(Attributes.ATTACK_DAMAGE, 2)
                .add(Attributes.FOLLOW_RANGE, 32);
    }

    @Override
    public SoundEvent getAmbientSound() {
        return SoundEvents.SKELETON_AMBIENT;
    }

    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return SoundEvents.SKELETON_HURT;
    }

    @Override
    public SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_DEATH;
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    @Override
    protected float getDamageLimit() {
        return (float) TheLastSwordConfiguration.getGuardianDamageLimitSafely();
    }

    @Override
    protected int getHurtResistTime() {
        return TheLastSwordConfiguration.getGuardianHurtResistTimeSafely();
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty,
            MobSpawnType reason, @Nullable SpawnGroupData livingdata, @Nullable CompoundTag tag) {
        SpawnGroupData result = super.finalizeSpawn(world, difficulty, reason, livingdata, tag);

        if (!level().isClientSide) {
            float maxHealth = (float) getAttributeValue(Attributes.MAX_HEALTH);
            setWorldAnchorMax(maxHealth);
            setWorldAnchor(maxHealth);

            if (!EntityUtil.hasProtection(this)) {
                EntityUtil.registerDefence(this, maxHealth);
            }

            setTheLastEndLevel(1);

            // 守卫没有生成动画，直接进入IDLE状态
            setAnimationState(STATE_IDLE);

            equipNetheriteArmor();
        }

        return result;
    }

    private void equipNetheriteArmor() {
        ItemStack helmet = new ItemStack(Items.NETHERITE_HELMET);
        helmet.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        helmet.enchant(Enchantments.THORNS, 3);
        helmet.enchant(Enchantments.UNBREAKING, 3);
        helmet.enchant(Enchantments.MENDING, 1);
        helmet.enchant(Enchantments.RESPIRATION, 3);
        helmet.enchant(Enchantments.AQUA_AFFINITY, 1);
        helmet.enchant(Enchantments.PROJECTILE_PROTECTION, 4);
        setItemSlot(EquipmentSlot.HEAD, helmet);
        setDropChance(EquipmentSlot.HEAD, 2.0F);

        ItemStack chestplate = new ItemStack(Items.NETHERITE_CHESTPLATE);
        chestplate.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        chestplate.enchant(Enchantments.THORNS, 3);
        chestplate.enchant(Enchantments.UNBREAKING, 3);
        chestplate.enchant(Enchantments.MENDING, 1);
        chestplate.enchant(Enchantments.BLAST_PROTECTION, 4);
        setItemSlot(EquipmentSlot.CHEST, chestplate);
        setDropChance(EquipmentSlot.CHEST, 2.0F);

        ItemStack leggings = new ItemStack(Items.NETHERITE_LEGGINGS);
        leggings.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        leggings.enchant(Enchantments.THORNS, 3);
        leggings.enchant(Enchantments.UNBREAKING, 3);
        leggings.enchant(Enchantments.MENDING, 1);
        leggings.enchant(Enchantments.FIRE_PROTECTION, 4);
        leggings.enchant(Enchantments.SWIFT_SNEAK, 3);
        setItemSlot(EquipmentSlot.LEGS, leggings);
        setDropChance(EquipmentSlot.LEGS, 2.0F);

        ItemStack boots = new ItemStack(Items.NETHERITE_BOOTS);
        boots.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        boots.enchant(Enchantments.THORNS, 3);
        boots.enchant(Enchantments.UNBREAKING, 3);
        boots.enchant(Enchantments.MENDING, 1);
        boots.enchant(Enchantments.DEPTH_STRIDER, 3);
        boots.enchant(Enchantments.FALL_PROTECTION, 4);
        boots.enchant(Enchantments.SOUL_SPEED, 3);
        setItemSlot(EquipmentSlot.FEET, boots);
        setDropChance(EquipmentSlot.FEET, 2.0F);
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return dimensions.height * 0.85f;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("GuardianType")) {
            int typeId = tag.getInt("GuardianType");
            this.entityData.set(GUARDIAN_TYPE, typeId);
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("GuardianType", this.entityData.get(GUARDIAN_TYPE));
    }

    @Override
    public void tick() {
        super.tick();

        if (tickCount % 20 == 0) {
            checkAndTransform();
        }

        this.refreshDimensions();
    }

    protected void checkAndTransform() {
        if (this.getClass() != GuardianOfSealedSpireEntity.class) {
            return;
        }

        ItemStack mainhand = getMainHandItem();
        if (mainhand.isEmpty()) return;

        GuardianType targetType = getTypeFromItem(mainhand);
        if (targetType == null) return;

        transformTo(targetType);
    }

    private GuardianType getTypeFromItem(ItemStack stack) {
        if (stack.getItem() instanceof SwordItem) {
            return GuardianType.SABER;
        } else if (stack.getItem() instanceof BowItem) {
            return GuardianType.ARCHER;
        } else if (stack.getItem() instanceof AxeItem) {
            return GuardianType.BERSERKER;
        }
        return null;
    }

    private void transformTo(GuardianType type) {
        if (level().isClientSide) {
            return;
        }

        GuardianOfSealedSpireEntity newEntity = null;
        switch (type) {
            case SABER:
                newEntity = new GuardianSaberEntity(ModEntities.GUARDIAN_SABER.get(), level());
                break;
            case ARCHER:
                newEntity = new GuardianArcherEntity(ModEntities.GUARDIAN_ARCHER.get(), level());
                break;
            case BERSERKER:
                newEntity = new GuardianBerserkerEntity(ModEntities.GUARDIAN_BERSERKER.get(), level());
                break;
        }

        if (newEntity == null) {
            return;
        }

        newEntity.moveTo(getX(), getY(), getZ(), getYRot(), getXRot());

        if (level() instanceof ServerLevel serverLevel) {
            newEntity.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(blockPosition()),
                MobSpawnType.CONVERSION, null, null);
        }

        if (getTarget() != null) {
            newEntity.setTarget(getTarget());
        }

        setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);

        level().addFreshEntity(newEntity);

        this.safeRemove();
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (source.getEntity() instanceof GuardianOfSealedSpireEntity) {
            return false;
        }
        return super.hurt(source, amount);
    }
}
