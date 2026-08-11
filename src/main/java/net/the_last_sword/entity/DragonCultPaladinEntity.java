package net.the_last_sword.entity;

import net.eca.api.EcaAPI;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.ai.DragonCultPaladinAttackGoal;
import net.the_last_sword.entity.ai.DragonCultPaladinBlockGoal;
import net.the_last_sword.entity.ai.DragonCultPaladinHeavyAttackGoal;
import net.the_last_sword.faction.DragonCultFaction;
import net.the_last_sword.init.ModItems;
import net.the_last_sword.util.EntityUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DragonCultPaladinEntity extends TheLastEndEntity {

    public static final int STATE_ATTACK = 3;
    public static final int STATE_BLOCK = 4;
    public static final int STATE_HEAVY_ATTACK = 5;

    //格挡免伤窗口，由格挡Goal开关
    private boolean blockImmune;
    private long lastBlockSoundTick = Long.MIN_VALUE;

    public DragonCultPaladinEntity(EntityType<? extends DragonCultPaladinEntity> type, Level world) {
        super(type, world);
        setMaxUpStep(0.6f);
        xpReward = 100;
        setPersistenceRequired();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(1, new DragonCultPaladinBlockGoal(this));
        this.goalSelector.addGoal(2, new DragonCultPaladinHeavyAttackGoal(this));
        //普攻Goal同时负责接近目标
        this.goalSelector.addGoal(3, new DragonCultPaladinAttackGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(5, new FloatGoal(this));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this)
                .setAlertOthers(DragonCultPaladinEntity.class, DragonCultistEntity.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    @Override
    public String getIdleAnimationName() {
        return "animation.idle";
    }

    @Override
    public String getWalkAnimationName() {
        return "animation.walk";
    }

    @Override
    public String getDeathAnimationName() {
        return "animation.death";
    }

    @Override
    public String getSpawnAnimationName() {
        return "animation.idle";
    }

    @Override
    public boolean hasSpawnAnimation() {
        return false;
    }

    @Override
    public String getSkillAnimationName(int attackState) {
        if (attackState == STATE_ATTACK) {
            return "animation.attack";
        }
        if (attackState == STATE_BLOCK) {
            return "animation.block";
        }
        if (attackState == STATE_HEAVY_ATTACK) {
            return "animation.heavy_attack";
        }
        return "";
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.MAX_HEALTH, 100)
                .add(Attributes.ARMOR, 0)
                .add(Attributes.ARMOR_TOUGHNESS, 2)
                .add(Attributes.ATTACK_DAMAGE, 2)
                .add(Attributes.FOLLOW_RANGE, 32);
    }

    @Override
    public SoundEvent getAmbientSound() {
        return SoundEvents.EVOKER_AMBIENT;
    }

    @Override
    public SoundEvent getHurtSound(@NotNull DamageSource ds) {
        return SoundEvents.EVOKER_HURT;
    }

    @Override
    public SoundEvent getDeathSound() {
        return SoundEvents.EVOKER_DEATH;
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    public boolean isBlockImmune() {
        return blockImmune;
    }

    public void setBlockImmune(boolean immune) {
        this.blockImmune = immune;
    }

    //格挡免伤窗口内完全不受伤
    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (blockImmune) {
            long gameTime = level().getGameTime();
            if (!level().isClientSide && gameTime != lastBlockSoundTick) {
                level().playSound(null, blockPosition(), SoundEvents.SHIELD_BLOCK,
                    getSoundSource(), 1.0F, 0.8F + getRandom().nextFloat() * 0.4F);
                lastBlockSoundTick = gameTime;
            }
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    protected float getDamageLimit() {
        return (float) TheLastSwordConfiguration.getDragonCultPaladinDamageLimitSafely();
    }

    @Override
    protected int getHurtResistTime() {
        return TheLastSwordConfiguration.getDragonCultPaladinHurtResistTimeSafely();
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
            setAnimationState(STATE_IDLE);

            EcaAPI.joinFaction(this, DragonCultFaction.ID);

            equipGear();
        }

        return result;
    }

    private void equipGear() {
        setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        setDropChance(EquipmentSlot.HEAD, 0.0F);

        setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        setDropChance(EquipmentSlot.CHEST, 0.0F);

        setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
        setDropChance(EquipmentSlot.LEGS, 0.0F);

        setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
        setDropChance(EquipmentSlot.FEET, 0.0F);

        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.KNIGHT_GREATSWORD.get()));
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
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
    }
}
