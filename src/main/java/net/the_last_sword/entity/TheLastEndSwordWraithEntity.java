package net.the_last_sword.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.damagesource.AbsoluteDestructionDamageSource;
import net.the_last_sword.entity.ai.*;
import net.the_last_sword.init.ModEffects;
import net.the_last_sword.util.EntityUtil;
import net.the_last_sword.util.ParticleUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// 终焉剑灵实体
public class TheLastEndSwordWraithEntity extends TheLastEndEntity {

    // 技能状态常量
    public static final int STATE_SWIFT_DASH = 3;
    public static final int STATE_ENCHANT = 4;
    public static final int STATE_DOUBLE_STRIKE = 5;
    public static final int STATE_CROSS_SLASH = 6;
    public static final int STATE_BLOCK = 7;
    public static final int STATE_MOON_LIGHT_STRIKE = 8;
    public static final int STATE_END_OF_ALL_THINGS = 9;

    public static final Map<UUID, Boolean> FORCE_CROSS_SLASH = new ConcurrentHashMap<>();

    public static final EntityDataAccessor<String> TEXTURE =
            SynchedEntityData.defineId(TheLastEndSwordWraithEntity.class, EntityDataSerializers.STRING);

    public final List<LivingEntity> targetList = new ArrayList<>();

    private static final String END_MARK_KEY = "THE_LAST_END";
    public static final int END_MARK_THRESHOLD = 13;

    //增加目标的终焉标记
    public static void addEndMark(LivingEntity target) {
        target.getPersistentData().putInt(END_MARK_KEY, getEndMark(target) + 1);
    }

    //获取目标的终焉标记数量
    public static int getEndMark(LivingEntity target) {
        return target.getPersistentData().getInt(END_MARK_KEY);
    }

    //减少目标的终焉标记
    public static void reduceEndMark(LivingEntity target) {
        int current = getEndMark(target);
        if (current > 0) {
            target.getPersistentData().putInt(END_MARK_KEY, current - 1);
        }
    }

