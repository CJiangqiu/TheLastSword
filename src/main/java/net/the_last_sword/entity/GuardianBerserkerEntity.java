package net.the_last_sword.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
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
import org.jetbrains.annotations.Nullable;

// 封印尖塔守卫 - 狂战士变种
public class GuardianBerserkerEntity extends GuardianOfSealedSpireEntity {

    private static final float LIFESTEAL_RATIO = 0.05F;

    public GuardianBerserkerEntity(EntityType<? extends GuardianBerserkerEntity> type, Level world) {
        super(type, world);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.20)
                .add(Attributes.MAX_HEALTH, 80)
                .add(Attributes.ARMOR, 2)
                .add(Attributes.ATTACK_DAMAGE, 4)
                .add(Attributes.FOLLOW_RANGE, 32);
    }

    @Override
    public GuardianType getGuardianType() {
        return GuardianType.BERSERKER;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty,
            MobSpawnType reason, @Nullable SpawnGroupData livingdata, @Nullable CompoundTag tag) {
        SpawnGroupData result = super.finalizeSpawn(world, difficulty, reason, livingdata, tag);

        ItemStack axe = new ItemStack(Items.NETHERITE_AXE);
        axe.enchant(Enchantments.SHARPNESS, 5);
        axe.enchant(Enchantments.BLOCK_FORTUNE, 3);
        axe.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
        axe.enchant(Enchantments.UNBREAKING, 3);
        axe.enchant(Enchantments.MENDING, 1);

        this.setItemSlot(EquipmentSlot.MAINHAND, axe);
        this.setDropChance(EquipmentSlot.MAINHAND, 2.0F);

        return result;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean result = super.doHurtTarget(target);

        if (result && target instanceof LivingEntity) {
            float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
            float healAmount = damage * LIFESTEAL_RATIO;

            if (healAmount > 0) {
                float currentHealth = getWorldAnchor();
                float maxHealth = getWorldAnchorMax();
                float newHealth = Math.min(currentHealth + healAmount, maxHealth);
                setWorldAnchor(newHealth);
            }
        }

        return result;
    }
}
