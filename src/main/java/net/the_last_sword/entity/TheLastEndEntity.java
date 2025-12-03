package net.the_last_sword.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.the_last_sword.util.EntityUtil;
import net.the_last_sword.util.TheLastSwordLogger;
import net.the_last_sword.defence.DefenceManager;
import software.bernie.geckolib.animatable.GeoEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

//终焉类型实体抽象基类
public abstract class TheLastEndEntity extends TamableAnimal implements GeoEntity {

    //终焉等级
    private static final EntityDataAccessor<Integer> LEVEL =
            SynchedEntityData.defineId(TheLastEndEntity.class, EntityDataSerializers.INT);

    //万物终焉状态
    private static final EntityDataAccessor<Boolean> ALL_THINGS_END =
            SynchedEntityData.defineId(TheLastEndEntity.class, EntityDataSerializers.BOOLEAN);

    //生成完成状态
    private static final EntityDataAccessor<Boolean> IS_SPAWNED =
            SynchedEntityData.defineId(TheLastEndEntity.class, EntityDataSerializers.BOOLEAN);

    //移动允许
    private static final EntityDataAccessor<Boolean> ALLOW_MOVING =
            SynchedEntityData.defineId(TheLastEndEntity.class, EntityDataSerializers.BOOLEAN);

    //服务端专用技能tick计数器（不同步到客户端，避免动画与逻辑不同步）
    protected int skillTick = 0;

    //死亡计数器
    protected int leaveTime = 110;

    //缓存UUID，避免死亡瞬间获取失败
    private UUID cachedUUID = null;

    protected TheLastEndEntity(EntityType<? extends TamableAnimal> type, Level world) {
        super(type, world);
        //在实体完全初始化后立即缓存UUID，避免在不稳定状态下首次调用
        try {
            this.cachedUUID = super.getUUID();
        } catch (Exception e) {
            //如果获取失败，保持null，下次调用时再试
        }
    }

    //覆写getUUID，使用缓存避免死亡瞬间UUID失效
    @Override
    public UUID getUUID() {
        if (cachedUUID == null) {
            cachedUUID = super.getUUID();
        }

        //双重保险：如果缓存的UUID在DefenceManager中找不到记录，尝试更新缓存
        //这种情况可能发生在首次缓存时UUID处于异常状态
        if (!level().isClientSide && cachedUUID != null) {
            if (!DefenceManager.hasDefenceRecordByUUID(cachedUUID)) {
                //尝试从super获取新的UUID
                UUID freshUUID = super.getUUID();
                if (freshUUID != null && !freshUUID.equals(cachedUUID)) {
                    //UUID确实变了，更新缓存
                    if (DefenceManager.hasDefenceRecordByUUID(freshUUID)) {
                        cachedUUID = freshUUID;
                    }
                }
            }
        }

        return cachedUUID;
    }

    //最小等级，子类可覆写
    protected int getMinLevel() {
        return 1;
    }

    //最大等级，子类可覆写
    protected int getMaxLevel() {
        return 13;
    }

    //万物终焉解锁等级，子类可覆写
    public int getAllThingsEndLevel() {
        return 13;
    }

    //终焉等级到防御等级的映射，子类可覆写
    public int mapToDefenseLevel() {
        int endLevel = getEndLevel();
        if (endLevel <= 5) {
            return 1;
        } else if (endLevel <= 12) {
            return 2;
        } else {
            if (isAllThingsEnd()) {
                return 3;
            } else {
                return 2;
            }
        }
    }


    //获取终焉等级
    public final int getEndLevel() {
        return this.entityData.get(LEVEL);
    }

    //设置终焉等级
    public void setEndLevel(int level) {
        int oldLevel = getEndLevel();
        int newLevel = Math.max(getMinLevel(), Math.min(getMaxLevel(), level));

        if (oldLevel != newLevel) {
            this.entityData.set(LEVEL, newLevel);
            updateDefenseLevel();
        }
    }

    //核心注册方法 - 统一处理所有注册逻辑
    private void ensureDefenseRegistration() {
        if (level().isClientSide) {
            return;
        }
        if (!DefenceManager.hasDefenceRecord(this) || DefenceManager.getDefenceLevel(this) < 1) {
            int defenseLevel = mapToDefenseLevel();
            DefenceManager.register(this, defenseLevel);
        } else {
            updateDefenseLevel();
        }

    }

    //更新防御等级
    public void updateDefenseLevel() {
        if (level().isClientSide) {
            return;
        }

        if (!DefenceManager.hasDefenceRecord(this) || DefenceManager.getDefenceLevel(this) < 1) {
            return;
        }

        int currentDefenseLevel = mapToDefenseLevel();
        int registeredLevel = DefenceManager.getDefenceLevel(this);

        if (currentDefenseLevel != registeredLevel) {
            DefenceManager.register(this, currentDefenseLevel);
        } else {
            DefenceManager.register(this, currentDefenseLevel);
        }
    }

    //获取生成完成状态
    public final boolean getIsSpawned() {
        return this.entityData.get(IS_SPAWNED);
    }

    //设置生成完成状态
    public final void setIsSpawned(boolean spawned) {
        this.entityData.set(IS_SPAWNED, spawned);
    }

