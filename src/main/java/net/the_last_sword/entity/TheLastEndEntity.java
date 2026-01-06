package net.the_last_sword.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.network.NetworkHooks;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.ai.TheLastEndAI;
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

//终焉种基类
public abstract class TheLastEndEntity extends TamableAnimal implements GeoEntity {

    //GeckoLib动画缓存
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    //AI系统
    private TheLastEndAI ai;

    //动画字段
    private static final EntityDataAccessor<String> ANIMATION =
            SynchedEntityData.defineId(TheLastEndEntity.class, EntityDataSerializers.STRING);

    //技能计时器
    private static final EntityDataAccessor<Integer> SKILL_TICK =
            SynchedEntityData.defineId(TheLastEndEntity.class, EntityDataSerializers.INT);

    //自定义无敌帧
    private static final EntityDataAccessor<Integer> ALLOW_HURT_TICK =
            SynchedEntityData.defineId(TheLastEndEntity.class, EntityDataSerializers.INT);

    //等级
    private static final EntityDataAccessor<Integer> THE_LAST_END_LEVEL =
            SynchedEntityData.defineId(TheLastEndEntity.class, EntityDataSerializers.INT);

    //死亡字段
    private static final EntityDataAccessor<Boolean> SHOULD_LEAVE =
            SynchedEntityData.defineId(TheLastEndEntity.class, EntityDataSerializers.BOOLEAN);

    //死亡计时器
    private static final EntityDataAccessor<Integer> LEAVE_TICK =
            SynchedEntityData.defineId(TheLastEndEntity.class, EntityDataSerializers.INT);

    //万物终焉状态
    private static final EntityDataAccessor<Boolean> IS_ALL_THINGS_END =
            SynchedEntityData.defineId(TheLastEndEntity.class, EntityDataSerializers.BOOLEAN);

    //生成完成标记（用于出场动画）
    private static final EntityDataAccessor<Boolean> IS_SPAWNED =
            SynchedEntityData.defineId(TheLastEndEntity.class, EntityDataSerializers.BOOLEAN);

    //生成时间计时器
    private static final EntityDataAccessor<Integer> SPAWN_TICK =
            SynchedEntityData.defineId(TheLastEndEntity.class, EntityDataSerializers.INT);

