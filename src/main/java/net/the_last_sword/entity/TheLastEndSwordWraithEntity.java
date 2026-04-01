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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.eca.api.EcaAPI;
import net.eca.network.EntityExtensionOverridePacket.SkyboxData;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.damagesource.AbsoluteDestructionDamageSource;
import net.the_last_sword.entity.ai.*;
import net.the_last_sword.init.ModEffects;
import net.the_last_sword.util.EntityUtil;
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

    private int spawnTick = 0;

    public static final int END_MARK_THRESHOLD = 13;
    private int endMarkCount = 0;

    //增加自身终焉标记
    public void addEndMark() {
        endMarkCount++;
    }

    //获取自身终焉标记
    public int getEndMark() {
        return endMarkCount;
    }

    //消耗自身终焉标记
    public void reduceEndMark() {
        if (endMarkCount > 0) {
            endMarkCount--;
        }
    }

    //重置终焉标记
    public void resetEndMark() {
        endMarkCount = 0;
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

        this.goalSelector.addGoal(1, new SwordWraithEnchantGoal(this));
        this.goalSelector.addGoal(2, new SwordWraithDoubleStrikeGoal(this));
        this.goalSelector.addGoal(2, new SwordWraithSwiftDashGoal(this));
        this.goalSelector.addGoal(2, new SwordWraithBlockGoal(this));
        this.goalSelector.addGoal(3, new SwordWraithCrossSlashGoal(this));
        this.goalSelector.addGoal(3, new SwordWraithMoonLightStrikeGoal(this));
        this.goalSelector.addGoal(4, new SwordWraithEndOfAllThingsGoal(this));

        this.goalSelector.addGoal(5, new SwordWraithChaseTargetGoal(this));
        this.goalSelector.addGoal(6, new SwordWraithFollowOwnerGoal(this));

        this.goalSelector.addGoal(7, new FloatGoal(this));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

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

            setAnimationState(STATE_SPAWNING);
            spawnTick = 0;
        }

        return result;
    }

    @Override
    protected float getStandingEyeHeight(@NotNull Pose pose, @NotNull EntityDimensions dimensions) {
        return dimensions.height * 0.85f;
    }

    @Override
    public int getSpawnAnimationDuration() {
        return 110;
    }

    @Override
    protected void onSpawningTick() {
        spawnTick++;
        if (spawnTick >= getSpawnAnimationDuration()) {
            setAnimationState(STATE_IDLE);
            spawnTick = 0;
        }
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

        private static final ResourceLocation THE_LAST_END_PRESET = new ResourceLocation("eca", "the_last_end");

        public static void start(TheLastEndSwordWraithEntity wraith) {
            ACTIVE_EFFECTS.put(wraith.getUUID(), 0);
            if (wraith.level() instanceof ServerLevel serverLevel) {
                SkyboxData skyboxData = new SkyboxData(
                        false, null,
                        true, THE_LAST_END_PRESET,
                        0.6f, 100.0f, 16.0f,
                        1.0f, 1.0f, 1.0f
                );
                EcaAPI.setGlobalSkybox(serverLevel, skyboxData);
            }
        }

        public static void handleTick(TheLastEndSwordWraithEntity wraith) {
            UUID id = wraith.getUUID();
            Integer currentTick = ACTIVE_EFFECTS.get(id);

            if (currentTick == null) {
                //存档恢复后状态残留，强制结束
                if (wraith.isAllThingsEnd()) {
                    end(wraith);
                }
                return;
            }

            int newTick = currentTick + 1;

            //每秒执行一次伤害+消耗标记
            if (newTick % 20 == 0) {
                executeAllThingsEndEffect(wraith);
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

            //终焉审判：生命值 > 50%最大生命值的目标直接击杀
            if (!wraith.level().isClientSide) {
                double effectRange = TheLastSwordConfiguration.getSkillEndOfAllThingsRangeSafely();
                List<LivingEntity> targets = EntityUtil.getTargetsInSphere(wraith, effectRange);
                for (LivingEntity target : targets) {
                    float health = EntityUtil.hasProtection(target)
                            ? EntityUtil.getWorldAnchor(target)
                            : target.getHealth();
                    float maxHealth = target.getMaxHealth();
                    if (health > maxHealth * 0.5f) {
                        AbsoluteDestructionDamageSource.applyAbsoluteDestruction(target, wraith, Float.MAX_VALUE);
                    }
                }
            }

            //重置标记
            wraith.resetEndMark();

            if (wraith.level() instanceof ServerLevel serverLevel) {
                EcaAPI.clearGlobalSkybox(serverLevel);
            }
        }

        private static void executeAllThingsEndEffect(TheLastEndSwordWraithEntity wraith) {
            if (wraith.level().isClientSide) {
                return;
            }

            //每秒消耗一层标记
            int currentMark = wraith.getEndMark();
            wraith.reduceEndMark();

            double effectRange = TheLastSwordConfiguration.getSkillEndOfAllThingsRangeSafely();
            List<LivingEntity> targets = EntityUtil.getTargetsInSphere(wraith, effectRange);

            for (LivingEntity target : targets) {
                //伤害 = 目标最大生命值 × (13% + 剩余标记%)
                float maxHealthDamage = (float) ((0.13 + currentMark / 100.0) * target.getMaxHealth());

                target.invulnerableTime = 0;
                AbsoluteDestructionDamageSource.applyAbsoluteDestruction(target, wraith, maxHealthDamage);
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

    }
}
