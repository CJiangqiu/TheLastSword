package net.the_last_sword.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.util.EntityUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

// 终焉种基类
public abstract class TheLastEndEntity extends TamableAnimal implements GeoEntity {

    // 状态常量
    public static final int STATE_DEATH = -1;
    public static final int STATE_UNSPAWNED = 0;
    public static final int STATE_SPAWNING = 1;
    public static final int STATE_IDLE = 2;

    // 同步字段
    private static final EntityDataAccessor<Integer> ANIMATION_STATE =
            SynchedEntityData.defineId(TheLastEndEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> HURT_RESIST_TICK =
            SynchedEntityData.defineId(TheLastEndEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> LEVEL =
            SynchedEntityData.defineId(TheLastEndEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> DEATH_TICK =
            SynchedEntityData.defineId(TheLastEndEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> ALL_THINGS_END =
            SynchedEntityData.defineId(TheLastEndEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected TheLastEndEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ANIMATION_STATE, STATE_UNSPAWNED);
        this.entityData.define(HURT_RESIST_TICK, 0);
        this.entityData.define(LEVEL, 1);
        this.entityData.define(DEATH_TICK, 0);
        this.entityData.define(ALL_THINGS_END, false);
    }

    // 状态访问器
    public int getAnimationState() {
        return this.entityData.get(ANIMATION_STATE);
    }

    public void setAnimationState(int state) {
        this.entityData.set(ANIMATION_STATE, state);
    }

    public boolean isReady() {
        return getAnimationState() >= STATE_IDLE;
    }

    public boolean isDying() {
        return getAnimationState() == STATE_DEATH;
    }

    public boolean canAct() {
        return getAnimationState() >= STATE_IDLE;
    }

    // 无敌帧
    public int getHurtResistTick() {
        return this.entityData.get(HURT_RESIST_TICK);
    }

    public void setHurtResistTick(int tick) {
        this.entityData.set(HURT_RESIST_TICK, tick);
    }

    protected int getHurtResistTime() {
        return 20;
    }

    // 等级
    public int getTheLastEndLevel() {
        return this.entityData.get(LEVEL);
    }

    public void setTheLastEndLevel(int level) {
        this.entityData.set(LEVEL, level);
    }

    // 死亡计时
    public int getDeathTick() {
        return this.entityData.get(DEATH_TICK);
    }

    public void setDeathTick(int tick) {
        this.entityData.set(DEATH_TICK, tick);
    }

    // 万物终焉
    public boolean isAllThingsEnd() {
        return this.entityData.get(ALL_THINGS_END);
    }

    public void setAllThingsEnd(boolean value) {
        this.entityData.set(ALL_THINGS_END, value);
    }

    // 自定义血量
    public float getWorldAnchor() {
        return EntityUtil.getWorldAnchor(this);
    }

    public void setWorldAnchor(float health) {
        EntityUtil.setWorldAnchor(this, health);
    }

    public float getWorldAnchorMax() {
        return EntityUtil.getWorldAnchorMax(this);
    }

    public void setWorldAnchorMax(float maxHealth) {
        EntityUtil.setWorldAnchorMax(this, maxHealth);
    }

    protected float getDamageLimit() {
        float maxHealth = (float) this.getAttributeValue(Attributes.MAX_HEALTH);
        float ratio = (float) TheLastSwordConfiguration.getDefenceCustomHealthDamageReductionSafely();
        float maxDamage = (float) TheLastSwordConfiguration.getDefenceMaxDamagePerHitSafely();
        return Math.min(maxHealth * ratio, maxDamage);
    }

    // 动画系统
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 0, this::animationPredicate));
    }

