package net.the_last_sword.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.the_last_sword.util.EntityUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// 女皇的逝去之影
public class ThePastShadowOfTheQueenEntity extends TheLastEndEntity {

    public ThePastShadowOfTheQueenEntity(EntityType<? extends ThePastShadowOfTheQueenEntity> type, Level level) {
        super(type, level);
        xpReward = 100;
        setMaxUpStep(0.6f);
        setPersistenceRequired();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.MAX_HEALTH, 20000)
                .add(Attributes.ARMOR, 0)
                .add(Attributes.ARMOR_TOUGHNESS, 0)
                .add(Attributes.ATTACK_DAMAGE, 200)
                .add(Attributes.FOLLOW_RANGE, 64);
    }

    @Override
    public final void setTheLastEndLevel(int level) {
        super.setTheLastEndLevel(11);
    }

    @Override
    public String getIdleAnimationName() {
        return "idle";
    }

    @Override
    public String getWalkAnimationName() {
        return "idle";
    }

    @Override
    public String getDeathAnimationName() {
        return "idle";
    }

    @Override
    public String getSpawnAnimationName() {
        return "spawn";
    }

    @Override
    public String getSkillAnimationName(int attackState) {
        return "";
    }

    @Override
    public SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.ENDER_DRAGON_HURT;
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
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty,
            MobSpawnType reason, @Nullable SpawnGroupData livingData, @Nullable CompoundTag tag) {
        SpawnGroupData result = super.finalizeSpawn(world, difficulty, reason, livingData, tag);

        if (!level().isClientSide) {
            setTheLastEndLevel(11);

            float maxHealth = (float) getAttributeValue(Attributes.MAX_HEALTH);
            setWorldAnchorMax(maxHealth);
            setWorldAnchor(maxHealth);

            if (!EntityUtil.hasProtection(this)) {
                EntityUtil.registerDefence(this, maxHealth);
            }

            setAnimationState(STATE_SPAWNING);
        }

        return result;
    }

    @Override
    protected void onSpawningTick() {
        this.refreshDimensions();
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
}
