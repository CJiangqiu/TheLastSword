package net.the_last_sword.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.eca.api.EcaAPI;
import net.eca.network.EntityExtensionOverridePacket.MusicData;
import net.eca.util.entity_extension.EntityExtensionManager;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.ai.LostWraithDragonFireBallGoal;
import net.the_last_sword.entity.ai.LostWraithChaseTargetGoal;
import net.the_last_sword.entity.ai.LostWraithEnchantGoal;
import net.the_last_sword.entity.ai.LostWraithEndStrikeGoal;
import net.the_last_sword.entity.ai.LostWraithPunchGoal;
import net.the_last_sword.entity.ai.LostWraithPatienceGoal;
import net.the_last_sword.entity.ai.LostWraithSummonLightningGoal;
import net.the_last_sword.util.EntityUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// 迷失战魂实体
public class LostWraithEntity extends TheLastEndEntity {

    // 技能状态常量
    public static final int STATE_ENCHANT = 3;
    public static final int STATE_PUNCH = 4;
    public static final int STATE_DRAGON_FIREBALL = 5;
    public static final int STATE_LIGHTNING = 6;
    public static final int STATE_END_STRIKE = 7;

    private static final EntityDataAccessor<Integer> TALK_INDEX =
            SynchedEntityData.defineId(LostWraithEntity.class, EntityDataSerializers.INT);

    // 生成动画计时
    private int spawnTick = 0;

    // 耐心机制
    private int patienceTicks = 0;
    private boolean forceEndStrike = false;

