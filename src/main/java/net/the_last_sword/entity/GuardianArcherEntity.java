package net.the_last_sword.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.ai.GuardianArcherMaintainDistanceGoal;
import net.the_last_sword.entity.ai.GuardianArcherTeleportGoal;
import net.the_last_sword.entity.ai.GuardianAssistAllyTargetGoal;
import net.the_last_sword.entity.ai.GuardianRangedAttackGoal;
import net.the_last_sword.util.EntityUtil;
import org.jetbrains.annotations.Nullable;

// 封印尖塔守卫 - 弓箭手变种
public class GuardianArcherEntity extends GuardianOfSealedSpireEntity {

    //瞬移是否就绪，同步到客户端用于决定是否显示粒子
    private static final EntityDataAccessor<Boolean> TELEPORT_READY =
            SynchedEntityData.defineId(GuardianArcherEntity.class, EntityDataSerializers.BOOLEAN);

    private long teleportCooldownEnd;

    public GuardianArcherEntity(EntityType<? extends GuardianArcherEntity> type, Level world) {
        super(type, world);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TELEPORT_READY, true);
    }

    public boolean isTeleportReady() {
        return this.entityData.get(TELEPORT_READY);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            if (isTeleportReady()) {
                spawnTeleportReadyParticles();
            }
            return;
        }

        //只在就绪状态翻转时写同步数据，避免每tick发包
        boolean ready = level().getGameTime() >= teleportCooldownEnd;
        if (ready != isTeleportReady()) {
            this.entityData.set(TELEPORT_READY, ready);
        }
    }

    //技能就绪时脚部持续冒末影人粒子
    private void spawnTeleportReadyParticles() {
        for (int i = 0; i < 2; i++) {
            level().addParticle(ParticleTypes.PORTAL,
                    getRandomX(0.6), getY() + random.nextDouble() * 0.3, getRandomZ(0.6),
                    (random.nextDouble() - 0.5) * 0.4, random.nextDouble() * 0.2, (random.nextDouble() - 0.5) * 0.4);
        }
    }

    //向随机安全落点瞬移，成功则进入冷却
    public boolean teleportAway() {
        double range = TheLastSwordConfiguration.getGuardianArcherTeleportRangeSafely();
        double x = getX() + (random.nextDouble() - 0.5) * 2.0 * range;
        double y = getY() + (random.nextInt((int) range) - range / 2.0);
        double z = getZ() + (random.nextDouble() - 0.5) * 2.0 * range;

        Vec3 teleportPosition = EntityUtil.findSafeTeleportPosition(
            this, new Vec3(x, y, z), Math.max(4, (int) Math.ceil(range)));
        if (teleportPosition == null) {
            return false;
        }

        //原地先响一次，让近身的目标听得到
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.ENDERMAN_TELEPORT, getSoundSource(), 1.0F, 1.0F);

        if (!EntityUtil.theLastEndTeleport(
                this, teleportPosition.x, teleportPosition.y, teleportPosition.z)) {
            return false;
        }

        playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        teleportCooldownEnd = level().getGameTime()
                + TheLastSwordConfiguration.getGuardianArcherTeleportCooldownSafely();
        this.entityData.set(TELEPORT_READY, false);
        return true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.MAX_HEALTH, 100)
                .add(Attributes.ARMOR, 0)
                .add(Attributes.ATTACK_DAMAGE, 2)
                .add(Attributes.FOLLOW_RANGE, 32);
    }

    @Override
    public GuardianType getGuardianType() {
        return GuardianType.ARCHER;
    }

    @Override
    public String getSkillAnimationName(int attackState) {
        if (attackState == STATE_ATTACK) {
            return "shoot";
        }
        return super.getSkillAnimationName(attackState);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new GuardianArcherTeleportGoal(this));
        this.goalSelector.addGoal(1, new GuardianRangedAttackGoal(this));
        this.goalSelector.addGoal(2, new GuardianArcherMaintainDistanceGoal(this));
        this.goalSelector.addGoal(5, new FloatGoal(this));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(GuardianOfSealedSpireEntity.class));
        this.targetSelector.addGoal(2, new GuardianAssistAllyTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty,
            MobSpawnType reason, @Nullable SpawnGroupData livingdata, @Nullable CompoundTag tag) {
        SpawnGroupData result = super.finalizeSpawn(world, difficulty, reason, livingdata, tag);

        ItemStack bow = new ItemStack(Items.BOW);
        bow.enchant(Enchantments.POWER_ARROWS, 5);
        bow.enchant(Enchantments.PUNCH_ARROWS, 2);
        bow.enchant(Enchantments.INFINITY_ARROWS, 1);
        bow.enchant(Enchantments.FLAMING_ARROWS, 1);
        bow.enchant(Enchantments.UNBREAKING, 3);
        bow.enchant(Enchantments.MENDING, 1);

        this.setItemSlot(EquipmentSlot.MAINHAND, bow);
        this.setDropChance(EquipmentSlot.MAINHAND, 2.0F);

        return result;
    }
}
