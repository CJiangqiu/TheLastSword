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
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

//封印尖塔守卫 - 剑士变种
public class GuardianSaberEntity extends GuardianOfSealedSpireEntity {

    public GuardianSaberEntity(EntityType<? extends GuardianSaberEntity> type, Level world) {
        super(type, world);
    }

    //属性
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.30)  //基础0.25 + 0.05
                .add(Attributes.MAX_HEALTH, 100)       //血量翻倍
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

        //创建满附魔下界合金剑
        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
        sword.enchant(Enchantments.SHARPNESS, 5);           //锋利V
        sword.enchant(Enchantments.SWEEPING_EDGE, 3);       //横扫之刃III
        sword.enchant(Enchantments.MOB_LOOTING, 3);         //抢夺III
        sword.enchant(Enchantments.KNOCKBACK, 2);           //击退II
        sword.enchant(Enchantments.UNBREAKING, 3);          //耐久III
        sword.enchant(Enchantments.MENDING, 1);             //经验修补I

        this.setItemSlot(EquipmentSlot.MAINHAND, sword);
        this.setDropChance(EquipmentSlot.MAINHAND, 2.0F);   //100%掉落（>1.0F保证掉落）

        return result;
    }
}
