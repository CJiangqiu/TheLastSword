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
import net.the_last_sword.entity.ai.GuardianArcherAI;
import net.the_last_sword.entity.ai.TheLastEndAI;
import org.jetbrains.annotations.Nullable;

//封印尖塔守卫 - 弓箭手变种
public class GuardianArcherEntity extends GuardianOfSealedSpireEntity {

    public GuardianArcherEntity(EntityType<? extends GuardianArcherEntity> type, Level world) {
        super(type, world);
    }

    //属性
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.30)  //基础0.25 + 0.05
                .add(Attributes.MAX_HEALTH, 50)        //血量不变
                .add(Attributes.ARMOR, 0)
                .add(Attributes.ATTACK_DAMAGE, 2)
                .add(Attributes.FOLLOW_RANGE, 32);
    }

    @Override
    public GuardianType getGuardianType() {
        return GuardianType.ARCHER;
    }

    //创建弓箭手专属AI
    @Override
    public TheLastEndAI createAI() {
        return new GuardianArcherAI(this);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty,
            MobSpawnType reason, @Nullable SpawnGroupData livingdata, @Nullable CompoundTag tag) {
        SpawnGroupData result = super.finalizeSpawn(world, difficulty, reason, livingdata, tag);

        //创建满附魔弓
        ItemStack bow = new ItemStack(Items.BOW);
        bow.enchant(Enchantments.POWER_ARROWS, 5);      //力量V
        bow.enchant(Enchantments.PUNCH_ARROWS, 2);      //冲击II
        bow.enchant(Enchantments.INFINITY_ARROWS, 1);   //无限I
        bow.enchant(Enchantments.FLAMING_ARROWS, 1);    //火矢I
        bow.enchant(Enchantments.UNBREAKING, 3);        //耐久III
        bow.enchant(Enchantments.MENDING, 1);           //经验修补I

        this.setItemSlot(EquipmentSlot.MAINHAND, bow);
        this.setDropChance(EquipmentSlot.MAINHAND, 2.0F);  //100%掉落（>1.0F保证掉落）

        return result;
    }
}
