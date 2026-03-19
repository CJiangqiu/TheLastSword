package net.the_last_sword.entity;

import net.minecraft.nbt.CompoundTag;
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
import net.the_last_sword.entity.ai.GuardianArcherMaintainDistanceGoal;
import net.the_last_sword.entity.ai.GuardianAssistAllyTargetGoal;
import net.the_last_sword.entity.ai.GuardianRangedAttackGoal;
import org.jetbrains.annotations.Nullable;

// 封印尖塔守卫 - 弓箭手变种
public class GuardianArcherEntity extends GuardianOfSealedSpireEntity {

    public GuardianArcherEntity(EntityType<? extends GuardianArcherEntity> type, Level world) {
        super(type, world);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.MAX_HEALTH, 50)
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
