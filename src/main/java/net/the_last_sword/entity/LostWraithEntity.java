package net.the_last_sword.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.the_last_sword.entity.ai.LostWraithAI;
import net.the_last_sword.entity.ai.TheLastEndAI;
import net.the_last_sword.util.EntityUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

//迷失战魂实体
public class LostWraithEntity extends TheLastEndEntity {

    //对话索引
    private static final EntityDataAccessor<Integer> TALK_INDEX =
            SynchedEntityData.defineId(LostWraithEntity.class, EntityDataSerializers.INT);

    public LostWraithEntity(EntityType<? extends LostWraithEntity> type, Level world) {
        super(type, world);
        setMaxUpStep(0.6f);
        xpReward = 200;
        setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TALK_INDEX, 1);  //默认从第1句开始
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new FloatGoal(this));
    }

    //属性
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.MAX_HEALTH, 200)
                .add(Attributes.ARMOR, 0)
                .add(Attributes.ATTACK_DAMAGE, 10)
                .add(Attributes.FOLLOW_RANGE, 64);
    }

    @Override
    public final void setTheLastEndLevel(int level) {
        super.setTheLastEndLevel(1);  //固定1级
    }

    //创建AI实例
    @Override
    public TheLastEndAI createAI() {
        return new LostWraithAI(this);
    }

    //动画名称
    @Override
    public String getIdleAnimationName() {
        return "idle";
    }

    @Override
    public String getMovementAnimationName() {
        return "idle";  //迷失战魂没有移动动画，始终用idle
    }

    @Override
    public String getDeathAnimationName() {
        return "death";
    }

    @Override
    public String getSpawnAnimationName() {
        return "spawn";
    }

    //声音
    @Override
    public SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_SKELETON_AMBIENT;
    }

    @Override
    public SoundEvent getHurtSound(@NotNull DamageSource ds) {
        return SoundEvents.WITHER_SKELETON_HURT;
    }

    @Override
    public SoundEvent getDeathSound() {
        return SoundEvents.WITHER_SKELETON_DEATH;
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    //限伤值
    @Override
    protected float getDamageLimit() {
        return 10.0f;
    }

    //无敌帧
    @Override
    protected int getAllowHurtTime() {
        return 10;
    }

    //伤害免疫
    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        //未激活时免疫所有伤害
        if (!isSpawned()) {
            return false;
        }
        //免疫龙息伤害
        if (source.is(DamageTypes.DRAGON_BREATH)) {
            return false;
        }
        //免疫掉落伤害
        if (source.is(DamageTypes.FALL)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    //生成时初始化
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty,
            MobSpawnType reason, @Nullable SpawnGroupData livingdata, @Nullable CompoundTag tag) {
        SpawnGroupData result = super.finalizeSpawn(world, difficulty, reason, livingdata, tag);

        //仅在服务端初始化
        if (!level().isClientSide) {
            //初始化等级为1
            setTheLastEndLevel(1);

            //标记为未激活
            setSpawned(false);

            //提前注册防御系统（确保 IS_PROTECTED 和 HEALTH_LOCK_ENABLED 同步）
            float maxHealth = (float) getAttributeValue(Attributes.MAX_HEALTH);
            if (!EntityUtil.hasProtection(this)) {
                EntityUtil.registerDefence(this, maxHealth);
            }

            //面向最近的玩家
            faceNearestPlayer();
        }

        return result;
    }

    @Override
    protected float getStandingEyeHeight(@NotNull Pose pose, @NotNull EntityDimensions dimensions) {
        return 3.0f;  //固定眼睛高度为3格
    }

    @Override
    public void tick() {
        //未激活状态：保持 init 动画，不执行 AI
        if (!isSpawned()) {
            //正在播放生成动画
            if (getSpawnTick() > 0) {
                setSpawnTick(getSpawnTick() + 1);
                setAnimation("spawn");

                //生成动画播放完毕
                if (getSpawnTick() >= getSpawnAnimationDuration()) {
                    setSpawned(true);
                    setSpawnTick(0);
                    setAnimation(getIdleAnimationName());
                }
            } else {
                //等待激活，保持 init 动画
                setAnimation("init");
            }
            //不调用 super.tick()，禁用 AI
            return;
        }

        super.tick();
        this.refreshDimensions();
    }

    //生成动画时长（5.5秒）
    @Override
    protected int getSpawnAnimationDuration() {
        return 110;
    }

    //死亡动画时长
    @Override
    public int getDeathAnimationDuration() {
        return 50;  //50tick（2.5秒）
    }

    //死亡开始时发送对话
    @Override
    protected void onDeathStart() {
        sendDeathTalkToNearbyPlayers();
    }

    //激活迷失战魂
    public void activate() {
        if (!isSpawned() && getSpawnTick() == 0) {
            setSpawnTick(1);  //开始计时
            setAnimation("spawn");
        }
    }

    //玩家交互
    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        //龙蛋激活
        if (itemstack.getItem() == Items.DRAGON_EGG && !isSpawned()) {
            if (!level().isClientSide) {
                activate();

                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }

                sendActivationTalk(player);

                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENDER_DRAGON_GROWL, this.getSoundSource(), 1.0F, 1.0F);

                spawnActivationParticles();
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }

        //空手对话
        if (itemstack.isEmpty() && !isSpawned()) {
            if (!level().isClientSide) {
                sendNextTalk(player);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }

        return super.mobInteract(player, hand);
    }

    //运动控制
    @Override
    public void setDeltaMovement(double x, double y, double z) {
        //未激活或死亡动画期间不允许移动
        if (!isSpawned() || shouldLeave()) {
            return;
        }
        super.setDeltaMovement(x, y, z);
    }

    @Override
    public boolean isPushable() {
        return false;  //不允许被推动
    }

    @Override
    public void push(@NotNull Entity entity) {
        //未激活时不允许被推动
        if (!isSpawned()) {
            return;
        }
        super.push(entity);
    }

    //对话系统
    private void sendDeathTalkToNearbyPlayers() {
        if (level().isClientSide) return;

        List<Player> nearbyPlayers = level().getEntitiesOfClass(Player.class,
                getBoundingBox().inflate(32.0));

        //按UUID去重
        java.util.Map<java.util.UUID, Player> uniquePlayers = new java.util.HashMap<>();
        for (Player p : nearbyPlayers) {
            uniquePlayers.putIfAbsent(p.getUUID(), p);
        }

        if (!uniquePlayers.isEmpty()) {
            Component prefixedMessage = Component.literal("[")
                .append(Component.translatable("entity.the_last_sword.lost_wraith"))
                .append(Component.literal("] "))
                .withStyle(ChatFormatting.DARK_PURPLE)
                .append(Component.translatable("talk.lost_wraith.death").copy().withStyle(ChatFormatting.WHITE));

            for (Player player : uniquePlayers.values()) {
                player.sendSystemMessage(prefixedMessage);
            }
        }
    }

    private void sendNextTalk(Player player) {
        if (player.level().isClientSide()) return;

        int currentIndex = getTalkIndex();
        String translationKey = "talk.lost_wraith.init_" + currentIndex;

        Component prefixedMessage = Component.literal("[")
            .append(Component.translatable("entity.the_last_sword.lost_wraith"))
            .append(Component.literal("] "))
            .withStyle(ChatFormatting.DARK_PURPLE)
            .append(Component.translatable(translationKey).copy().withStyle(ChatFormatting.WHITE));

        player.sendSystemMessage(prefixedMessage);

        //递增对话索引，最大为7（之后一直重复第7句）
        if (currentIndex < 7) {
            setTalkIndex(currentIndex + 1);
        }
    }

    //对话索引
    public int getTalkIndex() {
        return this.entityData.get(TALK_INDEX);
    }

    public void setTalkIndex(int index) {
        this.entityData.set(TALK_INDEX, Math.max(1, Math.min(7, index)));
    }

    private void sendActivationTalk(Player player) {
        if (player.level().isClientSide()) return;

        Component prefixedMessage = Component.literal("[")
            .append(Component.translatable("entity.the_last_sword.lost_wraith"))
            .append(Component.literal("] "))
            .withStyle(ChatFormatting.DARK_PURPLE)
            .append(Component.translatable("talk.lost_wraith.activation").copy().withStyle(ChatFormatting.WHITE));

        player.sendSystemMessage(prefixedMessage);
    }

    //粒子效果
    private void spawnActivationParticles() {
        if (level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 20; i++) {
                double d0 = random.nextGaussian() * 0.02D;
                double d1 = random.nextGaussian() * 0.02D;
                double d2 = random.nextGaussian() * 0.02D;
                serverLevel.sendParticles(ParticleTypes.PORTAL,
                    this.getX() + (random.nextDouble() - 0.5D) * 2.0D,
                    this.getY() + random.nextDouble() * 2.0D,
                    this.getZ() + (random.nextDouble() - 0.5D) * 2.0D,
                    1, d0, d1, d2, 0.1D);
            }

            for (int i = 0; i < 15; i++) {
                serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH,
                    this.getX() + (random.nextDouble() - 0.5D) * 1.5D,
                    this.getY() + random.nextDouble() * 1.5D + 0.5D,
                    this.getZ() + (random.nextDouble() - 0.5D) * 1.5D,
                    1, 0, 0.1D, 0, 0.02D);
            }
        }
    }

    //辅助方法
    private void faceNearestPlayer() {
        Player nearestPlayer = level().getNearestPlayer(this, 32.0);
        if (nearestPlayer != null) {
            EntityUtil.faceTarget(this, nearestPlayer);
        }
    }

    //NBT
    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setTheLastEndLevel(1);  //确保等级固定为1
        if (compound.contains("TalkIndex")) {
            setTalkIndex(compound.getInt("TalkIndex"));
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LEVEL", 1);
        compound.putInt("TalkIndex", getTalkIndex());
    }
}