    public LostWraithEntity(EntityType<? extends LostWraithEntity> type, Level world) {
        super(type, world);
        setMaxUpStep(0.6f);
        xpReward = 200;
        setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TALK_INDEX, 1);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new LostWraithPatienceGoal(this));
        this.goalSelector.addGoal(1, new LostWraithEnchantGoal(this));
        this.goalSelector.addGoal(2, new LostWraithEndStrikeGoal(this, true));
        this.goalSelector.addGoal(3, new LostWraithPunchGoal(this));
        this.goalSelector.addGoal(4, new LostWraithDragonFireBallGoal(this));
        this.goalSelector.addGoal(5, new LostWraithSummonLightningGoal(this));
        this.goalSelector.addGoal(6, new LostWraithEndStrikeGoal(this, false));
        this.goalSelector.addGoal(7, new LostWraithChaseTargetGoal(this));
        this.goalSelector.addGoal(8, new FloatGoal(this));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
            entity -> entity instanceof Player player && !player.isCreative() && !player.isSpectator()));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, GuardianOfSealedSpireEntity.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.MAX_HEALTH, 400)
                .add(Attributes.ARMOR, 10)
                .add(Attributes.ARMOR_TOUGHNESS, 10)
                .add(Attributes.ATTACK_DAMAGE, 10)
                .add(Attributes.FOLLOW_RANGE, 64);
    }

    @Override
    public final void setTheLastEndLevel(int level) {
        super.setTheLastEndLevel(1);
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
    public String getWaitAnimationName() {
        return "init";
    }

    @Override
    public String getSkillAnimationName(int attackState) {
        return switch (attackState) {
            case STATE_ENCHANT -> "enchant";
            case STATE_PUNCH -> "punch";
            case STATE_DRAGON_FIREBALL -> "dragon_fire_ball";
            case STATE_LIGHTNING -> "summon_lightning";
            case STATE_END_STRIKE -> "end_strike";
            default -> "";
        };
    }

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

    @Override
    protected float getDamageLimit() {
        return (float) TheLastSwordConfiguration.getLostWraithDamageLimitSafely();
    }

    @Override
    protected int getHurtResistTime() {
        return TheLastSwordConfiguration.getLostWraithHurtResistTimeSafely();
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (!isReady()) {
            return false;
        }
        if (source.is(DamageTypes.DRAGON_BREATH)) {
            return false;
        }
        if (source.is(DamageTypes.FALL)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty,
            MobSpawnType reason, @Nullable SpawnGroupData livingdata, @Nullable CompoundTag tag) {
        SpawnGroupData result = super.finalizeSpawn(world, difficulty, reason, livingdata, tag);

        if (!level().isClientSide) {
            setTheLastEndLevel(1);

            float maxHealth = (float) getAttributeValue(Attributes.MAX_HEALTH);
            setWorldAnchorMax(maxHealth);
            setWorldAnchor(maxHealth);

            if (!EntityUtil.hasProtection(this)) {
                EntityUtil.registerDefence(this, maxHealth);
            }

            faceNearestPlayer();
        }

        return result;
    }

    @Override
    protected float getStandingEyeHeight(@NotNull Pose pose, @NotNull EntityDimensions dimensions) {
        return 3.0f;
    }

    @Override
    protected void onUnspawnedTick() {
        // 等待激活状态，不执行任何逻辑
    }

    @Override
    protected void onSpawningTick() {
        spawnTick++;

        if (spawnTick >= getSpawnAnimationDuration()) {
            setAnimationState(STATE_IDLE);
            spawnTick = 0;
        }

        this.refreshDimensions();
    }

    @Override
    public void tick() {
        super.tick();
        this.refreshDimensions();
    }

    @Override
    public int getSpawnAnimationDuration() {
        return 110;
    }

    @Override
    public int getDeathAnimationDuration() {
        return 50;
    }

    @Override
    protected void onDeathStart() {
        sendDeathTalkToNearbyPlayers();

        if (level() instanceof ServerLevel serverLevel) {
            EcaAPI.clearGlobalMusic(serverLevel);
        }
    }

    public void activate() {
        if (getAnimationState() == STATE_UNSPAWNED) {
            setAnimationState(STATE_SPAWNING);
            spawnTick = 0;

            if (level() instanceof ServerLevel serverLevel) {
                // 触发ECA创建BossEvent
                EntityExtensionManager.onEntityJoin(this, serverLevel);

                // 为附近玩家补发追踪事件，使其能看到BossBar
                for (ServerPlayer sp : serverLevel.getEntitiesOfClass(
                        ServerPlayer.class, getBoundingBox().inflate(64.0))) {
                    EntityExtensionManager.onStartTracking(sp, this);
                }

                // 通过ECA API播放战斗音乐
                MusicData musicData = new MusicData(
                    new ResourceLocation("the_last_sword", "lost_wraith"),
                    SoundSource.MUSIC.ordinal(),
                    1.0f, 1.0f, true, true
                );
                EcaAPI.setGlobalMusic(serverLevel, musicData);
            }
        }
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (itemstack.getItem() == Items.DRAGON_EGG && !isReady()) {
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

        // 未激活时，除龙蛋以外的任何物品（包括空手）右键都会继续对话。
        if (!isReady()) {
            if (!level().isClientSide) {
                sendNextTalk(player);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public void setDeltaMovement(double x, double y, double z) {
        if (isDying()) {
            return;
        }
        if (getAnimationState() == STATE_SPAWNING) {
            super.setDeltaMovement(x, y, z);
            return;
        }
        if (isReady()) {
            super.setDeltaMovement(x, y, z);
            return;
        }
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(@NotNull Entity entity) {
        if (!isReady()) {
            return;
        }
        super.push(entity);
    }

    private void sendDeathTalkToNearbyPlayers() {
        if (level().isClientSide) return;

        List<Player> nearbyPlayers = level().getEntitiesOfClass(Player.class,
                getBoundingBox().inflate(32.0));

        Map<UUID, Player> uniquePlayers = new HashMap<>();
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

        if (currentIndex < 7) {
            setTalkIndex(currentIndex + 1);
        }
    }

    public int getTalkIndex() {
        return this.entityData.get(TALK_INDEX);
    }

    public void setTalkIndex(int index) {
        this.entityData.set(TALK_INDEX, Math.max(1, Math.min(7, index)));
    }

    public int incrementPatienceTick() {
        return ++patienceTicks;
    }

    public void resetPatience() {
        patienceTicks = 0;
    }

    public void setForceEndStrike(boolean force) {
        forceEndStrike = force;
    }

    public boolean isForceEndStrike() {
        return forceEndStrike;
    }

    public void clearForceEndStrike() {
        forceEndStrike = false;
    }

    public boolean consumeForceEndStrike() {
        if (!forceEndStrike) {
            return false;
        }
        forceEndStrike = false;
        return true;
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

    private void faceNearestPlayer() {
        Player nearestPlayer = level().getNearestPlayer(this, 32.0);
        if (nearestPlayer != null) {
            EntityUtil.faceTarget(this, nearestPlayer);
        }
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setTheLastEndLevel(1);
        if (compound.contains("TalkIndex")) {
            setTalkIndex(compound.getInt("TalkIndex"));
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("TalkIndex", getTalkIndex());
    }
}
