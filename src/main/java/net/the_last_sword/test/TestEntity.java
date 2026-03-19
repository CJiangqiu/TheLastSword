//本Mod中唯二可以划分到秒杀的东西，检验神器攻击是否具有反射列表清除或更强的清除，以及测试被攻击的其他实体是否具有线程复活和无法选中。
package net.the_last_sword.test;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.eca.api.EcaAPI;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.init.ModEffects;
import net.the_last_sword.util.EntityUtil;

public class TestEntity extends PathfinderMob {

    /*────────────────────  显示名渐变：先准备  ────────────────────*/
    private static final int GRADIENT_START = 0x800080;   // 紫
    private static final int GRADIENT_END   = 0x000000;   // 黑

    private static MutableComponent gradientBold(String txt) {
        MutableComponent result = Component.literal("");
        int len = txt.length();
        for (int i = 0; i < len; i++) {
            float t = len == 1 ? 0f : (float) i / (len - 1);
            int rgb = blend(GRADIENT_START, GRADIENT_END, t);
            result.append(Component.literal(String.valueOf(txt.charAt(i)))
                    .withStyle(st -> st.withBold(true)
                            .withColor(TextColor.fromRgb(rgb))));
        }
        return result;
    }
    private static int blend(int c1, int c2, float t) {
        int r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF;
        int r2 = (c2 >> 16) & 0xFF, g2 = (c2 >> 8) & 0xFF, b2 = c2 & 0xFF;
        int r = Mth.clamp((int) (r1 + (r2 - r1) * t), 0, 255);
        int g = Mth.clamp((int) (g1 + (g2 - g1) * t), 0, 255);
        int b = Mth.clamp((int) (b1 + (b2 - b1) * t), 0, 255);
        return (r << 16) | (g << 8) | b;
    }

    /*────────────────────  BOSS 条  ────────────────────*/
    private final ServerBossEvent bossInfo =
            new ServerBossEvent(Component.empty(), BossEvent.BossBarColor.PURPLE,
                    BossEvent.BossBarOverlay.NOTCHED_20);

    /*────────────── 构造 ─────────────*/
    public TestEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        setMaxUpStep(2f);
        xpReward = 100;
        setNoAi(false);
        setPersistenceRequired();
        if (bossInfo != null) {
            bossInfo.setName(getDisplayName());
        }
    }

    @Mod.EventBusSubscriber(modid = TheLastSwordMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class EntityRegisterHook {

        @SubscribeEvent
        public static void TheLastSwordTestEntityOnJoin(EntityJoinLevelEvent e) {
            if (e.getLevel().isClientSide()) return;
            if (e.getEntity() instanceof TestEntity te) {
                //注册防御（如果还没有防御数据）
                EntityUtil.registerDefence(te, te.getMaxHealth());
                EcaAPI.lockLocation(te);
            }
        }
    }

    /*────────────── 显示名（渐变＋粗体）──────────────*/
    @Override
    public Component getDisplayName() {
        String raw = Component.translatable("entity.the_last_sword.test_entity").getString();
        return gradientBold(raw);
    }


    /*────────────── AI ─────────────*/
    @Override
    protected void registerGoals() {
        this.getNavigation().getNodeEvaluator().setCanOpenDoors(true);
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }


    @Override public SoundEvent getHurtSound(DamageSource ds) {
        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.ender_dragon.hurt"));
    }
    @Override public SoundEvent getDeathSound() {
        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.ender_dragon.death"));
    }

    @Override public void startSeenByPlayer(ServerPlayer p) {
        super.startSeenByPlayer(p);
        if (bossInfo != null) {
            bossInfo.addPlayer(p);
        }
    }
    @Override public void stopSeenByPlayer(ServerPlayer p) {
        super.stopSeenByPlayer(p);
        if (bossInfo != null) {
            bossInfo.removePlayer(p);
        }
    }
    @Override public void customServerAiStep() {
        super.customServerAiStep();
        if (bossInfo != null) {
            bossInfo.setProgress(getHealth() / getMaxHealth());
        }
    }

    /*──────────────── 每 tick 逻辑 ────────────────*/
    @Override
    public void baseTick() {
        super.baseTick();
        // 1. 设置常数满血量
        EntityUtil.theLastEndSetHealth(this,1024f);

        // 2. 每tick强力范围攻击
        if (!this.level().isClientSide) {
            Vec3 center = this.position();
            PowerfulRangeAttack.execute(this.level(), this, center);
        }

        // 3. 每tick自带12秒虚化Buff（240 ticks）
        if (!this.level().isClientSide) {
            this.addEffect(new MobEffectInstance(ModEffects.PHASING.get(), 240, 0, false, false));
        }
    }

    @Override
    public void die(DamageSource cause) {
    }

    @Override
    public void kill() {
    }

    @Override
    public boolean isDeadOrDying() {
        return false;
    }

    @Override
    protected void tickDeath() {
    }


    @Override
    protected void dropExperience() {

    }

    @Override
    public void remove(RemovalReason reason) {
    }

    @Override
    public float getMaxHealth() {
        return 1024;
    }

    @Override
    public float getHealth() {
        return this.getMaxHealth();
    }

    @Override
    public void setHealth(float health) {
        EntityUtil.theLastEndSetHealth(this, health);
    }

    @Override
    public void heal(float amt) { this.setHealth(getMaxHealth()); }

    @Override
    public void setRemoved(RemovalReason reason) {
    }

    @Override
    public void setInvisible(boolean invisible) {
    }

    @Override
    public boolean isNoAi() {
        return false;
    }

    @Override
    public void setNoAi(boolean noAi) {
    }

    @Override protected boolean shouldDespawnInPeaceful() { return false; }
    @Override public void checkDespawn(){}
    @Override public boolean removeWhenFarAway(double d) { return false; }
    @Override public double getMyRidingOffset() { return -0.35D; }
    @Override public boolean isPushable() { return false; }
    @Override protected void doPush(Entity e) {}
    @Override protected void pushEntities() {}
    @Override public boolean canChangeDimensions() { return false; }
    @Override public void knockback(double s, double x, double z) {}
    @Override public void travel(Vec3 v) {
    }
    @Override public void setDeltaMovement(Vec3 m) {
    }
    @Override public void moveTo(double x, double y, double z, float yaw, float pitch) {

    }

    public void safeRemove() {
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            ClientboundRemoveEntitiesPacket removePacket = new ClientboundRemoveEntitiesPacket(this.getId());
            for (ServerPlayer player : serverLevel.getServer().getPlayerList().getPlayers()) {
                player.connection.send(removePacket);
            }
        }

        EntityUtil.clearDefence(this);
        EntityUtil.theLastEndRemove(this, RemovalReason.KILLED);
    }

    /*────────────── 属性注册 ─────────────*/
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED,        0.3)
                .add(Attributes.MAX_HEALTH,           1024)
                .add(Attributes.ARMOR,                20)
                .add(Attributes.ATTACK_DAMAGE,        Float.MAX_VALUE)
                .add(Attributes.FOLLOW_RANGE,         64)
                .add(Attributes.KNOCKBACK_RESISTANCE,Float.MAX_VALUE)
                .add(Attributes.ATTACK_KNOCKBACK,     Float.MAX_VALUE);
    }

    public static void init() {}
}
