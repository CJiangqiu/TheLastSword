package net.the_last_sword.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.dialogue.NpcDialogueManager;
import net.the_last_sword.dialogue.NpcDialogueProvider;
import net.the_last_sword.dialogue.NpcDialogueRegistry;
import net.the_last_sword.entity.ai.ThePastShadowOfTheQueenChaseTargetGoal;
import net.the_last_sword.entity.ai.ThePastShadowOfTheQueenEnchantGoal;
import net.the_last_sword.entity.ai.ThePastShadowOfTheQueenExecutionGoal;
import net.the_last_sword.entity.ai.ThePastShadowOfTheQueenHurtByTargetGoal;
import net.the_last_sword.entity.ai.ThePastShadowOfTheQueenLightningSpearGoal;
import net.the_last_sword.entity.ai.ThePastShadowOfTheQueenBlinkGoal;
import net.the_last_sword.entity.ai.ThePastShadowOfTheQueenTripleSlashGoal;
import net.the_last_sword.entity.ai.ThePastShadowOfTheQueenSummonProjectilesGoal;
import net.the_last_sword.event.TheLastSwordQuestHandler;
import net.the_last_sword.init.ModItems;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.util.EntityUtil;
import net.the_last_sword.util.health.TrueHealthManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// 女皇的逝去之影
public class ThePastShadowOfTheQueenEntity extends TheLastEndEntity implements NpcDialogueProvider {
    private static final float SKILL_TALK_CHANCE = 0.30F;
    private static final double SKILL_TALK_RADIUS = 32.0;
    private static final String TALK_KEY_PREFIX = "talk.the_past_shadow_of_the_queen.";
    private static final int DEATH_ANIMATION_DURATION = 160;
    private static final int DEATH_SECOND_TALK_TICK = 60;
    private static final int DEATH_ITEM_APPEAR_TICK = 70;
    private static final int DEATH_REWARD_TICK = 100;
    private static final String DEATH_REWARD_GRANTED_TAG = "QueenDeathRewardGranted";
    private static final String NPC_STATE_TAG = "QueenNpcState";
    public static final int STATE_LIGHTNING_SPEAR = 3;
    public static final int STATE_BLINK = 4;
    public static final int STATE_TRIPLE_SLASH = 5;
    public static final int STATE_SUMMON_PROJECTILES = 6;
    public static final int STATE_ENCHANT = 7;
    public static final int STATE_EXECUTION = 8;
    public static final int STATE_EXECUTION_FAIL = 9;
    public static final int STATE_EXECUTION_SUCCESS = 10;
    public static final int STATE_NPC = 11;
    private ThePastShadowOfTheQueenLightningSpearGoal lightningSpearGoal;
    private ThePastShadowOfTheQueenBlinkGoal blinkGoal;
    private ThePastShadowOfTheQueenSummonProjectilesGoal summonProjectilesGoal;
    private ThePastShadowOfTheQueenEnchantGoal enchantGoal;
    private ThePastShadowOfTheQueenExecutionGoal executionGoal;
    private static final EntityDataAccessor<Integer> SUMMON_TICK = SynchedEntityData.defineId(
            ThePastShadowOfTheQueenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> SUMMON_YAW = SynchedEntityData.defineId(
            ThePastShadowOfTheQueenEntity.class, EntityDataSerializers.FLOAT);
    private ThePastShadowOfTheQueenTripleSlashGoal tripleSlashGoal;
    private static final EntityDataAccessor<Integer> BLINK_TICK = SynchedEntityData.defineId(
            ThePastShadowOfTheQueenEntity.class, EntityDataSerializers.INT);
    private boolean blinkPhysicsActive;
    private boolean beforeBlinkNoPhysics;
    private static final EntityDataAccessor<Boolean> LIGHTNING_SPEAR_VISIBLE = SynchedEntityData.defineId(
            ThePastShadowOfTheQueenEntity.class, EntityDataSerializers.BOOLEAN);
    private boolean deathRewardGranted;

    public ThePastShadowOfTheQueenEntity(EntityType<? extends ThePastShadowOfTheQueenEntity> type, Level level) {
        super(type, level);
        xpReward = 1024;
        setMaxUpStep(0.6f);
        setPersistenceRequired();
    }

    public void trySendSkillTalk(String skillId) {
        if (level().isClientSide || skillId == null || skillId.isBlank()
                || getRandom().nextFloat() >= SKILL_TALK_CHANCE) {
            return;
        }

        Component message = createTalkMessage(skillId);
        double radiusSqr = SKILL_TALK_RADIUS * SKILL_TALK_RADIUS;
        for (Player player : level().getEntitiesOfClass(Player.class,
                getBoundingBox().inflate(SKILL_TALK_RADIUS), player -> distanceToSqr(player) <= radiusSqr)) {
            player.sendSystemMessage(message);
        }
    }

    private Component createTalkMessage(String talkId) {
        return Component.literal("[")
                .append(getDisplayName())
                .append(Component.literal("] "))
                .withStyle(ChatFormatting.DARK_PURPLE)
                .append(Component.translatable(TALK_KEY_PREFIX + talkId)
                        .withStyle(ChatFormatting.WHITE));
    }

    private void sendDeathTalk(String talkId) {
        if (!level().isClientSide && getKillCredit() instanceof ServerPlayer player) {
            player.sendSystemMessage(createTalkMessage(talkId));
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(LIGHTNING_SPEAR_VISIBLE, false);
        entityData.define(BLINK_TICK, -1);
        entityData.define(SUMMON_TICK, -1);
        entityData.define(SUMMON_YAW, 0F);
    }

    public int getSummonTick() {
        return getAnimationState() == STATE_SUMMON_PROJECTILES ? entityData.get(SUMMON_TICK) : -1;
    }

    public void setSummonTick(int tick) {
        entityData.set(SUMMON_TICK, tick);
    }

    public float getSummonYaw() {
        return entityData.get(SUMMON_YAW);
    }

    public void setSummonYaw(float yaw) {
        entityData.set(SUMMON_YAW, yaw);
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return getSummonTick() >= 0 ? super.getBoundingBoxForCulling().inflate(3, 6, 3)
                : super.getBoundingBoxForCulling();
    }

    public boolean isLightningSpearVisible() {
        return getAnimationState() == STATE_LIGHTNING_SPEAR && !isDying()
                && entityData.get(LIGHTNING_SPEAR_VISIBLE);
    }

    public void setLightningSpearVisible(boolean visible) {
        entityData.set(LIGHTNING_SPEAR_VISIBLE, visible);
    }

    public boolean isNpc() {
        return getAnimationState() == STATE_NPC;
    }

    @Override
    public String getDialogueId() {
        return NpcDialogueRegistry.QUEEN_DIALOGUE_ID;
    }

    @Override
    public boolean canStartDialogue(Player player) {
        return isNpc() && isAlive();
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if (isNpc()) {
            if (!level().isClientSide && player instanceof ServerPlayer serverPlayer) {
                NpcDialogueManager.open(serverPlayer, this);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean canAct() {
        return !isNpc() && super.canAct();
    }

    public int getBlinkTick() {
        return getAnimationState() == STATE_BLINK ? entityData.get(BLINK_TICK) : -1;
    }

    public void setBlinkTick(int tick) {
        entityData.set(BLINK_TICK, tick);
        updateBlinkPhysics();
    }

    public boolean isBlinkPhased() {
        int tick = getBlinkTick();
        return tick >= 20 && tick < 45;
    }

    private void updateBlinkPhysics() {
        if (isBlinkPhased() && !blinkPhysicsActive) {
            beforeBlinkNoPhysics = noPhysics;
            blinkPhysicsActive = true;
            noPhysics = true;
            setNoGravity(true);
            // 仅在进入消失阶段时停止惯性，显形后保留重力累积的下落速度。
            setDeltaMovement(Vec3.ZERO);
            refreshDimensions();
        } else if (!isBlinkPhased() && blinkPhysicsActive) {
            blinkPhysicsActive = false;
            // 恢复尺寸时保持无碰撞，避免尺寸恢复逻辑额外挪动女皇。
            refreshDimensions();
            noPhysics = beforeBlinkNoPhysics;
            setNoGravity(false);
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return isBlinkPhased() ? EntityDimensions.fixed(0.0F, 0.0F) : super.getDimensions(pose);
    }

    @Override
    public boolean isPickable() {
        return !isBlinkPhased() && super.isPickable();
    }

    @Override
    public boolean isPushable() {
        return !isBlinkPhased() && super.isPushable();
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return !isBlinkPhased() && super.canCollideWith(entity);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (BLINK_TICK.equals(key)) {
            updateBlinkPhysics();
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new FloatGoal(this));
        lightningSpearGoal = new ThePastShadowOfTheQueenLightningSpearGoal(this);
        this.goalSelector.addGoal(2, lightningSpearGoal);
        blinkGoal = new ThePastShadowOfTheQueenBlinkGoal(this);
        this.goalSelector.addGoal(2, blinkGoal);
        tripleSlashGoal = new ThePastShadowOfTheQueenTripleSlashGoal(this);
        this.goalSelector.addGoal(2, tripleSlashGoal);
        summonProjectilesGoal = new ThePastShadowOfTheQueenSummonProjectilesGoal(this);
        this.goalSelector.addGoal(2, summonProjectilesGoal);
        enchantGoal = new ThePastShadowOfTheQueenEnchantGoal(this);
        this.goalSelector.addGoal(2, enchantGoal);
        executionGoal = new ThePastShadowOfTheQueenExecutionGoal(this);
        this.goalSelector.addGoal(2, executionGoal);
        this.goalSelector.addGoal(3, new ThePastShadowOfTheQueenChaseTargetGoal(this));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new ThePastShadowOfTheQueenHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                entity -> entity instanceof Player player
                        && !isNpc()
                        && !player.isCreative()
                        && !player.isSpectator()
                        && EntityUtil.canAttack(this, player)));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.MAX_HEALTH, 20000)
                .add(Attributes.ARMOR, 0)
                .add(Attributes.ARMOR_TOUGHNESS, 0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.ATTACK_DAMAGE, 200)
                .add(Attributes.FOLLOW_RANGE, 64);
    }

    @Override
    protected float getDamageLimit() {
        float maxHealth = (float) getAttributeValue(Attributes.MAX_HEALTH);
        float ratio = (float) TheLastSwordConfiguration.getDefenceCustomHealthDamageReductionSafely();
        float maxDamage = (float) TheLastSwordConfiguration.getThePastShadowOfTheQueenDamageLimitSafely();
        return Math.min(maxHealth * ratio, maxDamage);
    }

    @Override
    public final void setTheLastEndLevel(int level) {
        super.setTheLastEndLevel(6);
    }

    @Override
    public String getIdleAnimationName() {
        return "idle";
    }

    @Override
    public String getWalkAnimationName() {
        return "walk";
    }

    @Override
    public String getDeathAnimationName() {
        return "death";
    }

    @Override
    public int getDeathAnimationDuration() {
        return DEATH_ANIMATION_DURATION;
    }

    @Override
    public String getSpawnAnimationName() {
        return "wake";
    }

    @Override
    public String getWaitAnimationName() {
        return "init";
    }

    @Override
    protected String getPersistentAnimationName(int animationState) {
        return animationState == STATE_NPC ? getIdleAnimationName() : super.getPersistentAnimationName(animationState);
    }

    @Override
    public String getSkillAnimationName(int attackState) {
        if (attackState == STATE_EXECUTION) {
            return "execution";
        }
        if (attackState == STATE_EXECUTION_FAIL) {
            return "execution_fail";
        }
        if (attackState == STATE_EXECUTION_SUCCESS) {
            return "execution_success";
        }
        if (attackState == STATE_ENCHANT) {
            return "enchant";
        }
        if (attackState == STATE_SUMMON_PROJECTILES) {
            return "summon_projectiles";
        }
        if (attackState == STATE_TRIPLE_SLASH) {
            return "triple_slash";
        }
        if (attackState == STATE_LIGHTNING_SPEAR) {
            return "lightning_spear";
        }
        if (attackState == STATE_BLINK) {
            return "blink";
        }
        return "";
    }

    @Override
    public SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
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
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (!isReady() || isNpc()) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        return !isNpc() && super.doHurtTarget(target);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(isNpc() ? null : target);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty,
            MobSpawnType reason, @Nullable SpawnGroupData livingData, @Nullable CompoundTag tag) {
        SpawnGroupData result = super.finalizeSpawn(world, difficulty, reason, livingData, tag);

        if (!level().isClientSide) {
            setTheLastEndLevel(6);

            float maxHealth = (float) getAttributeValue(Attributes.MAX_HEALTH);
            if (!EntityUtil.hasProtection(this)) {
                TrueHealthManager.register(this, maxHealth);
            }
        }

        return result;
    }

    @Override
    protected void onSpawningTick() {
        this.refreshDimensions();
    }

    public void startWakeAnimation() {
        if (getAnimationState() == STATE_UNSPAWNED) {
            setAnimationState(STATE_SPAWNING);
        }
    }

    public void activate() {
        if (isReady() || isDying()) {
            return;
        }

        setAnimationState(STATE_IDLE);
    }

    @Override
    protected void onDeathStart() {
        if (tripleSlashGoal != null) tripleSlashGoal.stop();
        if (summonProjectilesGoal != null) summonProjectilesGoal.stop();
        if (enchantGoal != null) enchantGoal.stop();
        if (executionGoal != null) executionGoal.stop();
        setBlinkTick(-1);
        setLightningSpearVisible(false);
        setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        sendDeathTalk("death_1");
    }

    private void tickDeathSequence() {
        int deathTick = getDeathTick();
        if (deathTick == DEATH_SECOND_TALK_TICK) {
            sendDeathTalk("death_2");
        } else if (deathTick == DEATH_ITEM_APPEAR_TICK) {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.THE_GIVERS_PAIN.get()));
        } else if (deathTick == DEATH_REWARD_TICK) {
            setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            giveDeathReward();
        }
    }