    protected TheLastEndEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ANIMATION, "idle");
        this.entityData.define(SKILL_TICK, 0);
        this.entityData.define(ALLOW_HURT_TICK, 0);
        this.entityData.define(THE_LAST_END_LEVEL, 1);
        this.entityData.define(SHOULD_LEAVE, false);
        this.entityData.define(LEAVE_TICK, 0);
        this.entityData.define(IS_ALL_THINGS_END, false);
        this.entityData.define(IS_SPAWNED, false);
        this.entityData.define(SPAWN_TICK, 0);
    }



    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("ANIMATION")) {
            setAnimation(tag.getString("ANIMATION"));
        }
        if (tag.contains("SKILL_TICK")) {
            setSkillTick(tag.getInt("SKILL_TICK"));
        }
        if (tag.contains("ALLOW_HURT_TICK")) {
            setAllowHurtTick(tag.getInt("ALLOW_HURT_TICK"));
        }
        if (tag.contains("THE_LAST_END_LEVEL")) {
            setTheLastEndLevel(tag.getInt("THE_LAST_END_LEVEL"));
        }
        if (tag.contains("SHOULD_LEAVE")) {
            setShouldLeave(tag.getBoolean("SHOULD_LEAVE"));
        }
        if (tag.contains("LEAVE_TICK")) {
            setLeaveTick(tag.getInt("LEAVE_TICK"));
        }
        if (tag.contains("IS_ALL_THINGS_END")) {
            setAllThingsEnd(tag.getBoolean("IS_ALL_THINGS_END"));
        }
        if (tag.contains("IS_SPAWNED")) {
            setSpawned(tag.getBoolean("IS_SPAWNED"));
        }
        if (tag.contains("SPAWN_TICK")) {
            setSpawnTick(tag.getInt("SPAWN_TICK"));
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("ANIMATION", getAnimation());
        tag.putInt("SKILL_TICK", getSkillTick());
        tag.putInt("ALLOW_HURT_TICK", getAllowHurtTick());
        tag.putInt("THE_LAST_END_LEVEL", getTheLastEndLevel());
        tag.putBoolean("SHOULD_LEAVE", shouldLeave());
        tag.putInt("LEAVE_TICK", getLeaveTick());
        tag.putBoolean("IS_ALL_THINGS_END", isAllThingsEnd());
        tag.putBoolean("IS_SPAWNED", isSpawned());
        tag.putInt("SPAWN_TICK", getSpawnTick());
        setSkillTick(0);
        setAnimation(getIdleAnimationName());
    }

    //动画字段
    public String getAnimation() {
        return this.entityData.get(ANIMATION);
    }

    public void setAnimation(String animation) {
        this.entityData.set(ANIMATION, animation);
    }

    //技能计时器
    public int getSkillTick() {
        return this.entityData.get(SKILL_TICK);
    }

    public void setSkillTick(int tick) {
        this.entityData.set(SKILL_TICK, tick);
    }

    //自定义血量（使用防御系统）
    public float getTheLastEndHealth() {
        return EntityUtil.getTrueHealth(this);
    }

    public void setTheLastEndHealth(float health) {
        EntityUtil.setTrueHealth(this, health);
    }

    //自定义最大血量（使用防御系统）
    public float getTheLastEndMaxHealth() {
        return EntityUtil.getTrueMaxHealth(this);
    }

    public void setTheLastEndMaxHealth(float maxHealth) {
        EntityUtil.setTrueMaxHealth(this, maxHealth);
    }

    //自定义无敌帧
    public int getAllowHurtTick() {
        return this.entityData.get(ALLOW_HURT_TICK);
    }

    public void setAllowHurtTick(int tick) {
        this.entityData.set(ALLOW_HURT_TICK, tick);
    }

    //子类可覆写：返回受伤后的无敌帧时长
    protected int getAllowHurtTime() {
        return 20;
    }

    //子类可覆写：返回单次受伤的限伤值
    protected float getDamageLimit() {
        float maxHealth = (float) this.getAttributeValue(Attributes.MAX_HEALTH);
        float ratio = (float) TheLastSwordConfiguration.getDefenceCustomHealthDamageReductionSafely();
        float maxDamage = (float) TheLastSwordConfiguration.getDefenceMaxDamagePerHitSafely();
        return Math.min(maxHealth * ratio, maxDamage);
    }

    //等级
    public int getTheLastEndLevel() {
        return this.entityData.get(THE_LAST_END_LEVEL);
    }

    public void setTheLastEndLevel(int level) {
        this.entityData.set(THE_LAST_END_LEVEL, level);
    }

    //死亡字段
    public boolean shouldLeave() {
        return this.entityData.get(SHOULD_LEAVE);
    }

    public void setShouldLeave(boolean shouldLeave) {
        this.entityData.set(SHOULD_LEAVE, shouldLeave);
    }

    //死亡计时器
    public int getLeaveTick() {
        return this.entityData.get(LEAVE_TICK);
    }

    public void setLeaveTick(int tick) {
        this.entityData.set(LEAVE_TICK, tick);
    }

    //万物终焉状态
    public boolean isAllThingsEnd() {
        return this.entityData.get(IS_ALL_THINGS_END);
    }

    public void setAllThingsEnd(boolean allThingsEnd) {
        this.entityData.set(IS_ALL_THINGS_END, allThingsEnd);
    }

    //生成完成标记
    public boolean isSpawned() {
        return this.entityData.get(IS_SPAWNED);
    }

    public void setSpawned(boolean spawned) {
        this.entityData.set(IS_SPAWNED, spawned);
    }

    //生成时间计时器
    public int getSpawnTick() {
        return this.entityData.get(SPAWN_TICK);
    }

    public void setSpawnTick(int tick) {
        this.entityData.set(SPAWN_TICK, tick);
    }

    //子类可覆写：返回生成动画时长（tick）
    protected int getSpawnAnimationDuration() {
        return 110;  //默认5.5秒
    }

    //GeckoLib动画系统
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        //运动控制器
        controllers.add(new AnimationController<>(this, "movement", 0, this::movementAnimationPredicate));
        //技能控制器
        controllers.add(new AnimationController<>(this, "skill", 0, this::skillAnimationPredicate));
    }

    //运动动画逻辑
    private PlayState movementAnimationPredicate(AnimationState<TheLastEndEntity> state) {
        String currentAnim = getAnimation();
        //如果正在播放技能动画，运动控制器停止
        if (!currentAnim.equals("idle") && !currentAnim.equals(getIdleAnimationName())) {
            return PlayState.STOP;
        }

        //正常的运动动画逻辑（使用 GeckoLib 的移动检测）
        if (state.isMoving()) {
            state.setAnimation(RawAnimation.begin().thenLoop(getMovementAnimationName()));
        } else {
            state.setAnimation(RawAnimation.begin().thenLoop(getIdleAnimationName()));
        }
        return PlayState.CONTINUE;
    }

    //技能动画逻辑
    private PlayState skillAnimationPredicate(AnimationState<TheLastEndEntity> state) {
        String currentAnim = getAnimation();
        String idleName = getIdleAnimationName();
        String deathName = getDeathAnimationName();
        String spawnName = getSpawnAnimationName();

        //死亡动画优先级最高，直接播放
        if (currentAnim.equals(deathName)) {
            state.setAnimation(RawAnimation.begin().thenPlay(deathName));
            return PlayState.CONTINUE;
        }

        //生成动画优先级次之
        if (currentAnim.equals(spawnName)) {
            state.setAnimation(RawAnimation.begin().thenPlay(spawnName));
            return PlayState.CONTINUE;
        }

        //技能动画播放（重置逻辑由技能系统自动管理）
        if (!currentAnim.equals("idle") && !currentAnim.equals(idleName)) {
            state.setAnimation(RawAnimation.begin().thenPlay(currentAnim));
            return PlayState.CONTINUE;
        }

        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    //子类实现：返回待机动画名称
    public abstract String getIdleAnimationName();

    //子类实现：返回移动动画名称
    public abstract String getMovementAnimationName();

    //子类实现：返回死亡动画名称
    public abstract String getDeathAnimationName();

    //子类实现：返回生成动画名称
    public abstract String getSpawnAnimationName();

    //子类实现：创建AI实例
    public abstract TheLastEndAI createAI();

    //获取死亡动画时长（tick）- 子类可覆写
    public int getDeathAnimationDuration() {
        return 20;  //默认20tick（1秒）
    }

    //死亡开始时的自定义逻辑 - 子类可覆写
    protected void onDeathStart() {
        //默认为空，子类按需实现
    }

    //安全移除实体（后门方法）
    public void safeRemove() {
        setTheLastEndHealth(0);
        setShouldLeave(true);
        if (ai != null) {
            ai.shutdown();
        }
        //清除防御保护
        EntityUtil.clearDefence(this);
        EntityUtil.theLastEndRemove(this, RemovalReason.KILLED);
    }

    //覆写逻辑
    @Override
    public void tick() {
        //首次 tick：初始化防御保护（服务端）
        if (!level().isClientSide && tickCount == 1) {
            if (!EntityUtil.hasProtection(this)) {
                float maxHealth = (float) this.getAttributeValue(Attributes.MAX_HEALTH);
                EntityUtil.registerDefence(this, maxHealth);
            }
        }

        super.tick();

        //统一死亡处理
        handleDeath();

        //生成动画处理
        handleSpawn();

        //AI系统（生成完成后才执行）
        if (isSpawned()) {
            if (ai == null) {
                ai = createAI();
            }
            ai.update();
        }

        //无敌帧递减
        if (getAllowHurtTick() > 0) {
            setAllowHurtTick(getAllowHurtTick() - 1);
        }
    }

    //统一死亡处理流程
    private void handleDeath() {
        //死亡处理
        if (shouldLeave()) {
            int leaveTick = getLeaveTick();
            if (leaveTick == 0) {
                if (ai != null) {
                    ai.shutdown();  //关闭AI
                }
                //死亡开始：播放死亡动画
                setAnimation(getDeathAnimationName());
                //调用子类自定义死亡开始逻辑
                onDeathStart();
            }
            setLeaveTick(leaveTick + 1);

            //死亡动画播放完毕后移除实体
            if (leaveTick >= getDeathAnimationDuration()) {
                //只在服务端掉落战利品
                if (!level().isClientSide) {
                    this.dropAllDeathLoot(damageSources().generic());
                }
                this.safeRemove();
            }
            return;
        }

        //血量归零触发死亡
        if (getTheLastEndHealth() <= 0 && !shouldLeave()) {
            setShouldLeave(true);
        }
    }

    //统一生成动画处理流程
    private void handleSpawn() {
        //如果已经生成完成或正在死亡，跳过
        if (isSpawned() || shouldLeave()) {
            return;
        }

        int spawnTick = getSpawnTick();

        //第一次tick，播放生成动画
        if (spawnTick == 0) {
            setAnimation(getSpawnAnimationName());
        }

        //递增生成计时器
        setSpawnTick(spawnTick + 1);

        //生成动画播放完毕
        if (spawnTick >= getSpawnAnimationDuration()) {
            setSpawned(true);
            setAnimation(getIdleAnimationName());
        }
    }

    //自定义血量系统（由 LivingEntityMixin 接管）
    //getHealth() 和 getMaxHealth() 由 Mixin 覆写，无需在此重复定义

    @Override
    public void actuallyHurt(@NotNull DamageSource damageSource, float damageAmount) {
        //无敌帧检查
        if (getAllowHurtTick() > 0) {
            return;
        }
        //获取限伤值
        float damageLimit = getDamageLimit();
        int level = getTheLastEndLevel();
        float realDamage;
        if (level <= 5) {
            //0-5级：限伤后扣血
            realDamage = Math.min(damageAmount, damageLimit);
        } else {
            //6-13级：超过限伤值则免疫
            if (damageAmount > damageLimit) {
                return;
            }
            realDamage = damageAmount;
        }
        //扣除真实血量
        if (realDamage > 0) {
            float currentHealth = getTheLastEndHealth();
            setTheLastEndHealth(currentHealth - realDamage);
        }
        //设置无敌帧
        int hurtTime = getAllowHurtTime();
        setAllowHurtTick(hurtTime);
        //同步原版无敌帧
        this.invulnerableTime = hurtTime;
    }

    @Override
    public void die(@NotNull DamageSource damageSource) {
        if (shouldLeave()) {
            super.die(damageSource);
        }
    }

    @Override
    protected void tickDeath() {
    }

    @Override
    public boolean isDeadOrDying() {
        return shouldLeave();
    }

    @Override
    public boolean isAlive() {
        return !shouldLeave();
    }

    @Override
    public void kill() {
        if (shouldLeave()) {
            super.kill();
        }
    }
    
    @Override
    public void remove(@NotNull RemovalReason reason) {
        if (level() instanceof ServerLevel && !shouldLeave()) {
            return;
        }
        super.remove(reason);
    }

    @Override
    public void setRemoved(@NotNull RemovalReason reason) {
        if (level() instanceof ServerLevel && !shouldLeave()) {
            return;
        }
        super.setRemoved(reason);
    }

    //终焉种特性
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

    //禁用繁殖
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
