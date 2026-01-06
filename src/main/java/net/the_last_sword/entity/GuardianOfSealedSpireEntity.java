package net.the_last_sword.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.the_last_sword.entity.ai.GuardianOfSealedSpireAI;
import net.the_last_sword.entity.ai.TheLastEndAI;
import net.the_last_sword.init.ModEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//封印尖塔守卫
public class GuardianOfSealedSpireEntity extends TheLastEndEntity {

    //守卫变种类型
    public enum GuardianType {
        SABER,      //剑士
        ARCHER,     //弓箭手
        BERSERKER   //狂战士
    }

    //守卫类型字段
    private static final EntityDataAccessor<Integer> GUARDIAN_TYPE =
            SynchedEntityData.defineId(GuardianOfSealedSpireEntity.class, EntityDataSerializers.INT);

    public GuardianOfSealedSpireEntity(EntityType<? extends GuardianOfSealedSpireEntity> type, Level world) {
        super(type, world);
        setMaxUpStep(0.6f);
        xpReward = 10;
        setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(GUARDIAN_TYPE, 0);  //默认剑士
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new FloatGoal(this));
    }

    //获取守卫类型
    public GuardianType getGuardianType() {
        int typeId = this.entityData.get(GUARDIAN_TYPE);
        return GuardianType.values()[Math.min(typeId, GuardianType.values().length - 1)];
    }

    //设置守卫类型
    public void setGuardianType(GuardianType type) {
        this.entityData.set(GUARDIAN_TYPE, type.ordinal());
    }

    //创建AI实例
    @Override
    public TheLastEndAI createAI() {
        return new GuardianOfSealedSpireAI(this);
    }

    //动画名称
    @Override
    public String getIdleAnimationName() {
        return "idle";
    }

    @Override
    public String getMovementAnimationName() {
        return "walk";
    }

    @Override
    public String getDeathAnimationName() {
        return "death";
    }

    @Override
    public String getSpawnAnimationName() {
        return "spawn";  //守卫没有生成动画，但必须实现此方法
    }

    //属性
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.MAX_HEALTH, 50)
                .add(Attributes.ARMOR, 0)
                .add(Attributes.ATTACK_DAMAGE, 2)
                .add(Attributes.FOLLOW_RANGE, 32);
    }

    //声音
    @Override
    public SoundEvent getAmbientSound() {
        return SoundEvents.SKELETON_AMBIENT;
    }

    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return SoundEvents.SKELETON_HURT;
    }

    @Override
    public SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_DEATH;
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

    //生成时初始化（仅服务端执行）
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty,
            MobSpawnType reason, @Nullable SpawnGroupData livingdata, @Nullable CompoundTag tag) {
        SpawnGroupData result = super.finalizeSpawn(world, difficulty, reason, livingdata, tag);

        //仅在服务端初始化（客户端会通过同步获取数据）
        if (!level().isClientSide) {
            //初始化血量
            float maxHealth = (float) getAttributeValue(Attributes.MAX_HEALTH);
            setTheLastEndMaxHealth(maxHealth);
            setTheLastEndHealth(maxHealth);

            //初始化等级为1（守卫固定1级）
            setTheLastEndLevel(1);

            //守卫没有生成动画，直接标记为已生成
            setSpawned(true);

            //装备满附魔下界合金甲
            equipNetheriteArmor();
        }

        return result;
    }

    //装备满附魔下界合金甲
    private void equipNetheriteArmor() {
        //头盔：保护4 + 荆棘3 + 耐久3 + 经验修补 + 水下呼吸3 + 水下速掘 + 弹射物保护4
        ItemStack helmet = new ItemStack(Items.NETHERITE_HELMET);
        helmet.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        helmet.enchant(Enchantments.THORNS, 3);
        helmet.enchant(Enchantments.UNBREAKING, 3);
        helmet.enchant(Enchantments.MENDING, 1);
        helmet.enchant(Enchantments.RESPIRATION, 3);
        helmet.enchant(Enchantments.AQUA_AFFINITY, 1);
        helmet.enchant(Enchantments.PROJECTILE_PROTECTION, 4);
        setItemSlot(EquipmentSlot.HEAD, helmet);
        setDropChance(EquipmentSlot.HEAD, 2.0F);  //100%掉落（>1.0F保证掉落）

        //胸甲：保护4 + 荆棘3 + 耐久3 + 经验修补 + 爆炸保护4
        ItemStack chestplate = new ItemStack(Items.NETHERITE_CHESTPLATE);
        chestplate.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        chestplate.enchant(Enchantments.THORNS, 3);
        chestplate.enchant(Enchantments.UNBREAKING, 3);
        chestplate.enchant(Enchantments.MENDING, 1);
        chestplate.enchant(Enchantments.BLAST_PROTECTION, 4);
        setItemSlot(EquipmentSlot.CHEST, chestplate);
        setDropChance(EquipmentSlot.CHEST, 2.0F);  //100%掉落（>1.0F保证掉落）

        //护腿：保护4 + 荆棘3 + 耐久3 + 经验修补 + 火焰保护4 + 迅捷潜行3
        ItemStack leggings = new ItemStack(Items.NETHERITE_LEGGINGS);
        leggings.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        leggings.enchant(Enchantments.THORNS, 3);
        leggings.enchant(Enchantments.UNBREAKING, 3);
        leggings.enchant(Enchantments.MENDING, 1);
        leggings.enchant(Enchantments.FIRE_PROTECTION, 4);
        leggings.enchant(Enchantments.SWIFT_SNEAK, 3);
        setItemSlot(EquipmentSlot.LEGS, leggings);
        setDropChance(EquipmentSlot.LEGS, 2.0F);  //100%掉落（>1.0F保证掉落）

        //靴子：保护4 + 荆棘3 + 耐久3 + 经验修补 + 深海探索者3 + 摔落保护4 + 灵魂疾行3
        ItemStack boots = new ItemStack(Items.NETHERITE_BOOTS);
        boots.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        boots.enchant(Enchantments.THORNS, 3);
        boots.enchant(Enchantments.UNBREAKING, 3);
        boots.enchant(Enchantments.MENDING, 1);
        boots.enchant(Enchantments.DEPTH_STRIDER, 3);
        boots.enchant(Enchantments.FALL_PROTECTION, 4);
        boots.enchant(Enchantments.SOUL_SPEED, 3);
        setItemSlot(EquipmentSlot.FEET, boots);
        setDropChance(EquipmentSlot.FEET, 2.0F);  //100%掉落（>1.0F保证掉落）
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return dimensions.height * 0.85f;
    }
    //NBT
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("GuardianType")) {
            int typeId = tag.getInt("GuardianType");
            this.entityData.set(GUARDIAN_TYPE, typeId);
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("GuardianType", this.entityData.get(GUARDIAN_TYPE));
    }

    @Override
    public void tick() {
        super.tick();

        //物品转换检测（每20tick检测一次）
        if (tickCount % 20 == 0) {
            checkAndTransform();
        }

        this.refreshDimensions();
    }

    //检测主手物品并转换类型
    protected void checkAndTransform() {
        //只有基础守卫可以转换，子类不能切换变种
        if (this.getClass() != GuardianOfSealedSpireEntity.class) {
            return;
        }

        ItemStack mainhand = getMainHandItem();
        if (mainhand.isEmpty()) return;

        GuardianType targetType = getTypeFromItem(mainhand);
        if (targetType == null || targetType == getGuardianType()) return;

        //执行转换
        transformTo(targetType);
    }

    //根据物品判断目标类型
    private GuardianType getTypeFromItem(ItemStack stack) {
        if (stack.getItem() instanceof SwordItem) {
            return GuardianType.SABER;
        } else if (stack.getItem() instanceof BowItem) {
            return GuardianType.ARCHER;
        } else if (stack.getItem() instanceof AxeItem) {
            return GuardianType.BERSERKER;
        }
        return null;
    }

    //转换为指定类型
    private void transformTo(GuardianType type) {
        //只在服务端执行
        if (level().isClientSide) {
            return;
        }

        //创建新实体
        GuardianOfSealedSpireEntity newEntity = null;
        switch (type) {
            case SABER:
                newEntity = new GuardianSaberEntity(ModEntities.GUARDIAN_SABER.get(), level());
                break;
            case ARCHER:
                newEntity = new GuardianArcherEntity(ModEntities.GUARDIAN_ARCHER.get(), level());
                break;
            case BERSERKER:
                newEntity = new GuardianBerserkerEntity(ModEntities.GUARDIAN_BERSERKER.get(), level());
                break;
        }

        if (newEntity == null) {
            return;
        }

        //复制位置和旋转
        newEntity.moveTo(getX(), getY(), getZ(), getYRot(), getXRot());

        //调用 finalizeSpawn 初始化实体（满血 + 满附魔武器）
        if (level() instanceof ServerLevel serverLevel) {
            newEntity.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(blockPosition()),
                MobSpawnType.CONVERSION, null, null);
        }

        //复制目标
        if (getTarget() != null) {
            newEntity.setTarget(getTarget());
        }

        //清除原实体的主手物品，避免掉落
        setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);

        //生成新实体
        level().addFreshEntity(newEntity);

        //移除当前实体
        this.safeRemove();
    }

    //友军伤害检测
    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (source.getEntity() instanceof GuardianOfSealedSpireEntity) {
            return false;
        }
        return super.hurt(source, amount);
    }

}
