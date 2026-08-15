package net.the_last_sword.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.common.ToolActions;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.ai.GuardianSaberBlockGoal;
import org.jetbrains.annotations.Nullable;

// 封印尖塔守卫 - 剑士变种
public class GuardianSaberEntity extends GuardianOfSealedSpireEntity {

    public static final int STATE_BLOCK = 4;

    private long shieldDisabledUntil;
    private boolean blockRequested;

    public GuardianSaberEntity(EntityType<? extends GuardianSaberEntity> type, Level world) {
        super(type, world);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new GuardianSaberBlockGoal(this));
    }

    @Override
    public String getSkillAnimationName(int attackState) {
        if (attackState == STATE_BLOCK) {
            return "block";
        }
        return super.getSkillAnimationName(attackState);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.MAX_HEALTH, 200)
                .add(Attributes.ARMOR, 0)
                .add(Attributes.ATTACK_DAMAGE, 2)
                .add(Attributes.FOLLOW_RANGE, 32);
    }

    @Override
    public GuardianType getGuardianType() {
        return GuardianType.SABER;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty,
            MobSpawnType reason, @Nullable SpawnGroupData livingdata, @Nullable CompoundTag tag) {
        SpawnGroupData result = super.finalizeSpawn(world, difficulty, reason, livingdata, tag);

        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
        sword.enchant(Enchantments.SHARPNESS, 5);
        sword.enchant(Enchantments.SWEEPING_EDGE, 3);
        sword.enchant(Enchantments.MOB_LOOTING, 3);
        sword.enchant(Enchantments.KNOCKBACK, 2);
        sword.enchant(Enchantments.UNBREAKING, 3);
        sword.enchant(Enchantments.MENDING, 1);

        this.setItemSlot(EquipmentSlot.MAINHAND, sword);
        this.setDropChance(EquipmentSlot.MAINHAND, 2.0F);

        this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
        this.setDropChance(EquipmentSlot.OFFHAND, 0.0F);

        return result;
    }

    public boolean isShieldDisabled() {
        return level().getGameTime() < shieldDisabledUntil;
    }

    public boolean hasBlockRequest() {
        return blockRequested;
    }

    public void requestBlock() {
        blockRequested = true;
    }

    public void consumeBlockRequest() {
        blockRequested = false;
    }

    //成功受伤后请求格挡；由高优先级Goal在下一次AI调度时进入block
    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !level().isClientSide) {
            requestBlock();
        }
        return hurt;
    }

    //沿用原版玩家逻辑：支持斧及其他声明可破盾的物品
    @Override
    protected void blockUsingShield(LivingEntity attacker) {
        super.blockUsingShield(attacker);
        ItemStack shield = getUseItem();
        if (attacker.getMainHandItem().canDisableShield(shield, this, attacker)) {
            shieldDisabledUntil = level().getGameTime()
                    + TheLastSwordConfiguration.getGuardianSaberShieldDisableTimeSafely();
            stopUsingItem();
            if (getAnimationState() == STATE_BLOCK) {
                setAnimationState(STATE_IDLE);
            }
            level().broadcastEntityEvent(this, (byte) 30);
        }
    }

    //LivingEntity默认不会损耗非玩家实体的盾牌，这里补齐原版玩家的耐久逻辑
    @Override
    protected void hurtCurrentlyUsedShield(float damage) {
        ItemStack shield = getUseItem();
        if (!shield.canPerformAction(ToolActions.SHIELD_BLOCK) || damage < 3.0F) {
            return;
        }

        int durabilityDamage = 1 + Mth.floor(damage);
        InteractionHand usedHand = getUsedItemHand();
        shield.hurtAndBreak(durabilityDamage, this, broken -> broken.broadcastBreakEvent(usedHand));

        if (shield.isEmpty()) {
            setItemSlot(usedHand == InteractionHand.MAIN_HAND
                    ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            stopUsingItem();
            playSound(SoundEvents.SHIELD_BREAK, 0.8F, 0.8F + level().random.nextFloat() * 0.4F);
            if (getAnimationState() == STATE_BLOCK) {
                setAnimationState(STATE_IDLE);
            }
        }
    }
}