    private void giveDeathReward() {
        if (deathRewardGranted || !(getKillCredit() instanceof ServerPlayer player)) {
            return;
        }
        deathRewardGranted = true;
        ItemStack reward = new ItemStack(ModItems.THE_GIVERS_PAIN.get());
        player.getInventory().add(reward);
        if (!reward.isEmpty()) {
            player.drop(reward, false);
        }
        if (level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            dropFromLootTable(damageSources().playerAttack(player), true);
        }
        TheLastSwordQuestHandler.grant(player, TheLastSwordQuestHandler.PROOF_OF_SOVEREIGNTY);
    }

    @Override
    protected void onDeathEnd() {
        if (level().isClientSide) {
            return;
        }

        finishDeathWithoutRemoval(STATE_NPC);
        getNavigation().stop();
        setTarget(null);
        setLastHurtByMob(null);
        setLastHurtMob(null);
        setAggressive(false);
        setDeltaMovement(Vec3.ZERO);
        setNoGravity(false);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        deathRewardGranted = tag.getBoolean(DEATH_REWARD_GRANTED_TAG);
        if (tag.getBoolean(NPC_STATE_TAG)) {
            setDeathTick(0);
            setAnimationState(STATE_NPC);
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean(DEATH_REWARD_GRANTED_TAG, deathRewardGranted);
        tag.putBoolean(NPC_STATE_TAG, isNpc());
    }

    @Override
    protected float getStandingEyeHeight(@NotNull Pose pose, @NotNull EntityDimensions dimensions) {
        return 3.0F;
    }

    @Override
    public void setAnimationState(int state) {
        int previousState = getAnimationState();
        super.setAnimationState(state);
        boolean executionTransition = isExecutionState(previousState) && isExecutionState(state);
        if (!level().isClientSide && previousState != state && !executionTransition) {
            // 先切换状态再清理，避免 stop 再次触发同一技能的退出。
            switch (previousState) {
                case STATE_LIGHTNING_SPEAR -> {
                    if (lightningSpearGoal != null) lightningSpearGoal.stop();
                }
                case STATE_BLINK -> {
                    if (blinkGoal != null) blinkGoal.stop();
                }
                case STATE_TRIPLE_SLASH -> {
                    if (tripleSlashGoal != null) tripleSlashGoal.stop();
                }
                case STATE_SUMMON_PROJECTILES -> {
                    if (summonProjectilesGoal != null) summonProjectilesGoal.stop();
                }
                case STATE_ENCHANT -> {
                    if (enchantGoal != null) enchantGoal.stop();
                }
                case STATE_EXECUTION, STATE_EXECUTION_FAIL, STATE_EXECUTION_SUCCESS -> {
                    if (executionGoal != null) executionGoal.stop();
                }
                default -> { }
            }
        }
        if (!level().isClientSide && state == STATE_IDLE) {
            setNoGravity(false);
        }
    }

    @Override
    public void tick() {
        // 旧存档可能保留技能的无重力标记，必须在 AI 起手前恢复。
        if (!level().isClientSide && getAnimationState() == STATE_IDLE) {
            setNoGravity(false);
        }
        super.tick();
        if (!level().isClientSide && isDying()) {
            tickDeathSequence();
        }
        if (!level().isClientSide && tripleSlashGoal != null) tripleSlashGoal.holdPose();
        if (!level().isClientSide && summonProjectilesGoal != null) summonProjectilesGoal.holdPose();
        if (!level().isClientSide && executionGoal != null) executionGoal.holdPose();
        updateBlinkPhysics();
        if (!level().isClientSide && getAnimationState() == STATE_IDLE) {
            setNoGravity(false);
        }
        if (!level().isClientSide && getAnimationState() != STATE_LIGHTNING_SPEAR) {
            setLightningSpearVisible(false);
        }
        this.refreshDimensions();
    }

    private static boolean isExecutionState(int state) {
        return state == STATE_EXECUTION || state == STATE_EXECUTION_FAIL || state == STATE_EXECUTION_SUCCESS;
    }
}
