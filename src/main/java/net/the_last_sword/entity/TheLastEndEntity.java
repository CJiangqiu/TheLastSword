package net.the_last_sword.entity;

import net.eca.api.EcaAPI;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.util.EntityUtil;
import net.the_last_sword.util.damage.AdvancedEquipmentDamageHandler;
import net.the_last_sword.util.damage.AdvancedEquipmentDamageHandler.DamageResult;
import net.the_last_sword.util.health.TrueHealthManager;
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

    //达到该终焉等级后交由 ECA 线程复活守护，只要保护性真实血量未清零就常驻
    public static final int RESURRECTION_LEVEL = 13;

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

    // 延迟到死亡动画结束后执行原版死亡结算，因此需要保留真正造成致死伤害的来源。
    // 否则改用 generic 会丢失击杀者、进度触发以及武器的抢夺等级。
    @Nullable
    private DamageSource deathDamageSource;

    // 强加载是否由复活追踪开启，避免退出追踪时误关其他系统开启的强加载
    private boolean resurrectionForceLoaded;

    private static final int VOID_RESCUE_RETRY_TICKS = 40;
    private static final int VOID_RESCUE_SEARCH_RADIUS = 8;
    private Vec3 lastSafePosition;
    private String lastSafeDimension = "";
    private boolean voidRescueActive;
    private int voidRescueRetryTicks;

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
        return !voidRescueActive && getAnimationState() >= STATE_IDLE;
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
                TrueHealthManager.register(this, maxHealth);
            }
        }

        // 无效目标清理
        if (!level().isClientSide) {
            LivingEntity target = getTarget();
            if (!EntityUtil.canAttack(this, target)) {
                setTarget(null);
            }
            syncResurrectionTracking();
        }

        // 死亡状态
        if (isDying()) {
            voidRescueActive = false;
            handleDeathTick();
            setDeltaMovement(Vec3.ZERO);
            super.tick();
            return;
        }

        if (!level().isClientSide) {
            tickVoidRescue();
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

    private void tickVoidRescue() {
        String dimension = level().dimension().location().toString();
        if (!dimension.equals(lastSafeDimension)) {
            lastSafePosition = null;
            lastSafeDimension = dimension;
            voidRescueActive = false;
        }
        if (!TheLastSwordConfiguration.getEntityVoidRescueEnabledSafely()) {
            voidRescueActive = false;
            voidRescueRetryTicks = 0;
            return;
        }

        if (!voidRescueActive && getY() < level().getMinBuildHeight() - 16.0) {
            voidRescueActive = true;
            voidRescueRetryTicks = 0;
            // 必须让正在运行的 Goal 释放自己的位置锁定，再选择传送落点。
            goalSelector.getRunningGoals().toList().forEach(WrappedGoal::stop);
            getNavigation().stop();
            if (getAnimationState() > STATE_IDLE) {
                setAnimationState(STATE_IDLE);
            }
            setDeltaMovement(Vec3.ZERO);
            fallDistance = 0;
        }

        if (voidRescueActive) {
            getNavigation().stop();
            setDeltaMovement(Vec3.ZERO);
            fallDistance = 0;
            if (voidRescueRetryTicks-- > 0) return;
            voidRescueRetryTicks = VOID_RESCUE_RETRY_TICKS;
            Vec3 destination = findVoidRescuePosition();
            if (destination != null && EntityUtil.theLastEndTeleport(
                    this, destination.x, destination.y, destination.z)) {
                voidRescueActive = false;
                lastSafePosition = destination;
                setDeltaMovement(Vec3.ZERO);
                fallDistance = 0;
            } else if (getY() < level().getMinBuildHeight() - 8.0) {
                // 无落点时停在建筑范围下方的空域，不修改实体原有的重力模式。
                EntityUtil.theLastEndTeleport(this, getX(), level().getMinBuildHeight() - 8.0, getZ());
            }
            return;
        }

        if (onGround() && !noPhysics && tickCount % 10 == 0) {
            Vec3 safePosition = checkVoidRescuePosition(position(), 0);
            if (safePosition != null) lastSafePosition = safePosition;
        }
    }

    @Nullable
    private Vec3 findVoidRescuePosition() {
        // 主人召回或外部传送已经把实体送回地面时，直接结束悬浮。
        Vec3 currentSafePosition = checkVoidRescuePosition(position(), 0);
        if (currentSafePosition != null) return currentSafePosition;
        if (lastSafePosition != null) {
            Vec3 position = checkVoidRescuePosition(lastSafePosition, 4);
            if (position != null) return position;
            position = findNearbyVoidRescuePosition(lastSafePosition);
            if (position != null) return position;
        }
        return findNearbyVoidRescuePosition(position());
    }

    @Nullable
    private Vec3 findNearbyVoidRescuePosition(Vec3 center) {
        BlockPos origin = BlockPos.containing(center);
        // 有界、低频搜索，避免在虚空中生成新区块或逐 tick 扫描地形。
        for (int radius = 0; radius <= VOID_RESCUE_SEARCH_RADIUS; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                    int x = origin.getX() + dx;
                    int z = origin.getZ() + dz;
                    if (!level().hasChunkAt(new BlockPos(x, level().getMinBuildHeight(), z))) continue;
                    int y = level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                    Vec3 safePosition = checkVoidRescuePosition(new Vec3(x + 0.5, y, z + 0.5), 2);
                    if (safePosition != null) return safePosition;
                }
            }
        }
        return null;
    }

    @Nullable
    private Vec3 checkVoidRescuePosition(Vec3 position, int verticalRange) {
        // 检查完整碰撞体覆盖的区块，不能只检查中心所在区块。
        double margin = getBbWidth() / 2.0 + 1.0;
        if (!level().hasChunksAt(BlockPos.containing(position.x - margin, position.y, position.z - margin),
                BlockPos.containing(position.x + margin, position.y, position.z + margin))) return null;
        Vec3 safePosition = EntityUtil.findSafeTeleportPosition(this, position, verticalRange);
        if (safePosition == null) return null;
        if (!level().getWorldBorder().isWithinBounds(BlockPos.containing(safePosition))) return null;
        if (level().containsAnyLiquid(getBoundingBox().move(safePosition.subtract(position())))) return null;
        return safePosition;
    }

    @Override
    public void move(@NotNull MoverType type, @NotNull Vec3 movement) {
        if (!level().isClientSide && voidRescueActive) {
            setDeltaMovement(Vec3.ZERO);
            fallDistance = 0;
            return;
        }
        super.move(type, movement);
    }

    @Override
    protected void onBelowWorld() {
        if (!level().isClientSide && voidRescueActive
                && TheLastSwordConfiguration.getEntityVoidRescueEnabledSafely()) return;
        super.onBelowWorld();
    }

    protected void onUnspawnedTick() {
    }

    protected void onSpawningTick() {
    }

    private void handleDeathTick() {
        hurtTime = 0;
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
        if (level().isClientSide) {
            return;
        }

        DamageSource damageSource = deathDamageSource;
        if (damageSource == null) {
            damageSource = getLastDamageSource();
        }
        if (damageSource == null) {
            damageSource = damageSources().generic();
        }

        float previousMaxHealth = TrueHealthManager.getMaxHealth(this);
        TrueHealthManager.setHealth(this, 0.0F);
        TrueHealthManager.clear(this);
        super.die(damageSource);

        if (this.dead) {
            safeRemove();
            return;
        }

        // 其他死亡事件处理器仍可取消最终结算；取消后必须退出死亡动画并恢复真实血量保护。
        float restoredMaxHealth = Float.isFinite(previousMaxHealth) && previousMaxHealth > 0.0F
                ? previousMaxHealth
                : getMaxHealth();
        float restoredHealth = super.getHealth();
        if (!Float.isFinite(restoredHealth) || restoredHealth <= 0.0F) {
            restoredHealth = restoredMaxHealth;
        }
        TrueHealthManager.setMaxHealth(this, restoredMaxHealth);
        TrueHealthManager.setHealth(this, Math.min(restoredHealth, restoredMaxHealth));
        EntityUtil.setProtection(this, true);
        deathDamageSource = null;
        setDeathTick(0);
        setAnimationState(STATE_IDLE);
    }

    public void triggerDeath(@NotNull DamageSource damageSource) {
        if (!isRemoved() && !dead && !hasPositiveRealHealth() && !isDying()) {
            // 死亡动画期间必须先退出复活追踪，否则守护线程会清掉 dead 标记导致死亡结算被打断
            stopResurrectionTracking();
            this.deathDamageSource = damageSource;
            setAnimationState(STATE_DEATH);
            setDeathTick(0);
        }
    }

    public void triggerDeath() {
        DamageSource damageSource = getLastDamageSource();
        triggerDeath(damageSource != null ? damageSource : damageSources().generic());
    }

    public void safeRemove() {
        stopResurrectionTracking();
        TrueHealthManager.setHealth(this, 0.0F);
        TrueHealthManager.clear(this);
        EntityUtil.theLastEndRemove(this, RemovalReason.KILLED);
    }

    // 是否应处于线程复活追踪
    protected boolean shouldTrackResurrection() {
        return getTheLastEndLevel() >= RESURRECTION_LEVEL
                && !isDying()
                && !isRemoved()
                && hasPositiveRealHealth();
    }

    // 每 tick 对齐追踪状态，ECA 的追踪表与手动强加载都不跨重载保留，需要在这里补回
    private void syncResurrectionTracking() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!shouldTrackResurrection()) {
            stopResurrectionTracking();
            return;
        }

        // ECA 会在读档时按 NBT 自动恢复追踪，但守护线程不会自启，这里无条件兜底
        if (!EcaAPI.isResurrectionRunning()) {
            EcaAPI.startResurrection();
        }

        if (!EcaAPI.isResurrectionTracked(this)) {
            EcaAPI.addResurrectionTarget(this);
        }

        // 已被其他系统强加载时不接管，避免退出追踪时把它们的票据一并释放
        if (!EcaAPI.isForceLoaded(this)) {
            EcaAPI.setForceLoading(this, serverLevel, true);
            resurrectionForceLoaded = true;
        }
    }

    // 退出线程复活追踪，任何强制清除路径都必须先调用
    protected void stopResurrectionTracking() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (EcaAPI.isResurrectionTracked(this)) {
            EcaAPI.removeResurrectionTarget(this);
        }

        if (resurrectionForceLoaded) {
            EcaAPI.setForceLoading(this, serverLevel, false);
            resurrectionForceLoaded = false;
        }
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

        DamageResult equipmentResult = AdvancedEquipmentDamageHandler.processThenPostEvent(
                this, damageSource, realDamage);
        if (equipmentResult.canceled()) {
            return;
        }
        realDamage = equipmentResult.amount();

        if (realDamage > 0) {
            float currentHealth = TrueHealthManager.getHealth(this);
            float newHealth = currentHealth - realDamage;

            //致死拦截：子类可在此消耗保命道具吃掉这次伤害
            if (newHealth <= 0 && !isDying() && onLethalDamage(damageSource)) {
                return;
            }

            TrueHealthManager.setHealth(this, newHealth);

            if (newHealth <= 0 && !isDying()) {
                triggerDeath(damageSource);
            }
        }

        int hurtTime = getHurtResistTime();
        setHurtResistTick(hurtTime);
        this.invulnerableTime = hurtTime;
    }

    // 限伤后仍会致死时调用，返回true表示已被保命道具吃掉这次伤害
    protected boolean onLethalDamage(@NotNull DamageSource damageSource) {
        return false;
    }

    // 死亡相关覆写
    @Override
    public void die(@NotNull DamageSource damageSource) {
        if (hasPositiveRealHealth()) {
            return;
        }
        triggerDeath(damageSource);
    }

    @Override
    protected void tickDeath() {
    }

    @Override
    public boolean isDeadOrDying() {
        return dead || isDying() || !hasPositiveRealHealth();
    }

    @Override
    public boolean isAlive() {
        return !isRemoved() && !dead && !isDying() && hasPositiveRealHealth();
    }

    @Override
    public void kill() {
        if (hasPositiveRealHealth()) {
            return;
        }
        triggerDeath();
    }

    @Override
    public void remove(@NotNull RemovalReason reason) {
        if (shouldBlockDestructiveRemoval(reason)) {
            return;
        }
        super.remove(reason);
    }

    @Override
    public void setRemoved(@NotNull RemovalReason reason) {
        if (shouldBlockDestructiveRemoval(reason)) {
            return;
        }
        super.setRemoved(reason);
    }

    private boolean hasPositiveRealHealth() {
        float health = TrueHealthManager.getHealth(this);
        return Float.isFinite(health) && health > 0.0F;
    }

    private boolean shouldBlockDestructiveRemoval(RemovalReason reason) {
        return level() instanceof ServerLevel
                && reason.shouldDestroy()
                && !isDying()
                && hasPositiveRealHealth();
    }

    // NBT
    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        CompoundTag rescue = tag.getCompound("VoidRescue");
        lastSafeDimension = rescue.getString("Dimension");
        lastSafePosition = null;
        if (rescue.contains("X") && rescue.contains("Y") && rescue.contains("Z")) {
            Vec3 savedPosition = new Vec3(rescue.getDouble("X"), rescue.getDouble("Y"), rescue.getDouble("Z"));
            if (Double.isFinite(savedPosition.x) && Double.isFinite(savedPosition.y)
                    && Double.isFinite(savedPosition.z)) lastSafePosition = savedPosition;
        }
        voidRescueActive = rescue.getBoolean("Active");
        voidRescueRetryTicks = 0;
        if (tag.contains("AnimationState")) {
            int savedState = tag.getInt("AnimationState");
            // Skill Goal timers are transient and are not serialized. Restoring one of
            // those states would leave GeckoLib frozen on the last animation frame.
            setAnimationState(savedState > STATE_IDLE ? STATE_IDLE : savedState);
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
                TrueHealthManager.register(this, maxHealth);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        CompoundTag rescue = new CompoundTag();
        rescue.putString("Dimension", lastSafeDimension);
        rescue.putBoolean("Active", voidRescueActive);
        if (lastSafePosition != null) {
            rescue.putDouble("X", lastSafePosition.x);
            rescue.putDouble("Y", lastSafePosition.y);
            rescue.putDouble("Z", lastSafePosition.z);
        }
        tag.put("VoidRescue", rescue);
        int animationState = getAnimationState();
        tag.putInt("AnimationState", animationState > STATE_IDLE ? STATE_IDLE : animationState);
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

    /**
     * TamableAnimal inherits Animal's random 1-3 XP reward. Use the explicit
     * reward configured by each end entity instead (xpReward in its constructor).
    */
    @Override
    public int getExperienceReward() {
        return this.xpReward;
    }

    @Override
    public boolean startRiding(@NotNull Entity entity) {
        return false;
    }

    @Override
    public void teleportTo(double x, double y, double z) {
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