    //获取服务端技能tick
    public final int getSkillTick() {
        return this.skillTick;
    }

    //设置服务端技能tick
    public final void setSkillTick(int tick) {
        this.skillTick = tick;
    }

    //获取死亡计数器
    public final int getLeaveTime() {
        return this.leaveTime;
    }

    //设置死亡计数器
    public final void setLeaveTime(int time) {
        this.leaveTime = time;
    }

    //获取移动允许状态
    public final boolean getAllowMoving() {
        return this.entityData.get(ALLOW_MOVING);
    }

    //设置移动允许状态
    public final void setAllowMoving(boolean allowMoving) {
        this.entityData.set(ALLOW_MOVING, allowMoving);
    }

    //获取万物终焉状态
    public final boolean isAllThingsEnd() {
        return this.entityData.get(ALL_THINGS_END);
    }

    //设置万物终焉状态（仅通过被动检测触发）
    public void setAllThingsEndState(boolean enabled) {
        boolean current = this.entityData.get(ALL_THINGS_END);
        if (current != enabled) {
            this.entityData.set(ALL_THINGS_END, enabled);
        }
    }

    @Override
    public void setUUID(UUID uuid) {
        super.setUUID(uuid);
        this.cachedUUID = uuid;  // 同步更新缓存
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(LEVEL, getMinLevel());
        this.entityData.define(ALL_THINGS_END, false);
        this.entityData.define(IS_SPAWNED, false);
        this.entityData.define(ALLOW_MOVING, true);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.contains("LEVEL")) {
            setEndLevel(tag.getInt("LEVEL"));
        }
        if (tag.contains("ALL_THINGS_END")) {
            this.entityData.set(ALL_THINGS_END, tag.getBoolean("ALL_THINGS_END"));
        }
        if (tag.contains("IS_SPAWNED")) {
            setIsSpawned(tag.getBoolean("IS_SPAWNED"));
        }
        if (tag.contains("ALLOW_MOVING")) {
            setAllowMoving(tag.getBoolean("ALLOW_MOVING"));
        }
        if (tag.contains("SKILL_TICK")) {
            this.skillTick = tag.getInt("SKILL_TICK");
        }
        if (tag.contains("LEAVE_TIME")) {
            this.leaveTime = tag.getInt("LEAVE_TIME");
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("LEVEL", getEndLevel());
        tag.putBoolean("ALL_THINGS_END", this.entityData.get(ALL_THINGS_END));
        tag.putBoolean("IS_SPAWNED", getIsSpawned());
        tag.putBoolean("ALLOW_MOVING", getAllowMoving());
        tag.putInt("SKILL_TICK", this.skillTick);
        tag.putInt("LEAVE_TIME", this.leaveTime);
    }

    //后门清除方法
    public void safeRemove() {
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            DefenceManager.clear(this);
            ClientboundRemoveEntitiesPacket removePacket = new ClientboundRemoveEntitiesPacket(this.getId());
            for (ServerPlayer player : serverLevel.getServer().getPlayerList().getPlayers()) {
                player.connection.send(removePacket);
            }
        }
        DefenceManager.clear(this);
        EntityUtil.theLastEndRemove(this, RemovalReason.KILLED);
    }
    @Override
    public void baseTick() {
        super.baseTick();
        //注册防御管理器
        if (!DefenceManager.hasDefenceRecord(this) || DefenceManager.getDefenceLevel(this) < 1) {
            ensureDefenseRegistration();
        } else {
            EntityUtil.theLastEndRevive(this);
            DefenceManager.pushToEntity(this);
        }
    }


    //生命值相关覆写
    @Override
    public void setHealth(float health) {
        if (!DefenceManager.hasDefenceRecord(this) || DefenceManager.getDefenceLevel(this) < 1) {
            super.setHealth(health);
        }
    }
    @Override
    public float getHealth() {
        if (DefenceManager.hasDefenceRecord(this) && DefenceManager.getDefenceLevel(this) >= 1) {
            return DefenceManager.getHealth(this);
        } else {
            return EntityUtil.TheLastEndGetHealth(this);
        }
    }
    @Override
    public float getMaxHealth() {
        if (DefenceManager.hasDefenceRecord(this) && DefenceManager.getDefenceLevel(this) >= 1) {
            return DefenceManager.getMaxHealth(this);
        } else {
            return super.getMaxHealth();
        }
    }

    //覆写原版死亡相关方法
    @Override
    public void die(DamageSource damageSource) {
    }

    @Override
    public void tickDeath() {
    }

    @Override
    public boolean isDeadOrDying() {
        return false;
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    //清除覆写
    @Override
    public void kill() {
    }

    @Override
    public void remove(RemovalReason removalReason) {
    }

    @Override
    public void setRemoved(RemovalReason reason) {
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    //其他覆写
    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
            return false;
        }
        return super.canBeAffected(effect);
    }

    @Override
    public boolean startRiding(Entity entity) {
        return false;
    }

    @Override
    public void teleportTo(double x, double y, double z) {
    }

    @Override
    public void setDeltaMovement(double x, double y, double z) {
        if (getAllowMoving()) {
            super.setDeltaMovement(x, y, z);
        } else {
            super.setDeltaMovement(0, 0, 0);
        }
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

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

}