    private PlayState animationPredicate(AnimationState<TheLastEndEntity> state) {
        int attackState = getAnimationState();

        if (attackState == STATE_UNSPAWNED) {
            String waitAnim = getWaitAnimationName();
            if (waitAnim != null && !waitAnim.isEmpty()) {
                state.setAnimation(RawAnimation.begin().thenLoop(waitAnim));
                return PlayState.CONTINUE;
            }
            return PlayState.STOP;
        }

        if (attackState == STATE_IDLE) {
            if (state.isMoving()) {
                state.setAnimation(RawAnimation.begin().thenLoop(getWalkAnimationName()));
            } else {
                state.setAnimation(RawAnimation.begin().thenLoop(getIdleAnimationName()));
            }
            return PlayState.CONTINUE;
        }

        if (attackState == STATE_SPAWNING) {
            state.setAnimation(RawAnimation.begin().thenPlay(getSpawnAnimationName()));
            return PlayState.CONTINUE;
        }

        if (attackState == STATE_DEATH) {
            state.setAnimation(RawAnimation.begin().thenPlay(getDeathAnimationName()));
            return PlayState.CONTINUE;
        }

        // 技能状态
        String skillAnim = getSkillAnimationName(attackState);
        if (skillAnim != null && !skillAnim.isEmpty()) {
            state.setAnimation(RawAnimation.begin().thenPlay(skillAnim));
            return PlayState.CONTINUE;
        }

        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // 子类必须实现的动画方法
    public abstract String getIdleAnimationName();
    public abstract String getWalkAnimationName();
    public abstract String getDeathAnimationName();
    public abstract String getSpawnAnimationName();

    public String getWaitAnimationName() {
        return "";
    }

    //是否有生成动画，无生成动画的实体召唤时直接进入 IDLE
    public boolean hasSpawnAnimation() {
        return true;
    }

    public abstract String getSkillAnimationName(int attackState);

    public int getDeathAnimationDuration() {
        return 20;
    }

    public int getSpawnAnimationDuration() {
        return 60;
    }

    // Tick逻辑
    @Override
    public void tick() {
        // 首次tick初始化防御系统
        if (!level().isClientSide && tickCount == 1) {
            if (!EntityUtil.hasProtection(this)) {
                float maxHealth = (float) this.getAttributeValue(Attributes.MAX_HEALTH);
                EntityUtil.registerDefence(this, maxHealth);
            }
        }

        // 无效目标清理
        if (!level().isClientSide) {
            LivingEntity target = getTarget();
            if (!EntityUtil.canAttack(this, target)) {
                setTarget(null);
            }
        }

        // 死亡状态
        if (isDying()) {
            handleDeathTick();
            return;
        }

        // 未生成状态
        if (getAnimationState() == STATE_UNSPAWNED) {
            onUnspawnedTick();
            return;
        }

        // 生成动画状态
        if (getAnimationState() == STATE_SPAWNING) {
            super.baseTick();
            onSpawningTick();
            return;
        }

        // 正常状态
        super.tick();

        // 无敌帧递减
        if (getHurtResistTick() > 0) {
            setHurtResistTick(getHurtResistTick() - 1);
        }
    }

    protected void onUnspawnedTick() {
    }

    protected void onSpawningTick() {
    }

    private void handleDeathTick() {
        int deathTick = getDeathTick();

        if (deathTick == 0) {
            onDeathStart();
        }

        setDeathTick(deathTick + 1);

        if (deathTick >= getDeathAnimationDuration()) {
            onDeathEnd();
        }
    }

    protected void onDeathStart() {
    }

    protected void onDeathEnd() {
        if (!level().isClientSide) {
            this.dropAllDeathLoot(damageSources().generic());
        }
        safeRemove();
    }

    public void triggerDeath() {
        if (!isDying()) {
            setAnimationState(STATE_DEATH);
            setDeathTick(0);
        }
    }

    public void safeRemove() {
        setWorldAnchor(0);
        EntityUtil.clearDefence(this);
        EntityUtil.theLastEndRemove(this, RemovalReason.KILLED);
    }

    // 伤害处理
    @Override
    public void actuallyHurt(@NotNull DamageSource damageSource, float damageAmount) {
        if (getHurtResistTick() > 0) {
            return;
        }

        float damageLimit = getDamageLimit();
        int level = getTheLastEndLevel();
        float realDamage;

        if (level <= 5) {
            realDamage = Math.min(damageAmount, damageLimit);
        } else {
            if (damageAmount > damageLimit) {
                return;
            }
            realDamage = damageAmount;
        }

        if (realDamage > 0) {
            float currentHealth = getWorldAnchor();
            float newHealth = currentHealth - realDamage;
            setWorldAnchor(newHealth);

            if (newHealth <= 0 && !isDying()) {
                triggerDeath();
            }
        }

        int hurtTime = getHurtResistTime();
        setHurtResistTick(hurtTime);
        this.invulnerableTime = hurtTime;
    }

    // 死亡相关覆写
    @Override
    public void die(@NotNull DamageSource damageSource) {
        if (isDying()) {
            super.die(damageSource);
        }
    }

    @Override
    protected void tickDeath() {
    }

    @Override
    public boolean isDeadOrDying() {
        return isDying();
    }

    @Override
    public boolean isAlive() {
        return !isDying();
    }

    @Override
    public void kill() {
        if (isDying()) {
            super.kill();
        }
    }

    @Override
    public void remove(@NotNull RemovalReason reason) {
        if (level() instanceof ServerLevel && !isDying()) {
            return;
        }
        super.remove(reason);
    }

    @Override
    public void setRemoved(@NotNull RemovalReason reason) {
        if (level() instanceof ServerLevel && !isDying()) {
            return;
        }
        super.setRemoved(reason);
    }

    // NBT
    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("AnimationState")) {
            setAnimationState(tag.getInt("AnimationState"));
        }
        if (tag.contains("Level")) {
            setTheLastEndLevel(tag.getInt("Level"));
        }
        if (tag.contains("AllThingsEnd")) {
            setAllThingsEnd(tag.getBoolean("AllThingsEnd"));
        }

        if (!level().isClientSide) {
            float maxHealth = (float) getAttributeValue(Attributes.MAX_HEALTH);
            if (!EntityUtil.hasProtection(this)) {
                EntityUtil.registerDefence(this, maxHealth);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("AnimationState", getAnimationState());
        tag.putInt("Level", getTheLastEndLevel());
        tag.putBoolean("AllThingsEnd", isAllThingsEnd());
    }

    // 终焉种特性
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected boolean isAlwaysExperienceDropper() {
        return true;
    }

    @Override
    public boolean startRiding(@NotNull Entity entity) {
        return false;
    }

    @Override
    public void teleportTo(double x, double y, double z) {
    }

    @Override
    public void knockback(double strength, double x, double z) {
    }

    @Override
    public boolean canStandOnFluid(FluidState fluidState) {
        return fluidState.is(FluidTags.WATER);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public void setInvisible(boolean invisible) {
    }

    @Override
    public boolean isInvisible() {
        return false;
    }

    @Override
    public boolean isNoAi() {
        return false;
    }

    @Override
    public void setNoAi(boolean noAi) {
    }

    @Override
    public boolean canBreed() {
        return false;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        return null;
    }

    @Override
    public int getAge() {
        return 0;
    }

    @Override
    public void setAge(int age) {
    }
}