    public TheLastEndSwordWraithEntity(EntityType<? extends TheLastEndSwordWraithEntity> type, Level world) {
        super(type, world);
        xpReward = 50;
        setMaxUpStep(0.6f);
        setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TEXTURE, "the_last_end_sword_wraith");
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.MAX_HEALTH, 200)
                .add(Attributes.ARMOR, 20)
                .add(Attributes.ATTACK_DAMAGE, 20)
                .add(Attributes.FOLLOW_RANGE, 64);
    }

    @Override
    public void setTheLastEndLevel(int level) {
        int oldLevel = getTheLastEndLevel();
        super.setTheLastEndLevel(level);
        int newLevel = getTheLastEndLevel();

        if (newLevel != oldLevel && !level().isClientSide) {
            updateAttributesByLevel(newLevel);
        }
    }

    private void updateAttributesByLevel(int level) {
        double healthPerLevel = (level <= 5)
                ? TheLastSwordConfiguration.getSwordWraithHealthPerLevelSafely()
                : TheLastSwordConfiguration.getSwordWraithHealthPerHighLevelSafely();
        double newMaxHealth = 200.0 + (level * healthPerLevel);

        double attackPerLevel = (level <= 5)
                ? TheLastSwordConfiguration.getSwordWraithAttackPerLevelSafely()
                : TheLastSwordConfiguration.getSwordWraithAttackPerHighLevelSafely();
        double newAttack = 20.0 + (level * attackPerLevel);

        var healthAttr = this.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.setBaseValue(newMaxHealth);
        }

        var attackAttr = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr != null) {
            attackAttr.setBaseValue(newAttack);
        }

        setWorldAnchorMax((float) newMaxHealth);
        setWorldAnchor((float) newMaxHealth);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(1, new SwordWraithSwiftDashGoal(this));
        this.goalSelector.addGoal(2, new SwordWraithCrossSlashGoal(this));
        this.goalSelector.addGoal(3, new SwordWraithEnchantGoal(this));
        this.goalSelector.addGoal(4, new SwordWraithDoubleStrikeGoal(this));
        this.goalSelector.addGoal(5, new SwordWraithBlockGoal(this));
        this.goalSelector.addGoal(6, new SwordWraithMoonLightStrikeGoal(this));
        this.goalSelector.addGoal(7, new SwordWraithEndOfAllThingsGoal(this));

        this.goalSelector.addGoal(8, new SwordWraithChaseTargetGoal(this));
        this.goalSelector.addGoal(9, new SwordWraithFollowOwnerGoal(this));

        this.goalSelector.addGoal(10, new FloatGoal(this));
        this.goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(12, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new SwordWraithOwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new SwordWraithOwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Monster.class, true));
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
        return "death";
    }

    @Override
    public String getSpawnAnimationName() {
        return "spawn";
    }

    @Override
    public String getSkillAnimationName(int attackState) {
        return switch (attackState) {
            case STATE_SWIFT_DASH -> "swift_dash";
            case STATE_ENCHANT -> "enchant";
            case STATE_DOUBLE_STRIKE -> "double_strike";
            case STATE_CROSS_SLASH -> "cross_slash";
            case STATE_BLOCK -> "block";
            case STATE_MOON_LIGHT_STRIKE -> "moon_light_strike";
            case STATE_END_OF_ALL_THINGS -> "end_of_all_things";
            default -> "";
        };
    }

    public void setTexture(String texture) {
        this.entityData.set(TEXTURE, texture);
    }

    public String getTexture() {
        return this.entityData.get(TEXTURE);
    }

    @Override
    public SoundEvent getHurtSound(@NotNull DamageSource ds) {
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
    public boolean canBeAffected(@NotNull MobEffectInstance effect) {
        return effect.getEffect() == ModEffects.VOID_ENCHANTING.get();
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty,
            MobSpawnType reason, @Nullable SpawnGroupData livingdata, @Nullable CompoundTag tag) {
        SpawnGroupData result = super.finalizeSpawn(world, difficulty, reason, livingdata, tag);

        if (!level().isClientSide) {
            if (getTheLastEndLevel() <= 0) {
                setTheLastEndLevel(1);
            }

            float maxHealth = (float) getAttributeValue(Attributes.MAX_HEALTH);
            setWorldAnchorMax(maxHealth);
            setWorldAnchor(maxHealth);

            if (!EntityUtil.hasProtection(this)) {
                EntityUtil.registerDefence(this, maxHealth);
            }

            // 剑灵直接进入IDLE状态
            setAnimationState(STATE_IDLE);
        }

        return result;
    }

    @Override
    protected float getStandingEyeHeight(@NotNull Pose pose, @NotNull EntityDimensions dimensions) {
        return dimensions.height * 0.85f;
    }

    @Override
    public void tick() {
        super.tick();

        AllThingsEndActiveEffect.handleTick(this);

        this.refreshDimensions();
    }

    @Override
    public int getDeathAnimationDuration() {
        return 100;
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("TEXTURE")) {
            this.setTexture(compound.getString("TEXTURE"));
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("TEXTURE", this.getTexture());
    }

    //万物终焉持续效果
    public static class AllThingsEndActiveEffect {
        private static final Map<UUID, Integer> ACTIVE_EFFECTS = new ConcurrentHashMap<>();
        private static final int DURATION = 260;  //13秒
        private static final int SHRINK_START = 240;

        public static void start(TheLastEndSwordWraithEntity wraith) {
            ACTIVE_EFFECTS.put(wraith.getUUID(), 0);
        }

        public static void handleTick(TheLastEndSwordWraithEntity wraith) {
            UUID id = wraith.getUUID();
            Integer currentTick = ACTIVE_EFFECTS.get(id);

            if (currentTick == null) {
                return;
            }

            int newTick = currentTick + 1;

            //每秒执行一次伤害+消耗标记
            if (newTick % 20 == 0) {
                executeAllThingsEndEffect(wraith);
            }

            if (newTick % 4 == 0) {
                spawnAllThingsEndParticles(wraith, newTick);
            }

            if (newTick >= DURATION) {
                end(wraith);
            } else {
                ACTIVE_EFFECTS.put(id, newTick);
            }
        }

        public static void end(TheLastEndSwordWraithEntity wraith) {
            ACTIVE_EFFECTS.remove(wraith.getUUID());
            wraith.setAllThingsEnd(false);
        }

        private static void executeAllThingsEndEffect(TheLastEndSwordWraithEntity wraith) {
            if (wraith.level().isClientSide) {
                return;
            }

            double effectRange = TheLastSwordConfiguration.getSkillEndOfAllThingsRangeSafely();
            List<LivingEntity> targets = EntityUtil.getTargetsInSphere(wraith, effectRange);

            float attackDamage = (float) wraith.getAttributeValue(Attributes.ATTACK_DAMAGE);

            for (LivingEntity target : targets) {
                int markCount = getEndMark(target);
                float maxHealthDamage = (float) ((markCount / 100.0 + 0.13) * target.getMaxHealth());
                float totalDamage = attackDamage + maxHealthDamage;

                target.invulnerableTime = 0;
                AbsoluteDestructionDamageSource.applyAbsoluteDestructionIntelligently(target, wraith, totalDamage);

                reduceEndMark(target);
                clearPositiveEffects(target);
            }
        }

        private static void clearPositiveEffects(LivingEntity target) {
            var activeEffects = new ArrayList<>(target.getActiveEffects());
            for (MobEffectInstance effect : activeEffects) {
                if (effect.getEffect().isBeneficial()) {
                    target.removeEffect(effect.getEffect());
                }
            }
        }

        private static void spawnAllThingsEndParticles(TheLastEndSwordWraithEntity wraith, int currentTick) {
            double radius = TheLastSwordConfiguration.getSkillEndOfAllThingsRangeSafely();
            Vec3 centerPos = wraith.position();

            if (currentTick < SHRINK_START) {
                ParticleUtil.spawnAllThingsEndCircles(wraith.level(), centerPos, radius);
                ParticleUtil.spawnAllThingsEndCenterParticles(wraith.level(), centerPos, false);
            } else {
                int shrinkTick = currentTick - SHRINK_START;
                double shrinkProgress = shrinkTick / 20.0;
                ParticleUtil.spawnAllThingsEndShrinkingCircles(wraith.level(), centerPos, radius, shrinkProgress);
                ParticleUtil.spawnAllThingsEndCenterParticles(wraith.level(), centerPos, true);
            }
        }
    }
}
