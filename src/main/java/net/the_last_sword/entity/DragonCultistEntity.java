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
import net.the_last_sword.entity.ai.DragonCultistMagicGoal;
import net.the_last_sword.entity.ai.DragonCultistMeleeAttackGoal;
import net.the_last_sword.faction.DragonCultFaction;
import net.the_last_sword.util.EntityUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// 拜龙教教徒，黑色皮革甲+铁剑，近战(<8格)和远程魔法(>=8格)两种攻击模式
public class DragonCultistEntity extends TheLastEndEntity {

    public static final int STATE_ATTACK = 3;
    public static final int STATE_MAGIC = 4;

    public DragonCultistEntity(EntityType<? extends DragonCultistEntity> type, Level world) {
        super(type, world);
        setMaxUpStep(0.6f);
        xpReward = 10;
        setPersistenceRequired();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(1, new DragonCultistMagicGoal(this));
        this.goalSelector.addGoal(2, new DragonCultistMeleeAttackGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(4, new FloatGoal(this));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this)
                .setAlertOthers(DragonCultistEntity.class, DragonCultPaladinEntity.class));
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
        if (attackState == STATE_ATTACK) {
            return "attack";
        }
        if (attackState == STATE_MAGIC) {
            return "magic";
        }
        return "";
    }

    @Override
    public int getDeathAnimationDuration() {
        return 31;
    }


    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.MAX_HEALTH, 40)
                .add(Attributes.ARMOR, 0)
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
            setAnimationState(STATE_IDLE);

            EcaAPI.joinFaction(this, DragonCultFaction.ID);

            equipGear();
        }

        return result;
    }

    private void equipGear() {
        ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
        helmet.getOrCreateTag().putInt("color", 0x1A1A1A);
        setItemSlot(EquipmentSlot.HEAD, helmet);
        setDropChance(EquipmentSlot.HEAD, 0.0F);

        ItemStack chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        chestplate.getOrCreateTag().putInt("color", 0x1A1A1A);
        setItemSlot(EquipmentSlot.CHEST, chestplate);
        setDropChance(EquipmentSlot.CHEST, 0.0F);

        ItemStack leggings = new ItemStack(Items.LEATHER_LEGGINGS);
        leggings.getOrCreateTag().putInt("color", 0x1A1A1A);
        setItemSlot(EquipmentSlot.LEGS, leggings);
        setDropChance(EquipmentSlot.LEGS, 0.0F);

        ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);
        boots.getOrCreateTag().putInt("color", 0x1A1A1A);
        setItemSlot(EquipmentSlot.FEET, boots);
        setDropChance(EquipmentSlot.FEET, 0.0F);

        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        setItemSlot(EquipmentSlot.MAINHAND, sword);
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
