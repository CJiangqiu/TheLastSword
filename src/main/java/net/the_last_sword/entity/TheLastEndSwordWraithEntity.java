package net.the_last_sword.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.registries.ForgeRegistries;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.defence.DefenceManager;
import net.the_last_sword.entity.ai.TheLastEndSwordWraithAI;
import net.the_last_sword.init.ModEffects;
import net.the_last_sword.init.ModEntities;
import net.the_last_sword.util.EntityUtil;
import net.the_last_sword.util.ParticleUtil;
import net.the_last_sword.util.TheLastSwordLogger;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TheLastEndSwordWraithEntity extends TheLastEndEntity {

    public static final EntityDataAccessor<Boolean> SHOOT =
            SynchedEntityData.defineId(TheLastEndSwordWraithEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<String> ANIMATION =
            SynchedEntityData.defineId(TheLastEndSwordWraithEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> TEXTURE =
            SynchedEntityData.defineId(TheLastEndSwordWraithEntity.class, EntityDataSerializers.STRING);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public final List<LivingEntity> targetList = new ArrayList<>();

    public TheLastEndSwordWraithEntity(PlayMessages.SpawnEntity packet, Level world) {
        this(ModEntities.THE_LAST_END_SWORD_WRAITH.get(), world);
    }

    public TheLastEndSwordWraithEntity(EntityType<? extends TheLastEndEntity> type, Level world) {
        super(type, world);
        xpReward = 50;
        setNoAi(false);
        setMaxUpStep(0.6f);
        setPersistenceRequired();
    }

    @Override
    protected int getMaxLevel() {
        return 13;
    }

    @Override
    public int getAllThingsEndLevel() {
        return 13;
    }

    @Override
    public void setEndLevel(int level) {
        int oldLevel = getEndLevel();
        super.setEndLevel(level);
        int newLevel = getEndLevel();

        if (newLevel != oldLevel && !level().isClientSide) {
            //根据等级更新属性
            updateAttributesByLevel(newLevel);

            if (DefenceManager.hasDefenceRecord(this) && DefenceManager.getDefenceLevel(this) >= 1) {
                DefenceManager.register(this, mapToDefenseLevel());
            }

            if (getIsSpawned()) {
                TheLastEndSwordWraithAI.handleLevelChange(this, oldLevel, newLevel);
            }
        }
    }

    //根据等级和配置更新属性
    private void updateAttributesByLevel(int level) {
        //计算生命值：基础值 + 等级 × 配置
        double healthPerLevel = (level <= 5)
                ? TheLastSwordConfiguration.getSwordWraithHealthPerLevelSafely()
                : TheLastSwordConfiguration.getSwordWraithHealthPerHighLevelSafely();
        double newMaxHealth = 100.0 + (level * healthPerLevel);

        //计算攻击力：基础值 + 等级 × 配置
        double attackPerLevel = (level <= 5)
                ? TheLastSwordConfiguration.getSwordWraithAttackPerLevelSafely()
                : TheLastSwordConfiguration.getSwordWraithAttackPerHighLevelSafely();
        double newAttack = 10.0 + (level * attackPerLevel);

        //应用属性
        var healthAttr = this.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.setBaseValue(newMaxHealth);
        }

        var attackAttr = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr != null) {
            attackAttr.setBaseValue(newAttack);
        }

        //治疗到满血
        this.setHealth(this.getMaxHealth());
    }

    @Override
    public void setAllThingsEndState(boolean enabled) {
        boolean current = isAllThingsEnd();
        super.setAllThingsEndState(enabled);

        if (current != enabled && !level().isClientSide) {
            if (enabled) {
                TheLastEndSwordWraithAI.onAllThingsEndUnlocked(this);
            } else {
                TheLastEndSwordWraithAI.onAllThingsEndLost(this);
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SHOOT, false);
        this.entityData.define(ANIMATION, "undefined");
        this.entityData.define(TEXTURE, "the_last_end_sword_wraith");
    }

    public void setTexture(String texture) {
        this.entityData.set(TEXTURE, texture);
    }

    public String getTexture() {
        return this.entityData.get(TEXTURE);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("TEXTURE", this.getTexture());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("TEXTURE")) {
            this.setTexture(compound.getString("TEXTURE"));
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }


    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.ender_dragon.hurt"));
    }

    @Override
    public SoundEvent getDeathSound() {
        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.ender_dragon.death"));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData livingdata,
                                        @Nullable CompoundTag tag) {
        SpawnGroupData retval = super.finalizeSpawn(world, difficulty, reason, livingdata, tag);
        if (!level().isClientSide) {
            if (getEndLevel() <= 0) {
                setEndLevel(1);
            }
            this.setAnimation("spawn");

            spawnSummonParticles();
        }
        return retval;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        if (effect.getEffect() == ModEffects.VOID_ENCHANTING.get()) {
            return true;
        }
        return false;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return super.getDimensions(pose).scale(1.0f);
    }

    public double getAttackRange() {
        return this.getAttributeValue(ForgeMod.ENTITY_REACH.get());
    }

    @Override
    public void baseTick() {
        super.baseTick();

        //死亡逻辑 - 只在服务端执行
        if (!level().isClientSide) {
            //必须直接获取record，不能使用DefenceManager.getHealth() API
            //原因未知：通过API获取会出现返回值错误（返回2700.0但接收到0.0），可能是JIT的神必内联问题。
            DefenceManager.DefenceRecord record = DefenceManager.getRecord(this);
            float healthCheck = 0.0F;
            if (record != null) {
                healthCheck = record.getHealth();
            }

            if (healthCheck <= 0.0F) {
                //如果计数器是110，说明刚进入死亡状态
                if (getLeaveTime() == 110) {
                    setAnimation("death");
                    TheLastEndSwordWraithAI.cleanupRemovedEntity(this.getUUID());
                }

                //每tick-1
                setLeaveTime(getLeaveTime() - 1);
                //双重检查：必须同时满足时间到且真实血量为0
                if (getLeaveTime() <= 0 && healthCheck <= 0.0F) {
                    safeRemove();
                }
                return;
            } else {
                //血量恢复，强制重置死亡计数器
                setLeaveTime(110);
            }
        }

        //生成动画逻辑
        if (!getIsSpawned()) {
            skillTick++;
            if (skillTick >= 110) {
                setIsSpawned(true);
                this.setAnimation("idle");
                skillTick = 0;
            }
        }

        TheLastEndSwordWraithAI.handleTick(this);

        boolean hasTarget = this.getTarget() != null;
        if (hasTarget) {
            if (this.tickCount % 20 == 0) {
                faceTarget(this.getTarget());
            }
        }

        this.refreshDimensions();
    }

    //让终焉剑灵面向目标
    private void faceTarget(LivingEntity target) {
        if (target == null) {
            return;
        }
        double deltaX = target.getX() - this.getX();
        double deltaZ = target.getZ() - this.getZ();
        double deltaY = target.getY() + target.getBbHeight() * 0.5 - (this.getY() + this.getBbHeight() * 0.5);
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float yaw = (float) (Math.atan2(-deltaX, deltaZ) * 180.0 / Math.PI);
        float pitch = (float) (Math.atan2(-deltaY, horizontalDistance) * 180.0 / Math.PI);
        this.setYRot(yaw);
        this.setXRot(pitch);
        this.yHeadRot = yaw;
        this.yBodyRot = yaw;
    }

    private PlayState movementPredicate(AnimationState event) {
        boolean hasTarget = this.getTarget() != null;
        Vec3 deltaMovement = this.getDeltaMovement();
        double horizontalSpeed = deltaMovement.horizontalDistanceSqr();
        boolean isMoving = horizontalSpeed > 0.001;

        boolean canMove = this.getAllowMoving();

        if (canMove && isMoving) {
            return event.setAndContinue(RawAnimation.begin().thenLoop("walk"));
        } else if (hasTarget) {
            return event.setAndContinue(RawAnimation.begin().thenLoop("battle_idle"));
        } else {
            return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }
    }

    String prevAnim = "empty";

    private PlayState procedurePredicate(AnimationState event) {
        String currentAnim = this.getSyncedAnimation();
        if (!currentAnim.equals("empty") && event.getController().getAnimationState() == AnimationController.State.STOPPED || (!currentAnim.equals(prevAnim) && !currentAnim.equals("empty"))) {
            if (!currentAnim.equals(prevAnim))
                event.getController().forceAnimationReset();
            event.getController().setAnimation(RawAnimation.begin().thenPlay(currentAnim));
            if (event.getController().getAnimationState() == AnimationController.State.STOPPED) {
                this.setAnimation("empty");
                event.getController().forceAnimationReset();
            }
        } else if (currentAnim.equals("empty")) {
            prevAnim = "empty";
            return PlayState.STOP;
        }
        prevAnim = currentAnim;
        return PlayState.CONTINUE;
    }

    public String getSyncedAnimation() {
        return this.entityData.get(ANIMATION);
    }

    public void setAnimation(String animation) {
        this.entityData.set(ANIMATION, animation);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "movement", 4, this::movementPredicate));
        data.add(new AnimationController<>(this, "procedure", 4, this::procedurePredicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public static void init() {
    }

    public static AttributeSupplier.Builder createAttributes() {
        AttributeSupplier.Builder builder = Mob.createMobAttributes();
        builder = builder.add(Attributes.MOVEMENT_SPEED, 0.2);
        builder = builder.add(Attributes.MAX_HEALTH, 200);
        builder = builder.add(Attributes.ARMOR, 0);
        builder = builder.add(Attributes.ATTACK_DAMAGE, 10);
        builder = builder.add(Attributes.FOLLOW_RANGE, 32);
        builder = builder.add(Attributes.ATTACK_KNOCKBACK, 0.1);
        builder = builder.add(ForgeMod.ENTITY_REACH.get(), 4.0);
        return builder;
    }


    @Override
    public boolean isPushable() {
        return false; // 不被其他实体推动
    }

    @Override
    protected void doPush(Entity entity) {
        super.doPush(entity); // 可以推动其他实体
    }

    //生成召唤粒子效果
    public void spawnSummonParticles() {
        ParticleUtil.spawnSummonParticles(this);
    }

}
