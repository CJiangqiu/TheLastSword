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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.ai.TheLastEndAI;
import net.the_last_sword.entity.ai.TheLastEndSwordWraithAI;
import net.the_last_sword.init.ModEffects;
import net.the_last_sword.util.EntityUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

//终焉剑灵实体
public class TheLastEndSwordWraithEntity extends TheLastEndEntity {

    //纹理字段
    public static final EntityDataAccessor<String> TEXTURE =
            SynchedEntityData.defineId(TheLastEndSwordWraithEntity.class, EntityDataSerializers.STRING);

    //技能目标列表
    public final List<LivingEntity> targetList = new ArrayList<>();

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

    //属性
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
            //根据等级更新属性
            updateAttributesByLevel(newLevel);
        }
    }

    //根据等级和配置更新属性
    private void updateAttributesByLevel(int level) {
        //计算生命值
        double healthPerLevel = (level <= 5)
                ? TheLastSwordConfiguration.getSwordWraithHealthPerLevelSafely()
                : TheLastSwordConfiguration.getSwordWraithHealthPerHighLevelSafely();
        double newMaxHealth = 200.0 + (level * healthPerLevel);

        //计算攻击力
        double attackPerLevel = (level <= 5)
                ? TheLastSwordConfiguration.getSwordWraithAttackPerLevelSafely()
                : TheLastSwordConfiguration.getSwordWraithAttackPerHighLevelSafely();
        double newAttack = 20.0 + (level * attackPerLevel);

        //应用属性
        var healthAttr = this.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.setBaseValue(newMaxHealth);
        }

        var attackAttr = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr != null) {
            attackAttr.setBaseValue(newAttack);
        }

        //同步更新真实生命值到最大值
        setTheLastEndMaxHealth((float) newMaxHealth);
        setTheLastEndHealth((float) newMaxHealth);
    }

    @Override
    public void setAllThingsEnd(boolean enabled) {
        boolean current = isAllThingsEnd();
        super.setAllThingsEnd(enabled);

        if (current != enabled && !level().isClientSide) {
            if (enabled) {
                //解锁万物终焉时的处理由AI负责
            }
        }
    }

    //创建AI实例
    @Override
    public TheLastEndAI createAI() {
        return new TheLastEndSwordWraithAI(this);
    }

    //动画名称
    @Override
    public String getIdleAnimationName() {
        return "idle";
    }

    @Override
    public String getMovementAnimationName() {
        return "idle";  //终焉剑灵没有移动动画，始终用idle
    }

    @Override
    public String getDeathAnimationName() {
        return "death";
    }

    @Override
    public String getSpawnAnimationName() {
        return "spawn";
    }

    //纹理
    public void setTexture(String texture) {
        this.entityData.set(TEXTURE, texture);
    }

    public String getTexture() {
        return this.entityData.get(TEXTURE);
    }

    //声音
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

    //只允许虚空附魔效果，免疫其他所有效果
    @Override
    public boolean canBeAffected(@NotNull MobEffectInstance effect) {
        if (effect.getEffect() == ModEffects.VOID_ENCHANTING.get()) {
            return true;
        }
        return false;
    }

    //生成时初始化
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty,
            MobSpawnType reason, @Nullable SpawnGroupData livingdata, @Nullable CompoundTag tag) {
        SpawnGroupData result = super.finalizeSpawn(world, difficulty, reason, livingdata, tag);

        //仅在服务端初始化
        if (!level().isClientSide) {
            //初始化等级（如果未设置）
            if (getTheLastEndLevel() <= 0) {
                setTheLastEndLevel(1);
            }

            //初始化血量
            float maxHealth = (float) getAttributeValue(Attributes.MAX_HEALTH);
            setTheLastEndMaxHealth(maxHealth);
            setTheLastEndHealth(maxHealth);

            //提前注册防御系统（确保 IS_PROTECTED 和 HEALTH_LOCK_ENABLED 同步）
            if (!EntityUtil.hasProtection(this)) {
                EntityUtil.registerDefence(this, maxHealth);
            }
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

        this.refreshDimensions();
    }

    //死亡动画时长
    @Override
    public int getDeathAnimationDuration() {
        return 100;  //100tick（5秒）
    }

    //NBT
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
}
